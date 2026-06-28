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
    public void userSearchesFor(String hotel) throws InterruptedException {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable(By.name("ss"))).click();
        sleep(500);

        try {
            By clearBtnLocator = By.xpath("//button[descendant::span[@aria-label='Очистить']]");
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
            shortWait.until(ExpectedConditions.elementToBeClickable(clearBtnLocator)).click();
            sleep(500);
        } catch (Exception e) {
            wait.until(ExpectedConditions.elementToBeClickable(By.name("ss"))).sendKeys(Keys.CONTROL + "a");
            wait.until(ExpectedConditions.elementToBeClickable(By.name("ss"))).sendKeys(Keys.BACK_SPACE);
            sleep(300);
        }

        for (char ch : hotel.toCharArray()) {
            wait.until(ExpectedConditions.elementToBeClickable(By.name("ss"))).sendKeys(String.valueOf(ch));
            sleep(150);
        }
        sleep(800);

        By dropdownItemsLocator = By.cssSelector("[data-testid='autocomplete-result'], [role='option']");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(dropdownItemsLocator));
            List<WebElement> options = driver.findElements(dropdownItemsLocator);
            if (!options.isEmpty()) {
                options.get(0).click();
                sleep(500);
            }
        } catch (Exception e) {
            wait.until(ExpectedConditions.elementToBeClickable(By.name("ss"))).sendKeys(Keys.ENTER);
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']"))).click();
    }

    @Then("{string} hotel is shown")
    public void hotelIsShown(String expectedResult) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='title']")));

        List<WebElement> titles = driver.findElements(By.cssSelector("[data-testid='title']"));
        boolean isHotelFound = false;
        for (WebElement title : titles) {
            if (title.getText().replaceAll("\\s+", " ").toLowerCase().contains(expectedResult.toLowerCase().trim())) {
                isHotelFound = true;
                break;
            }
        }
        assertTrue(isHotelFound, "Hotel '" + expectedResult + "' was not found");
    }

    @And("{string} hotel rating is {string}")
    public void hotelRating(String hotel, String expectedRating) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        String cardXpath = String.format(
                "//div[@data-testid='property-card'][descendant::div[@data-testid='title' and contains(text(), '%s')]]" +
                        "//div[@data-testid='review-score']/div[@aria-hidden='true']",
                hotel
        );

        WebElement scoreElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(cardXpath)));
        String actualRating = scoreElement.getText().trim().replace(",", ".");
        String normalizedExpected = expectedRating.trim().replace(",", ".");

        assertEquals(actualRating, normalizedExpected, "Hotel rating does not match");
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
