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

package com.decathlon.ara.lib.embed.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Holds the result of all structured embeddings parsed by {@link StructuredEmbeddingsExtractor#extract(String)}.
 */
public class StructuredEmbeddingsHolder {

    /**
     * All parsed structured embeddings.
     */
    private final List<ParsedStructuredEmbedding> embeddings = new ArrayList<>();

    /**
     * Try to extract an embedding of a particular kind.
     *
     * @param kind the kind of embedding to be searched for
     * @return the found embedding, if any
     */
    public Optional<String> extractStringData(String kind) {
        return embeddings.stream()
                .filter(e -> kind.equals(e.getKind()))
                .findFirst()
                .map(e -> (String) e.getData());
    }

	public List<ParsedStructuredEmbedding> getEmbeddings() {
		return embeddings;
	}

}
