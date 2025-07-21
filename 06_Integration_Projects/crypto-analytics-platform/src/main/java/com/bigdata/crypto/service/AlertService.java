package com.bigdata.crypto.service;

import com.bigdata.crypto.fraud.FraudAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AlertService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    public void sendFraudAlert(FraudAlert alert) {
        try {
            logger.warn("FRAUD ALERT: {} - User: {}, Risk Score: {}", 
                       alert.getType(), alert.getUserId(), alert.getRiskScore());
            
            // Send to Kafka topic for real-time processing
            kafkaTemplate.send("fraud-alerts", alert.getUserId(), alert);
            
            // Send email notification for high-risk alerts
            if (alert.getRiskScore() > 70) {
                sendEmailAlert(alert);
            }
            
            // Store in database (simplified)
            storeAlert(alert);
            
        } catch (Exception e) {
            logger.error("Error sending fraud alert: {}", alert, e);
        }
    }
    
    public void sendUrgentAlert(FraudAlert alert) {
        try {
            logger.error("URGENT FRAUD ALERT: {} - User: {}, Risk Score: {}", 
                        alert.getType(), alert.getUserId(), alert.getRiskScore());
            
            // Send to urgent alerts topic
            kafkaTemplate.send("urgent-alerts", alert.getUserId(), alert);
            
            // Send immediate email notification
            sendEmailAlert(alert);
            
            // Could also integrate with Slack, PagerDuty, etc.
            sendSlackAlert(alert);
            
        } catch (Exception e) {
            logger.error("Error sending urgent fraud alert: {}", alert, e);
        }
    }
    
    public void sendAlert(FraudAlert alert) {
        try {
            logger.info("ALERT: {} - User: {}, Risk Score: {}", 
                       alert.getType(), alert.getUserId(), alert.getRiskScore());
            
            // Send to general alerts topic
            kafkaTemplate.send("general-alerts", alert.getUserId(), alert);
            
            // Store in database
            storeAlert(alert);
            
        } catch (Exception e) {
            logger.error("Error sending alert: {}", alert, e);
        }
    }
    
    private void sendEmailAlert(FraudAlert alert) {
        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo("compliance@cryptoexchange.com");
                message.setSubject("Fraud Alert: " + alert.getType());
                message.setText(createEmailBody(alert));
                
                mailSender.send(message);
                logger.info("Email alert sent for: {}", alert.getAlertId());
            }
        } catch (Exception e) {
            logger.error("Error sending email alert", e);
        }
    }
    
    private void sendSlackAlert(FraudAlert alert) {
        try {
            // Simplified Slack integration
            logger.info("Would send Slack alert for: {}", alert.getType());
            // In production, integrate with Slack API
        } catch (Exception e) {
            logger.error("Error sending Slack alert", e);
        }
    }
    
    private void storeAlert(FraudAlert alert) {
        try {
            // Simplified storage - in production, store in database
            logger.debug("Storing alert: {}", alert.getAlertId());
        } catch (Exception e) {
            logger.error("Error storing alert", e);
        }
    }
    
    private String createEmailBody(FraudAlert alert) {
        return String.format(
            "Fraud Alert Details:\n\n" +
            "Type: %s\n" +
            "User ID: %s\n" +
            "Trade ID: %s\n" +
            "Risk Score: %.2f\n" +
            "Risk Factors: %s\n" +
            "Recommended Action: %s\n" +
            "Timestamp: %s\n\n" +
            "Please investigate immediately.",
            alert.getType(),
            alert.getUserId(),
            alert.getTradeId(),
            alert.getRiskScore(),
            alert.getRiskFactors(),
            alert.getRecommendedAction(),
            alert.getTimestamp()
        );
    }
}
