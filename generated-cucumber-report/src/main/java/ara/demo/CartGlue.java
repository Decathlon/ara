package ara.demo;

import cucumber.api.java.ContinueNextStepsOnException;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static ara.demo.HooksGlue.simulateExecution;

public class CartGlue {

    @When("^the user goes to the cart page$")
    public void the_user_goes_to_the_cart_page() {
        simulateExecution();
    }

    @When("^the user validates the order$")
    public void the_user_validates_the_order() {
        simulateExecution();
    }

    @ContinueNextStepsOnException
    @Then("^the cart page shows (\\d+) product[s]?$")
    public void the_cart_page_shows_products(int productCount) {
        simulateExecution();
        if (HooksGlue.failingLevel > 0 && productCount != 1) {
            HooksGlue.failedScenario = "cart";
            throw new AssertionError("expected:<[" + productCount + "]> but was:<[1]>");
        }
    }

}
