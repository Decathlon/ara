package com.decathlon.ara.service.dto.setting;

import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class SettingValueDTO {

    @Size(max = 512, message = "The value must not exceed {max} characters.")
    private String value;

}
