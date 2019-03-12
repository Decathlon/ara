package com.decathlon.ara.defect.rtc.bean;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "workitem")
public class WorkItemContainer {

    @XmlAttribute(name = "href")
    @Getter(onMethod = @__(@XmlTransient))
    private String nextPageUrl;

    private List<WorkItem> workItem;

}
