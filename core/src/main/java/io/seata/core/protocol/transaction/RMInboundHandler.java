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
package io.seata.core.protocol.transaction;

/**
 * The interface Rm inbound handler.
 *
 * @author sharajava
 */
public interface RMInboundHandler {

    /**
     * Handle branch commit response.
     *
     * @param request the request
     * @return the branch commit response
     */
    BranchCommitResponse handle(BranchCommitRequest request);

    /**
     * Handle branch rollback response.
     *
     * @param request the request
     * @return the branch rollback response
     */
    BranchRollbackResponse handle(BranchRollbackRequest request);

    /**
     * Handle delete undo log .
     *
     * @param request the request
     */
    void handle(UndoLogDeleteRequest request);

}
