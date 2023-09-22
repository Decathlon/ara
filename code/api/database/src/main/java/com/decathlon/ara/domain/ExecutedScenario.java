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

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SortNatural;

import com.decathlon.ara.domain.enumeration.Handling;

@Entity
@Table(indexes = @Index(columnList = "run_id"))
public class ExecutedScenario implements Comparable<ExecutedScenario> {

    public static final int CUCUMBER_ID_MAX_SIZE = 640;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "executed_scenario_id")
    @SequenceGenerator(name = "executed_scenario_id", sequenceName = "executed_scenario_id", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id")
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

    public void setRun(Run run) {
        this.run = run;
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
        Comparator<ExecutedScenario> runIdComparator = comparing(ExecutedScenario::getRunId, nullsFirst(naturalOrder()));
        Comparator<ExecutedScenario> featureFileComparator = comparing(ExecutedScenario::getFeatureFile, nullsFirst(naturalOrder()));
        Comparator<ExecutedScenario> nameComparator = comparing(ExecutedScenario::getName, nullsFirst(naturalOrder()));
        Comparator<ExecutedScenario> lineComparator = comparing(e -> Long.valueOf(e.getLine()), nullsFirst(naturalOrder()));
        return nullsFirst(runIdComparator
                .thenComparing(featureFileComparator)
                .thenComparing(nameComparator)
                .thenComparing(lineComparator)).compare(this, other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(featureFile, line, name, getRunId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ExecutedScenario)) {
            return false;
        }
        ExecutedScenario other = (ExecutedScenario) obj;
        return Objects.equals(featureFile, other.featureFile) && line == other.line && Objects.equals(name, other.name)
                && Objects.equals(getRunId(), other.getRunId());
    }

    public static int getCucumberIdMaxSize() {
        return CUCUMBER_ID_MAX_SIZE;
    }

    public Long getId() {
        return id;
    }

    public Long getRunId() {
        return run == null ? null : run.getId();
    }

    public String getFeatureFile() {
        return featureFile;
    }

    public void setFeatureFile(String featureFile) {
        this.featureFile = featureFile;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String getFeatureTags() {
        return featureTags;
    }

    public void setFeatureTags(String featureTags) {
        this.featureTags = featureTags;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCucumberId() {
        return cucumberId;
    }

    public void setCucumberId(String cucumberId) {
        this.cucumberId = cucumberId;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getScreenshotUrl() {
        return screenshotUrl;
    }

    public void setScreenshotUrl(String screenshotUrl) {
        this.screenshotUrl = screenshotUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getLogsUrl() {
        return logsUrl;
    }

    public void setLogsUrl(String logsUrl) {
        this.logsUrl = logsUrl;
    }

    public String getHttpRequestsUrl() {
        return httpRequestsUrl;
    }

    public void setHttpRequestsUrl(String httpRequestsUrl) {
        this.httpRequestsUrl = httpRequestsUrl;
    }

    public String getJavaScriptErrorsUrl() {
        return javaScriptErrorsUrl;
    }

    public void setJavaScriptErrorsUrl(String javaScriptErrorsUrl) {
        this.javaScriptErrorsUrl = javaScriptErrorsUrl;
    }

    public String getDiffReportUrl() {
        return diffReportUrl;
    }

    public void setDiffReportUrl(String diffReportUrl) {
        this.diffReportUrl = diffReportUrl;
    }

    public String getCucumberReportUrl() {
        return cucumberReportUrl;
    }

    public void setCucumberReportUrl(String cucumberReportUrl) {
        this.cucumberReportUrl = cucumberReportUrl;
    }

    public String getApiServer() {
        return apiServer;
    }

    public void setApiServer(String apiServer) {
        this.apiServer = apiServer;
    }

    public String getSeleniumNode() {
        return seleniumNode;
    }

    public void setSeleniumNode(String seleniumNode) {
        this.seleniumNode = seleniumNode;
    }

    public Set<Error> getErrors() {
        return errors;
    }

    public Run getRun() {
        return run;
    }

}
