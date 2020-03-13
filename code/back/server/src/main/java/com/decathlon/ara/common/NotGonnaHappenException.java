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

package com.decathlon.ara.common;

/**
 * Used in places, where a checked java exception is reported by the compiler but the programmer knows, that such an Exception will never be
 * thrown there.
 * <p>
 * This exception is only used to re-throw an exception and is an unchecked exception.
 *
 * @see <a href= "https://github.com/scravy/Obsolete/blob/master/AbusingJava/src/main/java/net/abusingjava/NotGonnaHappenException.java">GitHub
 * source</a>
 */
public class NotGonnaHappenException extends RuntimeException {

    private static final long serialVersionUID = 2807142911588081627L;

    /**
     * Constructor to throw an Exception in a branch of code that is not supposed to run.
     *
     * @param message The message/hint
     */
    public NotGonnaHappenException(final String message) {
        super(message);
    }

    /**
     * Constructor to re-throw an Exception with a more detailed message (e.g. a hint of what could have happened here).
     *
     * @param message The message/hint
     * @param cause   The exception
     */
    public NotGonnaHappenException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
