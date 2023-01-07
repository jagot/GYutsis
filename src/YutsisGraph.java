/* YutsisGraph.java
   ----------------
   2001 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium

   History: 
   - 2001/12/03 Initial draft
   - 2001/12/12 Changed name from CubicGraph to YutsisGraph
   - 2001/17/12 Dropped adjTable; removed nodes now have 
                edges[node] = {null,null,null}
   - 2002/01/03 Added methods to agree with new interface Graph.
   - 2002/01/07 Changed function of order to agree with the interface 
                graph. Order of a Yutsis Graph can be obtained by the
		method cubicOrder == order/2.
   - 2002/01/07 Removed most of the "get"'s in the get methods: e.g. 
                getNrOfNodes -> nrOfNodes...
   - 2002/01/28 Added code to determine J, a_i, b_i en S for calculating
                initial formula, adapted signatures for parseBraket
   - 2002/01/28 removed some bugs from latter code
   - 2002/01/29 inserted 3 new private methods checklabel (2) and addEdges
   - 2002/01/29 rewrote constructor en parseBraket such that YTS and BRAKET
                yield the same labeling and use latter private methods
   - 2002/02/04 inserted code such that logging can be done to every 
                outputstream and can be disabled by setting the logstream to 
		null
   - 2002/03/28 added code to store the braket notation of the original graph
   - 2002/04/28 implements new interface Yutsis, which is introduced
                to make a lighter object implementing the graph transformations
		without really generating the formula
   - 2002/04/30 added copy constructor and clone method (independant)
   - 2002/05/23 added method int [] [] triangles 
   - 2002/06/27 cleaned up code (in particular javadoc)
   - 2002/08/23 Code Refactoring: Shared code/fields between YutsisGraph and
                CubicGraph is moved to the ABSTRACT class Yutsis (was an
		interface) which implements now a StateChanged Model such
		that subclass YutsisModel becomes obsolete.
   - 2002/08/23 moved implementation of nrOfEdges to AbstractYutsis
   - 2002/08/23 moved method guessFileFormat to AbstractYutsis
   - 2002/08/30 added code checking if y != null when needed
   - 2002/09/02 added code checking if y != TD/girth high enough when needed
   - 2002/09/26 added code to ouput graph in gml-format 
   
   Bugs: 
   - 2001/13/12 Maybe better to drop adjTable...
   - 2002/01/04 Removed some bugs out of constructors: newlabels, 
                genrecoupcoeff were not constructed in some cases.
   - 2002/01/07 Removed bug in nodeIterator(int).
   - 2002/05/03 Clone method does not work like it should be: depends
                on a copy constructor to construct the clone.
   - 2002/05/07 bug in triangle: reduce f12 and then press cycles...
   - 2002/05/08 removed bug in triangle (uses nrOfNodes() in place of order())
   - 2002/05/13 copy constructor and clone method did not take in account 
                that Edge objects are shared by two entries in the Edgetable
   - 2002/05/27 removed bug from triangles
   - 2002/08/23 code in guessFileFormat probably better in abstract class
                AbstractYutsis with eventually extra code to recognize EDGELIST
*/

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/** 
 * Implementation of the abstract {@link AbstractYutsis} class
 * representing a Yutsis graph in order to generate the summation
 * formula in terms of 6j-coefficients for the general angular
 * momentum recoupling coefficient (or 3<var>n</var>j-coefficents)
 * represented by the couplings in this Yutsis graph. The main
 * function of this class is an interactive text interface which
 * allows the user to get information about the YutsisGraph and the
 * reduction process.
 * @see AbstractYutsis
 * @see NodeIterator
 * @see GenRecoupCoeff
 * @author Dries.VanDyck@rug.ac.be 
 **/
public class YutsisGraph extends AbstractYutsis{
    /*---------------- Field definitions ----------------*/

    /** 
     * Braket notation of the original YutsisGraph.
     **/
    String braket;

    /** 
     * Original order of this cubic graph (having 2n nodes and 3n edges). 
     **/
    private int order;
    
    /** 
     * Current order of this cubic graph.
     **/
    private int n;
    
    /** 
     * Array representing the signs of the nodes: <code>true</code> 
     * means '+', false '-'.
     **/
    private boolean [] nodeSign;
    
    /** 
     * Array with the edges of each node. Nodes sharing an edge also
     * share an Edge Object!.
     **/
    private Edge[][] edges;

    /** 
     * Set containing the new labels created by interchanges.
     **/
    private HashSet newlabels;

    /** 
     * Base string for new labels that arise during reduction. These
     * new labels will be <code>newlabelbase+newlabelcount</code>.
     **/
    private String newlabelbase = "z";

    /** 
     * Number of new labels already made. These new labels will be
     * <code>newlabelbase+newlabelcount</code>.
     **/
    private int newlabelcount = 1;
    
    private GenRecoupCoeff genrecoupcoeff;

    private PrintStream log = System.err;
    /*------------- Constructors -----------------*/

    /** 
     * Constructor creating a YutsisGraph from a BufferedReader in 
     * YTS format.
     * @param input The BufferedReader where the YutsisGraph is read from.
     * @throws IOException if an I/O error occurs
     **/
    public YutsisGraph(BufferedReader input) throws IOException{
	this(input,YTS);
    }

    /** 
     * Contructor creating a YutsisGraph from a BufferedReader in
     * given format.
     * @param input The BufferedReader where the YutsisGraph is read from.
     * @param format The format used (BRAKET or YTS)
     * @throws IOException if an I/O error occurs
     **/
    public YutsisGraph(BufferedReader input, int format) throws IOException{
	switch (format){
	case YTS:
	    StringTokenizer st = new StringTokenizer(input.readLine());
	    if (st.countTokens() > 1)
		st.nextToken();
	    n = order = Integer.parseInt(st.nextToken());
	    String root = null; // to please compiler
	    String [] a = new String[order-1];
	    String [] b = new String[order-1];
	    String [] s = new String[2*order];
	    HashMap edgeshm = new HashMap(4*order);
	    HashMap couplings = new HashMap(order);
	    edges = new Edge[2*order][3];
	    nodeSign = new boolean[2*order];
	    String [] label = new String[3];
	    // first tree
	    for (int node = 0; node < 2*order; node++){
		st = new StringTokenizer(input.readLine());
		for (int i = 0; i < 3; i++)
		    label[i] = checkLabel(st.nextToken());
		addEdges(node,label,node < order,"line " + (node+2),edgeshm);
		nodeSign[node] = node > order-1;
		s[node] = label[0];
		String newcoupling = "(";
		if (couplings.containsKey(label[0]))
		    newcoupling+=couplings.remove(label[0]);
		else 
		    newcoupling+=label[0];
		newcoupling+=",";
		if (couplings.containsKey(label[1]))
		    newcoupling+=couplings.remove(label[1]);
		else
		    newcoupling+=label[1];
		newcoupling+=")"+label[2];
		couplings.put(label[2],newcoupling);
		if (node < order-1){
		    a[node] = label[2];
		}
		else if (node == order-1){
		    root = label[2];
		    braket = "<" + couplings.remove(root);
		}
		else if (node < 2*order-1){
		    b[node-order] = label[2];
		}
		else if (!root.equals(label[2]))// node == 2*order-1
		    throw new IllegalArgumentException
			("Root labels from both trees differ:" + label[2] 
			 + "!=" + root);
	    }
	    braket += "|" + couplings.remove(root) + ">";
	    genrecoupcoeff = new GenRecoupCoeff(order,root,a,b,s);
	    break;
	case BRAKET:
	    String line = input.readLine();
	    parseBraket(line);
	    break;
	default:
	    throw new IllegalArgumentException("Unknown format: " + format);
	}
	newlabels = new HashSet(3*order);
    }

    /** 
     * Construcs a new YutsisGraph object from the braket in the String.
     * @param braket A string containing a Recoupling Coefficient as a BRAKET
     **/
    public YutsisGraph(String braket){
	parseBraket(braket);
	newlabels = new HashSet(3*order);
    }
    
