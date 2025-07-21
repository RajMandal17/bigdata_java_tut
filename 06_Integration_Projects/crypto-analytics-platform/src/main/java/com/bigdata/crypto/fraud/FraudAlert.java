package com.bigdata.crypto.fraud;

import java.time.Instant;
import java.util.List;

public class FraudAlert {
    
    private String alertId;
    private String type;
    private String tradeId;
    private String orderId;
    private String userId;
    private String symbol;
    private double riskScore;
    private List<String> riskFactors;
    private String recommendedAction;
    private Instant timestamp;
    private String status;
    private String assignedTo;
    
    public FraudAlert() {
        this.timestamp = Instant.now();
        this.status = "OPEN";
    }
    
    public static FraudAlertBuilder builder() {
        return new FraudAlertBuilder();
    }
    
    public static class FraudAlertBuilder {
        private FraudAlert alert = new FraudAlert();
        
        public FraudAlertBuilder alertId(String alertId) {
            alert.alertId = alertId;
            return this;
        }
        
        public FraudAlertBuilder type(String type) {
            alert.type = type;
            return this;
        }
        
        public FraudAlertBuilder tradeId(String tradeId) {
            alert.tradeId = tradeId;
            return this;
        }
        
        public FraudAlertBuilder orderId(String orderId) {
            alert.orderId = orderId;
            return this;
        }
        
        public FraudAlertBuilder userId(String userId) {
            alert.userId = userId;
            return this;
        }
        
        public FraudAlertBuilder symbol(String symbol) {
            alert.symbol = symbol;
            return this;
        }
        
        public FraudAlertBuilder riskScore(double riskScore) {
            alert.riskScore = riskScore;
            return this;
        }
        
        public FraudAlertBuilder riskFactors(List<String> riskFactors) {
            alert.riskFactors = riskFactors;
            return this;
        }
        
        public FraudAlertBuilder recommendedAction(String recommendedAction) {
            alert.recommendedAction = recommendedAction;
            return this;
        }
        
        public FraudAlertBuilder timestamp(Instant timestamp) {
            alert.timestamp = timestamp;
            return this;
        }
        
        public FraudAlertBuilder status(String status) {
            alert.status = status;
            return this;
        }
        
        public FraudAlertBuilder assignedTo(String assignedTo) {
            alert.assignedTo = assignedTo;
            return this;
        }
        
        public FraudAlert build() {
            return alert;
        }
    }
    
    // Getters and Setters
    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getTradeId() { return tradeId; }
    public void setTradeId(String tradeId) { this.tradeId = tradeId; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }
    
    public List<String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }
    
    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
}

enum RiskLevel {
    LOW, MEDIUM, HIGH
}

enum VelocityRiskLevel {
    LOW, MEDIUM, HIGH
}

class RiskScore {
    private double score;
    private List<String> riskFactors;
    private String description;
    
    public RiskScore(double score, List<String> riskFactors, String description) {
        this.score = score;
        this.riskFactors = riskFactors;
        this.description = description;
    }
    
    public double getScore() { return score; }
    public List<String> getRiskFactors() { return riskFactors; }
    public String getDescription() { return description; }
}

class AnomalyScore {
    private double score;
    private List<String> riskFactors;
    private String anomalyType;
    private double confidence;
    
    public AnomalyScore(double score, List<String> riskFactors, String anomalyType, double confidence) {
        this.score = score;
        this.riskFactors = riskFactors;
        this.anomalyType = anomalyType;
        this.confidence = confidence;
    }
    
    public double getScore() { return score; }
    public List<String> getRiskFactors() { return riskFactors; }
    public String getAnomalyType() { return anomalyType; }
    public double getConfidence() { return confidence; }
}

class TradingBehaviorScore {
    private double tradingFrequency;
    private double volumeDeviation;
    private double timePatternAnomaly;
    private double priceDeviationScore;
    private boolean washTradingFlag;
    private boolean frontRunningFlag;
    private boolean layeringFlag;
    private boolean spoofingFlag;
    private double overallScore;
    
    public static TradingBehaviorScoreBuilder builder() {
        return new TradingBehaviorScoreBuilder();
    }
    
    public static class TradingBehaviorScoreBuilder {
        private TradingBehaviorScore score = new TradingBehaviorScore();
        
        public TradingBehaviorScoreBuilder tradingFrequency(double tradingFrequency) {
            score.tradingFrequency = tradingFrequency;
            return this;
        }
        
