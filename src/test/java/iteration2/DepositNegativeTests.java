package iteration2;

import generators.RandomData;

import io.restassured.specification.RequestSpecification;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.steps.AdminSteps;
import requests.steps.CustomerSteps;
import requests.steps.AccountsSteps;
import specs.RequestSpecs;

import static org.assertj.core.api.Assertions.offset;

public class DepositNegativeTests extends BaseTest {

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    public void userCannotDepositInvalidSmallAmountsTest(int depositAmount) {
        // 1 - Create a new user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Create an account
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        AccountsSteps.createAccount(requestSpec);

        // 3 - Get customer profile before deposit
        GetCustomerProfileResponse customerProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 4 - Get customer account id from profile
        int accountId = customerProfile.getAccounts().get(0).getId();

        // 5 - Get balance before deposit
        double initialBalance = customerProfile.getAccounts().get(0).getBalance();

        // 6 - Try to make invalid deposit
        AccountsSteps.depositMoneyExpectingError(
                requestSpec,
                accountId,
                depositAmount,
                "Deposit amount must be at least 0.01"
        );

        // 7 - Get customer profile after deposit
        GetCustomerProfileResponse updatedProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 8 - Get balance after deposit
        double finalBalance = updatedProfile.getAccounts().get(0).getBalance();

        // 9 - Assert that balance remains unchanged
        softly.assertThat(finalBalance)
                .as("Balance should not change after invalid deposit")
                .isEqualTo(initialBalance, offset(0.001));
    }

    @ParameterizedTest
    @ValueSource(ints = {5001})
    public void userCannotDepositInvalidLargeAmountsTest(int depositAmount) {
        // 1 - Create a new user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Create an account
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        AccountsSteps.createAccount(requestSpec);

        // 3 - Get customer profile before deposit
        GetCustomerProfileResponse customerProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 4 - Get customer account id from profile
        int accountId = customerProfile.getAccounts().get(0).getId();

        // 5 - Get balance before deposit
        double initialBalance = customerProfile.getAccounts().get(0).getBalance();

        // 6 - Try to make invalid deposit
        AccountsSteps.depositMoneyExpectingError(
                requestSpec,
                accountId,
                depositAmount,
                "Deposit amount cannot exceed 5000"
        );

        // 7 - Get customer profile after deposit
        GetCustomerProfileResponse updatedProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 8 - Get balance after deposit
        double finalBalance = updatedProfile.getAccounts().get(0).getBalance();

        // 9 - Assert that balance remains unchanged
        softly.assertThat(finalBalance)
                .as("Balance should not change after invalid deposit")
                .isEqualTo(initialBalance, offset(0.001));
    }

    @Test
    public void userCannotDepositToAnotherUserAccountTest() {
        // 1 - Create two users
        CreateUserRequest user1Request = AdminSteps.createUser();
        CreateUserRequest user2Request = AdminSteps.createUser();

        // 2 - Create accounts for both users
        RequestSpecification user1Spec = RequestSpecs.authAsUser(user1Request.getUsername(), user1Request.getPassword());
        RequestSpecification user2Spec = RequestSpecs.authAsUser(user2Request.getUsername(), user2Request.getPassword());

        AccountsSteps.createAccount(user1Spec);
        AccountsSteps.createAccount(user2Spec);

        // 3 - Get user1 profile before deposit
        GetCustomerProfileResponse user1ProfileBefore = CustomerSteps.getCustomerProfile(user1Spec);

        // 4 - Get user1 account id and balance before deposit
        int user1AccountId = user1ProfileBefore.getAccounts().get(0).getId();
        double initialBalance = user1ProfileBefore.getAccounts().get(0).getBalance();

        // 5 - Attempt to deposit into another user's account
        int randomBalance = RandomData.getRandomBalance();
        AccountsSteps.depositMoneyExpectingForbiddenError(
                user2Spec,
                user1AccountId,
                randomBalance,
                "Unauthorized access to account"
        );

        // 6 - Get user1 profile after deposit attempt
        GetCustomerProfileResponse user1ProfileAfter = CustomerSteps.getCustomerProfile(user1Spec);

        // 7 - Get balance
        double finalBalance = user1ProfileAfter.getAccounts().get(0).getBalance();

        // 8 - Assert that balance remains unchanged
        softly.assertThat(finalBalance)
                .as("Balance should not change after unauthorized deposit")
                .isEqualTo(initialBalance, offset(0.001));
    }

    @Test
    public void userCannotDepositToNonExistingAccountTest() {
        // 1 - Create a new user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Create an account
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        AccountsSteps.createAccount(requestSpec);

        // 3 - Get customer profile before deposit
        GetCustomerProfileResponse customerProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 4 - Get customer balance from profile
        double initialBalance = customerProfile.getAccounts().get(0).getBalance();

        // 5 - Try to deposit
        int nonExistingAccountId = RandomData.getNonExistingAccountId();;
        int randomBalance = RandomData.getRandomBalance();
        AccountsSteps.depositMoneyExpectingForbiddenError(
                requestSpec,
                nonExistingAccountId,
                randomBalance,
                "Unauthorized access to account"
        );

        // 6 - Get profile again
        GetCustomerProfileResponse updatedProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 7 - Get customer balance from profile
        double finalBalance = updatedProfile.getAccounts().get(0).getBalance();

        // 8 - Assert balance unchanged
        softly.assertThat(finalBalance)
                .as("Balance should not change after trying to deposit to non-existing account")
                .isEqualTo(initialBalance, offset(0.001));
    }
}
