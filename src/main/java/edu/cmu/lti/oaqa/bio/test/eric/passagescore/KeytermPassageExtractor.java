package edu.cmu.lti.oaqa.bio.test.eric.passagescore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.oaqa.cse.basephase.ie.AbstractPassageExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class KeytermPassageExtractor extends AbstractPassageExtractor {

	private static DecimalFormat score = new DecimalFormat( "0.000" );
	private CompositeKeytermWindowScorer scorer;
	private SolrWrapper wrapper;
	private PassageCandidateComparator comparator;
	float totalBytes, valueBytes;
	
	// Configuration parameters.
	List<Double> scorerLambdas;
	private Integer keytermMatchLimit;
	private Integer passageSizeLimit;
	private Integer maxPassages;
	private Double overlapThreshold;

	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize( aContext );
		
		// Get configuration parameter values for solr wrapper.
		String serverUrl = (String) aContext.getConfigParameterValue("server");
		Integer serverPort = (Integer) aContext.getConfigParameterValue("port");
		Boolean embedded = (Boolean) aContext.getConfigParameterValue("embedded");
		String core = (String) aContext.getConfigParameterValue("core");

		// Get component configuration parameter values.
		
		// This doesn't work with cross-opts, which extracts Strings (can't be directly cast to Integer). Not sure
		// why it works when not using cross-opts...
		keytermMatchLimit = (Integer)aContext.getConfigParameterValue( "keytermMatchLimit" );
		passageSizeLimit = (Integer)aContext.getConfigParameterValue( "passageSizeLimit" );
		maxPassages = (Integer)aContext.getConfigParameterValue( "maxPassages" );

		// keytermMatchLimit = Integer.parseInt( (String)aContext.getConfigParameterValue( "keytermMatchLimit" ) );
		// passageSizeLimit = Integer.parseInt( (String) aContext.getConfigParameterValue( "passageSizeLimit" ) );
		// maxPassages = Integer.parseInt( (String)aContext.getConfigParameterValue( "maxPassages" ) );
		
		overlapThreshold = Double.parseDouble((String)aContext.getConfigParameterValue( "overlapThreshold" ));		
		List<Double> scorerLambdas = new ArrayList<Double>();
		for ( String lambdaString : (String[])aContext.getConfigParameterValue( "keytermScorerLambdas" )) 
			scorerLambdas.add( new Double( Double.parseDouble( lambdaString ) ) );
		totalBytes = 0;
		valueBytes = 0;

		// Create solr wrapper, scorer composite.
		try {
			this.wrapper = new SolrWrapper(serverUrl, serverPort, embedded, core);
			this.scorer = new CompositeKeytermWindowScorer();
			this.comparator = new PassageCandidateComparator();
			String[] scorers = (String[])aContext.getConfigParameterValue( "keytermWindowScorers" );
			if ( ! ( scorers.length == scorerLambdas.size() ))
				throw new RuntimeException( "Configuration Parameter values must be same length: keytermWindowScorers, keytermScorerLambdas" );
			for ( int i = 0 ; i < scorers.length ; i++ )
				scorer.add( (KeytermWindowScorer)Class.forName( scorers[ i ] ).newInstance() , scorerLambdas.get( i ) );
		} catch ( Exception e ) {
			throw new ResourceInitializationException( e );
		}
	}

	@Override
	protected List<PassageCandidate> extractPassages( String question, List<Keyterm> keyterms, List<RetrievalResult> documents ) {
		List<PassageCandidate> result = new ArrayList<PassageCandidate>();
		for ( RetrievalResult document : documents ) {
			String id = document.getDocID();
			try {
				String text = wrapper.getDocText( id );
				List<String> keytermStrings = Lists.transform(keyterms, new Function<Keyterm, String>() {
					public String apply(Keyterm keyterm) { return keyterm.getText(); }
				});
				List<PassageCandidate> passageSpans = extractTextPassages( id , text , keytermStrings.toArray(new String[0]) );
				passageSpans = removeOverlappingPassages( passageSpans );
				for ( PassageCandidate passageSpan : passageSpans ) 
					result.add( passageSpan );
			} catch ( SolrServerException e ) {
				e.printStackTrace();
			}
		}
		Collections.sort( result , comparator );
		System.out.println( "Raw passages: " + result.size() );
		result = result.subList( 0 , maxPassages );
		System.out.println( "Passages returned: " + result.size() );
		int rank = 0;
		for ( PassageCandidate p : result ) {
			rank++;
			String id = p.getDocID();
			String text = null;
			try {
				text = wrapper.getDocText( id );
			} catch (SolrServerException e) {
				e.printStackTrace();
			}
			text = text.substring( p.getStart() , p.getEnd() );
			float prob = p.getProbability();
			totalBytes += text.length();
			valueBytes += (float)text.length() * prob;
			// System.out.println( rank + ". [" + score.format( prob ) + " : " + id + " (" + ( p.getEnd() - p.getStart() )  + ")" + " : \"" + text + "\"]" );
		}
		return result;
	}

	public List<PassageCandidate> removeOverlappingPassages ( List<PassageCandidate> passages ){
		boolean[] overlapping = new boolean[ passages.size() ];
		for ( int i = 0 ; i < passages.size() ; i++ ) {
			if ( overlapping[i] ) continue;
			PassageCandidate p1 = passages.get( i );
			for ( int j = i+1 ; j < passages.size() ; j++ ) {
				PassageCandidate p2 = passages.get( j );
				double overlapRatio = overlapRatio( p1 , p2 );
				if ( overlapRatio > overlapThreshold ) 
					if ( p1.getProbability() >= p2.getProbability() ) {
						overlapping[ j ] = true;
					} else {
						overlapping[ i ] = true;
					}
			}
		}
		List<PassageCandidate> newResult = new ArrayList<PassageCandidate>();
		for ( int i = 0 ; i < passages.size(); i++ )
			if (! overlapping[ i ] )
				newResult.add( passages.get( i ) );
		return newResult;
	}

	private double overlapRatio ( PassageCandidate p1 , PassageCandidate p2 ) {
		int p1s = p1.getStart(); int p1e = p1.getEnd(); 
		int p2s = p2.getStart(); int p2e = p2.getEnd();
		int overlapSize = 0; int totalSize = 0;
		if ( p1s <= p2s && p1e <= p2e && p2s < p1e ) {
			overlapSize = p1e - p2s;
			totalSize = p2e - p1s;
		} else if ( p2s <= p1s && p2e <= p1e && p1s < p2e ) {
			overlapSize = p2e - p1s;
			totalSize = p1e - p2s;
		}
		return ( overlapSize == 0 ) ? 0.0d : (double)overlapSize / (double)totalSize;
	}

	public List<PassageCandidate> extractTextPassages( String id , String text , String[] keyterms ) {
		int totalMatches = 0;
		int totalKeyterms = 0;
		List<List<PassageSpan>> matchingSpans = new ArrayList<List<PassageSpan>>();

		// 1. Find all keyterm matches, up to keytermMatchLimit matches per keyterm.
		for ( String keyterm : keyterms ) {
			int keytermMatchesFound = 0;
			List<PassageSpan> matchedSpans = new ArrayList<PassageSpan>();
			Matcher m = Pattern.compile( keyterm ).matcher( text );
			while ( m.find() && keytermMatchesFound < keytermMatchLimit ) {
				matchedSpans.add( new PassageSpan( m.start() , m.end() ) );
				keytermMatchesFound++;
				totalMatches++;
			}
			if (! matchedSpans.isEmpty() ) {
				matchingSpans.add( matchedSpans );
				totalKeyterms++;
			}
		}

		// 2. Create set of left edges and right edges which define possible windows.
		List<Integer> leftEdges = new ArrayList<Integer>();
		List<Integer> rightEdges = new ArrayList<Integer>();
		for ( List<PassageSpan> keytermMatches : matchingSpans ) {
			for ( PassageSpan keytermMatch : keytermMatches ) {
				Integer leftEdge = keytermMatch.begin;
				Integer rightEdge = keytermMatch.end; 
				if (! leftEdges.contains( leftEdge ))
					leftEdges.add( leftEdge );
				if (! rightEdges.contains( rightEdge ))
					rightEdges.add( rightEdge );
			}
		}

		// 3. For every possible window, calculate keyterms found, matches found; score window, and create passage candidate.
		List<PassageCandidate> result = new ArrayList<PassageCandidate>();
		for ( Integer begin : leftEdges ) {
			for ( Integer end : rightEdges ) {
				if ( end <= begin ) continue; 
				if ( ( end - begin ) > passageSizeLimit ) continue;
				int keytermsFound = 0;
				int matchesFound = 0;
				for ( List<PassageSpan> keytermMatches : matchingSpans ) {
					boolean thisKeytermFound = false;
					for ( PassageSpan keytermMatch : keytermMatches ) {
						if ( keytermMatch.containedIn( begin , end ) ){
							matchesFound++;
							thisKeytermFound = true;
						}
					}
					if ( thisKeytermFound ) keytermsFound++;
				}
				double score = scorer.scoreWindow( begin , end , matchesFound , totalMatches , keytermsFound , totalKeyterms , text.length() );
				PassageCandidate window = null;
				try {
					window = new PassageCandidate( id , begin , end , (float) score , null );
				} catch ( AnalysisEngineProcessException e ) {
					e.printStackTrace();
				}
				result.add( window );
			}
		}
		return result;

	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		System.out.println( keytermMatchLimit + "," + passageSizeLimit + "," + maxPassages + "," + score.format( valueBytes ) + "," + totalBytes + "," + score.format( valueBytes/(float)totalBytes ) );
		wrapper.close();
	}

}
