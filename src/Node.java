import java.util.Hashtable;

/**
 *
 */
public class Node {
    protected int nodeID;
    protected Hashtable<Integer,Integer> linkCost = new Hashtable<Integer,Integer>();
    protected Hashtable<Integer,Integer> linkBandwidth = new Hashtable<Integer,Integer>();
    protected int distanceTable[][];
    protected int bottleneckBandwidthTable[];

    public Node(int nodeID, Hashtable<Integer,Integer> linkCost, Hashtable<Integer,Integer> linkBandwidth, int distanceTable[][], int bottleneckBandwidthTable[]){
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

    public Hashtable<Integer,Integer> getForwardingTable(){
        return null;
    }
}
