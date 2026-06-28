Feature: Search in Booking

  Background:
    Given booking search page is opened

  Scenario: Looking for 'Akra Kemer' hotel
    When user searches for "Akra Kemer"
    Then "Akra Kemer - Ultra All Inclusive" hotel is shown
    And 'Akra Kemer' hotel rating is "9.1"


    Scenario Outline: Looking hotels
      When user searches for "<hotel>"
      Then "<expectedResult>" hotel is shown
      Examples:
      | hotel | expectedResult |
      | Akra Kemer | Akra Kemer - Ultra All Inclusive |
      | Meraki     | Meraki Resort Sharm El Sheikh Adults only |