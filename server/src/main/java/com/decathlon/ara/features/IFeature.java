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
