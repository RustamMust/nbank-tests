package iteration2.ui;

import api.assertions.BalanceAssertions;
import api.generators.RandomData;
import api.helpers.AccountStepsHelper;
import api.requests.steps.AccountsSteps;
import api.specs.RequestSpecs;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

public class TransferMoneyTest extends BaseUiTest {
    @Test
    @UserSession(value = 2, auth = 1)
    public void userCanTransferMoneyTest() {
        var senderUser = SessionStorage.getUser(1);
        var receiverUser = SessionStorage.getUser(2);

        var senderSpec = RequestSpecs.authAsUser(senderUser.getUsername(), senderUser.getPassword());
        var receiverSpec = RequestSpecs.authAsUser(receiverUser.getUsername(), receiverUser.getPassword());

        AccountsSteps.createAccount(senderSpec);
        int senderAccountId = AccountStepsHelper.getAccountId(senderSpec);

        int initialDeposit = RandomData.getRandomBalance();
        AccountsSteps.depositMoney(senderSpec, senderAccountId, initialDeposit);

        AccountsSteps.createAccount(receiverSpec);
        String receiverAccountNumber = AccountStepsHelper.getAccountName(receiverSpec);

        double senderBalanceBefore = AccountStepsHelper.getBalance(senderSpec);
        double receiverBalanceBefore = AccountStepsHelper.getBalance(receiverSpec);

        int transferAmount = 1;

        new UserDashboard()
                .open()
                .makeTransfer()
                .chooseAccount(1)
                .enterRecipientName(receiverUser.getUsername())
                .enterRecipientAccountNumber(receiverAccountNumber)
                .enterAmount(transferAmount)
                .confirmDetails()
                .sendTransfer()
                .checkAlertMessageAndAccept(BankAlert.SUCCESSFULLY_TRANSFERRED.getMessage())
                .checkTransferPageVisible();

        double senderBalanceAfter = AccountStepsHelper.getBalance(senderSpec);
        double receiverBalanceAfter = AccountStepsHelper.getBalance(receiverSpec);

        BalanceAssertions.assertBalanceDecreasedBy(softly, senderBalanceBefore, senderBalanceAfter, transferAmount);
        BalanceAssertions.assertBalanceIncreasedBy(softly, receiverBalanceBefore, receiverBalanceAfter, transferAmount);
    }

    @Test
    @UserSession(value = 2, auth = 1)
    public void userCannotTransferMoneyTest() {
        var senderUser = SessionStorage.getUser(1);
        var receiverUser = SessionStorage.getUser(2);

        var senderSpec = RequestSpecs.authAsUser(senderUser.getUsername(), senderUser.getPassword());
        var receiverSpec = RequestSpecs.authAsUser(receiverUser.getUsername(), receiverUser.getPassword());

        AccountsSteps.createAccount(senderSpec);
        int senderAccountId = AccountStepsHelper.getAccountId(senderSpec);

        int initialDeposit = RandomData.getRandomBalance();
        AccountsSteps.depositMoney(senderSpec, senderAccountId, initialDeposit);

        AccountsSteps.createAccount(receiverSpec);
        String receiverAccountNumber = AccountStepsHelper.getAccountName(receiverSpec);

        double senderBalanceBefore = AccountStepsHelper.getBalance(senderSpec);
        double receiverBalanceBefore = AccountStepsHelper.getBalance(receiverSpec);

        int transferAmount = 20000;

        new UserDashboard()
                .open()
                .makeTransfer()
                .chooseAccount(1)
                .enterRecipientName(receiverUser.getUsername())
                .enterRecipientAccountNumber(receiverAccountNumber)
                .enterAmount(transferAmount)
                .confirmDetails()
                .sendTransfer()
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_CANNOT_EXCEED_LIMIT.getMessage())
                .checkTransferPageVisible();

        double senderBalanceAfter = AccountStepsHelper.getBalance(senderSpec);
        double receiverBalanceAfter = AccountStepsHelper.getBalance(receiverSpec);

        BalanceAssertions.assertBalanceUnchanged(softly, senderBalanceBefore, senderBalanceAfter);
        BalanceAssertions.assertBalanceUnchanged(softly, receiverBalanceBefore, receiverBalanceAfter);
    }
}