    private void parseBraket(String braket){
	if (!intermediateLabelsPresent(braket))
	    braket = addIntermediateLabels(braket);
	StringTokenizer st = new StringTokenizer(braket,"<|>");
	String bra,ket;
	if (st.hasMoreTokens())
	    bra = st.nextToken().trim();
	else 
	    throw new IllegalArgumentException("BRA is empty (<BRA|KET>)");
	if (st.hasMoreTokens())
	    ket = st.nextToken().trim();
	else 
	    throw new IllegalArgumentException("KET is empty (<BRA|KET>)");
	if (bra.charAt(0) != '(')
	    throw new IllegalArgumentException("<\n"+bra+"| : expected '('");
	if (ket.charAt(0) != '(')
	    throw new IllegalArgumentException("<\n"+ket+"| : expected '('");
	order = 0;
	for (int i = 0; i < bra.length(); i++)
	    if (bra.charAt(i) == '(')
		order++;
	edges = new Edge[2*order][3];
	nodeSign = new boolean[2*order];
	String [] a = new String[order-1];
	String [] b = new String[order-1];
	String [] s = new String[2*order];
	for (int i = 0; i < order; i++){
	    nodeSign[i] = false;
	    nodeSign[order+i] = true;
	}
	n = 0;
	HashMap edgeshm = new HashMap(4*order);
	int pos = parseBraket(bra,0,true,edgeshm,a,s);
	String rest = bra.substring(pos).trim(); // should be the root label
	rest = checkLabel(rest,"(,)","<" + bra.substring(0,pos)+'\n'
		   + bra.substring(pos) + "|"
		   + ": no matching opening brace");
	pos = parseBraket(ket,0,false,edgeshm,b,s);
	String root = ket.substring(pos).trim(); // should be the root label
	root = checkLabel(root,"(,)","|" + ket.substring(0,pos)+'\n'
		 + ket.substring(pos) + ">"
		   + ": no matching opening brace");
	if (!root.equals(rest))
	    throw new IllegalArgumentException
		("Root labels from both trees differ:" + rest + "!=" + root);
	n = n/2;
	genrecoupcoeff = new GenRecoupCoeff(order,root,a,b,s);
	this.braket = braket.trim();
    }

    private int parseBraket(String braket, int pos, boolean bra, 
			    HashMap edgeshm, String [] ab, String [] s){
	pos++;
	pos = skipSpaces(braket,pos);
	if (braket.charAt(pos) == '(')
	    pos = parseBraket(braket,pos,bra,edgeshm,ab,s);
	int nextpos = braket.indexOf(',',pos);
	if (nextpos == -1)
	    throw new IllegalArgumentException
		((bra ? "<" : "|") + braket.substring(0,pos)+'\n'
		 + braket.substring(pos) + (bra ? "|" : ">")
		 + ": expected label followed by ','");
	String [] label = new String[3]; 
	label[0] = braket.substring(pos,nextpos).trim();
	label[0] = checkLabel(label[0],"()", (bra ? "<" : "|") 
			      + braket.substring(0,pos)
			      +'\n'+ braket.substring(pos) + (bra ? "|" : ">")
			      + ": expected label followed by ','");
	pos = ++nextpos;
	pos = skipSpaces(braket,pos);
	if (braket.charAt(pos) == '(')
	    pos = parseBraket(braket,pos,bra,edgeshm,ab,s);
	nextpos = braket.indexOf(')',pos);
	if (nextpos == -1)
	    throw new IllegalArgumentException
		((bra ? "<" : "|") + braket.substring(0,pos)+'\n'
		 + braket.substring(pos) + (bra ? "|" : ">")
		 + ": expected label followed by ')'");
	label[1] = braket.substring(pos,nextpos).trim();
	label[1] = checkLabel(label[1],"(,",(bra ? "<" : "|") 
			      + braket.substring(0,pos)
			      +'\n'+ braket.substring(pos) + (bra ? "|" : ">")
			      + ": expected label followed by ')'");
	pos = ++nextpos; // just behind ')'
	pos = skipSpaces(braket,pos);
	while (nextpos < braket.length() && braket.charAt(nextpos) != ')' 
	       && braket.charAt(nextpos) != ',')
	    nextpos++;
	label[2] = braket.substring(pos,nextpos).trim();
	label[2] = checkLabel(label[2],"(",(bra ? "<" : "|") 
			      + braket.substring(0,pos)+'\n'
			      + braket.substring(pos) + (bra ? "|" : ">") 
			      + ": expected label followed by ',' or ')'");
	addEdges(n,label,bra,(bra ? "<" : "|") + braket.substring(0,pos)+'\n'
		 + braket.substring(pos) + (bra ? "|" : ">"),edgeshm);
	s[n] = label[0]; //first coupled node
	if (n != order-1 && n != 2*order-1)
	    ab[n < order ? n : n-order] = label[2];
	n++;
	return pos;
    }

    private void addEdges(int node, String [] label, boolean bra, String msg,
			  HashMap edgeshm){
	for (int i = 0; i < 2; i++){
	    edges[node][i] = (Edge) edgeshm.get(label[i]);
	    if (edges[node][i] == null){
		edges[node][i] = bra ? new Edge(label[i],node,-1) 
		    : new Edge(label[i],-1,node);
		edgeshm.put(label[i],edges[node][i]);
	    }
	    else if ((bra ? edges[node][i].tail() 
		      : edges[node][i].head()) != -1)
		throw new 
		    IllegalArgumentException(msg+":duplicate edge label " 
					     + label[i]);
	    else if (bra)
		edges[node][i].setTail(node) ;
	    else
		edges[node][i].setHead(node);
	}
	edges[node][2] = (Edge) edgeshm.get(label[2]);
	if (edges[node][2] == null){
	    edges[node][2] = bra ? new Edge(label[2],-1,node) 
		: new Edge(label[2],node,-1);
	    edgeshm.put(label[2],edges[node][2]);
	}
	else if ((bra ? edges[node][2].head() 
		      : edges[node][2].tail()) != -1)
	    throw new 
		IllegalArgumentException(msg+":duplicate edge label " 
					 + label[2]);
	else if (bra)
	    edges[node][2].setHead(node);
	else
	    edges[node][2].setTail(node);
    }

    private int skipSpaces(String s, int pos){
	while (s.charAt(pos) == ' ')
	    pos++;
	return pos;
    }

    private String checkLabel(String label){
	try { int test = Integer.parseInt(label); }
	catch (NumberFormatException nfe){ return label; }
	return "j"+label; // was an integer...
    }

    private String checkLabel(String label, String illegal, String message){
	if (label.length() == 0)
	    throw new IllegalArgumentException(message + "(empty label)");
	for (int i = 0; i < illegal.length(); i++)
	    if (label.indexOf(illegal.charAt(i)) != -1)
		throw new IllegalArgumentException(message);
	return checkLabel(label);
    }
    
    /** 
     * Constructs an exact copy of this YutsisGraph.
     * @param y the YutsisGraph to be duplicated
     **/
    public YutsisGraph(YutsisGraph y){
	n = y.n;
	order = y.order;
	newlabelcount = y.newlabelcount;
	// immutable: object sharing OK
	newlabelbase = y.newlabelbase;
	braket = y.braket;
	newlabels = new HashSet(newlabels);
	nodeSign = new boolean[y.nodeSign.length];
	System.arraycopy(y.nodeSign,0,nodeSign,0,nodeSign.length);
	edges = new Edge[y.edges.length][3];
	HashMap edgeshm = new HashMap(4*order());
	for (int i = 0; i < y.edges.length; i++)
	    if (!isRemoved(i))
		for (int j = 0; j < 3; j++){
		    Edge e = (Edge) edgeshm.get(y.edges[i][j].label());
		    if (e == null){
			edges[i][j] = (Edge) y.edges[i][j].clone();
			edgeshm.put(edges[i][j].label(), edges[i][j]);
		    }
		    else
			edges[i][j] = e;
		}
	    else
		for (int j = 0; j < 3; j++)
		    edges[i][j] = null;	      
	genrecoupcoeff = new GenRecoupCoeff(genrecoupcoeff);
	log = y.log;
    }

