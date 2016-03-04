
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
	private static Hashtable<String, Integer> shortestPartCheck = new Hashtable<>();
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
			System.out.println("*Compute routing tables        >> press 1");
			System.out.println("*Change cost                   >> press 2");
			System.out.println("*View the best route           >> press 3");
			System.out.println("*Apply/unapply a split-horizon >> press 4");
			System.out.println("*Show routing table            >> press 5");
			System.out.println("*Reset the routing table       >> press 6");
			System.out.println("*Exit                          >> press 7");
			System.out.println("-------------------------------------------");
			if (!isApplySplitHorizon) {
				System.out.println("*******Split horizon is not applied*******");
			} else {
				System.out.println("*******Split horizon is applied*******");
			}
			System.out.println();
			System.out.println("firstUpdate: "+firstUpdate);
			Scanner input = new Scanner(System.in);

			printMatrixCost();
			// printRoutingTable();

			int option = 0;
			if (input.hasNextInt()) {
				option = input.nextInt();
			} else {
				System.out.println("Wrong option number. Please try again.");
			}

			switch (option) {
			case 1:
				System.out.println("Number of iterations for updating: ");
				input = new Scanner(System.in);
				int interations = input.nextInt();
				System.out.println();
				updateRoutingTable(interations);
				// printRoutingTable();
				// printMatrixCost();

				break;
			case 2:
				System.out.println("Add or change the link, type in this form >> N1,N2,Cost: ");
				input = new Scanner(System.in);
				String line = input.nextLine();
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
				costShortTestPath = 0;
				countAchive=0;
				checkNotFail = true;
				isFinish = false;
				System.out.println("Type source and destination node in this form >> Nsource,Ndest: ");
				input = new Scanner(System.in);
				line = input.nextLine();
				if (line.split(",").length < 2) {
					System.out.println("Wrong input format.");
					break;
				}
				source = line.split(",")[0].trim();
				dest = line.split(",")[1].trim();
				startRoute = source;
				shortestPartCheck.clear();
				shortestPartCheck.put(source, 0);
				shortestPath.clear();
				shortestPath.add(source);
				System.out.println();
				firstFindBestRoute = true;
				findBestRoute(source, dest, 0, source);
				break;
			case 4:
				isApplySplitHorizon = !isApplySplitHorizon;

				break;
			case 5:
				System.out.println();
				printRoutingTable(routingTable);
				break;
			case 6:
				System.out.println();
				resetRoutingTable(false);
				firstUpdate=true;
				break;
			case 7:
				System.out.println();
				System.out.println("Bye Bye!");
				return;
			default:
				System.out.println();
				System.out.println("Please choose the option again!");
				break;
			}
		}
	}

	private static void printRoutingTable(Hashtable<String, ArrayList<NodeDetail>> routingTable) {
		for (int i = 0; i < matrixIndexInverse.size(); i++) {
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

	private static void resetRoutingTable(boolean fromChangeCost) {
		isFalsePath = true;
		nodesCount = 0;
		matrixIndex = new Hashtable<>();
		matrixIndexInverse = new Hashtable<>();
		routingTable = new Hashtable<>();
		neighborTable = new Hashtable<>();
		
		while (isFalsePath) {
			// System.out.println("Path to the text file (full path): ");
			// Scanner input = new Scanner(System.in);
			// String pathToTextFile = input.next();
			// readFile(pathToTextFile);
//			readFile("D:/workspace/ANC4_Routing/src/initial.txt");
			 readFile("D:/workspace/ANC4_Routing/src/test2.txt");
//			 readFile("D:/workspace/ANC4_Routing/src/test3.txt");
			// readFile("D:/workspace/ANC4_Routing/src/test.txt");
		}
		// printMatrixCost();
	}

	private static void findBestRoute(String source, String dest, int checkCost, String path) {
		ArrayList<NodeDetail> ndListS = routingTable.get(source);
		ArrayList<NodeDetail> ndListD = routingTable.get(dest);
		if (ndListS == null && ndListD == null) {
			System.out.println("This path " + source + "-" + dest + " not found");
			return;
		}
		ArrayList<NodeDetail> ndList = routingTable.get(source);
//		System.out.println("from " + startRoute + " to " + dest);

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
					
					costShortTestPath = matrixCost[matrixIndex.get(shortestPath.get(0))][matrixIndex
							.get(shortestPath.get(shortestPath.size() - 1))];

					if (ndList.get(i).getCost() >= 16 && !isFinish) {
						System.out.println("Link between "+ndList.get(i).getNode()+", "+ndList.get(i).getOutGoingNode()+" is fail.\nThis route " + startRoute + " to " + dest + " is unreachable.");
						return;
					}
//					System.out.println(checkCost+" != "+costShortTestPath);
					if(checkCost!=costShortTestPath){
						System.out.println(checkCost+" != "+costShortTestPath);
						return;
					}
					if (checkNotFail&&!isFinish) {
						System.out.println("The shortest path is ["+path + "]. cost: " + costShortTestPath);
//						isFinish = true;
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
			}
		}

		for (int i = 0; i < ndListD.size(); i++) {
			if (ndListD.get(i).getDestinationNode().equals(source)) {
				ndListD.get(i).setCost(cost);
				ndListD.get(i).setOutgoingNode(source);
				matrixCost[matrixIndex.get(dest)][matrixIndex.get(source)] = cost;
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

	private static void updateRoutingTable(int iterations) {
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
			// tmpRoutingTable.putAll(routingTable);
			int tmpCountAchive=0;
			int[][] tmpMatrixCost = new int[nodesCount][nodesCount];
			for (int i = 0; i < nodesCount; i++) {
				for (int j = 0; j < nodesCount; j++) {
					tmpMatrixCost[i][j] = matrixCost[i][j];
				}
			}
		
		
			
			Hashtable<String, ArrayList<NodeDetail>> tmpRoutingTable = new Hashtable<>();
			System.out.println("checkIsMoreThanOneInterface: "+checkIsMoreThanOneInterface);
			System.out.println("Updating routing table round: " + (it + 1));
			boolean allUpToDate = true;
			for (int i = 0; i < nodesCount; i++) { // all node
				String nodeName = matrixIndexInverse.get(i);
				ArrayList<String> neighborList = new ArrayList<>();
				neighborList.addAll(neighborTable.get(nodeName));
				ArrayList<String> my_NodesInTable = new ArrayList<>();
				ArrayList<String> neighbor_NodesInTable = new ArrayList<>();
				ArrayList<NodeDetail> tmpNodeDetailList = new ArrayList<>();
				tmpNodeDetailList.addAll(routingTable.get(nodeName));
				ArrayList<NodeDetail> tmpNodeDetailList2 = new ArrayList<>();
				tmpNodeDetailList2.addAll(routingTable.get(nodeName));
				Hashtable<String, Integer> checkMin = new Hashtable<>();
				Hashtable<String, NodeDetail> nodeDetailExist = new Hashtable<>();
				Hashtable<Integer, String> nodeDetailExistInvert = new Hashtable<>();
				for (int j = 0; j < neighborList.size(); j++) { // all neighbor
																// of this node

					ArrayList<NodeDetail> myRoutingTable = new ArrayList<>();
					myRoutingTable.addAll(routingTable.get(nodeName));
					ArrayList<NodeDetail> neighborRoutingTable = new ArrayList<>();
					neighborRoutingTable.addAll(routingTable.get(neighborList.get(j)));
					
					for (int k = 0; k < myRoutingTable.size(); k++) {
						my_NodesInTable.add(myRoutingTable.get(k).getDestinationNode());
						nodeDetailExist.put(myRoutingTable.get(k).getDestinationNode()+myRoutingTable.get(k).getOutGoingNode(),myRoutingTable.get(k));
						nodeDetailExistInvert.put(nodeDetailExist.size()-1, myRoutingTable.get(k).getDestinationNode()+myRoutingTable.get(k).getOutGoingNode());
					}
					for (int k = 0; k < neighborRoutingTable.size(); k++) {
						neighbor_NodesInTable.add(neighborRoutingTable.get(k).getDestinationNode());
					}
					
					
					for (int k = 0; k < myRoutingTable.size(); k++) {

						NodeDetail myNodeDetail = myRoutingTable.get(k);
						for (int k2 = 0; k2 < neighborRoutingTable.size(); k2++) {
							
							// ArrayList<String> checkNodeThatNotExist = new
							// ArrayList<>();
							NodeDetail neighborNodeDetail = neighborRoutingTable.get(k2);
							
							if(myNodeDetail.getNode().equals(neighborNodeDetail.getNode())){
								break;
							}else if(myNodeDetail.getCost()>=16&&neighborNodeDetail.getNode().equals(myNodeDetail.getDestinationNode())){
								break;
							}
							if (myNodeDetail.getDestinationNode().equals(neighborNodeDetail.getDestinationNode())) {
								// System.out.println(myNodeDetail.getNode()+"
								// "+neighborNodeDetail.getNode());
								// if(myNodeDetail.getNode().equals(neighborNodeDetail.getOutGoingNode()))
								// {
								// same destination and my node equal neighbor
								// outgoing node
								int cost = matrixCost[matrixIndex.get(nodeName)][matrixIndex
										.get(neighborNodeDetail.getNode())];
								// System.out.println(myNodeDetail.getCost()+"....."+neighborNodeDetail.getCost()+"
								// "+ cost);
								if (neighborNodeDetail.getCost() != 0
										&& ((myNodeDetail.getCost() > neighborNodeDetail.getCost() + cost)
												|| (myNodeDetail.getOutGoingNode().equals(neighborNodeDetail.getNode()))
														&& !isApplySplitHorizon)) {
									int tmpCost = 0;

									if (myNodeDetail.getCost() > neighborNodeDetail.getCost() + cost
											&& neighborNodeDetail.getCost() < 16) {
										tmpCost = Math.min(myNodeDetail.getCost(), neighborNodeDetail.getCost() + cost);
										System.out
												.println(
														tmpCost + ">>" + nodeName + "-" + neighborNodeDetail.getNode()
																+ "=>" + myNodeDetail.getDestinationNode() + ": "
																+ neighborNodeDetail.getCost()
																+ "________[" + matrixIndex
																		.get(nodeName)
																+ "][" + matrixIndex.get(
																		neighborNodeDetail.getDestinationNode())
																+ "]");
									} else if (neighborNodeDetail.getCost() >= 16) {
										tmpCost = Math.min(myNodeDetail.getCost(), neighborNodeDetail.getCost() + cost);
										System.out
												.println(
														tmpCost + ">>" + nodeName + "-" + neighborNodeDetail.getNode()
																+ "=>" + myNodeDetail.getDestinationNode() + ": "
																+ neighborNodeDetail.getCost()
																+ ".......[" + matrixIndex
																		.get(nodeName)
																+ "][" + matrixIndex.get(
																		neighborNodeDetail.getDestinationNode())
																+ "]");
									} else if ((neighborNodeDetail.getCost() + cost) >= 16) {
										tmpCost = 16;
										System.out
												.println(
														tmpCost + ">>" + nodeName + "-" + neighborNodeDetail.getNode()
																+ "=>" + myNodeDetail.getDestinationNode() + ": "
																+ neighborNodeDetail.getCost()
																+ "****[" + matrixIndex
																		.get(nodeName)
																+ "][" + matrixIndex.get(
																		neighborNodeDetail.getDestinationNode())
																+ "]");
									} else {
										tmpCost = neighborNodeDetail.getCost() + cost;
										System.out
												.println(
														tmpCost + ">>" + nodeName + "-" + neighborNodeDetail.getNode()
																+ "=>" + myNodeDetail.getDestinationNode() + ": "
																+ neighborNodeDetail.getCost()
																+ "+++++++[" + matrixIndex
																		.get(nodeName)
																+ "][" + matrixIndex.get(
																		neighborNodeDetail.getDestinationNode())
																+ "]");
									}
//									System.out.println(">>>>>>>>>>>>>>>>>");
									boolean checkSameOutgoing=false;
									boolean isAlreadyExist=false;
									NodeDetail removeNodeDetail = null;
									if (nodeDetailExist != null) {
										
										if (nodeDetailExist.get(neighborNodeDetail.getDestinationNode()+neighborNodeDetail.getOutGoingNode()) != null) {
											System.out.println(neighborNodeDetail.getDestinationNode()+neighborNodeDetail.getOutGoingNode()+"**********"+nodeDetailExist.get(""+neighborNodeDetail.getDestinationNode()+neighborNodeDetail.getOutGoingNode()).getCost()+"   "+tmpCost);
											System.out.println(nodeDetailExist.get(neighborNodeDetail.getDestinationNode()+neighborNodeDetail.getOutGoingNode()).getCost()+" <= "+ tmpCost);
											System.out.println(nodeDetailExist.get(neighborNodeDetail.getDestinationNode()+neighborNodeDetail.getOutGoingNode()).getOutGoingNode()+" "+(neighborNodeDetail.getNode()));
											if (nodeDetailExist.get(neighborNodeDetail.getDestinationNode()+neighborNodeDetail.getOutGoingNode()).getDestinationNode().equals(neighborNodeDetail.getDestinationNode())&&nodeDetailExist.get(neighborNodeDetail.getDestinationNode()+neighborNodeDetail.getOutGoingNode()).getCost() <= tmpCost) {
												System.out.println("breakkkkkk!");
												isAlreadyExist = true;
											}
//											else{
//												removeNodeDetail = new NodeDetail(nodeName,
//														neighborNodeDetail.getDestinationNode(), neighborNodeDetail.getNode(), nodeDetailExist.get(neighborNodeDetail.getDestinationNode()+neighborNodeDetail.getOutGoingNode()).getCost());
//											}
										}
									}
									nodeDetailExist.put(neighborNodeDetail.getDestinationNode()+neighborNodeDetail.getOutGoingNode(), new NodeDetail(nodeName,
											neighborNodeDetail.getDestinationNode(), neighborNodeDetail.getNode(),
											tmpCost));
			
									nodeDetailExistInvert.put(nodeDetailExist.size()-1, neighborNodeDetail.getDestinationNode()+neighborNodeDetail.getOutGoingNode());
									if (checkMin != null) {

										if (checkMin.get(neighborNodeDetail.getDestinationNode()+neighborNodeDetail.getOutGoingNode()) != null) {

											if (checkMin.get(neighborNodeDetail.getDestinationNode()+neighborNodeDetail.getOutGoingNode()) <= tmpCost) {

												break;
											}
										}
									}
									checkMin.put(neighborNodeDetail.getDestinationNode()+neighborNodeDetail.getOutGoingNode(),tmpCost);
									System.out.println(tmpNodeDetailList.size()+".............,,,,,,,......." + nodeDetailExist.size()+" "+nodeDetailExistInvert.size());
									
									if(!checkIsMoreThanOneInterface && !isAlreadyExist){
										NodeDetail tmpMyNodeDetail = new NodeDetail(myNodeDetail.getNode(),
												myNodeDetail.getDestinationNode(), neighborNodeDetail.getNode(), tmpCost);
										tmpNodeDetailList.add(k, tmpMyNodeDetail);
										System.out.println(tmpMyNodeDetail.getNode() + " "
												+ tmpMyNodeDetail.getDestinationNode() + " "
												+ tmpMyNodeDetail.getOutGoingNode() + " " + tmpMyNodeDetail.getCost());
										NodeDetail tmpMyNodeDetail1 = tmpNodeDetailList.remove(k + 1);
										System.out.println(tmpMyNodeDetail1.getNode() + " "
												+ tmpMyNodeDetail1.getDestinationNode() + " "
												+ tmpMyNodeDetail1.getOutGoingNode() + " " + tmpMyNodeDetail1.getCost());

										tmpRoutingTable.put(nodeName, tmpNodeDetailList);

										// myNodeDetail.setCost(tmpCost);
										// myNodeDetail.setOutgoingNode(neighborNodeDetail.getNode());
										// System.out.println(k2+" "+k+" "+j+" "+i);
										tmpMatrixCost[matrixIndex.get(nodeName)][matrixIndex
												.get(neighborNodeDetail.getDestinationNode())] = tmpCost;
										// System.out.println("blah blah");

										if (myNodeDetail.getCost() == tmpCost) {
											allUpToDate = allUpToDate & true;

										} else {
											allUpToDate = false;
										}
									}else if (!isAlreadyExist){
										boolean isremove=false;
										
										NodeDetail tmpMyNodeDetail = new NodeDetail(myNodeDetail.getNode(),
												myNodeDetail.getDestinationNode(), neighborNodeDetail.getNode(), tmpCost);
										for (int l = 0; l < tmpNodeDetailList.size(); l++) {
											
											if(tmpNodeDetailList.get(l).getDestinationNode().equals(neighborNodeDetail.getDestinationNode())&&(tmpNodeDetailList.get(l).getOutGoingNode().equals(neighborNodeDetail.getNode())||tmpNodeDetailList.get(l).getCost()>=16)&&(tmpNodeDetailList.get(l).getCost()>(neighborNodeDetail.getCost())||isApplySplitHorizon)){
												System.out.println(tmpNodeDetailList.get(l).getCost()+" > "+(neighborNodeDetail.getCost()));
												//for topology with one node has more than 1 interface
//											if(tmpNodeDetailList.get(l).getDestinationNode().equals(neighborNodeDetail.getDestinationNode())&&(tmpNodeDetailList.get(l).getCost()>(neighborNodeDetail.getCost())||isApplySplitHorizon)){
												//only one interface
												
												System.out.println("..."+tmpNodeDetailList.get(l).getNode() + " "
														+ tmpNodeDetailList.get(l).getDestinationNode() + " "
														+ tmpNodeDetailList.get(l).getOutGoingNode() + " " + tmpNodeDetailList.get(l).getCost());
												tmpNodeDetailList.remove(l);
												System.out.println("removeeeeeeeeeee");
												isremove=true;
												break;
											}
										}
										if(isremove){
											tmpNodeDetailList.add(tmpMyNodeDetail);
											System.out.println("..."+tmpMyNodeDetail.getNode() + " "
													+ tmpMyNodeDetail.getDestinationNode() + " "
													+ tmpMyNodeDetail.getOutGoingNode() + " " + tmpMyNodeDetail.getCost());
											tmpMatrixCost[matrixIndex.get(nodeName)][matrixIndex.get(neighborNodeDetail.getDestinationNode())] = tmpCost;
											
										}
									
										tmpRoutingTable.put(nodeName, tmpNodeDetailList);
	
										if (myNodeDetail.getCost() == tmpCost) {
											allUpToDate = allUpToDate & true;
											tmpCountAchive++;
										} else {
											allUpToDate = false;
										}
										//
									}
								}
							} else {
								//
								// add node that I don't have in my routing
								// table from neighbor
								// System.out.println("blah blah3");
								neighbor_NodesInTable.removeAll(my_NodesInTable);
								int cost = matrixCost[matrixIndex.get(neighborNodeDetail.getNode())][matrixIndex
										.get(neighborNodeDetail.getDestinationNode())];
								for (int l = 0; l < neighbor_NodesInTable.size(); l++) { // loop
																							// all
																							// node
																							// that
																							// I
																							// don't
																							// have
									
									if (k == 0 && neighbor_NodesInTable.get(l)
											.equals(neighborNodeDetail.getDestinationNode())) {
										
										// have
					
						// System.out.println(nodeName+" "+j+"
						// "+k+" "+k2+" "+l+" "+cost+"
						// "+neighborNodeDetail.getNode()+"
						// "+neighborNodeDetail.getDestinationNode());
						System.out.println(nodeDetailExist.size()+"........"+neighborNodeDetail.getDestinationNode()+neighborNodeDetail.getOutGoingNode());
						if (nodeDetailExist != null) {
							if (nodeDetailExist.get(neighborNodeDetail.getDestinationNode()+neighborNodeDetail.getOutGoingNode()) != null) {
								System.out.println(nodeDetailExist.get(""+neighborNodeDetail.getDestinationNode()+neighborNodeDetail.getOutGoingNode()).getCost()+"   "+neighborNodeDetail.getCost()+"+"+cost);
								if (nodeDetailExist.get(neighborNodeDetail.getDestinationNode()+neighborNodeDetail.getOutGoingNode()).getCost() <= neighborNodeDetail.getCost() + cost) {

									break;
								}
							}
						}
						
						nodeDetailExist.put(neighborNodeDetail.getDestinationNode()+neighborNodeDetail.getOutGoingNode(), new NodeDetail(nodeName,
								neighborNodeDetail.getDestinationNode(), neighborNodeDetail.getNode(),
								neighborNodeDetail.getCost() + cost));
						nodeDetailExistInvert.put(nodeDetailExist.size()-1, neighborNodeDetail.getDestinationNode()+neighborNodeDetail.getOutGoingNode());
//						ArrayList<NodeDetail> tmp = new ArrayList<>();
//						for (int m = 0; m < nodeDetailExist.size(); m++) {
////							tmpNodeDetailList.clear();
//							tmp.add(nodeDetailExist.get(nodeDetailExistInvert.get(m)));
//						}
//						tmpRoutingTable.put(nodeName, tmp);
						tmpNodeDetailList.add(new NodeDetail(nodeName,
								neighborNodeDetail.getDestinationNode(), neighborNodeDetail.getNode(),
								neighborNodeDetail.getCost() + cost));
						tmpRoutingTable.put(nodeName, tmpNodeDetailList);
						tmpMatrixCost[matrixIndex.get(nodeName)][matrixIndex.get(
								neighborNodeDetail.getDestinationNode())] = neighborNodeDetail.getCost()
										+ cost;
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
			printRoutingTable(routingTable);
			//
			// System.out.println("................"+it);
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
			int tmpIndexInverse = 0;
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				if (countLine == 0) {
					nodesCount = Integer.parseInt(line.split(":")[1].trim());
					matrixCost = new int[nodesCount][nodesCount];
					for (int i = 0; i < matrixCost.length; i++) {
						for (int j = 0; j < matrixCost[i].length; j++) {
							matrixCost[i][j] = 16;
						}
						System.out.println();
					}
				} else if (countLine == 1) {
					// do nothing
				} else {
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
						// if (!node.equals(outGoingNode)) {
						nb.addAll(neighborTable.get(node));
						nb.add(outGoingNode);
						neighborTable.put(node, nb);
						// }
					}

					// if(matrixIndexInverse.get(node)==null){
					// routingTableDetail.put(tmpIndexInverse++, nodeDetail);
					// }

					// TODO add routingTableDetail to routingTable
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
			// e.printStackTrace();
			System.out.println("No path found...");
			isFalsePath = true;
		} catch (IOException e) {
			System.out.println("Text file does not in the correct format.");
			isFalsePath = true;
		}
		// printRoutingTable();

	}

}
