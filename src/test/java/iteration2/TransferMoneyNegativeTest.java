package iteration2;

import generators.RandomData;

import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import requests.steps.AccountsSteps;
import requests.steps.CustomerSteps;
import specs.RequestSpecs;

public class TransferMoneyNegativeTest extends BaseTest {

    @Test
    public void userCannotTransferNegativeAmountTest() {
        // 1 - Create sender user
        CreateUserRequest senderUser = AdminSteps.createUser();

        // 2 - Create sender account
        var senderSpec = RequestSpecs.authAsUser(senderUser.getUsername(), senderUser.getPassword());
        AccountsSteps.createAccount(senderSpec);

        // 3 - Get sender profile before deposit
        GetCustomerProfileResponse senderProfileBeforeDeposit = CustomerSteps.getCustomerProfile(senderSpec);

        int senderAccountId = senderProfileBeforeDeposit.getAccounts().get(0).getId();

        // 4 - Deposit some money to sender account
        int randomBalance = RandomData.getRandomBalance();
        AccountsSteps.depositMoney(senderSpec, senderAccountId, randomBalance);

        // 5 - Create receiver
        CreateUserRequest receiverUser = AdminSteps.createUser();

        // 6 - Create receiver account
        var receiverSpec = RequestSpecs.authAsUser(receiverUser.getUsername(), receiverUser.getPassword());
        AccountsSteps.createAccount(receiverSpec);

        // 7 - Get receiver profile before transfer
        GetCustomerProfileResponse receiverProfileBefore = CustomerSteps.getCustomerProfile(receiverSpec);

        int receiverAccountId = receiverProfileBefore.getAccounts().get(0).getId();

        // 8 - Get updated sender profile before transfer
        GetCustomerProfileResponse senderProfileBefore = CustomerSteps.getCustomerProfile(senderSpec);

        double initialSenderBalance = senderProfileBefore.getAccounts().get(0).getBalance();
        double initialReceiverBalance = receiverProfileBefore.getAccounts().get(0).getBalance();

        // 9 - Attempt to perform transfer with invalid amount
        double transferAmount = -1;
        AccountsSteps.transferMoneyExpectingError(
                senderSpec,
                senderAccountId,
                receiverAccountId,
                transferAmount,
                "Transfer amount must be at least 0.01"
        );

        // 10 - Get profiles after failed transfer
        GetCustomerProfileResponse senderProfileAfter = CustomerSteps.getCustomerProfile(senderSpec);

        GetCustomerProfileResponse receiverProfileAfter = CustomerSteps.getCustomerProfile(receiverSpec);

        double finalSenderBalance = senderProfileAfter.getAccounts().get(0).getBalance();
        double finalReceiverBalance = receiverProfileAfter.getAccounts().get(0).getBalance();

        // 11 - Assert balances did not change
        softly.assertThat(finalSenderBalance)
                .as("Sender balance should not change after failed transfer")
                .isEqualTo(initialSenderBalance);

        softly.assertThat(finalReceiverBalance)
                .as("Receiver balance should not change after failed transfer")
                .isEqualTo(initialReceiverBalance);
    }


