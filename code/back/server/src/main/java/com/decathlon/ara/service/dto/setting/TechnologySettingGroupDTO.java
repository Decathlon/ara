package com.decathlon.ara.service.dto.setting;

import com.decathlon.ara.domain.enumeration.Technology;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class TechnologySettingGroupDTO extends SettingGroupDTO {

    private Technology technology;

}
