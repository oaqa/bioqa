package edu.cmu.lti.oaqa.bio.core.ie.learning;
//package edu.cmu.lti.oaqa.bio.test.eric.passagescore;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.solr.client.solrj.SolrServerException;
//import org.apache.uima.UimaContext;
//import org.apache.uima.resource.ResourceInitializationException;
//
//import com.google.common.base.Function;
//import com.google.common.collect.Lists;
//
//import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
//import edu.cmu.lti.oaqa.cse.basephase.ie.AbstractPassageExtractor;
//import edu.cmu.lti.oaqa.framework.data.Keyterm;
//import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
//import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
//import edu.stanford.nlp.dcoref.CorefChain;
//import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
//import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
//import edu.stanford.nlp.ling.CoreAnnotations;
//import edu.stanford.nlp.ling.CoreAnnotations.IndexAnnotation;
//import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
//import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
//import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
//import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
//import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
//import edu.stanford.nlp.ling.CoreLabel;
//import edu.stanford.nlp.pipeline.Annotation;
//import edu.stanford.nlp.trees.TypedDependency;
//import edu.stanford.nlp.trees.semgraph.SemanticGraph;
//import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
//import edu.stanford.nlp.util.CoreMap;
//
//public class StanfordCoreNLPPassageExtractor extends AbstractPassageExtractor {
//	
//	private SolrWrapper wrapper;
//	
//	public void initialize( UimaContext aContext ) throws ResourceInitializationException {
//		super.initialize( aContext );
//		
//		// Get configuration parameter values for solr wrapper.
//		String serverUrl = (String) aContext.getConfigParameterValue("server");
//		Integer serverPort = (Integer) aContext.getConfigParameterValue("port");
//		Boolean embedded = (Boolean) aContext.getConfigParameterValue("embedded");
//		String core = (String) aContext.getConfigParameterValue("core");
//
//		// Create solr wrapper, stanford instance.
//		try {
//			this.wrapper = new SolrWrapper(serverUrl, serverPort, embedded, core);
//			StanfordCoreNLPFactory.getInstance();
//		} catch ( Exception e ) {
//			throw new ResourceInitializationException( e );
//		}
//	}
//
//	@Override
//	protected List<PassageCandidate> extractPassages( String question , List<Keyterm> keyterms , List<RetrievalResult> documents ) {
//		List<PassageCandidate> result = new ArrayList<PassageCandidate>();
//		for ( RetrievalResult document : documents ) {
//			String id = document.getDocID();
//			try {
//				String text = wrapper.getDocText( id );
//				List<String> keytermStrings = Lists.transform(keyterms, new Function<Keyterm, String>() {
//					public String apply(Keyterm keyterm) { return keyterm.getText(); }
//				});
//				List<PassageCandidate> passageSpans = extractMatchingPassages( id , text , question , keytermStrings.toArray(new String[0]) );
//				for ( PassageCandidate passageSpan : passageSpans ) 
//					result.add( passageSpan );
//			} catch ( SolrServerException e ) {
//				e.printStackTrace();
//			}
//		}
//		return result;
//		
//	}
//	
//	private class Label {
//		int begin;
//		int end;
//		double likelihood;
//		String source;
//		protected Label() {
//			super();
//		}
//		public Label( int begin, int end ) {
//			this.begin = begin;
//			this.end = end;
//		}
//		public void setSource(String source) {
//			this.source = source;
//		}
//		public void setLikelihood(double likelihood) {
//			this.likelihood = likelihood;
//		}
//		public int getBegin() {
//			return begin;
//		}
//		public int getEnd() {
//			return end;
//		}
//		public double getLikelihood() {
//			return likelihood;
//		}
//		public String getSource() {
//			return source;
//		}
//	}
//	private class Token extends Label {
//		private Sentence sAnnotation;
//		private Integer index;
//		private String label;
//		private String word;
//		private String pos;
//		private String ne;
//		private ArrayList<Dependency> dependencies;
//		private Token repToken;
//		private List<CorefMention> mentions;
//		public Token(int begin, int end) {
//			super( begin , end );
//		}
//		public void setSentence( Sentence sAnnotation ) {
//			this.sAnnotation = sAnnotation;
//		}
//		public void setIndex( Integer index ) {
//			this.index = index;
//		}
//		public void setLabel( String label ) {
//			this.label = label;			
//		}
//		public void setWord( String word ) {
//			this.word = word;
//		}
//		public void setPos(String pos) {
//			this.pos = pos;
//		}
//		public void setNe(String ne) {
//			this.ne = ne;
//		}
//		public void setDependencies(ArrayList<Dependency> roles) {
//			this.dependencies = roles;			
//		}
//		public void setRepresentativeToken(Token repToken) {
//			this.repToken = repToken;
//		}
//		public void setCoreferentTokens(List<CorefMention> mentions) {
//			this.mentions = mentions;
//		}		
//	}
//	private class Dependency extends Label {
//		private Sentence sAnnotation;
//		private Token gov;
//		private Token dep;
//		private String rel;
//		public Dependency(Integer sentenceStart, Integer sentenceEnd) {
//			super( sentenceStart , sentenceEnd );
//		}
//		public void setSentence(Sentence sAnnotation) {
//			this.sAnnotation = sAnnotation;
//		}
//		public void setGovernor(Token gov) {
//			this.gov = gov;			
//		}
//		public void setDependent(Token dep) {
//			this.dep = dep;
//		}
//		public void setRelation(String rel) {
//			this.rel = rel;
//		}
//	}
//	private class Sentence extends Label {
//		private List<Dependency> dependencyList;
//		private List<Token> tokenList;
//		public Sentence( Integer sentenceStart, Integer sentenceEnd ) {
//			super( sentenceStart , sentenceEnd );
//		}
//		public void setDependencies(List<Dependency> dependencyList) {
//			this.dependencyList = dependencyList;
//		}
//		public void setTokens(List<Token> tokenList) {
//			this.tokenList = tokenList;
//		}
//	}
//	
//	public class StanfordModel {
//		List<Sentence> sentences;
//		List<Dependency> dependencies;
//		List<Token> tokens;
//		public StanfordModel() {
//			super();
//			sentences = new ArrayList<Sentence>();
//			dependencies = new ArrayList<Dependency>();
//			tokens = new ArrayList<Token>();
//		}
//		public String toString() {
//			return "[StanfordModel " + sentences.size() + " sentences, " + dependencies.size() + " dependencies, " + tokens.size() + " tokens.]";
//		}
//		public void addSentence( Sentence s ) {
//			sentences.add( s );
//		}
//		public void addDependency( Dependency d ) {
//			dependencies.add( d );
//		}
//		public void addToken( Token t ) {
//			tokens.add( t );
//		}
//	}
//	private List<PassageCandidate> extractMatchingPassages( String id , String answerText , String questionText , String[] keyterms ){
//		List<PassageCandidate> result = new ArrayList<PassageCandidate>();
//		Annotation questionDocument = StanfordCoreNLPFactory.annotateText( questionText );
//		Annotation answerDocument  = StanfordCoreNLPFactory.annotateText( answerText );
//		StanfordModel questionModel = extractFeatures( questionDocument );
//		StanfordModel documentModel = extractFeatures( answerDocument );
//		System.out.println( "Question: " + questionModel );
//		System.out.println( "Document: " + documentModel );
//		return result;
//	}
//	private StanfordModel extractFeatures( Annotation document ) {
//		StanfordModel result = new StanfordModel();
//		Map<String,Token> tokenIndex = new HashMap<String,Token>();
//		List<CoreMap> sentences = document.get( SentencesAnnotation.class );
//		HashMap<Integer,HashMap<Integer,Token>> tokenMap = new HashMap<Integer,HashMap<Integer,Token>>();
//		for( int i = 0 ; i < sentences.size(); i++ ) {
//			CoreMap sentence = sentences.get( i );
//			tokenIndex.clear();
//			HashMap<Integer,Token> thisTokenMap = new HashMap<Integer,Token>();
//			tokenMap.put( i + 1 ,  thisTokenMap );
//			Integer sentenceStart = (Integer)sentence.get( CoreAnnotations.CharacterOffsetBeginAnnotation.class );
//			Integer sentenceEnd = (Integer)sentence.get( CoreAnnotations.CharacterOffsetEndAnnotation.class );
//			Sentence sAnnotation = new Sentence( sentenceStart , sentenceEnd );
//			result.addSentence( sAnnotation );
//			List<Token> tokenList = new ArrayList<Token>();
//			List<Dependency> dependencyList = new ArrayList<Dependency>();
//			for ( CoreLabel token : sentence.get( TokensAnnotation.class ) ) {
//				// get token data.
//				int begin = token.beginPosition();
//				int end = token.endPosition();
//				String word = token.get( TextAnnotation.class );
//				String pos = token.get( PartOfSpeechAnnotation.class );
//				String ne = token.get( NamedEntityTagAnnotation.class );  
//				Integer index = token.get( IndexAnnotation.class );
//				String label = word + "-" + index ;
//				// create token.
//				Token a = new Token( begin , end );
//				a.setSentence( sAnnotation );
//				a.setIndex( index );
//				a.setLabel( label );
//				a.setWord( word );
//				a.setPos( pos );
//				a.setNe( ne );
//				a.setSource( this.getClass().getName() );
//				a.setLikelihood( 1.0d );
//				// cache & add to indexes.
//				thisTokenMap.put( index , a );
//				tokenList.add( a );
//				tokenIndex.put( label , a );
//				result.addToken( a );
//			}
//			SemanticGraph graph = sentence.get( CollapsedCCProcessedDependenciesAnnotation.class );
//			if (graph != null ) {
//				HashMap<Token,ArrayList<Dependency>> tokenToDependencyMap = new HashMap<Token,ArrayList<Dependency>>();
//				Collection<TypedDependency> dependencies = graph.typedDependencies(); 
//				for ( TypedDependency d :  dependencies) {
//					Dependency a = new Dependency( sentenceStart , sentenceEnd );
//
//					Token gov = tokenIndex.get( d.gov().toString() );
//					ArrayList<Dependency> cachedRoles = tokenToDependencyMap.get( gov );
//					if ( cachedRoles == null  ) {
//						cachedRoles = new ArrayList<Dependency>();
//						tokenToDependencyMap.put( gov , cachedRoles );
//					}
//					cachedRoles.add( a );
//
//					Token dep = tokenIndex.get( d.dep().toString() );
//					cachedRoles = tokenToDependencyMap.get( dep );
//					if ( cachedRoles == null  ) {
//						cachedRoles = new ArrayList<Dependency>();
//						tokenToDependencyMap.put( dep , cachedRoles );
//					}
//					cachedRoles.add( a );
//
//					String rel = d.reln().toString();
//					a.setSentence( sAnnotation );
//					a.setGovernor( gov );
//					a.setDependent( dep );
//					a.setRelation( rel );
//					a.setSource( this.getClass().getName() );
//					a.setLikelihood( 1.0d );
//					dependencyList.add( a );
//					result.addDependency( a );
//				}
//				for ( Token token : tokenToDependencyMap.keySet() ) {
//					ArrayList<Dependency> roles = tokenToDependencyMap.get( token );
//					token.setDependencies( roles );
//				}
//				sAnnotation.setDependencies( dependencyList );
//			}
//			sAnnotation.setTokens( tokenList );
//		}
//		Map<Integer, CorefChain> graph = document.get( CorefChainAnnotation.class );
//		if ( graph != null )
//			for ( CorefChain c : graph.values() ) {
//				List<Token> tokensInChain = new ArrayList<Token>();
//				List<CorefChain.CorefMention> mentions = c.getCorefMentions();
//				for ( int i = 0 ; i < mentions.size(); i++ ) {
//					CorefChain.CorefMention mention = mentions.get( i );
//					int headIndex = mention.headIndex;
//					Integer sentIndex = mention.sentNum;
//					Map<Integer,Token> anotherTokenMap = tokenMap.get( sentIndex );
//					Token token = anotherTokenMap.get( headIndex );
//					tokensInChain.add( token );
//				}
//				CorefChain.CorefMention rep = c.getRepresentativeMention();
//				int repIndex = rep.headIndex;
//				Integer repSent = rep.sentNum;
//				Map<Integer,Token> repTokenMap = tokenMap.get( repSent );
//				Token repToken = repTokenMap.get( repIndex );
//				for ( Token headToken : tokensInChain ) {
//					headToken.setRepresentativeToken( repToken );
//					headToken.setCoreferentTokens ( mentions );
//				}
//
//			}
//		return result;
//	}
//}
//	
//	
