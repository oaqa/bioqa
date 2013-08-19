package edu.cmu.lti.oaqa.bio.core.ie.learning;

import java.util.Comparator;

import edu.cmu.lti.oaqa.framework.data.PassageCandidate;

public class PassageCandidateComparator implements Comparator<PassageCandidate> {
	// Ranks by score, decreasing.
	public int compare( PassageCandidate s1 , PassageCandidate s2 ) {
		if ( s1.getProbability() < s2.getProbability() ) {
			return 1;
		} else if ( s1.getProbability() > s2.getProbability() ) {
			return -1;
		}
		return 0;
	}		
}
