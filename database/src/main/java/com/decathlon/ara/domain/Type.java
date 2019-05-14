package com.decathlon.ara.domain;

import java.util.Comparator;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
public class Type implements Comparable<Type> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    private long projectId;

    private String code;

    private String name;

    private boolean isBrowser;

    private boolean isMobile;

    /**
     * The source where .feature or .json (depending on source's technology) files are stored on VCS.<br>
     * CAN be null: in this case, a run of this type will not be indexed in the execution.<br>
     * If an unknown type is found during execution indexation, an error is thrown, so a type with null source must be
     * created to indicate it is not an mis-configuration.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Source source;

    @Override
    public int compareTo(Type other) {
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<Type> projectIdComparator = comparing(t -> Long.valueOf(t.getProjectId()), nullsFirst(naturalOrder()));
        Comparator<Type> codeComparator = comparing(Type::getCode, nullsFirst(naturalOrder()));
        return nullsFirst(projectIdComparator
                .thenComparing(codeComparator)).compare(this, other);
    }

}
