package edu.cmu.lti.oaqa.bio.test.eric.passagescore;

class PassageSpan {
	public int begin, end;
	public PassageSpan( int begin , int end ) {
		this.begin = begin;
		this.end = end;
	}
	public boolean containedIn ( int begin , int end ) {
		if ( begin <= this.begin && end >= this.end ) {
			return true;
		} else {
			return false;
		}
	}
}
