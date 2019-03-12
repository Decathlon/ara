package com.decathlon.ara.features.available;

import com.decathlon.ara.features.IFeature;

/**
 * Contains the values linked to the FeatureFlipping of the JIRA Defect Adapter.
 * <p>
 * To be removed after stabilization of the JIRA Defect Adapter.
 *
 * @author Sylvain Nieuwlandt
 */
public class JiraAdapterFeature implements IFeature {

    /**
     * The Feature Flipping's code.
     */
    public static final String CODE = "jira-adapter";

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public String getName() {
        return "JIRA Defect Adapter";
    }

    @Override
    public String getDescription() {
        return "The Adapter between ARA's defect monitoring and a JIRA Issue Tracker instance.";
    }
}
