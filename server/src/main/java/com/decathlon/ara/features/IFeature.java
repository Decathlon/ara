package com.decathlon.ara.features;

/**
 * The base interface for all the Feature Flipping classes.
 * <p>
 * Note that this interface is meant to be implemented by classes which holds temporary, constant, values. Those classes
 * are meant to be removed after the feature they watch is stable.
 *
 * @author Sylvain Nieuwlandt
 */
public interface IFeature {

    /**
     * @return the feature code of the current feature-flipping setting.
     */
    String getCode();

    /**
     * @return the pretty name of this feature flipping.
     */
    String getName();

    /**
     * @return the description of this feature flipping.
     */
    String getDescription();
}
