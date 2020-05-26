import java.util.Hashtable;

/**
 *
 */
public class Node {
    protected String nodeID;
    protected Hashtable<String,Integer> linkCost = new Hashtable<String,Integer>();
    protected Hashtable<String,Integer> linkBandwidth = new Hashtable<String,Integer>();
    protected int distanceTable[][];
    protected int bottleneckBandwidthTable[];
    protected Hashtable<String, String> forwardingTable = new Hashtable<String,String>();

    public Node(){}

    public Node(String nodeID, Hashtable<String,Integer> linkCost, Hashtable<String,Integer> linkBandwidth, int distanceTable[][], int bottleneckBandwidthTable[]){
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
