package com.decathlon.ara.web.rest;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import javax.persistence.EntityNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static com.decathlon.ara.util.TestUtil.NONEXISTENT;
import static com.decathlon.ara.util.TestUtil.header;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
@DatabaseSetup("/dbunit/functionality.xml")
public class FunctionalityResourceDeleteIT {

    private static final String PROJECT_CODE = "p";

    @Autowired
    private FunctionalityRepository functionalityRepository;

    @Autowired
    private FunctionalityResource cut;

    @Test
    public void testDeleteNonexistent() {
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, NONEXISTENT.longValue());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The functionality or folder does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");

        // Nothing should have changed
        assertCountIsNow(11);
    }

    @Test
    public void testDeleteOneFunctionality() {
        deleteSingle(22);
    }

    @Test
    public void testDeleteOneSingleFolder() {
        deleteSingle(12);
    }

    @Test
    public void testDeleteFolderWithChildren() {
        long id = 11;
        deleteAndAssertSuccess(id);
        assertCountIsNow(7);
        assertNotFoundAnymore(id);
        assertNotFoundAnymore(111);
        assertNotFoundAnymore(112);
        assertNotFoundAnymore(113);
    }

    @Test
    public void testDeleteRootFolderWithChildren() {
        long id = 1;
        deleteAndAssertSuccess(id);
        assertCountIsNow(5);
        assertNotFoundAnymore(id);
        assertNotFoundAnymore(11);
        assertNotFoundAnymore(12);
        assertNotFoundAnymore(111);
        assertNotFoundAnymore(112);
        assertNotFoundAnymore(113);
    }

    private void assertCountIsNow(int expected) {
        assertThat(functionalityRepository.count()).isEqualTo(expected);
    }

    private void assertNotFoundAnymore(long id) {
        final Functionality functionality = functionalityRepository.getOne(Long.valueOf(id));
        try {
            assertThat(functionality).isNull();
        } catch (EntityNotFoundException e) {
            // Expected
        }
    }

    private void deleteSingle(long id) {
        deleteAndAssertSuccess(id);
        assertCountIsNow(10);
        assertNotFoundAnymore(id);
    }

    private void deleteAndAssertSuccess(long id) {
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, id);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.functionality.deleted");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo(String.valueOf(id));
    }

}
