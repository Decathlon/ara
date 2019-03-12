package com.decathlon.ara.ci.service;

import java.util.Date;
import org.springframework.stereotype.Service;

/**
 * Utilities for date management, as a service, so the methods can be mocked.
 */
@Service
public class DateService {

    /**
     * @return the current date and time
     */
    public Date now() {
        return new Date();
    }

}
