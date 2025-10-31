import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/*

Generated using Claude 4.5 Sonnnet

10/28/2025 Tuesday

Direct hotel search: The URL now searches specifically for "Four Seasons Hotel Atlanta" on Booking.com
Focused extraction: The code looks specifically for Four Seasons in the results
Multiple search methods: Uses 3 different techniques to find the hotel:

Property card scanning
XPath search for "four seasons" text
Title element search with ancestor lookup


Detailed information extraction:

Hotel name
Price (multiple selectors to ensure we catch it)
Rating/review score
Distance from center
Address
Full card text for debugging


Hotel detail page: Automatically clicks into the hotel's detail page and extracts:

More detailed pricing
Amenities
All prices found on the page using JavaScript


Fallback: If Four Seasons isn't found, it shows the first 10 hotels that were found in the search results

The output will show:

Whether the hotel was found
All extracted information from the search results
Information from the detail page (if clickable)
A list of alternative hotels if Four Seasons isn't found

This should give you comprehensive information about the Four Seasons Hotel Atlanta specifically from Booking.com!

 */

public class Booking {

    public static void main(String[] args) {
        WebDriver driver = null;

        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            options.setExperimentalOption("useAutomationExtension", false);

            driver = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            js.executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

            // Specific hotel search
            //String hotelName = "Four Seasons Hotel Atlanta";
            String hotelName = "The Westin Peachtree Plaza, Atlanta";
            //String hotelName = "The Ritz-Carlton Atlanta";

            // Date parameters (check-in tomorrow, check-out day after)
            LocalDate checkIn = LocalDate.now().plusDays(1);
            LocalDate checkOut = checkIn.plusDays(1);

            // Build URL to search specifically for hotel name
            String url = buildBookingUrl(hotelName, checkIn, checkOut);

            System.out.println("Searching for: " + hotelName);
            System.out.println("Navigating to: " + url);
            System.out.println();

            driver.get(url);
            Thread.sleep(5000);

            // Handle cookie consent banner
            handleCookieBanner(driver, wait);

            // Close any popups
            closePopups(driver);

            System.out.println("Page loaded. Analyzing results...\n");

            // Scroll to load all results
            scrollToLoadResults(driver, js);

            // Search for the specific hotel in results
            System.out.println("=== Searching for Hotel  ===\n");
            WebElement hotelCard = findHotel(driver, hotelName);

            if (hotelCard != null) {
                System.out.println("✓ FOUND: Hotel");
                System.out.println("=" .repeat(60));
                extractDetailedHotelInfo(hotelCard, driver);
                System.out.println("=" .repeat(60));

                // Try to click into hotel detail page
                System.out.println("\n=== Attempting to open hotel detail page ===\n");
                clickIntoHotelDetail(driver, wait, js, hotelCard);
            } else {
                System.out.println("✗ Hotel not found in results");
                System.out.println("Possible reasons:");
                System.out.println("  - Hotel not available on Booking.com");
                System.out.println("  - No availability for selected dates");
                System.out.println("  - Different hotel name on Booking.com");

                // Show what hotels were found
                showAvailableHotels(driver);
            }

            // Keep browser open for inspection
            System.out.println("\n\nBrowser will stay open. Press Enter to close...");
            System.in.read();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }


