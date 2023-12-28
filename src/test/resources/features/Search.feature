@test
Feature: Search

  Scenario: Search
    Given I navigate to the Google homepage
    When I search for "Cucumber Selenium"
    Then the search result page title should contain "Cucumber Selenium - Google Search"
