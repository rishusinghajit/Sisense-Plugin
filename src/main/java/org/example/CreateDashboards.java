package org.example;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.JavascriptExecutor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

import java.io.*;
import java.time.Duration;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Properties;

public class CreateDashboards {
    public static void main(String[] args) throws InterruptedException {
        // Set the path to the Chrome WebDriver executable
        System.setProperty("webdriver.chrome.driver", "/Users/nkumar13/Downloads/chromedriver_mac64/chromedriver");

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
            Thread.sleep(10000);

            // Find and click on elements with specified attributes
            List<WebElement> folderElements = driver.findElements(By.className("list-item-holder--folder"));

            for (WebElement folderElement : folderElements) {
                String folder = folderElement.getText();
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
                    if (!elem.getText().equals(properties.getProperty("processFolder"))) continue;
                    elem.click();
                    Thread.sleep(2000);
                    // Find the element by XPath
                    WebElement addButton = driver.findElement(By.xpath("//span[@class='btn__text' and @data-translate='newWidget.selectData']"));
                    addButton.click();
                    Thread.sleep(1000);
                    // Locate the popup
                    WebElement popup = driver.findElement(By.cssSelector("div.popup-inner"));
                    // Assuming "Brand" value is within a span with class "title"
                    WebElement brandValue = popup.findElement(By.xpath(".//span[@title='Brand']"));
                    // Click on the "Brand" value
                    Actions actions = new Actions(driver);
                    actions.moveToElement(brandValue).click().perform();
                    Thread.sleep(1000);
                    // Find the element by class name and data-translate attribute
                    WebElement advanceConfigElement = driver.findElement(By.cssSelector("div.control-freak[data-translate='newWidget.advanced']"));
                    advanceConfigElement.click();
                    Thread.sleep(1000);

                    // Find and click the dropdown toggle element
                    WebElement dropdownToggle = driver.findElement(By.cssSelector("span.we-t-toggler[data-icon-name='\\'general-arrow-down\\'']"));
                    dropdownToggle.click();

                    // Find the scrollable container
                    WebElement scrollableContainer = driver.findElement(By.cssSelector("div.wt-items-host.content"));
                    // Scroll down to the desired option using JavaScript
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    js.executeScript("arguments[0].scrollBy(0, 1000)", scrollableContainer); // Adjust the scroll amount as needed

                    // Find and select the specific option (e.g., selecting 'Bar Chart')
                    WebElement barChartOption = driver.findElement(By.xpath("//span[contains(text(), 'QBeeQ Radial Bar')]"));
                    barChartOption.click();

                    // Find the element by class name and data-ng-controller attribute
                    WebElement parentElem = driver.findElement(By.cssSelector("div.ew-content-host[data-ng-controller='widget-editor.controllers.relocateController']"));


                    // Find child elements under the parent element
                    List<WebElement> childElements = parentElem.findElements(By.cssSelector("div.ew-panel"));
                    for(WebElement child : childElements){
                        //find the child elements of each child
                        WebElement labelElement = child.findElement(By.cssSelector("div.ew-p-header"));
                        String label = labelElement.getText();

                        // Find the specified child element within the parent element
                        WebElement addButt = child.findElement(By.cssSelector("div[class*='ew-p-add']"));
                        addButt.click();

                        //click on popup
                        WebElement innerPopup = driver.findElement(By.cssSelector("div.popup-inner"));

                        // find from config which value to pickup
                        String key = "";

                        //fetch from json file
                        JSONParser parser = new JSONParser();
                        try {
                            Object obj = parser.parse(new FileReader("src/main/resources/config.json"));

                            JSONObject jsonObject = (JSONObject) obj;

                            // Retrieve other properties similarly
                            key = (String) jsonObject.get(label);

                            // Print or use other properties as needed
                        } catch (IOException | ParseException e) {
                            e.printStackTrace();
                        }

                        // Build the XPath based on the elementToSelect parameter
                        String xpathExpression = "//span[@class='title' and @title='" + key + "']";
                        WebElement popupElem = innerPopup.findElement(By.xpath(xpathExpression));


                        // Assuming "Brand" value is within a span with class "title"
                        // Click on the "Brand" value
                        Actions acts = new Actions(driver);
                        acts.moveToElement(popupElem).click().perform();
                    }

                    //click on apply
                    driver.findElement(By.cssSelector("span[data-translate='we.apply']")).click();
                    Thread.sleep(1000);
                }


            }
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        } finally {
            // Close the browser when done
            driver.quit();
        }
    }
}
