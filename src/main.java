
import java.io.*;
import java.lang.reflect.Array;
import java.time.temporal.IsoFields;
import java.util.*;

import org.w3c.dom.NodeList;

public class main {
	private static boolean isFalsePath = true;
	private static int nodesCount = 0;
	private static int[][] matrixCost;
	private static Hashtable<String, Integer> matrixIndex = new Hashtable<>();
	private static Hashtable<Integer, String> matrixIndexInverse = new Hashtable<>();
	private static Hashtable<String, ArrayList<NodeDetail>> routingTable = new Hashtable<>();
	private static Hashtable<String, ArrayList<String>> neighborTable = new Hashtable<>();
	private static Hashtable<String, Integer> linkFail = new Hashtable<>();
	private static ArrayList<String> shortestPath = new ArrayList<>();
	private static int costShortTestPath = 0;
	private static boolean checkNotFail = true;
	private static boolean isFinish = false;
	private static String startRoute = "";
	private static boolean isApplySplitHorizon = false;
	private static int countAchive=0;
	private static boolean firstUpdate = true;
	private static boolean firstFindBestRoute = true;
	public static void main(String[] args) {
		resetRoutingTable(false);
		while (true) {
			System.out.println("\n");
			System.out.println("-------------------------------------------");
			System.out.println("Options:");
			System.out.println("-------------------------------------------");
			System.out.println("*Compute Routing Tables        >> PRESS 1");
			System.out.println("*Change Link Cost              >> PRESS 2");
			System.out.println("*View the best route           >> PRESS 3");
			System.out.println("*Apply/Unapply A Split-Horizon >> PRESS 4");
			System.out.println("*Show Routing Tables           >> PRESS 5");
			System.out.println("*Reset The Routing Tables      >> PRESS 6");
			System.out.println("*Exit The Program              >> PRESS 7");
			System.out.println("-------------------------------------------");
			if (!isApplySplitHorizon) {
				System.out.println("*******Split horizon is NOT applied*******");
			} else {
				System.out.println("*********Split horizon is applied*********");
			}
			Scanner input = new Scanner(System.in);
			printMatrixCost();
			int option = 0;
			if (input.hasNextInt()) {
				option = input.nextInt();
			} else {
				System.out.println("Wrong input format. Please try again.");
			}
			switch (option) {
			case 1:
				System.out.println("Type number of iterations for updating and \nset of nodes to trace\n(e.g. 3 N1,N3,N4): ");
				input = new Scanner(System.in);
				String line = input.nextLine();
				if (line.split(" ").length < 2) {
					System.out.println("Wrong input format.");
					break;
				}
				int interations = Integer.parseInt(line.split(" ")[0].trim());
				String nodesString = line.split(" ")[1].trim();
				System.out.println();
				updateRoutingTable(interations, nodesString);
				break;
			case 2:
				System.out.println("Change the cost link, type in this form\n(e.g. N1,N2,Cost): ");
				input = new Scanner(System.in);
				line = input.nextLine();
				if (line.split(",").length < 3) {
					System.out.println("Wrong input format.");
					break;
				}
				String source = line.split(",")[0].trim();
				String dest = line.split(",")[1].trim();
				int cost = Integer.parseInt(line.split(",")[2].trim());
				System.out.println();
				updateLinkCost(source, dest, cost);
				break;
			case 3:
				
				System.out.println("Type source and destination node in this form\n(e.g. Nsource,Ndest): ");
				input = new Scanner(System.in);
				line = input.nextLine();
				if (line.split(",").length < 2) {
					System.out.println("Wrong input format.");
					break;
				}
				source = line.split(",")[0].trim();
				dest = line.split(",")[1].trim();
				costShortTestPath = 0;
				countAchive=0;
				checkNotFail = true;
				isFinish = false;
				firstFindBestRoute = true;
				startRoute = source;
				System.out.println();
				findBestRoute(source, dest, 0, source);
				break;
			case 4:
				isApplySplitHorizon = !isApplySplitHorizon;
				break;
			case 5:
				System.out.println("Type set of nodes or \ntype 'all' to see all routing tables\n(e.g. N1,N2,N4,N5): ");
				input = new Scanner(System.in);
				line = input.nextLine();
				System.out.println();
				printRoutingTable(line);
				break;
			case 6:
				firstUpdate=true;
				System.out.println();
				resetRoutingTable(false);
				printRoutingTable("all");
				System.out.println("Updated!");
				break;
			case 7:
				System.out.println();
				System.out.println("Bye Bye!");
				return;
			default:
				System.out.println();
				System.out.println("Wrong option number. \nPlease choose the option again!");
				break;
			}
		}
	}

