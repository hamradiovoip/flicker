/* File: Popup.java */

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
import java.lang.*;
import java.io.*;

/**
 * Popup class supports various popup windows including:
 <PRE>
 *  1. popupFileDialog
 *  2. popupTextAreaViewer
 *  3. popup web browser.
 *</PRE>
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

public class Popup extends Frame
{ /* Popup */
  Flicker
    flk;
  Util
    util;
  			
  /** for use in popup FileDialog */
  public FileDialog 
    fileDialog;	
  
  
  /**
   * Popup() - constructor
   */
  public Popup(Flicker flk)
  { /* Popup */    
    this.flk= flk;
    this.util= flk.util;    
  } /* Popup */

  
  /**
   * popupFileDialog() - popup file dialog and get file name.
   * May be null if did CANCEL.
   * [TODO] add arg and ability to change the filename filter.
   * @param initialPath to start browsing
   * @param msg to display in prompt
   * @param fileLoadFlag if true, else do a SAVE
   * @return file name or null if did a cancel.
   */
  public String popupFileDialog(String initialPath, String sPrompt,
                                boolean fileLoadFlag)
  { /* popupFileDialog */
    Frame f= new Frame("Flicker Input File Name");
    int
      idx= (initialPath==null)
              ? -1
              : initialPath.lastIndexOf(flk.fileSeparator),
      fdType= (fileLoadFlag) ? FileDialog.LOAD : FileDialog.SAVE;
    String
      file,
      defDir= (idx==-1) ? flk.userDir : initialPath.substring(0,idx),
      defFile= (idx==-1) ? initialPath : initialPath.substring(idx+1),
      directory= "",
      fileName= "";			/* fileName to return */
    
    
    fileDialog= new FileDialog(f, sPrompt, fdType);
    
    if(fileDialog==null)
    {
      String msg= "Can't 'Open File' under Web browser";
      util.popupAlertMsg(msg, flk.alertColor);     
      return("");
    }
    else
    { /* run the modal popup file dialog box */
      if(initialPath!=null)
      {
        fileDialog.setDirectory(defDir);
        fileDialog.setFile(defFile);
      }
      
      fileDialog.pack();
      fileDialog.setVisible(true);
      fileName= fileDialog.getFile();         /* This is MODAL. */
      directory= fileDialog.getDirectory();
      
      if(fileName == null || directory == null)
        return(null);                         /* pressed cancel button */
      
      file= directory + fileName;
    }
    
    return(file);
  } /* popupFileDialog */
    
  
  /**
   * popupTextAreaViewer() - popup text area viewer
   * [TODO] needs work
   * @param sMsg to display
   * @param rows # of rows to display
   * @param cols # of columns to display
   */
  public void popupTextAreaViewer(String sMsg, int rows, int cols)
  { /* popupTextAreaViewer */
    String rStr= "";		/*  return string */
    Frame f= new Frame("Flicker Dialog");
    Panel p= new Panel();
    
    f.add(p);
    p.setLayout(new BorderLayout());
    
    if(rows==0)
      rows= 15;
    if(cols==0)
      cols= 60;
    TextArea ta= new TextArea(sMsg, rows, cols);
    p.add("North",ta);
    
    Button doneButton= new Button("Done");
    p.add("South",doneButton);
    
    f.pack();
    f.setVisible(true);
  } /* popupTextAreaViewer */  
  
  
  /**
   * popupViewer() - Display URL file in system Web browser.
   * This assumes that you have added the proper plugin to your browser
   * for this data if required.
   *<PRE>
   * Examples:
   * displayURL("http://www.javaworld.com")
   * displayURL("file://c:\\docs\\index.html")
   * displayURL("file:///user/joe/index.html");
   * displayURL("file:///user/joe/image.gif");
   * displayURL("file:///user/joe/image.pdf");
   * displayURL("file:///user/joe/image.xml");
   * displayURL("file:///user/joe/image.svg");
   *</PRE>
   * Under Unix, the system browser is hard-coded to be 'netscape'.
   * Netscape must be in your PATH for this to work.
   *<BR>
   * Under Windows, this will bring up the default browser under windows,
   * usually either Netscape or Microsoft IE. The default browser is
   * determined by the OS.
   *<BR>
   * adapted from :
   * http://www.javaworld.com/javaworld/javatips/jw-javatip66_p.html
   *<BR>
   * @param urlStr to display in popup browser. It is the file's url
   *            (the url must start with either "http://" or"file://").
   * @return false if error.
   */
  boolean popupViewer(String urlStr, String windowName)
  { /* popupViewer */    
    /* [TODO] Handle the Mac better. */
   
    String
      WIN_PATH= "rundll32",	          /* default browser under windows*/
      WIN_FLAG= "url.dll,FileProtocolHandler", /* flag to display url */
      UNIX_PATH= "netscape",	        /* default browser under UNIX */
      UNIX_FLAG= "-remote openURL";   /* flag to display a url.*/
    String cmd= null;
    
    try
    { /* try to start browser */
      if(flk.osName!=null && flk.osName.startsWith("Windows"))
      { /* windows */
        /* cmd= "rundll32 url.dll,FileProtocolHandler http://..." */
        cmd= WIN_PATH + " " + WIN_FLAG + " " + urlStr;
        Process p= Runtime.getRuntime().exec(cmd);
      } /* windows */
      
      /* Mac Code  [JE-06-6-2001]
       * Use the MRJ from Apple to envoke the default browser
       */
      else if(flk.osName.startsWith("Mac"))
      { /* Macintosh */
        try
        {
          Class C= Class.forName("com.apple.mrj.MRJFileUtils");
          C.getMethod("openURL",
                      new Class [] {Class.forName("java.lang.String")}
                     ).invoke((Object)null, new Object [] {urlStr});
        }
        catch(ClassNotFoundException e)
        {
          util.showMsg(
          "Can't start Web browser - missing 'com.apple.mrj.MRJFileUtils'",
                      Color.red);
          return(false);
        } /* Macintosh */
        catch(Exception e)
        { /* this catches following exceptions related to envoking MRJ
           * + NoSuchMethodException
           * + SecurityException
           * + IllegalAccessException
           * + IllegalArgumentExceptionb
           * + InvocationTargetException
           */
           util.showMsg("Can't start Mac Web browser, old version of MRJ "+e,
                       Color.red);
           return(false);
        }
      }
      
      else
      { /* UNIX */
        /* Under Unix, Netscape has to be running for the
         * "-remote" command to work. So, we try sending the
         * command and check for an exit value. If the exit
         * command is 0, it worked, otherwise we need to start
         * the browser.
         */
        
        /* cmd= "netscape -remote openURL(http://www.javaworld.com)" */
        cmd= UNIX_PATH + " " + UNIX_FLAG + "(" + urlStr + ")";
        Process p= Runtime.getRuntime().exec(cmd);
        
        try
        {
          /* wait for exit code -- if it's 0, command worked,
           * otherwise we need to start the browser up.
           */          
          if(p.waitFor()!=0)
          {
            /* Command failed, start up the browser*/
            /* cmd= "netscape http://www.javaworld.com" */
            cmd= UNIX_PATH + " " + urlStr;
            p= Runtime.getRuntime().exec(cmd);
          }
        }
        catch(InterruptedException x)
        {
          String msg= "Can't start browser";
        util.popupAlertMsg(msg, flk.alertColor);
          return(false);
        }
      } /* UNIX */
    } /* try to start browser */
    
    catch(IOException e)               /* couldn't exec browser*/
    {
      String msg= "Can't start browser";
        util.popupAlertMsg(msg, flk.alertColor);
      return(false);
    }
    
    return(true);
  } /* popupViewer */

  
  
} /* End of class Popup */
