package edu.cmu.lti.oaqa.bio.test.eric.passagescore;

import java.util.Properties;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class StanfordCoreNLPFactory {
	private static StanfordCoreNLP instance;
	
	private StanfordCoreNLPFactory() {}
	
	public static synchronized StanfordCoreNLP getInstance() {
		if ( instance == null ) initialize();
		return instance;
	}
	
	public static void initialize() {
		Properties props = new Properties();
		// props.setProperty( "annotators" , "tokenize, ssplit, pos, lemma, ner, parse, dcoref" );
		props.setProperty( "annotators" , "tokenize, ssplit" );
		instance = new StanfordCoreNLP( props );
	}
	
	public static Annotation annotateText( String text ) {
		Annotation a = new Annotation( text );
		try {
			getInstance().annotate( a ); 
		} catch ( NumberFormatException  e ) {
			System.out.println( "Swallowing NumberFormatException in StanfordCoreNLP" + e.getMessage() );
		}
		
		return a;
	}
	
	

}