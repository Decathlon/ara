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
