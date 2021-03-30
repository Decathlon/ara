package com.decathlon.ara.v2.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.*;
import java.io.Serializable;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ara_scenario_error_trace_pattern")
public class ScenarioErrorTracePattern implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scenario_error_trace_pattern_seq")
    private Long id;

    private String pattern;

    private SnapshotFilterValue snapshotFilterValue;

    @ManyToOne
    private Problem problem;

    @Data
    @With
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class SnapshotFilterValue implements Serializable {

        @Column(name = "snapshot_filter_release")
        private String release;

        @Column(name = "snapshot_filter_technology")
        private String technology;

        @Column(name = "snapshot_filter_tag")
        private String tag;

        @Column(name = "snapshot_filter_environment")
        private String environment;

        @Column(name = "snapshot_filter_scenario_name")
        private String scenario;

        @Column(name = "snapshot_filter_step_content")
        private String step;
    }
}
