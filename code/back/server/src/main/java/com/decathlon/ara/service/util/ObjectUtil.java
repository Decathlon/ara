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

package com.decathlon.ara.service.util;

import com.decathlon.ara.common.NotGonnaHappenException;
import java.lang.reflect.Field;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class ObjectUtil {

    /**
     * @param object will replace all String fields (not recursively) with trimmed Strings (or keep null if they are
     *               null), setting null when field would be empty
     */
    public void trimStringValues(Object object) {
        Class<?> clazz = object.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                trimField(object, field);
            }
            clazz = clazz.getSuperclass();
        }
    }

    private static void trimField(Object object, Field field) {
        try {
            field.setAccessible(true);
            Object value = field.get(object);
            if (value instanceof String) {
                final String trimmed = StringUtils.trimToNull((String) value);
                field.set(object, trimmed);
            }
        } catch (Exception e) {
            throw new NotGonnaHappenException("get/set String value is always not null, of type String, and we made sure it is accessible", e);
        }
    }

}