	private static void printRoutingTable(String nodesString) {
		String nodes[] = nodesString.split(",");
		Hashtable<String, Integer> nodesHash = new Hashtable<>();
		for (int i = 0; i < nodes.length; i++) {
			nodesHash.put(nodes[i], 0);
		}
		for (int i = 0; i < matrixIndexInverse.size(); i++) {
			if(nodesHash.get("all")!=null){
				ArrayList<NodeDetail> ndList = routingTable.get(matrixIndexInverse.get(i));
				System.out.println("Routing Table of Node " + matrixIndexInverse.get(i));
				System.out.println("---------------------------------");
				System.out.println("Dest-node   Cost   Outgoing-Link");
				System.out.println("---------------------------------");
				for (int j = 0; j < ndList.size(); j++) {
						NodeDetail nd = ndList.get(j);
						System.out.println("  " + nd.getDestinationNode() + "          " + nd.getCost() + "          "
								+ nd.getNode() + "-" + nd.getOutGoingNode());
				}
				System.out.println("---------------------------------");
			}else if(nodesHash.get(matrixIndexInverse.get(i))!=null){
				ArrayList<NodeDetail> ndList = routingTable.get(matrixIndexInverse.get(i));
				System.out.println("Routing Table of Node " + matrixIndexInverse.get(i));
				System.out.println("---------------------------------");
				System.out.println("Dest-node   Cost   Outgoing-Link");
				System.out.println("---------------------------------");
				for (int j = 0; j < ndList.size(); j++) {
						NodeDetail nd = ndList.get(j);
						System.out.println("  " + nd.getDestinationNode() + "          " + nd.getCost() + "          "
								+ nd.getNode() + "-" + nd.getOutGoingNode());
				}
				System.out.println("---------------------------------");
			}
			
		}

	}

	private static void resetRoutingTable(boolean fromChangeCost) {
		isFalsePath = true;
		nodesCount = 0;
		matrixIndex = new Hashtable<>();
		matrixIndexInverse = new Hashtable<>();
		routingTable = new Hashtable<>();
		neighborTable = new Hashtable<>();
		linkFail = new Hashtable<>();
		while (isFalsePath) {
//			 System.out.println("Path to the text file (full path): ");
//			 Scanner input = new Scanner(System.in);
//			 String pathToTextFile = input.next();
//			 readFile(pathToTextFile);
			readFile("D:/workspace/ANC4_Routing/src/initial.txt");
//			 readFile("D:/workspace/ANC4_Routing/src/test2.txt");
//			 readFile("D:/workspace/ANC4_Routing/src/test3.txt");
			// readFile("D:/workspace/ANC4_Routing/src/test.txt");
		}
	}

