/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

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
