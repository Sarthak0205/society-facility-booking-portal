package com.sarthak.societyfacilitybookingportal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SocietyHubSeleniumTest {

    private WebDriver driver;

    @Test
    void facilitiesPageLoads() {
        driver = new ChromeDriver();

        driver.get("http://localhost:8081/facilities");

        assertTrue(driver.getTitle().contains("SocietyHub"));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}