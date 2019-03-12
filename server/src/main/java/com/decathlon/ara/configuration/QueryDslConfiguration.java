package com.decathlon.ara.configuration;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.spring.SpringConnectionProvider;
import com.querydsl.sql.spring.SpringExceptionTranslator;
import java.sql.Connection;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryDslConfiguration {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    @Bean
    @SuppressWarnings("static-method")
    public com.querydsl.sql.Configuration querydslConfiguration() {
        SQLTemplates templates = MySQLTemplates.builder().build();
        com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(templates);
        configuration.setExceptionTranslator(new SpringExceptionTranslator());
        return configuration;
    }

    @Bean
    public SQLQueryFactory sqlQueryFactory(DataSource dataSource) {
        Provider<Connection> provider = new SpringConnectionProvider(dataSource);
        return new SQLQueryFactory(querydslConfiguration(), provider);
    }

}
