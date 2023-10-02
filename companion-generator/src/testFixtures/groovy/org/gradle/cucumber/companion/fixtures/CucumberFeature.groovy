/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.cucumber.companion.fixtures

import groovy.transform.CompileStatic
import org.intellij.lang.annotations.Language

import java.security.MessageDigest

@CompileStatic
enum CucumberFeature {
    PRODUCT_SEARCH(
        ExpectedOutcome.SUCCESS,
        "Product Search",
        '',
        'Users can search for products',
        '''\
            Feature: Product Search
              Scenario: Users can search for products
                Given a user is on the homepage
                When they enter a product name in the search bar
                And click the "Search" button
                Then they should see a list of matching products
        '''.stripIndent(true),
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
    ),

    SHOPPING_CART(
        ExpectedOutcome.SUCCESS,
        'Shopping Cart',
        '',
        'Users can add and remove items from the shopping cart',
        '''\
            Feature: Shopping Cart
              Scenario: Users can add and remove items from the shopping cart
                Given a user has added items to their cart
                When they remove an item from the cart
                Then the item should be removed from the cart
                And the cart total should be updated accordingly
        '''.stripIndent(true),
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
    ),

    USER_REGISTRATION(
        ExpectedOutcome.SUCCESS,
        'User Registration',
        'user',
        'New users can create an account',
        '''\
            Feature: User Registration
              Scenario: New users can create an account
                Given a user is on the registration page
                When they fill in their information
                And click the "Sign Up" button
                Then they should be registered and logged in
        '''.stripIndent(true),
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
    ),

    PASSWORD_RESET(
        ExpectedOutcome.SUCCESS,
        'Password Reset',
        'user',
        'Users can reset their password',
        '''\
            Feature: Password Reset
              Scenario: Users can reset their password
                Given a user is on the password reset page
                When they enter their email address
                And click the "Reset Password" button
                Then they should receive an email with instructions to reset their password
        '''.stripIndent(true),
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
    ),
    PASSWORD_RESET_V2(
        ExpectedOutcome.SUCCESS,
        'Password Reset',
        'user',
        'Users can reset their password',
        '''\
            Feature: Password Reset
              Scenario: Users can reset their password
                Given a user is on the password reset page
                When they enter their email address
                And enter the correct CAPTCHA solution
                And click the "Reset Password" button
                Then they should receive an email with instructions to reset their password
        '''.stripIndent(true),
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

                @When("enter the correct CAPTCHA solution")
                public void enterCorrectCaptcha() {
                    // Implementation code for entering correct captcha.
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
        '''.stripIndent(true),
        true
    ),

    USER_PROFILE(
        ExpectedOutcome.SUCCESS,
        'User Profile',
        'user',
        'Users can update their profile information',
        '''\
            Feature: User Profile
              Scenario: Users can update their profile information
                Given a user is logged in
                When they navigate to their profile page
                And edit their profile information
                And click the "Save" button
                Then their profile information should be updated
        '''.stripIndent(true),
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
    ),

    FAILING_FEATURE(
        ExpectedOutcome.FAILED,
        'Failing Feature',
        'failing',
        'A feature which does not succeed when executed',
        '''\
            Feature: Failing Feature
              Scenario: A feature which does not succeed when executed
                Given an arbitrary precondition
                Then a condition that fails
        '''.stripIndent(true),
        '''\
            package failing;

            import io.cucumber.java.en.Given;
            import io.cucumber.java.en.When;
            import io.cucumber.java.en.Then;

            import org.junit.jupiter.api.Assertions;

            public class FailingFeatureSteps {

                @Given("an arbitrary precondition")
                public void anArbitraryPrecondition() {
                    // just a stub
                }

                @Then("a condition that fails")
                public void aConditionThatFails() {
                    Assertions.fail("This Test always fails");
                }
            }
        '''.stripIndent(true)
    );

    static final List<CucumberFeature> ALL_FEATURES = Collections.unmodifiableList(values().findAll { !it.isReplacement } as List<CucumberFeature>)
    static final List<CucumberFeature> ALL_SUCCEEDING_FEATURES = Collections.unmodifiableList(
        values().findAll { it.expectedOutcome == ExpectedOutcome.SUCCESS && !it.isReplacement } as List<CucumberFeature>)

    final String featureName
    final String packageName
    final String scenarioName
    final String featureFileContent
    final String stepFileContent
    final String contentHash

    final String relativePath
    final String className
    final String featureFilePath
    final String stepFilePath

    final ExpectedOutcome expectedOutcome
    final boolean isReplacement

    CucumberFeature(
        ExpectedOutcome expectedOutcome,
        String featureName,
        String packageName,
        String scenarioName,
        @Language("gherkin") String featureFileContent,
        @Language("JAVA") String stepFileContent,
        boolean isReplacement = false
    ) {
        this.isReplacement = isReplacement
        this.expectedOutcome = expectedOutcome
        this.featureName = featureName
        this.packageName = packageName
        this.scenarioName = scenarioName
        this.featureFileContent = featureFileContent
        this.stepFileContent = stepFileContent

        this.className = featureName.replaceAll("[^a-zA-Z0-9_]", "_")
        this.relativePath = packageName.replaceAll(/\./, '/')
        this.featureFilePath = joinToPath(relativePath, featureName + ".feature")
        this.stepFilePath = joinToPath(relativePath, featureName.replaceAll("[^a-zA-Z0-9_]", "") + "Steps.java")
        this.contentHash = contentHash(featureFileContent)
    }

    String toExpectedTestTaskOutput(String outcome = "PASSED") {
        // Gives a string like this:
        // "User_Profile > Cucumber > User Profile > user.User_Profile.Users can update their profile information PASSED"
        def packagePrefix = packageName.empty ? "" : packageName + "."
        return "$className > Cucumber > $featureName > $packagePrefix$className.$scenarioName $outcome"
    }

    private static String contentHash(String input) {
        MessageDigest md = MessageDigest.getInstance("SHA-256")
        Base64.urlEncoder.encodeToString(md.digest(input.bytes))
    }

    private static String joinToPath(String path, String fileName) {
        if (path == "") {
            return fileName
        }
        "$path/$fileName"
    }

    static List<CucumberFeature> all() {
        ALL_FEATURES
    }

    static List<CucumberFeature> allSucceeding() {
        ALL_SUCCEEDING_FEATURES
    }
}
