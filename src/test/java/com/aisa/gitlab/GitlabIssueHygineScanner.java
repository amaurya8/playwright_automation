import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class GitLabGroupValidation {

    private static final String GITLAB_API_BASE_URL = "https://gitlab.com/api/v4";
    private static final String PRIVATE_TOKEN = "your_personal_access_token";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public static void main(String[] args) {
        int groupId = 12345; // Replace with your GitLab group ID
        validateGroupEpicsAndIssues(groupId);
    }

    public static void validateGroupEpicsAndIssues(int groupId) {
        RestAssured.baseURI = GITLAB_API_BASE_URL;
        Gson gson = new Gson();

        // Step 1: Validate Epics
        validateEpicsUnderGroup(groupId, gson);

        // Step 2: Validate Issues in Projects
        validateIssuesInProjectsUnderGroup(groupId, gson);
    }

    private static void validateEpicsUnderGroup(int groupId, Gson gson) {
        System.out.println("Validating epics under group...");
        int currentPage = 1;
        int perPage = 50;
        boolean hasMorePages = true;

        while (hasMorePages) {
            RequestSpecification epicRequest = RestAssured.given()
                    .header("PRIVATE-TOKEN", PRIVATE_TOKEN)
                    .queryParam("page", currentPage)
                    .queryParam("per_page", perPage);

            Response epicResponse = epicRequest.get("/groups/" + groupId + "/epics");

            if (epicResponse.getStatusCode() != 200) {
                System.err.println("Failed to fetch epics. HTTP Error Code: " + epicResponse.getStatusCode());
                return;
            }

            JsonArray epics = gson.fromJson(epicResponse.getBody().asString(), JsonArray.class);

            if (epics.size() == 0) {
                hasMorePages = false;
                break;
            }

            for (JsonElement epicElement : epics) {
                JsonObject epic = epicElement.getAsJsonObject();
                String title = epic.get("title").getAsString();
                String createdAt = epic.get("created_at").getAsString();

                if (isCreatedWithinLastYear(createdAt)) {
                    boolean hasStartDate = epic.has("start_date") && !epic.get("start_date").isJsonNull();
                    boolean hasDueDate = epic.has("due_date") && !epic.get("due_date").isJsonNull();

                    if (!hasStartDate || !hasDueDate) {
                        System.out.println("Epic '" + title + "' is missing start and/or due date.");
                    }
                }
            }

            String nextPageLink = epicResponse.getHeader("X-Next-Page");
            hasMorePages = (nextPageLink != null && !nextPageLink.isEmpty());
            currentPage++;
        }
        System.out.println("Epic validation completed.\n");
    }

    private static void validateIssuesInProjectsUnderGroup(int groupId, Gson gson) {
        System.out.println("Validating issues in projects under group...");
        int currentPage = 1;
        int perPage = 50;
        boolean hasMorePages = true;

        while (hasMorePages) {
            RequestSpecification projectRequest = RestAssured.given()
                    .header("PRIVATE-TOKEN", PRIVATE_TOKEN)
                    .queryParam("page", currentPage)
                    .queryParam("per_page", perPage);

            Response projectResponse = projectRequest.get("/groups/" + groupId + "/projects");

            if (projectResponse.getStatusCode() != 200) {
                System.err.println("Failed to fetch projects. HTTP Error Code: " + projectResponse.getStatusCode());
                return;
            }

            JsonArray projects = gson.fromJson(projectResponse.getBody().asString(), JsonArray.class);

            if (projects.size() == 0) {
                hasMorePages = false;
                break;
            }

            for (JsonElement projectElement : projects) {
                JsonObject project = projectElement.getAsJsonObject();
                int projectId = project.get("id").getAsInt();
                String projectName = project.get("name").getAsString();

                // Validate issues for this project
                validateIssuesInProject(projectId, projectName, gson);
            }

            String nextPageLink = projectResponse.getHeader("X-Next-Page");
            hasMorePages = (nextPageLink != null && !nextPageLink.isEmpty());
            currentPage++;
        }
        System.out.println("Issue validation completed.\n");
    }

    private static void validateIssuesInProject(int projectId, String projectName, Gson gson) {
        System.out.println("Validating issues in project: " + projectName);
        int currentPage = 1;
        int perPage = 50;
        boolean hasMorePages = true;

        while (hasMorePages) {
            RequestSpecification issueRequest = RestAssured.given()
                    .header("PRIVATE-TOKEN", PRIVATE_TOKEN)
                    .queryParam("page", currentPage)
                    .queryParam("per_page", perPage);

            Response issueResponse = issueRequest.get("/projects/" + projectId + "/issues");

            if (issueResponse.getStatusCode() != 200) {
                System.err.println("Failed to fetch issues for project '" + projectName + "'. HTTP Error Code: " + issueResponse.getStatusCode());
                return;
            }

            JsonArray issues = gson.fromJson(issueResponse.getBody().asString(), JsonArray.class);

            for (JsonElement issueElement : issues) {
                JsonObject issue = issueElement.getAsJsonObject();
                String issueTitle = issue.get("title").getAsString();
                String createdAt = issue.get("created_at").getAsString();

                if (isCreatedWithinLastYear(createdAt)) {
                    boolean hasWeight = issue.has("weight") && !issue.get("weight").isJsonNull();

                    if (!hasWeight) {
                        System.out.println("Issue '" + issueTitle + "' in project '" + projectName + "' is missing weight.");
                    }
                }
            }

            String nextPageLink = issueResponse.getHeader("X-Next-Page");
            hasMorePages = (nextPageLink != null && !nextPageLink.isEmpty());
            currentPage++;
        }
    }

    private static boolean isCreatedWithinLastYear(String createdAt) {
        LocalDate createdDate = LocalDate.parse(createdAt, DATE_FORMATTER);
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        return createdDate.isAfter(oneYearAgo);
    }
}