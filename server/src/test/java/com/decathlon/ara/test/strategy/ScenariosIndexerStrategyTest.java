package com.decathlon.ara.test.strategy;

import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.test.CucumberScenariosIndexer;
import com.decathlon.ara.test.PostmanScenariosIndexer;
import com.decathlon.ara.test.ScenariosIndexer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ScenariosIndexerStrategyTest {

    @Mock
    private PostmanScenariosIndexer postmanScenariosIndexer;

    @Mock
    private CucumberScenariosIndexer cucumberScenariosIndexer;

    @InjectMocks
    private ScenariosIndexerStrategy scenariosIndexerStrategy;

    @Test
    public void getScenariosIndexer_returnEmpty_whenTechnologyIsNull(){
        // Given

        // When

        // Then
        Optional<ScenariosIndexer> scenariosIndexer = scenariosIndexerStrategy.getScenariosIndexer(null);
        assertThat(scenariosIndexer).isEmpty();
    }

    @Test
    public void getScenariosIndexer_returnCucumberIndexer_whenTechnologyIsCucumber(){
        // Given

        // When

        // Then
        Optional<ScenariosIndexer> scenariosIndexer = scenariosIndexerStrategy.getScenariosIndexer(Technology.CUCUMBER);
        assertThat(scenariosIndexer).isEqualTo(Optional.of(cucumberScenariosIndexer));
    }

    @Test
    public void getScenariosIndexer_returnPostmanIndexer_whenTechnologyIsPostman(){
        // Given

        // When

        // Then
        Optional<ScenariosIndexer> scenariosIndexer = scenariosIndexerStrategy.getScenariosIndexer(Technology.POSTMAN);
        assertThat(scenariosIndexer).isEqualTo(Optional.of(postmanScenariosIndexer));
    }
}
