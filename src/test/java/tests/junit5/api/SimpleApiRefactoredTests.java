package tests.junit5.api;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import models.fakeapiusers.Address;
import models.fakeapiusers.AuthData;
import models.fakeapiusers.Geolocation;
import models.fakeapiusers.Name;
import models.fakeapiusers.POJORequestAddUser;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class SimpleApiRefactoredTests {
    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "https://fakestoreapi.com";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @Test
    public void getAllUsersTest() {
        given().get("/users")
                .then()
                .statusCode(200);

    }

    @Test
    public void getSingleUserTest() {
        int userId = 2;
        POJORequestAddUser response = given()
                .pathParam("userId", userId)
                .get("/users/{userId}")
                .then()
                .extract().as(POJORequestAddUser.class);

        Name name = given()
                .pathParam("userId", userId)
                .get("/users/{userId}")
                .then()
                .extract().jsonPath().getObject("name", Name.class);

        // --> реализация ДО
        //.body("user.id", equalTo(userId))
        //.body("user.address.zipcode", Matchers.matchesPattern("\\d{5}"));

        // --> реализация ПОСЛЕ
        Assertions.assertEquals(userId, response.getId());
        Assertions.assertTrue(response.getAddress().getZipcode().matches("\\d{5}-\\d{4}"));
    }

    @Test
    public void getAllUsersWithLimitTest() {
        //1. высылаем запрос с queryParam: api/users?limit=2
        int limitSize = 2;
        List<POJORequestAddUser> users = given()
                .queryParam("limit", limitSize)
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getList("", POJORequestAddUser.class);

        Assertions.assertEquals(2, users.size());
    }

    @Test
    public void getAllUsersSortByDescTest() {
        //1. тестируем сортировку данных, полученных с сайта и собственную
        //2. высылаем запрос с участие queryParam: /api/users?sort=desc
        String sortType = "desc";
        List<POJORequestAddUser> usersSorted = given()
                .queryParam("sort", sortType)
                .get("/users")
                .then()
                .extract()
                .jsonPath().getList("", POJORequestAddUser.class);

        List<POJORequestAddUser> usersNotSorted = given()
                .get("/users")
                .then()
                .extract()
                .jsonPath().getList("", POJORequestAddUser.class);

        List<Integer> sortedResponseIds = usersSorted.stream().map(x -> x.getId()).collect(Collectors.toList());
        List<Integer> notSortedIds = usersNotSorted.stream().map(POJORequestAddUser::getId).toList();

        List<Integer> sortedByCode = usersNotSorted.stream().map(x -> x.getId()).sorted(Comparator.reverseOrder()).toList();

        //Actual   :[20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1]
        Assertions.assertNotEquals(usersSorted, usersNotSorted);
        Assertions.assertEquals(sortedResponseIds, sortedByCode);
    }

    @Test
    public void addNewUserTest() {
        POJORequestAddUser user = getTestUser();

        Integer userId = given().body(user)
                .contentType(ContentType.JSON)
                .post("/users")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getInt("id");

        Assertions.assertNotNull(userId);
    }

    private POJORequestAddUser getTestUser() {
        Random random = new Random();
        Name name = new Name("Thomas", "Anderson");
        Geolocation geolocation = new Geolocation(70.123, 100.1);

        Address address = Address.builder()
                .city("Москва")
                .street("Noviy Arbat 12")
                .number(String.valueOf(random.nextInt(100)))
                .zipcode("54231-4231")
                .geolocation(geolocation)
                .build();

        return POJORequestAddUser.builder()
                .id(5)
                .email("fakemail@gmail.com")
                .username("thomasadmin")
                .password("mycoolpassword")
                .name(name)
                .address(address)
                .phone("791237192")
                .build();
    }

    @Test
    public void updateUserTest() {
        POJORequestAddUser user = getTestUser();
        String oldPassword = user.getPassword();
        user.setPassword("newpass12333");

        POJORequestAddUser updatedUser = given().contentType(ContentType.JSON)
                .body(user)
                .pathParam("userId", user.getId())
                .put("/users/{userId}")
                .then()
                .extract().as(POJORequestAddUser.class);

        Assertions.assertNotEquals(updatedUser.getPassword(), oldPassword);
    }

    @Test
    public void deleteUserTest() { //модифицировать нечего
        given().delete("https://fakestoreapi.in/api/users/7")
                .then()
                .log().all()
                .statusCode(200);

    }

    //405 Method Not Allowed
    @Test
    public void authUserTest() {
        AuthData authData = new AuthData("johnd", "m38rmF$");

        String token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        Assertions.assertNotNull(token);
    }
}
