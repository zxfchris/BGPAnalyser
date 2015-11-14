package nus.cs5229.model;

import java.util.HashMap;
import java.util.Map;

public class AS {
	private int number;
	private int degree;
	private Map<Integer,AS> neighbourMap;
	private Map<Integer,AS> orignNeighbourMap;
	
	private Map<Integer,Relationship> edge;
	private Map<Integer,Integer> transit;
	private Map<Integer,Integer> notpeering;
	
	private Classify classification;
	
	public AS () {
		this.number = 0;
		this.degree = 0;
	}
	
	public AS (int number) {
		this.number = number;
		this.setNeighbourMap(new HashMap<Integer,AS>());
		this.setTransit(new HashMap<Integer,Integer>());
		this.setEdge(new HashMap<Integer,Relationship>());
		this.setNotpeering(new HashMap<Integer,Integer>());
		this.setOrignNeighbourMap(new HashMap<Integer,AS>());
		this.degree = 0;
		this.setClassification(Classify.UNKOWN);
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getDegree() {
		return degree;
	}

	public void setDegree(int degree) {
		this.degree = degree;
	}
	
	public Map<Integer,AS> getNeighbourMap() {
		return neighbourMap;
	}

	public void setNeighbourMap(Map<Integer,AS> neighbourMap) {
		this.neighbourMap = neighbourMap;
	}

	public void insertNeibour(Integer key, AS neighbour) {
		neighbourMap.put(key, neighbour);
		orignNeighbourMap.put(key, neighbour);
		edge.put(key, Relationship.UNKOWN);
		transit.put(key, 0);
		notpeering.put(key, 0);
		this.degree++;
	}

	public Map<Integer,Relationship> getEdge() {
		return edge;
	}

	public void setEdge(Map<Integer,Relationship> edge) {
		this.edge = edge;
	}

	public Map<Integer,Integer> getTransit() {
		return transit;
	}

	public void setTransit(Map<Integer,Integer> transit) {
		this.transit = transit;
	}

	public Map<Integer,Integer> getNotpeering() {
		return notpeering;
	}

	public void setNotpeering(Map<Integer,Integer> notpeering) {
		this.notpeering = notpeering;
	}

	public Classify getClassification() {
		return classification;
	}

	public void setClassification(Classify classification) {
		this.classification = classification;
	}

	public Map<Integer,AS> getOrignNeighbourMap() {
		return orignNeighbourMap;
	}

	public void setOrignNeighbourMap(Map<Integer,AS> orignNeighbourMap) {
		this.orignNeighbourMap = orignNeighbourMap;
	}
}
