package org.example;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ApplyFIlter {
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

            // Perform hover action
            Actions actions = new Actions(driver);

            for (WebElement folderElement : folderElements) {
                String folder = "";
                try{
                    folder = folderElement.getText();

                }catch(StaleElementReferenceException e){
                    e.printStackTrace();
                    // Log or print the exception message
                    System.out.println("Stale Element Exception occurred: " + e.getMessage());
                    // Continue with the loop
                    break;
                }

                if (!folder.equals(properties.getProperty("folder"))) continue;

                actions.moveToElement(folderElement).perform();

                // Find and click on the three-dot menu icon within the hovered element
                WebElement threeDotMenuIcon = folderElement.findElement(By.cssSelector("button.navver-menu"));
                threeDotMenuIcon.click();


                // Initialize ChromeDriver
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

                // Wait for the menu content to be visible
                WebElement menuContent = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("menu-content")));
                // Find all menu items within the menu content
                // This assumes all menu items have the class name "menu-item"
                // Adjust the locator according to your specific scenario
                // Get the second menu item and click it
                WebElement secondMenuItem = menuContent.findElements(By.className("menu-item")).get(1);
                secondMenuItem.click();

                // Wait for the element to be visible
                WebElement dataSourceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".ds-text-holder > div")));

                // Click on the element
                dataSourceElement.click();

                // Wait for the datasources-container to be visible
                WebElement datasourcesContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("datasources-container")));

                // Using XPath to find elements with the desired placeholder text
                String datasourceName = properties.getProperty("datasource");
                WebElement dataSourceSelected = datasourcesContainer.findElement(By.xpath("//div[@class='datasource-caption' and text()='" + datasourceName + "']"));

                // Click on the element once found
                dataSourceSelected.click();

                // Wait for the "Create" button to be clickable
                WebElement createButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn.btn-ok")));

                // Wait for the input field to be visible
                WebElement titleInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.title-input-holder input.title-input")));

                // Clear any existing text in the input field (optional)
                Thread.sleep(1000);
                titleInput.clear();
                Thread.sleep(2000);

                // Fill in the text "Sample Dashboard"
                String dashboardTitle = properties.getProperty("dashboardName");
                titleInput.sendKeys(dashboardTitle);
                Thread.sleep(1000);

                // Click the "Create" button
                createButton.click();
                Thread.sleep(1000);

                //after dashboard is created, lets create the widgets required from here.
                CreateMultipleWidgets(driver, properties);

            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            // Close the browser when done
            driver.quit();
        }
    }// end of main

    private static void createOrModifyWidgets(WebDriver driver,
                                              String chartType,
                                              Map<String, String> keyValueMap,
                                              Properties properties) throws InterruptedException {
        WebElement addButton;
        try{
            //when there is NO widget then follow this path
            addButton = driver.findElement(By.xpath("//span[@class='btn__text' and @data-translate='newWidget.selectData']"));
            addButton.click();
            Thread.sleep(5000);

            //try to select any item from the list such as brand for e,g
            WebElement popup = driver.findElement(By.cssSelector("div.popup-inner"));

            // Wait for the popup to appear and then find the first item in the list
            WebElement firstItem = driver.findElement(By.cssSelector(".uc-db-table .uc-db-column"));
            // Click on the first value, so that you can see the advance configuration
            Actions actions = new Actions(driver);
            actions.moveToElement(firstItem).click().perform();

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

        //Filtering Logic here: Before clicking on apply , go to filter tab and click on this

        // Locate the toggle filter element using its unique attribute
        WebElement toggleFilterElement = driver.findElement(By.cssSelector("div.tab-action[data-ng-click='toggleFilters()']"));

        // Click on the toggle filter element
        toggleFilterElement.click();

        Thread.sleep(1000);

        // Wait for the "Add" button to be clickable under "Widget Filters"
        WebElement filterAddButton = driver.findElement(By.xpath("//div[text()='Widget Filters']/following-sibling::div[@class='fs-add']"));

        // Click on the "Add" button
        filterAddButton.click();

        //Now choose the filtering element, let say "Category"
        //try to select any item from the list such as brand for e,g
        WebElement filterPopup = driver.findElement(By.cssSelector("div.popup-inner"));
        Thread.sleep(500);

        // find from config which value to pickup
        String filterValue = keyValueMap.containsKey("filter") ? keyValueMap.get("filter") : "";

        WebElement categoryElement = filterPopup.findElement(By.xpath("//span[@title='" + filterValue + "']"));

        // Click on the first value, so that you can see the advance configuration
        Actions actions = new Actions(driver);
        actions.moveToElement(categoryElement).click().perform();
        Thread.sleep(500);

        //now find the first list item from the list
        // Find the element by class name
        WebElement listElements = driver.findElement(By.className("uc-ms-list"));
        Thread.sleep(2000);

        // Find the first child element by class name within the parent element
        WebElement firstListItem = listElements.findElement(By.className("list-item"));

        // Find the checkbox button element within the first list-item
        WebElement checkboxButton = firstListItem.findElement(By.cssSelector("button[data-qa='checker-button']"));
        checkboxButton.click();

        // Find the "OK" element by class name and click on it
        WebElement okButton = driver.findElement(By.className("uc-ok"));
        okButton.click();
        Thread.sleep(1000); // wait for the filter to get applied

        //After filtering, click on apply
        WebElement applyButton = driver.findElement(By.cssSelector("span[data-translate='we.apply']"));
        applyButton.click();
        Thread.sleep(5000);
    }

    public static void CreateMultipleWidgets(WebDriver driver, Properties properties) throws InterruptedException {
        // Find and click on elements with specified attributes
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
                    createOrModifyWidgets(driver, chartName, chartValues, properties);

                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
