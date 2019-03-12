package com.decathlon.ara.defect.rtc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class RtcDateTimeAdapter extends XmlAdapter<String, Date> {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * Convert an RTC-formatted String to a Date.
     *
     * @param value the date to be parsed (can be null)
     * @return the parsed date if it can be parsed
     * @throws ParseException if the date cannot be parsed
     */
    @Override
    public Date unmarshal(String value) throws ParseException {
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        // No constant for thread-safeness
        return new SimpleDateFormat(DATE_TIME_PATTERN).parse(value);
    }

    /**
     * Convert a Date to an RTC-formatted String.
     *
     * @param value the date to be formatted (can be null)
     * @return the formatted date
     */
    @Override
    public String marshal(Date value) {
        if (value == null) {
            return null;
        }

        // No constant for thread-safeness
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_TIME_PATTERN);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(value);
    }

}
