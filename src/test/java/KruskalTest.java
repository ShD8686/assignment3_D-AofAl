import graph.Edge;
import graph.Graph;
import algorithms.Kruskal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Расширенное тестирование алгоритма Краскала.
 */
public class KruskalTest {

    @Test
    void testConnectedGraph() {
        Graph G = new Graph(4);
        G.addEdge(new Edge(0, 1, 1.0));
        G.addEdge(new Edge(0, 2, 2.0));
        G.addEdge(new Edge(1, 2, 3.0));
        G.addEdge(new Edge(1, 3, 4.0));
        G.addEdge(new Edge(2, 3, 5.0));

        Kruskal mst = new Kruskal(G);

        int edgeCount = 0;
        double totalWeight = 0.0;
        for (Edge e : mst.edges()) {
            edgeCount++;
            totalWeight += e.weight();
        }

        assertEquals(3, edgeCount, "MST должно содержать V-1 рёбер");
        assertEquals(7.0, totalWeight, 1e-9, "Вес MST должен быть равен 7.0");
        assertEquals(7.0, mst.weight(), 1e-9, "Метод weight() должен возвращать корректное значение");
    }

    @Test
    void testMSTHasCorrectEdgeCount() {
        Graph G = new Graph(5);
        G.addEdge(new Edge(0, 1, 1.0));
        G.addEdge(new Edge(0, 2, 2.0));
        G.addEdge(new Edge(1, 3, 3.0));
        G.addEdge(new Edge(2, 4, 4.0));
        G.addEdge(new Edge(3, 4, 5.0));

        Kruskal mst = new Kruskal(G);

        int edgeCount = 0;
        for (Edge e : mst.edges()) {
            edgeCount++;
        }

        assertEquals(4, edgeCount, "MST должно содержать V-1 = 4 рёбер для графа с 5 вершинами");
    }

    @Test
    void testMSTIsAcyclic() {
        Graph G = new Graph(4);
        G.addEdge(new Edge(0, 1, 1.0));
        G.addEdge(new Edge(0, 2, 2.0));
        G.addEdge(new Edge(1, 3, 3.0));
        G.addEdge(new Edge(2, 3, 4.0));

        Kruskal mst = new Kruskal(G);

        // Проверяем что MST не содержит циклов
        Set<String> addedEdges = new HashSet<>();
        int[] parent = new int[4];
        for (int i = 0; i < 4; i++) parent[i] = i;

        for (Edge e : mst.edges()) {
            int v = e.either();
            int w = e.other(v);

            // Проверка на цикл
            assertNotEquals(find(parent, v), find(parent, w),
                    "MST не должно содержать циклов");
            union(parent, v, w);
        }
    }

    @Test
    void testMSTConnectsAllVertices() {
        Graph G = new Graph(6);
        G.addEdge(new Edge(0, 1, 1.0));
        G.addEdge(new Edge(1, 2, 2.0));
        G.addEdge(new Edge(2, 3, 3.0));
        G.addEdge(new Edge(3, 4, 4.0));
        G.addEdge(new Edge(4, 5, 5.0));

        Kruskal mst = new Kruskal(G);

        // Проверяем что все вершины связаны
        Set<Integer> connectedVertices = new HashSet<>();
        for (Edge e : mst.edges()) {
            connectedVertices.add(e.either());
            connectedVertices.add(e.other(e.either()));
        }

        assertEquals(6, connectedVertices.size(), "MST должно связывать все вершины");
    }

    @Test
    void testDisconnectedGraph() {
        Graph G = new Graph(5);
        G.addEdge(new Edge(0, 1, 1.0));
        G.addEdge(new Edge(1, 2, 2.0));
        // Вершины 3 и 4 изолированы

        Kruskal mst = new Kruskal(G);

        double expectedWeight = 3.0; // только связная компонента
        assertEquals(expectedWeight, mst.weight(), 1e-9,
                "Краскал должен корректно работать с несвязными графами");
    }

    @Test
    void testOperationCounts() {
        Graph G = new Graph(4);
        G.addEdge(new Edge(0, 1, 1.0));
        G.addEdge(new Edge(0, 2, 2.0));
        G.addEdge(new Edge(1, 2, 3.0));
        G.addEdge(new Edge(1, 3, 4.0));

        Kruskal mst = new Kruskal(G);

        // Проверяем что счетчики операций положительные
        assertTrue(mst.getComparisonCount() > 0, "Счетчик сравнений должен быть положительным");
        assertTrue(mst.getUnionFindOperations() > 0, "Счетчик Union-Find операций должен быть положительным");
        assertTrue(mst.getSortOperations() > 0, "Счетчик операций сортировки должен быть положительным");
        assertTrue(mst.getTotalOperations() > 0, "Общий счетчик операций должен быть положительным");
    }

    @Test
    void testSingleVertexGraph() {
        Graph G = new Graph(1); // Граф с одной вершиной

        Kruskal mst = new Kruskal(G);

        int edgeCount = 0;
        for (Edge e : mst.edges()) {
            edgeCount++;
        }

        assertEquals(0, edgeCount, "MST для графа с одной вершиной должно быть пустым");
        assertEquals(0.0, mst.weight(), 1e-9, "Вес MST для одной вершины должен быть 0");
    }

    @Test
    void testPerformanceConsistency() {
        Graph G = new Graph(4);
        G.addEdge(new Edge(0, 1, 1.0));
        G.addEdge(new Edge(0, 2, 2.0));
        G.addEdge(new Edge(1, 2, 3.0));
        G.addEdge(new Edge(1, 3, 4.0));

        // Запускаем несколько раз для проверки воспроизводимости
        double firstWeight = 0.0;
        int firstOperations = 0;

        for (int i = 0; i < 3; i++) {
            Kruskal mst = new Kruskal(G);

            if (i == 0) {
                firstWeight = mst.weight();
                firstOperations = mst.getTotalOperations();
            } else {
                assertEquals(firstWeight, mst.weight(), 1e-9,
                        "Результаты должны быть воспроизводимы");
                assertEquals(firstOperations, mst.getTotalOperations(),
                        "Количество операций должно быть одинаковым для одинаковых входных данных");
            }
        }
    }

    // Вспомогательные методы для проверки циклов
    private int find(int[] parent, int v) {
        if (parent[v] != v)
            parent[v] = find(parent, parent[v]);
        return parent[v];
    }

    private void union(int[] parent, int v, int w) {
        int rootV = find(parent, v);
        int rootW = find(parent, w);
        if (rootV != rootW) {
            parent[rootW] = rootV;
        }
    }
}