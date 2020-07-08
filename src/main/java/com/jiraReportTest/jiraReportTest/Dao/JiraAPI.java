package com.jiraReportTest.jiraReportTest.Dao;

import com.jiraReportTest.jiraReportTest.Model.Collaborator;
import com.jiraReportTest.jiraReportTest.Model.Sprint;
import com.jiraReportTest.jiraReportTest.Model.Team;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONElement;
import kong.unirest.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

@Repository
public class JiraAPI {

    /*
    DEBUT - Déclaration et définition des variables
     */
    final static String USERNAME = "benjamin.kermani@neo9.fr";
    final static String API_TOKEN = "sqjFnTAVspNM4NxLd1QZC5CB";
    final static String PLANNING_PATH = "planning.csv";
    final static String BOARD_ID = "391";

    final static Sprint sprint = new Sprint();
    static {
        String startDate = "";
        String endDate = "";
        String sprintName = "";
        HttpResponse<JsonNode> response = Unirest.get("https://apriltechnologies.atlassian.net/rest/agile/1.0/board/" + BOARD_ID + "/sprint")
                .basicAuth(USERNAME, API_TOKEN)
                .header("Accept", "application/json")
                .asJson();
        JSONObject myObj = response.getBody().getObject();
        JSONArray values = myObj.getJSONArray("values");
        for (int i = 0; i < values.length(); i++) {
            JSONObject value = values.getJSONObject(i);
            //Condition à modifier quant il n'y aura qu'un seul sprint actif
            if (value.getString("state").equals("active") && value.getString("name").equals("Sprint 30")) {
                sprintName = value.getString("name");
                startDate = value.getString("startDate");
                endDate = value.getString("endDate");
            }
        }
        sprint.setName(sprintName);
        sprint.setStartDate(sprint.toLocalDateTime(startDate));
        sprint.setEndDate(sprint.toLocalDateTime(endDate));
    }

    final static String SPRINT_NAME = "'" + sprint.getName() + "'";
    final static String START_DAY_SPRINT = Integer.toString(sprint.getStartDate().getDayOfMonth());
    final static String END_DAY_SPRINT = Integer.toString(sprint.getEndDate().getDayOfMonth());

    final static ArrayList<String> TEAM_ALPHA = new ArrayList<>(Arrays.asList(
            "5c17b4599f443a65fecae3ca", // Julien Mosset
            "5a9ebe1c4af2372a88a0656b", // Nicolas Ovejero
            "5bcd8282607ed038040177bb", // Pape Thiam
            "5cf921f6b06c540e82580cbd", // Valentin Pierrel
            "5ed76cdf2fdc580b88f3bbef", // Alex Cheuko
            "5e98521a3a8b910c085d6a28", // Kévin Youna
            "unassignedAlpha"

    ));
    final static ArrayList<String> TEAM_BETA = new ArrayList<>(Arrays.asList(
            "5ed754b0f93b230ba59a3d38", // Nicolas Beucler
            "5cb45bb34064460e407eabe4", // Guillermo Garcès
            "5a9ebdf74af2372a88a06565", // Gabriel Roquigny
            "5a2181081594706402dee482", // Etienne Bourgouin
            "5afe92f251d0b7540b43de81", // Malick Diagne
            "5d6e32e06e3e1f0d9623cb5a", // Pierre Tomasina
            "5ed64583620b1d0c168d4e36", // Anthony Hernandez
            "5ef1afd6561e0e0aae904914", // Yong Ma
            "unassignedBeta"
    ));
    final static ArrayList<String> TEAM_GAMMA = new ArrayList<>(Arrays.asList(
            "5aafb6012235812a6233652d", // Lionel Sgarbi
            "5e285008ee264b0e74591993", // Eric Coupal
            "5ed76cc1be03220ab32183be", // Thibault Foucault
            "557058:87b17037-8a69-4b38-8dab-b4cf904e960a", // Pierre Thevenet
            "5d9b0573ea65c10c3fdbaab2", // Maxime Fourt
            "5a8155f0cad06b353733bae8", // Guillaume Coppens
            "5dfd11b39422830cacaa8a79", // Carthy Marie Joseph
            "unassignedGamma"
    ));
    final static ArrayList<String> DONE = new ArrayList<>(Arrays.asList("Abandonné", "Livré", "Terminé",
            "Validé en recette", "A tester", "A valider", "A Livrer"));
    final static ArrayList<String> IN_PROGRESS = new ArrayList<>(Arrays.asList("En cours", "Dév terminé",
            "Refusé en recette", "En attente"));


