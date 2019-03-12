package com.decathlon.ara.service.support;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TreePosition {

    private Long parentId;
    private double order;

}
