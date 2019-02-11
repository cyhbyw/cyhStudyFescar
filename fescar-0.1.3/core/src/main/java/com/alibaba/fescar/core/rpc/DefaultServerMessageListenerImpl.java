/*
 *  Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.alibaba.fescar.core.rpc;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fescar.common.thread.NamedThreadFactory;
import com.alibaba.fescar.common.util.NetUtil;
import com.alibaba.fescar.core.protocol.AbstractMessage;
import com.alibaba.fescar.core.protocol.AbstractResultMessage;
import com.alibaba.fescar.core.protocol.HeartbeatMessage;
import com.alibaba.fescar.core.protocol.MergeResultMessage;
import com.alibaba.fescar.core.protocol.MergedWarpMessage;
import com.alibaba.fescar.core.protocol.RegisterRMRequest;
import com.alibaba.fescar.core.protocol.RegisterRMResponse;
import com.alibaba.fescar.core.protocol.RegisterTMRequest;
import com.alibaba.fescar.core.protocol.RegisterTMResponse;
import com.alibaba.fescar.core.protocol.Version;
import com.alibaba.fescar.core.rpc.netty.RegisterCheckAuthHandler;

import io.netty.channel.ChannelHandlerContext;

/**
 * The type Default server message listener.
 *
 * @Author: jimin.jm @alibaba-inc.com
 * @Project: fescar -all
 * @DateTime: 2018 /10/18 14:31
 * @FileName: DefaultServerMessageListenerImpl
 * @Description:
 */
public class DefaultServerMessageListenerImpl implements ServerMessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServerMessageListenerImpl.class);
    private static BlockingQueue<String> messageStrings = new LinkedBlockingQueue<>();
    private ServerMessageSender serverMessageSender;
    private final TransactionMessageHandler transactionMessageHandler;
    private static final int MAX_LOG_SEND_THREAD = 1;
    private static final long KEEP_ALIVE_TIME = 0L;
    private static final String THREAD_PREFIX = "batchLoggerPrint";
    private static final long IDLE_CHECK_MILLS = 3L;
    private static final String BATCH_LOG_SPLIT = "\n";

    public DefaultServerMessageListenerImpl(TransactionMessageHandler transactionMessageHandler) {
        this.transactionMessageHandler = transactionMessageHandler;
    }

    @Override
    public void onTrxMessage(long msgId, ChannelHandlerContext ctx, Object message, ServerMessageSender sender) {
        RpcContext rpcContext = ChannelManager.getContextFromIdentified(ctx.channel());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("server received message: " + message + ", clientIp: "
                    + NetUtil.toIpAddress(ctx.channel().remoteAddress()) + ", vgroup: "
                    + rpcContext.getTransactionServiceGroup());
        } else {
            messageStrings.offer(message + ", clientIp: " + NetUtil.toIpAddress(ctx.channel().remoteAddress())
                    + ", vgroup: " + rpcContext.getTransactionServiceGroup());
        }
        if (!(message instanceof AbstractMessage)) {
            return;
        }
        if (message instanceof MergedWarpMessage) {
            AbstractResultMessage[] results = new AbstractResultMessage[((MergedWarpMessage) message).msgs.size()];
            for (int i = 0; i < results.length; i++) {
                final AbstractMessage subMessage = ((MergedWarpMessage) message).msgs.get(i);
                results[i] = transactionMessageHandler.onRequest(subMessage, rpcContext);
            }
            MergeResultMessage resultMessage = new MergeResultMessage();
            resultMessage.setMsgs(results);
            sender.sendResponse(msgId, ctx.channel(), resultMessage);
        } else if (message instanceof AbstractResultMessage) {
            transactionMessageHandler.onResponse((AbstractResultMessage) message, rpcContext);
        }
    }

    @Override
    public void onRegRmMessage(long msgId, ChannelHandlerContext ctx, RegisterRMRequest message,
            ServerMessageSender sender, RegisterCheckAuthHandler checkAuthHandler) {

        boolean isSuccess = false;
        try {
            if (null == checkAuthHandler || checkAuthHandler.regResourceManagerCheckAuth(message)) {
                ChannelManager.registerRMChannel(message, ctx.channel());
                Version.putChannelVersion(ctx.channel(), message.getVersion());
                isSuccess = true;
            }
        } catch (Exception exx) {
            isSuccess = false;
            LOGGER.error(exx.getMessage());
        }
        sender.sendResponse(msgId, ctx.channel(), new RegisterRMResponse(isSuccess));
        LOGGER.info("RM_register_success. message: " + message + ", channel: " + ctx.channel());
    }

    @Override
    public void onRegTmMessage(long msgId, ChannelHandlerContext ctx, RegisterTMRequest message,
            ServerMessageSender sender, RegisterCheckAuthHandler checkAuthHandler) {
        String ipAndPort = NetUtil.toStringAddress(ctx.channel().remoteAddress());
        Version.putChannelVersion(ctx.channel(), message.getVersion());
        boolean isSuccess = false;
        try {
            if (null == checkAuthHandler || checkAuthHandler.regTransactionManagerCheckAuth(message)) {
                ChannelManager.registerTMChannel(message, ctx.channel());
                Version.putChannelVersion(ctx.channel(), message.getVersion());
                isSuccess = true;
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(String.format("checkAuth for client:%s vgroup:%s ok", ipAndPort,
                            message.getTransactionServiceGroup()));
                }
            }
        } catch (Exception exx) {
            isSuccess = false;
            LOGGER.error(exx.getMessage());
        }
        sender.sendResponse(msgId, ctx.channel(), new RegisterTMResponse(isSuccess));
    }

    @Override
    public void onCheckMessage(long msgId, ChannelHandlerContext ctx, ServerMessageSender sender) {
        try {
            sender.sendResponse(msgId, ctx.channel(), HeartbeatMessage.PONG);
        } catch (Throwable throwable) {
            LOGGER.error("", "send response error", throwable);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("received PING from " + ctx.channel().remoteAddress());
        }
    }

    /**
     * Init.
     */
    public void init() {
        ExecutorService mergeSendExecutorService = new ThreadPoolExecutor(MAX_LOG_SEND_THREAD, MAX_LOG_SEND_THREAD,
                KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                new NamedThreadFactory(THREAD_PREFIX, MAX_LOG_SEND_THREAD, true));
        mergeSendExecutorService.submit(new BatchLogRunnable());
    }

    public ServerMessageSender getServerMessageSender() {
        return serverMessageSender;
    }

    public void setServerMessageSender(ServerMessageSender serverMessageSender) {
        this.serverMessageSender = serverMessageSender;
    }

    /**
     * The type Batch log runnable.
     */
    class BatchLogRunnable implements Runnable {

        @Override
        public void run() {
            while (true) {
                if (messageStrings.size() > 0) {
                    StringBuilder builder = new StringBuilder();
                    while (!messageStrings.isEmpty()) {
                        builder.append(messageStrings.poll()).append(BATCH_LOG_SPLIT);
                    }
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info(builder.toString());
                    }
                }
                try {
                    Thread.sleep(IDLE_CHECK_MILLS);
                } catch (InterruptedException exx) {
                    LOGGER.error(exx.getMessage());
                }
            }
        }
    }

}
