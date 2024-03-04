package testrail;

public interface TestRailVars {

    String TESTRAIL_API_URL = "https://argosapps.testrail.net/index.php?/api/v2/";
    String TESTRAIL_USERNAME = "seleniumuser102@argosautomation.com";
    String TESTRAIL_PASSWORD = "Pass1234";
    String TESTRAIL_ID = System.getProperty("testrail.id", "0");
    Boolean TESTRAIL_IGNORE_PASSED = !System.getProperty("ignore.passed", "true").equalsIgnoreCase("false");
}
