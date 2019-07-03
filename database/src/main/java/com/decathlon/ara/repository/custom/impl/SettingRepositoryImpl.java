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

import com.decathlon.ara.domain.QSetting;
import com.decathlon.ara.repository.custom.SettingRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SettingRepositoryImpl implements SettingRepositoryCustom {

    @NonNull
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Map<String, String> getProjectSettings(long projectId) {
        return jpaQueryFactory.select(QSetting.setting.code, QSetting.setting.value)
                .from(QSetting.setting)
                .where(QSetting.setting.projectId.eq(Long.valueOf(projectId)))
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(QSetting.setting.code),
                        tuple -> {
                            final String value = tuple.get(QSetting.setting.value);
                            return value == null ? "" : value;
                        }));
    }

}
