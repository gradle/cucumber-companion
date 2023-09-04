package org.gradle.cucumber.companion.fixtures

import groovy.transform.Memoized
import spock.util.io.FileSystemFixture

@SuppressWarnings("GrMethodMayBeStatic") //
class CucumberFixture {

    @Memoized
    List<ExpectedCompanionFile> expectedCompanionFiles(String suffix = '') {
        [
            ['Product Search', ''],
            ['Shopping Cart', ''],
            ['User Registration', 'user'],
            ['Password Reset', 'user'],
            ['User Profile', 'user']
        ].collect {
            def (name, pkg) = it
            ExpectedCompanionFile.create(name, pkg, suffix)
        }
    }

    void createFeatureFiles(FileSystemFixture projectDir) {
        projectDir.create {
            dir("src/test/resources") {
                file("Product Search.feature") << """\
                    Feature: Product Search
                      Scenario: Users can search for products
                        Given a user is on the homepage
                        When they enter a product name in the search bar
                        And click the "Search" button
                        Then they should see a list of matching products
                """.stripIndent(true)
                file("Shopping Cart.feature") << """\
                    Feature: Shopping Cart
                      Scenario: Users can add and remove items from the shopping cart
                        Given a user has added items to their cart
                        When they remove an item from the cart
                        Then the item should be removed from the cart
                        And the cart total should be updated accordingly
                """.stripIndent(true)
                dir("user") {
                    file("User Registration.feature") << """\
                        Feature: User Registration
                          Scenario: New users can create an account
                            Given a user is on the registration page
                            When they fill in their information
                            And click the "Sign Up" button
                            Then they should be registered and logged in
                    """.stripIndent(true)
                    file("Password Reset.feature") << """\
                        Feature: Password Reset
                          Scenario: Users can reset their password
                            Given a user is on the password reset page
                            When they enter their email address
                            And click the "Reset Password" button
                            Then they should receive an email with instructions to reset their password
                    """.stripIndent(true)
                    file("User Profile.feature") << """\
                        Feature: User Profile
                          Scenario: Users can update their profile information
                            Given a user is logged in
                            When they navigate to their profile page
                            And edit their profile information
                            And click the "Save" button
                            Then their profile information should be updated
                    """.stripIndent(true)
                }
            }
        }
    }

    void createStepFiles(FileSystemFixture projectDir) {
        projectDir.create {
            dir("src/test/java") {
                file("ProductSearchSteps.java") <<
                    // language=java
                    '''\
                    import io.cucumber.java.en.Given;
                    import io.cucumber.java.en.Then;
                    import io.cucumber.java.en.When;

                    public class ProductSearchSteps {

                        @Given("a user is on the homepage")
                        public void userIsOnHomepage() {
                        }

                        @When("they enter a product name in the search bar")
                        public void enterProductName() {
                        }

                        @When("click the \\"Search\\" button")
                        public void clickSearchButton() {
                        }

                        @Then("they should see a list of matching products")
                        public void verifyMatchingProducts() {
                        }

                   }
                '''.stripIndent(true)
                file("ShoppingCartSteps.java") <<
                    // language=java
                    '''\
                    import io.cucumber.java.en.Given;
                    import io.cucumber.java.en.When;
                    import io.cucumber.java.en.Then;

                    public class ShoppingCartSteps {

                        @Given("a user has added items to their cart")
                        public void userHasAddedItemsToCart() {
                        }

                        @When("they remove an item from the cart")
                        public void removeItemFromCart() {
                        }

                        @Then("the item should be removed from the cart")
                        public void verifyItemRemovedFromCart() {
                        }

                        @Then("the cart total should be updated accordingly")
                        public void verifyCartTotalUpdated() {
                        }
                    }
                '''.stripIndent(true)
                dir("user") {
                    file("UserRegistrationSteps.java") <<
                    // language=java
                    '''\
                        package user;

                        import io.cucumber.java.en.Given;
                        import io.cucumber.java.en.When;
                        import io.cucumber.java.en.Then;

                        public class UserRegistrationSteps {

                            @Given("a user is on the registration page")
                            public void userIsOnRegistrationPage() {
                            }

                            @When("they fill in their information")
                            public void fillInUserInfo() {
                            }

                            @When("click the \\"Sign Up\\" button")
                            public void clickSignUpButton() {
                            }

                            @Then("they should be registered and logged in")
                            public void verifyRegistrationAndLogin() {
                            }
                        }
                    '''.stripIndent(true)
                    file("PasswordResetSteps.java") <<
                    // language=java
                    '''\
                        package user;

                        import io.cucumber.java.en.Given;
                        import io.cucumber.java.en.When;
                        import io.cucumber.java.en.Then;

                        public class PasswordResetSteps {

                            @Given("a user is on the password reset page")
                            public void userIsOnPasswordResetPage() {
                                // Implementation code to navigate to the password reset page.
                            }

                            @When("they enter their email address")
                            public void enterEmailAddress() {
                                // Implementation code for entering the user's email address.
                            }

                            @When("click the \\"Reset Password\\" button")
                            public void clickResetPasswordButton() {
                                // Implementation code to simulate clicking the Reset Password button.
                            }

                            @Then("they should receive an email with instructions to reset their password")
                            public void verifyPasswordResetEmailSent() {
                                // Implementation code to verify that the user receives the password reset email.
                            }
                        }
                    '''.stripIndent(true)
                    file("UserProfileSteps.java") <<
                    // language=java
                    '''\
                        package user;

                        import io.cucumber.java.en.Given;
                        import io.cucumber.java.en.When;
                        import io.cucumber.java.en.Then;

                        public class UserProfileSteps {

                            @Given("a user is logged in")
                            public void userIsLoggedIn() {
                                // Implementation code to simulate a user being logged in.
                            }

                            @When("they navigate to their profile page")
                            public void navigateToUserProfilePage() {
                                // Implementation code to navigate to the user's profile page.
                            }

                            @When("edit their profile information")
                            public void editUserProfileInformation() {
                                // Implementation code to edit the user's profile information.
                            }

                            @When("click the \\"Save\\" button")
                            public void clickSaveButton() {
                                // Implementation code to simulate clicking the Save button.
                            }

                            @Then("their profile information should be updated")
                            public void verifyProfileInformationUpdated() {
                                // Implementation code to verify that the user's profile information is updated.
                            }
                        }
                    '''.stripIndent(true)
                }
            }
        }
    }
}
