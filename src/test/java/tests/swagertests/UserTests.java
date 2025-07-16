package tests.swagertests;

import assertions.AssertableResponse;
import assertions.Conditions;
import assertions.GenericAssertableResponse;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import listener.CustomTpl;
import models.swager.FullUser;
import models.swager.Info;
import models.swager.JwtAuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static assertions.Conditions.hasMessage;
import static assertions.Conditions.hasStatusCode;
import static io.restassured.RestAssured.given;

public class UserTests {
    private static Random random;

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "http://85.192.34.140:8080/";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter(),
                CustomTpl.customLogFilter().withCustomTemplates());
        random = new Random();
    }

    @Test
    public void positiveRegisterTest() {
        int randomNumber = Math.abs(random.nextInt());
        FullUser user = FullUser.builder()
                .login("threadSergQAUser" + randomNumber)
                .pass("myCoolPass")
                .build();

        Info info = given().contentType(ContentType.JSON)
                .body(user)
                .post("/api/signup")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("User created", info.getMessage());
    }

    @Test
    public void negativeRegisterLoginExistTest() {
        int randomNumber = Math.abs(random.nextInt());
        FullUser user = FullUser.builder()
                .login("threadSergQAUser" + randomNumber)
                .pass("myCoolPass")
                .build();

        Info info = given().contentType(ContentType.JSON)
                .body(user)
                .post("/api/signup")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("User created", info.getMessage());

        Info errorInfo = given().contentType(ContentType.JSON)
                .body(user)
                .post("/api/signup")
                .then()
                .statusCode(400)
                .extract()
                .jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("Login already exist", errorInfo.getMessage());
    }

    @Test
    public void negativeRegisterNoPasswordTest() {
        int randomNumber = Math.abs(random.nextInt());
        FullUser user = FullUser.builder()
                .login("threadSergQAUser" + randomNumber)
                .build();

        Info info = given().contentType(ContentType.JSON)
                .body(user)
                .post("/api/signup")
                .then()
                .statusCode(400)
                .extract()
                .jsonPath().getObject("info", Info.class);

        //обертка над запросом
        new AssertableResponse(given().contentType(ContentType.JSON)
                .body(user)
                .post("/api/signup")
                .then()).should(hasMessage("Missing login or password"))
                .should(hasStatusCode(400))
                .as("info", Info.class);

        //обертка над запросом с указанием класса (в какой тип данных мы хотим это извлечь)
        new GenericAssertableResponse<Info>(given().contentType(ContentType.JSON)
                .body(user)
                .post("/api/signup")
                .then(), new TypeRef<Info>() {})
                .should(hasMessage("Missing login or password"))
                .should(hasStatusCode(400));
                //.asObject().getMessage();

        Assertions.assertEquals("Missing login or password", info.getMessage());
    }

    //Авторизация под админом и получение токена
    @Test
    public void positiveAdminAuthTest() {
        JwtAuthData authData = new JwtAuthData("admin", "admin");

        String token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getString("token");

        Assertions.assertNotNull(token);
    }

    //создание нового пользователя: POST "/api/signup"
    //и получение токена: POST "/api/login"
    @Test
    public void positiveNewUserAuthTest() {
        int randomNumber = Math.abs(random.nextInt());
        FullUser user = FullUser.builder()
                .login("threadSergQAUser" + randomNumber)
                .pass("myCoolPass")
                .build();

        Info info = given().contentType(ContentType.JSON)
                .body(user)
                .post("/api/signup")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("User created", info.getMessage());

        JwtAuthData authData = new JwtAuthData(user.getLogin(), user.getPass());

        String token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getString("token");

        Assertions.assertNotNull(token);
    }

    //логина и пароля не существует
    @Test
    public void negativeAuthTest() {
        JwtAuthData authData = new JwtAuthData("asdasdas2123", "password_123dsd23");

        given().contentType(ContentType.JSON)
                .body(authData)
                .post("/api/login")
                .then()
                .statusCode(401);
    }

    //Получение информации о пользователе: GET "/api/user"
    @Test
    public void positiveGetUserInfoTest() {
        JwtAuthData authData = new JwtAuthData("admin", "admin");

        String token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getString("token");

        Assertions.assertNotNull(token);

        //Вариант 1 - авторизация с хедером
        given().header("Authorization", "Bearer " + token)
                .get("/api/user")
                .then().statusCode(200);

        //Вариант 2 - гораздо красивее
        given().auth().oauth2(token)
                .get("/api/user")
                .then().statusCode(200);
    }

    //Некорректный токен
    @Test
    public void positiveGetUserInfoIncorrectJwtTest() {
        given().auth().oauth2("some values ")
                .get("/api/user")
                .then().statusCode(401);
    }

    //Получение информации без токена
    @Test
    public void negativeGetUserInfoWithoutJwtTest() {
        given()
                .get("/api/user")
                .then().statusCode(401);
    }

    //Обновление пароля у пользователя: PUT '/api/user'
    @Test
    public void positiveChangeUserPasswordTest() {

        int randomNumber = Math.abs(random.nextInt());
        FullUser user = FullUser.builder()
                .login("threadSergQAUser" + randomNumber)
                .pass("myCoolPass")
                .build();

        Info info = given().contentType(ContentType.JSON)
                .body(user)
                .post("/api/signup")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("User created", info.getMessage());

        JwtAuthData authData = new JwtAuthData(user.getLogin(), user.getPass());

        String token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getString("token");

        Map<String, String> password = new HashMap<>();
        String updatedPassValue = "newPassUpdated";
        password.put("password", updatedPassValue);

        Info updatedPassInfo = given().contentType(ContentType.JSON)
                .auth().oauth2(token)
                .body(password)
                .put("/api/user")
                .then().extract()
                .jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("User password successfully changed", updatedPassInfo.getMessage());

        authData.setPassword(updatedPassValue);

        token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getString("token");

        FullUser updatedUser = given().auth().oauth2(token)
                .get("/api/user")
                .then().statusCode(200)
                .extract().as(FullUser.class);

        Assertions.assertNotEquals(user.getPass(), updatedUser);
    }

    //Попытка поменять админу пароль - негативный тест
    @Test
    public void negativeChangeAdminPasswordTest() {
        JwtAuthData authData = new JwtAuthData("admin", "admin");

        String token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getString("token");

        Map<String, String> password = new HashMap<>();
        String updatedPassValue = "newPassUpdated";
        password.put("password", updatedPassValue);

        Info updatedPassInfo = given().contentType(ContentType.JSON)
                .auth().oauth2(token)
                .body(password)
                .put("/api/user")
                .then().statusCode(400).extract()
                .jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("Cant update base users", updatedPassInfo.getMessage());
    }

    //попытка удалить пользователя (админа)
    @Test
    public void negativeDeleteAdminTest() {
        JwtAuthData authData = new JwtAuthData("admin", "admin");

        String token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getString("token");

        Info info = given().auth().oauth2(token)
                .delete("/api/user")
                .then().statusCode(400).extract()
                .jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("Cant delete base users", info.getMessage());
    }

    //Удаление нового, созданного пользователя: DELETE "/api/user"
    @Test
    public void positiveDeleteNewUserTest() {
        int randomNumber = Math.abs(random.nextInt());
        FullUser user = FullUser.builder()
                .login("threadSergQAUser" + randomNumber)
                .pass("myCoolPass")
                .build();

        Info info = given().contentType(ContentType.JSON)
                .body(user)
                .post("/api/signup")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("User created", info.getMessage());

        JwtAuthData authData = new JwtAuthData(user.getLogin(), user.getPass());

        String token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getString("token");

        Info infoDelete = given().auth().oauth2(token)
                .delete("/api/user")
                .then().statusCode(200)
                .extract()
                .jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("User successfully deleted", infoDelete.getMessage());
    }

    //Получение информации о всех пользователях: GET "/api/users"
    @Test
    public void positiveGetAllUsersTest() {
        List<String> users = given().get("/api/users")
                .then().extract().as(new TypeRef<List<String>>() {});
        Assertions.assertTrue(users.size() >= 3);
    }
}