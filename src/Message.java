/**
 * This class represents the message exchanged between nodes
 */
public class Message {
    protected int senderID;
    protected int receiverID;
    protected int distanceVectorEstimate; // Distance vector estimate from a node to another
    protected int linkBandwidth; // Bandwidth between sender and receiver

    public Message(int senderID, int receiverID, int distanceVectorEstimate, int linkBandwidth){
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.distanceVectorEstimate = distanceVectorEstimate;
        this.linkBandwidth = linkBandwidth;
    }
}
