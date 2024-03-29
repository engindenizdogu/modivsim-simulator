import org.w3c.dom.traversal.NodeIterator;

import javax.net.ssl.SSLEngineResult;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Node class representing every router in a network graph
 */
public class Node {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 4444;
    protected Socket s;
    protected ObjectInputStream is;
    protected ObjectOutputStream os;
    protected String nodeID;
    protected Hashtable<String,Integer> linkCost = new Hashtable<>();
    protected Hashtable<String,Integer> linkBandwidth = new Hashtable<>();
    protected Hashtable<String,Node> neighborNodes = new Hashtable<>();
    protected Hashtable<String,Socket> neighborSockets = new Hashtable<>();
    protected Hashtable<String, String> forwardingTable = new Hashtable<>();
    protected int[][] distanceTable;
    protected int[] bottleneckBandwidthTable;
    protected int numNeighbors; // Total number of neighbors
    protected ArrayList<Integer> neighborIds = new ArrayList<>(); // ArrayList containing neighbor IDs
    protected boolean hasDynamicLink = false;
    protected ArrayList<String> dynamicNeighbors = new ArrayList<>();

    /**
     *
     */
    public Node(){}

    /**
     *
     * @param nodeID
     * @param linkCost
     * @param linkBandwidth
     * @param distanceTable
     * @param bottleneckBandwidthTable
     */
    public Node(String nodeID, Hashtable<String,Integer> linkCost, Hashtable<String,Integer> linkBandwidth, int[][] distanceTable, int[] bottleneckBandwidthTable){
        this.nodeID = nodeID;
        this.linkCost = linkCost;
        this.linkBandwidth = linkBandwidth;
        this.distanceTable = distanceTable;
        this.bottleneckBandwidthTable = bottleneckBandwidthTable;
    }

    /**
     * Creates a socket and connects to the server
     */
    public void Connect(){
        try{
            s = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Node" + nodeID + " connected successfully.");
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     *
     * @param numNodes Total number of nodes in the graph
     */
    public void initializeDistanceTable(int numNodes) {
        this.distanceTable = new int[numNodes][numNodes];
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                distanceTable[i][j] = 999;
            }
        }
    }

    /**
     *
     * @param numNodes
     */
    public void initializeForwardingTable(int numNodes){
        for(int i = 0; i < numNodes; i++)
            if(i != Integer.parseInt(nodeID)){
                if(neighborIds.contains(i)){
                    forwardingTable.put(String.valueOf(i),String.valueOf(i));
                } else {
                    forwardingTable.put(String.valueOf(i),"null");
                }
            }
    }

    /**
     *
     * @param senderId
     */
    public boolean receiveUpdate(String senderId){
        boolean isUpdated = false;
        Socket neighborSocket = neighborSockets.get(senderId);
        try {
            is = new ObjectInputStream(neighborSocket.getInputStream());
            Message m = (Message) is.readObject();
            System.out.println("Node " + nodeID + " received a message from Node " + m.senderID);

            // Update the distance table according to the formula dx(y) = min{ c(x,v) + dv(y) }
            int numNodes = distanceTable[0].length;
            int previousCostFromNbr = 999;

            for(int i = 0; i < numNodes; i++){
                if(i != Integer.parseInt(nodeID)){ // i == id -> cost will be 0, so don't check
                    int dx = distanceTable[Integer.parseInt(nodeID)][i]; // this node's distance table
                    for(int j = 0; j < numNeighbors; j++){ // Check costs to and from each neighbor
                        int nbrId = neighborIds.get(j);
                        int c = linkCost.get(String.valueOf(nbrId));
                        int dv = m.distanceTable[nbrId][i];
                        int costFromNeighbor = c + dv;

                        // TODO: Clarify this section
                        if(costFromNeighbor < dx){ // Update value
                            forwardingTable.replace(String.valueOf(i), String.valueOf(nbrId)); // Update forwarding table
                            distanceTable[Integer.parseInt(nodeID)][i] = costFromNeighbor;
                            distanceTable[i][Integer.parseInt(nodeID)] = costFromNeighbor;
                            isUpdated = true;
                        } else if (dx < costFromNeighbor && costFromNeighbor < previousCostFromNbr){ // Find the second closest route
                            String forwardingNodes = forwardingTable.get(String.valueOf(i));
                            String[] forwardInfo = forwardingNodes.split("\\,");
                            if(forwardInfo.length < 2){
                                forwardingNodes += ", " + String.valueOf(nbrId);
                                forwardingTable.replace(String.valueOf(i), forwardingNodes); // Update forwarding table
                            } else {
                                if(!forwardInfo[1].equals(String.valueOf(nbrId))){
                                    forwardingNodes = forwardInfo[0] + ", " + String.valueOf(nbrId);
                                    forwardingTable.replace(String.valueOf(i), forwardingNodes); // Update forwarding table
                                }
                            }

                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Print update summary
        System.out.println("SenderID: " + senderId);
        System.out.println("ReceiverID: " + nodeID);
        printDistanceTable();
        if(isUpdated){
            System.out.println("Node " + nodeID + " has been updated.");
        } else {
            System.out.println("No update occurred in Node " + nodeID);
        }

        return isUpdated;
    }

    /**
     *
     * @return
     */
    public boolean sendUpdate(){
        int counter = 0;
        for(int i = 0; i < numNeighbors; i++){
            int neighbor = neighborIds.get(i);
            Message m = new Message(Integer.parseInt(nodeID), neighbor, distanceTable, forwardingTable);
            try {
                os = new ObjectOutputStream(s.getOutputStream());
                os.writeObject(m); // Send message through socket
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Call neighbor's receive method
            Node neighborNode = neighborNodes.get(String.valueOf(neighbor));
            boolean isUpdated = neighborNode.receiveUpdate(nodeID);
            if(!isUpdated){
                counter++;
            }
        }

        // If the graph has converged
        if(counter != numNeighbors){
            return true; // An update in one of the nodes has occurred
        } else {
            return false; // Neighbors have converged
        }
    }

    /**
     *
     * @return
     */
    public Hashtable<String,String> getForwardingTable(){
        return this.forwardingTable;
    }

    /**
     *
     * @return
     */
    public int[][] getDistanceTable(){
        return this.distanceTable;
    }

    /**
     * Prints the content of the distanceTable
     */
    public void printDistanceTable(){
        System.out.println("Node " + nodeID + " distanceTable:");
        int length = distanceTable[0].length;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                if (j == length - 1) {
                    System.out.print(distanceTable[i][j]);
                } else {
                    System.out.print(distanceTable[i][j] + ", ");
                }
            }
            System.out.println("");
        }
    }
}
