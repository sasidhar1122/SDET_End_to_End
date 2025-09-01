package com.saucedemo.tests;

import com.aventstack.extentreports.*;
import com.saucedemo.utils.ExtentReportManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;

public class LoginTest_with_TestNG {

    private WebDriver driver;
    private WebDriverWait wait;
    private static ExtentReports extent;
    private ExtentTest test;

    @BeforeSuite
    public void startReport() {
        extent = ExtentReportManager.getInstance();
    }

    @BeforeMethod
    public void setup() {
        WebDriverManager.chromedriver().setup();

        // Headless Chrome for CI/CD environments
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @DataProvider(name = "users", parallel = false)
    public Object[][] userData() {
        return new Object[][]{
                {"standard_user", "secret_sauce", true},
                {"locked_out_user", "secret_sauce", false}, // Expected fail
                {"problem_user", "secret_sauce", true},
                {"performance_glitch_user", "secret_sauce", true}
        };
    }

    @Test(dataProvider = "users")
    public void loginTest(String username, String password, boolean expectedSuccess) {
        test = extent.createTest("Login Test - " + username);

        driver.get("https://www.saucedemo.com/");
        driver.manage().deleteAllCookies();

        driver.findElement(By.id("user-name")).clear();
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("user-name")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("login-button")).click();

        boolean isLoginSuccess = false;
        String errorMessage = "";

        try {
            wait.until(ExpectedConditions.urlContains("inventory.html"));
            isLoginSuccess = true;
        } catch (Exception e) {
            try {
                errorMessage = driver.findElement(By.cssSelector("h3[data-test='error']")).getText();
            } catch (Exception ex) {
                errorMessage = "Unknown error";
            }
        }

        // ✅ Validate against expected outcome
        if (isLoginSuccess == expectedSuccess) {
            test.pass("✅ " + username + " -> Test passed as expected");
            System.out.println("✅ " + username + " -> Test passed as expected");

            if (isLoginSuccess) {
                try {
                    driver.findElement(By.id("react-burger-menu-btn")).click();
                    wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
                    driver.findElement(By.id("logout_sidebar_link")).click();
                } catch (Exception logoutEx) {
                    test.warning("⚠ Logout failed for " + username);
                }
            }
        } else {
            test.fail("❌ " + username + " -> Test failed. Expected: " + expectedSuccess +
                      " | Actual: " + isLoginSuccess + " | Error: " + errorMessage);
            Assert.fail("Login validation failed for " + username +
                        " | Expected success=" + expectedSuccess +
                        " | Actual success=" + isLoginSuccess +
                        " | Error: " + errorMessage);
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @AfterSuite
    public void flushReport() {
        if (extent != null) {
            extent.flush();
        }
    }
}
