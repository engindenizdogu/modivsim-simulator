import java.io.Serializable;
import java.util.Hashtable;

/**
 * This class represents the message exchanged between nodes
 */
public class Message implements Serializable {
    protected int senderID;
    protected int receiverID;
    protected int[][] distanceTable; // Distance vector estimate from a node to another
    protected Hashtable<String, String> forwardingTable;

    /**
     * Message object is used to transfer information between nodes.
     * @param senderID Sender Id
     * @param receiverID Receiver Id
     * @param distanceTable Distance table of the sender
     * @param forwardingTable Forwarding table of the sender
     */
    public Message(int senderID, int receiverID, int[][] distanceTable, Hashtable<String, String> forwardingTable){
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.distanceTable = distanceTable;
        this.forwardingTable = forwardingTable;
    }
}
