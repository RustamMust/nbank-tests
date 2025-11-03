package iteration2;

import generators.RandomData;

import io.restassured.specification.RequestSpecification;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.assertj.core.api.Assertions.offset;

public class DepositNegativeTests extends BaseTest {

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    public void userCannotDepositInvalidSmallAmountsTest(int depositAmount) {
        // 1 - Create a new user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Create an account
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        UserSteps.createAccount(requestSpec);

        // 3 - Get customer profile before deposit
        GetCustomerProfileResponse customerProfile = new ValidatedCrudRequester<GetCustomerProfileResponse>(requestSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        // 4 - Get customer account id from profile
        int accountId = customerProfile.getAccounts().get(0).getId();

        // 5 - Get balance before deposit
        double initialBalance = customerProfile.getAccounts().get(0).getBalance();

        // 6 - Prepare data for deposit money request
        DepositMoneyRequest invalidDeposit = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(depositAmount)
                .build();

        // 7 - Try to make invalid deposit
        new CrudRequester(requestSpec,
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsBadRequestPlainText("Deposit amount must be at least 0.01"))
                .post(invalidDeposit);

        // 8 - Get customer profile after deposit
        GetCustomerProfileResponse updatedProfile = new ValidatedCrudRequester<GetCustomerProfileResponse>(requestSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        // 9 - Get balance after deposit
        double finalBalance = updatedProfile.getAccounts().get(0).getBalance();

        // 10 - Assert that balance remains unchanged
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
        UserSteps.createAccount(requestSpec);

        // 3 - Get customer profile before deposit
        GetCustomerProfileResponse customerProfile = new ValidatedCrudRequester<GetCustomerProfileResponse>(requestSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        // 4 - Get customer account id from profile
        int accountId = customerProfile.getAccounts().get(0).getId();

        // 5 - Get balance before deposit
        double initialBalance = customerProfile.getAccounts().get(0).getBalance();

        // 6 - Prepare data for deposit money request
        DepositMoneyRequest invalidDeposit = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(depositAmount)
                .build();

        // 7 - Try to make invalid deposit
        new CrudRequester(requestSpec,
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsBadRequestPlainText("Deposit amount cannot exceed 5000"))
                .post(invalidDeposit);

        // 8 - Get customer profile after deposit
        GetCustomerProfileResponse updatedProfile = new ValidatedCrudRequester<GetCustomerProfileResponse>(requestSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        // 9 - Get balance after deposit
        double finalBalance = updatedProfile.getAccounts().get(0).getBalance();

        // 10 - Assert that balance remains unchanged
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

        UserSteps.createAccount(user1Spec);
        UserSteps.createAccount(user2Spec);

        // 3 - Get user1 profile before deposit
        GetCustomerProfileResponse user1ProfileBefore = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                user1Spec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        // 4 - Get user1 account id and balance before deposit
        int user1AccountId = user1ProfileBefore.getAccounts().get(0).getId();
        double initialBalance = user1ProfileBefore.getAccounts().get(0).getBalance();

        // 5 - Prepare deposit request to another user's account
        int randomBalance = RandomData.getRandomBalance();
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(user1AccountId)
                .balance(randomBalance)
                .build();

        // 6 - Attempt to deposit into another user's account
        new CrudRequester(user2Spec,
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsForbiddenPlainText("Unauthorized access to account"))
                .post(depositRequest);

        // 7 - Get user1 profile after deposit attempt
        GetCustomerProfileResponse user1ProfileAfter = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                user1Spec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        // 8 - Verify that balance remains unchanged
        double finalBalance = user1ProfileAfter.getAccounts().get(0).getBalance();

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
        UserSteps.createAccount(requestSpec);

        // 3 - Get customer profile before deposit
        GetCustomerProfileResponse customerProfile = new ValidatedCrudRequester<GetCustomerProfileResponse>(requestSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        // 4 - Get customer balance from profile
        double initialBalance = customerProfile.getAccounts().get(0).getBalance();

        // 5 - Prepare invalid deposit for non-existing account
        int nonExistingAccountId = 999999;
        int randomBalance = RandomData.getRandomBalance();
        DepositMoneyRequest invalidDeposit = DepositMoneyRequest.builder()
                .id(nonExistingAccountId)
                .balance(randomBalance)
                .build();

        // 6 - Try deposit
        new CrudRequester(requestSpec,
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsForbiddenPlainText("Unauthorized access to account"))
                .post(invalidDeposit);

        // 7 - Get profile again
        GetCustomerProfileResponse updatedProfile = new ValidatedCrudRequester<GetCustomerProfileResponse>(requestSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        // 8 - Get customer balance from profile
        double finalBalance = updatedProfile.getAccounts().get(0).getBalance();

        // 9 - Assert balance unchanged
        softly.assertThat(finalBalance)
                .as("Balance should not change after trying to deposit to non-existing account")
                .isEqualTo(initialBalance, offset(0.001));
    }
}
