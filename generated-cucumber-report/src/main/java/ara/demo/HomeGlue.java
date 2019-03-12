package ara.demo;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static ara.demo.HooksGlue.simulateExecution;

public class HomeGlue {

    @Given("^the user is on the friendly home page$")
    public void the_user_is_on_the_friendly_home_page() {
        simulateExecution();
    }

    @When("^the user pauses the annoying carousel$")
    public void the_user_pauses_the_annoying_carousel() {
        simulateExecution();
    }

    @Then("^the annoying carousel finally stops making user's head spin$")
    public void the_annoying_carousel_finally_stops_making_user_s_head_spin() {
        simulateExecution();
    }

}
