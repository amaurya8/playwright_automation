// Perform validation for all groups
for (int groupId : groupIds) {
        String groupName = getGroupName(groupId);
        LOGGER.log(Level.INFO, "Validating group: {0} (ID: {1})", new Object[]{groupName, groupId});
        validateGroupEpicsAndIssues(groupId);
        }

private static String getGroupName(int groupId) {
        RestAssured.baseURI = GITLAB_API_BASE_URL;

        try {
        RequestSpecification request = RestAssured.given()
        .header("PRIVATE-TOKEN", PRIVATE_TOKEN);

        Response response = request.get("/groups/" + groupId);

        if (response.getStatusCode() == 200) {
        JsonObject groupDetails = new Gson().fromJson(response.getBody().asString(), JsonObject.class);
        return groupDetails.get("name").getAsString(); // Extract the group name
        } else {
        LOGGER.log(Level.WARNING, "Failed to fetch details for group ID {0}. HTTP Status: {1}",
        new Object[]{groupId, response.getStatusCode()});
        return "Unknown Group";
        }
        } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Error fetching group name for group ID {0}: {1}",
        new Object[]{groupId, e.getMessage()});
        return "Unknown Group";
        }
        }