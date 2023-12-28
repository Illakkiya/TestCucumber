
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.openqa.selenium.WebDriver;
import webdriver.DriverManager;

public class Hooks {

    private WebDriver driver = DriverManager.getDriver();

    @Before
    public void setUp() {
        // Any setup code before the scenario runs
    }

    @After
    public void tearDown() {
        // Any teardown code after the scenario runs
        DriverManager.quitDriver();
    }
}

