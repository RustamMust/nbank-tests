package iteration2.api;

import assertions.BalanceAssertions;
import generators.RandomData;

import helpers.AccountStepsHelper;
import iteration1.api.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import requests.steps.AccountsSteps;
import specs.RequestSpecs;

public class TransferMoneyNegativeTest extends BaseTest {

    @Test
    public void userCannotTransferNegativeAmountTest() {
        // 1 - Create sender user
        CreateUserRequest senderUser = AdminSteps.createUser();

        // 2 - Create sender account
        var senderSpec = RequestSpecs.authAsUser(senderUser.getUsername(), senderUser.getPassword());
        AccountsSteps.createAccount(senderSpec);

        // 3 - Get sender account id
        int senderAccountId = AccountStepsHelper.getAccountId(senderSpec);

        // 4 - Deposit some money to sender account
        int randomBalance = RandomData.getRandomBalance();
        AccountsSteps.depositMoney(senderSpec, senderAccountId, randomBalance);

        // 5 - Create receiver
        CreateUserRequest receiverUser = AdminSteps.createUser();

        // 6 - Create receiver account
        var receiverSpec = RequestSpecs.authAsUser(receiverUser.getUsername(), receiverUser.getPassword());
        AccountsSteps.createAccount(receiverSpec);

        // 7 - Get receiver account id
        int receiverAccountId = AccountStepsHelper.getAccountId(receiverSpec);

        // 8 - Get balances
        double initialSenderBalance = AccountStepsHelper.getBalance(senderSpec);
        double initialReceiverBalance = AccountStepsHelper.getBalance(receiverSpec);

        // 9 - Attempt to perform transfer with invalid amount
        double transferAmount = -1;
        AccountsSteps.transferMoneyExpectingError(
                senderSpec,
                senderAccountId,
                receiverAccountId,
                transferAmount,
                "Transfer amount must be at least 0.01"
        );

        // 10 - Get balances
        double finalSenderBalance = AccountStepsHelper.getBalance(senderSpec);
        double finalReceiverBalance = AccountStepsHelper.getBalance(receiverSpec);

        // 11 - Assert balances did not change
        BalanceAssertions.assertBalanceUnchanged(softly, initialSenderBalance, finalSenderBalance);
        BalanceAssertions.assertBalanceUnchanged(softly, initialReceiverBalance, finalReceiverBalance);
    }


    @Test
    public void userCannotTransferZeroAmountTest() {
        // 1 - Create sender user
        CreateUserRequest senderUser = AdminSteps.createUser();

        // 2 - Create sender account
        var senderSpec = RequestSpecs.authAsUser(senderUser.getUsername(), senderUser.getPassword());
        AccountsSteps.createAccount(senderSpec);

        // 3 - Get sender account id
        int senderAccountId = AccountStepsHelper.getAccountId(senderSpec);

        // 4 - Deposit money to sender account
        int randomBalance = RandomData.getRandomBalance();
        AccountsSteps.depositMoney(senderSpec, senderAccountId, randomBalance);

        // 5 - Create receiver
        CreateUserRequest receiverUser = AdminSteps.createUser();

        // 6 - Create receiver account
        var receiverSpec = RequestSpecs.authAsUser(receiverUser.getUsername(), receiverUser.getPassword());
        AccountsSteps.createAccount(receiverSpec);

        // 7 - Get receiver account id
        int receiverAccountId = AccountStepsHelper.getAccountId(receiverSpec);

        // 8 - Get balances
        double initialSenderBalance = AccountStepsHelper.getBalance(senderSpec);
        double initialReceiverBalance = AccountStepsHelper.getBalance(receiverSpec);

        // 9 - Attempt to perform transfer with invalid amount
        double transferAmount = 0;
        AccountsSteps.transferMoneyExpectingError(
                senderSpec,
                senderAccountId,
                receiverAccountId,
                transferAmount,
                "Transfer amount must be at least 0.01"
        );

        // 10 - Get balances
        double finalSenderBalance = AccountStepsHelper.getBalance(senderSpec);
        double finalReceiverBalance = AccountStepsHelper.getBalance(receiverSpec);

        // 11 - Assert balances did not change
        BalanceAssertions.assertBalanceUnchanged(softly, initialSenderBalance, finalSenderBalance);
        BalanceAssertions.assertBalanceUnchanged(softly, initialReceiverBalance, finalReceiverBalance);
    }

    @Test
    public void userCannotTransferMoreThanLimitTest() {
        // 1 - Create sender user
        CreateUserRequest senderUser = AdminSteps.createUser();

        // 2 - Create sender account
        var senderSpec = RequestSpecs.authAsUser(senderUser.getUsername(), senderUser.getPassword());
        AccountsSteps.createAccount(senderSpec);

        // 3 - Get sender account id
        int senderAccountId = AccountStepsHelper.getAccountId(senderSpec);

        // 4 - Deposit some money to sender account
        int randomBalance = RandomData.getRandomBalance();
        AccountsSteps.depositMoney(senderSpec, senderAccountId, randomBalance);

        // 5 - Create receiver
        CreateUserRequest receiverUser = AdminSteps.createUser();

        // 6 - Create receiver account
        var receiverSpec = RequestSpecs.authAsUser(receiverUser.getUsername(), receiverUser.getPassword());
        AccountsSteps.createAccount(receiverSpec);

        // 7 - Get receiver account id
        int receiverAccountId = AccountStepsHelper.getAccountId(receiverSpec);

        // 8 - Get balances
        double initialSenderBalance = AccountStepsHelper.getBalance(senderSpec);
        double initialReceiverBalance = AccountStepsHelper.getBalance(receiverSpec);

        // 9 - Attempt to perform transfer exceeding the limit
        double transferAmount = 10000.01;
        AccountsSteps.transferMoneyExpectingError(
                senderSpec,
                senderAccountId,
                receiverAccountId,
                transferAmount,
                "Transfer amount cannot exceed 10000"
        );

        // 10 - Get balances
        double finalSenderBalance = AccountStepsHelper.getBalance(senderSpec);
        double finalReceiverBalance = AccountStepsHelper.getBalance(receiverSpec);

        // 11 - Assert balances did not change
        BalanceAssertions.assertBalanceUnchanged(softly, initialSenderBalance, finalSenderBalance);
        BalanceAssertions.assertBalanceUnchanged(softly, initialReceiverBalance, finalReceiverBalance);
    }
}
