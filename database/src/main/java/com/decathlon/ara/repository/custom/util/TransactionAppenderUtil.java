/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

package com.decathlon.ara.repository.custom.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.transaction.Transactional;

/**
 * Helper service for JPA transactions.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransactionAppenderUtil {

    /**
     * Let Hibernate make the INSERT SQL statements now and make sure the commit was successful before executing the
     * given code. If the transaction is roll-backed, the code will of course not be executed.
     *
     * @param runnable the code to run after transaction commit
     */
    public void doAfterCommit(Runnable runnable) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            @SuppressWarnings("squid:S1604") // Anonymous inner classes containing only one method should become lambdas
            final TransactionSynchronization synchronization = new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    runnable.run();
                }
            };
            TransactionSynchronizationManager.registerSynchronization(synchronization);
        } else {
            log.error("Transaction synchronization is not active: doAfterCommit(Runnable) is skipped " +
                    "(OK for tests, but not for production environment!)");
        }
    }
}
