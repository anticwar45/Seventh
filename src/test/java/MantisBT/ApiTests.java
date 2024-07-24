package MantisBT;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class ApiTests {
    private SoftAssertions softAssert;
    private String PHPSESSID;
    private String MANTIS_secure_session;
    private String MANTIS_STRING_COOKIE;
    private Map<String, String> cookies = new HashMap<>();

    @BeforeEach
    public void getCookie() {
        Response responseLogin = RestAssured
                .given()
                .contentType("application/x-www-form-urlencoded")
                .body("return=index.php&username=admin&password=admin20&secure_session=on")
                .post("https://academ-it.ru/mantisbt/login.php")
                .andReturn();

        PHPSESSID = responseLogin.cookie("PHPSESSID");
        MANTIS_secure_session = responseLogin.cookie("MANTIS_secure_session");
        MANTIS_STRING_COOKIE = responseLogin.cookie("MANTIS_STRING_COOKIE");

        cookies.put("PHPSESSID", PHPSESSID);
        cookies.put("MANTIS_secure_session", MANTIS_secure_session);
        cookies.put("MANTIS_STRING_COOKIE", MANTIS_STRING_COOKIE);
    }

    @Test
    public void getViewAllBugPageTest() {

        Response response = RestAssured
                .given()
                .cookies(cookies)
                .get("https://academ-it.ru/mantisbt/view_all_bug_page.php")
                .andReturn();

        softAssert = new SoftAssertions();
        softAssert.assertThat(response.statusCode()).isEqualTo(200);
        softAssert.assertThat(response.asPrettyString()).contains("<title>View Issues - MantisBT</title>");
        softAssert.assertAll();
        response.prettyPrint();
    }

    @Test
    public void updateBugStatusTest() { // failed by application error #1105, need to ask developer
        String bugId = "0035030";//must be actual, check before test
        String bugStatus = "40"; //may be 10, 20, 30, 40, 50, 60, 70, 80
        Response updateResponse = RestAssured
                .given()
                .cookies(cookies)
                .contentType("application/x-www-form-urlencoded")
                .body("bug_id=" + bugId + "&status=" + bugStatus + "&last_updated=1721719145&handler_id=177&bugnote_text=&action_type=change_status")
                .post("https://academ-it.ru/mantisbt/bug_update.php")
                .andReturn();
        updateResponse.prettyPrint();

        softAssert = new SoftAssertions();
        softAssert.assertThat(updateResponse.statusCode()).isEqualTo(302);
        softAssert.assertAll();
    }

    @Test
    public void getAccountPage() {
        Response response = RestAssured
                .given()
                .cookies(cookies)
                .get("https://academ-it.ru/mantisbt/account_page.php")
                .andReturn();

        softAssert = new SoftAssertions();
        softAssert.assertThat(response.statusCode()).isEqualTo(200);
        softAssert.assertThat(response.asPrettyString()).contains("id=\"realname\"");
        softAssert.assertAll();
        response.prettyPrint();
    }

    @Test
    public void updateRealNameInAccountPage() {
        String newRealName = "Doris";
        String newEmail = "Boris@mail.ru";

        Response updateRealNameResponse = RestAssured
                .given()
                .cookies(cookies)
                .contentType("application/x-www-form-urlencoded")
                .body("password_current=&password=&password_confirm=&email=" + newEmail + "&realname=" + newRealName)
                .post("https://academ-it.ru/mantisbt/account_update.php")
                .andReturn();

        softAssert = new SoftAssertions();
        softAssert.assertThat(updateRealNameResponse.statusCode()).isEqualTo(200);
        softAssert.assertThat(updateRealNameResponse.asPrettyString()).contains("Real name successfully updated");

        Response getAccountPageResponse = RestAssured
                .given()
                .cookies(cookies)
                .get("https://academ-it.ru/mantisbt/account_page.php")
                .andReturn();

        softAssert = new SoftAssertions();
        softAssert.assertThat(getAccountPageResponse.statusCode()).isEqualTo(200);
        softAssert.assertThat(getAccountPageResponse.asPrettyString()).contains(newRealName);
        softAssert.assertThat(getAccountPageResponse.asPrettyString()).contains(newEmail);
        softAssert.assertAll();
    }
}