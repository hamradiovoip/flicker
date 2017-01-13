/* File: Util.java  */

import java.awt.*;
import java.awt.Color;
import java.awt.image.*;
import java.util.*;
import java.net.*;
import java.lang.*; 
import java.io.*;
import java.text.*;


/**
 * Class Util contains utility methods used by Flicker.
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

public class Util
{ /* Util */
  
  /** Instance of Flicker */
  private static Flicker
    flk;
  private String 
    lastMsgStr= "";
  private Color
    lastMsgColor;
  
  /** flag to enforce an atomic alerts */
  private static boolean   
    doingAlertFlag= false;
  
  /** Hash table of .flk values read */
  public Hashtable 
    flkStateHT;
  /** # of .flk hash table entries */
  public int
    nStateEntries= 0;
  /** # of hash table entries after call readNameValuesHashTableFromFile() */
  public int
    nEntries= 0;
  
  
  /**
   * Util() - constructor for Class Util
   * @param flk instance of Flicker
   */
  public Util(Flicker flkS)
  { /* Util */
    this.flk= flkS;
  } /* Util */
  
    
  /**
   * updateLMSvaluesInImages() - redraw landmarks in images if needed.
   */
  final public synchronized void updateLMSvaluesInImages()
  { /* updateLMSvaluesInImages */
    Landmark lms= flk.lms;
    
    if(flk.viewLMSflag && lms.nLM>0)
    { /* enable drawing landmarks */
      if(flk.allowXformFlag)
      { /* use the oImg landmarks */
        /* Note: we use the oImg landmarks even if there is no oImg. 
         * This is because the oImg landmarks are set to the iImg 
         * landmarks UNLESS there is a transform.
         **/
        flk.i1IS.setLandmarksTextListToDraw(lms.nLM, lms.colorLM, 
                                            lms.nameLM, lms.fontLM,
                                            lms.ox1, lms.oy1);
        flk.i2IS.setLandmarksTextListToDraw(lms.nLM, lms.colorLM, 
                                            lms.nameLM, lms.fontLM,
                                            lms.ox2, lms.oy2);
      }
      else
      { /* use the iImg landmarks */
        flk.i1IS.setLandmarksTextListToDraw(lms.nLM, lms.colorLM, 
                                            lms.nameLM, lms.fontLM,  
                                            lms.x1, lms.y1);
        flk.i2IS.setLandmarksTextListToDraw(lms.nLM, lms.colorLM,
                                            lms.nameLM, lms.fontLM, 
                                            lms.x2, lms.y2);
      }
    } /* enable drawing landmarks */
    else
    { /* disable drawing landmarks */
      flk.i1IS.clearLandmarkTextListToDraw();
      flk.i2IS.clearLandmarkTextListToDraw();
    }
  } /* updateLMSvaluesInImages */
       
  
  /**
   * setFlickerGUI() - set the Flicker GUI state
   */
  public synchronized void setFlickerGUI(boolean flkFlag)
  { /* setFlickerGUI */
    showMsg("Flickering is " + ((flkFlag) 
                                  ? "ON" : "OFF"), Color.black);
    flk.bGui.flickerCheckbox.setBackground((flkFlag) 
                                             ? Color.green 
                                             : Color.red);                                              
    if(flkFlag)
      flk.flkIS.setTitle("Flicker Window",true);
    else 
      flk.flkIS.setTitle("Flicker is off",true);
  } /* setFlickerGUI */
  
  
  /**
   * setFlickerState() - enable/disable flicker and the checkbox
   * and save the flickerFlag while doing Xform. On turning back on,
   * then restore the flicker state.
   * @param enableFlag to enable or disable flicker
   */
  public synchronized void setFlickerState(boolean enableFlag)
  { /* setFlickerState */
    BuildGUI bGui= flk.bGui;
    
    if(enableFlag)
    { /* enable flicker if the button had been pressed... */
      flk.flickerFlag= flk.prevFlickerFlag;
      bGui.flickerCheckbox.setBackground((flk.flickerFlag) 
                                           ? Color.green : Color.red);
      bGui.flickerCheckbox.setEnabled(true);
      if(flk.flickerFlag)
        flk.flkIS.setTitle("Flicker Window",true);
      else 
        flk.flkIS.setTitle("Flicker is off",true);
    }
    else
    { /* Disable flicker and save status functions */
      flk.prevFlickerFlag= flk.flickerFlag;
      flk.flickerFlag= false; /* stop flickering in the flk.run() loop */
      bGui.flickerCheckbox.setEnabled(false);
      bGui.flickerCheckbox.setBackground(Color.cyan);
      flk.flkIS.setTitle("Doing " + flk.xformName, true);
    }
  } /* setFlickerState */
  
  
  /**
   * sleepMsec() - sleep for mSec.
   */
  public synchronized static void sleepMsec(int mSec)
  { /* sleepMsec */
    try
    { Thread.sleep(mSec); }
    catch (InterruptedException e)
    {}
  } /* sleepMsec */


  /**
   * leftFillWithSpaces() - left fill the string with spaces to size n.
   * If n < str.length(), then just return str.
   * @param str to fill
   * @param n max width
   * @return adjusted string
   */
  final public static String leftFillWithSpaces(String str, int n)
  { /* leftFillWithSpaces */
    if(str==null)
      return(null);
    int lth= str.length();
    if(n<=0 || n<lth)
      return(str);
    
    String sR= str;
    for(int i=(n-lth);i>0;i--)
      sR = " "+sR;
    return(sR);
  } /* leftFillWithSpaces */
  
  
  /**
   * cvi2os() - convert integer number to octal string with leading 0.
   * @param iVal to convert
   * @return octal string
   * [TODO]
   */
  final public static String cvi2os(int iVal)
  {
    String sR= Integer.toOctalString(iVal);
    if(sR.charAt(0)!='0')
      sR= "0"+sR;
    return(sR);
  } 
     
  
  /**
   * cvi2hexs() - convert integer number to hex string
   * @param iVal to convert
   * @return hex string
   * [TODO]
   */
  final public static String cvi2hexs(int iVal)
  {
    String sR= Integer.toHexString(iVal);
    return(sR);
  }
  
  
  /**
   * cvf2s() - convert float to string with exact precision # of digits.
   * If precision > 0 then limit # of digits in fraction
   * @param v is value to convert
   * @param i is the # of digits precision in the mantissa
   * @return string approximating "%0.pd"
   * If abs(v) < pow(10.0,-p) then return "+0" or "-0"
   */
  static String cvf2s(float v, int precision)
  { /* cvf2s */
    NumberFormat nf= NumberFormat.getInstance();
    
    nf.setMaximumFractionDigits(precision);
    nf.setMinimumFractionDigits(precision);
    nf.setGroupingUsed(false);
    
    String s= nf.format(v);
    
    return(s);
  } /* cvf2s */
  
  
  /**
   * cvf2sVariable() - convert float to string with precision # of digits.
   * If precision > 0 then limit # of digits in fraction
   * @param v is value to convert
   * @param i is the # of digits precision in the mantissa
   * @return string approximating "%0.pd"
   * If abs(v) < pow(10.0,-p) then return "+0" or "-0"
   */
  static String cvf2sVariable(float v, int precision)
  { /* cvf2sVariable */
    NumberFormat nf= NumberFormat.getInstance();
    
    nf.setMaximumFractionDigits(precision);
    nf.setGroupingUsed(false);
    
    String s= nf.format(v);
    
    return(s);
  } /* cvf2sVariable */
  
  
  /**
   * cvd2s() - convert double to string with precision  # of digits
   * If precision > 0 then limit # of digits in fraction
   * @param v is value to convert
   * @param i is the # of digits precision in the mantissa
   * @return string approximating "%0.pd"
   * If abs(v) < pow(10.0,-p) then return "+0" or "-0"
   */
  static String cvd2s(double v, int precision)
  { /* cvd2s */
    NumberFormat nf= NumberFormat.getInstance();
    
    nf.setMaximumFractionDigits(precision);
    nf.setGroupingUsed(false);
    
    String s= nf.format(v);
    
    return(s);
  } /* cvd2s */
  
  
  /**
   * getFileName() - get the file name after last '/' if any
   */
  public String getFileName(String file)
  { /* getFileName */
    String sR= file;
    int i= file.lastIndexOf('/');
    if(i!=-1)
      sR= file.substring(i+1); /* move last last '/' */
    
    return(sR);
  } /* getFileName */
  
  
  /**
   * getLastMsgStr() -  get the last message string
   * @return last message
   */
  public String getLastMsgStr()
  {
    return(lastMsgStr);
  }
  
  
  /**
   * getLastMsgColor() -  get the last message color
   * @return last message color
   */
  public Color getLastMsgColor()
  { return(lastMsgColor); }
  
  
  /**
   * setLastMsgColor() - set the last message color
   * @param c is color to set
   */
  public void setLastMsgColor(Color c)
  { lastMsgColor= c; }
  
  
  /**
   * showMsg() - show msg in GUI area 1, set the status-line & set color.
   * Also append to the report window.
   *<PRE>
   * Default colors are:
   *   ERROR    = Color.red,
   *   OK       = Color.black
   *   evt debug= Color.blue.
   *   info     = Color.yellow
   *   % done   = Color.magenta
   *</PRE>
   * @param msg is status message to display
   * @param c is color to display it
   */
  public void showMsg(String msg, Color c)
  { showMsg1(msg, c); }
  
  
  /**
   * showMsg1() - show msg in GUI area 1, set the status-line & set color.
   * Also append to the report window if GUI exists.
   *<PRE>
   * Default colors are:
   *   ERROR    = Color.red,
   *   OK       = Color.black
   *   evt debug= Color.blue.
   *   info     = Color.yellow
   *   % done   = Color.magenta
   *</PRE>
   * @param msg is status message to display
   * @param c is color to display it
   */
  public void showMsg1(String msg, Color c)
  { /* showMsg1 */    
    showStatus(msg, c);
    
    if(flk.bGui!=null)
      appendReportMsg(msg+"\n",c);
  } /* showMsg1 */
  
  
  /**
   * showStatus() - show msg in GUI area 1, set the status-line & set color.
   * Do NOT append to the report window.
   *<PRE>
   * Default colors are:
   *   ERROR    = Color.red,
   *   OK       = Color.black
   *   evt debug= Color.blue.
   *   info     = Color.yellow
   *   % done   = Color.magenta
   *</PRE>
   * @param msg is status message to display
   * @param c is color to display it
   */
  public void showStatus(String msg, Color c)
  { /* showStatus */    
    /* [1] If debugging, print on terminal as well. */
    if(flk.dbugFlag)
      System.out.println(msg);
    
    /* [2] Write into our Status line. Save last msg for updateInfoStr() */
    lastMsgColor= c;
    lastMsgStr= msg;  
    BuildGUI bGui= flk.bGui;
    if(bGui!=null && bGui.textMsgLabel1!=null)
    {
      
      bGui.textMsgLabel1.setForeground(c);
      String msgS= (msg.length()>bGui.maxMsgSize)
                     ? msg.substring(0,bGui.maxMsgSize) : msg;
                     
      bGui.textMsgLabel1.setText(msgS);
    }
  } /* showStatus */
  
 
  /**
   * showMsg2() - show msg in GUI text area 2, status line and set color.
   * Also append to the report window.
   *<PRE>
   * Default colors are:
   *   ERROR    = Color.red,
   *   OK       = Color.black
   *   evt debug= Color.blue.
   *   info     = Color.yellow
   *   % done   = Color.magenta
   *</PRE>
   * @param msg is status message to display
   * @param c is color to display it
   */
  public void showMsg2(String msg, Color c)
  { /* showMsg2 */    
    /* [1] If debugging, print on terminal as well. */
    if(flk.dbugFlag)
      System.out.println(msg);
    
    /* [2] Write into our Status line. Save last msg for updateInfoStr() */ 
    BuildGUI bGui= flk.bGui;
    if(bGui!=null&& bGui.textMsgLabel2!=null)
    {
      bGui.textMsgLabel2.setForeground(c);
      String msgS= (msg.length()>bGui.maxMsgSize)
                     ? msg.substring(0,bGui.maxMsgSize) : msg;
      bGui.textMsgLabel2.setText(msgS);
      appendReportMsg(msg+"\n", c);
    }
  } /* showMsg2 */
  
  
  /**
   * clearReportMsg() - clear text in report popup window
   */
  void clearReportMsg()
  { /* clearReportMsg */
    if(flk.bGui!=null && flk.bGui.pra!=null)
      flk.bGui.pra.clearText();
  }  /* clearReportMsg */
  
  
  /**
   * setReportMsg() - set a report message to the report popup window
   * @param sMsg to assign
   */
  public void setReportMsg(String sMsg)
  { /* setReportMsg */
    if(flk.bGui!=null && flk.bGui.pra!=null)
      flk.bGui.pra.updateText(sMsg);
  } /* setReportMsg */
    
  
  /**
   * popupAlertMsg() - display the msg in msg1 and popup up an Alert message.
   * Default the background to red.
   * @param msg to display
   */
  public static void popupAlertMsg(String msg)
  { /* popupAlertMsg */
    popupAlertMsg(msg, flk.alertColor);
  } /* popupAlertMsg */
    
  
  /**
   * popupAlertMsg() - display the msg in msg1 and popup up an Alert message
   * @param msg to display
   * @param bgColor is the background color to use
   */
  public static void popupAlertMsg(String msg, Color bgColor)
  { /* popupAlertMsg */
    if(doingAlertFlag)
      return;
    doingAlertFlag= true;
    flk.util.showMsg(msg, ((bgColor==flk.alertColor) ? Color.red : bgColor));
    PopupYesNoDialogBox pyn= new PopupYesNoDialogBox(flk,msg,"Ok",bgColor);  
    doingAlertFlag= false;
  } /* popupAlertMsg */
  
  
  /**
   * fatalReportMsg() - display a fatal report message to the report popup 
   * window, then wait for before return. Clear the window first.
   * Bring the message to the front.
   * @param sMsg to display
   * @param timeoutMsec if != 0 then wait that time. If it is <0 then exit 
   *        the program after the timeout.
   */
  public void fatalReportMsg(String sMsg, int timeoutMsec)
  { /* fatalReportMsg */
    if(flk.bGui!=null && flk.bGui.pra!=null)
    {         
      flk.viewReportPopupFlag= true;         /* Force it to be true */
      flk.bGui.pra.setShow(flk.viewReportPopupFlag);
      flk.bGui.pra.setVisible(true);
      flk.bGui.pra.requestFocus();
      flk.bGui.pra.appendText(sMsg); 
    //  if(flk.)
      System.out.println(sMsg);
      if(timeoutMsec!=0)
      { /* wait and then exit */  
        sleepMsec(Math.abs(timeoutMsec));
        if(timeoutMsec>0)
          System.exit(-1);
      }         
    }
    else 
      System.out.println(sMsg);
  } /* fatalReportMsg */
  
  
  /**
   * appendReportMsg() - append a report message to the report popup window
   * @param sMsg to append
   */
  public static void appendReportMsg(String sMsg)
  { /* appendReportMsg */
    appendReportMsg(sMsg,Color.black);
  }
  
  
  /**
   * appendReportMsg() - append a report message to the report popup window 
   * @param sMsg to append
   * @param color if the color is Color.red, then force it to pop up
   *   if it is not visible.
   */
  public static void appendReportMsg(String sMsg, Color color)
  { /* appendReportMsg */
    if(flk.bGui!=null && flk.bGui.pra!=null)
      flk.bGui.pra.appendText(sMsg);
    
      if(color==Color.red && !flk.viewReportPopupFlag)
        forceReportWindowPopup();
  }  /* appendReportMsg */
  
    
  /**
   * forceReportWindowPopup() - force the append Report Window to popup
   */
  public static void forceReportWindowPopup()
  { /* forceReportWindowPopup */
    if(!flk.viewReportPopupFlag)
    { /* force the list to be viewed */
      flk.viewReportPopupFlag= true;
      flk.bGui.mi_showReportPopupCB.setState(true);
      flk.bGui.pra.setShow(flk.viewReportPopupFlag);
    }
  }  /* forceReportWindowPopup */
  
  
  /**
   * getScrollerValuesStateStr() - "Report scroller values" button
   */
  public String getScrollerValuesStateStr()
  { /* getScrollerValuesStateStr */
    String
      sR= "\nScroller values\n"+
            "============\n";      
    sR += flk.iData1.state.getStateStr(flk.iData1, flk.imageFile1)+"\n";
    sR += flk.iData2.state.getStateStr(flk.iData2, flk.imageFile2)+"\n";
    return(sR);
  } /* getScrollerValuesStateStr */
  
  
  /**
   * readFlkStateFile() - read a .flk state file and change the state
   * @param flkStateFile to read
   * @param reportErrorMsgsFlag to report error messages when loads.
   * @return true if read the file and changed the state
   * [TODO] restore the oImg .gif files if they exist...
   */
  public synchronized boolean readFlkStateFile(String flkStateFile,
                                               boolean reportErrorMsgsFlag)
  { /* readFlkStateFile */
    /* [1] Read a .flk state file and change the state to the new file names */
    Hashtable fsHT= readFlkState(flkStateFile, false);
    if(fsHT==null)
    {
      showMsg("File not found - Flicker state file",Color.red);
      showMsg2("["+flkStateFile+"]", Color.red);
      return(false);
    }
    else
      flk.flkStateFile= flkStateFile;
    
    /* [2] Now replace the images. Force images to be the demo images
     * read into (flk.imageFile1, flk.imageFile2)
     */
    flk.imgIO.changeImageFromSpec(flk.imageFile2, "right",false,
                                  reportErrorMsgsFlag);
    /* Do last so that LEFT image is selected */
    flk.imgIO.changeImageFromSpec(flk.imageFile1, "left", false,
                                  reportErrorMsgsFlag);
    
    /* [2.1] At this point, the flk.i1IS and flk.i2IS are set to the
     * new image scrollers. NOW we set the state data into the new 
     * Image scrollers.
     * Get values from the hash table an stuff into the image scrollers.
     */  
    setStateHashtableForGetValue(fsHT);
    flk.i1IS.oGifFileName= getStateValue("oGifFileName1", (String)null); 
    flk.i2IS.oGifFileName= getStateValue("oGifFileName2", (String)null);   
    int
      xObj1= getStateValue("xObj1", 0),
      yObj1= getStateValue("yObj1", 0),
      xObj2= getStateValue("xObj2", 0),
      yObj2= getStateValue("yObj2", 0); 
    flk.i1IS.setObjPosition(xObj1, yObj1);
    flk.i1IS.setImgPosition(xObj1, yObj1);
    flk.i2IS.setObjPosition(xObj2, yObj2);
    flk.i2IS.setImgPosition(xObj2, yObj2);
    
    /* [3] [TODO] If the oImg transforms exist on the disk, then read them into oImg.
     * Check if oImg names not null, then compute the full path and verify
     * on the disk. Then read it. If fail, force iData.oImg to null.
     */
    if(flk.i1IS.oGifFileName!=null)
    {
    }
    if(flk.i2IS.oGifFileName!=null)
    {
    }

    /* [4] Re-read .flk state file to update state and landmarks 
     * for the images just loaded. Also read spot lists this pass
     * since it may be VERY large and take a while.
     */
    if(readFlkState(flkStateFile,true)==null)
      return(false);                           /* failed! */
    
    /* [5] If zoom values are are not 1.0X, then do zoom transform */
    SliderState
      ss1= flk.iData1.state,
      ss2= flk.iData2.state;
    
    /* Note: zoomMag scroller value of DEF_ZOOM_MAG_SCR corresponds 
     * to 1.0X zoom.
     */
    int zoomMag1Scr= getStateValue("I1-zoomMagScr", 
                                   ss1.DEF_ZOOM_MAG_SCR);
    int zoomMag2Scr= getStateValue("I2-zoomMagScr", 
                                   ss2.DEF_ZOOM_MAG_SCR);    
    float defZoomMagVal1= ss1.cvtZoomMagScr2ZoomMagVal(zoomMag1Scr);
    float defZoomMagVal2= ss2.cvtZoomMagScr2ZoomMagVal(zoomMag2Scr);
    
    /* [5.1] If either left or right image has a zoom value different
     * from 1.0X
     */
    int 
      notifyDelay= 1000,
      extFctNbr= 0; 
    if(zoomMag1Scr!=ss1.DEF_ZOOM_MAG_SCR ||
       zoomMag2Scr!=ss2.DEF_ZOOM_MAG_SCR)
    { /* Setup to do "DeZoom" image transforms */
      String cmd= "DeZoom";    
      if(flk.doingXformFlag)
      {
        flk.util.showMsg("Can't do new transform until "+flk.xformName +
                         " finishes.", Color.red);
        return(false);
      }
      else
        flk.doingXformFlag= true;
      
      flk.imagesToProcess= 0;
      if(extFctNbr>0)
      {
        flk.xformName= flk.efName[extFctNbr];
      }
      else
        flk.xformName= (String)cmd; /* set the new xform name */
      setFlickerState(false);       /* disable flicker checkbox and
                                     * stop flickering while doing Xform */
    } /* Setup to do "DeZoom" image transforms */
    
    /* [5.2] Zoom first image if not 1.0X zoomMag. Compare scroller
     * value because of precision problem.
     */
    if(zoomMag1Scr!=ss1.DEF_ZOOM_MAG_SCR)
    //if(defZoomMagVal1!=1.0)
    { /* Zoom 1st image */      
      flk.curState.zoomMagVal= defZoomMagVal1;
      Date date= new Date();
      flk.iData1.startTime= date.getTime(); /* grab start time
                                             * msec since 1970*/
      flk.imagesToProcess++;
      flk.validAffineFlag= false;
      
      flk.ixf1= new ImageXform(flk, "left", flk.iData1, flk.xformName,
                               extFctNbr);
      flk.ixf1.start();
      flk.ixf1.setPriority(Thread.MIN_PRIORITY);
      showMsg("Doing [" + flk.xformName + "] on " + flk.imageFile1,
              Color.black );
      
      /* [5.2.1] Pause while waiting for first dezoom transform to finish */
      int x= 0;
      while(flk.imagesToProcess!=0 && x<5) 
      {
        sleepMsec(notifyDelay);
      //  showMsg("waiting for:" + flk.xformName+" "+ x, Color.black );
        x++;
      }
    } /* Zoom 1st image */
    
    /* [5.3] Zoom second image if not 1.0X zoomMag. Compare scroller
     * value because of precision problem.
     */
    if(zoomMag2Scr!=ss2.DEF_ZOOM_MAG_SCR)
    //if(defZoomMagVal2!=1.0) 
    { /* Do 2nd image */
      flk.curState.zoomMagVal= defZoomMagVal2;
      Date date= new Date();
      flk.iData2.startTime= date.getTime(); /* grab start time
                                             * msec since 1970 */
      flk.imagesToProcess++;
      flk.validAffineFlag= false;
      
      flk.ixf2= new ImageXform(flk, "right", flk.iData2, flk.xformName,
                               extFctNbr);
      flk.ixf2.start();
      flk.ixf2.setPriority(Thread.MIN_PRIORITY);
      showMsg("Doing [" + flk.xformName + "] on " + flk.imageFile2,
              Color.black );
      int x= 0;
      
      /* [5.3.1] pause while waiting for second dezoom transform to finish */
      while(flk.imagesToProcess!=0 && x<5) 
      {
        sleepMsec(notifyDelay);
       // showMsg("waiting for:" + flk.xformName+" "+ x, Color.black );
        x++;
      }
    } /* Do 2nd image */    
    
    /* [5.4] Restore flicker checkbox and enable flickering after
     * did zoom Xform.
     */
    setFlickerState(true);         
         
    /* [6] If Brightness/Contrast values are are not defaults, then
     * do initial BC transform.
     */
     boolean 
       doRepaint1Flag= false,   /* [TODO] may want incremental repaint */
       doRepaint2Flag= false;
    
     if(ss1.brightness!=ss1.DEF_BRIGHTNESS || ss1.contrast!=ss1.DEF_CONTRAST)
     { /* B/C xform image 1 */
       flk.i1IS.bcImgF.setBrCt(flk.i1IS, ss1.brightness, ss1.contrast,
                               ss1.MAX_BRIGHTNESS, ss1.MAX_CONTRAST,
                               true);
       doRepaint1Flag= flk.i1IS.processBCimage(flk.iData1);
     }
    
     if(ss2.brightness!=ss2.DEF_BRIGHTNESS || ss2.contrast!=ss2.DEF_CONTRAST)
     { /* B/C xform image 2 */
       flk.i2IS.bcImgF.setBrCt(flk.i2IS, ss2.brightness, ss2.contrast,
                               ss2.MAX_BRIGHTNESS, ss2.MAX_CONTRAST,
                               true);
       doRepaint2Flag= flk.i2IS.processBCimage(flk.iData2);
     }
       
     if(flk.lastISName.equals ("left"))
     {
       flk.evs.setEventScrollers (flk.iData1.state);
     }    
     else if(flk.lastISName.equals ("right"))
     {
       flk.evs.setEventScrollers (flk.iData2.state);
     }
    
    /* [7] Redraw the canvases */
    flk.i1IS.updateClickableCanvas();
    flk.i2IS.updateClickableCanvas();
  
    flk.doFullRepaint();           
      
    return(true);
  } /* readFlkStateFile */
    
  
  /**
   * getFullCalFilePath() - compute the .cal file from the qualified imageFile
   * If there is a subdirectory before the baseFile, then strip out the 
   * directory name and add it before the actual base file.
   *  "cal/{image file base}.cal" file
   * or
   *  "cal/{image subdirectory}-DIR-{image file base}.cal" file.
   * @param iData to use in computing the .cal file path
   * @return .cal file path
   */
  public String getFullCalFilePath(ImageData iData)
  { /* getFullCalFilePath */
    String relativeImageFile= iData.imageFile;
    int idxOptDir= relativeImageFile.indexOf("Images"+flk.fileSeparator);
    String 
      subImagesDirFile= (idxOptDir==-1) 
                           ? relativeImageFile
                           : relativeImageFile.substring(idxOptDir+7),
      imageFile= getFileNameFromPath(subImagesDirFile);
    
    /* Note: if it is a demo image, there is only 1 separator "Images/".
     * If the user had put image directories in 
     *      "Images/<user dir>/<image file>",
     * then there are 2 separators.
     */
    int idxSubDir= subImagesDirFile.indexOf(flk.fileSeparator);   
    String subDirName= ((idxSubDir==-1))
                         ? null
                         : subImagesDirFile.substring(0,idxSubDir);                    
    
    String baseDirPrefix= (idxSubDir==-1)
                             ? "" 
                             : (subDirName + "-DIR-");
         
    int idx= imageFile.lastIndexOf(".");   /* get rid of the extension */
    String baseFileName= imageFile.substring(0,idx);
    
    /* Build the calFile */
    String
      baseDirAndFile= baseDirPrefix + baseFileName,
      calFile= flk.userDir + "cal" + flk.fileSeparator +
               baseDirAndFile + ".cal";
    
    return(calFile);
  } /* getFullCalFilePath */
  
  
  /**
   * readCalibrationFile() - read the image calibration from the
   *  "cal/{image file base}.cal" file
   * or
   *  "cal/{image subdirectory}-DIR-{image file base}.cal" file.
   * @param iData is the Image Data to read the calibration
   * @return true if succeed
   */
  public boolean readCalibrationFile(ImageData iData)
  { /* readCalibrationFile */    
    String sI;
    if(iData!=flk.iData1 && iData!=flk.iData2)
      return(false);            /* Left or right must be selected */
        
    /* Compute the .cal file from the image file path */
    String
      calFile= getFullCalFilePath(iData),
      sMsg= "Reading Flicker ["+calFile+"] calibration file";       
    File fdTst= new File(calFile);
    if(!fdTst.exists())
    { /* problem -  file not found */
      return(false);
    }
    
    Hashtable ht= readNameValuesHashTableFromFile(calFile,sMsg,5101);
    if(ht==null)
    { /* problem - hash file is corrupted */
      fatalReportMsg("\n===> Flicker .cal state file ["+calFile+"]\n"+
                     "is corrupted - ignoring calibration and continuing.\n\n",
                     -6000);
      return(false);
    }
    setStateHashtableForGetValue(ht, nEntries);
    
    /* Parse the data in the hash table into state variables */
    if(nStateEntries==0)
      return(false);    
          
    iData.calib.readState(); /* read state from file into cal instance state*/
        
    return(true);
  } /* readCalibrationFile */
  
  
  /**
   * cvColorValue2Color() - convert color code to Color.xxx
   * @param colorCode
   * @return Color.xxx value
   */
  public Color cvColorValue2Color(int colorCode)
  { /* cvColorValue2Color */
    switch(colorCode)
    {
      case 1: return(Color.red); 
      case 2: return(Color.orange);
      case 3: return(Color.yellow);
      case 4: return(Color.green);
      case 5: return(Color.blue); 
      case 6: return(Color.cyan); 
      case 7: return(Color.black); 
      case 8: return(Color.gray); 
      case 9: return(Color.white); 
      case 10: return(Color.magenta); 
      default:
        break;
    }
    return(Color.red);
  } /* cvColorValue2Color */
  
  
  /**
   * cvColor2ColorValue() - convert Color.xxx to color code 
   * @param color
   * @return color code 
   */
  public int cvColor2ColorValue(Color color)
  { /* cvColor2ColorValue */
    if(color==Color.red)
      return(1);
    if(color==Color.orange)
      return(2);
    if(color==Color.yellow)
      return(3);
    if(color==Color.green)
      return(4);
    if(color==Color.blue)
      return(5);
    if(color==Color.cyan)
      return(6);
    if(color==Color.black)
      return(7);
    if(color==Color.gray)
      return(8);
    if(color==Color.white)
      return(9);
    if(color==Color.magenta)
      return(10);
    return(1);
  } /* cvColor2ColorValue */
    
  
  /**
   * readNameValuesHashTableFromFile() - read a hashtable of tab-delimited
   * name-value pairs from a file. Convert the (name\tvalue\n) data to
   * a hashtable. Allow popup error messages.
   * @param fileName is the name of the file to read
   * @param sMsg to print when reading the file.
   * @return hashtable else null if not found. The global variable nEntries
   *         contains the number of entries.
   */
  public Hashtable readNameValuesHashTableFromFile(String fileName, 
                                                   String sMsg,
                                                   int hashTableSize)
  { /* readNameValuesHashTableFromFile */
    return(readNameValuesHashTableFromFile(fileName, sMsg, hashTableSize,true));
  } /* readNameValuesHashTableFromFile */
  
  
  /** readNameValuesHashTableFromFile() - read a hashtable of tab-delimited
   * name-value pairs from a file. Convert the (name\tvalue\n) data to
   * a hashtable.
   * @param fileName is the name of the file to read
   * @param sMsg to print when reading the file.
   * @param usePopupAlertsFlag if allow popup error messages
   * @return hashtable else null if not found. The global variable nEntries
   *         contains the number of entries.
   */
  public Hashtable readNameValuesHashTableFromFile(String fileName, 
                                                   String sMsg,
                                                   int hashTableSize,
                                                   boolean usePopupAlertsFlag)
  { /* readNameValuesHashTableFromFile */
    String
      name,
      value,
      delimiter;
     
    String sData= flk.fio.readData(fileName, sMsg, usePopupAlertsFlag);
    sData= rmvRtnChars(sData);       /* make sure no "\r\n" stuff.. */
    
    Hashtable ht= new Hashtable(hashTableSize);
    nEntries= 0;
    if(sData==null)
      return(null);
    
    StringTokenizer parser= new StringTokenizer(sData, "\t\n", false);
    
    while(parser.hasMoreTokens())
    { /* parse (name,value) pairs and save in hash table */
      name= parser.nextToken();
      if(!parser.hasMoreTokens())
        break;
      value= parser.nextToken();
      if(name==null || name.length()==0 || value==null)
        break;
      else
        ht.put(name, value);
      nEntries++;  
    } /* parse (name,value) pairs and save in hash table */
    
    if(nEntries==0)
      return(null); 
  
    return(ht);
  } /* readNameValuesHashTableFromFile */
  
  
  /**
   * setStateHashtableForGetValue() - set working hash table for next
   * getStateValue calls
   * @param ht is hashtable to use
   * @param nEntriesVal is the number of elements used in the hash table.
   */
  public void setStateHashtableForGetValue(Hashtable ht, int nEntriesVal)
  { /* setStateHashtableForGetValue */
    flkStateHT= ht;
    nStateEntries= nEntriesVal;
  } /* setStateHashtableForGetValue */
  
  
  /**
   * setStateHashtableForGetValue() - set working hash table for next
   * getStateValue calls
   * @param ht is hashtable to use
   */
  public void setStateHashtableForGetValue(Hashtable ht)
  { /* setStateHashtableForGetValue */
    flkStateHT= ht;
  } /* setStateHashtableForGetValue */
  
  
  /**
   * readBaseFlkPropertiesFile() - read the base "Flicker.properties" file 
   * if it exists. This sets the user's default properties. If they
   * do a (File | Reset) it overides this.
   * @param fileName to read (e.g., "Flicker.properties")
   * @return true if succeed
   */
  public boolean readBaseFlkPropertiesFile(String fileName)
  { /* readBaseFlkPropertiesFile */
    if(fileName==null)
      fileName= "Flicker.properties";
    String
      sMsg= "Reading Flicker ["+fileName+"] user preferences file";     
      Hashtable 
        ht= readNameValuesHashTableFromFile(fileName,sMsg,5101,false);
      setStateHashtableForGetValue(ht, nEntries);
    
    /* Parse the data in the hash table into state variables */
    if(nStateEntries==0)
      return(false);          
      
    /* Parse the base flicker state properties */
    parseBaseFlkProperties();
    
    return(true);
  }  /* readBaseFlkPropertiesFile */
  
  
  /**
   * writeBaseFlkPropertiesFile() - write out "Flicker.properties" file
   * to save user preferences.
   * @param fileName to read (e.g., "Flicker.properties")
   * @return true if succeed
   */
  public boolean writeBaseFlkPropertiesFile(String fileName)
  { /* writeBaseFlkPropertiesFile */
    if(fileName==null)
      fileName= "Flicker.properties";    
    
    /* Add the tab-delim base Flicker properties State string */ 
    StringBuffer sBuf= new StringBuffer(30000);  /* est. - optimize */   
    getBaseFlkPropertiesStr(sBuf);     
    String sData= new String(sBuf);
    
    /* Now write out the base preferences file */
    boolean flag= flk.fio.writeFileToDisk(fileName, sData);
   
    return(flag);
  } /* writeBaseFlkPropertiesFile */
  
  
  /**
   * getAnnotationFromServer() - get the gel iData spot s data from 
   * proteomic web server
   * @param iData is the gel image
   * @param s is the spot (it contains the x,y) coordinates
   * @return true if succeed
   */
  public boolean getAnnotationFromServer(ImageData iData,Spot s)
  { /* getAnnotationFromServer */
    boolean clickEnabledFlag= (iData.userClickableImageDBflag &&
                               iData.isClickableDBflag);
    if(!clickEnabledFlag)
      return(false);
    
    String clickCGIbaseURL= null;
    if(iData==flk.iData1)
      clickCGIbaseURL= flk.clickableCGIbaseURL1;
    if(iData==flk.iData2)
      clickCGIbaseURL= flk.clickableCGIbaseURL2;
    if(clickCGIbaseURL==null)
      return(false);
    
    String pData[]= getProteinIDdataByXYurl(clickCGIbaseURL,
                                            s.xC,s.yC,
                                            "Reading spot #"+s.nbr+
                                            " annotation from active protein DB");
    if(pData==null)
    { /* problem */
      s.id= pData[0];
      s.name= pData[1];
      return(true);
    }
    else
      return(false);
  } /* getAnnotationFromServer */

          
  /**
   * getProteinIDdataByXYurl() - read data from protein web site
   * and parse {Swiss-Prot-id, protein-name} if found.
   * @param clickCGIbaseURL is web site to get protein data
   * @param x is x coordinate in active gel image 
   * @param y is y coordinate in active gel image 
   * @param msg is msg coordinate in active gel image 
   * @return {Swiss-Prot-id, protein-name} if succeed, null if fail
   */
  public String[] getProteinIDdataByXYurl(String clickCGIbaseURL,
                                          int x, int y, String msg)
  { /* getProteinIDdataByXYurl */
    if(x==-1 || y==-1 || clickCGIbaseURL==null ||
       clickCGIbaseURL.length()==0 )
      return(null);
    
    String dataURL= clickCGIbaseURL + x + "," + y;
    
    /* Now read the data*/
    if(msg!=null)
      showMsg("Reading data from active protein DB",Color.black);
    
    //String sData= flk.fio.readFileFromUrl(dataURL);        
    /* Read the data as byte[] and convert to string */
    byte uBuf[]= readBytesFromURL(dataURL, null);
    if(uBuf==null)  
      return(null);                   /* can't read file */
    String sData= new String(uBuf);
    
    if(sData==null)
      return(null);
    
    /* parse out the Swiss-Prot ID and protein name */
    int idx= sData.indexOf("1 protein has been found in the clicked spot");
    if(idx==-1)
      return(null);
    sData= sData.substring(idx);       /* shorten string */
    String sR[]= new String[2];
    idx= sData.indexOf("<A NAME=");
    if(idx==-1)
      idx= sData.indexOf("<a NAME="); /* need this kludge since they do it both ways!*/
    if(idx==-1)
      return(null);
    sData= sData.substring(idx+8);       /* shorten string */
    idx= sData.indexOf(">");
    if(idx==-1)
      return(null);
    String 
      id= sData.substring(0,idx),
      name= "";
    
    idx= sData.indexOf(">Entry name<");    
    sData= sData.substring(idx);     /* shorten string */
    if(idx==-1)
      return(sR);
    idx= sData.indexOf("<B>");     /* get start of protein name */
    if(idx==-1)
      idx= sData.indexOf("<b>");
    if(idx==-1)
      return(sR);
    int idx2= sData.indexOf("</B>");
    if(idx2==-1)
      idx2= sData.indexOf("</b>");  /* get end of protein name */
    if(idx2==-1)
      return(sR);
    name= sData.substring((idx+3),idx2); 
    
    sR[0]= id;
    sR[1]= name;
   
    return(sR);
  } /* getProteinIDdataByXYurl */
     
  
  /**
   * readFlkState() - read the Flicker State file stateFile 
   * with a .flk file extension.
   * @param stateFile is the name of the Flicker state file
   * @param readSpotListFilesFlag read the spt/*.spt files
   * @return hashtable of the state else null if not found or 
   *         the associated images are not found.
   */
  public synchronized Hashtable readFlkState(String stateFile,
                                             boolean readSpotListFilesFlag)
  { /* readFlkState */    
    String sMsg= "Reading Flicker ["+stateFile+"] state file";   
    File fdTst= new File(stateFile);
    if(!fdTst.exists())
    { /* problem - hash file not found */
      fatalReportMsg("\n===> Can't find Flicker disk .flk startup file\n"+
                     "["+stateFile+"] - ignoring startup file.\n\n",
                     -6000);
      return(null);
    }
    
    Hashtable ht= readNameValuesHashTableFromFile(stateFile,sMsg,15101);
    if(ht==null)
    { /* problem - hash file is corrupted */
      fatalReportMsg("\n===> Flicker .flk state file ["+stateFile+"]\n"+
                     "is corrupted - ignoring it and continuing.\n\n",
                     -6000);
      return(null);
    }
    setStateHashtableForGetValue(ht, nEntries);
    
    /* Parse the data in the hash table into state variables */
    if(nStateEntries==0)
      return(null);    
          
    String
      imageFile1= getStateValue("imageFile1", (String)null), 
      imageFile2= getStateValue("imageFile2", (String)null); 
    
    /* If they are disk files, lookup the files on the disk to make sure they
     * are still there. If not, put error messages in the Popup report window
     * and return null.
     */    
    if(!imageFile1.startsWith("http://"))
    { /* make sure the files exist */
      fdTst= new File(imageFile1);
      if(!fdTst.exists())
      { /* problem - image file not found */
        fatalReportMsg(
          "\n===> Can't find LEFT image file specified by .flk startup file in\n"+
          "["+stateFile+"]\n"+
          "ignoring .flk file. Missing disk image file is\n"+
          "["+imageFile1+"].\n\n", -12000);
        return(null);
      }
    } /* make sure the files exist */ 
    
    if(!imageFile2.startsWith("http://"))
    { /* make sure the files exist */
     fdTst= new File(imageFile2);
      if(!fdTst.exists())
      { /* problem - image file not found */
        fatalReportMsg(
          "\n===> Can't find RIGHT image file specified by .flk startup file in\n"+
          "["+stateFile+"]\n"+
          "ignoring .flk file. Missing disk image file is\n"+
          "["+imageFile2+"].\n\n", -12000);
        return(null);
      }
    } /* make sure the files exist */  
    
    /* Files are on the disk so copy them to the state! */           
    flk.imageFile1= imageFile1;
    flk.imageFile2= imageFile2; 
    
    /* [DEPRICATE]
    flk.i1IS.oGifFileName= getStateValue("oGifFileName1", (String)null); 
    flk.i2IS.oGifFileName= getStateValue("oGifFileName2", (String)null); 
    
    int
      xObj1= getStateValue("xObj1", 0),
      yObj1= getStateValue("yObj1", 0),
      xObj2= getStateValue("xObj2", 0),
      yObj2= getStateValue("yObj2", 0); 
    flk.i1IS.setObjPosition(xObj1, yObj1);
    flk.i1IS.setImgPosition(xObj1, yObj1);
    flk.i2IS.setObjPosition(xObj2, yObj2);
    flk.i2IS.setImgPosition(xObj2, yObj2);
     **/
    
    flk.iData1.idSL.measCtr= getStateValue("measCtr1", 0); 
    flk.iData2.idSL.measCtr= getStateValue("measCtr2", 0); 
       
    /* Parse the base flicker state properties */
    parseBaseFlkProperties();
    
    /* read the image slider states */
    flk.iData1.state.readState("I1");
    flk.iData2.state.readState("I2");    
    
    /* read the image landmark states for both I1 and I2 */
    flk.lms.readState();     
    
    /* Get the Spot lists if they exist. Note that the size of
     * the tmp spot lists returned by readState() returns the actual #
     * of spots found in the state file. We then copy that into a much
     * larger working list of size MAX_SPOTS.
     */
    if(readSpotListFilesFlag)
    { /* read the spt*.spt files */
      Spot tmpSpotList[];
      tmpSpotList= Spot.readState(flk.imageFile1, "I1");
      if(tmpSpotList==null)
        flk.iData1.idSL.measCtr= 0;                 /* reset it */
      flk.iData1.idSL.nSpots= (tmpSpotList==null) ? 0 : tmpSpotList.length;
      flk.iData1.idSL.spotList= new Spot[flk.MAX_SPOTS];  /* copy to state list */
      for(int i=0;i<flk.iData1.idSL.nSpots;i++)
        flk.iData1.idSL.spotList[i]= tmpSpotList[i];
      
      tmpSpotList= Spot.readState(flk.imageFile2, "I2");
      if(tmpSpotList==null)
        flk.iData2.idSL.measCtr= 0;                 /* reset it */
      flk.iData2.idSL.nSpots= (tmpSpotList==null) ? 0 : tmpSpotList.length;
      flk.iData2.idSL.spotList= new Spot[flk.MAX_SPOTS];  /* copy to state list */
      for(int i=0;i<flk.iData2.idSL.nSpots;i++)
        flk.iData2.idSL.spotList[i]= tmpSpotList[i];
    } /* read the spt*.spt files */
    
    return(flkStateHT);
  } /* readFlkState */
    
    
  /**
   * parseBaseFlkProperties() - parse the base Flicker State properties
   * in the current hash table. 
   * @return true if ok
   */
  public boolean parseBaseFlkProperties()
  { /* parseBaseFlkProperties */    
    /* Parse the data in the hash table into state variables */
    if(nStateEntries==0)
      return(false);    
                 
    flk.displayInfoFlag= getStateValue("displayInfoFlag", false); 
    //flk.userClickableImageDBflag= getStateValue("userClickableImageDBflag",false);      
    flk.allowXformFlag= getStateValue("allowXformFlag", true);  
    flk.composeXformFlag= getStateValue("composeXformFlag", false);    
    flk.useTotDensityFlag= getStateValue("useTotDensityFlag", true);    
    flk.useMeasCtrFlag= getStateValue("useMeasCtrFlag", true);     
    
    flk.useLogInputFlag= getStateValue("useLogInputFlag", false); 
      
    flk.viewLMSflag= getStateValue("viewLMSflag", false);   
    flk.viewTargetFlag= getStateValue("viewTargetFlag", false);   
    flk.viewTrialObjFlag= getStateValue("viewTrialObjFlag", false);   
    flk.viewBoundaryFlag= getStateValue("viewBoundaryFlag", false);    
    flk.viewRoiFlag= getStateValue("viewRoiFlag", false);     
    flk.viewMeasCircleFlag= getStateValue("viewMeasCircleFlag", true);
    flk.spotsListModeFlag= getStateValue("spotsListModeFlag", true); 
    
    flk.viewDrawSpotLocCircleFlag=
                           getStateValue("viewDrawSpotLocCircleFlag",true);
    flk.viewDrawSpotLocPlusFlag=
                            getStateValue("viewDrawSpotLocPlusFlag",false);
    flk.viewDrawSpotAnnNbrFlag=getStateValue("viewDrawSpotAnnNbrFlag",true);
    flk.viewDrawSpotAnnIdFlag=getStateValue("viewDrawSpotAnnIdFlag",false);
    
    flk.viewMultPopups= getStateValue("viewMultPopups", false);   
    flk.viewGangScrollFlag= getStateValue("viewGangScrollFlag", false);  
    flk.viewGangZoomFlag= getStateValue("viewGangZoomFlag", false);  
    flk.viewGangBCFlag= getStateValue("viewGangBCFlag", false); 
    if(flk.USE_GUARD)
      flk.useGuardRegionImageFlag= getStateValue("useGuardRegionImageFlag", false);
    else
      flk.useGuardRegionImageFlag= false;
    flk.viewDispGrayValuesFlag= getStateValue("viewDispGrayValuesFlag",
                                              false);   
    flk.viewNormalizedColorFlag= getStateValue("viewNormalizedColorFlag",
                                               false);   
    flk.viewPseudoColorFlag= getStateValue("viewPseudoColorFlag", false);    
    flk.viewRGB2GrayFlag= getStateValue("viewRGB2GrayFlag", false); 
    flk.viewReportPopupFlag= getStateValue("viewReportPopupFlag",true);
    
    flk.useNTSCrgbTograyCvtFlag= getStateValue("useNTSCrgbTograyCvtFlag",false);
    
    flk.doMeasureProtIDlookupAndPopupFlag= 
                        getStateValue("doMeasureProtIDlookupAndPopupFlag",false);
    flk.useSwiss2DpageServerFlag= getStateValue("useSwiss2DpageServerFlag",false);
    flk.usePIRUniprotServerFlag= getStateValue("usePIRUniprotServerFlag",false);
    flk.usePIRiProClassServerFlag= getStateValue("usePIRiProClassServerFlag",false);
    flk.usePIRiProLinkFlag= getStateValue("usePIRiProLinkFlag",false);
        
    flk.useThresholdInsideFlag= getStateValue("useThresholdInsideFlag",true);
    flk.saveOimagesWhenSaveStateflag= getStateValue("saveOimagesWhenSaveStateflag",
                                                    true);    
    flk.useProteinDBbrowserFlag= getStateValue("useProteinDBbrowserFlag",
                                               true);    
    
    flk.currentPRIbaseURL= getStateValue("currentPRIbaseURL",flk.UNIPROT);
       
    //.clickableCGIbaseURL1= getStateValue("clickableCGIbaseURL1", null); 
    //flk.clickableCGIbaseURL2= getStateValue("clickableCGIbaseURL2", null); 
    //flk.clickableCGIbaseURL1pix= getStateValue("clickableCGIbaseURL1pix",null); 
    //flk.clickableCGIbaseURL2pix= getStateValue("clickableCGIbaseURL2pix",null);
              
    flk.measCircleRadius= getStateValue("measCircleRadius", 
                                        flk.DEF_CIRCLE_MASK_RADIUS); 
    flk.bkgrdCircleRadius= getStateValue("bkgrdCircleRadius", 
                                         flk.measCircleRadius);
    flk.nCirMask= getStateValue("nCirMask", flk.measCircleRadius);
        
    flk.winDumpRadix= getStateValue("winDumpRadix", Windump.SHOW_DECIMAL);
    flk.maxColsToPrint= getStateValue("maxColsToPrint", 20); 
       
    flk.defaultFlickerDelay= getStateValue("defaultFlickerDelay", 
                                           BuildGUI.DEFAULT_DELAY);
        
    flk.canvasSize= getStateValue("canvasSize", flk.CANVAS_SIZE);
    flk.frameWidth= getStateValue("frameWidth", flk.scrWidth);
    flk.frameHeight= getStateValue("frameHeight", flk.scrHeight);
    
    flk.colorMode= getStateValue("colorMode", 0); 
    
    flk.targetColor= cvColorValue2Color(getStateValue("targetColor",
                                          cvColor2ColorValue(Color.cyan)));
    flk.trialObjColor= cvColorValue2Color(getStateValue("trialObjColor",
                                          cvColor2ColorValue(Color.orange)));
    flk.lmsColor= cvColorValue2Color(getStateValue("lmsColor", 
                                          cvColor2ColorValue(Color.red)));
    flk.roiColor= cvColorValue2Color(getStateValue("roiColor",
                                          cvColor2ColorValue(Color.blue)));
    flk.bkgrdCircleColor= cvColorValue2Color(getStateValue("bkgrdCircleColor",
                                          cvColor2ColorValue(Color.magenta)));
    flk.measCircleColor= cvColorValue2Color(getStateValue("measCircleColor",
                                          cvColor2ColorValue(Color.red)));
    
    /* Also set the curState */
    flk.curState.measCircleRadius= flk.measCircleRadius;    
    flk.curState.bkgrdCircleRadius= flk.bkgrdCircleRadius;
            
    return(true);
  } /* parseBaseFlkProperties */
  

  /**
   * writeFlkState() - write a .flk state file 
   * @param flkStateFile to write
   * @return true if wrote the file 
   */
  boolean writeFlkState(String flkStateFile)
  { /* writeFlkState */
    if(flkStateFile==null)
      return(false);
    
    if(flk.saveOimagesWhenSaveStateflag && flk.allowXformFlag)
    { /* "Enable saving transformed image when do a 'Save(As) state'" */
      flk.i1IS.saveOImgAsGifFile(null  /* use default */, 
                                 false /* no prompt */);
      flk.i2IS.saveOImgAsGifFile(null  /* use default */, 
                                 false /* no prompt */);
    }
    
    String sData= getStateStr();
    
    /* Now write out the state file */
    boolean flag= flk.fio.writeFileToDisk(flkStateFile, sData);
    if(flag)
      flk.flkStateFile= flkStateFile;        /* make global */
          
    /* clear the flags */
    flk.iData1.idSL.changeSpotList= false;
    flk.iData2.idSL.changeSpotList= false;
    
    return(flag);
  } /* writeFlkState */
  
  
  /**
   * writeCalibrationFile() - write the image calibration into the
   * "cal/{{demo image file base}.cal" file.
   * "cal/{subdirectory}-DIR-{image file base}.cal" file.
   * @param iData is the Image Data to write the calibration
   * @return true if succeed
   */
  public boolean writeCalibrationFile(ImageData iData)
  { /* writeCalibrationFile */    
    String imageFileName;
    if(iData!=flk.iData1 && iData!=flk.iData2)
      return(false); /* must be selected */
      
    imageFileName= iData.imageFile;
      
    /* Compute the .cal file from theimageFile */
    String calFile= getFullCalFilePath(iData);
        
    StringBuffer sBuf= new StringBuffer(100000);  /* est. - optimize */
    iData.calib.writeState(imageFileName,sBuf);   /* fill the buffer */
    
    String sData= new String(sBuf);               /* convert to String */
    
    /* Now write out the state file */
    boolean flag= flk.fio.writeFileToDisk(calFile, sData);
    
    return(flag);
  } /* writeCalibrationFile */
  
  
  /**
   * getStateStr() - generate a tab-delim Flicker State string for
   * when we write out the .flk state.
   * The format is tab-delimted (name \t value \n) with one entry/line.
   * @return the state string
   */
  public String getStateStr()
  { /* getStateStr*/
    StringBuffer sBuf= new StringBuffer(100000);  /* est. - optimize */
    
    sBuf.append("VERSION\t"+flk.VERSION+"\n");
    sBuf.append("DATE\t"+flk.DATE+"\n");
    
    sBuf.append("imageFile1\t"+flk.imageFile1+"\n");
    sBuf.append("imageFile2\t"+flk.imageFile2+"\n");
    
    if(flk.i1IS.oGifFileName!=null)
      sBuf.append("oGifFileName1\t"+flk.i1IS.oGifFileName+"\n");    
    if(flk.i2IS.oGifFileName!=null)
      sBuf.append("oGifFileName2\t"+flk.i2IS.oGifFileName+"\n");
    
    sBuf.append("xObj1\t"+flk.i1IS.getXObjPosition()+"\n");
    sBuf.append("yObj1\t"+flk.i1IS.getYObjPosition()+"\n");
    sBuf.append("xObj2\t"+flk.i2IS.getXObjPosition()+"\n");
    sBuf.append("yObj2\t"+flk.i2IS.getYObjPosition()+"\n");
    
    sBuf.append("measCtr1\t"+flk.iData1.idSL.measCtr+"\n");
    sBuf.append("measCtr2\t"+flk.iData2.idSL.measCtr+"\n");
        
    /* Add the tab-delim base Flicker properties State string */    
    getBaseFlkPropertiesStr(sBuf); 
    
    /* write out the image slider states */
    flk.iData1.state.writeState("I1",sBuf);
    flk.iData2.state.writeState("I2",sBuf);
    
    /* write out the image landmark states for both I1 and I2 */
    flk.lms.writeState(sBuf);
        
    /* write the Spot lists if they exist */
    Spot.writeState(flk.iData1.imageFile, "I1", flk.iData1.idSL.spotList,
                    flk.iData1.idSL.nSpots);
    Spot.writeState(flk.iData2.imageFile, "I2", flk.iData2.idSL.spotList, 
                    flk.iData2.idSL.nSpots);
    
    String sData= new String(sBuf);
    return(sData);
  } /* getStateStr */
  
  
  /**
   * getBaseFlkPropertiesStr() - add generate tab-delim base Flicker 
   * properties State string to the string buffer
   * The format is tab-delimited (name \t value \n) with one entry/line.
   * @param sBuf is the string buffer to add data to
   */
  public void getBaseFlkPropertiesStr(StringBuffer sBuf)
  { /* getBaseFlkPropertiesStr*/            
    sBuf.append("displayInfoFlag\t"+flk.displayInfoFlag+"\n");
    sBuf.append("userClickableImageDBflag\t"+flk.userClickableImageDBflag+"\n");    
    sBuf.append("allowXformFlag\t"+flk.allowXformFlag+"\n");
    sBuf.append("composeXformFlag\t"+flk.composeXformFlag+"\n");     
    
    sBuf.append("useTotDensityFlag\t"+flk.useTotDensityFlag+"\n"); 
    sBuf.append("useMeasCtrFlag\t"+flk.useMeasCtrFlag+"\n");     
    sBuf.append("useLogInputFlag\t"+flk.useLogInputFlag+"\n");
    
    sBuf.append("viewLMSflag\t"+flk.viewLMSflag+"\n");
    sBuf.append("viewTargetFlag\t"+flk.viewTargetFlag+"\n");
    sBuf.append("viewTrialObjFlag\t"+flk.viewTrialObjFlag+"\n");
    sBuf.append("viewBoundaryFlag\t"+flk.viewBoundaryFlag+"\n");
    sBuf.append("viewRoiFlag\t"+flk.viewRoiFlag+"\n");
    
    sBuf.append("viewDrawSpotLocCircleFlag\t"+
                flk.viewDrawSpotLocCircleFlag+"\n");
    sBuf.append("viewDrawSpotLocPlusFlag\t"+
                flk.viewDrawSpotLocPlusFlag+"\n");
    sBuf.append("viewDrawSpotAnnNbrFlag\t"+
                flk.viewDrawSpotAnnNbrFlag+"\n");
    sBuf.append("viewDrawSpotAnnIdFlag\t"+
                flk.viewDrawSpotAnnIdFlag+"\n");
    
    sBuf.append("viewMeasCircleFlag\t"+flk.viewMeasCircleFlag+"\n");
    sBuf.append("spotsListModeFlag\t"+flk.spotsListModeFlag+"\n");
    sBuf.append("viewMultPopups\t"+flk.viewMultPopups+"\n");
    sBuf.append("viewGangScrollFlag\t"+flk.viewGangScrollFlag+"\n");
    sBuf.append("viewGangZoomFlag\t"+flk.viewGangZoomFlag+"\n");
    sBuf.append("viewGangBCFlag\t"+flk.viewGangBCFlag+"\n");
    sBuf.append("useGuardRegionImageFlag\t"+flk.useGuardRegionImageFlag+"\n");    
    sBuf.append("viewDispGrayValuesFlag\t"+flk.viewDispGrayValuesFlag+"\n");
    sBuf.append("viewNormalizedColorFlag\t"+flk.viewNormalizedColorFlag+"\n");
    sBuf.append("viewPseudoColorFlag\t"+flk.viewPseudoColorFlag+"\n");
    sBuf.append("viewRGB2GrayFlag\t"+flk.viewRGB2GrayFlag+"\n");
    sBuf.append("viewReportPopupFlag\t"+flk.viewReportPopupFlag+"\n");
        
    sBuf.append("useNTSCrgbTograyCvtFlag\t"+flk.useNTSCrgbTograyCvtFlag+"\n");
    
    sBuf.append("doMeasureProtIDlookupAndPopupFlag\t"+
                flk.doMeasureProtIDlookupAndPopupFlag+"\n");
    sBuf.append("useSwiss2DpageServerFlag\t"+flk.useSwiss2DpageServerFlag+"\n");
    sBuf.append("usePIRUniprotServerFlag\t"+flk.usePIRUniprotServerFlag+"\n");
    sBuf.append("usePIRiProClassServerFlag\t"+flk.usePIRiProClassServerFlag+"\n");
    sBuf.append("usePIRiProLinkFlag\t"+flk.usePIRiProLinkFlag+"\n");
            
    sBuf.append("useThresholdInsideFlag\t"+flk.useThresholdInsideFlag+"\n");
    sBuf.append("saveOimagesWhenSaveStateflag\t"+
                flk.saveOimagesWhenSaveStateflag+"\n");
    sBuf.append("useProteinDBbrowserFlag\t"+
                flk.useProteinDBbrowserFlag+"\n");    
    
    sBuf.append("currentPRIbaseURL\t"+flk.currentPRIbaseURL+"\n");
        
    //sBuf.append("clickableCGIbaseURL1\t"+flk.clickableCGIbaseURL1+"\n");
    //sBuf.append("clickableCGIbaseURL2\t"+flk.clickableCGIbaseURL2+"\n");
    //sBuf.append("clickableCGIbaseURL1pix\t"+flk.clickableCGIbaseURL1pix+"\n");    
    //sBuf.append("clickableCGIbaseURL2pix\t"+flk.clickableCGIbaseURL2pix+"\n");
    
    sBuf.append("bkgrdCircleRadius\t"+flk.bkgrdCircleRadius+"\n");
    sBuf.append("measCircleRadius\t"+flk.measCircleRadius+"\n");   
    sBuf.append("nCirMask\t"+flk.nCirMask+"\n");  
    
    sBuf.append("winDumpRadix\t"+flk.winDumpRadix+"\n");  
    sBuf.append("maxColsToPrint\t"+flk.maxColsToPrint+"\n");
    
    sBuf.append("defaultFlickerDelay\t"+flk.defaultFlickerDelay+"\n"); 
        
    sBuf.append("canvasSize\t"+flk.canvasSize+"\n"); 
    sBuf.append("frameWidth\t"+flk.frameWidth+"\n"); 
    sBuf.append("frameHeight\t"+flk.frameHeight+"\n");
    
    sBuf.append("colorMode\t"+flk.colorMode+"\n");
    
    sBuf.append("targetColor\t"+cvColor2ColorValue(flk.targetColor)+"\n");
    sBuf.append("trialObjColor\t"+cvColor2ColorValue(flk.trialObjColor)+"\n");
    sBuf.append("lmsColor\t"+cvColor2ColorValue(flk.lmsColor)+"\n");
    sBuf.append("roiColor\t"+cvColor2ColorValue(flk.roiColor)+"\n");
    sBuf.append("bkgrdCircleColor\t"+
                cvColor2ColorValue(flk.bkgrdCircleColor)+"\n");
    sBuf.append("measCircleColor\t"+
                cvColor2ColorValue(flk.measCircleColor)+"\n");
  } /* getBaseFlkPropertiesStr */
  
    
  /**
   * getStateValue() - get PARAM and use default if not found.
   */
  public String getStateValue(String key, String def)
  { /* getStateValue */    
    if(flkStateHT==null)
      return(def);
    
    String value= (String)flkStateHT.get(key);
    if(value==null || value.equals("null"))
      value= def;
    return(rmvRtnChars(value));
  } /* getStateValue */
  
  
  /**
   * getStateValue() - get int PARAM and use default if not found.
   */
  public int getStateValue(String key, int def)
  { /* getStateValue */
    String sVal= getStateValue(key, null);
    int val;
    
    if(sVal==null)
      return(def);
    try
    {
      Integer valP= new Integer(sVal);
      val= valP.intValue();
    }
    catch(NumberFormatException e)
    { val= def;}
    
    return(val);
  } /* getStateValue */  
  
  
  /**
   * getStateValue() - get float PARAM and use default if not found.
   */
  public long getStateValue(String key, long def)
  { /* getStateValue */
    String sVal= getStateValue(key, null);    
    long val;
    
    if(sVal==null)
      return(def);
    try
    {
      Long valP= new Long(sVal);
      val= valP.longValue();
    }
    catch(NumberFormatException e)
    { val= def;}
    
    return(val);
  } /* getStateValue */   
  
  
  /**
   * getStateValue() - get float PARAM and use default if not found.
   */
  public float getStateValue(String key, float def)
  { /* getStateValue */
    String sVal= getStateValue(key, null);    
    float val;
    
    if(sVal==null)
      return(def);
    try
    {
      Float valP= new Float(sVal);
      val= valP.floatValue();
    }
    catch(NumberFormatException e)
    { val= def;}
    
    return(val);
  } /* getStateValue */
  
  
  /**
   * getStateValue() - get boolean PARAM and use default if not found.
   */
  public boolean getStateValue(String key, boolean def)
  { /* getStateValue */
    String
      sDef= (def) ? "TRUE" : "FALSE",
      sVal= getStateValue(key, sDef);
    boolean value= (sVal!=null && sVal.equalsIgnoreCase("TRUE"));
    
    return(value);
  } /* getStateValue */ 
  
  
  /**
   * copyFile() - binary copy of one file or URL toa local file
   * @param srcName is either a full path local file name or
   *        a http:// prefixed URL string of the source file.
   * @param dstName is the full path of the local destination file name
   * @param optUpdateMsg (opt) will display message in showMsg() and
   *        increasing ... in showMsg2(). One '.' for every 10K bytes read.
   *        This only is used when reading a URL. Set to null if not used.
   * @param optEstInputFileLth is the estimate size of the input file if 
   *        known else 0. Used in progress bar.
   * @return true if succeed.
   */
  public boolean copyFile(String srcName, String dstName,
                          String optUpdateMsg, int optEstInputFileLth)
  { /* copyFile */
    try
    { /* copy data from input to output file */
      FileOutputStream dstFOS= new FileOutputStream(new File(dstName));
      FileInputStream srcFIS= null;
      int
      bufSize= 100000,
      nBytesRead= 0,
      nBytesWritten= 0;
      byte buf[]= new byte[bufSize];
      
      boolean isURL= (srcName.startsWith("http://"));
      if(isURL)
      { /* Copy the file from Web site */
        if(optUpdateMsg!=null)
          showMsg(optUpdateMsg, Color.white);
        String sDots= "";
        URL url= new URL(srcName);
        InputStream urlIS= url.openStream();
        int nTotBytesRead= 0;
        while(true)
        { /* read-write loop */
          if(optUpdateMsg!=null)
          { /* show progress every read */
            sDots += ".";
            String
            sPct= (optEstInputFileLth>0)
                     ? ((int)((100*nTotBytesRead)/optEstInputFileLth))+"% "
                     : "",
            sProgress= "Copying " + sPct + sDots;
            showStatus(sProgress,Color.magenta);  /* DONT APPEND to report! */
          }
          nBytesRead= urlIS.read(buf);
          nTotBytesRead += nBytesRead;
          if(nBytesRead==-1)
            break;         /* end of data */
          else
          {
            dstFOS.write(buf,0,nBytesRead);
            nBytesWritten += nBytesRead;
          }
        } /* read-write loop */
        dstFOS.close();
        if(optUpdateMsg!=null)
        {
          showMsg("", Color.black);
        }
      }
      else
      { /* copy the file on the local file system */
        srcFIS= new FileInputStream(new File(srcName));
        while(true)
        { /* read-write loop */
          nBytesRead= srcFIS.read(buf);
          if(nBytesRead==-1)
            break;         /* end of data */
          else
          {
            dstFOS.write(buf,0,nBytesRead);
            nBytesWritten += nBytesRead;
          }
        } /* read-write loop */
        srcFIS.close();
        dstFOS.close();
      } /* copy the file on the local file system */
    } /* copy data from input to output file */
    
    catch(Exception e1)
    { /* just fail if any problems at all! */
      return(false);
    }
    
    return(true);
  } /* copyFile */
  
  
  /**
   * readBytesFromURL() - read binary data from URL
   * @param srcName is either a full path local file name or
   *        a http:// prefixed URL string of the source file.
   * @param optUpdateMsg (opt) will display message in showMsg() and
   *        increasing ... in showMsg2(). One '.' for every 10K bytes read.
   *        This only is used when reading a URL. Set to null if not used.
   * @return a byte[] if succeed, else null.
   */
  public byte[] readBytesFromURL(String srcName, String optUpdateMsg)
  { /* readBytesFromURL */
    if(!srcName.startsWith("http://"))
      return(null);
    int
      bufSize= 20000,
      nBytesRead= 0,
      nBytesWritten= 0,
      oByteSize= bufSize;
    byte
      buf[]= null,
      oBuf[]= null;
    
    try
    { /* copy data from input to output file */
      buf= new byte[bufSize];
      oBuf= new byte[bufSize];
      
      /* Copy the file from Web site */
      if(optUpdateMsg!=null)
        showMsg(optUpdateMsg, Color.black);
      String sDots= "";
      URL url= new URL(srcName);
      InputStream urlIS= url.openStream();
      while(true)
      { /* read-write loop */
        if(optUpdateMsg!=null)
        { /* show progress every read */
          sDots += ".";
          String sProgress= "Reading " + sDots;
          showMsg(sProgress, Color.red);
        }
        
        nBytesRead= urlIS.read(buf);
        if(nBytesRead==-1)
          break;         /* end of data */
        else
        { /* copy buf to end of oBuf */
          if(nBytesRead+nBytesWritten >= oByteSize)
          { /* regrow oBuf by bufSize */
            byte tmp[]= new byte[oByteSize+bufSize];
            for(int i=0;i<nBytesWritten;i++)
              tmp[i]= oBuf[i];
            oBuf= tmp;
            oByteSize += bufSize;
          }
          for(int i=0;i<nBytesRead;i++)
            oBuf[nBytesWritten++]= buf[i];
          //nBytesWritten += nBytesRead;  /* append bytes to end of list */
        }
      } /* read-write loop */
      
      /* shrink oBuf to exact size needed */
      byte tmp[]= new byte[nBytesWritten];
      for(int i=0;i<nBytesWritten;i++)
        tmp[i]= oBuf[i];
      oBuf= tmp;
      
      if(optUpdateMsg!=null)
      {
        showMsg("",Color.black);
      }
    } /* copy data from input to output file */
    
    catch(Exception e)
    { /* just fail if any problems at all! */
      System.out.println("readBytesFromURL() e="+e);
      return(null);
    }
    
    return(oBuf);
  } /* readBytesFromURL */
  
  
  /**
   * deleteLocalFile() - delete local file.
   * @return false if failed..
   */
  public boolean deleteLocalFile(String fileName)
  { /* deleteLocalFile */
    try
    {
      File srcF= new File(fileName);
      if(srcF.exists())
        srcF.delete();      /* delete it first */
    }
    catch(Exception e)
    { return(false); }
    
    return(true);
  } /* deleteLocalFile */
  
  
  /**
   * updateFlickerJarFile() - update Flicker.jar into program install area.
   *<PRE>
   * [1] Define directory for Flicker.jar path and other file and URL names.
   * [2] Backup the old Flicker.jar as Flicker.jar.bkup
   * [3] Open the url: from flkJarURL. This is hardwired to be
   *         "http://www.lecb.ncifcrf.gov/Flicker/Flicker.jar"
   *     and read the file from the Web into local file "Flicker.jar.tmp"
   * [4] Move the "Flicker.jar.tmp" file into "Flicker.jar" in the
   *     program directory
   *
   * Since changing the Flicker.jar file is a potential security risk,
   * we make this procedure final and hardwire the flkJarURL!
   *</PRE>
   * @return true if succeed
   * @see #copyFile
   * @see #deleteLocalFile
   */
  final boolean updateFlickerJarFile()
  { /* updateFlickerJarFile */
    /* [1] Define directory for Flicker.jar path and other file
     * and URL names.
     */
    String
    fileSep= flk.fileSeparator,
    userDir= flk.userDir,
    localFLKjarFile= userDir + "Flicker.jar",
    localFLKjarFileBkup= userDir + "Flicker.jar.bkup",
    localFLKjarFileTmp= userDir + "Flicker.jar.tmp",
    flkJarURL= flk.baseFlkServer+"Flicker.jar",
    flkJarServer= flk.baseFlkServer;
    
    /* [2] Backup the old Flicker.jar as Flicker.jar.bkup if it exists
     * (it won't if you are running from the debugger!).
     * But first, delete old backup if it exists.
     */
    deleteLocalFile(localFLKjarFileBkup);
    copyFile(localFLKjarFile, localFLKjarFileBkup, null,0);
    
    /* [3] Open the url: flkJarURL and read the file from the Web into
     * "Flicker.jar.tmp"
     */
    String updateMsg= "Updating your Flicker.jar file from "+flkJarServer+
                      " server.";
    File f= new File(localFLKjarFileBkup);
    int estInputFileLth= (f!=null) ? (int)f.length() : 0;
    if(! copyFile(flkJarURL,localFLKjarFileTmp, updateMsg, estInputFileLth))
      return(false);
    
    /* [4] Move the "Flicker.jar.tmp" file into  "Flicker.jar" in the
     * program directory where it was installed.
     */
    if(! deleteLocalFile(localFLKjarFile))
      return(false);
    if(! copyFile(localFLKjarFileTmp, localFLKjarFile, null,0))
      return(false);
    
    return(true);
  } /* updateFlickerJarFile */
  
  
  /**
   * updateFlkDBfiles() - update DB/<dbList> files into program install
   * area.
   *<PRE>
   * [1] Define directory for DB/Flk*DB.txt path and other file and URL 
   *     names.
   * [2] Backup the old DB/<dbList> as FlkMapDB.txt.bkup
   * [3] Open the url: from flkMapDBURL. This is hardwired to be
   *         "http://www.lecb.ncifcrf.gov/Flicker/DB/< dbList>"
   *     and read the file from the Web into local file "< dbList >.tmp"
   * [4] Move the "< dbList >.tmp" file into "DB/<dbList>.txt" in the 
   *    program directory
   *
   * Since changing the DB/<dbList> file is a potential security risk,
   * we make this procedure final and hardwire the flk*DBURL!
   *</PRE>
   * @param dbFile is a list of DB/<files> to read
   * @return true if succeed
   * @see #copyFile
   * @see #deleteLocalFile
   */
  final boolean updateFlkDBfiles(String dbFile[])
  { /* updateFlkDBfiles */
    /* [1] Define directory for DB/Flk*DB.txt path and other file
     * and URL names.
     */
    String
      fileName,
      fileSep= flk.fileSeparator,
      userDir= flk.userDir;
    int nFiles= dbFile.length;      
      
    for(int k=0;k<nFiles;k++)
    { /* Copy DB files */
      fileName= dbFile[k];
      String
        localFlkDBfile= userDir + "DB"+flk.fileSeparator+fileName,
        localFlkDBfileBkup= userDir + fileName+".bkup",
        localFlkDBfileTmp= userDir + fileName+".tmp",
        flkDBURL= flk.baseFlkServer+"DB/"+fileName,
        flkDBserver= flk.baseFlkServer;
      
      /* [2] Backup the old <fileName> as <fileName>.txt.bkup if it exists
      * (it won't if you are running from the debugger!).
       * But first, delete old backup if it exists.
       */
      deleteLocalFile(localFlkDBfileBkup);
      copyFile(localFlkDBfile, localFlkDBfileBkup, null,0);
      
      /* [3] Open the url: flkDBURL and read the file from the Web into
       * "<fileName>.txt.tmp"
      */
      String updateMsg= "Updating your DB/"+fileName+" file from "+
                        flkDBserver+ " server.";
      File f= new File(localFlkDBfileBkup);
      int estInputFileLth= (f!=null) ? (int)f.length() : 0;
      if(! copyFile(flkDBURL,localFlkDBfileTmp,updateMsg,estInputFileLth))
        return(false);
      
      /* [4] Move the "<fileName>.txt.tmp" file into  "DB/<fileName>" in the
       * program directory where it was installed.
       */
      if(! deleteLocalFile(localFlkDBfile))
        return(false);
      if(! copyFile(localFlkDBfileTmp, localFlkDBfile, null,0))
        return(false);
    } /* Copy DB files */
    
    return(true);
  } /* updateFlkDBfiles */
  
  
  /**
   * updateDemoDBfiles() - update DB/FlkDemoDB.txt into program install
   * area. Also read the DB/FlkDemoDB.dir to get the list of files in the
   * Images/ directory to read.
   *<PRE>
   * [1] Get the list of files in DB/FlkDemoDB.dir into < imgList >
   * [2] For each iFile in imgList[1:nFiles],
    *  [2.1] Backup the old Image/imgList[i] as imgList[i].bkup
   *   [2.2] Open the url: from flkDemoDBURL. This is hardwired to be
   *         "http://www.lecb.ncifcrf.gov/Flicker/Image/"+imgList[i]
   *         and read the file from the Web into local file 
   *          imgList[i]+".tmp"
   *   [2.3] Move the imgList[i]+".tmp" file into "Images/"+imgList[i] in the
   *    program directory
   * [3] Copy the "http://www.lecb.ncifcrf.gov/Flicker/Images/*" files
   *   to "<startupDB>/Images".
   *
   * Since changing the DB/FlkDemoDB.txt file is a potential security risk,
   * we make this procedure final and hardwire the flkDemoDBURL!
   *</PRE>
   * @return true if succeed
   * @see #copyFile
   * @see #deleteLocalFile
   */
  final boolean updateDemoDBfiles()
  { /* updateDemoDBfiles */    
    /* [1] Read the Demo DB and list of images directory file */
    String dbFile[]= {
                       "FlkDemoDB.dir",
                       "FlkDemoDB.txt"
                     };
                     
    if(!updateFlkDBfiles(dbFile))
      return(false);
                     
    /* [2] Read the directory file */
    String
      userDir= flk.userDir,
      dirStr= "DB" + flk.fileSeparator + "FlkDemoDB.dir",
      imgDirStr= flk.fio.readData(dirStr, "Reading local demo FlkDemoDB.dir"),
      imgList[]= cvs2Array(imgDirStr, 100,"\n");
    int nFiles= imgList.length;
                     
    /* [3] Read the list of image files into Images/ directory */           
    for(int k=0;k<nFiles;k++)
    { /* Copy DB files */     
      String
        fileName= imgList[k],
        localFlkDBfile= userDir + "Images"+flk.fileSeparator+fileName,
        localFlkDBfileBkup= userDir + fileName+".bkup",
        localFlkDBfileTmp= userDir + fileName+".tmp",
        flkDBURL= flk.baseFlkServer+"Images/"+fileName,
        flkDBserver= flk.baseFlkServer;
      
      /* [3.1] Backup the old <fileName> as <fileName>.txt.bkup if it exists
       * (it won't if you are running from the debugger!).
       * But first, delete old backup if it exists.
       */
      deleteLocalFile(localFlkDBfileBkup);
      copyFile(localFlkDBfile, localFlkDBfileBkup, null,0);
      
      /* [3.2] Open the url: flkDBURL and read the file from the Web into
       * "<fileName>.txt.tmp"
      */
      String updateMsg= "Updating your DemoDB/"+fileName+" files from "+
                        flkDBserver+ " server.";
      File f= new File(localFlkDBfileBkup);
      int estInputFileLth= (f!=null) ? (int)f.length() : 0;
      if(! copyFile(flkDBURL,localFlkDBfileTmp,updateMsg,estInputFileLth))
        return(false);
      
      /* [3.3] Move the "<fileName>.txt.tmp" file into  "DB/<fileName>" in the
       * program directory where it was installed.
       */
      if(! deleteLocalFile(localFlkDBfile))
        return(false);
      if(! copyFile(localFlkDBfileTmp, localFlkDBfile, null,0))
        return(false);
    } /* Copy DB files */
                     
    return(true);
  } /* updateDemoDBfiles */
  
   
  /**
   * addUserDemoFilesDBbyURL() - Add user's demo images to DB by userURL.
   * This will append a user's DB/FlkDemoDB.txt data with the current
   * Flicker DB/FlkDemoDB.txt file in the installation area.
   * Also read the user's DB/FlkDemoDB.dir to get the list of demo
   * files to read and add from the user's server to the installation
   * Images/ directory.
   *<PRE>
   * [1] This assumes that the DB/FlkDemoDB.txt has been read into the
   *    FlkDemo database in memory on when Flicker was started.
   *   [1.1] Copy DB/FlkDemo.txt to DB/FlkDemo.txt.bkup
   * [2] Read the URL file < userURL >/DB/FlkDemoDB.dir into < userImgList >
   * [3] Read the URL file < userURL >/DB/FlkDemoDB.txt into another instance
   *     of FlkDemo.
   * [4] Using < userImgList >, read each iFile in userImgList[1:nFiles],
   *   [4.1] Open the url: from userDemoDBurl. This URL compute as
   *           ( userDemoDBurl + "Images/" + userImgList[i] )
   *         and read the file from the Web into local file 
   *            ( "Images/" + ( userImgList[i])
   *   [4.2] Append an the entry for userImgList[i] into the in-core FlkDemo
   *         data structure.
   * [5] If successful, write out the DB/FlkDemo.txt file with the new data.
   * [6] Give user message that they need to restart Flicker to see the changes.
   *
   * Since changing the DB/FlkDemoDB.txt file is a potential security risk,
   * we make this procedure final and hardwire the flkDemoDBURL!
   *</PRE>
   * @param userDemoDBurl is the base address for the user's demo data
   *   this Web directory will contain subdirectories Images/ and DB/
   *   and DB/ contains FlkDemoDB.dir and FlkDemoDB.txt with the entries
   *   for the corresponding images.
   * @return true if succeed
   * @see #copyFile
   * @see #deleteLocalFile
   */      
   public boolean addUserDemoFilesDBbyURL(String userDemoDBurl)
   { /* addUserDemoFilesDBbyURL */   
     /* [1] Make sure URL is valid */
     if(userDemoDBurl==null || !userDemoDBurl.startsWith("http://"))
     {
       String msg= "Invalid URL - aborting";
       showMsg(msg, Color.red);
       popupAlertMsg(msg, flk.alertColor);
       return(false);
     }
     
     /* [1.1] Setup the URLS to the user's server */
     if(! userDemoDBurl.endsWith("/"))
      userDemoDBurl += "/";             /* make it consistent */
     String 
       urlBaseImagesList= userDemoDBurl + "DB/FlkDemoDB.dir",
       urlBaseDemoDBTbl= userDemoDBurl + "DB/FlkDemoDB.txt";
           
     /* [2] Read the users list of images directory and FlkDemoDB.dir file */
     String
       userImgDirStr= flk.fio.readData(urlBaseImagesList, 
                                       "Reading user demo FlkDemoDB.dir");
     if(userImgDirStr==null)
     {
       String msg= "Can't find ["+urlBaseImagesList+"] - aborting";
       showMsg(msg, Color.red);
       popupAlertMsg(msg, flk.alertColor);
       return(false);
     }
     String userImgList[]= cvs2Array(userImgDirStr, 100,"\n");
     int nFiles= userImgList.length;
   
     /* [3] Read the user's FlkDemoDB.txt files*/
     String
       userImgDemoDBStr= flk.fio.readData(urlBaseDemoDBTbl, 
                                          "Reading user demo FlkDemoDB.txt"); 
     if(userImgDemoDBStr==null)
     {
       String msg= "Can't find ["+urlBaseDemoDBTbl+"] - aborting";
       showMsg(msg, Color.red);
       popupAlertMsg(msg, flk.alertColor);
       return(false);
     }
     
     /* [3.1] Now  parse and add entry to global to flkDemos[0:nMaps-1]
      * if the data is well formed 
      */
     if(! flk.fDemo.parseAndAddEntry(userImgDemoDBStr))
     {
       String msg= "Ill-formed user FlkDemoDB.txt file["+urlBaseDemoDBTbl+
                   "] - aborting";
       showMsg(msg, Color.red);
       popupAlertMsg(msg, flk.alertColor);
       return(false);
     }
                                          
     /* [4] Read the list of image files from urlBaseImages server
      * into the local Images/ directory 
     */           
     for(int k=0;k<nFiles;k++)
     { /* Copy DB files */     
       String
         fileName= userImgList[k],
         localFlkImagesDBfile= flk.userDir + "Images"+flk.fileSeparator+fileName,
         urlBaseImagesFile= userDemoDBurl + "Images/"+fileName;
            
       /* [4.1] Open the url: flkDBURL and read the file from the Web into
        * "<fileName>.txt.tmp"
       */
       String updateMsg= "Appending Demo Images/"+fileName+" file from "+
                         userDemoDBurl+ " server.";
       if(! copyFile(urlBaseImagesFile,localFlkImagesDBfile,updateMsg,0))
       { /* remove the entry from the flk.fDemo.FlkDemo[] DB */
         return(false);
       }
      
       /* [4.2] [TODO] Verify update the FlkDemo database with the new entries. */
       
    } /* Copy DB files */
                     
    return(true);
   } /* addUserDemoFilesDBbyURL */
  
  
  /**
   * dateStr() - return a new Date string of the current day and time
   * @return a new Date string of the current day and time
   */
  static String dateStr()
  { /* dateStr */
    Date dateObj= new Date();
    String date= dateObj.toString();
    
    return(date);
  } /* dateStr */
  
  
  /**
   * getCurDateStr() - return date string in the format of YYMMDDHHMMSS.
   * This date string is sortable by date.
   * Note: Adds "0" to first single digit if value is 1-9.
   */
  static String getCurDateStr()
  { /* getCurDateStr */  
    GregorianCalendar cal= new GregorianCalendar();  /* setup date */
    int
      yy= cal.get(Calendar.YEAR)-2000,    /* year in 2 digit format */
      mm= cal.get(Calendar.MONTH)+1,      /* Month, note: add 1 since 0-11*/
      dd= cal.get(Calendar.DAY_OF_MONTH), /* day */
      hh= cal.get(Calendar.HOUR_OF_DAY),  /* hour */
      min= cal.get(Calendar.MINUTE),      /* minute */
      ss= cal.get(Calendar.SECOND);       /* seconds */
    String
      sY,                                 /* Year  i.e., "03" */
      sM,                                 /* Month i.e., "12" */
      sD,                                 /* Day i.e., "30" */
      sH,                                 /* Hour i.e., "01", */
      sMin,                               /* minutes i.e., "40" */
      sS;                                 /* Seconds i.e., "60" */
    Integer i; /* for converting int to str */

    
    i= new Integer(yy);
    sY= "0"+ i.toString();
    
    if(mm<10)
    {
      i= new Integer(mm);
      sM= "0"+ i.toString();
    }
    else
    {
      i= new Integer(mm);
      sM= i.toString();  
    }
    if(dd<10)
    {
      i= new Integer(dd);
      sD= "0"+ i.toString();
    }
    else
    {
      i= new Integer(dd);
      sD= i.toString();  
    }
    if(hh<10)
    {
      i= new Integer(hh);
      sH= "0"+ i.toString();
    }
    else
    {
      i= new Integer(hh);
      sH= i.toString();  
    }
    if(min<10)
    {
      i= new Integer(min);
      sMin= "0"+ i.toString();
    }
    else
    {
      i= new Integer(min);
      sMin= i.toString();  
    }
    if(ss<10)
    {
      i= new Integer(ss);
      sS= "0"+ i.toString();
    }     
    else
    {
      i= new Integer(ss);
      sS= i.toString();  
    }
    
    String date = sY+sM+sD+sH+sMin+sS;
  
  return(date);
  } /* getCurDateStr */
  
  
  /**
   * prettyPrintDateStr() -  pretty-print date string.
   * @param ds is the date string in the format of YYMMDDHHMMSS.
   * @return pretty print string DD/MM/YY HH:MM:SS
   */
  static String prettyPrintDateStr(String ds)
  { /* prettyPrintDateStr */
    String
      sY= ds.substring(0,2),               /* Year   i.e.,  "03" */
      sM= ds.substring(2,4),               /* Month i.e., "12" */
      sD= ds.substring(4,6),               /* Day i.e., "30" */
      sH= ds.substring(6,8),               /* Hour i.e., "01", */
      sMin= ds.substring(8,10),            /* minutes i.e., "40" */
      sS= ds.substring(10,12),             /* Seconds i.e., "60" */
     ppDate = sM+"/"+sD+"/"+sY+" "+sH+":"+sMin+":"+sS;
    
    return(ppDate);
  } /* prettyPrintDateStr */
  
  
  /**
   * cvs2Array() - cvt arg list "1,4,3,6,..."  to "," - delim String[].
   * If there are more than maxExpected number of args in string, ignore
   * them and just return what has been parsed so far.
   * @param str string containt a list of Strings
   * @param maxExpected # of numbers to be parsed [DEPRICATED and ignored]
   * @param delimChr delimiter to be used with "," the default
   * @return array of Strings else null if problem
   */
  static String[] cvs2Array(String str, int maxExpected, 
                            String delimiterChr)
  { /* cvs2Array */
    if(str==null || delimiterChr==null)
      return(null);
    
    if(maxExpected<=0)
      maxExpected= Math.min(str.length(),1000); /* estimate */
    char
      delim= delimiterChr.charAt(0),
      searchArray[]= str.toCharArray();
    int
      delimCnt= 0,
      count= 0,
      strLen= str.length();
    
    while(count <strLen)
    { /* count delimChr chars */
      if(searchArray[count++]==delim)
        delimCnt++;
    }
    delimCnt++; /* need one more */
    String
      token,
      tokArray[]= new String[delimCnt]; /* return them all at once */
    char
      ch,
      lineBuf[]= str.toCharArray(),     /* cvt input string to char[]*/
      tokBuf[]= new char[1000];         /* token buffer */
    int
      bufSize= str.length(),            /* size of input buffer */
      bufCtr= 0;                        /* working input buffer index */
       
    /* Parse data from line buffer into tokens */
    if(maxExpected<delimCnt)
      delimCnt= maxExpected;            /* min(maxExpected,delimCnt) */
    
    for(int c=0; c<delimCnt; c++)
    { /* get and store next token*/
      int
        lastNonSpaceTokCtr= 0,          /* idx of last non-space char*/
        tokCtr= 0;                      /* size of tokBuf */
      
      while(bufCtr<bufSize && lineBuf[bufCtr]!= delim)
      { /* build token*/
        ch= lineBuf[bufCtr++];
        
        /* track total string len and last non-space char */
        tokBuf[tokCtr++]= ch;
        lastNonSpaceTokCtr= tokCtr;     /* saves doing trim */        
      } /* build token*/
      
      tokBuf[tokCtr]= '\0';             /* terminate token */
      token= new String(tokBuf,0,tokCtr); /* cvt char[] to string */
      
      /* get just string we want with no trailing whitespace */
      token= token.substring(0,lastNonSpaceTokCtr);
      
      tokArray[c]= token;               /* i.e. save token */
      
      if(bufCtr<bufSize && lineBuf[bufCtr]==delim)
        bufCtr++;		                    /* move past delimChr */
    } /* get and store field names*/
    
    return(tokArray);
  } /* cvs2Array */
  
  
  /**
   * cvs2f() - convert String to float with default value
   * @param str to convert
   * @param defaultValue if bad numeric string
   * @return numeric value
   */
  static float cvs2f(String str, float defaultValue)
  { /* cvs2f */
    float f;
    try
    {
      Float F= new Float(str);
      f= F.floatValue();
    }
    catch(NumberFormatException e)
    {f= defaultValue;}
    return(f);
  } /* cvs2f */
  
  
  /**
   * cvs2i() - convert String to int with default value
   * @param str to convert
   * @param defaultValue if bad numeric string
   * @return numeric value
   */
  static int cvs2i(String str, int defaultValue)
  { /* cvs2i */
    int i;
    try
    {
      i= java.lang.Integer.parseInt(str);
    }
    catch(NumberFormatException e)
    {i= defaultValue;}
    return(i);
  } /* cvs2i */
  
  
  /**
   * cvByteToInt() - convert signed byte [-128:+127] to unsigned int
   * in range [0:255]
   * @param b is byte to convert
   * @return 
   */
  final private int cvByteToInt(byte b)
  { /* cvByteToInt */ 
    int i= (int)(128+b); 
    return(i);
  } /* cvByteToInt */
  
  
  /**
   * cvByteToChar() - convert signed byte [-128:+127] to unsigned char
   * in range [0:255]
   * @param b is byte to convert
   * @return unsigned char
   */
  final private char cvByteToChar(byte b)
  { /* cvByteToChar */
    char c= (char)((b>=0) ? (int)b : (int)(128-b-1));
    return(c);
  } /* cvByteToChar */
  
  
  /**
   * cvIntToByte() - convert int in range [0:255] to signed byte
   * in range of [-128:+127].
   * @param i is integer to convert
   * @return usigned byte
   */
  final private byte cvIntToByte(int i)
  { /* cvIntToByte */
    byte b= (byte) (i-128);
    return(b);
  } /* cvIntToByte */
    
     
  /**
   * quickSort() - sort the int[] array.
   * Based on QuickSort method by James Gosling from Sun's SortDemo applet
   * @param a array of data to sort
   * @param lo0 lower bound of array
   * @param hi0 uppper bound of array
   */
  static void quickSort(int a[], int lo0, int hi0)
  { /* quickSort */
    int
      lo= lo0,
      hi= hi0,
      mid,
      t;
    
    if (hi0 > lo0)
    {  /* need to sort */
      mid= a[(lo0 + hi0)/2];
      while(lo <= hi)
      { /* check if swap within range */
        while((lo < hi0) && (a[lo] < mid) )
          ++lo;
        while((hi > lo0) && (a[hi] > mid) )
          --hi;
        if(lo <= hi)
        {
          t= a[lo];
          a[lo]= a[hi];
          a[hi]= t;
          ++lo;
          --hi;
        }
      } /* check if swap within range */
      
      if(lo0 < hi)
        quickSort(a, lo0, hi);
      if(lo < hi0)
        quickSort(a, lo, hi0);
    } /* need to sort */
  } /* quickSort */
    
  
  /**
   * bubbleSort() - Sort String array via bubble sort w/len
   * @param data array of data to be sorted
   * @param len size of subarray array of data to be sorted [0:len-1]
   * @return the new sorted string[] array
   */
  static String[] bubbleSort(String data[], int len)
  { /* bubbleSort */
    if(data==null || len==0)
      return(data);
    
    String
      dataUCJM1,
      dataUCJ,
      tempStr,
      dataUC[]= new String[len];
    int lenMinus1= len-1;
    
    /* convert temp array to upper case  */
    for (int i= 0; i < len; i++)
      dataUC[i]= data[i].toUpperCase();
    
    for(int i= 0; i < len; i++)
    {
      for(int j= lenMinus1; j > i; j--)
      {
        dataUCJM1= dataUC[j-1];
        dataUCJ= dataUC[j];
        
        if(dataUCJM1.compareTo(dataUCJ) > 0)
        { /* exchange */
          tempStr= data[j-1];  /* parallel sort the data */
          data[j-1]= data[j];
          data[j]= tempStr;
          
          dataUC[j-1]= dataUCJ;
          dataUC[j]= dataUCJM1;
        }
      }
    }
    
    return(data);
  } /* bubbleSort */ 
 
  
  /**
   * bubbleSortIndex() - sort copy of String[0:len-1] data with bubble sort,
   * return index[].
   * Do NOT actually sort the original data[].
   * @param data array of data to be sorted
   * @param len size of subarray array of data to be sorted [0:len-1]
   * @param ascending sort if true
   * @return the index[] of the sorted data
   */
  static int[] bubbleSortIndex(String data[], int len, boolean ascending)
  { /* bubbleSortIndex */
    if(data==null || len==0)
      return(null);
    
    String
      dataUCJM1,
      dataUCJ,
      tempStr,
      dataUC[]= new String[len];
    int
      oldJm1,
      index[]= new int[len],
      j,
      strCmp,
      lenMinus1= (len-1);
    
    /* convert temp array to upper case  */
    for (int i= 0; i < len; i++)
    {
      dataUC[i]= data[i].toUpperCase();
      index[i]= i;
    }
    
    /* Do the sort */
    for(int i= 0; i < len; i++)
    {
      for(j= lenMinus1; j > i; j--)
      {
        dataUCJM1= dataUC[j-1];
        dataUCJ= dataUC[j];
        strCmp= dataUCJM1.compareTo(dataUCJ);
        if((ascending && strCmp > 0) || (!ascending && strCmp < 0))
        { /* exchange */
          oldJm1= index[j-1];  /* parallel sort the index and dataUC */
          index[j-1]= index[j];
          index[j]= oldJm1;
          
          dataUC[j-1]= dataUCJ;
          dataUC[j]= dataUCJM1;
        }
      }
    }
    
    return(index);
  } /* bubbleSortIndex */

      
  /**
   * bubbleSortIndex() - sort copy of float[0:len-1] data with bubble sort,
   * return index[].
   * Do NOT actually sort the original data[].
   * @param data array of data to be sorted
   * @param len size of subarray array of data to be sorted [0:len-1]
   * @param ascending sort if true
   * @return the index[] of the sorted data
   */
  static int[] bubbleSortIndex(float data[], int len, boolean ascending)
  { /* bubbleSortIndex */
    if(data==null || len==0)
      return(null);
    
    float
      dataJM1,
      dataJ,
      temp,
      dataT[]= new float[len];
    int
      oldJm1,
      index[]= new int[len],
      j,
      lenMinus1= (len-1);
    
    /* copy data to local tmp array since will change the order */
    for (int i= 0; i < len; i++)
    {
      dataT[i]= data[i];
      index[i]= i;
    }
    
    /* Do the sort */
    for(int i= 0; i < len; i++)
    {
      for(j= lenMinus1; j > i; j--)
      {
        dataJM1= dataT[j-1];
        dataJ= dataT[j];
        if((ascending && dataJM1>dataJ) ||
           (!ascending && dataJM1<dataJ))
        { /* exchange */
          oldJm1= index[j-1];  /* parallel sort the index and dataUC */
          index[j-1]= index[j];
          index[j]= oldJm1;
          
          dataT[j-1]= dataJ;
          dataT[j]= dataJM1;
        }
      }
    }    
    
    return(index);
  } /* bubbleSortIndex */

    
  /**
   * getFileNameFromPath() - extract the filename from the path
   * @param path 
   * @return filename
   */
  public String getFileNameFromPath(String path)
  { /* getFileNameFromPath */  
    if(path==null)
      return(null);
    int idx= path.lastIndexOf(flk.fileSeparator);
    if(idx>=0)
      idx++;
    String sR= (idx==-1) ? path : path.substring(idx);
    return(sR);
  } /* getFileNameFromPath */   
  
  
  /**
   * rmvFileExtension() - remove ".tif", ".jpg", ".gif", ".ppx" 
   * from file name and ignore the case.
   * @param fileName 
   * @return filename without file extension 
   */
  public String rmvFileExtension(String fileName)
  { /* rmvFileExtension */ 
    String
      sR= fileName,
      fnl= fileName.toLowerCase();
    if(fnl.endsWith(".tif") || fnl.endsWith(".jpg") ||
       fnl.endsWith(".gif") || fnl.endsWith(".ppx"))
    {
      int lth= fileName.length();
      sR= fileName.substring(0,lth-4);
    }
     
    return(sR);
  } /* rmvFileExtension */  
  
   
  /**
   * rmvRtnChars() - remove return chars. Map '\r' or "\r\n" to '\n' chars.
   * @param String str to process
   * @return String with '\r' removed.
   */
  static String rmvRtnChars(String str)
  { /* rmvRtnChars */    
    if(str==null)
      return(null);
        
    int
      lthOut= 0,
      lthIn= str.length(),
      lastIdx= lthIn-1;
    char 
      ch,
      chLA,
      cOut[]= new char[lthIn],
      cIn[]= str.toCharArray();
    
    int i= 0;
    while(i<lthIn)
    { /* process all characters */
      ch= cIn[i++];
      if(ch!='\r')
        cOut[lthOut++]= ch;       /* Copy all other chars */
      else
      { /* if \r\n or \r<not \n> then map to \n */
        chLA= (i==lastIdx) ? 0 : cIn[i+1];
        if(chLA=='\n')
          i += 1;            /* Move past the \n in \r\n */  
        cOut[lthOut++]= '\n';
      }
    } /* process all characters */
    
    String sR= new String(cOut,0,lthOut);  /* make exact length */
    
    return(sR);
  } /* rmvRtnChars */ 
  
  
  /**
   * rmvSpecifiedChar() - remove specified chararcter
   * @param str to process
   * @param rmvChar to remove
   * @return String with all instances of the specified charater removed.
   */
  public static String rmvSpecifiedChar(String str, char rmvChar)
  { /* rmvSpecifiedChar */    
    if(str==null)
      return(null);   
    if(rmvChar=='\0')
      return(str);
        
    int
      lthOut= 0,
      lthIn= str.length(),
      lastIdx= lthIn-1;
    char 
      ch,
      cOut[]= new char[lthIn],
      cIn[]= str.toCharArray();
    
    int i= 0;
    while(i<lthIn)
    { /* process all characters */
      ch= cIn[i++];
      if(ch!=rmvChar)
        cOut[lthOut++]= ch;          /* Copy all other chars */
    } /* process all characters */
    
    String sR= new String(cOut,0,lthOut);  /* make exact length */
    
    return(sR);
  } /* rmvSpecifiedChar */    
  
   
  /**
   * useFileSeparatorChar() - use specified file separator chararcter
   * Eg. if it is '\' then switch '/' to '\',
   * Eg. if it is '/' then switch '\' to '/',
   * @param str to process
   * @param newSeparatorStr to use
   * @return String with all instances of the specified character changed.
   */
  static String useFileSeparatorChar(String str, String newSeparatorStr)
  { /* useFileSeparatorChar */    
    if(str==null)
      return(null);   
    if(newSeparatorStr==null)
      return(str);
    
    String sR= "";
    int
      oldSize= str.length(),
      oldSizeM1= oldSize-1,
      newSize= 0,
      iLA,
      j= 0;
    char
      ch, 
      chLA,
      newSeparatorChar= newSeparatorStr.charAt(0),
      lookForChar= (newSeparatorChar=='\\')
                      ? '/'
                      : ((newSeparatorChar=='/')
                          ? '\\'
                          : newSeparatorChar);
                     
    for(int i= 0; i<oldSize; i++)
    { /* process the string */
      iLA= i+1;
      ch= str.charAt(i);
      
      if(ch==lookForChar)
        ch= newSeparatorChar;
      
      /* Build new string */
      sR += ch;
      newSize++;
    } /* process the string */
    
    return(sR);
  } /* useFileSeparatorChar */   
    
  
  /**
   * timeStr() - return a new daytime HH:MM:SS string of the current time.
   * @return a new daytime HH:MM:SS string of the current time
   */
  static String timeStr()
  { /* timeStr */
    Calendar cal= Calendar.getInstance();
    int
      hrs= cal.get(Calendar.HOUR_OF_DAY),
      mins= cal.get(Calendar.MINUTE),
      secs= cal.get(Calendar.SECOND);
    String dayTime= hrs+":"+mins+":"+secs;
    
    return(dayTime);
  } /* timeStr */
  
    
  /**
   * printCurrentMemoryUsage() - print: %free, total memory usage and 
   * time of day to stdout. A sample value is
   *<PRE>
   *   **** Memory[ BCF:setBrCt() end]:
   *   28% free, tot=24.36Mb [9:30:0] ****
  *,/PRE>
   * @param msg to display with this memory snapshot
   * @see FileIO#logMsgln
   * @see #cvd2s
   * @see #timeStr
   */
  public static void printCurrentMemoryUsage(String msg)
  { /* printCurrentMemoryUsage */   
    Runtime rt= Runtime.getRuntime();
    long
      totMem= rt.totalMemory(),
      freeMem= rt.freeMemory();
    int pctFreeMem= (int)(100*freeMem/totMem);
    if(msg==null)
      msg="-";
    String 
      totalMem= cvf2s((totMem/1000000.0F),2),    
      memStr= "**** Memory["+msg+ "]:\n  "+pctFreeMem+
                   "% free, tot="+ totalMem+
                   "Mb ["+ timeStr() + "] ****";
    System.out.println(memStr);
  } /* printCurrentMemoryUsage */  
   
  
  /**
   * gcAndMemoryStats() - String garbage collect & print memory statistics.
   * Note that the free memory number is only an estimate. It may be
   * more accurate if you run the garbage collector before calling
   * 'freeMemory()'.  Note: freeMemory is not always accurate.
   * Print the accompanying message.
   */
  public synchronized void gcAndMemoryStats(String msg)
  { /* gcAndMemoryStats */
    Runtime rt= Runtime.getRuntime();
    long
      freeMem1= rt.freeMemory(),  /* memory BEFORE G.C. */
      totalMem1= rt.totalMemory();
    
    System.runFinalization();     /* release resources so they are GC'ed */
    System.gc();	                /* makes memory numbers more accurate*/
    long
      freeMem2= rt.freeMemory(),
      totalMem2= rt.totalMemory();
        
    if(flk.GC_MEMORY_DBUG || flk.dbugFlag)
    { /* Print the memory times */
      if(msg==null)
        msg= "";
      String sR;
      
      if(!flk.GC_MEMORY_DBUG)
        sR= "Mem [" + msg + "] Tot=" + totalMem2 +
             " Free=" + freeMem2 +
             " GC(T,F)=(" + (totalMem2 - totalMem1) + "," +
             (freeMem2 - freeMem1) +")";
      else
      { /* pretty print memory times */
          
        /* Instinctively, when programmers want to create the current date, 
         * they immediately look at the Date class. While intuitive, this is
         * usually the wrong choice. For storing date and time information, 
         * most programmers should use the Calendar class, and to format dates
         * and times they should use the java.text.DateFormat class. This is 
         * all described with task-oriented documentation and examples in 
         * Internationalization. */
        String[] ids = TimeZone.getAvailableIDs(-5 * 60 * 60 * 1000);
        SimpleTimeZone edt = new SimpleTimeZone(-5 * 60 * 60 * 1000, ids[0]);
        // set up rules for daylight savings time
        edt.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
        edt.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);

        // create a GregorianCalendar with the Pacific Daylight time zone
        // and the current date and time
        Calendar calendar = new GregorianCalendar(edt);
        Date trialTime = new Date();
        calendar.setTime(trialTime);
        
        System.out.println("ERA: " + calendar.get(Calendar.ERA));
        System.out.println("YEAR: " + calendar.get(Calendar.YEAR));
        System.out.println("MONTH: " + calendar.get(Calendar.MONTH));
        System.out.println("WEEK_OF_YEAR: " + calendar.get(Calendar.WEEK_OF_YEAR));
        System.out.println("WEEK_OF_MONTH: " + calendar.get(Calendar.WEEK_OF_MONTH));
        System.out.println("DATE: " + calendar.get(Calendar.DATE));
        System.out.println("DAY_OF_MONTH: " + calendar.get(Calendar.DAY_OF_MONTH));
        System.out.println("DAY_OF_YEAR: " + calendar.get(Calendar.DAY_OF_YEAR));
        System.out.println("DAY_OF_WEEK: " + calendar.get(Calendar.DAY_OF_WEEK));
        System.out.println("DAY_OF_WEEK_IN_MONTH: "
                          + calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));
        System.out.println("AM_PM: " + calendar.get(Calendar.AM_PM));
        System.out.println("HOUR: " + calendar.get(Calendar.HOUR));
        System.out.println("HOUR_OF_DAY: " + calendar.get(Calendar.HOUR_OF_DAY));
        System.out.println("MINUTE: " + calendar.get(Calendar.MINUTE));
        System.out.println("SECOND: " + calendar.get(Calendar.SECOND));
        System.out.println("MILLISECOND: " + calendar.get(Calendar.MILLISECOND));
        System.out.println("ZONE_OFFSET: "
                           + (calendar.get(Calendar.ZONE_OFFSET)/(60*60*1000)));
        System.out.println("DST_OFFSET: "
                           + (calendar.get(Calendar.DST_OFFSET)/(60*60*1000)));
 
        NumberFormat nf= NumberFormat.getInstance(Locale.US);
        DecimalFormat df= (DecimalFormat) nf;
        int time= calendar.get(Calendar.SECOND);
        sR= time+" Mem [" + msg +"] Tot=" + df.format((double)totalMem2) +
            " Free=" + df.format((double)freeMem2) +
            " GC(T,F)=(" + (totalMem2 - totalMem1) + "," +
           (freeMem2 - freeMem1) +")";  
        
        /*
        Date d = new Date();
        int time= d.getSeconds();//Create a java.util.GregorianCalendar object and use its setters and getters instead
        NumberFormat nf= NumberFormat.getInstance(Locale.US);
        DecimalFormat df= (DecimalFormat) nf;
      
        sR= time+" Mem [" + msg +"] Tot=" + df.format((double)totalMem2) +
            " Free=" + df.format((double)freeMem2) +
            " GC(T,F)=(" + (totalMem2 - totalMem1) + "," +
            (freeMem2 - freeMem1) +")";*/
      } /* pretty print memory times */
      System.out.println(sR);    
    } /* Print the memory times */

  } /* gcAndMemoryStats */
  
  
  /**
   * computeCircularMaskList() - create a list of (x,y) point offsets
   * for use later in convolution types of operations.
   * [TODO] code is not complete....
   * @param r  - radius of circle inside the region
   * @return region[0:2*r][0:2*r] (i.e., x,y) with 1 
   */
  public boolean[][] computeCircularMaskList(int r)
  { /* computeCircularMaskList */
    int
      n= 2*r+1,
      x1= 0,
      x2= 2*r,
      y1= 0,
      y2= 2*r;      
    boolean region[][]= new boolean[n][n]; /* 0 fill */
    for(int x=0;x<x2;x++)
      for(int y=0;y<y2;x++)
      {
        region[x][y]= true;  /* add run length code */
      }
    return(region);
  } /* computeCircularMaskList */
  
  
   /**
    * resizeLAXfileData() - resize Flicker.lax file data for Flicker.
    * This file is read by the InstallAnywhere runtime when you start
    * Flicker and is used to set the initial heap and stack process size.
    * The LAX file (e.g., C:/Program Files/Flicker/Flicker.lax)
    * specifies the startup sizes for both the heap and stack.
    * It determines the current size, asks the user for a new size
    * then creates a new file if it succeeds in the Q and A.
    * The memory size specified must be > 128Mbytes and less than the 
    * swap space. However, it is not clear how to get access to that 
    * value across all systems.
    * @return -1 if fail, else the new memory size
    */
   int resizeLAXfileData()
   { /* resizeLAXfileData */
     int
       memSize= -1;
     try
     { /* do file modifications */
       /* [1] Define directory for Flicker.lax path and other file and URL names. */       
       String
         line,
         fileSep= flk.fileSeparator,
         userDir= flk.userDir,
         localFLKlaxFile= userDir + "Flicker.lax",
         localFLKlaxFileBkup= userDir + "Flicker.lax.bkup",
         localFLKlaxFileTmp= userDir + "Flicker.lax.tmp",
         laxHeapSizeStr= null,
         laxStackSizeStr= null,
         laxHeapSizeName=  "lax.nl.java.option.java.heap.size.max=",
         laxStackSizeName= "lax.nl.java.option.native.stack.size.max=",
         laxStr;
       int
         laxHeapSize= 0,
         laxStackSize= 0,
         lthHeapName= laxHeapSizeName.length(),
         lthStackName= laxStackSizeName.length();       
       
       /* [2] Parse out the two sizes from laxStr */
       File f= new File(localFLKlaxFile);
       if(!f.canRead() || !f.exists())
         return(-1);
       BufferedReader rin= new BufferedReader(new FileReader(f));
       while(laxHeapSizeStr==null || laxStackSizeStr==null)
       { /* read-write loop */
         line= rin.readLine();
         if(line==null)
           break;
         if (line.startsWith(laxHeapSizeName))
           laxHeapSizeStr= line.substring(lthHeapName);
         else if (line.startsWith(laxStackSizeName))
           laxStackSizeStr= line.substring(lthStackName);
       } /* read-write loop */ 
       
       /* [2.1] cvt to ints */
       if(laxHeapSizeStr==null || laxStackSizeStr==null)
         return(-1);
       laxHeapSize= Util.cvs2i(laxHeapSizeStr,96000000);
       laxStackSize= Util.cvs2i(laxStackSizeStr,96000000);
       memSize= Math.max(laxHeapSize, laxStackSize);
       
       /* [3] Popup a dialog box that asks for a new memory size */ 
       Popup popup= flk.evMu.popup;
       String
         oldSizeStr= (""+(memSize/1000000)),
         sPrompt= "Enter new Flicker memory size (Mbytes)",
         laxSizeStr= flk.bGui.pdq.dialogQuery(sPrompt,oldSizeStr);
       if(laxSizeStr==null)
         return(-1);
       memSize= Util.cvs2i(laxSizeStr,memSize);
       if(memSize<30)
       {
         String
           msg= "You must have at least 30Mb of memory specified - aborted";
         popupAlertMsg(msg, flk.alertColor);
         return(-1);                
       }
       else if(memSize>1768)
       {
         String 
           msg= "You must have less than 1,768Mb of memory specified - aborted";
         popupAlertMsg(msg, flk.alertColor);
         return(-1);                
       }
       
       memSize *= 1000000;    /* specify it in the LAX file in BYTES not Mb */
       
      /* [4] Backup the old Flicker.lax  as Flicker.lax.bkup if it exists
       * (it won't if you are running from the debugger!).
       * But first, delete old backup if it exists.
       */
       deleteLocalFile(localFLKlaxFileBkup);
       copyFile(localFLKlaxFile, localFLKlaxFileBkup, null,0);
       
       /* [5] Then create a new Flicker.lax file */
       laxHeapSize= memSize;
       laxStackSize= memSize;
       /* copy the data from Flicker.lax.bkup to Flicker.lax */
       StringBuffer strBuffer= new StringBuffer(25000);  /* est. - optimize */
       f= new File(localFLKlaxFileBkup);
       if(!f.canRead() || !f.exists())
         return(-1);
       rin= new BufferedReader(new FileReader(f));
       
      /* The data of interest is of the following form:
       * "#  LAX.NL.JAVA.OPTION.JAVA.HEAP.SIZE.MAX
       *  #   -------------------------------------
       *
       *  lax.nl.java.option.java.heap.size.max=128000000
       *
       *
       *  #   LAX.NL.JAVA.OPTION.NATIVE.STACK.SIZE.MAX
       *  #   ----------------------------------------
       *
       *  lax.nl.java.option.native.stack.size.max=128000000
       *  "
       */
       while(true)
       { /* read-write loop */
         line= rin.readLine();
         if(line==null)
           break;
         if (line.startsWith(laxHeapSizeName))
           line= laxHeapSizeName+laxHeapSize;
         else if (line.startsWith(laxStackSizeName))
           line= laxStackSizeName+laxStackSize;
         strBuffer.append(line + "\n");
       } /* read-write loop */   
       
       String dataStr= new String(strBuffer); /* make string to write */
       if(!flk.fio.writeFileToDisk(localFLKlaxFile,dataStr))
       {         
         String msg= "Problem updating LAX startup file- aborted";
         popupAlertMsg(msg, flk.alertColor);
         return(-1);
       }       
       showMsg("Updated LAX startup file", Color.black);       
     } /* do file modifications */
     
     catch(Exception e1)
     { /* just fail if any problems at all! */        
       String msg= "Problem updating LAX startup file- aborted";
         popupAlertMsg(msg, flk.alertColor);
       return(-1);
     }
     
     return(memSize);   
  } /* resizeLAXfileData */ 

    
   
} /* End of Util */

