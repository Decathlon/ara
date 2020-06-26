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

package com.decathlon.ara.coverage;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.service.dto.coverage.AxisPointDTO;
import java.util.stream.Stream;

interface AxisGenerator {

    /**
     * @return the ID of the axis: used as a filter in the URL of the functionality cartography screen
     */
    String getCode();

    /**
     * @return the displayed name (plural) of the entity represented by this axis
     */
    String getName();

    /**
     * @param projectId the ID of the project in which to work
     * @return all points on this axis, in order, excluding ALL (it will be prepended automatically)
     */
    Stream<AxisPointDTO> getPoints(long projectId);

    /**
     * @param functionality a NOT null functionality
     * @return an array of {@link AxisPointDTO#getId()} value points describing the given {@code functionality} for the current axis:
     * may be null or empty if no axis point matches the functionality
     */
    String[] getValuePoints(Functionality functionality);

}
