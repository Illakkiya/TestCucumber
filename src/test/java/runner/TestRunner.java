package runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
//import manager.GlobalHooks;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;


@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "stepdefinitions",
        plugin = {"com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:", "json:target/cucumber-reports/cucumber-report.json", "pretty"},
        monochrome = true
)
public class TestRunner {

    @BeforeClass
    public static void initialize() {

        // Obtain names through API calls
        getCucumberNamesFromAPI();

        // Run Cucumber tests using io.cucumber.core.cli.Main
        runCucumberTests();

    }

    private static void getCucumberNamesFromAPI() {
        System.out.println("Before setting properties: " + System.getProperty("cucumber.filter.name") + ", " + System.getProperty("cucumber.filter.tags"));

            System.setProperty("cucumber.filter.name", "Search");

            System.out.println("After setting properties: " + System.getProperty("cucumber.filter.name") + ", " + System.getProperty("cucumber.filter.tags"));
        }



    private static void runCucumberTests() {

        String[] argv = {
                "--glue", "stepdefinitions",
                "--plugin", "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:,json:target/cucumber-reports/cucumber-report.json,pretty",
                "--monochrome",
                "src/test/resources/features",
                "--tags", System.getProperty("cucumber.filter.tags"),
                "--name", System.getProperty("cucumber.filter.name") + " and " + System.getProperty("cucumber.filter.tags")
        };

        io.cucumber.core.cli.Main.run(argv, Thread.currentThread().getContextClassLoader());
    }
}