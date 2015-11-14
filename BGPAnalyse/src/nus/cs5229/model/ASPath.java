package nus.cs5229.model;

import java.util.Arrays;

public class ASPath {
	
	public ASPath() {}
	
	public ASPath(AS[] ases) {
		super();
		this.ases = ases;
	}

	private AS[] ases;

	@Override
	public String toString() {
		String pathString = "";
		for (int i = 0; i < ases.length; i ++) {
			pathString = pathString + ases[i].getNumber() + " ";
		}
		pathString = pathString.substring(0, pathString.length()-1);
		return pathString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(ases);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ASPath other = (ASPath) obj;
		if (!Arrays.equals(ases, other.ases))
			return false;
		return true;
	}

	public AS[] getAses() {
		return ases;
	}

	public void setAses(AS[] ases) {
		this.ases = ases;
	}
}
