package assertions.conditions;

import assertions.Condition;
import io.restassured.response.ValidatableResponse;
import lombok.RequiredArgsConstructor;
import models.swager.Info;
import org.junit.jupiter.api.Assertions;

import static org.hamcrest.Matchers.equalTo;

@RequiredArgsConstructor
public class MessageCondition implements Condition {

    private final String expectedMessage;

    @Override
    public void check(ValidatableResponse response) {
        //Вариант №1 реализации
        Info info = response.extract().jsonPath().getObject("info", Info.class);
        Assertions.assertEquals(expectedMessage, info.getMessage());

        //Вариант №2
        //response.body("info.message", equalTo(expectedMessage));
    }
}
