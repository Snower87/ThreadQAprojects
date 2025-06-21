package tests.junit5.api;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;

public class SimpleApiTests {
    String base_url = "https://fakestoreapi.in/api/users/";

    @Test
    public void getAllUsersTest() {
        given().get("https://fakestoreapi.in/api/users")
                .then().statusCode(200)
                .log().all();
    }

    @Test
    public void getSingleUserTest() {
        int userId = 2;
        given().pathParam("userId", userId)
                .get("https://fakestoreapi.in/api/users/{userId}")
                .then()
                .log().all()
                .body("user.id", equalTo(userId))
                .body("user.address.zipcode", Matchers.matchesPattern("\\d{5}"));
    }

    @Test
    public void sendOnequery() {
        given().get(base_url)
                .then()
                .log().all();
    }

    @Test
    public void getUserInfoAbout5User() {
        int userId = 5;
        given().pathParam("userId", userId)
                .get("https://fakestoreapi.in/api/users/{userId}")
                .then()
                .body("status", equalTo("SUCCESS"))
                .body("user.id", equalTo(5))
                .body("user.address.city", equalTo("West Paulport"))
                .log().all();
    }
}
