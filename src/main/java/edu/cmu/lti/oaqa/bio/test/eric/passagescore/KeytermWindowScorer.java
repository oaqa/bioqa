package edu.cmu.lti.oaqa.bio.test.eric.passagescore;

public interface KeytermWindowScorer {
	public double scoreWindow ( int begin , int end , int matchesFound , int totalMatches , int keytermsFound , int totalKeyterms , int textSize );
		
}
