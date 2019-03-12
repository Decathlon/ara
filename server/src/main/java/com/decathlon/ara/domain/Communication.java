package com.decathlon.ara.domain;

import com.decathlon.ara.domain.enumeration.CommunicationType;
import java.util.Comparator;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
public class Communication implements Comparable<Communication> {

    public static final String EXECUTIONS = "executions";
    public static final String SCENARIO_WRITING_HELPS = "scenario-writing-helps";
    public static final String HOW_TO_ADD_SCENARIO = "how" + "to-add-scenario";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    // 1/2 for @EqualsAndHashCode to work: used when an entity is fetched by JPA
    @Column(name = "project_id", insertable = false, updatable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    private String code;

    private String name;

    @Enumerated(EnumType.STRING)
    private CommunicationType type;

    @Lob
    private String message;

    // 2/2 for @EqualsAndHashCode to work: used for entities created outside of JPA
    public void setProject(Project project) {
        this.project = project;
        this.projectId = (project == null ? null : project.getId());
    }

    @Override
    public int compareTo(Communication other) {
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<Communication> projectIdComparator = comparing(c -> c.projectId, nullsFirst(naturalOrder()));
        Comparator<Communication> codeComparator = comparing(Communication::getCode, nullsFirst(naturalOrder()));
        return nullsFirst(projectIdComparator
                .thenComparing(codeComparator)).compare(this, other);
    }

}
