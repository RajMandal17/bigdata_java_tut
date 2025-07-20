package com.bigdata.spark.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Streaming Event Model for real-time data processing
 * Used for Kafka streaming and real-time analytics
 */
public class StreamingEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("event_id")
    private String eventId;
    
    @JsonProperty("event_type")
    private String eventType;
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("event_data")
    private Map<String, Object> eventData;
    
    @JsonProperty("event_timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTimestamp;
    
    @JsonProperty("source")
    private String source;
    
    @JsonProperty("ip_address")
    private String ipAddress;
    
    @JsonProperty("user_agent")
    private String userAgent;

    // Default constructor
    public StreamingEvent() {}

    // Constructor
    public StreamingEvent(String eventId, String eventType, String userId, 
                         String sessionId, Map<String, Object> eventData, 
                         LocalDateTime eventTimestamp, String source, 
                         String ipAddress, String userAgent) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.userId = userId;
        this.sessionId = sessionId;
        this.eventData = eventData;
        this.eventTimestamp = eventTimestamp;
        this.source = source;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Map<String, Object> getEventData() { return eventData; }
    public void setEventData(Map<String, Object> eventData) { this.eventData = eventData; }

    public LocalDateTime getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(LocalDateTime eventTimestamp) { this.eventTimestamp = eventTimestamp; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    @Override
    public String toString() {
        return "StreamingEvent{" +
                "eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", userId='" + userId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", eventData=" + eventData +
                ", eventTimestamp=" + eventTimestamp +
                ", source='" + source + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    }
}