    /*----------- Method definitions ----------------*/

    /*----- Implemtation of the Graph Interface -----*/
    
    /** 
     * Returns the original order (number or nodes) of this Graph as
     * specified by the {@link Graph} interface.
     * @return the order of this {@link Graph}
     * @see Graph
     **/
    public int order() { return 2*order; } 

    /** 
     * Returns the (current) number of nodes of this cubic graph as
     * specified by the {@link Graph} interface.
     * @return the number of nodes of this {@link Graph}
     * @see Graph
     **/
    public int nrOfNodes() { return 2*n;}

    /** 
     * Returns the neighbors of <code>node</code> or <code>null</code>
     * if the node is removed from the graph as specified by the
     * {@link Graph} interface.
     * @param node the node for wich the neighbors are returned
     * @return the neighbors of the node as an array
     * @see Graph
     **/
    public int [] neighbors(int node){
	if (isRemoved(node))
	    return null;
	int [] nghb = new int[3];
	for (int i = 0; i < 3; i++) 
	    nghb[i] = edges[node][i].otherNode(node);
	return nghb;
    }

    /** 
     * Class implementing the {@link NodeIterator} interface.
     * @see NodeIterator
     **/
    class YNodeIterator implements NodeIterator{
	/** 
	 * If <code>node == i</code> we see the nodeiterator as
	 * between the nodes <code>i</code> and the next node that is
	 * not removed.
	 **/
	int node = -1;
	
	YNodeIterator(){}
	
	YNodeIterator(int node){
	    this.node = node;
	}
	
	/** 
	 * Returns true if the iteration has more elements. (In other
	 * words, returns true if next would return an element rather
	 * than throwing an exception.)
	 * @return true if the iterator has more elements.
	 * @see Iterator
	 **/
	public boolean hasNext(){
	    for(int i=this.node+1;i < order(); i++)
		if (!isRemoved(i))
		    return true;
	    return false;
	}

	/** 
	 * Returns the next element in the iteration.  
	 * @return the next element in the iteration.  
	 * @throws NoSuchElementException - iteration has no more elements.
	 * @see Iterator
	 **/
	public int next(){
	    for(node++;node < order(); node++)
		if (!isRemoved(node))
		    return node;
	    throw new NoSuchElementException();
	}

	/** 
	 * Returns true if this is not the last element of the
	 * iteration. (In other words, returns true if previous would
	 * return an element rather than throwing an exception.)
	 * @return true if the iterator has more elements.
	 * @see Iterator 
	 **/
	public boolean hasPrevious(){
	    for(int i=node;i >= 0; i--)
		if (!isRemoved(i))
		    return true;
	    return false;
	}
	
	/** 
	 * Returns the previous element in the iteration.  
	 * @return the previous element in the iteration.  
	 * @throws NoSuchElementException - this is the first element.
	 * @see Iterator
	 **/
	public int previous(){
	    for(; node >= 0; node--)
		if (!isRemoved(node))
		    return node--;
	    throw new NoSuchElementException();
	}
    }

    /** 
     * Returns an {@link NodeIterator} over the nodeset starting at the node 
     * with lowest label as specified by the
     * {@link Graph} interface.
     * @return a {@link NodeIterator} pointing to the first node
     * @see NodeIterator
     * @see Graph
     **/
    public NodeIterator first(){
	return new YNodeIterator(-1);
    }

    /** 
     * Returns an {@link NodeIterator} over the nodeset starting at the node 
     * with highest label in some ordening as specified by the
     * {@link Graph} interface.
     * @return a {@link NodeIterator} pointing to the last node
     * @see NodeIterator
     * @see Graph
     **/
    public NodeIterator last(){
	return new YNodeIterator(order()-1);
    }

    /** 
     * Returns an {@link NodeIterator} which will return <code>node</code> 
     * by a {@link NodeIterator#next()} call as specified by the
     * {@link Graph} interface.
     * @param node the node to be returned by the first 
     *             {@link NodeIterator#next()} of the returned 
     * 	           {@link NodeIterator}
     * @return a {@link NodeIterator} returning <code>node</code> by the
     *         first {@link NodeIterator#next()} call.
     * @see NodeIterator
     * @see Graph
     **/
    public NodeIterator nodeIterator(int node){
	if (0 > node || node > order()-1 || isRemoved(node))
	    return null;
	for (int i = node-1; i >= 0 ; i--)
	    if (!isRemoved(i))
		return new YNodeIterator(i);
	return new YNodeIterator(node-1); // same effect as -1
    }
    
    /*---------- Basic graph operations -------------*/

    /** 
     * Return the original (cubic) order of this Cubic graph. A
     * graph having cubic order n has 2n nodes and 3n edges.
     * @return the original cubic order of the graph
     **/
    public int cubicOrder() { return order;}
    
    /** 
     * Returns the (current) (cubic) order of this cubic graph.
     * A graph having cubic order n has 2n nodes and 3n edges.
     * @return the current cubic order of the graph
     **/
    public int n(){ return n;}
        
    /** 
     * Implementation of the {@link Graph#isRemoved(int)} 
     * method.
     * @param node the node to be checked
     * @return true if the node is removed, false otherwise
     * @see Graph
     **/ 
    public boolean isRemoved(int node){ return edges[node][0] == null; }

    /** 
     * Returns (a copy) of the edges coupled in the node
     * <code>node</code>.
     * @param node the node of which edges will be returned
     * @return an array of {@link Edge}s containing a copy of the edges
     *         of the specified node
     **/
    public Edge [] edges(int node){
	Edge [] theedges = {(Edge) edges[node][0].clone(), 
			    (Edge) edges[node][1].clone(),
			    (Edge) edges[node][2].clone()};
	return theedges;
    }

    /** 
     * Returns the labels of the edges coupled in the node
     * <code>node</code>.
     * @param node the node of which the labels of its {@link Edge}s 
     *             have to be returned
     * @return the labels of the {@link Edge}s coupled in the specified node
     **/
    public String [] edgeLabels(int node){
	String [] labels = {edges[node][0].label(), 
			    edges[node][1].label(),
			    edges[node][2].label()};
	return labels;
    }

    /** 
     * Returns the edges between <code>nodes[0]</code> and 
     * <code>nodes[1]</code>(max 3) as an array of edges.
     * @param nodes an array containg the nodes of which the {@link Edge}s
     *              between them have to be returned
     * @return the {@link Edge}s connecting the specifed nodes as an 
     *         array of {@link Edge}s 
     * @see Edge
     **/
    public Edge [] edges(int [] nodes){
	return edges(nodes[0],nodes[1]);
    }

    /** 
     * Returns the edges between <code>node1</code> and 
     * <code>node2</code> (max 3) as a Vector or null if 
     * one of the nodes is removed.
     * @param node1 first node 
     * @param node2 second node
     * @return the {@link Edge}s between the specified nodes as an array
     *         of {@link Edge}s
     * @see Edge
     **/
    public Edge [] edges(int node1, int node2){
	if (isRemoved(node1) || isRemoved(node2))
	    return null;
	ArrayList theedges = new ArrayList(3);
	for (int i = 0; i < 3; i++)
	    if (edges[node1][i].otherNode(node1) == node2)
		theedges.add(edges[node1][i].clone());
	Edge [] toreturn = new Edge[theedges.size()];
	theedges.toArray(toreturn);
	return toreturn;
    }

    /*------------ Basic reduction operations ---------------*/
    
    /** 
     * Returns the sign of <code>node</code>.
     * @param node the node of which the sign has to be returned
     * @return true if the specified node is positive false otherwise
     **/
    public boolean sign(int node){ return nodeSign[node];}

