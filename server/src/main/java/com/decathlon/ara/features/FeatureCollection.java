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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.decathlon.ara.features.available.ExportImportCartographyFeature;
import com.decathlon.ara.features.available.JiraAdapterFeature;
import org.springframework.stereotype.Component;

/**
 * A FeatureCollection which holds all the Feature flipping settings available with this instance.
 *
 * @author Sylvain Nieuwlandt
 */
@Component
public class FeatureCollection {

    private final Map<String, IFeature> availableFeatures;

    /**
     * Constructor of the class.
     * <p>
     * Create a new collection of available features (used to be injected by Spring).
     */
    FeatureCollection() {
        this(new HashMap<>());
        // To register a new Feature Flipping class use the following syntax :
        //
        // this.register(new YourFeatureFlippingClass());
        //
        this.register(new JiraAdapterFeature());
        this.register(new ExportImportCartographyFeature());
    }

    /**
     * Copy constructor of the class
     * <p>
     * Create a new collection with its content matching the given map.
     *
     * @param allFeatures the mpa which will become the content of the new Collection.
     */
    FeatureCollection(Map<String, IFeature> allFeatures) {
        this.availableFeatures = allFeatures;
    }

    /**
     * List all the available features which an integrator can enable or disable.
     *
     * @return the list of all the available features.
     */
    public List<IFeature> list() {
        return new ArrayList<>(this.availableFeatures.values());
    }

    /**
     * Return the feature linked to the given code.
     *
     * @param code the code of the wanted feature.
     * @return an Optional containing the feature (if the code match), of an empty Optional if the code doesn't match an
     * existing feature.
     */
    public Optional<IFeature> get(String code) {
        if (this.availableFeatures.containsKey(code)) {
            return Optional.of(this.availableFeatures.get(code));
        }
        return Optional.empty();
    }

    private void register(IFeature feature) {
        this.availableFeatures.put(feature.getCode(), feature);
    }

}
