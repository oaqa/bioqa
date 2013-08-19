package edu.cmu.lti.oaqa.bio.utils.retrieval.query.structure;

import java.util.ArrayList;

public class QueryComponentContainer {

	private ArrayList<QueryComponent> queryItems;
	private int size = 0;
	
	public QueryComponentContainer() {
		this.queryItems=new ArrayList<QueryComponent>();
	}
	
	public void add(QueryComponent queryItem){
		this.queryItems.add(queryItem);
		size++;
	}
	
	public ArrayList<QueryComponent> getQueryComponent(){
		return this.queryItems;
	}
	
	public ArrayList<QueryComponent> getConceptQueryComponent() {
	  ArrayList<QueryComponent> newList = new ArrayList<QueryComponent>();
	  for(QueryComponent q: queryItems) {
	    if(q.isConcept()) newList.add(q);
	  }
	  return newList;
	}
	
	// for the sake of testing
	public void printOut() {
	    for(QueryComponent q: queryItems) {
	        System.out.print(q.getKeyterm().getText() + " : ");
	        System.out.print(q.getWeight() + " : ");
	        System.out.print(q.getSynonyms().toString() + "\n");
	    }
	}
	
	public void printOutConceptPart() {
    for(QueryComponent q: this.getConceptQueryComponent()) {
      System.out.print(q.getKeyterm().getText() + " : ");
      System.out.print(q.getWeight() + " : ");
      System.out.print(q.getSynonyms().toString() + "\n");
    }
	}
	
	public void printOutNotConceptPart() {
	  for(QueryComponent q: queryItems) {
      if(!q.isConcept()) {
      System.out.print(q.getKeyterm().getText() + " : ");
      System.out.print(q.getWeight() + " : ");
      System.out.print(q.getSynonyms().toString() + "\n");
      }
    }
	}
	
	public int size(){
	    return size;
	}
	
	public void clear(){
	  this.queryItems.clear();
	  size = 0;
	}
	
}
