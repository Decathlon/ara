package com.decathlon.ara.coverage;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.repository.CountryRepository;
import com.decathlon.ara.service.dto.coverage.AxisPointDTO;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CountryAxisGenerator implements AxisGenerator {

    @NonNull
    private final CountryRepository countryRepository;

    @Override
    public String getCode() {
        return "countries";
    }

    @Override
    public String getName() {
        return "Countries";
    }

    @Override
    public Stream<AxisPointDTO> getPoints(long projectId) {
        return countryRepository.findAllByProjectIdOrderByCode(projectId).stream()
                .map(country -> new AxisPointDTO(country.getCode(), country.getCode().toUpperCase(), country.getName()));
    }

    @Override
    public String[] getValuePoints(Functionality functionality) {
        if (StringUtils.isEmpty(functionality.getCountryCodes())) {
            return null;
        }
        return functionality.getCountryCodes().split(Functionality.COUNTRY_CODES_SEPARATOR);
    }

}
