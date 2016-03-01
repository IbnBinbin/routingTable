import java.util.ArrayList;

public class NodeDetail {
	private String node;
	private String destinationNode;
	private String outGoingNode;
	private int cost;
//	private ArrayList<String> neighborNode;
	
	public NodeDetail(String node, String destinationNode, String outGoingNode, int cost){
		this.node=node;
		this.destinationNode=destinationNode;
		this.outGoingNode=outGoingNode;
		this.cost=cost;
		
	}
	
	public String getNode(){return node;}
	public String getDestinationNode(){return destinationNode;}
	public String getOutGoingNode(){return outGoingNode;}
	public int getCost(){return cost;}
	public void setCost(int cost){this.cost=cost;}
//	public ArrayList getNeighBorNode(){return neighborNode;}
//	public void setNeighBorNode(String nbN){
//		neighborNode.add(nbN);
//	}
}
