package ara.demo;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;

import static ara.demo.HooksGlue.simulateExecution;

public class AccountCreationGlue {

    @Given("^the user is on the account creation page$")
    public void the_user_is_on_the_account_creation_page() {
        simulateExecution();
    }

    @When("^the user enters a new login$")
    public void the_user_enters_a_new_login() {
        simulateExecution();
    }

    @When("^the user enters a new password$")
    public void the_user_enters_a_new_password() {
        simulateExecution();
    }

    @When("^the user validates the account creation$")
    public void the_user_validates_the_account_creation() {
        simulateExecution();
    }

}
