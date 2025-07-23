package tests.swagertests;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import listener.CustomTpl;
import models.swager.FullUser;
import models.swager.Info;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import services.UserService;

import java.util.List;
import java.util.Random;

import static assertions.Conditions.hasMessage;
import static assertions.Conditions.hasStatusCode;
import static utils.RandomTestData.*;

public class UserNewTests {

    private static UserService userService;

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "http://85.192.34.140:8080/";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter(),
                CustomTpl.customLogFilter().withCustomTemplates());
        userService = new UserService();
    }

    //Создание пользователя
    @Test
    public void positiveRegisterTest() {
        FullUser user = getRandomUser();
        userService.register(user)
                .should(hasStatusCode(201))
                .should(hasMessage("User created"));
    }

    @Test
    public void positiveRegisterWithGamesTest() {
        FullUser user = getRandomUserWithGames();
        Response response = userService.register(user)
                .should(hasStatusCode(201))
                .should(hasMessage("User created"))
                .asResponse();
        Info info = response.jsonPath().getObject("info", Info.class);

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(response.statusCode()).as("Статус код не был 200")
                .isEqualTo(201);
        softAssertions.assertThat(info.getMessage()).as("Сообщение об ошибке было не верное")
                .isEqualTo("User created"); //было "фейк мессядж" и тест падал/выводил сообщение
        //softAssertions.assertAll(); //собирает все ошибки
    }

    //Пользователь уже существует
    @Test
    public void negativeRegisterLoginExistTest() {
        FullUser user = getRandomUser();
        userService.register(user)
                .should(hasStatusCode(201));

        userService.register(user)
                .should(hasStatusCode(400))
                .should(hasMessage("Login already exist"));
    }

    //Пароль не задан
    @Test
    public void negativeRegisterNoPasswordTest() {
        FullUser user = getRandomUser();
        user.setPass(null);

        userService.register(user)
                .should(hasStatusCode(400))
                .should(hasMessage("Missing login or password"));
    }

    //Авторизация под админом и получение токена
    @Test
    public void positiveAdminAuthTest() {
        FullUser user = getAdminUser();
        String token = userService.auth(user)
                .should(hasStatusCode(200))
                .asJwt();

        Assertions.assertNotNull(token);
    }

    //создание нового пользователя: POST "/api/signup"
    //и получение токена: POST "/api/login"
    @Test
    public void positiveNewUserAuthTest() {
        FullUser user = getRandomUser();
        userService.register(user);
        String token = userService.auth(user)
                .should(hasStatusCode(200)).asJwt();

        Assertions.assertNotNull(token);
    }

    //логина и пароля не существует
    @Test
    public void negativeAuthTest() {
        FullUser user = getRandomUser();
        userService.auth(user)
                        .should(hasStatusCode(401));
    }

    //Получение информации о пользователе: GET "/api/user"
    @Test
    public void positiveGetUserInfoTest() {
        FullUser user = getAdminUser();
        String token = userService.auth(user).asJwt();

        userService.getUserInfo(token)
                .should(hasStatusCode(200));
    }

    //Некорректный токен
    @Test
    public void positiveGetUserInfoIncorrectJwtTest() {
        userService.getUserInfo("some jwt")
                .should(hasStatusCode(401));
    }

    //Получение информации без токена
    @Test
    public void negativeGetUserInfoWithoutJwtTest() {
        userService.getUserInfo().should(hasStatusCode(401));
    }

    //Обновление пароля у пользователя: PUT '/api/user'
    @Test
    public void positiveChangeUserPasswordTest() {
        FullUser user = getRandomUser();
        String oldPassword = user.getPass();
        userService.register(user);

        userService.auth(user);

        String token = userService.auth(user).asJwt();

        String updatedPassValue = "newPassUpdated";

        userService.updatePass(updatedPassValue, token)
                .should(hasStatusCode(200))
                .should(hasMessage("User password successfully changed"));

        user.setPass(updatedPassValue);

        token = userService.auth(user).should(hasStatusCode(200)).asJwt();

        FullUser updatedUser = userService.getUserInfo(token).as(FullUser.class);

        Assertions.assertNotEquals(oldPassword, updatedUser.getPass());
    }

    //Попытка поменять админу пароль - негативный тест
    @Test
    public void negativeChangeAdminPasswordTest() {
        FullUser user = getAdminUser();

        String token = userService.auth(user).asJwt();

        String updatedPassValue = "newPassUpdated";
        userService.updatePass(updatedPassValue, token)
                .should(hasStatusCode(400))
                .should(hasMessage("Cant update base users"));
    }

    //попытка удалить пользователя (админа)
    @Test
    public void negativeDeleteAdminTest() {
        FullUser user = getAdminUser();

        String token = userService.auth(user).asJwt();

        userService.deleteUser(token)
                .should(hasStatusCode(400))
                .should(hasMessage("Cant delete base users"));
    }

    //Удаление нового, созданного пользователя: DELETE "/api/user"
    @Test
    public void positiveDeleteNewUserTest() {
        FullUser user = getRandomUser();
        userService.register(user);

        String token = userService.auth(user).asJwt();

        userService.deleteUser(token)
                .should(hasStatusCode(200))
                .should(hasMessage("User successfully deleted"));
    }

    //Получение информации о всех пользователях: GET "/api/users"
    @Test
    public void positiveGetAllUsersTest() {
        List<String> users = userService.getAllUsers().asList(String.class);
        Assertions.assertTrue(users.size() >= 3);
    }
}