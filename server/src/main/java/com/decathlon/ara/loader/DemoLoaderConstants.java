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

package com.decathlon.ara.loader;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DemoLoaderConstants {

    // Quite long code (not just "demo") and name, to not clash with a user-defined project, if they need a demo project
    public static final String PROJECT_CODE_DEMO = "the-demo-project";

    public static final String BRANCH_MASTER = "master";
    static final String BRANCH_DEVELOP = "develop";
    static final String CYCLE_DAY = "day";
    static final String CYCLE_NIGHT = "night";
    static final String SOURCE_CODE_API = "api";
    static final String SOURCE_CODE_WEB = "web";
    static final String TYPE_CODE_FIREFOX_DESKTOP = "firefox-desktop";

}
