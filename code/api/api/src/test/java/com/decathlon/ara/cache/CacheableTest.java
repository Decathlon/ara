package com.decathlon.ara.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.AopTestUtils;

import com.decathlon.ara.configuration.security.TestAuthentication;
import com.decathlon.ara.domain.Group;
import com.decathlon.ara.domain.GroupMember;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.ProjectGroupMember;
import com.decathlon.ara.domain.ProjectUserMember;
import com.decathlon.ara.domain.User;
import com.decathlon.ara.domain.enumeration.MemberRole;
import com.decathlon.ara.repository.GroupMemberRepository;
import com.decathlon.ara.repository.GroupRepository;
import com.decathlon.ara.repository.ProjectGroupMemberRepository;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.ProjectUserMemberRepository;
import com.decathlon.ara.repository.RootCauseRepository;
import com.decathlon.ara.repository.UserRepository;
import com.decathlon.ara.service.CommunicationService;
import com.decathlon.ara.service.GroupMemberService;
import com.decathlon.ara.service.ProjectGroupMemberService;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.ProjectUserMemberService;
import com.decathlon.ara.service.UserPreferenceService;
import com.decathlon.ara.service.dto.member.MemberDTO;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.GenericMapper;
import com.decathlon.ara.service.security.SecurityService;
import com.decathlon.ara.util.TestUtil;

@SpringBootTest
@ContextConfiguration(classes = { CacheableTest.CacheITTestConfig.class, ProjectGroupMemberService.class, ProjectUserMemberService.class, GroupMemberService.class })
class CacheableTest {
    
    @Autowired
    private EhCacheCacheManager cacheManager;

    @Autowired
    private SecurityService securityService;

    private SecurityService mockSecurityService;

    @Autowired
    private ProjectService projectService;

    private ProjectService mockProjectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private GenericMapper genericMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPreferenceService userPreferenceService;

    @Autowired
    private ProjectUserMemberService projectUserMemberService;

    @Autowired
    private ProjectGroupMemberService projectGroupMemberService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberService groupMemberService;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private ProjectUserMemberRepository projectUserMemberRepository;

    @Autowired
    private ProjectGroupMemberRepository projectGroupMemberRepository;

    private static final User USER = new User("name", "issuer");
    private static final Project PROJECT = new Project("code", "name");
    static {
        TestUtil.setField(PROJECT, "id", 1L);
    }
    private static final Group GROUP = new Group("name");

    @EnableCaching
    @TestConfiguration
    static class CacheITTestConfig {

        @Bean
        public EhCacheCacheManager cacheManager() {
            return new EhCacheCacheManager(EhCacheManagerUtils.buildCacheManager("test"));
        }

        @Bean
        public ProjectService projectService() {
            return Mockito.spy(new ProjectService(projectRepository(), rootCauseRepository(), userRepository(), projectUserMemberRepository(), projectGroupMemberRepository(), genericMapper(), communicationService(), userPreferenceService(), cacheService()));
        }

        @Bean
        public CacheService cacheService() {
            return new CacheService(cacheManager());
        }

        @Bean
        public SecurityService securityService() {
            return Mockito.mock(SecurityService.class);
        }

        @Bean
        public ProjectRepository projectRepository() {
            return Mockito.mock(ProjectRepository.class);
        }

        @Bean
        public RootCauseRepository rootCauseRepository() {
            return Mockito.mock(RootCauseRepository.class);
        }

        @Bean
        public UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean
        public ProjectUserMemberRepository projectUserMemberRepository() {
            return Mockito.mock(ProjectUserMemberRepository.class);
        }

        @Bean
        public ProjectGroupMemberRepository projectGroupMemberRepository() {
            return Mockito.mock(ProjectGroupMemberRepository.class);
        }

        @Bean
        public GenericMapper genericMapper() {
            return Mockito.mock(GenericMapper.class);
        }

        @Bean
        public CommunicationService communicationService() {
            return Mockito.mock(CommunicationService.class);
        }

        @Bean
        public UserPreferenceService userPreferenceService() {
            return Mockito.mock(UserPreferenceService.class);
        }

        @Bean
        public GroupMemberRepository groupMemberRepository() {
            return Mockito.mock(GroupMemberRepository.class);
        }

        @Bean
        public GroupRepository groupRepository() {
            return Mockito.mock(GroupRepository.class);
        }

    }