    final static HashMap<String, String> ID_COLLABS = new HashMap<>();
    static {
        ID_COLLABS.put("5aafb6012235812a6233652d", "scrum"); //Lionel Sjarbi
        ID_COLLABS.put("5c17b4599f443a65fecae3ca", "middle"); // Julien Mosset
        ID_COLLABS.put("5a9ebe1c4af2372a88a0656b", "front"); // Nicolas Ovejero
        ID_COLLABS.put("5bcd8282607ed038040177bb", "middle"); // Pape Thiam
        ID_COLLABS.put("5cf921f6b06c540e82580cbd", "front"); // Valentin Pierrel
        ID_COLLABS.put("5ed76cdf2fdc580b88f3bbef", "middle"); // Alex Cheuko
        ID_COLLABS.put("5ed64583620b1d0c168d4e36", "middle"); // Anthony Hernandez
        ID_COLLABS.put("5cb45bb34064460e407eabe4", "middle"); // Guillermo Garcès
        ID_COLLABS.put("5ed754b0f93b230ba59a3d38", "scrum"); // Nicolas Beucler
        ID_COLLABS.put("5a9ebdf74af2372a88a06565", "middle"); // Gabriel Roquigny
        ID_COLLABS.put("5a2181081594706402dee482", "front"); // Etienne Bourgouin
        ID_COLLABS.put("5afe92f251d0b7540b43de81", "middle"); // Malick Diagne
        ID_COLLABS.put("5e98521a3a8b910c085d6a28", "middle"); // Kévin Youna
        ID_COLLABS.put("5d6e32e06e3e1f0d9623cb5a", "middle"); // Pierre Tomasina
        ID_COLLABS.put("5e285008ee264b0e74591993", "middle"); // Eric Coupal
        ID_COLLABS.put("5ed76cc1be03220ab32183be", "front"); // Thibault Foucault
        ID_COLLABS.put("557058:87b17037-8a69-4b38-8dab-b4cf904e960a", "middle"); // Pierre Thevenet
        ID_COLLABS.put("5d9b0573ea65c10c3fdbaab2", "middle"); // Maxime Fourt
        ID_COLLABS.put("5a8155f0cad06b353733bae8", "middle"); // Guillaume Coppens
        ID_COLLABS.put("5dfd11b39422830cacaa8a79", "front"); // Carthy Marie Joseph
        ID_COLLABS.put("5ef1afd6561e0e0aae904914", "middle"); // Yong Ma
        ID_COLLABS.put(null, ""); // unassigned
    }

