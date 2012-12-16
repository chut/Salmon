package com.salmon.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

//import javax.swing.JTextArea;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.salmon.activities.AppPrefs;
import com.salmon.app.io.DatabaseConstants;
import com.salmon.app.io.IDatabaseProvider;
import com.salmon.app.io.extdb.HTTP_Apache;
import com.salmon.app.io.extdb.Socket;
import com.salmon.app.io.sqlite.SQLite;
import com.metrics.MetricGroup;

/**********************************************************
 * public class Route
 **********************************************************
 * description:	A Route object represents the route a user takes to 
 * 					get from point A to point B.  Each Route object contains 
 * 					(after a route has been	generated) an array of RouteSteps.
 * methods:		
 * creator:		Ken Richards, 02/11/2012
 * modified:	
 *********************************************************/
public class Route {

	// fields
	private static Route mInstance = null;
	
	private final Context context;
	private String routeID;
	private ArrayList<RouteStep> routeStepList;
	private ArrayList<Node> nodeList;
	private Node startNode;
	private Node endNode;
	private String startNodeID;
	private String endNodeID;
	private boolean verbose = false;
	private String stairsOrElevator = "elevator";	// possible values: stairs, elevator, either
	private boolean bIgnoreDeferredSetting = false;	// if set to true, it will ignore the 'stairOrElevator' setting
	
	// fields for Dijkstra algorithm in calculateRoute (no public setters and getters)
	private ArrayList<Node> settledBucket;
	private ArrayList<Node> unsettledBucket;
	private ArrayList<Node> deferredBucket;
	
	private IDatabaseProvider dbConn;
	private MetricGroup myMetrics;
	
	public static Route getInstance(Context context, String routeID, Node startNode, Node endNode) {
		if (mInstance == null) {
			mInstance = new Route(context, routeID, startNode, endNode);
		}
		return mInstance;
	}
	
	public static Route getInstance(Context context, String routeID, String startNodeID, String endNodeID, String ... settings) {
		if (mInstance == null) {
			mInstance = new Route(context, routeID, startNodeID, endNodeID, settings);
		}
		return mInstance;
	}
	