	private static void findBestRoute(String source, String dest, int checkCost, String path) {
		ArrayList<NodeDetail> ndListS = routingTable.get(source);
		ArrayList<NodeDetail> ndListD = routingTable.get(dest);
		if (ndListS == null && ndListD == null) {
			System.out.println("This path " + source + "-" + dest + " not found");
			return;
		}
		ArrayList<NodeDetail> ndList = routingTable.get(source);
		if (ndList == null) {
			System.out.println("No route from " + source + " to " + dest);
			return;
		}
		
		for (int i = 0; i < ndList.size(); i++) {
			
			if (ndList.get(i).getDestinationNode().equals(dest)) {
				shortestPath.add(ndList.get(i).getOutGoingNode());
				if (ndList.get(i).getOutGoingNode().equals(dest)) {
					if(firstFindBestRoute){
						checkCost = ndList.get(i).getCost();
						path= ndList.get(i).getNode();
						firstFindBestRoute = false;
					}else{
						path += ","+ndList.get(i).getOutGoingNode();
					}
					String tmpPath[] = path.split(",");
					costShortTestPath = matrixCost[matrixIndex.get(tmpPath[0])][matrixIndex
							.get(tmpPath[tmpPath.length-1])];

					if (ndList.get(i).getCost() >= 16 && !isFinish) {
						System.out.println("Link between "+ndList.get(i).getNode()+", "+ndList.get(i).getOutGoingNode()+" is fail.\nThis route " + startRoute + " to " + dest + " is unreachable.");
						return;
					}
					if(checkCost!=costShortTestPath){
						System.out.println(checkCost+" != "+costShortTestPath+ " "+path);
						return;
					}
					if (checkNotFail&&!isFinish) {
						System.out.println("The shortest path is ["+path + "]. cost: " + costShortTestPath);
						isFinish = true;
						return;

					}

				} else if (ndList.get(i).getCost() < 16) {
					firstFindBestRoute = false;
					boolean isLoop=false;
					String [] checkLoop = path.split(",");
					Hashtable<String, Integer> checkNode = new Hashtable<>();
					for (int j = 0; j < checkLoop.length; j++){
						if(checkNode.put(checkLoop[j],0)!=null){
							isLoop = true;
						}
					}
					
					if (isLoop){
						System.out.println("Routing loop occur!! >> " + shortestPath.toString());
						System.out.println("Link between "+ndList.get(i).getNode()+", "+ndList.get(i).getOutGoingNode()+" is fail.");
						System.out.println("This route: " + startRoute + " to " + dest + " is unreachable");
						checkNotFail = false;
						return;
					}
					if(checkNotFail){
						findBestRoute(ndList.get(i).getOutGoingNode(), dest, ndList.get(i).getCost()+checkCost, path+","+ndList.get(i).getOutGoingNode());
					}else{
						return;
					}

				} else if (!isFinish){
					System.out.println("Link from "+ndList.get(i).getNode()+" to "+ndList.get(i).getOutGoingNode()+" is fail.\nThis route " + source + " to " + dest + " is unreachable.");
					checkNotFail = false;
					return;
				}

			}
		}

	}

	private static void updateLinkCost(String source, String dest, int cost) {
		ArrayList<NodeDetail> ndListS = routingTable.get(source);
		ArrayList<NodeDetail> ndListD = routingTable.get(dest);
		boolean checkIsDirectNeighbor = false;
		
		if (ndListS == null || ndListD == null) {
			System.out.println("This link " + source + "-" + dest + " not found");
			return;
		}
		for (int i = 0; i < neighborTable.get(source).size(); i++) {
			if (neighborTable.get(source).get(i).equals(dest)) {
				checkIsDirectNeighbor = true;
			}
		}
		if(cost>=16){
			cost=16;
			linkFail.put(dest+","+source, cost);
			linkFail.put(source+","+dest, cost);
		}else{
			linkFail.remove(dest+","+cost);
			linkFail.remove(source+","+dest);
		}
		if (!checkIsDirectNeighbor) {
			System.out.println("This link " + source + "-" + dest + " not a direct neighbor");
			return;
		}
		for (int i = 0; i < ndListS.size(); i++) {
			if (ndListS.get(i).getDestinationNode().equals(dest)) {
				ndListS.get(i).setCost(cost);
				ndListS.get(i).setOutgoingNode(dest);
				matrixCost[matrixIndex.get(source)][matrixIndex.get(dest)] = cost;
				break;
			} else if (i == ndListS.size() - 1) {
				System.out.println("This link " + source + "-" + dest + " not found");
				return;
			}
		}

		for (int i = 0; i < ndListD.size(); i++) {
			if (ndListD.get(i).getDestinationNode().equals(source)) {
				ndListD.get(i).setCost(cost);
				ndListD.get(i).setOutgoingNode(source);
				matrixCost[matrixIndex.get(dest)][matrixIndex.get(source)] = cost;
				printRoutingTable("all");
				break;
			} else if (i == ndListS.size() - 1) {
				System.out.println("This link " + source + "-" + dest + " not found");
			}
		}

	}

