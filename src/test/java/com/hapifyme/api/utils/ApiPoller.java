package com.hapifyme.api.utils;

import java.util.Objects;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class ApiPoller {

    private static final int TIMEOUT_SECONDS = 20;
    private static final int POLL_INTERVAL_SECONDS = 2;

    public static String pollForField(
            String url,
            String apiKey,
            String username,
            String fieldName,
            String expectedValue
    ) {

        Objects.requireNonNull(apiKey, "API Key must not be null");
        Objects.requireNonNull(username, "Username must not be null");

        final String[] result = new String[1];

        await()
                .atMost(TIMEOUT_SECONDS, SECONDS)
                .pollInterval(POLL_INTERVAL_SECONDS, SECONDS)
                .until(() -> {

                    var response =
                            given()
                                    .baseUri(ConfigManager.getBaseUri()) //
                                    .header("Authorization", apiKey)
                                    .queryParam("username_or_email", username)
                                    .when()
                                    .get(url);

                    int status = response.getStatusCode();

                    if (status != 200) {
                        return false;
                    }

                    String value = response.jsonPath().getString(fieldName);

                    if ("NOT_NULL".equals(expectedValue)) {
                        if (value != null && !value.isEmpty()) {
                            result[0] = value;
                            return true;
                        }
                        return false;
                    }

                    if (value != null && value.equals(expectedValue)) {
                        result[0] = value;
                        return true;
                    }

                    return false;
                });

        return result[0];
    }
}