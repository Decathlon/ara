package com.decathlon.ara.repository;

import com.decathlon.ara.repository.custom.SettingRepositoryCustom;
import com.decathlon.ara.domain.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Setting entity.
 */
@Repository
public interface SettingRepository extends JpaRepository<Setting, Long>, SettingRepositoryCustom {

    Setting findByProjectIdAndCode(long projectId, String code);

}
