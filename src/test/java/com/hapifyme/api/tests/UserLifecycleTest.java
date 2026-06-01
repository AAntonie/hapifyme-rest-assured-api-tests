package com.hapifyme.api.tests;

import com.hapifyme.api.models.LoginResponse;
import com.hapifyme.api.models.ProfileResponse;
import com.hapifyme.api.models.UpdateProfileRequest;

import com.hapifyme.api.models.RegisterRequest;
import com.hapifyme.api.models.RegisterResponse;
import com.hapifyme.api.utils.ApiPoller;
import com.hapifyme.api.utils.ConfigManager;
import com.hapifyme.api.utils.DataGenerator;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.Test;


import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class UserLifecycleTest {

    @Test
    public void testRegisterWithTimestamp() {

        String uniqueEmail = DataGenerator.generateEmail();
        String password = "Pass1234!";

        RegisterRequest registerBody =
                new RegisterRequest("John", "Doe", uniqueEmail, password);

        // REGISTER
        Response response =
                given()
                        .baseUri(ConfigManager.getBaseUri())
                        .contentType(ContentType.JSON)
                        .body(registerBody)
                        .when()
                        .post("user/register.php")
                        .then()
                        .extract()
                        .response();

        assertThat(response.getStatusCode(), equalTo(201));

        RegisterResponse registerResponse = response.as(RegisterResponse.class);

        assertThat(registerResponse.getStatus(), equalTo("success"));
        assertThat(registerResponse.getApiKey(), notNullValue());
        assertThat(registerResponse.getUsername(), notNullValue());

        //  ASYNC CHECK (DOAR APEL LA POLLER)
        String confirmationToken = ApiPoller.pollForField(
                "user/retrieve_token.php",
                registerResponse.getApiKey(),
                registerResponse.getUsername(),
                "confirmation_token",
                "NOT_NULL"
        );

        System.out.println("Confirmation token: " + confirmationToken);

        assertThat(confirmationToken, notNullValue());
        // =====================================================
        // CONFIRM EMAIL (ACTIVATE CONT)
        // =====================================================

        Response confirmResponse =
                given()
                        .baseUri(ConfigManager.getBaseUri())
                        .queryParam("token", confirmationToken)
                        .when()
                        .get("user/confirm_email.php");

        System.out.println("CONFIRM STATUS: " + confirmResponse.getStatusCode());
        System.out.println("CONFIRM RESPONSE: " + confirmResponse.asString());

        assertThat(confirmResponse.getStatusCode(), equalTo(200));
        assertThat(confirmResponse.jsonPath().getString("status"), equalTo("success"));


        //

        // =================
        // LOGIN
        // =================
        LoginResponse login =
                given()
                        .baseUri(ConfigManager.getBaseUri())
                        .contentType(ContentType.JSON)
                        .body(
                                new java.util.HashMap<String, String>() {{
                                    put("username", registerResponse.getUsername());
                                    put("password", password);
                                }}
                        )
                        .when()
                        .post("user/login.php")
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(LoginResponse.class);

        // ASSERTIONS
        assertThat(login.getStatus(), equalTo("success"));

        System.out.println("Login message: " + login.getMessage());

        // TOKEN pentru pașii următori
        String bearerToken = login.getToken();

        System.out.println("Bearer token: " + bearerToken);

        assertThat(bearerToken, notNullValue());

        // =========================
        // READ & VALIDATE PROFILE
        // =========================

        Response profileResponse =
                given()
                        .log().all()
                        .baseUri(ConfigManager.getBaseUri())
                        .header("Authorization", registerResponse.getApiKey())
                        .queryParam("user_id", registerResponse.getUserId())
                        .when()
                        .get("user/get_profile.php")
                        .then()
                        .extract()
                        .response();

        System.out.println("PROFILE RESPONSE:");
        System.out.println(profileResponse.getBody().asString());

        // POJO MAPPING
        ProfileResponse profile = profileResponse.as(ProfileResponse.class);

        // ASSERTIONS
        assertThat(profile.getStatus(), equalTo("success"));

        assertThat(profile.getUser().getId(), equalTo(registerResponse.getUserId()));
        assertThat(profile.getUser().getEmail(), equalTo(uniqueEmail));
        assertThat(profile.getUser().getFirst_name(), equalTo("John"));
        assertThat(profile.getUser().getLast_name(), equalTo("Doe"));

        System.out.println("PROFILE VALIDAT CU SUCCES");


        // UPDATE PROFILE
        String updatedFirstName = "JohnUpdated_" + System.currentTimeMillis();

        UpdateProfileRequest updateBody =
                new UpdateProfileRequest(
                        registerResponse.getUserId(),
                        updatedFirstName,
                        "Doe",
                        uniqueEmail,
                        "default_profile_pic.png"
                );

        Response updateResponse =
                given()
                        .log().all()
                        .baseUri(ConfigManager.getBaseUri())
                        .header("Authorization", registerResponse.getApiKey())
                        .contentType(ContentType.JSON)
                        .body(updateBody)
                        .when()
                        .put("user/update_profile.php")
                        .then()
                        .extract()
                        .response();

        // ASSERT STATUS CODE
        assertThat(updateResponse.getStatusCode(), equalTo(200));

        // ASSERT BODY (WITH RESPONSE)
        assertThat(
                updateResponse.jsonPath().getString("status"),
                equalTo("success")
        );

        assertThat(
                updateResponse.jsonPath().getString("message"),
                equalTo("Profile updated successfully.")
        );

        System.out.println("UPDATE RESPONSE:");
        System.out.println(updateResponse.getBody().asString());

        // ==============
        // DELETE PROFILE
        // ==============

        Response deleteResponse =
                given()
                        .log().all()
                        .baseUri(ConfigManager.getBaseUri())
                        .header("Authorization", "Bearer " + bearerToken)
                        .when()
                        .delete("user/delete_profile.php")
                        .then()
                        .extract()
                        .response();

        assertThat(deleteResponse.getStatusCode(), equalTo(200));

        System.out.println("DELETE RESPONSE:");
        System.out.println(deleteResponse.getBody().asString());

        // =========================
        // NEGATIVE CHECK
        // =========================

        Response profileAfterDelete =
                given()
                        .log().all()
                        .baseUri(ConfigManager.getBaseUri())
                        .header("Authorization", registerResponse.getApiKey())
                        .queryParam("user_id", registerResponse.getUserId())
                        .when()
                        .get("user/get_profile.php")
                        .then()
                        .extract()
                        .response();

        System.out.println("PROFILE AFTER DELETE:");
        System.out.println(profileAfterDelete.getBody().asString());

        int statusCodeAfterDelete = profileAfterDelete.getStatusCode();

        System.out.println("STATUS AFTER DELETE = " + statusCodeAfterDelete);

        // ======
        // ASSERT
        // ======

        assertThat(
                profileAfterDelete.jsonPath().getString("status"),
                equalTo("error")
        );

        assertThat(
                profileAfterDelete.jsonPath().getString("message"),
                equalTo("User not found.")
        );
    }


}
