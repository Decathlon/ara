package com.decathlon.ara.dbunit;

import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import javax.persistence.EntityManager;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.hibernate.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * The class name does not start nor end with "Test" because it's not supposed to be run during build. This class is to be run on a development
 * machine to generate a new DbUnit XML data-source to feed to other integration tests.
 */
@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
public class DbUnitExporter {

    private static final String XML_PATH = "src/test/resources/dbunit/freshly-created-dataset-to-rename.xml";

    @Autowired
    private EntityManager entityManager;

    @Test
    public void export() {
        try (Session session = entityManager.unwrap(Session.class)) {
            session.doWork(jdbcConnection -> {
                // Can be launched in "ara" parent-module or in "ara/server" sub-module
                boolean inServerModule = System.getProperty("user.dir").endsWith("server");
                String xmlPath = (inServerModule ? "" : "server/") + XML_PATH;
                try (OutputStream outputStream = new FileOutputStream(xmlPath)) {
                    IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);

                    QueryDataSet partialDataSet = new QueryDataSet(connection);

                    // We export all tables: just remove any unneeded table in the generated XML
                    // Take note of the order to prevent FK constraint violation when re-inserting
                    // Project Segmentation
                    partialDataSet.addTable("project");
                    partialDataSet.addTable("setting");
                    // Configuration tables
                    partialDataSet.addTable("country");
                    partialDataSet.addTable("root_cause");
                    partialDataSet.addTable("team");
                    partialDataSet.addTable("source");
                    partialDataSet.addTable("type");
                    partialDataSet.addTable("cycle_definition");
                    partialDataSet.addTable("severity");
                    partialDataSet.addTable("communication");
                    // Execution indexation
                    partialDataSet.addTable("execution_completion_request");
                    partialDataSet.addTable("execution");
                    partialDataSet.addTable("country_deployment");
                    partialDataSet.addTable("run");
                    partialDataSet.addTable("executed_scenario");
                    partialDataSet.addTable("error");
                    // Problem assignation
                    partialDataSet.addTable("problem");
                    partialDataSet.addTable("problem_pattern");
                    partialDataSet.addTable("problem_occurrence");
                    // Functionality coverage
                    partialDataSet.addTable("functionality");
                    partialDataSet.addTable("scenario");
                    partialDataSet.addTable("functionality_coverage");

                    // XML file into which data needs to be exported
                    FlatXmlDataSet.write(partialDataSet, outputStream);
                    System.out.println("Dataset written to " + XML_PATH);
                } catch (DatabaseUnitException | IOException e) {
                    throw new SQLException("Cannot export database: " + e.getMessage(), e);
                }
            });
        }
    }

}
