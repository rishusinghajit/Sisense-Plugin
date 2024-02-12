package org.example;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.logging.Level;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.*;


import java.io.*;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ExportConsoleError {
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

        //delete the file if already present at desktop
        deleteExcelIfPresent();

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
            int fontSize = Integer.parseInt(properties.getProperty("fontSize"));

            // Click the login button
            loginButton.click();
            Thread.sleep(20000);


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

                //clear the console
                clearBrowserConsole(driver);

                for (WebElement elem : elementsInsideFolder) {

                    // try to find the folder name
                    WebElement folderNameElement = elem.findElement(By.cssSelector("a.li-title"));
                    String folderName = folderNameElement.getText();
                    System.out.println("=======Currently processing dashboard: " + folderName + " =======");

                    LogEntries beforeClearLogs = findConsoleLogs(driver);
                    //clear the console
                    clearBrowserConsole(driver);
                    elem.click();
                    LogEntries afterClearLogs = findConsoleLogs(driver);
                    // Sleep for a moment to allow the page to load (you can use WebDriverWait for a more robust solution)
                    try {
                        Thread.sleep(20000);
                        //code to find out the error widgets on this dashboards
                        List<WebElement> widgetElements = driver.findElements(By.cssSelector(".widget.columnar.narration-holder.narration-holder__dashboard.ui-draggable"));
                        List<WebElement> errorWidgets = driver.findElements(By.className("dashboard-error-wrapper"));
                        widgetElements.addAll(errorWidgets);
                        Thread.sleep(20000);

                        //try to find the console error if there are any
                        String consoleError = "";
                        consoleError = findFilteredConsoleLogs(beforeClearLogs, afterClearLogs);

                        String widgetType = "";
                        Duration timeoutDuration = Duration.ofSeconds(10);
                        WebDriverWait wait = new WebDriverWait(driver, timeoutDuration);

                        // Loop through the widget elements
                        for (WebElement widgetElement : widgetElements) {
                            wait.until(ExpectedConditions.visibilityOf(widgetElement));
                            wait.until(ExpectedConditions.elementToBeClickable(widgetElement));
                            widgetType = widgetElement.getAttribute("type");
                            String errorText = "";

                            try {

                                wait.until(ExpectedConditions.visibilityOf(widgetElement));
                                wait.until(ExpectedConditions.elementToBeClickable(widgetElement));
                                widgetType = widgetElement.getAttribute("type");
                                // Try to find the child element with data-widget-error-overlay
                                WebElement childElement = widgetElement.findElement(By.cssSelector("div[data-widget-error-overlay]"));

                                // Find the child element that matches either of the two specified types using a CSS selector
                                WebElement errorTextElement = childElement.findElement(By.cssSelector("div.info-text, div.info-text.long-text"));

                                //below error text will be used to determine if the error is registration or not.

                                errorText = errorTextElement.getAttribute("textContent");
                                writeToExcel(folderName, widgetType, errorText, consoleError, fontSize);

                            } catch (org.openqa.selenium.NoSuchElementException e) {

                                //try to find the error element here
                                try{
                                    errorText = widgetElement.findElement(By.className("info-box")).getText();
                                    writeToExcel(folderName, widgetType, errorText, consoleError, fontSize);
                                }catch(org.openqa.selenium.NoSuchElementException ex)
                                {
                                    writeToExcel(folderName, widgetType, errorText, consoleError, fontSize);
                                }
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

    private static void writeToExcel(String dashboardName, String chartName, String errorText, String consoleError, int fontSize) {
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
                headerRow.createCell(3).setCellValue("Console Error If Any");
            }

            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(dashboardName);
            row.createCell(1).setCellValue(chartName);
            row.createCell(2).setCellValue(errorText);
            row.createCell(3).setCellValue(consoleError);

            // Set the cell style to wrap text
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setWrapText(true);

            // Set border style for cells
            cellStyle.setBorderTop(BorderStyle.THICK);
            cellStyle.setBorderBottom(BorderStyle.THICK);
            cellStyle.setBorderLeft(BorderStyle.THICK);
            cellStyle.setBorderRight(BorderStyle.THICK);


            // Check for non-empty error or console error
            if (!errorText.isEmpty() || !consoleError.isEmpty()) {
                // Set red color for the entire row
                cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }

            // Create a font with the desired font size for column 4
            Font font = workbook.createFont();
            font.setFontHeightInPoints((short) fontSize); // Set the desired font size
            CellStyle column4Style = workbook.createCellStyle();
            column4Style.setFont(font);
            //column4Style.setWrapText(true);

            // Apply different styles for column 4
            Cell cell4 = row.getCell(3);
            if (cell4 == null) {
                cell4 = row.createCell(3);
            }
            cell4.setCellStyle(column4Style);

            for (int i = 0; i < 4; i++) {
                row.getCell(i).setCellStyle(cellStyle);
            }


            try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
                workbook.write(outputStream);
            }

            System.out.println("Data has been written to " + excelFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void clearBrowserConsole(WebDriver driver) {
        if (driver instanceof JavascriptExecutor) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("console.clear()");
        } else {
            throw new UnsupportedOperationException("The provided WebDriver instance does not support JavaScript execution.");
        }
    }

    private static void deleteExcelIfPresent(){
        String desktopPath = System.getProperty("user.home") + "/Desktop";
        String fileName = "output.xlsx";
        File file = new File(desktopPath, fileName);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println(fileName + " is deleted successfully.");
            } else {
                System.out.println("Failed to delete " + fileName);
            }
        } else {
            System.out.println(fileName + " does not exist on the desktop.");
        }
    }

    private static LogEntries findConsoleLogs(WebDriver driver)
    {
        return driver.manage().logs().get(LogType.BROWSER);
    }

    private static String findFilteredConsoleLogs(LogEntries logsBeforeClear, LogEntries logsAfterClear)
    {
        // Filter out logs that appeared after clearing the console
        List<LogEntry> filteredLogs = logsAfterClear.getAll().stream()
                .filter(log -> !logsBeforeClear.getAll().contains(log))
                .filter(log -> log.getLevel().equals(Level.SEVERE)) // Filter by Severe error logs
                .collect(Collectors.toList());

        // Create a concatenated string of the filtered logs
        StringBuilder concatenatedLogs = new StringBuilder();
        for (LogEntry log : filteredLogs) {
            concatenatedLogs.append(log.getMessage()).append("\n\n");
        }
        return concatenatedLogs.toString();
    }

    private static String canFindConsoleError(WebDriver driver)
    {
        // now try to find the logs
        // Get the logs from the browser console
        LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
        // Iterate through the log entries to find errors
        // Check if the console log has the error message

        // StringBuilder to store the concatenated error messages
        StringBuilder resultBuilder = new StringBuilder();

        for (LogEntry entry : logEntries) {
            if (entry.getLevel().equals(Level.SEVERE)) {
                resultBuilder.append(entry.getMessage()).append("\n\n");
            }
        }
        // Convert StringBuilder to a String
        return resultBuilder.toString();
    }
}