	private static void printMatrixCost() {
		for (int i = 0; i < matrixCost.length; i++) {
			for (int j = 0; j < matrixCost[i].length; j++) {
				System.out.print(matrixCost[i][j] + "    ");
			}
			System.out.println();
		}

	}

	private static void updateRoutingTable(int iterations, String nodesString) {
		for (int it = 0; it < iterations; it++) {
			boolean checkIsMoreThanOneInterface = false;
			if(!firstUpdate){
				
				for (int i = 0; i < nodesCount; i++) {
					if(routingTable.get(matrixIndexInverse.get(i)).size()>nodesCount){
						checkIsMoreThanOneInterface = checkIsMoreThanOneInterface|true;
					}
				}
			}
			firstUpdate=false;
			int tmpCountAchive=0;
			int[][] tmpMatrixCost = new int[nodesCount][nodesCount];
			for (int i = 0; i < nodesCount; i++) {
				for (int j = 0; j < nodesCount; j++) {
					tmpMatrixCost[i][j] = matrixCost[i][j];
				}
			}
			Hashtable<String, ArrayList<NodeDetail>> tmpRoutingTable = new Hashtable<>();
			System.out.println("Updating routing table round: " + (it + 1));
			boolean allUpToDate = true;
			
			for (int i = 0; i < nodesCount; i++) { // all node
				String nodeName = matrixIndexInverse.get(i);
				ArrayList<String> directNeighborList = new ArrayList<>();
				directNeighborList.addAll(neighborTable.get(nodeName));

				ArrayList<NodeDetail> myRoutingTable = new ArrayList<>();
				myRoutingTable.addAll(routingTable.get(nodeName));
				Hashtable<String, ArrayList<NodeDetail>> allMyNeighborRoutingTable = new Hashtable<>();
				
				System.out.println(nodeName);
				for (int j = 0; j < directNeighborList.size(); j++) { 
					// all neighbor of this node
					String neighborNodeName = directNeighborList.get(j);
					ArrayList<NodeDetail> neighborTable = new ArrayList<>();
					neighborTable.addAll(routingTable.get(directNeighborList.get(j)));
					allMyNeighborRoutingTable.put(neighborNodeName, neighborTable);
					System.out.print(directNeighborList.get(j)+" ");
					
				}
				System.out.println();
				
				for (int j = 0; j < myRoutingTable.size(); j++) {
					NodeDetail myPresentNode = myRoutingTable.get(j);
					ArrayList<NodeDetail> tmpNodeArray = new ArrayList<>();
					tmpNodeArray.addAll(myRoutingTable);
					for (int k = 0; k < directNeighborList.size(); k++) {
						
						ArrayList<NodeDetail> neighborRoutingTable = new ArrayList<>();
						neighborRoutingTable.addAll(allMyNeighborRoutingTable.get(directNeighborList.get(k)));
						for (int l = 0; l < neighborRoutingTable.size(); l++) {
							
							NodeDetail neighborPresentNode = neighborRoutingTable.get(l);
							if(linkFail.get(myPresentNode.getNode()+neighborPresentNode.getNode())!=null){
								// check link fail
								break;
							}else if(myPresentNode.getNode().equals(neighborPresentNode.getNode())){
								//same router
								break;
							}else if(myPresentNode.getDestinationNode().equals(neighborPresentNode.getDestinationNode())){
								//same destination
								
								
								
							}else{
								//different destination >> initial routing table
								boolean isExist = false;
//								System.out.println()
								if(allMyNeighborRoutingTable.get(neighborPresentNode.getDestinationNode())==null && !neighborPresentNode.getDestinationNode().equals(nodeName)){
									//add node that not exist in my table
//									System.out.println(neighborPresentNode.getNode()+" "+neighborPresentNode.getDestinationNode());
									int cost = neighborPresentNode.getCost()+matrixCost[matrixIndex.get(nodeName)][matrixIndex.get(neighborPresentNode.getNode())];
									NodeDetail newNode = new NodeDetail(nodeName, neighborPresentNode.getDestinationNode(),neighborPresentNode.getNode(), cost);	
									for (int m = 0; m < tmpNodeArray.size(); m++) {
										if(tmpNodeArray.get(m).getDestinationNode().equals(newNode.getDestinationNode())&&tmpNodeArray.get(m).getOutGoingNode().equals(newNode.getOutGoingNode())){
											//new node has the same destination and out going
											if(tmpNodeArray.get(m).getCost() < cost){
												tmpNodeArray.remove(m);
												isExist = false;
											}else{
												isExist = true;
											}
											break;
										}
									}
									if(!isExist){
										tmpNodeArray.add(newNode);
										tmpRoutingTable.put(nodeName, tmpNodeArray);
										tmpMatrixCost[matrixIndex.get(nodeName)][matrixIndex.get(newNode.getDestinationNode())] = cost;
										allUpToDate = false;
									}
								}
							}
						}
					}
				}
			}
			routingTable.putAll(tmpRoutingTable);
			for (int i = 0; i < matrixCost.length; i++) {
				for (int j = 0; j < matrixCost[i].length; j++) {
					matrixCost[i][j] = tmpMatrixCost[i][j];
				}
			}
			printMatrixCost();
			printRoutingTable("all");
//			printRoutingTable(nodesString);
			if (allUpToDate) {
				System.out.println("Stability already achieved at round " + it);
				countAchive=0;
				return;
			}else if(tmpCountAchive==countAchive&&countAchive!=0){
				System.out.println(">> Stability already achieved at round " + (it));
				countAchive=0;
				return;
			}else{
				countAchive=tmpCountAchive;
				System.out.println(countAchive+" "+tmpCountAchive);
			}
		}

	}

