package org.example;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class CheckAlertUsingJs {
    public static void main(String[] args) {
        // Set the path to your ChromeDriver executable
        System.setProperty("webdriver.chrome.driver", "/Users/nkumar13/Downloads/chromedriver_mac64/chromedriver");

        // Initialize the WebDriver
        WebDriver driver = new ChromeDriver();

        // Navigate to a web page (e.g., Google)
        driver.get("https://www.google.com");

        // Create a JavascriptExecutor object
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Execute JavaScript code to display an alert
        String message = "This is an alert message!";
        js.executeScript("alert(arguments[0]);", message);

        // Accept the alert
        //driver.switchTo().alert().accept();

        // Close the browser when done
        //driver.quit();
        System.out.println("we are done...");
    }
}
