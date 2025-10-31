package iteration2;

import generators.RandomData;

import io.restassured.specification.RequestSpecification;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.accounts.CreateAccountRequester;
import requests.accounts.DepositMoneyRequester;
import requests.admin.AdminCreateUserRequester;
import requests.customer.GetCustomerProfileRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.assertj.core.api.Assertions.offset;

public class DepositMoneyTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 4999, 5000})
    public void userCanDepositMoneyTest(int depositAmount) {
        // 1 - Prepare data for user creation
        String username = RandomData.getUsername();
        String password = RandomData.getPassword();

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(UserRole.USER.toString())
                .build();

        // 2 - Create a new user
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        // 3 - Create an account for the user
        RequestSpecification userSpec = RequestSpecs.authAsUser(username, password);
        new CreateAccountRequester(userSpec, ResponseSpecs.entityWasCreated())
                .post(null);

        // 4 - Get customer profile before deposit
        GetCustomerProfileResponse customerProfileBefore =
                new GetCustomerProfileRequester(userSpec, ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

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
        DepositMoneyResponse depositResponse =
                new DepositMoneyRequester(userSpec, ResponseSpecs.requestReturnsOK())
                        .post(depositRequest)
                        .extract()
                        .as(DepositMoneyResponse.class);

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
                .isEqualTo("DEPOSIT");

        // 10 - Get profile after deposit
        GetCustomerProfileResponse customerProfileAfter =
                new GetCustomerProfileRequester(userSpec, ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        // 11 - Get balance after deposit
        double finalBalance = customerProfileAfter.getAccounts().get(0).getBalance();

        // 12 - Assert balance from profile after deposit
        softly.assertThat(finalBalance)
                .as("Balance after deposit should match the expected value")
                .isCloseTo(initialBalance + depositAmount, offset(0.001));
    }
}
