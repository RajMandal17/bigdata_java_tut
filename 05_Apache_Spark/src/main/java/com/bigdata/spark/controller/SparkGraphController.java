package com.bigdata.spark.controller;

import com.bigdata.spark.graph.SparkGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Apache Spark GraphX operations
 */
@RestController
@RequestMapping("/api/spark/graph")
@CrossOrigin(origins = "*")
public class SparkGraphController {

    @Autowired
    private SparkGraphService sparkGraphService;

    /**
     * Create and analyze a social network graph
     */
    @GetMapping("/social-network")
    public ResponseEntity<Map<String, Object>> createSocialNetworkGraph() {
        try {
            Map<String, Object> result = sparkGraphService.createSocialNetworkGraph();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to create social network graph: " + e.getMessage()));
        }
    }

    /**
     * Analyze graph properties and metrics
     */
    @GetMapping("/properties")
    public ResponseEntity<Map<String, Object>> analyzeGraphProperties() {
        try {
            Map<String, Object> result = sparkGraphService.analyzeGraphProperties();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to analyze graph properties: " + e.getMessage()));
        }
    }

    /**
     * Perform PageRank algorithm
     */
    @GetMapping("/pagerank")
    public ResponseEntity<Map<String, Object>> performPageRank() {
        try {
            Map<String, Object> result = sparkGraphService.performPageRank();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to perform PageRank: " + e.getMessage()));
        }
    }

    /**
     * Find connected components in graph
     */
    @GetMapping("/connected-components")
    public ResponseEntity<Map<String, Object>> findConnectedComponents() {
        try {
            Map<String, Object> result = sparkGraphService.findConnectedComponents();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to find connected components: " + e.getMessage()));
        }
    }

    /**
     * Count triangles in graph
     */
    @GetMapping("/triangles")
    public ResponseEntity<Map<String, Object>> countTriangles() {
        try {
            Map<String, Object> result = sparkGraphService.countTriangles();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to count triangles: " + e.getMessage()));
        }
    }

    /**
     * Analyze citation network
     */
    @GetMapping("/citation-network")
    public ResponseEntity<Map<String, Object>> analyzeCitationNetwork() {
        try {
            Map<String, Object> result = sparkGraphService.analyzeCitationNetwork();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to analyze citation network: " + e.getMessage()));
        }
    }
}
