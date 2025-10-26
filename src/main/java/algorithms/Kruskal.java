package algorithms;

import graph.Edge;
import graph.Graph;

import java.util.*;

public class Kruskal {
    private final List<Edge> mst;
    private double totalWeight;

    private int comparisonCount;
    private int unionFindOperations;
    private int sortOperations;

    private int findOperations;
    private int unionOperations;

    public Kruskal(Graph graph) {
        int V = graph.V();
        mst = new ArrayList<>();

        comparisonCount = 0;
        unionFindOperations = 0;
        sortOperations = 0;
        findOperations = 0;
        unionOperations = 0;

        List<Edge> edges = new ArrayList<>();
        for (Edge e : graph.edges()) {
            edges.add(e);
            sortOperations++;
        }

        Collections.sort(edges);
        sortOperations += edges.size() * (int) Math.log(edges.size());

        int[] parent = new int[V];
        int[] rank = new int[V];
        for (int i = 0; i < V; i++) {
            parent[i] = i;
            rank[i] = 0;
            unionFindOperations++;
        }

        for (Edge e : edges) {
            comparisonCount++;
            if (mst.size() == V - 1) break;

            int v = e.either();
            int w = e.other(v);

            unionFindOperations += 2;
            if (find(parent, v) != find(parent, w)) {
                unionFindOperations++;
                unionOperations++;
                union(parent, rank, v, w);
                mst.add(e);
                totalWeight += e.weight();
            }
            comparisonCount++;
        }
    }

    private int find(int[] parent, int v) {
        unionFindOperations++;
        if (parent[v] != v) {
            parent[v] = find(parent, parent[v]);
        }
        return parent[v];
    }

    private void union(int[] parent, int[] rank, int v, int w) {
        int rootV = find(parent, v);
        int rootW = find(parent, w);

        comparisonCount++;
        if (rootV == rootW) return;

        comparisonCount += 2;
        if (rank[rootV] < rank[rootW]) {
            parent[rootV] = rootW;
        } else if (rank[rootV] > rank[rootW]) {
            parent[rootW] = rootV;
        } else {
            parent[rootW] = rootV;
            rank[rootV]++;
        }
        unionFindOperations += 2;
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

    public int getUnionFindOperations() {
        return unionFindOperations;
    }

    public int getSortOperations() {
        return sortOperations;
    }

    public int getTotalOperations() {
        return comparisonCount + unionFindOperations + sortOperations;
    }

    public int getFindOperations() {
        return findOperations;
    }

    public int getUnionOperations() {
        return unionOperations;
    }

}