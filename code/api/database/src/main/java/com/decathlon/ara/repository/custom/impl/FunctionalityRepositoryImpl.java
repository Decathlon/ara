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
                .where(QFunctionality.functionality.projectId.eq(projectId))
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
                .where(QFunctionality.functionality.projectId.eq(projectId))
                .where(QFunctionality.functionality.countryCodes.prepend(separator).concat(separator)
                        .like("%" + separator + countryCode + separator + "%"))
                .fetchFirst() != null;
    }
}
