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

import com.alibaba.fescar.core.protocol.AbstractMessage;
import com.alibaba.fescar.core.rpc.RpcContext;

/**
 * The type Branch commit request.
 */
public class BranchCommitRequest extends AbstractBranchEndRequest {

    @Override
    public short getTypeCode() {
        return AbstractMessage.TYPE_BRANCH_COMMIT;
    }

    @Override
    public AbstractTransactionResponse handle(RpcContext rpcContext) {
        return handler.handle(this);
    }
}