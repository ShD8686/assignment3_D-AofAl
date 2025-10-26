package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import graph.Edge;
import graph.Graph;
import algorithms.Kruskal;
import algorithms.Prim;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {

    private static final String[] INPUT_FILES = {
            "src/main/resources/input_small.json",
            "src/main/resources/input_medium.json",
            "src/main/resources/input_large.json",
            "src/main/resources/input_disconnected.json"
    };

    private static final String BENCHMARK_FILE = "src/main/resources/benchmark_results.csv";

    public static void main(String[] args) {
        initializeBenchmarkFile();

        // Обрабатываем каждый тестовый файл
        for (String inputFile : INPUT_FILES) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("FILE PROCESSING: " + inputFile);
            System.out.println("=".repeat(50));

            try {
                processGraphFile(inputFile);
            } catch (Exception e) {
                System.err.println("Error processing file " + inputFile + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("\nALL FILES HAVE BEEN PROCESSED!");
        System.out.println("The results are saved in benchmark_results.csv");
        System.out.println("Individual reports in a folder src/main/resources/");
    }

    private static void processGraphFile(String inputFilePath) {
        try {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(new FileReader(inputFilePath), JsonObject.class);

            JsonArray verticesJson = json.getAsJsonArray("vertices");
            JsonArray edgesJson = json.getAsJsonArray("edges");

            int V = verticesJson.size();
            int E = edgesJson.size();
            Graph graph = new Graph(V);

            Map<String, Integer> vertexMap = new HashMap<>();
            List<String> vertexNames = new ArrayList<>();
            for (int i = 0; i < V; i++) {
                String vertexName = verticesJson.get(i).getAsString();
                vertexMap.put(vertexName, i);
                vertexNames.add(vertexName);
            }

            for (int i = 0; i < edgesJson.size(); i++) {
                JsonObject e = edgesJson.get(i).getAsJsonObject();
                String src = e.get("source").getAsString();
                String dst = e.get("destination").getAsString();
                double w = e.get("weight").getAsDouble();
                graph.addEdge(new Edge(vertexMap.get(src), vertexMap.get(dst), w));
            }

            long startPrim = System.nanoTime();
            Prim prim = new Prim(graph);
            long endPrim = System.nanoTime();

            long startKruskal = System.nanoTime();
            Kruskal kruskal = new Kruskal(graph);
            long endKruskal = System.nanoTime();

            double primTime = (endPrim - startPrim) / 1_000_000.0;
            double kruskalTime = (endKruskal - startKruskal) / 1_000_000.0;

            String outputFileName = inputFilePath.replace("input", "output")
                    .replace(".json", "_result.json");

            saveDetailedResults(outputFileName, graph, prim, kruskal,
                    primTime, kruskalTime, vertexNames);

            saveToBenchmark(inputFilePath, V, E, prim, kruskal, primTime, kruskalTime);

            printConsoleResults(inputFilePath, V, E, prim, kruskal, primTime, kruskalTime);

        } catch (IOException e) {
            System.err.println("Error reading/writing files: " + e.getMessage());
        }
    }

    private static void saveDetailedResults(String outputFilePath, Graph graph,
                                            Prim prim, Kruskal kruskal,
                                            double primTime, double kruskalTime,
                                            List<String> vertexNames) throws IOException {
        JsonObject result = new JsonObject();
        result.addProperty("vertices", graph.V());
        result.addProperty("edges", graph.E());
        result.addProperty("graphType", getGraphType(graph.V(), graph.E()));


        JsonObject primJson = new JsonObject();
        primJson.addProperty("totalWeight", prim.weight());
        primJson.addProperty("executionTimeMs", primTime);
        primJson.addProperty("operationCount", prim.getTotalOperations());
        primJson.addProperty("comparisonCount", prim.getComparisonCount());
        primJson.addProperty("edgeVisitCount", prim.getEdgeVisitCount());
        primJson.addProperty("queueOperations", prim.getQueueOperations());
        primJson.add("mstEdges", getMSTEdgesJson(prim.edges(), vertexNames));


        JsonObject kruskalJson = new JsonObject();
        kruskalJson.addProperty("totalWeight", kruskal.weight());
        kruskalJson.addProperty("executionTimeMs", kruskalTime);
        kruskalJson.addProperty("operationCount", kruskal.getTotalOperations());
        kruskalJson.addProperty("comparisonCount", kruskal.getComparisonCount());
        kruskalJson.addProperty("unionFindOperations", kruskal.getUnionFindOperations());
        kruskalJson.addProperty("sortOperations", kruskal.getSortOperations());
        kruskalJson.add("mstEdges", getMSTEdgesJson(kruskal.edges(), vertexNames));


        result.add("PrimMST", primJson);
        result.add("KruskalMST", kruskalJson);


        result.addProperty("algorithmsConsistent",
                Math.abs(prim.weight() - kruskal.weight()) < 1e-9);


        Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            gsonPretty.toJson(result, writer);
        }
    }


    private static void saveToBenchmark(String inputFile, int V, int E,
                                        Prim prim, Kruskal kruskal,
                                        double primTime, double kruskalTime) throws IOException {
        try (FileWriter csv = new FileWriter(BENCHMARK_FILE, true)) {
            String graphName = getReadableGraphName(inputFile);

            csv.write(String.format(Locale.US,
                    "%s,Prim,%.2f,%d,%d,\"{%s}\",%.4f%n",
                    graphName, prim.weight(), V, E,
                    getPrimMetrics(prim), primTime
            ));

            csv.write(String.format(Locale.US,
                    "%s,Kruskal,%.2f,%d,%d,\"{%s}\",%.4f%n",
                    graphName, kruskal.weight(), V, E,
                    getKruskalMetrics(kruskal), kruskalTime
            ));
        }
    }

    private static String getPrimMetrics(Prim prim) {
        return String.format(
                "'key_comparisons': %d",
                prim.getComparisonCount()
        );
    }

    private static String getKruskalMetrics(Kruskal kruskal) {
        return String.format(
                "'edge_sorts': 1, 'find_operations': %d, 'union_operations': %d",
                kruskal.getFindOperations(),
                kruskal.getUnionOperations()
        );
    }

    private static void initializeBenchmarkFile() {
        try (FileWriter csv = new FileWriter(BENCHMARK_FILE)) {
            csv.write("Graph Name,Algorithm,MST Cost,Vertices,Edges,Detailed Metrics,Average Execution Time (ms)\n");
        } catch (IOException e) {
            System.err.println("Error initializing CSV file: " + e.getMessage());
        }
    }


    private static String getReadableGraphName(String inputFile) {
        if (inputFile.contains("small")) return "Little Count";
        if (inputFile.contains("medium")) return "Medium граф";
        if (inputFile.contains("large")) return "Large Count";
        if (inputFile.contains("disconnected")) return "Disconnected graph";
        return "Граф";
    }

    private static String getFasterAlgorithm(double primTime, double kruskalTime) {
        if (primTime < kruskalTime) return "Prim";
        if (kruskalTime < primTime) return "Kruskal";
        return "Equal";
    }

    private static void printConsoleResults(String inputFile, int V, int E,
                                            Prim prim, Kruskal kruskal,
                                            double primTime, double kruskalTime) {
        String graphName = inputFile.replace("src/main/resources/input_", "")
                .replace(".json", "");

        System.out.printf("Граф: %s (%d вершин, %d рёбер, %s)%n",
                graphName, V, E, getGraphType(V, E));

        System.out.printf("  Prim:    вес=%.2f, время=%.3f ms, операции=%d%n",
                prim.weight(), primTime, prim.getTotalOperations());

        System.out.printf("  Kruskal: вес=%.2f, время=%.3f ms, операции=%d%n",
                kruskal.weight(), kruskalTime, kruskal.getTotalOperations());

        System.out.printf("  Согласованность: %s%n",
                Math.abs(prim.weight() - kruskal.weight()) < 1e-9 ? "ДА" : "НЕТ");
    }

    private static JsonArray getMSTEdgesJson(Iterable<Edge> edges, List<String> vertexNames) {
        JsonArray edgesArray = new JsonArray();
        for (Edge edge : edges) {
            JsonObject edgeJson = new JsonObject();
            edgeJson.addProperty("source", vertexNames.get(edge.either()));
            edgeJson.addProperty("destination", vertexNames.get(edge.other(edge.either())));
            edgeJson.addProperty("weight", edge.weight());
            edgesArray.add(edgeJson);
        }
        return edgesArray;
    }
    
    private static String getGraphType(int V, int E) {
        int maxEdges = V * (V - 1) / 2;
        if (maxEdges == 0) return "single";

        double density = (double) E / maxEdges;

        if (density < 0.3) return "sparse";
        else if (density < 0.7) return "medium";
        else return "dense";
    }


}