        public TradingBehaviorScoreBuilder volumeDeviation(double volumeDeviation) {
            score.volumeDeviation = volumeDeviation;
            return this;
        }
        
        public TradingBehaviorScoreBuilder timePatternAnomaly(double timePatternAnomaly) {
            score.timePatternAnomaly = timePatternAnomaly;
            return this;
        }
        
        public TradingBehaviorScoreBuilder priceDeviationScore(double priceDeviationScore) {
            score.priceDeviationScore = priceDeviationScore;
            return this;
        }
        
        public TradingBehaviorScoreBuilder washTradingFlag(boolean washTradingFlag) {
            score.washTradingFlag = washTradingFlag;
            return this;
        }
        
        public TradingBehaviorScoreBuilder frontRunningFlag(boolean frontRunningFlag) {
            score.frontRunningFlag = frontRunningFlag;
            return this;
        }
        
        public TradingBehaviorScoreBuilder layeringFlag(boolean layeringFlag) {
            score.layeringFlag = layeringFlag;
            return this;
        }
        
        public TradingBehaviorScoreBuilder spoofingFlag(boolean spoofingFlag) {
            score.spoofingFlag = spoofingFlag;
            return this;
        }
        
        public TradingBehaviorScoreBuilder overallScore(double overallScore) {
            score.overallScore = overallScore;
            return this;
        }
        
        public TradingBehaviorScore build() {
            return score;
        }
    }
    
    public List<String> getRiskFactors() {
        List<String> factors = new java.util.ArrayList<>();
        if (washTradingFlag) factors.add("WASH_TRADING");
        if (frontRunningFlag) factors.add("FRONT_RUNNING");
        if (layeringFlag) factors.add("LAYERING");
        if (spoofingFlag) factors.add("SPOOFING");
        if (tradingFrequency > 50) factors.add("HIGH_FREQUENCY");
        if (volumeDeviation > 1000000) factors.add("HIGH_VOLUME_DEVIATION");
        return factors;
    }
    
    // Getters
    public double getTradingFrequency() { return tradingFrequency; }
    public double getVolumeDeviation() { return volumeDeviation; }
    public double getTimePatternAnomaly() { return timePatternAnomaly; }
    public double getPriceDeviationScore() { return priceDeviationScore; }
    public boolean isWashTradingFlag() { return washTradingFlag; }
    public boolean isFrontRunningFlag() { return frontRunningFlag; }
    public boolean isLayeringFlag() { return layeringFlag; }
    public boolean isSpoofingFlag() { return spoofingFlag; }
    public double getOverallScore() { return overallScore; }
}

class VelocityCheckResult {
    private double tradeVelocity;
    private double volumeVelocity;
    private VelocityRiskLevel tradeRisk;
    private VelocityRiskLevel volumeRisk;
    private VelocityRiskLevel overallRisk;
    
    public static VelocityCheckResultBuilder builder() {
        return new VelocityCheckResultBuilder();
    }
    
    public static class VelocityCheckResultBuilder {
        private VelocityCheckResult result = new VelocityCheckResult();
        
        public VelocityCheckResultBuilder tradeVelocity(double tradeVelocity) {
            result.tradeVelocity = tradeVelocity;
            return this;
        }
        
        public VelocityCheckResultBuilder volumeVelocity(double volumeVelocity) {
            result.volumeVelocity = volumeVelocity;
            return this;
        }
        
        public VelocityCheckResultBuilder tradeRisk(VelocityRiskLevel tradeRisk) {
            result.tradeRisk = tradeRisk;
            return this;
        }
        
        public VelocityCheckResultBuilder volumeRisk(VelocityRiskLevel volumeRisk) {
            result.volumeRisk = volumeRisk;
            return this;
        }
        
        public VelocityCheckResultBuilder overallRisk(VelocityRiskLevel overallRisk) {
            result.overallRisk = overallRisk;
            return this;
        }
        
        public VelocityCheckResult build() {
            return result;
        }
    }
    
    public List<String> getRiskFactors() {
        List<String> factors = new java.util.ArrayList<>();
        if (tradeRisk == VelocityRiskLevel.HIGH) factors.add("HIGH_TRADE_VELOCITY");
        if (volumeRisk == VelocityRiskLevel.HIGH) factors.add("HIGH_VOLUME_VELOCITY");
        return factors;
    }
    
