package com.decathlon.ara.repository.custom.impl;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.QFunctionality;
import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.repository.custom.FunctionalityRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FunctionalityRepositoryImpl implements FunctionalityRepositoryCustom {

    @NonNull
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Map<Long, Long> getFunctionalityTeamIds(long projectId) {
        return jpaQueryFactory.select(QFunctionality.functionality.id, QFunctionality.functionality.teamId)
                .distinct()
                .from(QFunctionality.functionality)
                .where(QFunctionality.functionality.type.eq(FunctionalityType.FUNCTIONALITY))
                .where(QFunctionality.functionality.projectId.eq(Long.valueOf(projectId)))
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(QFunctionality.functionality.id),
                        tuple -> tuple.get(QFunctionality.functionality.teamId)));
    }

    @Override
    public boolean existsByProjectIdAndCountryCode(long projectId, String countryCode) {
        final String separator = Functionality.COUNTRY_CODES_SEPARATOR;
        return jpaQueryFactory.select(QFunctionality.functionality.id)
                .from(QFunctionality.functionality)
                .where(QFunctionality.functionality.projectId.eq(Long.valueOf(projectId)))
                .where(QFunctionality.functionality.countryCodes.prepend(separator).concat(separator)
                        .like("%" + separator + countryCode + separator + "%"))
                .fetchFirst() != null;
    }

}
