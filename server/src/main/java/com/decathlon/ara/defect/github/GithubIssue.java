package com.decathlon.ara.defect.github;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
class GithubIssue {

    private String url;
    private long number;
    private String title;
    private String state;
    private Date createdAt;
    private Date updatedAt;
    private Date closedAt;
}
