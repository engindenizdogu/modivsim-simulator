import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Hashtable;
import java.util.PrimitiveIterator;

/**
 *
 */
public class Node {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 4444;
    protected Socket s;
    protected BufferedReader is;
    protected PrintWriter os;
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

    /**
     * Creates a socket and connects to the server
     */
    public void Connect(){
        try{
            s = new Socket(SERVER_ADDRESS, SERVER_PORT);
            is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            os = new PrintWriter(s.getOutputStream());

            os.println("Hi server");
            os.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
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
