import java.io.File;
import java.util.*;


class Part {
    int numberOfCells;
    TreeMap<Integer, LinkedList<Cell>> gain;
    int key;

    Part(int key) {
        gain = new TreeMap<>();
        this.key = key;
    }
}

class Partition {
    int balance;
    Cell movedCell;
    int cost;
}

public class Graph {
    Set<Integer> visited;
    int numberOfEdges;
    int numberOfCells;
    Part A;
    Part B;
    Edge[] edges;
    Cell[] vertices;
    int currentCutSize;

    private Graph() {
        A = new Part(0);
        B = new Part(1);
        edges = new Edge[numberOfEdges];
        vertices = new Cell[numberOfCells];
        visited = new HashSet<>();
        currentCutSize = 0;
    }

    public Partition moveVertex(Part part) {
        Partition partition = new Partition();
        Cell moved = part.gain.lastEntry().getValue().getFirst();
        part.gain.lastEntry().getValue().removeFirst();
        this.visited.add(moved.key);
        partition.movedCell = moved;
        reCountGains(moved, part);
        partition.balance = Math.abs(this.A.numberOfCells - this.B.numberOfCells);
        partition.cost = currentCutSize;
        return partition;
    }

    public void reCountGains(Cell moved, Part part) {
        Part sourcePart = part.key == 0 ? A : B;
        sourcePart.numberOfCells--;
        Part destPart = sourcePart.key == 0 ? B : A;
        destPart.numberOfCells++;
        for (Edge e : moved.edges) {
            boolean onlyOneInDestPartAfterMove = true;
            boolean onlyOneInSourcePartBeforeMove = true;
            boolean onlyOneInSourcePartAfterMove = true;
            boolean onlyOneInDestPartBeforeMove = true;
            boolean seenCellInSource = false;
            boolean seenCellInDest = false;

            for (Cell cell : e.cells) {
                if (cell.part == destPart.key) {
                    currentCutSize--;
                    onlyOneInDestPartAfterMove = false;
                    if (seenCellInDest) {
                        onlyOneInDestPartBeforeMove = false;
                    }
                    else {
                        seenCellInDest = true;
                    }
                }
                if (cell.part == sourcePart.key && cell.key != moved.key) {
                    currentCutSize++;
                    onlyOneInSourcePartBeforeMove = false;
                    if (seenCellInSource) {
                        onlyOneInSourcePartAfterMove = false;
                    }
                    else {
                        seenCellInSource = true;
                    }
                }
            }
            if (onlyOneInDestPartAfterMove || onlyOneInSourcePartBeforeMove || onlyOneInSourcePartAfterMove || onlyOneInDestPartBeforeMove) {
                for (Cell cell : e.cells) {
                    sourcePart.gain.get(cell.gain).remove(cell);
                    if (onlyOneInDestPartAfterMove || onlyOneInSourcePartAfterMove) {
                        cell.gain++;
                    }
                    else {
                        cell.gain--;
                    }
                    sourcePart.gain.get(cell.gain).add(cell);
                }
            }
        }
    }

    public Partition newPartition() {
        if (A.numberOfCells < B.numberOfCells) {
            return moveVertex(B);
        } else {
            return moveVertex(A);
        }
    }

    public void gainContainerInitializer() {
        for (int i = 0; i < numberOfCells; i++) {
            int part = vertices[i].part;
            int key = vertices[i].key;
            int gain = 0;
            for (int j = 0; j < vertices[i].edges.size(); i++) {
                boolean here = true;
                boolean there = true;
                for (int k = 0; k < vertices[i].edges.get(j).cells.size(); k++) {
                    Cell cell = vertices[i].edges.get(j).cells.get(k);
                    if (cell.part != part) {
                        currentCutSize++;
                        here = false;
                    }
                    if (cell.part == part && cell.key != key) {
                        there = false;
                    }
                }
                if (here) {
                    gain--;
                }
                if (there) {
                    gain++;
                }
            }
            vertices[i].gain = gain;
            if (part == 0) {
                List<Cell> cellsContainer = this.A.gain.getOrDefault(gain, new LinkedList<>());
                cellsContainer.add(vertices[i]);
            }
            if (part == 1) {
                List<Cell> cellsContainer = this.B.gain.getOrDefault(gain, new LinkedList<>());
                cellsContainer.add(vertices[i]);
            }
        }
        currentCutSize /= 2;
    }

    static void parseInput(Graph graph) {
        File file = new File("my.txt");
        try (Scanner scanner = new Scanner(file)) {
            graph.numberOfEdges = scanner.nextInt();
            graph.numberOfCells = scanner.nextInt();
            graph.A.numberOfCells = graph.numberOfCells / 2;
            graph.B.numberOfCells = graph.numberOfCells / 2 + graph.numberOfCells % 2;
            for (int i = 0; i < graph.numberOfEdges; i++) {
                Edge e = new Edge(i);
                graph.edges[e.key] = e;
                e.cells = new ArrayList<>();
                String[] values = scanner.nextLine().split(" ");
                for (int j = 0; j < values.length; j++) {
                    int key = Integer.parseInt(values[j]);
                    if (graph.vertices[key] == null) {
                        Cell cell = new Cell(Integer.parseInt(values[j]), graph.numberOfCells);
                        graph.vertices[key] = cell;
                    }
                    e.cells.add(graph.vertices[key]);
                    graph.vertices[key].edges.add(e);
                }
            }
        } catch (Exception e) {
            System.out.println("Error");
        }
    }

    public static void main(String[] args) {
        Graph graph = new Graph();
        parseInput(graph);
        graph.gainContainerInitializer();
        Stack<Partition> stack = new Stack<>();
        while (graph.visited.size() != graph.numberOfCells) {
            stack.push(graph.newPartition());
        }
        Partition minBalancePartition;
        Partition minCostPartition;
        int minCost = Integer.MAX_VALUE;
        int minBalance = Integer.MAX_VALUE;
        while (!stack.isEmpty()) {
            Partition partition = stack.pop();
            if (minCost > partition.cost) {
                minCostPartition = partition;
                minCost = partition.cost;
            }
            if (minBalance > partition.balance) {
                minBalancePartition = partition;
                minBalance = partition.balance;
            }
        }
    }
}


class Edge {
    int key;
    List<Cell> cells;

    Edge(int key) {
        this.key = key;
        cells = new ArrayList<>();
    }
}

class Cell {
    int part;
    int key;
    List<Edge> edges;
    int gain;

    @Override
    public boolean equals(Object other) {
        return ((Cell) other).key == this.key;
    }

    Cell(int key, int n) {
        if (key < n / 2) {
            part = 0;

        } else {
            part = 1;
        }
        this.key = key;
        edges = new ArrayList<>();
    }
}
