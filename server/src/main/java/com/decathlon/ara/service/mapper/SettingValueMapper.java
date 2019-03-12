package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Setting;
import com.decathlon.ara.service.dto.setting.SettingValueDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Setting and its DTO SettingValueDTO.
 */
@Mapper
public interface SettingValueMapper extends EntityMapper<SettingValueDTO, Setting> {

    // All methods are parameterized for EntityMapper

}
