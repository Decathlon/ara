package com.decathlon.ara.features;

import com.decathlon.ara.features.available.ExecutionShortenerFeature;
import com.decathlon.ara.features.available.JiraAdapterFeature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        this.register(new ExecutionShortenerFeature());
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
