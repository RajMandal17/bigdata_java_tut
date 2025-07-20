package com.bigdata.kafka.service;

import com.bigdata.kafka.model.TransactionEvent;
import com.bigdata.kafka.model.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Notification Service for sending alerts and notifications
 * Handles email, SMS, push notifications for various events
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    /**
     * Send high-value transaction notification
     */
    public void sendHighValueTransactionNotification(TransactionEvent transaction) {
        logger.info("Sending high-value transaction notification for: {}", 
                   transaction.getTransactionId());

        try {
            String subject = "High-Value Transaction Alert";
            String message = String.format(
                "A high-value transaction of $%s has been processed for customer %s.\n\n" +
                "Transaction ID: %s\n" +
                "Amount: $%s\n" +
                "Category: %s\n" +
                "Time: %s\n\n" +
                "If this transaction was not authorized by you, please contact us immediately.",
                transaction.getAmount(),
                transaction.getCustomerId(),
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getCategory(),
                transaction.getTimestamp()
            );

            sendNotification(transaction.getCustomerId(), subject, message, "HIGH_VALUE_TRANSACTION");

        } catch (Exception e) {
            logger.error("Error sending high-value transaction notification", e);
        }
    }

    /**
     * Send international transaction notification
     */
    public void sendInternationalTransactionNotification(TransactionEvent transaction) {
        logger.info("Sending international transaction notification for: {}", 
                   transaction.getTransactionId());

        try {
            String subject = "International Transaction Alert";
            String message = String.format(
                "An international transaction has been processed on your account.\n\n" +
                "Transaction ID: %s\n" +
                "Amount: $%s\n" +
                "Category: %s\n" +
                "Time: %s\n\n" +
                "If you did not authorize this transaction, please contact us immediately.",
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getCategory(),
                transaction.getTimestamp()
            );

            sendNotification(transaction.getCustomerId(), subject, message, "INTERNATIONAL_TRANSACTION");

        } catch (Exception e) {
            logger.error("Error sending international transaction notification", e);
        }
    }

    /**
     * Send first-time merchant notification
     */
    public void sendFirstTimeMerchantNotification(TransactionEvent transaction) {
        logger.info("Sending first-time merchant notification for: {}", 
                   transaction.getTransactionId());

        try {
            String subject = "New Merchant Transaction";
            String message = String.format(
                "You've made your first transaction with a new merchant.\n\n" +
                "Transaction ID: %s\n" +
                "Amount: $%s\n" +
                "Merchant Category: %s\n" +
                "Time: %s\n\n" +
                "This is for your security awareness.",
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getCategory(),
                transaction.getTimestamp()
            );

            sendNotification(transaction.getCustomerId(), subject, message, "FIRST_TIME_MERCHANT");

        } catch (Exception e) {
            logger.error("Error sending first-time merchant notification", e);
        }
    }

    /**
     * Send fraud alert notification
     */
    public void sendFraudAlertNotification(TransactionEvent transaction) {
        logger.warn("Sending fraud alert notification for: {}", 
                   transaction.getTransactionId());

        try {
            String subject = "URGENT: Fraudulent Activity Detected";
            String message = String.format(
                "URGENT: We have detected potentially fraudulent activity on your account.\n\n" +
                "Suspicious Transaction:\n" +
                "Transaction ID: %s\n" +
                "Amount: $%s\n" +
                "Category: %s\n" +
                "Time: %s\n\n" +
                "Your account has been temporarily secured for your protection.\n" +
                "Please contact us immediately at our fraud hotline: 1-800-FRAUD-HELP\n\n" +
                "If this transaction was authorized by you, please verify your identity with our customer service.",
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getCategory(),
                transaction.getTimestamp()
            );

            sendUrgentNotification(transaction.getCustomerId(), subject, message, "FRAUD_ALERT");

        } catch (Exception e) {
            logger.error("Error sending fraud alert notification", e);
        }
    }

    /**
     * Send user engagement notification
     */
    public void sendUserEngagementNotification(UserEvent userEvent) {
        logger.info("Sending user engagement notification for user: {}", userEvent.getUserId());

        try {
            String subject = getEngagementSubject(userEvent.getEventType());
            String message = getEngagementMessage(userEvent);

            sendNotification(userEvent.getUserId(), subject, message, "USER_ENGAGEMENT");

        } catch (Exception e) {
            logger.error("Error sending user engagement notification", e);
        }
    }

    /**
     * Send spending limit alert
     */
    public void sendSpendingLimitAlert(String customerId, String limitType, 
                                     String currentSpending, String limit) {
        logger.warn("Sending spending limit alert for customer: {}", customerId);

        try {
            String subject = "Spending Limit Alert";
            String message = String.format(
                "You are approaching your %s spending limit.\n\n" +
                "Current Spending: $%s\n" +
                "Limit: $%s\n" +
                "Time: %s\n\n" +
                "Please monitor your spending to avoid any declined transactions.",
                limitType.toLowerCase().replace("_", " "),
                currentSpending,
                limit,
                LocalDateTime.now()
            );

            sendNotification(customerId, subject, message, "SPENDING_LIMIT_ALERT");

        } catch (Exception e) {
            logger.error("Error sending spending limit alert", e);
        }
    }

    /**
     * Send account security notification
     */
    public void sendAccountSecurityNotification(String customerId, String securityEvent, 
                                               Map<String, Object> details) {
        logger.info("Sending account security notification for customer: {}", customerId);

        try {
            String subject = "Account Security Alert";
            String message = String.format(
                "Important security event on your account:\n\n" +
                "Event: %s\n" +
                "Time: %s\n" +
                "Details: %s\n\n" +
                "If this was not authorized by you, please contact us immediately.",
                securityEvent,
                LocalDateTime.now(),
                formatDetails(details)
            );

            sendNotification(customerId, subject, message, "SECURITY_ALERT");

        } catch (Exception e) {
            logger.error("Error sending account security notification", e);
        }
    }

    /**
     * Generic notification sender
     */
    private void sendNotification(String customerId, String subject, String message, String type) {
        try {
            // Log notification
            logger.info("Sending {} notification to customer: {}", type, customerId);

            // Send email notification
            sendEmailNotification(customerId, subject, message);

            // Send SMS notification (if critical)
            if (isCriticalNotification(type)) {
                sendSMSNotification(customerId, message);
            }

            // Send push notification
            sendPushNotification(customerId, subject, message);

            // Log notification sent
            logNotificationSent(customerId, type, subject);

        } catch (Exception e) {
            logger.error("Error sending notification to customer: {}", customerId, e);
        }
    }

    /**
     * Send urgent notification with multiple channels
     */
    private void sendUrgentNotification(String customerId, String subject, String message, String type) {
        try {
            logger.warn("Sending URGENT {} notification to customer: {}", type, customerId);

            // Send via all available channels for urgent notifications
            sendEmailNotification(customerId, subject, message);
            sendSMSNotification(customerId, message);
            sendPushNotification(customerId, subject, message);
            
            // Could also trigger phone call for fraud alerts
            if ("FRAUD_ALERT".equals(type)) {
                triggerFraudPhoneCall(customerId);
            }

            logNotificationSent(customerId, type + "_URGENT", subject);

        } catch (Exception e) {
            logger.error("Error sending urgent notification to customer: {}", customerId, e);
        }
    }

    /**
     * Send email notification
     */
    private void sendEmailNotification(String customerId, String subject, String message) {
        if (mailSender == null) {
            logger.warn("Mail sender not configured, skipping email notification");
            return;
        }

        try {
            // In production, get email from customer database
            String email = getCustomerEmail(customerId);
            if (email != null) {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setTo(email);
                mailMessage.setSubject(subject);
                mailMessage.setText(message);
                mailMessage.setFrom("noreply@bigdatabank.com");

                mailSender.send(mailMessage);
                logger.info("Email notification sent to: {}", email);
            }

        } catch (Exception e) {
            logger.error("Error sending email notification", e);
        }
    }

    /**
     * Send SMS notification (placeholder implementation)
     */
    private void sendSMSNotification(String customerId, String message) {
        try {
            // In production, integrate with SMS service (Twilio, AWS SNS, etc.)
            String phoneNumber = getCustomerPhone(customerId);
            if (phoneNumber != null) {
                logger.info("SMS notification would be sent to: {} with message: {}", 
                           phoneNumber, message.substring(0, Math.min(message.length(), 50)) + "...");
                
                // SMS sending logic here
                // smsService.sendSMS(phoneNumber, message);
            }

        } catch (Exception e) {
            logger.error("Error sending SMS notification", e);
        }
    }

    /**
     * Send push notification (placeholder implementation)
     */
    private void sendPushNotification(String customerId, String subject, String message) {
        try {
            // In production, integrate with push notification service (Firebase, etc.)
            logger.info("Push notification would be sent to customer: {} with subject: {}", 
                       customerId, subject);
            
            // Push notification sending logic here
            // pushService.sendPush(customerId, subject, message);

        } catch (Exception e) {
            logger.error("Error sending push notification", e);
        }
    }

    /**
     * Trigger fraud phone call (placeholder implementation)
     */
    private void triggerFraudPhoneCall(String customerId) {
        try {
            // In production, integrate with voice service for fraud alerts
            String phoneNumber = getCustomerPhone(customerId);
            if (phoneNumber != null) {
                logger.warn("Fraud phone call would be triggered to: {}", phoneNumber);
                
                // Voice call logic here
                // voiceService.triggerFraudCall(phoneNumber);
            }

        } catch (Exception e) {
            logger.error("Error triggering fraud phone call", e);
        }
    }

    // Helper methods
    private boolean isCriticalNotification(String type) {
        return "FRAUD_ALERT".equals(type) || 
               "SECURITY_ALERT".equals(type) || 
               "SPENDING_LIMIT_ALERT".equals(type);
    }

    private String getEngagementSubject(String eventType) {
        return switch (eventType.toUpperCase()) {
            case "LOGIN" -> "Welcome Back!";
            case "PURCHASE" -> "Thank You for Your Purchase";
            case "PROFILE_UPDATE" -> "Profile Updated Successfully";
            default -> "Account Activity Notification";
        };
    }

    private String getEngagementMessage(UserEvent userEvent) {
        return String.format(
            "Hi! We noticed activity on your account.\n\n" +
            "Event: %s\n" +
            "Time: %s\n" +
            "Session: %s\n\n" +
            "Thank you for being a valued customer!",
            userEvent.getEventType(),
            userEvent.getTimestamp(),
            userEvent.getSessionId()
        );
    }

    private String formatDetails(Map<String, Object> details) {
        if (details == null || details.isEmpty()) {
            return "No additional details";
        }

        StringBuilder sb = new StringBuilder();
        details.forEach((key, value) -> 
            sb.append(key).append(": ").append(value).append("\n"));
        
        return sb.toString();
    }

    private String getCustomerEmail(String customerId) {
        // In production, query database for customer email
        return customerId + "@example.com"; // Placeholder
    }

    private String getCustomerPhone(String customerId) {
        // In production, query database for customer phone
        return "+1-555-0123"; // Placeholder
    }

    private void logNotificationSent(String customerId, String type, String subject) {
        logger.info("Notification logged - Customer: {}, Type: {}, Subject: {}", 
                   customerId, type, subject);
        // In production, save to notifications table in database
    }
}
