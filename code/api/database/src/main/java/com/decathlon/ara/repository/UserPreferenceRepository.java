package com.decathlon.ara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.decathlon.ara.domain.UserPreference;
import com.decathlon.ara.domain.UserPreference.UserPreferencePk;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, UserPreferencePk> {

    UserPreference findByIdUserIdAndIdKey(String id, String key);

}
