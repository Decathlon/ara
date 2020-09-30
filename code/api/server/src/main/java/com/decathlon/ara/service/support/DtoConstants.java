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

package com.decathlon.ara.service.support;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoConstants {

    public static final String CODE_PATTERN = "[a-z0-9]+(?:-[a-z0-9]+)*";
    public static final String CODE_MESSAGE = "The code must be one or more groups of lower-case letters or digits, optionally separated by dashes (\"-\").";
    public static final String CODE_NAME_MESSAGE = "The name must be one or more groups of lower-case letters or digits, optionally separated by dashes (\"-\").";

}
