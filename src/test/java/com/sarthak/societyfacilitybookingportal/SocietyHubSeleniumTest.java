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

        // 3. Open a facility with test slots
        driver.get("http://localhost:8081/slots?facilityId=7&date=2026-10-13");

        // 4. Select an available slot
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a.slot-card")
        )).click();

        // 5. Wait for booking page
        wait.until(ExpectedConditions.urlContains("/booking"));

        // 6. Confirm booking
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".confirm-booking-button")
        )).click();

        // 7. Verify booking submission
        WebElement successMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".success-header .page-title")
                )
        );

        assertTrue(
                successMessage.getText().contains(
                        "Your booking request is submitted."
                )
        );

        // 8. Capture the newly created booking ID
        String bookingId = driver.findElement(
                By.cssSelector(".booking-id strong")
        ).getText();

        // 9. Open My Bookings
        driver.get("http://localhost:8081/bookings");

        // 10. Find the exact booking that was just created
        WebElement bookingCard = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(
                                ".booking-card[data-booking-id='" + bookingId + "']"
                        )
                )
        );

        // 11. Cancel the same booking
        bookingCard.findElement(
                By.cssSelector(".cancel-button")
        ).click();

        // 12. Wait for the bookings page to reload
        wait.until(ExpectedConditions.urlContains("/bookings"));

        // 13. Verify the booking is cancelled
        By cancelledStatusLocator = By.cssSelector(
                ".booking-card[data-booking-id='" + bookingId + "'] .status"
        );

        wait.until(driver -> {
            try {
                WebElement element = driver.findElement(cancelledStatusLocator);
                return element.isDisplayed()
                        && element.getText().trim().equals("CANCELLED");
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                return false;
            }
        });
    }

    @Test
    void gymnasiumDynamicCapacityFlow() {
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

        // 3. Open Gymnasium slots
        driver.get("http://localhost:8081/slots?facilityId=8&date=2026-10-13");

        // 4. Verify Gymnasium capacity badge and occupancy/availability info
        WebElement pageTitle = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".page-title"))
        );
        assertTrue(pageTitle.getText().contains("Gymnasium"));

        WebElement capacityBadge = driver.findElement(By.cssSelector(".facility-capacity-badge"));
        assertTrue(capacityBadge.getText().contains("Capacity: 25"));

        WebElement slotCard = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a.slot-card"))
        );
        String initialSlotText = slotCard.getText();
        assertTrue(initialSlotText.contains("booked"));
        assertTrue(initialSlotText.contains("spots available") || initialSlotText.contains("spot available"));
        assertTrue(initialSlotText.contains("Book Now"));

        // 5. Select the Gymnasium slot and book
        slotCard.click();

        wait.until(ExpectedConditions.urlContains("/booking"));

        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".confirm-booking-button")
        )).click();

        // 6. Verify booking submission succeeds
        WebElement successMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".success-header .page-title")
                )
        );
        assertTrue(successMessage.getText().contains("Your booking request is submitted."));

        String bookingId = driver.findElement(
                By.cssSelector(".booking-id strong")
        ).getText();

        // 7. Verify Gymnasium slot availability page remains accessible
        driver.get("http://localhost:8081/slots?facilityId=8&date=2026-10-13");
        WebElement availableSlot = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.slot-card"))
        );
        assertTrue(availableSlot.getText().contains("booked"));

        // 8. Open My Bookings and cancel the booking
        driver.get("http://localhost:8081/bookings");

        WebElement bookingItem = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".booking-card[data-booking-id='" + bookingId + "']")
                )
        );

        bookingItem.findElement(By.cssSelector(".cancel-button")).click();

        wait.until(ExpectedConditions.urlContains("/bookings"));

        By cancelledStatusLocator = By.cssSelector(
                ".booking-card[data-booking-id='" + bookingId + "'] .status"
        );

        wait.until(driver -> {
            try {
                WebElement element = driver.findElement(cancelledStatusLocator);
                return element.isDisplayed()
                        && element.getText().trim().equals("CANCELLED");
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                return false;
            }
        });

        // 9. Verify Gymnasium slots still reflect availability
        driver.get("http://localhost:8081/slots?facilityId=8&date=2026-10-13");
        WebElement restoredSlot = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.slot-card"))
        );
        assertTrue(restoredSlot.getText().contains("spots available") || restoredSlot.getText().contains("spot available"));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}