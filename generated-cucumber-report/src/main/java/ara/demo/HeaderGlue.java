package ara.demo;

import cucumber.api.java.ContinueNextStepsOnException;
import cucumber.api.java.en.Then;

import static ara.demo.HooksGlue.simulateExecution;

public class HeaderGlue {

    @ContinueNextStepsOnException
    @Then("^the cart (?:now )?has (\\d+) product[s]?$")
    public void the_cart_now_has_products(int productCount) {
        simulateExecution();
    }

}
