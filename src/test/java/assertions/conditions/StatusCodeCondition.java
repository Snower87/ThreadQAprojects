package assertions.conditions;

import assertions.Condition;
import io.restassured.response.ValidatableResponse;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;

@RequiredArgsConstructor
public class StatusCodeCondition implements Condition {
    private final Integer statusCode;

    @Override
    public void check(ValidatableResponse response) {
        //Вариант №1 реализации
        int actualStatusCode = response.extract().statusCode();
        Assertions.assertEquals(statusCode, actualStatusCode);

        //Вариант №2
        //response.assertThat().statusCode(statusCode);
    }
}
