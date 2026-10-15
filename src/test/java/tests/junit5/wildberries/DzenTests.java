package tests.junit5.wildberries;

import org.junit.jupiter.api.Test;
import tests.junit5.wildberries.pages.MainPage;

public class DzenTests extends BaseTest {

    @Test
    public void searchResultTest() {
        String expectedItem = "iPhone";
        MainPage mainPage = new MainPage(driver);
        mainPage.searchItem(expectedItem);

        String currentUrl = driver.getCurrentUrl();
        System.out.println("🌐 Текущий URL: " + currentUrl);

        System.out.println(driver.getTitle());
    }
}