    public static int getPriceOfRoom(String hotelName, LocalDate start) {
        WebDriver driver = null;

        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            options.addArguments("--headless");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            options.setExperimentalOption("useAutomationExtension", false);

            driver = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            js.executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

            // Date parameters (check-in tomorrow, check-out day after)
            LocalDate checkIn = start.plusDays(1);
            LocalDate checkOut = checkIn.plusDays(1);

            // Build URL to search specifically for the hotel name
            String url = buildBookingUrl(hotelName, checkIn, checkOut);

            System.out.println("Searching for: " + hotelName);
            System.out.println("Navigating to: " + url);
            System.out.println();

            driver.get(url);
            Thread.sleep(5000);

            // Handle cookie consent banner
            handleCookieBanner(driver, wait);

            // Close any popups
            closePopups(driver);

            System.out.println("Page loaded. Analyzing results...\n");

            // Scroll to load all results
            scrollToLoadResults(driver, js);

            // Search for the specific hotel in results
            System.out.println("=== Searching for" + hotelName +" ===\n");
            WebElement hotelCard = findHotel(driver, hotelName);

            if (hotelCard != null) {
                System.out.println("✓ FOUND: " + hotelName);
                System.out.println("=".repeat(60));
                extractDetailedHotelInfo(hotelCard, driver);
                System.out.println("=".repeat(60));

                // Try to click into hotel detail page
                System.out.println("\n=== Attempting to open hotel detail page ===\n");
                clickIntoHotelDetail(driver, wait, js, hotelCard);
            } else {
                System.out.println("✗ " + hotelName +" not found in results");
                System.out.println("Possible reasons:");
                System.out.println("  - Hotel not available on Booking.com");
                System.out.println("  - No availability for selected dates");
                System.out.println("  - Different hotel name on Booking.com");

                // Show what hotels were found
                showAvailableHotels(driver);
            }

            // Keep browser open for inspection
            System.out.println("\n\nBrowser will stay open. Press Enter to close...");
            System.in.read();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
        return 0;
    }

    private static String buildBookingUrl(String hotelName, LocalDate checkIn, LocalDate checkOut) {
        // Search specifically for the hotel name
        return String.format(
                "https://www.booking.com/searchresults.html?ss=%s&checkin=%s&checkout=%s&group_adults=2&no_rooms=1",
                hotelName.replace(" ", "+"),
                checkIn.toString(),
                checkOut.toString()
        );
    }

