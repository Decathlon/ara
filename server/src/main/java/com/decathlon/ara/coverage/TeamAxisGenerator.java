package com.decathlon.ara.coverage;

import com.decathlon.ara.repository.TeamRepository;
import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.Team;
import com.decathlon.ara.service.dto.coverage.AxisPointDTO;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TeamAxisGenerator implements AxisGenerator {

    @NonNull
    private final TeamRepository teamRepository;

    @Override
    public String getCode() {
        return "team";
    }

    @Override
    public String getName() {
        return "Teams";
    }

    @Override
    public Stream<AxisPointDTO> getPoints(long projectId) {
        return teamRepository.findAllByProjectIdOrderByName(projectId).stream()
                .filter(Team::isAssignableToFunctionalities)
                .map(team -> new AxisPointDTO(team.getId().toString(), team.getName(), null));
    }

    @Override
    public String[] getValuePoints(Functionality functionality) {
        return (functionality.getTeamId() == null ? null : new String[] { functionality.getTeamId().toString() });
    }

}
