package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.WebDriver;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.openqa.selenium.JavascriptExecutor;

public class ConsoleErrorChecker {

    public static void main(String[] args) {
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
            Thread.sleep(20000);

            //click automation folder
            // Find the element by its class name and click it
            // Find the element by its XPath and click it
            WebElement folderElement = driver.findElement(By.xpath("//a[@class='li-title' and contains(@data-ng-if, 'folder') and @data-qa='navver-item-title-text-link']"));
            folderElement.click();

            //now click the folder test 1 which has console error

            // Find the element by XPath and click it
            WebElement  element= driver.findElement(By.xpath("//a[@class='li-title' and text()='Test1']"));
            element.click();
            Thread.sleep(20000);



            // Output the result
            if (canFindConsoleError(driver)) {
                System.out.println("Console Error found in the console log. \n");
                // You can perform additional actions here if the error is found.
            } else {
                System.out.println("Console Error NOT found in the console log.");
            }

            // Clear the console
            clearBrowserConsole(driver);

        }catch (InterruptedException e) {
            //Throw runtime exception
            throw new RuntimeException(e);
        } finally {
            // Close the browser when done
            driver.quit();
        }
    }

    public static void clearBrowserConsole(WebDriver driver) {
        if (driver instanceof JavascriptExecutor) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("console.clear()");
        } else {
            throw new UnsupportedOperationException("The provided WebDriver instance does not support JavaScript execution.");
        }
    }

    public static boolean canFindConsoleError(WebDriver driver)
    {
        // now try to find the logs
        // Get the logs from the browser console
        LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
        // Iterate through the log entries to find errors
        // Check if the console log has the error message
        boolean errorFound = false;
        String actualError = "";
        for (LogEntry entry : logEntries) {
            if (entry.getMessage().contains("TypeError: e.checkLiense is not a function")) {
                return true;
            }
        }
        return false;
    }
}
