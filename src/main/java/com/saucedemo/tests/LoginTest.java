package com.saucedemo.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class LoginTest {
    public static void main(String[] args) {

        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        // List of username/password
        List<String[]> credentials = Arrays.asList(
                new String[]{"standard_user", "secret_sauce"},
                new String[]{"locked_out_user", "secret_sauce"},
                new String[]{"problem_user", "secret_sauce"},
                new String[]{"performance_glitch_user", "secret_sauce"}
        );

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20)); // increased wait

        String reportFile = "login_test_report.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {

            for (String[] cred : credentials) {
                String username = cred[0];
                String password = cred[1];

                try {
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
                        // Wait for URL to change to inventory.html → reliable for all users
                        wait.until(ExpectedConditions.urlContains("inventory.html"));
                        isLoginSuccess = true;
                    } catch (Exception e) {
                        // Login failed → get error message
                        try {
                            errorMessage = driver.findElement(By.cssSelector("h3[data-test='error']")).getText();
                        } catch (Exception ex) {
                            errorMessage = "Unknown error";
                        }
                        isLoginSuccess = false;
                    }

                    if (isLoginSuccess) {
                        String passMsg = "✅ PASS -> " + username + " : " + password;
                        System.out.println(passMsg);
                        writer.write(passMsg);
                        writer.newLine();

                        // Logout safely
                        try {
                            driver.findElement(By.id("react-burger-menu-btn")).click();
                            wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
                            driver.findElement(By.id("logout_sidebar_link")).click();
                        } catch (Exception logoutEx) {
                            String warnMsg = "⚠ WARNING: Logout failed for " + username + " : " + password
                                    + " -> " + logoutEx.getMessage();
                            System.out.println(warnMsg);
                            writer.write(warnMsg);
                            writer.newLine();
                        }

                    } else {
                        String failMsg = "❌ FAIL -> " + username + " : " + password + " | Error: " + errorMessage;
                        System.out.println(failMsg);
                        writer.write(failMsg);
                        writer.newLine();
                    }

                } catch (Exception e) {
                    String errMsg = "⚠ ERROR -> " + username + " : " + password + " -> " + e.getMessage();
                    System.out.println(errMsg);
                    writer.write(errMsg);
                    writer.newLine();
                }
            }

            System.out.println("\nTest results saved to " + reportFile);

        } catch (IOException e) {
            System.out.println("Error writing report file: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }
}
