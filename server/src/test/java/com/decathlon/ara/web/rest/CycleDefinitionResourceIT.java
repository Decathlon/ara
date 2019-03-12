package com.decathlon.ara.web.rest;

import com.decathlon.ara.service.dto.cycledefinition.CycleDefinitionDTO;
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

import static com.decathlon.ara.util.TestUtil.NONEXISTENT;
import static com.decathlon.ara.util.TestUtil.header;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
@DatabaseSetup("/dbunit/cycleDefinition.xml")
public class CycleDefinitionResourceIT {

    private static final String PROJECT_CODE = "p";

    @Autowired
    private CycleDefinitionResource cut;

    @Test
    public void create_ShouldInsertEntity_WhenAllRulesAreRespected() {
        // GIVEN
        CycleDefinitionDTO cycleDefinitionDTO = new CycleDefinitionDTO()
                .withId(null)
                .withBranch("   newBranch \t ")
                .withName(" newName\t")
                .withBranchPosition(Integer.valueOf(0));

        // WHEN
        ResponseEntity<CycleDefinitionDTO> response = cut.create(PROJECT_CODE, cycleDefinitionDTO);

        // THEN

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation().getPath()).isEqualTo("/api/projects/p/cycle-definitions/" + response.getBody().getId());
        assertThat(response.getBody().getBranch()).isEqualTo("newBranch");
        assertThat(response.getBody().getName()).isEqualTo("newName");
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly(
                new CycleDefinitionDTO(response.getBody().getId(), "newBranch", "newName", 0),
                new CycleDefinitionDTO(201L, "master", "cycle2", 1),
                new CycleDefinitionDTO(200L, "develop", "cycle1", 2),
                new CycleDefinitionDTO(202L, "stab", "cycle3", 3),
                new CycleDefinitionDTO(199L, "stab", "cycle4", 3));
    }

    @Test
    public void create_ShouldFailAsBadRequest_WhenIdProvided() {
        // GIVEN
        final CycleDefinitionDTO cycleDefinitionWithId = new CycleDefinitionDTO(NONEXISTENT, "any", "any", 0);

        // WHEN
        ResponseEntity<CycleDefinitionDTO> response = cut.create(PROJECT_CODE, cycleDefinitionWithId);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.id_exists");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("cycle-definition");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A new cycle-definition cannot already have an ID.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void create_ShouldFailAsNotUnique_WhenBranchAndNameAlreadyExist() {
        // GIVEN
        CycleDefinitionDTO cycleDefinitionWithExistingBranchAndName = new CycleDefinitionDTO();
        cycleDefinitionWithExistingBranchAndName.setBranch("develop");
        cycleDefinitionWithExistingBranchAndName.setName("cycle1");

        // WHEN
        ResponseEntity<CycleDefinitionDTO> response = cut.create(PROJECT_CODE, cycleDefinitionWithExistingBranchAndName);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("cycle-definition");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The branch and name couple is already used by another cycle.");
        assertThat(header(response, HeaderUtil.DUPLICATE_PROPERTY_NAME)).isEqualTo("branch");
        assertThat(header(response, HeaderUtil.OTHER_ENTITY_KEY)).isEqualTo("200");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void getAll_ShouldReturnAllEntitiesOrderedByBranchPositionBranchAndName() {
        // WHEN
        ResponseEntity<List<CycleDefinitionDTO>> response = cut.getAll(PROJECT_CODE);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly( // Also test they are ordered by branchPosition, branch and name
                new CycleDefinitionDTO(201L, "master", "cycle2", 1),
                new CycleDefinitionDTO(200L, "develop", "cycle1", 2),
                new CycleDefinitionDTO(202L, "stab", "cycle3", 3),
                new CycleDefinitionDTO(199L, "stab", "cycle4", 3));
    }

    @Test
    public void update_ShouldUpdateEntity_WhenAllRulesAreRespected() {
        // GIVEN
        CycleDefinitionDTO cycleDefinitionDTO = new CycleDefinitionDTO(null, "   updated \t ", "  updatedToo\t", 0);

        // WHEN
        ResponseEntity<CycleDefinitionDTO> response = cut.update(PROJECT_CODE, Long.valueOf(200), cycleDefinitionDTO);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.cycle-definition.updated");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("200");
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly(// Ordered by branchPosition, branch and name
                new CycleDefinitionDTO(200L, "updated", "updatedToo", 0),
                new CycleDefinitionDTO(201L, "master", "cycle2", 1),
                new CycleDefinitionDTO(202L, "stab", "cycle3", 3),
                new CycleDefinitionDTO(199L, "stab", "cycle4", 3));
    }

    @Test
    public void testUpdateExistingBranchAndName() {
        // GIVEN
        final Long id = Long.valueOf(200);
        final CycleDefinitionDTO cycleDefinition = new CycleDefinitionDTO(null, "stab", "cycle3", 4);

        // WHEN
        ResponseEntity<CycleDefinitionDTO> response = cut.update(PROJECT_CODE, id, cycleDefinition);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("cycle-definition");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The branch and name couple is already used by another cycle.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void testUpdateNonexistent() {
        // GIVEN
        final CycleDefinitionDTO anyCycleDefinition = new CycleDefinitionDTO(null, "nonexistent", "nonexistent", 0);

        // WHEN
        ResponseEntity<CycleDefinitionDTO> response = cut.update(PROJECT_CODE, NONEXISTENT, anyCycleDefinition);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The cycle definition does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("cycle-definition");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void update_ShouldUpdateBranchPositionOfAllCyclesOfSameBranch_WhenUpdatingBranchPositionOfOneCycleOfABranch() {
        // GIVEN
        final Long idOfABranchWithAnotherCycle = Long.valueOf(199);
        final CycleDefinitionDTO cycleDefinitionWithUpdatedPosition =
                new CycleDefinitionDTO(null, "stab", "cycle4", 9);

        // WHEN
        cut.update(PROJECT_CODE, idOfABranchWithAnotherCycle, cycleDefinitionWithUpdatedPosition);

        // THEN
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly(// Ordered by branchPosition, branch and name
                new CycleDefinitionDTO(201L, "master", "cycle2", 1),
                new CycleDefinitionDTO(200L, "develop", "cycle1", 2),
                new CycleDefinitionDTO(202L, "stab", "cycle3", 9),
                new CycleDefinitionDTO(199L, "stab", "cycle4", 9));
    }

    @Test
    public void testDeleteOk() {
        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, Long.valueOf(201));

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.cycle-definition.deleted");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("201");

        // Make sure it has been deleted correctly
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly(// Ordered by branchPosition, branch and name
                new CycleDefinitionDTO(Long.valueOf(200), "develop", "cycle1", Integer.valueOf(2)),
                new CycleDefinitionDTO(Long.valueOf(202), "stab", "cycle3", Integer.valueOf(3)),
                new CycleDefinitionDTO(Long.valueOf(199L), "stab", "cycle4", Integer.valueOf(3)));
    }

    @Test
    public void testDeleteNonexistent() {
        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, NONEXISTENT);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The cycle definition does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("cycle-definition");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void testDelete_used_to_execution() {
        // GIVEN 
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, Long.valueOf(202));

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.used_by_execution");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("cycle-definition");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The cycle definition is used by at least one execution: please wait for such executions to be purged.");
    }

    private void assertThatTableHasNotChangedInDataBase() {
        getAll_ShouldReturnAllEntitiesOrderedByBranchPositionBranchAndName();
    }

}
