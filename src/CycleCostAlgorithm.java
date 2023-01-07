/* CycleCostAlgorithm.java
   -----------------------
   2002 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/
import java.util.ArrayList;
import java.util.Iterator;

import java.io.PrintStream;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/** 
 * This class represents an improved algorithm to be used in
 * collaboration with an heuristic on the algorithm using graphical
 * methods by Yutsis, Vanagas et. al for the calculation of general
 * recoupling coefficients of angular momenta. When constructing an
 * CycleCostAlgorithm object an heuristic by means of an object
 * implementing the CCAHeuristic interface has to be delivered.  
 *
 * <p> This algorithm will reduce the graph in the following way: when
 * there are bubbles present they are removed, otherwise when there
 * are triangles present they are removed. When there are no bubbles
 * nor triangles present, the heuristic will decide which interchange
 * will be applied.
 * @see Yutsis 
 * @see CCAHeuristic
 * @author Dries.VanDyck@rug.ac.be
 **/
public class CycleCostAlgorithm
    implements ChangeListener{

    /** 
     * The Yutsis object defining the problem.
     * @see Yutsis
     **/
    Yutsis y;

    /**
     * The heuristic used to select an operation when no bubbles or
     * triangles are present.
     * @see CCAHeuristic
     **/
    CCAHeuristic h;

    /**
     * The CycleGenerator delevering the relevant cycles.
     * @see CycleGenerator
     **/
    CycleGenerator cg;

    /**
     * PrintStream to log the choice of rules.
     **/
    PrintStream log = System.err;  

    int nrofinterchanges = 0;

    boolean stateChanged = true;

    /** 
     * Basename of the files where the graph after each step will be
     * written in gml-output (graphlet).
     **/
    String gmlbasename = null;

    /** 
     * Constructs a new CycleCostAlgorithm object for the 
     * Yutsis y with heuristic h.
     * @param y the Yutsis object defining the problem
     * @param h the heuristic to be used when no bubbles or triangles are
     *          present
     * @see Yutsis
     * @see CCAHeuristic
     **/
    public CycleCostAlgorithm(Yutsis y, CCAHeuristic h){
	this.y = y;
	this.h = h;
	y.addChangeListener(this);
	outputGml();
    }

    /** 
     * Returns the number of interchanges performed so far to reduce the
     * Yutsis object.
     * @return the number of interchanges performed so far to reduce
     *         the Yutsis object.  
     * @see Yutsis
     **/
    public int nrOfInterchanges() { return nrofinterchanges; }

    /** 
     * Searches for the best operation to reduce the Yutsis object and
     * performs it.  If there is a bubble or triangle in the graph it
     * will remove the bubble or triangle. Otherwise it will use the
     * delivered CCAHeuristic object to select an operation.
     * @see CCAHeuristic
     **/
    public void performOperation(){
	int [] bubble = y.bubble();  
	if (bubble[0] != -1){
	    log("Best Cycle: " 
		+ bubble[0] + "->" + bubble[1] + "->"+bubble[0]);
	    y.removeBubble(bubble);
	    return;
	}
	nrofinterchanges++; // triangle counts for an interchange
	int [] triangle = y.triangle();
	if (triangle[0] != -1){
	    log("Best cycle: "+ 
		triangle[0]+"->"+triangle[1]
		+"->"+triangle[2]+"->"+triangle[0]);
	    y.removeTriangle(triangle);
	    return;
	}
	int [] bestcycleedge = {-1,-1};
	int [] besticnodes = {-1,-1};
	ArrayList candidates = new ArrayList();
	Cycle bestcycle = h.bestCycle(bestcycleedge, besticnodes, candidates);
	log("Best Cycle:"+bestcycle+"; best edge: "
	    + (y instanceof YutsisGraph ? 
	       ((YutsisGraph) y).edges(bestcycleedge[0],
				       bestcycleedge[1])[0].toString() 
	       : "(" +bestcycleedge[0]+","+bestcycleedge[1]+")"));
	if (candidates.size() > 0){
	    log("Equivalent operations:");
	    for (Iterator i = candidates.iterator(); i.hasNext();){
		String operation = (String) i.next();
		log(operation);
	    }
	}
	y.interchange(bestcycleedge,besticnodes);
    }

    /**
     * Returns the Yutsis object defining the problem.
     * @return the Yutsis object defining the problem.
     * @see Yutsis
     **/
    public Yutsis problem() { return y; } 

    /**
     * Returns the used CCAHeuristic object.
     * @return the used CCAHeuristic object.
     * @see CCAHeuristic
     **/
    public CCAHeuristic heuristic(){ return h; }

    /**
     * Sets the CCAHeuristic, used to select an operation when no 
     * bubbles or triangles are present.
     * @param h the CCAHeuristic object to be used.
     **/
    public void setHeuristic(CCAHeuristic h){ this.h = h; }

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
     * Sets the PrintStream to which all operation which alter the
     * graph will be logged. Default logstream is <code>System.err</code>.
     * When no logging is desired, set it to be <code>null</code>.
     * @param logstream The PrintStream where logging will be done
     **/
    public void setLogStream(PrintStream logstream){ log = logstream;}

    /** 
     * Reduces the Yutsis object to a so called triangular delta
     * by repeatedly calling performOperation. 
     * @see #performOperation.
     * @see Yutsis
     **/
    public void reduce(){
	while (!y.triangularDelta()){
	    performOperation();
	}
    }  

    /**
     * Implementation of the ChangeListener interface.
     **/
    public void stateChanged(ChangeEvent e){ 
	stateChanged = true; 
	outputGml();
    }

    /** 
     * Sets the basename of the file(s) where the graphs will be written
     * after each reductionstep in gml-format (graphlet). The graph after 
     * step <var>i</var> will be in the file <br>
     *   "<code>gmlbasename</code>.<var>i</var>.gml".
     * @param gmlbasename the basename of the files to which gml-output
     *                    will be written after each step.
     **/
    public void setGmlOutputBasename(String gmlbasename){
	this.gmlbasename = gmlbasename;
    }

    void outputGml(){
	if (gmlbasename == null)
	    return;
	try{
	    PrintStream gmloutput 
		= new PrintStream(new BufferedOutputStream(new 
		    FileOutputStream(gmlbasename+"."
				     +nrOfInterchanges()+".gml")));
	    y.toGml(gmloutput);
	    gmloutput.close();
	}
	catch(FileNotFoundException fnfe){
	    System.err.println(fnfe.getMessage());
	} 
    }

    /** 
     * This program reduces a Yutsis Graph to a so called triangular
     * delta in as few operations possible, while constructing the
     * general recoupling coefficient associated with it, which is
     * written to the screen.
     **/
    public static void main(String [] args) throws IOException{
	final int EDGE_COST = 0;
	final int MORE_SMALLER_LESS_BIGGER = 1;
	final int CYCLE_COUNT = 2;
	final int ALL = 64; //leave room for other heuristics
	GRVisitor grv = null;
	String usage = 
	    "Usage: java [-options] CycleCostAlgorithm <YutsisGraph>"
	    +"\n <YutsisGraph>:= \"<Bra|Ket>\" | <filename>\n <filename> must be in BRAKET or YTS format"
	    + "\nOptions:"
	    + "\n heuristics:a: run both CycleCountHeuristics and output the best"
	    + "\n            e: use the EdgeCostHeuristic"
	    + "\n            b: use the CycleCountHeuristic.MORE_SMALLER_LESS_BIGGER (default)"
	    + "\n            c: use the CycleCountHeuristic.CYCLE_COUNT"
	    + "\n output:    G: generic output (same as default but wrapped)"
	    + "\n            L: LaTeX ouput"
	    + "\n            M: Maple output"
	    + "\n            R: Racah output"
	    + "\n            l: output macros for the LaTeX format"
	    + "\n            m: output macros for the Maple format"
	    + "\n general:   v: verbose output (same as \"-or\")"
	    + "\n            o: output graph operations"
	    + "\n            r: output rule selection"
	    + "\n            g: ouput graph after each step in gml-format";
	if (args.length < 1){
	    System.err.println(usage);
	    return;
	}
	boolean outputoperations = false;
	boolean outputrules = false;
	boolean outputgml = false;
	int heuristic = MORE_SMALLER_LESS_BIGGER;
	for (int i = 0; i < args.length-1; i++)
	    if (args[i].charAt(0) == '-'){
		for (int k = 1; k < args[i].length(); k++)
		    switch (args[i].charAt(k)){
			//Heuristics
		    case 'a':
			heuristic = ALL;
			break;
		    case 'e':
			heuristic = EDGE_COST;
			break;
		    case 'b':
			heuristic = MORE_SMALLER_LESS_BIGGER;
			break;
		    case 'c':
			heuristic = CYCLE_COUNT;
			break;
			// Output format
		    case 'G':
			grv = new GRWrappedStringVisitor();
			break;
		    case 'L':
			grv = new GRWrappedLaTeXVisitor();
			break;
		    case 'M':
			grv = new GRWrappedMapleVisitor();
			break;
		    case 'R':
			grv = new GRWrappedRacahVisitor();
			break;
		    case 'l':
			System.out.println(GRWrappedLaTeXVisitor.macros());
			return;
		    case 'm':
			System.out.println(GRWrappedMapleVisitor.macros());
			return;
			//General options
		    case 'v':
			outputoperations = outputrules  = true;
			break;
		    case 'o':
			outputoperations = true;
			break;
		    case 'r': 
			outputrules = true;
			break;
		    case 'g':
			outputgml = true;
			break;
		    default:
			System.err.println("Unknown option -" + 
					   args[i].charAt(k) + "\n" + usage);
			return;
		    }
	    }
	YutsisGraph y;
	String gmlbasename;
	try {	  
	    if (args[args.length-1].indexOf('|') != -1){
		y = new YutsisGraph(args[args.length-1]);
		gmlbasename = "newgraph"+(y.nrOfNodes()/2);
	    }
	    else {
		int format = YutsisGraph.guessFileFormat(args[args.length-1]);
		if (format == -1){
		    System.err.println(usage);
		    return;
		}
		y = new YutsisGraph(new BufferedReader(new FileReader(args[args.length-1])),
				    format);
		gmlbasename = 
		    args[args.length-1].substring
		    (0,args[args.length-1].lastIndexOf('.'));
	    }
	}
	catch (FileNotFoundException e){
	    System.err.println("File " + args[args.length-1] + " not found.");
	    return;
	}
	catch (IllegalArgumentException e){
	    System.err.println(e.getMessage());
	    return;
	}
	gmlbasename = outputgml ? gmlbasename+".cca" : null;
	y.setLogStream(outputoperations ? System.out : null);
	CCAHeuristic h = (heuristic == EDGE_COST) ? 
	    (CCAHeuristic) new EdgeCostHeuristic(y) :
		(CCAHeuristic) new CycleCountHeuristic(y);
	if (heuristic == CYCLE_COUNT)//MORE_SMALLER_LESS_BIGGER is default
	    ((CycleCountHeuristic) h).setStrategy
		(CycleCountHeuristic.CYCLE_COUNT);
	CycleCostAlgorithm cca;
	if (heuristic == ALL){
	    YutsisGraph yclone = (YutsisGraph) y.clone();
	    CycleCountHeuristic cch = new CycleCountHeuristic(yclone);
	    cch.setStrategy(CycleCountHeuristic.CYCLE_COUNT);
	    CycleCostAlgorithm ccatemp = 
		reduceYutsisGraph(yclone, cch, 
				  outputrules ? System.out : null, 
				  gmlbasename);
	    cca = reduceYutsisGraph(y, h, outputrules ? 
				System.out : null, gmlbasename);
	    if (cca.nrOfInterchanges() <= ccatemp.nrOfInterchanges()){
		System.out.println("Results from CycleCount.MORE_SMALLER_LESS_BIGGER ("
				   +(ccatemp.nrOfInterchanges()
				     -cca.nrOfInterchanges())
				   +" IC's better than CycleCount.CYCLE_COUNT):");
		cca = ccatemp;
		y = yclone;
	    }
	    else
		System.out.println("Results from CycleCount.CYCLE_COUNT ("
				   +(cca.nrOfInterchanges()
				     -ccatemp.nrOfInterchanges())
				   +" IC's better than CycleCount.MORE_SMALLER_LESS_BIGGER):");
	}
	else
	    cca = reduceYutsisGraph(y, h, outputrules ? 
				    System.out : null, gmlbasename);
	outputResults(y,cca,grv);
    }

    /** 
     * This function calls performOperation
     * until the given Yutsis object is equal to a triangular delta and
     * returns the used CycleCostAlgorithm object.
     * @param y the Yutsis to be reduced
     * @param h the heuristic to be used as a CCAHeuristic object
     * @param ruleslogstream the stream to where the applied rules have 
     *                       to be logged 
     * @param gmlbasename the basename of the files to which the graphs
     *                    will be written in gml-format (graphlet) after
     *                    each step or null for no gml output.
     * @return the used CycleCostAlgorithm object.
     * @see #performOperation
     * @see Yutsis
     * @see CCAHeuristic
     **/
    public static 
	CycleCostAlgorithm reduceYutsisGraph(Yutsis y, 
					     CCAHeuristic h, 
					     PrintStream ruleslogstream,
					     String gmlbasename){
	CycleCostAlgorithm cca = new CycleCostAlgorithm(y,h);
	cca.setLogStream(ruleslogstream);
	if (gmlbasename != null)
	    cca.setGmlOutputBasename(gmlbasename);
	cca.reduce();
	return cca;
    }

    /**
     * Outputs the results of the reduction process; if the Yutsis
     * object is a instance of YutsisGraph, the generated summation
     * formula, an instance of GenRecoupCoeff, is printed to System.out.
     * @param y the Yutsis object for which the results will be printed.
     * @param cca the CycleCostAlgorithm object used to reduce y.
     * @see Yutsis
     * @see YutsisGraph
     * @see GenRecoupCoeff
     **/
    public static void outputResults(Yutsis y, CycleCostAlgorithm cca){
	outputResults(y, cca, null, System.out);
    }

    /**
     * Outputs the results of the reduction process; if the Yutsis
     * object is a instance of YutsisGraph, the generated summation
     * formula, an instance of GenRecoupCoeff, is printed to the given
     * PrintStream in the format delivered by the GRVisitor.
     * @param y the Yutsis object for which the results will be printed.
     * @param cca the CycleCostAlgorithm object used to reduce y.
     * @param grv the GRVisitor visiting the GenRecoupCoeff
     * @param out the PrintStream to which the output will be printed.
     * @see Yutsis
     * @see YutsisGraph
     * @see GRVisitor
     * @see GenRecoupCoeff
     **/
    public static void outputResults(Yutsis y, CycleCostAlgorithm cca,
				     GRVisitor grv){
	outputResults(y, cca, grv, System.out);
    }

    /**
     * Outputs the results of the reduction process; if the Yutsis
     * object is a instance of YutsisGraph, the generated summation
     * formula, an instance of GenRecoupCoeff, is printed to the given
     * PrintStream in the format delivered by the GRVisitor.
     * @param y the Yutsis object for which the results will be printed.
     * @param cca the CycleCostAlgorithm object used to reduce y.
     * @param grv the GRVisitor visiting the GenRecoupCoeff
     * @param out the PrintStream to which the output will be printed.
     * @see Yutsis
     * @see YutsisGraph
     * @see GRVisitor
     * @see GenRecoupCoeff
     **/
    public static void outputResults(Yutsis y, CycleCostAlgorithm cca,
				     GRVisitor grv, PrintStream out){
	if (y instanceof YutsisGraph){
	    YutsisGraph yg = (YutsisGraph) y;
	    System.out.println("#summations: " + 
			       yg.genRecoupCoeff().nrOfSummations()
			       + " #6j's: " + yg.genRecoupCoeff().nrOf6js());
	    if (grv == null)
		System.out.println(yg.genRecoupCoeff());
	    else {
		yg.genRecoupCoeff().accept(grv);
		System.out.println(grv.result());
	    }
	}
	else
	    System.out.println("#ic's cca:"+cca.nrOfInterchanges());
    }
}
