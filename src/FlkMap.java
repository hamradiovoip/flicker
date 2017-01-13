/* File: FlkMap.java  */

import java.awt.*;
import java.awt.Color;
import java.awt.image.*;
import java.util.*;
import java.net.*;
import java.lang.*;
import java.io.*;

/**
 * FlkMap contains methods to maintain the DB/FlkMapDB.txt database
 * used by Flicker.
 * The file is a tab-delimited file with the following 5 fields:
 *        MenuName, CickableURL, ImageURL, BaseURL, DatabaseName
 *<PRE>
 * The map is used to build menus of images to load
 * a map entry contains the following fields:
 * 1. MenuName: is the menu image name: e.g.,
 *   "Expasy Plasma (3-7)"
 * 2. CickableURL: is the clickable map URL added to the BaseURL: e.g.,
 *   "http://www.expasy.org/cgi-bin/map3/big/PLASMA_HUMAN?"
 * 3. ImageURL: is the Image URL added to the BaseURL: e.g.
 *   "http://www.expasy.org/ch2dgifs/PLASMA_HUMAN/PLASMA_HUMAN_id.gif"
 * 4. BaseURL: of the database web site URL: e.g.,
 *   "www.expasy.org"
 * 5. DatabaseName: is the Web database name (used to create a sub-menu):
 *   "Swiss-2DPAGE"
 *
 * The database file DB/FlkMapDB.txt contains data Fields:
 *        MenuName, ClickableURL, ImageURL, BaseURL, DatabaseName
 * E.g. of a data entry:
 *        Human Liver
 *        cgi-bin/map3/big/LIVER_HUMAN?
 *        ch2dgifs/LIVER_HUMAN/LIVER_HUMAN_id
 *        http://www.expasy.org/
 *        SWISS-2DPROT
 *
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

public class FlkMap
{ /* FlkMap */
  
  /** Instance of Flicker */
  private static Flicker
    flk;
  
  /** Max # of clickable image maps on the Web */
  public final static int
    MAX_MAPS= 400;
  /** Maximum number of Web clickable databases */
  public final static int
    MAX_DATABASES= 30;
  
  /** FlkMap database [0:nMaps] */
  public static FlkMap
    flkMaps[]= null;
  public static int 
    nMaps= 0;
  
  /** name of the database file */
  public static String
    flkMapDBfile;
  
  /* ------------------- FlkMap Instance Database ----------------- */
  /** file field MenuName: is the menu image name: e.g.,
   *   "Expasy Plasma (3-7)" 
  */
   String
     menuName;
   /** file field ClickableURL: is the clickable map URL added to
    * the BaseURL: e.g.,
    *   "cgi-bin/map3/big/PLASMA_HUMAN?"
   */
   String
     clickableURL;
   /** file field ImageURL: is the Image URL added to the BaseURL: e.g.
    *   "ch2dgifs/PLASMA_HUMAN/PLASMA_HUMAN_id.gif"
   */
   String
     imageURL;
   /** file field BaseURL: of the database web site URL: e.g., 
    *   "http://www.expasy.org/"
   */
   String
     baseURL;
   /** file field DatabaseName: is the Web database name (used to
    * create a sub-menu):
    *   "Swiss-2DPAGE"
   */
   String
     databaseName;
  
 
  /**
   * FlkMap() - constructor for Class FlkMap
   * @param flk instance of Flicker
   */
  public FlkMap(Flicker flkS)
  { /* FlkMap */
    flk= flkS;  
   
    /* FlkMap database [0:nMaps] */    
    flkMaps= new FlkMap[MAX_MAPS];
    nMaps= 0;  
    
    flkMapDBfile= flk.userDir + "DB"+flk.fileSeparator+"FlkMapDB.txt";
      
    read();                  /* read initial DB/FlkMapDB.txt database */       
  } /* FlkMap */
 
 
  /**
   * FlkMap() - constructor to add a new FlkMap entry
   * NOTE: to do add of an entry, use 'new FlkMap()' method 
   * @param MenuName
   * @param ClickableURL
   * @param ImageURL
   * @param BaseURL
   * @param DatabaseName
   */
  public FlkMap(String menuName, String clickableURL, String imageURL,
                String baseURL, String databaseName)
  { /* FlkMap */
    this.menuName= menuName; 
    int lth= (clickableURL==null) ? 0 : clickableURL.length();
    this.clickableURL= (lth>0) ? clickableURL : null;
    this.imageURL= imageURL;
    this.baseURL= baseURL;
    this.databaseName= databaseName; 
    
    flkMaps[nMaps++]= this;
  } /* FlkMap */
      
  
  /**
   * read() - read database from DB/FlkMapDB.txt file to flkMaps[0:nMaps-1].
   * @return true if succeed
   */
  static boolean read()
  { /* read */ 
    String
      rowdata,
      token[],
      flkMapDB= flk.fio.readData(flkMapDBfile,
                                 "Reading Flicker Map DB file");
    flkMapDB= flk.util.rmvSpecifiedChar(flkMapDB,'\"');
    int nMaps= 0;
    
    if(flkMapDB==null)
    { /* bad file */
      flk.util.showMsg2("Can't read DB/FlkMapDB.txt database file",
                        Color.red);
      return(false);
    }
    else
    { /* Parse the flkMapDB */
      StringTokenizer parser= new StringTokenizer(flkMapDB, "\n", false);
      nMaps= -1;
      while(parser.hasMoreTokens())
      { /* parse file names into the flkMaps[0:nMaps-1] */
        rowdata= parser.nextToken();
        if(rowdata!=null && rowdata.indexOf("\r")>-1)
          rowdata= rowdata.substring(0, rowdata.length()-1);
        if(rowdata==null || rowdata.length()==0)
          continue;
        /* Get the fields */
        token= flk.util.cvs2Array(rowdata, 5, "\t");
        if(nMaps==-1)
        { /* Save and Check field names */
          if(!"MenuName".equals(token[0]) || 
             !"ClickableURL".equals(token[1]) ||
             !"ImageURL".equals(token[2]) ||
             !"BaseURL".equals(token[3]) ||
             !"DatabaseName".equals(token[4]))
          { /* bad file */
            flk.util.showMsg2("Corrupted FlkMapDB.txt database file",
                              Color.red);
            return(false);
          }
          nMaps++;            /* set to 0 */
          continue;           /* do NOT save the header */
        } /* Save and Check field names */
        
        /* Create a new map 5-tuple entry into the DB */
        String
          menuName= token[0],
          clickableURL= token[1],
          imageURL= token[2],
          baseURL= token[3],
          databaseName= token[4];
        flkMaps[nMaps++]= new FlkMap(menuName, clickableURL, imageURL,
                                     baseURL, databaseName);
      } /* Parse the prpDirStr into a list of flkMaps[0:nMaps-1] */
    } /* Parse the flkMapDB */
        
    return(true);
  } /* read */
      
  
  /**
   * write() - write database in memory to DB/FlkMapDB.txt database file.
   * @return true if succeed
   */
  static boolean write()
  { /* write */ 
    String
      header= "MenuName\tClickableURL\tImageURL\tBaseURL\tDatabaseName\n",
      sData= header,
      sEntry;
   FlkMap
     fm;
    
    if(nMaps==0)
    { /* bad file */
      flk.util.showMsg2("Can't write DB/FlkMapDB.txt DB file - continuing",
                        Color.red);
      return(false);
    }
   
   for(int i=0;i<nMaps;i++)
    { /* Build the data to write */
      fm= flkMaps[i];
      sEntry= fm.menuName+"\t"+
              fm.clickableURL+"\t"+
              fm.imageURL+"\t"+
              fm.baseURL+"\t"+
              fm.databaseName+"\n";
      sData += sEntry;
    }  /* Build the data to write */   
   
    /* Now write out the state file */
    boolean flag= flk.fio.writeFileToDisk(flkMapDBfile, sData);
        
    return(flag);
  } /* write */
      
        
  /**
   * lookup() - lookup entry to database flkMaps[0:nMaps-1].
   * @param fm is the instance to lookup
   * @return index of flkMaps[] if succeed, else -1
   */
  static int lookup(FlkMap fm)
  { /* lookup */  
    for(int i=0;i<nMaps;i++)
      if(fm==flkMaps[i])
      { /* found it */
        return(i);
      }
        
    return(-1); 
  } /* lookup */
      
        
  /**
   * lookupByMenuName() - lookup entry by menu name in flkMaps[0:nMaps-1].
   * @param menuName is the menu name of the instance to lookup
   * @return index of flkMaps[] if succeed, else -1
   */
  static int lookupByMenuName(String menuName)
  { /* lookupByMenuName */ 
    for(int i=0;i<nMaps;i++)
      if(menuName.equals(flkMaps[i].menuName))
      { /* found it */
        return(i);
      }
        
    return(-1);
  } /* lookupByMenuName */
      
  
  /**
   * set() - set entry to database flkMaps[0:nMaps-1].
   * NOTE: to do add of an entry, use 'new FlkMap()' method 
   * @return true if succeed
   */
  boolean set(String menuName, String clickableURL, String imageURL, 
            String baseURL, String databaseName)
  { /* set */ 
    if(nMaps>= MAX_MAPS)
      return(false);
    
    this.menuName= menuName; 
    this.clickableURL= (clickableURL!=null && clickableURL.length()>0)
                          ? clickableURL : null;
    this.imageURL= imageURL;
    this.baseURL= baseURL;
    this.databaseName= databaseName; 
        
    return(true);
  } /* set */
  
  
  /**
   * delete() - rmv entry from database flkMaps[0:nMaps-1].
   * @return true if succeed
   */
  boolean delete()
  { /* delete */  
    int n= lookup(this);
    if(n==-1)
      return(false);
    
    for(int i=n;i<(nMaps-1);n++)
      flkMaps[i]= flkMaps[i+1];
    flkMaps[nMaps-1]= null;
    nMaps--;  
        
    return(true);
  } /* delete */
      
  
  /**
   * clear() - clear database flkMaps[0:nMaps-1].
   */
  static void clear()
  { /* clear */  
    for(int i=0;i<nMaps;i++)
      flkMaps[i]= null;
    nMaps= 0;    
  } /* clear */ 
          
   
} /* End of FlkMap */