	public static Route getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new Route(context);
		}
		return mInstance;
	}
	
	// constructor 
	private Route(Context context, String routeID, Node startNode, Node endNode) {
		this.myMetrics = new MetricGroup();
		myMetrics.addMetric(routeID).setStartTime();
		
		this.context = context;
		
		// setup database connection provider
		switch (AppConstants.DATABASE_PROVIDER_ALGORITHM) {
		case AppConstants.PROVIDER_EXT_HTTP_APACHE:
			dbConn = new HTTP_Apache();
			break;
		
		case AppConstants.PROVIDER_EXT_SOCKET:
			dbConn = new Socket();
			break;
			
		case AppConstants.PROVIDER_INT_SQLITE:
			dbConn = new SQLite(context);
			//dbConn = SQLite.getInstance(context);
			Log.i("ROUTE","Provider - SQLITE");
			break;
		
		default:
			// default to SQLITE
			dbConn = new SQLite(context);
			//dbConn = SQLite.getInstance(context);
			Log.i("ROUTE","Provider - defaulting to SQLITE");
									
			break;
		}
				
		this.routeID = routeID;
		this.routeStepList = new ArrayList<RouteStep>();
		this.nodeList = new ArrayList<Node>();
		this.startNode = startNode;
		this.endNode = endNode;
		
		this.nodeList.add(startNode);
		this.nodeList.add(endNode);
	}

	// overload constructor
	private Route(Context context, String routeID, String startNodeID, String endNodeID, String ... settings) {
		this.myMetrics = new MetricGroup();
		myMetrics.addMetric(routeID).setStartTime();
		myMetrics.setDbaseProvider(getDatabaseProvider());
		
		this.context = context;
		
		// setup database connection provider
		switch (AppConstants.DATABASE_PROVIDER_ALGORITHM) {
		case AppConstants.PROVIDER_EXT_HTTP_APACHE:
			dbConn = new HTTP_Apache();
			break;
		
		case AppConstants.PROVIDER_EXT_SOCKET:
			dbConn = new Socket();
			break;
			
		case AppConstants.PROVIDER_INT_SQLITE:
			dbConn = new SQLite(context);
			//dbConn = SQLite.getInstance(context);
			Log.i("ROUTE","Provider - SQLITE");
			break;
		
		default:
			// default to SQLITE
			dbConn = new SQLite(context);
			//dbConn = SQLite.getInstance(context);
			Log.i("ROUTE","Provider - defaulting to SQLITE");
			
			break;
		}
				
		this.routeID = routeID;
		this.routeStepList = new ArrayList<RouteStep>();
		this.nodeList = new ArrayList<Node>();
		
		this.startNode = getNodeByID(startNodeID);
		if (this.startNode == null) {
			this.startNode = loadFloorNodeNeighborData(null, startNodeID);
			//this.startNode = getNodeByID(startNodeID);
		}
		
		this.endNode = getNodeByID(endNodeID);
		if (this.endNode == null) {
			this.endNode = loadFloorNodeNeighborData(null, endNodeID);
			//this.endNode = getNodeByID(endNodeID);
		}	
		
		int arrayLength = settings.length;
		if (arrayLength > 0) {
			String[] mySetting = null;
			for (int i = 0; i < arrayLength; i++) {
				mySetting = settings[i].split("=");
				if (mySetting[0].equals("verbose")) {this.verbose = mySetting[1].toLowerCase().equals("true");}
				else if (mySetting[0].equals("soe")) {this.stairsOrElevator = mySetting[1].toLowerCase();}				
			}
		}
		
	}
	
	// overload constructor
	private Route(Context context) {
		this.context = context;
		
		// initialize array lists
		this.routeStepList = new ArrayList<RouteStep>();
		this.nodeList = new ArrayList<Node>();
		
		// setup database connection provider
		//switch (AppConstants.DATABASE_PROVIDER_ALGORITHM) {
		switch (Integer.parseInt(AppPrefs.getAlogrithmDatabaseProvider(context))) {
		case AppConstants.PROVIDER_EXT_HTTP_APACHE:
			dbConn = new HTTP_Apache();
			Log.i("ROUTE","Provider - HTTP_Apache");
			break;
		
		case AppConstants.PROVIDER_EXT_SOCKET:
			dbConn = new Socket();
			Log.i("ROUTE","Provider - Socket");
			break;
		
		case AppConstants.PROVIDER_INT_SQLITE:
			dbConn = new SQLite(context);
			//dbConn = SQLite.getInstance(context);
			Log.i("ROUTE","Provider - SQLITE");
			break;
		
		default:
			// default to SQLITE
			dbConn = new SQLite(context);
			//dbConn = SQLite.getInstance(context);
			Log.i("ROUTE","Provider - defaulting to SQLITE");
						
			break;
		}
	}
	
	// this is a low cost method, meant to be run from the GUI thread
	public void setup(String routeID, String startNodeID, String endNodeID, String ... settings) {
		this.routeID = routeID;
		this.startNodeID = startNodeID;
		this.endNodeID = endNodeID;
		
		int arrayLength = settings.length;
		if (arrayLength > 0) {
			String[] mySetting = null;
			for (int i = 0; i < arrayLength; i++) {
				mySetting = settings[i].split("=");
				if (mySetting[0].equals("verbose")) {this.verbose = mySetting[1].toLowerCase().equals("true");}
				else if (mySetting[0].equals("soe")) {this.stairsOrElevator = mySetting[1].toLowerCase();}				
			}
		}
	}
	
	// this is a relatively high cost method.  
	// call this method from the same thread that Route.calculate() will run on
	// i.e. Don't call this method from the GUI thread
	public Route initialize() {
		this.myMetrics = new MetricGroup();
		myMetrics.addMetric(routeID).setStartTime();
		myMetrics.setDbaseProvider(getDatabaseProvider());
		
		// initialize array lists
		this.routeStepList = new ArrayList<RouteStep>();
				
		// convert the start/end string IDs into Node objects
		this.startNode = getNodeByID(startNodeID);
		if (this.startNode == null) {
			this.startNode = loadFloorNodeNeighborData(null, startNodeID);
			//this.startNode = getNodeByID(startNodeID);
		}
		
		Log.i("ROUTE","nodeList size - after load startNode: " + this.nodeList.size());
		//logNodeList();
		
		this.endNode = getNodeByID(endNodeID);
		if (this.endNode == null) {
			this.endNode = loadFloorNodeNeighborData(null, endNodeID);
			//this.endNode = getNodeByID(endNodeID);
		}	
		
		Log.i("ROUTE","nodeList size - after load endNode: " + this.nodeList.size());
		//logNodeList();
		
		return this;
	}
	
	// setters and getters
	public void setRouteID(String routeID) {
		this.routeID = routeID;		
	}

	public String getRouteID() {
		return routeID;
	}

	public void setStartNode(String startNode) {
		this.startNode = getNodeByID(startNode);
	}

	public Node getStartNode() {
		return startNode;
	}

	public void setEndNode(String endNode) {
		this.endNode = getNodeByID(endNode);
	}

	public Node getEndNode() {
		return endNode;
	}
	
	public String getStartNodeID() {
		return startNodeID;
	}
	
	public String getEndNodeID() {
		return endNodeID;
	}
	
	public RouteStep addRouteStep(RouteStep routeStepObj) {
		this.routeStepList.add(routeStepObj);
		return routeStepObj;
	}

	public ArrayList<RouteStep> getRouteStepList() {
		return routeStepList;
	}

	public Node addNode(Node nodeObj) {
		nodeList.add(nodeObj);
		return nodeObj;
	}

	public List<Node> getNodeList() {
		return nodeList;
	}

	public Node getNodeByID(String nodeID) {
		if ((nodeList == null) || (nodeList.isEmpty())) {
			return null;
		}
		
		/*
		 * Binary search proved to be slower - using iteration search instead
		 *
//		Node tempNode = new Node(nodeID);
//		int index = Collections.binarySearch(nodeList, tempNode, nodeComparator);
//		System.out.println("nodeID: " + nodeID + ", index: " + index);
//		if (index >= 0) return nodeList.get(index);
		*/
		
		int listSize = nodeList.size();
		for (int i = 0; i < listSize; i++) {
			if (nodeList.get(i).getNodeID().equals(nodeID)) {
				return nodeList.get(i);	// match found
			}
		}
		// no matches found
		return null;
	}
	
	public void setVerbose(boolean b) {
		this.verbose = b;
	}
	
	public boolean getVerbose() {
		return verbose;
	}
	
	public void setStairsOrElevator(String stairsOrElevator) {
		this.stairsOrElevator = stairsOrElevator;
	}
	
	public String getStairsOrElevator() {
		return stairsOrElevator;
	}
	
	public String getDatabaseProvider() {
		return dbConn.getClass().getSimpleName();
//		switch (AppConstants.DATABASE_PROVIDER_ALGORITHM) {
//		case AppConstants.PROVIDER_EXT_HTTP_APACHE:
//			
//			return "HTTP_Apache";
//		
//		case AppConstants.PROVIDER_EXT_SOCKET:
//			return "Socket";
//		
//		case AppConstants.PROVIDER_INT_SQLITE:
//			return "SQLITE";
//		
//		default:
//			return "unknown";
//		}
		//return IDatabaseProvider.class.getCanonicalName();
	}
	
	public void setDatabaseProvider(int provider) {
		// setup database connection provider
		switch (provider) {
		case AppConstants.PROVIDER_EXT_HTTP_APACHE:
			dbConn = new HTTP_Apache();
			break;
		
		case AppConstants.PROVIDER_EXT_SOCKET:
			dbConn = new Socket();
			break;
			
		case AppConstants.PROVIDER_INT_SQLITE:
			dbConn = new SQLite(context);
			//dbConn = SQLite.getInstance(context);
			Log.i("ROUTE","Provider - SQLITE");
			break;
		
		default:
			// default to SQLITE
			dbConn = new SQLite(context);
			//dbConn = SQLite.getInstance(context);
			Log.i("ROUTE","Provider - defaulting to SQLITE");
			
			break;
		}
	}
	
	public MetricGroup getMyMetrics() {
		return myMetrics;
	}
		
	/**********************************************************
	 * public int calculateRoute()
	 **********************************************************
	 * returns: 	0 - success, route generated
	 * 				1 - error - startNode, endNode, or nodeList == null
	 * 				2 - error - startNode == endNode
	 * 				3 - error - nodeList is empty
	 * 				4 - error - routeStepList has less than two steps (start and end)
	 * 				5 - error - first routeStep != startNode and last routeStep != endNode
	 * requires:	this.startNode, this.endNode, this.nodeList
	 * effects:		this.routeStepList
	 * modifies:	none
	 * calls:		private void relaxNeighbors(Node myNode)
	 * 				private Node extractMinimum()
	 * description:	1. gather and verify all required data
	 * 				2. initialize Dijkstra algorithm
	 * 				3. perform algorithm to find shortest path
	 * 				   (based on pseudo-code from 
	 * 				   http://renaud.waldura.com/doc/java/dijkstra/)
	 * 				4. generate the RouteSteps in routeStepList
	 * 				5. return result code
	 * creator:		Ken Richards, 11/12/2011
	 * modified:	02/12/2012 - Ken Richards - adapted from
	 * 					previous project into this project
	 *********************************************************/
	public int calculateRoute() {
		Log.i("ROUTE","calculateRoute - begin");
		// verify required fields are not NULL
		if((startNode == null) || (endNode == null) || (nodeList == null)) {
			//System.out.println("startNode, endNode, or nodeList");
			return 1;	// error - startNode, endNode, or nodeList == null
		}
		
//		System.out.println("\nstartNode: " + startNode.getNodeID() + ", Building: " + startNode.getBuildingID() + ", Floor: " + startNode.getFloorID() + ", mapImg: " + startNode.getMapImg() + ", photoImg: " + startNode.getPhotoImg() + ", X: " + startNode.getX() + ", Y: " + startNode.getY());
//		System.out.println("endNode: " + endNode.getNodeID() + ", Building: " + endNode.getBuildingID() + ", Floor: " + endNode.getFloorID() + ", mapImg: " + endNode.getMapImg() + ", photoImg: " + endNode.getPhotoImg() + ", X: " + endNode.getX() + ", Y: " + endNode.getY());
//		System.out.println("nodeList.size() = " + nodeList.size());
//		Log.i("ROUTE", "startNode: " + startNode.getNodeID() + ", Building: " + startNode.getBuildingID() + ", Floor: " + startNode.getFloorID() + ", mapImg: " + startNode.getMapImg() + ", photoImg: " + startNode.getPhotoImg() + ", X: " + startNode.getX() + ", Y: " + startNode.getY());
//		Log.i("ROUTE", "endNode: " + endNode.getNodeID() + ", Building: " + endNode.getBuildingID() + ", Floor: " + endNode.getFloorID() + ", mapImg: " + endNode.getMapImg() + ", photoImg: " + endNode.getPhotoImg() + ", X: " + endNode.getX() + ", Y: " + endNode.getY());
//		Log.i("ROUTE", "nodeList.size() = " + nodeList.size());
		
		// verify startNode is NOT the same as endNode
		if(startNode.getNodeID().equals(endNode.getNodeID())) {
			//System.out.println("required data did not verify");
			return 2;	// error - startNode == endNode
		}
		
		// verify nodeList is not empty
		if (nodeList.size() == 0) {
			return 3;	// error - nodeList is empty
		}
		
		// initialize algorithm
		settledBucket = new ArrayList<Node>();	// S
		unsettledBucket = new ArrayList<Node>();	// Q
		deferredBucket = new ArrayList<Node>();
		Node currentNode = startNode;	// u 
		boolean endNodeSettled = false;
		
		// set shortestDist for each grid point to insanely high number
		// set predecessor to null for each grid point
		for (int i = 0; i < nodeList.size(); i++) {
			nodeList.get(i).setShortestDist(999999999);		// d
			nodeList.get(i).setPredecessor(null);			// n
		}
		
		// add startNode to unsettledBucket and set its shortestDist to 0
		unsettledBucket.add(startNode);
		startNode.setShortestDist(0);
		
		//for testing - print out contents nodeList
//		for (Node iN : nodeList) {
//			System.out.println("Node: " + iN.getNodeID() + "; shortestDist: " + iN.getShortestDist() + "; predecessor: " + iN.getPredecessor());
//		}
		
//		System.out.println("verbose: " + verbose);
//		System.out.println("stairsOrElevator: " + stairsOrElevator);
//		System.out.println("bIgnoreDeferredSetting: " + bIgnoreDeferredSetting);
//		System.out.println("Initialization done");
		Log.i("ROUTE", "startNode: " + startNode.getNodeID());
		Log.i("ROUTE", "endNode: " + endNode.getNodeID());
		Log.i("ROUTE", "verbose: " + verbose);
		Log.i("ROUTE", "stairsOrElevator: " + stairsOrElevator);
		Log.i("ROUTE", "bIgnoreDeferredSetting: " + bIgnoreDeferredSetting);
		Log.i("ROUTE", "Initialization done");
		
		/*
		 * List of algorithm optimizations
		 * 
		 * OPTIMIZATION			description
		 * endNode settled		algorithm loop will terminate when endNode has been settled
		 * 						this prevents the algorithm from processing extra nodes
		 * 
		 */
		
		
		
		// perform algorithm to find shortest path
		//System.out.println("\n***Begin algorithm*** \n");
		Log.i("ROUTE", "***Begin algorithm***");
		while ((unsettledBucket.size() != 0) && (!endNodeSettled)) {	// OPTIMIZATION ** endNode settled
			// 
			//System.out.print(".");
			
			myMetrics.getMetricsByID(routeID).addAlgorithmLoop();
			
			//System.out.println("Top of Loop");
			currentNode = extractMinimum(currentNode);
			settledBucket.add(currentNode);
			
			if (currentNode.getNodeID().equals(endNode.getNodeID())) {
				//System.out.println("\nendNode has been settled");	
				endNodeSettled = true;	// OPTIMIZATION ** endNode settled
			} else {
				relaxNeighbors(currentNode);
			}
			
			// if unsettledBucket is empty, and endNode is not settled, check deferredBucket
			// and extract connector Node with shortest distance
			if ((unsettledBucket.size() == 0) && (!endNodeSettled)) {	// FEATURE ** stairs or elevator
				unsettledBucket.add(extractMinimumDeferred());
			}
			
		}
		
		//System.out.println("\n----Out of algorithm loop--- \n \n");
		Log.i("ROUTE", "----Out of algorithm loop---");
		
		// for testing - print out contents of settledBucket to console
		// test_printBuckets();
		
		
		
		// assemble route steps, add directional text, route numbers, remove nodes when traveling straight down hall, etc
		createRouteStepList();
		determineDirectionalText();
		if (!verbose) determineNavPoints();	
		generateStepText();
		
		
//		// add step numbers
//		for (int i = 1; i <= this.getRouteStepList().size(); i++) {
//			this.getRouteStepList().get(i-1).setStepText(i + ". " + this.getRouteStepList().get(i-1).getStepText());
//		}
		
		// verify routeStepList has two or more routeSteps (startLoc, endLoc)
		if (this.routeStepList.size() < 2) {
			return 4; 	// error, routeList has less than two steps (start and end)
		}
		
		// verify startLoc is at start, endLoc is at end
		int iLast = this.routeStepList.size() - 1;
		if (!(this.routeStepList.get(0).getStepNode().getNodeID().equals(this.startNode.getNodeID())) || !(this.routeStepList.get(iLast).getStepNode().getNodeID().equals(this.endNode.getNodeID()))) {
			return 5;  // error, first routeStep != startLoc and last routeStep != endLoc
		}
		
		dbConn.close();
		
		myMetrics.getMetricsByID(routeID).setLoadedNodes(nodeList.size());
		myMetrics.getMetricsByID(routeID).setEndTime();
		return 0;
	}
	
	/**********************************************************
	 * private void relaxNeighbors(Node myNode)
	 **********************************************************
	 * returns: 	none
	 * requires:	myNode - the GridPoint object we are currently working on
	 * 				this.settledBucket ArrayList
	 * 				this.unsettledBucket ArrayList	
	 * effects:		none
	 * modifies:	this method may modify:	
	 * 				this.unsettledBucket ArrayList
	 * 				Node.shortestDist
	 * 				Node.predecessor
	 * calls:		none
	 * description:	this method is used by calculateRoute()
	 * 				1. Inspect neighbor Nodes of myNode that are not already in settledBucket
	 * 				2. For each neighbor Node who's shortest distance value is
	 * 					greater than myNode's shortest distance plus distance to neighbor
	 * 					2.1 Set the neighbor's shortest distance to myNode's shortest distance plus distance to neighbor
	 * 					2.2 Set the neighbor's predecessor Node to myNode
	 * 					2.3 Add the neighbor Node to unsettledBucket
	 * creator: 	Ken Richards, 11/12/2011
	 * modified:	02/12/2012 - Ken Richards - adapted from
	 * 					previous project into this project
	 *********************************************************/
	private void relaxNeighbors(Node myNode) {
		//System.out.println("\n-----------\nBegin relaxNeighbors\n-----------\n");
		//test_printBuckets();
		
		// for each neighbor Node of myNode
		//System.out.println("before - myNode.neighborList: "	+ myNode.getNeighborList().size());
		//System.out.println("before - route.nodeList: " + nodeList.size());
		//generateNeighborList(myNode);
		
		// if myNode doesn't have any neighbors, then load data from database
		if (myNode.getNeighborList().size() == 0) {
			//System.out.println(myNode.getNodeID() + " neighborList is empty - database call");
			loadFloorNodeNeighborData(myNode);
		}
		//System.out.println("after - myNode.neighborList: "	+ myNode.getNeighborList().size());
		//System.out.println("after - route.nodeList: " + nodeList.size());
		
		for (int i = 0; i < myNode.getNeighborList().size(); i++) {
			//System.out.println("n1 nodeID: " + n1.getNode().getNodeID() + ", shortestDist: " + n1.getNode().getShortestDist() + ", distance: " + n1.getDistance());
			
			// TODO - create settledBucket and unsettledBucket inner-classes?
			// find neighbor Node in settledBucket
			int inSettled = -1;
			for (int j = 0; j < settledBucket.size(); j++) {
				if (myNode.getNeighborList().get(i).getNode().getNodeID().equals(settledBucket.get(j).getNodeID())) {
					inSettled = settledBucket.indexOf(settledBucket.get(j));
				}
			}
			
			// TODO ** same floor
			// if endNode is on same floor as myNode 
			if ((endNode.getBuildingID().equals(myNode.getBuildingID())) && (endNode.getFloorID().equals(myNode.getFloorID()))) {
				// if neighbor is not on same floor, flag it as settled so algorithm ignores it
				if (!(myNode.getNeighborList().get(i).getNode().getBuildingID().equals(myNode.getBuildingID())) || !(myNode.getNeighborList().get(i).getNode().getFloorID().equals(myNode.getFloorID()))) {
					inSettled = 999;  //inSettled is not -1, so algorithm will ignore Neighbor n1
				}
			}
			
			// if neighbor is not in settledBucket
			if (inSettled == -1) {
				//System.out.println("   " + n1.getNode().getNodeID()	+ " is not in settledBucket");
				// if a shorter distance exists to neighbor
				int neighborDistance = calculateDistance(myNode.getX(), myNode.getNeighborList().get(i).getNode().getX(), myNode.getY(), myNode.getNeighborList().get(i).getNode().getY());
				myNode.getNeighborList().get(i).setDistance(neighborDistance);
				//Log.i("ROUTE","Node: " + myNode.getNodeID() + ", Neighbor: " + myNode.getNeighborList().get(i).getNodeID() + ", distance: " + neighborDistance);
				if (myNode.getNeighborList().get(i).getNode().getShortestDist() > (myNode.getShortestDist() + neighborDistance)) {
				//if (myNode.getNeighborList().get(i).getNode().getShortestDist() > (myNode.getShortestDist() + myNode.getNeighborList().get(i).getDistance())) {

					// set the new shortestDist to neighbor
					myNode.getNeighborList().get(i).getNode().setShortestDist((myNode.getShortestDist() + neighborDistance));
					
					// set the new predecessor of neighbor to currPoint (myNode)
					myNode.getNeighborList().get(i).getNode().setPredecessor(myNode);
					
					// check to see if neighbor Node is already in unsettledBucket
					int inUnsettled = -1;
					for (int j = 0; j < unsettledBucket.size(); j++) {
						if (myNode.getNeighborList().get(i).getNode().getNodeID().equals(unsettledBucket.get(j).getNodeID())) {
							inUnsettled = unsettledBucket.indexOf(unsettledBucket.get(j));
						}
					}
					
					// add neighbor Node to unsettledBucket, if doesn't already exist
					if (inUnsettled == -1) {
						//System.out.println("   " + n1.getNode().getNodeID()	+ " is not in unsettledBucket");
						unsettledBucket.add(myNode.getNeighborList().get(i).getNode());
					}
					
				}	// end - if a shorter distance exists to neighbor
			}	// end - if neighbor is not in settledBucket
				
		}	// end -for each neighbor GridPoint of uG
		
		//System.out.println("--Out of relaxNeighbors \n");
		//test_printBuckets();
	}
	
	
	/**********************************************************
	 * private void loadFloorNodeNeighborData(Node myNode)
	 **********************************************************
	 * returns: 	none
	 * requires:	myNode (type Node)
	 * 				this.nodeList	
	 * effects:		none
	 * modifies:	creates Node objects
	 * 				Creates Neighbor objects
	 * 				assigns Neighbor objects to each Node object created
	 * calls:		DBaseInterface.getDataFromDatabase()
	 * description:	this method is used by relaxNeighbors()
	 * 				1. initialize local variables
	 * 				2. retrieve all Node/Neighbor data on same floor as myNode from database
	 * 				3. load/create all nodes from database results
	 * 				4. check each Node retrieved from database
	 * 					3.1. load/create all Neighbors for that Node
	 * creator:		Ken Richards, 2/13/2012
	 * modified:	2/17/2012 - Ken Richards - method now loads all Nodes/Neighbors on
	 * 					same floor as myNode (it used to only load Neighbors of myNode)
	 *********************************************************/
	public Node loadFloorNodeNeighborData(Node myNode, String ... myID) {
		Log.i("ROUTE","loadFloorNodeNeighborData - begin");
		//long methodStart = System.currentTimeMillis();
		
		// initialize local variables
		long starttime = 0;
		final int NEIGHBORS = DatabaseConstants.QUERY_NEIGHBORS;
		final int BLDG_FLR = DatabaseConstants.QUERY_BLDG_FLR_BY_NODEID;
		//Cursor cursor = null;
		final ArrayList<String> myResultsList = new ArrayList<String>();
		ArrayList<Node> nodesToBeAdded = new ArrayList<Node>();
		ArrayList<NodeNeighborMap> nodeNeighborList = new ArrayList<NodeNeighborMap>();
		String[] fieldsList = null;
		//String[] rowNode = null;
		String lastNode = "";
		String strNodeID = "xxx";
		Node thisNode = null;
		Node neighborNode = null;
		
		// retrieve list of all Nodes / Neighbors on same floor as myNode from database
		int d1 = -1;
		if (myNode == null) {
			// string IDs were passed - either buildingID and floorID, or nodeID
//			if (myMetrics != null) {d1 = myMetrics.getMetricsByID(routeID).addDatabaseCall();}
//			if (myMetrics != null) {myMetrics.getMetricsByID(routeID).getDatabaseCalls().get(d1).setStartTime();}
//			
//			dbConn.getDataFromDatabase(NEIGHBORS, myID);
//			
//			if (myMetrics != null) {myMetrics.getMetricsByID(routeID).getDatabaseCalls().get(d1).setEndTime();}
			
			if (myID.length == 1) {
				Log.i("ROUTE","get data by nodeID");
				strNodeID = myID[0];
				
				// obtaining data from database by nodeID is a very costly SQL query
				// so we will first obtain buildingID, floorID that is associated  
				// with nodeID from database
//				if (myMetrics != null) {d1 = myMetrics.getMetricsByID(routeID).addDatabaseCall();}
//				if (myMetrics != null) {myMetrics.getMetricsByID(routeID).getDatabaseCalls().get(d1).setStartTime();}
//				
//				dbConn.getDataFromDatabase(BLDG_FLR, myID);
//				
//				if (myMetrics != null) {myMetrics.getMetricsByID(routeID).getDatabaseCalls().get(d1).setEndTime();}
//				
//				// update myNodeID array to include buildingID and floorID
//				dbConn.getCursor().moveToFirst();
//				myID = new String[2];		
//				myID[0] = dbConn.getCursor().getString(0);	
//				myID[1] = dbConn.getCursor().getString(1);
								
				// obtain data from database using buildingID and floorID (much faster SQL query)				
				if (myMetrics != null) {d1 = myMetrics.getMetricsByID(routeID).addDatabaseCall();}
				if (myMetrics != null) {myMetrics.getMetricsByID(routeID).getDatabaseCalls().get(d1).setStartTime();}
				
				myResultsList.addAll(dbConn.submitQuery(NEIGHBORS, myID).getData());
				
				if (myMetrics != null) {myMetrics.getMetricsByID(routeID).getDatabaseCalls().get(d1).setEndTime();}
			} else {
				Log.i("ROUTE","get data by buildingID and floorID");
				if (myMetrics != null) {d1 = myMetrics.getMetricsByID(routeID).addDatabaseCall();}
				if (myMetrics != null) {myMetrics.getMetricsByID(routeID).getDatabaseCalls().get(d1).setStartTime();}
				
				myResultsList.addAll(dbConn.submitQuery(NEIGHBORS, myID).getData());
				
				if (myMetrics != null) {myMetrics.getMetricsByID(routeID).getDatabaseCalls().get(d1).setEndTime();}
			}
			
			
		} else {
			// node object was passed
			Log.i("ROUTE","get data by Node object");
			myID = new String[2];
			myID[0] = myNode.getBuildingID();
			myID[1] = myNode.getFloorID();
			if (myMetrics != null) {d1 = myMetrics.getMetricsByID(routeID).addDatabaseCall();}
			if (myMetrics != null) {myMetrics.getMetricsByID(routeID).getDatabaseCalls().get(d1).setStartTime();}
			
			myResultsList.addAll(dbConn.submitQuery(NEIGHBORS, myID).getData());
			
			if (myMetrics != null) {myMetrics.getMetricsByID(routeID).getDatabaseCalls().get(d1).setEndTime();}
			
		}
		Log.i("ROUTE","nodes obtained from database - next, load into java");
		//Log.i("ROUTE","myResultsList.size: " + myResultsList.size());
		
//		if (dbConn.getCursor() != null) {
//			if (dbConn.getCursor().getCount() > 0) {
		//if (myResultsList != null) {
			if (myResultsList.size() > 0) {
				// sort the results
				//Collections.sort(myResultsList);
				
				String cursor_NodeID;	 				
				String cursor_nodeLabel;				
				String cursor_typeName;					
				String cursor_photoImg;				
				int cursor_x;							
				int cursor_y;							
				boolean cursor_isConnector;	
				boolean cursor_isPOI;			
				String cursor_poiIconImg;			
				String cursor_buildingID;				
				//String cursor_buildingName;			
				String cursor_floorID;				
				int cursor_floorLevel;					
				String cursor_mapImg;				
				String cursor_neighborNode;			
				int cursor_distance;				
				
				// load all nodes on floor from resultList
				starttime = System.currentTimeMillis();				
//				while (dbConn.getCursor().moveToNext()) {
				for (int i = 0; i < myResultsList.size(); i++) {
					
					//Log.i("ROUTE","loading...");
//					cursor_NodeID = dbConn.getCursor().getString(0);	 				// columnIndex 0 = nodeID
//					cursor_nodeLabel = dbConn.getCursor().getString(1);					// columnIndex 1 = nodeLabel
//					cursor_typeName = dbConn.getCursor().getString(2);					// columnIndex 2 = typeName
//					cursor_photoImg = dbConn.getCursor().getString(3);					// columnIndex 3 = photoImg
//					cursor_x = dbConn.getCursor().getInt(4);							// columnIndex 4 = x
//					cursor_y = dbConn.getCursor().getInt(5);							// columnIndex 5 = y
//					cursor_isConnector = dbConn.getCursor().getString(6).equals("1");	// columnIndex 6 = isConnector
//					cursor_isPOI = dbConn.getCursor().getString(7).equals("1");			// columnIndex 7 = isPOI
//					cursor_poiIconImg = dbConn.getCursor().getString(8);				// columnIndex 8 = poiIconImg
//					cursor_buildingID = dbConn.getCursor().getString(9);				// columnIndex 9 = buildingID
//					//cursor_buildingName =dbConn.getCursor().getString(10);				// columnIndex 10 = buildingName
//					cursor_floorID = dbConn.getCursor().getString(11);					// columnIndex 11 = floorID
//					cursor_floorLevel = dbConn.getCursor().getInt(12);					// columnIndex 12 = floorLevel
//					cursor_mapImg = dbConn.getCursor().getString(13);					// columnIndex 13 = mapImg
//					cursor_neighborNode = dbConn.getCursor().getString(14);				// columnIndex 14 = neighborNode
//					cursor_distance = dbConn.getCursor().getInt(15);					// columnIndex 15 = distance
					
//					cursor_NodeID = dbConn.getCursor().getString(0);	 				// columnIndex 0 = nodeID
//					cursor_typeName = dbConn.getCursor().getString(1);					// columnIndex 1 = typeName
//					cursor_isConnector = dbConn.getCursor().getString(2).equals("1");	// columnIndex 2 = isConnector
//					cursor_buildingID = dbConn.getCursor().getString(3);				// columnIndex 3 = buildingID
//					cursor_floorID = dbConn.getCursor().getString(4);					// columnIndex 4 = floorID
//					cursor_neighborNode = dbConn.getCursor().getString(5);				// columnIndex 5 = neighborNode
//					cursor_distance = dbConn.getCursor().getInt(6);						// columnIndex 6 = distance
					
					//Log.i("ROUTE","before split: " + myResultsList.get(i));
					fieldsList = myResultsList.get(i).split(",");
					cursor_NodeID = fieldsList[0];	 									// columnIndex 0 = nodeID
					cursor_nodeLabel = fieldsList[1];									// columnIndex 1 = nodeLabel
					cursor_typeName = fieldsList[2];									// columnIndex 2 = typeName
					cursor_photoImg = fieldsList[3];									// columnIndex 3 = photoImg
					cursor_x = Integer.parseInt(fieldsList[4]);							// columnIndex 4 = x
					cursor_y = Integer.parseInt(fieldsList[5]);							// columnIndex 5 = y
					cursor_isConnector = fieldsList[6].equals("1");						// columnIndex 6 = isConnector
					cursor_isPOI = fieldsList[7].equals("1");							// columnIndex 7 = isPOI
					cursor_poiIconImg = fieldsList[8];									// columnIndex 8 = poiIconImg
					cursor_buildingID = fieldsList[9];									// columnIndex 9 = buildingID
					//cursor_buildingName = fieldsList[10];								// columnIndex 10 = buildingName
					cursor_floorID = fieldsList[11];									// columnIndex 11 = floorID
					cursor_floorLevel = Integer.parseInt(fieldsList[12]);				// columnIndex 12 = floorLevel
					cursor_mapImg = fieldsList[13];										// columnIndex 13 = mapImg
					cursor_neighborNode = fieldsList[14];								// columnIndex 14 = neighborNode
					cursor_distance = Integer.parseInt(fieldsList[15]);					// columnIndex 15 = distance
					
					// TODO fix - ext database columnIndexes will be different
				
					// since resultList is sorted (SQL-side ORDER BY), we can skip duplicate Nodes
					if (!lastNode.equals(cursor_NodeID)) {
						// create the node to be added (note: nodeID is fieldList[0], NOT fieldList[1].  This is because the query results treat the neighbor as the full node object, and the nodeID as the ID of the neighbor (backwards, ya I know)
						thisNode = new Node(cursor_NodeID,
											cursor_nodeLabel,
											cursor_buildingID,
											cursor_floorID,
											cursor_floorLevel,
											cursor_typeName,
											cursor_isConnector,
											cursor_mapImg,
											cursor_photoImg,
											cursor_x,
											cursor_y,
											cursor_isPOI,
											cursor_poiIconImg);
//						thisNode = new Node(cursor_NodeID,
//											null,	//nodeLabel					
//											cursor_buildingID,
//											cursor_floorID,
//											-1, 	//floorLevel
//											cursor_typeName,
//											cursor_isConnector,
//											null,	//mapImg
//											null,	//photoImg
//											-1,		//x
//											-1,		//y
//											false,	//isPOI
//											null);	//poiIconImg
						
						// if Node is not a connector, then add it
						if (!cursor_isConnector) {
							nodesToBeAdded.add(thisNode);
							lastNode = cursor_NodeID;
						} else {
							// we need to check to see if the connector node already exists in nodeList
							// TODO Node connNode = getNodeByID(fieldsList[0]);
							if (getNodeByID(cursor_NodeID) == null) {
								nodesToBeAdded.add(thisNode);
								lastNode = cursor_NodeID;
							} else {
								thisNode = getNodeByID(cursor_NodeID);
							}
						}
					}
					
					// make sure myNode has a handle on the current dijkstra node
					if (myNode == null) {
						if (strNodeID.equals(cursor_NodeID)) myNode = thisNode;
					}
						
					// add to node/neighbor temporary list
					nodeNeighborList.add(new NodeNeighborMap(thisNode, cursor_neighborNode, cursor_distance));
					
				}
				Log.i("MICRO","loaded nodes: " + (System.currentTimeMillis() - starttime) + " milliseconds");
				
				nodeList.addAll(nodesToBeAdded);
				//Collections.sort(nodeList, nodeComparator);
				
				// check to make sure myNode is not null.  if it is, find any node on the floor we're loading and assign it to myNode
				if (myNode == null) {
					int index = 0;
					while (myNode == null && index < nodeList.size()) {
						if (nodeList.get(index).getBuildingID().equals(myID[0]) && nodeList.get(index).getFloorID().equals(myID[1])) myNode = nodeList.get(index);
						index ++;
					}
				}
				
				// load Neighbor data from resultList
				// for each node in nodeNeighborList, add it's neighbor
				for (int i = 0; i < nodeNeighborList.size(); i++) {
					// only check Nodes that are on the same floor as myNode
					if ((myNode.getBuildingID().equals(nodeNeighborList.get(i).node.getBuildingID())) && (myNode.getFloorID().equals(nodeNeighborList.get(i).node.getFloorID()))) {
						
						// if the Neighbor does not exist, add it
						if (nodeNeighborList.get(i).node.getNeighborByNodeID(nodeNeighborList.get(i).neighbor) == null) {
							neighborNode = getNodeByID(nodeNeighborList.get(i).neighbor);
							
							nodeNeighborList.get(i).node.addNeighbor(neighborNode, nodeNeighborList.get(i).distance);
						}
						
					}
					
				}
				
			} else {	// myNode's floor has no nodes / neighbors (shouldn't happen)
				System.out.println("ERROR - generateFloorNodeNeighborData() generated an empty recordSet for Node: " + myNode.getNodeID() + ", Bldg: " + myNode.getBuildingID() + ", Floor: " + myNode.getFloorID());
			}
//		} else {  // myResultsList == null (shouldn't happen)
//			System.out.println("ERROR - generateFloorNodeNeighborData() generated an null recordSet for Node: " + myNode.getNodeID() + ", Bldg: " + myNode.getBuildingID() + ", Floor: " + myNode.getFloorID());
//		}
		
		//Log.i("MICRO","load: " + (System.currentTimeMillis() - methodStart));
		return myNode;
	}
	
	
	/**********************************************************
	 * private Node extractMinimum()
	 **********************************************************
	 * returns: 	a Node object that has the smallest value of
	 * 					Node.shortestDist within the unsettledBucket
	 * requires:	this.unsettledBucket ArrayList	
	 * effects:		none
	 * modifies:	removes a Node from this.unsettledBucket
	 * calls:		none
	 * description:	this method is used by calculateRoute()
	 * 				1. initialize local variables
	 * 				2. find Node with smallest shortestDist value
	 * 				3. remove the Node from unsettledBucket
	 * 				4. return the Node
	 * creator:		Ken Richards, 11/12/2011
	 * modified:	02/12/2012 - Ken Richards - adapted from
	 * 					previous project into this project
	 *********************************************************/
	private Node extractMinimum(Node currentDijkstraNode) {
//		System.out.println("\n-----------\nBegin extractMinimum\n-----------\n");
//		System.out.println("extractMinimum - currentNode: " + currentDijkstraNode.getNodeID());
//		test_printBuckets();
		
		// initialize local variables
		Node shortNode = null;
		Node shortNodeTieBreaker = null;
		boolean foundEndFloor = false;
		int iShortest = 999999999; //unsettledBucket.get(0).getShortestDist();
		
		// find Node with smallest shortestDist
		ListIterator<Node> litr = unsettledBucket.listIterator();
		
		while (litr.hasNext() && !foundEndFloor) {
			Node loopNode = litr.next();

			// if endNode is on a different floor than currentDijkstraNode
			if (!(currentDijkstraNode.getBuildingID().equals(endNode.getBuildingID())) || !(currentDijkstraNode.getFloorID().equals(endNode.getFloorID()))) {
				
				// if currentDijkstraNode is a connector
				if (currentDijkstraNode.getIsConnector()) {
					
					// if loopNode is on same floor as endNode
					if ((loopNode.getBuildingID().equals(endNode.getBuildingID())) && (loopNode.getFloorID().equals(endNode.getFloorID()))) {
						
						// TODO ** smart node floor check
						shortNode = loopNode;
						foundEndFloor = true;
					
					} else {
						// loopNode is NOT on same floor as endNode
						
						// this is where we would handle finding floor near endNode
						// for now, just handle this as a regular node
						
						if (loopNode.getShortestDist() <= iShortest) {
							iShortest = loopNode.getShortestDist();
							shortNode = loopNode;
						}
						
					}
						
				} else {
					// currentDijkstraNode is NOT a connector
					
					if (loopNode.getShortestDist() <= iShortest) {
						
						// if loopNode is a connector
						if (loopNode.getIsConnector()) {
							
							// TODO ** weighted connector nodes
							// TODO ** stairs or elevator
							if (!stairsOrElevator.equals("either") && !currentDijkstraNode.getNodeID().equals(startNode.getNodeID()) && !bIgnoreDeferredSetting) {
								//System.out.println("stairsOrElevator: " + stairsOrElevator);
								if (stairsOrElevator.equals("stairs")) {
									if (loopNode.getNodeType().equals("stairs")) {
										iShortest = loopNode.getShortestDist();
										shortNodeTieBreaker = loopNode;
										shortNode = loopNode;
									} else {
										// connector node is not stairs, remove it from unsettledBucket and add it to deferredBucket
										addNodeToDeferred(loopNode);
										litr.remove();
									}
									
								} else if (stairsOrElevator.equals("elevator")) {
									if (loopNode.getNodeType().equals("elevator")) {
										iShortest = loopNode.getShortestDist();
										shortNodeTieBreaker = loopNode;
										shortNode = loopNode;
									} else {
										// connector node is not elevator, remove it from unsettledBucket and add it to deferredBucket
										addNodeToDeferred(loopNode);
										litr.remove();
									}
									
								} else {
									// stairsOrElevator is set to something it should not
									// so act as if this setting is set to "either"
									iShortest = loopNode.getShortestDist();
									shortNodeTieBreaker = loopNode;
									shortNode = loopNode;
								}
							} else {
								// stairsOrElevator is set to "either", or bIgnoreDeferredSetting == true
								iShortest = loopNode.getShortestDist();
								shortNodeTieBreaker = loopNode;
								shortNode = loopNode;
							}
							
						} else {
							// loopNode is NOT a connector
							iShortest = loopNode.getShortestDist();
							shortNode = loopNode;
							
						}
						
					}	// end (loopNode.getShortestDist() <= iShortest)
					
				}	
				
			} else {
				// endNode is on same floor as currentDijkstraNode
				
				// if loopNode is NOT on same floor as endNode
				if ((!loopNode.getBuildingID().equals(endNode.getBuildingID())) || (!loopNode.getFloorID().equals(endNode.getFloorID()))) {
					
					// TODO ** same floor only (defer nodes on other floors)
					addNodeToDeferred(loopNode);
					litr.remove();
					
				} else {
					// loopNode is on same floor as endNode
					
					if (loopNode.getShortestDist() <= iShortest) {
						
						// if loopNode == endNode
						if (loopNode.getNodeID().equals(endNode.getNodeID())) {
							
							// TODO ** weighted endNode
							iShortest = loopNode.getShortestDist();
							shortNodeTieBreaker = loopNode;
							shortNode = loopNode;
							
						} else {
							// loopNode is NOT endNode
							iShortest = loopNode.getShortestDist();
							shortNode = loopNode;
						}
						
					}	// (loopNode.getShortestDist() <= iShortest)
					
				}
				
			}
			
		}	// end while (litr.hasNext())
		
		// if there is a tie between shortest distance, shortNodeTieBreaker wins
		if (shortNodeTieBreaker != null && !foundEndFloor) {
			if (shortNode.getShortestDist() == shortNodeTieBreaker.getShortestDist()) {
				shortNode = shortNodeTieBreaker;
			}
		}
		

		
		// if we needed to ignore any settings that would defer a connector node, we can turn those settings back on if we have reached a non-connector node
		if (bIgnoreDeferredSetting && !shortNode.getIsConnector()) {
			//System.out.println("shortNode: " + shortNode.getNodeID() + " - isConnector: " + shortNode.getIsConnector() + " - reseting bIgnoreDeferredSetting to false");
			bIgnoreDeferredSetting = false;	
		}
		
		// remove the Node from unsettledBucket and return it
		//System.out.println("extracting: " + shortNode.getNodeID());
		unsettledBucket.remove(shortNode);
		
		//System.out.println("--Out of relaxNeighbors \n");
		//test_printBuckets();		
		return shortNode;
	}
	
	private Node extractMinimumDeferred() {
		//System.out.println("\n-----------\nBegin extractMinimumDeferred\n-----------\n");
		//test_printBuckets();
		
		// return null if deferredBucket is empty
		if (deferredBucket.size() == 0) return null;
		
		// initialize local variables
		Node shortNode = null;
		int iShortest = 999999999; 
		
		// find Node with smallest shortestDist
		for (int i = 0; i < deferredBucket.size(); i++) {
			//System.out.println("bucket: " + n1.getNodeID() + ", shortestDist: " + n1.getShortestDist() + ", isConnector: " + n1.getIsConnector());
			if (deferredBucket.get(i).getShortestDist() <= iShortest) {
				iShortest = deferredBucket.get(i).getShortestDist();
				shortNode = deferredBucket.get(i);
			}
		}
		
		//System.out.println("extracting: " + shortNode.getNodeID());
		bIgnoreDeferredSetting = true;
		deferredBucket.remove(shortNode);
		return shortNode;
	}
	
	private void addNodeToDeferred(Node n1) {
		// check to see if Node is already in deferredBucket
		int inDeferred = -1;
		for (int i = 0; i < deferredBucket.size(); i++) {
			
		//}
		//for (Node dN : deferredBucket) {
			if (n1.getNodeID().equals(deferredBucket.get(i).getNodeID())) {
				inDeferred = deferredBucket.indexOf(deferredBucket.get(i));
			}
		}
		
		// add neighbor Node to deferredBucket, if doesn't already exist
		if (inDeferred == -1) {
			//System.out.println("   " + n1.getNodeID()	+ " is not in deferredBucket");
			//System.out.println("deferring: " + n1.getNodeID());
			deferredBucket.add(n1);
		}
	}
	
	// for testing 
	/**********************************************************
	 * private void test_printBuckets()
	 **********************************************************
	 *description:	FOR TESTING
	 * creator:		Ken Richards, 11/12/2011
	 * modified:	02/12/2012 - Ken Richards - adapted from
	 * 					previous project into this project
	 *********************************************************/
	private void test_printBuckets() {
		String testLabel;
		System.out.println("*******************");
		System.out.println("*  settledBucket  *");
		System.out.println("*******************");
		for (int i = 0; i < settledBucket.size(); i++) {
			if (settledBucket.get(i).getPredecessor() == null) {
				testLabel = "null";
			} else {
				testLabel = settledBucket.get(i).getPredecessor().getNodeID();
			}
			System.out.println("* Node: " + settledBucket.get(i).getNodeID() + "; shortestDist: " + settledBucket.get(i).getShortestDist() + "; predecessor: " + testLabel);
		}
		System.out.println("********************\n");
		
		System.out.println("*********************");
		System.out.println("*  unsettledBucket  *");
		System.out.println("*********************");
		for (int i = 0; i < unsettledBucket.size(); i++) {
			if (unsettledBucket.get(i).getPredecessor() == null) {
				testLabel = "null";
			} else {
				testLabel = unsettledBucket.get(i).getPredecessor().getNodeID();
			}
			System.out.println("* Node: " + unsettledBucket.get(i).getNodeID() + "; shortestDist: " + unsettledBucket.get(i).getShortestDist() + "; predecessor: " + testLabel);
		}
		System.out.println("*********************\n");
		
		System.out.println("*********************");
		System.out.println("*  deferredBucket  *");
		System.out.println("*********************");
		for (int i = 0; i < deferredBucket.size(); i++) {
			if (deferredBucket.get(i).getPredecessor() == null) {
				testLabel = "null";
			} else {
				testLabel = deferredBucket.get(i).getPredecessor().getNodeID();
			}
			System.out.println("* Node: " + deferredBucket.get(i).getNodeID() + "; shortestDist: " + deferredBucket.get(i).getShortestDist() + "; predecessor: " + testLabel);
		}
		System.out.println("*********************\n");
	}
	
	private void createRouteStepList() {
		// generate the RouteSteps in routeStepList by starting with endNode,
		// and tracing back to startNode through predecessor
		
		// initialize local variables needed during process
		Node neighborNode = null;
		Node currentNode = null;
		
		long startTime = System.currentTimeMillis();
		
		// begin with endNode
		// create route step - add Node, direction text
		Log.i("RSTEP","get route step info");
//		int d1 = -1;
//		String[] myID = new String[1];
//		myID[0] = endNode.getNodeID();
//		if (myMetrics != null) {d1 = myMetrics.getMetricsByID(routeID).addDatabaseCall();}
//		if (myMetrics != null) {myMetrics.getMetricsByID(routeID).getDatabaseCalls().get(d1).setStartTime();}
//		
//		dbConn.getDataFromDatabase(DatabaseConstants.QUERY_ROUTESTEP_BY_NODEID, myID);
//		
//		if (myMetrics != null) {myMetrics.getMetricsByID(routeID).getDatabaseCalls().get(d1).setEndTime();}
//		
//		dbConn.getCursor().moveToFirst();
//		endNode.setNodeLabel(dbConn.getCursor().getString(0));			// columnIndex 0 = nodeLabel
//		endNode.setPhotoImg(dbConn.getCursor().getString(1));			// columnIndex 1 = photoImg
//		endNode.setX(dbConn.getCursor().getInt(2));						// columnIndex 2 = x
//		endNode.setY(dbConn.getCursor().getInt(3));						// columnIndex 3 = y
//		endNode.setIsPOI(dbConn.getCursor().getString(4).equals("1"));	// columnIndex 4 = isPOI
//		endNode.setPoiIconImg(dbConn.getCursor().getString(5));			// columnIndex 5 = poiIconImg
//		//endNode.setNodeLabel(dbConn.getCursor().getString(6));		// columnIndex 6 = buildingName
//		endNode.setFloorLevel(dbConn.getCursor().getInt(7));			// columnIndex 7 = floorLevel
//		endNode.setMapImg(dbConn.getCursor().getString(8));				// columnIndex 8 = mapImg
				
		addRouteStep(new RouteStep(endNode, "node: " + endNode.getNodeID(),-1));	// we can't put this in the while loop because we need to hardcode the -1 value
		
		// find next step
		neighborNode = endNode;
		currentNode = endNode.getPredecessor();
		
		// add routeSteps
		// if currPoint is null, we have reached the start location
		while (currentNode != null) {
//			myID[0] = currentNode.getNodeID();
//			if (myMetrics != null) {d1 = myMetrics.getMetricsByID(routeID).addDatabaseCall();}
//			if (myMetrics != null) {myMetrics.getMetricsByID(routeID).getDatabaseCalls().get(d1).setStartTime();}
//			
//			dbConn.getDataFromDatabase(DatabaseConstants.QUERY_ROUTESTEP_BY_NODEID, myID);
//			
//			if (myMetrics != null) {myMetrics.getMetricsByID(routeID).getDatabaseCalls().get(d1).setEndTime();}
//			
//			dbConn.getCursor().moveToFirst();
//			currentNode.setNodeLabel(dbConn.getCursor().getString(0));			// columnIndex 0 = nodeLabel
//			currentNode.setPhotoImg(dbConn.getCursor().getString(1));			// columnIndex 1 = photoImg
//			currentNode.setX(dbConn.getCursor().getInt(2));						// columnIndex 2 = x
//			currentNode.setY(dbConn.getCursor().getInt(3));						// columnIndex 3 = y
//			currentNode.setIsPOI(dbConn.getCursor().getString(4).equals("1"));	// columnIndex 4 = isPOI
//			currentNode.setPoiIconImg(dbConn.getCursor().getString(5));			// columnIndex 5 = poiIconImg
//			//currentNode.setNodeLabel(dbConn.getCursor().getString(6));		// columnIndex 6 = buildingName
//			currentNode.setFloorLevel(dbConn.getCursor().getInt(7));			// columnIndex 7 = floorLevel
//			currentNode.setMapImg(dbConn.getCursor().getString(8));				// columnIndex 8 = mapImg
		
			addRouteStep(new RouteStep(currentNode,"node: " + currentNode.getNodeID(), currentNode.getNeighborByNode(neighborNode).getDistance()));
			neighborNode = currentNode;
			currentNode = currentNode.getPredecessor();
		}
		
		// reverse the order of routeStepList, so starLoc is at beginning
		Collections.reverse(routeStepList);
		
		Log.i("MICRO","createRouteStepList took: " + (System.currentTimeMillis() - startTime) + " milliseconds");
	}
	
	private void determineDirectionalText() {
		Node prevNode = null;
		Node currNode = null;
		Node nextNode = null;
		
		// generate directional text for all steps
		for (int i = 0; i < routeStepList.size(); i++) {
			if (i == 0) {
				prevNode = null;
			} else {
				prevNode = routeStepList.get(i-1).getStepNode();
			}
			
			currNode = routeStepList.get(i).getStepNode();
			
			if (i == (routeStepList.size() -1)) {
				nextNode = null;
			} else {
				nextNode = routeStepList.get(i+1).getStepNode();
			}
			
			routeStepList.get(i).setDirectionalText(generateDirectionalText(prevNode, currNode, nextNode));
		}
	}
	
	private void determineNavPoints() {
		// not all steps on the list are needed
		// i.e. we don't need to display a step for each node next to a room while traveling straight down a hall
		int myDistance = -1;
		boolean bFirstSkip = true;
		int indexOfFirstSkip = -1;
		
		// all steps by default have isNavPoint == true
		// check all steps (other than 1st and last) to see if it should be set to false
		for (int i = 1; i < (routeStepList.size() - 1); i++) {
			
			// if current step == hall/path, and next step is a hall/path or intersection, and previous step is hall/path or intersection --> then set isNavPoint = false
			if ((routeStepList.get(i).getStepNode().getNodeType().equals("hall") || routeStepList.get(i).getStepNode().getNodeType().equals("path")) && ((routeStepList.get(i+1).getStepNode().getNodeType().equals("hall")) || routeStepList.get(i+1).getStepNode().getNodeType().equals("path") || routeStepList.get(i+1).getStepNode().getNodeType().equals("intersection")) && ((routeStepList.get(i-1).getStepNode().getNodeType().equals("hall")) || (routeStepList.get(i-1).getStepNode().getNodeType().equals("path")) || routeStepList.get(i-1).getStepNode().getNodeType().equals("intersection"))) {
				routeStepList.get(i).setIsNavPoint(false);
				
				// if this is the first skip, then mark which index, and initialize the distance counter
				if (bFirstSkip) {
					bFirstSkip = false;
					indexOfFirstSkip = i;
					myDistance = routeStepList.get(i).getDistanceToNextStep();
				} else {
					// this is not the first skip in a sequential set of skips, but keep track of the distance traveled so far
					myDistance = myDistance + routeStepList.get(i).getDistanceToNextStep();
				}
			} else {
				// this step is a valid navigation point
				
				// if we have skipped any steps, then reset bFirstSkip = true, and add the distance we've been keeping track of to the step before our first skipped step
				if (!bFirstSkip) {
					bFirstSkip = true;
					routeStepList.get(indexOfFirstSkip - 1).setDistanceToNextStep(routeStepList.get(indexOfFirstSkip - 1).getDistanceToNextStep() + myDistance);
				}
			}
		}
	}
	
	private void generateStepText() {
		String myText = "";
		String nodeType = "";
		
		// format start node text
		if (!routeStepList.get(0).getStepNode().getIsConnector()) {
			// start node is not a connector node
			
			nodeType = routeStepList.get(0).getStepNode().getNodeType();
			if (nodeType.equals("hall") || nodeType.equals("intersection") || nodeType.equals("stairs")) {
				// start node is a hall type (stairs that are not connectors are considered hall types)
				
				// what is the next node type?
				nodeType = routeStepList.get(1).getStepNode().getNodeType();
				if (nodeType.equals("hall") || nodeType.equals("intersection")) {
					// next node is a hall type
					myText = "Walk down the hall";
					
				} else if (nodeType.equals("room") || nodeType.equals("office") || nodeType.equals("bathroom") || nodeType.equals("cafeteria")) {
					// next node is a room type
					myText = "Enter " + routeStepList.get(1).getStepNode().getNodeLabel();
					
				} else if (nodeType.equals("stairs") || nodeType.equals("elevator") || nodeType.equals("exit")) {
					// next node is a connector type
					myText = "Goto the " + routeStepList.get(1).getStepNode().getNodeLabel();
					
				} else {
					// catch all (should not get here)
					myText = "Start at your current location";
				}
				
				
			} else if (nodeType.equals("room") || nodeType.equals("office") || nodeType.equals("bathroom") || nodeType.equals("cafeteria")){
				// start node is a room type
				
				// what is the next node type?
				nodeType = routeStepList.get(1).getStepNode().getNodeType();
				if (nodeType.equals("hall") || nodeType.equals("intersection")) {
					// next node is a hall type
					myText = "Exit " + routeStepList.get(0).getStepNode().getNodeLabel() + " into the hall";
					
				} else if (nodeType.equals("room") || nodeType.equals("office") || nodeType.equals("bathroom") || nodeType.equals("cafeteria")) {
					// next node is a room type
					myText = "Enter " + routeStepList.get(1).getStepNode().getNodeLabel();
					
				} else if (nodeType.equals("stairs") || nodeType.equals("elevator") || nodeType.equals("exit")) {
					// next node is a connector type
					myText = "Goto the " + routeStepList.get(1).getStepNode().getNodeLabel();
					
				} else {
					// catch all (should not get here)
					myText = "Start at your current location";
				}
			} else {
				// catch all (should not get here)
				myText = "Start at your current location";
			}
			
			
		} else {
			// start node is a connector node
			
			// what is the next node type?
			if (routeStepList.get(1).getStepNode().getIsConnector()) {
				// next node is a connector, we are changing floors
				myText = "At " + routeStepList.get(0).getStepNode().getNodeLabel() + ", go " + routeStepList.get(0).getDirectionalText();
				
			} else {
				// next node is not a connector
				
				// so what type of node is it?
				nodeType = routeStepList.get(1).getStepNode().getNodeType();
				if (nodeType.equals("hall") || nodeType.equals("intersection")) {
					// next node is a hall type
					myText = "Exit " + routeStepList.get(0).getStepNode().getNodeLabel() + " into the hall";
					
				} else if (nodeType.equals("room") || nodeType.equals("office") || nodeType.equals("bathroom") || nodeType.equals("cafeteria")) {
					// next node is a room type
					myText = "Exit " + routeStepList.get(0).getStepNode().getNodeLabel() + " into " + routeStepList.get(1).getStepNode().getNodeLabel();
					
				} else {
					// catch all (should not get here)
					myText = "Start at your current location";
				}
			}
		}
		routeStepList.get(0).setStepText(myText);
		
		// format end node text
		routeStepList.get(routeStepList.size() - 1).setStepText("You have reached your destination: " + routeStepList.get(routeStepList.size() - 1).getStepNode().getNodeLabel());
		
		// format all other nodes in route
		for (int i = 1; i < (routeStepList.size() - 1); i++) {
			
			if (!routeStepList.get(i).getStepNode().getIsConnector()) {
				// this node is not a connector node
				
				nodeType = routeStepList.get(i).getStepNode().getNodeType();
				if (nodeType.equals("hall") || nodeType.equals("stairs")) {
					// this node is a hall type (stairs that are not connectors are considered hall types)
					
					// what is the next node type?
					nodeType = routeStepList.get(i+1).getStepNode().getNodeType();
					if (nodeType.equals("hall") || nodeType.equals("intersection")) {
						// next node is a hall type
						if (routeStepList.get(i).getDirectionalText().equals("go straight")) {
							myText = "Continue straight down hall";
						} else {
							myText = capitalizeFirstLetter(routeStepList.get(i).getDirectionalText()) + " and continue down hall";
						}

					} else if (nodeType.equals("room") || nodeType.equals("office") || nodeType.equals("bathroom") || nodeType.equals("cafeteria") || nodeType.equals("stairs") || nodeType.equals("elevator") || nodeType.equals("exit")) {
						// next node is a room type, or a connector type
						if (routeStepList.get(i).getDirectionalText().equals("go straight")) {
							myText = capitalizeFirstLetter(routeStepList.get(i+1).getStepNode().getNodeLabel()) + " will be straight ahead";
						} else if (routeStepList.get(i).getDirectionalText().matches(".*left.*")) {
							myText = capitalizeFirstLetter(routeStepList.get(i+1).getStepNode().getNodeLabel()) + " will be on the left";
						} else if (routeStepList.get(i).getDirectionalText().matches(".*right.*")) {
							myText = capitalizeFirstLetter(routeStepList.get(i+1).getStepNode().getNodeLabel()) + " will be on the right";
						} else {
							// catch all (should not get here)
							myText = "Enter " + routeStepList.get(i+1).getStepNode().getNodeLabel();
						}
						
					} else {
						// catch all (should not get here)
						myText = "Continue following the route";
					}
				
				} else if (nodeType.equals("intersection")) {
					// this node is a hall intersection
					
					// what is the next node type?
					nodeType = routeStepList.get(i+1).getStepNode().getNodeType();
					if (nodeType.equals("hall") || nodeType.equals("intersection")) {
						// next node is a hall type
						if (routeStepList.get(i).getDirectionalText().equals("go straight")) {
							myText = "At hall intersection, " + routeStepList.get(i).getDirectionalText();
						} else {
							myText = "At hall intersection, " + routeStepList.get(i).getDirectionalText() + " and continue down hall";
						}

					} else if (nodeType.equals("room") || nodeType.equals("office") || nodeType.equals("bathroom") || nodeType.equals("cafeteria") || nodeType.equals("stairs") || nodeType.equals("elevator") || nodeType.equals("exit")) {
						// next node is a room type, or a connector type
						if (routeStepList.get(i).getDirectionalText().equals("go straight")) {
							myText = "At hall intersection, " + routeStepList.get(i+1).getStepNode().getNodeLabel() + " will be straight ahead";
						} else if (routeStepList.get(i).getDirectionalText().matches(".*left.*")) {
							myText = "At hall intersection, " + routeStepList.get(i+1).getStepNode().getNodeLabel() + " will be on the left";
						} else if (routeStepList.get(i).getDirectionalText().matches(".*right.*")) {
							myText = "At hall intersection, " + routeStepList.get(i+1).getStepNode().getNodeLabel() + " will be on the right";
						} else {
							// catch all (should not get here)
							myText = "At hall intersection, enter " + routeStepList.get(i+1).getStepNode().getNodeLabel();
						}
						
					} else {
						// catch all (should not get here)
						myText = "At hall intersection, continue following the route";
					}
					
				} else if (nodeType.equals("room") || nodeType.equals("office") || nodeType.equals("bathroom") || nodeType.equals("cafeteria")){
					// this node is a room type
					
					// what is the next node type?
					nodeType = routeStepList.get(i+1).getStepNode().getNodeType();
					if (nodeType.equals("hall") || nodeType.equals("intersection")) {
						// next node is a hall type
						if (routeStepList.get(i).getDirectionalText().equals("go straight")) {
							myText = "In " + routeStepList.get(i).getStepNode().getNodeLabel() + ", take the exit straight ahead into the hall";
						} else if (routeStepList.get(i).getDirectionalText().matches(".*left.*")) {
							myText = "In " + routeStepList.get(i).getStepNode().getNodeLabel() + ", take the exit on your left into the hall";
						} else if (routeStepList.get(i).getDirectionalText().matches(".*right.*")) {
							myText = "In " + routeStepList.get(i).getStepNode().getNodeLabel() + ", take the exit on your right into the hall";
						} else {
							// catch all (should not get here)
							myText = "Exit " + routeStepList.get(i).getStepNode().getNodeLabel() + " into the hall";
						}						
						
					} else if (nodeType.equals("room") || nodeType.equals("office") || nodeType.equals("bathroom") || nodeType.equals("cafeteria") || nodeType.equals("stairs") || nodeType.equals("elevator") || nodeType.equals("exit")) {
						// next node is a room type, or a connector type
						if (routeStepList.get(i).getDirectionalText().equals("go straight")) {
							myText = "In " + routeStepList.get(i).getStepNode().getNodeLabel() + ", " + routeStepList.get(i+1).getStepNode().getNodeLabel() + " will be straight ahead";
						} else if (routeStepList.get(i).getDirectionalText().matches(".*left.*")) {
							myText = "In " + routeStepList.get(i).getStepNode().getNodeLabel() + ", " + routeStepList.get(i+1).getStepNode().getNodeLabel() + " will be on your left";
						} else if (routeStepList.get(i).getDirectionalText().matches(".*right.*")) {
							myText = "In " + routeStepList.get(i).getStepNode().getNodeLabel() + ", " + routeStepList.get(i+1).getStepNode().getNodeLabel() + " will be on your right";
						} else {
							// catch all (should not get here)
							myText = "In " + routeStepList.get(i).getStepNode().getNodeLabel() + ", you will find " + routeStepList.get(i+1).getStepNode().getNodeLabel();
						}						
										
					} else {
						// catch all (should not get here)
						myText = "In " + routeStepList.get(i).getStepNode().getNodeLabel() + ", continue following the route";
					}
				} else {
					// catch all (should not get here)
					myText = "Continue following the route";
				}
								
			} else {
				// this node is a connector
				
				// what is the next node type?
				if (routeStepList.get(i+1).getStepNode().getIsConnector()) {
					// next node is a connector, we are changing floors
					myText = "At " + routeStepList.get(i).getStepNode().getNodeLabel() + ", go " + routeStepList.get(i).getDirectionalText();
					
				} else {
					// next node is not a connector
					
					// so what type of node is it?
					nodeType = routeStepList.get(i+1).getStepNode().getNodeType();
					if (nodeType.equals("hall") || nodeType.equals("intersection")) {
						// next node is a hall type
						myText = "Exit " + routeStepList.get(i).getStepNode().getNodeLabel() + " into the hall";
						
					} else if (nodeType.equals("room") || nodeType.equals("office") || nodeType.equals("bathroom") || nodeType.equals("cafeteria")) {
						// next node is a room type
						myText = "Exit " + routeStepList.get(i).getStepNode().getNodeLabel() + " into " + routeStepList.get(i+1).getStepNode().getNodeLabel();
						
					} else {
						// catch all (should not get here)
						myText = "Continue following the route";
					}
				}
				
				
			}
			
			routeStepList.get(i).setStepText(myText);
		}
		
		
		
	}
	
	private String generateDirectionalText(Node prevNode, Node currNode, Node nextNode) {
		double myAngle = 0;
		String myText = "";
		
		// check for null nodes
		if (currNode == null || nextNode == null) return "null node";
		
		// 1st, check to see if we are changing floors
		if (currNode.getIsConnector() && nextNode.getIsConnector()) {
			// we are changing floors, check which direction
			if (currNode.getFloorLevel() < nextNode.getFloorLevel()) {
				// we are going up
				myText = "up to " + nextNode.getFloorID();
			} else if (currNode.getFloorLevel() > nextNode.getFloorLevel()){
				// we are going down
				myText = "down to " + nextNode.getFloorID();
			} else {
				// we are staying on same floor
				// this may happen if two buildings are connected by a hall
				// or it may happen if we are exiting a building
				
			}
		} else if (prevNode != null) {
			// we needed to check for null, because we might be on the start node
		
			if (currNode.getIsConnector() && prevNode.getIsConnector()){
				// we have just changed floors, and are now staying on same floor, eg. exiting stairs into hall
				// or this may happen if two buildings are connected by a hall
				// or it may happen if we are exiting a building
				//myText = "exit " + currNode.getNodeType();
				
			} else {
				// we are not changing floors
				
				//calculate angle of turn
				double angle1 = Math.atan2((prevNode.getY() - currNode.getY()), (prevNode.getX() - currNode.getX()));
				//System.out.println("    angle1: " + Math.toDegrees(angle1) + ", (prevY - currY), (prevX - currX): (" + (prevNode.getY() - currNode.getY()) + ", " + (prevNode.getX() - currNode.getX()) + ")");
				double angle2 = Math.atan2((nextNode.getY() - currNode.getY()), (nextNode.getX() - currNode.getX()));
				//System.out.println("    angle2: " + Math.toDegrees(angle2) + ", (nextY - currY), (nextX - currX): (" + (nextNode.getY() - currNode.getY()) + ", " + (nextNode.getX() - currNode.getX()) + ")");
				
				// angles are in radians - convert to degrees
				myAngle = Math.toDegrees((angle1 - angle2));
				
				// myAngle can be negative or positive
				// add 360 to negative results to standardize on positive results
				if (myAngle < 0 ) myAngle = myAngle + 360;
							
				// generate directional text from angle
				if ((0 < myAngle) && (myAngle <= 55)) myText = "turn hard right";
				if ((55 < myAngle) && (myAngle <= 125)) myText = "turn right";
				if ((125 < myAngle) && (myAngle <= 165)) myText = "bear right";
				if ((165 < myAngle) && (myAngle < 195)) myText = "go straight";
				if ((195 <= myAngle) && (myAngle < 235)) myText = "bear left";
				if ((235 <= myAngle) && (myAngle < 305)) myText = "turn left";
				if ((305 <= myAngle) && (myAngle < 360)) myText = "turn hard left";
			}
		} else {
			// prevNode is null, when it should not be
			return "prevNode is null";
		}
		
		return myText;
	}
	
	private String capitalizeFirstLetter(String string) {
		String firstLetter = string.substring(0, 1).toUpperCase();
		string = firstLetter + string.substring(1);
		return string;
	}
	
	private int calculateDistance(int x1, int x2, int y1, int y2) {
		// use pythagorean theorem
		// the distance between the two points is represented as the hypotenuse of the right triangle created by the two points.
				
		return (int) Math.hypot(Math.abs(x1 - x2), Math.abs(y1 - y2));
	}
	
	@Override
	public String toString() {
		String output = "";
		int stepNum = 1;
		
		output = output + "Route Steps:" + "\n";
		for (int i = 0; i < routeStepList.size(); i++) {
			if (routeStepList.get(i).getIsNavPoint()) {
				output = output + stepNum + ". " + routeStepList.get(i).getStepNode().getNodeID() + ". " + routeStepList.get(i).getStepText() + ", distance to next step: " + routeStepList.get(i).getDistanceToNextStep() + ", mapImg: " + routeStepList.get(i).getStepNode().getMapImg() + ", floorLevel: " + routeStepList.get(i).getStepNode().getFloorLevel() + ", nodeType: " + routeStepList.get(i).getStepNode().getNodeType() + ", photoImg: " + routeStepList.get(i).getStepNode().getPhotoImg() + ", X: " + routeStepList.get(i).getStepNode().getX() + ", Y: " + routeStepList.get(i).getStepNode().getY() + ", isNavPoint: " + routeStepList.get(i).getIsNavPoint() + ", directionalText: " + routeStepList.get(i).getDirectionalText() + "\n";
				stepNum ++;
			}
		}
		return output;
	}
	
	public String printOutData() {
		String printout = "";
		for (Node node : nodeList) {
			printout = printout + "Node: " + node.getNodeID() + ", Label: " + node.getNodeLabel() + ", BldgID: " + node.getBuildingID() + ", FloorID: " + node.getFloorID() + ", Level(#): " + node.getFloorLevel() + ", Type: " + node.getNodeType() + ", X: " + node.getX() + ", Y: " + node.getY() + ", isConn: " + node.getIsConnector() + ", mapImg: " + node.getMapImg() + ", photoImg: " + node.getPhotoImg() + ", isPOI: " + node.getIsPOI() + ", POIimg: " + node.getPoiIconImg() + "\n";
			for (Neighbor neighbor : node.getNeighborList()) {
				printout = printout + "   Neighbor: " + neighbor.getNode().getNodeID() + ", Distance: " + neighbor.getDistance() + "\n";
			}
		}
		
		return printout;
	}
	
	public String printOutData(String buildingID, String floorID) {
		String printout = "";
		for (Node node : nodeList) {
			if (node.getBuildingID().equals(buildingID) && node.getFloorID().equals(floorID)) {
				printout = printout + "Node: " + node.getNodeID() + ", Label: " + node.getNodeLabel() + ", BldgID: " + node.getBuildingID() + ", FloorID: " + node.getFloorID() + ", Level(#): " + node.getFloorLevel() + ", Type: " + node.getNodeType() + ", X: " + node.getX() + ", Y: " + node.getY() + ", isConn: " + node.getIsConnector() + ", mapImg: " + node.getMapImg() + ", photoImg: " + node.getPhotoImg() + ", isPOI: " + node.getIsPOI() + ", POIimg: " + node.getPoiIconImg() + "\n";
				for (Neighbor neighbor : node.getNeighborList()) {
					printout = printout + "   Neighbor: " + neighbor.getNode().getNodeID() + ", Distance: " + neighbor.getDistance() + "\n";
				}
			}
		}
		
		return printout;
	}
	
	private void logNodeList() {
		String string;
		Log.i("ROUTE","*****nodeList.size: " + nodeList.size());
		for (Node node : nodeList) {
			string = ""
					+ "node: " + node.getNodeID() + ", "
					+ "label: " + node.getNodeLabel() + ", "
					+ "bldg: " + node.getBuildingID() + ", "
					+ "flr: " + node.getFloorID() + ", "
					+ "isConn: " + node.getIsConnector();
			Log.i("ROUTE",string);
		}
		Log.i("ROUTE","************");
	}
	
	//inner class
	public class NodeNeighborMap {
		Node node;
		String neighbor;
		int distance;
		
		public NodeNeighborMap(Node node, String neighbor, int distance) {
			this.node = node;
			this.neighbor = neighbor;
			this.distance = distance;
		}
	}
	
}
