package com.salmonGUI.app.async_core;

import java.util.ArrayList;



public abstract class PostRunnableBase<E1, E2> implements Runnable {

	protected final E1 element1;
	protected final E2 element2;
	protected ArrayList<String> results;
	
	public PostRunnableBase(E1 element1, E2 element2) {
		this.element1 = element1;
		this.element2 = element2;	// secondary element, e.g. array adapter for an arrayList
	}
	
	public void setResults(ArrayList<String> results) {
		this.results = results;
	}
		
	public abstract void run();

}