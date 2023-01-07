/* StreamPanel.java
   2001 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;

import java.io.IOException;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

/** 
 * Class representing a panel with a JLabel with a title (North), a
 * JStreamedTextArea (Center) and corresponding clear- and
 * save-to-file-button (South).
 * @see JStreamedTextArea
 * @author Dries.VanDyck@rug.ac.be
 **/
public class StreamPanel extends JPanel implements ActionListener{
    JStreamedTextArea sta;
    JButton tofilebutton;
    JButton clearbutton;

    String savedirectory;

    /** 
     * Creates a new StreamPanel with given number of rows and columns,
     * a clear button, a save-to-file-button and a title.
     * @param rows the number of rows
     * @param columns the number of columns
     * @param title the title
     **/
    public StreamPanel(int rows, int columns, String title){
	this(rows,columns,title,true);
    }

    /** 
     * Creates a new StreamPanel with given number of rows and columns,
     * a clear button, if <code>savebutton</code> is <code>true</code> 
     * a save-to-file-button and a title.
     * @param rows the number of rows
     * @param columns the number of columns
     * @param title the title
     * @param savebutton when true a save-to-file-buton is present
     **/
    public StreamPanel(int rows, int columns, String title, 
		       boolean savebutton){
	super(new BorderLayout());
	sta = new JStreamedTextArea(rows, columns);
	add(new JLabel(title),BorderLayout.NORTH);
	add(new JScrollPane(sta), BorderLayout.CENTER);
	JPanel southpanel = new JPanel(new FlowLayout());
	clearbutton = new JButton("Clear");
	clearbutton.addActionListener(this);
	southpanel.add(clearbutton);
	if (savebutton)
	    addSaveButton("Save to file", southpanel);
	add(southpanel,BorderLayout.SOUTH);
    }

    private void addSaveButton(String text, JPanel panel){
	tofilebutton = new JButton(text);
	tofilebutton.addActionListener(this);
	savedirectory = System.getProperty("user.dir");
	panel.add(tofilebutton);
    }
    
    /** 
     * Implementation of the ActionListener interface listening to 
     * both buttons.
     **/
    public void actionPerformed(ActionEvent e){
	if (e.getSource() == clearbutton)
	    clear();
	else {
	    JFileChooser fc = new JFileChooser(savedirectory);
	    int answer = fc.showSaveDialog(this);
	    if (answer == JFileChooser.APPROVE_OPTION){
		File f = fc.getSelectedFile();
		try {
		    PrintWriter pw = new 
			PrintWriter(new BufferedWriter(new FileWriter(f)));
		    pw.print(sta.getText());
		    pw.close();
		}
		catch(IOException ioe){
		    JOptionPane.showMessageDialog(this,"An unknown I/O error occured. Check your write permissions and if there is sufficient space.", 
						  "I/O error", 
						  JOptionPane.ERROR_MESSAGE);
		}
	    }
	}
    }

    /** 
     * Empties the Streampanel.
     **/
    public void clear(){ sta.clear();}

    /** 
     * Returns a PrintStream which is connected with the JStreamedTextArea.
     * @return a PrintStream which is connected with the
     *         JStreamedTextArea.
     * @see JStreamedTextArea
     **/
    public PrintStream getPrintStream(){
	return new PrintStream(sta.getOutputStream());
    }

    /** 
     * Sets the initial directory of the save to file dialog.
     * @param directory the initial save directory
     **/
    public void setSaveDir(String directory){ savedirectory = directory; }

    /** 
     * Returns the initial directory of the save to file dialog.
     * @return the initial directory of the save to file dialog.
     **/
    public String getSaveDir() { return savedirectory; }
}
