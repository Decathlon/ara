package ara.demo;

import cucumber.api.java.en.Given;

import static ara.demo.HooksGlue.simulateExecution;

public class GivenGlue {

    @Given("^the user has (\\d+) product[s]? in cart$")
    public void the_user_has_products_in_cart(int productCount) {
        simulateExecution();
    }

    @Given("^the user has products in cart$")
    public void the_user_has_products_in_cart() {
        the_user_has_products_in_cart(2);
    }

    @Given("^the user chosen a delivery option$")
    public void the_user_chosen_a_delivery_option() {
        simulateExecution();
    }

    @Given("^the \"([^\"]*)\" product is on sale with a \"([^\"]*)\" reduction$")
    public void the_product_is_on_sale_with_a_reduction(String productName, String reduction) {
        simulateExecution();
    }

}
