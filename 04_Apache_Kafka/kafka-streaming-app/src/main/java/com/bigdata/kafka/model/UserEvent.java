package com.bigdata.kafka.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
    
    @JsonProperty("event_id")
    private String eventId;
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("event_type")
    private String eventType;
    
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    @JsonProperty("data")
    private Map<String, Object> data;
    
    @JsonProperty("user_agent")
    private String userAgent;
    
    @JsonProperty("ip_address")
    private String ipAddress;
    
    @JsonProperty("platform")
    private String platform;
    
    @JsonProperty("app_version")
    private String appVersion;
    
    @JsonProperty("device_id")
    private String deviceId;
    
    @JsonProperty("location")
    private LocationData location;
    
    @JsonProperty("duration_ms")
    private Long durationMs;
    
    @JsonProperty("referrer")
    private String referrer;
    
    @JsonProperty("source")
    private String source;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationData {
        private String country;
        private String region;
        private String city;
        private String timezone;
        private Double latitude;
        private Double longitude;
    }
}
