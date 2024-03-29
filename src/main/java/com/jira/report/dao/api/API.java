package com.jira.report.dao.api;

import com.jira.report.config.JiraReportConfigExternal;
import com.jira.report.config.JiraReportConfigGlobal;
import com.jira.report.config.JiraReportConfigIndividuals;
import com.jira.report.config.JiraReportConfigQuery;
import com.jira.report.model.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jira.report.dao.api.constant.APIConstant.UNASSIGNED_FIRST_NAME;
import static com.jira.report.dao.api.constant.APIConstant.UNASSIGNED_NAME;

@Slf4j
@Service
public class API {
    private final JiraAPI jiraAPI;
    private final int maxResults;
    private final JiraGreenhopperAPI jiraGreenhopperAPI;
    private final JiraAgileAPI jiraAgileAPI;
    private final ExternalFiles externalFiles;
    private final String projectName;
    private final int nbSprintsRetrospective;
    private final int nbDaysBacklog;
    private final String unassignedAccountId;
    private final HashMap<String, String> teamPair = new HashMap<>();
    private final HashMap<String, List<String>> teams = new HashMap<>();
    private final HashMap<String, SprintEntity> activeSprints = new HashMap<>();
    private final String planningPath;
    private final String releasePath;
    private final String bug;

    public API(JiraAPI jiraAPI, JiraGreenhopperAPI jiraGreenhopperAPI,
               JiraAgileAPI jiraAgileAPI, ExternalFiles externalFiles,
               JiraReportConfigIndividuals jiraReportConfigIndividuals,
               JiraReportConfigQuery jiraReportConfigQuery,
               JiraReportConfigExternal jiraReportConfigExternal,
               JiraReportConfigGlobal jiraReportConfigGlobal){
        this.jiraAPI = jiraAPI;
        this.jiraGreenhopperAPI = jiraGreenhopperAPI;
        this.jiraAgileAPI = jiraAgileAPI;
        this.externalFiles = externalFiles;
        this.maxResults = jiraReportConfigQuery.getMaxResults();
        String teamNameAlpha = jiraReportConfigIndividuals.getTeamNameOne();
        String teamNameBeta = jiraReportConfigIndividuals.getTeamNameTwo();
        String teamNameGamma = jiraReportConfigIndividuals.getTeamNameThree();
        String teamNameDelta = jiraReportConfigIndividuals.getTeamNameFour();
        String boardIdAlpha = jiraReportConfigGlobal.getBoardIdOne();
        String boardIdBeta = jiraReportConfigGlobal.getBoardIdTwo();
        String boardIdGamma = jiraReportConfigGlobal.getBoardIdThree();
        String boardIdDelta = jiraReportConfigGlobal.getBoardIdFour();
        List<String> teamAlpha = jiraReportConfigIndividuals.getTeamOne();
        List<String> teamBeta = jiraReportConfigIndividuals.getTeamTwo();
        List<String> teamGamma = jiraReportConfigIndividuals.getTeamThree();
        List<String> teamDelta = jiraReportConfigIndividuals.getTeamFour();
        SprintEntity sprintActifAlpha = jiraAgileAPI.getLastlyActiveTeamSprint(teamNameAlpha, boardIdAlpha);
        SprintEntity sprintActifBeta = jiraAgileAPI.getLastlyActiveTeamSprint(teamNameBeta, boardIdBeta);
        SprintEntity sprintActifGamma = jiraAgileAPI.getLastlyActiveTeamSprint(teamNameGamma, boardIdGamma);
        SprintEntity sprintActifDelta = jiraAgileAPI.getLastlyActiveTeamSprint(teamNameDelta, boardIdDelta);
        this.projectName = jiraReportConfigGlobal.getProjectName();
        this.activeSprints.put(teamNameAlpha, sprintActifAlpha);
        this.activeSprints.put(teamNameBeta, sprintActifBeta);
        this.activeSprints.put(teamNameGamma, sprintActifGamma);
        this.activeSprints.put(teamNameDelta, sprintActifDelta);
        this.teamPair.put(teamNameAlpha, boardIdAlpha);
        this.teamPair.put(teamNameBeta, boardIdBeta);
        this.teamPair.put(teamNameGamma, boardIdGamma);
        this.teamPair.put(teamNameDelta, boardIdDelta);
        this.teams.put(teamNameAlpha, teamAlpha);
        this.teams.put(teamNameBeta, teamBeta);
        this.teams.put(teamNameGamma, teamGamma);
        this.teams.put(teamNameDelta, teamDelta);
        this.bug = jiraReportConfigQuery.getBug();
        this.planningPath = jiraReportConfigExternal.getPlanning();
        this.releasePath = jiraReportConfigExternal.getRelease();
        this.unassignedAccountId = jiraReportConfigQuery.getUnassignedAccountId();
        this.nbDaysBacklog = jiraReportConfigGlobal.getNbDaysBacklog();
        this.nbSprintsRetrospective = jiraReportConfigGlobal.getNbSprintsRetrospective();
    }

