import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ModivSim extends Thread {
    //private static final String nodesFolder = "D:\\Code\\modivsim-simulator\\nodes";
    private static final String nodesFolder = "/Users/berrakperk/Desktop/416/modivsim-simulator/nodes";
    private static final int SERVER_PORT = 4444;
    //protected static ObjectInputStream is;
    //protected static ObjectOutputStream os;
    static ArrayList<Node> nodes = new ArrayList<>(); // Arraylist to keep nodes
    static  ArrayList<Socket> sockets = new ArrayList<>(); // Arraylist to keep sockets
    static int numDynamicLinks;
    static Random rand = new Random();

    public static void main(String args[]) throws IOException, InterruptedException {
        System.out.println("ModivSim started...");
        Scanner sc= new Scanner(System.in);
        System.out.print("Please enter the period: ");
        int p = sc.nextInt();
        /* Reading nodes */
        String[] nodeFiles;
        File f = new File(nodesFolder);
        nodeFiles = f.list();
        int numNodes = nodeFiles.length; // Total number of nodes

        numDynamicLinks = 0;
        String nodeInfo;
        for(String nodeFile : nodeFiles){
            //nodeInfo = readNode(nodesFolder + "\\" + nodeFile);
            nodeInfo = readNode(nodesFolder + "/" + nodeFile);
            Node n = initializeNode(nodeInfo, numNodes);
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
        nodes.forEach(node -> node.Connect());
        for(int i = 0; i < nodes.size(); i++){
            try{
                s = serverSocket.accept();
                sockets.add(s);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        System.out.println("All nodes initialized successfully.");



        /* Update HashTables of nodes to help with neighbor communications */
        nodes.forEach(node -> {
            node.neighborIds.forEach(neighborId -> { // For each neighbor of the node
                node.neighborNodes.put(String.valueOf(neighborId), nodes.get(neighborId));
                node.neighborSockets.put(String.valueOf(neighborId), sockets.get(neighborId));
            });
        });

        while(true){
            // Generate random numbers
            int[] randomCosts = new int[numDynamicLinks];
            if(numDynamicLinks > 0){
                for(int i = 0; i < numDynamicLinks; i++){
                    boolean hey = rand.nextBoolean();
                    if(hey){
                        randomCosts[i] = rand.nextInt(10) + 1;
                    } else {
                        randomCosts[i] = -1;
                    }
                }

                for(int j = 0; j < nodes.size(); j++){
                    Node node = nodes.get(j);
                    if(node.hasDynamicLink){
                        for(int k = 0; k < node.dynamicNeighbors.size(); k++){
                            String dynamicNbr = node.dynamicNeighbors.get(k);
                            int index = Integer.parseInt(dynamicNbr) - 1;
                            if(index < 0) index = 0;
                            if(randomCosts[index] != -1){
                                node.linkCost.replace(dynamicNbr,randomCosts[index]);
                            }
                        }
                    }
                }
            }

            int counter = 0;
            boolean isUpdated = false;
            for(int i = 0; i < numNodes; i++){
                Node node = nodes.get(i);
                isUpdated = node.sendUpdate();
                if(!isUpdated){
                    counter++;
                }
            }

            if(counter == numNodes){
                System.out.println("The graph has converged!");
                break;
            }
            Thread.sleep(p);
        }

        /* POPUP */

        String[] column = new String[numNodes];
        Arrays.fill(column, "Node");
        String[][] a = new String[numNodes][numNodes];
        String[][] b = new String[numNodes][2];
        double time=0.0;

        //Distance table
        for(int x=0;x<nodes.size();x++) {
            final JFrame output = new JFrame("Output window for Router #" + x);
            output.setVisible(true);
            JLabel l = new JLabel("Current state for router " + x + " at time " + time);
            output.add(l,BorderLayout.NORTH);

            output.setSize(300, 300);
            int length = nodes.get(x).distanceTable[0].length;
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < length; j++) {
                    int[][] temp = nodes.get(x).getDistanceTable();
                    a[i][j] = (String.valueOf(temp[i][j]));
                    JTable jt = new JTable(a, column);
                    output.add(jt, BorderLayout.CENTER);
                }
            }

            //Forwarding table
            for (int i = 0; i < numNodes; i++) {
                Hashtable<String, String> temp = nodes.get(x).getForwardingTable();
                b[i][1] = temp.get(i);
                b[i][0]=String.valueOf(temp.get(i));
                JTable ft = new JTable(b, column);
                output.add(ft, BorderLayout.SOUTH);

            }
        }
        //TODO: distance ve forwardingTable'lar hazır. Burda pencerelerde gösterebiliriz (popupları buraya taşıyabiliriz). getDistanceTable() ve getForwardingTable() methodlarını kullanabilirsin

        //TODO: Close sockets (modivsim and nodes)
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
    private static Node initializeNode(String nodeInfo, int numNodes) {
        Node node = new Node();
        node.initializeDistanceTable(numNodes); // Initialize the distance table

        String info[] = nodeInfo.split("\\,\\(|\\)\\,\\(|\\)"); // Example nodeInfo: "0,(1,5,10),(2,3,15)"

        node.nodeID = info[0]; // Set node id (string)
        int id = Integer.parseInt(info[0]);
        node.distanceTable[id][id] = 0; // Cost to self is 0

        // Getting neighbor info
        String neighbor;
        String[] neighborInfo;
        String neighborID;
        String linkCost;
        int c; // link cost
        int linkBandwidth;
        int numNeighbors = info.length - 1; // First element is the id
        node.numNeighbors = numNeighbors;
        for(int i = 1; i <= numNeighbors; i++){
            neighbor = info[i];
            neighborInfo = neighbor.split("\\,");
            // The information we need
            neighborID = neighborInfo[0];
            linkCost = neighborInfo[1];
            if(linkCost.equals("x")){
                numDynamicLinks++;
                node.hasDynamicLink = true;
                node.dynamicNeighbors.add(neighborID);
                c = rand.nextInt(10) + 1; // between 1 and 10
            }else{
                c = Integer.parseInt(neighborInfo[1]);
            }
            linkBandwidth = Integer.parseInt(neighborInfo[2]);
            node.linkCost.put(neighborID, c); // Fill hashtables in node class
            node.linkBandwidth.put(neighborID, linkBandwidth); // Fill hashtables in node class

            // Set distance table values
            int neighborIdToInt = Integer.parseInt(neighborID);
            node.distanceTable[id][neighborIdToInt] = c;
            node.distanceTable[neighborIdToInt][id] = c;

            // Also save neighbor id
            node.neighborIds.add(neighborIdToInt);
        }

        node.initializeForwardingTable(numNodes); // Forwarding table

        return node;
    }

}
