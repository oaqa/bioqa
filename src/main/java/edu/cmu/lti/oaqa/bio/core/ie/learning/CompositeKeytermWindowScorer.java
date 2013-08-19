package edu.cmu.lti.oaqa.bio.core.ie.learning;

import java.util.ArrayList;
import java.util.List;

public class CompositeKeytermWindowScorer implements KeytermWindowScorer {
	
	private List<KeytermWindowScorer> scorers;
	
	public CompositeKeytermWindowScorer () {
		super();
		scorers = new ArrayList<KeytermWindowScorer>();
	}
	
	public void useDefaultScorers() {
		removeAll();
		add( new PercentMatchesScore() , .25d );
		add( new KeytermMatchesScore() , .25d );
		add( new PassageBrevityScore() , .25d );
		add( new PassageOffsetScore() , .25d );
	}
	
	public void removeAll() {
		scorers.clear();
	}
	
	public void add( KeytermWindowScorer scorer, Double lambda ) {
		scorers.add( new WeightedScorer( scorer , lambda ) );
	}

	@Override
	public double scoreWindow(int begin, int end, int matchesFound,
			int totalMatches, int keytermsFound, int totalKeyterms, int textSize) {
		double result = 0.0d;
		for ( KeytermWindowScorer scorer : scorers ) {
			double score = scorer.scoreWindow( begin , end , matchesFound , totalMatches , keytermsFound , totalKeyterms , textSize );
			if ( score > 1.0d || score < 0.0d )
				System.out.println( scorer.getClass().getSimpleName() + " OUT OF BOUNDS: " + score );
			result += score;			
		}
		return result;
	}
	private class WeightedScorer implements KeytermWindowScorer {
		Double lambda;
		KeytermWindowScorer scorer;
		public WeightedScorer( KeytermWindowScorer scorer , Double lambda ) {
			this.scorer = scorer;
			this.lambda = lambda;
		}
		@Override
		public double scoreWindow(int begin, int end, int matchesFound,
				int totalMatches, int keytermsFound, int totalKeyterms,
				int textSize) {
			return lambda * scorer.scoreWindow( begin , end , matchesFound , totalMatches , keytermsFound , totalKeyterms , textSize );
		}
		
	}
	

	
	
	

}
