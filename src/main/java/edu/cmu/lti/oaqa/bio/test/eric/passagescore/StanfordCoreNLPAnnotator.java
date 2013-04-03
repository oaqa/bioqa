package edu.cmu.lti.oaqa.openqa.hello.passage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;


import edu.cmu.lti.oaqa.openqa.hello.types.Dependency;
import edu.cmu.lti.oaqa.openqa.hello.types.MentionChain;
import edu.cmu.lti.oaqa.openqa.hello.types.Sentence;
import edu.cmu.lti.oaqa.openqa.hello.types.Token;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.IndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;


public class StanfordCoreNLPAnnotator extends JCasAnnotator_ImplBase {
	StanfordCoreNLP processor;	
	
	public void initialize( UimaContext c ) {
		processor = StanfordCoreNLPFactory.getInstance();
	}

	@Override
	public void process( JCas jcas ) throws AnalysisEngineProcessException {
		Annotation qdoc = new Annotation( jcas.getDocumentText() );
		processor.annotate( qdoc );
		Map<String,Token> tokenIndex = new HashMap<String,Token>();
		List<CoreMap> sentences = qdoc.get( SentencesAnnotation.class );
		HashMap<Integer,HashMap<Integer,Token>> tokenMap = new HashMap<Integer,HashMap<Integer,Token>>();
		for( int i = 0 ; i < sentences.size(); i++ ) {
			CoreMap sentence = sentences.get( i );
			tokenIndex.clear();
			HashMap<Integer,Token> thisTokenMap = new HashMap<Integer,Token>();
			tokenMap.put( i + 1 ,  thisTokenMap );
			Integer sentenceStart = (Integer)sentence.get( CoreAnnotations.CharacterOffsetBeginAnnotation.class );
			Integer sentenceEnd = (Integer)sentence.get( CoreAnnotations.CharacterOffsetEndAnnotation.class );
			Sentence sAnnotation = new Sentence( jcas , sentenceStart , sentenceEnd );
			List<Token> tokenList = new ArrayList<Token>();
			List<Dependency> dependencyList = new ArrayList<Dependency>();
			for ( CoreLabel token : sentence.get( TokensAnnotation.class ) ) {
				// get token data.
				int begin = token.beginPosition();
				int end = token.endPosition();
				String word = token.get( TextAnnotation.class );
				String pos = token.get( PartOfSpeechAnnotation.class );
				String ne = token.get( NamedEntityTagAnnotation.class );  
				Integer index = token.get( IndexAnnotation.class );
				String label = word + "-" + index ;
				// create token.
				Token a = new Token( jcas , begin , end );
				a.setSentence( sAnnotation );
				a.setIndex( index );
				a.setLabel( label );
				a.setWord( word );
				a.setPos( pos );
				a.setNe( ne );
				a.setSource( this.getClass().getName() );
				a.setLikelihood( 1.0d );
				// cache & add to indexes.
				thisTokenMap.put( index , a );
				tokenList.add( a );
				a.addToIndexes();
				tokenIndex.put( label , a );
			}
			SemanticGraph graph = sentence.get( CollapsedCCProcessedDependenciesAnnotation.class );
			HashMap<Token,ArrayList<Dependency>> tokenToDependencyMap = new HashMap<Token,ArrayList<Dependency>>();
			for ( TypedDependency d : graph.typedDependencies() ) {
				Dependency a = new Dependency( jcas , sentenceStart , sentenceEnd );

				Token gov = tokenIndex.get( d.gov().toString() );
				ArrayList<Dependency> cachedRoles = tokenToDependencyMap.get( gov );
				if ( cachedRoles == null  ) {
					cachedRoles = new ArrayList<Dependency>();
					tokenToDependencyMap.put( gov , cachedRoles );
				}
				cachedRoles.add( a );

				Token dep = tokenIndex.get( d.dep().toString() );
				cachedRoles = tokenToDependencyMap.get( dep );
				if ( cachedRoles == null  ) {
					cachedRoles = new ArrayList<Dependency>();
					tokenToDependencyMap.put( dep , cachedRoles );
				}
				cachedRoles.add( a );
				
				String rel = d.reln().toString();
				a.setSentence( sAnnotation );
				a.setGovernor( gov );
				a.setDependent( dep );
				a.setRelation( rel );
				a.setSource( this.getClass().getName() );
				a.setLikelihood( 1.0d );
				dependencyList.add( a );
				a.addToIndexes();
			}
			for ( Token token : tokenToDependencyMap.keySet() ) {
				ArrayList<Dependency> roles = tokenToDependencyMap.get( token );
				FSArray roleArray = new FSArray( jcas , roles.size() );
				for ( int d = 0 ; d < roles.size() ; d++ )
					roleArray.set( d ,  roles.get( d ) );
				token.setDependencies( roleArray );
			}
			FSArray dependencyArray = new FSArray( jcas , dependencyList.size() );
			for ( int j = 0 ; j < dependencyList.size(); j++ )
				dependencyArray.set( j ,  (Dependency)dependencyList.get( j ) );
			sAnnotation.setDependencies( dependencyArray );
			FSArray tokenArray = new FSArray( jcas , tokenList.size() );
			for ( int k = 0 ; k < tokenList.size(); k++ )
				tokenArray.set( k ,  (Token)tokenList.get( k ) );
			sAnnotation.setTokens( tokenArray );
			sAnnotation.addToIndexes();
		}
		Map<Integer, CorefChain> graph = qdoc.get( CorefChainAnnotation.class );
		for ( CorefChain c : graph.values() ) {
			List<Token> tokensInChain = new ArrayList<Token>();
			List<CorefChain.CorefMention> mentions = c.getCorefMentions();
			FSArray headTokenArray = new FSArray( jcas , mentions.size() );
			for ( int i = 0 ; i < mentions.size(); i++ ) {
				CorefChain.CorefMention mention = mentions.get( i );
				int headIndex = mention.headIndex;
				Integer sentIndex = mention.sentNum;
				Map<Integer,Token> anotherTokenMap = tokenMap.get( sentIndex );
				Token token = anotherTokenMap.get( headIndex );
				tokensInChain.add( token );
				headTokenArray.set( i , token );
			}
			CorefChain.CorefMention rep = c.getRepresentativeMention();
			int repIndex = rep.headIndex;
			Integer repSent = rep.sentNum;
			Map<Integer,Token> repTokenMap = tokenMap.get( repSent );
			Token repToken = repTokenMap.get( repIndex );
			for ( Token headToken : tokensInChain ) {
				headToken.setRepresentativeToken( repToken );
				headToken.setCoreferentTokens ( headTokenArray );
			}
			MentionChain mc = new MentionChain( jcas , 0 , jcas.getDocumentText().length() );
			mc.setHeadTokens( headTokenArray );
			mc.setRepresentativeToken( repToken );
			mc.addToIndexes();
		}

	}
	
	


}
