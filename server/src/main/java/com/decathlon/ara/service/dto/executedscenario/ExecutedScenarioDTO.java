package com.decathlon.ara.service.dto.executedscenario;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutedScenarioDTO {

    private Long id;

    private String featureFile;

    private String featureName;

    private String featureTags;

    private String tags;

    private String severity;

    private String name;

    private String cucumberId;

    private int line;

    private String content;

    private Date startDateTime;

    private String screenshotUrl;

    private String videoUrl;

    private String logsUrl;

    private String httpRequestsUrl;

    private String javaScriptErrorsUrl;

    private String diffReportUrl;

    private String cucumberReportUrl;

    private String apiServer;

    private String seleniumNode;

}
