package edu.kaist.mrlab.cw.data;

import java.util.Comparator;


public class InstanceComparator implements Comparator<Instance> {

	@Override
	public int compare(Instance o1, Instance o2) {
		// TODO Auto-generated method stub
		double score1 = o1.getScore();
		double score2 = o2.getScore();
		if (score2 > score1) {
			return 1;
		} else if (score1 > score2) {
			return -1;
		} else {
			return 0;
		}
	}

}
