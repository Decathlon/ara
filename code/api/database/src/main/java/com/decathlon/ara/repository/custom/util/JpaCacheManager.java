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

package com.decathlon.ara.repository.custom.util;

import java.util.Collection;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Helper class to manage JPA cache, and evict some regions where needed.
 */
@Component
public class JpaCacheManager {

    private static final Logger LOG = LoggerFactory.getLogger(JpaCacheManager.class);

    private EntityManager entityManager;

    public JpaCacheManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Evict the cache data for the given identified collection instance.
     *
     * @param collectionRegion the "collection role" (in form [owner-entity-name].[collection-property-name])
     * @param ownerIdentifier  the identifier of the owning entity
     */
    public void evictCollection(String collectionRegion, Long ownerIdentifier) {
        LOG.debug("Evicting collection cache {} for owner entity {}", collectionRegion, ownerIdentifier);
        entityManager
                .unwrap(Session.class)
                .getSessionFactory()
                .getCache()
                .evictCollectionData(collectionRegion, ownerIdentifier);
    }

    /**
     * Evict the cache data for the given identified collection instances.
     *
     * @param collectionRegion the "collection role" (in form [owner-entity-name].[collection-property-name])
     * @param ownerIdentifiers the identifiers of the owning entities
     */
    public void evictCollections(String collectionRegion, Collection<Long> ownerIdentifiers) {
        for (Long ownerIdentifier : ownerIdentifiers) {
            evictCollection(collectionRegion, ownerIdentifier);
        }
    }

}