    /** 
     * Reverses the sign of <code>node</code>.
     * @param node the node of which the sign has to be inverted
     **/
    public void invertSign(int node){
	// Code to generate C=(-1)^(j1+j2+j3), node = (j1,j2,j3)
	genrecoupcoeff.invertNode(edgeLabels(node));
	nodeSign[node] = !nodeSign[node];
	log("Inverted node: " + node);
    }
    
    /** 
     * Generates the phase factor (-1)^(2j), with j the label of the
     * edge with endpoint <code>node</code> and inverts the edge 
     * afterwards.
     * @param node an endpoint of the {@link Edge} to be inverted
     * @param Edge the {@link Edge} to be inverted
     * @see Edge
     **/
    public void invertEdge(int node, Edge e){
	for (int i = 0; i < 3; i++)
	    if (e.equals(edges[node][i]))
		invertEdge(edges[node][i]);
    }

    /** 
     * Directly inverts the given an {@link Edge}. Make sure this edge
     * is in the graph.
     * @param e the Edge to be inverted
     **/
    private void invertEdge(Edge e){
	// Code to generate C=(-1)^(2j), j edge label of e
	genrecoupcoeff.invertEdge(e.label());
	e.invert();
	log("Inverted edge: " + e);
    }

    /** 
     * True if this label did not appear in the original graph, if true
     * this label will yield a summation over its complete domain.
     * @param label the label which to be checked
     * @return true if the label did not appear in the original YutsisGraph,
     *         false otherwise
     * @see Edge
     **/
    public boolean isNewLabel(String label){
	return newlabels.contains(label);
    }

    /** 
     * Removes the nodes <code>node1</code> and <code>node2</code>
     * and their {@Link Edge}s.
     * @param node1 first node to be removed
     * @param node2 second node to be removed
     **/
    private void removeNodes(int node1, int node2){
	for (int i = 0; i < 3; i++){
	    edges[node1][i] = null;
	    edges[node2][i] = null;
	}
	n--;
	log("Removed nodes: " + node1 + ", " + node2);
    }

    /** 
     * Implementation of the abstract method {@link Yutsis#bubble}.
     * @return the nodes of the bubble as an array or {-1,-1} if no bubbles
     *         are present
     * @see Yutsis
     **/
    public int [] bubble(){
	int [] bubble = {-1,-1};
	for (int i = 0; i < order()-1; i++)
	    if (!isRemoved(i)){
		int [] nghb = neighbors(i);
		if (nghb[0]==nghb[1] || nghb[0] == nghb[2] ){
		    bubble[0]=i;
		    bubble[1]=nghb[0];
		    break;
		}
		else if (nghb[1] == nghb[2]){
		    bubble[0] = i;
		    bubble[1] = nghb[1];
		    break;
		}
	    }
	return bubble;
    }

    /** 
     * Implementation of the abstract {@link Yutsis#removeBubble(int
     * [])} method.
     * @param bubble the bubble to be removed.
     * @see Yutsis 
     **/
    public void removeBubble(int [] bubble){
	int [] nghb = new int[2]; 
	Edge [] bedges = new Edge[2]; 
	Edge [] nghbedges = new Edge[2];
	formatBubble(bubble,nghb,bedges,nghbedges);
	removeBubble(bubble,nghb,bedges,nghbedges);
	fireStateChanged();
    }

    /** 
     * Brings the bubble in correct form so that the bubble reduction
     * rule can be applied and fills in the last tree arguments 
     * (neighbors of the bubble, edges of the bubble, and the edges
     * outside the bubble). 
     * @param bubble array containing the nodes of the bubble
     * @param nghb the neighbors of the nodes in the bubble in the 
     *             proper order
     * @param bedges the Edges of the bubble
     * @param nghbedges the Edges to the neighbors of the bubble in the
     *                  proper order
     * @see Edge
     **/
    private void formatBubble(int [] bubble, int [] nghb, 
			      Edge [] bedges, Edge [] nghbedges){
	log("Formatting bubble: " + bubble[0] + ", " + bubble[1]);
	int nrofbedges = 0;
	for (int i = 0; i < 3; i++)
	    if (edges[bubble[0]][i].otherNode(bubble[0]) == bubble[1])
		bedges[nrofbedges++] = edges[bubble[0]][i];
	    else
		nghbedges[0] = edges[bubble[0]][i];
	
	for (int i = 0; i < 3; i++)
	    if (edges[bubble[1]][i].otherNode(bubble[1]) != bubble[0])
		nghbedges[1] = edges[bubble[1]][i];

	nghb[0] = nghbedges[0].otherNode(bubble[0]);
	nghb[1] = nghbedges[1].otherNode(bubble[1]);

	boolean invertsign = sign(bubble[0]) == sign(bubble[1]);
	boolean [] invertbedge = new boolean[2];
	invertbedge[0] = sign(bubble[0]) ? 
	    bedges[0].head() == bubble[0] : 
	    bedges[0].tail() == bubble[0];
	invertbedge[1]  = sign(bubble[0]) ? 
	    bedges[1].head() == bubble[0] : 
	    bedges[1].tail() == bubble[0];
	boolean [] invertnghbedge = new boolean[2];
	invertnghbedge[0] = sign(bubble[0]) ? 
	    nghbedges[0].head() == bubble[0]
	    : nghbedges[0].tail() == bubble[0];
	invertnghbedge[1] = sign(bubble[0]) 
	    ? nghbedges[1].tail() == bubble[1] 
	    : nghbedges[1].head() == bubble[1];
	
	if (invertsign){
	    if (invertbedge[0] && invertbedge[1]){
		invertSign(bubble[0]); 
		if (!invertnghbedge[0])
		    invertEdge(nghbedges[0]);
		if (!invertnghbedge[1])
		    invertEdge(nghbedges[1]);
	    }
	    else{
		invertSign(bubble[1]);
		if (invertbedge[0])
		    invertEdge(bedges[0]); 
		else
		    invertEdge(bedges[1]);
		if (invertnghbedge[0])
		    invertEdge(nghbedges[0]);
		if (invertnghbedge[1])
		    invertEdge(nghbedges[1]);
	    }    
	} else { // invert the edges that need to be inverted
	    if (invertbedge[0])
		invertEdge(bedges[0]);
	    if (invertbedge[1])
		invertEdge(bedges[1]);
	    if (invertnghbedge[0])
		invertEdge(nghbedges[0]);
	    if (invertnghbedge[1])
		invertEdge(nghbedges[1]);
	}
    }

    /** 
     * Does the effective removal of the bubble, assuming it is in
     * the correct form for applying the bubble removal rule.
     * @param bubble array containing the nodes of the bubble
     * @param nghb the neighbors of the nodes in the bubble in the 
     *             proper order
     * @param bedges the Edges of the bubble
     * @param nghbedges the Edges to the neighbors of the bubble in the
     *                  proper order
     * @see Edge
     **/
    private void removeBubble(int [] bubble, int [] nghb, 
			      Edge [] bedges, Edge [] nghbedges){
	// Remove edge with label which causes a summuation
	// first!
	int stay, remove;
	if (isNewLabel(nghbedges[0].label())){
	    //Code to generate C=(2*l1+1)^-1*delta(l1,l2), l1 label nghbedge[1]
	    genrecoupcoeff.bubble(nghbedges[1].label(),
				  nghbedges[0].label());
	    stay = 1;
	    remove = 0;
	}
	else {
	    //Code to generate C=(2*l1+1)^-1*delta(l1,l2), l1 label nghbedge[0]
	    genrecoupcoeff.bubble(nghbedges[0].label(),
				  nghbedges[1].label());
	    stay = 0;
	    remove = 1;
	}

	if (nghbedges[stay].tail() == nghb[stay])
	    nghbedges[stay].setHead(nghb[remove]);
	else
	    nghbedges[stay].setTail(nghb[remove]);
	for (int i = 0; i < 3; i++)
	    if (edges[nghb[remove]][i].equals(nghbedges[remove]))
		edges[nghb[remove]][i] = nghbedges[stay];
	removeNodes(bubble[0],bubble[1]);
	log("Removed bubble: " + bubble[0] + ", " + bubble[1] 
		    + " keeping edge: " + nghbedges[stay].label());
    }

