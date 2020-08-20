package com.decathlon.ara.service.dto.setting;

import com.decathlon.ara.domain.enumeration.Technology;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class TechnologySettingGroupDTO extends SettingGroupDTO {

    private Technology technology;

}
