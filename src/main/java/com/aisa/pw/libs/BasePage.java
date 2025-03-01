package com.aisa.pw.libs;


import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.nio.file.Paths;
import java.util.List;
import com.microsoft.playwright.options.WaitForSelectorState;

/*
 * Provides reusable UI actions, reducing boilerplate in test cases, for usage create a new class that extends BasePage
 * and call the methods in your test cases by creating objects of the class.
 * */

public class BasePage {
    protected final Page page;

    public BasePage() {
        this.page = PlaywrightManager.getPage();
    }

    public BasePage(Page page) {
        this.page = page;
    }

    /** Launch URL */
    public void launchUrl(String url) {
        page.navigate(url);
    }

    /** Launch URL with navigate options */
    public void launchUrlWithNavOptions(String url, Page.NavigateOptions options) {
        page.navigate(url, options);
    }

    /** Returns page title*/
    public String getPageTitle() {
        return page.title();
    }

    /** Returns page title */
    public String  getPageTitle(Page page) {
        return page.title();
    }

    /** Pause page for debug, halts execution until debugger closed */
    public void pausePageForDebug() {
        page.pause();
    }

    /** Pause page for debug, halts execution until debugger closed */
    public void pausePageForDebug(Page page) {
        page.pause();
    }

    /** Click on an element */
    public void click(String selector) {
        page.locator(selector).click();
    }

    /** Fill input field */
    public void fill(String selector, String text) {
        page.locator(selector).fill(text);
    }

    /** Get text content of an element */
    public String getText(String selector) {
        return page.locator(selector).textContent();
    }

    /** Get attribute value */
    public String getAttribute(String selector, String attribute) {
        return page.locator(selector).getAttribute(attribute);
    }

    /** Check if element is visible */
    public boolean isVisible(String selector) {
        return page.locator(selector).isVisible();
    }

    /** Check if element is enabled */
    public boolean isEnabled(String selector) {
        return page.locator(selector).isEnabled();
    }

    /** Select from dropdown by value */
    public void selectByValue(String selector, String value) {
        page.locator(selector).selectOption(value);
    }

    /** Check a checkbox */
    public void checkCheckbox(String selector) {
        Locator locator = page.locator(selector);
        if (!locator.isChecked()) {
            locator.check();
        }
    }

    /** Uncheck a checkbox */
    public void uncheckCheckbox(String selector) {
        Locator locator = page.locator(selector);
        if (locator.isChecked()) {
            locator.uncheck();
        }
    }

    /** Click a radio button */
    public void selectRadioButton(String selector) {
        page.locator(selector).click();
    }

    /** Hover over an element */
    public void hover(String selector) {
        page.locator(selector).hover();
    }

    /** Upload file */
    public void uploadFile(String selector, String filePath) {
        page.locator(selector).setInputFiles(Paths.get(filePath));
    }

    /** Take screenshot of the page */
    public void takeScreenshot(String fileName) {
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(fileName)).setFullPage(true));
    }

    /** Take screenshot of an element */
    public void takeElementScreenshot(String selector, String fileName) {
        page.locator(selector).screenshot(new Locator.ScreenshotOptions().setPath(Paths.get(fileName)));
    }

    /** Execute JavaScript */
    public Object executeJavaScript(String script) {
        return page.evaluate(script);
    }

    /** Wait for an element to be visible */
    public void waitForVisibility(String selector) {
        page.waitForSelector(selector, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
    }


    public void waitForInvisibility(String selector) {
        page.waitForSelector(selector, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
    }

    /** Scroll to an element */
    public void scrollToElement(String selector) {
        page.locator(selector).scrollIntoViewIfNeeded();
    }

    /** Get a list of all elements matching a selector */
    public List<ElementHandle> getElements(String selector) {
        return page.querySelectorAll(selector);
    }

    /** Get count of elements matching a selector */
    public int getElementCount(String selector) {
        return page.locator(selector).count();
    }

    /** Drag and drop an element */
    public void dragAndDrop(String sourceSelector, String targetSelector) {
        page.locator(sourceSelector).dragTo(page.locator(targetSelector));
    }

    /** Close the page */
    public void close() {
        page.close();
    }
}