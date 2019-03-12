package com.decathlon.ara.repository.custom.impl;

import com.decathlon.ara.domain.QScenario;
import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.domain.projection.IgnoredScenario;
import com.decathlon.ara.domain.projection.ScenarioIgnoreCount;
import com.decathlon.ara.domain.projection.ScenarioSummary;
import com.decathlon.ara.repository.custom.ScenarioRepositoryCustom;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ScenarioRepositoryImpl implements ScenarioRepositoryCustom {

    @NonNull
    private final JPAQueryFactory jpaQueryFactory;

    /**
     * @param projectId the ID of the project in which to work
     * @return all scenarios that have no associated functionalities or have wrong or nonexistent functionality identifier
     */
    @Override
    public List<ScenarioSummary> findAllWithFunctionalityErrors(long projectId) {
        final QScenario scenario = QScenario.scenario;
        return jpaQueryFactory
                .select(ScenarioSummary.projectionOf(scenario))
                .from(scenario)
                .where(scenario.functionalities.isEmpty().or(scenario.wrongFunctionalityIds.isNotEmpty())
                        .or(scenario.countryCodes.isNull()).or(scenario.countryCodes.isEmpty()).or(scenario.wrongCountryCodes.isNotEmpty())
                        .or(scenario.severity.isNull()).or(scenario.severity.isEmpty()).or(scenario.wrongSeverityCode.isNotEmpty()))
                .where(scenario.source.projectId.eq(Long.valueOf(projectId)))
                .orderBy(scenario.source.code.asc(),
                        scenario.featureName.asc(),
                        scenario.name.asc(),
                        scenario.line.asc())
                .fetch();
    }

    /**
     * @param projectId the ID of the project in which to work
     * @return the count of ignored and not ignored scenarios for each source (API, Web...) and severity
     */
    @Override
    public List<ScenarioIgnoreCount> findIgnoreCounts(long projectId) {
        final QScenario scenario = QScenario.scenario;
        return jpaQueryFactory
                .select(Projections.constructor(ScenarioIgnoreCount.class,
                        scenario.source,
                        scenario.severity,
                        scenario.ignored,
                        scenario.count()))
                .from(scenario)
                .where(scenario.source.projectId.eq(Long.valueOf(projectId)))
                .groupBy(scenario.source,
                        scenario.severity,
                        scenario.ignored)
                .fetch();
    }

    /**
     * @param projectId the ID of the project in which to work
     * @return a summary of all ignored scenarios, ordered by source code, feature file and scenario name
     */
    @Override
    public List<IgnoredScenario> findIgnoredScenarios(long projectId) {
        final QScenario scenario = QScenario.scenario;
        return jpaQueryFactory
                .select(Projections.constructor(IgnoredScenario.class,
                        scenario.source,
                        scenario.featureFile,
                        scenario.featureName,
                        scenario.severity,
                        scenario.name))
                .from(scenario)
                .where(scenario.ignored.isTrue())
                .where(scenario.source.projectId.eq(Long.valueOf(projectId)))
                .orderBy(scenario.source.code.asc(),
                        scenario.featureFile.asc(),
                        scenario.name.asc())
                .fetch();
    }

    /**
     * @param projectId the ID of the project in which to work
     * @param countryCode a {@link Country#code}
     * @return true if and only if at least one scenario has this country code among its {@link Scenario#countryCodes}
     */
    @Override
    public boolean existsByProjectIdAndCountryCode(long projectId, String countryCode) {
        final String separator = Scenario.COUNTRY_CODES_SEPARATOR;
        return jpaQueryFactory.select(QScenario.scenario.id)
                .from(QScenario.scenario)
                .where(QScenario.scenario.source.projectId.eq(Long.valueOf(projectId)))
                .where(QScenario.scenario.countryCodes.prepend(separator).concat(separator)
                        .like("%" + separator + countryCode + separator + "%"))
                .fetchFirst() != null;
    }

}