    @Test
    public void userCannotTransferZeroAmountTest() {
        // 1 - Create sender user
        CreateUserRequest senderUser = AdminSteps.createUser();

        // 2 - Create sender account
        var senderSpec = RequestSpecs.authAsUser(senderUser.getUsername(), senderUser.getPassword());
        AccountsSteps.createAccount(senderSpec);

        // 3 - Get sender profile before deposit
        GetCustomerProfileResponse senderProfileBeforeDeposit = CustomerSteps.getCustomerProfile(senderSpec);

        int senderAccountId = senderProfileBeforeDeposit.getAccounts().get(0).getId();

        // 4 - Deposit some money to sender account
        int randomBalance = RandomData.getRandomBalance();
        AccountsSteps.depositMoney(senderSpec, senderAccountId, randomBalance);

        // 5 - Create receiver
        CreateUserRequest receiverUser = AdminSteps.createUser();

        // 6 - Create receiver account
        var receiverSpec = RequestSpecs.authAsUser(receiverUser.getUsername(), receiverUser.getPassword());
        AccountsSteps.createAccount(receiverSpec);

        // 7 - Get receiver profile before transfer
        GetCustomerProfileResponse receiverProfileBefore = CustomerSteps.getCustomerProfile(receiverSpec);

        int receiverAccountId = receiverProfileBefore.getAccounts().get(0).getId();

        // 8 - Get updated sender profile before transfer
        GetCustomerProfileResponse senderProfileBefore = CustomerSteps.getCustomerProfile(senderSpec);

        double initialSenderBalance = senderProfileBefore.getAccounts().get(0).getBalance();
        double initialReceiverBalance = receiverProfileBefore.getAccounts().get(0).getBalance();

        // 9 - Attempt to perform transfer with invalid amount
        double transferAmount = 0;
        AccountsSteps.transferMoneyExpectingError(
                senderSpec,
                senderAccountId,
                receiverAccountId,
                transferAmount,
                "Transfer amount must be at least 0.01"
        );

        // 10 - Get profiles after failed transfer
        GetCustomerProfileResponse senderProfileAfter = CustomerSteps.getCustomerProfile(senderSpec);

        GetCustomerProfileResponse receiverProfileAfter = CustomerSteps.getCustomerProfile(receiverSpec);

        double finalSenderBalance = senderProfileAfter.getAccounts().get(0).getBalance();
        double finalReceiverBalance = receiverProfileAfter.getAccounts().get(0).getBalance();

        // 11 - Assert balances did not change
        softly.assertThat(finalSenderBalance)
                .as("Sender balance should not change after failed transfer")
                .isEqualTo(initialSenderBalance);

        softly.assertThat(finalReceiverBalance)
                .as("Receiver balance should not change after failed transfer")
                .isEqualTo(initialReceiverBalance);
    }

    @Test
    public void userCannotTransferMoreThanLimitTest() {
        // 1 - Create sender user
        CreateUserRequest senderUser = AdminSteps.createUser();

        // 2 - Create sender account
        var senderSpec = RequestSpecs.authAsUser(senderUser.getUsername(), senderUser.getPassword());
        AccountsSteps.createAccount(senderSpec);

        // 3 - Get sender profile before deposit
        GetCustomerProfileResponse senderProfileBeforeDeposit = CustomerSteps.getCustomerProfile(senderSpec);

        int senderAccountId = senderProfileBeforeDeposit.getAccounts().get(0).getId();

        // 4 - Deposit some money to sender account
        int randomBalance = RandomData.getRandomBalance();
        AccountsSteps.depositMoney(senderSpec, senderAccountId, randomBalance);

        // 5 - Create receiver
        CreateUserRequest receiverUser = AdminSteps.createUser();

        // 6 - Create receiver account
        var receiverSpec = RequestSpecs.authAsUser(receiverUser.getUsername(), receiverUser.getPassword());
        AccountsSteps.createAccount(receiverSpec);

        // 7 - Get receiver profile before transfer
        GetCustomerProfileResponse receiverProfileBefore = CustomerSteps.getCustomerProfile(receiverSpec);

        int receiverAccountId = receiverProfileBefore.getAccounts().get(0).getId();

        // 8 - Get updated sender profile before transfer
        GetCustomerProfileResponse senderProfileBefore = CustomerSteps.getCustomerProfile(senderSpec);

        double initialSenderBalance = senderProfileBefore.getAccounts().get(0).getBalance();
        double initialReceiverBalance = receiverProfileBefore.getAccounts().get(0).getBalance();

        // 10 - Attempt to perform transfer exceeding the limit
        double transferAmount = 10000.01;
        AccountsSteps.transferMoneyExpectingError(
                senderSpec,
                senderAccountId,
                receiverAccountId,
                transferAmount,
                "Transfer amount cannot exceed 10000"
        );

        // 11 - Get profiles after failed transfer
        GetCustomerProfileResponse senderProfileAfter = CustomerSteps.getCustomerProfile(senderSpec);

        GetCustomerProfileResponse receiverProfileAfter = CustomerSteps.getCustomerProfile(receiverSpec);

        double finalSenderBalance = senderProfileAfter.getAccounts().get(0).getBalance();
        double finalReceiverBalance = receiverProfileAfter.getAccounts().get(0).getBalance();

        // 12 - Assert balances did not change
        softly.assertThat(finalSenderBalance)
                .as("Sender balance should not change after failed transfer exceeding the limit")
                .isEqualTo(initialSenderBalance);

        softly.assertThat(finalReceiverBalance)
                .as("Receiver balance should not change after failed transfer exceeding the limit")
                .isEqualTo(initialReceiverBalance);
    }
}
