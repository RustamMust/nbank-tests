package iteration2.ui;

import api.assertions.BalanceAssertions;
import api.generators.RandomData;
import api.helpers.AccountStepsHelper;
import api.models.CreateUserRequest;
import api.requests.steps.AccountsSteps;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

public class TransferMoneyTest extends BaseUiTest {
    @Test
    public void userCanTransferMoneyTest() {
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

        // 9 - Set authToken to localStorage via API
        authAsUser(senderUser);

        // 10 - Get transfer amount
        int transferAmount = 1;

        // 11 - Open dashboard page
        new UserDashboard()
                .open()
                .makeTransfer()
                .chooseAccount(1)
                .enterRecipientName(receiverUser.getUsername())
                .enterRecipientAccountNumber(receiverAccountName)
                .enterAmount(transferAmount)
                .confirmDetails()
                .sendTransfer()
                .checkAlertMessageAndAccept(BankAlert.SUCCESSFULLY_TRANSFERRED.getMessage())
                .checkTransferPageVisible();

        // 12 - Get sender and receiver balances
        double senderBalanceAfter = AccountStepsHelper.getBalance(senderSpec);
        double receiverBalanceAfter = AccountStepsHelper.getBalance(receiverSpec);

        // 13 - Assert balances via API
        BalanceAssertions.assertBalanceDecreasedBy(softly, senderBalanceBefore, senderBalanceAfter, transferAmount);
        BalanceAssertions.assertBalanceIncreasedBy(softly, receiverBalanceBefore, receiverBalanceAfter, transferAmount);
    }

    @Test
    public void userCannotTransferMoneyTest() {
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

        // 9 - Set authToken to localStorage via API
        authAsUser(senderUser);

        // 10 - Get transfer amount
        int transferAmount = 20000;

        // 11 - Open dashboard page
        new UserDashboard()
                .open()
                .makeTransfer()
                .chooseAccount(1)
                .enterRecipientName(receiverUser.getUsername())
                .enterRecipientAccountNumber(receiverAccountName)
                .enterAmount(transferAmount)
                .confirmDetails()
                .sendTransfer()
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_CANNOT_EXCEED_LIMIT.getMessage())
                .checkTransferPageVisible();

        // 12 - Get sender and receiver balances
        double senderBalanceAfter = AccountStepsHelper.getBalance(senderSpec);
        double receiverBalanceAfter = AccountStepsHelper.getBalance(receiverSpec);

        // 13 - Assert balances did not change via API
        BalanceAssertions.assertBalanceUnchanged(softly, senderBalanceBefore, senderBalanceAfter);
        BalanceAssertions.assertBalanceUnchanged(softly, receiverBalanceBefore, receiverBalanceAfter);
    }
}
