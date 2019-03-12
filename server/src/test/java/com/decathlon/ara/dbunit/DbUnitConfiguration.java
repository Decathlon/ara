package com.decathlon.ara.dbunit;

import com.github.springtestdbunit.bean.DatabaseConfigBean;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import javax.sql.DataSource;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.ext.mysql.MySqlMetadataHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class DbUnitConfiguration {

    @Value("${spring.datasource.driver-class-name}")
    private String driver;

    @Bean
    public DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection(final DataSource dataSource) {
        DatabaseConfigBean databaseConfig = new DatabaseConfigBean();
        if (driver.toLowerCase().contains("mysql")) {
            databaseConfig.setDatatypeFactory(new MySqlDataTypeFactory());
            databaseConfig.setMetadataHandler(new MySqlMetadataHandler());
            databaseConfig.setEscapePattern("`?`");
            databaseConfig.setAllowEmptyFields(Boolean.TRUE);
        }

        final DatabaseDataSourceConnectionFactoryBean connectionFactory = new DatabaseDataSourceConnectionFactoryBean();
        connectionFactory.setDataSource(dataSource);
        connectionFactory.setDatabaseConfig(databaseConfig);
        return connectionFactory;
    }

}
