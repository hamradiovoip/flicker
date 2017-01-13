/* File: FileIO.java */

import java.awt.*;
import java.util.zip.*;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;
import java.io.FileReader;
import java.lang.String;

/** Class FileIO is used to read file I/O from local disk or URL CGI
 * server.
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
 *
 *<P>
 * This work was derived from MAExplorer under the Mozilla 1.1 Open Source
 * Public License by Peter Lemkin of the National Cancer Institute, an
 * agency of the United States Government subject to the limitations noted
 * in the accompanying LEGAL file. See licence info on
 * http://maexplorer.sourceforge.org/
 */

public class FileIO
{ /* FileIO  */
  /** instance of Flicker */
  private Flicker 
    flk;  
  /** instance of Util */
  private Util
    util;
    
    
  /**
   * FileIO() - constructor
   * @param flk is instance of Flicker
   */
  FileIO(Flicker flk)
  { /* FileIO */
    this.flk= flk;
    util= flk.util;
  } /* FileIO */
  
  
  /**
   * readData() - read data from URL or file depending on prefix.
   * return null if fail.
   * @param fileName to read
   * @param msg to display while reading
   * @return null if failed.
   */
  public String readData(String fileName, String msg)
  { /* readData */
    String sR= null;
    if(fileName==null)
      return(null);
    
    if (fileName.startsWith("http://"))
    { /* Read from URL if it is a http address */
      flk.util.showMsg(msg, Color.black);
      sR= readFileFromUrl(fileName);
    }
    else
    { /* read from disk */
      flk.util.showMsg(msg, Color.black);
      sR= readFileFromDisk(fileName, true);
    }
    
    return(sR);
  } /* readData */
  
  
  /**
   * readData() - read data from URL or file depending on prefix.
   * return null if fail.
   * @param fileName to read
   * @param msg to display while reading
   * @param usePopupAlertsFlag if allow popup error messages
   * @return null if failed.
   */
  public String readData(String fileName, String msg,
                         boolean usePopupAlertsFlag)
  { /* readData */
    String sR= null;
    if(fileName==null)
      return(null);
    
    if (fileName.startsWith("http://"))
    { /* Read from URL if it is a http address */
      flk.util.showMsg(msg, Color.black);
      sR= readFileFromUrl(fileName);
    }
    else
    { /* read from disk */
      flk.util.showMsg(msg, Color.black);
      sR= readFileFromDisk(fileName,usePopupAlertsFlag);
    }
    
    return(sR);
  } /* readData */
  
  
  /**
   * readFileFromDisk() - Will read file from disk & returns as String
   *  Allow popup error messages.
   * @param fileName to read
   * @return null if failed.
   */
  public String readFileFromDisk(String fileName)
  { /* readFileFromDisk */
  return( readFileFromDisk(fileName,true) );
  } /* readFileFromDisk */
    
    
  /**
   * readFileFromDisk() - Will read file from disk & returns as String
   * @param fileName to read
   * @param usePopupAlertsFlag if allow popup error messages
   * @return null if failed.
   */
  public String readFileFromDisk(String fileName, boolean usePopupAlertsFlag)
  { /* readFileFromDisk */
    String sR;
    File f;
    RandomAccessFile rin= null;
    byte dataB[]= null;
    int size;
    
    /* Otherwise, just read it as unpacked file */
    try
    {
      f= new File(fileName);
      if(!f.canRead())
      {
        String msg= "FIO-RFFD Can't read file ["+fileName+"]";
        if(usePopupAlertsFlag)
          util.popupAlertMsg(msg, flk.alertColor);
        else
          System.out.println(msg);
        return(null);
      }
      
      if(!f.exists())
      {
        String msg= "FIO-RFFD File not found ["+fileName+"]";
        if(usePopupAlertsFlag)
          util.popupAlertMsg(msg, flk.alertColor);
        else
          System.out.println(msg);
        return(null);
      }
      
      rin= new RandomAccessFile(f,"r");      
      size= (int) f.length();      
      dataB= new byte[size];     /* make char array exact size needed! */      
      rin.readFully(dataB);
      
      rin.close();               /* done reading */
      f=null;
      System.runFinalization();
      System.gc();
      sR= new String(dataB);     /* convert String from char[]*/
      
      dataB= null;
      System.runFinalization();
      System.gc();
      
      return(sR);
    }
    
    catch (SecurityException e)
    {
      String msg= "FIO-RFFD securityException ["+fileName+"] "+e;
      if(usePopupAlertsFlag)
        util.popupAlertMsg(msg, flk.alertColor);
      else
        System.out.println(msg);
    }
    
    catch (FileNotFoundException e)
    {
      String msg= "FIO-RFFD FileNotFoundException ["+fileName+"] "+e;
      if(usePopupAlertsFlag)
        util.popupAlertMsg(msg, flk.alertColor);
      else
        System.out.println(msg);
    }
    catch (IOException e)
    {
      String msg= "FIO-RFFD IOException ["+fileName+"] "+e;
      if(usePopupAlertsFlag)
        util.popupAlertMsg(msg, flk.alertColor);
      else
        System.out.println(msg);
    }
    
    return(null);                             /* error */
  } /* readFileFromDisk */
    
  
  /**
   * readFileFromUrl() - read data from http URL, using JavaCGIBridge
   * If the code has "\r\n", then map them to "\n".
   * If the code has "\r" without "\n", then map them to "\n"
   * @param URLaddress to read web page as string from server
   * @returns string if successful, else null if failed.
   */
  public String readFileFromUrl(String URLaddress)
  { /* readFileFromUrl */
    byte uBuf[]= util.readBytesFromURL(URLaddress, null);
    if(uBuf==null)  
      return(null);
    
    /* Read the data as byte[] and convert to string */
    String 
      sR= "",
      s= new String(uBuf);
    int idxNULL= s.indexOf('\0');
    if(idxNULL!=-1)
      s= s.substring(0,idxNULL);
    int 
      idxLF= s.indexOf('\r'),
      idxCR= s.indexOf('\n'),          
      idx= (idxLF!=-1 && idxCR!=-1)
              ? Math.min(idxLF,idxCR)
              : Math.max(idxLF,idxCR);   
              
    /* If no \r, then just return */
    if(idxCR==-1)
    {
      sR= s;
    }
              
    /* If \r\n or \r then map all instances to \n */
    if(idxCR!=-1)
    { /* map all \r\n or \r instances to \n  */    
      String tok;    
      StringTokenizer parser= new StringTokenizer(s, "\r\n", true);
      boolean
        foundCR= false,
        foundNL= false;
      while(parser.hasMoreTokens())
      { /* look for and remove \r */
        tok= parser.nextToken();
        if(tok==null || tok.length()==0)
        { /* end of the input buffer */
          if(foundNL)
            sR += "\n";
          break;
        }
        else
        {
          if(tok.equals("\r"))
            foundCR= true;
          else if(tok.equals("\n"))
          {
            foundNL= true;
          }
          else 
          {
            if(foundCR || foundNL)
              sR += "\n";
            foundCR= false;
            foundNL= false;
            sR += tok;
          }
        }
      } /* map all \r\n or \r instances to \n  */    
    } /* map all \r\n or \r instances to \n  */ 
              
    return(sR);
  } /* readFileFromUrl */

  
  /**
   * writeFileToDisk() - write string data to a local disk file.
   * @param fileName is the full path filename to write the data
   * @param data is the string to write to the file.
   * @return string data for entire file if succeed, else null if fail.
   */
  boolean writeFileToDisk(String fileName, String data)
  { /* writeFileToDisk */
    String sR;
    File f;
    FileWriter out= null;
    char dataBuf[];
    int size= data.length();
    
    try
    { /* try to write it */
      f= new File(fileName);
      out= new FileWriter(f);
      if(!f.canWrite())
        return(false);
            
      dataBuf= data.toCharArray();   
      
      out.write(dataBuf, 0, size);
      out.close();                       /* done writing */
      return(true);
    }
    catch (Exception e)
    {
      return(false);
    }
  } /* writeFileToDisk */  
  
  
  /**
   * getFilesWithLegalExtnInDir() - get list of files in directory with
   * one of the specified file extensions
   * @param dir directory to read
   * @param legalExtList is the file extensions list 
   *       (e.g. {".tif", ".jpg", ...}) to match if not null, 
   *       else accept all files
   * @return list of files
   */
  public String[] getFilesWithLegalExtnInDir(String dir, String legalExtList[])
  { /* getFilesWithLegalExtnInDir */    
    if(dir==null)
      return(null);
    
    if(legalExtList==null)
    { /* treat it as if it were a single null file extension */
      return(getFilesInDir(dir, (String)null));
    }
    
    String dirList[]= null;  
    int nExtList= legalExtList.length;
    
    try
    { /* lookup the files in the directory */
      File f= new File(dir);
      if(f.isDirectory())
      {
        dirList= f.list();
        if(dirList==null || dirList.length==0)
          return(null);
        int
          nAll= dirList.length,
          nExt= 0;                    /* # found */
        String tmp[]= new String[nAll];
        for(int i=0;i<nAll;i++)
        { /* check all files in the list */
          for(int k=0;k<nExtList;k++)
            if(dirList[i].endsWith(legalExtList[k]))
            { /* make sure extension matches */
              tmp[nExt++]= dirList[i];
              break;
            }
        } /* check all files in the list */
        if(nExt==0)
          return(null);
        dirList= new String[nExt];   /* shrink it */
        for(int j=0;j<nExt;j++)
          dirList[j]= tmp[j];
      }
    } /* lookup the files in the directory */
    
    catch (Exception ef)
    {
      return(null);
    }

    return(dirList);
  } /* getFilesWithLegalExtnInDir */
  
  
  /**
   * getFilesInDir() - get list of files in directory the specified
   * file extension
   * @param dir directory to read
   * @param ext is the file extension (e.g. ".tif") to match if not null,
   *       else accept all files
   * @return list of files
   */
  public String[] getFilesInDir(String dir, String ext)
  { /* getFilesInDir */    
    if(dir==null)
      return(null);
    
    String dirList[]= null;     
    try
    { /* lookup the files in the directory */
      File f= new File(dir);
      if(f.isDirectory())
      {
        dirList= f.list();
        if(dirList==null || dirList.length==0)
          return(null);
        if(ext==null)
          return(dirList);
        int
          nAll= dirList.length,
          nExt= 0;
        String tmp[]= new String[nAll];
        for(int i=0;i<nAll;i++)
          if(dirList[i].endsWith(ext))
            tmp[nExt++]= dirList[i];
        if(nExt==0)
          return(null);
        dirList= new String[nExt];   /* shrink it */
        for(int j=0;j<nExt;j++)
          dirList[j]= tmp[j];
      }
    } /* lookup the files in the directory */
    
    catch (Exception ef)
    {
      return(null);
    }

    return(dirList);
  } /* getFilesInDir */
  
  
} /* end of class FileIO  */
