package com.decathlon.ara.integration;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate"
})
@SpringBootTest
public interface LiquibaseValidationTestIT {

    String databaseName();

    @Test
    default void checkValidation(@Value("${spring.jpa.hibernate.ddl-auto}") String hbm2ddl,
                                 @Value("${ara.database.target}") String databaseTarget) {
        Assert.assertEquals("validate", hbm2ddl);
        Assert.assertEquals(databaseName(), databaseTarget);
    }

    @TestPropertySource(properties = {
            "ara.database.target=mysql"
    })
    class MysqlLiquibaseValidationTestIT implements LiquibaseValidationTestIT {

        @Override
        public String databaseName() {
            return "mysql";
        }
    }

    @TestPropertySource(properties = {
            "ara.database.target=postgresql"
    })
    class PostgreSQLLiquibaseValidationTestIT implements LiquibaseValidationTestIT {

        @Override
        public String databaseName() {
            return "postgresql";
        }
    }

    @TestPropertySource(properties = {
            "ara.database.target=h2"
    })
    class H2LiquibaseValidationTestIT implements LiquibaseValidationTestIT {

        @Override
        public String databaseName() {
            return "h2";
        }
    }

}
