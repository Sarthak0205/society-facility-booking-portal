package com.sarthak.societyfacilitybookingportal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.WebElement;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SocietyHubSeleniumTest {

    private WebDriver driver;

    @Test
    void facilitiesPageLoads() {
        driver = new ChromeDriver();

        driver.get("http://localhost:8081/facilities");

        assertTrue(driver.getTitle().contains("SocietyHub"));
    }

    @Test
    void userCanCreateBooking() {
        driver = new ChromeDriver();

        String email = System.getProperty("test.email");
        String password = System.getProperty("test.password");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 1. Login
        driver.get("http://localhost:8081/users/login");

        driver.findElement(By.id("email"))
                .sendKeys(email);

        driver.findElement(By.id("password"))
                .sendKeys(password);

        driver.findElement(By.cssSelector("button[type='submit']"))
                .click();

        // 2. Wait for successful login
        wait.until(ExpectedConditions.urlContains("/facilities"));

        driver.get("http://localhost:8081/slots?facilityId=7&date=2026-09-05");

        // 5. Wait for slots page
        wait.until(ExpectedConditions.urlContains("/slots"));

        // 6. Select an available slot
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a.slot-card")
        )).click();

        // 7. Wait for booking page
        wait.until(ExpectedConditions.urlContains("/booking"));

        // 8. Confirm booking
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".confirm-booking-button")
        )).click();

        // 9. Verify booking submission
        WebElement successMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".success-header .page-title")
                )
        );

        assertTrue(
                successMessage.getText().contains("Your booking request is submitted.")
        );
    }
    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}