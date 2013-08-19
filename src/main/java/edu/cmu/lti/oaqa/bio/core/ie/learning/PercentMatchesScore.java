package edu.cmu.lti.oaqa.bio.core.ie.learning;

public class PercentMatchesScore implements KeytermWindowScorer {
	@Override
	public double scoreWindow(int begin, int end, int matchesFound,
			int totalMatches, int keytermsFound, int totalKeyterms,
			int textSize) {
		return (double)matchesFound / (double)totalMatches;
	}
	
}
