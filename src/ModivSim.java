import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class ModivSim extends Thread {
    //TODO: add a config file
    private static final String nodesFolder = "D:\\Code\\modivsim-simulator\\nodes";
    private static final String flowPath = "D:\\Code\\modivsim-simulator\\flow\\flow.txt";
    static ArrayList<Node> nodes = new ArrayList<>(); // Arraylist to keep nodes
    static int numDynamicLinks;
    static Random rand = new Random();

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("ModivSim started...");
        Scanner sc= new Scanner(System.in);

        System.out.print("Please enter the period in seconds: ");
        int p = sc.nextInt();

        /* Reading nodes */
        String[] nodeFiles;
        File f = new File(nodesFolder);
        nodeFiles = f.list();
        assert nodeFiles != null;
        int numNodes = nodeFiles.length; // Total number of nodes

        numDynamicLinks = 0;
        String nodeInfo;
        for(String nodeFile : nodeFiles){
            nodeInfo = readNode(nodesFolder + "\\" + nodeFile);
            //nodeInfo = readNode(nodesFolder + "/" + nodeFile); //TODO: fix this line
            Node n = initializeNode(nodeInfo, numNodes);
            nodes.add(n);
        }

        System.out.println("All nodes initialized successfully.");

        /* Update HashTables of nodes to help with neighbor communications */
        nodes.forEach(node -> node.neighborIds.forEach(neighborId -> { // For each neighbor of the node
            node.neighborNodes.put(String.valueOf(neighborId), nodes.get(neighborId));
        }));

        int iterations = 0;
        while(true){
            /* A dynamic link's cost changes each iteration with a probability of 0.5
            If the graph has dynamic links generate random numbers in the beginning of each iteration
            The range is between [0,10] */
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

                for (Node node : nodes) {
                    if (node.hasDynamicLink) { //Update dynamic costs
                        for (int k = 0; k < node.dynamicNeighbors.size(); k++) {
                            String dynamicNbr = node.dynamicNeighbors.get(k);
                            int index = Integer.parseInt(dynamicNbr) - 1;
                            if (index < 0) index = 0;
                            if (randomCosts[index] != -1) {
                                node.linkCost.replace(dynamicNbr, randomCosts[index]);
                            }
                        }
                    }
                }
            }

            int counter = 0;
            boolean isUpdated;
            for(Node node : nodes){
                isUpdated = node.sendUpdate(); // main class invokes the send method
                if(!isUpdated){
                    counter++;
                }
            }

            if(counter == numNodes){
                System.out.println("The graph has converged!");
                break;
            }
            iterations++;
            Thread.sleep(p * 1000);
        }

        System.out.println("\nTotal number of iterations: " + iterations);

        /* Flow simulation */
        System.out.println("\nStarting the simulation...");
        simulateFlow();
        System.out.println();

        /* Pop-up windows */
        String[] column1 = new String[numNodes];
        String[] column2 = new String[2];
        Arrays.fill(column1, "Node");
        Arrays.fill(column2, "Node");

        for(int x=0;x<nodes.size();x++) {
            String[][] a = new String[numNodes][numNodes];
            String[][] b = new String[numNodes][2];
            final JFrame output = new JFrame("Output window for Router #" + x);

            output.setSize(350, 350);
            int length = nodes.get(x).distanceTable[0].length;
            // Display distance table
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < length; j++) {
                    int[][] temp = nodes.get(x).getDistanceTable();
                    a[i][j] = (String.valueOf(temp[i][j]));
                }
                JTable jt = new JTable(a, column1);
                output.add(jt, BorderLayout.CENTER);
            }

            // Display forwarding table
            for (int i = 0; i < numNodes; i++) {
                Hashtable<String, String> temp = nodes.get(x).getForwardingTable();
                b[i][0] = String.valueOf(i);
                b[i][1] = temp.get(String.valueOf(i));
            }
            JTable ft = new JTable(b, column2);
            output.add(ft, BorderLayout.SOUTH);

            output.setVisible(true);
        }
    }

    /**
     * Reads the node file and returns the single line node info.
     * @param nodePath Path to the "node*.txt" file where * is the node id.
     * @return Single line information written in the node.txt file
     * nodeInfo structure: <nodeID, (neighborId, linkCostToNeighbor, bandwidthToNeighbor), (...), (...)>
     * "(...)" represents additional neighbors
     * Example nodeInfo: "0,(1,5,10),(2,3,15)"
     * @throws IOException Input/output exception
     */
    public static String readNode(String nodePath) throws IOException {
        FileReader fr = new FileReader(nodePath);
        BufferedReader br = new BufferedReader(fr);
        String nodeInfo = br.readLine();
        br.close();

        return nodeInfo;
    }

    /**
     * Reads flow.txt file and return each line in an array.
     *
     * Flow file structure,
     * (Flow name, start, destination, file size)
     * (... additional flows ...)
     *
     * Example flow.txt,
     * A,0,3,100
     * B,0,3,200
     * C,1,2,100
     *
     * @param flowPath Path to the flow.txt file
     * @return An array containing each line
     * @throws IOException Input/output exception
     */
    public static List<String> readFlow(String flowPath) throws IOException {
        FileReader fr = new FileReader(flowPath);
        BufferedReader br = new BufferedReader(fr);
        List<String> flowInfo = new ArrayList<>();

        String line = br.readLine();
        while(line != null){
            flowInfo.add(line);
            line = br.readLine();
        }

        return flowInfo;
    }

    /**
     * Creates a node from the given nodeInfo. If a node has dynamic links, a random cost is generated between [0,10].
     * distanceTable and forwardingTable's are initialized here.
     * @param nodeInfo Node information as a single string, read from the node*.txt file
     * Example nodeInfo: "0,(1,5,10),(2,3,15)"
     * @param numNodes Total number of nodes in the graph.
     * @return Initialized Node object
     */
    private static Node initializeNode(String nodeInfo, int numNodes) {
        Node node = new Node();
        node.initializeDistanceTable(numNodes); // Initialize the distance table

        String[] info; // Example nodeInfo: "0,(1,5,10),(2,3,15)"
        info = nodeInfo.split(",\\(|\\),\\(|\\)");

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
            neighborInfo = neighbor.split(",");
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

    /**
     * Simulates flow(s) depending on the information in the flow.txt file. Paths are marks as "occupied" if they are
     * being used. Example flow simulation,
     *
     * Flow.txt:
     * A,0,3,100
     * B,0,3,200
     *
     * The shortest path from 0 to 3 will be occupied for 100/B seconds where B is the link bandwidth. If a new flow
     * arrives during this period, it is queued.
     *
     * @throws IOException Input/output exception
     */
    private static void simulateFlow() throws IOException {
        List<String> flowInfoArray;
        flowInfoArray = readFlow(ModivSim.flowPath);

        long start = System.nanoTime();

        /*
        Example link duration table,
        Flow - Duration
         01  |   5
         10  |   5
         This means that the link between node 0 and 1 (also 1 and 0 since its bidirectional) is occupied for 5 seconds.
         */
        Hashtable<String,String> linkDuration = new Hashtable<>();
        Queue<String> flowQueue = new LinkedList<>(); // Queued flows are stored here

        System.out.println(flowInfoArray.size() + " flows found.");

        for(String flow : flowInfoArray){
            boolean isQueued = false;
            String[] flowInfo = flow.split(",");
            String flowId = flowInfo[0];
            String source = flowInfo[1];
            String destination = flowInfo[2];
            String size = flowInfo[3];

            System.out.println("\nSimulating Flow " + flowId);
            System.out.println("Source: " + source);
            System.out.println("Destination: " + destination);
            System.out.println("Size: " + size + " Mb");

            List<String> path = new ArrayList<>();
            path.add(source);
            Node node = nodes.get(Integer.parseInt(source)); // Initially this is the source node
            int bottleneck = 999; // Bottleneck bandwidth
            while(!node.nodeID.equals(destination)){ // Until we reach our destination continue
                String hops = node.forwardingTable.get(destination);
                String firstHop = hops.substring(0,1);

                String l1 = source + firstHop;
                String durationForL1 = linkDuration.get(l1);

                if(durationForL1 == null){ // Assign first hop
                    path.add(firstHop);
                    int bandwidthToHop = node.linkBandwidth.get(firstHop);
                    if(bandwidthToHop < bottleneck) bottleneck = bandwidthToHop;
                } else {
                    // TODO: Clear linkDuration table after the transfer is complete and add new flow, else add to queue
                    System.out.println("Links are occupied, adding Flow " + flowId + " to the queue.");
                    flowQueue.add(flow);
                    isQueued = true;
                    break;
                }

                node = nodes.get(Integer.parseInt(firstHop)); // Retrieve next node
            }

            // Fill the link duration table
            int duration = Integer.parseInt(size) / bottleneck;
            for(int i = 0; i < path.size() - 1; i++){
                String link = path.get(i) + path.get(i + 1);
                String linkBackwards = path.get(i + 1) + path.get(i);
                linkDuration.put(link, String.valueOf(duration));
                linkDuration.put(linkBackwards, String.valueOf(duration));
            }

            if(!isQueued) {
                System.out.print("Path: ");
                for (int i = 0; i < path.size(); i++) {
                    if (i == path.size() - 1) {
                        System.out.print(path.get(i) + "\n");
                    } else {
                        System.out.print(path.get(i) + " -> ");
                    }
                }
                System.out.println("Bottleneck bandwidth is " + bottleneck + " Mbits.");
                System.out.println("This path is occupied for " + duration + " seconds.");
            }
        }
    }
}
