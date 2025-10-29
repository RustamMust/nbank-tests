package iteration2;

import generators.RandomData;
import models.*;
import requests.accounts.CreateAccountRequester;
import requests.accounts.DepositMoneyRequester;
import requests.accounts.TransferMoneyRequester;
import requests.admin.AdminCreateUserRequester;
import requests.customer.GetCustomerProfileRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.assertj.core.api.Assertions.within;

import iteration1.BaseTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TransferMoneyTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 9999, 10000})
    public void userCanTransferMoneyTest(int transferAmount) {
        // 1 - Prepare sender data
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

        // 6 - Prepare data to deposit to sender account
        DepositMoneyRequest depositToSender = DepositMoneyRequest.builder()
                .id(senderAccountId)
                .balance(transferAmount)
                .build();

        // 7 - Deposit to sender account
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

        // 13 - Refresh sender profile after deposit
        senderProfile = new GetCustomerProfileRequester(senderSpec, ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(GetCustomerProfileResponse.class);

        // 14 - Get balances before transfer
        double senderBalanceBefore = senderProfile.getAccounts().get(0).getBalance();
        double receiverBalanceBefore = receiverProfile.getAccounts().get(0).getBalance();

        // 15 - Prepare data to perform transfer
        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(transferAmount)
                .build();

        // 16 - Perform transfer
        new TransferMoneyRequester(
                senderSpec,
                ResponseSpecs.requestReturnsOKWithMessage("Transfer successful")
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
        double senderBalanceAfter = senderAfter.getAccounts().get(0).getBalance();
        double receiverBalanceAfter = receiverAfter.getAccounts().get(0).getBalance();

        // 20 - Assert that balances have changed
        softly.assertThat(senderBalanceAfter)
                .as("Sender balance should decrease by transfer amount")
                .isEqualTo(senderBalanceBefore - transferAmount, within(0.001));

        softly.assertThat(receiverBalanceAfter)
                .as("Receiver balance should increase by transfer amount")
                .isEqualTo(receiverBalanceBefore + transferAmount, within(0.001));
    }
}
