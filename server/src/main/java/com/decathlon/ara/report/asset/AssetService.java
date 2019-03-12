package com.decathlon.ara.report.asset;

/**
 * Save (write to disk, upload to SSH, etc.) part of the data from Cucumber and Postman reports (Cucumber screenshots
 * and Postman HTTP logs), and return URLs where they can be accessed.
 */
public interface AssetService {

    /**
     * Save a Cucumber scenario screenshot: it is extracted from reports for it to be accessible by other applications
     * by a simple URL.
     *
     * @param screenshot   the PNG bytes of the screenshot
     * @param scenarioName the name of the scenario for which the screenshot was taken (date is prepended, and png
     *                     extension is appended to generate file name)
     * @return the complete URL of the file having been saved, or null if save failed
     */
    String saveScreenshot(byte[] screenshot, String scenarioName);

    /**
     * Save a Postman HTTP logs: they are extracted as an HTML from reports for them to be accessible by other
     * applications by a simple URL.
     *
     * @param html the HTML representing the HTTP logs
     * @return the complete URL of the file having been saved, or null if save failed
     */
    String saveHttpLogs(String html);

}
