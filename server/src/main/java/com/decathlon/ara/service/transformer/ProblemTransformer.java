package com.decathlon.ara.service.transformer;

import com.decathlon.ara.SpringApplicationContext;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.service.DefectService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.dto.problem.ProblemDTO;
import com.decathlon.ara.service.support.Settings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service provide transformation utilities (DTO - DO and DO - DTO) for the Problem.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.0.1
 */
@Service
public class ProblemTransformer {

    @Autowired
    private TeamTransformer teamTransformer;

    @Autowired
    private RootCauseTransformer rootCauseTransformer;

    /**
     * Transform the given Problem DO to a ProblemDTO object.
     * <p>
     * Returns an empty ProblemDTO if the parameter is null.
     *
     * @param problem the DO to transform
     * @return the result DTO.
     */
    ProblemDTO toDto(Problem problem) {
        ProblemDTO result = new ProblemDTO();
        result.setId(0L);
        if (null != problem) {
            result.setId(problem.getId());
            result.setName(problem.getName());
            result.setComment(problem.getComment());
            result.setStatus(problem.getStatus());
            result.setEffectiveStatus(problem.getEffectiveStatus());
            result.setBlamedTeam(teamTransformer.toDto(problem.getBlamedTeam()));
            result.setDefectId(problem.getDefectId());
            result.setDefectExistence(problem.getDefectExistence());
            result.setClosingDateTime(problem.getClosingDateTime());
            result.setRootCause(rootCauseTransformer.toDto(problem.getRootCause()));
            result.setCreationDateTime(problem.getCreationDateTime());
            result.setFirstSeenDateTime(problem.getFirstSeenDateTime());
            result.setLastSeenDateTime(problem.getLastSeenDateTime());
        }
        return result;
    }

    /**
     * Transform the given list of Problem DO to a list of ProblemDTO.
     * <p>
     * Returns an empty list if the parameter is null or empty.
     *
     * @param problems the list of DO to transform
     * @return the list of resulting DTO.
     */
    public List<ProblemDTO> toDtos(Collection<Problem> problems) {
        if (null == problems) {
            return new ArrayList<>();
        }
        return problems.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
