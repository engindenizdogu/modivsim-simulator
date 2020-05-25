import java.util.Hashtable;

/**
 *
 */
public class Node {
    protected int nodeID;
    protected Hashtable<String,Integer> linkCost;
    protected Hashtable<String,Integer> linkBandwidth;
    protected int distanceTable[][];
    protected int bottleneckBandwidthTable[];
    protected Hashtable<String, String> forwardingTable;

    public Node(int nodeID, Hashtable<String,Integer> linkCost, Hashtable<String,Integer> linkBandwidth, int distanceTable[][], int bottleneckBandwidthTable[]){
        this.nodeID = nodeID;
        this.linkCost = linkCost;
        this.linkBandwidth = linkBandwidth;
        this.distanceTable = distanceTable;
        this.bottleneckBandwidthTable = bottleneckBandwidthTable;
    }

    public void receiveUpdate(Message m){
        System.out.println("Sender ID: " + m.senderID);
        System.out.println("Receiver ID: " + m.receiverID);
    }

    public boolean sendUpdate(){
        return false;
    }

    // Initialize forwarding table
    public Hashtable<Integer,Integer> getForwardingTable(){
        return null;
    }
}
