package iteration2;

import generators.RandomData;
import models.*;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AccountsSteps;
import requests.steps.AdminSteps;
import requests.steps.CustomerSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.assertj.core.api.Assertions.offset;

import iteration1.BaseTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TransferMoneyTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 9999.99, 10000.00})
    public void userCanTransferMoneyTest(double transferAmount) {
        // 1 - Create sender user
        CreateUserRequest senderUser = AdminSteps.createUser();

        // 2 - Create sender account
        var senderSpec = RequestSpecs.authAsUser(senderUser.getUsername(), senderUser.getPassword());
        AccountsSteps.createAccount(senderSpec);

        // 3 - Get sender profile before deposit
        GetCustomerProfileResponse senderProfileBefore = CustomerSteps.getCustomerProfile(senderSpec);

        // 4 - Get sender account id from profile
        int senderAccountId = senderProfileBefore.getAccounts().get(0).getId();

        // 5 - Deposit to sender account
        int randomBalance = RandomData.getRandomBalance();
        AccountsSteps.depositMoney(senderSpec, senderAccountId, randomBalance);

        // 6 - Create receiver user
        CreateUserRequest receiverUser = AdminSteps.createUser();

        // 7 - Create receiver account
        var receiverSpec = RequestSpecs.authAsUser(receiverUser.getUsername(), receiverUser.getPassword());
        AccountsSteps.createAccount(receiverSpec);

        // 8 - Get sender and receiver profiles before transfer
        GetCustomerProfileResponse senderProfileBeforeTransfer = CustomerSteps.getCustomerProfile(senderSpec);
        GetCustomerProfileResponse receiverProfileBefore = CustomerSteps.getCustomerProfile(receiverSpec);

        // 9 - Get receiver account id
        int receiverAccountId = receiverProfileBefore.getAccounts().get(0).getId();

        // 10 - Get sender and receiver balances
        double senderBalanceBefore = senderProfileBeforeTransfer.getAccounts().get(0).getBalance();
        double receiverBalanceBefore = receiverProfileBefore.getAccounts().get(0).getBalance();

        // 11 - Perform transfer
        AccountsSteps.transferMoney(senderSpec, senderAccountId, receiverAccountId, transferAmount);

        // 12 - Get sender and receiver profiles after transfer
        GetCustomerProfileResponse senderProfileAfter = CustomerSteps.getCustomerProfile(senderSpec);
        GetCustomerProfileResponse receiverProfileAfter = CustomerSteps.getCustomerProfile(receiverSpec);

        // 13 - Get sender and receiver balances
        double senderBalanceAfter = senderProfileAfter.getAccounts().get(0).getBalance();
        double receiverBalanceAfter = receiverProfileAfter.getAccounts().get(0).getBalance();

        // 14 - Assert balances
        softly.assertThat(senderBalanceAfter)
                .as("Sender balance should decrease by transfer amount")
                .isCloseTo(senderBalanceBefore - transferAmount, offset(0.001));

        softly.assertThat(receiverBalanceAfter)
                .as("Receiver balance should increase by transfer amount")
                .isCloseTo(receiverBalanceBefore + transferAmount, offset(0.001));
    }
}
