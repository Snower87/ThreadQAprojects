package tests.junit5.api;

import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

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

    @Test
    public void getAllUsersWithLimitTest() {
        //1. высылаем запрос с queryParam: api/users?limit=2
        int limitSize = 2;
        given().queryParam("limit", limitSize)
                .when()
                .get(base_url)
                .then()
                .log().all()
                .statusCode(200)
                .body("users", Matchers.hasSize(limitSize));
    }

    @Test
    public void getAllUsersSortByDescTest() {
        //1. тестируем сортировку данных, полученных с сайта и собственную
        //2. высылаем запрос с участие queryParam: /api/users?sort=desc
        String sortType = "desc";
        Response sortedResponse = given().queryParam("sort", sortType)
                .get("https://fakestoreapi.in/api/users/")
                .then().log().all()
                .statusCode(200)
                .extract().response();

        Response notSorted = given()
                .get("https://fakestoreapi.in/api/users/")
                .then().log().all()
                .extract().response();

        List<Integer> sortedResponseIds = sortedResponse.jsonPath().getList("users.id");
        List<Integer> notSortedIds = notSorted.jsonPath().getList("users.id");

        List<Integer> sortedByCode = notSortedIds.stream()
                .sorted(Comparator.reverseOrder())    // reverseOrder() - инверстно (от большего к меньшего)
                                                      // naturalOrder () - естественный порядок (по возрастанию)
                .toList();      // -> .collect(Collectors.toList());

        //Actual   :[20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1]
        Assertions.assertNotEquals(sortedResponseIds, notSortedIds);
        Assertions.assertEquals(sortedByCode, sortedResponseIds);
    }

    @Test
    public void getTimeResponse() {
        //1. получаем время ответа от сервера
        long time = given()
                .get(base_url)
                .then()
                .statusCode(200)
                .extract()
                .timeIn(TimeUnit.MILLISECONDS);
        System.out.println("Time responce: " + time + " ms");
    }
}