	private static void readFile(String path) {
		String line;
		try {
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			isFalsePath = false;
			int countLine = 0;
			int tmpIndex = 0;
			while ((line = br.readLine()) != null) {
				if (countLine == 0) {
					nodesCount = Integer.parseInt(line.split(":")[1].trim());
					matrixCost = new int[nodesCount][nodesCount];
					for (int i = 0; i < matrixCost.length; i++) {
						for (int j = 0; j < matrixCost[i].length; j++) {
							matrixCost[i][j] = 16;
						}
					}
				} else if (countLine > 1){
					String detail[] = line.split(" ");
					String node = detail[2].split("-")[0];
					String outGoingNode = detail[2].split("-")[1];
					String destinationNode = detail[0];
					int cost = Integer.parseInt(detail[1]);
					NodeDetail nodeDetail = new NodeDetail(node, destinationNode, outGoingNode, cost);
					if (matrixIndex.get(node) == null) {
						matrixIndexInverse.put(tmpIndex, node);
						matrixIndex.put(node, tmpIndex++);
					}
					if (matrixIndex.get(outGoingNode) == null) {
						matrixIndexInverse.put(tmpIndex, outGoingNode);
						matrixIndex.put(outGoingNode, tmpIndex++);
					}
					ArrayList<String> nb = new ArrayList<>();
					if (neighborTable.get(node) == null) {
						if (!node.equals(outGoingNode)) {
							nb.add(outGoingNode);
							neighborTable.put(node, nb);
						}
					} else {
						nb.addAll(neighborTable.get(node));
						nb.add(outGoingNode);
						neighborTable.put(node, nb);
					}
					ArrayList<NodeDetail> routingTableDetail = new ArrayList<NodeDetail>();
					if (routingTable.get(node) == null) {
						routingTableDetail.add(nodeDetail);
						routingTable.put(node, routingTableDetail);
					} else {
						routingTableDetail.addAll(routingTable.get(node));
						routingTableDetail.add(nodeDetail);
						routingTable.put(node, routingTableDetail);
					}
					matrixCost[matrixIndex.get(node)][matrixIndex.get(outGoingNode)] = cost;
				}
				countLine++;
			}

		} catch (FileNotFoundException e) {
			System.out.println("No path found...");
			isFalsePath = true;
		} catch (IOException e) {
			System.out.println("Text file does not in the correct format.");
			isFalsePath = true;
		}
		 printRoutingTable("all");

	}

}
