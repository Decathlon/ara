package com.decathlon.ara.features;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * FeatureActivator manages all the in memory features flipping at the current instant.
 *
 * @author Sylvain Nieuwlandt
 */
@Slf4j
@Component
@Scope("singleton")
@AllArgsConstructor
@NoArgsConstructor
public class FeatureActivator {

    @Autowired
    private Environment environment;

    @NonNull
    private Map<String, Boolean> currentStates = new HashMap<>();
    @NonNull
    private Map<String, Boolean> defaultStates = new HashMap<>();

    /**
     * Load the current activator with a default FeatureCollection.
     * <p>
     * Called by Spring after initialization of the Bean.
     */
    @PostConstruct
    public void load() {
        this.load(new FeatureCollection());
    }

    /**
     * Load the activator current state with the default states of the features contains in the given collection.
     * <p>
     * Clear the in-memory current and default state before checking the new values.
     *
     * @param collection the collection which will contains the available features.
     */
    public void load(FeatureCollection collection) {
        log.info("Loading features states...");
        this.currentStates.clear();
        this.defaultStates.clear();
        int enabledCounter = 0;
        List<IFeature> allFeatures = collection.list();
        String featurePrefix = "ara.features.";
        for (IFeature feature : allFeatures) {
            String propertyActivation = featurePrefix + feature.getCode();
            boolean status = false; // Default : disabled.
            if (null != System.getProperty(propertyActivation)) { // -Dara.feature.<code>
                status = Boolean.valueOf(System.getProperty(propertyActivation));
            } else if (this.environment.containsProperty(propertyActivation)) { // application.properties file
                status = Boolean.valueOf(this.environment.getProperty(propertyActivation));
            }

            if (status) {
                enabledCounter++;
            }
            this.defaultStates.put(feature.getCode(), status);
            this.currentStates.put(feature.getCode(), status);
        }
        log.info("Features states loaded ( {} enabled on {} ).", enabledCounter, allFeatures.size());
    }

    /**
     * Return the current state of the given feature's code.
     *
     * @param code the code of wanted feature
     * @return true if the feature is enabled, false if it's disabled.
     * @throws IllegalArgumentException if the code doesn't match a feature
     */
    public boolean getState(String code) throws IllegalArgumentException {
        if (!this.currentStates.containsKey(code)) {
            throw new IllegalArgumentException("Unknown feature code : " + code);
        }
        return this.currentStates.get(code);
    }

    /**
     * Change the current state of the feature pointed by the given code to the new state given.
     * <p>
     * Do nothing if the code doesn't match an existing feature.
     *
     * @param code     the code of the feature to update
     * @param newState the new state to apply to the feature, true is enable, false is disable.
     */
    public void changeStateOf(String code, boolean newState) {
        if (this.currentStates.containsKey(code)) {
            log.info("Switching state of {} to {}.", code, newState ? "ENABLED" : "DISABLED");
            this.currentStates.put(code, newState);
        }
    }

    /**
     * Return the default state of the feature linked to the given code.
     *
     * @param code the code of the wanted feature
     * @return true if the feature is enabled by default, false if it's disabled by default.
     * @throws IllegalArgumentException if the code doesn't match an existing feature.
     */
    public boolean isEnabledByDefault(String code) throws IllegalArgumentException {
        if (!this.defaultStates.containsKey(code)) {
            throw new IllegalArgumentException("Unknown code : " + code);
        }
        return this.defaultStates.get(code);
    }
}
