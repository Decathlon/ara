package com.decathlon.ara.v2.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ara_scenario_step")
public class ScenarioStep {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scenario_step_seq")
    private Long id;

    private int line;

    private String content;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScenarioStep that = (ScenarioStep) o;
        return line == that.line && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, content);
    }
}