    final static String[] REQUESTS_SPRINT = new String[ID_COLLABS.size()];
    final static String[] REQUESTS_WEEK = new String[ID_COLLABS.size()];
    static {
        int i = 0;
        for (String s : ID_COLLABS.keySet()) {
            REQUESTS_SPRINT[i] = "search?jql=project=BMKP+AND+assignee=" + s +
                    "+AND+sprint=" + SPRINT_NAME +"&maxResults=100";
            try {
                REQUESTS_WEEK[i] = "search?jql=project=BMKP+AND+assignee=" + s +
                        "+AND+updated " + URLEncoder.encode("<=", "utf-8") + "-1w &maxResults=100";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            i++;
        }
    }


    /*
    FIN - Déclaration et définition des variables
     */


    /*
    DEBUT - Méthodes utilisés pour obtenir les informations sur la couche données (DAO)
     */
    //Retourne les informations sur le sprint actif
    public static Sprint callJiraSprintAPI(){
        HashMap<String, Team> hmTeams = getTeams(REQUESTS_SPRINT);
        Team [] teams = new Team[hmTeams.size()];
        int i = 0;
        for(String s: hmTeams.keySet()){
            teams[i] = hmTeams.get(s);
            i++;
        }
        sprint.setTeams(teams);
        sprint.setTotalTime(Sprint.durationOfSprint(sprint.getStartDate(),sprint.getEndDate()));
        sprint.setTimeLeft(Sprint.timeLeftOnSprint(sprint.getEndDate()));
        return sprint;
    }
    //Retourne la liste des collaborateurs en prenant en compte les tickets sur le sprint actif
    public static HashMap<String, Collaborator> callJiraCollabSprintAPI() {
        return getCollaborators(REQUESTS_SPRINT);
    }

    //Retourne la liste des collaborateurs en prenant en compte les tickets sur la dernière semaine
    public static HashMap<String, Collaborator> callJiraCollabWeekAPI() {
        return getCollaborators(REQUESTS_WEEK);
    }

    //Retourne la liste des équipes en prenant en compte les tickets sur le sprint actif
    public static HashMap<String, Team> callJiraSprintTeamAPI() {
        return getTeams(REQUESTS_SPRINT);
    }

    //Retourne la liste des équipes en prenant en compte les tickets sur la dernière semaine
    public static HashMap<String, Team> callJiraWeekTeamAPI() {
        return getTeams(REQUESTS_WEEK);
    }


    /*
     FIN - Méthodes utilisés pour obtenir les informations sur la couche données (DAO)
     */


    /*
    DEBUT - Méthodes pour appeler l'API, les services externes et stocker ces données
     */

    //Méthode principale : Appel à l'API JIRA et stockage des informations dans une HashMap
    public static HashMap<String, Collaborator> getCollaborators(String[] requests) {
        HashMap<String, Collaborator> collaborators = new HashMap<>();
        for (String request : requests) {
            int timespent = 0, estimated = 0, remaining = 0;
            double spTotal = 0, spDone = 0, spInProgress = 0, spToDo = 0;
            int ticketsDone = 0;
            int ticketsInProgress = 0;
            int ticketsToDo = 0;
            String accountId = "";
            String emailAddress = "";
            String nom = "";
            String prenom = "";
            HttpResponse<JsonNode> response = Unirest.get("https://apriltechnologies.atlassian.net/rest/api/3/" +
                    request)
                    .basicAuth(USERNAME, API_TOKEN)
                    .header("Accept", "application/json")
                    .asJson();
            JSONObject myObj = response.getBody().getObject();
            int total = myObj.getInt("total");
            JSONArray issues = myObj.getJSONArray("issues");
            if(request.contains("null")){
                List<Collaborator> collabs = new ArrayList<>();
                collabs = getUnassignedPerTeam();
                for(Collaborator c: collabs){
                    collaborators.put(c.getAccountId(),c);
                }
            }
            if (total == 0) {
                continue;
            }
            for (int j = 0; j < issues.length(); j++) {
                //Ensemble des objets JSON utiles
                JSONObject issue = issues.getJSONObject(j);
                JSONObject fields = issue.getJSONObject("fields");
                JSONObject status = fields.getJSONObject("status");
                //Renseignements sur le collaborateur
                /*
                Cas spécial pour les tâches non-assignées
                 */
                if (request.contains("null")) {
                    prenom = "Non";
                    nom = "Assigné";
                } else {
                    JSONObject assignee = fields.getJSONObject("assignee");
                    if (accountId.isEmpty()) {
                        accountId = assignee.getString("accountId");
                    }
                    if (emailAddress.isEmpty() && assignee.has("emailAddress")) {
                        emailAddress = assignee.getString("emailAddress");
                        int indexDot = emailAddress.indexOf(".");
                        int indexAt = emailAddress.indexOf("@");
                        prenom = emailAddress.substring(0, indexDot);
                        nom = emailAddress.substring(indexDot + 1, indexAt);
                    } else if (nom.isEmpty() && prenom.isEmpty()) {
                        //Si plusieurs noms/prenoms le découpage peut-être incorrecte
                        String fullName = assignee.getString("displayName");
                        int fullNameLength = fullName.length();
                        int indexSpace = fullName.indexOf(" ");
                        if (indexSpace < 0) {
                            prenom = fullName;
                        } else {
                            prenom = fullName.substring(0, indexSpace);
                            nom = fullName.substring(indexSpace + 1, fullNameLength);
                        }
                    }
                }
                //Renseignements sur le statut de la demande
                String statut = status.getString("name");
                //Attribution du temps de travail
                if (!fields.isNull("timeestimate")) {
                    remaining += (fields.getInt("timeestimate") / 3600);
                }
                if (DONE.contains(statut)) {
                    ticketsDone++;
                    if (!fields.isNull("timeoriginalestimate")) {
                        estimated += (fields.getInt("timeoriginalestimate") / 3600);
                    }
                } else if (IN_PROGRESS.contains(statut)) {
                    ticketsInProgress++;
                    if (!fields.isNull("aggregatetimespent")) {
                        timespent += (fields.getInt("aggregatetimespent") / 3600);
                    }
                } else {
                    ticketsToDo++;
                }
                //Attribution des story points
                if (!fields.isNull("customfield_10005")) {
                    double curStoryPoints = fields.getDouble("customfield_10005");
                    spTotal += curStoryPoints;
                    if (DONE.contains(statut)) {
                        spDone += curStoryPoints;
                    } else if (IN_PROGRESS.contains(statut)) {
                        spInProgress += curStoryPoints;
                    } else {
                        spToDo += curStoryPoints;
                    }
                }

            }
            //Attribution du role
            String role = ID_COLLABS.get(accountId);
            Collaborator c = new Collaborator();
            c.setAccountId(accountId);
            c.setEmailAddress(emailAddress);
            c.setName(nom);
            c.setFirstName(prenom);
            c.setLoggedTime(timespent);
            c.setEstimatedTime(estimated);
            c.setNbTickets(total);
            c.setNbDone(ticketsDone);
            c.setNbInProgress(ticketsInProgress);
            c.setNbToDo(ticketsToDo);
            c.setRemainingTime(remaining);
            c.setSpTotal(spTotal);
            c.setSpDone(spDone);
            c.setSpInProgress(spInProgress);
            c.setSpToDo(spToDo);
            c.setRole(role);
            collaborators.put(c.getAccountId(), c);
        }
        // On assigne le temps de travail sur le sprint
        HashMap<String, Float> planning = getPlanning(PLANNING_PATH, START_DAY_SPRINT, END_DAY_SPRINT);
        for (String s : planning.keySet()) {
            if (collaborators.containsKey(s)) {
                Collaborator c = collaborators.get(s);
                c.setWorkedTime(planning.get(s));
                collaborators.put(s, c);
            }
        }
        return collaborators;
    }

    //Retourne dans un objet Collaborator les informations, par équipe, où "assignee=null" (MAUVAISE INTEGRATION)
    public static List<Collaborator> getUnassignedPerTeam(){
        List<Collaborator> collaborators = new ArrayList<>();
        String request = "search?jql=project=BMKP+AND+assignee=null+AND+sprint="+SPRINT_NAME+"&maxResults=100";
        //0 : alpha , 1: beta, 2: gamma
        int[] timespent = new int[3];
        int[] estimated = new int[3];
        int[] remaining = new int[3];
        double[] spTotal = new double[3];
        double[] spDone = new double[3];
        double[] spInProgress = new double[3];
        double[] spToDo = new double[3];
        int[] ticketsDone = new int[3];
        int[] ticketsInProgress = new int[3];
        int[] ticketsToDo = new int[3];
        String[] accountId = new String[3];
        String[] prenom = new String[3];
        String[] nom = new String [3];
        HttpResponse<JsonNode> response = Unirest.get("https://apriltechnologies.atlassian.net/rest/api/3/" +
                request)
                .basicAuth(USERNAME, API_TOKEN)
                .header("Accept", "application/json")
                .asJson();
        JSONObject myObj = response.getBody().getObject();
        JSONArray issues = myObj.getJSONArray("issues");
        for (int i = 0; i < issues.length(); i++) {
            JSONObject issue = issues.getJSONObject(i);
            JSONObject fields = issue.getJSONObject("fields");
            JSONObject status = fields.getJSONObject("status");
            JSONArray labels = fields.getJSONArray("labels");
            for(int j = 0; j < labels.length(); j++){
                if(labels.getString(j).contains("ALPHA")){
                    accountId[0] = "unassignedAlpha";
                    prenom[0] = "Non";
                    nom[0] = "Assigné (Alpha)";
                    //Renseignements sur le statut de la demande
                    String statut = status.getString("name");
                    //Attribution du temps de travail
                    if (!fields.isNull("timeestimate")) {
                        remaining[0] += (fields.getInt("timeestimate") / 3600);
                    }
                    if (DONE.contains(statut)) {
                        ticketsDone[0]++;
                        if (!fields.isNull("timeoriginalestimate")) {
                            estimated[0] += (fields.getInt("timeoriginalestimate") / 3600);
                        }
                    } else if (IN_PROGRESS.contains(statut)) {
                        ticketsInProgress[0]++;
                        if (!fields.isNull("aggregatetimespent")) {
                            timespent[0] += (fields.getInt("aggregatetimespent") / 3600);
                        }
                    } else {
                        ticketsToDo[0]++;
                    }
                    //Attribution des story points
                    if (!fields.isNull("customfield_10005")) {
                        double curStoryPoints = fields.getDouble("customfield_10005");
                        spTotal[0] += curStoryPoints;
                        if (DONE.contains(statut)) {
                            spDone[0] += curStoryPoints;
                        } else if (IN_PROGRESS.contains(statut)) {
                            spInProgress[0] += curStoryPoints;
                        } else {
                            spToDo[0] += curStoryPoints;
                        }
                    }

                }
                if(labels.getString(j).contains("BETA")){
                    accountId[1] = "unassignedBeta";
                    prenom[1] = "Non";
                    nom[1] = "Assigné (Beta)";
                    //Renseignements sur le statut de la demande
                    String statut = status.getString("name");
                    //Attribution du temps de travail
                    if (!fields.isNull("timeestimate")) {
                        remaining[1] += (fields.getInt("timeestimate") / 3600);
                    }
                    if (DONE.contains(statut)) {
                        ticketsDone[1]++;
                        if (!fields.isNull("timeoriginalestimate")) {
                            estimated[1] += (fields.getInt("timeoriginalestimate") / 3600);
                        }
                    } else if (IN_PROGRESS.contains(statut)) {
                        ticketsInProgress[1]++;
                        if (!fields.isNull("aggregatetimespent")) {
                            timespent[1] += (fields.getInt("aggregatetimespent") / 3600);
                        }
                    } else {
                        ticketsToDo[1]++;
                    }
                    //Attribution des story points
                    if (!fields.isNull("customfield_10005")) {
                        double curStoryPoints = fields.getDouble("customfield_10005");
                        spTotal[1] += curStoryPoints;
                        if (DONE.contains(statut)) {
                            spDone[1] += curStoryPoints;
                        } else if (IN_PROGRESS.contains(statut)) {
                            spInProgress[1] += curStoryPoints;
                        } else {
                            spToDo[1] += curStoryPoints;
                        }
                    }
                }
                if(labels.getString(j).contains("GAMMA") || labels.getString(j).contains("GAMA")){
                    accountId[2] = "unassignedGamma";
                    prenom[2] = "Non";
                    nom[2] = "Assigné (Gamma)";
                    //Renseignements sur le statut de la demande
                    String statut = status.getString("name");
                    //Attribution du temps de travail
                    if (!fields.isNull("timeestimate")) {
                        remaining[2] += (fields.getInt("timeestimate") / 3600);
                    }
                    if (DONE.contains(statut)) {
                        ticketsDone[2]++;
                        if (!fields.isNull("timeoriginalestimate")) {
                            estimated[2] += (fields.getInt("timeoriginalestimate") / 3600);
                        }
                    } else if (IN_PROGRESS.contains(statut)) {
                        ticketsInProgress[2]++;
                        if (!fields.isNull("aggregatetimespent")) {
                            timespent[2] += (fields.getInt("aggregatetimespent") / 3600);
                        }
                    } else {
                        ticketsToDo[2]++;
                    }
                    //Attribution des story points
                    if (!fields.isNull("customfield_10005")) {
                        double curStoryPoints = fields.getDouble("customfield_10005");
                        spTotal[2] += curStoryPoints;
                        if (DONE.contains(statut)) {
                            spDone[2] += curStoryPoints;
                        } else if (IN_PROGRESS.contains(statut)) {
                            spInProgress[2] += curStoryPoints;
                        } else {
                            spToDo[2] += curStoryPoints;
                        }
                    }
                }
            }
        }
        for(int i = 0; i <3; i++){
            int totalTickets = ticketsDone[i] + ticketsInProgress[i] + ticketsToDo[i];
            Collaborator c = new Collaborator();
            c.setAccountId(accountId[i]);
            c.setName(nom[i]);
            c.setFirstName(prenom[i]);
            c.setLoggedTime(timespent[i]);
            c.setEstimatedTime(estimated[i]);
            c.setNbTickets(totalTickets);
            c.setNbDone(ticketsDone[i]);
            c.setNbInProgress(ticketsInProgress[i]);
            c.setNbToDo(ticketsToDo[i]);
            c.setRemainingTime(remaining[i]);
            c.setSpTotal(spTotal[i]);
            c.setSpDone(spDone[i]);
            c.setSpInProgress(spInProgress[i]);
            c.setSpToDo(spToDo[i]);
            collaborators.add(c);
        }
        return collaborators;
    }


    //Méthode secondaire : Fait un appel à getCollaborators() et aggrège les données par équipes
    public static HashMap<String, Team> getTeams(String[] requests) {
        HashMap<String, Collaborator> collaborators = getCollaborators(requests);
        HashMap<String, Team> teams = new HashMap<>();
        List<Collaborator> collaboratorsAlpha = new ArrayList<>();
        List<Collaborator> collaboratorsBeta = new ArrayList<>();
        List<Collaborator> collaboratorsGamma = new ArrayList<>();
        for (Collaborator c : collaborators.values()) {
            if (TEAM_ALPHA.contains(c.getAccountId())) {
                collaboratorsAlpha.add(c);
            } else if (TEAM_BETA.contains(c.getAccountId())) {
                collaboratorsBeta.add(c);
            } else if (TEAM_GAMMA.contains(c.getAccountId())) {
                collaboratorsGamma.add(c);
            }
        }
        Team alpha = new Team("alpha", collaboratorsAlpha);
        Team beta = new Team("beta", collaboratorsBeta);
        Team gamma = new Team("gamma", collaboratorsGamma);
        teams.put(alpha.getName(), alpha);
        teams.put(beta.getName(), beta);
        teams.put(gamma.getName(), gamma);
        return teams;
    }


    //Lit le planning (CSV) et retourne les informations dans une table de hachage <accountId,workedTime>
    public static HashMap<String, Float> getPlanning(String PLANNING_PATH, String START_DAY_SPRINT, String END_DAY_SPRINT) {
        HashMap<String, Float> planning = new HashMap<>();
        float workedTime = 0;
        String accountId;
        final int indexAccountId = 2; //3ème colonne
        int startIndex = -1;
        int endIndex = -1;
        try {
            FileReader filereader = new FileReader(PLANNING_PATH);
            CSVParser parser = new CSVParserBuilder().withSeparator(',').build();
            CSVReader csvReader = new CSVReaderBuilder(filereader)
                    .withCSVParser(parser)
                    .build();
            //On saute 4 lignes du CSV
            for (int i = 0; i < 4; i++) {
                csvReader.readNext();
            }
            String[] dates = csvReader.readNext();
            for (int i = 0; i < dates.length; i++) {
                if (dates[i].equals(START_DAY_SPRINT) && startIndex < 0) {
                    startIndex = i;
                }
                if (dates[i].equals(END_DAY_SPRINT) && startIndex > 0) {
                    endIndex = i;
                }
            }
            //On saute une ligne
            csvReader.readNext();
            String[] infos;
            while ((infos = csvReader.readNext()) != null) {
                if (!infos[indexAccountId].isEmpty()) {
                    accountId = infos[indexAccountId];
                    workedTime = 8 * (endIndex - startIndex + 1);
                    for (int i = startIndex; i < endIndex; i++) {
                        if (!infos[i].isEmpty()) {
                            workedTime -= parseFloat(infos[i]) * 8;
                        }
                    }
                    planning.put(accountId, workedTime);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return planning;
    }

    /*
    FIN - Méthodes pour appeler l'API, les services externes et stocker ces données
     */
}




