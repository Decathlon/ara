package com.decathlon.ara.web.rest;

import com.decathlon.ara.domain.enumeration.CommunicationType;
import com.decathlon.ara.service.dto.communication.CommunicationDTO;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static com.decathlon.ara.util.TestUtil.header;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
@DatabaseSetup("/dbunit/communication.xml")
public class CommunicationResourceIT {

    private static final String PROJECT_CODE = "p";

    @Autowired
    private CommunicationResource cut;

    @Test
    public void testFindAll() {
        // WHEN
        ResponseEntity<List<CommunicationDTO>> response = cut.getAll(PROJECT_CODE);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(
                new CommunicationDTO("code1", "name1", CommunicationType.TEXT, "message1"),
                new CommunicationDTO("code2", "name2", CommunicationType.HTML, "message2"),
                new CommunicationDTO("code3", "name3", CommunicationType.TEXT, "message3"));
    }

    @Test
    public void getOneByCode_ShouldReturnTheRequestedCommunication_WhenRequestingAnExistingCode() {
        // GIVEN
        String code = "code1";

        // WHEN
        ResponseEntity<CommunicationDTO> response = cut.getOneByCode(PROJECT_CODE, code);

        // THEN
        assertThat(response.getBody().getCode()).isEqualTo("code1");
        assertThat(response.getBody().getName()).isEqualTo("name1");
        assertThat(response.getBody().getType()).isEqualTo(CommunicationType.TEXT);
        assertThat(response.getBody().getMessage()).isEqualTo("message1");
    }

    @Test
    public void getOneByCode_ShouldFailAsNotFound_WhenGettingANonExistentCode() {
        // GIVEN
        String nonexistentCode = "nonexistent";

        // WHEN
        ResponseEntity<CommunicationDTO> response = cut.getOneByCode(PROJECT_CODE, nonexistentCode);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The communication does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("communication");
    }

    @Test
    public void update_ShouldOnlyUpdateTypeAndMessage_WhenCodeIsFound() {
        // GIVEN
        String codeToUpdate = "code2";
        CommunicationDTO dtoToUpdate = new CommunicationDTO("any", "any", CommunicationType.TEXT, "updated");

        // WHEN
        ResponseEntity<CommunicationDTO> response = cut.update(PROJECT_CODE, codeToUpdate, dtoToUpdate);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCode()).isEqualTo("code2"); // Not updated
        assertThat(response.getBody().getName()).isEqualTo("name2"); // Not updated
        assertThat(response.getBody().getType()).isEqualTo(CommunicationType.TEXT); // Was HTML
        assertThat(response.getBody().getMessage()).isEqualTo("updated");
    }

    @Test
    public void update_ShouldFailAsNotFound_WhenUpdatingANonExistentCode() {
        // GIVEN
        final String nonexistentCode = "nonexistent";
        CommunicationDTO anyCommunication = new CommunicationDTO();

        // WHEN
        ResponseEntity<CommunicationDTO> response = cut.update(PROJECT_CODE, nonexistentCode, anyCommunication);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The communication does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("communication");
    }

}
