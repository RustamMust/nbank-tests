package iteration2.ui;

import api.assertions.BalanceAssertions;
import api.generators.RandomData;
import api.helpers.AccountStepsHelper;
import api.models.CreateUserRequest;
import api.models.LoginUserRequest;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.steps.AccountsSteps;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import com.codeborne.selenide.*;
import io.restassured.specification.RequestSpecification;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;

import java.util.Map;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoneyTest {
    protected SoftAssertions softly;

    @BeforeAll
    public static void setupSelenoid() {
        //Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://localhost:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }

    @BeforeEach
    public void setupSoftAssertions() {
        softly = new SoftAssertions();
    }

    @Test
    public void userCanDepositMoneyTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // 1 - Create a new user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Create an account
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        AccountsSteps.createAccount(requestSpec);

        // 3 - Get account balance from profile
        double initialBalance = AccountStepsHelper.getBalance(requestSpec);

        // 4 - Get user auth header
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(userRequest.getUsername()).password(userRequest.getPassword()).build())
                .extract()
                .header("Authorization");

        // 5 - Open page
        Selenide.open("/");

        // 6 - Set authToken to localStorage
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        // 7 - Open dashboard page
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // 8 - Click on "Deposit Money" button
        $(byText("\uD83D\uDCB0 Deposit Money")).click();

        // 9 - Click on "Choose an account" dropdown
        SelenideElement accountSelect = $x("//option[normalize-space(text())='-- Choose an account --']/ancestor::select");

        // 10 - Select the first account
        accountSelect.selectOption(1);

        // 11 - Find "Enter amount" input
        SelenideElement amountInput = $("[placeholder='Enter amount']");

        // 12 - Clear "Enter amount" input
        amountInput.shouldBe(visible).clear();

        // 13 - Set value to "Enter amount" input
        int randomBalance = RandomData.getRandomBalance();
        amountInput.setValue(String.valueOf(randomBalance));

        // 14 - Click on "Deposit" button
        $(byText("💵 Deposit")).click();

        // 15 - Assert that Successfully deposited from UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("✅ Successfully deposited");
        alert.accept();

        // 16 - Assert that after clicking OK, the user is redirected to the "User Dashboard" page
        $(Selectors.byText("User Dashboard")).shouldBe(Condition.visible);

        // 17 - Get balance after deposit
        double finalBalance = AccountStepsHelper.getBalance(requestSpec);

        // 18 - Assert balance from profile after deposit via API
        BalanceAssertions.assertBalanceIncreasedBy(softly, initialBalance, finalBalance, randomBalance);
    }

    @Test
    public void userCannotDepositInvalidAmountMoneyTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // 1 - Create a new user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Create an account
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        AccountsSteps.createAccount(requestSpec);

        // 3 - Get account balance from profile
        double initialBalance = AccountStepsHelper.getBalance(requestSpec);

        // 4 - Get user auth header
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(userRequest.getUsername()).password(userRequest.getPassword()).build())
                .extract()
                .header("Authorization");

        // 5 - Open page
        Selenide.open("/");

        // 6 - Set authToken to localStorage
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        // 7 - Open dashboard page
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // 8 - Click on "Deposit Money" button
        $(byText("\uD83D\uDCB0 Deposit Money")).click();

        // 9 - Click on "Choose an account" dropdown
        SelenideElement accountSelect = $x("//option[normalize-space(text())='-- Choose an account --']/ancestor::select");

        // 10 - Select the first account
        accountSelect.selectOption(1);

        // 11 - Find "Enter amount" input
        SelenideElement amountInput = $("[placeholder='Enter amount']");

        // 12 - Clear "Enter amount" input
        amountInput.shouldBe(visible).clear();

        // 13 - Set value to "Enter amount" input
        int randomBalance = 0;
        amountInput.setValue(String.valueOf(randomBalance));

        // 14 - Click on "Deposit" button
        $(byText("💵 Deposit")).click();

        // 15 - Assert that validation message appear from UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Please enter a valid amount.");
        alert.accept();

        // 16 - Assert that after clicking OK, the user does not redirect to the "User Dashboard" page
        $(Selectors.byText("User Dashboard")).shouldNotBe(Condition.visible);

        // 17 - Assert that after clicking OK, the user still on "Deposit Money" page
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);

        // 18 - Get balance after deposit
        double finalBalance = AccountStepsHelper.getBalance(requestSpec);

        // 19 - Assert balance did not change
        BalanceAssertions.assertBalanceUnchanged(softly, initialBalance, finalBalance);
    }
}
