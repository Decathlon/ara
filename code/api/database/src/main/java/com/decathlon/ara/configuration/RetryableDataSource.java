package com.decathlon.ara.configuration;

import org.springframework.jdbc.datasource.AbstractDataSource;
import javax.sql.DataSource;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import java.sql.Connection;
import java.sql.SQLException;

public class RetryableDataSource extends AbstractDataSource {

    private DataSource delegate;

    RetryableDataSource(DataSource delegate) {
        this.delegate = delegate;
    }

    @Override
    @Retryable(maxAttempts=10, backoff=@Backoff(multiplier=2.3, maxDelay=30000))
    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }

    @Override
    @Retryable(maxAttempts=10, backoff=@Backoff(multiplier=2.3, maxDelay=30000))
    public Connection getConnection(String username, String password) throws SQLException {
        return delegate.getConnection(username, password);
    }

}