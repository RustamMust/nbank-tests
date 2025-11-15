package iteration2.ui;

import api.assertions.BalanceAssertions;
import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.requests.steps.AccountsSteps;
import api.specs.RequestSpecs;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoneyTest extends BaseUiTest {
    @Test
    @UserSession
    public void userCanDepositMoneyTest() {
        var userSteps = SessionStorage.getSteps();
        AccountsSteps.createAccount(RequestSpecs.authAsUser(
                SessionStorage.getUser().getUsername(),
                SessionStorage.getUser().getPassword()
        ));

        List<CreateAccountResponse> accounts = userSteps.getAllAccounts();
        assertThat(accounts).hasSize(1);

        CreateAccountResponse account = accounts.getFirst();
        double initialBalance = account.getBalance();

        int depositAmount = RandomData.getRandomBalance();

        new UserDashboard()
                .open()
                .depositMoney()
                .chooseAccount(1)
                .enterAmount(depositAmount)
                .submitDeposit()
                .checkAlertMessageAndAccept(BankAlert.SUCCESSFULLY_DEPOSITED.getMessage());

        new UserDashboard().checkUserDashboardVisible();

        double finalBalance = userSteps.getAllAccounts().getFirst().getBalance();

        BalanceAssertions.assertBalanceIncreasedBy(softly, initialBalance, finalBalance, depositAmount);
    }

    @Test
    @UserSession
    public void userCannotDepositInvalidAmountMoneyTest() {
        var userSteps = SessionStorage.getSteps();
        AccountsSteps.createAccount(RequestSpecs.authAsUser(
                SessionStorage.getUser().getUsername(),
                SessionStorage.getUser().getPassword()
        ));

        List<CreateAccountResponse> accounts = userSteps.getAllAccounts();
        assertThat(accounts).hasSize(1);

        CreateAccountResponse account = accounts.getFirst();
        double initialBalance = account.getBalance();

        int depositAmount = 0;

        new UserDashboard()
                .open()
                .depositMoney()
                .chooseAccount(1)
                .enterAmount(depositAmount)
                .submitInvalidDeposit()
                .checkAlertMessageAndAccept(BankAlert.INVALID_AMOUNT.getMessage())
                .checkDepositPageVisible();

        double finalBalance = SessionStorage.getSteps().getAllAccounts().getFirst().getBalance();

        BalanceAssertions.assertBalanceUnchanged(softly, initialBalance, finalBalance);
    }
}
