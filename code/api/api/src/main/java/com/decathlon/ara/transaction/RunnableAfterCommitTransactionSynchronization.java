package com.decathlon.ara.transaction;

import org.springframework.transaction.support.TransactionSynchronization;

/**
 * {@link Runnable} and {@link TransactionSynchronization} extension that call {@link Runnable#run()} when {@link TransactionSynchronization#afterCommit()} is called
 * @author z15lross
 */
@FunctionalInterface
public interface RunnableAfterCommitTransactionSynchronization extends TransactionSynchronization, Runnable {

    @Override
    default void afterCommit() {
        run();
    }

}
