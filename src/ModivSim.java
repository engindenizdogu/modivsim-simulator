import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;

public class ModivSim extends Thread {
    private static final String nodesFolder = "D:\\Code\\modivsim-simulator\\nodes";
    private static final String flowPath = "D:\\Code\\modivsim-simulator\\flow\\flow.txt";
    //private static final String nodesFolder = "/Users/berrakperk/Desktop/416/modivsim-simulator/nodes";
    private static final int SERVER_PORT = 4444;
    static ArrayList<Node> nodes = new ArrayList<>(); // Arraylist to keep nodes
    static  ArrayList<Socket> sockets = new ArrayList<>(); // Arraylist to keep sockets
    static int numDynamicLinks;
    static Random rand = new Random();

    public static void main(String args[]) throws IOException, InterruptedException {
        System.out.println("ModivSim started...");
        Scanner sc= new Scanner(System.in);

        System.out.print("Please enter the period in seconds: ");
        int p = sc.nextInt();

        /* Reading nodes */
        String[] nodeFiles;
        File f = new File(nodesFolder);
        nodeFiles = f.list();
        int numNodes = nodeFiles.length; // Total number of nodes

        numDynamicLinks = 0;
        String nodeInfo;
        for(String nodeFile : nodeFiles){
            nodeInfo = readNode(nodesFolder + "\\" + nodeFile);
            //nodeInfo = readNode(nodesFolder + "/" + nodeFile);
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

        int iterations = 0;
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
            iterations++;
            Thread.sleep(p * 1000);
        }

        System.out.println("\nTotal number of iterations: " + iterations);

        /* Flow simulation */
        System.out.println("\nStarting the simulation...");
        simulateFlow(flowPath);
        System.out.println("");

        /* POPUP */
        String[] column1 = new String[numNodes];
        String[] column2 = new String[2];
        Arrays.fill(column1, "Node");
        Arrays.fill(column2, "Node");
        double time=0.0;
        //Distance table
        for(int x=0;x<nodes.size();x++) {
            String[][] a = new String[numNodes][numNodes];
            String[][] b = new String[numNodes][2];
            final JFrame output = new JFrame("Output window for Router #" + x);
            //output.setVisible(true);
            JLabel l = new JLabel("Current state for router " + x + " at time " + time);
            output.add(l,BorderLayout.NORTH);

            output.setSize(350, 350);
            int length = nodes.get(x).distanceTable[0].length;
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < length; j++) {
                    int[][] temp = nodes.get(x).getDistanceTable();
                    a[i][j] = (String.valueOf(temp[i][j]));
                }
                JTable jt = new JTable(a, column1);
                output.add(jt, BorderLayout.CENTER);
            }

            //Forwarding table
            for (int i = 0; i < numNodes; i++) {
                Hashtable<String, String> temp = nodes.get(x).getForwardingTable();
                //b[i][0] = String.valueOf(temp.get(i));
                b[i][0] = String.valueOf(i);
                b[i][1] = temp.get(String.valueOf(i));
            }
            JTable ft = new JTable(b, column2);
            output.add(ft, BorderLayout.SOUTH);

            output.setVisible(true);
        }

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
        br.close();

        return nodeInfo;
    }

    /**
     *
     * @param flowPath
     * @return
     * @throws IOException
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

    /**
     *
     * @param flowPath
     */
    private static void simulateFlow(String flowPath) throws IOException {
        List<String> flowInfoArray = readFlow(flowPath);

        long start = System.nanoTime();
        Hashtable<String,String> linkDuration = new Hashtable<>();
        Queue<String> flowQueue = new LinkedList<>();

        System.out.println(flowInfoArray.size() + " flows found.");

        for(String flow : flowInfoArray){
            boolean isQueued = false;
            String[] flowInfo = flow.split("\\,");
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
                String secondHop = hops.substring(3,4);

                String l1 = source + firstHop;
                String l2 = source + secondHop;
                String durationForL1 = linkDuration.get(l1);
                String durationForL2 = linkDuration.get(l2);

                if(durationForL1 == null){ // Assign first hop
                    path.add(firstHop);
                    int bandwidthToHop = node.linkBandwidth.get(firstHop);
                    if(bandwidthToHop < bottleneck) bottleneck = bandwidthToHop;
                } else {
                    /*
                    int startInSecond = Math.toIntExact(TimeUnit.SECONDS.convert(start, TimeUnit.NANOSECONDS));
                    int currentTimeInSeconds = Math.toIntExact(TimeUnit.SECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS));
                    int timeElapsed = currentTimeInSeconds - startInSecond;
                    if(timeElapsed < Integer.parseInt(durationForL1)){ // Link1 is still occupied, check the next link
                        if(durationForL2 == null){
                            path.add(secondHop);
                            int bandwidthToHop = node.linkBandwidth.get(secondHop);
                            if(bandwidthToHop < bottleneck) bottleneck = bandwidthToHop;
                        } else {
                            if(timeElapsed < Integer.parseInt(durationForL2)){ // Second link is also occupied, queue link
                                System.out.println("Links are occupied, adding Flow " + flowId + " to the queue.");
                                flowQueue.add(flow);
                            } else { // Time has elapsed, assign link 2
                                path.add(secondHop);
                                int bandwidthToHop = node.linkBandwidth.get(secondHop);
                                if(bandwidthToHop < bottleneck) bottleneck = bandwidthToHop;
                            }
                        }
                    } else { // Time has elapsed, assign link 1
                        path.add(firstHop);
                        int bandwidthToHop = node.linkBandwidth.get(firstHop);
                        if(bandwidthToHop < bottleneck) bottleneck = bandwidthToHop;
                    }
                    */
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
