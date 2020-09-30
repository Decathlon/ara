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
