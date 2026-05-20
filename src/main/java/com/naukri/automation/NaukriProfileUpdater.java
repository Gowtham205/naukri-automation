package com.naukri.automation;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * NaukriProfileUpdater
 * <p>
 * Opens Naukri, logs in, navigates to the profile page and clicks Save
 * without making any real changes. This updates the "Last Updated" timestamp,
 * which pushes the profile higher in recruiter searches.
 * <p>
 * Credentials are read from environment variables (never hardcoded):
 * NAUKRI_EMAIL    – your registered Naukri email
 * NAUKRI_PASSWORD – your Naukri password
 */
public class NaukriProfileUpdater {

    private static final Logger log = LoggerFactory.getLogger(NaukriProfileUpdater.class);

    // ── URLs ──────────────────────────────────────────────────────────────────
    private static final String NAUKRI_HOME    = "https://www.naukri.com";
    private static final String LOGIN_URL      = "https://www.naukri.com/nlogin/login";
    private static final String PROFILE_URL    = "https://www.naukri.com/mnjuser/profile";
    private static final String LOGOUT_URL = "https://www.naukri.com/nlogin/logout";

    // ── Timeouts ──────────────────────────────────────────────────────────────
    private static final Duration PAGE_WAIT    = Duration.ofSeconds(20);
    private static final Duration ELEMENT_WAIT = Duration.ofSeconds(15);

    // ── Selectors (update these if Naukri changes its DOM) ────────────────────
    // Login page
    private static final String SEL_EMAIL_INPUT    = "input[placeholder='Enter Email ID / Username']";
    private static final String SEL_PASSWORD_INPUT = "input[placeholder='Enter Password']";
    private static final String SEL_LOGIN_BUTTON   = "button[type='submit']";

    // Profile page — "Resume headline" section Save button
    // Naukri has multiple save buttons; we target the one inside the headline widget
    private static final String SEL_HEADLINE_EDIT  = "em.icon.edit";          // pencil icon to open edit
    private static final String SEL_SAVE_BUTTON     = "saveBasicDetailsBtn";   // save inside the widget

    // ── Random delay helper ───────────────────────────────────────────────────
    private static final Random RNG = new Random();

    public static void main(String[] args) {
        log.info("=== Naukri Profile Updater started at {} ===",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // Read credentials from environment
        String email    = System.getenv("NAUKRI_EMAIL");
        String password = System.getenv("NAUKRI_PASSWORD");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            log.error("NAUKRI_EMAIL or NAUKRI_PASSWORD environment variable is not set. Exiting.");
            System.exit(1);
        }

        WebDriver driver = null;
        try {
            driver = buildDriver();
            WebDriverWait wait = new WebDriverWait(driver, ELEMENT_WAIT);

            step1_login(driver, wait, email, password);
            step2_openProfileEditor(driver, wait);
            step3_saveProfile(driver, wait);
            step3b_closeSuccessPopup(driver, wait);
            step4_logout(driver, wait);

            log.info("✅  Profile updated successfully. Timestamp refreshed on Naukri.");

        } catch (Exception e) {
            log.error("❌  Automation failed: {}", e.getMessage(), e);
            System.exit(2);
        } finally {
            if (driver != null) {
                driver.quit();
                log.info("Browser closed.");
            }
        }
    }

    // ── Driver setup ──────────────────────────────────────────────────────────

    private static WebDriver buildDriver() {
        log.info("Setting up headless Chrome...");
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        // Headless mode — required on servers with no display
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-blink-features=AutomationControlled");

        // Mimic a real browser user-agent to reduce bot-detection risk
        options.addArguments(
            "--user-agent=Mozilla/5.0 (X11; Linux x86_64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/122.0.0.0 Safari/537.36"
        );

        // Hide the "Chrome is being controlled by automated software" bar
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);

        ChromeDriver driver = new ChromeDriver(options);

        // Remove the `navigator.webdriver` flag that bot-detection scripts check for
        ((JavascriptExecutor) driver).executeScript(
            "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})"
        );

