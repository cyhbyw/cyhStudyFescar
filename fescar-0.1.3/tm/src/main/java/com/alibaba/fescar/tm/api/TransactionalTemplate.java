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

package com.alibaba.fescar.tm.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fescar.core.exception.TransactionException;

/**
 * Template of executing business logic with a global transaction.
 */
public class TransactionalTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionalTemplate.class);

    public Object execute(TransactionalExecutor business) throws TransactionalExecutor.ExecutionException {

        // 1. get or create a transaction
        GlobalTransaction tx = GlobalTransactionContext.getCurrentOrCreate();

        // 2. begin transaction
        try {
            LOGGER.debug("开始全局事务--开始");
            tx.begin(business.timeout(), business.name());
            LOGGER.debug("开始全局事务--成功");
        } catch (TransactionException txe) {
            LOGGER.debug("开始全局事务--异常", txe);
            throw new TransactionalExecutor.ExecutionException(tx, txe, TransactionalExecutor.Code.BeginFailure);
        }

        Object rs = null;
        try {
            // Do Your Business
            LOGGER.debug("执行业务逻辑--开始");
            rs = business.execute();
            LOGGER.debug("执行业务逻辑--成功");
        } catch (Throwable ex) {
            LOGGER.debug("执行业务逻辑--异常，将进行回滚", ex);
            // 3. any business exception, rollback.
            try {
                tx.rollback();
                // 3.1 Successfully rolled back
                LOGGER.debug("执行业务逻辑--异常，回滚成功");
                throw new TransactionalExecutor.ExecutionException(tx, TransactionalExecutor.Code.RollbackDone, ex);
            } catch (TransactionException txe) {
                // 3.2 Failed to rollback
                LOGGER.debug("执行业务逻辑--异常，回滚失败");
                throw new TransactionalExecutor.ExecutionException(tx, txe, TransactionalExecutor.Code.RollbackFailure,
                        ex);
            } finally {
                GlobalTransactionContext.clean();
            }
        }

        // 4. everything is fine, commit.
        try {
            LOGGER.debug("提交全局事务--开始");
            tx.commit();
            LOGGER.debug("提交全局事务--成功");
        } catch (TransactionException txe) {
            LOGGER.debug("提交全局事务--异常", txe);
            // 4.1 Failed to commit
            throw new TransactionalExecutor.ExecutionException(tx, txe, TransactionalExecutor.Code.CommitFailure);
        } finally {
            GlobalTransactionContext.clean();
        }
        return rs;
    }

}
