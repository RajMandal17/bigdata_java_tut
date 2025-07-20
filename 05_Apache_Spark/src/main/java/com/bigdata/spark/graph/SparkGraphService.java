package com.bigdata.spark.graph;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.graphx.*;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.storage.StorageLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Tuple2;
import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Apache Spark GraphX operations
 * Provides graph processing capabilities for network analysis
 */
@Service
public class SparkGraphService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private SparkSession sparkSession;

    @Autowired
    private JavaSparkContext javaSparkContext;

    /**
     * Create a sample social network graph
     */
    public Map<String, Object> createSocialNetworkGraph() {
        try {
            // Create vertices (users)
            List<Tuple2<Object, String>> vertices = Arrays.asList(
                new Tuple2<>(1L, "Alice"),
                new Tuple2<>(2L, "Bob"),
                new Tuple2<>(3L, "Charlie"),
                new Tuple2<>(4L, "David"),
                new Tuple2<>(5L, "Eve")
            );

            // Create edges (friendships)
            List<Edge<String>> edges = Arrays.asList(
                new Edge<>(1L, 2L, "friend"),
                new Edge<>(2L, 3L, "friend"),
                new Edge<>(3L, 4L, "friend"),
                new Edge<>(4L, 5L, "friend"),
                new Edge<>(5L, 1L, "friend"),
                new Edge<>(2L, 4L, "friend")
            );

            JavaRDD<Tuple2<Object, String>> verticesRDD = javaSparkContext.parallelize(vertices);
            JavaRDD<Edge<String>> edgesRDD = javaSparkContext.parallelize(edges);

            // Convert to Scala RDDs for GraphX
            Graph<String, String> graph = Graph.apply(
                verticesRDD.rdd(),
                edgesRDD.rdd(),
                "Unknown",
                StorageLevel.MEMORY_AND_DISK(),
                StorageLevel.MEMORY_AND_DISK(),
                ClassTag$.MODULE$.apply(String.class),
                ClassTag$.MODULE$.apply(String.class)
            );

            Map<String, Object> result = new HashMap<>();
            result.put("vertexCount", graph.vertices().count());
            result.put("edgeCount", graph.edges().count());
            result.put("vertices", vertices.stream()
                .map(v -> Map.of("id", v._1(), "name", v._2()))
                .collect(Collectors.toList()));
            result.put("edges", edges.stream()
                .map(e -> Map.of("src", e.srcId(), "dst", e.dstId(), "relationship", e.attr()))
                .collect(Collectors.toList()));

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error creating social network graph", e);
        }
    }

    /**
     * Analyze graph properties
     */
    public Map<String, Object> analyzeGraphProperties() {
        try {
            // Create a larger graph for analysis
            List<Tuple2<Object, String>> vertices = new ArrayList<>();
            List<Edge<String>> edges = new ArrayList<>();

            // Create vertices
            for (int i = 1; i <= 10; i++) {
                vertices.add(new Tuple2<>((long) i, "User" + i));
            }

            // Create random edges
            Random random = new Random(42);
            for (int i = 1; i <= 10; i++) {
                for (int j = i + 1; j <= 10; j++) {
                    if (random.nextDouble() < 0.3) { // 30% probability of connection
                        edges.add(new Edge<>((long) i, (long) j, "connection"));
                    }
                }
            }

            JavaRDD<Tuple2<Object, String>> verticesRDD = javaSparkContext.parallelize(vertices);
            JavaRDD<Edge<String>> edgesRDD = javaSparkContext.parallelize(edges);

            Graph<String, String> graph = Graph.apply(
                verticesRDD.rdd(),
                edgesRDD.rdd(),
                "Unknown",
                StorageLevel.MEMORY_AND_DISK(),
                StorageLevel.MEMORY_AND_DISK(),
                ClassTag$.MODULE$.apply(String.class),
                ClassTag$.MODULE$.apply(String.class)
            );

            // Calculate graph metrics
            Map<String, Object> result = new HashMap<>();
            result.put("vertexCount", graph.vertices().count());
            result.put("edgeCount", graph.edges().count());
            result.put("maxDegree", graph.degrees().values().max(scala.math.Ordering.Long()));

            // Calculate in-degrees and out-degrees
            result.put("avgInDegree", graph.inDegrees().values().mean());
            result.put("avgOutDegree", graph.outDegrees().values().mean());

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error analyzing graph properties", e);
        }
    }

    /**
     * Perform PageRank algorithm on graph
     */
    public Map<String, Object> performPageRank() {
        try {
            // Create a web graph
            List<Tuple2<Object, String>> vertices = Arrays.asList(
                new Tuple2<>(1L, "Page A"),
                new Tuple2<>(2L, "Page B"),
                new Tuple2<>(3L, "Page C"),
                new Tuple2<>(4L, "Page D")
            );

            List<Edge<Double>> edges = Arrays.asList(
                new Edge<>(1L, 2L, 1.0),
                new Edge<>(1L, 3L, 1.0),
                new Edge<>(2L, 3L, 1.0),
                new Edge<>(3L, 1L, 1.0),
                new Edge<>(3L, 4L, 1.0),
                new Edge<>(4L, 1L, 1.0)
            );

            JavaRDD<Tuple2<Object, String>> verticesRDD = javaSparkContext.parallelize(vertices);
            JavaRDD<Edge<Double>> edgesRDD = javaSparkContext.parallelize(edges);

            Graph<String, Double> graph = Graph.apply(
                verticesRDD.rdd(),
                edgesRDD.rdd(),
                "Unknown",
                StorageLevel.MEMORY_AND_DISK(),
                StorageLevel.MEMORY_AND_DISK(),
                ClassTag$.MODULE$.apply(String.class),
                ClassTag$.MODULE$.apply(Double.class)
            );

            // Run PageRank
            Graph<Double, Double> pageRankGraph = graph.staticPageRank(10);

            // Collect results
            List<Tuple2<Object, Double>> pageRankResults = pageRankGraph.vertices().toJavaRDD().collect();

            Map<String, Object> result = new HashMap<>();
            result.put("algorithm", "PageRank");
            result.put("iterations", 10);
            result.put("results", pageRankResults.stream()
                .map(pr -> Map.of("vertexId", pr._1(), "pageRank", pr._2()))
                .sorted((a, b) -> Double.compare((Double) b.get("pageRank"), (Double) a.get("pageRank")))
                .collect(Collectors.toList()));

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error performing PageRank", e);
        }
    }

    /**
     * Find connected components in graph
     */
    public Map<String, Object> findConnectedComponents() {
        try {
            // Create a graph with multiple components
            List<Tuple2<Object, String>> vertices = Arrays.asList(
                new Tuple2<>(1L, "Node1"), new Tuple2<>(2L, "Node2"), new Tuple2<>(3L, "Node3"),
                new Tuple2<>(4L, "Node4"), new Tuple2<>(5L, "Node5"), new Tuple2<>(6L, "Node6")
            );

            List<Edge<String>> edges = Arrays.asList(
                new Edge<>(1L, 2L, "connected"),
                new Edge<>(2L, 3L, "connected"),
                new Edge<>(4L, 5L, "connected")
                // Node 6 is isolated
            );

            JavaRDD<Tuple2<Object, String>> verticesRDD = javaSparkContext.parallelize(vertices);
            JavaRDD<Edge<String>> edgesRDD = javaSparkContext.parallelize(edges);

            Graph<String, String> graph = Graph.apply(
                verticesRDD.rdd(),
                edgesRDD.rdd(),
                "Unknown",
                StorageLevel.MEMORY_AND_DISK(),
                StorageLevel.MEMORY_AND_DISK(),
                ClassTag$.MODULE$.apply(String.class),
                ClassTag$.MODULE$.apply(String.class)
            );

            // Find connected components
            Graph<Object, String> ccGraph = graph.connectedComponents();
            List<Tuple2<Object, Object>> components = ccGraph.vertices().toJavaRDD().collect();

            Map<String, Object> result = new HashMap<>();
            result.put("algorithm", "Connected Components");
            result.put("totalVertices", graph.vertices().count());
            result.put("components", components.stream()
                .map(cc -> Map.of("vertexId", cc._1(), "componentId", cc._2()))
                .collect(Collectors.toList()));

            // Group by component
            Map<Object, List<Object>> componentGroups = components.stream()
                .collect(Collectors.groupingBy(
                    cc -> cc._2(),
                    Collectors.mapping(cc -> cc._1(), Collectors.toList())
                ));

            result.put("componentGroups", componentGroups);
            result.put("numberOfComponents", componentGroups.size());

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error finding connected components", e);
        }
    }

    /**
     * Perform triangle counting
     */
    public Map<String, Object> countTriangles() {
        try {
            // Create a graph suitable for triangle counting
            List<Tuple2<Object, String>> vertices = Arrays.asList(
                new Tuple2<>(1L, "A"), new Tuple2<>(2L, "B"), new Tuple2<>(3L, "C"),
                new Tuple2<>(4L, "D"), new Tuple2<>(5L, "E")
            );

            List<Edge<String>> edges = Arrays.asList(
                new Edge<>(1L, 2L, "edge"), new Edge<>(2L, 3L, "edge"), new Edge<>(3L, 1L, "edge"), // Triangle 1-2-3
                new Edge<>(2L, 4L, "edge"), new Edge<>(4L, 5L, "edge"), new Edge<>(5L, 2L, "edge"), // Triangle 2-4-5
                new Edge<>(1L, 4L, "edge") // Additional edge
            );

            JavaRDD<Tuple2<Object, String>> verticesRDD = javaSparkContext.parallelize(vertices);
            JavaRDD<Edge<String>> edgesRDD = javaSparkContext.parallelize(edges);

            Graph<String, String> graph = Graph.apply(
                verticesRDD.rdd(),
                edgesRDD.rdd(),
                "Unknown",
                StorageLevel.MEMORY_AND_DISK(),
                StorageLevel.MEMORY_AND_DISK(),
                ClassTag$.MODULE$.apply(String.class),
                ClassTag$.MODULE$.apply(String.class)
            );

            // Count triangles
            Graph<Integer, String> triangleGraph = graph.triangleCount();
            List<Tuple2<Object, Integer>> triangleCounts = triangleGraph.vertices().toJavaRDD().collect();

            Map<String, Object> result = new HashMap<>();
            result.put("algorithm", "Triangle Count");
            result.put("triangleCounts", triangleCounts.stream()
                .map(tc -> Map.of("vertexId", tc._1(), "triangleCount", tc._2()))
                .collect(Collectors.toList()));

            int totalTriangles = triangleCounts.stream()
                .mapToInt(tc -> tc._2())
                .sum() / 3; // Each triangle is counted 3 times

            result.put("totalTriangles", totalTriangles);

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error counting triangles", e);
        }
    }

    /**
     * Create and analyze a citation network
     */
    public Map<String, Object> analyzeCitationNetwork() {
        try {
            // Create a citation graph (papers citing other papers)
            List<Tuple2<Object, String>> papers = Arrays.asList(
                new Tuple2<>(1L, "Paper on Machine Learning"),
                new Tuple2<>(2L, "Deep Learning Survey"),
                new Tuple2<>(3L, "Neural Networks Basics"),
                new Tuple2<>(4L, "Advanced CNN"),
                new Tuple2<>(5L, "RNN Applications"),
                new Tuple2<>(6L, "Transformer Architecture")
            );

            List<Edge<Integer>> citations = Arrays.asList(
                new Edge<>(2L, 1L, 1), // Deep Learning Survey cites ML paper
                new Edge<>(2L, 3L, 1), // Deep Learning Survey cites NN Basics
                new Edge<>(4L, 2L, 1), // CNN cites Deep Learning Survey
                new Edge<>(4L, 3L, 1), // CNN cites NN Basics
                new Edge<>(5L, 2L, 1), // RNN cites Deep Learning Survey
                new Edge<>(5L, 3L, 1), // RNN cites NN Basics
                new Edge<>(6L, 4L, 1), // Transformer cites CNN
                new Edge<>(6L, 5L, 1)  // Transformer cites RNN
            );

            JavaRDD<Tuple2<Object, String>> papersRDD = javaSparkContext.parallelize(papers);
            JavaRDD<Edge<Integer>> citationsRDD = javaSparkContext.parallelize(citations);

            Graph<String, Integer> citationGraph = Graph.apply(
                papersRDD.rdd(),
                citationsRDD.rdd(),
                "Unknown Paper",
                StorageLevel.MEMORY_AND_DISK(),
                StorageLevel.MEMORY_AND_DISK(),
                ClassTag$.MODULE$.apply(String.class),
                ClassTag$.MODULE$.apply(Integer.class)
            );

            // Calculate citation metrics
            Map<String, Object> result = new HashMap<>();
            result.put("totalPapers", citationGraph.vertices().count());
            result.put("totalCitations", citationGraph.edges().count());

            // Most cited papers (in-degree)
            List<Tuple2<Object, Integer>> inDegrees = citationGraph.inDegrees().toJavaRDD().collect();
            result.put("mostCitedPapers", inDegrees.stream()
                .sorted((a, b) -> Integer.compare(b._2(), a._2()))
                .limit(3)
                .map(id -> Map.of("paperId", id._1(), "citationCount", id._2()))
                .collect(Collectors.toList()));

            // Papers that cite most (out-degree)
            List<Tuple2<Object, Integer>> outDegrees = citationGraph.outDegrees().toJavaRDD().collect();
            result.put("mostCitingPapers", outDegrees.stream()
                .sorted((a, b) -> Integer.compare(b._2(), a._2()))
                .limit(3)
                .map(od -> Map.of("paperId", od._1(), "citingCount", od._2()))
                .collect(Collectors.toList()));

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error analyzing citation network", e);
        }
    }
}
