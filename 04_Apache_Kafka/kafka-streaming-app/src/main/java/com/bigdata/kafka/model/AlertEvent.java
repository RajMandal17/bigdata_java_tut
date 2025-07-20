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
public class AlertEvent {
    
    @JsonProperty("alert_id")
    private String alertId;
    
    @JsonProperty("alert_type")
    private String alertType;
    
    @JsonProperty("severity")
    private AlertSeverity severity;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    @JsonProperty("source_system")
    private String sourceSystem;
    
    @JsonProperty("entity_id")
    private String entityId;
    
    @JsonProperty("entity_type")
    private String entityType;
    
    @JsonProperty("data")
    private Map<String, Object> data;
    
    @JsonProperty("threshold_value")
    private Double thresholdValue;
    
    @JsonProperty("actual_value")
    private Double actualValue;
    
    @JsonProperty("tags")
    private Map<String, String> tags;
    
    @JsonProperty("resolved")
    private boolean resolved;
    
    @JsonProperty("resolution_timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime resolutionTimestamp;
    
    public enum AlertSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
