package api.helpers;

import api.requests.steps.CustomerSteps;
import io.restassured.specification.RequestSpecification;

public class AccountStepsHelper {
    public static double getBalance(RequestSpecification spec) {
        return CustomerSteps.getCustomerProfile(spec).getAccounts().getFirst().getBalance();
    }

    public static int getAccountId(RequestSpecification spec) {
        return CustomerSteps.getCustomerProfile(spec)
                .getAccounts()
                .getFirst()
                .getId();
    }

    public static String getAccountName(RequestSpecification spec) {
        return CustomerSteps.getCustomerProfile(spec)
                .getAccounts()
                .getFirst()
                .getAccountNumber();
    }
}
