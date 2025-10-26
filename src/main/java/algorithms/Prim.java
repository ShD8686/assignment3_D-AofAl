package algorithms;

import graph.Edge;
import graph.Graph;

import java.util.*;

public class Prim {
    private final List<Edge> mst;
    private final boolean[] marked;
    private double totalWeight;
    private final PriorityQueue<Edge> pq;

    private int comparisonCount;
    private int edgeVisitCount;
    private int queueOperations;

    public Prim(Graph graph) {
        int V = graph.V();
        mst = new ArrayList<>();
        marked = new boolean[V];
        pq = new PriorityQueue<>();

        comparisonCount = 0;
        edgeVisitCount = 0;
        queueOperations = 0;

        for (int v = 0; v < V; v++) {
            comparisonCount++;
            if (!marked[v]) {
                prim(graph, v);
            }
        }
    }

    private void prim(Graph graph, int s) {
        visit(graph, s);

        while (!pq.isEmpty()) {
            comparisonCount++;
            queueOperations++;
            Edge e = pq.poll();
            int v = e.either();
            int w = e.other(v);

            comparisonCount += 2;
            if (marked[v] && marked[w]) continue;

            mst.add(e);
            totalWeight += e.weight();

            comparisonCount++;
            if (!marked[v]) visit(graph, v);
            comparisonCount++;
            if (!marked[w]) visit(graph, w);
        }
    }

    private void visit(Graph graph, int v) {
        marked[v] = true;
        for (Edge e : graph.adj(v)) {
            edgeVisitCount++;
            comparisonCount++;
            if (!marked[e.other(v)]) {
                queueOperations++;
                pq.offer(e);
            }
        }
    }

    public Iterable<Edge> edges() {
        return mst;
    }

    public double weight() {
        return totalWeight;
    }

    public int getComparisonCount() {
        return comparisonCount;
    }

    public int getEdgeVisitCount() {
        return edgeVisitCount;
    }

    public int getQueueOperations() {
        return queueOperations;
    }

    public int getTotalOperations() {
        return comparisonCount + edgeVisitCount + queueOperations;
    }

    public int getKeyComparisons() {
        return comparisonCount;
    }
}