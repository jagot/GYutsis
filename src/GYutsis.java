/* GYutsis.java
   2002 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.Iterator;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;

/** 
 * This class implements a GUI illustrating the algorithm implemented
 * by {@link CycleCostAlgorithm}. In advanced mode the algorithm can
 * be followed step by step while keeping track of the changes in the
 * underlying Yutsis Graph and the applied rules.
 * @see YutsisGraph 
 * @see GenRecoupCoeff 
 * @see StreamPanel
 * @author Dries.VanDyck@rug.ac.be 
 **/
public class GYutsis extends JFrame 
    implements ActionListener, ChangeListener, ComponentListener{
    static final String VERSION = "1.1";
    static final int OUTPUTROWS = 8;
    static final int OUTPUTCOLUMNS = 50;
    static final String HELPFILE = "GYutsisHelp.html";
    static final String ABOUTTEXT = 
	"GYutsis " + VERSION+ "\n"+ 
	" Copyright (c) 2002 Dries Van Dyck\n"+
	" Homepage: http://caagt.rug.ac.be/yutsis/yutsis-software.caagt"+
	" Author: Dries.VanDyck@rug.ac.be\n" + 
	"             Researchgroup Combinatorial Algorithms & Algoritmic Graph Theory\n"+
	"             Department of Applied Mathematics & Computer Science\n"+
	"             University of Ghent\n"+
	"             Krijgslaan 281 -- S9, Ghent, Belgium\n"+
	" License: free for academic use, otherwise contact author";
    
    /**
     * Generic output.
     **/ 
    public static final int GENERIC_OUTPUT = 0;

    /**
     * Generate LaTeX output.
     **/
    public static final int MAPLE_OUTPUT = 1;

    /**
     * Generate Maple output.
     **/
    public static final int LATEX_OUTPUT = 2;

    /**
     * Generate RACAH output.
     **/
    public static final int RACAH_OUTPUT = 4;

    /**
     * Use an instance of EdgeCostHeuristic as heuristic.
     * @see EdgeCostHeuristic
     **/
    public static final int EDGE_COST_HEURISTIC = 0;

    /**
     * Use an instance of CycleCountHeuristic.MORE_SMALLER_LESS_BIGGER as
     * heuristic (default).
     * @see CycleCountHeuristic
     * @see CycleCountHeuristic#MORE_SMALLER_LESS_BIGGER
     **/
    public static final int MORE_SMALLER_LESS_BIGGER_HEURISTIC = 2;

    /**
     * Use an instance of CycleCountHeuristic.CYCLE_COUNT as heuristic.
     * @see CycleCountHeuristic
     * @see CycleCountHeuristic#CYCLE_COUNT
     **/
    public static final int CYCLE_COUNT_HEURISTIC = 4;
    

    // Underlying model
    YutsisGraph y;
    CycleCostAlgorithm cca;
    int heuristic = MORE_SMALLER_LESS_BIGGER_HEURISTIC;
    int outputformat = GENERIC_OUTPUT;
    boolean reduced = false;
    int newgraphnumber = 0;
    // IO panel
    String lastdir = System.getProperty("user.dir");
    JPanel iopanel;
    JTextField input,braket;
    JButton reducebutton, browsebutton, toggleadvanced, 
	savebraket, saveformula;
    JScrollPane scrollpane;
    JTextArea output;
    JTextField nrofsummations;
    JTextField nrof6js;
    //Advanced Panel
    boolean advancedmode = false;
    JPanel advancedpanel, buttonpanel;
    StreamPanel rules, operations, user;
    PrintStream userstream;
    //Advanced Buttons
    JButton graph, savegraph, cycles, bestcycle, step, generatemacros;
    JCheckBox saveeachstep;
    //Menus
    JMenuBar menubar;
    JMenu filemenu, viewmenu, operationsmenu, 
	heuristicmenu, outputmenu, helpmenu;
    //File Menu
    JMenuItem browsemi, savebraketmi, saveformulami, exitmi;
    //View Menu
    JMenuItem toggleadvancedmi;
    //Operations Menu
    JMenuItem graphmi, savegraphmi, cyclesmi, bestcyclemi, stepmi, reducemi;
    JCheckBoxMenuItem saveeachstepmi;
    //Heuristic Menu
    JRadioButtonMenuItem edgecostmi, moresmallerlessbiggermi, cyclecountmi;
    //Output Menu
    JRadioButtonMenuItem genericmi, maplemi, latexmi, racahmi;
    JCheckBoxMenuItem usemacrosmi;
    JMenuItem generatemacrosmi;
    //Help Menu
    JMenuItem helpmi, aboutmi;
    
    /** 
     * Creates the GYutsis window.
     **/
    public GYutsis(){
	super("GYutsis " + VERSION + " -- ");
	iopanel = new JPanel(new GridBagLayout());
	createIOPanel(iopanel);
	setContentPane(iopanel);
	createAdvancedPanel();
	createMenus();
    }

    /** 
     * Creates the GYutsis window with the given YutsisGraph.
     * @param y the current YutsisGraph defining the problem
     * @see YutsisGraph
     **/
    public GYutsis(YutsisGraph y) throws IOException{
	this();
	setTitle("GYutsis " + VERSION + " -- new braket");
	setYutsis(y);
    }

    /** 
     * Creates the GYutsis window with title <code>title</code> and
     * reads the graph from the File <code>file</code>.
     * @param title the title of the window
     * @param f the file from which a problem is read
     **/
    public GYutsis(File f) throws IOException{
	this();
	setYutsisFromFile(f);
    }
    
    private void createIOPanel(Container panel){
	// Yutsis Graph: label
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.insets = new Insets(4,2,2,4);
	gbc.gridx=0; gbc.gridy=0;
	gbc.gridwidth = 1; gbc.gridheight= 1;
	panel.add(new JLabel("Yutsis Graph:"),gbc);
	// Input textfield
	input = new JTextField(40);
	input.setToolTipText("Fill in filename or BRAKET of a Yutsis Graph");
	input.addActionListener(this);
	gbc.gridwidth = 2; gbc.gridx = 1;
	gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
	panel.add(input,gbc);
	// Browse button
	browsebutton = new JButton("Browse");
	browsebutton.setToolTipText("Browse files for a Yutsis Graph (BRAKET or YTS format)");
	browsebutton.addActionListener(this);
	gbc.gridx = 3; gbc.gridwidth = 1;
	gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
	panel.add(browsebutton,gbc);
	// Save braket butotn
	savebraket = new JButton("Save Braket");
	savebraket.setEnabled(false);
	savebraket.setToolTipText("Save the braket to a file");
	savebraket.addActionListener(this);
	gbc.gridx = 4;
	panel.add(savebraket,gbc);
	// Braket: label
	gbc.gridx = 0; gbc.gridy = 1;
	panel.add(new JLabel("Braket:"),gbc);
	// Braket textfield
	braket = new JTextField(40);
	braket.setEditable(false);
	braket.setToolTipText("The BRAKET notation of this Yutsis graph");
	braket.addActionListener(this);
	gbc.gridwidth = 2; gbc.gridx = 1;
	gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
	panel.add(braket,gbc);
	// Reduce button
	reducebutton = new JButton("Reduce");
	reducebutton.setEnabled(false);
	reducebutton.setToolTipText("Reduce the Yutsis Graph to a triangular delta");
	reducebutton.addActionListener(this);
	gbc.gridx = 3; gbc.gridwidth = 1;
	gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
	panel.add(reducebutton,gbc);
	// Save formula button
	saveformula = new JButton("Save Formula");
	saveformula.setEnabled(false);
	saveformula.setToolTipText("Save the formula to a file");
	saveformula.addActionListener(this);
	gbc.gridx = 4;
	panel.add(saveformula,gbc);
	// Summation Formula: label
	gbc.gridx = 0; gbc.gridy = 2;
	gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.NORTH;
	gbc.weighty = 0.5;
	panel.add(new JLabel("Summation Formula:"),gbc);
	// #Summations: label 
	gbc.anchor = GridBagConstraints.CENTER; 
	gbc.gridy = 3; gbc.gridwidth = 1;
	gbc.weighty = 0;
	panel.add(new JLabel("#Summations:"),gbc);
	// #Summations: textfield
	nrofsummations = new JTextField(3);
	nrofsummations.setEditable(false);
	gbc.anchor = GridBagConstraints.WEST;
	gbc.gridx = 1; gbc.gridy = 3;
	panel.add(nrofsummations,gbc);
	// #Wigner 6j's: label
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.gridx = 0; gbc.gridy = 4; 
	gbc.gridwidth = 1;
	panel.add(new JLabel("#Wigner 6j's:"),gbc);
	// #Wigner 6j's: textfield
	nrof6js = new JTextField(3);
	nrof6js.setEditable(false);
	gbc.anchor = GridBagConstraints.WEST;
	gbc.gridx = 1; gbc.gridy = 4;
	panel.add(nrof6js,gbc);
	// Advanced Panel button
	toggleadvanced = new JButton("Show Advanced Panel");
	toggleadvanced.addActionListener(this);
	gbc.anchor = GridBagConstraints.SOUTH;
	gbc.weighty = 0.5;
	gbc.gridx = 0; gbc.gridy = 5;
	gbc.gridwidth = 2;
	panel.add(toggleadvanced,gbc);
	// Output textarea
	output = new JTextArea(OUTPUTROWS,OUTPUTCOLUMNS);
	output.setEditable(false);
	gbc.insets = new Insets(0,0,0,0);
	gbc.gridx = 2; gbc.gridy = 2;
	gbc.gridwidth = 3; gbc.gridheight = 4;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1; gbc.weighty = 1;
	scrollpane = new JScrollPane(output,
				     JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				     JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	scrollpane.addComponentListener(this);
	panel.add(scrollpane,gbc);
    }

    private void createAdvancedPanel(){
	advancedpanel = new JPanel(new BorderLayout());
	// Create TabbedPane
	JTabbedPane tabpane = new JTabbedPane();
	user = new StreamPanel(15,30,"User commands output:");
	userstream = user.getPrintStream();
	tabpane.add("User",user);
	rules = new StreamPanel(15,30,"Rule selection output:");
	tabpane.add("Rules",rules);
	operations = new StreamPanel(15,30,"Graph operations output:");
	tabpane.add("Operations",operations);
	tabpane.setSelectedIndex(0);
	advancedpanel.add(tabpane,BorderLayout.CENTER);
	// Create Panel with user operation buttons
	buttonpanel = new JPanel(new GridBagLayout());
	// Set GridBag Constraints
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.gridheight = 1;
	gbc.gridwidth = 1;
	gbc.fill = GridBagConstraints.NONE;
	JLabel userlabel = new JLabel("User operations:");
	buttonpanel.add(userlabel,gbc);
	// add buttons
	gbc.fill = GridBagConstraints.HORIZONTAL;
	graph = new JButton("Print Graph");
	graph.setToolTipText("Print the graph textually to the User-pane");
	graph.setEnabled(false);
	gbc.insets = new Insets(16,0,0,0); //top, left, bottom, right
	gbc.gridy = gbc.gridy+1;// (0,1)
	graph.addActionListener(this);
	buttonpanel.add(graph,gbc);
	savegraph = new JButton("Save Graph");
	savegraph.setToolTipText("Save the graph graphically in gml-format for the graph drawing tool Graphlet");
	savegraph.setEnabled(false);
	savegraph.addActionListener(this);
	gbc.gridy = gbc.gridy+1; //(0,2)
	gbc.insets = new Insets(0,0,0,0);
	buttonpanel.add(savegraph,gbc);
	saveeachstep = new JCheckBox("Save each step", false);
	saveeachstep.setToolTipText("Save the graph graphically after each step");
	saveeachstep.setEnabled(false);
	saveeachstep.addActionListener(this);
	gbc.gridy = gbc.gridy+1;//(0,3)
	buttonpanel.add(saveeachstep,gbc);
	cycles = new JButton("Cycles");
	cycles.setToolTipText("Show all girth cycles");
	cycles.setEnabled(false);
	cycles.addActionListener(this);
	gbc.insets = new Insets(16,0,0,0);
	gbc.gridy = gbc.gridy+1;//(0,4)
	buttonpanel.add(cycles,gbc);
	bestcycle = new JButton("Best Cycle");
	bestcycle.setToolTipText("Show the best cycle and the best edge to interchange (not for triangles)");
	bestcycle.setEnabled(false);
	bestcycle.addActionListener(this);
	gbc.insets = new Insets(0,0,0,0); //top, left, bottom, right
	gbc.gridy = gbc.gridy+1;// (0,5)
	buttonpanel.add(bestcycle,gbc);
	step = new JButton("Step");
	step.setToolTipText("Perform one step in the reduction algorithm");
	step.setEnabled(false);
	step.addActionListener(this);
	gbc.gridy = gbc.gridy+1;// (0,6)
	buttonpanel.add(step,gbc);
	generatemacros = new JButton("Print Macros");
	generatemacros.setToolTipText("Generate macros (to be used with output in LaTeX or Maple format).");
	generatemacros.addActionListener(this);
	gbc.weighty = 1;
	gbc.anchor = GridBagConstraints.NORTH;
	gbc.gridy = gbc.gridy + 1;
	gbc.insets = new Insets(16,0,0,0);
	generatemacros.setEnabled(false);
	buttonpanel.add(generatemacros,gbc);
	advancedpanel.add(buttonpanel,BorderLayout.EAST);
    }
    
    private void createMenus(){
	menubar = new JMenuBar();
	setJMenuBar(menubar);
	//File menu
	filemenu = new JMenu("File");
	browsemi = new JMenuItem("Open", 'O');
	browsemi.addActionListener(this);
	filemenu.add(browsemi);
	filemenu.addSeparator();
	savebraketmi = new JMenuItem("Save Braket", 'S');
	savebraketmi.addActionListener(this);
	savebraketmi.setEnabled(false);
	filemenu.add(savebraketmi);
	filemenu.addSeparator();
	saveformulami = new JMenuItem("Save Formula", 'F');
	saveformulami.addActionListener(this);
	saveformulami.setEnabled(false);
	filemenu.add(saveformulami);
	filemenu.addSeparator();
	exitmi = new JMenuItem("Exit", 'X');
	exitmi.addActionListener(this);
	filemenu.add(exitmi);
	menubar.add(filemenu);
	//View menu
	viewmenu = new JMenu("View");
	toggleadvancedmi = new JMenuItem("Show Advanced Panel",'A');
	toggleadvancedmi.addActionListener(this);
	viewmenu.add(toggleadvancedmi);
	menubar.add(viewmenu);
	//Operations menu
	operationsmenu = new JMenu("Operations");
	reducemi = new JMenuItem("Reduce",'R');
	reducemi.addActionListener(this);
	reducemi.setEnabled(false);
	operationsmenu.add(reducemi);
	menubar.add(operationsmenu);
	//Additional operations menu item when in advanced mode
	graphmi = new JMenuItem("Print Graph", 'G');
	graphmi.addActionListener(this);
	graphmi.setEnabled(false);
	savegraphmi = new JMenuItem("Save Graph", 'S');
	savegraphmi.addActionListener(this);
	savegraphmi.setEnabled(false);
	saveeachstepmi = new JCheckBoxMenuItem("Save each step",false);
	saveeachstepmi.setEnabled(false);
	saveeachstepmi.addActionListener(this);
	cyclesmi = new JMenuItem("Girth Cycles", 'C');
	cyclesmi.addActionListener(this);
	cyclesmi.setEnabled(false);
	bestcyclemi = new JMenuItem("Best Cycle",'B');
	bestcyclemi.addActionListener(this);
	bestcyclemi.setEnabled(false);
	stepmi = new JMenuItem("Step", 't');
	stepmi.addActionListener(this);
	stepmi.setEnabled(false);
	//Heuristic menu
	heuristicmenu = new JMenu("Heuristic");
	ButtonGroup heuristicgrp = new ButtonGroup();
	edgecostmi = 
	    new JRadioButtonMenuItem("Edge Cost Heuristic", false);
	edgecostmi.addActionListener(this);
	heuristicmenu.add(edgecostmi); heuristicgrp.add(edgecostmi);
	moresmallerlessbiggermi = 
	    new JRadioButtonMenuItem("More Smaller/Less Bigger Heuristic", 
				     true);
	moresmallerlessbiggermi.addActionListener(this);
	heuristicmenu.add(moresmallerlessbiggermi); 
	heuristicgrp.add(moresmallerlessbiggermi);
	cyclecountmi = 
	    new JRadioButtonMenuItem("Cycle Count Heuristic", false);
	cyclecountmi.addActionListener(this);
	heuristicmenu.add(cyclecountmi); heuristicgrp.add(cyclecountmi);
	menubar.add(heuristicmenu);
	// Output menu
	outputmenu = new JMenu("Output");
	ButtonGroup outputgrp = new ButtonGroup();
	genericmi = new JRadioButtonMenuItem("Generic",true);
	outputmenu.add(genericmi); outputgrp.add(genericmi);
	genericmi.addActionListener(this);
	latexmi = new JRadioButtonMenuItem("LaTeX");
	outputmenu.add(latexmi); outputgrp.add(latexmi);
	latexmi.addActionListener(this);
	maplemi = new JRadioButtonMenuItem("Maple");
	outputmenu.add(maplemi); outputgrp.add(maplemi);
	maplemi.addActionListener(this);
	racahmi = new JRadioButtonMenuItem("Racah");
	outputmenu.add(racahmi); outputgrp.add(racahmi);
	racahmi.addActionListener(this);
	menubar.add(outputmenu);
	usemacrosmi = new JCheckBoxMenuItem("Use Macros");
	usemacrosmi.addActionListener(this);
	generatemacrosmi = new JMenuItem("Generate macros");
	generatemacrosmi.addActionListener(this);
	// Help menu on the right
	helpmenu = new JMenu("Help");
	helpmi = new JMenuItem("Help",'H');
	helpmi.addActionListener(this);
	helpmenu.add(helpmi);
	aboutmi = new JMenuItem("About",'A');
	aboutmi.addActionListener(this);
	helpmenu.add(aboutmi);
	menubar.add(Box.createHorizontalGlue());
	menubar.add(helpmenu);
    }

    private void setLogStreams(){
	y.setLogStream(operations.getPrintStream());
	cca.setLogStream(rules.getPrintStream());
    }

    /** 
     * Changes the YutsisGraph y.
     * @param y the new YutsisGraph
     * @see YutsisGraph
     **/
    void setYutsis(YutsisGraph y){
	this.y = y;
	braket.setText(y.braket());
	cca = new CycleCostAlgorithm(y, getHeuristic());
	y.addChangeListener(this);
	setLogStreams();
	boolean notreduced = !y.triangularDelta();
	reducebutton.setEnabled(notreduced);
	reducemi.setEnabled(notreduced);
	user.clear();
	rules.clear();
	operations.clear();
	graph.setEnabled(true);
	graphmi.setEnabled(true);
	savegraph.setEnabled(true);
	savegraphmi.setEnabled(true);
	saveeachstep.setSelected(false);
	saveeachstep.setEnabled(true);
	saveeachstepmi.setState(false);
	saveeachstepmi.setEnabled(true);
	savebraket.setEnabled(true);
	savebraketmi.setEnabled(true);
	if (!advancedmode){
	    output.setText("");
	    nrofsummations.setText("");
	    nrof6js.setText("");
	}
	stateChanged(new ChangeEvent(y));
    }

    /** 
     * Reads a new YutsisGraph from the file
     * <code>filename</code>.
     * @param file the file from which a new YutsisGraph is read
     * @see YutsisGraph
     **/
    void setYutsisFromFile(File file){
	try{
	    setYutsis(new YutsisGraph(new BufferedReader(new 
		FileReader(file)), YutsisGraph.
				      guessFileFormat(file.getPath())));
	    updateLastDir(file.getParent() == null ? 
			  lastdir : file.getParent());
	    input.setText(file.getName());
	    setTitle("GYutsis " + VERSION + " -- " + file.getName());
	}
	catch (IllegalArgumentException iae){
	    JOptionPane.showMessageDialog(this, iae.getMessage(),
					  "Illegal input",
					  JOptionPane.ERROR_MESSAGE);
	}
	catch (IOException ioe){
	    JOptionPane.showMessageDialog(this, ioe instanceof 
					  FileNotFoundException ?
					  "File " + file.getName() + 
					  " not found." :
					  "An unknown I/O error occured. Check your write permissions and if there is sufficient space.", 
					  "I/O error", 
					  JOptionPane.ERROR_MESSAGE);
	}
    }

    private void reduced(){
	reducebutton.setEnabled(false);
	reducemi.setEnabled(false);
	cycles.setEnabled(false);
	cyclesmi.setEnabled(false);
	bestcycle.setEnabled(false);
	bestcyclemi.setEnabled(false);
	step.setEnabled(false);
	stepmi.setEnabled(false);
    }
    
    private void toggleAdvancedPanel(){
	if (advancedmode){
	    setContentPane(iopanel);
	    toggleadvanced.setText("Show Advanced Panel");
	    toggleadvancedmi.setText("Show Advanced Panel");
	    operationsmenu.removeAll();
	    operationsmenu.add(reducemi);
	    if (!reduced){
		output.setText("");
		nrofsummations.setText("");
		nrof6js.setText("");
		saveformula.setEnabled(false);
		saveformulami.setEnabled(false);
	    }
	    if (outputformat == LATEX_OUTPUT || outputformat == MAPLE_OUTPUT)
		outputmenu.remove(generatemacrosmi);
	    if (outputformat == MAPLE_OUTPUT)
		outputmenu.remove(outputmenu.getItemCount()-1);// the separator
	}
	else{
	    setContentPane(new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					  iopanel,advancedpanel));
	    toggleadvanced.setText("Hide Advanced Panel");
	    toggleadvancedmi.setText("Hide Advanced Panel");
	    operationsmenu.removeAll();
	    operationsmenu.add(graphmi);
	    operationsmenu.add(savegraphmi);
	    operationsmenu.add(saveeachstepmi);
	    operationsmenu.addSeparator();
	    operationsmenu.add(cyclesmi);
	    operationsmenu.add(bestcyclemi);
	    operationsmenu.addSeparator();
	    operationsmenu.add(stepmi);
	    operationsmenu.add(reducemi);
	    if (outputformat == MAPLE_OUTPUT)
		outputmenu.addSeparator();
	    if (outputformat == LATEX_OUTPUT || outputformat == MAPLE_OUTPUT)
		outputmenu.add(generatemacrosmi);	    
	    if (y != null){ 
		outputFormula();
		saveformula.setEnabled(true);
		saveformulami.setEnabled(true);
	    }
	}
	advancedmode = !advancedmode;
	pack();
    }

    /** 
     * Default implementation of the ComponentListener.
     **/
    public void componentHidden(ComponentEvent e){}
    
    /** 
     * Default implementation of the ComponentListener.
     **/
    public void componentMoved(ComponentEvent e){}
    
    /** 
     * Default implementation of the ComponentListener.
     **/
    public void componentShown(ComponentEvent e){}
    
    /** 
     * Implementation of the ComponentListener, reuse 
     * GRWrappedStringVisitor to wrap the formula.
     * @see GRWrappedStringVisitor
     **/
    public void componentResized(ComponentEvent e){
	//Listens to the scrollpane...
	if (y != null && (advancedmode || reduced))
	    outputFormula();
    }

    /** 
     * Implementation of the ActionListener interface.
     **/
    public void actionPerformed(ActionEvent e){
	if (e.getSource() == reducebutton || e.getSource() == reducemi)
	    reduce();
	else if (e.getSource() == browsebutton 
		 || e.getSource() == browsemi)
	    browse();
	else if (e.getSource() == input){
	    String text = input.getText().trim();
	    if (text.charAt(0) == '<'){
		try{ 
		    setYutsis(new YutsisGraph(text));
		    setTitle("GYutsis " + VERSION + " -- new braket " + 
			     ++newgraphnumber);
		}
		catch (IllegalArgumentException iae){
		    JOptionPane.showMessageDialog(this, iae.getMessage(),
						  "Illegal input",
						  JOptionPane.ERROR_MESSAGE);
		}
	    }
	    else {// filename
		File f = new File(text);
		if (f.exists())
		    setYutsisFromFile(f);
		else {
		    File f2 = new File(lastdir,f.getName());
		    if (f2.exists())
			setYutsisFromFile(f2);
		    else 
			setYutsisFromFile(f);
		}
	    }
	}
	else if (e.getSource() == savebraket 
		 || e.getSource() == savebraketmi)
	    saveBraket();
	else if (e.getSource() == saveformula
		 || e.getSource() == saveformulami)
	    saveFormula();
	else if (e.getSource() == toggleadvanced 
		 || e.getSource() == toggleadvancedmi)
	    toggleAdvancedPanel();
	else if (e.getSource() == edgecostmi)
	    setHeuristic(EDGE_COST_HEURISTIC);
	else if (e.getSource() == moresmallerlessbiggermi)
	    setHeuristic(MORE_SMALLER_LESS_BIGGER_HEURISTIC);
	else if (e.getSource() == cyclecountmi)
	    setHeuristic(CYCLE_COUNT_HEURISTIC);
	else if (e.getSource() == genericmi)
	    setOutputFormat(GENERIC_OUTPUT);
	else if (e.getSource() == latexmi)
	    setOutputFormat(LATEX_OUTPUT);
	else if (e.getSource() == maplemi)
	    setOutputFormat(MAPLE_OUTPUT);
	else if (e.getSource() == racahmi)
	    setOutputFormat(RACAH_OUTPUT);
	else if (e.getSource() == usemacrosmi)
	    outputFormula();
	else if (e.getSource() == helpmi)
	    showHelpFile();//show help dialog
	else if (e.getSource() == aboutmi)
	    JOptionPane.showMessageDialog(this, ABOUTTEXT, 
					  "About GYutsis " + VERSION,
					  JOptionPane.INFORMATION_MESSAGE);
	else if (e.getSource() == exitmi)
	    System.exit(0);
	else if(advancedmode){
	    if (e.getSource() == graph || e.getSource() == graphmi)
		printGraph();
	    else if (e.getSource() == savegraph 
		     || e.getSource() == savegraphmi){
		saveGraph();
	    }
	    else if (e.getSource() == saveeachstep){
		saveeachstepmi.setState(saveeachstep.isSelected());
		toggleSaveEachStep();
	    }
	    else if (e.getSource() == saveeachstepmi){
		saveeachstep.setSelected(saveeachstepmi.getState());
		toggleSaveEachStep();
	    }
	    else if (e.getSource() == cycles 
		     || e.getSource() == cyclesmi)
		printCycles();
	    else if (e.getSource() == bestcycle
		     ||e.getSource() == bestcyclemi)
		printBestCycle();
	    else if (e.getSource() == step || e.getSource() == stepmi)
		step();
	    else if (e.getSource() == generatemacrosmi 
		     || e.getSource() == generatemacros)
		printMacros();
	}	
    }

    private void saveGraph(){
	JFileChooser fc = new JFileChooser(lastdir);
	int answer = fc.showSaveDialog(this);
	if (answer == JFileChooser.APPROVE_OPTION){
	    File f = fc.getSelectedFile();
	    try {
		PrintStream ps = 
		    new PrintStream(new 
			BufferedOutputStream(new FileOutputStream(f)));
		y.toGml(ps);
		ps.close();
	    }
	    catch(IOException ioe){
		JOptionPane.showMessageDialog(this,"An unknown I/O error occured. Check your write permissions and if there is sufficient space.", 
					      "I/O error", 
					      JOptionPane.ERROR_MESSAGE);
	    }
	}
    }

    private void toggleSaveEachStep(){
	if (!saveeachstep.isSelected()){
	    cca.setGmlOutputBasename(null);
	    return;
	}
	String filename = input.getText().trim();
	if (filename.charAt(0) == '<'){ // no filename yet
	    cca.setGmlOutputBasename("newgraph"+newgraphnumber);
	}
	else if (filename.toLowerCase().endsWith(".braket")){ // is a filename
	    int index = filename.toLowerCase().lastIndexOf(".braket");
	    cca.setGmlOutputBasename(filename.substring(0,index));
	}
	else if (filename.toLowerCase().endsWith(".yts")){
	    int index = filename.toLowerCase().lastIndexOf(".braket");
	    cca.setGmlOutputBasename(filename.substring(0,index));
	}
	else 
	    cca.setGmlOutputBasename(filename);
    }
    
    private void printMacros(){
	userstream.println(outputformat == LATEX_OUTPUT ?
			   GRWrappedLaTeXVisitor.macros() : 
			   GRWrappedMapleVisitor.macros());
    }

    private void reduce() { cca.reduce();}

    private void browse() {
	JFileChooser fc = new JFileChooser(lastdir);
	int answer = fc.showOpenDialog(this);
	if (answer == JFileChooser.APPROVE_OPTION)
	    setYutsisFromFile(fc.getSelectedFile());
    }

    private void printGraph(){ userstream.println(y);}

    private void printCycles(){
	int [] abubble = y.bubble();
	if (abubble[0] != -1)
	    userstream.println("Bubble between nodes " 
			       + abubble[0] + " " + abubble[1] + '\n');
	else {
	    ArrayList girthcycles = cca.heuristic().cycleGenerator().girthCycles();
	    for (int i = 0; i < girthcycles.size(); i++)
		userstream.println(girthcycles.get(i));
	    userstream.println();
	}
    }

    private void printBestCycle(){
	int [] abubble = y.bubble();
	if (abubble[0] != -1)
	    userstream.println("Bubble between nodes " 
			       + abubble[0] + " " + abubble[1]+ '\n');
	else {
	    int [] bestedge = {-1,-1};
	    int [] besticnodes = {-1,-1};
	    ArrayList candidates = new ArrayList();
	    Cycle c = 
		cca.heuristic().bestCycle(bestedge, besticnodes,candidates);
	    userstream.println("Best cycle: " + c 
			       + (bestedge[0] != -1 ? " ; best edge: " 
				  +  bestedge[0] + " " + bestedge[1] 
				  : "" ));
	    if (candidates.size() > 0){
		userstream.println("Equivalent operations:");
		for (Iterator i = candidates.iterator(); i.hasNext();){
		    String operation = (String) i.next();
		    userstream.println(operation);
		}
	    }
	    userstream.println();
	}
    }

    private void step() { cca.performOperation(); }
    
    private void saveBraket(){ 
	File f = saveToFileDialog(y.braket()); 
	if (f != null){
	    input.setText(f.getName());
	    setTitle("GYutsis " + VERSION + " -- " + f.getName());
	    updateLastDir(f.getParent());
	}
    }

    private void saveFormula(){ 
	saveToFileDialog(y.genRecoupCoeff().toString());
    }

    private void updateLastDir(String newlastdir){
	lastdir = newlastdir;
	user.setSaveDir(lastdir);
	rules.setSaveDir(lastdir);
	operations.setSaveDir(lastdir);
    }

    private File saveToFileDialog(String tosave){
	JFileChooser fc = new JFileChooser(lastdir);
	int answer = fc.showSaveDialog(this);
	if (answer == JFileChooser.APPROVE_OPTION){
	    File f = fc.getSelectedFile();
	    try {
		PrintWriter pw = new 
		    PrintWriter(new BufferedWriter(new FileWriter(f)));
		pw.print(tosave);
		pw.close();
	    }
	    catch(IOException ioe){
		JOptionPane.showMessageDialog(this,"An unknown I/O error occured. Check your write permissions and if there is sufficient space.", 
					      "I/O error", 
					      JOptionPane.ERROR_MESSAGE);
		return null;
	    }
	    return f;
	}
	else
	    return null;
    }

    /** 
     * Implementation of the ChangeListener interface.
     **/
    public void stateChanged(ChangeEvent ce){
	reduced = y.triangularDelta();
	if (advancedmode || reduced){
	    outputFormula();
	    saveformula.setEnabled(true);
	    saveformulami.setEnabled(true);
	}
	if (reduced)
	    reduced();
	else{
	    cycles.setEnabled(true);
	    cyclesmi.setEnabled(true);
	    bestcycle.setEnabled(true);
	    bestcyclemi.setEnabled(true);
	    step.setEnabled(true);
	    stepmi.setEnabled(true);
	    saveformula.setEnabled(false);
	    saveformulami.setEnabled(false);
	}
    }

    /**
     * Sets the heuristic to be used when no bubbles or triangles 
     * are available.
     * @param heuristic the heuristic to be used, possible values are
     *                  EDGE_COST_HEURISTIC, MORE_SMALLER_LESS_BIGGER_HEURISTIC,
     *                  CYCLE_COUNT_HEURISTIC.
     * @see #EDGE_COST_HEURISTIC
     * @see #MORE_SMALLER_LESS_BIGGER_HEURISTIC
     * @see #CYCLE_COUNT_HEURISTIC
     **/
    public void setHeuristic(int heuristic){
	if (this.heuristic == heuristic)
	    return;
	this.heuristic = heuristic;
	if (y == null)
	    return;
	cca.setHeuristic(getHeuristic());
    }

    CCAHeuristic getHeuristic(){
	String hs = heuristic == EDGE_COST_HEURISTIC ? "Edge Cost" :
	    heuristic == MORE_SMALLER_LESS_BIGGER_HEURISTIC ? "Bigger Smaller" 
	    : "Cycle Count";
	CycleGenerator cg;
	CCAHeuristic h;
	if (cca != null && cca.problem() == y){// Yep, object comparison
	     cg = cca.heuristic().cycleGenerator();
	    h = (heuristic == EDGE_COST_HEURISTIC) ? 
		(CCAHeuristic) new EdgeCostHeuristic(y,cg) :
		 (CCAHeuristic) new CycleCountHeuristic(y,cg);
	}
	else {
	    h = (heuristic == EDGE_COST_HEURISTIC) ? 
		(CCAHeuristic) new EdgeCostHeuristic(y) :
		 (CCAHeuristic) new CycleCountHeuristic(y);
	}
	//MORE_SMALLER_LESS_BIGGER is default
	if (heuristic == CYCLE_COUNT_HEURISTIC)
	    ((CycleCountHeuristic) h).setStrategy
		(CycleCountHeuristic.CYCLE_COUNT);
	return h;
    }
    
    /**
     * Sets the format of the output in the outputfield.
     *
     * @param outputformat the format to be used, possible values are: 
     *                     GENERIC_OUTPUT, LATEX_OUTPUT, 
     *                     MAPLE_OUTPUT, RACAH_OUTPUT.
     * @see #GENERIC_OUTPUT
     * @see #LATEX_OUTPUT
     * @see #MAPLE_OUTPUT
     * @see #RACAH_OUTPUT
     **/
    public void setOutputFormat(int outputformat){
	if (this.outputformat == outputformat)
	    return;
	if (this.outputformat == LATEX_OUTPUT){
	    outputmenu.remove(usemacrosmi);
	    if (advancedmode)
		outputmenu.remove(generatemacrosmi);
	    outputmenu.remove(outputmenu.getItemCount()-1);// the separator
	} 
	else if (this.outputformat == MAPLE_OUTPUT && advancedmode){
	    outputmenu.remove(generatemacrosmi);
	    outputmenu.remove(outputmenu.getItemCount()-1);// the separator
	}
	if (this.outputformat == LATEX_OUTPUT 
	    || this.outputformat == MAPLE_OUTPUT){
	   generatemacros.setEnabled(false);
	}
	this.outputformat = outputformat;
	if (outputformat == LATEX_OUTPUT){
	    outputmenu.addSeparator();
	    usemacrosmi.setState(true);
	    outputmenu.add(usemacrosmi);
	    if (advancedmode)
		outputmenu.add(generatemacrosmi);
	}
	else if (outputformat == MAPLE_OUTPUT && advancedmode){
	    outputmenu.addSeparator();
	    outputmenu.add(generatemacrosmi);
	}
	if (outputformat == LATEX_OUTPUT || outputformat == MAPLE_OUTPUT){
	    generatemacros.setVisible(true);
	    generatemacros.setEnabled(true);
	}
	if (advancedmode || reduced)
	    outputFormula();
    }

    void outputFormula(){
	if (y == null)
	    return;
	GRVisitor v = null;
	switch(outputformat){
	case GENERIC_OUTPUT:
	    v = new GRWrappedStringVisitor
		(Math.min((int) output.getSize().getWidth(), 
			  (int) scrollpane.getSize().getWidth()),
		 output.getFontMetrics(output.getFont()));
	    break;
	case LATEX_OUTPUT:
	    v = new GRWrappedLaTeXVisitor 
		(Math.min((int) output.getSize().getWidth(), 
			  (int) scrollpane.getSize().getWidth()),
		 output.getFontMetrics(output.getFont()));
	    if (usemacrosmi.getState())
		((GRWrappedLaTeXVisitor) v).useMacros(true);
	    break;
	case MAPLE_OUTPUT:
	    v = new GRWrappedMapleVisitor  
		(Math.min((int) output.getSize().getWidth(), 
		      (int) scrollpane.getSize().getWidth()),
	     output.getFontMetrics(output.getFont()));
	    break;
	case RACAH_OUTPUT:
	    v = new GRWrappedRacahVisitor
		(Math.min((int) output.getSize().getWidth(), 
			  (int) scrollpane.getSize().getWidth()),
		 output.getFontMetrics(output.getFont()));
	    break;
	}
	y.genRecoupCoeff().accept(v);
	output.setText((String) v.result());
	nrofsummations.setText(""+y.genRecoupCoeff().nrOfSummations());
	nrof6js.setText(""+y.genRecoupCoeff().nrOf6js());
    }

    void showHelpFile(){
	JFrame helpwindow = new JFrame("GYutsis Help File");
	try{
	    final JEditorPane editorpane = 
		new JEditorPane(GYutsis.class.getResource(HELPFILE)); 
	    editorpane.setEditable(false);
	    editorpane.addHyperlinkListener(new HyperlinkListener(){
		    public void hyperlinkUpdate(HyperlinkEvent he){
			if (he.getEventType() 
			     == HyperlinkEvent.EventType.ACTIVATED){
			    try{
				editorpane.setPage(he.getURL());
			    }
			    catch(IOException ioe){}//should not be possible
			}
		    }
		});
	    helpwindow.getContentPane().add(new 
		JScrollPane(editorpane, 
			    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
	    helpwindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    helpwindow.setSize(700,700);
	    helpwindow.setVisible(true);
	}
	catch (IOException ioe){
	    JOptionPane.showMessageDialog(this,"An unknown I/O error occured:"
					  +ioe.getMessage(),
					  "I/O error", 
					  JOptionPane.ERROR_MESSAGE);
	}
    }
        
    /** 
     * Main method which creates a GYutsis window.
     **/
    public static void main(String [] args) throws IOException{
	GYutsis window = null;
	if (args.length == 1 && args[0].length() > 5)
	    if (args[0].charAt(0) == '<'){
		try{
		    window = new GYutsis(new YutsisGraph(args[0]));
		}
		catch (IllegalArgumentException iae){
		    window = new GYutsis();
		    JOptionPane.showMessageDialog(window, iae.getMessage(),
						  "Illegal input",
						  JOptionPane.ERROR_MESSAGE);
		}
		window.input.setText(args[0]);
	    }
	    else
		window = new GYutsis(new File(args[0]));
	else
	    window = new GYutsis();
	window.pack();
	window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	window.setVisible(true);
    }
}
