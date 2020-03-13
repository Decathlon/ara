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
import cucumber.api.java.en.When;

import static ara.demo.HooksGlue.simulateExecution;

public class AccountCreationGlue {

    @Given("^the user is on the account creation page$")
    public void the_user_is_on_the_account_creation_page() {
        simulateExecution();
    }

    @When("^the user enters a new login$")
    public void the_user_enters_a_new_login() {
        simulateExecution();
    }

    @When("^the user enters a new password$")
    public void the_user_enters_a_new_password() {
        simulateExecution();
    }

    @When("^the user validates the account creation$")
    public void the_user_validates_the_account_creation() {
        simulateExecution();
    }

}
