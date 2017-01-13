/* File: PopupYesNoDialogBox.java  */

import java.awt.*;
import java.text.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Class PopupYesNoDialogBox is a dialog box with with 1, 2 or 3 
 * yesMsg, noMsg, and cancelMsg.
 *<P>
 * If you press yes or no, it sets the this.okFlag to true or false and
 * the this.cancelFlag to false.
 *<P>
 * If you press cancel, it sets the this.okFlag to false and
 * the this.cancelFlag to true.
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
  
  public class PopupYesNoDialogBox extends Dialog implements ActionListener,
                                                             WindowListener
  {    
    /** Frame where dialog box is added */
    private Frame
      f;
    /** Message for dialog box */
    private String
      msg;
    /** Yes label for dialog box */
    private String
      yesMsg;
    /** No label for dialog box */
    private String
      noMsg;
    /** Cancel label for dialog box */
    private String
      cancelMsg;
    /** background color */
    private Color
      bgColor;
    
    /** flag set by the yes button (no or cancel button set to false)  */
    public boolean
      okFlag= false;    
    /** flag set by cancel button */
    public boolean
      cancelFlag= false;
    
    
    /**
     * PopupYesNoDialogBox() - Constructor for popup YES/NO/CANCEL modal
     * dialog.
     * If you press yes or no, it sets the this.okFlag to true or false and
     * the this.cancelFlag to false.
     * If you press cancel, it sets the this.okFlag to false and
     * the this.cancelFlag to true.
     * @param f is the application parent frame of dialog box 
     * @param msg for dialog box
     * @param yesMsg for dialog box if it exists
     * @param noMsg for dialog box if it exists
     * @param cancelMsg for dialog box if it exists
     * @param bgColor for background else defaults to gray.
     */
    PopupYesNoDialogBox(Frame f, String msg, 
                        String yesMsg, String noMsg, String cancelMsg,
                        Color bgColor)
    { /* PopupYesNoDialogBox */
      super(f, "Message", true);      
      setupDialogBox(f, msg, yesMsg, noMsg, cancelMsg, bgColor);
    } /* PopupYesNoDialogBox */
    
    
    /**
     * PopupYesNoDialogBox() - Constructor for popup YES/NO/CANCEL modal
     * dialog.
     * If you press yes or no, it sets the this.okFlag to true or false and
     * the this.cancelFlag to false.
     * If you press cancel, it sets the this.okFlag to false and
     * the this.cancelFlag to true.
     * @param f is the application parent frame of dialog box 
     * @param msg for dialog box
     * @param yesMsg for dialog box if it exists
     * @param noMsg for dialog box if it exists
     * @param cancelMsg for dialog box if it exists
     */
    PopupYesNoDialogBox(Frame f, String msg, 
                        String yesMsg, String noMsg, String cancelMsg)
    { /* PopupYesNoDialogBox */
      super(f, "Message", true);
      setupDialogBox(f, msg, yesMsg, noMsg, cancelMsg, null);
    } /* PopupYesNoDialogBox */
       
        
    /**
     * PopupYesNoDialogBox() - Constructor for popup YES/NO modal
     * dialog.
     * If you press yes or no, it sets the this.okFlag to true or false and
     * the this.cancelFlag to false.
     * @param f is the application parent frame of dialog box 
     * @param msg for dialog box
     * @param yesMsg for dialog box if it exists
     * @param noMsg for dialog box if it exists
     * @param bgColor for background else defaults to gray.
     */
    PopupYesNoDialogBox(Frame f, String msg, 
                        String yesMsg, String noMsg, Color bgColor)
    { /* PopupYesNoDialogBox */
      super(f, "Message", true);      
      setupDialogBox(f, msg, yesMsg, noMsg, null, bgColor);
    } /* PopupYesNoDialogBox */
    
    
    /**
     * PopupYesNoDialogBox() - Constructor for popup YES/NO modal dialog.
     * If you press yes or no, it sets the this.okFlag to true or false and
     * the this.cancelFlag to false.
     * @param f is the application parent frame of dialog box 
     * @param msg for dialog box
     * @param yesMsg for dialog box if it exists
     * @param noMsg for dialog box if it exists
     */
    PopupYesNoDialogBox(Frame f, String msg, String yesMsg, String noMsg)
    { /* PopupYesNoDialogBox */
      super(f, "Message", true);
      setupDialogBox(f, msg, yesMsg, noMsg, null, null);
    } /* PopupYesNoDialogBox */
              
        
    /**
     * PopupYesNoDialogBox() - Constructor for popup OK modal dialog.
     * Press the okMsg button to return.
     * @param f is the application parent frame of dialog box 
     * @param msg for dialog box
     * @param okMsg for dialog box if it exists
     * @param bgColor for background else defaults to gray.
     */
    PopupYesNoDialogBox(Frame f, String msg, String okMsg, Color bgColor)
    { /* PopupYesNoDialogBox */
      super(f, "Message", true);      
      setupDialogBox(f, msg, okMsg, null, null, bgColor);
    } /* PopupYesNoDialogBox */
    
    
    /**
     * PopupYesNoDialogBox() - Constructor for popup OK modal dialog.
     * Press the okMsg button to return.
     * @param f is the application parent frame of dialog box 
     * @param msg for dialog box
     * @param okMsg for dialog box if it exists
     */
    PopupYesNoDialogBox(Frame f, String msg, String okMsg)
    { /* PopupYesNoDialogBox */
      super(f, "Message", true);
      setupDialogBox(f, msg, okMsg, null, null, null);
    } /* PopupYesNoDialogBox */
       
        
    /**
     * setupDialogBox() - setup for popup yes/no/cancel modal dialog.
     * If you press yes or no, it sets the this.okFlag to true or false and
     * the this.cancelFlag to false.
     * If you press cancel, it sets the this.okFlag to false and
     * the this.cancelFlag to true.
     * @param f is the application parent frame of dialog box 
     * @param msg for dialog box
     * @param yesMsg for dialog box if it exists
     * @param noMsg for dialog box if it exists
     * @param cancelMsg for dialog box if it exists
     * @param bgColor for background else defaults to gray.
     */
    private void setupDialogBox(Frame f, String msg, String yesMsg,
                                String noMsg, String cancelMsg, Color bgColor)
    { /* setupDialogBox */ 
      this.f= f;
      this.msg= msg;
      this.yesMsg= yesMsg;
      this.noMsg= noMsg;
      this.cancelMsg= cancelMsg;
      this.bgColor= bgColor;
            
      okFlag= false;   /* default */
      cancelFlag= true;
      
      if(bgColor!=null)
        this.setBackground(bgColor);
      
      buildGUI();
    } /* setupDialogBox */
    
    
    /**
     * buildGUI() - Build GUI
     */
    private void buildGUI()
    { /* buildGUI */
      String displayMsg;
      Button
        yesButton= new Button(yesMsg),
        noButton= (noMsg!=null)
                          ? new Button(noMsg)
                          : null,
        cancelButton= (cancelMsg!=null)
                          ? new Button(cancelMsg)
                          : null;
      Label label;
     
      yesButton.addActionListener(this);
      if(noButton!=null)
        noButton.addActionListener(this);
      if(cancelButton!=null)
        cancelButton.addActionListener(this);
          
      if(cancelMsg==null)
        displayMsg= (msg!=null) ? msg : "Choose "+yesMsg+" or "+noMsg;
      else
        displayMsg= (msg!=null)
                      ? msg
                      : "Choose "+yesMsg+" or "+noMsg+", or "+cancelMsg;
      label= new Label(displayMsg);
      
      Panel
        buttonPanel= new Panel(),
        mainPanel= new Panel(new BorderLayout());
      
      buttonPanel.add(yesButton);
      if(noButton!=null)
      buttonPanel.add(noButton);
      if(cancelButton!=null)
        buttonPanel.add(cancelButton);
      mainPanel.add(label,BorderLayout.NORTH);
      mainPanel.add(buttonPanel,BorderLayout.SOUTH);
      
      this.add(mainPanel);
      this.setSize(250,300);
      this.setTitle(displayMsg);
      this.pack();
      
      /* put the Dialog box in the middle of the frame */
      Dimension
        myDim= getSize(),
        frameDim= f.getSize(),
        screenSize= getToolkit().getScreenSize();
      Point loc= f.getLocation();
      
      loc.translate((frameDim.width - myDim.width)/2,
                    (frameDim.height - myDim.height)/2);
      loc.x= Math.max(0,Math.min(loc.x,screenSize.width - getSize().width));
      loc.y= Math.max(0,Math.min(loc.y, screenSize.height - getSize().height));
      
      this.setLocation(loc.x,loc.y);
      this.setVisible(true);
    } /* buildGUI */
    
    
    /**
     * actionPerformed() - handle action events
     * @param ae is the ActionEvent 
     */
    public void actionPerformed(java.awt.event.ActionEvent ae)
    {  /* actionPerformed */
      String cmd= ae.getActionCommand();
      Button item= (Button) ae.getSource();
      
      if(cmd.equals(yesMsg))
      {
        okFlag= true;
        cancelFlag= false;
        close();
      }
      
      else if(cmd.equals(noMsg))
      {
        okFlag= false;
        cancelFlag= false;
        close();
      }
      else if(cmd.equals(cancelMsg))
      {
        okFlag= false;
        cancelFlag= true;
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
    
  } /* End Class PopupYesNoDialogBox */
   
  