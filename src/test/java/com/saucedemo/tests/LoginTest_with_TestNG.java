package com.saucedemo.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
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

    @BeforeMethod
    public void setup() {
        WebDriverManager.chromedriver().setup();

        // Headless Chrome for CI environments
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @DataProvider(name = "users", parallel = false)
    public Object[][] userData() {
        return new Object[][]{
                {"standard_user", "secret_sauce"},
                {"locked_out_user", "secret_sauce"},
                {"problem_user", "secret_sauce"},
                {"performance_glitch_user", "secret_sauce"}
        };
    }

    @Test(dataProvider = "users")
    public void loginTest(String username, String password) {

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

        if (isLoginSuccess) {
            System.out.println("✅ PASS -> " + username + " : " + password);
            // Logout for next iteration
            try {
                driver.findElement(By.id("react-burger-menu-btn")).click();
                wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
                driver.findElement(By.id("logout_sidebar_link")).click();
            } catch (Exception logoutEx) {
                System.out.println("⚠ WARNING: Logout failed for " + username);
            }
        } else {
            System.out.println("❌ FAIL -> " + username + " : " + password + " | Error: " + errorMessage);
            Assert.fail("Login failed for " + username + " : " + password + " | Error: " + errorMessage);
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}
