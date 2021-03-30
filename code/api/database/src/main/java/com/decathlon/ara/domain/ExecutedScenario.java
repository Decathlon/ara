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

import com.decathlon.ara.domain.enumeration.Handling;
import com.decathlon.ara.v2.domain.Feature;
import com.decathlon.ara.v2.domain.ScenarioStep;
import com.decathlon.ara.v2.domain.ScenarioVersion;
import com.querydsl.core.annotations.QueryInit;
import lombok.*;
import org.hibernate.annotations.*;
import org.springframework.data.util.Pair;
import org.springframework.util.CollectionUtils;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.*;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
@Entity
// Keep business key in sync with compareTo(): see https://developer.jboss.org/wiki/EqualsAndHashCode
@EqualsAndHashCode(of = { "runId", "featureFile", "name", "line" })
@Table(indexes = @Index(columnList = "run_id"))
public class ExecutedScenario implements Comparable<ExecutedScenario>, Serializable {

    public static final int CUCUMBER_ID_MAX_SIZE = 640;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "executed_scenario_id")
    @SequenceGenerator(name = "executed_scenario_id", sequenceName = "executed_scenario_id", allocationSize = 1)
    private Long id;

    // 1/2 for @EqualsAndHashCode to work: used when an entity is fetched by JPA
    @Column(name = "run_id", insertable = false, updatable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Long runId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id")
    @QueryInit("*.*") // Requires Q* class regeneration https://github.com/querydsl/querydsl/issues/255
    private Run run;

    private String featureFile;

    private String featureName;

    private String featureTags;

    private String tags;

    @Column(length = 32)
    private String severity;

    @Column(length = 512)
    private String name;

    @Column(length = 640)
    private String cucumberId;

    private int line;

    @Lob
    @org.hibernate.annotations.Type(type = "org.hibernate.type.TextType")
    private String content;

    @Column(name = "start_date_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDateTime;

    @Column(length = 512)
    private String screenshotUrl;

    @Column(length = 512)
    private String videoUrl;

    @Column(length = 512)
    private String logsUrl;

    @Column(length = 512)
    private String httpRequestsUrl;

    @Column(length = 512)
    private String javaScriptErrorsUrl;

    @Column(length = 512)
    private String diffReportUrl;

    @Column(length = 512)
    private String cucumberReportUrl;

    @Column(length = 16)
    private String apiServer;

    @Column(length = 128)
    private String seleniumNode;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "executedScenario", orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @SortNatural
    @Fetch(FetchMode.SUBSELECT)
    private Set<Error> errors = new TreeSet<>();

    // 2/2 for @EqualsAndHashCode to work: used for entities created outside of JPA
    public void setRun(Run run) {
        this.run = run;
        this.runId = (run == null ? null : run.getId());
    }

    public void addError(Error error) {
        // Set the child-entity's foreign-key BEFORE adding the child-entity to the TreeSet,
        // as the foreign-key is required to place the child-entity in the right order (with child-entity's compareTo)
        // and is required not to change while the child-entity is in the TreeSet
        error.setExecutedScenario(this);
        this.errors.add(error);
    }

    public void addErrors(Iterable<? extends Error> errorsToAdd) {
        for (Error error : errorsToAdd) {
            addError(error);
        }
    }

    public void removeError(Error error) {
        this.errors.remove(error);
        error.setExecutedScenario(null);
    }

    /**
     * @return SUCCESS if the scenario has no error, HANDLED if at least one error has at least one problem that is open
     * or did not reappear after closing date, UNHANDLED otherwise (has errors with only open or reappeared problems)
     */
    public Handling getHandling() {
        if (getErrors().isEmpty()) {
            return Handling.SUCCESS;
        }

        for (Error error : getErrors()) {
            for (ProblemOccurrence problemOccurrence : error.getProblemOccurrences()) {
                var problemPattern = problemOccurrence.getProblemPattern();
                if (problemPattern.getProblem().isHandled()) {
                    return Handling.HANDLED;
                }
            }
        }

        return Handling.UNHANDLED;
    }

    @Override
    public int compareTo(ExecutedScenario other) {
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<ExecutedScenario> runIdComparator = comparing(e -> e.runId, nullsFirst(naturalOrder()));
        Comparator<ExecutedScenario> featureFileComparator = comparing(ExecutedScenario::getFeatureFile, nullsFirst(naturalOrder()));
        Comparator<ExecutedScenario> nameComparator = comparing(ExecutedScenario::getName, nullsFirst(naturalOrder()));
        Comparator<ExecutedScenario> lineComparator = comparing(e -> Long.valueOf(e.getLine()), nullsFirst(naturalOrder()));
        return nullsFirst(runIdComparator
                .thenComparing(featureFileComparator)
                .thenComparing(nameComparator)
                .thenComparing(lineComparator)).compare(this, other);
    }

    /**
     * Split the (raw) name into 2 parts:
     *      - the name without functionality codes
     *      - a list containing those functionality codes
     * @return a pair
     */
    public Pair<String, List<String>> getNameWithoutCodesAndFunctionalityCodesFromScenarioName() {
        return Scenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName(name);
    }

    /**
     * Return true iff an executed scenario matches a legacy scenario and a source
     * @param legacyScenario the legacy scenario
     * @param legacySource the legacy source
     * @return true iff an executed scenario matches a legacy scenario and a source
     */
    public boolean matchesScenario(Scenario legacyScenario, Source legacySource) {
        final var scenarioName = legacyScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName().getFirst();
        final var executedScenarioName = getNameWithoutCodesAndFunctionalityCodesFromScenarioName().getFirst();

        var sameFeatureFiles = Objects.equals(featureFile, legacyScenario.getFeatureFile());
        var sameScenarioNames = Objects.equals(scenarioName, executedScenarioName);
        var sameSources = Objects.equals(legacySource, legacyScenario.getSource());
        return sameFeatureFiles && sameScenarioNames && sameSources;
    }

    /**
     * Get stateless scenario steps, i.e. steps without value and state (line and content only)
     * @return stateless scenario steps
     */
    public List<Scenario.ScenarioStep> getStatelessScenarioSteps() {
        var splitContents = Scenario.getSplitContents(content);
        return splitContents.stream()
                .map(splitLine -> new Scenario.ScenarioStep()
                        .withLine(Integer.valueOf(splitLine[0]))
                        .withContent(splitLine[splitLine.length - 1])
                )
                .collect(Collectors.toList());
    }

    /**
     * Get executed scenario steps (i.e. contain value and state)
     * @return executed scenario steps
     */
    public List<ExecutedScenarioStep> getExecutedScenarioSteps() {
        var splitContents = Scenario.getSplitContents(content);
        return splitContents.stream()
                .map(splitLine -> new ExecutedScenarioStep(
                        Integer.valueOf(splitLine[0]),
                        splitLine[splitLine.length - 1],
                        splitLine.length == 4 ? splitLine[2] : null,
                        splitLine[1]
                        )
                )
                .collect(Collectors.toList());
    }

    /**
     * Return true iff 2 executed scenarios share the same functionality codes
     * @param executedScenario the executed scenario to compare with
     * @return true iff 2 executed scenarios share the same functionality codes
     */
    public boolean shareTheSameFunctionalityCodesAs(ExecutedScenario executedScenario) {
        final var codes1 = getNameWithoutCodesAndFunctionalityCodesFromScenarioName().getSecond();
        final var codes2 = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName().getSecond();

        return codes1.equals(codes2);
    }

    /**
     * Return true iff 2 executed scenarios share the same steps
     * @param executedScenario the executed scenario to compare with
     * @return true iff 2 executed scenarios share the same steps
     */
    public boolean shareTheSameStepsAs(ExecutedScenario executedScenario) {
        var steps1 = getExecutedScenarioSteps();
        var steps2 = executedScenario.getExecutedScenarioSteps();
        return steps1.equals(steps2);
    }

    @Getter
    public static class ExecutedScenarioStep extends Scenario.ScenarioStep {

        private String value;

        private String state;

        public ExecutedScenarioStep(int line, String content, String value, String state) {
            super(line, content);
            this.value = value;
            this.state = state;
        }

        public String getState() {
            return state;
        }

        public Optional<String> getValue() {
            return Optional.ofNullable(value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            ExecutedScenarioStep that = (ExecutedScenarioStep) o;
            return Objects.equals(state, that.state);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), state);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @With
    public static class ExtendedExecutedScenario {
        private ExecutedScenario legacyExecutedScenario;
        private Source legacySource;
        private String branchName;

        /**
         * Get a matching legacy scenario, if found
         * @param allLegacyScenarios the legacy scenarios to browse
         * @return a matching legacy scenario, if found
         */
        public Optional<com.decathlon.ara.domain.Scenario> getPotentialMatchingScenario(
                List<com.decathlon.ara.domain.Scenario> allLegacyScenarios
        ) {
            return allLegacyScenarios.stream()
                    .filter(scenario -> legacyExecutedScenario.matchesScenario(scenario, legacySource))
                    .findFirst();
        }

        /**
         * Get a matching migration scenario version, if found
         * @param migrationScenarios the migrations scenarios to browse
         * @return a matching migration scenario version, if found
         */
        public Optional<ScenarioVersion> getMatchingMigrationScenarioVersion(List<com.decathlon.ara.v2.domain.Scenario> migrationScenarios) {
            if (CollectionUtils.isEmpty(migrationScenarios)) {
                return Optional.empty();
            }
            var migrationScenario = migrationScenarios.stream()
                    .filter(this::shareTheSameNameAsAScenario)
                    .filter(this::shareTheSameTypeAsAScenario)
                    .findFirst();
            if (migrationScenario.isEmpty()) {
                return Optional.empty();
            }
            return migrationScenario
                    .get()
                    .getVersions()
                    .stream()
                    .filter(this::shareTheSameFileNameAsAMigrationScenarioVersion)
                    .filter(this::shareTheSameBranchAsAMigrationScenarioVersion)
                    .filter(this::shareTheSameSeverityAsAMigrationScenarioVersion)
                    .filter(this::shareExactlyTheSameFunctionalitiesAsAMigrationScenarioVersion)
                    .filter(this::shareExactlyTheSameStepsAsAMigrationScenarioVersion)
                    .findFirst();
        }

        /**
         * Return true iff it shares the same name as the migration scenario
         * @param migrationScenario the migration scenario
         * @return true iff it shares the same name as the migration scenario
         */
        private boolean shareTheSameNameAsAScenario(
                com.decathlon.ara.v2.domain.Scenario migrationScenario
        ) {
            var migrationScenarioName = migrationScenario.getName();
            var pair = legacyExecutedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName();
            var legacyScenarioName = pair.getFirst();
            return Objects.equals(migrationScenarioName, legacyScenarioName);
        }

        /**
         * Return true iff it shares the same type as the migration scenario
         * @param migrationScenario the migration scenario
         * @return true iff it shares the same type as the migration scenario
         */
        private boolean shareTheSameTypeAsAScenario(
                com.decathlon.ara.v2.domain.Scenario migrationScenario
        ) {
            var scenarioType = migrationScenario.getType();
            var scenarioTypeId = scenarioType.getId();
            return Objects.equals(scenarioTypeId.getCode(), legacySource.getCode());
        }

        /**
         * Return true iff it shares the same file name as the migration scenario version
         * @param migrationScenarioVersion the migration scenario version
         * @return true iff it shares the same file name as the migration scenario version
         */
        private boolean shareTheSameFileNameAsAMigrationScenarioVersion(ScenarioVersion migrationScenarioVersion) {
            var migrationFileName = migrationScenarioVersion.getFileName();
            var legacyFileName = legacyExecutedScenario.getFeatureName();
            return Objects.equals(migrationFileName, legacyFileName);
        }

        /**
         * Return true iff it shares the same branch name as the migration scenario version
         * @param migrationScenarioVersion the migration scenario version
         * @return true iff it shares the same branch name as the migration scenario version
         */
        private boolean shareTheSameBranchAsAMigrationScenarioVersion(ScenarioVersion migrationScenarioVersion) {
            var migrationBranch = migrationScenarioVersion.getBranch();
            var migrationBranchId = migrationBranch.getId();
            var migrationBranchName = migrationBranchId.getCode();
            var legacyBranchName = branchName;
            return Objects.equals(migrationBranchName, legacyBranchName);
        }

        /**
         * Return true iff it shares the same severity as the migration scenario version
         * @param migrationScenarioVersion the migration scenario version
         * @return true iff it shares the same severity as the migration scenario version
         */
        private boolean shareTheSameSeverityAsAMigrationScenarioVersion(ScenarioVersion migrationScenarioVersion) {
            var migrationSeverity = migrationScenarioVersion.getSeverity();
            var migrationSeverityId = migrationSeverity.getId();
            var migrationSeverityCode = migrationSeverityId.getCode();
            var legacySeverityCode = legacyExecutedScenario.getSeverity();
            return Objects.equals(migrationSeverityCode, legacySeverityCode);
        }

        /**
         * Return true iff it shares the same feature codes as the migration scenario version
         * @param migrationScenarioVersion the migration scenario version
         * @return true iff it shares the same feature codes as the migration scenario version
         */
        private boolean shareExactlyTheSameFunctionalitiesAsAMigrationScenarioVersion(ScenarioVersion migrationScenarioVersion) {
            var migrationFeatureCodes = migrationScenarioVersion
                    .getCoveredFeatures()
                    .stream()
                    .map(Feature::getCode)
                    .sorted()
                    .distinct()
                    .collect(Collectors.toList());
            var legacyFunctionalityCodes = legacyExecutedScenario
                    .getNameWithoutCodesAndFunctionalityCodesFromScenarioName()
                    .getSecond()
                    .stream()
                    .sorted()
                    .distinct()
                    .collect(Collectors.toList());
            return Objects.equals(legacyFunctionalityCodes, migrationFeatureCodes);
        }

        /**
         * Return true iff it shares the same steps as the migration scenario version
         * @param migrationScenarioVersion the migration scenario version
         * @return true iff it shares the same steps as the migration scenario version
         */
        private boolean shareExactlyTheSameStepsAsAMigrationScenarioVersion(ScenarioVersion migrationScenarioVersion) {
            var migrationSteps = migrationScenarioVersion.getSteps();
            var convertedLegacySteps = legacyExecutedScenario.getStatelessScenarioSteps()
                    .stream()
                    .map(legacyStep -> new ScenarioStep()
                            .withLine(legacyStep.getLine())
                            .withContent(legacyStep.getContent())
                    )
                    .collect(Collectors.toList());
            return Objects.equals(migrationSteps, convertedLegacySteps);
        }
    }

    public ExtendedExecutedScenario getExtendedExecutedScenario(String branchName, Source source) {
        return new ExtendedExecutedScenario()
                .withLegacyExecutedScenario(this)
                .withBranchName(branchName)
                .withLegacySource(source);
    }

}
