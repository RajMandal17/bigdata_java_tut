package com.bigdata.kafka.consumer;

import com.bigdata.kafka.model.UserEvent;
import com.bigdata.kafka.service.UserAnalyticsService;
import com.bigdata.kafka.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class UserEventConsumer {
    
    @Autowired
    private UserAnalyticsService userAnalyticsService;
    
    @Autowired
    private RecommendationService recommendationService;
    
    /**
     * Main user event consumer
     */
    @KafkaListener(
        topics = "${kafka.topics.user-events}",
        groupId = "user-event-processors",
        containerFactory = "userEventListenerContainerFactory"
    )
    public void consumeUserEvent(
            @Payload UserEvent userEvent,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.debug("Received user event: {} for user: {} from partition: {}, offset: {}",
                    userEvent.getEventType(), userEvent.getUserId(), partition, offset);
            
            // Process user event
            userAnalyticsService.processUserEvent(userEvent);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing user event: {} for user: {}",
                    userEvent.getEventId(), userEvent.getUserId(), e);
        }
    }
    
    /**
     * Login event processor
     */
    @KafkaListener(
        topics = "${kafka.topics.user-events}",
        groupId = "login-processors",
        containerFactory = "userEventListenerContainerFactory",
        condition = "'login'.equals(#root.payload.eventType)"
    )
    public void consumeLoginEvents(
            @Payload UserEvent userEvent,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Processing login event for user: {} from: {}",
                    userEvent.getUserId(), userEvent.getIpAddress());
            
            // Handle login-specific logic
            userAnalyticsService.processLoginEvent(userEvent);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing login event for user: {}", userEvent.getUserId(), e);
        }
    }
    
    /**
     * Purchase event processor for recommendations
     */
    @KafkaListener(
        topics = "${kafka.topics.user-events}",
        groupId = "recommendation-processors",
        containerFactory = "userEventListenerContainerFactory",
        condition = "'purchase'.equals(#root.payload.eventType)"
    )
    public void consumePurchaseEvents(
            @Payload UserEvent userEvent,
            Acknowledgment acknowledgment) {
        
        try {
            log.debug("Processing purchase event for recommendations: user {}",
                    userEvent.getUserId());
            
            // Update recommendation engine
            recommendationService.updateUserPreferences(userEvent);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing purchase event for user: {}", userEvent.getUserId(), e);
        }
    }
    
    /**
     * Page view analytics processor
     */
    @KafkaListener(
        topics = "${kafka.topics.user-events}",
        groupId = "page-view-analytics",
        containerFactory = "userEventListenerContainerFactory",
        condition = "'page_view'.equals(#root.payload.eventType)"
    )
    public void consumePageViewEvents(
            @Payload UserEvent userEvent,
            Acknowledgment acknowledgment) {
        
        try {
            log.debug("Processing page view event: user {} viewed {}",
                    userEvent.getUserId(), userEvent.getData().get("page"));
            
            // Track page views
            userAnalyticsService.trackPageView(userEvent);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing page view event for user: {}", userEvent.getUserId(), e);
        }
    }
    
    /**
     * Session analytics batch processor
     */
    @KafkaListener(
        topics = "${kafka.topics.user-events}",
        groupId = "session-analytics-batch",
        containerFactory = "batchListenerContainerFactory"
    )
    public void consumeUserEventBatch(
            List<ConsumerRecord<String, UserEvent>> records,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received batch of {} user events for session analytics", records.size());
            
            List<UserEvent> events = records.stream()
                .map(ConsumerRecord::value)
                .toList();
            
            // Process batch for session analytics
            userAnalyticsService.processSessionAnalytics(events);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing user event batch", e);
        }
    }
    
    /**
     * Real-time user behavior processor
     */
    @KafkaListener(
        topics = "${kafka.topics.user-events}",
        groupId = "real-time-behavior",
        containerFactory = "userEventListenerContainerFactory"
    )
    public void consumeForRealTimeBehavior(
            @Payload UserEvent userEvent,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp,
            Acknowledgment acknowledgment) {
        
        try {
            // Real-time behavior analysis
            userAnalyticsService.analyzeRealTimeBehavior(userEvent, timestamp);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error in real-time behavior analysis for user: {}", 
                    userEvent.getUserId(), e);
        }
    }
    
    /**
     * Mobile events processor
     */
    @KafkaListener(
        topics = "${kafka.topics.user-events}",
        groupId = "mobile-processors",
        containerFactory = "userEventListenerContainerFactory",
        condition = "'mobile'.equals(#root.payload.platform)"
    )
    public void consumeMobileEvents(
            @Payload UserEvent userEvent,
            Acknowledgment acknowledgment) {
        
        try {
            log.debug("Processing mobile event: {} for user: {}",
                    userEvent.getEventType(), userEvent.getUserId());
            
            // Mobile-specific processing
            userAnalyticsService.processMobileEvent(userEvent);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing mobile event for user: {}", userEvent.getUserId(), e);
        }
    }
    
    /**
     * User engagement processor
     */
    @KafkaListener(
        topics = "${kafka.topics.user-events}",
        groupId = "engagement-processors",
        containerFactory = "userEventListenerContainerFactory"
    )
    public void consumeForEngagement(
            @Payload UserEvent userEvent,
            Acknowledgment acknowledgment) {
        
        try {
            // Calculate engagement metrics
            userAnalyticsService.updateEngagementMetrics(userEvent);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing engagement for user: {}", userEvent.getUserId(), e);
        }
    }
}
