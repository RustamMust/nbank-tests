package iteration2;

import generators.RandomData;

import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import requests.accounts.DepositMoneyRequester;
import requests.accounts.TransferMoneyRequester;
import requests.customer.GetCustomerProfileRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.assertj.core.api.Assertions.within;

public class TransferMoneyNegativeTest extends BaseTest {

    @Test
    public void userCannotTransferNegativeAmountTest() {
        // 1 - Prepare data to create sender
        String senderUsername = RandomData.getUsername();
        String password = RandomData.getPassword();

        CreateUserRequest senderUserRequest = CreateUserRequest.builder()
                .username(senderUsername)
                .password(password)
                .role(UserRole.USER.toString())
                .build();

        // 2 - Create sender
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(senderUserRequest);

        // 3 - Create sender account
        var senderSpec = RequestSpecs.authAsUser(senderUsername, password);
        new CreateAccountRequester(senderSpec, ResponseSpecs.entityWasCreated())
                .post(null);

        // 4 - Get sender profile
        GetCustomerProfileResponse senderProfile =
                new GetCustomerProfileRequester(senderSpec, ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        // 5 - Get sender account id from profile
        int senderAccountId = senderProfile.getAccounts().get(0).getId();

        // 6 - Prepare data to make deposit for sender
        int randomBalance = RandomData.getRandomBalance();
        DepositMoneyRequest depositToSender = DepositMoneyRequest.builder()
                .id(senderAccountId)
                .balance(randomBalance)
                .build();

        // 7 - Make deposit for sender
        new DepositMoneyRequester(senderSpec, ResponseSpecs.requestReturnsOK())
                .post(depositToSender);

        // 8 - Prepare data to create receiver
        String receiverUsername = RandomData.getUsername();
        CreateUserRequest receiverUserRequest = CreateUserRequest.builder()
                .username(receiverUsername)
                .password(password)
                .role(UserRole.USER.toString())
                .build();

        // 9 - Create receiver
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(receiverUserRequest);

        // 10 - Create receiver account
        var receiverSpec = RequestSpecs.authAsUser(receiverUsername, password);
        new CreateAccountRequester(receiverSpec, ResponseSpecs.entityWasCreated())
                .post(null);

        // 11 - Get receiver profile
        GetCustomerProfileResponse receiverProfile =
                new GetCustomerProfileRequester(receiverSpec, ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        // 12 - Get receiver account id from profile
        int receiverAccountId = receiverProfile.getAccounts().get(0).getId();

        // 13 - Refresh sender profile to get updated balance
        senderProfile = new GetCustomerProfileRequester(senderSpec, ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(GetCustomerProfileResponse.class);

        // 14 - Get balances before transfer
        double initialSenderBalance = senderProfile.getAccounts().get(0).getBalance();
        double initialReceiverBalance = receiverProfile.getAccounts().get(0).getBalance();

        // 15 - Prepare data to transfer amount greater than limit
        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(-1)
                .build();

        // 16 - Attempt to transfer amount greater than limit
        new TransferMoneyRequester(
                senderSpec,
                ResponseSpecs.requestReturnsBadRequestPlainText("Transfer amount must be at least 0.01")
        ).post(transferRequest);

        // 17 - Get sender profile after transfer
        GetCustomerProfileResponse senderAfter =
                new GetCustomerProfileRequester(senderSpec, ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        // 18 - Get receiver profile after transfer
        GetCustomerProfileResponse receiverAfter =
                new GetCustomerProfileRequester(receiverSpec, ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        // 19 - Get balances after transfer
        double finalSenderBalance = senderAfter.getAccounts().get(0).getBalance();
        double finalReceiverBalance = receiverAfter.getAccounts().get(0).getBalance();

        // 20 - Assert that balances have not changed
        softly.assertThat(finalSenderBalance)
                .as("Sender balance should not change after failed transfer")
                .isEqualTo(initialSenderBalance);

        softly.assertThat(finalReceiverBalance)
                .as("Receiver balance should not change after failed transfer")
                .isEqualTo(initialReceiverBalance);
    }


    @Test
    public void userCannotTransferZeroAmountTest() {
        // 1 - Prepare data to create sender
        String senderUsername = RandomData.getUsername();
        String password = RandomData.getPassword();

        CreateUserRequest senderUserRequest = CreateUserRequest.builder()
                .username(senderUsername)
                .password(password)
                .role(UserRole.USER.toString())
                .build();

        // 2 - Create sender
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(senderUserRequest);

        // 3 - Create sender account
        var senderSpec = RequestSpecs.authAsUser(senderUsername, password);
        new CreateAccountRequester(senderSpec, ResponseSpecs.entityWasCreated())
                .post(null);

        // 4 - Create sender profile
        GetCustomerProfileResponse senderProfile =
                new GetCustomerProfileRequester(senderSpec, ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        // 5 - Create sender account id from profile
        int senderAccountId = senderProfile.getAccounts().get(0).getId();

        // 6 - Prepare data to make deposit for sender
        int randomBalance = RandomData.getRandomBalance();
        DepositMoneyRequest depositToSender = DepositMoneyRequest.builder()
                .id(senderAccountId)
                .balance(randomBalance)
                .build();

        // 7 - Make deposit for sender
        new DepositMoneyRequester(senderSpec, ResponseSpecs.requestReturnsOK())
                .post(depositToSender);

        // 8 - Prepare data to create receiver
        String receiverUsername = RandomData.getUsername();
        CreateUserRequest receiverUserRequest = CreateUserRequest.builder()
                .username(receiverUsername)
                .password(password)
                .role(UserRole.USER.toString())
                .build();

        // 9 - Create receiver
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(receiverUserRequest);

        // 10 - Create receiver account
        var receiverSpec = RequestSpecs.authAsUser(receiverUsername, password);
        new CreateAccountRequester(receiverSpec, ResponseSpecs.entityWasCreated())
                .post(null);

        // 11 - Get receiver profile
        GetCustomerProfileResponse receiverProfile =
                new GetCustomerProfileRequester(receiverSpec, ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        // 12 - Get receiver account id from profile
        int receiverAccountId = receiverProfile.getAccounts().get(0).getId();

        // 13 - Refresh sender profile to get updated balance
        senderProfile = new GetCustomerProfileRequester(senderSpec, ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(GetCustomerProfileResponse.class);

        // 14 - Get balances before transfer
        double initialSenderBalance = senderProfile.getAccounts().get(0).getBalance();
        double initialReceiverBalance = receiverProfile.getAccounts().get(0).getBalance();

        // 15 - Prepare data to transfer zero amount
        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(0)
                .build();

        // 16 - Attempt to transfer zero amount
        new TransferMoneyRequester(
                senderSpec,
                ResponseSpecs.requestReturnsBadRequestPlainText("Transfer amount must be at least 0.01")
        ).post(transferRequest);

        // 17 - Get sender profile after transfer
        GetCustomerProfileResponse senderAfter =
                new GetCustomerProfileRequester(senderSpec, ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        // 18 - Get receiver profile after transfer
        GetCustomerProfileResponse receiverAfter =
                new GetCustomerProfileRequester(receiverSpec, ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        // 19 - Get balances after transfer
        double finalSenderBalance = senderAfter.getAccounts().get(0).getBalance();
        double finalReceiverBalance = receiverAfter.getAccounts().get(0).getBalance();

        // 20 - Assert that balances have not changed
        softly.assertThat(finalSenderBalance)
                .as("Sender balance should not change after failed transfer")
                .isCloseTo(initialSenderBalance, within(0.0001));

        softly.assertThat(finalReceiverBalance)
                .as("Receiver balance should not change after failed transfer")
                .isCloseTo(initialReceiverBalance, within(0.0001));
    }

    @Test
    public void userCannotTransferMoreThanLimitTest() {
        // 1 - Prepare data to create sender
        String senderUsername = RandomData.getUsername();
        String password = RandomData.getPassword();

        CreateUserRequest senderUserRequest = CreateUserRequest.builder()
                .username(senderUsername)
                .password(password)
                .role(UserRole.USER.toString())
                .build();

        // 2 - Create sender
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(senderUserRequest);

        // 3 - Create sender account
        var senderSpec = RequestSpecs.authAsUser(senderUsername, password);
        new CreateAccountRequester(senderSpec, ResponseSpecs.entityWasCreated())
                .post(null);

        // 4 - Get sender profile
        GetCustomerProfileResponse senderProfile =
                new GetCustomerProfileRequester(senderSpec, ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        // 5 - Get sender account id from profile
        int senderAccountId = senderProfile.getAccounts().get(0).getId();

        // 6 - Prepare data to make deposit for sender
        int randomBalance = RandomData.getRandomBalance();
        DepositMoneyRequest depositToSender = DepositMoneyRequest.builder()
                .id(senderAccountId)
                .balance(randomBalance)
                .build();

        // 7 - Make deposit for sender
        new DepositMoneyRequester(senderSpec, ResponseSpecs.requestReturnsOK())
                .post(depositToSender);

        // 8 - Prepare data to create receiver
        String receiverUsername = RandomData.getUsername();
        CreateUserRequest receiverUserRequest = CreateUserRequest.builder()
                .username(receiverUsername)
                .password(password)
                .role(UserRole.USER.toString())
                .build();

        // 9 - Create receiver
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(receiverUserRequest);

        // 10 - Create receiver account
        var receiverSpec = RequestSpecs.authAsUser(receiverUsername, password);
        new CreateAccountRequester(receiverSpec, ResponseSpecs.entityWasCreated())
                .post(null);

        // 11 - Get receiver profile
        GetCustomerProfileResponse receiverProfile =
                new GetCustomerProfileRequester(receiverSpec, ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        // 12 - Get receiver account id from profile
        int receiverAccountId = receiverProfile.getAccounts().get(0).getId();

        // 13 - Refresh sender profile to get updated balance
        senderProfile = new GetCustomerProfileRequester(senderSpec, ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(GetCustomerProfileResponse.class);

        // 14 - Get balances before transfer
        double initialSenderBalance = senderProfile.getAccounts().get(0).getBalance();
        double initialReceiverBalance = receiverProfile.getAccounts().get(0).getBalance();

        // 15 - Prepare data to transfer amount greater than limit
        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(10000.01)
                .build();

        // 16 - Attempt to transfer amount greater than limit
        new TransferMoneyRequester(
                senderSpec,
                ResponseSpecs.requestReturnsBadRequestPlainText("Transfer amount cannot exceed 10000")
        ).post(transferRequest);

        // 17 - Get sender profile after transfer
        GetCustomerProfileResponse senderAfter =
                new GetCustomerProfileRequester(senderSpec, ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        // 18 - Get receiver profile after transfer
        GetCustomerProfileResponse receiverAfter =
                new GetCustomerProfileRequester(receiverSpec, ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        // 19 - Get balances after transfer
        double finalSenderBalance = senderAfter.getAccounts().get(0).getBalance();
        double finalReceiverBalance = receiverAfter.getAccounts().get(0).getBalance();

        // 20 - Assert that balances have not changed
        softly.assertThat(finalSenderBalance)
                .as("Sender balance should not change after failed transfer")
                .isCloseTo(initialSenderBalance, within(0.0001));

        softly.assertThat(finalReceiverBalance)
                .as("Receiver balance should not change after failed transfer")
                .isCloseTo(initialReceiverBalance, within(0.0001));
    }
}
