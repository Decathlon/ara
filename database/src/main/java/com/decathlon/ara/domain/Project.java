package com.decathlon.ara.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
@Entity
// Keep business key in sync with compareTo(): see https://developer.jboss.org/wiki/EqualsAndHashCode
@EqualsAndHashCode(of = { "code" })
public class Project implements Comparable<Project> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    private String code;

    private String name;

    /**
     * True to use that project as the default one appearing at ARA's client startup when no project code is present in
     * URL. Only one project can be declared as the default.
     */
    private boolean defaultAtStartup;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "projectId", orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Communication> communications = new ArrayList<>();

    public void addCommunication(Communication communication) {
        // Set the child-entity's foreign-key BEFORE adding the child-entity to the TreeSet,
        // as the foreign-key is required to place the child-entity in the right order (with child-entity's compareTo)
        // and is required not to change while the child-entity is in the TreeSet
        communication.setProject(this);
        communications.add(communication);
    }

    @Override
    public int compareTo(Project other) {
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<Project> codeComparator = comparing(Project::getCode, nullsFirst(naturalOrder()));
        return nullsFirst(codeComparator).compare(this, other);
    }

}
