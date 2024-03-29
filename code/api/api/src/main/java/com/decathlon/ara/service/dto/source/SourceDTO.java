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

package com.decathlon.ara.service.dto.source;

import static com.decathlon.ara.service.support.DtoConstants.CODE_MESSAGE;
import static com.decathlon.ara.service.support.DtoConstants.CODE_PATTERN;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import java.util.Comparator;
import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.decathlon.ara.domain.enumeration.Technology;

public class SourceDTO implements Comparable<SourceDTO> {

    /**
     * Fake source that encompass all ones, to have a global summary.
     */
    public static final SourceDTO ALL = new SourceDTO("*", "All", "*", null, null, null, false);

    @NotNull(message = "The code is required.")
    @Size(min = 1, max = 16, message = "The code is required and must not exceed {max} characters.")
    @Pattern(regexp = CODE_PATTERN, message = CODE_MESSAGE)
    private String code;

    @NotNull(message = "The name is required.")
    @Size(min = 1, max = 32, message = "The name is required and must not exceed {max} characters.")
    private String name;

    @NotNull(message = "The letter is required.")
    @Size(min = 1, max = 1, message = "The letter is required and must not exceed {max} characters.")
    private String letter;

    @NotNull(message = "The technology is required.")
    private Technology technology;

    @NotNull(message = "The Version Control System URL is required.")
    @Size(min = 1, max = 256,
            message = "The Version Control System URL is required and must not exceed {max} characters.")
    @Pattern(regexp = ".*\\{\\{branch}}.*", message = "The Version Control System URL must have a {{branch}} placeholder.")
    private String vcsUrl;

    @NotNull(message = "The default branch is required.")
    @Size(min = 1, max = 16, message = "The default branch is required and must not exceed {max} characters.")
    private String defaultBranch;

    private boolean postmanCountryRootFolders;

    public SourceDTO() {
    }

    public SourceDTO(
            String code,
            String name,
            String letter,
            Technology technology,
            String vcsUrl,
            String defaultBranch,
            boolean postmanCountryRootFolders) {
        this.code = code;
        this.name = name;
        this.letter = letter;
        this.technology = technology;
        this.vcsUrl = vcsUrl;
        this.defaultBranch = defaultBranch;
        this.postmanCountryRootFolders = postmanCountryRootFolders;
    }

    @Override
    public int compareTo(SourceDTO other) {
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<SourceDTO> codeComparator = comparing(SourceDTO::getCode, nullsFirst(naturalOrder()));
        return nullsFirst(codeComparator).compare(this, other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SourceDTO)) {
            return false;
        }
        SourceDTO other = (SourceDTO) obj;
        return Objects.equals(code, other.code);
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

    public String getLetter() {
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
