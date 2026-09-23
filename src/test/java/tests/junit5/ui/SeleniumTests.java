package tests.junit5.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class SeleniumTests {

    private WebDriver driver;

    //сохраняем скачанный файл во внутренню папку "build" в проекте
    private String downloadFolder = System.getProperty("user.dir") + File.separator + "build" + File.separator + "downloadFiles";


    @BeforeAll
    public static void downloadDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setUp() { //настройки, инициализация драйвера
        ChromeOptions options = new ChromeOptions();

        Map<String, String> prefs = new HashMap<>();
        //prefs.put("download.default_directory", "/path/to/download"); // укажите нужный путь - ДО
        prefs.put("download.default_directory", downloadFolder); // укажите нужный путь - ПОСЛЕ
        options.setExperimentalOption("prefs", prefs);

        System.setProperty("webdriver.chrome.driver", "src/test/resources/chromedriver.exe");
        driver = new ChromeDriver(options);
        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
    }

    @Test
    public void simpleUITest() {
        String expectedTitle = "Oleg Pendrak | ThreadQA";

        driver.get("https://threadqa.ru");
        String actualTitle = driver.getTitle();

        Assertions.assertEquals(expectedTitle, actualTitle);
    }

    @Test
    public void simpleForTest() {
        //1. Тестовые данные в самом верху
        String expectedName = "Tomas Anderson";
        String expectedEmail = "tomas@matrix.ru";
        String expectedCurrentAddress = "USA Los Angeles";
        String expectedPermanentAddress = "USA Miami";

        //2. Какие-то операции: заходим, кликаем
        driver.get("http://85.192.34.140:8081");
        WebElement elementsCard = driver.findElement(By.xpath("//div[@class='card-body']//h5[text()='Elements']"));
        elementsCard.click();
        WebElement elementsTextBox = driver.findElement(By.xpath("//span[text()='Test Box']"));
        elementsTextBox.click();

        //3. Ищем все элементы на странице
        WebElement fullName = driver.findElement(By.id("userName"));
        WebElement email = driver.findElement(By.id("userEmail"));
        WebElement currentAddress = driver.findElement(By.id("currentAddress"));
        WebElement permanentAddress = driver.findElement(By.id("permanentAddress"));
        WebElement submit = driver.findElement(By.id("submit"));

        fullName.sendKeys(expectedName);
        email.sendKeys(expectedEmail);
        currentAddress.sendKeys(expectedCurrentAddress);
        permanentAddress.sendKeys(expectedPermanentAddress);
        submit.click();

        //4. Получаем свежие элементы после взаимодействия
        WebElement nameNew = driver.findElement(By.id("name"));
        WebElement emailNew = driver.findElement(By.id("email"));
        WebElement currentAddressNew = driver.findElement(By.xpath("//div[@id='output']/p[@id='currentAddress']"));
        WebElement permanentAddressNew = driver.findElement(By.xpath("//div[@id='output']/p[@id='permanentAddress']"));

        //5. Матчим, сравниваем ожидаемый и реальный результат
        String actualName = nameNew.getText();
        String actualEmail = emailNew.getText();
        String actualCurrentAddress = currentAddressNew.getText();
        String actualPermanentAddress = permanentAddressNew.getText();

        Assertions.assertTrue(actualName.contains(expectedName));
        Assertions.assertTrue(actualEmail.contains(expectedEmail));
        Assertions.assertTrue(actualCurrentAddress.contains(expectedCurrentAddress));
        Assertions.assertTrue(actualPermanentAddress.contains(expectedPermanentAddress));
    }

    @Test
    public void testUploadFile() {
        driver.get("http://85.192.34.140:8081/");

        WebElement elementsCard = driver.findElement(By.xpath("//div[@class='card-body']/h5[text()='Elements']"));
        elementsCard.click();

        WebElement elementsTextBox = driver.findElement(By.xpath("//span[text()='Upload and Download']"));
        elementsTextBox.click();

        WebElement uploadBtn = driver.findElement(By.id("uploadFile"));
        uploadBtn.sendKeys(System.getProperty("user.dir") + "/src/test/resources/threadqa.jpeg");

        WebElement uploadedFakePath = driver.findElement(By.id("uploadedFilePath"));
        Assertions.assertTrue(uploadedFakePath.getText().contains("котэ.jpeg"));
    }

    @Test
    public void testDownload() {
        driver.get("http://85.192.34.140:8081/");

        WebElement elementsCard = driver.findElement(By.xpath("//div[@class='card-body']//h5[text()='Elements']"));
        elementsCard.click();

        WebElement elementsTextBox = driver.findElement(By.xpath("//span[text()='Upload and Download']"));
        elementsTextBox.click();

        WebElement downloadBtn = driver.findElement(By.id("downloadButton"));
        downloadBtn.click();

        //добавим умное ожидание, которое будет ждать скачивание/появление файла в загрузках
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(x -> Paths.get(downloadFolder, "sticker.png").toFile().exists());

        //указываем путь к нашему скачанному файлу
        File file = new File("build/downloadFiles/sticker.png");
        Assertions.assertTrue(file.length() != 0);
        Assertions.assertNotNull(file);
    }


    @AfterEach
    public void tearDown() {
        driver.close();
    }
}