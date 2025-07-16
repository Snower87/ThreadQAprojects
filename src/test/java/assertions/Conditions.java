package assertions;

import assertions.conditions.MessageCondition;
import assertions.conditions.StatusCodeCondition;

/**
 * Conditions - через статичные методы создаем экземпляры
 */
public class Conditions {
    public static MessageCondition hasMessage(String expectedMessage) {
        return new MessageCondition(expectedMessage);
    }

    public static StatusCodeCondition hasStatusCode(Integer expectedStatus) {
        return new StatusCodeCondition(expectedStatus);
    }
}
