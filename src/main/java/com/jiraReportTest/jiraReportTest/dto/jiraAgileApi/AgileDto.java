package com.jiraReportTest.jiraReportTest.dto.jiraAgileApi;

import lombok.Data;

@Data
public class AgileDto {
    private int maxResuts;
    private int startAt;
    private SprintDto[] values;
}
