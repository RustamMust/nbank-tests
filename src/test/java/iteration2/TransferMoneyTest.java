package iteration2;

import models.*;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
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
        new CrudRequester(
                senderSpec,
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        // 3 - Get sender profile before deposit
        GetCustomerProfileResponse senderProfileBefore = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                senderSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK()
        ).get();

        int senderAccountId = senderProfileBefore.getAccounts().get(0).getId();

        // 4 - Deposit to sender account
        DepositMoneyRequest depositToSender = DepositMoneyRequest.builder()
                .id(senderAccountId)
                .balance(transferAmount)
                .build();

        new ValidatedCrudRequester<DepositMoneyResponse>(
                senderSpec,
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK()
        ).post(depositToSender);

        // 5 - Create receiver user
        CreateUserRequest receiverUser = AdminSteps.createUser();

        // 6 - Create receiver account
        var receiverSpec = RequestSpecs.authAsUser(receiverUser.getUsername(), receiverUser.getPassword());
        new CrudRequester(
                receiverSpec,
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        // 7 - Get profiles before transfer
        GetCustomerProfileResponse senderProfileBeforeTransfer = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                senderSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK()
        ).get();

        GetCustomerProfileResponse receiverProfileBefore = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                receiverSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK()
        ).get();

        int receiverAccountId = receiverProfileBefore.getAccounts().get(0).getId();
        double senderBalanceBefore = senderProfileBeforeTransfer.getAccounts().get(0).getBalance();
        double receiverBalanceBefore = receiverProfileBefore.getAccounts().get(0).getBalance();

        // 8 - Perform transfer
        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(transferAmount)
                .build();

        new CrudRequester(
                senderSpec,
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsOKWithMessage("Transfer successful")
        ).post(transferRequest);

        // 9 - Get profiles after transfer
        GetCustomerProfileResponse senderProfileAfter = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                senderSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK()
        ).get();

        GetCustomerProfileResponse receiverProfileAfter = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                receiverSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK()
        ).get();

        double senderBalanceAfter = senderProfileAfter.getAccounts().get(0).getBalance();
        double receiverBalanceAfter = receiverProfileAfter.getAccounts().get(0).getBalance();

        // 10 - Assert balances
        softly.assertThat(senderBalanceAfter)
                .as("Sender balance should decrease by transfer amount")
                .isCloseTo(senderBalanceBefore - transferAmount, offset(0.001));

        softly.assertThat(receiverBalanceAfter)
                .as("Receiver balance should increase by transfer amount")
                .isCloseTo(receiverBalanceBefore + transferAmount, offset(0.001));
    }
}
