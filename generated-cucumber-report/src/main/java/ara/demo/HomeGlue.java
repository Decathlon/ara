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
