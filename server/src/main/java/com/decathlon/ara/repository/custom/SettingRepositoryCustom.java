package com.decathlon.ara.repository.custom;

import com.decathlon.ara.domain.Setting;

import java.util.Map;

public interface SettingRepositoryCustom {

    /**
     * @param projectId the ID of the project in which to work
     * @return all settings of the project, as a map of {@link Setting#code} and {@link Setting#value}
     */
    Map<String, String> getProjectSettings(long projectId);

}
