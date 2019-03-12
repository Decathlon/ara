package com.decathlon.ara.util;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.transaction.Transactional;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

/**
 * <p>Annotation to use alongside {@code @RunWith(SpringRunner.class)} (this one cannot be inherited and has to be
 * declared manually).</p>
 * <p>Used to write integration tests using the Spring context and making sure everything is rollbacked (both Spring
 * context and database changes in the transaction).</p>
 * <p>{@code @Scheduled} tasks are also disabled to make tests reproducibles: you should call the scheduled methods
 * directly to simulate their triggering.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@SpringBootTest
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class,
        MockitoTestExecutionListener.class })
@DirtiesContext
@Transactional
@TestPropertySource(properties = "ara.scheduling.enable=false")
public @interface TransactionalSpringIntegrationTest {

}
