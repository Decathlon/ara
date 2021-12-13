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

package com.decathlon.ara.features;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * FeatureActivator manages all the in memory features flipping at the current instant.
 *
 * @author Sylvain Nieuwlandt
 */
@Component
@Scope("singleton")
public class FeatureActivator {

    private static final Logger LOG = LoggerFactory.getLogger(FeatureActivator.class);

    private Environment environment;

    private Map<String, Boolean> currentStates = new HashMap<>();
    private Map<String, Boolean> defaultStates = new HashMap<>();

    public FeatureActivator(Environment environment) {
        this.environment = environment;
    }

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
        LOG.info("Loading features states...");
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
        LOG.info("Features states loaded ( {} enabled on {} ).", enabledCounter, allFeatures.size());
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
            LOG.info("Switching state of {} to {}.", code, newState ? "ENABLED" : "DISABLED");
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
