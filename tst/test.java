import org.junit.*;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;

public class test {
//    private static WebDriver driver;
//    private static WebDriverWait wait;
//
//    private static Connection conn;
//    private static PreparedStatement insertPs;
//
//    private static final Pattern PRICE_PATTERN = Pattern.compile("[\\$£€]\\s*([0-9][0-9,]*)");
//    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;
//    private static final String CITY = "El Paso, Texas";
//    private static final String CURRENCY = "$";
//
//    @BeforeClass
//    public static void setUpClass() throws Exception {
//        // ---- WebDriver ----
//        FirefoxOptions options = new FirefoxOptions();
//        options.addArguments("--width=1920", "--height=1080");
//        // options.addArguments("-headless"); // uncomment for CI
//        driver = new FirefoxDriver(options);
//        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
//
//        // ---- SQLite ----
//        conn = DriverManager.getConnection("jdbc:sqlite:hotelAuto");
//        try (Statement st = conn.createStatement()) {
//            st.execute("CREATE TABLE IF NOT EXISTS hotel_price (" +
//                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
//                    "city TEXT NOT NULL," +
//                    "brand TEXT NOT NULL," +
//                    "hotel_name TEXT NOT NULL," +
//                    "checkin_date TEXT NOT NULL," +          // yyyy-MM-dd
//                    "price_cents INTEGER NOT NULL," +
//                    "currency TEXT NOT NULL," +
//                    "source_url TEXT NOT NULL," +
//                    "scraped_at TEXT NOT NULL" +             // ISO timestamp
//                    ")");
//            st.execute("CREATE INDEX IF NOT EXISTS idx_city_brand_date ON hotel_price(city, brand, checkin_date)");
//        }
//        insertPs = conn.prepareStatement(
//                "INSERT INTO hotel_price(city, brand, hotel_name, checkin_date, price_cents, currency, source_url, scraped_at) " +
//                        "VALUES(?,?,?,?,?,?,?,?)");
//    }
//
//    @AfterClass
//    public static void tearDownClass() throws Exception {
//        if (insertPs != null) insertPs.close();
//        if (conn != null) conn.close();
//        if (driver != null) driver.quit();
//    }
//
//    @Test
//    public void scrapeAndInsert_ElPaso_Nov10_to_Dec11() throws Exception {
//        LocalDate start = LocalDate.of(2025, 11, 10);
//        LocalDate end   = LocalDate.of(2025, 12, 11);
//
//        int totalInserted = 0;
//        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
//            totalInserted += scrapeOneNightAndInsert(CITY, d);
//            // Polite pause to reduce bot flags
//            Thread.sleep(1100);
//        }
//
//        System.out.println("Inserted rows: " + totalInserted);
//        Assert.assertTrue("Expected at least some rows inserted.", totalInserted > 0);
//    }
//
//    // -------------------- helpers --------------------
//
//    private static int scrapeOneNightAndInsert(String city, LocalDate checkIn) throws Exception {
//        LocalDate checkOut = checkIn.plusDays(1);
//        String url = buildExpediaUrl(city, checkIn, checkOut);
//
//        driver.get(url);
//
//
//        try {
//            WebElement consent = new WebDriverWait(driver, Duration.ofSeconds(10))
//                    .until(ExpectedConditions.elementToBeClickable(By.cssSelector("button, [role='button']")));
//            String t = consent.getText().toLowerCase();
//            if (t.contains("accept") || t.contains("agree")) consent.click();
//        } catch (Exception ignored) {}
//
//        By anyCard = By.cssSelector("[data-stid='property-listing'], .uitk-card");
//        wait.until(ExpectedConditions.presenceOfElementLocated(anyCard));
//
//        // Scroll until results stabilize
//        int stableRounds = 0, totalRounds = 0, lastCount = 0;
//        JavascriptExecutor js = (JavascriptExecutor) driver;
//        while (stableRounds < 2 && totalRounds < 12) {
//            List<WebElement> cards = driver.findElements(anyCard);
//            int count = cards.size();
//            if (count == lastCount) stableRounds++; else { stableRounds = 0; lastCount = count; }
//            js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
//            Thread.sleep(1000);
//            totalRounds++;
//        }
//
//        // Extract per-card -> (hotelName, price)
//        List<WebElement> cards = driver.findElements(anyCard);
//        Map<String, Integer> cheapestByHotel = new HashMap<>(); // key: hotelName, value: min dollars
//
//        for (WebElement card : cards) {
//            String hotel = extractHotelName(card);
//            if (hotel == null || hotel.isBlank()) {
//                hotel = fallbackHotelName(card);
//                if (hotel == null || hotel.isBlank()) continue;
//            }
//
//            Integer dollars = extractFirstPrice(card);
//            if (dollars == null || dollars <= 20 || dollars >= 2000) continue;
//
//            // keep cheapest per hotel for this date
//            cheapestByHotel.merge(hotel, dollars, Math::min);
//        }
//
//        // Insert rows
//        int inserted = 0;
//        String nowIso = OffsetDateTime.now().toString();
//        for (Map.Entry<String,Integer> e : cheapestByHotel.entrySet()) {
//            String hotelName = e.getKey();
//            int priceCents = e.getValue() * 100;
//            String brand = detectBrand(hotelName);
//
//            insertPs.setString(1, CITY);
//            insertPs.setString(2, brand);
//            insertPs.setString(3, hotelName);
//            insertPs.setString(4, checkIn.format(ISO));
//            insertPs.setInt(5, priceCents);
//            insertPs.setString(6, CURRENCY);
//            insertPs.setString(7, url);
//            insertPs.setString(8, nowIso);
//            insertPs.addBatch();
//            inserted++;
//        }
//        if (inserted > 0) insertPs.executeBatch();
//        return inserted;
//    }
//
//    private static String buildExpediaUrl(String city, LocalDate d1, LocalDate d2) {
//        String base = "https://www.expedia.com/Hotel-Search";
//        String dest = city.replace(" ", "%20");
//        return base + "?destination=" + dest +
//                "&d1=" + d1.format(ISO) + "&d2=" + d2.format(ISO) +
//                "&adults=2&rooms=1&sort=RECOMMENDED";
//    }
//
//    private static String extractHotelName(WebElement card) {
//        // Try common title selectors first; fall back to anchors/headings
//        List<By> selectors = List.of(
//                By.cssSelector("[data-stid='content-hotel-title']"),
//                By.cssSelector("a[data-stid='open-hotel-information']"),
//                By.cssSelector("h3"), By.cssSelector("h2"), By.cssSelector("[data-stid='heading']")
//        );
//        for (By sel : selectors) {
//            try {
//                WebElement el = card.findElement(sel);
//                String txt = el.getText();
//                if (txt != null && !txt.isBlank()) return txt.trim();
//            } catch (NoSuchElementException ignored) {}
//        }
//        return null;
//    }
//
//    private static String fallbackHotelName(WebElement card) {
//        // Crude fallback: text before the first price token
//        String txt = card.getText();
//        if (txt == null || txt.isBlank()) return null;
//        Matcher m = PRICE_PATTERN.matcher(txt);
//        if (m.find()) {
//            String before = txt.substring(0, m.start()).trim();
//            // take the last line before price as hotel name candidate
//            String[] lines = before.split("\\r?\\n");
//            String last = lines.length == 0 ? "" : lines[lines.length - 1].trim();
//            return last.length() > 3 ? last : null;
//        }
//        return null;
//    }
//
//    private static Integer extractFirstPrice(WebElement card) {
//        String txt = card.getText();
//        if (txt == null) return null;
//        Matcher m = PRICE_PATTERN.matcher(txt);
//        if (m.find()) {
//            String num = m.group(1).replace(",", "");
//            try { return Integer.parseInt(num); }
//            catch (NumberFormatException ignored) {}
//        }
//        return null;
//    }
//
//    private static String detectBrand(String hotelName) {
//        String name = hotelName.toLowerCase();
//        // Canonical brand buckets for your rubric
//        if (name.contains("hilton") || name.contains("doubletree") || name.contains("hampton") || name.contains("homewood") || name.contains("embassy")) return "Hilton";
//        if (name.contains("hyatt") || name.contains("andaz") || name.contains("park hyatt") || name.contains("hyatt place") || name.contains("hyatt regency")) return "Hyatt";
//        if (name.contains("four seasons")) return "Four Seasons";
//        if (name.contains("best western") || name.matches(".*\\bbw\\b.*")) return "Best Western";
//        if (name.contains("holiday inn")) return "Holiday Inn";
//        // Unknown → keep full chain as-is (helps you see what to add later)
//        return "Other";
//    }
//
    //here above only once city