    /** 
     * Implementation of the abstract {@link Yutsis#triangle} method.
     * @return the nodes of the triangle as an array or {-1,-1,-1} if
     *         no triangle are present.
     * @see Yutsis
     **/
    public int [] triangle(){
	for (int i = 0; i < order()-2; i++)
	    if (!isRemoved(i)){
		int [] nghb = neighbors(i);
		for (int j = 0; j < 2; j++){
		    int [] nghbj = neighbors(nghb[j]);
		    for (int k = 0; k < 3; k++)
			if (nghbj[k] == nghb[j+1] || nghbj[k] == nghb[(j+2)%3])
			    if (nghb[j] < nghbj[k])
				// return it like CycleGenerator
				return new int [] {i, nghb[j], nghbj[k]};
			    else 
				return new int [] {i, nghbj[k], nghb[j]};
		}
	    }
	return new int [] {-1,-1,-1};
    }

    /**
     * Implementation of the abstract {@link Yutsis#triangles} method.
     * @return all triangles as an array of int [3] objects.
     * @see Yutsis
     **/
    public int [][] triangles(){
	ArrayList triangles = new ArrayList();
	for (int i = 0; i < order()-2; i++)
	    if (!isRemoved(i)){
		int [] nghb = neighbors(i);
		for (int j = 0; j < 3; j++)
		    if (i < nghb[j]){
			int [] nghbj = neighbors(nghb[j]);
			for (int k = 0; k < 3; k++)
			    if (i < nghbj[k] && (nghbj[k] == nghb[(j+1)%3] 
				|| nghbj[k] == nghb[(j+2)%3]))
				if (nghb[j] < nghbj[k])
				    triangles.add(new int [] 
					{i, nghb[j], nghbj[k]});
		    }
	    }
	int [] [] toreturn = new int [triangles.size()][];
	triangles.toArray(toreturn);
	return toreturn;
    }

    /** 
     * Formats and removes the triangle <code>triangle[0],
     * triangle[1], triangle[2]</code>, as specified by the {@link
     * Yutsis} interface.
     * @param triangle array containing the nodes of the triangle to 
     *                 be removed 
     * @see Yutsis
     **/
    public void removeTriangle(int [] triangle){
	int [] nghb = new int[3]; 
	Edge [] tedges = new Edge[3]; 
	Edge [] nghbedges = new Edge[3];
	formatTriangle(triangle,nghb,tedges,nghbedges);
	for (int i = 0; i < 3; i++){
	    nghbedges[i].setTail(triangle[0]);
	    edges[triangle[0]][i] = nghbedges[i];
	}
	removeNodes(triangle[1],triangle[2]);
	//nodesign must be inverted, but WITHOUT formulageneration
	nodeSign[triangle[0]] = true; 
	log("Removed Triangle: " + triangle[0] + ", " +
			   triangle[1] + ", " + triangle[2]);
	//Code to generate 6j(nghbedges[2],nghbedges[0],nghbedges[1],
	// tedges[0],tedges[1],tedges[2])
	String [] l = {tedges[0].label(), tedges[1].label(), 
		       tedges[2].label()};
	String [] j = {nghbedges[2].label(), nghbedges[0].label(), 
		       nghbedges[1].label()};
	genrecoupcoeff.triangle(l,j);
	fireStateChanged();
    }
    
    /** 
     * Brings the triangle in the required form for applying the
     * triangle removal rule and fills in the last tree arguments
     * (neighbors of the triangle, edges of the triangle and the
     * edges outside the triangle).
     * @param triangle the nodes of the triangle as an array
     * @param nghb the neigbors of the nodes of the triangle in the 
     *             proper order
     * @param tedges the Edges of the triangle in the proper order
     * @param nghbedges the Edges to the neighbors of the triangle 
     *                  in the proper order
     * @see Edge
     **/
    private void formatTriangle(int [] triangle, int [] nghb,
				Edge [] tedges, Edge [] nghbedges){
	log("Formatting triangle: " + triangle[0] + ", " +
			   triangle[1] + ", " + triangle[2]);
	// Make all nodes negative
	for (int i = 0; i < 3; i++)
	    if (sign(triangle[i]))
		invertSign(triangle[i]);

	boolean [] inverttedges = new boolean[3];
	for (int i = 0; i < 3; i++)
	    if (edges[triangle[0]][i].otherNode(triangle[0]) 
		== triangle[1]){
		tedges[0] = edges[triangle[0]][i];
		inverttedges[0] = tedges[0].tail() == triangle[0];
	    }
	    else if (edges[triangle[0]][i].otherNode(triangle[0]) 
		     == triangle[2]){
		tedges[2] = edges[triangle[0]][i];
		inverttedges[2] = tedges[2].tail() == triangle[2];
	    }
	    else{
		nghbedges[0] = edges[triangle[0]][i];
		nghb[0] = nghbedges[0].otherNode(triangle[0]);
	    }

	for (int i = 0; i < 3; i++)
	    if (edges[triangle[1]][i].otherNode(triangle[1]) 
		== triangle[2]){
		tedges[1] = edges[triangle[1]][i];
		inverttedges[1] = tedges[1].tail() == triangle[1];
	    }
	    else if (edges[triangle[1]][i].otherNode(triangle[1])
		     != triangle[0]){
		nghbedges[1] = edges[triangle[1]][i];
		nghb[1] = nghbedges[1].otherNode(triangle[1]);
	    }

	for (int i = 0; i < 3; i++)
	    if (edges[triangle[2]][i].otherNode(triangle[2]) 
		  != triangle[0]
		&& edges[triangle[2]][i].otherNode(triangle[2]) 
		     != triangle[1]){
		nghbedges[2] = edges[triangle[2]][i];
		nghb[2] = nghbedges[2].otherNode(triangle[2]);
	    }

	int nroftrue = 0;
	for (int i = 0; i < 3; i++)
	    if (inverttedges[i])
		nroftrue++;
	if (nroftrue > 1)
	    for (int i = 0; i < 3; i++)
		inverttedges[i] = !inverttedges[i];
	for (int i = 0; i < 3; i++){
	    if (inverttedges[i])
		invertEdge(tedges[i]);
	    if (nghbedges[i].tail() != triangle[i])
		invertEdge(nghbedges[i]);
	}
    }

    /** 
     * Implementation of the abstract {@link Yutsis#interchange(int
     * [], int[] )} method.  
     * @param nodes array containing the endpoints of the {@link Edge}
     *              on which the interchange is performed
     * @param icnodes array containing the neighbors of the corresponding
     *               nodes of nodes to be interchanged 
     * @see Yutsis 
     **/
    public void interchange(int [] nodes, int [] icnodes){
	Edge [] ices = new Edge[2];
	Edge [] nghbedges = new Edge[2];
	formatInterchange(nodes, icnodes, ices, nghbedges);
	performInterchange(nodes, icnodes, ices, nghbedges);
	fireStateChanged();
    }

    /** 
     * Formats the nodes and the edges involved as needed by the 
     * interchange rule.
     * @param nodes the endpoints of the Edge on which the interchange
     *              is performed
     * @param icnodes the endpoints of the Edges to be interchanged in 
     *                the proper order
     * @param ices the Edges to be interchanged in the proper order
     * @param nhbedges the Edges coupled in the endpoint of the 
     *                 Edge on which the interchange is performed
     *                 which are NOT to be interchanged
     * @see Edge
     **/
    private void formatInterchange(int [] nodes, int [] icnodes, 
				   Edge [] ices, Edge [] nghbedges){
	for (int i=0; i < 3; i++)
	    if (edges[nodes[0]][i].otherNode(nodes[0]) == icnodes[0])
		ices[0] = edges[nodes[0]][i];
	    else if (edges[nodes[0]][i].otherNode(nodes[0]) != nodes[1])
		nghbedges[0] = edges[nodes[0]][i];
	for (int i=0; i < 3; i++)
	    if (edges[nodes[1]][i].otherNode(nodes[1]) == icnodes[1])
		ices[1] = edges[nodes[1]][i];
	    else if (edges[nodes[1]][i].otherNode(nodes[1]) != nodes[0])
		nghbedges[1] = edges[nodes[1]][i];
	log("Formatting interchange on edge: " 
	    + edges(nodes[0],nodes[1])[0] + "\n  interchanging edges: "
	    + ices[0] +", " + ices[1]);

	if (!sign(nodes[0]))
	    invertSign(nodes[0]);
	if (!sign(nodes[1]))
	    invertSign(nodes[1]);
	if (ices[0].tail() != nodes[0])
	    invertEdge(ices[0]);
	if (ices[1].tail() != nodes[1])
	    invertEdge(ices[1]);
	if (nghbedges[0].tail() != nodes[0])
	    invertEdge(nghbedges[0]);
	if (nghbedges[1].tail() != nodes[1])
	    invertEdge(nghbedges[1]);
    }

