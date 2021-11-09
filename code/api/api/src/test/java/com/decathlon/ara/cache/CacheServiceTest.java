package com.decathlon.ara.cache;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.User;
import com.decathlon.ara.repository.GroupMemberRepository;

import net.sf.ehcache.Ehcache;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {
    
    @Mock
    private CacheManager cacheManager;
    @Mock
    private GroupMemberRepository groupMemberRepository;
    
    @InjectMocks
    private CacheService cacheService;
    
    private static final String USER_PROJECTS_CACHE_NAME = "security.user.projects";
    private static final String USER_PROJECT_ROLES_CACHE_NAME = "security.user.project.roles";
    
    @Test
    void evictCachesForProjectShouldClearUserProjectsCacheWhenCacheExists() {
        Project project = new Project("test", "test");
        Cache cache = Mockito.mock(Cache.class);
        Mockito.when(cacheManager.getCache(USER_PROJECT_ROLES_CACHE_NAME)).thenReturn(null);
        Mockito.when(cacheManager.getCache(USER_PROJECTS_CACHE_NAME)).thenReturn(cache);
        
        cacheService.evictCaches(project);
        
        Mockito.verify(cache).clear();
    }
    
    @Test
    void evictCachesForProjectShouldClearUserProjectRolesCacheWhenCacheExistsAndItsNotAnEhCache() {
        Project project = new Project("test", "test");
        Cache cache = Mockito.mock(Cache.class);
        Mockito.when(cacheManager.getCache(USER_PROJECT_ROLES_CACHE_NAME)).thenReturn(cache);
        Mockito.when(cacheManager.getCache(USER_PROJECTS_CACHE_NAME)).thenReturn(null);
        
        cacheService.evictCaches(project);
        
        Mockito.verify(cache).clear();
    }
    
    @Test
    void evictCachesForProjectShouldEvictMatchingKeysInUserProjectRolesCacheWhenCacheExistsAndItsAnEhCache() {
        Project project = new Project("test", "test");
        EhCacheCache cache = Mockito.mock(EhCacheCache.class);
        Mockito.when(cacheManager.getCache(USER_PROJECT_ROLES_CACHE_NAME)).thenReturn(cache);
        Mockito.when(cacheManager.getCache(USER_PROJECTS_CACHE_NAME)).thenReturn(null);
        Ehcache nativeCache = Mockito.mock(Ehcache.class);
        Mockito.when(cache.getNativeCache()).thenReturn(nativeCache);
        Mockito.when(nativeCache.getKeys()).thenReturn(List.of("testuser1", "testuser2", "totouser1", "totouser2", "testuser3"));
        
        cacheService.evictCaches(project);
        
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(cache, Mockito.times(3)).evict(captor.capture());
        List<String> evictedKeys = captor.getAllValues();
        Assertions.assertEquals(3, evictedKeys.size());
        Assertions.assertEquals("testuser1", evictedKeys.get(0));
        Assertions.assertEquals("testuser2", evictedKeys.get(1));
        Assertions.assertEquals("testuser3", evictedKeys.get(2));
    }
    
    @Test
    void evictCachesForProjectShouldDoNothingUntilCommitOfTransactionIfTransactionStartedBeforeCall() {
        try {
            TransactionSynchronizationManager.initSynchronization();
            Project project = new Project("test", "test");
            Cache cache = Mockito.mock(Cache.class);
            Cache cache2 = Mockito.mock(Cache.class);
            Mockito.when(cacheManager.getCache(USER_PROJECT_ROLES_CACHE_NAME)).thenReturn(cache);
            Mockito.when(cacheManager.getCache(USER_PROJECTS_CACHE_NAME)).thenReturn(cache2);
            
            cacheService.evictCaches(project);
            
            Mockito.verifyNoInteractions(cache);
            Mockito.verifyNoInteractions(cache2);
            
            TransactionSynchronizationUtils.triggerAfterCommit();
            
            Mockito.verify(cache).clear();
            Mockito.verify(cache2).clear();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
    
    @Test
    void evictCachesForProjectAndUserNameShouldEvictMatchingKeyInUserProjectsCacheWhenCacheExists() {
        Project project = new Project("test", "test");
        User user = new User("name", "issuer");
        Cache cache = Mockito.mock(Cache.class);
        Mockito.when(cacheManager.getCache(USER_PROJECT_ROLES_CACHE_NAME)).thenReturn(null);
        Mockito.when(cacheManager.getCache(USER_PROJECTS_CACHE_NAME)).thenReturn(cache);
        
        cacheService.evictCaches(project, user.getMemberName());
        
        Mockito.verify(cache).evict(user.getMemberName());
    }
    
    @Test
    void evictCachesForProjectAndUserNameShouldEvictMatchingKeyInUserProjectRolesCacheWhenCacheExists() {
        Project project = new Project("test", "test");
        User user = new User("name", "issuer");
        Cache cache = Mockito.mock(Cache.class);
        Mockito.when(cacheManager.getCache(USER_PROJECT_ROLES_CACHE_NAME)).thenReturn(cache);
        Mockito.when(cacheManager.getCache(USER_PROJECTS_CACHE_NAME)).thenReturn(null);
        
        cacheService.evictCaches(project, user.getMemberName());
        
        Mockito.verify(cache).evict(project.getCode().concat(user.getMemberName()));
    }
    
    @Test
    void evictCachesForProjectAndUserNameShouldDoNothingUntilCommitOfTransactionIfTransactionStartedBeforeCall() {
        try {
            TransactionSynchronizationManager.initSynchronization();
            Project project = new Project("test", "test");
            User user = new User("name", "issuer");
            Cache cache = Mockito.mock(Cache.class);
            Cache cache2 = Mockito.mock(Cache.class);
            Mockito.when(cacheManager.getCache(USER_PROJECT_ROLES_CACHE_NAME)).thenReturn(cache);
            Mockito.when(cacheManager.getCache(USER_PROJECTS_CACHE_NAME)).thenReturn(cache2);
            
            cacheService.evictCaches(project, user.getMemberName());
            
            Mockito.verifyNoInteractions(cache);
            Mockito.verifyNoInteractions(cache2);
            
            TransactionSynchronizationUtils.triggerAfterCommit();
            
            Mockito.verify(cache).evict(project.getCode().concat(user.getMemberName()));
            Mockito.verify(cache2).evict(user.getMemberName());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
    
    @Test
    void evictCachesForUserNameShouldEvictMatchingKeyInUserProjectCacheWhenCacheExists() {
        User user = new User("name", "issuer");
        Cache cache = Mockito.mock(Cache.class);
        Mockito.when(cacheManager.getCache(USER_PROJECT_ROLES_CACHE_NAME)).thenReturn(null);
        Mockito.when(cacheManager.getCache(USER_PROJECTS_CACHE_NAME)).thenReturn(cache);
        
        cacheService.evictCaches(user.getMemberName());
        
        Mockito.verify(cache).evict(user.getMemberName());
    }
    
    @Test
    void evictCachesForUserNameShouldClearUserProjectRolesCacheWhenCacheExistsAndItsNotAnEhCache() {
        User user = new User("name", "issuer");
        Cache cache = Mockito.mock(Cache.class);
        Mockito.when(cacheManager.getCache(USER_PROJECT_ROLES_CACHE_NAME)).thenReturn(cache);
        Mockito.when(cacheManager.getCache(USER_PROJECTS_CACHE_NAME)).thenReturn(null);
        
        cacheService.evictCaches(user.getMemberName());
        
        Mockito.verify(cache).clear();
    }
    
    @Test
    void evictCachesForUserNameShouldEvictMatchingKeyInUserProjectRolesCacheWhenCacheExistsAndItsAnEhCache() {
        User user = new User("user1", "issuer");
        EhCacheCache cache = Mockito.mock(EhCacheCache.class);
        Mockito.when(cacheManager.getCache(USER_PROJECT_ROLES_CACHE_NAME)).thenReturn(cache);
        Mockito.when(cacheManager.getCache(USER_PROJECTS_CACHE_NAME)).thenReturn(null);
        Ehcache nativeCache = Mockito.mock(Ehcache.class);
        Mockito.when(cache.getNativeCache()).thenReturn(nativeCache);
        Mockito.when(nativeCache.getKeys()).thenReturn(List.of("test"+user.getId(), "testuser2-issuer", "toto"+user.getId(), "totouser2-issuer", "testuser3-issuer"));
        
        cacheService.evictCaches(user.getMemberName());
        
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(cache, Mockito.times(2)).evict(captor.capture());
        List<String> evictedKeys = captor.getAllValues();
        Assertions.assertEquals(2, evictedKeys.size());
        Assertions.assertEquals("test"+user.getId(), evictedKeys.get(0));
        Assertions.assertEquals("toto"+user.getId(), evictedKeys.get(1));
    }
    
    @Test
    void evictCachesForUserNameShouldDoNothingUntilCommitOfTransactionIfTransactionStartedBeforeCall() {
        try {
            TransactionSynchronizationManager.initSynchronization();
            User user = new User("name", "issuer");
            Cache cache = Mockito.mock(Cache.class);
            Cache cache2 = Mockito.mock(Cache.class);
            Mockito.when(cacheManager.getCache(USER_PROJECT_ROLES_CACHE_NAME)).thenReturn(cache);
            Mockito.when(cacheManager.getCache(USER_PROJECTS_CACHE_NAME)).thenReturn(cache2);
            
            cacheService.evictCaches(user.getMemberName());
            
            Mockito.verifyNoInteractions(cache);
            Mockito.verifyNoInteractions(cache2);
            
            TransactionSynchronizationUtils.triggerAfterCommit();
            
            Mockito.verify(cache).clear();
            Mockito.verify(cache2).evict(user.getMemberName());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
    
}
