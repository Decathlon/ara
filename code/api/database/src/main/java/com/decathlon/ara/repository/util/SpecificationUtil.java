package com.decathlon.ara.repository.util;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.domain.Type;
import com.decathlon.ara.domain.enumeration.DefectExistence;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.domain.enumeration.ProblemStatusFilter;
import com.decathlon.ara.domain.filter.ProblemFilter;

import liquibase.util.StringUtil;

public class SpecificationUtil {

    private static final String NAME_ATTRIBUTE = "name";
    private static final String CODE_ATTRIBUTE = "code";
    private static final String PROJECT_ID_ATTRIBUTE = "projectId";
    private static final char LIKE_CHAR = '%';

    private SpecificationUtil() {
    }

    private static Predicate like(CriteriaBuilder criteriaBuilder, Expression<String> expression, String value, boolean startWith, boolean endWith, boolean caseSensitive) {
        StringBuilder likeBuilder = new StringBuilder();
        if (endWith) {
            likeBuilder.append(LIKE_CHAR);
        }
        likeBuilder.append(value);
        if (startWith) {
            likeBuilder.append(LIKE_CHAR);
        }
        if (caseSensitive) {
            return criteriaBuilder.like(expression, likeBuilder.toString());
        } else {
            return ignoreCase(criteriaBuilder, expression, likeBuilder.toString(), criteriaBuilder::like);
        }
    }

    private static Predicate ignoreCase(CriteriaBuilder criteriaBuilder, Expression<String> expression, String value, BiFunction<Expression<String>, String, Predicate> predicateFunction) {
        return predicateFunction.apply(criteriaBuilder.lower(expression), value.toLowerCase());
    }

    private static Predicate containsIgnoreCase(CriteriaBuilder criteriaBuilder, Expression<String> expression, String value) {
        return like(criteriaBuilder, expression, value, true, true, false);
    }

    private static Predicate startsWith(CriteriaBuilder criteriaBuilder, Expression<String> expression, String value) {
        return like(criteriaBuilder, expression, value, true, false, true);
    }

    private static void addEqualsOrStartWithPredicate(List<Predicate> predicates, CriteriaBuilder criteriaBuilder, Expression<String> expression, String value, boolean startWith) {
        if (StringUtils.isNotEmpty(value)) {
            if (startWith) {
                predicates.add(startsWith(criteriaBuilder, expression, value));
            } else {
                predicates.add(criteriaBuilder.equal(expression, value));
            }
        }
    }

    private static void addEqualsPredicate(List<Predicate> predicates, CriteriaBuilder criteriaBuilder, Expression<String> expression, String value) {
        if (StringUtil.isNotEmpty(value)) {
            predicates.add(criteriaBuilder.equal(expression, value));
        }
    }

    private static void addProblemDefectIdPredicate(List<Predicate> predicates, CriteriaBuilder criteriaBuilder, Path<Problem> problem, String defectId) {
        if (StringUtils.isNotEmpty(defectId)) {
            Path<String> defectIdPath = problem.get("defectId");
            if ("none".equalsIgnoreCase(defectId)) {
                predicates.add(criteriaBuilder.or(criteriaBuilder.isNull(defectIdPath), criteriaBuilder.equal(defectIdPath, "")));
            } else {
                predicates.add(containsIgnoreCase(criteriaBuilder, defectIdPath, defectId));
            }
        }
    }

    private static void addProblemStatusPredicate(List<Predicate> predicates, CriteriaBuilder criteriaBuilder, Path<Problem> problem, ProblemStatusFilter problemStatusFilter) {
        if (problemStatusFilter != null) {
            Path<ProblemStatus> status = problem.get("status");
            final Predicate open = criteriaBuilder.equal(status, ProblemStatus.OPEN);
            final Predicate closed = criteriaBuilder.equal(status, ProblemStatus.CLOSED);

            Path<Date> closingDateTime = problem.get("closingDateTime");
            Path<Date> lastSeenDateTime = problem.get("lastSeenDateTime");
            // This business logic is also present in another form in Problem.getEffectiveStatus()
            final Predicate reappeared = criteriaBuilder.and(criteriaBuilder.equal(status, ProblemStatus.CLOSED),
                    criteriaBuilder.isNotNull(closingDateTime), criteriaBuilder.isNotNull(lastSeenDateTime),
                    criteriaBuilder.lessThan(closingDateTime, lastSeenDateTime));

            predicates.add(switch (problemStatusFilter) {
                case OPEN -> open;
                case CLOSED -> criteriaBuilder.and(closed, reappeared.not());
                case REAPPEARED -> reappeared;
                case OPEN_OR_REAPPEARED -> criteriaBuilder.or(open, reappeared);
                default -> closed; // CLOSED status includes the REAPPEARED effectiveStatus
            });
        }
    }

