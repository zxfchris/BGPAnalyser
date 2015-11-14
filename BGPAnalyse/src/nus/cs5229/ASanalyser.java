package nus.cs5229;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import nus.cs5229.model.AS;
import nus.cs5229.model.ASPath;
import nus.cs5229.model.Classify;
import nus.cs5229.model.Relationship;

public class ASanalyser {
	private static Map<Integer,AS> asMap = new HashMap<Integer,AS>();
	private static Map<Integer,AS> originAsMap = new HashMap<Integer,AS>(); 
	private static Map<String,ASPath> asPathMap = new HashMap<String,ASPath>();
	private static Map<Integer,AS> stubs = new HashMap<Integer,AS>();	//level 4
	private static Map<Integer,AS> regionISPs = new HashMap<Integer,AS>();	//level 3
	private static Map<Integer,AS> denseCores = new HashMap<Integer,AS>();	//level 0
	private static Map<Integer,AS> transitCores = new HashMap<Integer,AS>();	//level 1
	private static Map<Integer,AS> outerCores = new HashMap<Integer,AS>();	//level 2
	private static Scanner scanner;
	
	public static void main(String[] args) {
		ASanalyser analyser = new ASanalyser();
		analyser.preprocessDataset();
		analyser.constructASGraph();
		analyser.annotateASGraph();
		analyser.classifyASes();
		System.out.println("originalASMap size:"+originAsMap.size());
		System.out.println("asmap size:"+asMap.size());
		System.out.println("----------------------level 4-------------------------");
		analyser.asDegreeDistributionByLevel(stubs);
		System.out.println("----------------------level 3-------------------------");
		analyser.asDegreeDistributionByLevel(regionISPs);
		System.out.println("----------------------level 2-------------------------");
		analyser.asDegreeDistributionByLevel(outerCores);
		System.out.println("----------------------level 1-------------------------");
		analyser.asDegreeDistributionByLevel(transitCores);
		System.out.println("----------------------level 0-------------------------");
		analyser.asDegreeDistributionByLevel(denseCores);
		analyser.drawInterConnectTable();
		analyser.asSumary();
		
		scanner = new Scanner(System.in);
		while (true) {
			System.out.println("Input your test choice: (1: test AS pair relation; 2: test AS classification)");
			int choice = scanner.nextInt();
			if (choice == 1) {
				System.out.println("Testing AS pair ralation, input your AS pair:");
				Integer as1No = scanner.nextInt();
				Integer as2No = scanner.nextInt();
				AS as1 = originAsMap.get(as1No);
				if (null == as1) {
					System.out.println("AS " + as1No + "do not exist");
					continue;
				}
				if (originAsMap.get(as2No) == null) {
					System.out.println("AS " + as1No + "do not exist");
					continue;
				}
				System.out.println(as1No + "   "+as2No);
				if (as1.getEdge().get(as2No) == null) {
					System.out.println("relation do not exist");
					continue;
				}
				switch (as1.getEdge().get(as2No)) {
				case P2P:
					System.out.println(as1No + " " + as2No + " p2p");
					break;
				case C2P:
					System.out.println(as1No + " " + as2No + " c2p");
					break;
				case S2S:
					System.out.println(as1No + " " + as2No + " s2s");
					break;
				case P2C:
					System.out.println(as1No + " " + as2No + " p2c");
					break;
				case UNKOWN:
					System.out.println(as1No + " " + as2No + " unkown");
				default:
					break;
				}
				
			} else if (choice == 2) {
				System.out.println("Testing AS classification, input your AS number:");
				int asNo = scanner.nextInt();
				AS as = originAsMap.get(asNo);
				switch (as.getClassification()) {
				case STUB:
					System.out.println(as.getNumber() + " stub");
					break;
				case DENSECORE:
					System.out.println(as.getNumber() + " dense core");
					break;
				case OUTCORE:
					System.out.println(as.getNumber() + " out core");
					break;
				case REGIONISP:
					System.out.println(as.getNumber() + " regional ISP");
					break;
				case TRANSITCORE:
					System.out.println(as.getNumber() + " transit core");
					break;
				case UNKOWN:
					break;
				default:
					break;			
				}
			} else {
				System.out.println("Sorry, please input again.");
			}
		}
	}
	/**
	 * preprocessDataset
	 * Task 1
	 */
	private void preprocessDataset() {
		BufferedReader br = null;
		FileOutputStream fos = null;
		PrintStream ps = null;
		try {
			fos = new FileOutputStream(Constants.preprocessFile);
			ps = new PrintStream(fos);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String line = "";
		try {
			br = new BufferedReader(new FileReader(Constants.bgpdata));
			while ((line = br.readLine()) != null) {// && index < 5
				
				if (line.contains("{")|| line.contains("}")) {
					//System.out.println(line);
				} else {
					String ASString = line.substring(Constants.bpgdataPrefix.length());
					String []ASes = ASString.split(" ");
					if (ASes.length == 0 || ASString.equals("")) {
						continue;
					}
					List<Integer> asKeysInpath = new ArrayList<Integer>();
					for (int i = 0; i < ASes.length; i ++) {
						Integer asKey = Integer.parseInt(ASes[i]);
						if (asKeysInpath.indexOf(asKey)<0) {
							asKeysInpath.add(asKey);
						}
						
						if (!asMap.containsKey(asKey)) {
							AS newAs = new AS(asKey.intValue());
							asMap.put(asKey, newAs);
							originAsMap.put(asKey, newAs);
						} 
					}
					AS []asesInpath = new AS[asKeysInpath.size()];
					for (int i = 0; i < asKeysInpath.size(); i++) {
						asesInpath[i] = asMap.get(asKeysInpath.get(i));
					}
					ASPath path = new ASPath(asesInpath);
					//System.out.println("path:"+path.toString());
					if (!asPathMap.containsKey(path.toString())) {
						asPathMap.put(path.toString(), path);
						ps.println(path.toString());
					} 
				}
				//String[] orderline = line.split(",");
			}
			System.out.println("Number of ASes: " + asMap.size());
			ps.println("Number of ASes: " + asMap.size());
			System.out.println("Number of AS paths:" + asPathMap.size());
			ps.println("Number of AS paths:" + asPathMap.size());
			//System.out.println("duplicated aspathes:"+index);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * constructASGraph
	 * Task 2
	 */
	private void constructASGraph() {
		for (Map.Entry<String, ASPath> entry : asPathMap.entrySet()) {
			ASPath path = entry.getValue();
			AS []ases = path.getAses();
			if (ases.length == 1)
				continue;
			for (int i = 0; i < ases.length; i ++) {
				AS as = ases[i];
				if (i == 0) {
					AS next = ases[i + 1];
					if (!as.getNeighbourMap().containsKey(next.getNumber())) {
						as.insertNeibour(next.getNumber(), next);
					}
				} else if (i == ases.length - 1) {
					AS previous = ases[i - 1];
					if (!as.getNeighbourMap().containsKey(previous.getNumber())) {
						as.insertNeibour(previous.getNumber(), previous);
					}
				} else {
					AS previous = ases[i - 1];
					AS next = ases[i + 1];
					if (!as.getNeighbourMap().containsKey(next.getNumber())) {
						as.insertNeibour(next.getNumber(), next);
					}
					if (!as.getNeighbourMap().containsKey(previous.getNumber())) {
						as.insertNeibour(previous.getNumber(), previous);
					}
				}
			}
		}
		AS []top10 = new AS[10];
		int index = 0;
		for (Map.Entry<Integer, AS> entry : asMap.entrySet()) {
			AS as = entry.getValue();
			if (index < 10) {
				top10[index] = as;
				//Arrays.sort(top10, new ByWeightComparator());
			} else if (index == 10) {
				Arrays.sort(top10, new ByDegreeComparator());
				if (as.getDegree() > top10[0].getDegree()) {
					top10[0] = as;
				}
				Arrays.sort(top10, new ByDegreeComparator());
			} else {
				if (as.getDegree() > top10[0].getDegree()) {
					top10[0] = as;
				}
				Arrays.sort(top10, new ByDegreeComparator());
			}
			index ++;
		}
		for (int i = 0; i < 10; i ++) {
			System.out.println("AS "+top10[i].getNumber()+" degree:"+top10[i].getDegree());
		}
//		System.out.println("Neighbours of AS " + top10[0].getNumber());
//		for (Map.Entry<Integer, AS> entry : top10[0].getNeighbourMap().entrySet()) {
//			System.out.println(entry.getValue().getNumber());
//		}
	}
	/**
	 * constructASGraph
	 * Task 3
	 */
	private void annotateASGraph() {
		// algorithm 1
		// phase 2
		for (Map.Entry<String, ASPath> entry : asPathMap.entrySet()) {
			ASPath path = entry.getValue();
			AS []ases = path.getAses();
			if (ases.length == 1) {
				continue;
			}
			int j = 0;
			int maxdegree = 0;
			for (int i = 0; i < ases.length; i ++) {
				if (ases[i].getDegree() > maxdegree) {
					maxdegree = ases[i].getDegree();
					j = i;
				}
			}
			for (int i = 0; i < j; i ++) {
				Map<Integer, Integer> transitMap = ases[i].getTransit();
				if (transitMap.containsKey(ases[i+1].getNumber())) {
					Integer transit = transitMap.get(ases[i+1].getNumber());
					transit = transit + 1;
					transitMap.replace(ases[i+1].getNumber(), transit);
				} else {
					System.err.println("AS "+ ases[i].getNumber() +" do not contain transit of AS "+ases[i+1].getNumber());
					transitMap.put(ases[i+1].getNumber(), 1);
				}
			}

			for (int i = j; i < ases.length - 1; i ++) {
				Map<Integer, Integer> transitMap = ases[i+1].getTransit();
				if (transitMap.containsKey(ases[i].getNumber())) {
					Integer transit = transitMap.get(ases[i].getNumber());
					transit = transit + 1;
					transitMap.replace(ases[i].getNumber(), transit);
				} else {
					System.err.println("AS "+ ases[i+1].getNumber() +" do not contain transit of AS "+ases[i].getNumber());
					transitMap.put(ases[i].getNumber(), 1);
				}
			}
		}
		//phase 3
		for (Map.Entry<String, ASPath> entry : asPathMap.entrySet()) {
			ASPath path = entry.getValue();
			AS []ases = path.getAses();
			if (ases.length == 1) {
				continue;
			}
			for (int i = 0; i < ases.length - 1; i ++) {
				Map<Integer,Integer> transFor_ui = ases[i].getTransit();
				Map<Integer,Integer> transFor_ui1 = ases[i+1].getTransit();
				Integer ui = ases[i].getNumber();
				Integer ui1 = ases[i+1].getNumber();
				Map<Integer,Relationship> edge_ui = ases[i].getEdge();
				Map<Integer,Relationship> edge_ui1 = ases[i+1].getEdge();
				
				if ((transFor_ui1.get(ui) > Constants.L
							&& transFor_ui.get(ui1) > Constants.L)
						|| (transFor_ui.get(ui1) <= Constants.L
							&& transFor_ui.get(ui1) > 0
							&& transFor_ui1.get(ui) <= Constants.L
							&& transFor_ui1.get(ui) > 0)) {
					edge_ui.replace(ui1, Relationship.S2S);
					edge_ui1.replace(ui, Relationship.S2S);
				} else if (transFor_ui1.get(ui) > Constants.L
						|| transFor_ui.get(ui1) == 0) {
					edge_ui.replace(ui1, Relationship.P2C);
					edge_ui1.replace(ui, Relationship.C2P);
				} else if (transFor_ui.get(ui1) > Constants.L
						|| transFor_ui1.get(ui) == 0) {
					edge_ui.replace(ui1, Relationship.C2P);
					edge_ui1.replace(ui, Relationship.P2C);
				}
			}
		}
		
		// algorithm 2, annotating p2p relationship
		// Phase 2
		for (Map.Entry<String, ASPath> entry : asPathMap.entrySet()) {
			ASPath path = entry.getValue();
			AS []ases = path.getAses();
			if (ases.length == 1) {
				continue;
			}
			int j = 0;
			int maxdegree = 0;
			for (int i = 0; i < ases.length; i ++) {
				if (ases[i].getDegree() > maxdegree) {
					maxdegree = ases[i].getDegree();
					j = i;
				}
			}
			
			for (int i = 0; i < j - 2; i ++) {
				Map<Integer, Integer> notPeer_ui = ases[i].getNotpeering();
				if (notPeer_ui.containsKey(ases[i+1].getNumber())) {
					notPeer_ui.replace(ases[i+1].getNumber(), 1);
				} else {
					System.err.println("AS "+ ases[i].getNumber() +" do not contain notpeer of AS "+ases[i+1].getNumber());
					notPeer_ui.put(ases[i+1].getNumber(), 1);
				}
			}
			
			for (int i = j + 1; i < ases.length - 1; i ++) {
				Map<Integer, Integer> notPeer_ui = ases[i].getNotpeering();
				if (notPeer_ui.containsKey(ases[i+1].getNumber())) {
					notPeer_ui.replace(ases[i+1].getNumber(), 1);
				} else {
					System.err.println("AS "+ ases[i].getNumber() +" do not contain notpeer of AS "+ases[i+1].getNumber());
					notPeer_ui.put(ases[i+1].getNumber(), 1);
				}
			}
			
			if (j > 0 && j < ases.length - 1) {
				Map<Integer, Relationship> edgeMap_uj = ases[j].getEdge();
				Map<Integer, Relationship> edgeMap_uj_1 = ases[j-1].getEdge();
				if (edgeMap_uj_1.get(ases[j].getNumber()) != Relationship.S2S
						&& edgeMap_uj.get(ases[j+1].getNumber()) != Relationship.S2S) {
					if (ases[j-1].getDegree() > ases[j+1].getDegree()) {
						ases[j].getNotpeering().replace(ases[j+1].getNumber(),1);
					} else {
						ases[j-1].getNotpeering().replace(ases[j].getNumber(), 1);
					}
				}
			}
		}
		
		// Phase 3
		for (Map.Entry<String, ASPath> entry : asPathMap.entrySet()) {
			ASPath path = entry.getValue();
			AS []ases = path.getAses();
			if (ases.length == 1) {
				continue;
			}
			
			for (int j = 0; j < ases.length - 1; j ++) {
				Map<Integer, Integer> notPeering_uj = ases[j].getNotpeering();
				Map<Integer, Integer> notPeering_uj1 = ases[j+1].getNotpeering();
				AS uj = ases[j];
				AS uj1 = ases[j+1];
				if (notPeering_uj.get(uj1.getNumber()) != 1
						&& notPeering_uj1.get(uj.getNumber()) != 1
						&& (int)(uj.getDegree()/uj1.getDegree()) < Constants.R
						&& (double)((double)uj.getDegree()/(double)uj1.getDegree()) > (double)((double)1/(double)Constants.R)) {
					uj.getEdge().replace(uj1.getNumber(), Relationship.P2P);
					uj1.getEdge().replace(uj.getNumber(), Relationship.P2P);
				}
			}
		}
		
		//output
		int counter = 0;
		for (Map.Entry<String, ASPath> entry : asPathMap.entrySet()) {
			ASPath path = entry.getValue();
			AS []ases = path.getAses();
			if (ases.length == 1)
				continue;
			for (int i = 0; i < ases.length - 1; i ++) {
				switch (ases[i].getEdge().get(ases[i+1].getNumber())) {
				case P2P:
					System.out.println(ases[i].getNumber() + " " + ases[i+1].getNumber() + " p2p");
					break;
				case C2P:
					System.out.println(ases[i].getNumber() + " " + ases[i+1].getNumber() + " c2p");
					break;
				case S2S:
					System.out.println(ases[i].getNumber() + " " + ases[i+1].getNumber() + " s2s");
					break;
				case P2C:
					System.out.println(ases[i].getNumber() + " " + ases[i+1].getNumber() + " p2c");
					break;
				case UNKOWN:
					System.out.println(ases[i].getNumber() + " " + ases[i+1].getNumber() + " unkown");
				default:
					break;
				}
				//System.out.println(ases[i+1].getNumber() + " " + ases[i].getNumber() + ases[i+1].getEdge().get(ases[i].getNumber()));
			}
			if (counter > 10) {
				break;
			}
			counter++;
		}
	}
	/**
	 * classifyASes
	 * Task 4
	 */
	private void classifyASes() {
		//1st step, classify all leaf nodes as stub nodes and remove stub nodes
		//System.out.println("total as number: "+ asMap.size());
		for (Map.Entry<Integer, AS> entry : asMap.entrySet()) {
			AS as = entry.getValue();
			boolean isStub = true;
			Map<Integer, AS> neighbours = as.getNeighbourMap();
			for (Map.Entry<Integer, AS> nEntry : neighbours.entrySet()) {
				AS neighbour = nEntry.getValue();
				if (as.getEdge().get(neighbour.getNumber()) != Relationship.C2P
						&& as.getEdge().get(neighbour.getNumber()) != Relationship.S2S) {
					isStub = false;
					break;
				}
			}
			if (isStub == true) {
				as.setClassification(Classify.STUB);
				stubs.put(as.getNumber(), as);
				for (Map.Entry<Integer, AS> nEntry : neighbours.entrySet()) {
					AS neighbour = nEntry.getValue();
					neighbour.getNeighbourMap().remove(as.getNumber());
					neighbour.getEdge().remove(as.getNumber());
				}
			}
		}

		for (Map.Entry<Integer, AS> entry : stubs.entrySet()) {
			AS as = entry.getValue();
			asMap.remove(as.getNumber());
		}
		System.out.println("stub number: "+stubs.size());
		System.out.println("remaining ases: "+asMap.size());
		//2nd step, classify regional ISPs and remove these nodes
		while (true) {
			List<AS> newRegionISPs = new ArrayList<AS>();
			for (Map.Entry<Integer, AS> entry : asMap.entrySet()) {
				AS as = entry.getValue();
				boolean isRegionISP = true;
				Map<Integer, AS> neighbours = as.getNeighbourMap();
				for (Map.Entry<Integer, AS> nEntry : neighbours.entrySet()) {
					AS neighbour = nEntry.getValue();
					if (as.getEdge().get(neighbour.getNumber()) != Relationship.C2P
							&& as.getEdge().get(neighbour.getNumber()) != Relationship.S2S) {
						isRegionISP = false;
						break;
					}
				}
				if (isRegionISP == true) {
					as.setClassification(Classify.REGIONISP);
					newRegionISPs.add(as);
					regionISPs.put(as.getNumber(), as);
					for (Map.Entry<Integer, AS> nEntry : neighbours.entrySet()) {
						AS neighbour = nEntry.getValue();
						neighbour.getNeighbourMap().remove(as.getNumber());
						neighbour.getEdge().remove(as.getNumber());
					}
				}
			}
			if (newRegionISPs.size() == 0) {
				break;
			}
			//System.out.println("classified "+newRegionISPs.size()+" regional ISPs this round");
			for (int i = 0; i < newRegionISPs.size(); i ++) {
				asMap.remove(newRegionISPs.get(i).getNumber());
			}
		}
		//3nd step, classify dense cores, transit cores and outer cores
		for (Map.Entry<Integer, AS> entry : asMap.entrySet()) {
			AS as = entry.getValue();
			boolean isDenseCore = true;
			Map<Integer, AS> neighbours = as.getNeighbourMap();
			for (Map.Entry<Integer, AS> nEntry : neighbours.entrySet()) {
				AS neighbour = nEntry.getValue();
				if (as.getEdge().get(neighbour.getNumber()) == Relationship.C2P) {
					isDenseCore = false;
					break;
				}
			}
			if (isDenseCore == true) {
				as.setClassification(Classify.DENSECORE);
				denseCores.put(as.getNumber(), as);
			}
		}
		//System.out.println("dense core number: "+denseCores.size());
		for (Map.Entry<Integer,AS> Entry : denseCores.entrySet()) {
			AS as = Entry.getValue();
			Map<Integer, AS> neighbours = as.getNeighbourMap();
			for (Map.Entry<Integer, AS> nEntry : neighbours.entrySet()) {
				AS neighbour = nEntry.getValue();

				if ((as.getEdge().get(neighbour.getNumber()) == Relationship.P2P)
						//|| as.getEdge().get(neighbour.getNumber()) == Relationship.S2S)
						&& !denseCores.containsKey(neighbour.getNumber())) {
					neighbour.setClassification(Classify.TRANSITCORE);
					transitCores.put(neighbour.getNumber(), neighbour);
					if (asMap.get(neighbour.getNumber()) == null) {
						System.err.println("ERROR");
					}
				}
			}
		}
		//System.out.println("transit core number: "+transitCores.size());
		for (Map.Entry<Integer, AS> entry : asMap.entrySet()) {
			AS as = entry.getValue();
			if (as.getClassification() == Classify.UNKOWN) {
				as.setClassification(Classify.OUTCORE);
				outerCores.put(as.getNumber(), as);
			}
		}
		System.out.println("outer core number: "+outerCores.size());
		
		for (Map.Entry<Integer, AS> entry : denseCores.entrySet()) {
			AS as = entry.getValue();
			System.out.println("Dense Core: "+as.getNumber()+" degree: "+as.getDegree());
		}
		
		int index = 0;
		for (Map.Entry<Integer,AS> asEntry : originAsMap.entrySet()) {
			AS as = asEntry.getValue();
			switch (as.getClassification()) {
			case STUB:
				System.out.println(as.getNumber() + " stub");
				break;
			case DENSECORE:
				System.out.println(as.getNumber() + " dense core");
				break;
			case OUTCORE:
				System.out.println(as.getNumber() + " out core");
				break;
			case REGIONISP:
				System.out.println(as.getNumber() + " regional ISP");
				break;
			case TRANSITCORE:
				System.out.println(as.getNumber() + " transit core");
				break;
			case UNKOWN:
				break;
			default:
				break;			
			}
			if (index > 10)
				break;
			index ++;
		}
	}
	/**
	 * drawInterConnectTable
	 * Advanced part
	 */
	private void drawInterConnectTable() {
		int [][]edgeNumber = new int[4][5];
		//denseCore
		for (Map.Entry<Integer, AS> entry : denseCores.entrySet()) {
			AS as = entry.getValue();
			Map<Integer,AS> neighbourMap = as.getOrignNeighbourMap();
			Map<Integer,Relationship> edge = as.getEdge();
			for (Map.Entry<Integer, AS> nEntry : neighbourMap.entrySet()) {
				AS neighbour = nEntry.getValue();
				
				if (edge.get(neighbour.getNumber()) != Relationship.C2P
						&& edge.get(neighbour.getNumber()) != Relationship.S2S) {
					//there is an edge from node to neighbor
					Classify neighbourClass = originAsMap.get(neighbour.getNumber()).getClassification();
					if (neighbourClass == Classify.DENSECORE) {
						edgeNumber[0][0]++;
					} else if (neighbourClass == Classify.TRANSITCORE) {
						edgeNumber[0][1]++;
					} else if (neighbourClass == Classify.OUTCORE) {
						edgeNumber[0][2]++;
					} else if (neighbourClass == Classify.REGIONISP) {
						edgeNumber[0][3]++;
					} else if (neighbourClass == Classify.STUB) {
						edgeNumber[0][4]++;
					}
				}
			}			
		}
		//transitCore
		for (Map.Entry<Integer, AS> entry : transitCores.entrySet()) {
			AS as = entry.getValue();
			Map<Integer,AS> neighbourMap = as.getOrignNeighbourMap();
			Map<Integer,Relationship> edge = as.getEdge();
			for (Map.Entry<Integer, AS> nEntry : neighbourMap.entrySet()) {
				AS neighbour = nEntry.getValue();
				
				if (edge.get(neighbour.getNumber()) != Relationship.C2P
						&& edge.get(neighbour.getNumber()) != Relationship.S2S) {
					//there is an edge from node to neighbor
					Classify neighbourClass = originAsMap.get(neighbour.getNumber()).getClassification();
					if (neighbourClass == Classify.DENSECORE) {
						edgeNumber[1][0]++;
					} else if (neighbourClass == Classify.TRANSITCORE) {
						edgeNumber[1][1]++;
					} else if (neighbourClass == Classify.OUTCORE) {
						edgeNumber[1][2]++;
					} else if (neighbourClass == Classify.REGIONISP) {
						edgeNumber[1][3]++;
					} else if (neighbourClass == Classify.STUB) {
						edgeNumber[1][4]++;
					}
				}
			}
		}
		//outerCore
		for (Map.Entry<Integer, AS> entry : outerCores.entrySet()) {
			AS as = entry.getValue();
			Map<Integer,AS> neighbourMap = as.getOrignNeighbourMap();
			Map<Integer,Relationship> edge = as.getEdge();
			for (Map.Entry<Integer, AS> nEntry : neighbourMap.entrySet()) {
				AS neighbour = nEntry.getValue();
				
				if (edge.get(neighbour.getNumber()) != Relationship.C2P
						&& edge.get(neighbour.getNumber()) != Relationship.S2S) {
					//there is an edge from node to neighbor
					Classify neighbourClass = originAsMap.get(neighbour.getNumber()).getClassification();
					if (neighbourClass == Classify.DENSECORE) {
						edgeNumber[2][0]++;
					} else if (neighbourClass == Classify.TRANSITCORE) {
						edgeNumber[2][1]++;
					} else if (neighbourClass == Classify.OUTCORE) {
						edgeNumber[2][2]++;
					} else if (neighbourClass == Classify.REGIONISP) {
						edgeNumber[2][3]++;
					} else if (neighbourClass == Classify.STUB) {
						edgeNumber[2][4]++;
					}
				}
			}			
		}
		//regionISP
		for (Map.Entry<Integer, AS> entry : regionISPs.entrySet()) {
			AS as = entry.getValue();
			Map<Integer,AS> neighbourMap = as.getOrignNeighbourMap();
			Map<Integer,Relationship> edge = as.getEdge();
			for (Map.Entry<Integer, AS> nEntry : neighbourMap.entrySet()) {
				AS neighbour = nEntry.getValue();
				
				if (edge.get(neighbour.getNumber()) != Relationship.C2P
						&& edge.get(neighbour.getNumber()) != Relationship.S2S) {
					//there is an edge from node to neighbor
					Classify neighbourClass = originAsMap.get(neighbour.getNumber()).getClassification();
					if (neighbourClass == Classify.DENSECORE) {
						edgeNumber[3][0]++;
					} else if (neighbourClass == Classify.TRANSITCORE) {
						edgeNumber[3][1]++;
					} else if (neighbourClass == Classify.OUTCORE) {
						edgeNumber[3][2]++;
					} else if (neighbourClass == Classify.REGIONISP) {
						edgeNumber[3][3]++;
					} else if (neighbourClass == Classify.STUB) {
						edgeNumber[3][4]++;
					}
				}
			}
		}
		System.out.println("------------------------------------------------------------");
		System.out.println("INTER-CONNECTIVITY ACROSS LAYERS");
		System.out.println("Layer\t0\t1\t2\t3\t4");
		for (int i = 0; i < 4; i ++) {
			System.out.printf("%4d%8d%8d%8d%8d%8d\n",i,edgeNumber[i][0],edgeNumber[i][1],
					edgeNumber[i][2],edgeNumber[i][3],edgeNumber[i][4]);
		}
	}
	/**
	 * asDegreeDistributionByLevel
	 * Advanced part
	 */
	private void asDegreeDistributionByLevel(Map<Integer,AS> levelMap) {
		int []stage = {1,2,3,5,8,10,15,20,50,80,100,150,200,500,1000};
		int []levelNumber = new int[stage.length];
		for (Map.Entry<Integer, AS> entry : levelMap.entrySet()) {
			AS as = entry.getValue();
			for (int i = 0; i < stage.length; i ++) {
				if (as.getOrignNeighbourMap().size() < stage[i]) {
					levelNumber[i] ++;
					break;
				}
			}
		}
		int sumNumber = 0;
		for (int i = 0; i < stage.length; i ++) {
			sumNumber+=levelNumber[i];
			System.out.println("number for stage " + stage[i] +" :"+sumNumber+
					" percentage:"+(double)((double)sumNumber/(double)levelMap.size()));
		}
	}
	
	private void asSumary() {
		int totalPathLen = 0;
		for (Map.Entry<String, ASPath> entry : asPathMap.entrySet()) {
			ASPath path = entry.getValue();
			AS []ases = path.getAses();
			totalPathLen += ases.length;
		}
		System.out.println("--------------------Summary--------------------------------");
		System.out.println("number of ASes:"+originAsMap.size());
		System.out.println("number of AS pathes:"+asPathMap.size());
		System.out.println("average length of AS paths: "+ totalPathLen/asPathMap.size());
		System.out.println("number of dense cores:"+denseCores.size());
		System.out.println("number of transit cores:"+transitCores.size());
		System.out.println("number of outer cores:"+outerCores.size());
		System.out.println("number of regional ISPs:"+regionISPs.size());
		System.out.println("number of stubs:"+stubs.size());
	}
}

class ByDegreeComparator implements Comparator {

	public final int compare(Object pFirst, Object pSecond) {
		int aFirstWeight = ((AS) pFirst).getDegree();
		int aSecondWeight = ((AS) pSecond).getDegree();
		int diff = aFirstWeight - aSecondWeight;
		if (diff > 0)
			return 1;
		if (diff < 0)
			return -1;
		else
			return 0;
	}
}