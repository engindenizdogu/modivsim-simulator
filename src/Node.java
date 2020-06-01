import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Node class representing a router in a network graph
 */
public class Node {
    protected String nodeID;
    protected int numNeighbors; // Total number of neighbors
    protected boolean hasDynamicLink = false;
    protected Hashtable<String,Integer> linkCost = new Hashtable<>();
    protected Hashtable<String,Integer> linkBandwidth = new Hashtable<>();
    protected Hashtable<String,Node> neighborNodes = new Hashtable<>();
    protected Hashtable<String, String> forwardingTable = new Hashtable<>();
    protected ArrayList<Integer> neighborIds = new ArrayList<>(); // ArrayList containing neighbor IDs
    protected ArrayList<String> dynamicNeighbors = new ArrayList<>();
    protected int[][] distanceTable;

    public Node(){}

    /**
     * Node object that represents a router in a network
     * @param nodeID Id of the node (router) object
     * @param linkCost Hashtable containing link costs to neighbors
     * @param linkBandwidth Hashtable containing bandwidths to neighbors
     * @param distanceTable 2d array containing lowest costs to every node in the network
     */
    public Node(String nodeID, Hashtable<String,Integer> linkCost, Hashtable<String,Integer> linkBandwidth, int[][] distanceTable){
        this.nodeID = nodeID;
        this.linkCost = linkCost;
        this.linkBandwidth = linkBandwidth;
        this.distanceTable = distanceTable;
    }

    /**
     * Initializes the distance table. Initially, every cell is set to 999 to indicate infinity.
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
     * Initializes the forwarding table. Initially, only neighbors are assigned a value. If the node is not connected
     * "null" is written to the table.
     *
     * For example, given the graph below
     * 0 ---- 1 ----- 2
     *  \   /
     *   \ /
     *    3
     *
     * Node 0's forwarding table during initialization would look like the following,
     * Destination -  Forward to
     *   Node 1    |     1
     *   Node 2    |     2
     *   Node 3    |    null
     *
     * The values are updates in receiveUpdate() method.
     * @param numNodes Total number of nodes in the graph
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
     * receiveUpdate() takes a Message object and looks for updates. If a shorter path exists, the distanceTable and
     * forwardingTable is updated. The formula used for finding the shortest path is as follows,
     * dx(y) = min{ c(x,v) + dv(y) } where x is the source node and y is the destination. v indicates every neighbor
     * of x.
     *
     * The function looks for a path which goes through v. There may be multiple nodes between v and y. To calculate
     * the distance between v and y, v's distanceTable is used.
     *
     *   x --- v --- ... --- y
     *
     * @param m Message object sent from neighbor
     * @return true if node was updated, false otherwise
     */
    public boolean receiveUpdate(Message m){
        boolean isUpdated = false;

        // Retrieve values
        int receiverID = m.receiverID;
        int senderID = m.senderID;
        System.out.println("Node " + receiverID + " received a message from Node " + senderID);

        // Update the distance table according to the formula dx(y) = min{ c(x,v) + dv(y) }
        int numNodes = distanceTable[0].length;

        for(int i = 0; i < numNodes; i++){
            if(i != receiverID){ // i == id -> cost will be 0, so don't check
                int dx = distanceTable[receiverID][i]; // this node's distance table
                for(int j = 0; j < numNeighbors; j++){ // Check costs to and from each neighbor
                    int nbrId = neighborIds.get(j);
                    int c = linkCost.get(String.valueOf(nbrId));
                    int dv = m.distanceTable[nbrId][i];
                    int costFromNeighbor = c + dv;

                    if(costFromNeighbor < dx){ // Update value
                        forwardingTable.replace(String.valueOf(i), String.valueOf(nbrId)); // Update forwarding table
                        distanceTable[receiverID][i] = costFromNeighbor;
                        distanceTable[i][receiverID] = costFromNeighbor;
                        isUpdated = true;
                    }
                }
            }
        }

        // Print update summary
        System.out.println("SenderID: " + senderID);
        System.out.println("ReceiverID: " + receiverID);
        printDistanceTable();
        if(isUpdated){
            System.out.println("Node " + receiverID + " has been updated.");
        } else {
            System.out.println("No update occurred in Node " + receiverID);
        }

        return isUpdated;
    }

    /**
     * Send updates to neighbors in a Message object. For details of the message see Message.java
     * @return true if an update has occurred in at least one of the neighbors, false otherwise
     */
    public boolean sendUpdate(){
        int counter = 0;
        for(int i = 0; i < numNeighbors; i++){
            int neighbor = neighborIds.get(i);
            Message m = new Message(Integer.parseInt(nodeID), neighbor, distanceTable, forwardingTable);

            // Call neighbor's receive method
            Node neighborNode = neighborNodes.get(String.valueOf(neighbor));
            boolean isUpdated = neighborNode.receiveUpdate(m);
            if(!isUpdated){
                counter++;
            }
        }

        // If the neighbors have converged (no more updates are taking place) return true
        // Neighbors have converged
        return counter != numNeighbors; // An update in one of the nodes has occurred
    }

    /**
     * Returns the forwarding table.
     * @return Hashtable<String,String> forwardingTable
     */
    public Hashtable<String,String> getForwardingTable(){
        return this.forwardingTable;
    }

    /**
     * Returns the distance table.
     * @return int[][] distanceTable
     */
    public int[][] getDistanceTable(){
        return this.distanceTable;
    }

    /**
     * Prints the content of the distanceTable.
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
            System.out.println();
        }
    }
}
