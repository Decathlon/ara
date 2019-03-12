package com.decathlon.ara.web.rest;

import com.decathlon.ara.Entities;
import com.decathlon.ara.service.FeatureService;
import com.decathlon.ara.service.dto.feature.DetailledFeatureDTO;
import com.decathlon.ara.service.dto.feature.FeatureDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import com.decathlon.ara.web.rest.util.RestConstants;
import io.micrometer.core.annotation.Timed;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This REST resource will handle the Feature flipping of ARA, to enable or disable experimental features.
 *
 * It will not use BDD based values, due to the fact the flipping should not be shared between instances if several
 * instances of Ara use the same database. This will provide an easy way to do Canary deployments or A/B testing.
 *
 * @author Sylvain Nieuwlandt
 */
@RestController
@RequestMapping(FeatureResource.PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FeatureResource {

    private static final String NAME = Entities.FEATURE;
    /**
     * The full path to this Rest resource from the basename.
     */
    static final String PATH = RestConstants.API_PATH + "/" + NAME + "s";

    @NonNull
    private FeatureService featureService;

    /**
     * List will return all the features available with their current state.
     *
     * @return the features available (minimum informations) in a HTTP 200 response.
     */
    @GetMapping("")
    @Timed
    public ResponseEntity<List<FeatureDTO>> list() {
        return ResponseEntity.ok(this.featureService.listAll());
    }

    /**
     * Describe will return all the informations about a given feature.
     *
     * @param code the code of the feature
     * @return all the information about the given feature in a HTTP 200 response, or a HTTP 404 response if the code
     *  doesn't match an existing feature.
     */
    @GetMapping("/{code:[a-z_-]+}")
    @Timed
    public ResponseEntity<DetailledFeatureDTO> describe(@PathVariable String code) {
        try {
            DetailledFeatureDTO detailledFeature = this.featureService.find(code);
            return ResponseEntity.ok(detailledFeature);
        } catch (NotFoundException ex) {
            return ResponseUtil.handle(ex);
        }
    }

    /**
     * StateOf will return all the informations about a given feature.
     *
     * @param code the code of the feature
     * @return the state of the feature and its code.
     */
    @GetMapping("/{code:[a-z_-]+}/state")
    @Timed
    public ResponseEntity<FeatureDTO> stateOf(@PathVariable String code) {
        try {
            DetailledFeatureDTO detailledFeature = this.featureService.find(code);
            FeatureDTO result = new FeatureDTO(detailledFeature.getCode(), detailledFeature.isEnabled());
            return ResponseEntity.ok(result);
        } catch (NotFoundException ex) {
            return ResponseUtil.handle(ex);
        }
    }

    /**
     * Retrieve the default setting of the given feature.
     *
     * @param code the code of the feature
     * @return the default setting of the given feature in a HTTP 200 response, or a HTTP 404 response if the code
     * doesn't match an existing feature.
     */
    @GetMapping("/{code:[a-z_-]+}/default")
    @Timed
    public ResponseEntity<FeatureDTO> retrieveDefaultSetting(@PathVariable String code) {
        try {
            FeatureDTO feature = this.featureService.findDefaultOf(code);
            return ResponseEntity.ok(feature);
        } catch (NotFoundException ex) {
            return ResponseUtil.handle(ex);
        }
    }
    /**
     * Update the given list of features to the new state.
     *
     * If the list contains FeatureDTO(code=my-feature, enabled=true), then it will update my-feature to the state
     * ENABLED, even if it was already in this state.
     *
     * Note that if at least one feature in the list doesn't exists, then this request will be rollbacked, and all the
     * elements in the list will not be updated.
     *
     * @param featuresToUpdate the list of features to update with their new state
     * @return the list of updated features with their state in a HTTP 200 response, or a HTTP 404 response if at least
     *  one feature code doesn't exists.
     */
    @PatchMapping("")
    @Timed
    public ResponseEntity<List<FeatureDTO>> updateAll(@RequestBody List<FeatureDTO> featuresToUpdate) {
        try {
            this.featureService.update(featuresToUpdate);
        } catch (NotFoundException ex) {
            return ResponseUtil.handle(ex);
        }
        List<FeatureDTO> updatedList = this.featureService.retrieveStateOf(featuresToUpdate.stream()
                .map(FeatureDTO::getCode)
                .collect(Collectors.toList()));
        return ResponseEntity.ok(updatedList);
    }

    /**
     * Update the given feature with the given state.
     *
     * @param code the code of the feature to update
     * @param enabled the new state to apply to the feature.
     * @return all the information of the updated feature in a HTTP 200 response, or a HTTP 404 response if the given
     *  code doesn't match an existing feature.
     */
    @PatchMapping("/{code:[a-z_-]+}")
    @Timed
    public ResponseEntity<DetailledFeatureDTO> update(@PathVariable String code, @RequestParam boolean enabled) {
        FeatureDTO wantedFeature = new FeatureDTO(code, enabled);
        try {
            this.featureService.update(Collections.singletonList(wantedFeature));
        } catch (NotFoundException ex) {
            return ResponseUtil.handle(ex);
        }
        return this.describe(code);
    }

    /**
     * Reset the given list of features to their default state.
     *
     * The default state of the feature will be calculated with this priority :
     * - if the admin launch Ara with a flag -Dfeature.my-feature=$state then use $state
     * - if no system property for this feature, then check in the config/feature.properties file for the state
     * - if no system property and no mention in config/feature.properties, then the default is disabled.
     *
     * Note that if at least one feature in the list doesn't exists, then this request will be rollbacked, and all the
     * elements in the list will not be reset.
     *
     * @param featuresToReset the list of features to reset to their default state
     * @return the list of impacted features with their state in a HTTP 200 response, or a HTTP 404 response if at least
     *  one feature code doesn't exists.
     */
    @DeleteMapping("")
    @Timed
    public ResponseEntity<List<FeatureDTO>> resetAll(@RequestBody List<String> featuresToReset) {
        try {
            this.featureService.reset(featuresToReset);
        } catch (NotFoundException ex) {
            return ResponseUtil.handle(ex);
        }
        return ResponseEntity.ok(this.featureService.retrieveStateOf(featuresToReset));
    }

    /**
     * Reset the given feature to its default state.
     *
     * The default state of a feature will be calculated with this priority :
     * - if the admin launch Ara with a flag -Dfeature.my-feature=$state, then use $state
     * - if no system property for this feature, then check in the config/feature.properties file for the state
     * - if no system property and no mention in config/feature.properties, then the default is disabled.
     *
     * @param code the code of the feature to reset
     * @return all the information of the impacted feature in a HTTP 200 response, or a HTTP 404 response if the given
     *  code doesn't match an existing feature.
     */
    @DeleteMapping("/{code:[a-z_-]+}")
    @Timed
    public ResponseEntity<DetailledFeatureDTO> reset(@PathVariable String code) {
        try {
            this.featureService.reset(Collections.singletonList(code));
        } catch (NotFoundException ex) {
            return ResponseUtil.handle(ex);
        }
        return this.describe(code);
    }
}
