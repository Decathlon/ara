package com.decathlon.ara.scenario.cypress.bean.media;

import lombok.Data;

import java.util.List;

@Data
public class CypressMedia {

    private String feature;

    private CypressVideo video;

    private List<CypressSnapshot> snapshots;
}
