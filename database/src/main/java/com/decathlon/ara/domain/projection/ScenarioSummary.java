package com.decathlon.ara.domain.projection;

import com.decathlon.ara.domain.QScenario;
import com.decathlon.ara.domain.Source;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class ScenarioSummary {

    private Long id;
    private Source source;
    private String featureFile;
    private String featureName;
    private String name;

    private int functionalityCount;
    private boolean hasCountryCodes;
    private boolean hasSeverity;

    private String wrongFunctionalityIds;
    private String wrongCountryCodes;
    private String wrongSeverityCode;

    // Keep synced with fields in order
    public static ConstructorExpression<ScenarioSummary> projectionOf(QScenario scenario) {
        QScenario subScenario = new QScenario("sub");

        return Projections.constructor(ScenarioSummary.class,
                scenario.id,
                scenario.source,
                scenario.featureFile,
                scenario.featureName,
                scenario.name,

                JPAExpressions
                        .select(subScenario.functionalities.size())
                        .from(subScenario)
                        .where(subScenario.id.eq(scenario.id)),
                scenario.countryCodes.isNotEmpty(),
                scenario.severity.isNotEmpty(),

                scenario.wrongFunctionalityIds,
                scenario.wrongCountryCodes,
                scenario.wrongSeverityCode);
    }

}
