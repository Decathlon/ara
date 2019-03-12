package com.decathlon.ara.web.rest;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;
import com.decathlon.ara.service.dto.request.FunctionalityPosition;
import com.decathlon.ara.service.dto.request.NewFunctionalityDTO;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static com.decathlon.ara.util.TestUtil.NONEXISTENT;
import static com.decathlon.ara.util.TestUtil.header;
import static com.decathlon.ara.util.TestUtil.longs;
import static com.decathlon.ara.web.rest.FunctionalityResourceITSuite.TEAM_ID_NOT_ASSIGNABLE_TO_FUNCTIONALITIES;
import static com.decathlon.ara.web.rest.FunctionalityResourceITSuite.folder;
import static com.decathlon.ara.web.rest.FunctionalityResourceITSuite.functionality;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
@DatabaseSetup("/dbunit/functionality.xml")
public class FunctionalityResourceCreateIT {

    private static final String PROJECT_CODE = "p";
    private static final long CREATED_ID = -42;

    @Autowired
    private FunctionalityRepository functionalityRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private FunctionalityResource cut;

    private int counter = 0;

    @Test
    public void testCreate() {
        ResponseEntity<FunctionalityDTO> response = create(FunctionalityPosition.LAST_CHILD, Long.valueOf(12));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation().getPath()).isEqualTo("/api/projects/p/functionalities/" + response.getBody().getId());

