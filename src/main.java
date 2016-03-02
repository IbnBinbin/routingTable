
import java.io.*;
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
	private static ArrayList<NodeDetail> tmpChangeCostToFail = new ArrayList<>();
	private static ArrayList<NodeDetail> tmpChangeCost = new ArrayList<>();

	public static void main(String[] args) {
		resetRoutingTable(false);
		while (true) {
			System.out.println("Options:");
			System.out.println("*Compute routing tables  >> press 1");
			System.out.println("*Change cost             >> press 2");
			System.out.println("*View the best route     >> press 3");
			System.out.println("*Apply a split-horizon   >> press 4");
			System.out.println("*Reset the routing table >> press 5");
			System.out.println("*Show routing table      >> press 6");
			System.out.println("*Exit                    >> press 7");
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
				updateLinkCost(source, dest, cost);
				break;
			case 3:
				System.out.println("Type source and destination node in this form >> Nsource,Ndestination: ");
				input = new Scanner(System.in);
				line = input.nextLine();
				if (line.split(",").length < 2) {
					System.out.println("Wrong input format.");
					break;
				}
				source = line.split(",")[0].trim();
				dest = line.split(",")[1].trim();

				shortestPartCheck.clear();
				shortestPartCheck.put(source, 0);
				shortestPath.clear();
				shortestPath.add(source);
				findBestRoute(source, dest);
				break;
			case 4:
				applySplitHorizon();
				break;
			case 5:
				resetRoutingTable(false);
				break;
			case 6:
				printRoutingTable();
				break;
			case 7:
				System.out.println("Bye Bye!");
				return;
			default:
				System.out.println("Please choose the option again!");
				break;
			}
		}
	}

	private static void printRoutingTable() {
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
		if (!fromChangeCost) {
			tmpChangeCost = new ArrayList<>();
			tmpChangeCostToFail = new ArrayList<>();
		}
		while (isFalsePath) {
			// System.out.println("Path to the text file (full path): ");
			// Scanner input = new Scanner(System.in);
			// String pathToTextFile = input.next();
			// ReadFile(pathToTextFile);
			ReadFile("D:/workspace/ANC4_Routing/src/initial.txt");
			// ReadFile("D:/workspace/ANC4_Routing/src/test.txt");
		}
		// printMatrixCost();
	}

	private static void applySplitHorizon() {
		// TODO Auto-generated method stub

	}

	private static void findBestRoute(String source, String dest) {
		ArrayList<NodeDetail> ndList = routingTable.get(source);
		boolean check = false;
		if (ndList == null) {
			System.out.println("No route from " + source + " to " + dest);
			return;
		}
		for (int i = 0; i < ndList.size(); i++) {
			if (ndList.get(i).getDestinationNode().equals(dest)) {
				shortestPath.add(ndList.get(i).getOutGoingNode());
				if (ndList.get(i).getOutGoingNode().equals(dest)) {
					int cost = matrixCost[matrixIndex.get(shortestPath.get(0))][matrixIndex
							.get(shortestPath.get(shortestPath.size() - 1))];
					if (cost >= 16) {
						System.out.println("This route; " + source + " to " + dest + " is unreachable");
					} else {
						System.out.println(shortestPath.toString() + " cost: " + cost);
						check = true;
					}
					return;
				} else {
					findBestRoute(ndList.get(i).getOutGoingNode(), dest);

					if (shortestPartCheck.get(ndList.get(i).getOutGoingNode()) == null) {
						shortestPartCheck.put(ndList.get(i).getOutGoingNode(), 0);
					} else {
						System.out.println("Routing loop occur!! >> " + shortestPath.toString());
					}

				}

			} else {
				if (ndList.size() - 1 == i) {
					System.out.println("This route...; " + source + " to " + dest + " is unreachable");
				}
			}
		}

	}

	private static void updateLinkCost(String source, String dest, int cost) {
		ArrayList<NodeDetail> ndListS = routingTable.get(source);
		ArrayList<NodeDetail> ndListD = routingTable.get(dest);
		boolean checkIsDirectNeighbor = false;
		if (ndListS == null && ndListD == null) {
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
				matrixCost[matrixIndex.get(source)][matrixIndex.get(dest)] = cost;
				break;
			} else if (i == ndListS.size() - 1) {
				System.out.println("This link " + source + "-" + dest + " not found");
			}
		}
		
		for (int i = 0; i < ndListD.size(); i++) {
			if (ndListD.get(i).getDestinationNode().equals(source)) {
				ndListD.get(i).setCost(cost);
				matrixCost[matrixIndex.get(dest)][matrixIndex.get(source)] = cost;
				break;
			} else if (i == ndListS.size() - 1) {
				System.out.println("This link " + source + "-" + dest + " not found");
			}
		}

		

		// boolean check1 = false, check2 = false;
		// resetRoutingTable(true);
		// for (int i = 0; i < matrixIndexInverse.size(); i++) {
		// ArrayList<NodeDetail> nd =
		// routingTable.get(matrixIndexInverse.get(i));
		// for (int j = 0; j < nd.size(); j++) {
		// for (int k = 0; k < tmpChangeCost.size(); k++) {
		// if (cost >= 16) {
		// if (nd.get(j).getNode().equals(tmpChangeCost.get(k).getNode())
		// &&
		// nd.get(j).getOutGoingNode().equals(tmpChangeCost.get(k).getDestinationNode()))
		// {
		// if(!(source.equals(nd.get(j).getNode().equals(tmpChangeCost.get(k).getNode()))&&dest.equals(nd.get(j).getOutGoingNode().equals(tmpChangeCost.get(k).getDestinationNode())))){
		// nd.remove(j);
		//
		// matrixCost[matrixIndex.get(tmpChangeCost.get(k).getDestinationNode())][matrixIndex
		// .get(tmpChangeCost.get(k).getNode())] = cost;
		// }
		//
		// } else if
		// (nd.get(j).getNode().equals(tmpChangeCost.get(k).getDestinationNode())
		// &&
		// nd.get(j).getOutGoingNode().equals(tmpChangeCost.get(k).getNode())) {
		// if(!(dest.equals(nd.get(j).getNode().equals(tmpChangeCost.get(k).getNode()))&&source.equals(nd.get(j).getOutGoingNode().equals(tmpChangeCost.get(k).getDestinationNode())))){
		// nd.remove(j);
		//
		// matrixCost[matrixIndex.get(tmpChangeCost.get(k).getNode())][matrixIndex
		// .get(tmpChangeCost.get(k).getDestinationNode())] = cost;
		// }
		// }
		// } else {
		// if
		// (nd.get(j).getNode().equals(tmpChangeCost.get(k).getDestinationNode())
		// &&
		// nd.get(j).getOutGoingNode().equals(tmpChangeCost.get(k).getNode())) {
		// nd.get(i).setOutgoingNode(tmpChangeCost.get(k).getNode());
		// nd.get(i).setCost(tmpChangeCost.get(k).getCost());
		// matrixCost[matrixIndex.get(tmpChangeCost.get(k).getNode())][matrixIndex
		// .get(tmpChangeCost.get(k).getDestinationNode())] = cost;
		// } else if (nd.get(j).getNode().equals(tmpChangeCost.get(k).getNode())
		// &&
		// nd.get(j).getOutGoingNode().equals(tmpChangeCost.get(k).getDestinationNode()))
		// {
		// matrixCost[matrixIndex.get(tmpChangeCost.get(k).getDestinationNode())][matrixIndex
		// .get(tmpChangeCost.get(k).getNode())] = cost;
		// nd.get(i).setOutgoingNode(tmpChangeCost.get(k).getDestinationNode());
		// nd.get(i).setCost(tmpChangeCost.get(k).getCost());
		// }
		// }
		// }
		// }
		// }
		// if (cost >= 16) { // link fail
		// System.out.println(".................................." +
		// tmpChangeCostToFail.size());
		// for (int i = 0; i < matrixIndexInverse.size(); i++) {
		// ArrayList<NodeDetail> nd =
		// routingTable.get(matrixIndexInverse.get(i));
		// for (int j = 0; j < nd.size(); j++) {
		//
		// if (nd.get(j).getNode().equals(source) &&
		// nd.get(j).getOutGoingNode().equals(dest)) {
		// nd.remove(j);
		// check1 = true;
		// System.out.println(">>>>>>>>>>>>>>>check1");
		//
		// } else if (nd.get(j).getNode().equals(dest) &&
		// nd.get(j).getOutGoingNode().equals(source)) {
		// nd.remove(j);
		// check2 = true;
		// System.out.println(">>>>>>>>>>>>>>>check2");
		// }
		// // else {
		// // for (int k = 0; k < tmpChangeCostToFail.size(); k++) {
		// // if
		// //
		// (nd.get(j).getNode().equals(tmpChangeCostToFail.get(k).getDestinationNode())
		// // &&
		// //
		// nd.get(j).getOutGoingNode().equals(tmpChangeCostToFail.get(k).getNode()))
		// // {
		// // nd.remove(j);
		// //
		// matrixCost[matrixIndex.get(tmpChangeCostToFail.get(k).getNode())][matrixIndex
		// // .get(tmpChangeCostToFail.get(k).getDestinationNode())] =
		// // cost;
		// // check3 = true;
		// // System.out.println(">>>>>>>>>>>>>>>check3");
		// // break;
		// //
		// // } else if
		// // (nd.get(j).getNode().equals(tmpChangeCostToFail.get(k).getNode())
		// // && nd.get(j)
		// //
		// .getOutGoingNode().equals(tmpChangeCostToFail.get(k).getDestinationNode()))
		// // {
		// // nd.remove(j);
		// //
		// matrixCost[matrixIndex.get(tmpChangeCostToFail.get(k).getDestinationNode())][matrixIndex
		// // .get(tmpChangeCostToFail.get(k).getNode())] = cost;
		// // check4 = true;
		// // System.out.println(">>>>>>>>>>>>>>>check4");
		// // break;
		// // }
		// //
		// // }
		// // }
		//
		// // if (tmpChangeCostToFail.size() == 0) {
		// // check3 = true;
		// // check4 = true;
		// // }
		//
		// if (check1 && check2) {
		// matrixCost[matrixIndex.get(dest)][matrixIndex.get(source)] = cost;
		// matrixCost[matrixIndex.get(source)][matrixIndex.get(dest)] = cost;
		// tmpChangeCost.add(new NodeDetail(source, dest, dest, cost));
		// printMatrixCost();
		// printRoutingTable();
		// return;
		// }
		//
		// }
		// }
		// } else {
		// for (int i = 0; i < matrixIndexInverse.size(); i++) {
		// ArrayList<NodeDetail> nd =
		// routingTable.get(matrixIndexInverse.get(i));
		// for (int j = 0; j < nd.size(); j++) {
		//
		// if (nd.get(j).getNode().equals(source) &&
		// nd.get(j).getOutGoingNode().equals(dest)) {
		// nd.get(j).setCost(cost);
		// check1 = true;
		// } else if (nd.get(j).getNode().equals(dest) &&
		// nd.get(j).getOutGoingNode().equals(source)) {
		// nd.get(j).setCost(cost);
		// check2 = true;
		// }
		//
		//
		// if (check1 && check2) {
		// matrixCost[matrixIndex.get(dest)][matrixIndex.get(source)] = cost;
		// matrixCost[matrixIndex.get(source)][matrixIndex.get(dest)] = cost;
		// tmpChangeCost.add(new NodeDetail(source, dest, dest, cost));
		// printMatrixCost();
		// printRoutingTable();
		// return;
		// }
		//
		// }
		// }
		//
		// }

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
		ArrayList<String> myDestList = new ArrayList<>();
		ArrayList<String> neighborDestList = new ArrayList<>();
		Hashtable<String, ArrayList<NodeDetail>> tmpRoutingTable = new Hashtable<>();
		tmpRoutingTable.putAll(routingTable);
		int[][] tmpMatrixCost = new int[matrixCost.length][matrixCost[0].length];
		for (int i = 0; i < tmpMatrixCost.length; i++) {
			for (int j = 0; j < tmpMatrixCost[i].length; j++) {
				tmpMatrixCost[i][j] = matrixCost[i][j];
			}
		}

		for (int it = 0; it < iterations; it++) {
			System.out.println("Updating routing table round: " + (it + 1));
			boolean checkUpToDate = true;
			for (int i = 0; i < matrixIndexInverse.size(); i++) {
				String node = matrixIndexInverse.get(i);
				System.out.println("node: " + node);
				ArrayList<String> nbs = neighborTable.get(node);
				// System.out.println(nbs.toString());
				ArrayList<NodeDetail> myNode = routingTable.get(node);
				myDestList.clear();
				neighborDestList.clear();
				for (int j = 0; j < nbs.size(); j++) {
					ArrayList<NodeDetail> neighBorNode = routingTable.get(nbs.get(j));

					for (int a = 0; a < myNode.size(); a++) {
						// System.out.println("myDestList: "+
						// myNode.get(a).getDestinationNode());
						myDestList.add(myNode.get(a).getDestinationNode());
					}
					for (int a = 0; a < neighBorNode.size(); a++) {
						// System.out.println("myNeighborList: "+
						// neighBorNode.get(a).getDestinationNode());
						neighborDestList.add(neighBorNode.get(a).getDestinationNode());
					}
					neighborDestList.removeAll(myDestList);

					for (int l = 0; l < myNode.size(); l++) {
						String myDestNode = myNode.get(l).getDestinationNode();
						for (int k = 0; k < neighBorNode.size(); k++) {
							String neighborDestNode = neighBorNode.get(k).getDestinationNode();
							
							if (neighborDestNode.equals(myDestNode)) {
								System.out.println("[" + matrixIndex.get(node) + "]["
										+ matrixIndex.get(neighBorNode.get(k).getDestinationNode()) + "]");
								
								int cost = matrixCost[matrixIndex.get(node)][matrixIndex
										.get(neighBorNode.get(k).getNode())];
								// if (neighBorNode.get(k).getCost() >= 16||
								// myNode.get(l).getCost() >= 16) {
								// break;
								// }
								if (neighBorNode.get(k).getCost() + cost < myNode.get(l).getCost()
										|| myNode.get(l).getOutGoingNode().equals(neighBorNode.get(k).getNode())) {

									if (myNode.get(l).getCost() == (neighBorNode.get(k).getCost() + cost)) {
										checkUpToDate = checkUpToDate & true;

									} else {
										checkUpToDate = false;
									}

									 myNode.get(l).setCost(neighBorNode.get(k).getCost()
									 + cost);
									 myNode.get(l).setOutgoingNode(neighBorNode.get(k).getNode());
									ArrayList<NodeDetail> routingTableDetail = new ArrayList<NodeDetail>();
									ArrayList<NodeDetail> tmpMyNode = tmpRoutingTable.get(node);
									//
									for (int m = 0; m < tmpMyNode.size(); m++) {
										if (tmpMyNode.get(m).getDestinationNode()
												.equals(myNode.get(l).getDestinationNode())) {
											tmpMyNode.get(m).setCost(neighBorNode.get(k).getCost() + cost);
											tmpMyNode.get(m).setOutgoingNode(neighBorNode.get(k).getNode());
											
										}
										
									}
									
									routingTableDetail.addAll(tmpMyNode);
									tmpRoutingTable.put(node, routingTableDetail);
									tmpMatrixCost[matrixIndex.get(node)][matrixIndex.get(
											neighBorNode.get(k).getDestinationNode())] = neighBorNode.get(k).getCost()
													+ cost;
									System.out.println("cost [" + matrixIndex.get(node) + "]["
											+ matrixIndex.get(neighBorNode.get(k).getDestinationNode()) + "]: "
											+ neighBorNode.get(k).getCost() + cost);

								} else if (checkUpToDate && i == matrixIndexInverse.size() - 1) {
									System.out.println("Stability already achieved at round " + it);
									return;
								}
							}
							else {
//								System.out.println("...");
								for (int b = 0; b < neighborDestList.size(); b++) {

									if (neighborDestList.get(b).equals(neighborDestNode)) {
										int cost = matrixCost[matrixIndex.get(node)][matrixIndex
												.get(neighBorNode.get(k).getNode())];

										// if (neighBorNode.get(k).getCost() >=
										// 16|| myNode.get(l).getCost() >= 16) {
										// break;
										// }
										NodeDetail nodeDetail = new NodeDetail(node,
												neighBorNode.get(k).getDestinationNode(), neighBorNode.get(k).getNode(),
												(neighBorNode.get(k).getCost() + cost));
										ArrayList<NodeDetail> routingTableDetail = new ArrayList<NodeDetail>();
										routingTableDetail.addAll(routingTable.get(node));
										routingTableDetail.add(nodeDetail);
										tmpRoutingTable.put(node, routingTableDetail);
										tmpMatrixCost[matrixIndex.get(node)][matrixIndex
												.get(neighBorNode.get(k).getDestinationNode())] = neighBorNode.get(k)
														.getCost() + cost;
										System.out.println(
												routingTableDetail.size() + " cost..[" + matrixIndex.get(node) + "]["
														+ matrixIndex.get(neighBorNode.get(k).getDestinationNode())
														+ "]: " + (neighBorNode.get(k).getCost() + cost));
										checkUpToDate = false;
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
			printRoutingTable();
		}

	}

	private static void ReadFile(String path) {
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
						if (!node.equals(outGoingNode)) {
							nb.addAll(neighborTable.get(node));
							nb.add(outGoingNode);
							neighborTable.put(node, nb);
						}
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
