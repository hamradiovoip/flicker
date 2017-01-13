/* File: Flicker.java */

import java.awt.*;
import java.awt.event.*;

import java.awt.image.*;
import java.util.*;
import java.io.*;
import javax.swing.*;

/* Flicker is a Java program to visually compare two gel (or other) images.
 * Class Flicker is the main class. The Flicker program is a Java 
 * application for comparing two images using flicker visual comparison.
 * It also contains some rudimentary image processing methods to adjust
 * the images to make the comparison easier. Images may be loaded either
 * from the user's local file system or from a URL over the Web.
 *
 *<PRE>
 * The program is in the process of being refactored from the old (nasty)
 * Java applet code on the http://www.lecb.ncifcrf.gov/flicker server
 * to a better maintaineded Java application that is available for
 * download on the http://www.lecb.ncifcrf.gov/Flicker server. When it
 * is more stable, the new application will migrate to the
 * http://open2dprot.sourceforge.net/Flicker server.
 *
 * Some of the architecture of the new program is described here:
 *
 * The program is controlled by menu selections, clicking and draging on
 * images and parameter sliders, and the use of keyboard shortcuts.
 *
 * Menu System
 * -----------
 * The program includes the following menus:
 *  File menu - to load/save the Flicker .flk state, load images, 
 *                  active map urls, update (from the server) program,
 *                  DB/Flk*DB.txt database files, demo images
 *  Edit menu - to change various defaults
 *  View menu - to change the display overlay options
 *  Landmark menu - to define landmarks for warping or other operations
 *  Transform menu - contains various image processing transforms
 *  Quantify menu - contains circle & boundary measurements, 
 *               ROI, OD calibrations
 *  Plugins menu - [FUTURE]
 *  Help menu - popup Web browser documentation on Flicker
 *
 * The .flk startup state file
 * ---------------------------
 * When you (File menu | Save (or SaveAs) state file), you save the
 * current state of Flicker including which images it was working on,
 * scroller parameter settings, etc. Normally these .flk files are saved
 * in the installation directory FlkStartups/ subdirectory.
 * If you have used the installer (ZeroG) for installing Flicker, then it
 * lets you click on a specific .flk you have previously saved to restart
 * it where you left off.
 *
 * Mouse control of images
 * -----------------------
 * 1. Pressing the mouse in an image (I1 or I2) selects it. If flickering
 *    is active, then it will move the flicker image center for the 
 *    selected image to that position. A little yellow "+" indicates the
 *    position you have selected. If the "[ ] Click to access DB" 
 *    checkbox is enabled, then it will request that the remote map
 *    database server try to identify the spot you have clicked on. 
 * 2. Dragging the mouse is similar to pressing it. However, only
 *    pressing it will invoke a clickable database.
 * 3. Control/Press will position the selected image so that the point
 *    you have clicked on will be in the center of the crosshairs.
 *    If you are near the edge of the image, it will ignore this request.
 * 4. Shift/Drag will invoke the brightness/contrast
 *    filter with minimum brightness and contrast in the lower left hand
 *    corner.
 *
 * Checkbox control
 * ----------------
 * 1. The "[ ] Flicker" checkbox enables/disables flickering
 * 2. The "[ ] Click to access DB" checkbox enables/disables access
 *    to a Web server that is associated with a clickable image DB
 *    if it exists for the selected image.
 * 3. The "[ ] Allow transforms" checkbox enables/disables using the
 *    image transforms. If it is not allowed, then use the initial
 *    input image.
 * 4. The "[ ] Sequential transforms" checkbox enables/disables using the
 *    last image transform output as input for the next image transform.
 *
 * Keyboard shortcut controls (C-key means Control key and other key)
 * ------------------------------------------------------------------
 * C-A add landmark (you must have selected both I1 and I2 trial objects)
 * C-B capture background gray value for current image
 * C-D delete landmark - the last landmark defined
 * C-E edit currently selected measured spot data if it exists.
 * C-F toggle flickering
 * C-G toggle displaying gray values in left or right image titles as 
 *     move cursor
 * C-H popup computing window ROI grayscale histogram
 * C-I popup prompt to define 'id' field for selected measured spot(s) if 
 *     it (they) exist. If clickable DB enabled, get data from protein web DB.
 * C-J Toggle between spot-list and trial-spot measurement mode
 * C-K delete currently selected measured spot if it exists.
 * C-L define the LRHC of the ROI in selected image from trial object.
 * C-M measure & show gray circular mask value for current image, report
 *     circular mask bkgrd corrected value. Alternative form: ALT-click
 *     to select and measure circular mask at one time.
 * C-P (undocumented) toggle the flk.dbugFlag
 * C-Q Stop lookup of annotation data from from proteomic Web server
 * C-R measure & show gray value for ROI computing window for current image,
 *     report circular mask bkgrd corrected value
 * C-T repeat last transform (if there was one)
 * C-U define the ULHC of the ROI in selected image from trial object.
 * C-V show the pixel window data at the current selected spot.
 * C-W clear the current ROI in selected image
 * C-Y if using the plasmaH/plasmaL demo image, then force 3 corresponding 
 *     landmarks for use with the Affine warp transform
 * C-Z if using the plasmaH/plasmaL demo image, then force 6 corresponding 
 *     landmarks for use with the Polynomial warp transform
 * C-Keypad '+' to increase canvas size
 * C-Keypad '-' to decrease canvas size
 *
 * Reporting the status
 * --------------------
 * 1. There are two status lines in the upper left of the main window.
 * 2. The selected image (clicking on the left or right image) turns its
 *    title to blue from black.
 * 3. A Flicker Report popup window is created when the program is started
 *    and it may be temporarily removed by closing it. You can get it back\
 *    at any time by selecting (View menu | Show report popup).
 *
 * Image Types
 * -----------
 * Currently, Flicker is able to read 4 types of images recognized by
 * their file extensions.
 *  1. Jpeg (.jpg) - read using Java image loader
 *  2. Gif (.gif) - read using Java image loader
 *  3. Tiff (.tif or .tiff) - read using TiffLoader class image loader 
 *     written by Jai Evans (CIT). It is not currently a full Tiff image
 *     reader. Tiff images are 8-bit up to 16-bit grayscale (NO color
 *     currently available). Images with more than 8-bits/pixel are scaled
 *     to 8-bits by the maximum value in the image.
 *  4. PPX (.ppx) - read Portable PiXture files used by the GELLAB-II
 *     System. We read .ppx files using the PpxLoader class  image reader.  
 * 
 * Image data model
 * ----------------
 * There are three viewable image canvases: "left" or I1 and "Right" or I2
 * which are always displayed on the lower part of the main window. An 
 * The Flicker window is created, but is only displayed when flicker
 * is enabled. These viewable images are instances of ImageScroller which
 * in turn contains a subclass ScrollableImageCanvas which extends canvas.
 * Only I1 and I2 have scrolling enabled. These three instances are 
 * flk.i1IS, flk.i2IS and flk.flkIS. A 4th variable flk.lastIS is set to
 * either flk.i1IS or flk.i2IS depending which is being displayed 
 * (determined by the run() method) when flicker is enabled. 
 *
 * The I1 and I2 canvases also have a title, a scrollable delay (mSec),
 * and a horizontal and a vertical slider for positioning the canvas
 * on part of the image.  Any of the short-cut keys used must be typed in
 * the selected I1 or I2 window.
 * 
 * The Images (iImg, oImg, zImg, bcImg) and iPix[] pixel data for images is
 * kept in instances of ImageData class (flk.iData1 and flk.iData2). The
 * image to appear in the flkIS window is set by assinging either iData1
 * or iData2 to the flkIS window. ImageData contains details about
 * the image data: # bits/pixel, height, width, blackIsZeroFlag, etc.
 * iPix[] is the original input image data.
 *
 * The output pixels oPix[] data is created in and is local to ImageXform
 * and is used to create iData.oImg after the transform is finished. 
 * If zooming is used, then then zImg is created from either the
 * (oImg or iImg - in that order). If brightness/contrast filtering is used,
 * then bcImg is computed from (zImg, oImg or iImg - in that order). The
 * display image is taken from (bcImg, zImg, oImg or iImg - in that order).
 *
 * Image transforms display model
 * ------------------------------
 * There are four display models usd in the image pipeline. These include
 * image transforms, zoom filtering, and brightness contrast. These are 
 * applied to the left and right windows and also are shown in the 
 * flicker window. Two checkboxes in the upper left of the main window 
 * control transforms: "Allow transform" enables/disables transforms, and 
 * "Sequential transforms" allows using the previous transform as the input 
 * to the next transform. The original image is iImg. If you allow 
 * transforms and are also composing image transforms, you may optionally
 * use the previous transformed output image (oImg) as input to the next 
 * image transform. The output1 (either iImg, or zImg) is then sent to the 
 * zoom transform (if the magnification is not 1X). The output2 is then
 * sent to the brightness-contrast (B-C) filter which you specify by 
 * dragging the mouse in the selected window with the SHIFT-key pressed.
 * If you have never used the zoom transform or brightness-contrast 
 * filtering, then these steps are omitted in the displayed image pipeline.
 *
 * a)  If no transforms, brightness-contrast filtering are used 
 *   on selected image
 *    iImg --> output
 * 
 * b) (Optional) sequential composition of image transforms on
 *   selected image 
 *               +----[compose]---+
 *               |                |
 *               |                |
 *               V   transform    |
 *      iImg --> +------------> oImg --> output1
 *  or
 *      iImg --------------------------> output1
 *
 * c) (Optional) zoom on selected image
 *                   zoom
 *   output1 --> +------------> zImg --> output2
 *  or
 *   output1 --------------------------> output2
 *
 * d) (Optional) brightness-contrast filter on output2 image. 
 *                  BC filter
 *   output2 --> +-----------> bcImg --> display
 *  or
 *   output2 --------------------------> display
 *
 * An image transform will map iData.iPix[] to flk.oPix[]. The image is
 * first selected by clicking on it and then a transform initiated by 
 * selected it from the Transform menu. Zoom images are saved in
 * iData.zPix[].
 *
 * The Brightness-Contrast uses class BrightnessContrastFilter which
 * extends RGBImageFilter. The Brightness/Contrast is set on a per image
 * basis using the SHIFT-(drag)(x,y) coordinates of the selected
 * image or by the Brightness and Contrast scroll bars.
 *
 * The zoom filter uses either: a) USE_AWT_ZOOM the Java zoom built into 
 * the drawImage() method, or b) USE_DE_ZOOM that uses the 
 * ImageXform.deZoom() transform to zoom or de-zoom the image. 
 *
 * Ganged controls
 * ---------------
 * Several scroll-related functions may be set to gang-change both the
 * left and right images together by the same amount. These include:
 * zooming, moving the images with CONTROL-press, and brightness-contrast. 
 *
 * Sliders for defining parameters used in transforms
 * --------------------------------------------------
 *  angle (degrees)              - for transform
 *  brightness (%)               - for B/C adjustment filter
 *  contrast (%)                 - for B/C adjustment filter
 *  eScale (%)                   - for transform
 *  threshold1 (grayscale or od) - for transform
 *  threshold2 (grayscale or od) - for transform
 *  zScale (%)                   - for transform
 *  zoomMag (X)                  - for zoom/de-zoom [1/10X : 10X]
 *
 * Region of Interest (ROI) defining a rectangular region
 * ------------------------------------------------------
 * Flicker lets you define a region of interest (ROI) associated with 
 * iData1 and a separate one for iData2. These are defined using the
 * (C-U) command after clicking on a spot to define the ULHC and
 * (C-L) command after clicking on a spot to define the LRHC. Once both
 * points are defined, the ROI iData.(roiX1,roiY1,roiX2,roiY2) is assigned
 * to the computing window iData.(cwx1,cwy1,cwx2,cwy2) that is then used
 * when making measurements (C-R). If you calibrate an ND wedge scanned
 * with the image, you would define the ROI for the wedge region. Then
 * when you do a (Quantify | Calibrate | Calibrate OD wedge), it assigns
 * the ROI to iData.calib.(ndcwx1,ndcwy1,ndcwx2,ndcwy2) that could be used
 * in calibrating the wedge.
 * 
 * Histogram of grayvalues under the ROI
 * --------------------------------------
 * You can create popup histograms for both the left and right images
 * for pixels inside the ROI associated with those images. 
 * 
 * Calibrating Gray scale in terms of OD or other measure
 * -------------------------------------------------------
 * You can calibrate both the left and right images by mapping pixel gray
 * values to corresponding calibration values such as optical density (OD)
 * counts/minute (CPM), etc. Normally, one would define a set of monotonic
 * values and then define corresponding gray values. If you have scanned
 * the image with a neutral density step wedge (that you know the 
 * corresponding OD values), then you can use 
 * (Quantify | Calibrate | Calibrate OD wedge) command to popup up
 * a calibration wizard. It uses the wedge ROI you have previously defined
 * that was saved to iData.calib.(ndcwx1,ndcwy1,ndcwx2,ndcwy2) to generate
 * a histogram. It then finds the peaks in the histogram and enters them
 * in an editable table of (od,gray-peak) values. The user then adds
 * or deletes peaks to correct any errors the peak-finder made. Then
 * when the user "Saves calibration", it saves it in the cal/*.cal file
 * for use next time the image is loaded.
 *
 * Alternatively, the peaks may be specifiable using circular mask 
 * measurements.
 *
 * Local DB files
 * --------------
 * Several tab-delimited (spreadheet derived) .txt files in the DB/
 * directory (located where the Flicker.jar file is located).
 * These DB/Flk*DB.txt files are read on startup and are used to setup 
 * the (File menu | Open ... image | ...) menu trees.
 *  DB/FlkMapDB.txt - contains instances of Web based active image maps
 *        with fields:
 *        (MenuName, ClickableURL, ImageURL, BaseURL, DatabaseName)
 *  DB/FlkDemoDB.txt - contains instances of pairs of images in the
 *        local Images/ directory and contains fields:
 *        (SubMenuName, SubMenuEntry, ClickableURL1, ImageURL1, 
 *         ClickableURL2,ImageURL2,StartupData)
 *  DB/FlkRecentDB.txt - contains instances of recently accessed
 *         non-demo images with fields:
 *        (DbMenuName, ClickableURL, ImageURL, DatabaseName, TimeStamp)
 * 
 * Local Images directory
 * ----------------------
 * The demo images are saved in the Images/ directory. If the user wants
 * to have Flicker discover subdirectories of their images, then copy
 * these subdirectories into Images/. When Flicker starts up, they will 
 * appear in the (File menu | Open user images) submenus.
 *
 * Files required
 * --------------
 * The following files are packaged in the distribution
 * 1. Flicker.jar
 * 2. jai_core.jar - from SUN's Java Advanced Imaging JDK
 * 3. jai_codec.jar - from SUN's Java Advanced Imaging JDK
 * 4. DB/Flk*DB.txt - tab-delimited database files
 * 5. Images/ - directory holding demo .gif and .ppx sample files
 * 6. FlkStartups/ - empty directory to put the startup files
 * 
 *</PRE>
 *<P>
 *<PRE>
 * This code was derived from FlkJ RCS: 5.11, Date: 1998/12/28 21:09:30 
 * and new code was added from MAExplorer (maexplorer.sourceforge.net).
 * The original flicker applet is described in several papers:
 * (1) Lemkin PF (1997) Comparing Two-Dimensional electrophoretic gels across 
 *     the Internet. <I>Electrophoresis</I>, 18, 461-470.<BR>
 * (2) Lemkin PF (1997) Comparing 2D Electrophoretic gels across Internet 
 *    databases. In 2-D Protocols for Proteome Analysis, Andrew Link (Ed), a 
 *    book in <U>Methods in Molecular Biology</U>, Vol. 112, Humana Press,
 *    Totowa, NJ, pp 339-410. <BR>
 * (3) Lemkin PF, Thornwall G (1999) Flicker image comparison of 2-D gel 
 *    images for putative protein identification using the 2DWG meta-database.  
 *    <I> Biotechnology</I> 12(2): 159-172.<BR>
 * (4) Lemkin PF, Thornwall G (2000) Comparing 2D Electrophoretic gels across 
 *    Internet databases. In 2-D Protocols for Proteome Analysis, --- (Ed), a 
 *    book in <U>Methods in Molecular Biology</U>, Vol. 112, Humana Press,
 *    Totowa, NJ, pp ---. <BR>
 *</PRE>
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
 * @see <A HREF=FLK_BASE_URL+"/Flicker">Flicker Home</A>
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.AdjustmentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.TextEvent;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.image.*;
import java.util.*;
import java.util.EventListener;
import java.net.*;
import java.lang.*;
import java.io.*;
import javax.swing.JComponent.*;
import javax.swing.*;

public class Flicker extends JFrame implements Runnable, ImageObserver, 
                                               WindowListener, ComponentListener
{ /* Flicker */
  
  /** Compiled Version constant for use in version comparison etc. */
  final static String
    VERSION= "V0.87.2-beta";  
  final static String
  DATE= "04-12-2007"; /* Previous major release "12-23-2005" */

  /* --- Debugging constants ...*/
  /** Debugging flag - Always TRUE */
  static boolean
    CONSOLE_FLAG= true;
  /** Debugging flag - Always FALSE */
  static boolean
    NEVER= false; 
  /** This is not available yet if it is set to -1,
   * for DEBUGGING, set it to 0. It is used in
   * both the menu generation and the messages for 
   * unimplemented code.
   */
  public int
    NOT_AVAIL_YET= -1;           /* 0 to allow all, -1 to disable */

  /** Enable guard region in the menu */
  public final boolean
    USE_GUARD= false;
  /** Enable plugins in the menu */
  public final boolean
    USE_PLUGINS= false;
  
  /** DEBUGGING Enable using "View Gang Brightness/Contrast" */
  public final boolean
    USE_GANG_BC= false;  
    
  /** DEBUGGING convert color RGB image to grayscale using NTSC algor. */
  public final boolean
    DBUG_NTSC_RGB2GRAY= false;  
  
  /** Enable using AWT implemented zoom only does positive integer zoom */
  public final boolean
    USE_AWT_ZOOM= false;
  /** Enable using de_zoom transform over 1/N to NX zoom (-N to +N) */
  public final boolean
    USE_DE_ZOOM= true;
    
  /** Enable pretty-printing GC Memory debugging */
  public final boolean
    GC_MEMORY_DBUG= false;  
  
  /* --- Misc. constants ---- */
  
  /** name of this operating system */
  public static String
    osName= System.getProperty("os.name");
  /** File separator */
  public static String
    fileSeparator= System.getProperty("file.separator");  
  /** User directory */
  public static String
    userDir= System.getProperty("user.dir")+fileSeparator;  
  
  /** User temporary "tmp/" directory used for misc. output
   * and SaveAs files.
   */
  public static String
    userTmpDir= userDir+"tmp"+fileSeparator; 
  /** User calibration "cal/" directory used for saving .cal files */
  public static String
    userCalDir= userDir+"cal"+fileSeparator; 
  /** User spot data "spt/" directory used for saving .spt files */
  public static String
    userSptDir= userDir+"spt"+fileSeparator; 
    
  /** Flicker server Base URL */  
  final public static String
    FLK_BASE_URL= "http://open2dprot.sourceforge.net/";
  /** Base URL of Flicker server containing documentation and resources */
  public static String
    baseFlkServer= FLK_BASE_URL+"/Flicker/";
  
  /** Popup HTML browser documentation for Flicker. */  
  final public static String
    FLICKER_HOME= baseFlkServer+"index.html",
    QUICK_START= baseFlkServer+"flkHome.html#quickStart",
    REF_MANUAL= baseFlkServer+"indexRefMan.html", 
    REF_MAN_KEYSTROKES= baseFlkServer+"flkRefMan.html#keyboardCmds", 
    REF_MAN_MOUSE= baseFlkServer+"flkRefMan.html#mouseCmds",  
    REF_MAN_SLIDERS= baseFlkServer+"flkRefMan.html#sliderCtls",  
    REF_MAN_CHECKBOX= baseFlkServer+"flkRefMan.html#checkboxCmds", 
    REF_MAN_MENUS= baseFlkServer+"flkRefMan.html#menuCmds", 
    REF_MAN_TRANSFORMS= baseFlkServer+"flkRefMan.html#transforms",   
    REF_MAN_SPOTLISTS_CREATE= baseFlkServer+"flkRefMan.html#ExampleSpotList",   
    REF_MAN_SPOTLISTS_ANNOTATE= baseFlkServer+"flkRefMan.html#ExampleSpotListAnnotation",   
    REF_MAN_PUTATIVESPOTID= baseFlkServer+"flkRefMan.html#putativeSpotLookup", 
    REF_MAN_UPDATING= baseFlkServer+"flkRefMan.html#UpdateCmds",
    REF_MAN_ADDUSERIMAGES= baseFlkServer+"flkRefMan.html#userImageData", 
    REF_VERSION_NBR= baseFlkServer+"FlickerJarVersion.txt",  
    VIGNETTES= baseFlkServer+"Vignettes/flkVignettesToc.html";    
    
  /** Popup HTML browser additional documentation for related to Flicker. */  
  final public static String
    WALKER_BOOK_CHAPTER_2005= "http://open2dprot.sourceforge.net/Flicker/PDF/Lemkin-ProteomicsProtocolsHandbook-279-306-2005.pdf",
    OLD_FLICKER_HOME= "http://www.lecb.ncifcrf.gov/flicker/index.html",	 
    EP97PAPER_FILE= "http://www.lecb.ncifcrf.gov/flicker/flkPaper.html",
    POSTER_FILE1= "http://www.lecb.ncifcrf.gov/flicker/poster.html",
    POSTER_FILE2= "http://www.lecb.ncifcrf.gov/flicker/ABRF97/index.html",
    POSTER_FILE3= "http://www.lecb.ncifcrf.gov/flicker/NCISEM97/index.html", 
    POSTER_FILE4= "http://www.lecb.ncifcrf.gov/flicker/NMHCC-dec98/index.html",
    //SUN_JAVA_LICENSE= "http://developer.java.sun.com/berkeley_license.html",
    SUN_JAI_LICENSE= "release-license-jai.html";
    
  /** Popup HTML browser additional documentation for finding 2D gels on 
   * the Internet
   */  
  final public static String
    SWISS_2DPAGE= "http://www.expasy.org/ch2d/index.html",
    WORLD_2DPAGE= "http://www.expasy.org/ch2d/2d-index.html",
    TWO_D_HUNT= "http://www.expasy.org/ch2d/2DHunt/index.html",
    GOOGLE_2D_SEARCH= "http://www.google.com/search?hl=en&lr=&ie=UTF-8&oe=UTF-8&q=%282D+or+2-D+or+two-Dimensional%29+gel+electrophoresis+database+and+identify&btnG=Google+Search";
    
  /** Popup HTML browser additional documentation for finding 2D gels on \
   * the Internet
   */  
  final public static String
    UNIPROT= "http://www.pir.uniprot.org/cgi-bin/upEntry?id=",
    IPROCLASS= "http://pir.georgetown.edu/cgi-bin/textsearch.pl?s_field=SWACCS&query=",
    IPROLINK= "http://pir.georgetown.edu/cgi-bin/biblio_search.pl?db=NREF&s_field=SWACCS&query=";
  
  /** default left image1 file */
  public static String
    IMAGE_FILE1= "Images"+fileSeparator+"plasmaH.gif";

  /** default right image2 file */
  public static String
    IMAGE_FILE2= "Images"+fileSeparator+"plasmaL.gif";
    
  /** normal color mapping, no change */
  final public static int
    NORM_COLOR= 1;		
  /** Pseudo color hsv2rgb mapping */
  final public static int
    PSEUDO_COLOR= 2;          
  /** RGB to gray color using .33*r+.5*g+.17*b */
  final public static int
    RGB_TO_GRAY_COLOR= 3;       

  /** # lines to compute before status report */
  final public static int
    LINES_REPORT_STATUS= 100;	
  /** 5 min. Max # seconds for keepalive */
  final public static int
    MAX_KEEPALIVE_TIME= 180;
  
  /** canvas size used in all canvas image windows */
  final public static int
    CANVAS_SIZE= 250;		            /* was 220 */	
  /** Minimum canvas size used in all canvas image windows */
  public static int
    MIN_CANVAS_SIZE= 150;
  /** Maximum canvas size used in all canvas image windows */
  public static int
    MAX_CANVAS_SIZE= 410;
  
  /** Canvas change increment size - empirically determined */
  public static int
    CANVAS_INCR= 10;
  
  /**  Frame height */
  final public static int
    FRAME_HEIGHT= 730;		          /* was 700 */
  /**  Frame width */
  final public static int
    FRAME_WIDTH= 780;	                  /* was 650 */
  /** Frame change increment size - empirically determined */
  public static int
    FRAME_INCR= 15;
  
  /** NOTE: set to 512 to enable if do image resizing */
  final public static int
    DEFAULT_TARGET_SIZE= 0;
  /** min target size if do initial image resizing */
  final public static int
    MIN_TARGET_SIZE= 400;	
  /** max target size if do initial image resizing */
  final public static int
    MAX_TARGET_SIZE= 1000;	        /* was 800 */
  
  /** maximum circle mask radius. filter is 2N+1, 7 is 15x15
   * 15 is 31x31, 25 is 51x51.
   */
  final static int
    MAX_CIRCLE_RADIUS= 25;
  /** maximum circle mask radius. A value of 0 is a single pixel */
  final static int
    MIN_CIRCLE_RADIUS= 0;
  /** The radius is used to determine the size of the
   * circle mask circleMask[nCirMask][nCirMask].
   * The value nCirMask = (2*radius+1). So a radius of 5 is 11x11,
   * etc.  A radius= 0 is a single pixel. 
   */
  public int 
    DEF_CIRCLE_MASK_RADIUS= 5;
  
  /** Maximum # of spots that can be measured/gel image 
   * and saved in .spt file
   */
  final static int
    MAX_SPOTS= 2000;
  
  /** --- Global class instances --- */
    
  /** Affine instance */
  public Affine 
    aff;   
  /** BuildGUI instance of user interface */
  public BuildGUI 
    bGui; 
  /** Calibrate OD instance */
  public CalibrateOD
    cal;
  /** get kbd event handler */
  public EventKbd
    ekb;
  /** instance of event scroller handler */
  public EventScroller
    evs;
  /* Event menu handler */
  public EventMenu
    evMu;  
  /** FileIO utilities */
  public FileIO
    fio;
  /** FlkDemo database */
  public FlkDemo
    fDemo;
  /** FlkUser database */
  public FlkUser
    fUser;
  /** FlkMap database */
  public FlkMap
    fm;
  /** FlkRecent database */
  public FlkRecent
    fRecent;
  /** Flicker image I/O and conversion functions */
  public ImageIO
    imgIO;
  /** image transform */
  public ImageXform
    ix;
  /** Info status region */
  public Info
    info;
  /** list of landmarks */
  public Landmark
    lms;	
  /** Plugin manager */
  public PluginMgr
    piMgr= null;
  /** poly warp spatial transform */
  public SpatialXform
    sxf;
  /** Spot structures */
  public Spot
    spt;
  /** Tiff image reader */
  public TiffLoader
    tr;
  /** misc utility methods */
  public Util
    util;
  /**  Window digital data dump */
  public Windump
    windmp;
  
  /* --- multiple instances of data object classes --- */
  
  /** last selected image canvas - used in an operation and is
   * selected by clicking on either throws left or right (I1 or I2) images.
   */
  public ImageScroller
    lastIS;			
  /** (non-scrollable) image canvas for drawing flicker images. When
   * flickering, this will switch between containing data from iData1
   * and iData2 depending on the flicker state variable showI1flag
   * that is toggled in the run() method.
   */
  public ImageScroller
    flkIS;			
  /** scrollable image 1 scrollable image (left) */
  public ImageScroller
    i1IS;
  /** scrollable image 2 scrollable image (right) */
  public ImageScroller
    i2IS;
  
  /** image 1 picture data associated with ImageScroller i1IS (left) */
  public ImageData
    iData1;	
  /** image 2 picture data associated with ImageScroller i2IS (right) */
  public ImageData
    iData2;			

  /** current image transform if any */
  public ImageXform
    imgXform;			
  /** image transform for image 1 if any */
  public ImageXform
    ixf1;			
  /** image transform for image 2 if any */
  public ImageXform
    ixf2;			

  /* -- Threads used for multiprocessing background image processing -- */
  /** Flicker thread */
  public Thread
    runT;
  /** Set to kill the run loop */
  private boolean
    notDoneFlag= true;
  /* --- State variables ---- */
  public String
    args[];
  
  /** current Flicker .flk state file that lives in "DB/". This may be the
   * initial startup file or may be the result of
   * "Open state file" or "SaveAs state file".
   */
  public String 
    flkStateFile= null;
  /** last IS that was clicked on*/
  public String
    lastISName= "";
  /* Startup date */
  public Date
    startupDate;
  
  /** Title of program */
  String
    title;
    
  /** active image "left", "right", "both" === was both */
  public String
    activeImage= "flicker";
  /** incremental loading message */  
  public String
    sWorking= "";
  	/** error string if not "" */
  public String
    errStr= "";		

  /** method to use when filter RBG in ImageScroller*/
  public int
    colorMode= NORM_COLOR;	
  
  /** Color to use for background of alert messages */
  public Color
    alertColor= new Color(255,182,179); /* pink-gray else Color.red */	
  /** save last message color state */
  public Color
    lastMsgColor= Color.black;	
  /** save last message state */
  public String
    lastMsgStr= "";		
  
  /** default name of popup window */
  public String
    popupWindowName= "FlkWind2"; 
  
  /** full path of image 1 (left) file or URL */
  public String  
    imageFile1;			
  /** full path of image 2 (right) file or URL */
  public String  
    imageFile2;			
  /** current transform */
  public String     
    xformName= "";		
    
  /** image font for overlay */
  public Font
    imageFont;  /*= new Font("TimesRoman",Font.PLAIN,10)*/
  /** text font for messages */
  public Font
    textFont= new Font("TimesRoman",Font.PLAIN,10); 
  
  /** flag to indicate that I1 is being displayed in flkIS else
   * I2 is being displayed.
   **/
  public boolean
    showI1flag= true;
  /** flag that finished transform 1 */
  public boolean
    finished1= false;
  /** finished transform 2 */
  public boolean
    finished2= false;
  /** test and set thread counter. Increment when transform completes
   * of it aborts. It is tested in the run() loop and cleared by
   * testAndSetThreadCounter(). */
  public int
    xformThreadCtr= 0;
  /** set when do valid affine xform */
  public boolean
    validAffineFlag= false; 

  /** set by ImageObserver imageUpdate() */
  public boolean
    errorLoadingImageFlag= false;
  /** if TRUE then display enter/exit info */
  public boolean
    displayInfoFlag= false;	
  /** doing the transform */
  public boolean
    doingXformFlag= false;
  /** Threads were suspended by stop() */
  public boolean
    isSuspendedFlag= false;	
  /** finished reading images. */
  public boolean
    readyFlag= false;			
  /** true if image loading error */
  public boolean
    imageLoadErrorFlag= false;		
  /** set true if run into problems when exiting and do not want
   * to write out the state as we may corrupt it.
   */
  public boolean
    abortFlag= false;
  /** Flag to stop spot list annotation lookup from proteomics Web server.
   * The flag is set by typing C-Q and tested in the lookup loop.
   */
  public boolean
    stopAnnotationUpdateFlag= false;

  /* ++++++ GUI view flags++++++ */
  /** view landmarks overlay */
  public boolean
    viewLMSflag= true; 
  /** view target overlay */
  public boolean
    viewTargetFlag= true; 
  /** view trial objects overlay */
  public boolean
    viewTrialObjFlag= true; 
  /** view boundary overlay */
  public boolean
    viewBoundaryFlag= false; 
  /** view ROI overlay */
  public boolean
    viewRoiFlag= true; 
  /** view measurement circle overlay */
  public boolean
    viewMeasCircleFlag= true;  
  
  /** view measurement location as 'circle'{annotation} overlay */
  public boolean
    viewDrawSpotLocCircleFlag= true;  
  /** view measurement location as '+'{annotation} overlay */
  public boolean
    viewDrawSpotLocPlusFlag= false;
  
  /** view measurement annotation as 'spot nbr' overlay */
  public boolean
    viewDrawSpotAnnNbrFlag= true;
  
  /** view measurement annotation as 'spot nbr' overlay */
  public boolean
    viewDrawSpotAnnIdFlag= false;
  /** view multiple popups */
  public boolean
    viewMultPopups= false; 
  /** gang scroll both images */
  public boolean
    viewGangScrollFlag= false; 
  /** gang zoom both images */
  public boolean
    viewGangZoomFlag= false; 
  /** gang brightnessContrast both images */
  public boolean
    viewGangBCFlag= false;
  /** view display image RGB as gray values */
  public boolean
    viewDispGrayValuesFlag= false; 
 /** first time to load img at start up, need to fix bug in ImgMeasure */
  public boolean
    firstTimeThruFlag= true;
  /** view normalized color */
  public boolean
    viewNormalizedColorFlag= false;
  /** view pseudo color display */
  public boolean
    viewPseudoColorFlag= false;
  /** RGB to gray color .33*r+.5*g+.17*b image display */
  public boolean
    viewRGB2GrayFlag= false;
  /** view Report popup window */
  public boolean
    viewReportPopupFlag= true;
  /* view guard Region Image */
  public boolean
    useGuardRegionImageFlag= false; 
  
  /** use NTSC RGB to grayscale when read in color image when
   * generate the iData.iPix[] pixel array.
   */
  public boolean
    useNTSCrgbTograyCvtFlag= false;
  
  /** Do spot measurement adding it to the spotList, protein lookup for that
   * spot, and popup current access server Web page in one operation when
   * the user clicks on a spot in an active image. The use must have
   * enabled one of the 
   *    (Edit | Select access to active DB server | ...)
   * options.
   * If doMeasureProtIDlookupAndPopupFlag is NOT set, the user must set the 
   * "Click to access DB" checkbox to have Flicker popup the browser. 
   */
  public boolean
    doMeasureProtIDlookupAndPopupFlag= false;
  
  /** Enable access to the Swiss-2DPAGE server for accessing spot data. */
  public boolean
    useSwiss2DpageServerFlag= false;
  /** Enable access to the PIR UniProt server for accessing spot data using the
   * previously looked up SWISS-PROT Accession Name Spot.id.
   */
  public boolean
    usePIRUniprotServerFlag= false;
  /** Enable access to the PIR iProClass server for accessing spot data using the
   * previously looked up SWISS-PROT Accession Name Spot.id.
   */
  public boolean
    usePIRiProClassServerFlag= false;
  /** Enable access to the PIR iProLink server for accessing spot data using the
   * previously looked up SWISS-PROT Accession Name Spot.id.
   */
  public boolean
    usePIRiProLinkFlag= false;
  
  /** Base URL for currently selected PIR server for accessing spot 
   * data using the previously looked up SWISS-PROT ID. It may be
   * UNIPROT, IPROCLASS, or IPROLINK.
   */
  public String 
   currentPRIbaseURL= UNIPROT;
  
  
  /** Use demo leukemia gels ND wedge calibration preloads.
   * If this flag is set PRIOR to loading the demo Leukemia gels,
   * It sets up the calibration (ROI, OD list) and then when you
   * invoke (Quantify | Calibrate | Calibrate OD by step wedge)
   * you get to practice editing the calibrating and then saving it.
   * NOTE: this switch is NOT saved in the state since it would 
   * wipe out the calibration next time it is read.
   */
  public boolean
    useDemoLeukemiaCalPreFlag= false; 
  
  /** Use threshold inside/outside filter [t1:t2] else outside of [t1:t2] */
  public boolean
    useThresholdInsideFlag= true;
  /** Save oImgs when do a "Save(As) state" if they exist and
   * allowXformFlag is enabled.
   */
  public boolean
    saveOimagesWhenSaveStateflag= true;
  /** Use protein DB browser, else lookup ID and name on active images.
   * For now, use popup Swiss-2DPAGE browser, else parse the data from
   * the web site.
   */
  public boolean
    useProteinDBbrowserFlag= true;
  
  /* ++++++ User values supplied from the GUI ++++++ */ 
   
  /** User enabled image as clickable DB checkbox to get DB entry */
  public boolean
    userClickableImageDBflag= false;  
  /** transforms are allowed */
  public boolean
    allowXformFlag= true;
  /** Replace(Preserve) orig imgs w/ xforms*/
  public boolean
    composeXformFlag= false;
  /** true if debug printout, set by GUI */
  public boolean
    dbugFlag= false;	 
  
  /** previous state of flickerFlag saved when start ain ImageXform*/
  public boolean
    prevFlickerFlag= false;		
  /** enable flickering, set by GUI also from <STATE> if set */
  public boolean
    flickerFlag= false;		
  
  /** <STATE> is clickable to get DB entry */
  public String
    clickableCGIbaseURL1= null;
  /** <STATE> is clickable to get DB entry */
  public String
    clickableCGIbaseURL2= null;
  /** <STATE> is clickable to get DB entry */
  public String
    clickableCGIbaseURL1pix= null;
  /** <STATE> is clickable to get DB entry */
  public String
    clickableCGIbaseURL2pix= null;
  /** flag set if currently selected image is clickable */
  public boolean 
    isClickableDBflag;

  /* --- GUI thresholds <STATE> --- */
  /** This is the working SliderState instance which will
   * be either the "base-state" or the I1 or I2 instance.
   */
  public SliderState
    curState;        	
  
  /** <STATE> default delay if not flickering */
  public int
    defaultFlickerDelay= BuildGUI.DEFAULT_DELAY;
 /** size for flicker canvas which does not include
  * scrollbars etc. This is derived from canvasSize.
 */
  public int
    flkCanvasSize; 
  /** size for left and right image canvases. The
   * flkCanvasSize is derived from this size.
   */
  public int
    canvasSize; 
  /** main frame width */
  public int
    frameWidth;
  /** main frame height */
  public int
    frameHeight;
  /** min (FRAME_WIDTH, screenWidth) */
  public int
    scrWidth;
  /** min (FRAME_HEIGHT, screenHeight) */
  public int
    scrHeight;
  /** allow dragging the frame to cause it to resize */
  public boolean
    resizeFrameFlag= false;
  
  /** WinDump data printing radix */
  public int
   winDumpRadix= Windump.SHOW_DECIMAL;
  /** WinDump window size in pixels*/
  public int
   maxColsToPrint= 20;
  
  /** "Use log of pixels if > 8-bits grayscale image" */
  public boolean
    useLogInputFlag= false;
  
  /** "Use sum density else mean" in measurements */
  public boolean
    useTotDensityFlag= true;
  /** "Use measurement counters" in measurements */
  public boolean
    useMeasCtrFlag= true;  
  /** 
   * C-J Toggle between list-of-spots else trial-spot measurement 
   * mode. This will set the overlay view to the measured list of spots. 
   * If this is set, then (C-M) pushes spots into the iData.spotList[] and
   * they can be displayed together on the screen as numbered
   * circles or "+" with optional spot id annotation. If it is NOT set,
   * then do NOT save spots in the spot list and treat C-M commands
   * as trial measurements.
   */
  public boolean
    spotsListModeFlag= true; 
  
  /** "background" circle radius. At the time the background measurement
   * is made, it is set to be the current measCircleRadius size. It is
   * only changed when the (C-B) command is used.
   */
  public int 
    bkgrdCircleRadius;
  /** "measurement" circle radius. This is set to the value from the
   * curState.measCircleRadius value.
   */
  public int 
    measCircleRadius;
  /** "measurement" circle mask of size nCirMask X nCirMask */
  public int 
    circleMask[][]= null;	 
  /** The radius determines the size of circle mask 
   * circleMask[nCirMask][nCirMask].
   * The value nCirMask = (2*radius+1). So a radius of 5 is 11x11,
   * etc.  A radius= 0 is a single pixel.
   */
  public int 
    nCirMask;	        	 

  /** position of the image for flickering */
  private Point
    imgPos;
  /** position of the object for flickering */
  private Point
    objPos;
  
  /** CountDown to determine if finished. 1 or 2 transforms */
  public int
    imagesToProcess;
  /** set to # of unique non-colinear affine transform */
  public int
    nbrUniqueAFT;		     

  /** co-linearity threshold for testing LMS */
  public float
    thrColinearity;
  /** for image 1 w.r.t landmarks */
  public float
    minLSQcolinearity1;		
  /** for image 2 w.r.t landmarks */
  public float
    minLSQcolinearity2;	
       
  /* adjustable object colors */
  public Color
    roiColor= Color.blue;   
  /** adjustable cross-hairs color  */
  static Color
    targetColor= Color.cyan;
  /** Adjustable trial object color */
  public Color
    trialObjColor= Color.orange;
  /** Adjustable landmark color */
  public Color
    lmsColor= Color.red;  
  /** Adjustable measurement circle color */
  public Color
    bkgrdCircleColor= Color.magenta;  
  /** Adjustable measurement circle color */
  public Color
    measCircleColor= Color.red;
 /** Adjustable guard region color */
  public Color
    guardRegionColor= Color.white;
  /** maximum # of external Fcts (10 or 20 is nice)*/
  final public static int
    MAX_EXTERN_FCTS= 3;		
  /** name of command */
  public String
    efName[]= new String[MAX_EXTERN_FCTS+1];
   /** what appears  in menu */
  public String
    efMnuName[]= new String[MAX_EXTERN_FCTS+1];
  /** function number  1 to MAX_EXTERN_FCTS */
  public int
    efNbr[]= new int[MAX_EXTERN_FCTS+1];
  /** xForm sync */
  public boolean
    repaintLockFlag= false;
  
  
  /**
   * Flicker() - constructor for Class Flicker
   */
  public Flicker()
  { /* Flicker */    
    title= "Flicker - " + VERSION+" "+DATE;
    System.out.println(title);
  } /* Flicker */


  /**
   * main() - for Flicker application
   * CMD LINE:    java Flicker [image1] [image2]
   * @param args is the command line arg list
   */
  public static void main(String args[])
  { /* main */    
    /* [1] Make new structures */
    Flicker flk= new Flicker();   /* top level Flicker class */     
    
    /* [2] Start up the Application */
    flk.init(args);		            /* create GUI */    
    
    /* [3] Run it */
    flk.start();		              /* Start the Flicker thread */    
   } /* main */

  
  /**
   * initState() - init Flicker state variables.
   */
  private void initState(String args[])
  { /* initState */
    this.args= args; 
         
    flkStateFile= (args.length>0 && args[0].endsWith(".flk"))
                     ? args[0] : null;      
          
    /* [1] Compute the maximimum screen size and initial frame size */
    scrWidth= Toolkit.getDefaultToolkit().getScreenSize().width;
    scrHeight= Toolkit.getDefaultToolkit().getScreenSize().height;
    scrWidth= Math.min(FRAME_WIDTH,scrWidth);
    scrHeight= Math.min(FRAME_HEIGHT,scrHeight);
    frameWidth= scrWidth;
    frameHeight= scrHeight;
    
     /* [2] Set the initial state variables */
    reInitStateVars();
                
    /* [3] Do additional initializations. Init External Functions */
    for(int i=0;i<=MAX_EXTERN_FCTS;i++)
    {
      efName[i]= null;
      efMnuName[i]= null;
      efNbr[i]= 0;
    }    
  } /* initState */
  
   
  /**
   * resetDefaultView() - Reset default state of views, colors, etc.
   * We also have to change the menu checkboxes so they are synced as well.
   */
  public void resetDefaultView()
  { /* resetDefaultView */
    readyFlag= false;            /* finished reading images. */    
    flickerFlag= false;	         /* TURN it off... */
    displayInfoFlag= false;      /* if true then display enter/exit info*/
    composeXformFlag= false;     /* Replace (reuse) orig images w/ xforms*/
    dbugFlag= false;
    userClickableImageDBflag= false; /* image is clickable to get DB entry */
     
    /* Default sizes */
    measCircleRadius= DEF_CIRCLE_MASK_RADIUS;	
    bkgrdCircleRadius= measCircleRadius;
    nCirMask= measCircleRadius;
    
    winDumpRadix= Windump.SHOW_DECIMAL;
    maxColsToPrint= 20;
    
    useTotDensityFlag= true;
    useMeasCtrFlag= true; 
    spotsListModeFlag= true; 
    
    /* Reset colors */
    roiColor= Color.blue; 
    targetColor= Color.cyan;
    trialObjColor= Color.orange;
    lmsColor= Color.red;  
    bkgrdCircleColor= Color.magenta;  
    measCircleColor= Color.red;
    
    useLogInputFlag= false;
  
    viewLMSflag= true;
    viewTargetFlag= true;
    viewTrialObjFlag= true;
    viewBoundaryFlag= false; 
    viewRoiFlag= true; 
    viewMeasCircleFlag= true; 
    
    viewDrawSpotLocCircleFlag= true;
    viewDrawSpotLocPlusFlag= false;
    viewDrawSpotAnnNbrFlag= true;
    viewDrawSpotAnnIdFlag= false;
    
    viewMultPopups= false;
    viewGangScrollFlag= false;
    viewGangZoomFlag= false; 
    viewGangBCFlag= false; 
    viewDispGrayValuesFlag= false;
    viewNormalizedColorFlag= false;
    viewPseudoColorFlag= false;
    viewRGB2GrayFlag= false;
    viewReportPopupFlag= true;
    useGuardRegionImageFlag= false;
    useNTSCrgbTograyCvtFlag= false;
    
    doMeasureProtIDlookupAndPopupFlag= false;
    useSwiss2DpageServerFlag= false; 
    usePIRUniprotServerFlag= false; 
    usePIRiProClassServerFlag= false; 
    usePIRiProLinkFlag= false;
    
    useDemoLeukemiaCalPreFlag= false; 
    
    useThresholdInsideFlag= true;
    saveOimagesWhenSaveStateflag= true;
    useProteinDBbrowserFlag= true;
    
    imageLoadErrorFlag= false;
    imagesToProcess= 0;	       /* none in progress */
    doingXformFlag= false;
    
    canvasSize= CANVAS_SIZE;
    flkCanvasSize= CANVAS_SIZE;
    frameWidth= scrWidth;
    frameHeight= scrHeight;
    setCanvasAndFrameSize(canvasSize, frameWidth,frameHeight);
    
    thrColinearity= BuildGUI.THR_COLINEARITY;
    
    xformName= "";	        /* default */
    lastIS= null;		/* last canvas - in new operation */
    activeImage= "both";
    
    /* Sync GUI checkboxes and menu checkboxes with the state which may
     * have changed.
     */
    if(bGui!=null)
      bGui.syncGuiWithState();    
  } /* resetDefaultView */
    
      
  /**
   * reInitStateVars() - reinitialize the state variables
   */
  public void reInitStateVars()
  { /* reInitStateVars */
    /* Reset default state of views, colors, etc. */
    resetDefaultView();
    
    curState= new SliderState();
    curState.init(this,"base-state");
        
    /* position of the image for flickering */
    imgPos= new Point(0,0);
    /* position of the object for flickering */
    objPos= new Point(0,0);       
    
    /* [1] Get the image file names from the command line. */
    if(args.length>=1)
      imageFile1= args[0];
    else
      imageFile1= IMAGE_FILE1;
    
    if(args.length>=2)
      imageFile2= args[1];
    else
      imageFile2= IMAGE_FILE2;
    
    /* [TODO] add code to interrogate each of the external classes */
    
    /* [TODO] re-evaluate usage and possibly modify or deprecate */
    clickableCGIbaseURL1= null;
    clickableCGIbaseURL2= null;
    clickableCGIbaseURL1pix= null;
    clickableCGIbaseURL2pix= null;    
  } /* reInitStateVars */
  

  /**
   * init() - init Flicker state variables.
   */
  public void init(String args[])
  { /* init */
    /* [1] Init Flicker state variables. */
    util= new Util(this);    /* set up global links */
    fio= new FileIO(this);     
    initState(args);
    
    /* [1.1] Read the base user preferences file it it exists */
    String flkPropFile= userDir+"Flicker.properties";
    boolean flkPropFlag= util.readBaseFlkPropertiesFile(flkPropFile);
    if(!flkPropFlag)
      System.out.println("Flicker.java:init(): Error: flk prop file not read!!");
    /* [2] setup initial startup data */ 
    startupDate= new Date(); /* for timing and data logging purposes */	
            
    /* [2.1] Setup other class instances */ 
    cal= new CalibrateOD(this,0);
    windmp= new Windump(this);
    imgIO= new ImageIO(this,null); 
    tr= new TiffLoader(false);
    spt= new Spot(this);
        
    /* [3] Setup new list of landmarks for Image 1 and Image 2.
     * The 0 param means use MAXLMS
     */
    lms= new Landmark(this,new Font("Helvetica", Font.PLAIN,10), 52);  
       
    /* [3.1] Must be after lms creation */
    ekb= new EventKbd(this);
    aff= new Affine(this);
    sxf= new SpatialXform(this);
    
    /* [4] Setup FlkMap database from "DB/FlkMapDB.txt" file.
     * These will be used to add a set of submenues in
     * (File | Open Map Image | <flkMap database>)
     */  
    fm= new FlkMap(this);          /* read "DB/FlkMapDB.txt" database */
    fDemo= new FlkDemo(this);      /* read "DB/FlkDemoDB.txt" database */
    fRecent= new FlkRecent(this);  /* read "DB/FlkRecentDB.txt" database */
    fUser= new FlkUser(this);      /* analyze Images/* directories and
                                    * setup the database of user images */
    
    /* [5] Setup the Plugin Manager */
    if(USE_PLUGINS)
      piMgr= new PluginMgr(this);
        
    /* [6] Create empty ImageData objects */
    Dimension xyDim= new Dimension(0,0);
    iData1= new ImageData("--1--", null, xyDim, false, 
                          null, this);
    iData2= new ImageData("--2--", null, xyDim, false,
                          null, this);
    
    /* [7] Build the GUI consisting of panels and image areas.
     * This will set up info, msg and other class instances.
     */
    this.setTitle(title);
    bGui= new BuildGUI(this,title);   
    
    /* [7.1] Make sure the other directories exist including:
     *   <userDir>"tmp/"
     *   <userDir>"cal/" 
     *   <userDir>"spt/" 
     */    
    File utfd= new File(userTmpDir);
    if(!utfd.isDirectory())
      utfd.mkdirs();              /* make it - should be there... */
    File calfd= new File(userCalDir);
    if(!calfd.isDirectory())
      calfd.mkdirs();             /* make it - should be there... */
    File sptfd= new File(userSptDir);
    if(!sptfd.isDirectory())
      sptfd.mkdirs();             /* make it - should be there... */    
             
    /* [8] Update show for popup report area window if enabled */
    String initRptStr= title+"\n"+
                       util.dateStr()+"\n"+
                       "Flicker report ...\n\n";
    
    bGui.pra= new ShowReportPopup(this, initRptStr, 17,60,
                                  "Flicker Report", "FlickerReport.txt",
                                  viewReportPopupFlag);    
    
    /* [9] Overload the images from the .flk file if the .flk file is OK.*/
    if(flkStateFile!=null)
    { /* read the state file to get the initial values */
      /* Don't report error messages yet. */
      if(!util.readFlkStateFile(flkStateFile,false))
      {
        util.fatalReportMsg(
                 "\n===> FATAL Problem reading initial Flicker state file\n"+
                 "["+flkStateFile+"]\n"+
                 "- exiting Flicker.\n\n",10000);
        flkStateFile= null;
        reInitStateVars();
      }
      else
      { /* Add this to the DB/FlkRecentDB.txt database */
        String 
          flkName= util.getFileNameFromPath(flkStateFile),
          dbMenuName= flkName.substring(0,flkName.length()-4),
          timeStamp= util.getCurDateStr();
        fRecent.addOrUpdatePair(dbMenuName, 
                                clickableCGIbaseURL1, clickableCGIbaseURL2, 
                                imageFile1, imageFile2, 
                                "Flk-Pair", timeStamp); 
      }  
    } /* read the state file to get the initial values */
                         
    /* [9.1] check if contains 1 or more clickable DB images
     * then set the isClickableDBflag. Also enable/disable
     * the "Clickable DB" checkbox depending on whether the data exists.
     */
     chkIfClickableDB(false);
   
    /* [10] Add event handlers */
    this.addWindowListener(this);    
    this.addComponentListener(this);
        
    /* [11] Set the initial frame size and enable display */
    setSize(frameWidth,frameHeight);
    setVisible(true);             /* map windows so visible */                
    resizeFrameFlag= true;        /* stops flickering of frame */
    
    /* Center frame on the screen, position report window on the screen */
    bGui.pra.positionReportWindow(); 
    firstTimeThruFlag= false;    
  } /* init */

  
  /**
   * start() - start new  Flicker threads
   */
  public synchronized void start()
  { /* start */
    if(runT==null)
    {
      runT= new Thread(this, "Flicker");
      runT.start();   
    }
    else
      if(isSuspendedFlag)
      {
        if(runT!=null)
          try
          {
            runT.start(); /* NOTE: do not use resume(), it has been deprecated */                
          }
          catch(IllegalThreadStateException e)
          {
            System.out.println("Flicker.java: start():Error with thread");
            e.printStackTrace();
          }
      }   
      else
        notify();    
  } /* start */


  /**
   * stop() - stop or suspend Flicker threads
   */
  public void stop()
  { /* stop */    
    /* [1] disable stuff */
    flickerFlag= false;	
    
    runT= null; /* do NOT use Thread.resume() or Thread.suspend()*/
    ixf1= null;       
    ixf2= null;
    util.setFlickerGUI(flickerFlag);
    
    /* [2] Suspend until do timeout - start may wake it up */    
    isSuspendedFlag= true;
  } /* stop */
  
  
  /**
   * run() - Flicker method to run threads and perform timing 
   * dependent operators. It first loads the images and then enters 
   * the thread based timing loop.
   */
  public void run()
  { /* run */    
    int delay= defaultFlickerDelay;    
    showI1flag= true;   /* startup with image I1 in flicker window */
    
    /* [1] Load image data */
    initialImageLoad();         /* load the initial images */        
    
    /* [2] Enable GUI operations now images are completely loaded. */
    bGui.setGuiEnable(true);
    /* Enable GUI Transforms menu operations */
    bGui.setTransformsEnable(true);
    util.showMsg("SELECT IMAGE - ready to Flicker or do transforms", Color.black);
    
    /* [2.1] Init the canvas size to the current setting. */
    changeCanvasSize(null);
    readyFlag= true;		        /* enable flags */
    doingXformFlag= false;
        
    /* [3] Main event loop to flicker etc.
     * Note: always set the outer loop (which has a sleep) to
     * a higher priority so transforms can be interrupted
     * to allow screen updates.
     */
    if(runT!=null)
      runT.setPriority(Thread.NORM_PRIORITY);
    
    repaint();
    
    while(runT != null)
    { /* flicker loop */           
            
      /* [3.1] If we did a stop() so just sleep and retest in a little while*/
      if(isSuspendedFlag)
      { /* we did a stop() so just sleep and retest in a little while */       
        try 
        {
	        Thread.sleep(500);     /* resolution = few milliseconds */
        } 
        catch (InterruptedException e)
        {
          System.out.println("run() thread problem");
          System.out.println(e);
        }
        continue;
      } /* we did a stop() so just sleep and retest in a little while */
                  
      /* [3.2] if flickering show iData1.?Img else iData2.?Img in 
       * the flkIS canvas where ?Img is either bcImg, zImg, oImg or iImg
       * in that order depending on the transform, zoom, and B-C status.
       */      
      if(flickerFlag)
      { /* do flicker stuff */
        /* [3.2.1] Toggle image and associated delays. */
        showI1flag= !showI1flag; /* Toggle the image to flicker */                
        delay= ((showI1flag) ? i1IS.getDelay() : i2IS.getDelay());   
                
        /* [3.2.2] Change the image and set the position of the image to
         * be copied into the flicker window.  It will then
         * call repaint() as required..
         */
        updateFlickerImage();
        if(dbugFlag)
        { /* ********** DEBUG ********* */
          System.out.println("***Flicker:run() canvas[" +
                             flkIS.title + "] @");
          System.out.println(" objPos= (" + objPos.x + "," + objPos.y + ")" );
          System.out.println(" imgPos= (" + imgPos.x + "," + imgPos.y + ")" );
          //util.sleepMsec(2000);
        } /* ********** DEBUG ********* */ 
      } /* do flicker stuff */          
      
      /* [3.2.3] Update status info string with possible Image?Xform data */
      if(doingXformFlag)
      { /* update status line */
        /* Update the State area on the GUI.
         * [TODO] why does it delay doing this when doing xform?
         * Almost seems like never called.
         * Could add a counter model to do it when overflows.
         */
        info.updateInfoString(); /* maybe don't do each time. */        
              
        String
          pcntMsg= ((ixf1!=null && ixf2!=null)
                      ? "Doing image(1,2) xform"
                      : ((ixf1!=null)
                           ? "Doing image1 Xform"
                          : ((ixf2!=null)
                                ? "Doing image2 Xform"
                                : null)));
        if(pcntMsg!=null)
          util.showMsg(pcntMsg,Color.magenta);
                                     
      } /* update status line */
            
      if(dbugFlag && xformThreadCtr>0)
        System.out.println("F-run() xformThreadCtr=" +
                           xformThreadCtr);            
        try 
        {
	        Thread.sleep(delay);    /* resolution = few milliseconds */
        } 
        catch (InterruptedException e)
        {
          System.out.println("run() thread problem");
          System.out.println(e);
        }
     
      /* [3.2.4] Process any finished transforms */
      if(xformThreadCtr>0)
        chkDoneWithTransform();     /* set when finish one of xforms*/
      
      delay= defaultFlickerDelay;	  /* reset delay for next time,
                                     * assuming no flickering */    
    } /* flicker loop */
    
    /* Note: when we get here - we exit! */
  } /* run */    
  
    
  /**
   * chkDoneWithTransform() - called by run() thread when xform completed
   * and the xformThreadCtr was incremented by softDoneWithTransform().
   * ImageXform operates on iData to either iData1 or iData2 and .
   * At that point we can create an image from the output iData.oImg and
   * repaint it.
   * Note: If composeXformFlag is set, then if we were using the
   * original image, it may have changed.
   * @see ImageXform#softDoneWithTransform
   */
  public synchronized void chkDoneWithTransform()
  { /* chkDoneWithTransform */     
    /* # of seconds to process last transform*/
    int runTimeMsec= 0;
    
    /* [1] Cleared flag when finish one of xforms*/
    xformThreadCtr--;   /* decrement once for each thread */
        
    /* [2] All done, swap the image to be displayed. */
    if(errStr!=null && ! errStr.equals(""))
    {
      util.popupAlertMsg(errStr, alertColor);
      errStr= "";	
    }
    
    if(validAffineFlag)
    {
      String sAffineMsg= aff.showAffineCalcs();
      util.appendReportMsg(sAffineMsg);
    }
    
    /* Setup images to flicker if turn on flickering */
    Date endDate= new Date();
    if(finished1 && ixf1!=null)
    {
      iData1.endTime= endDate.getTime(); /* msec since 1970 */
      iData1.runTimeMsec= (int)((iData1.endTime-iData1.startTime)/1.0F);
      iData1.runTimeMsec= runTimeMsec;    
      ixf1= null;                    /* So can G.C. */
      finished1= false;  	           /* reset it */    
    }
    else if(finished2 && ixf2!=null)
    {
      iData2.endTime= endDate.getTime(); /* msec since 1970*/
      iData2.runTimeMsec= (int)((iData2.endTime-iData2.startTime)/1.0F);
      iData2.runTimeMsec= runTimeMsec;   
      ixf2= null;                      /* So can G.C. */
      finished2= false;  	         /* reset it */    
    }
        
    /* [3] Do any processing required when all transforms are finished.
     * Note: imagesToProcess can be 0, 1 or 2 transforms actiave.
     */
    if(--imagesToProcess <= 0)
    { /* process finished all transforms */
      doingXformFlag= false;         /* Indicate that it is now OK to do
                                      * another xform. This prevents
                                      * multiple launches at same time */
    
      /* -- [DEPRICATE] Reenable flicker AFTER doing Xform. */
      util.setFlickerState(true);         
      if(errStr==null || "".equals(errStr))
        {
          String sMsg= "Finished transform[" + Math.abs(runTimeMsec) + 
                       " msec]";
          util.showMsg(sMsg, Color.black );
        }    
    } /* process finished all transforms */
    
    /* Try to G.C. */
    util.gcAndMemoryStats("After doXform");    
  
  } /* chkDoneWithTransform */
  
   
  /**
   * checkIfSaveFlkFile() - test if need to save the .flk file
   * If so, ask them if they want to do it.
   * @return true if no problems.
   */
  public boolean checkIfSaveFlkFile()
  { /* checkIfSaveFlkFile */ 
	if(iData1==null || iData2==null)
	  return(false);
	if(iData1.idSL==null || iData2.idSL==null)
	  return(false);
	
    if(iData1.idSL.changeSpotList || iData2.idSL.changeSpotList)
    { /* one of the spot lists changed - offer to save it */
      String msg=
        "The spot list changed. Do you want to save it with the .flk file?";
      PopupYesNoDialogBox
                 pyn= new PopupYesNoDialogBox(this,msg,"Yes","No","Cancel");
      
      abortFlag= pyn.cancelFlag;      /* get out now */
      
      if(pyn.okFlag)
      { /* Write a .flk state file */
        String
          initialPath= userDir+"FlkStartups"+fileSeparator+"FlkStartup.flk",
          newFlkStateFile= evMu.popup.popupFileDialog(initialPath,
                                                     "Write .flk state file",
                                                      false);
      if(newFlkStateFile==null || newFlkStateFile.length()==0)
        return(false);
      else
        flkStateFile= newFlkStateFile;
        
      if(!flkStateFile.endsWith(".flk"))
      {
        msg= "Flicker state file must have '.flk' file extension.";
        util.popupAlertMsg(msg, alertColor);
        return(false);
      }
        if(!util.writeFlkState(flkStateFile))
        {
          msg= "Problems saving Flicker state file: "+flkStateFile;
          util.popupAlertMsg(msg, alertColor);
          return(false);
        }
      }
    } /* one of the spot lists changed - offer to save it */
    
    /* clear the flags */
    iData1.idSL.changeSpotList= false;
    iData2.idSL.changeSpotList= false;
    
    return(true);
  } /* checkIfSaveFlkFile */ 
   
    
  /**
   * finalize() - total stop and kill Flicker threads.Write out
   * the "Flicker.properties" file to save user preferences.
   * NOTE: add other termination stuff here...
   */
  public void finalize()
  { /* finalize */    
    if(CONSOLE_FLAG && dbugFlag)
      System.out.println("DBUG: finalize()");
    
    /* Test if need to save the .flk file. If so, ask them if 
     * they want to do it. It will return true if there is nothing
     * to be saved OR the user did NOT want to save the .flk state.
     */    
    if(!checkIfSaveFlkFile())
    {
      /* If that is the case, don't write out the "Flicker.properties"
       * file either.
      */
      //return;
    }
    
    if(abortFlag)
    {
      abortFlag= false;
      return;
    }
      
    /* Always write out the properties */
    /* Write out  "Flicker.properties" file to save user preferences. */
    String flkPropFile= userDir+"Flicker.properties";
    util.writeBaseFlkPropertiesFile(flkPropFile);
    
    /* Write out "DB/FlkRecentDB.txt" file if the DB changed */
    if(fRecent.flkRecentChangedFlag)
      fRecent.write();
    
    runT= null; /* use null this instead of Thread.stop(); */
    ixf1= null;
    ixf2= null;
    
    this.setVisible(false);
    System.exit(0);
  } /* finalize */
  
      
  /**
   * initialImageLoad() - load the initial images when first enter run().
   * [NOTE] This code has a bug if the initial input images can not
   * be found and loadBlankImageData() is called. loadBlankImageData()
   * does not work.
   */
   private void initialImageLoad()
  { /* initialImageLoad */
    int timeToDelayWhileReadStatus= 1;  /* why is this here??*/
    boolean readCalibFlag= false;
    
    /* [1] Load image 1, if blank then create iData1
     * else use existing one 
     */
    if(iData1.imageFile.equals("--1--")) 
    {
      iData1= imgIO.loadPixIntoImageData(imageFile1, null,
                                         1, /* imgNbr */
                                         true); 
      if(iData1==null)
        iData1= iData1.loadBlankImageData("Image 1", DEFAULT_TARGET_SIZE,
                                          DEFAULT_TARGET_SIZE);
    }    
    readCalibFlag= util.readCalibrationFile(iData1);
    if(readCalibFlag)
    { /* setup the map */
      iData1.mapGrayToOD= iData1.calib.getMapGrayToOD();  /* setup map */
      iData1.hasODmapFlag= iData1.calib.getHasODmapFlag();
    }
    i1IS.setImageData(iData1);
    flkIS.setImageData(iData1);
    flkIS.setcurrentIS(i1IS);   
    
    if(imageFile1!=null)
    { /* get tht image scroller title without the path */
      String title1= util.getFileNameFromPath(imageFile1);
      i1IS.setTitle(title1,true);
    }
    
    i1IS.repaint();                  /* just repaint LEFT image */
    util.showMsg("Finished loading ["+imageFile1+"]", Color.black);
    util.showMsg2("#bits/pixel="+iData1.nBitsPerPixel+
                  " grayscale ["+iData1.minG+":"+
                  iData1.maxG+"] [WxH]=["+
                  iData1.iWidth+"x"+iData1.iHeight+"]",  
                  Color.black);
    util.sleepMsec(timeToDelayWhileReadStatus);
    
    util.gcAndMemoryStats("After load initial I1"); /* try to G.C. */    
    
    /* [2] Load image 2, if blank then create iData2
     * else use existing one 
     */
    if(iData2.imageFile.equals("--2--")) 
    {
      iData2= imgIO.loadPixIntoImageData(imageFile2, null,
                                         2, /* imgNbr */
                                         true);    
      if(iData2==null)
        iData2= iData2.loadBlankImageData("Image 2", DEFAULT_TARGET_SIZE,
                                          DEFAULT_TARGET_SIZE);
     }
    readCalibFlag= util.readCalibrationFile(iData2);
    if(readCalibFlag)
    { /* setup the map */
      iData2.mapGrayToOD= iData2.calib.getMapGrayToOD();  /* setup map */
      iData2.hasODmapFlag= iData2.calib.getHasODmapFlag();
    }
    i2IS.setImageData(iData2);
    
    if(imageFile2!=null)
    { /* get the image scroller title without the path */      
      String title2= util.getFileNameFromPath(imageFile2);
      i2IS.setTitle(title2,true);
    }    
    
    i2IS.repaint();                 /* just repaint RIGHT image */
    util.showMsg("Finished loading ["+imageFile2+"]", Color.black);
    util.showMsg2("#bits/pixel="+iData2.nBitsPerPixel+
                  " grayscale ["+iData2.minG+":"+
                  iData2.maxG+"] [WxH]=["+
                  iData2.iWidth+"x"+iData2.iHeight+"]",
                  Color.black);
    util.sleepMsec(timeToDelayWhileReadStatus);
    
    util.gcAndMemoryStats("After load initial I2");   /* try to G.C. */          
  } /* initialImageLoad */
   
   
  /**
   * changeCanvasSize() - change the canvas size by the specified
   * + or - increment command. If no increment command is used,
   * then just resize it to the current setting.
   * @return true if resize it. 
   */
   public boolean changeCanvasSize(String cmd)
   { /* changeCanvasSize */
     resizeFrameFlag= false;  /* disable resize */
     
     /* Popup up TextField to get new image map DB URL */
     int
       newCanvasSize= canvasSize,
       newFrameWidth= frameWidth,
       newFrameHeight= frameHeight;
     
     if(cmd!=null && cmd.equals("CanvasSize:+"))
     {
       newCanvasSize= canvasSize + CANVAS_INCR;
       newFrameWidth= frameWidth + FRAME_INCR;
       newFrameHeight= frameHeight + FRAME_INCR;
     }
     else if(cmd!=null && cmd.equals("CanvasSize:-"))
     {
       newCanvasSize= canvasSize - CANVAS_INCR;
       newFrameWidth= frameWidth - FRAME_INCR;
       newFrameHeight= frameHeight - FRAME_INCR;
     }
     
     boolean flag= setCanvasAndFrameSize(newCanvasSize,
                                         newFrameWidth,
                                         newFrameHeight);
     flkCanvasSize= newCanvasSize;
     
     return(flag);
   } /* changeCanvasSize */
   
   
  /**
   * setCanvasAndFrameSize() - change the canvas and frame sizes 
   * @param newCanvasSize
   * @param newFramWidth
   * @param newFrameHeight
   * @return true if resize it. 
   */
   public boolean setCanvasAndFrameSize(int newCanvasSize,
                                        int newFrameWidth,
                                        int newFrameHeight)
   { /* setCanvasAndFrameSize */  
     String msg= null;
     if(newCanvasSize<MIN_CANVAS_SIZE)
     {
       msg= "Can't change the canvas size to "+newCanvasSize+
             " which is too small";
       util.popupAlertMsg(msg, alertColor);
       resizeFrameFlag= true;
       return(false);
     }
     else if(newCanvasSize>MAX_CANVAS_SIZE)
     {
       msg= "Can't change the canvas size to "+newCanvasSize+
            " which is too large";
       util.popupAlertMsg(msg, alertColor);
       resizeFrameFlag= true;
       return(false);
     }
     
     canvasSize= newCanvasSize;
     flkCanvasSize= newCanvasSize;
     
     /* set the size on the canvasSizeLabel */
     if(bGui!=null)
     {
       String msgLabel= "Canvas size: " + newCanvasSize;
       bGui.canvasSizeLabel.setText(msgLabel);
     }
     /* set new sizes for each canvas */
     if(flkIS!=null)
       flkIS.setCanvasSize(canvasSize);
     if(i1IS!=null)
       i1IS.setCanvasSize(canvasSize);
     if(i2IS!=null)
       i2IS.setCanvasSize(canvasSize);
     
     /* frame size should change */
     frameWidth= newFrameWidth;
     frameHeight= newFrameHeight;
     if(bGui!=null)
     { /* adjust the GUI only if it exists! */
       setSize(frameWidth,frameHeight);
       
       if(dbugFlag)
         System.out.println("Flk.SCFS setSize="+canvasSize+
         " frameWidth="+frameWidth+
         " frameHeight="+frameHeight);
       /* resize it */
       pack();
       setVisible(true);
       
       if(bGui.userImagesProblemFlag)
       {
         msg= "You had too many images in Images/ subdirectories - "+
              "ignoring. Use fewer images per directory and restart Flicker.";
         util.popupAlertMsg(msg, alertColor);
       }
     } /* adjust the GUI only if it exists! */
     
     /* redraw the canvases if they exist */    
     doFullRepaint();
     
     resizeFrameFlag= true;
     
     return(true);
   } /* setCanvasAndFrameSize */
   
   
   /** 
    * doFullRepaint() - repaint flkIS, i1IS, i2IS and their canvases
    * as well as flk.repaint().
    */
   public void doFullRepaint()
   { /* doFullRepaint */                 
     if(flkIS!=null)
       flkIS.repaint();
     if(i1IS!=null)
       i1IS.repaint();
     if(i2IS!=null)
       i2IS.repaint();
     repaint(); 
   } /* doFullRepaint */   

  
  /**
   * updateFlickerImage() - update the flicker image.
   * Show iData1.?Img else iData2.?Img in the flkIS canvas where
   * ?Img is either bcImg, zImg, oImg or iImg in that order depending on the 
   * transform, zoom, and B-C status.
   */
  public void updateFlickerImage()
  { /* updateFlickerImage */
    int
      widthC,
      heightC;
            
    /* [1] Change the image and set the position of the image to
     * be copied into the flicker window.  It will then
     * call repaint() as required..
     */
    /* map flk image by mouse x,y coords to ULHC of image */
    if(showI1flag)
    { /* position flicker window a left I1 position */
      widthC= i1IS.preferredWidth;
      heightC= i1IS.preferredHeight;      
      Point pt= i1IS.getObjPosition();      
      flkIS.setObjPosition(pt);
      imgPos= i1IS.getObjPosition();
      flkIS.setcurrentIS(i1IS);
      //never
    //  System.out.println("updateFlickerImage()L:pt="+pt+" imgPos="+imgPos);
    }
    else
    {  /* position flicker window a right I2 position */
      widthC= i2IS.preferredWidth;
      heightC= i2IS.preferredHeight;
      Point pt= i2IS.getObjPosition();      
      flkIS.setObjPosition(pt);
      imgPos= i2IS.getObjPosition();
      flkIS.setcurrentIS(i2IS);
      //never
    //  System.out.println("updateFlickerImage()R:pt="+pt+" imgPos="+imgPos);
    }
    
    /* Set the ULHC northeast from (x,y) */
    objPos.x= -(imgPos.x - widthC/2);
    objPos.y= -(imgPos.y - heightC/2);
    
    /* Position the flicker image at objPos on its canvas */
    flkIS.setObjPosition(objPos);
    //never
   // System.out.println("updateFlickerImage()objPos="+objPos);
    
    /* Switch flicker image title */
    String
      fwName= (showI1flag)
                 ? util.getFileName(imageFile1)
                 : util.getFileName(imageFile2);
    flkIS.setTitle("Flicker Window " + fwName, true);
    
    /* Switch flicker image image and position */
    ImageData flkData= (showI1flag) ? iData1 : iData2;
   
    flkIS.setImageData(flkData);
    
    /* If flickering copy list of text objects to draw
     * (i.e., landmarks) from active I1 or I2 image to flkIS.
     */
    if(showI1flag)
      flkIS.setLandmarksTextListToDraw(i1IS.getNbrTextItems(), 
                                       i1IS.getColorText(),
                                       i1IS.getText(),
                                       i1IS.getFontText(),
                                       i1IS.getXText(),
                                       i1IS.getYText());
    else
      flkIS.setLandmarksTextListToDraw(i2IS.getNbrTextItems(),
                                       i2IS.getColorText(),
                                       i2IS.getText(),
                                       i2IS.getFontText(),
                                       i2IS.getXText(),
                                       i2IS.getYText());
      
    /* [2] Update status info string with possible Image?Xform data */
    if(doingXformFlag)
    { /* update status line */
      /* Update the State area on the GUI.
       * [TODO] why does it delay doing this when doing xform?
       * Almost seems like never called.
       * Could add a counter model to do it when overflows.
       */
      info.updateInfoString(); /* maybe don't do each time. */
      
      String
        pcntMsg= ((ixf1!=null && ixf2!=null)
                  ? "Doing image(1,2) xform"
                  : ((ixf1!=null)
                       ? "Doing image1 Xform"
                       : ((ixf2!=null)
                             ? "Doing image2 Xform"
                             : null)));
                             
      if(CONSOLE_FLAG && ixf1!=null)
        System.out.println("FLK-UFI ixf1.nameLR="+ixf1.nameLR+
                           " imagesToProcess="+imagesToProcess+
                           " doingXformFlag="+doingXformFlag+
                           " ixf1.useXform="+ixf1.useXform);
      if(CONSOLE_FLAG && ixf2!=null)
        System.out.println("FLK-UFI ixf2.nameLR="+ixf2.nameLR+
                           " imagesToProcess="+imagesToProcess+
                           " doingXformFlag="+doingXformFlag+
                           " ixf2.useXform="+ixf2.useXform);
      if(pcntMsg!=null)
        util.showMsg(pcntMsg,Color.magenta);
    } /* update status line */
                        
    if(flickerFlag)
      flkIS.repaint();
  } /* updateFlickerImage */
  
    
  /**
   * testAndSetThreadCounter() -  safe test and set of thread counter
   */
  public synchronized void testAndSetThreadCounter()
  { /* testAndSetThreadCounter */
    xformThreadCtr++;
    if(dbugFlag)
      System.out.println("F-TASTC xformThreadCtr=" +
                         xformThreadCtr);
  } /* testAndSetThreadCounter */     
  
  
  /**
   * chkIfClickableDB() - check if currently selected image is a
   * clickable active map data base in that it has a valid url.
   * Then set the isClickableDBflag. Also enable/disable
   * the "Clickable DB" checkbox depending on whether the data exists.
   * @param showMsgFlag to also report a message if true
   * @return true if clickable
   */
  public boolean chkIfClickableDB(boolean showMsgFlag)
  { /* chkIfClickableDB */
    isClickableDBflag= false;        /* if image supports active map */
    
    if(activeImage.equals("left"))
    {
      if(clickableCGIbaseURL1!=null &&
         clickableCGIbaseURL1.length()>0)
        isClickableDBflag= true;
      iData1.isClickableDBflag= isClickableDBflag;
      bGui.clickableCheckbox.setSelected(iData1.userClickableImageDBflag);
    }
    else if(activeImage.equals("right"))
    {
      if(clickableCGIbaseURL2!=null &&
         clickableCGIbaseURL2.length()>0)
        isClickableDBflag= true; 
      iData2.isClickableDBflag= isClickableDBflag;
      bGui.clickableCheckbox.setSelected(iData2.userClickableImageDBflag);
    }

    if(bGui.clickableCheckbox!=null)
    {      
      bGui.clickableCheckbox.setEnabled(isClickableDBflag); 
    }
    
    if(showMsgFlag)
    { /* report it */
      String
        sMsg= (isClickableDBflag)
                ? "Image is a clickable database image"
                : "Image is NOT a clickable database image";
      if(isClickableDBflag)
        sMsg += (userClickableImageDBflag)
                  ? ", access enabled"
                  : ", access disabled";
        util.showMsg(sMsg, Color.black);
    }
        
    return(isClickableDBflag);
  } /* chkIfClickableDB */ 
         
  
  /**
   * lookupCurrentImageScroller() - lookup the current active ImageData 
   * if it is left or right
   * @return either flk.iData1 or flk.iData2, else null if not selected.
   */
  ImageScroller lookupCurrentImageScroller()
  { /* lookupCurrentImageScroller */
    if(activeImage==null)
      return(null);
    else if(activeImage.equals("left"))
      return(i1IS);
    else if(activeImage.equals("right"))
      return(i2IS);
    else
      return(null);
  } /* lookupCurrentImageScroller */
  
  
  /**
   * lookupCurrentImageData() - lookup the current active ImageData 
   * if it is left or right
   * @return either flk.iData1 or flk.iData2, else null if not selected.
   */
  ImageData lookupCurrentImageData()
  { /* lookupCurrentImageData */   
    if(activeImage==null)
      return(null);
    else if(activeImage.equals("left"))
      return(iData1);
    else if(activeImage.equals("right"))
      return(iData2);
    else
      return(null);
  } /* lookupCurrentImageData */

  
  /**
   * paintComponent() - paint Flicker
   * @param g is graphics context
   */
  public void paintComponent(Graphics g)
  { /* paintComponent */
    /* [1] Update the landmark overlays if needed. */
    /* redraw the LMS one time - not every time unless need to repair */
    util.updateLMSvaluesInImages();    
    
    /* [1.1] Repaint each of the top level image canvases if they exist.
     * Note: must use wait() since they could be null due to threads!
     */  
    while(i1IS==null)
      try
      {
        wait(10);
      }
      catch(InterruptedException e)
      {
        System.out.println(e);
      }       
     while(i2IS==null)
      try
      {
        wait(10);
      }
      catch(InterruptedException e)
      {
        System.out.println(e);
      } 
           
    /* [2] Update the landmark overlays if needed. */
    /* redraw the LMS one time - not every time unless need to repair */    
    util.updateLMSvaluesInImages();    
    i1IS.paintSiCanvas();  
    i2IS.paintSiCanvas();  

  } /* paintComponent */
  
  public void windowClosing(java.awt.event.WindowEvent windowEvent)
  {  finalize(); }  
  
  
  public void windowClosed(java.awt.event.WindowEvent windowEvent) {   }
  public void windowActivated(java.awt.event.WindowEvent windowEvent) {  }
  public void windowDeactivated(java.awt.event.WindowEvent windowEvent) {  }  
  public void windowDeiconified(java.awt.event.WindowEvent windowEvent) {  }  
  public void windowIconified(java.awt.event.WindowEvent windowEvent) {  }  
  public void windowOpened(java.awt.event.WindowEvent windowEvent) {  }  
  public void componentHidden(java.awt.event.ComponentEvent componentEvent) { }  
  
  public void componentMoved(java.awt.event.ComponentEvent componentEvent) { }  
  public void componentShown(java.awt.event.ComponentEvent componentEvent) { }
     
  
  /**
   * componentResized() - resize canvases to proper sizes when frame is resized.
   * [TODO]:
   * @param componentEvent event
   */
  public void componentResized(java.awt.event.ComponentEvent componentEvent) 
  { /* componentResized */
    if(NEVER)
    {
      if(resizeFrameFlag)
      { /* do the resize */
        Dimension d = this.getSize();
        float ratio= (float)((float)CANVAS_SIZE/(float)FRAME_WIDTH);
        int newCanvasSize= (int)(d.width * ratio);
        if(CONSOLE_FLAG)
          System.out.println("Size of frame changed width="+
                             d.width+" h="+d.height+
                             " newCanvasSize= "+newCanvasSize);        
        canvasSize= newCanvasSize; 
        /* set new sizes for each canvas */
        flkIS.setCanvasSize(newCanvasSize);
        i1IS.setCanvasSize(newCanvasSize);
        i2IS.setCanvasSize(newCanvasSize);
        resizeFrameFlag= false;
        pack();
      } /* do the resize */
    }
  } /* componentResized */
  
} /* End of class Flicker */
