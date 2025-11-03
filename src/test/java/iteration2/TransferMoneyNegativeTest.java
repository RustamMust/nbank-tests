package iteration2;

import generators.RandomData;

import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class TransferMoneyNegativeTest extends BaseTest {

    @Test
    public void userCannotTransferNegativeAmountTest() {
        // 1 - Create sender
        CreateUserRequest senderUser = AdminSteps.createUser();

        // 2 - Create sender account
        var senderSpec = RequestSpecs.authAsUser(senderUser.getUsername(), senderUser.getPassword());
        UserSteps.createAccount(senderSpec);

        // 3 - Get sender profile before deposit
        GetCustomerProfileResponse senderProfileBeforeDeposit = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                senderSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        int senderAccountId = senderProfileBeforeDeposit.getAccounts().get(0).getId();

        // 4 - Deposit some money to sender account
        int randomBalance = RandomData.getRandomBalance();
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(senderAccountId)
                .balance(randomBalance)
                .build();

        new ValidatedCrudRequester<DepositMoneyResponse>(
                senderSpec,
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        // 5 - Create receiver
        CreateUserRequest receiverUser = AdminSteps.createUser();

        // 6 - Create receiver account
        var receiverSpec = RequestSpecs.authAsUser(receiverUser.getUsername(), receiverUser.getPassword());
        UserSteps.createAccount(receiverSpec);

        // 7 - Get receiver profile before transfer
        GetCustomerProfileResponse receiverProfileBefore = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                receiverSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        int receiverAccountId = receiverProfileBefore.getAccounts().get(0).getId();

        // 8 - Get updated sender profile before transfer
        GetCustomerProfileResponse senderProfileBefore = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                senderSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        double initialSenderBalance = senderProfileBefore.getAccounts().get(0).getBalance();
        double initialReceiverBalance = receiverProfileBefore.getAccounts().get(0).getBalance();

        // 9 - Prepare transfer with negative amount
        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(-1)
                .build();

        // 10 - Attempt to perform transfer with invalid amount
        new CrudRequester(
                senderSpec,
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequestPlainText("Transfer amount must be at least 0.01")
        ).post(transferRequest);

        // 11 - Get profiles after failed transfer
        GetCustomerProfileResponse senderProfileAfter = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                senderSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        GetCustomerProfileResponse receiverProfileAfter = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                receiverSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        double finalSenderBalance = senderProfileAfter.getAccounts().get(0).getBalance();
        double finalReceiverBalance = receiverProfileAfter.getAccounts().get(0).getBalance();

        // 12 - Assert balances did not change
        softly.assertThat(finalSenderBalance)
                .as("Sender balance should not change after failed transfer")
                .isEqualTo(initialSenderBalance);

        softly.assertThat(finalReceiverBalance)
                .as("Receiver balance should not change after failed transfer")
                .isEqualTo(initialReceiverBalance);
    }


    @Test
    public void userCannotTransferZeroAmountTest() {
        // 1 - Create sender
        CreateUserRequest senderUser = AdminSteps.createUser();

        // 2 - Create sender account
        var senderSpec = RequestSpecs.authAsUser(senderUser.getUsername(), senderUser.getPassword());
        UserSteps.createAccount(senderSpec);

        // 3 - Get sender profile before deposit
        GetCustomerProfileResponse senderProfileBeforeDeposit = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                senderSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        int senderAccountId = senderProfileBeforeDeposit.getAccounts().get(0).getId();

        // 4 - Deposit some money to sender account
        int randomBalance = RandomData.getRandomBalance();
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(senderAccountId)
                .balance(randomBalance)
                .build();

        new ValidatedCrudRequester<DepositMoneyResponse>(
                senderSpec,
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        // 5 - Create receiver
        CreateUserRequest receiverUser = AdminSteps.createUser();

        // 6 - Create receiver account
        var receiverSpec = RequestSpecs.authAsUser(receiverUser.getUsername(), receiverUser.getPassword());
        UserSteps.createAccount(receiverSpec);

        // 7 - Get receiver profile before transfer
        GetCustomerProfileResponse receiverProfileBefore = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                receiverSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        int receiverAccountId = receiverProfileBefore.getAccounts().get(0).getId();

        // 8 - Get updated sender profile before transfer
        GetCustomerProfileResponse senderProfileBefore = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                senderSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        double initialSenderBalance = senderProfileBefore.getAccounts().get(0).getBalance();
        double initialReceiverBalance = receiverProfileBefore.getAccounts().get(0).getBalance();

        // 9 - Prepare transfer with zero amount
        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(0)
                .build();

        // 10 - Attempt to perform transfer with invalid amount
        new CrudRequester(
                senderSpec,
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequestPlainText("Transfer amount must be at least 0.01")
        ).post(transferRequest);

        // 11 - Get profiles after failed transfer
        GetCustomerProfileResponse senderProfileAfter = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                senderSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        GetCustomerProfileResponse receiverProfileAfter = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                receiverSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        double finalSenderBalance = senderProfileAfter.getAccounts().get(0).getBalance();
        double finalReceiverBalance = receiverProfileAfter.getAccounts().get(0).getBalance();

        // 12 - Assert balances did not change
        softly.assertThat(finalSenderBalance)
                .as("Sender balance should not change after failed transfer")
                .isEqualTo(initialSenderBalance);

        softly.assertThat(finalReceiverBalance)
                .as("Receiver balance should not change after failed transfer")
                .isEqualTo(initialReceiverBalance);
    }

    @Test
    public void userCannotTransferMoreThanLimitTest() {
        // 1 - Create sender
        CreateUserRequest senderUser = AdminSteps.createUser();

        // 2 - Create sender account
        var senderSpec = RequestSpecs.authAsUser(senderUser.getUsername(), senderUser.getPassword());
        UserSteps.createAccount(senderSpec);

        // 3 - Get sender profile before deposit
        GetCustomerProfileResponse senderProfileBeforeDeposit = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                senderSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        int senderAccountId = senderProfileBeforeDeposit.getAccounts().get(0).getId();

        // 4 - Deposit some money to sender account
        int randomBalance = RandomData.getRandomBalance();
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(senderAccountId)
                .balance(randomBalance)
                .build();

        new ValidatedCrudRequester<DepositMoneyResponse>(
                senderSpec,
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        // 5 - Create receiver
        CreateUserRequest receiverUser = AdminSteps.createUser();

        // 6 - Create receiver account
        var receiverSpec = RequestSpecs.authAsUser(receiverUser.getUsername(), receiverUser.getPassword());
        UserSteps.createAccount(receiverSpec);

        // 7 - Get receiver profile before transfer
        GetCustomerProfileResponse receiverProfileBefore = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                receiverSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        int receiverAccountId = receiverProfileBefore.getAccounts().get(0).getId();

        // 8 - Get updated sender profile before transfer
        GetCustomerProfileResponse senderProfileBefore = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                senderSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        double initialSenderBalance = senderProfileBefore.getAccounts().get(0).getBalance();
        double initialReceiverBalance = receiverProfileBefore.getAccounts().get(0).getBalance();

        // 9 - Prepare transfer with amount greater than allowed limit
        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(10000.01)
                .build();

        // 10 - Attempt to perform transfer exceeding the limit
        new CrudRequester(
                senderSpec,
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequestPlainText("Transfer amount cannot exceed 10000")
        ).post(transferRequest);

        // 11 - Get profiles after failed transfer
        GetCustomerProfileResponse senderProfileAfter = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                senderSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        GetCustomerProfileResponse receiverProfileAfter = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                receiverSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

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
