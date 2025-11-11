package helpers;

import io.restassured.specification.RequestSpecification;
import requests.steps.CustomerSteps;

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
