package com.aisa.pw.libs;

import com.microsoft.playwright.*;

public class PlaywrightManager {
    private static final ThreadLocal<Playwright> playwrightThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browserThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();

    // Enum for supported browsers
    public enum BrowserEnum {
        CHROME, FIREFOX, EDGE, SAFARI
    }

    public static synchronized void initialize(BrowserEnum browserType, boolean headless) {
        if (playwrightThreadLocal.get() != null) {
            return; // Already initialized for this thread
        }

        Playwright playwright = Playwright.create();
        playwrightThreadLocal.set(playwright);

        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(headless);
        Browser browser;

        switch (browserType) {
            case FIREFOX:
                browser = playwright.firefox().launch(options);
                break;
            case SAFARI:
                browser = playwright.webkit().launch(options); // WebKit is closest to Safari
                break;
            case EDGE:
                browser = playwright.chromium().launch(options.setChannel("msedge"));
                break;
            case CHROME:
            default:
                browser = playwright.chromium().launch(options.setChannel("chrome"));
                break;
        }

        browserThreadLocal.set(browser);
        BrowserContext context = browser.newContext();
        contextThreadLocal.set(context);
        pageThreadLocal.set(context.newPage());
    }

    public static Page getPage() {
        Page page = pageThreadLocal.get();
        if (page == null) {
            throw new IllegalStateException("Playwright is not initialized. Call initialize() first.");
        }
        return page;
    }

    public static synchronized void close() {
        try {
            if (pageThreadLocal.get() != null) {
                pageThreadLocal.get().close();
                pageThreadLocal.remove();
            }
            if (contextThreadLocal.get() != null) {
                contextThreadLocal.get().close();
                contextThreadLocal.remove();
            }
            if (browserThreadLocal.get() != null) {
                browserThreadLocal.get().close();
                browserThreadLocal.remove();
            }
            if (playwrightThreadLocal.get() != null) {
                playwrightThreadLocal.get().close();
                playwrightThreadLocal.remove();
            }
        } catch (Exception e) {
            System.err.println("Error while closing Playwright resources: " + e.getMessage());
        }
    }
}