Feature: Messaging Listeners and Senders

  Scenario: Persist stat update request and verify persistence
    Given a stat-update-request payload from file "src/test/resources/payload.json"
    Then the data should be persisted and the username "Timmy.Smith" should be in the data base

