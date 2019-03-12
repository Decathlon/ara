package ara.demo;

import cucumber.api.java.ContinueNextStepsOnException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static ara.demo.HooksGlue.simulateExecution;

public class ProductsListingGlue {

    @Given("^the user is on the useless-products listing page$")
    public void the_user_is_on_the_useless_products_listing_page() {
        simulateExecution();
    }

    @When("^the user counts the visible products$")
    public void the_user_counts_the_visible_products() {
        simulateExecution();
    }

    @ContinueNextStepsOnException
    @Then("^there (?:is|are) (\\d+) useless product[s]?$")
    public void there_are_useless_products(int productCount) {
        simulateExecution();
    }

}
