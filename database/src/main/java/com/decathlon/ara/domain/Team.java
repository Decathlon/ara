package com.decathlon.ara.domain;

import java.util.ArrayList;
import java.util.List;
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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Wither
// Keep business key in sync with compareTo(): see https://developer.jboss.org/wiki/EqualsAndHashCode
@EqualsAndHashCode(of = { "projectId", "name" })
public class Team {

    public static final Team NOT_ASSIGNED = new Team(Long.valueOf(-404), -404, "(No team)", true, false, new ArrayList<>());

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    private long projectId;

    private String name;

    private boolean assignableToProblems;

    private boolean assignableToFunctionalities;

    // No cascade, as this collection is only used while removing a team
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "blamedTeam")
    private List<Problem> problems = new ArrayList<>();

}
