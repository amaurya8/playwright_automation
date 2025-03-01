package com.aisa.pw.libs;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentReportManager {
    private static final ExtentReports extentReports = new ExtentReports();
    private static ExtentTest test;

    static {
        ExtentSparkReporter spark = new ExtentSparkReporter("test-output/ExtentReport.html");
        extentReports.attachReporter(spark);
    }

    public static void createTest(String testName) {
        test = extentReports.createTest(testName);
    }

    public static void logInfo(String message) {
        test.info(message);
    }

    public static void logPass(String message) {
        test.pass(message);
    }

    public static void logFail(String message) {
        test.fail(message);
    }

    public static void flush() {
        extentReports.flush();
    }
}