    private static void handleCookieBanner(WebDriver driver, WebDriverWait wait) {
        try {
            String[] cookieSelectors = {
                    "button[id*='onetrust-accept']",
                    "button[aria-label*='Accept']",
                    "#onetrust-accept-btn-handler",
                    "button:contains('Accept')"
            };

            for (String selector : cookieSelectors) {
                try {
                    WebElement acceptButton = driver.findElement(By.cssSelector(selector));
                    if (acceptButton.isDisplayed()) {
                        acceptButton.click();
                        System.out.println("✓ Accepted cookies\n");
                        Thread.sleep(1000);
                        return;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (Exception e) {
            // No cookie banner or already accepted
        }
    }

    private static void closePopups(WebDriver driver) {
        try {
            String[] closeSelectors = {
                    "button[aria-label*='Dismiss']",
                    "button[aria-label*='Close']",
                    ".bui-modal__close",
                    "[data-testid='header-sign-in-button-close']",
                    "button[data-testid='genius-onboarding-close-button']"
            };

            for (String selector : closeSelectors) {
                try {
                    List<WebElement> closeButtons = driver.findElements(By.cssSelector(selector));
                    for (WebElement btn : closeButtons) {
                        if (btn.isDisplayed()) {
                            btn.click();
                            Thread.sleep(500);
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private static void scrollToLoadResults(WebDriver driver, JavascriptExecutor js) {
        try {
            for (int i = 0; i < 3; i++) {
                js.executeScript("window.scrollBy(0, 800)");
                Thread.sleep(1500);
            }
            js.executeScript("window.scrollTo(0, 0)");
            Thread.sleep(1000);
        } catch (Exception e) {
            // Ignore
        }
    }

    private static WebElement findHotel(WebDriver driver, String hotelName) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.cssSelector("[data-testid='property-card']")
            ));

            List<WebElement> propertyCards = driver.findElements(By.cssSelector("[data-testid='property-card']"));
            System.out.println("Scanning " + propertyCards.size() + " properties...\n");

            for (WebElement card : propertyCards) {
                try {
                    // Locate the hotel title inside each card
                    WebElement titleElement = card.findElement(By.cssSelector("[data-testid='title']"));
                    String title = titleElement.getText().toLowerCase();

                    if (title.contains(hotelName.toLowerCase())) {
                        System.out.println("✅ Found hotel: " + title);
                        return card;
                    }

                } catch (NoSuchElementException ignored) {
                    // Skip cards that don't have title
                }
            }

            System.out.println("No hotel found matching: " + hotelName);
            return null;

        } catch (Exception e) {
            System.out.println("Error finding hotel: " + e.getMessage());
            return null;
        }
    }


    private static void extractDetailedHotelInfo(WebElement propertyCard, WebDriver driver) {
        try {
            // Extract hotel name
            System.out.println("HOTEL INFORMATION:");
            System.out.println("-" .repeat(60));

            try {
                WebElement nameElement = propertyCard.findElement(
                        By.cssSelector("[data-testid='title']")
                );
                System.out.println("Name: " + nameElement.getText());
            } catch (Exception e) {
                try {
                    WebElement nameElement = propertyCard.findElement(By.cssSelector("h3, [class*='title']"));
                    System.out.println("Name: " + nameElement.getText());
                } catch (Exception ex) {
                    System.out.println("Name: Could not extract");
                }
            }

            // Extract price
            try {
                WebElement priceElement = propertyCard.findElement(
                        By.cssSelector("[data-testid='price-and-discounted-price']")
                );
                System.out.println("Price: " + priceElement.getText());
            } catch (Exception e) {
                try {
                    List<WebElement> priceElements = propertyCard.findElements(
                            By.cssSelector("[class*='price'], [aria-label*='price']")
                    );

                    for (WebElement priceElem : priceElements) {
                        String priceText = priceElem.getText();
                        if (!priceText.isEmpty() && (priceText.contains("$") || priceText.contains("US$") || priceText.matches(".*\\d+.*"))) {
                            System.out.println("Price: " + priceText);
                            break;
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("Price: Could not extract");
                }
            }

            // Extract rating/review score
            try {
                WebElement ratingElement = propertyCard.findElement(
                        By.cssSelector("[data-testid='review-score']")
                );
                System.out.println("Rating: " + ratingElement.getText());
            } catch (Exception e) {
                try {
                    WebElement ratingElement = propertyCard.findElement(
                            By.cssSelector("[aria-label*='Scored'], [class*='review-score']")
                    );
                    System.out.println("Rating: " + ratingElement.getText());
                } catch (Exception ex) {
                    System.out.println("Rating: Not available");
                }
            }

            // Extract location/distance
            try {
                WebElement locationElement = propertyCard.findElement(
                        By.cssSelector("[data-testid='distance']")
                );
                System.out.println("Distance: " + locationElement.getText());
            } catch (Exception e) {
                // Not critical
            }

            // Extract address
            try {
                WebElement addressElement = propertyCard.findElement(
                        By.cssSelector("[data-testid='address']")
                );
                System.out.println("Address: " + addressElement.getText());
            } catch (Exception e) {
                // Not critical
            }

            System.out.println("-" .repeat(60));
            System.out.println("\nFULL CARD TEXT:");
            System.out.println(propertyCard.getText());

        } catch (Exception e) {
            System.out.println("Error extracting hotel info: " + e.getMessage());
        }
    }

    private static void clickIntoHotelDetail(WebDriver driver, WebDriverWait wait, JavascriptExecutor js, WebElement hotelCard) {
        try {
            // Find the clickable link/title
            WebElement clickableElement = null;

            try {
                clickableElement = hotelCard.findElement(By.cssSelector("[data-testid='title']"));
            } catch (Exception e) {
                try {
                    clickableElement = hotelCard.findElement(By.cssSelector("a[data-testid='title-link']"));
                } catch (Exception ex) {
                    try {
                        clickableElement = hotelCard.findElement(By.cssSelector("a"));
                    } catch (Exception exc) {
                        System.out.println("Could not find clickable element");
                        return;
                    }
                }
            }

            // Store original window
            String originalWindow = driver.getWindowHandle();

            // Scroll to element and click
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", clickableElement);
            Thread.sleep(1000);

            System.out.println("Clicking into hotel detail page...");

            try {
                clickableElement.click();
            } catch (Exception e) {
                js.executeScript("arguments[0].click();", clickableElement);
            }

            Thread.sleep(3000);

            // Check if new window/tab opened
            Set<String> windowHandles = driver.getWindowHandles();
            if (windowHandles.size() > 1) {
                for (String handle : windowHandles) {
                    if (!handle.equals(originalWindow)) {
                        driver.switchTo().window(handle);
                        break;
                    }
                }
            }

            System.out.println("✓ Opened hotel detail page: " + driver.getTitle());
            Thread.sleep(2000);

            // Extract information from detail page
            extractFromDetailPage(driver, js);

        } catch (Exception e) {
            System.out.println("Error clicking into hotel detail: " + e.getMessage());
        }
    }

    private static void extractFromDetailPage(WebDriver driver, JavascriptExecutor js) {
        try {
            System.out.println("\n" + "=" .repeat(60));
            System.out.println("DETAIL PAGE INFORMATION:");
            System.out.println("=" .repeat(60));

            // Scroll to load price section
            js.executeScript("window.scrollBy(0, 300)");
            Thread.sleep(2000);

            // Extract hotel name from detail page
            try {
                WebElement nameElement = driver.findElement(By.cssSelector("h2[class*='pp-header__title']"));
                System.out.println("Hotel Name: " + nameElement.getText());
            } catch (Exception e) {
                System.out.println("Hotel Name: Could not extract from detail page");
            }

            // Extract prices from detail page
            System.out.println("\nPRICES:");
            String[] priceSelectors = {
                    "[data-testid='price-and-discounted-price']",
                    "[class*='prco-text-nowrap-helper']",
                    "[class*='prco-valign-middle-helper']",
                    "span[aria-label*='price']",
                    "[class*='bui-price-display__value']"
            };

            for (String selector : priceSelectors) {
                try {
                    List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                    for (WebElement elem : elements) {
                        String text = elem.getText();
                        if (!text.isEmpty() && (text.contains("$") || text.contains("US$") || text.matches(".*\\d+.*"))) {
                            System.out.println("  - " + text);
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            // Extract amenities
            System.out.println("\nAMENITIES:");
            try {
                List<WebElement> amenities = driver.findElements(
                        By.cssSelector("[class*='facility'], [class*='important_facility']")
                );
                int count = 0;
                for (WebElement amenity : amenities) {
                    String text = amenity.getText();
                    if (!text.isEmpty() && count < 10) {
                        System.out.println("  - " + text);
                        count++;
                    }
                }
            } catch (Exception e) {
                System.out.println("  Could not extract amenities");
            }

            // JavaScript extraction for all prices
            System.out.println("\nALL PRICES FOUND ON PAGE:");
            String script =
                    "var prices = new Set();" +
                            "var regex = /\\$\\s?\\d+(?:,\\d{3})*(?:\\.\\d{2})?|US\\$\\s?\\d+(?:,\\d{3})*(?:\\.\\d{2})?/g;" +
                            "var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);" +
                            "while(walker.nextNode()) {" +
                            "    var matches = walker.currentNode.textContent.match(regex);" +
                            "    if (matches) {" +
                            "        matches.forEach(function(m) { prices.add(m); });" +
                            "    }" +
                            "}" +
                            "return Array.from(prices);";

            Object result = js.executeScript(script);
            if (result instanceof List) {
                List<String> prices = (List<String>) result;
                for (String price : prices) {
                    System.out.println("  - " + price);
                }
            }

        } catch (Exception e) {
            System.out.println("Error extracting from detail page: " + e.getMessage());
        }
    }

    private static void showAvailableHotels(WebDriver driver) {
        try {
            System.out.println("\n=== Hotels found in search results ===\n");

            List<WebElement> propertyCards = driver.findElements(
                    By.cssSelector("[data-testid='property-card']")
            );

            int count = 1;
            for (WebElement card : propertyCards) {
                try {
                    WebElement nameElement = card.findElement(By.cssSelector("[data-testid='title']"));
                    String name = nameElement.getText();
                    if (!name.isEmpty()) {
                        System.out.println(count + ". " + name);
                        count++;
                        if (count > 10) break; // Show first 10
                    }
                } catch (Exception e) {
                    continue;
                }
            }

        } catch (Exception e) {
            System.out.println("Could not list available hotels");
        }
    }

}