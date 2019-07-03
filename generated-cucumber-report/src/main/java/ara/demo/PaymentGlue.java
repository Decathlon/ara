/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

package ara.demo;

import cucumber.api.java.ContinueNextStepsFor;
import cucumber.api.java.ContinueNextStepsOnException;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.NoSuchElementException;

import static ara.demo.HooksGlue.simulateExecution;

public class PaymentGlue {

    @When("^the user goes to the payment page$")
    public void the_user_goes_to_the_payment_page() {
        simulateExecution();
    }

    @When("^the user choose the payment \"([^\"]*)\"$")
    public void the_user_choose_the_payment(String paymentMethodName) {
        simulateExecution();
    }

    @When("^the user validates the payment$")
    public void the_user_validates_the_payment() {
        simulateExecution();
    }

    @Then("^the user is redirected to the payment page$")
    public void the_user_is_redirected_to_the_payment_page() {
        simulateExecution();
    }

    @ContinueNextStepsOnException
    @Then("^the delivery option is \"([^\"]*)\"$")
    public void the_delivery_option_is(String deliveryOptionName) {
        simulateExecution();
        if (HooksGlue.failingLevel > 0) {
            HooksGlue.failedScenario = "delivery";
            throw new AssertionError("expected:<[" + deliveryOptionName + "]> but was:<[3D Printing]>");
        }
    }

    @ContinueNextStepsFor(AssertionError.class)
    @Then("^the delivery price is \"([^\"]*)\"$")
    public void the_delivery_price_is(String deliveryPrice) {
        simulateExecution();
        if (HooksGlue.failingLevel > 0) {
            HooksGlue.failedScenario = "delivery";
            throw new AssertionError("expected:<[" + deliveryPrice + "]> but was:<[50 cents]>");
        }
    }

    // No @ContinueNextSteps..., because the scenario won't be able to continue if it assumes an accepted order
    @Then("^the order is accepted$")
    public void the_order_is_accepted() {
        simulateExecution();
        if (HooksGlue.failingLevel > 1) {
            HooksGlue.failedScenario = "payment";
            throw new NoSuchElementException("Cannot locate {By.cssSelector: #order-confirmation}");
        }
    }

}
