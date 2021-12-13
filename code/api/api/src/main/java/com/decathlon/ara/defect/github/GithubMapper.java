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

package com.decathlon.ara.defect.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Map the Github API Json responses to Java POJO in this project.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.1.0
 */
@Service
class GithubMapper {

    private static final Logger LOG = LoggerFactory.getLogger(GithubMapper.class);

    static final TypeReference<GithubIssue> TYPE_REFERENCE_TO_GITHUB_ISSUE =
            new TypeReference<>() {
            };
    static final TypeReference<List<GithubIssue>> TYPE_REFERENCE_TO_LIST_GITHUB_ISSUE =
            new TypeReference<>() {
            };

    private final ObjectMapper objectMapper;

    public GithubMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Map the given json String to a GithubIssue.
     *
     * @param json the GitHub REST API response json
     * @return an optional containing the GithubIssue or an empty one if the json is malformed / don't match.
     */
    Optional<GithubIssue> jsonToIssue(String json) {
        try {
            return Optional.of(this.objectMapper.readValue(json, TYPE_REFERENCE_TO_GITHUB_ISSUE));
        } catch (IOException ex) {
            LOG.error("DEFECT|Unable to cast this json to a Github issue : " + json, ex);
            return Optional.empty();
        }
    }

    /**
     * Map the given json array String to a list GithubIssue.
     *
     * @param json the GitHub REST API response json
     * @return the list of GithubIssue or an empty list if the json is malformed / don't match.
     */
    List<GithubIssue> jsonToIssueList(String json) {
        try {
            return this.objectMapper.readValue(json, TYPE_REFERENCE_TO_LIST_GITHUB_ISSUE);
        } catch (IOException ex) {
            LOG.error("DEFECT|Unable to cast this json to a list of Github issues : " + json, ex);
            return new ArrayList<>();
        }
    }
}
