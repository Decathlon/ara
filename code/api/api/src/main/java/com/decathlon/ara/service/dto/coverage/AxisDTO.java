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

public class AxisDTO {

    private String code;
    private String name;
    private List<AxisPointDTO> points;

    public AxisDTO() {
    }

    public AxisDTO(String code, String name, List<AxisPointDTO> points) {
        super();
        this.code = code;
        this.name = name;
        this.points = points;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public List<AxisPointDTO> getPoints() {
        return points;
    }
}
