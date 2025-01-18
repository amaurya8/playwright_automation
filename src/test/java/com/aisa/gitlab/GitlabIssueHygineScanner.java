import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class GitLabEpicAndIssueValidator {

    private static final String GITLAB_API_BASE_URL = "https://gitlab.com/api/v4";
    private static final String PRIVATE_TOKEN = "your_personal_access_token";

    public static void main(String[] args) {
        int groupId = 12345; // Replace with your GitLab group ID
        validateEpicsAndIssues(groupId);
    }

    public static void validateEpicsAndIssues(int groupId) {
        RestAssured.baseURI = GITLAB_API_BASE_URL;

        // Initialize Gson
        Gson gson = new Gson();

        // Fetch epics for the group
        RequestSpecification request = RestAssured.given()
                .header("PRIVATE-TOKEN", PRIVATE_TOKEN)
                .header("Content-Type", "application/json");

        Response epicResponse = request.get("/groups/" + groupId + "/epics");

        if (epicResponse.getStatusCode() != 200) {
            System.err.println("Failed to fetch epics. HTTP Error Code: " + epicResponse.getStatusCode());
            return;
        }

        // Parse the response into a JsonArray
        JsonArray epics = gson.fromJson(epicResponse.getBody().asString(), JsonArray.class);

        for (JsonElement epicElement : epics) {
            JsonObject epic = epicElement.getAsJsonObject();

            String epicTitle = epic.has("title") ? epic.get("title").getAsString() : "Unknown Epic";
            String startDate = epic.has("start_date") && !epic.get("start_date").isJsonNull()
                    ? epic.get("start_date").getAsString()
                    : null;
            String endDate = epic.has("due_date") && !epic.get("due_date").isJsonNull()
                    ? epic.get("due_date").getAsString()
                    : null;

            // Check if the epic has a start and end date
            if (startDate == null || endDate == null) {
                System.err.println("Epic '" + epicTitle + "' is missing start or end date.");
                continue;
            }

            System.out.println("Epic '" + epicTitle + "' has valid start and end dates.");

            // Fetch issues for the epic
            int epicId = epic.get("id").getAsInt();
            Response issueResponse = request.get("/groups/" + groupId + "/epics/" + epicId + "/issues");

            if (issueResponse.getStatusCode() != 200) {
                System.err.println("Failed to fetch issues for epic '" + epicTitle + "'. HTTP Error Code: " + issueResponse.getStatusCode());
                continue;
            }

            // Parse the issues response into a JsonArray
            JsonArray issues = gson.fromJson(issueResponse.getBody().asString(), JsonArray.class);

            for (JsonElement issueElement : issues) {
                JsonObject issue = issueElement.getAsJsonObject();

                String issueTitle = issue.has("title") ? issue.get("title").getAsString() : "Unknown Issue";
                Integer weight = issue.has("weight") && !issue.get("weight").isJsonNull()
                        ? issue.get("weight").getAsInt()
                        : null;

                // Check if the issue has a weight
                if (weight == null) {
                    System.err.println("Issue '" + issueTitle + "' in epic '" + epicTitle + "' is missing a weight.");
                } else {
                    System.out.println("Issue '" + issueTitle + "' in epic '" + epicTitle + "' has a valid weight.");
                }
            }
        }
    }
}