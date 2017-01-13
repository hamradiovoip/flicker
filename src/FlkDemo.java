/* File: FlkDemo.java */

import java.awt.*;
import java.awt.Color;
import java.awt.image.*;
import java.util.*;
import java.net.*;
import java.lang.*;
import java.io.*;

/**
 * FlkDemo contains methods to maintain the DB/FlkDemoDB.txt database
 * used by Flicker.
 * The file is a tab-delimited file with the following 7 fields:
 *        SubMenuName, SubMenuEntry, ClickableURL1, ImageURL1, 
 *        ClickableURL2,ImageURL2,StartupData
 *<PRE>
 * The map is used to build menus of images to load
 * a map entry contains the following fields:
 * 1. SubMenuName: is the menu image name: e.g.,
 *   "Human Plasma"
 * 2. SubMenuEntry: is the menu image name: e.g.,
 *   "(Swiss2DPAGE vs Merril) gels - clickable"
 * 3. ClickableURL1: is the clickable map URL for image 1: e.g.,
 *   "http://www.expasy.org/cgi-bin/map3/big/PLASMA_HUMAN?"
 * 4. ImageURL1: is the Image URL for image 1: e.g.
 *   "http://www.expasy.org/ch2dgifs/PLASMA_HUMAN/PLASMA_HUMAN_id.gif"
 * 5. ClickableURL2: is the clickable map URL for image 2: e.g.,
 *   ""
 * 6. ImageURL2: is the Image URL for image 2: e.g.
 *   "plasmaL.gif"
 * 7. StartupData: is TRUE if should be the default startup for Flicker.
 *   "TRUE"
 *
 * The database file DB/FlkDemoDB.txt contains data Fields:
 *        SubMenuName, SubMenuEntry, ClickableURL1, ImageURL1, 
 *        ClickableURL2,ImageURL2,StartupData

 * E.g. of a data entry:
 *        Human Plasma
 *        (Swiss2DPAGE vs Merril) gels - clickable
 *        http://www.expasy.org/cgi-bin/map3/PLASMA_HUMAN?
 *        PLASMA_HUMAN_id.gif	
 *        <null>
 *        plasmaL.gif
 *        FALSE
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

public class FlkDemo
{ /* FlkDemo */
  
  /** Instance of Flicker */
  private static Flicker
    flk;  
  /** Instance of Util */
  private static Util
    util;
  
  /* ----------- Static FlkDemo database --------------------- */
  
  /** Max # of demo images  */
  public final static int
    MAX_DEMOS= 400;
  
  /** MAX # of demo databases */
  public final static int
    MAX_DATABASES= 50;
  
  /** FlkDemo database [0:nMaps] */
  public static FlkDemo
    flkDemos[]= null;
  /** # of FlkDemos[] map entries */
  public static int 
    nMaps= 0;
  
  /** name of the database file */
  public static String
    FlkDemoDBfile;  
    
  /* ------------------- FlkDemo Instance Database ----------------- */
  /** is the menu image name: e.g.,
   *   "Human Plasma" 
  */
   String
     subMenuName;
   /** is the menu image name: e.g.,
    *   "(Swiss2DPAGE vs Merril) gels - clickable"
   */
   String
     subMenuEntry;
   /** ClickableURL1: is the clickable map URL for image 1: e.g.,
    *   "http://www.expasy.org/cgi-bin/map3/big/PLASMA_HUMAN?" 
   */
   String
     clickableURL1;
   /** ImageURL1: is the Image URL for image 1: e.g.
    *   "http://www.expasy.org/ch2dgifs/PLASMA_HUMAN/PLASMA_HUMAN_id.gif" 
   */
   String
     imageURL1;
   /** ClickableURL2: is the clickable map URL for image 2: e.g.,
    *   ""
   */
   String
     clickableURL2;
   /** imageURL2: is the Image URL for image 2: e.g.
    *   "plasmaL.gif"
   */
   String
     imageURL2;
   /** is TRUE if should be the default startup for Flicker.
    *   "TRUE" 
   */
   boolean
     startupDataFlag;
  
 
  /**
   * FlkDemo() - constructor for Class FlkDemo
   * @param flk instance of Flicker
   */
  public FlkDemo(Flicker flkS)
  { /* FlkDemo */
    flk= flkS;  
    util= flk.util;
   
    /* [1] FlkDemo database [0:nMaps] */    
    flkDemos= new FlkDemo[MAX_DEMOS];
    nMaps= 0;  
    
    /* [2] Read initial DB/FlkDemoDB.txt database */ 
    FlkDemoDBfile= flk.userDir + "DB" + flk.fileSeparator + "FlkDemoDB.txt";    
    
    boolean ok= read();  
    
    /* [3] Add any test images if needed */
    flkDemos[nMaps++]= new FlkDemo("Test images",
                                   "Affine triangle test A and B", 
                                   "", "Images/testA.gif",
                                   "", "Images/testB.gif",
                                   "FALSE");    
  } /* FlkDemo */
 
 
  /**
   * FlkDemo() - constructor to add a new FlkDemo entry
   * NOTE: to do add of an entry, use 'new FlkDemo()' method 
   * @param subMenuName
   * @param subMenuEntry
   * @param clickableURL1
   * @param imageURL1
   * @param clickableURL2
   * @param imageURL2 
   * @param startupData
   */
  public FlkDemo(String subMenuName, String subMenuEntry, 
                 String clickableURL1, String imageURL1,
                 String clickableURL2, String imageURL2,
                 String startupData)
  { /* FlkDemo */
    if(clickableURL1!=null && clickableURL1.indexOf("://")==-1)
      clickableURL1= flk.util.useFileSeparatorChar(clickableURL1,
                                                   flk.fileSeparator);
    if(imageURL1!=null && imageURL1.indexOf("://")==-1)
      imageURL1= flk.util.useFileSeparatorChar(imageURL1, 
                                               flk.fileSeparator);
    if(clickableURL2!=null && clickableURL2.indexOf("://")==-1)
      clickableURL2= flk.util.useFileSeparatorChar(clickableURL2, 
                                                   flk.fileSeparator);
    if(imageURL2!=null && imageURL2.indexOf("://")==-1)
      imageURL2= flk.util.useFileSeparatorChar(imageURL2,
                                               flk.fileSeparator);
    
    this.subMenuName= subMenuName; 
    this.subMenuEntry= subMenuEntry;
    this.imageURL1= imageURL1;
    this.imageURL2= imageURL2;
    int 
      lth1= (clickableURL1==null) ? 0 : clickableURL1.length(),
      lth2= (clickableURL2==null) ? 0 : clickableURL2.length();
    this.clickableURL1= (lth1>0) ? clickableURL1 : null;
    this.clickableURL2= (lth2>0) ? clickableURL2 : null;
    this.startupDataFlag= startupData.equalsIgnoreCase("TRUE");     
  } /* FlkDemo */
     
            
  /**
   * read() - read database from FlkDemoDB.txt file to flkDemos[0:nMaps-1].
   * Append the data to the FlkDemos list.
   * @return true if succeed
   */
  private static boolean read()
  { /* read */ 
    String
      flkDemoDBdata= flk.fio.readData(FlkDemoDBfile,
                                      "Reading Flicker Demo DB file");    
    if(flkDemoDBdata==null)
    { /* bad file */
      String msg= "Can't read DB/FlkDemoDB.txt database file";
      flk.util.showMsg2(msg, Color.red);
      util.popupAlertMsg(msg, flk.alertColor);
      return(false);
    }
     
    boolean ok= parseAndAddEntry(flkDemoDBdata);
        
    return(ok);
  } /* read */
  
  
  /**
   * parseAndAddEntry() - parse and add entry to global to flkDemos[0:nMaps-1]
   * if the data is well formed. Also check to see if the entry already
   * exists (SubMenuName and SubMenuEntry), in which case overide it.
   * @param flkDemoDBdata is a text file tab-delimited \n row terminated
   *        set of data for parsing into the FlkDemo[] database table.
   * @return true if succeed
   */
  public static boolean parseAndAddEntry(String flkDemoDBdata)
  { /* parseAndAddEntry */
    String
      rowdata,
      token[];
    
    /* Remove quotes */
    flkDemoDBdata= flk.util.rmvSpecifiedChar(flkDemoDBdata,'\"');
    
    /* Parse the flkDemoDBdata */
    StringTokenizer parser= new StringTokenizer(flkDemoDBdata, "\n", false);
    int nRow= -1;
    while(parser.hasMoreTokens())
    { /* parse file names into the flkDemos[0:nMaps-1] */
      nRow++;
      rowdata= parser.nextToken();
      if(rowdata!=null && rowdata.indexOf("\r")>-1)
        rowdata= rowdata.substring(0, rowdata.length()-1);
      if(rowdata==null || rowdata.length()==0)
        continue;
      /* Get the fields */
      token= flk.util.cvs2Array(rowdata, 7, "\t");
      if(nRow==0)
      { /* Save and Check field names */
        if(!"SubMenuName".equals(token[0]) ||
           !"SubMenuEntry".equals(token[1]) ||
           !"ClickableURL1".equals(token[2]) ||
           !"ImageURL1".equals(token[3]) ||
           !"ClickableURL2".equals(token[4]) ||
           !"ImageURL2".equals(token[5]) ||
           !"StartupData".equals(token[6]))
        { /* badfile */
          String msg= "Corrupted DB/FlkDemoDB.txt database file";
          flk.util.showMsg2(msg, Color.red);
          util.popupAlertMsg(msg, flk.alertColor);
          return(false);
        }
        continue;           /* do NOT save the header */
      } /* Save and Check field names */
      
      /* Create a new map 7-tuple entry into the DB */
      String
        subMenuName= token[0],
        subMenuEntry= token[1],
        clickableURL1= token[2],
        imageURL1= token[3],
        clickableURL2= token[4],
        imageURL2= token[5],
        startupData= token[6];
      
      FlkDemo fd= null;
      int i= lookupBySubMenuNameAndEntry(subMenuName, subMenuEntry);
      if(i!=-1)
      { /* overide an existing DB entry */
        fd= flkDemos[i];
        boolean startupDataFlag= startupData.equalsIgnoreCase("true");
        boolean ok= fd.set(subMenuName, subMenuEntry,
                           clickableURL1, imageURL1,
                           clickableURL2, imageURL2, startupDataFlag);
        if(!ok)
          fd= null;
      } /* overide an existing DB entry */
      else
      { /* Create a new DB instance */
        fd= new FlkDemo(subMenuName, subMenuEntry, clickableURL1, imageURL1,
                        clickableURL2, imageURL2, startupData);
      } /* Create a new DB instance */
      
      /* only add well formed entries to the DB */
      if(fd!=null)
        flkDemos[nMaps++]= fd;
    } /* Parse the prpDirStr into a list of flkDemos[0:nMaps-1] */
    
    return(true);
  } /* parseAndAddEntry */
  

  /**
   * write() - write database in memory to DB/FlkDemoDB.txt database file.
   * @return true if succeed
   */
  public static boolean write()
  { /* write */ 
    String
      header= "SubMenuName\tSubMenuEntry\tClickableURL1\tImageURL1\t"+
              "ClickableURL2\tImageURL2\tStartupData\n",
      sData= header,
      sEntry;
   FlkDemo
     fm;
    
    if(nMaps==0)
    { /* bad file */
      String msg= "Can't write DB/FlkDemoDB.txt DB file - do data";
      util.popupAlertMsg(msg, flk.alertColor);
      return(false);
    }
   
   for(int i=0;i<nMaps;i++)
    { /* Build the data to write */
      fm= flkDemos[i];
      sEntry= fm.subMenuName+"\t"+
              fm.subMenuEntry+"\t"+
              fm.clickableURL1+"\t"+
              fm.imageURL1+"\t"+
              fm.clickableURL2+"\t"+
              fm.imageURL2+"\t"+
              fm.startupDataFlag+"\n";
      sData += sEntry;
    }  /* Build the data to write */   
   
    /* Now write out the state file */
    boolean flag= flk.fio.writeFileToDisk(FlkDemoDBfile, sData);
        
    return(flag);
  } /* write */
      
        
  /**
   * lookup() - lookup entry to database flkDemos[0:nMaps-1].
   * @param fm is the instance to lookup
   * @return index of flkDemos[] if succeed, else -1
   */
  public static int lookup(FlkDemo fm)
  { /* lookup */  
    for(int i=0;i<nMaps;i++)
      if(fm==flkDemos[i])
      { /* found it */
        return(i);
      }
        
    return(-1); 
  } /* lookup */
      
        
  /**
   * lookupBySubmenu() - lookup entry by submenu name to flkDemos[0:nMaps-1].
   * @param subMenuEntry is the menu name of the instance to lookup
   * @return index of flkDemos[] if succeed, else -1
   */
  static int lookupBySubmenu(String subMenuEntry)
  { /* lookupBySubmenu */ 
    for(int i=0;i<nMaps;i++)
      if(subMenuEntry.equals(flkDemos[i].subMenuEntry))
      { /* found it */
        return(i);
      }
        
    return(-1);
  } /* lookupBySubmenu */
      
        
  /**
   * lookupBySubMenuNameAndEntry() - lookup entry by matching BOTH the
   * SubMenuName and SubMenuEntry in the flkDemos[0:nMaps-1]. database
   * @param subMenuName is the menu name of the instance to lookup
   * @param subMenuEntry is the menu name of the instance to lookup
   * @return index of flkDemos[] if succeed, else -1
   */
  static int lookupBySubMenuNameAndEntry(String subMenuName, 
                                         String subMenuEntry)
  { /* lookupBySubMenuNameAndEntry */ 
    for(int i=0;i<nMaps;i++)
      if(subMenuName.equals(flkDemos[i].subMenuName) &&
         subMenuEntry.equals(flkDemos[i].subMenuEntry))
      { /* found it */
        return(i);
      }
        
    return(-1);
  } /* lookupBySubMenuNameAndEntry */
      
        
  /**
   * lookupByFileName() - lookup entry by file name to flkDemos[0:nMaps-1].
   * @param fileName1 is the fileName name of the instance to lookup
   *       for imageURL1 (if not null)
   * @param fileName2 is the fileName name of the instance to lookup
   *       for imageURL2 (if not null)
   * @return index of flkDemos[] if succeed, else -1
   */
  public static int lookupByFileName(String fileName1, String fileName2)
  { /* lookupByFileName */ 
    for(int i=0;i<nMaps;i++)
      if(fileName1!=null && fileName1.equals(flkDemos[i].imageURL1))
      { /* found it */
        return(i);
      }
    else if(fileName2!=null && fileName2.equals(flkDemos[i].imageURL1))
      { /* found it */
        return(i);
      }
        
    return(-1);
  } /* lookupByFileName */
      
  
  /**
   * set() - set entry to database flkDemos[0:nMaps-1].
   * NOTE: to do add of an entry, use 'new FlkDemo()' method 
   * @param subMenuName
   * @param subMenuEntry
   * @param clickableURL1
   * @param imageURL1
   * @param clickableURL2
   * @param imageURL2 
   * @param startupData
   * @return true if succeed
   */
  public boolean set(String subMenuName, String subMenuEntry, 
                     String clickableURL1, String imageURL1,
                     String clickableURL2, String imageURL2,
                     boolean startupDataFlag)
  { /* set */ 
    if(nMaps>= MAX_DEMOS)
      return(false);
        
    this.subMenuName= subMenuName; 
    this.subMenuEntry= subMenuEntry;
    this.clickableURL1= (clickableURL1!=null && clickableURL1.length()>0)
                          ? clickableURL1 : null;
    this.imageURL1= imageURL1;
    this.clickableURL2= (clickableURL2!=null && clickableURL2.length()>0)
                          ? clickableURL2 : null;
    this.imageURL2= imageURL2;
    this.startupDataFlag= startupDataFlag; 
        
    return(true);
  } /* set */
  
  
  /**
   * delete() - rmv entry from database flkDemos[0:nMaps-1].
   * @return true if succeed
   */
  public boolean delete()
  { /* delete */  
    int n= lookup(this);
    if(n==-1)
      return(false);
    
    for(int i=n;i<(nMaps-1);n++)
       flkDemos[i]= flkDemos[i+1];
    flkDemos[nMaps-1]= null;
    nMaps--;  
        
    return(true);
  } /* delete */
      
  
  /**
   * clear() - clear database flkDemos[0:nMaps-1].
   */
  public static void clear()
  { /* clear */  
    for(int i=0;i<nMaps;i++)
      flkDemos[i]= null;
    nMaps= 0;    
  } /* clear */ 
      
  
 
} /* End of FlkDemo */

