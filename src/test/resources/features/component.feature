Feature: Messaging Listeners and Senders

  Scenario: Persist stat update request and verify persistence
    Given a stat-update-request payload "{\"trainerId\":\"5\", \"year\":\"2025\", \"month\":\"8\", \"duration\":\"60\", \"actionType\":\"ADD\", \"userName\":\"Timmy.Smith\", \"firstName\":\"Timmy\", \"lastName\":\"Smith\", \"status\":\"true\"}"
    Then the data should be persisted and the username "Timmy.Smith" should be in the data base