    /**
     * Creates a Map (key: team name, value: sprint)
     * Service used to retrieve sprints and sprint per team.
     * @return A HashMap containing sprints for each team.
     */
    public Map<String,SprintEntity> callJiraSprintAPI() {
        for (Map.Entry<String, List<String>> entry: teams.entrySet()) {
            String label = entry.getKey();
            TeamEntity t = getTeam(teams.get(label), label);
            activeSprints.get(label).setTeam(t);
            //Set addedWork
            List<String> addedIssues = this.jiraGreenhopperAPI
                    .getAddedIssueKeys(activeSprints.get(label).getId(), this.teamPair.get(activeSprints.get(label).getTeam().getName())) ;
            TicketEntity addedTickets = this.jiraAPI.getTicketsInfos(addedIssues);
            double addedWork = this.jiraAPI.getEstimatedTime(addedIssues);
            activeSprints.get(label).setAddedTickets(addedTickets);
            activeSprints.get(label).setAddedWork(addedWork);

        }
        return activeSprints;
    }

    /**
     *
     * Creates a Map (key: accountId, value: Collaborator)
     * Service used to retrieve collaborators data
     * @return A HashMap containing all collaborators data
     */
    public Map<String, CollaboratorEntity> callJiraCollabSprintAPI() {
        HashMap<String, CollaboratorEntity> collaborators = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : teams.entrySet()) {
            String label = entry.getKey();
            Map<String, CollaboratorEntity> c = getCollaboratorsPerTeam(teams.get(label), label);
            CollaboratorEntity unassigned = c.get(this.unassignedAccountId);
            if(unassigned != null){
                unassigned.setAccountId(this.unassignedAccountId + ' ' + label);
                c.remove(this.unassignedAccountId);
            }else{
                unassigned = CollaboratorEntity.builder()
                        .firstName(UNASSIGNED_FIRST_NAME)
                        .name(UNASSIGNED_NAME)
                        .emailAddress("")
                        .accountId(this.unassignedAccountId + ' ' + label)
                        .storyPoints(StoryPointEntity.builder().build())
                        .tickets(TicketEntity.builder().build())
                        .role("")
                        .assignedIssues(new ArrayList<>())
                        .build();
            }
            c.put(unassigned.getAccountId(), unassigned);
            collaborators.putAll(c);
        }
        return collaborators;
    }

    /**
     * Creates a Backlog object
     * Service used to retrieve project's backlog data (number of bugs,...)
     * @return A Backlog object containing project's data
     */
    public BacklogEntity callJiraBacklogAPI() {
        int[] bugs = jiraAPI.getProjectIncidentBug(projectName, bug,maxResults);
        return BacklogEntity.builder()
                .nbBugs(bugs[0])
                .nbBugsLow(bugs[1])
                .nbBugsMedium(bugs[2])
                .nbBugsHigh(bugs[3])
                .nbBugsHighest(bugs[4])
                .nbBugsCreated(jiraAPI.getCreated(nbDaysBacklog, projectName, bug,maxResults))
                .nbBugsResolved(jiraAPI.getResolved(nbDaysBacklog, projectName, bug,maxResults))
                .build();
    }

    /**
     * Creates a Map (key: team name, value: retrospective object).
     * Service used to retrieve initial and former sprints data (initial commitment,....).
     * @return A map containing retrospective objects.
     */
    public Map<String, RetrospectiveEntity> callJiraRetrospectiveAPI() {
        HashMap<String, RetrospectiveEntity> retrospectives = new HashMap<>();
        for (Map.Entry<String, String> entry : teamPair.entrySet()) {
            String teamName = entry.getKey();
            List<SprintCommitmentEntity> sprints = jiraAgileAPI.getLastlyClosedSprints(nbSprintsRetrospective, teamName, teamPair.get(teamName));
            for (SprintCommitmentEntity sprint : sprints) {
                if (sprint.getId() != null) {
                    if (sprint.getId() != 0) {
                        double[] commitment = jiraGreenhopperAPI.getCommitment(sprint, teamPair.get(teamName));
                        List<String> issueKeys = jiraGreenhopperAPI.getAddedIssueKeys(sprint.getId(), teamPair.get(teamName));
                        sprint.setAddedIssueKeys(issueKeys);
                        sprint.setInitialCommitment(commitment[0]);
                        sprint.setFinalCommitment(commitment[1]);
                        sprint.setAddedWork(commitment[2]);
                        sprint.setCompletedWork(commitment[3]);
                    }
                }
            }
            RetrospectiveEntity r = RetrospectiveEntity.builder()
                    .teamName(teamName)
                    .sprints(sprints)
                    .build();
            retrospectives.put(r.getTeamName(), r);
        }
        return retrospectives;
    }

    /**
     * Creates a List of Release object.
     * Service used to retrieve actual and upcoming releases data.
     * @return A List of release object.
     * @throws IOException, If the file cannot be found.
     * @throws ParseException, If the data cannot be parsed
     */
    public List<ReleaseEntity> getReleases() throws IOException, ParseException {
        return externalFiles.getReleases(releasePath);
    }

    /**
     * Creates a Map (key: accountId, value: Collaborator object)
     * Assign total working time and available time by fetching data from "planning.csv"
     * @param teamAccId All collaborator accountId in a team, plus an empty accountId representing null assignee.
     * @param teamName name of the team linked to previous parameter.
     * @return A Map of collaborators for a specific team
     */
    public Map<String, CollaboratorEntity> getCollaboratorsPerTeam(List<String> teamAccId, String teamName) {
        Map<String, Float[]> planning = externalFiles.getPlanning(planningPath, activeSprints.get(teamName));
        Map<String, CollaboratorEntity> collaborators = new HashMap<>();
        CollaboratorEntity c;
        for (String accId : teamAccId) {
            // In application.yml, unassigned who is defined with a nil value is returned as an empty char
            if(accId.isEmpty()){
                accId = null;
            }
            if ((c = jiraAPI.getCollaborator(accId, activeSprints.get(teamName),this.projectName,maxResults)) != null) {
                if(planning.containsKey(accId)){
                    Float[] timeValues = planning.get(accId);
                    c.setTotalWorkingTime(timeValues[0]);
                    c.setAvailableTime(timeValues[1]);
                }
                collaborators.put(c.getAccountId(), c);
            }
        }
        return collaborators;
    }

    /**
     * Creates a Team object
     * @param teamAccId All collaborator accountId in a team, plus null assignee
     * @param teamName Team name linked to previous parameter
     * @return A Team object containing a list of Collaborators and team name.
     */
    public TeamEntity getTeam(List<String> teamAccId, String teamName) {
        Map<String, CollaboratorEntity> collaborators = getCollaboratorsPerTeam(teamAccId, teamName);
        List<CollaboratorEntity> c = new ArrayList<>(collaborators.values());
        return TeamEntity.builder()
                .name(teamName)
                .collaborators(c)
                .build();
    }
}
