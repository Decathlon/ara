package com.decathlon.ara.service;

import com.decathlon.ara.Entities;
import com.decathlon.ara.features.FeatureActivator;
import com.decathlon.ara.features.FeatureCollection;
import com.decathlon.ara.features.IFeature;
import com.decathlon.ara.service.dto.feature.DetailledFeatureDTO;
import com.decathlon.ara.service.dto.feature.FeatureDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The FeatureService will manage CRUD operations on the Feature Flipping settings.
 *
 * @author Sylvain Nieuwlandt
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FeatureService {

    @Autowired
    private FeatureCollection featureCollection;

    @Autowired
    private FeatureActivator featureActivator;

    private static NotFoundException throwFeatureDontExists(String code) {
        return new NotFoundException("The feature '" + code + "' doesn't exists.", Entities.FEATURE);
    }

    /**
     * List all the features available with a minimum of informations.
     *
     * @return the list all the features available.
     */
    public List<FeatureDTO> listAll() {
        return this.featureCollection.list().stream()
                .map(feature -> new FeatureDTO(feature.getCode(), this.featureActivator.getState(feature.getCode())))
                .collect(Collectors.toList());
    }

    /**
     * Return the detailled informations about the given feature's code.
     *
     * @param code the code of the wanted feature
     * @return the detailled informations about the wanted feature.
     * @throws NotFoundException if the given code doesn't match an existing feature
     */
    public DetailledFeatureDTO find(String code) throws NotFoundException {
        return this.featureCollection.get(code)
                .map(feature -> new DetailledFeatureDTO(
                        feature.getCode(),
                        this.featureActivator.getState(feature.getCode()),
                        feature.getName(),
                        feature.getDescription()))
                .orElseThrow(() -> throwFeatureDontExists(code));
    }

    /**
     * Return the default status (true if enabled, false if not) of the wanted feature.
     *
     * @param code the code of the wanted feature.
     * @return true if enabled by default, false if disabled by default
     * @throws NotFoundException if the code doesn't match an existing feature.
     */
    public FeatureDTO findDefaultOf(String code) throws NotFoundException {
        IFeature wantedFeature = this.featureCollection.get(code).orElseThrow(() -> throwFeatureDontExists(code));
        return new FeatureDTO(wantedFeature.getCode(), this.featureActivator.isEnabledByDefault(wantedFeature.getCode()));
    }

    /**
     * Return the minimum informations about the given features' code.
     *
     * If one code doesn't match in the list, it will be excluded from the result.
     *
     * @param codes the code of the wanted features
     * @return the minimum informations (usually only the state) of the wanted features.
     */
    public List<FeatureDTO> retrieveStateOf(List<String> codes) {
        return codes.stream()
                .filter(code -> this.featureCollection.get(code).isPresent())
                .map(code -> new FeatureDTO(code, this.featureActivator.getState(code)))
                .collect(Collectors.toList());
    }

    /**
     * Update the given features list with the wanted states.
     * <p>
     * Note that if at least one feature in the list throw a <code>NotFoundException</code>, then it will not update any
     * element in the list (all or none).
     *
     * @param featuresToUpdate the wanted features to update with their wanted states.
     * @throws NotFoundException if at least one feature code doesn't match an existing feature.
     */
    public void update(List<FeatureDTO> featuresToUpdate) throws NotFoundException {
        String codeInErrors = featuresToUpdate.stream()
                .filter(feature -> !this.featureCollection.get(feature.getCode()).isPresent())
                .map(FeatureDTO::getCode)
                .collect(Collectors.joining(", "));
        if (!codeInErrors.isEmpty()) {
            throw new NotFoundException("Unknown feature(s) '" + codeInErrors + "'.", Entities.FEATURE);
        }
        featuresToUpdate.forEach(feature -> this.featureActivator.changeStateOf(feature.getCode(), feature.isEnabled()));
    }

    /**
     * Reset the given features list to their default state.
     * <p>
     * Note that if at least one feature in the list throw a <code>NotFoundException</code>, then it will not reset any
     * element in the list (all or none).
     *
     * @param featuresToReset the wanted features to reset to their default state.
     * @throws NotFoundException if at least one feature code doesn't match an existing feature.
     */
    public void reset(List<String> featuresToReset) throws NotFoundException {
        String codesInError = featuresToReset.stream()
                .filter(code -> !this.featureCollection.get(code).isPresent())
                .collect(Collectors.joining(", "));
        if (!codesInError.isEmpty()) {
            throw new NotFoundException("Unknown feature(s) '" + codesInError + "'.", Entities.FEATURE);
        }
        featuresToReset.forEach(code -> this.featureActivator.changeStateOf(code, this.featureActivator.isEnabledByDefault(code)));
    }

    /**
     * Return the current activation state of the wanted feature.
     * <p>
     * This method is design to be used in other part of the Ara sources to enabled or not feature beahviors. Note that
     * it will not check if the given code exists or not. If the code doesn't exists, then it will return false to
     * prevent a bad behavior and avoid risks.
     *
     * @param featureCode the code of the wanted feature to check
     * @return true if the feature is enable, false if it's disable or doesn't exists.
     */
    public boolean isEnabled(String featureCode) {
        return this.featureActivator.getState(featureCode);
    }
}
