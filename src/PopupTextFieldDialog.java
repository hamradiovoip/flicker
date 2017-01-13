/* File: PopupTextFieldDialog.java */

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
import java.lang.*;
import java.io.*;

/**
 * The PopupTextFieldDialog class provides a popup text field with
 * a Done and Cancel buttons. The result will be in 
 *<P>
 * This work was produced by Peter Lemkin of the National Cancer
 * Institute, an agency of the United States Government.  As a work of
 * the United States Government there is no associated copyright.  It is
 * offered as open source software under the Mozilla Public License
 * (version 1.1) subject to the limitations noted in the accompanying
 * LEGAL file. This notice must be included with the code. The Flicker 
 * Mozilla and Legal files are available on 
 * http://open2dprot.sourceforge.net/Flicker
 *<P>
 * @author P. Lemkin (LECB/NCI), G. Thornwall (SAIC), Frederick, MD
 * @version $Date$   $Revision$
 * @see <A HREF="http://open2dprot.sourceforge.net/Flicker">Flicker Home</A>
 */

public class PopupTextFieldDialog extends Dialog implements ActionListener,
                                                            WindowListener
{ /* Popup */
  Flicker
    flk;
  Util
    util;

  /** Frame where dialog box is added */
  private Frame
    f;
    
  /** Message to display */
  private String
    msg;

  /** Returned value from text field */
  public String
    answer;
  /** Returned true if pressed DONE */
  public boolean
    okFlag;
  /** text field */
  private TextField
    tf;
  	
  
  /**
   * PopupTextFieldDialog () - constructor
   * @param msg to display
   */
  public PopupTextFieldDialog (Flicker flk, String msg, String defaultAnswer)
  { /* Popup */ 
    super(flk, "Message", true);
      
    this.flk= flk;
    this.util= flk.util;  
    
    this.f= flk;
    this.msg= msg;

    answer= (defaultAnswer==null) ? "" : defaultAnswer;
    okFlag= false;

    popupTextField();
  } /* PopupTextFieldDialog */

  
  /**
   * popupTextField() - popup text field
   */
  public void popupTextField()
  { /* popupTextAreaViewer */ 
    if(msg==null)
      msg= "Enter response and press Done";
    else if(msg.length()<20)
    { /* add trailing spaces */
      while(msg.length()<20)
       msg = msg + " ";  
    }
      
    this.add("North",new Label(msg));
    
    Panel
      ctrlPanel= new Panel(),
      textPanel= new Panel(new BorderLayout());
    this.add("Center",textPanel);
    this.add("South",ctrlPanel);
    
    /* Add text area */
    tf= new TextField(answer);
    textPanel.add("Center",tf);

    /* add buttons */
    Button b;
    b= new Button("Done");
    b.addActionListener(this);
    ctrlPanel.add(b);

    b= new Button("Cancel");
    ctrlPanel.add(b);
    b.addActionListener(this);
    
    /* set size */
    this.setSize(400,35);
    this.setTitle(msg);
    this.pack();
      
    /* put the Dialog box in the middle of the frame */
    Dimension
      myDim= this.getSize(),
      frameDim= f.getSize(),
      screenSize= getToolkit().getScreenSize();
    Point loc= f.getLocation();
    
    loc.translate((frameDim.width - myDim.width)/2,
    (frameDim.height - myDim.height)/2);
    loc.x= Math.max(0,Math.min(loc.x, screenSize.width - this.getSize().width));
    loc.y= Math.max(0,Math.min(loc.y, screenSize.height - this.getSize().height));
    
    this.setLocation(loc.x,loc.y);
    this.setVisible(true);
  } /* popupTextAreaViewer */  
  
     
    /**
     * actionPerformed() - handle action events
     * @param ae is the ActionEvent 
     */
    public void actionPerformed(java.awt.event.ActionEvent ae)
    {  /* actionPerformed */
      String cmd= ae.getActionCommand();
      Button item= (Button) ae.getSource();
      
      if(cmd.equals("Done"))
      {
        answer= tf.getText();
        okFlag= true;
        close();
      }
      
      else if(cmd.equals("Cancel"))
      {
        okFlag= false;
        close();
      }
    }  /* actionPerformed */
    
    
    /**
     * close() - close this popup
     */
    private void close()
    { /* close */
      this.dispose();
    } /* close */
    
    
    /**
     * windowClosing() - close down the window - assume false.
     */
    public void windowClosing(WindowEvent e)
    { /* close */
      close();
    } /* close */
        
    /* Others not used at this time */
    public void windowOpened(WindowEvent e)  { }
    public void windowActivated(WindowEvent e)  { }
    public void windowClosed(WindowEvent e)  {}
    public void windowDeactivated(WindowEvent e)  { }
    public void windowDeiconified(WindowEvent e)  { }
    public void windowIconified(WindowEvent e)  { }
    
  
} /* End of class Popup */