    public static Specification<Problem> toProblemSpecification(ProblemFilter filter) {
        return (problem, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(problem.get(PROJECT_ID_ATTRIBUTE), filter.getProjectId()));

            String name = filter.getName();
            if (StringUtils.isNotEmpty(name)) {
                predicates.add(containsIgnoreCase(criteriaBuilder, problem.get(NAME_ATTRIBUTE), name));
            }

            addProblemStatusPredicate(predicates, criteriaBuilder, problem, filter.getStatus());

            Long blamedTeam = filter.getBlamedTeamId();
            if (blamedTeam != null) {
                predicates.add(criteriaBuilder.equal(problem.get("blamedTeam"), blamedTeam));
            }
            addProblemDefectIdPredicate(predicates, criteriaBuilder, problem, filter.getDefectId());
            DefectExistence defectExistence = filter.getDefectExistence();
            if (defectExistence != null) {
                predicates.add(criteriaBuilder.equal(problem.get("defectExistence"), defectExistence));
            }

            Long rootCauseId = filter.getRootCauseId();
            if (rootCauseId != null) {
                predicates.add(criteriaBuilder.equal(problem.get("rootCause"), rootCauseId));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    public static Specification<ExecutedScenario> toExecutedScenarioSpecification(long projectId, String cucumberId, String branch, String cycleName, String countryCode, String runTypeCode, Optional<Period> duration) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Path<Run> run = root.get("run");
            Path<Execution> execution = run.get("execution");
            Path<Country> country = run.get("country");
            Path<Type> type = run.get("type");
            predicates.add(criteriaBuilder.equal(execution.get("cycleDefinition").get(PROJECT_ID_ATTRIBUTE), projectId));
            predicates.add(criteriaBuilder.equal(root.get("cucumberId"), cucumberId));
            addEqualsPredicate(predicates, criteriaBuilder, execution.get("branch"), branch);
            addEqualsPredicate(predicates, criteriaBuilder, execution.get(NAME_ATTRIBUTE), cycleName);
            addEqualsPredicate(predicates, criteriaBuilder, country.get(CODE_ATTRIBUTE), countryCode);
            addEqualsPredicate(predicates, criteriaBuilder, type.get(CODE_ATTRIBUTE), runTypeCode);

            var today = LocalDateTime.now();
            var startDate = duration
                    .map(today::minus)
                    .map(localDateTime -> localDateTime.atZone(ZoneId.systemDefault()))
                    .map(ChronoZonedDateTime::toInstant)
                    .map(Date::from);
            if (startDate.isPresent()) {
                predicates.add(criteriaBuilder.greaterThan(execution.get("testDateTime"), startDate.get()));
            }

            criteriaQuery.orderBy(criteriaBuilder.asc(execution.get("testDateTime")),
                    criteriaBuilder.asc(country.get(CODE_ATTRIBUTE)),
                    criteriaBuilder.asc(type.get(CODE_ATTRIBUTE)),
                    criteriaBuilder.asc(root.get("line")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    public static Specification<Error> toErrorSpecification(long projectId, ProblemPattern problemPattern, List<Long> errorIds) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Path<ExecutedScenario> executedScenario = root.get("executedScenario");
            Path<Run> run = executedScenario.get("run");
            Path<Execution> execution = run.get("execution");
            Path<Type> typePath = run.get("type");
            predicates.add(criteriaBuilder.equal(execution.get("cycleDefinition").get(PROJECT_ID_ATTRIBUTE), projectId));
            addEqualsPredicate(predicates, criteriaBuilder, executedScenario.get("featureFile"), problemPattern.getFeatureFile());
            addEqualsPredicate(predicates, criteriaBuilder, executedScenario.get("featureName"), problemPattern.getFeatureName());
            addEqualsOrStartWithPredicate(predicates, criteriaBuilder, executedScenario.get(NAME_ATTRIBUTE), problemPattern.getScenarioName(), problemPattern.isScenarioNameStartsWith());
            addEqualsOrStartWithPredicate(predicates, criteriaBuilder, root.get("step"), problemPattern.getStep(), problemPattern.isStepStartsWith());
            addEqualsOrStartWithPredicate(predicates, criteriaBuilder, root.get("stepDefinition"), problemPattern.getStepDefinition(), problemPattern.isStepDefinitionStartsWith());
            addEqualsPredicate(predicates, criteriaBuilder, root.get("exception"), problemPattern.getException());
            addEqualsPredicate(predicates, criteriaBuilder, execution.get("release"), problemPattern.getRelease());
            Country country = problemPattern.getCountry();
            if (country != null && StringUtils.isNotEmpty(country.getCode())) {
                predicates.add(criteriaBuilder.equal(run.get("country").get(CODE_ATTRIBUTE), country.getCode()));
            }
            addEqualsPredicate(predicates, criteriaBuilder, run.get("platform"), problemPattern.getPlatform());
            Type type = problemPattern.getType();
            if (type != null && StringUtils.isNotEmpty(type.getCode())) {
                predicates.add(criteriaBuilder.equal(typePath.get(CODE_ATTRIBUTE), type.getCode()));
            }

            Boolean typeIsBrowser = problemPattern.getTypeIsBrowser();
            if (typeIsBrowser != null) {
                predicates.add(criteriaBuilder.equal(typePath.get("isBrowser"), typeIsBrowser));
            }

            Boolean typeIsMobile = problemPattern.getTypeIsMobile();
            if (typeIsMobile != null) {
                predicates.add(criteriaBuilder.equal(typePath.get("isMobile"), typeIsMobile));
            }
            if (errorIds != null) {
                predicates.add(run.get("id").in(errorIds));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

}
