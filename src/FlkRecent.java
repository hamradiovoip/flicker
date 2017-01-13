/* File: FlkRecent.java  */

import java.awt.*;
import java.awt.Color;
import java.awt.image.*;
import java.util.*;
import java.net.*;
import java.lang.*;
import java.io.*;

/**
 * FlkRecent contains methods to maintain the FlkRecentDB.txt database
 * used by Flicker. These are recently accessed image files. The file is
 * written after EACH new image is loaded.
 *<PRE>
 * The file is a tab-delimited file with the following fields:
 *        DbMenuName, ClickableURL, ImageURL, DatabaseName, TimeStamp
 *
 * The map is used to build menus of images to load
 * a map entry contains the following fields:
 * 1. DbMenuName: is the name of the database+menuName: e.g.,
 *   "SWISS-2DPROT Human Plasma"
 * 2. ClickableURL1: is the clickable map URL for image 1: e.g.,
 *   "http://www.expasy.org/cgi-bin/map3/big/PLASMA_HUMAN?"
 * 3. ClickableURL2: is the clickable map URL for image 2: e.g.,
 *   it may be null
 * 4. ImageURL1: is the Image URL for image 1: e.g.
 *   "http://www.expasy.org/ch2dgifs/PLASMA_HUMAN/PLASMA_HUMAN_id.gif"
 * 5. ImageURL2: is the Image URL for image 2: e.g.
 *   it may be null
 * 6. DatabaseName: is name of the database: e.g.,
 *   "SWISS-2DPROT Human"
 * 7. TimeStamp: is the time stamp when it was saved: e.g.
 *   "20030918-170230"
 *
 * The database file FlkRecentDB.txt contains data Fields:
 *        DbMenuName, ClickableURL, ImageURL, DatabaseName, TimeStamp
 * E.g. of a data entry:
 *        SWISS-2DPROT Human Plasma
 *        http://www.expasy.org/cgi-bin/map3/PLASMA_HUMAN?
 *        http://www.expasy.org/ch2dgifs/PLASMA_HUMAN/PLASMA_HUMAN_id.gif
 *        SWISS-2DPROT Human
 *        20030918-170230
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

public class FlkRecent
{ /* FlkRecent */
  
  /** Instance of Flicker */
  private static Flicker
    flk;
  /** Util instance */
  private static Util
    util;
  
  /** Max # of recent images */
  public final static int
    MAX_RECENTS= 10;
    
  /** FlkRecent database [0:nMaps] */
  public static FlkRecent
    flkRecents[]= null;
  public static int 
    nMaps= 0;
  
  /** name of the database file */
  public static String
    FlkRecentDBfile;
  
  /** flat that is set true if we edit the file (add/delete) since
   * we loaded it from the disk.
   */
  public static boolean
    flkRecentChangedFlag;
  
  /* ------------------- FlkRecent Instance Database ----------------- */
  /** is the name of the database+menuName: e.g.,
   *   "SWISS-2DPROT Human Plasma"
  */
   String
     dbMenuName;
   /** is the clickable map URL for image 1: e.g.,
    *   "http://www.expasy.org/cgi-bin/map3/big/PLASMA_HUMAN?"
   */
   String
     clickableURL1;
   /** is the clickable map URL for image 2: e.g.,
    *   "http://www.expasy.org/cgi-bin/map3/big/PLASMA_HUMAN?"
   */
   String
     clickableURL2;
   /** ImageURL: is the Image URL for image: e.g.
    *   "http://www.expasy.org/ch2dgifs/PLASMA_HUMAN/PLASMA_HUMAN_id.gif" 
   */
   String
     imageURL1;
   /** ImageURL: is the Image URL for image: e.g.
    *   "http://www.expasy.org/ch2dgifs/PLASMA_HUMAN/PLASMA_HUMAN_id.gif" 
   */
   String
     imageURL2;
   /** is name of the database: e.g.,
    *   "SWISS-2DPROT Human"
   */
   String
     databaseName;
   /**  is the time stamp when it was saved: e.g.
    *   "20030918-170230"
   */
   String
     timeStamp;
  
 
  /**
   * FlkRecent() - constructor for Class FlkRecent
   * @param flk instance of Flicker
   */
  public FlkRecent(Flicker flkS)
  { /* FlkRecent */
    flk= flkS;  
    util= flkS.util;
   
    /* FlkRecent database [0:nMaps] */    
    flkRecents= new FlkRecent[MAX_RECENTS];
    nMaps= 0; 
    flkRecentChangedFlag= false;
    
    FlkRecentDBfile= flk.userDir+"DB"+flk.fileSeparator+"FlkRecentDB.txt";
      
    read();         /* read initial FlkRecentDB.txt database */       
  } /* FlkRecent */
 
 
  /**
   * FlkRecent() - constructor to add a new FlkRecent entry
   * NOTE: to do add of an entry, use 'new FlkRecent()' method 
   * @param dbMenuName
   * @param clickableURL
   * @param imageURL
   * @param databaseName
   * @param timeStamp
   */
  public FlkRecent(String dbMenuName, String clickableURL, 
                   String imageURL, String databaseName,
                   String timeStamp)
  { /* FlkRecent */
    if(clickableURL!=null && clickableURL.indexOf("://")==-1)
      clickableURL= flk.util.useFileSeparatorChar(clickableURL,
                                                  flk.fileSeparator);
    if(imageURL!=null && imageURL.indexOf("://")==-1)
      imageURL= flk.util.useFileSeparatorChar(imageURL,
                                              flk.fileSeparator);
    
    this.dbMenuName= dbMenuName;  
    int lth= (clickableURL==null) ? 0 : clickableURL.length();
    this.clickableURL1= (lth>0) ? clickableURL : "";
    this.clickableURL2= "";
    this.imageURL1= imageURL;
    this.imageURL2= "";
    this.databaseName= databaseName;
    this.timeStamp= timeStamp; 
    
    flkRecents[nMaps++]= this;
  } /* FlkRecent */
       
 
  /**
   * FlkRecent() - constructor to add a new FlkRecent entry pair
   * NOTE: to do add of an entry, use 'new FlkRecent()' method 
   * @param dbMenuName
   * @param clickableURL1
   * @param clickableURL2
   * @param imageURL1
   * @param imageURL2
   * @param databaseName
   * @param timeStamp
   */
  public FlkRecent(String dbMenuName, String clickableURL1, 
                   String clickableURL2, String imageURL1, 
                   String imageURL2, String databaseName,
                   String timeStamp)
  { /* FlkRecent */
    if(clickableURL1!=null && clickableURL1.indexOf("://")==-1)
      clickableURL1= flk.util.useFileSeparatorChar(clickableURL1,
                                                   flk.fileSeparator);
    if(clickableURL2!=null && clickableURL2.indexOf("://")==-1)
      clickableURL2= flk.util.useFileSeparatorChar(clickableURL2,
                                                   flk.fileSeparator);
    if(imageURL1!=null && imageURL1.indexOf("://")==-1)
      imageURL1= flk.util.useFileSeparatorChar(imageURL1,
                                               flk.fileSeparator);
    if(imageURL2!=null && imageURL2.indexOf("://")==-1)
      imageURL2= flk.util.useFileSeparatorChar(imageURL2,
                                               flk.fileSeparator);
    
    this.dbMenuName= dbMenuName;  
    
    int lth1= (clickableURL1==null) ? 0 : clickableURL1.length();
    this.clickableURL1= (lth1>0) ? clickableURL1 : "";
    
    int lth2= (clickableURL1==null) ? 0 : clickableURL1.length();
    this.clickableURL2= (lth2>0) ? clickableURL2 : "";
    
    this.imageURL1= imageURL1;
    this.imageURL2= imageURL2;
    this.databaseName= databaseName;
    this.timeStamp= timeStamp; 
    
    flkRecents[nMaps++]= this;
  } /* FlkRecent */
      
  
  /**
   * read() - read database from FlkRecentDB.txt file to FlkRecents[0:nMaps-1].
   * @return true if succeed
   */
  static boolean read()
  { /* read */ 
    String
      rowdata,
      token[],
      FlkRecentDB= flk.fio.readData(FlkRecentDBfile,
                                    "Reading Flicker Demo DB file");
    FlkRecentDB= flk.util.rmvSpecifiedChar(FlkRecentDB,'\"');
    int nMaps= 0;
    
    if(FlkRecentDB==null)
    { /* bad file */
      String sErr= "Can't read DB/FlkRecentDB.txt database file";
      flk.util.showMsg1(sErr, Color.red);
      System.out.println(sErr);
      clear();          /* clear out the DB */
      return(false);
    }
    else
    { /* Parse the FlkRecentDB */
      StringTokenizer parser= new StringTokenizer(FlkRecentDB, "\n", false);
      nMaps= -1;
      while(parser.hasMoreTokens())
      { /* parse file names into the FlkRecents[0:nMaps-1] */
        rowdata= parser.nextToken();
        if(rowdata!=null && rowdata.indexOf("\r")>-1)
          rowdata= rowdata.substring(0, rowdata.length()-1);
        if(rowdata==null || rowdata.length()==0)
          continue;
        /* Get the fields */
        token= flk.util.cvs2Array(rowdata, 7, "\t");
        if(token!=null && token.length!=7)
        {
          String
            sErr= "Ignoring corrupted DB/FlkRecentDB.txt DB file length="+
                  token.length;
          System.out.println(sErr);
          flk.util.showMsg1(sErr, Color.red);
          clear();          /* clear out the DB */
          return(false);
        }
        if(nMaps==-1)
        { /* Save and Check field names */
          boolean
           ok0= "DbMenuName".equals(token[0]),
           ok1= "ClickableURL1".equals(token[1]),
           ok2= "ClickableURL2".equals(token[2]),
           ok3= "ImageURL1".equals(token[3]),
           ok4= "ImageURL2".equals(token[4]),
           ok5= "DatabaseName".equals(token[5]),
           ok6= "TimeStamp".equals(token[6]),
           ok= (ok0 && ok1 && ok2 && ok3 && ok4 && ok5 && ok6);
          if(!ok)
          { /* bad file */
            String sErr= "Corrupted DB/FlkRecentDB.txt database file fields [";
            if(!ok0)
              sErr += "DbMenuName ";
            if(!ok1)
              sErr += "ClickableURL1 ";
            if(!ok2)
              sErr += "ClickableURL2 ";
            if(!ok3)
              sErr += "ImageURL1 ";
            if(!ok4)
              sErr += "ImageURL2 ";
            if(!ok5)
              sErr += "DatabaseName ";
            if(!ok6)
              sErr += "TimeStamp ";
            sErr += "]";
            
            System.out.println(sErr);
            flk.util.showMsg1(sErr, Color.red);
            clear();          /* clear out the DB */
            return(false);
          }
          nMaps++;            /* set to 0 */
          continue;           /* do NOT save the header */
        } /* Save and Check field names */
        
        /* Create a new map 5-tuple entry into the DB */
        String
          dbMenuName= token[0],
          clickableURL1= token[1],
          clickableURL2= token[2],
          imageURL1= token[3],
          imageURL2= token[4],
          databaseName= token[5],
          timeStamp= token[6]; 
        
        if(imageURL1.equals(imageURL2))
        {
          String
            sErr= "Corrupted DB/FlkRecentDB.txt - left and right images have same name ["+
                  imageURL1+"]";
          System.out.println(sErr);
          flk.util.showMsg1(sErr, Color.red);
          clear();          /* clear out the DB */
          return(false);
        }
        
        flkRecents[nMaps++]= new FlkRecent(dbMenuName, clickableURL1, 
                                           clickableURL2, imageURL1, 
                                           imageURL2, databaseName, 
                                           timeStamp);
        if(nMaps>=MAX_RECENTS)
          return(true);                  /* ignore the rest of the file */
      } /* Parse the prpDirStr into a list of FlkRecents[0:nMaps-1] */
    } /* Parse the FlkRecentDB */
        
    return(true);
  } /* read */
      
  
  /**
   * write() - write database in memory to FlkRecentDB.txt database file.
   * @return true if succeed
   */
  static boolean write()
  { /* write */ 
    String
      header= "DbMenuName\tClickableURL1\tClickableURL2\t"+
              "ImageURL1\tImageURL2\tDatabaseName\tTimeStamp\n",
      sData= header,
      sEntry;
   FlkRecent
     fm;
    
    if(nMaps==0)
    { /* bad file */
      String msg= "Can't write FlkRecentDB.txt DB file -continuing";
      util.showMsg(msg, Color.red);
      return(false);
    }
   
   for(int i=0;i<nMaps;i++)
    { /* Build the data to write */
      fm= flkRecents[i];
      sEntry= fm.dbMenuName+"\t"+
              ((fm.clickableURL1==null) ? "" : fm.clickableURL1)+"\t"+
              ((fm.clickableURL2==null) ? "" : fm.clickableURL2)+"\t"+
              fm.imageURL1+"\t"+
              fm.imageURL2+"\t"+
              fm.databaseName+"\t"+
              fm.timeStamp+"\n";
      sData += sEntry;
    }  /* Build the data to write */   
   
    /* Now write out the state file */
    boolean flag= flk.fio.writeFileToDisk(FlkRecentDBfile, sData);
        
    return(flag);
  } /* write */
      
        
  /**
   * lookup() - lookup entry to database FlkRecents[0:nMaps-1].
   * @param fm is the instance to lookup
   * @return index of FlkRecents[] if succeed, else -1
   */
  static int lookup(FlkRecent fm)
  { /* lookup */  
    for(int i=0;i<nMaps;i++)
      if(fm==flkRecents[i])
      { /* found it */
        return(i);
      }
        
    return(-1); 
  } /* lookup */
      
        
  /**
   * lookupByDbMenuName() - lookup entry by dbMenuName in FlkRecents[0:nMaps-1].
   * @param dbMenuName is the menu name of the instance to lookup
   * @return matching instance of FlkRecents[] if succeed, else null
   */
  static FlkRecent lookupByDbMenuName(String dbMenuName)
  { /* lookupByDbMenuName */ 
    for(int i=0;i<nMaps;i++)
      if(dbMenuName.equals(flkRecents[i].dbMenuName))
      { /* found it */
        return(flkRecents[i]);
      }
        
    return(null);
  } /* lookupByDbMenuName */
      
        
  /**
   * lookupByImageURL() - lookup entry by dbMenuName in FlkRecents[0:nMaps-1].
   * @param imageURLname is the imageURL name of the instance to lookup
   * @return matching instance of FlkRecents[] if succeed, else null
   */
  static FlkRecent lookupByImageURL(String imageURLname)
  { /* lookupByImageURL */ 
    for(int i=0;i<nMaps;i++)
    {
      FlkRecent fk= flkRecents[i];
      if(imageURLname.equals(fk.imageURL1))
      { /* found it */
        if(fk.imageURL2.length()==0)
          return(flkRecents[i]);
      }
    }
        
    return(null);
  } /* lookupByImageURL */
      
        
  /**
   * lookupByImageURL() - lookup oair entry by dbMenuName in FlkRecents[0:nMaps-1].
   * @param imageURLname1 is the imageURL1 name of the instance to lookup
   * @param imageURLname2 is the imageURL2 name of the instance to lookup
   * @return matching instance of FlkRecents[] if succeed, else null
   */
  static FlkRecent lookupByImageURL(String imageURLname1, String imageURLname2)
  { /* lookupByImageURL */ 
    for(int i=0;i<nMaps;i++)
    {
      FlkRecent fk= flkRecents[i];
      if(imageURLname1.equals(fk.imageURL1) &&
         imageURLname2.equals(fk.imageURL2))
      { /* found it */
        return(flkRecents[i]);
      }
    }        
    return(null);
  } /* lookupByImageURL */
      
  
  /**
   * addOrUpdate() - add or Update an entry to database FlkRecents[0:nMaps-1].
   * If the imageURL is not in the recent database, then add it by doing a 
   * 'new FlkRecent()' method .
   * If it is found, then do a set.
   * @param dbMenuName
   * @param clickableURL may be null
   * @param imageURL  URL or local file path
   * @param databaseName
   * @param timeStamp
   * @return true if succeed
   */
  static FlkRecent addOrUpdate(String dbMenuName, String clickableURL, 
                               String imageURL, String databaseName,
                               String timeStamp)
  { /* addOrUpdate */     
    
    FlkRecent fr= lookupByImageURL(imageURL);
    if(nMaps>=MAX_RECENTS)
      deleteOldestEntry();
    if(fr==null)
      fr= new FlkRecent(dbMenuName, clickableURL, imageURL, databaseName,
                        timeStamp);
    else     
      fr.set(dbMenuName, clickableURL, imageURL, databaseName, timeStamp);        
     
    flkRecentChangedFlag= true; /* indicate that it should be saved on exit */
    
    if(nMaps>=MAX_RECENTS)
      deleteOldestEntry();
    return(fr);
  } /* addOrUpdate */ 
  
  
  /**
   * addOrUpdatePair() - add or Update an entry  pair to DB FlkRecents[0:nMaps-1].
   * If the imageURL is not in the recent database, then add it by doing a 
   * 'new FlkRecent()' method .
   * If it is found, then do a set.
   * @param dbMenuName
   * @param clickableURL may be null
   * @param imageURL  URL or local file path
   * @param databaseName
   * @param timeStamp
   * @return true if succeed
   */
  static FlkRecent addOrUpdatePair(String dbMenuName, 
                                   String clickableURL1, String clickableURL2, 
                                   String imageURL1, String imageURL2,
                                   String databaseName, String timeStamp)
  { /* addOrUpdate */     
    FlkRecent
      fr= lookupByImageURL(imageURL1, imageURL2);
    if(fr==null)
    {
      if(nMaps>=MAX_RECENTS)
        deleteOldestEntry();
      fr= new FlkRecent(dbMenuName, clickableURL1, clickableURL2,
                        imageURL1, imageURL2, databaseName, timeStamp);     
    }
    else     
    {
      if(nMaps>=MAX_RECENTS)
        deleteOldestEntry();
      fr.set(dbMenuName, clickableURL1, clickableURL2,
                        imageURL1, imageURL2, databaseName, timeStamp);              
    }
     
    flkRecentChangedFlag= true; /* indicate that it should be saved on exit */  
    return(fr);
  } /* addOrUpdate */  
      
  
  /**
   * set() - set entry to database FlkRecents[0:nMaps-1].
   * NOTE: to do add of an entry, use 'new FlkRecent()' method 
   * @param dbMenuName
   * @param clickableURL
   * @param imageURL
   * @param databaseName
   * @param timeStamp
   * @return true if succeed
   */
  boolean set(String dbMenuName, String clickableURL, 
              String imageURL, String databaseName,
              String timeStamp)
  { /* set */ 
    if(nMaps>= MAX_RECENTS)
      return(false);
        
    this.dbMenuName= dbMenuName; 
    this.clickableURL1= (clickableURL!=null && clickableURL.length()>0)
                          ? clickableURL : null;
    this.clickableURL2= null;
    this.imageURL1= imageURL;
    this.imageURL2= "";
    this.databaseName= databaseName;
    this.timeStamp= timeStamp; 
        
    flkRecentChangedFlag= true; /* indicate that it should be saved on exit */
    
    return(true);
  } /* set */  
      
  
  /**
   * set() - set entry pair to database FlkRecents[0:nMaps-1].
   * NOTE: to do add of an entry, use 'new FlkRecent()' method 
   * @param dbMenuName
   * @param clickableURL1
   * @param clickableURL2
   * @param imageURL1
   * @param imageURL2
   * @param databaseName
   * @param timeStamp
   * @return true if succeed
   */
  boolean set(String dbMenuName, String clickableURL1, String clickableURL2, 
              String imageURL1, String imageURL2, 
              String databaseName, String timeStamp)
  { /* set */ 
    if(nMaps>= MAX_RECENTS)
      return(false);
        
    this.dbMenuName= dbMenuName; 
    this.clickableURL1= (clickableURL1!=null && clickableURL1.length()>0)
                          ? clickableURL1 : null;
    this.clickableURL2= (clickableURL2!=null && clickableURL2.length()>0)
                          ? clickableURL2 : null;
    this.imageURL1= imageURL1;
    this.imageURL2= imageURL2;
    this.databaseName= databaseName;
    this.timeStamp= timeStamp; 
        
    flkRecentChangedFlag= true; /* indicate that it should be saved on exit */
    
    return(true);
  } /* set */
      
  
  /**
   * setByImageURL() - set entry to database FlkRecents[0:nMaps-1].
   * NOTE: to do add of an entry, use 'new FlkRecent()' method 
   * @param dbMenuName
   * @param clickableURL
   * @param imageURL
   * @param databaseName
   * @param timeStamp
   * @return true if succeed
   */
  static boolean setByImageURL(String dbMenuName, String clickableURL, 
                               String imageURL, String databaseName,
                               String timeStamp)
  { /* setByImageURL */ 
    FlkRecent fr= lookupByImageURL(imageURL); 
    if(fr==null)
      return(false);   /* not found */
        
    fr.dbMenuName= dbMenuName; 
    fr.clickableURL1= (clickableURL!=null && clickableURL.length()>0)
                          ? clickableURL : null;
                          
    fr.clickableURL2= null;
    fr.imageURL1= imageURL;
    fr.imageURL2= "";
    fr.databaseName= databaseName;
    fr.timeStamp= timeStamp; 
        
    flkRecentChangedFlag= true; /* indicate that it should be saved on exit */
    return(true);
  } /* setByImageURL */
  
  
  /**
   * deleteOldestEntry() - delete the oldest entry
   * @return true if succeed
   */
  static boolean deleteOldestEntry()
  { /* deleteOldestEntry */
    FlkRecent fOld= null;
    String 
      ts,
      tsOld= flk.util.getCurDateStr();
    
    int dateI=  Util.cvs2i(tsOld, 1);;
    
    /* find oldest entry */
    for(int i=0;i<nMaps;i++)
    { /* find oldest date */
       ts= flkRecents[i].timeStamp;
       
       if(tsOld.compareTo(ts)>0)
       { /* found older string */
         tsOld= ts;
         fOld= flkRecents[i];
       }
    } /* find oldest date */
    
    if(fOld!=null)
    { /* delete oldest entry */      
      fOld.delete();
      return(true);
    }
    else
      return(false);
  } /* deleteOldestEntry */
      
      
  /**
   * delete() - rmv entry from database FlkRecents[0:nMaps-1].
   * @return true if succeed
   */
  boolean delete()
  { /* delete */  
    int n= lookup(this);
    if(n==-1 || n>MAX_RECENTS+1)
      return(false);
    
    for(int i=n;i<(nMaps-1);i++)
       flkRecents[i]= flkRecents[i+1];
    flkRecents[nMaps-1]= null;
    nMaps--;  
        
    flkRecentChangedFlag= true; /* indicate that it should be saved on exit */
    return(true);
  } /* delete */
      
  
  /**
   * clear() - clear database FlkRecents[0:nMaps-1].
   */
  static void clear()
  { /* clear */  
    for(int i=0;i<nMaps;i++)
      flkRecents[i]= null;
    nMaps= 0;    
    flkRecentChangedFlag= true; /* indicate that it should be saved on exit */
  } /* clear */ 
          
   
} /* End of FlkRecent */

