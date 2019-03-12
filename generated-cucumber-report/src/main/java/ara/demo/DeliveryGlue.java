package ara.demo;

import cucumber.api.java.en.When;

import static ara.demo.HooksGlue.simulateExecution;

public class DeliveryGlue {

    @When("^the user chooses the delivery option \"([^\"]*)\"$")
    public void the_user_chooses_the_delivery_option(String deliveryOptionName) {
        simulateExecution();
    }

}
