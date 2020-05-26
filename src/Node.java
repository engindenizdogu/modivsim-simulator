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
     * @param senderId
     */
    public void receiveUpdate(String senderId){
        Socket neighborSocket = neighborSockets.get(senderId);
        try {
            is = new ObjectInputStream(neighborSocket.getInputStream());
            Message m = (Message) is.readObject();
            System.out.println("Node" + nodeID + " received a message from Node" + m.senderID);

            /*
            // Content of the distanceTable (sent from neighbor)
            int length = m.distanceTable[0].length;
            for(int i = 0; i < length; i++){
                for(int j = 0; j < length; j++){
                    System.out.print(m.distanceTable[i][j] + ", ");
                }
                System.out.println("");
            }
            */

            //TODO: Update the distance table according to the formula dx(y) = min{ c(x,v) + dv(y) }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return
     */
    public boolean sendUpdate(){
        //TODO: Decide whether an update is needed or not
        if(true){ // If an update is needed
            neighborIds.forEach(neighbor -> { // For each neighbor
                Message m = new Message(Integer.parseInt(nodeID), neighbor, distanceTable);
                try {
                    os = new ObjectOutputStream(s.getOutputStream());
                    os.writeObject(m); // Send message through socket
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Call neighbor's receive method
                Node neighborNode = neighborNodes.get(String.valueOf(neighbor));
                neighborNode.receiveUpdate(nodeID);
            });
            return true;
        }

        return false;
    }

    /**
     *
     * @return
     */
    public Hashtable<Integer,Integer> getForwardingTable(){
        return null;
    }
}
