package com.aisa.gitlab;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class GitLabIssuesFetcherGson {

    private static final String GITLAB_API_BASE_URL = "https://gitlab.com/api/v4";
    private static final String PRIVATE_TOKEN = "your_personal_access_token";

    public static void main(String[] args) {
        int projectId = 12345; // Replace with your GitLab project ID
        fetchProjectIssues(projectId);
    }

    public static void fetchProjectIssues(int projectId) {
        RestAssured.baseURI = GITLAB_API_BASE_URL;

        RequestSpecification request = RestAssured.given()
                .header("PRIVATE-TOKEN", PRIVATE_TOKEN)
                .header("Content-Type", "application/json");

        Response response = request.get("/projects/" + projectId + "/issues");

        if (response.getStatusCode() != 200) {
            System.err.println("Failed to fetch issues. HTTP Error Code: " + response.getStatusCode());
            return;
        }

        JsonArray issues = JsonParser.parseString(response.getBody().asString()).getAsJsonArray();

        for (JsonElement element : issues) {
            JsonObject issue = element.getAsJsonObject();

            String title = issue.has("title") ? issue.get("title").getAsString() : "No Title";
            int weight = issue.has("weight") ? issue.get("weight").getAsInt() : 0;
            String startDate = issue.has("start_date") ? issue.get("start_date").getAsString() : "N/A";
            String dueDate = issue.has("due_date") ? issue.get("due_date").getAsString() : "N/A";
            boolean isLinkedWithEpic = issue.has("epic") && !issue.get("epic").isJsonNull();

            System.out.println("Title: " + title);
            System.out.println("Weight: " + weight);
            System.out.println("Start Date: " + startDate);
            System.out.println("Due Date: " + dueDate);
            System.out.println("Linked with Epic: " + isLinkedWithEpic);
            System.out.println("--------------------------");
        }
    }
}