package com.decathlon.ara.web.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {
        "ara.database.target=h2"
})
@AutoConfigureMockMvc
public class TemplateResourceIT {

    @Autowired
    private TemplateResource controller;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void contextLoads() {
        assertThat(controller).isNotNull();
    }

    @Test
    public void shouldBeBadRequest() throws Exception {
        this.mockMvc.perform(get(TemplateResource.PATH + "/" + "cycle-execution")).andDo(print()).andExpect(status().isBadRequest());
    }

    @Test
    public void shouldBeNotFound() throws Exception {
        this.mockMvc.perform(get(TemplateResource.PATH + "/" + "cycle-execution?project=test&branch=develop&cycle=day")).andDo(print()).andExpect(status().isNotFound());
    }

}
