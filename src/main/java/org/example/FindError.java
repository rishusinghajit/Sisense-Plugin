package org.example;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
public class FindError {
    public static void main(String[] args) throws InterruptedException {
        // Set the path to the Chrome WebDriver executable
        System.setProperty("webdriver.chrome.driver", "/Users/nkumar13/Downloads/chromedriver_mac64/chromedriver");

        // Create ChromeOptions object to configure browser settings
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized"); // Start Chrome in full screen mode

        // Initialize the WebDriver with the ChromeOptions
        WebDriver driver = new ChromeDriver(options);

        try {
            // Navigate to the login page of the website
            driver.get("http://35.154.46.138:30845/app/main/home");
            // Find the username and password input fields and the login button
            WebElement usernameField = driver.findElement(By.id("username"));
            WebElement passwordField = driver.findElement(By.id("password"));

            String targetText = "Login";
            WebElement loginButton = driver.findElement(By.xpath("//span[@class='btn__text' and text()='" + targetText + "']"));

            // Enter your username and password
            usernameField.sendKeys("shweta.agrawal@64sqs.in");
            passwordField.sendKeys("LAA7i10%DXvEe=Wc=xqG");

            // Click the login button
            loginButton.click();
            Thread.sleep(20000);

            // Find the element using the class name and other attributes
            WebElement linkElement = driver.findElement(By.cssSelector("a.li-title[data-qa='navver-item-title-text-link'][data-ng-href*='/app/main/dashboards']"));

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
                wait.until(ExpectedConditions.visibilityOf(widgetElement));
                widgetType = widgetElement.getAttribute("type");

                String message = "";

                try {
                    // Try to find the child element with data-widget-error-overlay
                    WebElement childElement = widgetElement.findElement(By.cssSelector("div[data-widget-error-overlay]"));

                    // Find the child element that matches either of the two specified types using a CSS selector
                    WebElement errorTextElement = childElement.findElement(By.cssSelector("div.info-text, div.info-text.long-text"));

                    String errorText = errorTextElement.getAttribute("textContent");
                    System.out.println(errorText);

                    //compare the error text with actual error text
                    String expectedErrorText = " widget type:" + widgetType+ " is not registered ";
                    if(errorText.equals(expectedErrorText)){
                        System.out.println("The error type in this widget of type:" +widgetType+" is REGISTERATION_ERROR");
                    }else{
                        System.out.println("The error type in this widget of type:" +widgetType+" is not a REGISTERATION_ERROR");
                    }
                }catch (org.openqa.selenium.NoSuchElementException e) {
                    message = "widget type:"+ widgetType + " is registered";
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
