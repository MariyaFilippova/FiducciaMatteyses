import java.io.File;
import java.util.*;


class Part {
    int numberOfCells;
    TreeMap<Integer, ArrayList<Cell>> gain;
    int key;

    Part(int key) {
        gain = new TreeMap<>();
        this.key = key;
    }
}

class Partition {
    Cell movedCell;
    int cost;
}

public class Graph {
    int[] visited;
    int numberOfEdges;
    int numberOfCells;
    Part A;
    Part B;
    Edge[] edges;
    Cell[] vertices;

    private Graph(int numberOfEdges, int numberOfCells) {
        this.numberOfCells = numberOfCells;
        this.numberOfEdges = numberOfEdges;
        A = new Part(0);
        B = new Part(1);
        edges = new Edge[numberOfEdges];
        vertices = new Cell[numberOfCells];
        visited = new int[numberOfCells];
    }

    public Partition moveVertex(Part part) {
        Partition partition = new Partition();
        Cell moved = part.gain.lastEntry().getValue().get(0);
        part.gain.lastEntry().getValue().remove(0);
        if (part.gain.lastEntry().getValue().size() == 0) {
            part.gain.remove(part.gain.lastKey());
        }
        partition.movedCell = moved;
        reCountGains(moved);
        partition.cost = getCut();
        return partition;
    }

    public void reCountGains(Cell moved) {
        Part sourcePart = moved.part == 0 ? A : B;
        sourcePart.numberOfCells--;
        Part destPart = moved.part == 0 ? B : A;
        destPart.numberOfCells++;
        moved.part = moved.part == 0 ? 1 : 0;
        visited[moved.key - 1] = 1;
        for (Edge e : moved.edges) {
            boolean onlyOneInDestPartAfterMove = true;
            boolean onlyOneInSourcePartBeforeMove = true;
            boolean onlyOneInSourcePartAfterMove = true;
            boolean onlyOneInDestPartBeforeMove = true;
            boolean seenCellInSource = false;
            boolean seenCellInDest = false;

            for (Cell cell : e.cells) {
                if (cell.part == destPart.key) {
                    onlyOneInDestPartAfterMove = false;
                    if (seenCellInDest) {
                        onlyOneInDestPartBeforeMove = false;
                    }
                    else {
                        seenCellInDest = true;
                    }
                }
                if (cell.part == sourcePart.key) {
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
                    int prevGain = cell.gain;
                    if (visited[cell.key - 1] == 1) {
                        continue;
                    }
                    if (onlyOneInDestPartAfterMove || onlyOneInSourcePartAfterMove) {
                        cell.gain++;
                    }
                    if (onlyOneInSourcePartBeforeMove || onlyOneInDestPartBeforeMove) {
                        cell.gain--;
                    }
                    if (cell.gain == prevGain) {
                        continue;
                    }
                    Part cellPart = cell.part == 0 ? A : B;
                    if (!cellPart.gain.containsKey(cell.gain)) {
                        cellPart.gain.put(cell.gain, new ArrayList<>());
                    }
                    cellPart.gain.get(cell.gain).add(cell);
                    cellPart.gain.get(prevGain).remove(cell);
                    if (cellPart.gain.get(prevGain).size() == 0) {
                        cellPart.gain.remove(prevGain);
                    }
                }
            }
        }
    }

    public Partition newPartition() {
        if (A.gain.isEmpty() && B.gain.isEmpty()) {
            return null;
        }
        if (A.gain.isEmpty()) {
            return moveVertex(B);
        }
        if (B.gain.isEmpty()) {
            return moveVertex(A);
        }
        if (Math.abs(A.numberOfCells - B.numberOfCells) <= 2) {
            return A.gain.lastEntry().getKey() > B.gain.lastEntry().getKey() ? moveVertex(A) : moveVertex(B);
        }
        else if (A.numberOfCells < B.numberOfCells) {
            return moveVertex(B);
        } else {
            return moveVertex(A);
        }
    }

