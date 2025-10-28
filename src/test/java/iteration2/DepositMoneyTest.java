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
        Assertions.assertEquals(
                initialBalance + depositAmount,
                depositResponse.getBalance(),
                0.001,
                "Balance should increase by deposit amount"
        );

        Assertions.assertNotNull(depositResponse.getTransactions(), "Transactions list should not be null");
        Assertions.assertFalse(depositResponse.getTransactions().isEmpty(), "Transactions list should not be empty");

        DepositMoneyResponse.Transaction lastTransaction =
                depositResponse.getTransactions().get(depositResponse.getTransactions().size() - 1);

        Assertions.assertEquals(depositAmount, lastTransaction.getAmount(), 0.001, "Transaction amount mismatch");
        Assertions.assertEquals("DEPOSIT", lastTransaction.getType(), "Transaction type should be DEPOSIT");

        // 10 - Verify balance from profile after deposit
        GetCustomerProfileResponse customerProfileAfter =
                new GetCustomerProfileRequester(userSpec, ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        double finalBalance = customerProfileAfter.getAccounts().get(0).getBalance();

        Assertions.assertEquals(
                initialBalance + depositAmount,
                finalBalance,
                0.001,
                "Balance after deposit should match the expected value"
        );
    }
}