    /** 
     * Performs the interchange assuming the subgraph involved is 
     * in correct shape.
     * @param nodes the endpoints of the Edge on which the interchange
     *              is performed
     * @param icnodes the endpoints of the Edges to be interchanged in 
     *                the proper order
     * @param ices the Edges to be interchanged in the proper order
     * @param nhbedges the Edges coupled in the endpoint of the 
     *                 Edge on which the interchange is performed
     *                 which are NOT to be interchanged
     * @see Edge
     **/
    private void performInterchange(int [] nodes, int [] icnodes, 
				    Edge [] ices, Edge [] nghbedges){
	Edge e = null;
	for (int i = 0; i < 3; i++){
	    if (edges[nodes[0]][i].otherNode(nodes[0]) == icnodes[0])
		edges[nodes[0]][i] = ices[1];
	    else if (edges[nodes[0]][i].otherNode(nodes[0]) == nodes[1]){
		e = edges[nodes[0]][i];
	    }
	    if (edges[nodes[1]][i].otherNode(nodes[1]) == icnodes[1])
		edges[nodes[1]][i] = ices[0];
	}
	String oldlabel = e.label();
	e.setLabel(newlabelbase+(newlabelcount++));
	newlabels.add(e.label());
	ices[0].setTail(nodes[1]);
	ices[1].setTail(nodes[0]);
	log("Performed interchange on edge: " + oldlabel + "->" + e 
		    + "\n  interchanging edges: " + ices[0] + ", " + ices[1]);
	//code for generating sum(nl)[(-1)^(ic1+ic2+e+nl) (2nl+1) 
	// 6j(nghbedges[0], ices[1], nl, nghbedges[1], ices[0], e) or 
	// 6j(nghbedges[1], ices[0], nl, nghbedges[0], ices[1], e)] 
	// depending on direction of e -> does not matter (invariants)
	genrecoupcoeff.interchange(oldlabel,
				   ices[0].label(), ices[1].label(), 
				   nghbedges[0].label(), 
				   nghbedges[1].label(),e.label());
    }

    /** 
     * Overrides the {@link Yutsis#triangularDelta} method, taking in
     * account node signs and edge directions.
     * @return true if this graph can (and will) be formatted as a
     *         triangular delta 
     * @see Yutsis
     **/
    public boolean triangularDelta(){
	if (n() != 1)
	    return false;
	int [] td = new int[2]; int tdcount = 0;
	for (int i = 0; i < order(); i++)
	    if (!isRemoved(i))
		td[tdcount++] = i;

	return triangularDelta(td[0],td[1]);
    }

    /** 
     * Overrides the {@link Yutsis#triangularDelta(int [])} method,
     * taking in account node signs and edge directions.
     * @param td the nodes of the triangular delta as an array
     * @return true if this YutsisGraph is a triangular delta between
     *         the specified nodes 
     * @see Yutsis 
     **/
    public boolean triangularDelta(int [] td){
	return triangularDelta(td[0],td[1]);
    }
    
    /** 
     * Overrides the {@link Yutsis#triangularDelta(int, int)} method,
     * taking in account node signs and edge directions.  
     * @param node1 first node of the triangular delta to be formatted
     * @param node2 second node of the triangular delta to be formatted
     * @return true if this YutsisGraph is a triangular delta between
     *         the specified nodes 
     * @see Yutsis 
     **/
    public boolean triangularDelta(int node1, int node2){
	if (n() != 1 || isRemoved(node1) || isRemoved(node2))
	    return false;
	log("Formatting graph as triangular delta");
	boolean [] invertedges = new boolean[3];
        int invertcount = 0; // nr of edges to be inverted with sign(node1)
	for (int i = 0; i < 3; i++){
	    invertedges[i] = sign(node1) ? 
		edges[node1][i].head() == node1 : 
		edges[node1][i].tail() == node1;
	    if (invertedges[i])
		invertcount++;
	}

	if ((sign(node1) && sign(node2)) ||
	    (!sign(node1) && !sign(node2))){
	    if (invertcount > 1){
		invertSign(node1);
		for (int i = 0; i < 3; i++)
		    if (!invertedges[i])
			invertEdge(edges[node1][i]);
	    }
	    else {
		invertSign(node2);
		for (int i = 0; i < 3; i++)
		    if (invertedges[i])
			invertEdge(edges[node1][i]);
	    }
	}
	else  // only edges need to be inverted
	    for (int i = 0; i < 3; i++)
		    if (invertedges[i])
			invertEdge(edges[node1][i]);
	return true;
    }
    
    /** 
     * Returns the General Recoupling Coefficient corresponding with
     * the current state of this YutsisGraph. 
     * @return the {@link GenRecoupCoeff} 
     * @see GenRecoupCoeff
     **/
    public GenRecoupCoeff genRecoupCoeff(){
	return genrecoupcoeff;
    }

    /** 
     * Write the String to the logstream.
     * @param tolog the String to be logged
     **/
    protected void log(String tolog){
	if (log == null)
	    return;
	log.println(tolog);
    }

    /** 
     * Returns the braket notation of the original YutsisGraph.
     * @return the braket notation of the original YutsisGraph
     **/
    public String braket(){ return braket; }

    /** 
     * Sets the PrintStream to which all operation which alter the
     * graph will be logged. Default logstream is <code>System.err</code>.
     * When no logging is desired, set it to be <code>null</code>.
     * @param logstream The PrintStream where logging will be done
     **/
    public void setLogStream(PrintStream logstream){
	log = logstream;
    }

    /** 
     * A String representation of this graph in it's current state.
     * Format: <code>n</code> <br><code> ... </code><br>
     * <code>node | edgelabel1: nghb1 edgelabel2: nghb2 edgelabel3: nghb3</code> 
     * <br> <code> ... </code>.
     * @return a String representation of this YutsisGraph.
     **/
    public String toString(){
	StringBuffer sb = new StringBuffer();
	sb.append(n()); sb.append('\n');
	for (int i = 0; i < order(); i++)
	    if (!isRemoved(i)){
		sb.append((sign(i) ? "+" : "-") + i + " | ");
		for (int j = 0; j < 3; j++){
		    sb.append(edges[i][j].label() + ":" 
			      + (edges[i][j].head() == i ? '+' : '-')
			      + edges[i][j].otherNode(i) + " ");
		}
		sb.append('\n');
	    }
	return sb.toString();
    }

    /**
     * Returns the edge label to be used for saving the Graph in 
     * GML-format; overloads AbstractGraph.gmlEdgeLabel.
     * @param i first node of the edge
     * @param j second node of the edge
     * @return a label for the edge (i,j) or null if no label is desired.
     **/
    public String [] gmlEdgeLabels(int i){
	String [] labels = new String[3];
	Edge [] edges = edges(i);
	for (int j = 0; j < 3; j++)
	    labels[j] = edges[j].label();
	return labels;
    }