    public void gainContainerInitializer() {
        Arrays.fill(visited, 0);
        A.gain.clear();
        B.gain.clear();
        for (int i = 0; i < numberOfCells; i++) {
            int part = vertices[i].part;
            int key = vertices[i].key;
            int gain = 0;
            for (int j = 0; j < vertices[i].edges.size(); j++) {
                boolean here = true;
                boolean there = true;
                for (int k = 0; k < vertices[i].edges.get(j).cells.size(); k++) {
                    Cell cell = vertices[i].edges.get(j).cells.get(k);
                    if (cell.part != part) {
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
                ArrayList<Cell> cellsContainer = this.A.gain.getOrDefault(gain, new ArrayList<>());
                cellsContainer.add(vertices[i]);
                this.A.gain.put(gain, cellsContainer);
            }
            if (part == 1) {
                ArrayList<Cell> cellsContainer = this.B.gain.getOrDefault(gain, new ArrayList<>());
                cellsContainer.add(vertices[i]);
                this.B.gain.put(gain, cellsContainer);
            }
        }
    }

    int getCut() {
        int cut = 0;
        for (Edge e: edges) {
            boolean inA = false;
            boolean inB = false;
            for (Cell cell : e.cells) {
                if (cell.part == 0) {
                    inA = true;
                }
                else {
                    inB = true;
                }
                if (inA && inB) {
                    cut++;
                    break;
                }
            }
        }
        return cut;
    }

    static Graph parseInput(String fileName) {
        Graph graph = null;
        File file = new File("./src/" + fileName);
        try (Scanner scanner = new Scanner(file)) {
            graph = new Graph(scanner.nextInt(), scanner.nextInt());
            graph.A.numberOfCells = graph.numberOfCells / 2;
            graph.B.numberOfCells = graph.numberOfCells / 2 + graph.numberOfCells % 2;
            scanner.nextLine();
            for (int i = 0; i < graph.numberOfEdges; i++) {
                Edge e = new Edge(i);
                graph.edges[e.key] = e;
                e.cells = new ArrayList<>();
                String[] values = scanner.nextLine().split(" ");
                for (int j = 0; j < values.length; j++) {
                    int key = Integer.parseInt(values[j]) - 1;
                    if (graph.vertices[key] == null) {
                        Cell cell = new Cell(Integer.parseInt(values[j]), graph.numberOfCells);
                        graph.vertices[key] = cell;
                    }
                    e.cells.add(graph.vertices[key]);
                    graph.vertices[key].edges.add(e);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return graph;
    }

    public static void main(String[] args) {
        String fileName = args[0];
        long start = System.currentTimeMillis();
        Graph graph = parseInput(fileName);
        int cut = Integer.MAX_VALUE;
        for (int i = 0; ; i++) {
            graph.gainContainerInitializer();
            Partition initialPartition = new Partition();
            initialPartition.cost = graph.getCut();
            ArrayList<Partition> stack = new ArrayList<>();
            stack.add(initialPartition);
            while (true) {
                Partition partition = graph.newPartition();
                if (partition == null) {
                    break;
                }
                stack.add(partition);
            }
            Partition minCostPartition = null;
            int minCost = Integer.MAX_VALUE;
            for (Partition partition : stack) {
                if (minCost > partition.cost) {
                    minCostPartition = partition;
                    minCost = partition.cost;
                }
            }
            if (minCost >= cut) {
                System.out.println("Test name: " + "mmmm" + "\n" +
                        " Num of vertecies " + graph.numberOfCells + "\n" +
                        " Num of edges " + graph.numberOfCells + "\n" +
                        " balance " + Math.abs(graph.A.numberOfCells - graph.B.numberOfCells) + "\n" +
                        " cut " + cut + "\n" +
                        " time " + (System.currentTimeMillis() - start)/(1000*60) + "\n" +
                        " iters " + (i + 1));
                break;
            }
            cut = minCost;
            for (i = stack.size() - 1; i >= 0; i--) {
                if (stack.get(i).equals(minCostPartition)) {
                    break;
                }
                else {
                    if (stack.get(i).movedCell.part == 0) {
                        graph.A.numberOfCells--;
                        graph.B.numberOfCells++;
                        stack.get(i).movedCell.part = 1;
                    }
                    else {
                        graph.A.numberOfCells++;
                        graph.B.numberOfCells--;
                        stack.get(i).movedCell.part = 0;
                    }
                }
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
        if (key <= n / 2) {
            part = 0;
        } else {
            part = 1;
        }
        this.key = key;
        edges = new ArrayList<>();
    }
}
