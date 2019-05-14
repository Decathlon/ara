package com.decathlon.ara.domain;

import com.decathlon.ara.domain.enumeration.Technology;
import java.util.Comparator;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import org.hibernate.annotations.GenericGenerator;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
@Entity
// Keep business key in sync with compareTo(): see https://developer.jboss.org/wiki/EqualsAndHashCode
@EqualsAndHashCode(of = { "projectId", "code" })
public class Source implements Comparable<Source> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    private long projectId;

    /**
     * Technical and short code to be used by the build system to send Cucumber-scenarios and Postman-requests as they
     * sit on the Version Control System, for indexation purpose, and for functional coverage computation.
     */
    private String code;

    /**
     * Full name displayed in functionality cartography/coverage.
     */
    private String name;

    /**
     * Unique recognizable letter to be displayed in functionality cartography/coverage where space is very limited.
     */
    private char letter;

    /**
     * The technology used for tests stored in this VCS source: Cucumber .feature files, Postman .json collections...
     */
    @Enumerated(EnumType.STRING)
    private Technology technology;

    /**
     * The URL where the source files are stored (and could be edited, if possible).<br>
     * Used for functionality coverage and ignored scenarios tracking.<br>
     * The special "{{branch}}" token will be replaced by the VCS branch.<br>
     * Eg. "https://git.company.com/project/edit/{{branch}}/src/main/resources/"
     *
     * @see #defaultBranch defaultBranch used to replace "{{branch}}" when indexing functionalities coverage
     */
    private String vcsUrl;

    /**
     * The primary reference branch to use with {@link #vcsUrl} when indexing functionalities coverage.
     */
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

}
