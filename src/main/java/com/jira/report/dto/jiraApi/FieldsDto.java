package com.jira.report.dto.jiraApi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FieldsDto {
    private AssigneeDto assignee;
    private StatusDto status;
    private PriorityDto priority;
    private int timeoriginalestimate;
    private int timeestimate;
    private int timespent;
    @JsonProperty("customfield_10005")
    private double storyPoints; // story points
    private String created;
    private String updated;
}