        driver.manage().timeouts().pageLoadTimeout(PAGE_WAIT);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        log.info("Chrome driver ready.");
        return driver;
    }

    // ── Step 1: Login ─────────────────────────────────────────────────────────

    /*private static void step1_login(WebDriver driver, WebDriverWait wait,
                                     String email, String password) throws InterruptedException {
        log.info("Navigating to login page...");
        driver.get(LOGIN_URL);
        humanDelay(2000, 3000);

        // Fill email
        WebElement emailField = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector(SEL_EMAIL_INPUT))
        );
        emailField.clear();
        typeSlowly(emailField, email);
        humanDelay(500, 1000);

        // Fill password
        WebElement passField = driver.findElement(By.cssSelector(SEL_PASSWORD_INPUT));
        passField.clear();
        typeSlowly(passField, password);
        humanDelay(700, 1200);

        // Click login
        WebElement loginBtn = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector(SEL_LOGIN_BUTTON))
        );
        loginBtn.click();
        log.info("Login submitted. Waiting for home page...");

        // Wait until URL changes away from login page
        wait.until(ExpectedConditions.not(
            ExpectedConditions.urlContains("nlogin")
        ));
        humanDelay(2000, 3500);
        log.info("Logged in. Current URL: {}", driver.getCurrentUrl());
    }*/

    private static void step1_login(WebDriver driver, WebDriverWait wait,
                                    String email, String password) throws InterruptedException {
        log.info("Navigating to Naukri homepage...");
        driver.get(NAUKRI_HOME);
        humanDelay(3000, 4000);

        // Click the Login button on homepage
        log.info("Looking for Login button on homepage...");
        String[] loginBtnSelectors = {
                "a[href*='login']",
                "a.login",
                "button.login",
                "a[title='Login']",
                "li.login a",
                "a[data-ga-track*='login']"
        };

        boolean loginClicked = false;
        for (String sel : loginBtnSelectors) {
            try {
                WebElement loginLink = wait.until(
                        ExpectedConditions.elementToBeClickable(By.cssSelector(sel))
                );
                scrollToElement(driver, loginLink);
                humanDelay(500, 1000);
                loginLink.click();
                log.info("Clicked login button with selector: {}", sel);
                loginClicked = true;
                humanDelay(2000, 3000);
                break;
            } catch (TimeoutException e) {
                log.debug("Login button not found with: {}", sel);
            }
        }

        if (!loginClicked) {
            throw new RuntimeException("Could not find Login button on homepage.");
        }

        // Fill email in the modal that appears
        log.info("Filling email...");
        String[] emailSelectors = {
                "input[type='text']",
                "input[placeholder*='Email']",
                "input[placeholder*='email']",
                "input[placeholder*='Username']",
                "input[name='username']"
        };

        WebElement emailField = null;
        for (String sel : emailSelectors) {
            try {
                emailField = wait.until(
                        ExpectedConditions.elementToBeClickable(By.cssSelector(sel))
                );
                log.info("Found email field: {}", sel);
                break;
            } catch (TimeoutException e) {
                log.debug("Email selector not found: {}", sel);
            }
        }

        if (emailField == null) throw new RuntimeException("Email field not found.");
        emailField.clear();
        typeSlowly(emailField, email);
        humanDelay(500, 1000);

        // Fill password
        log.info("Filling password...");
        WebElement passField = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='password']"))
        );
        passField.clear();
        typeSlowly(passField, password);
        humanDelay(700, 1200);

        // Click Sign In inside the modal
        log.info("Submitting login...");
        String[] submitSelectors = {
                "button[type='submit']",
                "button.blue-btn",
                "button[class*='login']",
                "input[type='submit']"
        };

        boolean submitted = false;
        for (String sel : submitSelectors) {
            try {
                WebElement submitBtn = wait.until(
                        ExpectedConditions.elementToBeClickable(By.cssSelector(sel))
                );
                submitBtn.click();
                log.info("Clicked submit with selector: {}", sel);
                submitted = true;
                break;
            } catch (TimeoutException e) {
                log.debug("Submit selector not found: {}", sel);
            }
        }

        if (!submitted) throw new RuntimeException("Could not click Sign In button.");

        // Wait until logged in — homepage URL stays same but page content changes
        log.info("Waiting for login to complete...");
        humanDelay(4000, 5000); // give it time to process

        // Verify login by checking page source for logged-in indicators
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("div.nI-gNb-drawer__bars, a[href*='mnjuser/profile'], div[class*='user'], .user-name")
                ),
                ExpectedConditions.titleContains("Jobs")
        ));

        log.info("Logged in successfully. URL: {}", driver.getCurrentUrl());
    }

    // ── Step 2: Navigate to profile via UI ────────────────────────────────────

    private static void step2_openProfileEditor(WebDriver driver, WebDriverWait wait)
            throws InterruptedException {
        log.info("Checking for chatbot overlay...");

        // Close chatbot overlay if present — it blocks all clicks
        try {
            WebElement chatbotOverlay = driver.findElement(
                    By.cssSelector("div.chatbot_Overlay.show")
            );
            // Click outside the overlay to dismiss it
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].style.display='none';", chatbotOverlay
            );
            log.info("Dismissed chatbot overlay.");
            humanDelay(1000, 1500);
        } catch (NoSuchElementException e) {
            log.debug("No chatbot overlay found.");
        }

        // Also close any other overlays that might be present
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var overlays = document.querySelectorAll('[class*=\"Overlay\"]');" +
                            "overlays.forEach(function(el){ el.style.display='none'; });"
            );
            log.debug("Cleared all overlay elements.");
            humanDelay(500, 800);
        } catch (Exception e) {
            log.debug("No overlays to clear.");
        }

        log.info("Looking for profile icon/menu in navbar...");

        // Now click profile icon using JavaScript to avoid any remaining intercepts
        String[] profileIconSelectors = {
                "div.nI-gNb-drawer__bars",
                "span[class*='username']",
                "div[class*='user-name']",
                "a[href*='mnjuser']"
        };

        for (String sel : profileIconSelectors) {
            try {
                WebElement icon = wait.until(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(sel))
                );
                scrollToElement(driver, icon);
                humanDelay(800, 1200);
                // Use JS click — bypasses overlay interception
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", icon);
                log.info("Clicked profile icon with selector: {}", sel);
                humanDelay(1500, 2000);
                break;
            } catch (TimeoutException e) {
                log.debug("Profile icon not found: {}", sel);
            }
        }

        // Click "View & Update Profile" from the dropdown
        String[] viewProfileSelectors = {
                "a[href*='mnjuser/profile']",
                "a[title*='profile']",
                "a[title*='Profile']",
                "li a[href*='profile']",
                "a[class*='profile']"
        };

        for (String sel : viewProfileSelectors) {
            try {
                WebElement viewProfile = wait.until(
                        ExpectedConditions.elementToBeClickable(By.cssSelector(sel))
                );
                humanDelay(500, 800);
                viewProfile.click();
                log.info("Clicked View Profile with selector: {}", sel);
                humanDelay(3000, 4000);
                break;
            } catch (TimeoutException e) {
                log.debug("View profile not found: {}", sel);
            }
        }

        log.info("Current URL after profile nav: {}", driver.getCurrentUrl());

        // Now click the edit icon on the Resume Headline section
        log.info("Looking for edit icon on profile page...");
        String[] editSelectors = {
                "em.icon.edit",
                "em[class*='edit']",
                "div.hdn em",
                "span[class*='edit']",
                "[class*='editIcon']",
                "div.widgetHead em"
        };

        boolean clicked = false;
        for (String sel : editSelectors) {
            try {
                WebElement editIcon = wait.until(
                        ExpectedConditions.elementToBeClickable(By.cssSelector(sel))
                );
                scrollToElement(driver, editIcon);
                humanDelay(800, 1200);
                editIcon.click();
                log.info("Clicked edit icon with selector: {}", sel);
                clicked = true;
                humanDelay(2000, 2500);
                break;
            } catch (TimeoutException e) {
                log.debug("Edit selector not found: {}", sel);
            }
        }

        if (!clicked) {
            log.warn("No edit icon found — will attempt save directly...");
        }
    }

