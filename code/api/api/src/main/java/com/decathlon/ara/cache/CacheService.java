package com.decathlon.ara.cache;

import java.util.function.Predicate;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.transaction.RunnableAfterCommitTransactionSynchronization;

@Service
public class CacheService {

    private static final String USER_PROJECTS_CACHE_NAME = "security.user.projects";
    private static final String USER_PROJECT_ROLES_CACHE_NAME = "security.user.project.roles";

    private CacheManager cacheManager;

    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void evictCaches(Project project) {
        evictCacheAfterCommit(() -> {
            evictCache(USER_PROJECT_ROLES_CACHE_NAME, key -> key.toString().startsWith(project.getCode()));

            Cache cache = cacheManager.getCache(USER_PROJECTS_CACHE_NAME);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    public void evictCaches(Project project, String userName) {
        evictsUserProjectRolesCache(project, userName);
        evictsUserProjectsCache(userName);
    }

    public void evictsUserProjectRolesCache(Project project, String userName) {
        evictCacheAfterCommit(() -> {
            Cache cache = cacheManager.getCache(USER_PROJECT_ROLES_CACHE_NAME);
            if (cache != null) {
                cache.evict(project.getCode().concat(userName));
            }
        });
    }

    public void evictsUserProjectRolesCache(String userName) {
        evictCacheAfterCommit(() -> evictCache(USER_PROJECT_ROLES_CACHE_NAME, key -> key.toString().endsWith(userName)));
    }

    public void evictsUserProjectsCache(String userName) {
        evictCacheAfterCommit(() -> {
            Cache cache = cacheManager.getCache(USER_PROJECTS_CACHE_NAME);
            if (cache != null) {
                cache.evict(userName);
            }
        });
    }

    public void evictCaches(String userName) {
        evictsUserProjectRolesCache(userName);
        evictsUserProjectsCache(userName);
    }

    @SuppressWarnings("unchecked")
    private void evictCache(String cacheName, Predicate<Object> keyPredicate) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            if (cache instanceof EhCacheCache ehCacheCache) {
                ehCacheCache.getNativeCache().getKeys().stream().filter(keyPredicate).forEach(cache::evict);
            } else {
                cache.clear();
            }
        }
    }

    private void evictCacheAfterCommit(RunnableAfterCommitTransactionSynchronization evictRunnable) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(evictRunnable);
        } else {
            evictRunnable.run();
        }
    }

}
