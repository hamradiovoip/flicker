/* File: ImageIO.java */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.image.ImageObserver;
import java.util.*;
import java.net.*;
import java.lang.*;
import java.io.*;

/**
 * ImageIO class is used to do Flicker image I/O and conversion functions.
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
 * @author P. Lemkin (LECB/NCI), G. Thornwall (SAIC),  Frederick, MD, USA
 * @version $Date$   $Revision$
 * @see <A HREF="http://open2dprot.sourceforge.net/Flicker">Flicker Home</A>
 */

public class ImageIO
{ /* ImageIO */
  
  /** Flicker class */
  public Flicker
    flk;
  /** BuildGUI instance */
  private BuildGUI
    bGui;
  /** extended Flicker state variable class */
  private Util
    util;
  
  /** list of legal image file extensions */
  final public static String
    legalImageFileExtensions[]=
             { ".gif", ".jpg", ".tif", ".tiff", ".ppx",  //".j2k",
               ".GIF", ".JPG", ".TIF", ".TIFF", ".PPX" //, ".J2K"
             };
  
  /** set after loaded */
  public  boolean
    isLoadedFlag= false;	
  /** # of lines to read before update status*/
  public final static int
    NLINES= 80;		
  /** counter to print '.' every NLINES lines*/
  public static
    int count= NLINES;	
  /** for incremental loading message progress string */
  String
    sWorking= ""; 
  
  
  /**
   * ImageIO() - Constructor
   */
  public ImageIO(Flicker flk, String sWorking)
  { /* ImageIO */
    this.flk= flk;
    this.sWorking= sWorking;
    
    bGui= flk.bGui;
    util= flk.util;   
    
    isLoadedFlag= false;	        /* set true after loaded */
  } /* ImageIO */
  
  
  /**
   * get_sWorking() - get the working image progress string
   */
  public synchronized String get_sWorking()
  { return(sWorking); }
  
  
  /**
   * set_sWorking() - set the working image progress string
   */
  public synchronized void set_sWorking(String s)
  { sWorking= s; }
  
  
  /**
   * loadPixIntoImageData() - read pix file into ImageData obj.
   * which includes a iPix[] of the data.
   * If overloading a new image into an existing ImageData structure
   * then specify the oldImageData structure else set it to null.
   * If the file is used standalone with imageURL set to null,
   * then it assumes it will be
   *    "file://localhost/" + imageFile or 
   *    "file://localhost" + imageFile
   * This is done using the constructor:
   *   URL(protocol, host, filename)
   * where  protocol= "file"
   *        host= "localhost".
   * Note: if DOS file system "C:\pixfile.gif",
   * then remap to "file://localhost/C:\pixfile.gif"
   * @param imageURL url for the image file
   * @param imageFile image file name
   * @param oldImageData is old ImageData
   * @param imgNbr for debugging
   * @param reportErrorMsgsFlag to report error it it occurs on load
   * @return ImageData for the picture else null if a problem.
   */
  public ImageData loadPixIntoImageData(String imageFile,
                                        ImageData oldImageData,
                                        int imgNbr,
                                        boolean reportErrorMsgsFlag)
  { /* loadPixIntoImageData */
    /* [1] Test if process a TIFF file  */
    ImageData idn= null;
    int idxDot= imageFile.lastIndexOf(".");
    String fileExt= (idxDot>0) ? imageFile.substring(idxDot) : null;
    
    if(fileExt==null) 
      return(null);
    
    if(fileExt.equalsIgnoreCase(".tif") || 
       fileExt.equalsIgnoreCase(".tiff"))
    { /* process as TIFF file */
      idn= loadTiffPixIntoImageData(imageFile,oldImageData, imgNbr,
                                    reportErrorMsgsFlag);            
      return(idn);
    } /* process as TIFF image */
    
    else if(fileExt!=null && fileExt.equalsIgnoreCase(".ppx"))
    { /* process as PPX file */
      idn= loadPPXpixIntoImageData(imageFile,oldImageData, imgNbr,
                                   reportErrorMsgsFlag);
      return(idn);
    } /* process as PPX image */     
    
    else if(fileExt!=null && fileExt.equalsIgnoreCase(".j2k"))
    { /* process as JPEG2000 file */
      idn= loadJpeg2000PixIntoImageData(imageFile,oldImageData, imgNbr,
                                        reportErrorMsgsFlag);
      return(idn);
    } /* process as JPEG2000 image */ 
    
    else if(fileExt!=null && 
       (fileExt.equalsIgnoreCase(".jpg") || 
        fileExt.equalsIgnoreCase(".gif")))
    { /* process as JPEG or GIF file */
      idn= loadGifOrJpegPixIntoImageData(imageFile,oldImageData, imgNbr,
                                         reportErrorMsgsFlag);
      return(idn);
    } /* process as JPEG or GIF image */
    
    else
      return(null);
  } /* loadPixIntoImageData */ 
  
  
  /**
   * loadTiffPixIntoImageData() - read Tiff pix file into ImageData
   * object which includes a iPix[] of the data.
   * If overloading a new image into an existing ImageData structure
   * then specify the oldImageData structure else set it to null.
   * @param imageURL url for the image file
   * @param imageFile image file name
   * @param oldImageData is old ImageData
   * @param imgNbr for debugging
   * @param reportErrorMsgsFlag to report error it it occurs on load
   * @return ImageData for the picture else null if a problem.
   */
  public synchronized ImageData loadTiffPixIntoImageData(String imageFile,
                                            ImageData oldImageData,
                                            int imgNbr,
                                            boolean reportErrorMsgsFlag)
  { /* loadTiffPixIntoImageData */
    Image iImg;
    ImageData iDataNew= oldImageData;
    Toolkit toolkit= Toolkit.getDefaultToolkit(); 
    
    /* [1] Get iImg from TiffLoader */    
    flk.readyFlag= false;
    TiffLoader tr= new TiffLoader(flk.dbugFlag);
    
    tr.setUseLogFlag(flk.useLogInputFlag);
    String fatalMsg= tr.doTiffLoad(imageFile,true);  /* get the image */
    if(fatalMsg!=null)
    {
      if(fatalMsg.startsWith("java.lang.RuntimeException:"))
      {
        int idx= fatalMsg.indexOf(":");
        fatalMsg= fatalMsg.substring(idx+2);
      }
      util.showMsg1("Can't read TIFF image", Color.red);
      util.showMsg2(fatalMsg, Color.red);
      tr= null;                    /* set for G.C. */
      return(null);
    }    
    
    iImg= tr.getImage();            
    flk.readyFlag= true;
    if(iImg==null)
    {
      tr= null;                    /* set for G.C. */
      return(oldImageData);
    }
    
    float mapGrayToOD[]= null;     /* could overide if read it from file */
    
    if(flk.dbugFlag)
      System.out.println("tr="+tr.toString());    
    
    /* [2] Resize image to target size if the image size is outside the range
     * [minTargetSize : maxTargetSize].
     * We resize if we DO need to resize AND (DEFAULT_TARGET_SIZE!=0).
     * Also keep the (h/w) aspect ratio the same.
     */
    Image rImg= ImageData.resizeImageToTargetSize(iImg, tr.nCols, tr.nRows,
                                                  flk.MIN_TARGET_SIZE,
                                                  flk.MAX_TARGET_SIZE,
                                                  flk.DEFAULT_TARGET_SIZE);   
        
    /* [3] set the rImg and associated parameters to the oldImageData
     * object. If oldImageData does not exist (i.e. null), then 
     * create a new one.
     */ 
    iDataNew= setImageToImageData(imageFile, rImg, tr.blackIsZeroFlag,  
                                  oldImageData, mapGrayToOD, imgNbr);
    if(rImg!=null)
      iDataNew.checkAndMakeIpix(reportErrorMsgsFlag);       /* Make iPix */ 
          
    tr= null;                            /* set for G.C. */
    
     return(iDataNew);
  } /* loadTiffPixIntoImageData */
  
     
  /**
   * loadPPXpixIntoImageData() - read GELLAB-II PPX pix file into 
   * ImageData object which includes a iPix[] of the data.
   * If overloading a new image into an existing ImageData structure
   * then specify the oldImageData structure else set it to null.
   * @param imageURL url for the image file
   * @param imageFile image file name
   * @param oldImageData is old ImageData
   * @param imgNbr for debugging
   * @param reportErrorMsgsFlag to report error it it occurs on load
   * @return ImageData for the picture else null if a problem.
   */
  public synchronized ImageData loadPPXpixIntoImageData(String imageFile,
                                            ImageData oldImageData,
                                            int imgNbr,
                                            boolean reportErrorMsgsFlag)
  { /* loadPPXpixIntoImageData */
    Image iImg;
    ImageData iDataNew= oldImageData;         
    flk.readyFlag= false;
    Toolkit toolkit= Toolkit.getDefaultToolkit(); 
    
    /* [1] Get the iImg from PpxLoader */
    PpxLoader ppx= new PpxLoader();
    if(! ppx.readPPXfile(imageFile))
    {
      ppx= null;                   /* set for G.C. */
      return(oldImageData);
    }
    
    iImg= ppx.getImage();   
             
    flk.readyFlag= true;
    if(iImg==null)
    {
      ppx= null;                   /* set for G.C. */
      return(oldImageData);
    }
    
    if(flk.dbugFlag)
      System.out.println("ppx="+ppx.toString());
    
    /* [2] Resize image to target size if the image size is outside the range
     * [minTargetSize : maxTargetSize].
     * We resize if we DO need to resize AND (DEFAULT_TARGET_SIZE!=0).
     * Also keep the (h/w) aspect ratio the same.
     */
    Image rImg= ImageData.resizeImageToTargetSize(iImg, ppx.ncols, ppx.nrows,
                                                  flk.MIN_TARGET_SIZE,
                                                  flk.MAX_TARGET_SIZE,
                                                  flk.DEFAULT_TARGET_SIZE);
    
    /* [3] set the rImg and associated parameters to the oldImageData
     * object. If oldImageData does not exist (i.e. null), then 
     * create a new one.
     */ 
    float mapGrayToOD[]= null;   /* could overide IF read it from file */    
    iDataNew= setImageToImageData(imageFile, rImg, ppx.blackIsZeroFlag,  
                                  oldImageData, mapGrayToOD, imgNbr);
    if(rImg!=null)
      iDataNew.checkAndMakeIpix(reportErrorMsgsFlag);       /* Make iPix */           
          
    ppx= null;                           /* set for G.C. */
    
     return(iDataNew);
  } /* loadPPXpixIntoImageData */
  
  
  /**
   * loadJpeg2000PixIntoImageData() - read JPEG 2000 pix file into 
   * ImageData object which includes a iPix[] of the data.
   * If overloading a new image into an existing ImageData structure
   * then specify the oldImageData structure else set it to null.
   * [TODO] need to integrate a JPEG 2000 Java reader.
   * Could possibly use JJ2000 at http://jj2000.epfl.ch/
   * @param imageURL url for the image file
   * @param imageFile image file name
   * @param oldImageData is old ImageData
   * @param imgNbr for debugging
   * @param reportErrorMsgsFlag to report error it it occurs on load
   * @return ImageData for the picture else null if a problem.
   */
  public synchronized ImageData loadJpeg2000PixIntoImageData(String imageFile,
                                                ImageData oldImageData,
                                                int imgNbr,
                                                boolean reportErrorMsgsFlag)
  { /* loadJpeg2000PixIntoImageData */
    Image iImg;
    ImageData iDataNew= oldImageData;         
    flk.readyFlag= false;
    Toolkit toolkit= Toolkit.getDefaultToolkit(); 
    
    /* [1] Get the iImg from JPEG2000 Loader */
    iDataNew= null;      /* Replace with load code */
    
     return(iDataNew);
  } /* loadJpeg2000PixIntoImageData */
    
  
  /**
   * loadGifOrJpegPixIntoImageData() - read GIF or JPEG pix file into
   * ImageData object which includes an iPix[] of the data.
   * If overloading a new image into an existing ImageData structure
   * then specify the oldImageData structure else set it to null.
   * If the file is used standalone with imageURL set to null,
   * then it assumes it will be
   *    "file://localhost/" + imageFile or 
   *    "file://localhost" + imageFile
   * This is done using the constructor:
   *   URL(protocol, host, filename)
   * where  protocol= "file"
   *        host= "localhost".
   * Note: if DOS file system "C:\pixfile.gif",
   * then remap to "file://localhost/C:\pixfile.gif"
   * @param imageURL url for the image file
   * @param imageFile image file name
   * @param oldImageData is old ImageData
   * @param imgNbr for debugging
   * @param reportErrorMsgsFlag to report error it it occurs on load
   * @return ImageData for the picture else null if a problem.
   */
  public synchronized ImageData loadGifOrJpegPixIntoImageData(String imageFile,
                                                   ImageData oldImageData,
                                                   int imgNbr,
                                                   boolean reportErrorMsgsFlag)
  { /* loadGifOrJpegPixIntoImageData */    
    /* [1] Process as either .gif or .jpg - other types are handled above */
    Image iImg= null;
    ImageData iDataNew= oldImageData;    /* default is to use the old ImageData */
    Toolkit toolkit= Toolkit.getDefaultToolkit(); 
    if(flk.CONSOLE_FLAG)
      flk.util.gcAndMemoryStats("Begining of loadGifOrJpegPixIntoImageData()");
    
    /* [2] Try to create a URL if image file starts with a URL prefix */
    URL imageURL= null;
    if(imageFile.indexOf("://")>0)
    { /* try to build the URL */
      try 
      {   
        imageURL= new URL(imageFile);
      }
      catch (Exception e) 
      {
        System.out.println("Illegal URL: '" + imageFile + "'");
        return((ImageData)null);
      }
    } /* try to build the URL */
                                        
    /* [3] Get image with a FILE or URL spec. */
    if(imageURL==null)
     { /* File */       
       try 
       {   
         iImg= toolkit.getImage(imageFile);
       }
       catch (Exception e) 
       {
         System.out.println("Can't load File: '" + imageFile + "'");
         return(null);
       }
    } /* File */
    else
    { /* URL */
      try
      {
        iImg= toolkit.getImage(imageURL);
      }
      catch (Exception e)
      {
        System.out.println("Can't load File: '" + imageURL + "'");
        return(null);
      }
    } /* URL */
    
    /* [4] Wait while it is loaded. Use MediaTracker() */
    MediaTracker tracker= new MediaTracker(flk);
    tracker.addImage(iImg, 0);
    
    int
      nSeconds= 0,
      pctDone= 0,
      bits= 0,
      delayTrackerMsec= 2000, /* 2 seconds */
      maxRetries= 120,        /* 2 minutes = 2*60*1000 mSec */
      nRetries= maxRetries;
    String mErrs= "";
    double log10= Math.log(10);
    
    while(nRetries-- > 0)
    { /* keep testing with Media tracker */
      try
      { 
        tracker.waitForID(0,delayTrackerMsec); 
      }
      catch(InterruptedException ie)
      {
        String msg= "Fatal error - interrupted loading image: "+imageFile;
        util.popupAlertMsg(msg, flk.alertColor);
        ie.printStackTrace();
        return(null);
      }
      bits= tracker.statusID(0, false);
      
      if (tracker.isErrorAny())
      { /* has tracker errors */
        String status= showMediaTrackerStatusBits("IIO-LPIID[a]", imageURL, 
                                                  imageFile, tracker);
        System.out.println(status);
        String msg= "Fatal error - can't load image "+imageFile;
        util.popupAlertMsg(msg, flk.alertColor);
        return(null);
      } /* has tracker errors */
      
      else if((bits & MediaTracker.LOADING)!=0)
      { /* still loading  - report to console */
        nSeconds= maxRetries-nRetries;
        pctDone= (int)((100*(nSeconds))/maxRetries);
        double
          fraction= (double)(nSeconds+maxRetries)/(double)maxRetries,
          logPctDone= 100.0*Math.log(5.0*fraction)/log10;
        pctDone= (int)logPctDone;
        
        mErrs= pctDone + "% done";
        util.showStatus(mErrs, Color.magenta);  /* DO NOT append to report*/
      }
      
      else if((bits & MediaTracker.COMPLETE)!=0)
      {
        if(flk.dbugFlag)
         System.out.println(
                   "IIO-LPIID-[b]: MediaTracker: Image COMPLETE nRetries="+
                            nRetries+pctDone + "% done");        
      }
    } /* keep testing with Media tracker */
    
    if((bits & MediaTracker.COMPLETE)==0)
    {
      String msg= "Fatal err: timed out - can't load image. nRetries="+
                  nRetries+pctDone + "% done";
      util.popupAlertMsg(msg, flk.alertColor);
    }
    
    if(flk.dbugFlag)
    {
      String status= showMediaTrackerStatusBits("IIO-LPIID[c]", imageURL, 
                                                imageFile, tracker);
      System.out.println(status);
    }    
        
    /* [5] Force image to be read to get size */
    Dimension iSize= new Dimension();
    /* OLD:  iSize= toolkit.getImageSize(iImg, fo);  */
    /* Kludge to force image data to be read so we can get pixels. */    
    flk.readyFlag= false;
    FlkObserver fo= new FlkObserver(flk,this); /* get image observer */
    while ((iSize.width= iImg.getWidth(fo)) < 0)
      util.sleepMsec(100);
    while ((iSize.height= iImg.getHeight(fo)) < 0 )
      util.sleepMsec(100); 
    
    /* Remove the media tracker */
    tracker.removeImage(iImg);
    tracker= null;
    toolkit= null;
    if(flk.CONSOLE_FLAG)
      flk.util.gcAndMemoryStats(
                   "loadGifOrJpegPixIntoImageData() before resizeImage()");
    
    float mapGrayToOD[]= null;  /* could overide if read it from file */
    
    /* [6] Resize image to target size if the image size is outside the range
     * [minTargetSize : maxTargetSize].
     * We resize if we DO need to resize AND (DEFAULT_TARGET_SIZE!=0).
     * Also keep the (h/w) aspect ratio the same.
     */
    Image rImg= ImageData.resizeImageToTargetSize(iImg, iSize.width,
                                                  iSize.height,
                                                  flk.MIN_TARGET_SIZE,
                                                  flk.MAX_TARGET_SIZE,
                                                  flk.DEFAULT_TARGET_SIZE);
    
    /* [7] set the rImg and associated parameters to the oldImageData
     * object. If oldImageData does not exist (i.e. null), then 
     * create a new one.
     */ 
    boolean blackIsZeroFlag= true;
    iDataNew= setImageToImageData(imageFile, rImg, blackIsZeroFlag,  
                                  oldImageData, mapGrayToOD, imgNbr);
    if(rImg!=null)
      iDataNew.checkAndMakeIpix(reportErrorMsgsFlag);       /* Make iPix */ 
          
    return(iDataNew);
  } /* loadGifOrJpegPixIntoImageData */
  
  
  /**
   * showMediaTrackerStatusBits() - print the media tracker status bits
   * @param msg to add prefix to status string
   * @param imageURL
   * @param imageFile
   * @param tracker MediaTracker being used
   * @return status string
   */
  private synchronized String showMediaTrackerStatusBits(String msg, 
                                                         URL imageURL, 
                                                         String imageFile, 
                                                         MediaTracker tracker)
  { /* showMediaTrackerStatusBits */
    int bits= tracker.statusID(0, false);
    String mErrs= "";
    if((bits & MediaTracker.ABORTED)!=0)
      mErrs += "Aborted ";
    if((bits & MediaTracker.ERRORED)!=0)
      mErrs += "Errored ";
    if((bits & MediaTracker.LOADING)!=0)
      mErrs += "Errored ";
    if((bits & MediaTracker.COMPLETE)!=0)
      mErrs += "Errored ";
    
    String sR= msg + ".1 Error loading image ["+ mErrs + "]\n"+
               msg + ".2 imageURL= '" + imageURL + "'\n"+
               msg + ".3 imageFile= '" + imageFile + "'\n";
    return(sR);
  } /* showMediaTrackerStatusBits */
  
  
  /**
   * setImageToImageData() - set the Image and associated 
   * parameters to the ImageData object imgData. If imgData
   * does not exist (i.e. null), then create a new ImageData
   * object.
   * @param imageFile image file name
   * @param iImg image just read
   * @param blackIsZeroFlag is true if image has black as 0 gray value
   * @param imgData is old ImageData object if it exists
   * @param mapGrayToOD is Map of Gray to OD if exists and is not null
   * @param imgNbr for debugging
   * @return ImageData is returned, null if error.
   */
  public ImageData setImageToImageData(String imageFile, Image iImg,
                                       boolean blackIsZeroFlag,
                                       ImageData imgData,
                                       float mapGraytoOD[], int imgNbr)
  { /* setImageToImageData */    
    /* [1] Get the image size in case it changed */
    Dimension iSizeR= new Dimension();
    iSizeR.width= iImg.getWidth(flk);
    iSizeR.height= iImg.getHeight(flk);
        
    /* Report results */
    if(flk.dbugFlag)
      System.out.println("IIO-LPTIID: ImageSize[" + imgNbr +
                         " (W,H) size=("+iSizeR.width +
                         "," + iSizeR.height + ")");
      
     /* [2] Package iImg in imgData */
     if(imgData==null)
     { /* No old image data object - create it.*/
       /* Note: probably not needed, most likely not to be null 
        * since we create a blank version.
        */
       imgData= new ImageData(imageFile, iImg, iSizeR,
                               blackIsZeroFlag,
                               mapGraytoOD,
                               flk);
     }
     else 
     { /* reuse old image data object */
       imgData.changeImageData(imageFile, iImg, iSizeR, blackIsZeroFlag,
                               mapGraytoOD);
     } 
   
     /* [3] Set default colormap for Image and set into indexColorModel*/      
     imgData.setDefaultGrayscaleIndexColorMap();
               
     return(imgData);
  } /* setImageToImageData */  
            

