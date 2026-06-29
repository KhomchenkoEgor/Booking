package steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.testng.Assert.*;

public class SearchStep {

    WebDriver driver;

    @Before
    public void setUp() {

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.addArguments("--disable-blink-features=AutomationControlled");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @Given("booking search page is opened")
    public void bookingSearchPageIsOpened() {
        driver.get("https://www.booking.com/searchresults.en-gb.html");
    }

    @When("user searches for {string}")
    public void userSearchesFor(String hotel) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        wait.until(ExpectedConditions.elementToBeClickable(By.name("ss"))).click();

        try {
            By clearBtnLocator = By.xpath("//button[descendant::span[@aria-label='Очистить']]");
            new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.elementToBeClickable(clearBtnLocator)).click();
        } catch (Exception e) {
            wait.until(ExpectedConditions.elementToBeClickable(By.name("ss")))
                    .sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        }

        if (hotel != null && !hotel.isEmpty()) {
            for (char ch : hotel.toCharArray()) {
                wait.until(ExpectedConditions.elementToBeClickable(By.name("ss")))
                        .sendKeys(String.valueOf(ch));
                try { Thread.sleep(150); } catch (InterruptedException ignored) {}
            }
        }

        String partialHotelName = hotel.split(" ")[0];
        By specificOptionLocator = By.xpath(String.format(
                "//ul[contains(@class, 'results')]//li[descendant::*[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '%s')]]" +
                        "| //*[@data-testid='autocomplete-result'][contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '%s')]",
                partialHotelName.toLowerCase(), partialHotelName.toLowerCase()
        ));

        try {
            WebElement correctOption = wait.until(ExpectedConditions.elementToBeClickable(specificOptionLocator));
            correctOption.click();
        } catch (Exception e) {
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            By defaultDropdownLocator = By.cssSelector("[data-testid='autocomplete-result'], [role='option']");
            try {
                wait.until(ExpectedConditions.elementToBeClickable(defaultDropdownLocator)).click();
            } catch (Exception ex) {
                wait.until(ExpectedConditions.elementToBeClickable(By.name("ss"))).sendKeys(Keys.ENTER);
            }
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']"))).click();
    }

    @Then("{string} hotel is shown")
    public void hotelIsShown(String expectedResult) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        String cleanExpected = expectedResult.replaceAll("\\s+", " ").toLowerCase().trim();

        List<WebElement> titles = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("[data-testid='title']")));

        boolean isHotelFound = titles.stream()
                .map(WebElement::getText)
                .map(text -> text.replaceAll("\\s+", " ").toLowerCase())
                .anyMatch(text -> text.contains(cleanExpected));

        assertTrue(isHotelFound, "Hotel '" + expectedResult + "' was not found in the results");
    }

    @And("{string} hotel rating is {string}")
    public void hotelRating(String hotel, String expectedRating) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        String cardXpath = String.format(
                "//div[@data-testid='property-card'][descendant::div[@data-testid='title' and contains(normalize-space(.), '%s')]]" +
                        "//div[@data-testid='review-score']/div[@aria-hidden='true']",
                hotel
        );

        String actualRating = "";
        int attempts = 0;

        while (attempts < 3) {
            try {
                WebElement scoreElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(cardXpath)));
                actualRating = scoreElement.getText().replaceAll("\\s+", "").replace(",", ".");
                break;
            } catch (Exception e) {
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                attempts++;
            }
        }

        String normalizedExpected = expectedRating.replaceAll("\\s+", "").replace(",", ".");

        assertFalse(actualRating.isEmpty(), "Не удалось прочитать рейтинг отеля из-за постоянного обновления страницы");
        assertEquals(normalizedExpected, actualRating,
                String.format("Рейтинг для отеля '%s' не совпадает.", hotel));
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
