package com.bigdata.foundations.service;

import com.bigdata.foundations.mysql.entity.User;
import com.bigdata.foundations.mysql.repository.UserRepository;
import com.bigdata.foundations.mongodb.document.Event;
import com.bigdata.foundations.mongodb.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class DataIntegrationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    // Polyglot persistence example
    public User createUserWithActivity(User user, String initialActivity) {
        // Save user in MySQL
        User savedUser = userRepository.save(user);
        
        // Create event in MongoDB
        Event event = new Event();
        event.setEventId("evt_" + System.currentTimeMillis());
        event.setUserId(savedUser.getId().toString());
        event.setEventType("user_registration");
        event.setTimestamp(LocalDateTime.now());
        
        Map<String, Object> data = new HashMap<>();
        data.put("email", savedUser.getEmail());
        data.put("country", savedUser.getCountry());
        data.put("initialActivity", initialActivity);
        event.setData(data);
        
        eventRepository.save(event);
        
        // Send notification via Kafka
        String message = String.format(
            "{\"userId\":\"%s\",\"email\":\"%s\",\"event\":\"registration\",\"timestamp\":\"%s\"}",
            savedUser.getId(), savedUser.getEmail(), LocalDateTime.now()
        );
        kafkaTemplate.send("user-events", savedUser.getId().toString(), message);
        
        return savedUser;
    }
    
    @Cacheable(value = "userStats", key = "#userId")
    public Map<String, Object> getUserAnalytics(String userId) {
        Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));
        if (userOpt.isEmpty()) {
            return Map.of("error", "User not found");
        }
        
        User user = userOpt.get();
        List<Event> events = eventRepository.findByUserId(userId);
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("user", user);
        analytics.put("totalEvents", events.size());
        analytics.put("eventTypes", events.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                Event::getEventType,
                java.util.stream.Collectors.counting()
            ))
        );
        
        if (!events.isEmpty()) {
            analytics.put("firstActivity", events.stream()
                .map(Event::getTimestamp)
                .min(LocalDateTime::compareTo)
                .orElse(null));
            analytics.put("lastActivity", events.stream()
                .map(Event::getTimestamp)
                .max(LocalDateTime::compareTo)
                .orElse(null));
        }
        
        return analytics;
    }
    
    @CacheEvict(value = "userStats", key = "#userId")
    public void invalidateUserCache(String userId) {
        // This method will clear the cache for the user
    }
    
    public Page<User> getActiveUsers(Pageable pageable) {
        return userRepository.findByStatus(User.UserStatus.ACTIVE, pageable);
    }
    
    public List<Event> getRecentUserActivity(String userId, int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return eventRepository.findByUserIdAndTimestampAfter(userId, cutoff);
    }
    
    // Batch processing example
    public void processUnprocessedEvents() {
        List<Event> unprocessedEvents = eventRepository.findByProcessedFalse();
        
        for (Event event : unprocessedEvents) {
            try {
                // Process the event (example: send to analytics service)
                processEvent(event);
                
                // Mark as processed
                event.setProcessed(true);
                eventRepository.save(event);
                
                // Send processed event notification
                String message = String.format(
                    "{\"eventId\":\"%s\",\"userId\":\"%s\",\"type\":\"%s\",\"status\":\"processed\"}",
                    event.getEventId(), event.getUserId(), event.getEventType()
                );
                kafkaTemplate.send("processed-events", event.getEventId(), message);
                
            } catch (Exception e) {
                // Log error and continue with next event
                System.err.println("Failed to process event: " + event.getEventId() + ", Error: " + e.getMessage());
            }
        }
    }
    
    private void processEvent(Event event) {
        // Example processing logic
        if ("purchase".equals(event.getEventType())) {
            // Update user purchase history
            // Calculate recommendations
            // Update analytics
        } else if ("login".equals(event.getEventType())) {
            // Update last login time
            // Check for suspicious activity
        }
        // Add more processing logic as needed
    }
    
    // Analytics aggregation example
    public Map<String, Object> getDashboardData(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> dashboard = new HashMap<>();
        
        // User statistics from MySQL
        List<Object[]> userStats = userRepository.getUserStatsByCountry(startDate);
        dashboard.put("usersByCountry", userStats);
        
        List<Object[]> dailyRegistrations = userRepository.getDailyRegistrationStats(startDate, endDate);
        dashboard.put("dailyRegistrations", dailyRegistrations);
        
        // Event statistics from MongoDB
        List<EventRepository.DailyEventCount> eventCounts = eventRepository.getDailyEventCounts(startDate, endDate);
        dashboard.put("dailyEventCounts", eventCounts);
        
        List<EventRepository.CategoryEventSummary> categoryStats = eventRepository.getEventSummaryByCategory("purchase");
        dashboard.put("purchasesByCategory", categoryStats);
        
        return dashboard;
    }
}
