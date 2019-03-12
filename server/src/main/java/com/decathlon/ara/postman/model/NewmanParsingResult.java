package com.decathlon.ara.postman.model;

import com.decathlon.ara.postman.bean.Collection;
import com.decathlon.ara.postman.bean.Execution;
import com.decathlon.ara.postman.bean.Failure;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewmanParsingResult {

    private Collection collection;

    private List<Execution> executions;

    private List<Failure> failures;

}
