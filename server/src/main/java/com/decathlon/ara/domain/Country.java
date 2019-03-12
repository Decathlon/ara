package com.decathlon.ara.domain;

import java.util.Comparator;
import javax.persistence.Entity;
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
public class Country implements Comparable<Country> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    private long projectId;

    private String code;

    private String name;

    @Override
    public int compareTo(Country other) {
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<Country> projectIdComparator = comparing(c -> Long.valueOf(c.getProjectId()), nullsFirst(naturalOrder()));
        Comparator<Country> codeComparator = comparing(Country::getCode, nullsFirst(naturalOrder()));
        return nullsFirst(projectIdComparator
                .thenComparing(codeComparator)).compare(this, other);
    }

}
