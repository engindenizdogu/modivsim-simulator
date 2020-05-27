import java.io.Serializable;

/**
 * This class represents the message exchanged between nodes
 */
public class Message implements Serializable {
    protected int senderID;
    protected int receiverID;
    protected int[][] distanceTable; // Distance vector estimate from a node to another
    protected int linkBandwidth; // Bandwidth between sender and receiver

    /**
     *
     * @param senderID
     * @param receiverID
     * @param distanceTable
     */
    public Message(int senderID, int receiverID, int[][] distanceTable){
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.distanceTable = distanceTable;
    }
}
