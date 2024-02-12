package org.example;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

public class MultipleWidgets {
    public static void main(String[] args) throws InterruptedException {
    // Set the path to the Chrome WebDriver executable
    System.setProperty("webdriver.chrome.driver", "/Users/nkumar13/Downloads/chromedriver-mac-arm64/chromedriver");

    // Create ChromeOptions object to configure browser settings
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--start-maximized"); // Start Chrome in full screen mode

    // Initialize the WebDriver with the ChromeOptions
    WebDriver driver = new ChromeDriver(options);

    // load the properties file from resources folder
    Properties properties = new Properties();
    FileInputStream input = null;
    try {
        input = new FileInputStream("src/main/resources/config.properties");
    } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
    }
    try {
        properties.load(input);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }

    try {
        //load the web url from properties file
        driver.get(properties.getProperty("url"));
        // Find the username and password input fields and the login button
        WebElement usernameField = driver.findElement(By.id("username"));
        WebElement passwordField = driver.findElement(By.id("password"));

        String targetText = "Login";
        WebElement loginButton = driver.findElement(By.xpath("//span[@class='btn__text' and text()='" + targetText + "']"));

        // Enter your username and password by reading from properties file
        usernameField.sendKeys(properties.getProperty("username"));
        passwordField.sendKeys(properties.getProperty("password"));

        // Click the login button
        loginButton.click();
        Thread.sleep(5000);

        // Find and click on elements with specified attributes
        List<WebElement> folderElements = driver.findElements(By.className("list-item-holder--folder"));

        for (WebElement folderElement : folderElements) {

            String folder = "";
            try{
                folder = folderElement.getText();
            }
            catch (StaleElementReferenceException e) {
                // Log or print the exception message
                System.out.println("Stale Element Exception occurred: " + e.getMessage());
                // Continue with the loop
                break;
            }

            if (!folder.equals(properties.getProperty("folder"))) continue;

            folderElement.click();
            // Sleep for a moment to allow the page to load (you can use WebDriverWait for a more robust solution)
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Find elements within the folder and print their tag names
            List<WebElement> elementsInsideFolder = folderElement.findElements(By.cssSelector("div[data-navver-tree-dashboard]"));

            for (WebElement elem : elementsInsideFolder) {
                try {
                    if (!elem.getText().equals(properties.getProperty("processFolder"))) continue;

                    // Your code to handle the element goes here
                    // ...
                } catch (StaleElementReferenceException e) {
                    // Log or print the exception message
                    System.out.println("Stale Element Exception occurred: " + e.getMessage());
                    // Continue with the loop
                    break;
                }

                elem.click();
                Thread.sleep(2000);
                //find the + button anc click over it
                // Find the button element by its class name

                //For each chart type, create the widget

                JSONParser parser = new JSONParser();
                try {
                    Object obj = parser.parse(new FileReader("src/main/resources/Charts.json"));

                    JSONObject jsonObject = (JSONObject) obj;
                    JSONArray chartTypes = (JSONArray) jsonObject.get("chartTypes");

                    for (Object chartObj : chartTypes) {
                        JSONObject chart = (JSONObject) chartObj;
                        String chartName = (String) chart.get("name");
                        String fileName = "src/main/resources/" + chartName + ".json"; // attributes

                        try {
                            Object chartFileObj = parser.parse(new FileReader(fileName));
                            JSONObject chartFileJSON = (JSONObject) chartFileObj;

                            // Process the content of the chart file here
                            // Create a HashMap to store values from JSON
                            Map<String, String> chartValues = new HashMap<>();
                            // Extract key-value pairs and store in the HashMap
                            for (Object key : chartFileJSON.keySet()) {
                                String value = String.valueOf(chartFileJSON.get(key));
                                chartValues.put(String.valueOf(key), value);
                            }

                            //create the dashvoard/widger
                            createOrModifyWidgets(driver, chartName, chartValues);

                        } catch (IOException | ParseException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }catch (InterruptedException e){
        throw new RuntimeException(e);
    } finally {
        // Close the browser when done
        driver.quit();
    }
}

private static void createOrModifyWidgets(WebDriver driver,
                                          String chartType,
                                          Map<String, String> keyValueMap) throws InterruptedException {
        WebElement addButton;
        try{
            //when there is NO widget then follow this path
            addButton = driver.findElement(By.xpath("//span[@class='btn__text' and @data-translate='newWidget.selectData']"));
            addButton.click();
            Thread.sleep(5000);

            //try to select any item from the list such as brand for e,g
            WebElement popup = driver.findElement(By.cssSelector("div.popup-inner"));
            // Assuming "Brand" value is within a span with class "title"
            WebElement brandValue = popup.findElement(By.xpath(".//span[@title='Brand']"));
            // Click on the "Brand" value
            Actions actions = new Actions(driver);
            actions.moveToElement(brandValue).click().perform();


        }catch (NoSuchElementException e) {
            // Handle the exception (Element not found)
            //When there are widgets then follow this path
            addButton = driver.findElement(By.cssSelector("button.btn.btn--new-widget.js--btn-action.js--btn-new-widget"));
            addButton.click();
        }

        Thread.sleep(2000);
        WebElement advanceConfigElement = driver.findElement(By.cssSelector("div.control-freak[data-translate='newWidget.advanced']"));
        advanceConfigElement.click();
        Thread.sleep(2000);

        // Find and click the dropdown toggle element
        WebElement dropdownToggle = driver.findElement(By.cssSelector("span.we-t-toggler[data-icon-name='\\'general-arrow-down\\'']"));
        dropdownToggle.click();

        // Find the scrollable container
        WebElement scrollableContainer = driver.findElement(By.cssSelector("div.wt-items-host.content"));
        // Scroll down to the desired option using JavaScript
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollBy(0, 10)", scrollableContainer); // Adjust the scroll amount as needed

        // Find and select the specific option (e.g., selecting 'Bar Chart')
        // Find and select the specific option (e.g., selecting 'Bar Chart')
        // Assuming properties is a Properties object loaded with your properties file

        WebElement barChartOption = driver.findElement(By.xpath("//span[contains(text(),'" + chartType + "')]"));
        Thread.sleep(2000);
        barChartOption.click();

        // Find the element by class name and data-ng-controller attribute
        WebElement parentElem = driver.findElement(By.cssSelector("div.ew-content-host[data-ng-controller='widget-editor.controllers.relocateController']"));

        // Find child elements under the parent element
        List<WebElement> childElements = parentElem.findElements(By.cssSelector("div.ew-panel"));
        for(WebElement child : childElements){

            //Find if a value under the legend is already selected then delete the value
            // Find the div element by its class name
            WebElement divElement = null;
            try {
                divElement = child.findElement(By.cssSelector("div.ew-i-rem.ew-i-act.btn--dark"));
            } catch (org.openqa.selenium.NoSuchElementException e) {
                // If the element is not found, the variable will remain null
            }

            // Check if the div element exists and perform a click action
            if (divElement != null) {
                divElement.click();
                System.out.println("Clicked the element");
            }

            //find the child elements of each child
            WebElement labelElement = child.findElement(By.cssSelector("div.ew-p-header"));
            String label = labelElement.getText();

            // Find the specified child element within the parent element
            WebElement addButt = child.findElement(By.cssSelector("div[class*='ew-p-add']"));
            Actions actions = new Actions(driver);
            actions.moveToElement(addButt).click().perform();
            //click on popup
            Thread.sleep(2000);
            WebElement innerPopup = driver.findElement(By.cssSelector("div.popup-inner"));

            // find from config which value to pickup
            String key = keyValueMap.containsKey(label) ? keyValueMap.get(label) : "";

            // Build the XPath based on the elementToSelect parameter
            String xpathExpression = "//span[@class='title' and @title='" + key + "']";
            Thread.sleep(2000);
            WebElement popupElem = innerPopup.findElement(By.xpath(xpathExpression));

            // Assuming "Brand" value is within a span with class "title"
            // Click on the "Brand" value
            Actions acts = new Actions(driver);
            acts.moveToElement(popupElem).click().perform();
        }
        Thread.sleep(5000);
        //click on apply
        driver.findElement(By.cssSelector("span[data-translate='we.apply']")).click();
        Thread.sleep(5000);
    }
}