    // Getters
    public double getTradeVelocity() { return tradeVelocity; }
    public double getVolumeVelocity() { return volumeVelocity; }
    public VelocityRiskLevel getTradeRisk() { return tradeRisk; }
    public VelocityRiskLevel getVolumeRisk() { return volumeRisk; }
    public VelocityRiskLevel getOverallRisk() { return overallRisk; }
}

class OrderRiskAssessment {
    private String orderId;
    private double riskScore;
    private RiskLevel riskLevel;
    private List<String> riskFactors;
    private Instant timestamp;
    
    public static OrderRiskAssessmentBuilder builder() {
        return new OrderRiskAssessmentBuilder();
    }
    
    public static class OrderRiskAssessmentBuilder {
        private OrderRiskAssessment assessment = new OrderRiskAssessment();
        
        public OrderRiskAssessmentBuilder orderId(String orderId) {
            assessment.orderId = orderId;
            return this;
        }
        
        public OrderRiskAssessmentBuilder riskScore(double riskScore) {
            assessment.riskScore = riskScore;
            return this;
        }
        
        public OrderRiskAssessmentBuilder riskLevel(RiskLevel riskLevel) {
            assessment.riskLevel = riskLevel;
            return this;
        }
        
        public OrderRiskAssessmentBuilder riskFactors(List<String> riskFactors) {
            assessment.riskFactors = riskFactors;
            return this;
        }
        
        public OrderRiskAssessmentBuilder timestamp(Instant timestamp) {
            assessment.timestamp = timestamp;
            return this;
        }
        
        public OrderRiskAssessment build() {
            return assessment;
        }
    }
    
    // Getters
    public String getOrderId() { return orderId; }
    public double getRiskScore() { return riskScore; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public List<String> getRiskFactors() { return riskFactors; }
    public Instant getTimestamp() { return timestamp; }
}

class ComprehensiveRiskAssessment {
    private double combinedScore;
    private RiskLevel riskLevel;
    private List<String> riskFactors;
    private RiskScore ruleBasedScore;
    private AnomalyScore mlAnomalyScore;
    private TradingBehaviorScore behaviorScore;
    private VelocityCheckResult velocityResult;
    private Instant timestamp;
    
    public static ComprehensiveRiskAssessmentBuilder builder() {
        return new ComprehensiveRiskAssessmentBuilder();
    }
    
    public static class ComprehensiveRiskAssessmentBuilder {
        private ComprehensiveRiskAssessment assessment = new ComprehensiveRiskAssessment();
        
        public ComprehensiveRiskAssessmentBuilder combinedScore(double combinedScore) {
            assessment.combinedScore = combinedScore;
            return this;
        }
        
        public ComprehensiveRiskAssessmentBuilder riskLevel(RiskLevel riskLevel) {
            assessment.riskLevel = riskLevel;
            return this;
        }
        
        public ComprehensiveRiskAssessmentBuilder riskFactors(List<String> riskFactors) {
            assessment.riskFactors = riskFactors;
            return this;
        }
        
        public ComprehensiveRiskAssessmentBuilder ruleBasedScore(RiskScore ruleBasedScore) {
            assessment.ruleBasedScore = ruleBasedScore;
            return this;
        }
        
        public ComprehensiveRiskAssessmentBuilder mlAnomalyScore(AnomalyScore mlAnomalyScore) {
            assessment.mlAnomalyScore = mlAnomalyScore;
            return this;
        }
        
        public ComprehensiveRiskAssessmentBuilder behaviorScore(TradingBehaviorScore behaviorScore) {
            assessment.behaviorScore = behaviorScore;
            return this;
        }
        
        public ComprehensiveRiskAssessmentBuilder velocityResult(VelocityCheckResult velocityResult) {
            assessment.velocityResult = velocityResult;
            return this;
        }
        
        public ComprehensiveRiskAssessmentBuilder timestamp(Instant timestamp) {
            assessment.timestamp = timestamp;
            return this;
        }
        
        public ComprehensiveRiskAssessment build() {
            return assessment;
        }
    }
    
    // Getters
    public double getCombinedScore() { return combinedScore; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public List<String> getRiskFactors() { return riskFactors; }
    public RiskScore getRuleBasedScore() { return ruleBasedScore; }
    public AnomalyScore getMlAnomalyScore() { return mlAnomalyScore; }
    public TradingBehaviorScore getBehaviorScore() { return behaviorScore; }
    public VelocityCheckResult getVelocityResult() { return velocityResult; }
    public Instant getTimestamp() { return timestamp; }
}
