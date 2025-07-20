package com.bigdata.foundations.mongodb.repository;

import com.bigdata.foundations.mongodb.document.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
    
    // Basic finder methods
    List<Event> findByUserId(String userId);
    
    List<Event> findByEventType(String eventType);
    
    List<Event> findByProcessedFalse();
    
    // Date range queries
    List<Event> findByUserIdAndTimestampBetween(
        String userId, 
        LocalDateTime start, 
        LocalDateTime end);
    
    List<Event> findByEventTypeAndTimestampAfter(
        String eventType, 
        LocalDateTime after);
    
    // Pagination support
    Page<Event> findByUserId(String userId, Pageable pageable);
    
    Page<Event> findByEventTypeAndProcessedFalse(String eventType, Pageable pageable);
    
    // Complex queries with @Query
    @Query("{'userId': ?0, 'eventType': ?1, 'timestamp': {$gte: ?2, $lte: ?3}}")
    List<Event> findUserEventsByTypeAndDateRange(
        String userId, 
        String eventType, 
        LocalDateTime startDate, 
        LocalDateTime endDate);
    
    @Query("{'data.productId': ?0, 'eventType': 'purchase'}")
    List<Event> findPurchaseEventsByProductId(String productId);
    
    @Query("{'tags': {$in: ?0}, 'processed': false}")
    List<Event> findUnprocessedEventsByTags(List<String> tags);
    
    // Geospatial queries (if location data exists)
    @Query("{'metadata.location': {$near: {$geometry: {type: 'Point', coordinates: [?0, ?1]}, $maxDistance: ?2}}}")
    List<Event> findEventsNearLocation(double longitude, double latitude, double maxDistance);
    
    // Aggregation pipelines
    @Aggregation(pipeline = {
        "{ $match: { eventType: ?0, timestamp: { $gte: ?1, $lte: ?2 } } }",
        "{ $group: { _id: '$userId', eventCount: { $sum: 1 }, lastActivity: { $max: '$timestamp' } } }",
        "{ $sort: { eventCount: -1 } }",
        "{ $limit: ?3 }"
    })
    List<UserActivitySummary> getTopActiveUsers(
        String eventType, 
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        int limit);
    
    @Aggregation(pipeline = {
        "{ $match: { timestamp: { $gte: ?0, $lte: ?1 } } }",
        "{ $group: { _id: { $dateToString: { format: '%Y-%m-%d', date: '$timestamp' } }, count: { $sum: 1 } } }",
        "{ $sort: { '_id': 1 } }"
    })
    List<DailyEventCount> getDailyEventCounts(LocalDateTime startDate, LocalDateTime endDate);
    
    @Aggregation(pipeline = {
        "{ $match: { eventType: ?0 } }",
        "{ $group: { _id: '$data.category', totalEvents: { $sum: 1 }, uniqueUsers: { $addToSet: '$userId' } } }",
        "{ $project: { _id: 1, totalEvents: 1, uniqueUsers: { $size: '$uniqueUsers' } } }",
        "{ $sort: { totalEvents: -1 } }"
    })
    List<CategoryEventSummary> getEventSummaryByCategory(String eventType);
    
    // Count queries
    long countByEventType(String eventType);
    
    long countByUserIdAndEventType(String userId, String eventType);
    
    long countByProcessedFalse();
    
    // Bulk operations helper methods
    @Query(value = "{'processed': false}", delete = true)
    void deleteUnprocessedEvents();
    
    // DTO classes for aggregation results
    interface UserActivitySummary {
        String getUserId();
        int getEventCount();
        LocalDateTime getLastActivity();
    }
    
    interface DailyEventCount {
        String getDate();
        int getCount();
    }
    
    interface CategoryEventSummary {
        String getCategory();
        int getTotalEvents();
        int getUniqueUsers();
    }
}
