package com.decathlon.ara.defect.rtc.bean;

import com.decathlon.ara.defect.rtc.RtcDateTimeAdapter;
import java.util.Date;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class WorkItem {

    private String id;

    private State state;

    @XmlJavaTypeAdapter(RtcDateTimeAdapter.class)
    @Getter(onMethod = @__(@XmlTransient))
    private Date resolutionDate;

}
