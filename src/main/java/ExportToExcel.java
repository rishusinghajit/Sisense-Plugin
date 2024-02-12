import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.*;
import java.time.Duration;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Properties;

public class ExportToExcel {
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
            Thread.sleep(20000);

            // Find and click on elements with specified attributes
            List<WebElement> folderElements = driver.findElements(By.className("list-item-holder--folder"));

            for (WebElement folderElement : folderElements) {
                String folder = folderElement.getText(); //automation, test, plugin
                if(!folder.equals(properties.getProperty("folder"))) continue;

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

                        // Loop through the widget elements
                        for (WebElement widgetElement : widgetElements) {
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
                                writeToExcel(folderName, widgetType, errorText);
                            } catch (org.openqa.selenium.NoSuchElementException e) {
                                message = "widget type:" + widgetType + " is registered";
                                writeToExcel(folderName, widgetType, message);
                            }

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (InterruptedException e) {
            //Throw runtime exception
            throw new RuntimeException(e);
        } finally {
            // Close the browser when done
            driver.quit();
        }

    }

    private static void writeToExcel(String dashboardName, String chartName, String errorText) {
        // Set the path to the output Excel file
        String excelFilePath = System.getProperty("user.home") + "/Desktop/output.xlsx";
        File file = new File(excelFilePath);

        try {
            Workbook workbook;
            if (file.exists()) {
                FileInputStream inputStream = new FileInputStream(file);
                workbook = new XSSFWorkbook(inputStream);
            } else {
                workbook = new XSSFWorkbook();
            }

            Sheet sheet = workbook.getSheet("Dashboard Data");
            if (sheet == null) {
                sheet = workbook.createSheet("Dashboard Data");
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Dashboard Name");
                headerRow.createCell(1).setCellValue("Chart Name");
                headerRow.createCell(2).setCellValue("Error If Any");
            }

            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(dashboardName);
            row.createCell(1).setCellValue(chartName);
            row.createCell(2).setCellValue(errorText);

            try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
                workbook.write(outputStream);
            }

            System.out.println("Data has been written to " + excelFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

