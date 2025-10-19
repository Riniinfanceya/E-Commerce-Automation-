package ecomscraper;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class EcomPriceScraper {

    public static void main(String[] args) {
        // Product to search
        String product = args.length > 0 ? String.join(" ", args) : "iphone 13";
        System.out.println("Searching for: " + product);

        // Setup ChromeDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));

            System.out.println("\n--- Amazon ---");
            searchAmazon(driver, product);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    // Amazon scraper with enhancements
    private static void searchAmazon(WebDriver driver, String query) {
        String amazonUrl = "https://www.amazon.in/";
        driver.get(amazonUrl);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            // Accept cookies if visible
            try {
                WebElement acceptBtn = driver.findElement(By.id("sp-cc-accept"));
                acceptBtn.click();
            } catch (Exception ignored) {}

            WebElement searchBox = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("twotabsearchtextbox")));
            searchBox.sendKeys(query);
            searchBox.sendKeys(Keys.ENTER);

            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.s-main-slot")));

            List<WebElement> items = driver.findElements(
                    By.cssSelector("div.s-main-slot div[data-component-type='s-search-result']"));

            if (items.isEmpty()) {
                System.out.println("No results found on Amazon.");
                return;
            }

            // Print top 3 results
            for (int i = 0; i < Math.min(3, items.size()); i++) {
                WebElement item = items.get(i);
                System.out.println("\nItem " + (i + 1) + ":");

                String title = tryFindText(item, List.of(
                        "h2 > a > span",
                        "span.a-size-medium.a-color-base.a-text-normal"));

                String price = tryFindText(item, List.of(
                        "span.a-price > span.a-offscreen",
                        "span.a-price-whole"));

                String rating = tryFindText(item, List.of(
                        "span.a-icon-alt"));

                String reviews = tryFindText(item, List.of(
                        "span.a-size-base"));

                System.out.println("Title: " + (title != null ? title : "N/A"));
                System.out.println("Price: " + (price != null ? price : "Price not found"));
                System.out.println("Rating: " + (rating != null ? rating : "N/A"));
                System.out.println("Reviews: " + (reviews != null ? reviews : "N/A"));
            }

        } catch (Exception e) {
            System.out.println("Amazon error: " + e.getMessage());
        }
    }

    // Helper function to try multiple CSS selectors
    private static String tryFindText(WebElement root, List<String> selectors) {
        for (String sel : selectors) {
            try {
                WebElement el = root.findElement(By.cssSelector(sel));
                if (el != null && !el.getText().isBlank()) return el.getText().trim();
            } catch (NoSuchElementException ignored) {}
        }
        return null;
    }
}
