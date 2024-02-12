import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class GoogleSearchTest {
    public static void main(String[] args) {
        // Set the path to the Chrome WebDriver executable
        System.setProperty("webdriver.chrome.driver", "/Users/nkumar13/Downloads/chromedriver_mac64/chromedriver");

        // Create a new instance of the ChromeDriver
        WebDriver driver = new ChromeDriver();

        // Navigate to Google.com
        //driver.get("http://www.google.com/");
        driver.get("http://35.154.46.138:30845/app/main/dashboards/64ba2ec673cdb5002ab02125");

        // Perform additional actions here, if needed

        // Close the browser when done
        driver.quit();
    }
}