  /**
   * changeImageFromSpec() - load a new image from file.
   * leftRightStr should be either "left" (i.e. iData1)
   * import "right" (i.e., iData2).
   * At that point we can create an image from the input iPix
   * data and repaint it.
   * @param newImageFileStr is either URL string or File path
   * @param leftRightStr is "left" or "right" image indicator
   * @param initSliderStateFlag to init the slider state
   * @param reportErrorMsgsFlag to report error it it occurs on load
   * @return true if succeed
   */
  public synchronized boolean changeImageFromSpec(String newImageFileStr,
                                                  String leftRightStr,
                                                  boolean initSliderStateFlag,
                                                  boolean reportErrorMsgsFlag)
  { /* changeImageFromSpec */
    /* [1] Verify that image is I1 or I2 and filename is not null */
    if(! "left".equals(leftRightStr) && ! "right".equals(leftRightStr))
    {
      String msg= "First pick left or right image to overload";
      util.popupAlertMsg(msg, flk.alertColor);
      return(false);
    }
    
    /* [2] Test if need to save the .flk file if something changed. 
     * If so, ask them if they want to do it.
     */
    flk.checkIfSaveFlkFile(); 
    
    /* [2.1] Check if current image contains clickable DB images when set
     * the isClickableDBflag
     */
    flk.chkIfClickableDB(true);
    
    /* [2.2] Reset the slide state and slider GUIs to the defaults.
     * Note: dont't reset it if using the .flk state data just read.
     */
    if(initSliderStateFlag)
    { /* reset the slide state and slider GUIs to the defaults */
      if(leftRightStr.equals("left"))
      {
        flk.iData1.state.init(flk,"left");
        flk.evs.setEventScrollers(flk.iData1.state);
      }
      else if(leftRightStr.equals("right"))
      {
        flk.iData2.state.init(flk,"right");
        flk.evs.setEventScrollers(flk.iData2.state);
      }
    } /* reset the slide state and slider GUIs to the defaults */
  
    /* [2.3] ... */
    String origActiveImage= flk.activeImage;
    boolean readCalibFlag= false;
    ImageData iDataNew= null;
        
    /* [2.4] Mark active image */
    flk.activeImage= leftRightStr;
        
    if (newImageFileStr!=null)
    { /* [TODO] may want to get short image name rather than full path */
      util.showMsg("Changing '"+leftRightStr+"' image to " +
                   newImageFileStr, Color.black);
    }
    else
    {
      String msg= "Bad "+leftRightStr+" image file name";
      util.showMsg2(msg, Color.red);
      flk.activeImage= origActiveImage;   /* restore active image state */
      return(false);
    }
    
    /* [3] Go get the image into flk.iData */
    util.showMsg2("Loading image ["+newImageFileStr+"]", Color.black);
    if("left".equals(flk.activeImage))
    { /* change image 1 */
      restoreImage(flk.iData1,reportErrorMsgsFlag);
      iDataNew= loadPixIntoImageData(newImageFileStr, flk.iData1, 
                                     1, /* imgNbr */
                                     reportErrorMsgsFlag);
      if(iDataNew==null)
      { /* Failed to read image */
        String msg= "Bad image data file ["+newImageFileStr+"]";
        util.showMsg(msg, Color.red);
        util.popupAlertMsg(msg, flk.alertColor);
        flk.activeImage= origActiveImage;   /* restore active image state */
        return(false);
      }
      else if(iDataNew==flk.iData1)
      { /* replace image */
        flk.imageFile1= iDataNew.imageFile;
        String dbMenuName= util.getFileNameFromPath(flk.imageFile1);
        /* Add to the "FlkRecent" database if it is NOT a DEMO image */
        if(FlkDemo.lookupByFileName(flk.imageFile1,flk.imageFile1)!=-1)
        {          
          FlkRecent fr= FlkRecent.addOrUpdate(dbMenuName,
                                              flk.clickableCGIbaseURL1,   
                                              flk.imageFile1, "dbName", 
                                              util.getCurDateStr());
        }
        
        /* Read the calibration files if it exists */
        readCalibFlag= util.readCalibrationFile(flk.iData1);
        if(readCalibFlag)
        { /* copy the calibration to active ImageData instance */
          flk.iData1.mapGrayToOD= flk.iData1.calib.getMapGrayToOD();
          flk.iData1.hasODmapFlag= flk.iData1.calib.getHasODmapFlag();
          util.showMsg("Read the image calibration file for: "+dbMenuName,
                       Color.black);
        }        
        util.showMsg2("#bits/pixel="+iDataNew.nBitsPerPixel+
                      " grayscale ["+iDataNew.minG+":"+
                      flk.iData1.maxG+"] [WxH]=["+
                      flk.iData1.iWidth+"x"+flk.iData1.iHeight+"]", 
                      Color.black);
        util.showMsg2("Done", Color.black);
        
        /* Regenerate the scrollable image */
        ImageScroller oldi1IS= flk.i1IS;
        flk.i1IS= new ImageScroller(flk, "left", "Image I1", flk.iData1,
                                    flk.canvasSize, flk.canvasSize,
                                    flk.defaultFlickerDelay,
                                    flk.bGui.MIN_FLICKER_DELAY,
                                    flk.bGui.MAX_FLICKER_DELAY,
                                    true);
        flk.bGui.pImages.remove(oldi1IS);
        flk.bGui.pImages.remove(flk.i2IS);   
        flk.bGui.pImages.add(flk.i1IS);
        flk.bGui.pImages.add(flk.i2IS); 
        flk.pack();
        
        flk.i1IS.title= flk.imageFile1;        
        flk.i1IS.setObjPosition(-1, -1);  /* reset the position */
        flk.i1IS.setImgPosition(0, 0);
        flk.i1IS.drawImageTitle(); 
               
        flk.i1IS.paintSiCanvas();
        flk.repaint();
      } /* replace image */
        
      flk.activeImage= origActiveImage;   /* restore active image state */
      return(true);
    } /* change image 1 */
    
    else if("right".equals(flk.activeImage))
    { /* change image 2 */
      restoreImage(flk.iData2,reportErrorMsgsFlag);
      iDataNew= loadPixIntoImageData(newImageFileStr, flk.iData2, 
                                     2, /* imgNbr */
                                     reportErrorMsgsFlag);
      if(iDataNew==null)
      { /* Faied to read image */
        String msg= "Bad image data file ["+newImageFileStr+"]";
        util.showMsg2(msg, Color.red);
        util.popupAlertMsg(msg, flk.alertColor);
        flk.activeImage= origActiveImage;   /* restore active image state */
        return(false);
      }
      else if(iDataNew==flk.iData2)
      { /* replace image */
        flk.imageFile2= iDataNew.imageFile;
        String dbMenuName= util.getFileNameFromPath(flk.imageFile2);
        /* Add to the "FlkRecent" database if it is NOT a DEMO image */
        if(FlkDemo.lookupByFileName(flk.imageFile2,flk.imageFile2)!=-1)
        {
          FlkRecent fr= FlkRecent.addOrUpdate(dbMenuName,
                                              flk.clickableCGIbaseURL2,
                                              flk.imageFile2, "dbName",
                                              util.getCurDateStr());
        }
        
        /* Read the calibration files if it exists */
        readCalibFlag= util.readCalibrationFile(flk.iData2);
        if(readCalibFlag)
        { /* copy the calibration to active ImageData instance */
          flk.iData2.mapGrayToOD= flk.iData2.calib.getMapGrayToOD(); 
          flk.iData2.hasODmapFlag= flk.iData2.calib.getHasODmapFlag();
          util.showMsg("Read the image calibration file for: "+dbMenuName,
                       Color.black);
        }
        util.showMsg2("#bits/pixel="+flk.iData2.nBitsPerPixel+
                      " grayscale ["+flk.iData2.minG+":"+
                      flk.iData2.maxG+"] [WxH]=["+
                      flk.iData2.iWidth+"x"+flk.iData2.iHeight+"]", 
                      Color.black);
        util.showMsg2("Done", Color.black); 
        
        /* Regenerate the scrollable image */
        ImageScroller oldi2IS= flk.i2IS;
        flk.i2IS= new ImageScroller(flk, "right", "Image I2", flk.iData2,
                                    flk.canvasSize, flk.canvasSize,
                                    flk.defaultFlickerDelay,
                                    flk.bGui.MIN_FLICKER_DELAY,
                                    flk.bGui.MAX_FLICKER_DELAY,
                                    true);  
        flk.bGui.pImages.remove(flk.i1IS);
        flk.bGui.pImages.remove(oldi2IS);   
        flk.bGui.pImages.add(flk.i1IS);
        flk.bGui.pImages.add(flk.i2IS); 
        flk.pack();
        
        flk.i2IS.setObjPosition(-1, -1);  /* reset the position */
        flk.i2IS.setImgPosition(0, 0);
        flk.i2IS.title= flk.imageFile2;
        flk.i2IS.drawImageTitle();
        flk.i2IS.paintSiCanvas();
        flk.repaint();
      } /* replace image */
        
      flk.activeImage= origActiveImage;   /* restore active image state */
      return(true);
    } /* change image 2 */    
         
    return(false);
  } /* changeImageFromSpec */
  
  
  /**
   * restoreImage() - restore images from original copy (iPix) that was
   * first read in. This deletes the oImg and bgImg so that it displays the
   * original image. It does NOT restore the various parameters.
   * [TODO] there are some conditions where it may not restore correctly.
   * We need to document these conditions and fix it.
   * @param reportErrorMsgsFlag to report error it it occurs on load
   * @param iDataR is the image to restore. It should be either 
   *        flk.iData1 or flk.iData2
   */
  public synchronized void restoreImage(ImageData iDataR,
                                        boolean reportErrorMsgsFlag)
  { /* restoreImage */ 
    /* Test if need to save the .flk file if something changed. 
     * If so, ask them if they want to do it.
     */
    flk.checkIfSaveFlkFile();
    
    bGui= flk.bGui;
    /* [1] Reset to point to original image */
    if(iDataR==flk.iData1)
    { /* Image 1 */
      /* backup to original copy */
      if(flk.ixf1!=null)
        flk.ixf1= null;
      
      flk.i1IS.img_selectedFlag= false;
      
      if(iDataR.dwHist!=null)
        iDataR.dwHist.close();           /* kill histograms */
      iDataR.dwHist= null;
      if(iDataR.dwCalHist!=null)
        iDataR.dwCalHist.close();
      iDataR.dwCalHist= null;
      
      iDataR.resetImageData(false);
      util.gcAndMemoryStats("After Restore I1"); /* try to G.C. */
      iDataR.checkAndMakeIpix(reportErrorMsgsFlag); /* check & make iPix  */  
      flk.i1IS.drawImageTitle();
    } /* Image 1 */
    
    else if(iDataR==flk.iData2)
    { /* Image 2 */
      /* backup to original copy */
      if(flk.ixf2!=null)
        flk.ixf2= null;       
     
      flk.i2IS.img_selectedFlag= false;
      
      if(iDataR.dwHist!=null)
        iDataR.dwHist.close();           /* kill histograms */
      flk.iData2.dwHist= null;
      if(iDataR.dwCalHist!=null)
        iDataR.dwCalHist.close();
      iDataR.dwCalHist= null;
      
      iDataR.resetImageData(false);
      util.gcAndMemoryStats("After Restore I2"); /* try to G.C. */
      iDataR.checkAndMakeIpix(reportErrorMsgsFlag); /* check & make iPix  */ 
      flk.i2IS.drawImageTitle();
    } /* Image 2 */
    
    /* [2] Clean up other stuff */    
    flk.aff.initDefaultState();              /* remove affine data */
    
    /* [3] Set default flicker image */
    util.gcAndMemoryStats("After Restore images"); /* try to G.C. */
    iDataR.xObj= -1;
    iDataR.yObj= -1;
    iDataR.errStr= "";
    
    flk.activeImage= "both"; 
    flk.repaint();
  } /* restoreImage */
  
  
  /**
   * flushImageResources() - flush image resources of no-longer needed image.
   * Do this before garbage collect memory before create additional data 
   * See Chen&Lee Vol 2, pg 801-802 for freeing resources 
   * @param img is the image to flush
   * @return true if image was flushed.
   */ 
  public static synchronized boolean flushImageResources(Image img)
  { /* flushImageResources */
    if(img!=null)
    {
      img.flush();        /* free resources */
      return(true);
    }
    else
      return(false);
  } /* flushImageResources */
  
} /* End of class ImageIO */