// ── Step 3: Save via UI ───────────────────────────────────────────────────

    private static void step3_saveProfile(WebDriver driver, WebDriverWait wait)
            throws InterruptedException {
        log.info("Looking for Save button...");

        boolean saved = false;
        try {
            WebElement saveBtn = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id(SEL_SAVE_BUTTON))
            );
            scrollToElement(driver, saveBtn);
            humanDelay(600, 1000);
            saveBtn.click();
            log.info("Clicked Save with selector: {}", SEL_SAVE_BUTTON);
            saved = true;
            humanDelay(3000, 4000);
        } catch (TimeoutException e) {
            log.debug("Save selector not found: {}", SEL_SAVE_BUTTON);
        }

        if (!saved) {
            log.warn("CSS selectors failed, trying JavaScript save...");
            saved = tryJsSave(driver);
        }

        if (!saved) {
            throw new RuntimeException("Could not find or click Save button.");
        }

        log.info("Profile saved successfully!");
    }

    // ── Step 3b: Close success popup after save ───────────────────────────────

    private static void step3b_closeSuccessPopup(WebDriver driver, WebDriverWait wait)
            throws InterruptedException {
        log.info("Closing profile update success popup...");
        humanDelay(2000, 2500); // wait for popup to fully render

        boolean closed = false;

        // Try JavaScript click first — more reliable for layered popups
        try {
            WebElement closeBtn = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("div.profileUpdatedProLayer div.crossLayer span.icon")
                    )
            );
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", closeBtn);
            log.info("Closed popup via JS click on span.icon inside profileUpdatedProLayer.");
            closed = true;
            humanDelay(1500, 2000);
        } catch (TimeoutException e) {
            log.debug("Primary popup selector not found.");
        }

        // Fallback 1 — click the crossLayer div itself via JS
        if (!closed) {
            try {
                WebElement crossLayer = driver.findElement(By.cssSelector("div.crossLayer"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", crossLayer);
                log.info("Closed popup via JS click on crossLayer div.");
                closed = true;
                humanDelay(1500, 2000);
            } catch (NoSuchElementException e) {
                log.debug("crossLayer div not found.");
            }
        }

        // Fallback 2 — click the ltLayer overlay to dismiss
        if (!closed) {
            try {
                WebElement overlay = driver.findElement(By.cssSelector("div.ltLayer"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", overlay);
                log.info("Closed popup by clicking overlay ltLayer.");
                closed = true;
                humanDelay(1500, 2000);
            } catch (NoSuchElementException e) {
                log.debug("ltLayer not found.");
            }
        }

        // Fallback 3 — ESC key
        if (!closed) {
            driver.findElement(By.cssSelector("body")).sendKeys(Keys.ESCAPE);
            log.info("Sent ESC key to dismiss popup.");
            humanDelay(1000, 1500);
        }
    }

// ── Step 4: Logout via UI ─────────────────────────────────────────────────

    private static void step4_logout(WebDriver driver, WebDriverWait wait)
            throws InterruptedException {
        log.info("Logging out via UI...");

        // Click profile icon/avatar again to open dropdown
        String[] profileIconSelectors = {
                "div.nI-gNb-drawer__bars",
                "span[class*='username']",
                "div[class*='user-name']",
                "div.user-name",
                "span.nI-gNb-menuBtn"
        };

        for (String sel : profileIconSelectors) {
            try {
                WebElement profileIcon = wait.until(
                        ExpectedConditions.elementToBeClickable(By.cssSelector(sel))
                );
                scrollToElement(driver, profileIcon);
                humanDelay(800, 1200);
                profileIcon.click();
                log.info("Clicked profile menu for logout: {}", sel);
                humanDelay(1500, 2000);
                break;
            } catch (TimeoutException e) {
                log.debug("Profile icon not found: {}", sel);
            }
        }

        // Click Logout from dropdown
        String[] logoutSelectors = {
                "a[title='Logout']",
                "a[href*='logout']",
                "li.logout a",
                "button[class*='logout']",
                "a[class*='logout']"
        };

        boolean loggedOut = false;
        for (String sel : logoutSelectors) {
            try {
                WebElement logoutBtn = wait.until(
                        ExpectedConditions.elementToBeClickable(By.cssSelector(sel))
                );
                humanDelay(500, 800);
                logoutBtn.click();
                log.info("Clicked logout with selector: {}", sel);
                loggedOut = true;
                humanDelay(2000, 3000);
                break;
            } catch (TimeoutException e) {
                log.debug("Logout selector not found: {}", sel);
            }
        }

        if (!loggedOut) {
            // Fallback — find logout via link text
            try {
                WebElement logoutLink = driver.findElement(
                        By.xpath("//a[contains(text(),'Logout') or contains(text(),'logout') or contains(text(),'Sign Out')]")
                );
                logoutLink.click();
                log.info("Clicked logout via XPath text match.");
                humanDelay(2000, 3000);
            } catch (NoSuchElementException e) {
                log.warn("Could not find logout button — skipping logout.");
            }
        }

        log.info("Logout complete.");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Types text character by character with small random delays — mimics human typing.
     */
    private static void typeSlowly(WebElement element, String text) throws InterruptedException {
        for (char c : text.toCharArray()) {
            element.sendKeys(String.valueOf(c));
            Thread.sleep(50 + RNG.nextInt(80)); // 50–130ms per key
        }
    }

    /**
     * Scrolls a web element into view using JavaScript.
     */
    private static void scrollToElement(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element
        );
    }

    /**
     * Tries to find and click a save button via JavaScript (fallback).
     */
    private static boolean tryJsSave(WebDriver driver) {
        try {
            Boolean result = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var btns = document.querySelectorAll('button');" +
                "for(var i=0; i<btns.length; i++){" +
                "  if(btns[i].innerText.trim().toLowerCase()==='save'){" +
                "    btns[i].click(); return true;" +
                "  }" +
                "}" +
                "return false;"
            );
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Waits a random amount of time between minMs and maxMs milliseconds.
     * Makes the bot behave less like a bot.
     */
    private static void humanDelay(int minMs, int maxMs) throws InterruptedException {
        int delay = minMs + RNG.nextInt(maxMs - minMs);
        log.debug("Waiting {}ms...", delay);
        Thread.sleep(delay);
    }
}
