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

package com.decathlon.ara.domain;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import java.util.Comparator;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import com.decathlon.ara.domain.enumeration.Technology;

@Entity
public class Source implements Comparable<Source> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "source_id")
    @SequenceGenerator(name = "source_id", sequenceName = "source_id", allocationSize = 1)
    private Long id;

    private long projectId;

    /**
     * Technical and short code to be used by the build system to send Cucumber-scenarios and Postman-requests as they
     * sit on the Version Control System, for indexation purpose, and for functional coverage computation.
     */
    @Column(length = 16, nullable = false)
    private String code;

    /**
     * Full name displayed in functionality cartography/coverage.
     */
    @Column(length = 32, nullable = false)
    private String name;

    /**
     * Unique recognizable letter to be displayed in functionality cartography/coverage where space is very limited.
     */
    @Column(nullable = false)
    private char letter;

    /**
     * The technology used for tests stored in this VCS source: Cucumber .feature files, Postman .json collections...
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private Technology technology;

    /**
     * The URL where the source files are stored (and could be edited, if possible).<br>
     * Used for functionality coverage and ignored scenarios tracking.<br>
     * The special "{{branch}}" token will be replaced by the VCS branch.<br>
     * Eg. "https://git.company.com/project/edit/{{branch}}/src/main/resources/"
     *
     * @see #defaultBranch defaultBranch used to replace "{{branch}}" when indexing functionalities coverage
     */
    @Column(nullable = false)
    private String vcsUrl;

    /**
     * The primary reference branch to use with {@link #vcsUrl} when indexing functionalities coverage.
     */
    @Column(length = 16, nullable = false)
    private String defaultBranch;

    /**
     * Only for a source whose {@link #technology} is {@link Technology#POSTMAN POSTMAN}.<br>
     * If true, the root folders are country codes ("all", "fr+us"...) on which to run the contained requests.<br>
     * If false, all requests are executed for all countries.
     */
    private boolean postmanCountryRootFolders;

    @Override
    public int compareTo(Source other) {
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<Source> projectIdComparator = comparing(s -> Long.valueOf(s.getProjectId()), nullsFirst(naturalOrder()));
        Comparator<Source> codeComparator = comparing(Source::getCode, nullsFirst(naturalOrder()));
        return nullsFirst(projectIdComparator
                .thenComparing(codeComparator)).compare(this, other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, projectId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Source)) {
            return false;
        }
        Source other = (Source) obj;
        return Objects.equals(code, other.code) && projectId == other.projectId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public char getLetter() {
        return letter;
    }

    public Technology getTechnology() {
        return technology;
    }

    public String getVcsUrl() {
        return vcsUrl;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public boolean isPostmanCountryRootFolders() {
        return postmanCountryRootFolders;
    }

}
