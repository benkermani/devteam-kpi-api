package com.jira.report.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("jira.report.query")
@Data
public class JiraReportConfigQuery {
    private int maxResults;
    private String unassignedAccountId;
    private String bug;
    private String incident;
    private String active;

}
