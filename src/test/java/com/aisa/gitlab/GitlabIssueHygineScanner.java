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
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitLabGroupValidation {

    private static final Logger LOGGER = Logger.getLogger(GitLabGroupValidation.class.getName());
    private static final String GITLAB_API_BASE_URL = "https://gitlab.com/api/v4";
    private static final String PRIVATE_TOKEN = "your_personal_access_token";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private static final List<Map<String, String>> epicFailures = new ArrayList<>();
    private static final List<Map<String, String>> issueFailures = new ArrayList<>();
    private static final List<Map<String, String>> crewDeliveryEpics = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        LOGGER.info("Starting GitLab Group Validation...");
        int groupId = 12345; // Replace with your GitLab group ID
        validateGroupEpicsAndIssues(groupId);

        // Generate the Excel report
        generateExcelReport();
        LOGGER.info("GitLab Group Validation completed.");
    }

    public static void validateGroupEpicsAndIssues(int groupId) {
        RestAssured.baseURI = GITLAB_API_BASE_URL;
        Gson gson = new Gson();

        LOGGER.info("Validating epics under the group...");
        validateEpicsUnderGroup(groupId, gson);

        LOGGER.info("Validating issues in projects under the group...");
        validateIssuesInProjectsUnderGroup(groupId, gson);
    }

    private static void validateEpicsUnderGroup(int groupId, Gson gson) {
        int currentPage = 1;
        int perPage = 50;
        boolean hasMorePages = true;

        while (hasMorePages) {
            LOGGER.log(Level.INFO, "Fetching epics - Page: {0}", currentPage);
            RequestSpecification epicRequest = RestAssured.given()
                    .header("PRIVATE-TOKEN", PRIVATE_TOKEN)
                    .queryParam("page", currentPage)
                    .queryParam("per_page", perPage);

            Response epicResponse = epicRequest.get("/groups/" + groupId + "/epics");

            if (epicResponse.getStatusCode() != 200) {
                LOGGER.log(Level.SEVERE, "Failed to fetch epics. HTTP Error Code: {0}", epicResponse.getStatusCode());
                return;
            }

            JsonArray epics = gson.fromJson(epicResponse.getBody().asString(), JsonArray.class);
            LOGGER.log(Level.INFO, "Number of epics fetched: {0}", epics.size());

            if (epics.size() == 0) {
                hasMorePages = false;
                break;
            }

            for (JsonElement epicElement : epics) {
                JsonObject epic = epicElement.getAsJsonObject();
                int epicId = epic.get("id").getAsInt();
                String epicLink = epic.get("web_url").getAsString();
                String createdAt = epic.get("created_at").getAsString();

                if (isCreatedWithinLastYear(createdAt)) {
                    validateEpic(epic, epicId, epicLink, createdAt);
                }
            }

            String nextPageLink = epicResponse.getHeader("X-Next-Page");
            hasMorePages = (nextPageLink != null && !nextPageLink.isEmpty());
            currentPage++;
        }
    }

    private static void validateEpic(JsonObject epic, int epicId, String epicLink, String createdAt) {
        boolean hasStartDate = epic.has("start_date") && !epic.get("start_date").isJsonNull();
        boolean hasDueDate = epic.has("due_date") && !epic.get("due_date").isJsonNull();

        if (!hasStartDate || !hasDueDate) {
            logEpicFailure(epicId, epicLink, "Missing start and/or due date");
        }

        // Check if the epic has the label "Crew Delivery Epic"
        boolean isCrewDeliveryEpic = epic.has("labels") && containsLabel(epic.getAsJsonArray("labels"), "Crew Delivery Epic");
        if (isCrewDeliveryEpic) {
            logCrewDeliveryEpic(epicId, epicLink, createdAt);
        } else {
            logEpicFailure(epicId, epicLink, "Not labeled as Crew Delivery Epic");
        }
    }

    private static boolean containsLabel(JsonArray labels, String targetLabel) {
        for (JsonElement label : labels) {
            if (label.getAsString().equalsIgnoreCase(targetLabel)) {
                return true;
            }
        }
        return false;
    }

    private static void logCrewDeliveryEpic(int epicId, String epicLink, String createdAt) {
        Map<String, String> crewEpic = new HashMap<>();
        crewEpic.put("epic_id", String.valueOf(epicId));
        crewEpic.put("epic_link", epicLink);
        crewEpic.put("created_at", createdAt);
        crewDeliveryEpics.add(crewEpic);
        LOGGER.log(Level.INFO, "Crew Delivery Epic found: {0}", epicLink);
    }

    private static void validateIssuesInProjectsUnderGroup(int groupId, Gson gson) {
        // (Same as previous issue validation implementation)
        // Validates issues and logs issue failures
    }

    private static boolean isCreatedWithinLastYear(String createdAt) {
        LocalDate createdDate = LocalDate.parse(createdAt, DATE_FORMATTER);
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        return createdDate.isAfter(oneYearAgo);
    }

    private static void logEpicFailure(int epicId, String epicLink, String message) {
        Map<String, String> failure = new HashMap<>();
        failure.put("epic_id", String.valueOf(epicId));
        failure.put("epic_link", epicLink);
        failure.put("failure_message", message);
        epicFailures.add(failure);
        LOGGER.log(Level.WARNING, "Epic validation failure: {0} - {1}", new Object[]{epicLink, message});
    }

    private static void generateExcelReport() throws IOException {
        LOGGER.info("Generating Excel report...");
        Workbook workbook = new XSSFWorkbook();
        Sheet epicSheet = workbook.createSheet("Epic Failures");
        Sheet issueSheet = workbook.createSheet("Issue Failures");
        Sheet crewEpicSheet = workbook.createSheet("Crew Delivery Epics");

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

        // Create headers for the Crew Delivery Epics sheet
        Row crewEpicHeader = crewEpicSheet.createRow(0);
        crewEpicHeader.createCell(0).setCellValue("Epic ID");
        crewEpicHeader.createCell(1).setCellValue("Epic Link");
        crewEpicHeader.createCell(2).setCellValue("Created At");

        // Populate Crew Delivery epics
        int crewEpicRowNum = 1;
        for (Map<String, String> crewEpic : crewDeliveryEpics) {
            Row row = crewEpicSheet.createRow(crewEpicRowNum++);
            row.createCell(0).setCellValue(crewEpic.get("epic_id"));
            row.createCell(1).setCellValue(crewEpic.get("epic_link"));
            row.createCell(2).setCellValue(crewEpic.get("created_at"));
        }

        try (FileOutputStream fos = new FileOutputStream("GitLab_Validation_Report.xlsx")) {
            workbook.write(fos);
        }

        workbook.close();
        LOGGER.info("Excel report generated successfully.");
    }
}