/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
/*                 CLASS  FlkObserver                             */
/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */


/**
 * FlkObserver implements our own (rather than default) image observer
 */

class FlkObserver implements ImageObserver
{
  /** Global instances */
  Flicker
    flk;
  Util
    util;
  ImageIO
    imgIO;
  
    
  /**
   * FlkObserver() - Constructor
   */
  FlkObserver(Flicker flk, ImageIO imgIO)
  {
    this.flk= flk;
    this.imgIO= imgIO;
    this.util= util;
  }
  
    
  /**
   * imageUpdate() - called when loading reporting progress by printing "..."
   * Set by ImageObserver.
   * @param img is image to update
   * @param flags
   * @param x
   * @param y
   * @param w is image width
   * @param h is image height
   * @return true if succeed
   */
  public synchronized boolean imageUpdate(Image img, int flags,
                                          int x, int y, int w, int h)
  { /* imageUpdate */
    if((flags & ERROR) !=0)
    {
      imgIO.isLoadedFlag= true;
      flk.errorLoadingImageFlag= true;
      return(false);	/* don't come again */
    }
    else if((flags & (ERROR | FRAMEBITS | ALLBITS)) != 0)
    {
      imgIO.isLoadedFlag= true;
      flk.errorLoadingImageFlag= false;
      flk.readyFlag= true;
      String sWorking= "Completed reading image";
      imgIO.set_sWorking(sWorking);
      if(flk.CONSOLE_FLAG)
        util.showMsg(sWorking, Color.black);
      return(false);	                  /* all done!!! don't come again!*/
    }
    else
    { /* still reading it */
      String  sWorking= imgIO.get_sWorking();
      if(--ImageIO.count == 0)
      {
        sWorking += ".";	              /*  so can see it load */
        ImageIO.count= ImageIO.NLINES;	/* reset it */
      }
      imgIO.set_sWorking(sWorking);;
      if(flk.CONSOLE_FLAG)
        util.showMsg(sWorking,Color.black);
      return(true);
    }
  } /* imageUpdate */

  
}    /* End of class FlkObserver */
