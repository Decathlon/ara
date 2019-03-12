package com.decathlon.ara.dbunit;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;

/**
 * <p>Utility class to import a DbUnit dataset into a database and COMMIT it, so you can debug the dataset, or use it to
 * complement it using the Web interface and re-export it... To use this class, set the dataset to import in the
 * \@DatabaseSetup annotation, and run it with the dev profile, for instance (VM arg: -Dspring.profiles.active=dev).</p>
 * <p>Be careful: your tables will be cleared before the import is done!</p>
 * <p>Implementation note: do NOT use {@link TransactionalSpringIntegrationTest}, as we do not want the transaction to
 * be rollbacked after import!</p>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
@DatabaseSetup("/dbunit/freshly-created-dataset-to-rename.xml")
@TestPropertySource(properties = "ara.scheduling.enable=false")
public class DbUnitImporter {

    // WARNING: you may need to put &sessionVariables=FOREIGN_KEY_CHECKS=0 at the end of spring.datasource.url in
    // application-dev.properties

    @Test
    // No @Transactional, for data to be committed
    public void importTheDataSetConfiguredInDatabaseSetup() {
        // All the work is done by @DatabaseSetup
    }

}
