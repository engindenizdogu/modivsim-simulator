import javax.swing.plaf.synth.SynthLookAndFeel;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ModivSim extends Thread {
    private static final String nodesFolder = "D:\\Code\\modivsim-simulator\\nodes";
    private static final int SERVER_PORT = 4444;
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

        /* Initialize server socket */
        Socket s;
        ServerSocket serverSocket = null;
        try{
            serverSocket = new ServerSocket(SERVER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Create node sockets and accept incoming requests */
        nodes.forEach((node) -> node.Connect());

        for(int i = 0; i < nodes.size(); i++){
            try{
                s = serverSocket.accept();
                System.out.println("Connection established.");

                BufferedReader is = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintWriter os = new PrintWriter(s.getOutputStream());
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        // TODO: Invoke sendUpdate() every p seconds

        //initialize();
        //print();
    }

    /**
     *
     * @param nodePath
     * @return
     * @throws IOException
     */
    public static String readNode(String nodePath) throws IOException {
        FileReader fr = new FileReader(nodePath);
        BufferedReader br = new BufferedReader(fr);
        String nodeInfo = br.readLine();

        return nodeInfo;
    }

    /**
     *
     * @param nodeInfo
     * @return
     */
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