    @BeforeEach
    void beforeEach() {
        mockSecurityService = AopTestUtils.getTargetObject(securityService);
        mockProjectService = AopTestUtils.getTargetObject(projectService);

        Mockito.reset(mockSecurityService, mockProjectService, projectRepository, genericMapper, userPreferenceService, projectUserMemberRepository, projectGroupMemberRepository, groupMemberRepository);

        final AtomicInteger securityServiceCallCounter = new AtomicInteger(0);

        Mockito.when(mockSecurityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName())).thenAnswer(invocationOnMock -> {
            securityServiceCallCounter.incrementAndGet();
            return new HashSet<>() {

                private static final long serialVersionUID = 1L;

                @Override
                public int size() {
                    return securityServiceCallCounter.get();
                }
            };
        });

        final AtomicInteger projectServiceCallCounter = new AtomicInteger(0);

        Mockito.when(mockProjectService.findAll(USER.getMemberName())).thenAnswer(invocationOnMock -> {
            projectServiceCallCounter.incrementAndGet();
            return new ArrayList<>() {

                private static final long serialVersionUID = 1L;

                @Override
                public int size() {
                    return projectServiceCallCounter.get();
                }
            };
        });
    }

    @AfterEach
    void afterEach() {
        for (String cacheName : cacheManager.getCacheNames()) {
            cacheManager.getCache(cacheName).clear();
        }
    }


    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndClearedWhenProjectIsCreated() throws BadRequestException {
        ProjectDTO projectDTO = new ProjectDTO(null, PROJECT.getCode(), PROJECT.getName(), false);
        Mockito.when(genericMapper.map(projectDTO, Project.class)).thenReturn(PROJECT);
        Mockito.when(projectRepository.save(PROJECT)).thenReturn(PROJECT);
        ProjectDTO result = new ProjectDTO(1l, PROJECT.getCode(), PROJECT.getName(), true);
        Mockito.when(genericMapper.map(PROJECT, ProjectDTO.class)).thenReturn(result);
        Mockito.when(userRepository.findByMemberName(USER.getMemberName())).thenReturn(USER);
        SecurityContextHolder.getContext().setAuthentication(new TestAuthentication(USER.getMemberName(), Collections.emptyList()));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectService.create(projectDTO);

        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndClearedWhenProjectIsDeleted() throws BadRequestException {
        Mockito.when(projectRepository.findOneByCode(PROJECT.getCode())).thenReturn(PROJECT);
        SecurityContextHolder.getContext().setAuthentication(new TestAuthentication(USER.getMemberName(), Collections.emptyList()));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectService.delete(PROJECT.getCode());

        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndClearedWhenUserAddedAsMemberOfProject() throws BadRequestException {
        Mockito.when(projectRepository.findByContainerIdentifier(PROJECT.getCode())).thenReturn(PROJECT);
        Mockito.when(userRepository.findByMemberName(USER.getMemberName())).thenReturn(USER);

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectUserMemberService.addMember(PROJECT.getCode(), new MemberDTO(USER.getMemberName(), MemberRole.ADMIN));

        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndClearedWhenUserRoleIsChangedOnProject() throws BadRequestException {
        Mockito.when(projectUserMemberRepository.findByContainerIdentifierAndMemberName(PROJECT.getCode(), USER.getMemberName())).thenReturn(new ProjectUserMember(PROJECT, USER));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectUserMemberService.updateMemberRole(PROJECT.getCode(), USER.getMemberName(), MemberRole.ADMIN);

        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndClearedWhenUserDeletedAsMemberOfProject() throws BadRequestException {
        Mockito.when(projectUserMemberRepository.findByContainerIdentifierAndMemberName(PROJECT.getCode(), USER.getMemberName())).thenReturn(new ProjectUserMember(PROJECT, USER));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectUserMemberService.deleteMember(PROJECT.getCode(), USER.getMemberName());

        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndClearedWhenGroupContainingUserIsAddedAsMemberOfProject() throws BadRequestException {
        Mockito.when(projectRepository.findByContainerIdentifier(PROJECT.getCode())).thenReturn(PROJECT);
        Mockito.when(groupRepository.findByMemberName(GROUP.getName())).thenReturn(GROUP);
        Mockito.when(groupMemberRepository.findAllByIdGroupName(GROUP.getName())).thenReturn(List.of(new GroupMember(GROUP, USER)));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectGroupMemberService.addMember(PROJECT.getCode(), new MemberDTO(GROUP.getName(), MemberRole.ADMIN));

        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndClearedWhenGroupContainingUserRoleIsUpdatedOnProject() throws BadRequestException {
        Mockito.when(projectGroupMemberRepository.findByContainerIdentifierAndMemberName(PROJECT.getCode(), GROUP.getName())).thenReturn(new ProjectGroupMember(PROJECT, GROUP));
        Mockito.when(groupMemberRepository.findAllByIdGroupName(GROUP.getName())).thenReturn(List.of(new GroupMember(GROUP, USER)));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectGroupMemberService.updateMemberRole(PROJECT.getCode(), GROUP.getName(), MemberRole.ADMIN);

        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndClearedWhenGroupContainingUserIsDeletedAsMemberOfProject() throws BadRequestException {
        Mockito.when(projectGroupMemberRepository.findByContainerIdentifierAndMemberName(PROJECT.getCode(), GROUP.getName())).thenReturn(new ProjectGroupMember(PROJECT, GROUP));
        Mockito.when(groupMemberRepository.findAllByIdGroupName(GROUP.getName())).thenReturn(List.of(new GroupMember(GROUP, USER)));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectGroupMemberService.deleteMember(PROJECT.getCode(), GROUP.getName());

        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndClearedWhenUserIsAddedAsMemberOfGroup() throws BadRequestException {
        Mockito.when(groupRepository.findByContainerIdentifier(GROUP.getName())).thenReturn(GROUP);
        Mockito.when(userRepository.findByMemberName(USER.getMemberName())).thenReturn(USER);
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        groupMemberService.addMember(GROUP.getName(), new MemberDTO(USER.getMemberName(), MemberRole.ADMIN));

        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndClearedWhenUserRoleIsUpdatedOnGroup() throws BadRequestException {
        Mockito.when(groupMemberRepository.findByContainerIdentifierAndMemberName(GROUP.getName(), USER.getMemberName())).thenReturn(new GroupMember(GROUP, USER));
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        groupMemberService.updateMemberRole(GROUP.getName(), USER.getMemberName(), MemberRole.ADMIN);

        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndClearedWhenUserIsDeletedAsMemberOfGroup() throws BadRequestException {
        Mockito.when(groupMemberRepository.findByContainerIdentifierAndMemberName(GROUP.getName(), USER.getMemberName())).thenReturn(new GroupMember(GROUP, USER));
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        groupMemberService.deleteMember(GROUP.getName(), USER.getMemberName());

        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(2, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenProjectIsCreatedByAnotherUser() throws BadRequestException {
        User otherUser = new User("otherName", "issuer");
        ProjectDTO projectDTO = new ProjectDTO(null, PROJECT.getCode(), PROJECT.getName(), false);
        Mockito.when(genericMapper.map(projectDTO, Project.class)).thenReturn(PROJECT);
        Mockito.when(projectRepository.save(PROJECT)).thenReturn(PROJECT);
        ProjectDTO result = new ProjectDTO(1l, PROJECT.getCode(), PROJECT.getName(), true);
        Mockito.when(genericMapper.map(PROJECT, ProjectDTO.class)).thenReturn(result);
        Mockito.when(userRepository.findByMemberName(otherUser.getMemberName())).thenReturn(otherUser);
        SecurityContextHolder.getContext().setAuthentication(new TestAuthentication(otherUser.getMemberName(), Collections.emptyList()));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectService.create(projectDTO);

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenAnotherProjectIsCreated() throws BadRequestException {
        ProjectDTO projectDTO = new ProjectDTO(null, "anotherCode", "otherName", false);
        Project otherProject = new Project("anotherCode", "otherName");
        TestUtil.setField(otherProject, "id", 2L);
        Mockito.when(genericMapper.map(projectDTO, Project.class)).thenReturn(otherProject);
        Mockito.when(projectRepository.save(otherProject)).thenReturn(otherProject);
        ProjectDTO result = new ProjectDTO(1l, "anotherCode", "otherName", false);
        Mockito.when(genericMapper.map(otherProject, ProjectDTO.class)).thenReturn(result);
        Mockito.when(userRepository.findByMemberName(USER.getMemberName())).thenReturn(USER);
        SecurityContextHolder.getContext().setAuthentication(new TestAuthentication(USER.getMemberName(), Collections.emptyList()));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectService.create(projectDTO);

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenAnotherProjectIsDeleted() throws BadRequestException {
        Project otherProject = new Project("anotherCode", "otherName");
        Mockito.when(projectRepository.findOneByCode(otherProject.getCode())).thenReturn(otherProject);
        SecurityContextHolder.getContext().setAuthentication(new TestAuthentication(USER.getMemberName(), Collections.emptyList()));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectService.delete(otherProject.getCode());

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenUserAddedAsMemberOfAnotherProject() throws BadRequestException {
        Project otherProject = new Project("anotherCode", "otherName");
        Mockito.when(projectRepository.findByContainerIdentifier(otherProject.getCode())).thenReturn(otherProject);
        Mockito.when(userRepository.findByMemberName(USER.getMemberName())).thenReturn(USER);

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectUserMemberService.addMember(otherProject.getCode(), new MemberDTO(USER.getMemberName(), MemberRole.ADMIN));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenAnotherUserAddedAsMemberOfProject() throws BadRequestException {
        User otherUser = new User("otherName", "issuer");
        Mockito.when(projectRepository.findByContainerIdentifier(PROJECT.getCode())).thenReturn(PROJECT);
        Mockito.when(userRepository.findByMemberName(otherUser.getMemberName())).thenReturn(otherUser);

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectUserMemberService.addMember(PROJECT.getCode(), new MemberDTO(otherUser.getMemberName(), MemberRole.ADMIN));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenUserRoleIsChangedOnAnotherProject() throws BadRequestException {
        Project otherProject = new Project("anotherCode", "otherName");
        Mockito.when(projectUserMemberRepository.findByContainerIdentifierAndMemberName(otherProject.getCode(), USER.getMemberName())).thenReturn(new ProjectUserMember(otherProject, USER));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectUserMemberService.updateMemberRole(otherProject.getCode(), USER.getMemberName(), MemberRole.ADMIN);

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenAnotherUserRoleIsChangedOnProject() throws BadRequestException {
        User otherUser = new User("otherName", "issuer");
        Mockito.when(projectUserMemberRepository.findByContainerIdentifierAndMemberName(PROJECT.getCode(), otherUser.getMemberName())).thenReturn(new ProjectUserMember(PROJECT, otherUser));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectUserMemberService.updateMemberRole(PROJECT.getCode(), otherUser.getMemberName(), MemberRole.ADMIN);

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenUserDeletedAsMemberOfAnotherProject() throws BadRequestException {
        Project otherProject = new Project("anotherCode", "otherName");
        Mockito.when(projectUserMemberRepository.findByContainerIdentifierAndMemberName(otherProject.getCode(), USER.getMemberName())).thenReturn(new ProjectUserMember(otherProject, USER));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectUserMemberService.deleteMember(otherProject.getCode(), USER.getMemberName());

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenAnotherUserDeletedAsMemberOfProject() throws BadRequestException {
        User otherUser = new User("otherName", "issuer");
        Mockito.when(projectUserMemberRepository.findByContainerIdentifierAndMemberName(PROJECT.getCode(), otherUser.getMemberName())).thenReturn(new ProjectUserMember(PROJECT, otherUser));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectUserMemberService.deleteMember(PROJECT.getCode(), otherUser.getMemberName());

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenGroupNotContainingUserIsAddedAsMemberOfProject() throws BadRequestException {
        Mockito.when(projectRepository.findByContainerIdentifier(PROJECT.getCode())).thenReturn(PROJECT);
        Mockito.when(groupRepository.findByMemberName(GROUP.getName())).thenReturn(GROUP);

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectGroupMemberService.addMember(PROJECT.getCode(), new MemberDTO(GROUP.getName(), MemberRole.ADMIN));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenGroupContainingUserIsAddedAsMemberOfAnotherProject() throws BadRequestException {
        Project otherProject = new Project("anotherCode", "otherName");
        Mockito.when(projectRepository.findByContainerIdentifier(otherProject.getCode())).thenReturn(otherProject);
        Mockito.when(groupRepository.findByMemberName(GROUP.getName())).thenReturn(GROUP);
        Mockito.when(groupMemberRepository.findAllByIdGroupName(GROUP.getName())).thenReturn(List.of(new GroupMember(GROUP, USER)));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectGroupMemberService.addMember(otherProject.getCode(), new MemberDTO(GROUP.getName(), MemberRole.ADMIN));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenGroupNotContainingUserRoleIsUpdatedOnProject() throws BadRequestException {
        Mockito.when(projectGroupMemberRepository.findByContainerIdentifierAndMemberName(PROJECT.getCode(), GROUP.getName())).thenReturn(new ProjectGroupMember(PROJECT, GROUP));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectGroupMemberService.updateMemberRole(PROJECT.getCode(), GROUP.getName(), MemberRole.ADMIN);

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenGroupContainingUserRoleIsUpdatedOnAnotherProject() throws BadRequestException {
        Project otherProject = new Project("anotherCode", "otherName");
        Mockito.when(projectGroupMemberRepository.findByContainerIdentifierAndMemberName(otherProject.getCode(), GROUP.getName())).thenReturn(new ProjectGroupMember(otherProject, GROUP));
        Mockito.when(groupMemberRepository.findAllByIdGroupName(GROUP.getName())).thenReturn(List.of(new GroupMember(GROUP, USER)));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectGroupMemberService.updateMemberRole(otherProject.getCode(), GROUP.getName(), MemberRole.ADMIN);

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenGroupNotContainingUserIsDeletedAsMemberOfProject() throws BadRequestException {
        Mockito.when(projectGroupMemberRepository.findByContainerIdentifierAndMemberName(PROJECT.getCode(), GROUP.getName())).thenReturn(new ProjectGroupMember(PROJECT, GROUP));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectGroupMemberService.deleteMember(PROJECT.getCode(), GROUP.getName());

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenGroupContainingUserIsDeletedAsMemberOfAnotherProject() throws BadRequestException {
        Project otherProject = new Project("anotherCode", "otherName");
        Mockito.when(projectGroupMemberRepository.findByContainerIdentifierAndMemberName(otherProject.getCode(), GROUP.getName())).thenReturn(new ProjectGroupMember(otherProject, GROUP));
        Mockito.when(groupMemberRepository.findAllByIdGroupName(GROUP.getName())).thenReturn(List.of(new GroupMember(GROUP, USER)));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectGroupMemberService.deleteMember(otherProject.getCode(), GROUP.getName());

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenAnotherUserIsAddedAsMemberOfGroup() throws BadRequestException {
        User otherUser = new User("otherName", "issuer");
        Mockito.when(groupRepository.findByContainerIdentifier(GROUP.getName())).thenReturn(GROUP);
        Mockito.when(userRepository.findByMemberName(otherUser.getMemberName())).thenReturn(otherUser);
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        groupMemberService.addMember(GROUP.getName(), new MemberDTO(otherUser.getMemberName(), MemberRole.ADMIN));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenAnotherUserRoleIsUpdatedOnGroup() throws BadRequestException {
        User otherUser = new User("otherName", "issuer");
        Mockito.when(groupMemberRepository.findByContainerIdentifierAndMemberName(GROUP.getName(), otherUser.getMemberName())).thenReturn(new GroupMember(GROUP, otherUser));
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        groupMemberService.updateMemberRole(GROUP.getName(), otherUser.getMemberName(), MemberRole.ADMIN);

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenAnotherUserIsDeletedAsMemberOfGroup() throws BadRequestException {
        User otherUser = new User("otherName", "issuer");
        Mockito.when(groupMemberRepository.findByContainerIdentifierAndMemberName(GROUP.getName(), otherUser.getMemberName())).thenReturn(new GroupMember(GROUP, otherUser));
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        groupMemberService.deleteMember(GROUP.getName(), otherUser.getMemberName());

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenProjectIsUpdatedAsDefaultProject() throws BadRequestException {
        ProjectDTO projectDTO = new ProjectDTO(1L, PROJECT.getCode(), PROJECT.getName(), true);
        Mockito.when(genericMapper.map(projectDTO, Project.class)).thenReturn(PROJECT);
        Mockito.when(projectRepository.findOneByCode(PROJECT.getCode())).thenReturn(PROJECT);
        SecurityContextHolder.getContext().setAuthentication(new TestAuthentication(USER.getMemberName(), Collections.emptyList()));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectService.update(PROJECT.getCode(), projectDTO);

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenAnotherProjectIsUpdatedAsDefaultProject() throws BadRequestException {
        ProjectDTO projectDTO = new ProjectDTO(2L, "anotherCode", "otherName", true);
        Project otherProject = new Project("anotherCode", "otherName");
        TestUtil.setField(otherProject, "id", 2L);
        Mockito.when(genericMapper.map(projectDTO, Project.class)).thenReturn(otherProject);
        Mockito.when(projectRepository.findOneByCode(otherProject.getCode())).thenReturn(otherProject);
        SecurityContextHolder.getContext().setAuthentication(new TestAuthentication(USER.getMemberName(), Collections.emptyList()));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectService.update(otherProject.getCode(), projectDTO);

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenDefaultProjectIsUpdatedAsNotDefaultProject() throws BadRequestException {
        ProjectDTO projectDTO = new ProjectDTO(1L, PROJECT.getCode(), PROJECT.getName(), false);
        Mockito.when(genericMapper.map(projectDTO, Project.class)).thenReturn(PROJECT);
        Mockito.when(projectRepository.findOneByCode(PROJECT.getCode())).thenReturn(PROJECT);
        Mockito.when(userPreferenceService.getValue(UserPreferenceService.DEFAULT_PROJECT)).thenReturn(PROJECT.getCode());
        SecurityContextHolder.getContext().setAuthentication(new TestAuthentication(USER.getMemberName(), Collections.emptyList()));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectService.update(PROJECT.getCode(), projectDTO);

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void securityServiceGetProjectMemberRolesShouldBeCachedAndNotClearedWhenProjectNotDefaultIsUpdatedAsNotDefaultProject() throws BadRequestException {
        ProjectDTO projectDTO = new ProjectDTO(1L, PROJECT.getCode(), PROJECT.getName(), false);
        Mockito.when(genericMapper.map(projectDTO, Project.class)).thenReturn(PROJECT);
        Mockito.when(projectRepository.findOneByCode(PROJECT.getCode())).thenReturn(PROJECT);
        SecurityContextHolder.getContext().setAuthentication(new TestAuthentication(USER.getMemberName(), Collections.emptyList()));

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());

        projectService.update(PROJECT.getCode(), projectDTO);

        Assertions.assertEquals(1, securityService.getProjectMemberRoles(PROJECT.getCode(), USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndClearedWhenAProjectIsCreated() throws NotUniqueException {
        ProjectDTO projectDTO = new ProjectDTO(null, "anotherCode", "otherName", false);
        Project otherProject = new Project("anotherCode", "otherName");
        TestUtil.setField(otherProject, "id", 2L);
        Mockito.when(genericMapper.map(projectDTO, Project.class)).thenReturn(otherProject);
        Mockito.when(projectRepository.save(otherProject)).thenReturn(otherProject);
        ProjectDTO result = new ProjectDTO(1l, "anotherCode", "otherName", false);
        Mockito.when(genericMapper.map(otherProject, ProjectDTO.class)).thenReturn(result);
        Mockito.when(userRepository.findByMemberName(USER.getMemberName())).thenReturn(USER);
        SecurityContextHolder.getContext().setAuthentication(new TestAuthentication(USER.getMemberName(), Collections.emptyList()));

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        projectService.create(projectDTO);

        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndClearedWhenAProjectIsUpdatedAsDefaultProject() throws BadRequestException {
        ProjectDTO projectDTO = new ProjectDTO(1L, PROJECT.getCode(), PROJECT.getName(), true);
        Mockito.when(genericMapper.map(projectDTO, Project.class)).thenReturn(PROJECT);
        Mockito.when(projectRepository.findOneByCode(PROJECT.getCode())).thenReturn(PROJECT);
        SecurityContextHolder.getContext().setAuthentication(new TestAuthentication(USER.getMemberName(), Collections.emptyList()));

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        projectService.update(PROJECT.getCode(), projectDTO);

        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndClearedWhenDefaultProjectIsUpdatedAsNotDefaultProject() throws BadRequestException {
        Project otherProject = new Project("anotherCode", "otherName");
        TestUtil.setField(otherProject, "id", 2L);
        ProjectDTO projectDTO = new ProjectDTO(2L, otherProject.getCode(), otherProject.getName(), false);
        Mockito.when(genericMapper.map(projectDTO, Project.class)).thenReturn(otherProject);
        Mockito.when(projectRepository.findOneByCode(otherProject.getCode())).thenReturn(otherProject);
        Mockito.when(userPreferenceService.getValue(UserPreferenceService.DEFAULT_PROJECT)).thenReturn(otherProject.getCode());
        SecurityContextHolder.getContext().setAuthentication(new TestAuthentication(USER.getMemberName(), Collections.emptyList()));

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        projectService.update(otherProject.getCode(), projectDTO);

        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndClearedWhenAProjectIsDeleted() throws BadRequestException {
        Mockito.when(projectRepository.findOneByCode(PROJECT.getCode())).thenReturn(PROJECT);
        SecurityContextHolder.getContext().setAuthentication(new TestAuthentication(USER.getMemberName(), Collections.emptyList()));

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        projectService.delete(PROJECT.getCode());

        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndClearedWhenUserAddedAsMemberOfAProject() throws BadRequestException {
        Mockito.when(projectRepository.findByContainerIdentifier(PROJECT.getCode())).thenReturn(PROJECT);
        Mockito.when(userRepository.findByMemberName(USER.getMemberName())).thenReturn(USER);

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        projectUserMemberService.addMember(PROJECT.getCode(), new MemberDTO(USER.getMemberName(), MemberRole.ADMIN));

        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndClearedWhenUserDeletedAsMemberOfAProject() throws BadRequestException {
        Mockito.when(projectUserMemberRepository.findByContainerIdentifierAndMemberName(PROJECT.getCode(), USER.getMemberName())).thenReturn(new ProjectUserMember(PROJECT, USER));

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        projectUserMemberService.deleteMember(PROJECT.getCode(), USER.getMemberName());

        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndClearedWhenGroupContainingUserIsAddedAsMemberOfAProject() throws BadRequestException {
        Mockito.when(projectRepository.findByContainerIdentifier(PROJECT.getCode())).thenReturn(PROJECT);
        Mockito.when(groupRepository.findByMemberName(GROUP.getName())).thenReturn(GROUP);
        Mockito.when(groupMemberRepository.findAllByIdGroupName(GROUP.getName())).thenReturn(List.of(new GroupMember(GROUP, USER)));

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        projectGroupMemberService.addMember(PROJECT.getCode(), new MemberDTO(GROUP.getName(), MemberRole.ADMIN));

        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndClearedWhenGroupContainingUserIsDeletedAsMemberOfAProject() throws BadRequestException {
        Mockito.when(projectGroupMemberRepository.findByContainerIdentifierAndMemberName(PROJECT.getCode(), GROUP.getName())).thenReturn(new ProjectGroupMember(PROJECT, GROUP));
        Mockito.when(groupMemberRepository.findAllByIdGroupName(GROUP.getName())).thenReturn(List.of(new GroupMember(GROUP, USER)));

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        projectGroupMemberService.deleteMember(PROJECT.getCode(), GROUP.getName());

        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndClearedWhenUserIsAddedAsMemberOfGroup() throws BadRequestException {
        Mockito.when(groupRepository.findByContainerIdentifier(GROUP.getName())).thenReturn(GROUP);
        Mockito.when(userRepository.findByMemberName(USER.getMemberName())).thenReturn(USER);

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        groupMemberService.addMember(GROUP.getName(), new MemberDTO(USER.getMemberName(), MemberRole.ADMIN));

        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndClearedWhenUserIsDeletedAsMemberOfGroup() throws BadRequestException {
        Mockito.when(groupMemberRepository.findByContainerIdentifierAndMemberName(GROUP.getName(), USER.getMemberName())).thenReturn(new GroupMember(GROUP, USER));

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        groupMemberService.deleteMember(GROUP.getName(), USER.getMemberName());

        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(2, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndNotClearedWhenProjectIsCreatedByAnotherUser() throws BadRequestException {
        User otherUser = new User("otherName", "issuer");
        ProjectDTO projectDTO = new ProjectDTO(null, PROJECT.getCode(), PROJECT.getName(), false);
        Mockito.when(genericMapper.map(projectDTO, Project.class)).thenReturn(PROJECT);
        Mockito.when(projectRepository.save(PROJECT)).thenReturn(PROJECT);
        ProjectDTO result = new ProjectDTO(1l, PROJECT.getCode(), PROJECT.getName(), true);
        Mockito.when(genericMapper.map(PROJECT, ProjectDTO.class)).thenReturn(result);
        Mockito.when(userRepository.findByMemberName(otherUser.getMemberName())).thenReturn(otherUser);
        SecurityContextHolder.getContext().setAuthentication(new TestAuthentication(otherUser.getMemberName(), Collections.emptyList()));

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        projectService.create(projectDTO);

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndNotClearedWhenProjectNotDefaultIsUpdatedAsNotDefaultProject() throws BadRequestException {
        ProjectDTO projectDTO = new ProjectDTO(1L, PROJECT.getCode(), PROJECT.getName(), false);
        Mockito.when(genericMapper.map(projectDTO, Project.class)).thenReturn(PROJECT);
        Mockito.when(projectRepository.findOneByCode(PROJECT.getCode())).thenReturn(PROJECT);
        SecurityContextHolder.getContext().setAuthentication(new TestAuthentication(USER.getMemberName(), Collections.emptyList()));

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        projectService.update(PROJECT.getCode(), projectDTO);

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndNotClearedWhenAnotherUserAddedAsMemberOfAProject() throws BadRequestException {
        User otherUser = new User("otherName", "issuer");
        Mockito.when(projectRepository.findByContainerIdentifier(PROJECT.getCode())).thenReturn(PROJECT);
        Mockito.when(userRepository.findByMemberName(otherUser.getMemberName())).thenReturn(otherUser);

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        projectUserMemberService.addMember(PROJECT.getCode(), new MemberDTO(otherUser.getMemberName(), MemberRole.ADMIN));

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndNotClearedWhenUserRoleIsChangedOnAProject() throws BadRequestException {
        Mockito.when(projectUserMemberRepository.findByContainerIdentifierAndMemberName(PROJECT.getCode(), USER.getMemberName())).thenReturn(new ProjectUserMember(PROJECT, USER));

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        projectUserMemberService.updateMemberRole(PROJECT.getCode(), USER.getMemberName(), MemberRole.ADMIN);

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndNotClearedWhenAnotherUserDeletedAsMemberOfAProject() throws BadRequestException {
        User otherUser = new User("otherName", "issuer");
        Mockito.when(projectUserMemberRepository.findByContainerIdentifierAndMemberName(PROJECT.getCode(), otherUser.getMemberName())).thenReturn(new ProjectUserMember(PROJECT, otherUser));

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        projectUserMemberService.deleteMember(PROJECT.getCode(), otherUser.getMemberName());

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndNotClearedWhenGroupNotContainingUserIsAddedAsMemberOfAProject() throws BadRequestException {
        Mockito.when(projectRepository.findByContainerIdentifier(PROJECT.getCode())).thenReturn(PROJECT);
        Mockito.when(groupRepository.findByMemberName(GROUP.getName())).thenReturn(GROUP);

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        projectGroupMemberService.addMember(PROJECT.getCode(), new MemberDTO(GROUP.getName(), MemberRole.ADMIN));

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndNotClearedWhenGroupContainingUserRoleIsUpdatedOnAProject() throws BadRequestException {
        Mockito.when(projectGroupMemberRepository.findByContainerIdentifierAndMemberName(PROJECT.getCode(), GROUP.getName())).thenReturn(new ProjectGroupMember(PROJECT, GROUP));
        Mockito.when(groupMemberRepository.findAllByIdGroupName(GROUP.getName())).thenReturn(List.of(new GroupMember(GROUP, USER)));

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        projectGroupMemberService.updateMemberRole(PROJECT.getCode(), GROUP.getName(), MemberRole.ADMIN);

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndNotClearedWhenGroupNotContainingUserIsDeletedAsMemberOfAProject() throws BadRequestException {
        Mockito.when(projectGroupMemberRepository.findByContainerIdentifierAndMemberName(PROJECT.getCode(), GROUP.getName())).thenReturn(new ProjectGroupMember(PROJECT, GROUP));

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        projectGroupMemberService.deleteMember(PROJECT.getCode(), GROUP.getName());

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndNotClearedWhenAnotherUserIsAddedAsMemberOfGroup() throws BadRequestException {
        User otherUser = new User("otherName", "issuer");
        Mockito.when(groupRepository.findByContainerIdentifier(GROUP.getName())).thenReturn(GROUP);
        Mockito.when(userRepository.findByMemberName(otherUser.getMemberName())).thenReturn(otherUser);

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        groupMemberService.addMember(GROUP.getName(), new MemberDTO(otherUser.getMemberName(), MemberRole.ADMIN));

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndNotClearedWhenUserRoleIsUpdatedOnGroup() throws BadRequestException {
        Mockito.when(groupMemberRepository.findByContainerIdentifierAndMemberName(GROUP.getName(), USER.getMemberName())).thenReturn(new GroupMember(GROUP, USER));

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        groupMemberService.updateMemberRole(GROUP.getName(), USER.getMemberName(), MemberRole.ADMIN);

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
    }

    @Test
    void projectServiceFindAllShouldBeCachedAndNotClearedWhenAnotherUserIsDeletedAsMemberOfGroup() throws BadRequestException {
        User otherUser = new User("otherName", "issuer");
        Mockito.when(groupMemberRepository.findByContainerIdentifierAndMemberName(GROUP.getName(), otherUser.getMemberName())).thenReturn(new GroupMember(GROUP, otherUser));

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());

        groupMemberService.deleteMember(GROUP.getName(), otherUser.getMemberName());

        Assertions.assertEquals(1, projectService.findAll(USER.getMemberName()).size());
    }
}
