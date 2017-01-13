/* File: PopupDialogQuery.java */

import java.text.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.io.*;

/**
 * The PopupDialogQuery class is a generic popup query dialog window.
 * It displays a dialog window containing a editable 
 * TextField. There are also 2 buttons ("Ok" and "Cancel") to pass the 
 * information on. If you have one button the label is "Continue".
 *<P>
 * This work was produced by Peter Lemkin of the National Cancer
 * Institute, an agency of the United States Government.  As a work of
 * the United States Government there is no associated copyright.  It is
 * offered as open source software under the Mozilla Public License
 * (version 1.1) subject to the limitations noted in the accompanying
 * LEGAL file. This notice must be included with the code. The Flicker 
 * Mozilla and Legal files are available on http://open2dprot.sourceforge.net/.
 *<P>
 * @author P. Lemkin (NCI), G. Thornwall (SAIC), NCI-Frederick, Frederick, MD
 * @version $Date$   $Revision$
 * @see <A HREF="http://open2dprot.sourceforge.net/Flicker">Flicker Home</A>
 */
 
class PopupDialogQuery extends Dialog
      implements ActionListener, ItemListener, WindowListener 
{
  /** default # of columns */
  final static int
    DEF_COL_SIZE= 80;
  
  /** for returning data back to caller */                        
  public String
    data;
  /** # of columns to display */
  private int 
    colSize;
  /** size of frame */
  private int 
    width;	
  /** size of frame */		
  private int 			
    height;
  /** popup frame instance */
  private Frame
    frame;
  /** place text to be edited here */
  TextField
    textField;
  /** for data label */
  private Label				
    label;
  /** for options */
  private Panel
    optionPanel;
  /** opt. option choice list */  
  private Choice
    optionChoice; 
  /** # of buttons to add.
   * if 0, then none,
   * if 1, then add CONTINUE,
   * if 2 then add OK and CANCEL.
   */
  private int
    addButtonsCnt; 
  /** button pressed flag */
  boolean
    alertDone;                  
  /** wait for button to be pushed */
  boolean    
    sleepFlag;			
  /** Tried this instead of "this" */
  ActionListener 
    listener;			
  /** DEF_COL_SIZE spaces */
  private String
    spaces;                     
  /** list of option values if present */
  String
    optionValues[]= null;       
  /** optionValues[0:nOptions] */
  int
    nOptions= 0;                
    

  /**
   * PopupDialogQuery() - Constructor
   * @param f is frame of parent
   * @param addButtonsCnt is # of buttons to use where: 1 is (OK),
   * 2 is (Continue, Cancel), 3 is OptionsChoice & (Continue, Cancel)
   * @see #startPopupDialog
   */
  PopupDialogQuery(Frame f, int addButtonsCnt )
  { /* PopupDialogQuery */
    super(f,"dialog box",true);
    
    /* [1] set some defaults */
    this.addButtonsCnt= addButtonsCnt;
    colSize= DEF_COL_SIZE;
    data= spaces;
    frame= f;
    alertDone= false;
    spaces= "                                                                               ";
    
    /* [2] create popup and hide it for use later */
    this.startPopupDialog("Dialog",colSize);
  } /* PopupDialogQuery */
  
  
  /**
   * startPopupDialog() - create a hidden dialog panel within a frame.
   * @param windowTitle is the title of the dialog window
   * @param colSize is the size of the textField
   */
  void startPopupDialog(String windowTitle, int colSize)
  { /* startPopupDialog */
    Panel buttonPanel= null;	    /* place buttons here */
    Button
      ok,		                      /* update data */
      cancel;		                	/* use default data */
    GridLayout gl;                /* for layout of text fields, label, etc */
    
    
    /* [1] initialize */
    gl= new GridLayout(4,1);
    this.setLayout(gl);	    /* set gridlayout to frame */
    
    /* [1] Create User instruction label */
    label= new Label(spaces);
    
    /* [2] Create the buttons and arrange to handle button clicks */
    if(addButtonsCnt>0)
    { /* add button panel */
      buttonPanel= new Panel();
      
      if(addButtonsCnt==1)
        ok= new Button("Continue");
      else ok= new Button("Ok");
      ok.addActionListener(this);
      buttonPanel.add("Center",ok);
      
      if(addButtonsCnt==2)
      {
        cancel= new Button(" Cancel");
        cancel.addActionListener(this);
        buttonPanel.add("Center", cancel);
      }
    } /* add button panel */
    
    /* [3] add data text fields to panel */
    this.add(label);                /* add to grid. data description label */
    if(addButtonsCnt>=2)
    {
      optionPanel= new Panel();
      this.add(optionPanel);
      
      textField= new TextField(colSize);
      this.add(textField);           /* editable text */
    }
    
    /* [4] add buttons panel */
    if(buttonPanel!=null)
      this.add(buttonPanel);         /* buttons (ok & cancel) */
    this.addWindowListener(this);    /* listener for window events */
    
    /* [5] add components and create frame */
    this.setTitle(windowTitle);      /* frame title */
    this.pack();
    
    /* Center frame on the screen, PC only */
    Dimension screen= Toolkit.getDefaultToolkit().getScreenSize();
    Point pos= new Point((screen.width-frame.getSize().width)/2,
                         (screen.height-frame.getSize().height)/2);
    this.setLocation(pos);
    
    this.setVisible(false);	      /* hide frame which can be shown later */
  } /* startPopupDialog */
  
  
  /**
   * updatePopupDialog() - display/unhide popup dialog frame and set new values.
   * Remove recreate actionListeners &  components.
   * @param defaultDataMsg is the label for textField
   * @param defaultDatais the  data for textField
   * @param optionValues is the list of option values
   * @param nOptions is the number of options
   */
  void updatePopupDialog(String defaultDataMsg, String defaultData,
                         String optionValues[], int nOptions)
  { /* updatePopupDialog */
    /* [1] remove components so they can updated below */
    alertDone= false;               /* reset the flag */
    data= defaultData;	      /* store default data  */
    String dataMsg= (addButtonsCnt==2)
                       ? "Enter "+defaultDataMsg
                       : defaultDataMsg;   /* setup data label */
    label.setText(dataMsg);
    
    /* [2] add data text fields to panel */
    /* Remove old option entries */
    if(optionChoice!=null)
      optionPanel.remove(optionChoice);
    optionChoice= null;
    
    if(addButtonsCnt==2)
    {
      if(optionValues!=null)
      { /* add option choice */
        optionChoice= new Choice();
        optionPanel.add(optionChoice);
        for(int i=0;i<nOptions; i++)
          optionChoice.add(optionValues[i]);
        optionChoice.addItemListener(this);
      } /* add option choice */
      textField.setText(defaultData);
    }
    
    /* [3] add components and unhide */
    this.setVisible(true);		/* display it; unhide it */
  } /* updatePopupDialog */
  
  
  /**
   * alertTimeout() - update the popup dialog msg - wait for "Continue"
   * @param msg is message to display
   * @see #updatePopupDialog
   */
  void alertTimeout(String msg)
  { /* alertTimeout */
    this.sleepFlag= false;	/* flag for waiting */    
    updatePopupDialog(msg, "", null, 0);
  } /* alertTimeout */
  
  
  /**
   * actionPerformed() - Handle button clicks
   * @param e is action event when button pressed
   */
  public void actionPerformed(ActionEvent e)
  { /* actionPerformed */
    String
    cmd= e.getActionCommand();
    
      /* [1] see which button was pushed and do the right thing,
       * hide window and return default/altered data */
    if (cmd.equals(" Cancel") || cmd.equals("Continue"))
    { /* send default data back - data is already stored into this.data */
      this.setVisible(false);    /* hide frame which can be shown later */
    } /* send default data back */
    else
      if(cmd.equals("Ok"))
      { /* hide window and return data back entered by user */
        data= textField.getText(); /* altered data returned */
        this.setVisible(false);     /* hide frame which can be shown later*/
      }
    alertDone= true;
  } /* actionPerformed */
  
  
  /**
   * itemStateChanged() - event handler for Choices
   * @param e is item event when choices selected
   * @see #repaint
   */
  public void itemStateChanged(ItemEvent e)
  { /* itemStateChanged */
    Object
    obj= e.getSource();
    Choice
    itemC= (Choice)obj;
    
    if(itemC==optionChoice)
    { /* change the option used */
      /* get matching option */
      String
      optionStr= optionChoice.getSelectedItem();
      if(optionStr!=null)
      { /* update text field */
        textField.setText(optionStr);
        repaint();
      }
    } /* change the option used */
    
  } /* itemStateChanged */
  
  
  /**
   * windowClosing() - close down the window on PC only.
   * hide frame which can be shown later.
   * @param e is window closing event
   */
  public void windowClosing(WindowEvent e)
  { this.setVisible(false); }
   

  /**
   * dialogQuery() - query String variable requested
   * @param msg is message to display in dialog box
   * @param defaultValue to use if press OK and there is no data
   * @see #updatePopupDialog
   */
  String dialogQuery(String msg, String defaultValue)
  { /* dialogQuery */
    this.sleepFlag= true;	/* flag for waiting */
    this.data= defaultValue;	/* save default */
    
    updatePopupDialog(msg, defaultValue, null, 0); /* do it */
    
    return(data);		/* return string */
  } /* dialogQuery */
  
  
  /**
   * dialogQuery() - query String variable int requested
   * @param msg is message to display in dialog box
   * @param defaultValue to use
   * @param optionValues to use
   * @param nOptions number of option values
   * @see #updatePopupDialog
   */
  String dialogQuery(String msg, String defaultValue, String optionValues[],
                     int nOptions)
  { /* dialogQuery */
    this.sleepFlag= true;	/* flag for waiting */
    this.data= defaultValue;	/* save default */
    
    /* save the new list */
    this.optionValues= optionValues;
    this.nOptions= nOptions;
    
    updatePopupDialog(msg, defaultValue, optionValues,nOptions); /* do it */
    
    return(data);		/* return string */
  } /* dialogQuery */
  

  /*Others not used at this time */
  public void windowOpened(WindowEvent e) { }
  public void windowActivated(WindowEvent e) { }
  public void windowClosed(WindowEvent e) { }
  public void windowDeactivated(WindowEvent e) { }
  public void windowDeiconified(WindowEvent e) { }
  public void windowIconified(WindowEvent e) { }

} /* end of PopupDialogQuery class */

