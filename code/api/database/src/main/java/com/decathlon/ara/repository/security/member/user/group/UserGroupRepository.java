package com.decathlon.ara.repository.security.member.user.group;

import com.decathlon.ara.domain.security.member.user.group.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

    boolean existsByName(String name);

    @Modifying
    @Query(value = "delete from UserGroup user_group where user_group.id = ?1")
    void deleteById(Long id);

    List<UserGroup> findAllByProviderName(String providerName);

    List<UserGroup> findAllByProviderNameAndMembersLogin(String providerName, String login);

    List<UserGroup> findAllByProviderNameAndManagersLogin(String providerName, String login);
}
