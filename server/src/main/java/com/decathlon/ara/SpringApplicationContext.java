package com.decathlon.ara;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Wrapper to always return a reference to the Spring Application Context from
 * within non-Spring enabled beans. Unlike Spring MVC's WebApplicationContextUtils
 * we do not need a reference to the Servlet context for this. All we need is
 * for this bean to be initialized during application startup.
 */
@Component
public class SpringApplicationContext implements ApplicationContextAware {

    private static ApplicationContext context;

    /**
     * This is about the same as context.getBean("beanName"), except it has its
     * own static handle to the Spring context, so calling this method statically
     * will give access to the beans by name in the Spring application context.
     * As in the context.getBean("beanName") call, the caller must cast to the
     * appropriate target class. If the bean does not exist, then a Runtime error
     * will be thrown.
     *
     * @param beanClass the class of the bean to get
     * @param <T>       the class of the bean to get
     * @return a reference to the requested bean
     */
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    /**
     * This method is called from within the ApplicationContext once it is
     * done starting up, it will stick a reference to itself into this bean.
     *
     * @param context a reference to the ApplicationContext
     */
    @Override
    @SuppressWarnings("squid:S2696") // Instance methods should not write to "static" fields
    public void setApplicationContext(ApplicationContext context) {
        SpringApplicationContext.context = context;
    }

}