        FunctionalityDTO functionality = response.getBody();
        assertThat(functionality.getId()).isPositive();
        assertThat(functionality.getParentId()).isEqualTo(12);
        assertThat(functionality.getOrder()).isEqualTo(Double.MAX_VALUE / 2);
        assertThat(functionality.getType()).isEqualTo("FUNCTIONALITY");
        assertThat(functionality.getName()).startsWith("New one ");
    }

    @Test
    public void testCreateAboveWithNullReference() {
        ResponseEntity<FunctionalityDTO> response = create(FunctionalityPosition.ABOVE, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.no_reference");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("Attempting to insert above or below no functionality nor folder.");
    }

    @Test
    public void testCreateAboveTheFirstRootReference() {
        ResponseEntity<FunctionalityDTO> response = create(FunctionalityPosition.ABOVE, Long.valueOf(1));
        assertCreated(response, null, 500, longs(CREATED_ID, 1, 2, 3));
    }

    @Test
    public void testCreateAboveTheLastRootReference() {
        ResponseEntity<FunctionalityDTO> response = create(FunctionalityPosition.ABOVE, Long.valueOf(3));
        assertCreated(response, null, 2500, longs(1, 2, CREATED_ID, 3));
    }

    @Test
    public void testCreateAboveTheFirstLeafReference() {
        ResponseEntity<FunctionalityDTO> response = create(FunctionalityPosition.ABOVE, Long.valueOf(111));
        assertCreated(response, Long.valueOf(11), 500, longs(CREATED_ID, 111, 112, 113));
    }

    @Test
    public void testCreateAboveTheLastLeafReference() {
        ResponseEntity<FunctionalityDTO> response = create(FunctionalityPosition.ABOVE, Long.valueOf(113));
        assertCreated(response, Long.valueOf(11), 2500, longs(111, 112, CREATED_ID, 113));
    }

    @Test
    public void testCreateBelowWithNullReference() {
        ResponseEntity<FunctionalityDTO> response = create(FunctionalityPosition.BELOW, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.no_reference");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("Attempting to insert above or below no functionality nor folder.");
    }

    @Test
    public void testCreateBelowTheFirstRootReference() {
        ResponseEntity<FunctionalityDTO> response = create(FunctionalityPosition.BELOW, Long.valueOf(1));
        assertCreated(response, null, 1500, longs(1, CREATED_ID, 2, 3));
    }

    @Test
    public void testCreateBelowTheLastRootReference() {
        ResponseEntity<FunctionalityDTO> response = create(FunctionalityPosition.BELOW, Long.valueOf(3));
        assertCreated(response, null, (3000 + Double.MAX_VALUE) / 2, longs(1, 2, 3, CREATED_ID));
    }

    @Test
    public void testCreateBelowTheFirstLeafReference() {
        ResponseEntity<FunctionalityDTO> response = create(FunctionalityPosition.BELOW, Long.valueOf(111));
        assertCreated(response, Long.valueOf(11), 1500, longs(111, CREATED_ID, 112, 113));
    }

    @Test
    public void testCreateBelowTheLastLeafReference() {
        ResponseEntity<FunctionalityDTO> response = create(FunctionalityPosition.BELOW, Long.valueOf(113));
        assertCreated(response, Long.valueOf(11), (3000 + Double.MAX_VALUE) / 2, longs(111, 112, 113, CREATED_ID));
    }

    @Test
    public void testCreateChildOnNodeWithExistingChildren() {
        ResponseEntity<FunctionalityDTO> response = create(FunctionalityPosition.LAST_CHILD, Long.valueOf(11));
        assertCreated(response, Long.valueOf(11), (3000 + Double.MAX_VALUE) / 2, longs(111, 112, 113, CREATED_ID));
    }

    @Test
    public void testCreateChildOnNodeWithoutAnyChildren() {
        ResponseEntity<FunctionalityDTO> response = create(FunctionalityPosition.LAST_CHILD, Long.valueOf(12));
        assertCreated(response, Long.valueOf(12), Double.MAX_VALUE / 2, longs(CREATED_ID));
    }

    @Test
    public void testCreateChildWithNulls() {
        // Position null means LAST_CHILD, and referenceId null means root => BELOW last child of root
        ResponseEntity<FunctionalityDTO> response = create(null, null);
        assertCreated(response, null, (3000 + Double.MAX_VALUE) / 2, longs(1, 2, 3, CREATED_ID));
    }

    @Test
    public void testCreateChildOfFunctionality() {
        ResponseEntity<FunctionalityDTO> response = create(FunctionalityPosition.LAST_CHILD, Long.valueOf(22));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.functionalities_cannot_have_children");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("Functionality cannot have children.");
    }

    @Test
    public void testCreateRelativeToNonexistentNode() {
        ResponseEntity<FunctionalityDTO> response = create(FunctionalityPosition.LAST_CHILD, NONEXISTENT);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The functionality or folder where to insert does not exist: it has perhaps been removed.");
    }

    @Test
    public void testCreateWithUnknownType() {
        NewFunctionalityDTO dto = new NewFunctionalityDTO().withFunctionality(new FunctionalityDTO().withType("NONEXISTENT").withName("Some"));
        ResponseEntity<FunctionalityDTO> response = cut.create(PROJECT_CODE, dto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.unknown_type");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("Unknown type.");
    }

    @Test
    public void testCreateWithId() {
        NewFunctionalityDTO dto = new NewFunctionalityDTO().withFunctionality(new FunctionalityDTO().withId(Long.valueOf(1)).withType("FOLDER").withName("Some"));
        ResponseEntity<FunctionalityDTO> response = cut.create(PROJECT_CODE, dto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.id_exists");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A new functionality cannot already have an ID.");
    }

    @Test
    public void testCreateShouldNotGenerateInfinity() {
        // This folder is empty
        Long parentId = Long.valueOf(12);

        // During first insert, order will be fine: (0 + Double.MAX_VALUE) / 2
        ResponseEntity<FunctionalityDTO> response = create(FunctionalityPosition.LAST_CHILD, parentId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getOrder()).isEqualTo(Double.MAX_VALUE / 2);
        entityManager.flush();

        // During second insert, order should NEVER be computed with "((Double.MAX_VALUE / 2) + Double.MAX_VALUE) / 2"
        // This would generate INFINITY and throw "java.sql.SQLException: 'Infinity' is not a valid numeric or approximate numeric value"
        // Instead, this test case ensures the computation will remain "(Double.MAX_VALUE / 4) + (Double.MAX_VALUE / 2)" for the computation to stay in safe ranges
        response = create(FunctionalityPosition.LAST_CHILD, parentId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        // Check that, in case the database allow Infinity and there is no exception
        assertThat(response.getBody().getOrder()).isNotEqualTo(Double.POSITIVE_INFINITY);
        entityManager.flush();
    }

    @Test
    public void testCreateWithNonexistentTeam() {
        final NewFunctionalityDTO newFunctionalityDTO = new NewFunctionalityDTO().withFunctionality(functionality().withTeamId(NONEXISTENT));

        ResponseEntity<FunctionalityDTO> response = cut.create(PROJECT_CODE, newFunctionalityDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("team");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The team does not exist: it has perhaps been removed.");
    }

    @Test
    public void testCreateWithNonAssignableTeam() {
        final NewFunctionalityDTO newFunctionalityDTO = new NewFunctionalityDTO().withFunctionality(functionality().withTeamId(TEAM_ID_NOT_ASSIGNABLE_TO_FUNCTIONALITIES));

        ResponseEntity<FunctionalityDTO> response = cut.create(PROJECT_CODE, newFunctionalityDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_assignable_team");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("team");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The team cannot be assigned to a functionality.");
    }

    @Test
    public void testCreateFolderWithZeroCoveredAndIgnoredScenarios() {
        final FunctionalityDTO folder = folder("Name")
                .withCoveredScenarios(Integer.valueOf(0))
                .withIgnoredScenarios(Integer.valueOf(0));
        final NewFunctionalityDTO newFunctionalityDTO = new NewFunctionalityDTO().withFunctionality(folder);

        ResponseEntity<FunctionalityDTO> response = cut.create(PROJECT_CODE, newFunctionalityDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    public void testCreateShouldSetCreationAndUpdateDateTime() {
        // GIVEN
        Date startDate = new Date();

        // WHEN
        final ResponseEntity<FunctionalityDTO> response = create(null, null);

        // THEN
        final Functionality createdFunctionality = functionalityRepository.findById(response.getBody().getId())
                .orElseThrow(() -> new AssertionError("Should have created a functionality"));
        assertThat(createdFunctionality.getCreationDateTime()).isEqualTo(createdFunctionality.getUpdateDateTime());
        assertThat(createdFunctionality.getCreationDateTime()).isBetween(startDate, new Date(), true, true);
    }

    private ResponseEntity<FunctionalityDTO> create(FunctionalityPosition position, Long referenceId) {
        NewFunctionalityDTO dto = new NewFunctionalityDTO();
        dto.setFunctionality(functionality().withName("New one " + (counter++)).withComment("Comment"));
        dto.setReferenceId(referenceId);
        dto.setRelativePosition(position);
        return cut.create(PROJECT_CODE, dto);
    }

    private void assertCreated(ResponseEntity<FunctionalityDTO> response, Long parentId, double order, Long[] expectedSiblings) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isPositive();
        assertThat(response.getBody().getParentId()).isEqualTo(parentId);
        assertThat(response.getBody().getOrder()).isEqualTo(order);
        assertThat(response.getBody().getType()).isEqualTo("FUNCTIONALITY");
        assertThat(response.getBody().getName()).startsWith("New one ");
        assertThat(response.getBody().getComment()).isEqualTo("Comment");

        entityManager.flush();

        for (int i = 0; i < expectedSiblings.length; i++) {
            if (expectedSiblings[i].longValue() == CREATED_ID) {
                expectedSiblings[i] = response.getBody().getId();
                break;
            }
        }

        List<Functionality> siblings = functionalityRepository.findAllByProjectIdAndParentIdOrderByOrder(1, parentId);
        assertThat(siblings.stream().map(Functionality::getId)).containsExactly(expectedSiblings);
    }

}
