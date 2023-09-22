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

package com.decathlon.ara.dbunit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

/**
 * <p>Utility class to import a DbUnit dataset into a database and COMMIT it, so you can debug the dataset, or use it to
 * complement it using the Web interface and re-export it... To use this class, set the dataset to import in the
 * \@DatabaseSetup annotation, and run it with the dev profile, for instance (VM arg: -Dspring.profiles.active=dev).</p>
 * <p>Be careful: your tables will be cleared before the import is done!</p>
 * <p>Implementation note: do NOT use {@link TransactionalSpringIntegrationTest}, as we do not want the transaction to
 * be rollbacked after import!</p>
 */
@SpringBootTest
@TestExecutionListeners({
    TransactionalTestExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DbUnitTestExecutionListener.class
})
@DatabaseSetup("/dbunit/brand-new-project-dataset.xml")
@TestPropertySource(
		locations = "classpath:application-db-h2.properties")
class DbUnitImporter {

    // WARNING: you may need to put &sessionVariables=FOREIGN_KEY_CHECKS=0 at the end of spring.datasource.url in
    // application-dev.properties

    @Test
    // No @Transactional, for data to be committed
    void importTheDataSetConfiguredInDatabaseSetup() {
        // All the work is done by @DatabaseSetup
    }

}