    private static WebDriver driver;
    private static WebDriverWait wait;

    private static Connection conn;
    private static PreparedStatement insertPs;

    private static final Pattern PRICE_PATTERN = Pattern.compile("[\\$£€]\\s*([0-9][0-9,]*)");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final List<String> CITIES = List.of(
            "El Paso, Texas",
            "Queens, New York",
            "Jersey City, New Jersey",
            "Miami, Florida",
            "Atlanta, Georgia"
    );
    private static final String CURRENCY = "$";
    private static final int MAX_PAGES_PER_CITY = 13;

    @BeforeClass
    public static void setUpClass() throws Exception {
        // ---- WebDriver ----
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--width=1920", "--height=1080");
        // options.addArguments("-headless"); // uncomment for CI
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

    // Replace your current test with this multi-city version
    @Test
    public void scrapeAndInsert_FiveCities_Nov10_to_Dec11() throws Exception {
        LocalDate start = LocalDate.of(2025, 11, 10);
        LocalDate end   = LocalDate.of(2025, 12, 11);

        int totalInserted = 0;
        int pagesThisSession = 0;

        for (String city : CITIES) {
            int pagesForCity = 0;  // <= cap per city

            for (LocalDate d = start;
                 !d.isAfter(end) && pagesForCity < MAX_PAGES_PER_CITY;
                 d = d.plusDays(1)) {

                int added = scrapeOneNightAndInsert(city, d);
                totalInserted += added;
                pagesForCity++;                 // count 1 page per date (attempt)
                pagesThisSession++;

                // polite pacing
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
            System.out.println("City done with " + pagesForCity + " pages: " + city);
        }

        System.out.println("Inserted rows across all cities: " + totalInserted);
        Assert.assertTrue("Expected at least some rows inserted.", totalInserted > 0);
    }

    // -------------------- helpers --------------------

    private static int scrapeOneNightAndInsert(String city, LocalDate checkIn) throws Exception {
        LocalDate checkOut = checkIn.plusDays(1);
        String url = buildExpediaUrl(city, checkIn, checkOut);

        driver.get(url);


        try {
            WebElement consent = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(By.cssSelector("button, [role='button']")));
            String t = consent.getText().toLowerCase();
            if (t.contains("accept") || t.contains("agree")) consent.click();
        } catch (Exception ignored) {}

        By anyCard = By.cssSelector("[data-stid='property-listing'], .uitk-card");
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

        // Extract per-card -> (hotelName, price)
        List<WebElement> cards = driver.findElements(anyCard);
        Map<String, Integer> cheapestByHotel = new HashMap<>(); // key: hotelName, value: min dollars

        for (WebElement card : cards) {
            String hotel = extractHotelName(card);
            if (hotel == null || hotel.isBlank()) {
                hotel = fallbackHotelName(card);
                if (hotel == null || hotel.isBlank()) continue;
            }

            Integer dollars = extractFirstPrice(card);
            if (dollars == null || dollars <= 20 || dollars >= 2000) continue;

            // keep cheapest per hotel for this date
            cheapestByHotel.merge(hotel, dollars, Math::min);
        }

        // Insert rows
        int inserted = 0;
        String nowIso = OffsetDateTime.now().toString();
        // IMPORTANT: use the city parameter when inserting (not a fixed constant)
        for (Map.Entry<String,Integer> e : cheapestByHotel.entrySet()) {
            String hotelName = e.getKey();
            int priceCents = e.getValue() * 100;
            String brand = detectBrand(hotelName);

            insertPs.setString(1, city);                    // <-- use method param 'city'
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

    private static String buildExpediaUrl(String city, LocalDate d1, LocalDate d2) {
        String base = "https://www.expedia.com/Hotel-Search";
        String dest = URLEncoder.encode(city, StandardCharsets.UTF_8); // handles commas/() etc.
        return base + "?destination=" + dest +
                "&d1=" + d1.format(ISO) + "&d2=" + d2.format(ISO) +
                "&adults=2&rooms=1&sort=RECOMMENDED";
    }


    private static String extractHotelName(WebElement card) {
        // Try common title selectors first; fall back to anchors/headings
        List<By> selectors = List.of(
                By.cssSelector("[data-stid='content-hotel-title']"),
                By.cssSelector("a[data-stid='open-hotel-information']"),
                By.cssSelector("h3"), By.cssSelector("h2"), By.cssSelector("[data-stid='heading']")
        );
        for (By sel : selectors) {
            try {
                WebElement el = card.findElement(sel);
                String txt = el.getText();
                if (txt != null && !txt.isBlank()) return txt.trim();
            } catch (NoSuchElementException ignored) {}
        }
        return null;
    }

    private static String fallbackHotelName(WebElement card) {
        // Crude fallback: text before the first price token
        String txt = card.getText();
        if (txt == null || txt.isBlank()) return null;
        Matcher m = PRICE_PATTERN.matcher(txt);
        if (m.find()) {
            String before = txt.substring(0, m.start()).trim();
            // take the last line before price as hotel name candidate
            String[] lines = before.split("\\r?\\n");
            String last = lines.length == 0 ? "" : lines[lines.length - 1].trim();
            return last.length() > 3 ? last : null;
        }
        return null;
    }

    private static Integer extractFirstPrice(WebElement card) {
        String txt = card.getText();
        if (txt == null) return null;
        Matcher m = PRICE_PATTERN.matcher(txt);
        if (m.find()) {
            String num = m.group(1).replace(",", "");
            try { return Integer.parseInt(num); }
            catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private static String detectBrand(String hotelName) {
        String name = hotelName.toLowerCase();
        // Canonical brand buckets for your rubric
        if (name.contains("hilton") || name.contains("doubletree") || name.contains("hampton") || name.contains("homewood") || name.contains("embassy")) return "Hilton";
        if (name.contains("hyatt") || name.contains("andaz") || name.contains("park hyatt") || name.contains("hyatt place") || name.contains("hyatt regency")) return "Hyatt";
        if (name.contains("four seasons")) return "Four Seasons";
        if (name.contains("best western") || name.matches(".*\\bbw\\b.*")) return "Best Western";
        if (name.contains("holiday inn")) return "Holiday Inn";
        // Unknown → keep full chain as-is (helps you see what to add later)
        return "Other";
    }



}

