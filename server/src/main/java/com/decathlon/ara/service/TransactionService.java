package com.decathlon.ara.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Helper service for JPA transactions.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransactionService {

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
