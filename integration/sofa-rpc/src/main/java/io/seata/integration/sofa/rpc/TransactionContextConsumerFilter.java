/*
 *  Copyright 1999-2019 Seata.io Group.
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
package io.seata.integration.sofa.rpc;

import com.alipay.sofa.rpc.context.RpcInternalContext;
import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.ext.Extension;
import com.alipay.sofa.rpc.filter.AutoActive;
import com.alipay.sofa.rpc.filter.Filter;
import com.alipay.sofa.rpc.filter.FilterInvoker;
import io.seata.common.util.StringUtils;
import io.seata.core.context.RootContext;
import io.seata.core.model.BranchType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TransactionContext on consumer side.
 * 
 * @author Geng Zhang
 * @since 0.6.0
 */
@Extension(value = "transactionContextConsumer")
@AutoActive(consumerSide = true)
public class TransactionContextConsumerFilter extends Filter {

    /**
     * Logger for this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionContextConsumerFilter.class);

    @Override
    public SofaResponse invoke(FilterInvoker filterInvoker, SofaRequest sofaRequest) throws SofaRpcException {
        String xid = RootContext.getXID();
        String rpcXid = getRpcXid();
        BranchType branchType = RootContext.getBranchType();
        String rpcBranchType = getBranchType();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("context in RootContext[{},{}], context in RpcContext[{},{}]", xid, branchType, rpcXid, rpcBranchType);
        }
        boolean bind = false;
        if (xid != null) {
            sofaRequest.addRequestProp(RootContext.KEY_XID, xid);
            sofaRequest.addRequestProp(RootContext.KEY_BRANCH_TYPE, branchType.name());
        } else {
            if (rpcXid != null) {
                RootContext.bind(rpcXid);
                if (StringUtils.equals(BranchType.TCC.name(), rpcBranchType)) {
                    RootContext.bindBranchType(BranchType.TCC);
                }
                bind = true;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("bind[{}] to RootContext",rpcXid);
                }
            }
        }
        try {
            return filterInvoker.invoke(sofaRequest);
        } finally {
            if (bind) {
                String unbindXid = RootContext.unbind();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("unbind[{}] from RootContext", unbindXid);
                }
                BranchType previousBranchType = RootContext.getBranchType();
                if (BranchType.TCC == previousBranchType) {
                    RootContext.unbindBranchType();
                }
                if (!rpcXid.equalsIgnoreCase(unbindXid)) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("xid in change during RPC from [{}] to [{}]", rpcXid,unbindXid);
                    }
                    if (unbindXid != null) {
                        RootContext.bind(unbindXid);
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("bind [{}] back to RootContext", unbindXid);
                        }
                        if (BranchType.TCC == previousBranchType) {
                            RootContext.bindBranchType(BranchType.TCC);
                            LOGGER.warn("bind branchType [{}] back to RootContext", previousBranchType);
                        }
                    }
                }
            }
        }
    }

    /**
     * get rpc xid
     * @return
     */
    private String getRpcXid() {
        String rpcXid = (String) RpcInternalContext.getContext().getAttachment(RootContext.HIDDEN_KEY_XID);
        if (rpcXid == null) {
            rpcXid = (String) RpcInternalContext.getContext().getAttachment(RootContext.HIDDEN_KEY_XID.toLowerCase());
        }
        return rpcXid;
    }
    private String getBranchType() {
        return (String) RpcInternalContext.getContext().getAttachment(RootContext.HIDDEN_KEY_BRANCH_TYPE);
    }

}
