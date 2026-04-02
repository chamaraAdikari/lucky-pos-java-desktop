package com.turboboostteam.pos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditLogger {

    // Uses the special AUDIT logger from logback.xml
    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    public static void log(String action, String username, String details) {
        audit.info("{} | user={} | details={}", action, username, details);
    }

    // Convenience methods
    public static void loginSuccess(String username) {
        log("LOGIN_SUCCESS", username, "User logged in");
    }

    public static void loginFailed(String username) {
        log("LOGIN_FAILED", username, "Invalid PIN attempt");
    }

    public static void logout(String username) {
        log("LOGOUT", username, "User logged out");
    }

    public static void saleCompleted(String username, String saleId) {
        log("SALE_COMPLETED", username, "saleId=" + saleId);
    }

    public static void saleVoided(String username, String saleId) {
        log("SALE_VOIDED", username, "saleId=" + saleId);
    }
}