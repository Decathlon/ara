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

package com.decathlon.ara.postman.support;

import com.fasterxml.jackson.core.JsonParser;
import java.io.IOException;

@FunctionalInterface
public interface JsonStreamer<R> {

    /**
     * Parse the given JSON stream, process it, and optionally return an object.<br>
     * The returned object can represent a downsized parsed version of the stream, or the result of a map/reduce processing, or null...
     *
     * @param jsonParser the parser to use while streaming JSON for processing
     * @return the optional result of the process (can be {@link Void} if processing returns nothing)
     * @throws IOException on streaming problem (you are also strongly encouraged to throw HttpMessageNotReadableException on parsing error)
     */
    R stream(JsonParser jsonParser) throws IOException;

}
