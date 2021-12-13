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

package com.decathlon.ara.service.dto.coverage;

import java.util.List;

public class CoverageDTO {

    private List<AxisDTO> axes;
    private int[] values;

    public List<AxisDTO> getAxes() {
        return axes;
    }

    public void setAxes(List<AxisDTO> axes) {
        this.axes = axes;
    }

    public int[] getValues() {
        return values;
    }

    public void setValues(int[] values) {
        this.values = values;
    }

}
