package com.decathlon.ara.web.rest;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;
import com.decathlon.ara.service.dto.request.FunctionalityPosition;
import com.decathlon.ara.service.dto.request.MoveFunctionalityDTO;
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
import static com.decathlon.ara.util.TestUtil.longs;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
@DatabaseSetup("/dbunit/functionality.xml")
public class FunctionalityResourceMoveIT {

    private static final String PROJECT_CODE = "p";

    @Autowired
    private FunctionalityRepository functionalityRepository;

    @Autowired
    private FunctionalityResource cut;

    @Test
    public void testMoveNonexistentSource() {
        ResponseEntity<FunctionalityDTO> response = move(NONEXISTENT.longValue(), Long.valueOf(1), FunctionalityPosition.LAST_CHILD);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The functionality or folder to move does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    public void testMoveNonexistentDestination() {
        ResponseEntity<FunctionalityDTO> response = move(1, NONEXISTENT, FunctionalityPosition.LAST_CHILD);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The functionality or folder where to insert does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    public void testMoveIntoAFunctionality() {
        ResponseEntity<FunctionalityDTO> response = move(1, Long.valueOf(31), FunctionalityPosition.LAST_CHILD);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.functionalities_cannot_have_children");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("Functionality cannot have children.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    public void testMoveIntoItself() {
        ResponseEntity<FunctionalityDTO> response = move(1, Long.valueOf(1), FunctionalityPosition.LAST_CHILD);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.cannot_move_to_itself_or_sub_folder");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("You cannot move a folder or functionality to itself or a sub-folder.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    public void testMoveIntoASubFolderOfItself() {
        ResponseEntity<FunctionalityDTO> response = move(1, Long.valueOf(11), FunctionalityPosition.LAST_CHILD);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.cannot_move_to_itself_or_sub_folder");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("You cannot move a folder or functionality to itself or a sub-folder.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    public void testMoveToTopOfFolder() {
        final long id = 113;
        ResponseEntity<FunctionalityDTO> response = move(id, Long.valueOf(111), FunctionalityPosition.ABOVE);
        assertMoved(response, id, Long.valueOf(11), 500);
        assertChildren(Long.valueOf(11), longs(113, 111, 112));
    }

    @Test
    public void testMoveBelowFirstOfFolder() {
        final long id = 113;
        ResponseEntity<FunctionalityDTO> response = move(id, Long.valueOf(111), FunctionalityPosition.BELOW);
        assertMoved(response, id, Long.valueOf(11), 1500);
        assertChildren(Long.valueOf(11), longs(111, 113, 112));
    }

    @Test
    public void testMoveAboveLastOfFolder() {
        final long id = 111;
        ResponseEntity<FunctionalityDTO> response = move(id, Long.valueOf(113), FunctionalityPosition.ABOVE);
        assertMoved(response, id, Long.valueOf(11), 2500);
        assertChildren(Long.valueOf(11), longs(112, 111, 113));
    }

    @Test
    public void testMoveToBottomOfFolder() {
        final long id = 111;
        ResponseEntity<FunctionalityDTO> response = move(id, Long.valueOf(113), FunctionalityPosition.BELOW);
        assertMoved(response, id, Long.valueOf(11), 1500 + Double.MAX_VALUE / 2);
        assertChildren(Long.valueOf(11), longs(112, 113, 111));
    }

    @Test
    public void testMoveFunctionalityIntoAnotherFolder() {
        final long id = 111;
        ResponseEntity<FunctionalityDTO> response = move(id, Long.valueOf(21), FunctionalityPosition.LAST_CHILD);
        assertMoved(response, id, Long.valueOf(21), Double.MAX_VALUE / 2);
        assertChildren(Long.valueOf(11), longs(112, 113));
        assertChildren(Long.valueOf(21), longs(111));
    }

    @Test
    public void testMoveFunctionalityNextToAnotherFolderInRoot() {
        final long id = 112;
        ResponseEntity<FunctionalityDTO> response = move(id, Long.valueOf(2), FunctionalityPosition.ABOVE);
        assertMoved(response, id, null, 1500);
        assertChildren(Long.valueOf(11), longs(111, 113));
        assertChildren(null, longs(1, 112, 2, 3));
    }

    @Test
    public void testMoveFunctionalityAppendingItToRoot() {
        final long id = 112;
        ResponseEntity<FunctionalityDTO> response = move(id, null, FunctionalityPosition.LAST_CHILD);
        assertMoved(response, id, null, 1500 + Double.MAX_VALUE / 2);
        assertChildren(Long.valueOf(11), longs(111, 113));
        assertChildren(null, longs(1, 2, 3, 112));
    }

    @Test
    public void testPositionNullIsLastChild() {
        final long id = 112;
        ResponseEntity<FunctionalityDTO> response = move(id, null, null);
        assertMoved(response, id, null, 1500 + Double.MAX_VALUE / 2);
        assertChildren(Long.valueOf(11), longs(111, 113));
        assertChildren(null, longs(1, 2, 3, 112));
    }

    @Test
    public void testMoveFolderWithItsChildren() {
        final long id = 1;
        ResponseEntity<FunctionalityDTO> response = move(id, Long.valueOf(31), FunctionalityPosition.BELOW);
        assertMoved(response, id, Long.valueOf(3), 500 + Double.MAX_VALUE / 2);
        assertChildren(null, longs(2, 3));
        assertChildren(Long.valueOf(3), longs(31, 1));
        assertChildren(Long.valueOf(1), longs(11, 12));
        assertChildren(Long.valueOf(11), longs(111, 112, 113));
    }

    @Test
    public void testMoveAboveWithNullReference() {
        ResponseEntity<FunctionalityDTO> response = move(1, null, FunctionalityPosition.ABOVE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.no_reference");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("Attempting to insert above or below no functionality nor folder.");
    }

    @Test
    public void testMoveBelowWithNullReference() {
        ResponseEntity<FunctionalityDTO> response = move(1, null, FunctionalityPosition.BELOW);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.no_reference");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("Attempting to insert above or below no functionality nor folder.");
    }

    private ResponseEntity<FunctionalityDTO> move(long sourceId, Long referenceId, FunctionalityPosition relativePosition) {
        return cut.move(PROJECT_CODE, new MoveFunctionalityDTO(sourceId, referenceId, relativePosition));
    }

    private void assertMoved(ResponseEntity<FunctionalityDTO> response, long id, Long parentId, double newOrder) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(id);
        assertThat(response.getBody().getParentId()).isEqualTo(parentId);
        assertThat(response.getBody().getOrder()).isEqualTo(newOrder);
        assertThat(response.getBody().getName()).isNotEmpty();
    }

    private void assertChildren(Long parentId, Long[] expectedSiblings) {
        List<Functionality> siblings = functionalityRepository.findAllByProjectIdAndParentIdOrderByOrder(1, parentId);
        assertThat(siblings.stream().map(Functionality::getId)).containsExactly(expectedSiblings);
    }

}
