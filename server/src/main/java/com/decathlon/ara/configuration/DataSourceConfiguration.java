package com.decathlon.ara.configuration;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

/**
 * When starting ARA server at the same time as the database (with eg. Docker Compose), make sure the application waits
 * for a live database connection during a few seconds before failing to start up.
 */
@Configuration
public class DataSourceConfiguration {

    @Bean
    public BeanPostProcessor dataSourceWrapper() {
        return new RetryableDataSourceBeanPostProcessor();
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    private static class RetryableDataSourceBeanPostProcessor implements BeanPostProcessor {

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            if (bean instanceof DataSource) {
                bean = new RetryableDataSource((DataSource) bean);
            }
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            return bean;
        }

    }

    private static class RetryableDataSource extends AbstractDataSource {

        private DataSource delegate;

        RetryableDataSource(DataSource delegate) {
            this.delegate = delegate;
        }

        @Override
        @Retryable(maxAttempts = 45, backoff = @Backoff(delay = 1000))
        public Connection getConnection() throws SQLException {
            return delegate.getConnection();
        }

        @Override
        @Retryable(maxAttempts = 45, backoff = @Backoff(delay = 1000))
        public Connection getConnection(String username, String password) throws SQLException {
            return delegate.getConnection(username, password);
        }

    }

}
