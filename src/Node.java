import java.util.Hashtable;

public class Node {
    private int nodeID;
    private Hashtable<Integer,Integer> linkCost = new Hashtable<Integer,Integer>();
    private Hashtable<Integer,Integer> linkBandwidth = new Hashtable<Integer,Integer>();
    private int distanceTable[][];
    private int bottleneckBandwidthTable[];

    public Node(int nodeID, Hashtable<Integer,Integer> linkCost, Hashtable<Integer,Integer> linkBandwidth, int distanceTable[][], int bottleneckBandwidthTable[]){
        this.nodeID = nodeID;
        this.linkCost = linkCost;
        this.linkBandwidth = linkBandwidth;
        this.distanceTable = distanceTable;
        this.bottleneckBandwidthTable = bottleneckBandwidthTable;
    }

    public void receiveUpdate(Message m){

    }

    public boolean sendUpdate(){
        return false;
    }

    public Hashtable<Integer,Integer> getForwardingTable(){
        return null;
    }
}
