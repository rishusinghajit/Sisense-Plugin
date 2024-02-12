import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.chrome.ChromeOptions;
import java.time.Duration;
import java.util.List;

public class TestingOfFolderLevelDashboards {

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
                List<WebElement> elementsInsideFolder = folderElement.findElements(By.cssSelector("div[data-navver-tree-dashboard]"));
                for (WebElement elem : elementsInsideFolder) {

                    // try to find the folder name
                    //findElement(By.cssSelector("a.li-title"));
                    WebElement folderNameElement = elem.findElement(By.cssSelector("a.li-title"));
                    String folderName = folderNameElement.getText();
                    System.out.println("=======Currently processing dashboard: " + folderName + " =======");
                    elem.click();
                    // Sleep for a moment to allow the page to load (you can use WebDriverWait for a more robust solution)
                    try {
                        Thread.sleep(20000);
                        //code to find out the error widgets on this dashboards
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
                            String message = "";

                            try {
                                // Try to find the child element with data-widget-error-overlay
                                WebElement childElement = widgetElement.findElement(By.cssSelector("div[data-widget-error-overlay]"));

                                // Find the child element that matches either of the two specified types using a CSS selector
                                WebElement errorTextElement = childElement.findElement(By.cssSelector("div.info-text, div.info-text.long-text"));

                                //below error text will be used to determine if the error is registration or not.
                                String errorText = errorTextElement.getAttribute("textContent");
                                System.out.println("Dashboard Name: " + folderName + "  Chart Name: " + widgetType + "  Error If Any: " + errorText);
                            }catch (org.openqa.selenium.NoSuchElementException e) {
                                message = "widget type:"+ widgetType + " is registered";
                                // show alert onto chrome
                                System.out.println(message);
                            }

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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
