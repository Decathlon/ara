package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.CountryDeployment;
import com.decathlon.ara.service.dto.countrydeployment.CountryDeploymentDTO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service provide transformation utilities (DTO - DO and DO - DTO) for the CountryDeployment.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.0.1
 */
@Service
class CountryDeploymentTransformer {

    @Autowired
    private CountryTransformer countryTransformer;

    /**
     * Transform the given CountryDeployment DO to a CountryDeployementDTO object.
     * <p>
     * Returns an empty CountryDeployementDTO if the parameter is null.
     *
     * @param countryDeployment the DO to transform
     * @return the result DTO.
     */
    CountryDeploymentDTO toDto(CountryDeployment countryDeployment) {
        CountryDeploymentDTO result = new CountryDeploymentDTO();
        result.setId(0L);
        if (null != countryDeployment) {
            result.setId(countryDeployment.getId());
            result.setCountry(countryTransformer.toDto(countryDeployment.getCountry()));
            result.setPlatform(countryDeployment.getPlatform());
            result.setJobUrl(countryDeployment.getJobUrl());
            result.setStatus(countryDeployment.getStatus());
            result.setResult(countryDeployment.getResult());
            result.setStartDateTime(countryDeployment.getStartDateTime());
            result.setEstimatedDuration(countryDeployment.getEstimatedDuration());
            result.setDuration(countryDeployment.getDuration());
        }
        return result;
    }

    /**
     * Transform the given list of CountryDeployment DO to a list of CountryDeploymentDTO.
     * <p>
     * Returns an empty list if the parameter is null or empty.
     *
     * @param countryDeployments the list of DO to transform
     * @return the list of resulting DTO.
     */
    List<CountryDeploymentDTO> toDtos(Collection<CountryDeployment> countryDeployments) {
        if (null == countryDeployments) {
            return new ArrayList<>();
        }
        return countryDeployments.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
