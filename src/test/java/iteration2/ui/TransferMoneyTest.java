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
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoneyTest extends BaseUiTest {
    @Test
    public void userCanTransferMoneyTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // 1 - Create sender user
        CreateUserRequest senderUser = AdminSteps.createUser();

        // 2 - Create sender account
        var senderSpec = RequestSpecs.authAsUser(senderUser.getUsername(), senderUser.getPassword());
        AccountsSteps.createAccount(senderSpec);

        // 3 - Get sender account id from profile
        int senderAccountId = AccountStepsHelper.getAccountId(senderSpec);

        // 4 - Deposit to sender account
        int randomBalance = RandomData.getRandomBalance();
        AccountsSteps.depositMoney(senderSpec, senderAccountId, randomBalance);

        // 5 - Create receiver user
        CreateUserRequest receiverUser = AdminSteps.createUser();

        // 6 - Create receiver account
        var receiverSpec = RequestSpecs.authAsUser(receiverUser.getUsername(), receiverUser.getPassword());
        AccountsSteps.createAccount(receiverSpec);

        // 7 - Get receiver account name
        String receiverAccountName = AccountStepsHelper.getAccountName(receiverSpec);

        // 8 - Get sender and receiver balances
        double senderBalanceBefore = AccountStepsHelper.getBalance(senderSpec);
        double receiverBalanceBefore = AccountStepsHelper.getBalance(receiverSpec);

        // Авторизация через API
        authAsUser(senderUser);

        // 12 - Open dashboard page
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // 13 - Click on "Make a Transfer" button
        $(byText("\uD83D\uDD04 Make a Transfer")).click();

        // 14 - Click on "Choose an account" dropdown
        SelenideElement accountSelect = $x("//option[normalize-space(text())='-- Choose an account --']/ancestor::select");

        // 15 - Select the first account
        accountSelect.selectOption(1);

        // 16 - Find "Enter recipient name" input
        SelenideElement enterRecipientInput = $("[placeholder='Enter recipient name']");

        // 17 - Clear "Enter recipient name" input
        enterRecipientInput.shouldBe(visible).clear();

        // 18 - Set value to "Enter recipient name" input
        enterRecipientInput.setValue(receiverUser.getUsername());

        // 19 - Find "Enter recipient account number" input
        SelenideElement enterRecipientAccountNumberInput = $("[placeholder='Enter recipient account number']");

        // 20 - Clear "Enter recipient account number" input
        enterRecipientAccountNumberInput.shouldBe(visible).clear();

        // 21 - Set value to "Enter recipient account number" input
        enterRecipientAccountNumberInput.setValue(receiverAccountName);

        // 22 - Find "Enter amount" input
        SelenideElement amountInput = $("[placeholder='Enter amount']");

        // 23 - Clear "Enter amount" input
        amountInput.shouldBe(visible).clear();

        // 24 - Set value to "Enter amount" input
        int transferAmount = 1;
        amountInput.setValue(String.valueOf(transferAmount));

        // 25 - Click on "Confirm details are correct" checkbox
        SelenideElement confirmCheckbox = $("#confirmCheck");
        confirmCheckbox.shouldBe(visible).click();

        // 26 - Click on "Send Transfer" button
        $(byText("\uD83D\uDE80 Send Transfer")).click();

        // 27 - Assert that Successfully transferred from UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("✅ Successfully transferred");
        alert.accept();

        // 28 - Assert that after clicking OK, the user still on "Make a Transfer" page
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);

        // 29 - Get sender and receiver balances
        double senderBalanceAfter = AccountStepsHelper.getBalance(senderSpec);
        double receiverBalanceAfter = AccountStepsHelper.getBalance(receiverSpec);

        // 30 - Assert balances via API
        BalanceAssertions.assertBalanceDecreasedBy(softly, senderBalanceBefore, senderBalanceAfter, transferAmount);
        BalanceAssertions.assertBalanceIncreasedBy(softly, receiverBalanceBefore, receiverBalanceAfter, transferAmount);
    }

    @Test
    public void userCannotTransferMoneyTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // 1 - Create sender user
        CreateUserRequest senderUser = AdminSteps.createUser();

        // 2 - Create sender account
        var senderSpec = RequestSpecs.authAsUser(senderUser.getUsername(), senderUser.getPassword());
        AccountsSteps.createAccount(senderSpec);

        // 3 - Get sender account id from profile
        int senderAccountId = AccountStepsHelper.getAccountId(senderSpec);

        // 4 - Deposit to sender account
        int randomBalance = RandomData.getRandomBalance();
        AccountsSteps.depositMoney(senderSpec, senderAccountId, randomBalance);

        // 5 - Create receiver user
        CreateUserRequest receiverUser = AdminSteps.createUser();

        // 6 - Create receiver account
        var receiverSpec = RequestSpecs.authAsUser(receiverUser.getUsername(), receiverUser.getPassword());
        AccountsSteps.createAccount(receiverSpec);

        // 7 - Get receiver account name
        String receiverAccountName = AccountStepsHelper.getAccountName(receiverSpec);

        // 8 - Get sender and receiver balances
        double senderBalanceBefore = AccountStepsHelper.getBalance(senderSpec);
        double receiverBalanceBefore = AccountStepsHelper.getBalance(receiverSpec);

        // 9 - Get user auth header
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(senderUser.getUsername()).password(senderUser.getPassword()).build())
                .extract()
                .header("Authorization");

        // 10 - Open page
        Selenide.open("/");

        // 11 - Set authToken to localStorage
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        // 12 - Open dashboard page
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // 13 - Click on "Make a Transfer" button
        $(byText("\uD83D\uDD04 Make a Transfer")).click();

        // 14 - Click on "Choose an account" dropdown
        SelenideElement accountSelect = $x("//option[normalize-space(text())='-- Choose an account --']/ancestor::select");

        // 15 - Select the first account
        accountSelect.selectOption(1);

        // 16 - Find "Enter recipient name" input
        SelenideElement enterRecipientInput = $("[placeholder='Enter recipient name']");

        // 17 - Clear "Enter recipient name" input
        enterRecipientInput.shouldBe(visible).clear();

        // 18 - Set value to "Enter recipient name" input
        enterRecipientInput.setValue(receiverUser.getUsername());

        // 19 - Find "Enter recipient account number" input
        SelenideElement enterRecipientAccountNumberInput = $("[placeholder='Enter recipient account number']");

        // 20 - Clear "Enter recipient account number" input
        enterRecipientAccountNumberInput.shouldBe(visible).clear();

        // 21 - Set value to "Enter recipient account number" input
        enterRecipientAccountNumberInput.setValue(receiverAccountName);

        // 22 - Find "Enter amount" input
        SelenideElement amountInput = $("[placeholder='Enter amount']");

        // 23 - Clear "Enter amount" input
        amountInput.shouldBe(visible).clear();

        // 24 - Set value to "Enter amount" input
        int transferAmount = 20000;
        amountInput.setValue(String.valueOf(transferAmount));

        // 25 - Click on "Confirm details are correct" checkbox
        SelenideElement confirmCheckbox = $("#confirmCheck");
        confirmCheckbox.shouldBe(visible).click();

        // 26 - Click on "Send Transfer" button
        $(byText("\uD83D\uDE80 Send Transfer")).click();

        // 27 - Assert validation message from UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Error: Transfer amount cannot exceed 10000");
        alert.accept();

        // 28 - Assert that after clicking OK, the user still on "Make a Transfer" page
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);

        // 29 - Get sender and receiver balances
        double senderBalanceAfter = AccountStepsHelper.getBalance(senderSpec);
        double receiverBalanceAfter = AccountStepsHelper.getBalance(receiverSpec);

        // 30 - Assert balances did not change via API
        BalanceAssertions.assertBalanceUnchanged(softly, senderBalanceBefore, senderBalanceAfter);
        BalanceAssertions.assertBalanceUnchanged(softly, receiverBalanceBefore, receiverBalanceAfter);
    }
}
