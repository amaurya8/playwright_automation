import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GitLabGroupValidation {

    private static final String GITLAB_API_BASE_URL = "https://gitlab.com/api/v4";
    private static final String PRIVATE_TOKEN = "your_personal_access_token";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final Set<Integer> VALID_PARENT_EPICS = Set.of(99999, 88888, 77777); // Predefined parent epics

    // Data structures to store validation failures
    private static final List<Map<String, String>> epicFailures = new ArrayList<>();
    private static final List<Map<String, String>> issueFailures = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        int groupId = 12345; // Replace with your GitLab group ID
        validateGroupEpicsAndIssues(groupId);

        // Generate the Excel report
        generateExcelReport();
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
                int epicId = epic.get("id").getAsInt();
                String epicTitle = epic.get("title").getAsString();
                String epicLink = epic.get("web_url").getAsString();
                String createdAt = epic.get("created_at").getAsString();

                if (isCreatedWithinLastYear(createdAt)) {
                    // Check for start and due date
                    boolean hasStartDate = epic.has("start_date") && !epic.get("start_date").isJsonNull();
                    boolean hasDueDate = epic.has("due_date") && !epic.get("due_date").isJsonNull();

                    if (!hasStartDate || !hasDueDate) {
                        Map<String, String> failure = new HashMap<>();
                        failure.put("epic_id", String.valueOf(epicId));
                        failure.put("epic_link", epicLink);
                        failure.put("failure_message", "Missing start and/or due date");
                        epicFailures.add(failure);
                    }

                    // Check for valid parent epic
                    boolean hasParentEpic = epic.has("parent_id") && !epic.get("parent_id").isJsonNull();
                    if (hasParentEpic) {
                        int parentId = epic.get("parent_id").getAsInt();
                        if (!VALID_PARENT_EPICS.contains(parentId)) {
                            Map<String, String> failure = new HashMap<>();
                            failure.put("epic_id", String.valueOf(epicId));
                            failure.put("epic_link", epicLink);
                            failure.put("failure_message", "Invalid parent ID: " + parentId);
                            epicFailures.add(failure);
                        }
                    } else {
                        Map<String, String> failure = new HashMap<>();
                        failure.put("epic_id", String.valueOf(epicId));
                        failure.put("epic_link", epicLink);
                        failure.put("failure_message", "No parent ID");
                        epicFailures.add(failure);
                    }
                }
            }

            String nextPageLink = epicResponse.getHeader("X-Next-Page");
            hasMorePages = (nextPageLink != null && !nextPageLink.isEmpty());
            currentPage++;
        }
    }

    private static void validateIssuesInProjectsUnderGroup(int groupId, Gson gson) {
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

                validateIssuesInProject(projectId, projectName, gson);
            }

            String nextPageLink = projectResponse.getHeader("X-Next-Page");
            hasMorePages = (nextPageLink != null && !nextPageLink.isEmpty());
            currentPage++;
        }
    }

    private static void validateIssuesInProject(int projectId, String projectName, Gson gson) {
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
                int issueId = issue.get("id").getAsInt();
                String issueTitle = issue.get("title").getAsString();
                String issueLink = issue.get("web_url").getAsString();
                String createdAt = issue.get("created_at").getAsString();

                if (isCreatedWithinLastYear(createdAt)) {
                    boolean hasWeight = issue.has("weight") && !issue.get("weight").isJsonNull();

                    if (!hasWeight) {
                        Map<String, String> failure = new HashMap<>();
                        failure.put("issue_id", String.valueOf(issueId));
                        failure.put("issue_link", issueLink);
                        failure.put("failure_message", "Missing weight");
                        issueFailures.add(failure);
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

    private static void generateExcelReport() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet epicSheet = workbook.createSheet("Epic Failures");
        Sheet issueSheet = workbook.createSheet("Issue Failures");

        // Create headers for the epic sheet
        Row epicHeader = epicSheet.createRow(0);
        epicHeader.createCell(0).setCellValue("Epic ID");
        epicHeader.createCell(1).setCellValue("Epic Link");
        epicHeader.createCell(2).setCellValue("Failure Message");

        // Populate epic failures
        int epicRowNum = 1;
        for (Map<String, String> failure : epicFailures) {
            Row row = epicSheet.createRow(epicRowNum++);
            row.createCell(0).setCellValue(failure.get("epic_id"));
            row.createCell(1).setCellValue(failure.get("epic_link"));
            row.createCell(2).setCellValue(failure.get("failure_message"));
        }

        // Create headers for the issue sheet
        Row issueHeader = issueSheet.createRow(0);
        issueHeader.createCell(0).setCellValue("Issue ID");
        issueHeader.createCell(1).setCellValue("Issue Link");
        issueHeader.createCell(2).setCellValue("Failure Message");

        // Populate issue failures
        int issueRowNum = 1;
        for (Map<String, String> failure : issueFailures) {
            Row row = issueSheet.createRow(issueRowNum++);
            row.createCell(0).setCellValue(failure.get("issue_id"));
            row.createCell(1).setCellValue(failure.get("issue_link"));
            row.createCell(2).setCellValue(failure.get("failure_message"));
        }

        // Write the workbook to a file
        try (FileOutputStream fos = new FileOutputStream("GitLabValidationReport.xlsx")) {
            workbook.write(fos);
        }
        workbook.close();
        System.out.println("Excel report generated: GitLabValidationReport.xlsx");
    }
}

<dependency>
<groupId>org.apache.poi</groupId>
<artifactId>poi</artifactId>
<version>5.2.3</version>
</dependency>