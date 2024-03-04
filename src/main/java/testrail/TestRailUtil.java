package testrail;

import io.restassured.path.json.JsonPath;
import manager.GlobalHooks;
import org.apache.http.HttpStatus;
import utils.FileUtil;
import utils.StringUtil;
import utils.argosutil.ArgosRunUtil;
import utils.argosutil.functionalutils.order.Order;
import utils.argosutil.functionalutils.scenario.ScenarioDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

/**
 * To post to testrail, set the following VMOptions:
 * testrail.id = "YOUR_PLAN_ID"
 * ignore.passed = true/false
 */
//@formatter:off
public class TestRailUtil implements TestRailVars {

    public static HashMap<String, String> getTests(String id) throws IOException {
        try {
            String apiURL = TESTRAIL_API_URL + "get_plan/" + id;
            JsonPath jsonResponse = given()
                        .header("Content-Type", "application/json")
                        .auth().preemptive().basic(TESTRAIL_USERNAME, TESTRAIL_PASSWORD)
                        .get(apiURL)
                        .then().statusCode(HttpStatus.SC_OK).log().ifError()
                        .extract().jsonPath();
            return getTestsFromRuns(jsonResponse.get("entries.runs.id"));
        } catch(AssertionError notPlan) {
            try {
                String apiURL = TESTRAIL_API_URL + "get_run/" + id;
                JsonPath jsonResponse = given()
                        .header("Content-Type", "application/json")
                        .auth().preemptive().basic(TESTRAIL_USERNAME, TESTRAIL_PASSWORD)
                        .get(apiURL)
                        .then().statusCode(HttpStatus.SC_OK).log().ifError()
                        .extract().jsonPath();
                return getTestsFromRun(jsonResponse.getInt("id"));
            } catch (AssertionError notRun) {
                throw new RuntimeException("Could not resolve Testrail plan/run id, please double check the entered parameter");
            }
        }
    }

    private static HashMap<String, String>
    getTestsFromRun(Integer runID) throws IOException {
        boolean iteratedTillLastTestSet = false;
        ArrayList<HashMap> tests= new ArrayList<>();
        int offset = 0; // skips offset count of tests in the response
        String apiURL;

        // getTests API get the first 250 tests of a testRun/testPlan, to get all the tests need to include offset parameter
        while (!iteratedTillLastTestSet){
            apiURL = TESTRAIL_API_URL + "get_tests/" + runID+"&offset="+ offset;
            ArrayList<HashMap> tempList = given()
                    .header("Content-Type", "application/json")
                    .auth().preemptive().basic(TESTRAIL_USERNAME, TESTRAIL_PASSWORD)
                    .get(apiURL)
                    .then().statusCode(HttpStatus.SC_OK)
                    .extract().jsonPath().get("tests");
            if(!tempList.isEmpty()){
                tests.addAll(tempList);
                offset = offset + 250;
            } else{
                iteratedTillLastTestSet = true;
            }
        }

        HashMap<String, String> testMap = new HashMap<>();
        //Build map of id's and names that we need to run, ignore already passed or blocked tests if applicable
        for(HashMap test : tests) {
            if(TESTRAIL_IGNORE_PASSED) {
                if(!(test.get("status_id").toString().equals("1") || test.get("status_id").toString().equals("2"))) {
                    testMap.put(test.get("id").toString(), test.get("title").toString());
                }
            } else {
                testMap.put(test.get("id").toString(), test.get("title").toString());
            }
        }
        return FileUtil.filterTestMap(testMap);
    }

    private static HashMap<String, String> getTestsFromRuns(List<List<Integer>> runIDs) throws IOException {
        List<Integer> runs = runIDs.stream().flatMap(List::stream).collect(Collectors.toList());
        HashMap<String, String> testMap = new HashMap<>();
        for(Integer runID : runs) {
            testMap.putAll(getTestsFromRun(runID));
        }
        return testMap;
    }

    public static void publishResults() {
        for(ScenarioDetails scenario : GlobalHooks.scenarios) {
            if(!ArgosRunUtil.getHDCCEnabled()){
                addResult(scenario.getTestrailID(), scenario.getStatus() ? 1 : 4, scenario.getOrders());
            } else{
                int statusId = 4; // retest status
                if(StringUtil.containsAnyOf(scenario.getName(), "CHA_2ManFleet_OM_001_Add_Notes","CHA_2manFleet_OM_002_SLMQ_Cancellation", "CHA_2manFleet_OM_003_MLMQ_CancelLine_MasterCard", "CHA_2ManFleet_OM_004_Rebook_FutureSlot", "CHA_2manFleet_OM_005_MLMQ_CancelOrder", "CHA_2ManFleet_OM_007_Remove_Delivery_Charge", "CHA_2ManFleet_OM_008_Change_Delivery_Postcode", "CHA_2ManFleet_OM_009_Reduce_Qty", "CHA_FTD_OM_001_MLMQ_CancelLine_MasterCard", "CHA_FTD_OM_001_MLMQ_CancelLine_MasterCard","CHA_FTD_OM_002_MLMQ_CancelOrder","CHA_FTD_OM_003_MLMQ_Rebook_FutureSlot", "CHA_CF_OM_001_Cancel_Line", "CHA_CF_OM_002_MLMQ_CancelOrder_VisaCredit", "CHA_CF_OM_003_SLSQ_Rebook_Future_Slot", "CHA_CF_OM_004_Exchange_Like_for_Like", "CHA_CF_OM_005_MLMQ_CancelLine_ArgosCard")
                        && scenario.getStatus()){
                    statusId = 6; // in progress status
                } else if(scenario.getStatus()){
                    statusId = 1; // passed status
                }
                addResult(scenario.getTestrailID(), statusId, scenario.getOrders());
            }
        }
    }

    private static void addResult(String testID, int scenarioStatus, List<Order> orderNumbers) {
        String apiURL = TESTRAIL_API_URL + "add_result/" + testID;
        String requestBody;
        if (orderNumbers.isEmpty()){
            requestBody = "{\"status_id\": " + scenarioStatus + "}";
        } else {
            String orders = "";
            for(Order order: orderNumbers){
                orders = orders + order.getOrderNumber() + ", ";
            }
            requestBody =  "{\"status_id\": " + scenarioStatus + ",\"comment\":\"Order number(s): "+ orders +"\"}";
        }
        given()
                .header("Content-Type", "application/json")
                .auth().preemptive().basic(TESTRAIL_USERNAME, TESTRAIL_PASSWORD)
                .body(requestBody)
                .post(apiURL)
                .then().statusCode(HttpStatus.SC_OK).log().ifValidationFails();
    }
}
