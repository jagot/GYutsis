/* AbstractYutsis.java
   -------------------
   2002 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/

import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/** 
 * Abstract implementation of a (cubic) graph allowing the
 * graph transformations used to reduce a {@link YutsisGraph}
 * by implementing the {@link Graph} interface. A ChangeListener
 * can be registered to listen to ChangeEvents fired when the
 * underlying cubic graph is structurally changed, i.e. a change
 * reflected in the Cycle Vector Space spanned by a cycle base
 * of the graph.
 * @see Graph
 * @author Dries.VanDyck@rug.ac.be 
 **/

public abstract class AbstractYutsis 
    extends AbstractGraph
    implements Yutsis{
    
    /** 
     * Int representing the YTS format in which the graphs are stored
     * in a file: the first line contains the (cubic) order
     * (<var>n</var>) of the graph, followed by 2<var>n</var> lines
     * with three labels representing the couplings in the nodes. The
     * first <var>n</var> lines, of those 2<var>n</var> lines
     * correspond to the left tree in the braket, the next
     * <var>n</var> lines those of the right tree in the braket.  
     **/
    public static final int YTS = 1;

    /** 
     * Int representing the BRAKET format in which the graphs are
     * stored in a file: this represents the well known braket
     * notation e.g. <pre><(j1,j2)j12|(j2,j1)j12></pre>.  
     **/
    public static final int BRAKET = 2;

    /** 
     * Int representing the EDGELIST format in which the graphs are
     * stored in a file: <pre> &lt;nrOfnodes&gt &lt;nrOfEdges&gt
     * &lt;edge&gt &lt;edge&gt ... &lt;edge&gt </pre> With &lt;edge&gt
     * = node1 node2, the edges may be distributed over several lines,
     * but linebreaks are not allowed between two nodes of the same
     * edge. No syntax checks are performed.  
     **/
    public static final int EDGELIST = 4;

    /** 
     * Guesses the format of the file by looking at the first two lines.
     * The aim is to distinguinsh the different file formats by simple
     * differences like the number of tokens on a line, NOT to check for
     * correct syntax!
     * @param filename the filename of the file 
     * @return the guessed format as an int (BRAKET, YTS or EDGELIST) or -1 
     *         if unsupported (i.e. not recognized)
     **/ 
    public static int guessFileFormat(String filename) throws IOException{
	BufferedReader file = new BufferedReader(new FileReader(filename));
	String firstline = file.readLine().trim();
	if (firstline.startsWith("<") && firstline.endsWith(">"))
	    return BRAKET;

	String secondline = file.readLine().trim();
	if (new StringTokenizer(secondline).countTokens() == 3)
	    return YTS;
       
	if (new StringTokenizer(firstline).countTokens() == 2)
	    return EDGELIST;
	else
	    return -1;
    }

    /**
     * Returns true if the intermediate labels APPEAR to be present,
     * false otherwise. Only the first intermediate label is checked.
     * @param braket the String to be checked if intermediate labels
     *               are present
     * @return true if the intermediate labels APPEAR to be present,
     *         otherwise false.
     **/
    public boolean intermediateLabelsPresent(String braket){
	int index = braket.indexOf(')');
	if (index == -1)
	    throw new IllegalArgumentException("Not a BRAKET: does not contain ')'.");
	while (Character.isWhitespace(braket.charAt(++index)))
	    ;
	return braket.charAt(index) != ',' 
	    && braket.charAt(index) != ')' 
	    && braket.charAt(index) != '|';
    }

    /**
     * Adds intermediate labels to a BRAKET representation.
     * @param braket the BRAKET representation
     * @return a new BRAKET representation with intermediate
     *         labels 
     **/
    public String addIntermediateLabels(String braket){
	int number=1;
	StringBuffer newbraket = new StringBuffer(braket.replaceAll(" ",""));
	//no spaces anymore..
	int index = 1;
	String root = null;
	while(index < newbraket.length()){
	    if (newbraket.charAt(index) == ')'){
		String label = "t"+number++;
		int indexcomma = newbraket.indexOf(",", index+1);
		int indexclosing = newbraket.indexOf(")", index+1);
		if (indexcomma == indexclosing) // -1 thus
		    indexcomma = indexclosing = newbraket.length();
		int indexborder = 
		    newbraket.indexOf(root == null ? "|" : ">", index+1);
		if (indexborder < indexcomma && indexborder < indexclosing &&
		    !newbraket.substring(index+1,indexborder).equals("")){
		    if (root == null){
			root = newbraket.substring(index+1,indexborder);
		    }
		    else if (!root.equals(newbraket.substring(index+1,indexborder)))
			throw new IllegalArgumentException("Invalid BRAKET: root labels differ.");
		    label = "";//no insertion needed
		    number--;// number is not used..
		    index+=root.length();
		}
		else if (newbraket.charAt(index+1) == '|'){//first root
		    root = label;
		}
		else if (newbraket.charAt(index+1) == '>'){//second root
		    label = root;
		}
		newbraket.insert(++index,label);
		index += label.length();
	    }
	    else
		index++;
	}
	return newbraket.toString();
    }

    /**
     * Implementation of the method {@link Graph#nrOfEdges} as 
     * specified by the {@link Graph} interface.
     * @see AbstractGraph
     * @see Graph
     * @return the number of edges
     **/
    public int nrOfEdges(){ return 3*nrOfNodes()/2; }

    /** 
     * Interchange on the edge <code>(node1, node2)</code> interchanging 
     * the edges <code>(node1, icnode1)</code> and 
     * <code>(node2, icnode2)</code>.
     * @param node1 first node of the edge on which the interchange 
     *              is performed
     * @param node2 second node of the edge on which the interchange 
     *              is performed
     * @param icnode1 neighbor of node1 to be interchanged with icnode2
     * @param icnode2 neighbor of node2 to be interchanged with icnode1
     **/
    public void interchange(int node1, int node2, int icnode1, int icnode2){
	interchange(new int [] {node1,node2}, new int [] {icnode1,icnode2});
    }

    /** 
     * Removes the triangle <code>node1, node2, node3</code>.
     * @param node1 first node of the triangle to be removed
     * @param node2 second node of the triangle to be removed
     * @param node3 third node of the triangle to be removed 
     **/
    public void removeTriangle(int node1, int node2, int node3){
	removeTriangle(new int [] {node1, node2, node3});
    }

    /** 
     * True if the graph represents a triangular delta, false
     * otherwise.
     * @return true if this graph represents a triangular delta 
     **/
    public boolean triangularDelta(){
	return nrOfNodes() == 2;
    }

    /** 
     * True if the graph represents a triangular delta between node
     * <code>td[0]</code> and <code>td[1]</code>, false otherwise.
     * @param td the nodes of the triangular delta as an array
     * @return true if this Graph is a triangular delta between
     *         the specified nodes 
     **/
    public boolean triangularDelta(int [] td){
	if (!isRemoved(td[0]) && !isRemoved(td[1]) && nrOfNodes() == 2)
	    return true;
	return false;
    }

    /** 
     * True if the graph represents a triangular delta between node
     * <code>node1</code> and <code>node2</code>, false otherwise.
     * @param node1 first node of the triangular delta to be formatted
     * @param node2 second node of the triangular delta to be formatted
     * @return true if this Graph is a triangular delta between
     *         the specified nodes 
     **/
    public boolean triangularDelta(int node1, int node2){
	return triangularDelta(new int [] {node1, node2});
    }
}
