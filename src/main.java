
import java.io.*;
import java.time.temporal.IsoFields;
import java.util.*;

public class main {
	private static boolean isFalsePath = true;
	private static int nodesCount = 0;
	private static int[][] matrixCost;
	private static Hashtable<String, Integer> matrixIndex = new Hashtable<>();
	private static Hashtable<Integer, String> matrixIndexInverse = new Hashtable<>();
	private static Hashtable<String, ArrayList<NodeDetail>> routingTable = new Hashtable<>();
	private static Hashtable<String, ArrayList<NodeDetail>> tmpRoutingTable = new Hashtable<>();
	private static Hashtable<String, ArrayList<String>> neighborTable = new Hashtable<>();

	public static void main(String[] args) {
		while (isFalsePath) {
			// System.out.println("Path to the text file (full path): ");
			// Scanner input = new Scanner(System.in);
			// String pathToTextFile = input.next();
			// ReadFile(pathToTextFile);
			ReadFile("D:/workspace/ANC4_Routing/src/initial.txt");
		}
		printMatrixCost();

		System.out.println(routingTable.size());

		System.out.println("Number of iterations for updating: ");
		Scanner input = new Scanner(System.in);
		int interations = input.nextInt();

		updateRoutingTable(interations);

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
		for(int it=0;it<iterations;it++){
			System.out.println("size matrix: "+matrixIndexInverse.size());
			for (int i = 0; i < matrixIndexInverse.size(); i++) {
				String node = matrixIndexInverse.get(i);
				System.out.println("node: "+node);
				ArrayList<String> nbs = neighborTable.get(node);
				ArrayList<NodeDetail> myNode = routingTable.get(node);
				myDestList.clear();
				neighborDestList.clear();
				for (int j = 0; j < nbs.size(); j++) {
					ArrayList<NodeDetail> neighBorNode = routingTable.get(nbs.get(j));
					for (int a = 0; a < myNode.size(); a++) {
						System.out.println("myDestList: "+ myNode.get(a).getDestinationNode());
						myDestList.add(myNode.get(a).getDestinationNode());
					}
					for (int a = 0; a < neighBorNode.size(); a++) {
						System.out.println("myNeighborList: "+ neighBorNode.get(a).getDestinationNode());
						neighborDestList.add(neighBorNode.get(a).getDestinationNode());
					}
					neighborDestList.removeAll(myDestList);
					System.out.println(neighborDestList.toString());
					for (int l = 0; l < myNode.size(); l++) {
						String myDestNode = myNode.get(l).getDestinationNode();
						for (int k = 0; k < neighBorNode.size(); k++) {
							String neighborDestNode = neighBorNode.get(k).getDestinationNode();
							
							if (neighborDestNode.equals(myDestNode)) {
								int cost = matrixCost[matrixIndex.get(node)][matrixIndex.get(neighBorNode.get(k).getNode())];
								if (neighBorNode.get(k).getCost() + cost < myNode.get(l).getCost() && !myNode.get(l).getDestinationNode().equals(myNode.get(l).getOutGoingNode())) {
									myNode.get(l).setCost(neighBorNode.get(k).getCost() + 1);
									ArrayList<NodeDetail> routingTableDetail = new ArrayList<NodeDetail>();
									routingTableDetail.addAll(myNode);
									tmpRoutingTable.put(node, routingTableDetail);
									matrixCost[matrixIndex.get(node)][matrixIndex
											.get(neighBorNode.get(k).getNode())] = neighBorNode.get(k).getCost() + cost;
									System.out.println("cost...: "+neighBorNode.get(k).getCost() + cost);
								}
							}else{
								for(int b=0;b<neighborDestList.size();b++){
									if(neighborDestList.get(b).equals(neighborDestNode)){
										int cost = matrixCost[matrixIndex.get(node)][matrixIndex.get(neighBorNode.get(k).getNode())];
										NodeDetail nodeDetail = new NodeDetail(node, neighBorNode.get(k).getDestinationNode(), neighBorNode.get(k).getNode(), neighBorNode.get(k).getCost()+cost);
										ArrayList<NodeDetail> routingTableDetail = new ArrayList<NodeDetail>();
										routingTableDetail.addAll(routingTable.get(node));
										routingTableDetail.add(nodeDetail);
										tmpRoutingTable.put(node, routingTableDetail);
										matrixCost[matrixIndex.get(node)][matrixIndex
																			.get(neighBorNode.get(k).getDestinationNode())] = neighBorNode.get(k).getCost() + cost;
//										System.out.println("cost: "+(neighBorNode.get(k).getCost() + cost));
									}
								}
							}
						}
					}
				}
			}
			routingTable.putAll(tmpRoutingTable);
			printMatrixCost();
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
						nb.add(outGoingNode);
						neighborTable.put(node, nb);
					} else {
						nb.addAll(neighborTable.get(node));
						nb.add(outGoingNode);
						neighborTable.put(node, nb);
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

	}

}