    /** 
     * Returns an exact copy of this YutsisGraph, as specified by the
     * Cloneable interface.
     * @return a clone of this YutsisGraph.
     **/
    public Object clone(){
	YutsisGraph y = null;
	y = (YutsisGraph) super.clone();
	y.nodeSign = new boolean[nodeSign.length];
	System.arraycopy(nodeSign,0,y.nodeSign,0,nodeSign.length);
	y.edges = new Edge[edges.length][3];
	HashMap edgeshm = new HashMap(4*order());
	for (int i = 0; i < edges.length; i++)
	    if (!isRemoved(i))
		for (int j = 0; j < 3; j++){
		    Edge e = (Edge) edgeshm.get(edges[i][j].label());
		    if (e == null){
			y.edges[i][j] = (Edge) edges[i][j].clone();
			edgeshm.put(y.edges[i][j].label(), y.edges[i][j]);
		    }
		    else
			y.edges[i][j] = e;
		}
	    else
		for (int j = 0; j < 3; j++)
		    y.edges[i][j] = null;	      
	// Immutable objects -> values don't have to be cloned
	y.newlabels = (HashSet) newlabels.clone();
	y.genrecoupcoeff = (GenRecoupCoeff) genrecoupcoeff.clone();
	return y;
    }
    
    /** 
     * This program constructs the YutsisGraph specified by the first
     * argument and presents an interactive interface allowing the
     * user to get information about the YutsisGraph and the
     * reductionprocess. When ran without arguments it prints a
     * "usage" to System.err.
     **/
    public static void main(String [] args) throws IOException{
    String usage = new String("Usage: java YutsisGraph <YutsisGraph>\n <YutsisGraph>:= \"<Bra|Ket>\" | <filename>\n <filename> must be in BRAKET of YTS format\n  (see documentation of Class YutsisGraph for more info)");
      YutsisGraph y = null;
      if (args.length == 1){
	  try {	  
	      if (args[0].charAt(0) == '<')
		  y = new YutsisGraph(args[0]);
	      else {
		  int format = YutsisGraph.guessFileFormat(args[0]);
		  if (format == -1){
		      System.err.println(usage);
		      return;
		  }
		  y = new YutsisGraph(new BufferedReader(new FileReader(args[0])),
				      format);
	      }
	  }
	  catch (IllegalArgumentException e){
	      System.err.println(e.getMessage());
	      return;
	  }
	  catch (FileNotFoundException e){
	      System.err.println("File " + args[0] + " not found.");
	      return;
	  }
      System.out.println(y);
      }
      else{
	  System.err.println(usage);
      }
      interactive(y);
    }

    /** 
     * This function presents an interactive text interface allowing
     * the user to get information about this YutsisGraph and the
     * reduction process. Uses {@link CycleCostAlgorithm}. {@link
     * CycleGenerator}, {@link PathGenerator}..
     * @param y the intial YutsisGraph
     * @throws IOException if an I/O error occurs
     **/
    public static void interactive(YutsisGraph y) throws IOException{
 	BufferedReader kb = new BufferedReader(new 
	    InputStreamReader(System.in));
	boolean commands = true;
	String command;
	String help = "Commands (shortkey):"
	    +"\nhelp (h)\t\t\t: prints this message"
	    +"\nprint (p)\t\t\t: prints the Yutsis Graph Y"
	    +"\npaths (pt)\t\t\t: prints all shortest paths of Y"
	    +"\ncpaths (cp)\t\t\t: prints all canonical paths of Y"
	    +"\ncycles (c)\t\t\t: prints all relevant cycles of Y"
	    +"\ncyclecosts (cc)\t\t\t: prints all cycles with edgecosts" 
	    +"\nformula (f)\t\t\t: prints the formula generated so far"
	    +"\nnsum6j (n) \t\t\t: prints the number of interchanges so far"
	    +"\nbestcycle (bc)\t\t\t: prints the best cycle and it's best edge"
	    +"\nstep (s)\t\t\t: performs one step in the reduction proces"
	    +"\nreduce (r)\t\t\t: reduces the graph to a triangular delta"
	    +"\nbubble (b)\t\t\t: removes an arbitrary bubble if present"
	    +"\ntriangle (t) n1 n2 n3\t\t: removes the triangle n1, n2, n3"
	    +"\ninterchange (i) n1 n2 b1 b2\t: interchanges (n1,b1) with (n2,b2)"
	    +"\neffect (e) n1 n2 b1 b2\t: prints inc/decreasing cycles for the given interchange"
	    +"\ngraph (y)\t\t\t: reads a new graph in BRAKET notation <T_1|T_2>"
	    +"\nload (l) filename\t\t: reads a new graph from the file filename"
	    +"\nouput (o) filename\t\t: ouput the graph in gml-format to the file filename";

	PathGenerator pg = null;
	CycleGenerator cg = null;
	CycleCostAlgorithm cca = null;

	if (y == null)
	    System.out.println("You are now in the interactive interface" + 
			       "\n Use the \"load\"(l) command to load a graph or type \"help\"(h) for more info");
	else{
	    pg = new PathGenerator(y);
	    cg = new CycleGenerator(y);
	    cca = new CycleCostAlgorithm(y, new CycleCountHeuristic(y));
	}
	    
	do{
	    System.out.print(">");
	    command = kb.readLine().trim().toLowerCase();
	    try{
		if (command.equals("q") || command.equals("quit"))
	            commands = false;
		else if (command.equals("y") || command.equals("graph")){
		    System.out.println(y = new YutsisGraph(kb.readLine()));
		    pg = new PathGenerator(y);
		    cg = new CycleGenerator(y);
		    cca = new CycleCostAlgorithm(y,new CycleCountHeuristic(y));
		}
		else if((command.equals("p") || command.equals("print"))
			&& yNotNull(y))
	            System.out.println(y);
	        else if ((command.equals("pt") || command.equals("paths"))
			 && yNotNull(y))
	            System.out.println(pg);
	        else if ((command.equals("cp") || command.equals("cpaths"))
			 && yNotNull(y)){
		    pg.makeCanonical();
		    System.out.println(pg);
	        }
	        else if ((command.equals("c") || command.equals("cycles"))
			 && yNotNull(y))
		    System.out.println(cg);
	        else if ((command.equals("b") || command.equals("bubble"))
			 && yNotNull(y)){
		    int [] bubble = y.bubble();
		    if (bubble[0] != -1)
			y.removeBubble(bubble);
		}
	        else if ((command.equals("bc") 
			  || command.equals("bestcycle"))
			 && yNotNull(y) && yNotReduced(y)){
	            int [] bestcycleedge = {-1,-1};
		    int [] besticnodes = {-1,-1};
		    ArrayList candidates = new ArrayList();
		    Cycle bestcycle = 
			cca.heuristic().bestCycle(bestcycleedge, besticnodes, candidates);
		    if (bestcycle.length() == 3)
			System.out.println("Best cycle:"+bestcycle);
	            else {
			System.out.println("Best cycle:"+bestcycle
					   +" ; best operation: " 
					   + (String) candidates.get(0));
			System.out.println("Equivalent operations:");
			for(int i = 1; i < candidates.size(); i++)
			    System.out.println((String) candidates.get(i));
		    }		    
		}
		else if ((command.equals("cc") 
			  || command.equals("cyclecosts"))
			 && yNotNull(y) && yMinGirth(3, cg)){
		    CycleCostAlgorithm cca1 = new 
			CycleCostAlgorithm(y, new EdgeCostHeuristic(y));
		    ((EdgeCostHeuristic) cca1.heuristic()).printCycleCosts();
		}
		else if ((command.equals("s") || command.equals("step"))
			 && yNotNull(y) && yNotReduced(y)){
		    cca.performOperation();
		}
	        else if ((command.equals("r") || command.equals("reduce"))
			 && yNotNull(y) && yNotReduced(y))
	            CycleCostAlgorithm.reduceYutsisGraph(y,new CycleCountHeuristic(y),null,null);
		else if ((command.equals("f") || command.equals("formula"))
			 && yNotNull(y))
		    System.out.println(y.genRecoupCoeff());
		else if ((command.equals("n") || command.equals("nsum6j"))
			 && yNotNull(y))
		    System.out.println("#summations: "
				       + y.genRecoupCoeff().nrOfSummations()
				       + " #6j's:"
				       + y.genRecoupCoeff().nrOf6js());
		else {
	            StringTokenizer args = new StringTokenizer(command);
		    String token = args.nextToken();
	            if (token.equals("l") || token.equals("load")){
	        	String filename = null;
			try {
			    filename = args.nextToken();
			    int format = guessFileFormat(filename);
			    if (format == -1)
				System.err.println("Illegal format: use BRAKET or YTS format (see documentation YutsisGraph)");			
				y = new YutsisGraph(new BufferedReader(new 
				    FileReader(filename)),format);
				pg = new PathGenerator(y);
				cg = new CycleGenerator(y);
				cca = new CycleCostAlgorithm(y, new CycleCountHeuristic(y));
			}
			catch (FileNotFoundException e){
			    System.err.println("File "+filename+" not found");
			}
			catch (NoSuchElementException e){
			    System.err.println("No file argument specified");
			}
		    }
		    else if ((token.equals("o") || token.equals("ouput")) 
		             && yNotNull(y)){
		        try{
			    PrintStream gmloutput 
			        = new PrintStream(new BufferedOutputStream(new 
				    FileOutputStream(args.nextToken())));
			    y.toGml(gmloutput);
			    gmloutput.close();
			}
			catch(FileNotFoundException fnfe){
			    System.err.println(fnfe.getMessage());
			}
		    }
	            else if ((token.equals("t") || token.equals("triangle"))
			     && yNotNull(y) && yNotReduced(y))
			y.removeTriangle(Integer.parseInt(args.nextToken()),
					 Integer.parseInt(args.nextToken()),
					 Integer.parseInt(args.nextToken()));
	            else if ((token.equals("i") || token.equals("interchange"))
			     && yNotNull(y) && yNotReduced(y))
	        	y.interchange(Integer.parseInt(args.nextToken()),
	        		      Integer.parseInt(args.nextToken()),
	        		      Integer.parseInt(args.nextToken()),
	        		      Integer.parseInt(args.nextToken()));
		    else if ((token.equals("e") || token.equals("effect"))
			      && yNotNull(y) && yMinGirth(4,cg)){
			
			((CycleCountHeuristic) cca.heuristic()).printEffect(Integer.parseInt(args.nextToken()),
					Integer.parseInt(args.nextToken()),
					Integer.parseInt(args.nextToken()),
					Integer.parseInt(args.nextToken())
					);
		    }
	            else 
			throw new IllegalArgumentException();
		} 
	    }
	    catch(IllegalArgumentException e){
		if (e.getMessage() != null)
		    System.out.println(e.getMessage());
		else
		    System.out.println(help);
	    }   
	    catch(NoSuchElementException e){
		System.out.println("Expected more arguments (press h for help).");
	    } 
	} 
	while (commands);
    }

