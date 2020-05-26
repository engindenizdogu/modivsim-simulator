import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ModivSim extends Thread {
    static final String nodesFolder = "D:\\Code\\modivsim-simulator\\nodes";
    static ArrayList<Node> nodes = new ArrayList<Node>(); // Arraylist to keep nodes
    static int table[][]=new int[5][5];

    public static void main(String args[]) throws IOException {
        System.out.println("ModivSim started...");

        /* Reading nodes */
        String[] nodeFiles;
        File f = new File(nodesFolder);
        nodeFiles = f.list();

        String nodeInfo;
        for(String nodeFile : nodeFiles){
            nodeInfo = readNode(nodesFolder + "\\" + nodeFile);
            Node n = initializeNode(nodeInfo);
            nodes.add(n);
        }

        //initialize();
        //print();
    }

    // TODO: Read files and initialize nodes here

    public static String readNode(String nodePath) throws IOException {
        FileReader fr = new FileReader(nodePath);
        BufferedReader br = new BufferedReader(fr);
        String nodeInfo = br.readLine();

        return nodeInfo;
    }

    private static Node initializeNode(String nodeInfo) {
        Node node = new Node();
        /* Example nodeInfo: "0,(1,5,10),(2,3,15)" */
        String info[] = nodeInfo.split("\\,\\(|\\)\\,\\(|\\)");

        String id = info[0]; // Get node id
        node.nodeID = id; // Set node id

        // Getting neighbor info
        String neighbor;
        String[] neighborInfo;
        String neighborID;
        int linkCost;
        int linkBandwidth;
        for(int i = 1; i < info.length; i++){
            neighbor = info[i];
            neighborInfo = neighbor.split("\\,");
            // The information we need
            neighborID = neighborInfo[0];
            linkCost = Integer.parseInt(neighborInfo[1]);
            linkBandwidth = Integer.parseInt(neighborInfo[2]);
            node.linkCost.put(neighborID, linkCost); // Fill hashtables in node class
            node.linkBandwidth.put(neighborID, linkBandwidth); // Fill hashtables in node class
        }

        return node;
    }

    public static void initialize() {
        for (int x = 0; x < 5; x++) {
            for (int z = 1; z < 5; z++) {
                table[x][z] = 999;
            }
        }
    }

    public static void print() {
        System.out.println("Distance Table:");
        System.out.println("dst   |   0        1        2        3");
        System.out.printf("---------------------------------------\n");
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                System.out.print(table[x][z] + "      ");
            }
            System.out.println();
        }

    }
}
