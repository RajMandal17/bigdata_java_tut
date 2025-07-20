package com.bigdata.foundations.mongodb.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@Document(collection = "events")
@CompoundIndexes({
    @CompoundIndex(name = "user_timestamp_idx", def = "{'userId' : 1, 'timestamp' : -1}"),
    @CompoundIndex(name = "event_processed_idx", def = "{'eventType' : 1, 'processed' : 1, 'timestamp' : -1}")
})
public class Event {
    
    @Id
    private String id;
    
    @Indexed
    private String eventId;
    
    @Indexed
    private String userId;
    
    @Indexed
    private String eventType;
    
    @Indexed
    @CreatedDate
    private LocalDateTime timestamp;
    
    private Map<String, Object> data;
    private Map<String, Object> metadata;
    private List<String> tags;
    
    @Indexed
    private boolean processed = false;
    
    private String source;
    private String version;
    
    // Constructors
    public Event() {
        this.timestamp = LocalDateTime.now();
    }
    
    public Event(String eventId, String userId, String eventType, Map<String, Object> data) {
        this();
        this.eventId = eventId;
        this.userId = userId;
        this.eventType = eventType;
        this.data = data;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
