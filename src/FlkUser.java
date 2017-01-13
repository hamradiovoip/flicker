/* File: FlkUser.java */

import java.awt.*;
import java.awt.Color;
import java.awt.image.*;
import java.util.*;
import java.net.*;
import java.lang.*;
import java.io.*;

/**
 * FlkUser contains methods to maintain the internal FlkUserDB 
 * database created by analyzing the user's image directories they have 
 * placed in the Images/ folder. This database is used by Flicker
 * to create entries in the (File | Open user images| ...) menus.
 * This allows users to add many of their gel images without editing the 
 * FlkDemoDB.txt file. By placing your data in the Images/ directory, 
 * located in the installation directory, Flicker will discover it 
 * when it starts and add it to the demo menu. 
 * It works as follows:
 *<PRE>
 *  1. You copy or move one or more of your directories of with images you 
 *     want to use with Flicker in the Images/ folder.
 *  2. When Flicker starts, it creates additional submenu entries in the
 *    (File | Open user images | ...) menu that are the names of the user's
 *    directories.
 *  3. Within each submenu, it will generate all unique combinations of 
 *     the image files within the corresponding directory and denote them
 *     as for example, "image3 vs. image5", etc.
 *  4. Then just access them from the user menu as you would with the 
 *     built-in pairs of images in (File | Open demo images | ...). 
 *
 * Note that it does not generate comparisons between directories. You can 
 * still do that by clicking on the left (and then later the right) and 
 * using the (File | Open File Images)command to manually load the image.
 *
 * If you have this type of data, it will also add the 
 * (File | Open user images | List user's images by directory) command.
 * You can use this to get a report of all of the files in the popup report
 * window.
 *
 * The structure contains the following 4 fields:
 *        SubMenuName, SubMenuEntry,  ImageURL1, ImageURL2
 *<PRE>
 * The map is used to build menus of images to load
 * a map entry contains the following fields:
 * 1. SubMenuName: is the menu image name: e.g.,
 *   "Human Plasma"
 * 2. SubMenuEntry: is the menu image name: e.g.,
 *   "(Swiss2DPAGE vs. plasmaL) gels - clickable"
 * 3. ImageURL1: is the Image URL for image 1: e.g.
 *   "Swiss2DPAGE.gif"
 * 4. ImageURL2: is the Image URL for image 2: e.g.
 *   "plasmaL.gif"
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

public class FlkUser
{ /* FlkUser */
  
  /** Instance of Flicker */
  private static Flicker
    flk;  
  /** Instance of Util */
  private static Util
    util;
  
  /** Max # of images since can have LOTS of combinations of user data */
  public final static int
    MAX_USER_IMAGES= 10000;
  
  /** MAX # of user databases */
  public final static int
    MAX_DATABASES= 200;
  
  /** FlkUser database [0:nMaps] */
  public static FlkUser
    flkUsers[]= null;
  /** # of flkUsers[] map entries */
  public static int 
    nMaps= 0;  
  
  /* --- static database of user image directories added to user DB --- */
  /** [0:nDirs-1] user image directories names if any */
  public static String
    dirList[]= null;
  /** [0:nDirs-1] user image directory paths if any */
  public static String
    dirListPath[]= null;
  /** # of user image directories */
  public static int
    nDirs= 0;  
  /** [0:nDirs-1][0:nFiles(dir)-1] all image file names for all directories */
  public static String
    allImgFileList[][]= null;      
  /** # of user data pairs added */
  public static int
    nPairs= 0;      
  
  
  /* ------------------- FlkUser Instance Database ----------------- */
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
   /** ImageURL1: is the Image URL for image 1: e.g.
    *   "http://www.expasy.org/ch2dgifs/PLASMA_HUMAN/PLASMA_HUMAN_id.gif" 
   */
   String
     imageURL1;
   /** imageURL2: is the Image URL for image 2: e.g.
    *   "plasmaL.gif"
   */
   String
     imageURL2;
  
 
  /**
   * FlkUser() - constructor for Class FlkUser
   * @param flk instance of Flicker
   */
  public FlkUser(Flicker flkS)
  { /* FlkUser */
    flk= flkS;  
    util= flk.util;
   
    /* [1] FlkUser database [0:nMaps] */    
    flkUsers= new FlkUser[MAX_USER_IMAGES];
    nMaps= 0;  
        
    /* [2] Add "Images/<userDirectories>/..." data to
     * (File | Open user images | ...subdirectories) menu entries.
     * Look for directories in "Images/" and then compute
     * all combinations of images (i,j) as  im#i vs im#j, etc.
     * The SubMenuEntry is "#i vs #j".
     * So the data is organized by subdirectory. Use the subdirectory
     * name as the SubMenuNames.
     */
    int nPairs= addUserImagesPairs2UserDB();    
  } /* FlkUser */
 
 
  /**
   * FlkUser() - constructor to add a new FlkUser entry
   * NOTE: to do add of an entry, use 'new FlkUser()' method 
   * @param subMenuName
   * @param subMenuEntry
   * @param imageURL1
   * @param imageURL2 
   */
  public FlkUser(String subMenuName, String subMenuEntry, 
                 String imageURL1, String imageURL2)
  { /* FlkUser */
    if(imageURL1!=null && imageURL1.indexOf("://")==-1)
      imageURL1= flk.util.useFileSeparatorChar(imageURL1, 
                                               flk.fileSeparator);
    if(imageURL2!=null && imageURL2.indexOf("://")==-1)
      imageURL2= flk.util.useFileSeparatorChar(imageURL2,
                                               flk.fileSeparator);
    
    this.subMenuName= subMenuName; 
    this.subMenuEntry= subMenuEntry;
    this.imageURL1= imageURL1;
    this.imageURL2= imageURL2;     
  } /* FlkUser */
      
  
  /** 
   * addUserImagesPairs2UserDB() - add "Images/<userDirectories>/..." 
   * data to  (File | Open user images | ...subdirectories) menu entries.
   * Look for directories in "Images/" and then compute
   * all combinations of images (i,j) as  im#i vs im#j, etc.
   * The SubMenuEntry is "#i vs #j".
   * So the data is organized by subdirectory. Use the subdirectory
   * name as the SubMenuNames.
   * @return # of additional user image pairs added if added user image 
   *     subdirectories
   */
  private int addUserImagesPairs2UserDB()
  { /* addUserImagesPairs2UserDB */
    /* [1] Build list of sub-directories */
    String
      imagesDirPath= flk.userDir + "Images",
      topLevelFiles[]= flk.fio.getFilesInDir(imagesDirPath, null);
    if(topLevelFiles==null)
      return(0);    
    
    int 
      nTopLevelFiles= topLevelFiles.length,
      nFiles= 0;                       /* # of files in working directory */
    String
      imgFile,                         /* next img file if any */
      dirFile,                         /* next directory file if any */
      fullDirPath,                     /* full path of next directory */      
      subMenuName,
      subMenuEntry,
      sLeft, sLeftName,
      imageURL1, imageURL2,
      sRight, sRightName;       
    String legalExtensions[]= ImageIO.legalImageFileExtensions;
    
    dirList= new String[nTopLevelFiles];  /* worst case */
    dirListPath= new String[nTopLevelFiles];
  
    for(int i= 0;i<nTopLevelFiles;i++)
    { /* get a list of non-empty directories */     
      dirFile= topLevelFiles[i];
      fullDirPath= imagesDirPath + flk.fileSeparator + dirFile;
      File fd= new File(fullDirPath);
      if(fd.isDirectory())
      { /* only add directories that have at least 1 file in them */
        String
          filesList[]= flk.fio.getFilesWithLegalExtnInDir(fullDirPath, 
                                                          legalExtensions);
        if(filesList==null || filesList.length==0)
          continue;
        dirList[nDirs]= dirFile; 
        dirListPath[nDirs++]= fullDirPath; 
      } 
    } /* get a list of non-empty directories */ 
    
    /* Sort the list of directories */
    dirList= flk.util.bubbleSort(dirList, nDirs);
    dirListPath= flk.util.bubbleSort(dirListPath, nDirs);
    allImgFileList= new String[nDirs][];
    
    /* [2] Look for image files inside of each directory */ 
    for(int i= 0;i<nDirs;i++)
    { /* add images in ith image subdirectory */
      dirFile= dirList[i];
      fullDirPath= dirListPath[i];
      String 
        imgFileList[]= flk.fio.getFilesWithLegalExtnInDir(fullDirPath, 
                                                          legalExtensions);
      if(imgFileList==null)
        continue;              /* ignore this directory */
      nFiles= imgFileList.length;
      //if(nFiles<=1)
      //  continue;             /* must have at least 2 images to do pairs */      
      imgFileList= util.bubbleSort(imgFileList, nFiles);
      allImgFileList[i]= imgFileList;
      
      /* [2.1] Add combinations of these files to user DB */
      if(nFiles==1)
      { /* save single image */ 
        subMenuName= dirFile;       
        subMenuEntry= imgFileList[0];
        imageURL1= "Images/"+dirFile+"/"+imgFileList[0];
        flkUsers[nMaps++]= new FlkUser(subMenuName, subMenuEntry,
                                       imageURL1, null);
      } /* save single image */
      else
      { /* build pairs of images */
        for(int j= 0;j<(nFiles-1);j++)
        { /* left image */
          subMenuName= dirFile;
          sLeft= imgFileList[j];
          imageURL1= "Images/"+dirFile+"/"+sLeft;
          sLeftName= util.getFileNameFromPath(sLeft);
          /* Remove ".tif", ".jpg", ".gif", ".ppx" */
          sLeftName= util.rmvFileExtension(sLeftName);
          
          for(int k= (j+1);k<nFiles;k++)
          { /* right image */
            sRight= imgFileList[k];
            imageURL2= "Images/"+dirFile+"/"+sRight;
            sRightName= util.getFileNameFromPath(sRight);
            /* Remove ".tif", ".jpg", ".gif", ".ppx" */
            sRightName= util.rmvFileExtension(sRightName);
            subMenuEntry= sLeftName + " vs. " + sRightName;
            nPairs++;
            /* push pair of images */
            flkUsers[nMaps++]= new FlkUser(subMenuName, subMenuEntry,
                                           imageURL1, imageURL2);
          } /* right image */
        } /* left image */
      }  /* buildpairs of images */
    } /* add images in ith image subdirectory */
     
    return(nPairs);
  } /* addUserImagesPairs2UserDB */
  
        
  /**
   * lookup() - lookup entry to database flkUsers[0:nMaps-1].
   * @param fm is the instance to lookup
   * @return index of flkUsers[] if succeed, else -1
   */
  public static int lookup(FlkUser fm)
  { /* lookup */  
    for(int i=0;i<nMaps;i++)
      if(fm==flkUsers[i])
      { /* found it */
        return(i);
      }
        
    return(-1); 
  } /* lookup */
      
        
  /**
   * lookupBySubmenu() - lookup entry by submenu name to flkUsers[0:nMaps-1].
   * @param subMenuEntry is the menu name of the instance to lookup
   * @return index of flkUsers[] if succeed, else -1
   */
  static int lookupBySubmenu(String subMenuEntry)
  { /* lookupBySubmenu */ 
    for(int i=0;i<nMaps;i++)
      if(subMenuEntry.equals(flkUsers[i].subMenuEntry))
      { /* found it */
        return(i);
      }
        
    return(-1);
  } /* lookupBySubmenu */
      
        
  /**
   * lookupByFileName() - lookup entry by file name to flkUsers[0:nMaps-1].
   * @param fileName1 is the fileName name of the instance to lookup
   *       for imageURL1 (if not null)
   * @param fileName2 is the fileName name of the instance to lookup
   *       for imageURL2 (if not null)
   * @return index of flkUsers[] if succeed, else -1
   */
  public static int lookupByFileName(String fileName1, String fileName2)
  { /* lookupByFileName */ 
    for(int i=0;i<nMaps;i++)
      if(fileName1!=null && fileName1.equals(flkUsers[i].imageURL1))
      { /* found it */
        return(i);
      }
    else if(fileName2!=null && fileName2.equals(flkUsers[i].imageURL1))
      { /* found it */
        return(i);
      }
        
    return(-1);
  } /* lookupByFileName */
      
  
  /**
   * set() - set entry to database flkUsers[0:nMaps-1].
   * NOTE: to do add of an entry, use 'new FlkUser()' method 
   * @param subMenuName
   * @param subMenuEntry
   * @param imageURL1
   * @param imageURL2 
   * @return true if succeed
   */
  public boolean set(String subMenuName, String subMenuEntry, 
                     String imageURL1, String imageURL2)
  { /* set */ 
    if(nMaps>= MAX_USER_IMAGES)
      return(false);
        
    this.subMenuName= subMenuName; 
    this.subMenuEntry= subMenuEntry;
    this.imageURL1= imageURL1;
    this.imageURL2= imageURL2;
        
    return(true);
  } /* set */
  
  
  /**
   * delete() - rmv entry from database flkUsers[0:nMaps-1].
   * @return true if succeed
   */
  public boolean delete()
  { /* delete */  
    int n= lookup(this);
    if(n==-1)
      return(false);
    
    for(int i=n;i<(nMaps-1);n++)
       flkUsers[i]= flkUsers[i+1];
    flkUsers[nMaps-1]= null;
    nMaps--;  
        
    return(true);
  } /* delete */
      
  
  /**
   * clear() - clear database flkUsers[0:nMaps-1].
   */
  public static void clear()
  { /* clear */  
    for(int i=0;i<nMaps;i++)
      flkUsers[i]= null;
    nMaps= 0;    
  } /* clear */ 
      
  
  /**
   * listUserImageDirs() - List user's images by directory
   */
  public static void listUserImageDirs()
  { /* listUserImageDirs */ 
    if(nPairs==0)
    {
      String msg= "There are no user image data directories";
      util.popupAlertMsg(msg, flk.alertColor); 
      return;
    }
    
    String 
      sMsg= "\n"+
            "List of user image data directories\n"+
            "-------------------------------------\n";
    
    sMsg += "Added "+nPairs+
            " pairs of comparisons of user's image data files.\n";
    
    /* Print each directory in turn */
    for(int i= 0;i<nDirs;i++)
    { /* add images in ith image subdirectory */
      String dirFile= dirList[i];      
      sMsg += "Folder ["+(i+1)+"] "+ dirFile + "\n";
      
      String fileList[]= allImgFileList[i];
      int nFiles= fileList.length;
      for(int j=0;j<nFiles;j++)        
        sMsg += "   " + fileList[j] + "\n";
      sMsg += "\n";
    }
      
    flk.util.appendReportMsg(sMsg);
  } /* listUserImageDirs */
   
} /* End of FlkUser */

