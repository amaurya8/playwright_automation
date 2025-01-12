package com.aisa.playwright;

import com.microsoft.playwright.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.MalformedURLException;
import java.net.URL;

public class PlaywrightRemoteBrowser {

    public static void main(String[] args) throws MalformedURLException {
        // Selenium Grid URL
        String gridUrl = "http://localhost:4444/wd/hub";

        // ChromeOptions for Selenium Grid
        ChromeOptions options = new ChromeOptions();
        options.setCapability("browserName", "chrome");
        options.setCapability("platformName", "LINUX");

        // Launch Remote Browser via WebDriver
        RemoteWebDriver remoteWebDriver = new RemoteWebDriver(new URL(gridUrl), options);

        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
            launchOptions.setChannel("chrome");
            Browser browser = playwright.chromium().connectOverCDP("ws://localhost:4444");
            Page page = browser.newPage();
            page.navigate("https://www.calculator.net/");
            System.out.println("Page Title: " + page.title());
            browser.close();
        }
    }
}
