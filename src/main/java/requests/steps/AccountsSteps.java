package requests.steps;

import io.restassured.specification.RequestSpecification;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import specs.ResponseSpecs;

public class AccountsSteps {
    public static void createAccount(RequestSpecification userSpec) {
        new CrudRequester(
                userSpec,
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated()
        ).post(null); // null because body not needed
    }
}
