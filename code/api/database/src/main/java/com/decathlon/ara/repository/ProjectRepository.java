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

import org.springframework.stereotype.Repository;

import com.decathlon.ara.domain.MemberContainerRepository;
import com.decathlon.ara.domain.Project;

/**
 * Spring Data JPA repository for the Project entity.
 */
@Repository
public interface ProjectRepository extends MemberContainerRepository<Project, Long> {

    @Override
    default Project findByContainerIdentifier(String identifier) {
        return findOneByCode(identifier);
    }

    List<Project> findAllByOrderByName();

    Project findOneByCode(String code);

    Project findOneByName(String name);

}
