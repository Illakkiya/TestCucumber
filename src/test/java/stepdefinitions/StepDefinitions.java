package stepdefinitions;


import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import webdriver.DriverManager;
import static org.junit.Assert.assertEquals;

public class StepDefinitions {

    private WebDriver driver = DriverManager.getDriver();

    @Given("I navigate to the Google homepage")
    public void iNavigateToGoogleHomepage() {
        driver.get("https://www.google.com");
    }

    @When("I search for {string}")
    public void iSearchFor(String query) {
        driver.findElement(By.name("q")).sendKeys(query);
        driver.findElement(By.name("q")).submit();
    }

    @Then("the search result page title should contain {string}")
    public void theSearchResultPageTitleShouldContain(String expectedTitle) {
        String actualTitle = driver.getTitle();
        assertEquals(expectedTitle, actualTitle);
    }

    @Given("Open browser")
    public void OpenBrowser() {
        driver.get("https://www.google.com");
    }
}