    private static boolean yNotNull(YutsisGraph y) 
	throws IllegalArgumentException{
	if (y == null)
	    throw new IllegalArgumentException
		("You must first define a graph to use this function (press h for help).");
	return true;
    }
    
    private static boolean yNotReduced(YutsisGraph y)
	throws IllegalArgumentException{
	if (y.triangularDelta())
	    throw new IllegalArgumentException("Graph is already reduced (press h for help).");
	return true;
    }
    
    private static boolean yMinGirth(int girth, CycleGenerator cg)
	throws IllegalArgumentException{
	if (cg.girth() < girth)
	    throw new IllegalArgumentException("Girth must be at least " 
					       + girth + " to use this function (press h for help).");
	return true;
    }

    /** 
     * Test function (debug).
     **/
    private static void test() throws IOException{
	YutsisGraph y = new YutsisGraph(new BufferedReader(new 
	    FileReader("/data/files/cpp/veerle/f0.dat")));
	/*
	System.out.print(y);
	checkIterator(y);
	y.interchange(1,2,4,5);
	checkIterator(y);
	y.removeTriangle(4,0,2);
	checkIterator(y);
	y.removeTriangle(1,5,3);
	checkIterator(y);
	*/
	//System.out.println(y);
	//checkF0();
	checkG2();
    }
    
    /** 
     * Test function (debug).
     **/
    private static void checkIterator(YutsisGraph y){
	System.out.println("Iterating Forward:");
	NodeIterator n = y.first();
	System.out.print(n.next());
	while(n.hasNext())
	    System.out.print(", " + n.next());
	System.out.println("\nIterating Backward:");
	//n = y.last();
	System.out.print(n.previous());
	while(n.hasPrevious())
	    System.out.print(", " + n.previous());
	System.out.println("\nIterating Forward again:");
	System.out.print(n.next());
	while(n.hasNext())
	    System.out.print(", " + n.next());
	System.out.println();
	NodeIterator ii = y.first();
	System.out.println("Forward iterating over all i < j:");
	while (ii.hasNext()){
	    int i = ii.next();
	    System.out.println("i=" + i);
	    NodeIterator ji = y.nodeIterator(i); 
	    if (ji != null){
		ji.next();
		System.out.print("j=");
		while (ji.hasNext()){// Always i < j
		    int j = ji.next();
		    System.out.print(" " + j);
		}
		System.out.println();
	    }
	}
    }
    
    /** 
     * Test function (debug).
     **/
    private static void checkF0(){
	YutsisGraph f0 = new 
	    YutsisGraph("<((j1,j2)j5,(j3,j4)j6)j7|((j1,j3)j8,(j2,j4)j9)j7>");
	System.out.println("Graph:"+f0);
	int [] bubble = f0.bubble();
	System.out.println("Bubble?" + bubble[0] + " " + bubble[1]);
	if (bubble[0] != -1)
	    f0.removeBubble(bubble);
	f0.interchange(1,4,0,2);
	System.out.println("GRC="+f0.genRecoupCoeff());
	f0.removeTriangle(0,3,4);
	System.out.println("GRC="+f0.genRecoupCoeff());
	f0.removeTriangle(1,2,5);
	System.out.println("GRC="+f0.genRecoupCoeff());
	System.out.println("Triangular delta? " + f0.triangularDelta());
	System.out.println("GRC="+f0.genRecoupCoeff());
	System.out.println("#6j="+f0.genRecoupCoeff().nrOf6js()+ 
			   "; #summations="+
			   f0.genRecoupCoeff().nrOfSummations());
    }
    
    /** 
     * Test function (debug).
     **/
    private static void checkG2(){
	YutsisGraph g2 = new 
	    YutsisGraph("<(((j1,j2)j8,((j3,  j5)j9,(j6 ,j7)j10)  j11)j12,j4)j13  | (( (j1,j2)j14,(   (j3,(j7,j5)j15)j16, j6) j17)j18,j4)j13>");
	System.out.println(g2);
	int [] bubble = g2.bubble();
	System.out.println("Bubble?" + bubble[0] + " " + bubble[1]);
	if (bubble[0] != -1)
	    g2.removeBubble(bubble);
	System.out.println("GRC="+g2.genRecoupCoeff());
	bubble = g2.bubble();
	System.out.println("Bubble?" + bubble[0] + " " + bubble[1]);
	if (bubble[0] != -1)
	    g2.removeBubble(bubble);
	System.out.println("GRC="+g2.genRecoupCoeff());
	bubble = g2.bubble();
	System.out.println("Bubble?" + bubble[0] + " " + bubble[1]);
	if (bubble[0] != -1)
	    g2.removeBubble(bubble);
	System.out.println("GRC="+g2.genRecoupCoeff());
	g2.removeTriangle(3,5,9);
	System.out.println("GRC="+g2.genRecoupCoeff());
	g2.removeTriangle(3,4,10);
	System.out.println("GRC="+g2.genRecoupCoeff());
	System.out.println("Triangular delta?" + g2.triangularDelta());
	System.out.println("GRC="+g2.genRecoupCoeff());
    }
    
}
