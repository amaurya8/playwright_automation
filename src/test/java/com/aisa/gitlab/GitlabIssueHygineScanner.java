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

        // Set of group IDs to validate
        Set<Integer> groupIds = new HashSet<>(Arrays.asList(12345, 67890)); // Replace with actual group IDs

        // Perform validation for all groups
        for (int groupId : groupIds) {
            LOGGER.log(Level.INFO, "Validating group with ID: {0}", groupId);
            validateGroupEpicsAndIssues(groupId);
        }

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
            LOGGER.log(Level.INFO, "Fetching epics - Group ID: {0}, Page: {1}", new Object[]{groupId, currentPage});
            RequestSpecification epicRequest = RestAssured.given()
                    .header("PRIVATE-TOKEN", PRIVATE_TOKEN)
                    .queryParam("page", currentPage)
                    .queryParam("per_page", perPage);

            Response epicResponse = epicRequest.get("/groups/" + groupId + "/epics");

            if (epicResponse.getStatusCode() != 200) {
                LOGGER.log(Level.SEVERE, "Failed to fetch epics for group ID {0}. HTTP Error Code: {1}",
                        new Object[]{groupId, epicResponse.getStatusCode()});
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

        // Crew Delivery Epic Check based on label
        JsonArray labels = epic.getAsJsonArray("labels");
        boolean isCrewDeliveryEpic = labels != null && labels.toString().toLowerCase().contains("crew delivery epic");

        if (isCrewDeliveryEpic) {
            logCrewDeliveryEpic(epicId, epicLink, createdAt);
        } else {
            logEpicFailure(epicId, epicLink, "Not a Crew Delivery epic");
        }
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

        // Create headers and populate data for each sheet (same logic as before)

        try (FileOutputStream fos = new FileOutputStream("GitLab_Validation_Report.xlsx")) {
            workbook.write(fos);
        }

        workbook.close();
        LOGGER.info("Excel report generated successfully.");
    }
}