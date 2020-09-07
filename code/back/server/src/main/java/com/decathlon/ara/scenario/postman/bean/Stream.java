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

package com.decathlon.ara.scenario.postman.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.File;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Stream {

    /**
     * The body of an HTTP response (or request) as a stream of bytes.<br>
     * Can potentially be big, so while streaming the JSON file, the data is saved to a temporary file on disk and the field is set to false to avoid OutOfMemoryErrors. The file can then be removed (if request was a success) or uploaded (if the request was an error and we need the response stream data to debug it).<br>
     * There is a "type":"Buffer" in the Stream object, but it might be for future expansion, as there is currently no other values.
     */
    private byte[] data;

    /**
     * Temporary file used to temporarily dump the stream from {@code data}.
     *
     * @see #data data for more detailed documentation of the process
     */
    private File tempFile;

}
