package iteration2;

import io.restassured.specification.RequestSpecification;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import requests.steps.CustomerSteps;
import requests.steps.AccountsSteps;
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
        AccountsSteps.createAccount(requestSpec);

        // 3 - Get customer profile before deposit
        GetCustomerProfileResponse customerProfileBefore = CustomerSteps.getCustomerProfile(requestSpec);

        // 4 - Extract account id from profile
        int accountId = customerProfileBefore.getAccounts().get(0).getId();

        // 5 - Extract account balance from profile
        double initialBalance = customerProfileBefore.getAccounts().get(0).getBalance();

        // 6 - Assert that initial balance is non-negative
        softly.assertThat(initialBalance)
                .as("Initial balance should be non-negative")
                .isGreaterThanOrEqualTo(0);

        // 7 - Perform deposit
        DepositMoneyResponse depositResponse = AccountsSteps.depositMoney(requestSpec, accountId, depositAmount);

        // 8 - Assert deposit response
        softly.assertThat(depositResponse.getBalance())
                .as("Balance should increase by deposit amount")
                .isCloseTo(initialBalance + depositAmount, offset(0.001));

        softly.assertThat(depositResponse.getTransactions())
                .as("Transactions list should not be null")
                .isNotNull();

        softly.assertThat(depositResponse.getTransactions())
                .as("Transactions list should not be empty")
                .isNotEmpty();

        // 9 - Get transaction from deposit response
        DepositMoneyResponse.Transaction lastTransaction =
                depositResponse.getTransactions().get(depositResponse.getTransactions().size() - 1);

        // 10 - Assert transaction info
        softly.assertThat(lastTransaction.getAmount())
                .as("Transaction amount mismatch")
                .isCloseTo(depositAmount, offset(0.001));

        softly.assertThat(lastTransaction.getType())
                .as("Transaction type should be DEPOSIT")
                .isEqualTo(TransactionType.DEPOSIT.name());

        // 11 - Get profile after deposit
        GetCustomerProfileResponse customerProfileAfter = new ValidatedCrudRequester<GetCustomerProfileResponse>(requestSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                        .get();

        // 12 - Get balance after deposit
        double finalBalance = customerProfileAfter.getAccounts().get(0).getBalance();

        // 13 - Assert balance from profile after deposit
        softly.assertThat(finalBalance)
                .as("Balance after deposit should match the expected value")
                .isCloseTo(initialBalance + depositAmount, offset(0.001));
    }
}
