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

package com.decathlon.ara.configuration;

import java.io.IOException;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

@Configuration
public class JsonFactoryConfiguration {

    /**
     * From the JsonFactory documentation:
     * factory instances are thread-safe and reusable [...]
     * Factory reuse is important if efficiency matters;
     * most recycling of expensive construct is done on per-factory basis.
     *
     * @return a globally shared JsonFactory
     */
    @Bean
    public JsonFactory jsonFactory() {
        return new JsonFactory();
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.deserializers(new StringTrimDeserializer());
    }

    private static class StringTrimDeserializer extends StdDeserializer<String> {

        private static final long serialVersionUID = 1L;

        protected StringTrimDeserializer() {
            super(String.class);
        }

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.getValueAsString().trim();
        }

    }

}
