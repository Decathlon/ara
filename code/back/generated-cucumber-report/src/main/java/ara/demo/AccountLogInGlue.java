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

public class AccountLogInGlue {

    private static final String LOG_IN = "Log in";
    private static final String CREATE_ACCOUNT = "Account creation";

    private String userConnectionReason = CREATE_ACCOUNT;

    @Given("^the user is on the log-in page$")
    public void the_user_is_on_the_log_in_page() {
        simulateExecution();
    }

    @When("^the user enters a login$")
    public void the_user_enters_a_login() {
        simulateExecution();
    }

    @When("^the user enters a password$")
    public void the_user_enters_a_password() {
        simulateExecution();
    }

    @When("^the user validates the connection$")
    public void the_user_validates_the_connection() {
        simulateExecution();
        userConnectionReason = LOG_IN;
    }

    @Then("^the user is connected$")
    public void the_user_is_connected() throws WebsiteException {
        simulateExecution();
        if (HooksGlue.failingLevel > 1) {
            HooksGlue.failedScenario = (CREATE_ACCOUNT.equals(userConnectionReason) ? "create-account" : "log-in");
            throw new WebsiteException(userConnectionReason + " failed for occult reasons.");
        }
    }

}
