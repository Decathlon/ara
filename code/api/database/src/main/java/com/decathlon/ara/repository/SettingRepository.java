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

package com.decathlon.ara.repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.decathlon.ara.domain.Setting;

/**
 * Spring Data JPA repository for the Setting entity.
 */
@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {

    Setting findByProjectIdAndCode(long projectId, String code);

    List<Setting> findByProjectId(long projectId);

    default Map<String, String> getProjectSettings(long projectId) {
        return findByProjectId(projectId).stream().collect(Collectors.toMap(Setting::getCode, setting -> setting.getValue() == null ? "" : setting.getValue()));
    }

}
