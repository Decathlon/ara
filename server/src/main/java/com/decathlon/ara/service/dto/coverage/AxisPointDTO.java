package com.decathlon.ara.service.dto.coverage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class AxisPointDTO {

    public static final AxisPointDTO ALL = new AxisPointDTO("", "All", null);

    private String id;
    private String name;
    private String tooltip;

}
