package iteration2;

import io.restassured.specification.RequestSpecification;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.assertj.core.api.Assertions.offset;

public class DepositMoneyTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 4999, 5000})
    public void userCanDepositMoneyTest(int depositAmount) {
        // 1 - Create a new user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Create an account
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        UserSteps.createAccount(requestSpec);

        // 3 - Get customer profile before deposit
        GetCustomerProfileResponse customerProfileBefore = new ValidatedCrudRequester<GetCustomerProfileResponse>(requestSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        // 5 - Extract account info
        int accountId = customerProfileBefore.getAccounts().get(0).getId();
        double initialBalance = customerProfileBefore.getAccounts().get(0).getBalance();

        // 6 - Assert that initial balance is non-negative
        Assertions.assertTrue(initialBalance >= 0, "Initial balance should be non-negative");

        // 7 - Prepare deposit request
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(depositAmount)
                .build();

        // 8 - Perform deposit and deserialize response
        DepositMoneyResponse depositResponse = new ValidatedCrudRequester<DepositMoneyResponse>(requestSpec,
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                        .post(depositRequest);

        // 9 - Assert deposit response
        softly.assertThat(depositResponse.getBalance())
                .as("Balance should increase by deposit amount")
                .isCloseTo(initialBalance + depositAmount, offset(0.001));

        softly.assertThat(depositResponse.getTransactions())
                .as("Transactions list should not be null")
                .isNotNull();

        softly.assertThat(depositResponse.getTransactions())
                .as("Transactions list should not be empty")
                .isNotEmpty();

        DepositMoneyResponse.Transaction lastTransaction =
                depositResponse.getTransactions().get(depositResponse.getTransactions().size() - 1);

        softly.assertThat(lastTransaction.getAmount())
                .as("Transaction amount mismatch")
                .isCloseTo(depositAmount, offset(0.001));

        softly.assertThat(lastTransaction.getType())
                .as("Transaction type should be DEPOSIT")
                .isEqualTo(TransactionType.DEPOSIT.name());

        // 10 - Get profile after deposit
        GetCustomerProfileResponse customerProfileAfter = new ValidatedCrudRequester<GetCustomerProfileResponse>(requestSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                        .get();

        // 11 - Get balance after deposit
        double finalBalance = customerProfileAfter.getAccounts().get(0).getBalance();

        // 12 - Assert balance from profile after deposit
        softly.assertThat(finalBalance)
                .as("Balance after deposit should match the expected value")
                .isCloseTo(initialBalance + depositAmount, offset(0.001));
    }
}
