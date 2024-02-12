package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;



public class TestFolders {

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



            // Set the path to the Chrome WebDriver executable
            System.setProperty("webdriver.chrome.driver", "/Users/nkumar13/Downloads/chromedriver_mac64/chromedriver");

            // Navigate to the login page of the website
            driver.get(properties.getProperty("url"));
            //driver.get("http://35.154.46.138:30845/app/main/home");
            // Find the username and password input fields and the login button
            WebElement usernameField = driver.findElement(By.id("username"));
            WebElement passwordField = driver.findElement(By.id("password"));

            String targetText = "Login";
            WebElement loginButton = driver.findElement(By.xpath("//span[@class='btn__text' and text()='" + targetText + "']"));

            // Enter your username and password
            //usernameField.sendKeys("shweta.agrawal@64sqs.in");
            //passwordField.sendKeys("LAA7i10%DXvEe=Wc=xqG");

            // Enter your username and password by reading from properties file
            usernameField.sendKeys(properties.getProperty("username"));
            passwordField.sendKeys(properties.getProperty("password"));

            // Click the login button
            loginButton.click();
            Thread.sleep(20000);

            //AFTER WE HAVE LOGGED IN
            // Find and click on elements with specified attributes
            List<WebElement> folderElements = driver.findElements(By.className("list-item-holder--folder"));

            for (WebElement folderElement : folderElements) {
                folderElement.click();
                // Sleep for a moment to allow the page to load (you can use WebDriverWait for a more robust solution)
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Find elements within the folder and print their tag names
                List<WebElement> elementsInsideFolder = folderElement.findElements(By.cssSelector("div.sub-holder-container[data-qa='navver-item-childs']"));
                for (WebElement elem : elementsInsideFolder) {
                    //elem.click();
                    // Sleep for a moment to allow the page to load (you can use WebDriverWait for a more robust solution)
                    /*try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    System.out.println("Element Tag Name: " + elem.getTagName());
                    System.out.println("Element Tag Name: " + elem.getAttribute("type"));
                }
            }


            // Get the count of the elements
            //int elementCount = elements.size();

            WebElement linkElement = driver.findElement(By.xpath("//a[@class='li-title' and @data-ng-href='/app/main/dashboards/64ba2ec673cdb5002ab02125']"));

            linkElement.click();
            Thread.sleep(20000);

            // Find all elements with the specified compound class name
            List<WebElement> widgetElements = driver.findElements(By.cssSelector(".widget.columnar.narration-holder.narration-holder__dashboard.ui-draggable"));
            Thread.sleep(20000);

            String widgetType = "";
            Duration timeoutDuration = Duration.ofSeconds(10);
            WebDriverWait wait = new WebDriverWait(driver, timeoutDuration);
            System.out.println("Total number of widgets are: "+ widgetElements.size());
            // Loop through the widget elements
            for (WebElement widgetElement : widgetElements)
            {
                if (widgetElement.isEnabled() || widgetElement.isDisplayed()) {
                    System.out.println("Widget element is clickable.");
                } else {
                    System.out.println("Widget element is not clickable.");
                }

                wait.until(ExpectedConditions.visibilityOf(widgetElement));
                wait.until(ExpectedConditions.elementToBeClickable(widgetElement));
                widgetType = widgetElement.getAttribute("type");

                // Create a JavascriptExecutor object
                String message = "";

                try {
                    // Try to find the child element with data-widget-error-overlay
                    WebElement childElement = widgetElement.findElement(By.cssSelector("div[data-widget-error-overlay]"));

                    /*
                    * Below code is to check if the error widget is for "registration error"

                    Thread.sleep(20000);

                    // Find the error text element to see which kind of error is this
                    WebElement widgetErrorTextElement = new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.info-text.long-text")));

                    // Get the text of the error widget element
                    String childText = widgetErrorTextElement.getText();

                    //compare the error text with actual error text
                    String expectedErrorText = "widget type:" + widgetType+ " is not registered";
                    if(childText.equals(expectedErrorText)){
                        System.out.println("The error type in this widget of type:" +widgetType+" is REGISTERATION_ERROR");
                    }else{
                        System.out.println("The error type in this widget of type:" +widgetType+" is not a REGISTERATION_ERROR");
                    }

                    // Print the text
                    System.out.println("Text of the child element: " + childText);

                     */

                    if (childElement.isDisplayed()) {
                        message = "widget type:"+ widgetType + " is not registered";
                        // show alert onto chrome
                        System.out.println(message);
                    } else {
                        message = "widget type:"+ widgetType + " is  registered";
                        // show alert onto chrome
                        System.out.println(message);
                    }
                }catch (org.openqa.selenium.NoSuchElementException e) {
                    message = "widget type:"+ widgetType + " is registered";
                    // show alert onto chrome
                    System.out.println(message);
                }

            }
        }catch (InterruptedException e) {
            //Throw runtime exception
            throw new RuntimeException(e);
        } finally {
            // Close the browser when done
            driver.quit();
        }

    }
}
