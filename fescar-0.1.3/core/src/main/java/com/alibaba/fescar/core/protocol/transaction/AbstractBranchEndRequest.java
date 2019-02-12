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

package com.alibaba.fescar.core.protocol.transaction;

import java.nio.ByteBuffer;

import com.alibaba.fescar.core.model.BranchType;

import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * The type Abstract branch end request.
 */
@Data
public abstract class AbstractBranchEndRequest extends AbstractTransactionRequestToRM {

    private static final long serialVersionUID = 5083828939317068713L;

    /** The Xid. */
    protected String xid;

    /** The Branch id. */
    protected long branchId;

    /** The Branch type. */
    protected BranchType branchType = BranchType.AT;

    /** The Resource id. */
    protected String resourceId;

    /** The Application data. */
    protected String applicationData;


    @Override
    public byte[] encode() {
        byte[] applicationDataBytes = null;
        if (this.applicationData != null) {
            applicationDataBytes = applicationData.getBytes(UTF8);
            if (applicationDataBytes.length > 512) {
                byteBuffer = ByteBuffer.allocate(applicationDataBytes.length + 1024);
            }
        }

        // 1. xid
        if (this.xid != null) {
            byte[] bs = xid.getBytes(UTF8);
            byteBuffer.putShort((short) bs.length);
            if (bs.length > 0) {
                byteBuffer.put(bs);
            }
        } else {
            byteBuffer.putShort((short) 0);
        }
        // 2. Branch Id
        byteBuffer.putLong(this.branchId);
        // 3. Branch Type
        byteBuffer.put((byte) this.branchType.ordinal());
        // 4. Resource Id
        if (this.resourceId != null) {
            byte[] bs = resourceId.getBytes(UTF8);
            byteBuffer.putShort((short) bs.length);
            if (bs.length > 0) {
                byteBuffer.put(bs);
            }
        } else {
            byteBuffer.putShort((short) 0);
        }

        // 5. Application Data
        if (this.applicationData != null) {
            byteBuffer.putInt(applicationDataBytes.length);
            if (applicationDataBytes.length > 0) {
                byteBuffer.put(applicationDataBytes);
            }
        } else {
            byteBuffer.putInt(0);
        }

        byteBuffer.flip();
        byte[] content = new byte[byteBuffer.limit()];
        byteBuffer.get(content);
        return content;
    }

    @Override
    public boolean decode(ByteBuf in) {
        int leftLen = in.readableBytes();
        int read = 0;
        int xidLen = in.readShort();
        if (xidLen > 0) {
            if (leftLen < xidLen) {
                return false;
            }
            byte[] bs = new byte[xidLen];
            in.readBytes(bs);
            setXid(new String(bs, UTF8));
            leftLen -= xidLen;
        }
        this.branchId = in.readLong();
        leftLen -= 8;
        this.branchType = BranchType.get(in.readByte());
        leftLen--;

        int resourceIdLen = in.readShort();
        if (resourceIdLen > 0) {
            if (leftLen < resourceIdLen) {
                return false;
            }
            byte[] bs = new byte[resourceIdLen];
            in.readBytes(bs);
            setResourceId(new String(bs, UTF8));
            leftLen -= resourceIdLen;
        }

        int applicationDataLen = in.readInt();
        if (applicationDataLen > 0) {
            if (leftLen < applicationDataLen) {
                return false;
            }
            byte[] bs = new byte[applicationDataLen];
            in.readBytes(bs);
            setApplicationData(new String(bs, UTF8));
            leftLen -= applicationDataLen;
        }

        return true;
    }

    @Override
    public String toString() {
        return "AbstractBranchEndRequest{" + "xid='" + xid + '\'' + ", branchId=" + branchId + ", branchType="
                + branchType + ", resourceId='" + resourceId + '\'' + ", applicationData='" + applicationData + '\''
                + '}';
    }
}
