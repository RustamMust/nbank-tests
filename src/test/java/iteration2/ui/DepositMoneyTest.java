package iteration2.ui;

import api.assertions.BalanceAssertions;
import api.generators.RandomData;
import api.helpers.AccountStepsHelper;
import api.models.CreateUserRequest;
import api.requests.steps.AccountsSteps;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import io.restassured.specification.RequestSpecification;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

public class DepositMoneyTest extends BaseUiTest {
    @Test
    public void userCanDepositMoneyTest() {
        // 1 - Create a new user via API
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Create an account via API
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        AccountsSteps.createAccount(requestSpec);

        // 3 - Get account balance from profile via API
        double initialBalance = AccountStepsHelper.getBalance(requestSpec);

        // 4 - Set authToken to localStorage via API
        authAsUser(userRequest);

        // 5 - Get random balance
        int randomBalance = RandomData.getRandomBalance();

        // 6 - Open dashboard page
        new UserDashboard()
                .open()
                .depositMoney()
                .chooseAccount(1)
                .enterAmount(randomBalance)
                .submitDeposit()
                .checkAlertMessageAndAccept(BankAlert.SUCCESSFULLY_DEPOSITED.getMessage());

        new UserDashboard().checkUserDashboardVisible();

        // 7 - Get balance after deposit via API
        double finalBalance = AccountStepsHelper.getBalance(requestSpec);

        // 8 - Assert balance from profile after deposit via API
        BalanceAssertions.assertBalanceIncreasedBy(softly, initialBalance, finalBalance, randomBalance);
    }

    @Test
    public void userCannotDepositInvalidAmountMoneyTest() {
        // 1 - Create a new user via API
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Create an account via API
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        AccountsSteps.createAccount(requestSpec);

        // 3 - Get account balance from profile via API
        double initialBalance = AccountStepsHelper.getBalance(requestSpec);

        // 4 - Set authToken to localStorage via API
        authAsUser(userRequest);

        // 5 - Get invalid balance
        int randomBalance = 0;

        // 6 - Open dashboard page
        new UserDashboard()
                .open()
                .depositMoney()
                .chooseAccount(1)
                .enterAmount(randomBalance)
                .submitInvalidDeposit()
                .checkAlertMessageAndAccept(BankAlert.INVALID_AMOUNT.getMessage())
                .checkDepositPageVisible();

        // 7 - Get balance after deposit via API
        double finalBalance = AccountStepsHelper.getBalance(requestSpec);

        // 8 - Assert balance did not change via API
        BalanceAssertions.assertBalanceUnchanged(softly, initialBalance, finalBalance);
    }
}
