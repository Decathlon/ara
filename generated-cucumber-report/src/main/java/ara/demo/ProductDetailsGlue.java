package ara.demo;

import cucumber.api.java.ContinueNextStepsOnException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static ara.demo.HooksGlue.simulateExecution;

public class ProductDetailsGlue {

    @When("^the user goes to the product details page$")
    public void the_user_goes_to_the_product_details_page() {
        simulateExecution();
    }

    @Given("^the user is on the useless \"([^\"]*)\" product details page$")
    public void the_user_is_on_the_useless_product_details_page(String productName) {
        simulateExecution();
    }

    @When("^the user clicks on the Add To Cart button$")
    public void the_user_clicks_on_the_Add_To_Cart_button() {
        simulateExecution();
    }

    @When("^the user clicks on Reviews$")
    public void the_user_clicks_on_Reviews() {
        simulateExecution();
    }

    @ContinueNextStepsOnException
    @Then("^the review (\\d+) is \"([^\"]*)\"$")
    public void the_review_is(int reviewPosition, String reviewText) {
        simulateExecution();
    }

    @ContinueNextStepsOnException
    @Then("^the displayed price reduction is \"([^\"]*)\"$")
    public void the_displayed_price_reduction_is(String reduction) {
        simulateExecution();
    }

}
