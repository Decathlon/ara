@ignore
Feature: Reusable function to capture info about the date

  # Example:
  # * def dateResponse = call read('classpath:features/test_getdate.feature')
  # * def todaysDate = dateResponse.today

  Background: Setup
    * def getDate =
      """
      function() {
        var SimpleDateFormat = Java.type('java.text.SimpleDateFormat');
        var sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        var date = new java.util.Date();
        return sdf.format(date);
      } 
      """
    * def toTime =
      """
      function(s) {
        var SimpleDateFormat = Java.type('java.text.SimpleDateFormat');
        var sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        var date = sdf.parse(s)
        return date.time
      }
      """

  Scenario: Capture todays date
    * def today = getDate()
    * karate.log("Today's date is: " + today )
    * def todayTime = toTime(today)
