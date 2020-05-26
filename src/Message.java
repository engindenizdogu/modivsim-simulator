/**
 * This class represents the message exchanged between nodes
 */
public class Message {
    protected int senderID;
    protected int receiverID;
    protected int distanceVector[]; // Distance vector estimate from a node to another
    protected int linkBandwidth; // Bandwidth between sender and receiver

    /**
     *
     * @param senderID
     * @param receiverID
     * @param distanceVector
     * @param linkBandwidth
     */
    public Message(int senderID, int receiverID, int distanceVector[], int linkBandwidth){
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.distanceVector = distanceVector;
        this.linkBandwidth = linkBandwidth;
    }
}
