/* File: ShowReportPopup.java */

import java.text.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.io.*;
import javax.swing.JComponent.*;
import javax.swing.*;
/**
 * The ShowReportPopup class creates and displays scrollable string text area 
 * in a popup window. Various control buttons are also provided at the bottom 
 * of the window that are dependent on the type of text area being displayed.
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
 
class ShowReportPopup extends JFrame implements ItemListener, ActionListener,
                                               WindowListener 
{
  
  /** link to global Flicker instance */
  private Flicker
    flk;        
  
  /** string for title of frame */
  String
    title;  
  /** message after Saved Txt into file */
  String
    savedMsg;
  /** copy of text in textArea */
  private String
    textReport;
  private String
  /** default SAVE AS .txt file */  
    defTxtFile;
  
  /** "SaveAs" .txt button */
  private Button 
    saveAsButton;
  /** "Clear" button */
  private Button 
    clearButton;   
  /** "Close" button */
  private Button 
    closeButton;   
  /** Text area GUI for main report */             
  private TextArea 
    textarea;    
  /** font size */
  private int
    textAreaFontSize= 12;            
  /** # of character rows to show */
  private int
    nRows= 24;              
  /** # of character cols to show */
  private int
    nCols= 80;
  /** Text area font family */
  public String   
    textFontFamily= "Helvetica";
  /* showStatusFlag to show (true), hide(false) */
  boolean 
    showStatusFlag;
 
    
  /**
   * ShowReportPopup() - Constructor. Display String in textArea
   * @param flk instance of Flicker
   * @param textReport is initial text string for buffer. 
   * @param nRows is maximum size of window
   * @param nCols, is maximum size of window
   * @param title of the window
   * @param String defTxtFile is default SAVE AS .txt file.Default
   *        "FlickerReport.txt"
   * @param showStatusFlag to show (true), hide(false).
   */
  public ShowReportPopup(Flicker flk, String textReport, 
                         int nRows, int nCols, String title, 
                         String defTxtFile, boolean showStatusFlag)
  { /* ShowReportPopup */
    super("ShowReportPopup");    
    
    this.flk= flk; 
    this.title= title;
    this.defTxtFile= (defTxtFile!=null) ? defTxtFile : "FlickerReport.txt";
        
    savedMsg= null;       /* changed to message after do "SaveAs" */
    
    /* Count # lines and make size MIN of nRows and nLines */
    if(textReport==null)
      textReport= "";
    this.textReport= textReport;
    this.nCols= nCols;
    this.nRows= nRows;
    this.showStatusFlag= showStatusFlag;
        
    /* Create a TextArea to display the contents of the file/string.
     * Should use equi-space font such as courier.
     */;
    //textFontFamily= "Helvetica";
    textFontFamily= "Courier";          /* Required for WinDump */
    textAreaFontSize= 10;
    
    textarea= new TextArea("", nRows,nCols);
    textarea.setFont(new Font(textFontFamily, Font.PLAIN, textAreaFontSize));
    textarea.setEditable(false);
    textarea.setBackground(Color.white);
    
    this.getContentPane().add("Center", textarea);    
    
    /* Create a bottom panel to hold a couple of buttons  */
    FlowLayout cpFlowLayout= new FlowLayout(FlowLayout.LEFT, 6, 1);
    Panel controlPanel= new Panel();
    controlPanel.setLayout(cpFlowLayout);
   
    this.getContentPane().add(controlPanel, "South");    
     
    /* Create the buttons and arrange to handle button clicks */
    Font buttonFont= new Font("Helvetica", Font.PLAIN /*BOLD*/, 12);
          
    saveAsButton= new Button("SaveAs");
    saveAsButton.addActionListener(this);
    saveAsButton.setFont(buttonFont);
    controlPanel.add(saveAsButton);  
    
    clearButton= new Button("Clear");
    clearButton.addActionListener(this);
    clearButton.setFont(buttonFont);
    controlPanel.add(clearButton); 
    
    closeButton= new Button("Close");
    closeButton.addActionListener(this);
    closeButton.setFont(buttonFont);
    controlPanel.add(closeButton);
       
    this.pack();
    textarea.setText(textReport);
    
    this.addWindowListener(this);  /* listener for window events */
    
    this.setTitle(title);
    
    /* Center frame on the screen, position report window on the screen */
    positionReportWindow(); 
    
    this.setVisible(showStatusFlag);
  } /* ShowReportPopup */
  
  
  /**
   * positionReportWindow() - position report window on the screen
   */
  public void positionReportWindow()
  { /* positionReportWindow */
    /* Center frame on the screen, PC only */
    Dimension 
      rptSize= this.getSize(),
      flkSize= flk.getSize(),
      screen= Toolkit.getDefaultToolkit().getScreenSize();
    int
      xPos= ((flkSize.width+rptSize.width) < screen.width) 
              ? (flkSize.width +10)
              : (screen.width - rptSize.width +50)/2,
      yPos= (flkSize.height - rptSize.height)/2;
    if(xPos<0 || yPos<0)
    { /* came up too soon */      
      xPos= (screen.width - rptSize.width)/2; 
      yPos= (screen.height - rptSize.height)/2;
    }
    Point pos= new Point(xPos,yPos);
    this.setLocation(pos);
  } /* positionReportWindow */
  
  
  /**
   * setShow() - set the show/hide popup status
   * @param showStatusFlag to show (true), hide(false).
   */
  public void setShow(boolean showStatusFlag)
  { this.setVisible(showStatusFlag);  }
  
  
  /**
   * clearText() - clear text in popup window
   */
  public void clearText()
  { /* clearText */
    this.textReport= "";    
    textarea.setText(this.textReport); 
  } /* clearText */
  
  
  /**
   * updateText() - update text in popup window
   * @param newText is string to copy into text window
   */
  public void updateText(String newText)
  { /* updateText */
    this.textReport= newText;    
    textarea.setText(this.textReport); 
  } /* updateText */
  
  
  /**
   * appendText() - append text in popup window
   * @param newText is string to append into text window
   */
  public void appendText(String newText)
  { /* appendText */
    this.textReport += newText;    
    textarea.append(newText); 
  } /* appendText */
  
  
  /**
   * updateTitle() - update title in popup window
   * @param title is new title.
   */
  public void updateTitle(String title)
  { /* updateTitle */
    this.title= title; 
    this.setTitle(title);
  } /* updateTitle */
  
    
  /**
   * updateSaveAsFile() - update SaveAs file default file name
   * @param title is new title.
   */
  public void updateSaveAsFile(String defTxtFile)
  {  this.defTxtFile= defTxtFile;  }
    
  
  /**
   * close() - close this popup and reset flags if needed
   * @see ProtPlot#repaint
   * @see PopupRegistry#removePopupByKey
   */
  public void close()
  { /* close */
    this.setVisible(false);
    flk.viewReportPopupFlag= false;
    flk.bGui.mi_showReportPopupCB.setState(false);
    //this.dispose();
  } /* close */
  
  
  /**
   * actionPerformed() - Handle button clicks
   * @param e is action evet
   */
  public void actionPerformed(ActionEvent e)
  { /* actionPerformed */
    String cmd= e.getActionCommand();
    
    if (cmd.equals("Close"))
    { /* Close the window by hiding it */
      this.close();
    }  
    
    else if (cmd.equals("Clear"))
    { /* Close or ALERT */
      this.clearText();
    }  
               
    else if(cmd.equals("SaveAs"))
    { /* Save data as TXT file in <userDir>/tmp/ */ 
      String initialPath= flk.userTmpDir+defTxtFile; 
      Popup popup= new Popup(flk); /* Open the startup directory browser */
      String savFile= popup.popupFileDialog(initialPath,
                                            "Save Report as .txt file",
                                            false);            
      flk.fio.writeFileToDisk(savFile, textReport);
    } /* Save data as TXT  file */
  } /* actionPerformed */  
    
  
  /**
   * quit() - closing down the window, get rid of the frame.
   * @see #close
   */
  public void quit()
  { close(); } 
  
  
  /**
   * itemStateChanged() - handle item state changed events
   * NOTE: need to implement radio groups here since AWT only
   * implements radio groups for Checkboxes, and CheckboxMenuItems.
   * @param e is ItemEvent
   */
  public void itemStateChanged(ItemEvent e)
  { /* itemStateChanged */;
    Object obj= e.getSource();
    Checkbox itemCB= (Checkbox)obj;    
  } /* itemStateChanged */
  
  
  /**
   * windowClosing() - closing down the window, get rid of the frame.
   * @param e is window closing event
   * @see #close
   */
  public void windowClosing(WindowEvent e)
  { this.close(); } 
  
  
  /*Others not used at this time */
  public void windowOpened(WindowEvent e) { }
  public void windowActivated(WindowEvent e) { }
  public void windowClosed(WindowEvent e) { }
  public void windowDeactivated(WindowEvent e) { }
  public void windowDeiconified(WindowEvent e) { }
  public void windowIconified(WindowEvent e) { }
  
  
  
} /* end of ShowReportPopup class */

