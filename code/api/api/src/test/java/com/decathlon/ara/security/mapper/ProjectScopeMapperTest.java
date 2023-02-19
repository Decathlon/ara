package com.decathlon.ara.security.mapper;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.ProjectScope;
import com.decathlon.ara.domain.security.member.user.ProjectRole;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectScopeMapperTest {

    @InjectMocks
    private ProjectScopeMapper projectScopeMapper;

    @Test
    void getUserAccountScopesFromProjectScopes_returnMappedScopes_whenListNeitherEmptyNorNull() {
        // Given
        var scope1 = mock(ProjectScope.class);
        var role1 = ProjectRole.ADMIN;
        var project1 = mock(Project.class);
        var projectCode1 = "project-code1";

        var scope2 = mock(ProjectScope.class);
        var role2 = ProjectRole.MAINTAINER;
        var project2 = mock(Project.class);
        var projectCode2 = "project-code2";

        var scope3 = mock(ProjectScope.class);
        var role3 = ProjectRole.MEMBER;
        var project3 = mock(Project.class);
        var projectCode3 = "project-code3";

        var scopes = Set.of(scope1, scope2, scope3);

        // When
        when(scope1.getRole()).thenReturn(role1);
        when(scope1.getProject()).thenReturn(project1);
        when(project1.getCode()).thenReturn(projectCode1);
        when(scope2.getRole()).thenReturn(role2);
        when(scope2.getProject()).thenReturn(project2);
        when(project2.getCode()).thenReturn(projectCode2);
        when(scope3.getRole()).thenReturn(role3);
        when(scope3.getProject()).thenReturn(project3);
        when(project3.getCode()).thenReturn(projectCode3);

        // Then
        var convertedScopes = projectScopeMapper.getUserAccountScopesFromProjectScopes(scopes);
        assertThat(convertedScopes)
                .extracting("project", "role")
                .containsExactlyInAnyOrder(
                        tuple(projectCode1, UserAccountScopeRole.ADMIN),
                        tuple(projectCode2, UserAccountScopeRole.MAINTAINER),
                        tuple(projectCode3, UserAccountScopeRole.MEMBER)
                );
    }
}
