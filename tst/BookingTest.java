
import org.junit.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.regex.*;

public class BookingTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static Connection conn;
    private static PreparedStatement insertPs;

    private static final Pattern PRICE_PATTERN = Pattern.compile("(?:US\\$|\\$|£|€)\\s*([0-9][0-9,]*)");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final List<String> CITIES = List.of(
            "El Paso, Texas",
            "Queens, New York",
            "Jersey City, New Jersey",
            "Miami, Florida",
            "Atlanta, Georgia"
    );

    private static final String CURRENCY = "$";          // force-normalize to USD for DB
    private static final int MAX_PAGES_PER_CITY = 13;    // cap: 13 dates per city

    @BeforeClass
    public static void setUpClass() throws Exception {
        // ---- WebDriver ----
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--width=1920", "--height=1080");
        // options.addArguments("-headless"); // enable on CI if needed
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // ---- SQLite ----
        conn = DriverManager.getConnection("jdbc:sqlite:hotelAuto");
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS hotel_price (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "city TEXT NOT NULL," +
                    "brand TEXT NOT NULL," +
                    "hotel_name TEXT NOT NULL," +
                    "checkin_date TEXT NOT NULL," +          // yyyy-MM-dd
                    "price_cents INTEGER NOT NULL," +
                    "currency TEXT NOT NULL," +
                    "source_url TEXT NOT NULL," +
                    "scraped_at TEXT NOT NULL" +             // ISO timestamp
                    ")");
            st.execute("CREATE INDEX IF NOT EXISTS idx_city_brand_date ON hotel_price(city, brand, checkin_date)");
        }
        insertPs = conn.prepareStatement(
                "INSERT INTO hotel_price(city, brand, hotel_name, checkin_date, price_cents, currency, source_url, scraped_at) " +
                        "VALUES(?,?,?,?,?,?,?,?)");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (insertPs != null) insertPs.close();
        if (conn != null) conn.close();
        if (driver != null) driver.quit();
    }

    @Test
    public void scrapeAndInsert_FiveCities_Nov10_to_Dec11() throws Exception {
        LocalDate start = LocalDate.of(2025, 11, 10);
        LocalDate end   = LocalDate.of(2025, 12, 11);

        int totalInserted = 0;
        int pagesThisSession = 0;

        for (String city : CITIES) {
            int pagesForCity = 0;

            for (LocalDate d = start;
                 !d.isAfter(end) && pagesForCity < MAX_PAGES_PER_CITY;
                 d = d.plusDays(1)) {

                int added = scrapeOneNightAndInsert(city, d);
                totalInserted += added;
                pagesForCity++;
                pagesThisSession++;

                // polite pacing to reduce flags
                Thread.sleep(2500 + (long)(Math.random() * 2500));

                // optional: rotate browser every ~6 pages
                if (pagesThisSession >= 6) {
                    driver.quit();
                    FirefoxOptions options = new FirefoxOptions();
                    options.addArguments("--width=1920", "--height=1080");
                    driver = new FirefoxDriver(options);
                    wait = new WebDriverWait(driver, Duration.ofSeconds(30));
                    pagesThisSession = 0;
                    Thread.sleep(10_000); // cooldown
                }
            }
            System.out.println("City done with " + pagesForCity + " dates: " + city);
        }

        System.out.println("Inserted rows across all cities: " + totalInserted);
        Assert.assertTrue("Expected at least some rows inserted.", totalInserted > 0);
    }

    // -------------------- helpers --------------------

    private static int scrapeOneNightAndInsert(String city, LocalDate checkIn) throws Exception {
        LocalDate checkOut = checkIn.plusDays(1);
        String url = buildBookingUrl(city, checkIn, checkOut);

        driver.get(url);

        handleCookieBannerIfAny();   // Booking.com cookie consent
        closePopupsIfAny();          // dismiss sign-in/genius/etc.

        // Booking result cards
        By anyCard = By.cssSelector("[data-testid='property-card']");
        wait.until(ExpectedConditions.presenceOfElementLocated(anyCard));

        // Scroll until results stabilize
        int stableRounds = 0, totalRounds = 0, lastCount = 0;
        JavascriptExecutor js = (JavascriptExecutor) driver;
        while (stableRounds < 2 && totalRounds < 12) {
            List<WebElement> cards = driver.findElements(anyCard);
            int count = cards.size();
            if (count == lastCount) stableRounds++; else { stableRounds = 0; lastCount = count; }
            js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
            Thread.sleep(1000);
            totalRounds++;
        }

        // Extract per-card (hotelName, price)
        List<WebElement> cards = driver.findElements(anyCard);
        Map<String, Integer> cheapestByHotel = new HashMap<>();

        for (WebElement card : cards) {
            String hotel = extractHotelName(card);   // Booking selectors
            if (hotel == null || hotel.isBlank()) {
                hotel = fallbackHotelName(card);
                if (hotel == null || hotel.isBlank()) continue;
            }

            Integer dollars = extractFirstPrice(card);   // Booking price selectors
            if (dollars == null || dollars <= 20 || dollars >= 2000) continue;

            cheapestByHotel.merge(hotel, dollars, Math::min);
        }

        // Insert rows
        int inserted = 0;
        String nowIso = OffsetDateTime.now().toString();
        for (Map.Entry<String,Integer> e : cheapestByHotel.entrySet()) {
            String hotelName = e.getKey();
            int priceCents = e.getValue() * 100;
            String brand = detectBrand(hotelName);

            insertPs.setString(1, city);
            insertPs.setString(2, brand);
            insertPs.setString(3, hotelName);
            insertPs.setString(4, checkIn.format(ISO));
            insertPs.setInt(5, priceCents);
            insertPs.setString(6, CURRENCY);
            insertPs.setString(7, url);
            insertPs.setString(8, nowIso);
            insertPs.addBatch();
            inserted++;
        }
        if (inserted > 0) insertPs.executeBatch();
        return inserted;
    }

    private static String buildBookingUrl(String city, LocalDate d1, LocalDate d2) {
        // Booking destination search; force en-US + USD for consistency
        String dest = URLEncoder.encode(city, StandardCharsets.UTF_8);
        return "https://www.booking.com/searchresults.html" +
                "?ss=" + dest +
                "&checkin=" + d1.format(ISO) +
                "&checkout=" + d2.format(ISO) +
                "&group_adults=2&no_rooms=1" +
                "&lang=en-us&selected_currency=USD";
    }

    private static void handleCookieBannerIfAny() {
        // Try common cookie accept buttons (avoid :contains in CSS; use XPath for text contains)
        List<By> tries = List.of(
                By.cssSelector("button[id*='onetrust-accept']"),
                By.cssSelector("#onetrust-accept-btn-handler"),
                By.cssSelector("button[aria-label*='Accept']"),
                By.xpath("//button[contains(.,'Accept')]")
        );
        for (By sel : tries) {
            try {
                WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(sel));
                if (btn.isDisplayed()) { btn.click(); Thread.sleep(800); return; }
            } catch (Exception ignored) {}
        }
    }

    private static void closePopupsIfAny() {
        List<By> tries = List.of(
                By.cssSelector("button[aria-label*='Dismiss']"),
                By.cssSelector("button[aria-label*='Close']"),
                By.cssSelector(".bui-modal__close"),
                By.cssSelector("[data-testid='header-sign-in-button-close']"),
                By.cssSelector("button[data-testid='genius-onboarding-close-button']")
        );
        for (By sel : tries) {
            try {
                List<WebElement> btns = driver.findElements(sel);
                for (WebElement b : btns) if (b.isDisplayed()) { b.click(); Thread.sleep(300); }
            } catch (Exception ignored) {}
        }
    }

    private static String extractHotelName(WebElement card) {
        List<By> selectors = List.of(
                By.cssSelector("[data-testid='title']"), // Booking title on list card
                By.cssSelector("a[data-testid='title-link']"),
                By.cssSelector("h3"), By.cssSelector("h2")
        );
        for (By sel : selectors) {
            try {
                String txt = card.findElement(sel).getText();
                if (txt != null && !txt.isBlank()) return txt.trim();
            } catch (NoSuchElementException ignored) {}
        }
        return null;
    }

    private static Integer extractFirstPrice(WebElement card) {
        // Primary: Booking's combined price element
        List<By> priceSelectors = List.of(
                By.cssSelector("[data-testid='price-and-discounted-price']"),
                By.cssSelector("[class*='prco']"), // generic Booking price classes
                By.cssSelector("[aria-label*='price']")
        );
        for (By sel : priceSelectors) {
            try {
                for (WebElement el : card.findElements(sel)) {
                    String t = el.getText();
                    if (t == null || t.isBlank()) continue;
                    Matcher m = PRICE_PATTERN.matcher(t);
                    if (m.find()) {
                        String num = m.group(1).replace(",", "");
                        try { return Integer.parseInt(num); } catch (NumberFormatException ignored) {}
                    }
                }
            } catch (Exception ignored) {}
        }

        // Fallback: scan whole card text
        try {
            String all = card.getText();
            Matcher m = PRICE_PATTERN.matcher(all);
            if (m.find()) {
                String num = m.group(1).replace(",", "");
                return Integer.parseInt(num);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String fallbackHotelName(WebElement card) {
        String txt = card.getText();
        if (txt == null || txt.isBlank()) return null;
        Matcher m = PRICE_PATTERN.matcher(txt);
        if (m.find()) {
            String before = txt.substring(0, m.start()).trim();
            String[] lines = before.split("\\r?\\n");
            String last = lines.length == 0 ? "" : lines[lines.length - 1].trim();
            return last.length() > 3 ? last : null;
        }
        return null;
    }

    private static String detectBrand(String hotelName) {
        String n = hotelName.toLowerCase(Locale.ROOT);
        if (n.contains("hilton") || n.contains("doubletree") || n.contains("hampton") || n.contains("homewood") || n.contains("embassy")) return "Hilton";
        if (n.contains("hyatt") || n.contains("andaz") || n.contains("park hyatt") || n.contains("hyatt place") || n.contains("hyatt regency")) return "Hyatt";
        if (n.contains("four seasons")) return "Four Seasons";
        if (n.contains("best western") || n.matches(".*\\bbw\\b.*")) return "Best Western";
        if (n.contains("holiday inn")) return "Holiday Inn";
        return "Other";
    }
}
