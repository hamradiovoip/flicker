/* File: DrawHistogram.java */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.EventListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent.*;
import javax.swing.*;

/** The DrawHistogram class is used to draw an image intensity histogram
 * in a popup frame. It is used for both the a) ROI region intensity
 * histogram, and b) the ND step wedge calibraion (grayscale to OD)
 * histogram. It is created on a per image (left or right) basis.
 *<PRE>
 * NOTES:
 * =====
 * - The this.calibHistFlag indicates if it is a calibration or ROI
 *   histogram.
 * - The instance lives in iData[12].dwHist and in iData[12].dwCalHist.
 * - The histogram lives in iData.hist[] and is computed by 
 *   iData.calcHistogramROI() and iData.calcHistogram()
 * - The (Quantify | Region Of Interest | Show ROI histogram (C-H))
 *   creates the histogram and computes the ROI data.
 *   i.e., (Quantify | Region of interest (ROI) | Capture measurement
 *   by ROI (C-R)) will also update the histogram if it exists.
 * - Popup the calibration histogram from the menu
 *   (Quantify | Calibrate | Optical density).
 * - There are different menu buttons in this popup depending on the
 *   functionality: either computing window ROI histogram or calibration
 *   (ND step wedge) histogram.
 *</PRE>
 *<PRE>
 * The ND step wedge or spot-list grayscale to OD calibration procedure
 *============================--------------============================
 * If a cal/{imageFile}.cal file exists when the image is loaded into
 * flicker, it was read in at that time. Then when the Calibrate OD
 * histogram was started, it uses those values in the Calibration Peaks
 * table. Otherwise, the table will be null.
 * NOTE: if you are using one of the four demo Leukemia gels, it will
 * preload the OD values for demonstration purposes. 
 * 
 * 1. If you are using the step wedge, define a well-define ROI region 
 *    around the ND step wedge in the image using C-U (ULHC) and C-L
 *    (LRHC). Then compute the histogram using C-R.
 * 1.1 If instead, you are using the getPeaksByMeasFlag option, then do 
 *    NOT import the ROI histogram, but instead get the data from the 
 *    iData.spotList[] to save in a new histogram and the gray-peaks
 *    table as the mean spot values.
 * 2. If there are no OD data in the peaks table, then enter the OD 
 *    calibration values into the red Optical Density TextFields. [If the
 *    calibration was set previously and we read them in from the .cal 
 *    file, then preset these fields.] Also preset the corresponding 
 *    grayscale Gray-peak TextField values if you know what the values are.
 *    Special hack: If the current gel is not calibrated and the other
 *    gel has a calibration step list calib.ndWedgeODvalues[0:maxNDsteps-1]
 *    for maxNDsteps>0, then use it to save time typing it in.
 * 3. To force it to analyze the ROI wedge area, click on the
 *    "Analyze wedge ROI" button. This recomputes the histogram on
 *    the ROI (which may have changed) and the recomputes the calibration
 *    curve. 
 * 4. This will also update the Calibration Peak Table and generate the
 *    extrapolated mapGrayToOD[] calibration. The new histogram will show
 *    the Gray-peak values cooresponding to the OD values with red tick
 *    marks on the peaks. 
 * 4.1 It calls calib.findPeaks() to find the peaks and copies the peaks 
 *    into the Calibration Peak Table Gray-peak TextFields.
 * 4.2  It calls calib.extrapolateNDwedge() to compute calib.mapGrayToOD[] 
 * 4.3 It redraws the histogram plot with
 *     a) overlay calibration curve calib.mapGrayToOD[]
 *     b) Peaks table of (OD values, peak gray values)
 * 5. You can edit the peak list, by selecting a peak with the mouse and
 *    then pressing either throws "Add peak" or "Delete peak" button in 
 *    which case it redoes steps 4 through 4.3.
 * 6. When you are done editing you can save the calibration by pressing
 *    the "Save calibration state" button. This saves the calibration data
 *    in the cal/{imageFile}.cal file and copies the calib.mapGrayToOD[] 
 *    to the iData instance which you can then use for making calibrated
 *    measurements.
 * 7. Press "Done" to exit the calibration wizard.
 *</PRE>
 * This work was produced by Peter Lemkin of the National Cancer
 * Institute, an agency of the United States Government.  As a work of
 * the United States Government there is no associated copyright.  It is
 * offered as open source software under the Mozilla Public License
 * (version 1.1) subject to the limitations noted in the accompanying
 * LEGAL file. This notice must be included with the code. The Flicker 
 * Mozilla and Legal files are available on 
 * http://open2dprot.sourceforge.net/Flicker.
 *<P>
 * @author P. Lemkin (NCI), G. Thornwall (SAIC), NCI-Frederick, Frederick, MD
 * @version $Date$   $Revision$ 
 * @see <A HREF="http://open2dprot.sourceforge.net/Flicker">Flicker Home</A>
 * @see ShowHistPopup
*/



class DrawHistogram extends JFrame
      implements ActionListener, MouseListener, MouseMotionListener,
                 WindowListener

{
  
  /* --- plot types used in various plots ---- */
  /** # gray values in a pixel */
  final static int
    MAX_GRAY= CalibrateOD.MAX_GRAY; 
  /** # of histogram into bins */
  final static int
    MAXBINS= MAX_GRAY+1;
  
  /** Draw histogram mode - draw CW ROI histogram */
  final static int
    MODE_CW_ROI_HIST= 1;
  /** Draw histogram mode - draw calibration ND wedge histogram 
   * based on ND ROI region.
   */
  final static int
    MODE_CALIB_ND_ROI_HIST= 2;
  /** Draw histogram mode - draw calibration histogram based on
   * spot list data.
   */
  final static int
    MODE_CALIB_SPOTLIST_HIST= 3;
  
  
  /** default frame size width of popup */
  final static int
    ROI_POPUP_WIDTH= 512;           
  /** default frame size height of popup */
  final static int
    ROI_POPUP_HEIGHT= 512;             
  /** default frame size width of popup */
  final static int
    CAL_POPUP_WIDTH= 820;         
  /** default frame size height of popup */
  final static int
    CAL_POPUP_HEIGHT= 520;             
  /** default # of initial rows in the peak wedge calibration table */
  final static int
    N_PCT_PROLOGUE= 3;
  /** default # of peak wedge calibration table steps */
  final static int
    MAX_ND_STEPS= CalibrateOD.MAX_ND_STEPS;
  /** default # of editPanel rows
   */
  final static int
    PEAK_CAL_TBL_ROWS= (N_PCT_PROLOGUE + MAX_ND_STEPS);
  
  /** default # of editPanel cols */
  final static int
    PEAK_CAL_TBL_COLS= 4;

  /** link to global Flicker instance */
  private static Flicker 
    flk;                              
  /** link to global CalibrateOD instance */
  private static Util
    util;  
  
  /** link to working data related CalibrateOD instance */
  private CalibrateOD
    calib;   
  /** Backup copy of the initial CalibrateOD instance that can
   * be used for restoring the calibration.
   */
  private CalibrateOD
    calibBKUP;                         
  /** link to data related ImageData instance */
  private ImageData
    iData;                        
    
  /** current image file name */
  private String
    imageFile;
  /** active image name is "left" or "right" */
  private String
    activeImage;
   
  /** The histogram mode is either:
   * MODE_CW_ROI_HIST, MODE_CALIB_ND_ROI_HIST, or MODE_CALIB_SPOTLIST_HIST
   */
  private int
    histMode;
  
  /** The histogram is a calibration histogram */
  private boolean
    calibHistFlag;
  
  /** The calibration peak table changed */
  private boolean
    calibChangedFlag;
 
  /** Get the peaks from the circular mask measurement spot list using
   * the mean circle measurement grayscale values not corrected
   * for background (since we do not know what background is).
   * Otherwise the ND step wedge histogram peak analysis is used.
   */
  private boolean
    getPeaksByMeasFlag;
  
  /** size of frame */
  Dimension
    frameSize;
  /** working canvas width */
  private int
    cWidth;
  /** working canvas height */
  private int
    cHeight;
  
  /** full Gif file path name */
  private String
    oGifFileName;
  /** current canvas title */
  private String
    title;               
  /** vertical caption */
  private String
    vertCaption;                  
  /** horizontal caption */
  private String
    horizCaption; 
  /** extra sample data */
  String
    r1= null;
  /** extra sample data */
  String
    r2= null;
  /** extra sample data */
  String
    r3= null;
  	        
  /** active bin idx [0:nHist-1] else -1 */
  int
    activeBin= -1;    		
  /** freq hist[0:nHist-1].*/
  private int
    hist[];		  		
  /** modified freq sHist[0:nHist-1].*/
  private int
    sHist[];		
    
  /** set if draw plot to GIF file*/
  private boolean
    drawIntoImageFlag= false;					   
  
  /* --- GUI --- */
  /** small font used for labeling */
  private Font
    smallFont;
  /** medium font used for labeling */
  private Font
    mediumFont;
  /** medium font used for labeling */
  private Font
    largeFont;
  /** Font family */
  private String
    fontFamily;  
  /** font size for peak number labels */
  private int
    peakNbrFontSize= 10;
  
  /* light white that is different from background color */
  private Color
    lightWhite;
  /* optimal background color */
  private Color     
    optBkgrdColor; 
  /* Input TextField color */
  private Color    
    textInputColor;
  /* histogram drawing color */
  private Color    
    histColor;
  /* histogram top drawing color */
  private Color    
    histTopColor;
  /* OD curve drawing color */
  private Color    
    odColor; 
  /** calibration peaks on histogram color */
  private Color
    calibPeakColor;
  /* text labeling color */
  private Color    
    labelColor;
  /** peak number color */
  private Color
    peakNbrColor;
    
  /** Array of TextFields used in constructing the editable 
   * Peak Calibration Table.
   */
  private Panel
    editPanel;
  /** Array of TextFields used in constructing the
   * Peak Calibration Table editPanel. Not all entries are used.
   * It is of size [0:PEAK_CAL_TBL_ROWS-1][0:PEAK_CAL_TBL_COLS-1].
   */
  private TextField
    peakCalTblTF[][];  
  
  /** status Label */
  private Label
    statusLabel;
  
  /** active hit coordinates */
  private static int
    xPainted[];
  /** active y hit coordinates */
  private static int
    yPainted[];
  /** If the Histogram is visible */
  public boolean
    isVisibleFlag;
   
  
  /**
   * DrawHistogram() - create a histogram plot for left or right
   * selected image. If the image is not selected, it is not valid
   * and the this.activeImage is set to null.
   * The histogram data is in iData.hist[] and depends on the
   * left or right image being selected. If calibHistFlag is
   * set, then present a calibration histogram GUI.
   * @param flk is the Flicker instance
   * @param title is window title
   * @param horizCaption is horizontal caption
   * @param vertCaption is vertical caption (generally frequency)
   * @param histMode is either MODE_CW_ROI_HIST, MODE_CALIB_ND_ROI_HIST,
   *        or MODE_CALIB_SPOTLIST_HIST
   */
  public DrawHistogram(Flicker flk, String title, String horizCaption,
                       String vertCaption, int histMode)
  { /* DrawHistogram */    
    this.flk= flk;  
    util= flk.util;
       
    /* Verify valid ImageData */
    if(flk.activeImage.equals("left"))
      iData= flk.iData1;
    else if(flk.activeImage.equals("right"))
      iData= flk.iData2;
    else
    {      
      activeImage= null;
      String msg= "Select left or right image to analyze first.";
      util.popupAlertMsg(msg, flk.alertColor);
      return;
    }
    
    /* Set up the parameters */
    this.histMode= histMode;
    switch(histMode)
    {
      case MODE_CW_ROI_HIST:        /* draw CW ROI histogram */
        calibHistFlag= false;
        getPeaksByMeasFlag= false;
        frameSize= new Dimension(512,512);
        break;
        
      case MODE_CALIB_ND_ROI_HIST:  /* draw calib. hist. based on
                                     * ND step wedge. */
        calibHistFlag= true;
        getPeaksByMeasFlag= false;
        frameSize= new Dimension(875,550); 
        break;
        
      case MODE_CALIB_SPOTLIST_HIST: /*draw calib. hist. based on
                                      * spot list data.*/
        calibHistFlag= true;
        getPeaksByMeasFlag= true;
        frameSize=  new Dimension(875,600); 
        break;
        
      default:                       /* BOGUS mode */
        return;
    }
    
    cWidth= frameSize.width; 
    cHeight= frameSize.height;
    
    /* Make backup instance to save original calibration as 
     * backup calibration
     */    
    calib= iData.calib;
    calibBKUP= new CalibrateOD(calib); 
          
    activeImage= flk.activeImage;      /* active image name */    
    
    /* Copy parameters */
    this.horizCaption= (horizCaption==null)
                         ? "Gray-value"
                         : horizCaption;
    this.vertCaption= (vertCaption==null)
                         ? "Freq"
                         : vertCaption;
    this.calibHistFlag= calibHistFlag;
    this.getPeaksByMeasFlag= getPeaksByMeasFlag;
    if(title==null)
      title= "ROI Histogram [" + 
             util.getFileNameFromPath(iData.imageFile) +"]";    
    this.title= title;  
          
    /* Setup default colors */
    odColor= Color.black;
    calibPeakColor= Color.red;
    optBkgrdColor= new Color(230,230,230); /* darker than lightWhite! */
    textInputColor= Color.magenta;
    histColor= new Color(140,140,255);     /* lightBlue */
    histTopColor= Color.yellow;
    labelColor= Color.black;
    peakNbrColor= new Color(255,40,255);
    setExtraInfo(null, null, null);
    
    /* allocate structures */
    sHist= new int[256];
    xPainted= new int[256];
    yPainted= new int[256];
    
    fontFamily= "Helvetica";
    smallFont= new Font(fontFamily, Font.PLAIN, 10);
    mediumFont= new Font(fontFamily, Font.PLAIN, 11);
    largeFont= new Font(fontFamily, Font.PLAIN, 12);
        
    activeBin= -1;              /* active bin # [0:MAXBINS-1] else -1 */            
    calibChangedFlag= false;                       
    
    /* Set the default units to "Optical density", unitsAbbrev to "od" and 
     * unitsManufacturerPartNbr to "< opt. part # >".
     */
    calib.setDefaultUnits();
    
    /* Build the histogram GUI */
    buildHistGUI();
  } /* DrawHistogram */
  
  
  /**
   * buildHistGUI() - build the histogram GUI
   */
  private void buildHistGUI()
  { /* buildHistGUI */
    Button b;
    Label lbl;
    Panel
      statusPanel= new Panel(),     /* North */
      editPanel= new Panel(),       /* East */
      controlPanel= new Panel();    /* South */    
   
    /* Add listener to the frame */      
    this.addWindowListener(this);  
        
    /* Add components to frame */
    this.setLayout(new BorderLayout());
    
    /* Add a label to the statusPanel */    
    statusPanel.setLayout(new BorderLayout());
    statusLabel=
      new Label("                                                          ");
    statusPanel.add("North", statusLabel);
    add("North", statusPanel);
     
    /* Note: the mouse listener must be listening to the Frame */
    this.addMouseListener(this); 
    this.addMouseMotionListener(this);
        
    if(calibHistFlag)
    { /* add Calibration edit panel */
      /*
       * Array of TextFields used in constructing the editable
       * Peak Calibration Table.
       * It is of size [0:PEAK_CAL_TBL_ROWS-1][0:PEAK_CAL_TBL_COLS-1].
       * holding the N_PCT_PROLOGUE rows plus the MAX_ND_STEPS.
       *  -------------------------------------------------------------
       *  col #0             col #1      col #2     col #3
       *  -------------------------------------------------------------
       * 0  Calibration units ["optical density" / "Grayvalue" / "CPM", etc.]
       * 1  Wedge window      [ndcwx1 : ndcwx2, ndcwy1 : ndcwy2] - set by ROI
       * 2  Wedge identifier  ["manufacturer and part #"]
       * 3  Step #1 OD        [od value] Gray peak [gray value]
       *    Step #2 OD        [od value] Gray peak [gray value]
       *           . . .
       * n  Step #n OD        [od value] Gray peak [gray value]
       *  -------------------------------------------------------------
       */
      int
        nRows= PEAK_CAL_TBL_ROWS, /* or N_PCT_PROLOGUE+calib.maxNDsteps, */
        nCols= PEAK_CAL_TBL_COLS;
      TextField tf;

      /* Create the above GUI and text event handlers */
      editPanel.setLayout(new GridLayout(nRows,nCols));      
      peakCalTblTF= new TextField[nRows][nCols];
      for(int r= 0;r<nRows;r++)
        for(int c=0;c<nCols;c++)
        {
          tf= new TextField("");
          peakCalTblTF[r][c]= tf;
          editPanel.add("Center",tf);          
          boolean editableFlag= (c==1 || c==3);
          tf.setEditable(editableFlag);
          Color
            bg= (editableFlag) ? optBkgrdColor : Color.lightGray,
            fg= (c==1 && r>=N_PCT_PROLOGUE)
                      ? calibPeakColor
                      : ((c==3 && r>=N_PCT_PROLOGUE)
                           ? histColor 
                           : labelColor);
          tf.setForeground(fg);
          tf.setBackground(bg);
        }
      
      /* [HACK] Disable unused fields for now */
      peakCalTblTF[1][3].setEditable(false);
      peakCalTblTF[2][3].setEditable(false);
      peakCalTblTF[1][3].setBackground(Color.lightGray);
      peakCalTblTF[2][3].setBackground(Color.lightGray);
      
      /* Fill in the text fields from calib data */
      updatePeakCalTable();
            
      add("East", editPanel);
    } /* add Calibration edit panel */
    
    /* Configure controlPanel layout */
    controlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));
    add("South", controlPanel);     /* Seems to work best this way */
        
    if(calibHistFlag)
    { /* add Calibration histogram buttons */
      /* [NOTE] get the peak list from the measured spot list using
       * a call to genPeaksFromMeasSpotList() from the calling method
       * in order to better do error handling.
       */ 
      if(!getPeaksByMeasFlag)       
      { /* Get peak list from analyzing wedge ROI histogram */
        b= new Button("Analyze wedge ROI");
        b.addActionListener(this);
        controlPanel.add(b);
        
        Label lb1= new Label("");
        lb1.setForeground(optBkgrdColor);
        lb1.setBackground(optBkgrdColor);
        controlPanel.add(lb1);    /* spacer */
        
        b= new Button("Add peak");
        b.addActionListener(this);
        controlPanel.add(b);
        
        b= new Button("Delete peak");
        b.addActionListener(this);
        controlPanel.add(b);
      } /* Getpeak list from analyzing wedge ROI histogram */
      
      b= new Button("Refresh peak table");
      b.addActionListener(this);
      controlPanel.add(b);      
      
      Label lb2= new Label("");
      lb2.setForeground(optBkgrdColor);
      lb2.setBackground(optBkgrdColor);
      controlPanel.add(lb2);    /* spacer */      
      
      b= new Button("Save calibration state");
      b.addActionListener(this);
      controlPanel.add(b);
    } /* add Calibration histogram buttons */
    else
    { /* computing window ROI histogram */
      b= new Button("Recompute ROI histogram");
      b.addActionListener(this);
      controlPanel.add(b);
    } /* ROI histogram */
    
    b= new Button("SaveAs GIF");
    b.addActionListener(this);
    //controlPanel.add(b);    /* disable until debug why does not work */
    
    b= new Button("Close");
    b.addActionListener(this);
    controlPanel.add(b);
    
    /* Realize it but don't display it */
    this.pack();
    this.setSize(frameSize);
    positionHistWindow();
         
    isVisibleFlag= false;
    this.setVisible(false);   /* enable it later... */
  } /* buildHistGUI */
    
  
  /**
   * getPreferredSize() - getPreferredSize, needed for sizing frame correctly. 
   * @return frame size
   */
  public Dimension getPreferredSize() 
  { /* getPreferredSize */      
    return(new Dimension(frameSize));  
  } /* getPreferredSize */
      
  
  /**
   * positionHistWindow() - position histogram window on the screen
   */
  private void positionHistWindow()
  { /* positionHistWindow */
    /* Center frame on the screen, PC only */
    Dimension 
      histWindSize= this.getSize(),
      flkSize= flk.getSize(),
      screen= Toolkit.getDefaultToolkit().getScreenSize();
    int
      xOffsetForROIhistogram= (calibHistFlag) ? -40 : 0,
      yOffsetForROIhistogram= (calibHistFlag) ? 50 : 0,
      xPos= ((flkSize.width+histWindSize.width) < screen.width) 
              ? (flkSize.width +10)
              : (screen.width - histWindSize.width)/2,
      yPos= (flkSize.height - histWindSize.height)/2;
    if(xPos<0 || yPos<0)
    { /* came up too soon */      
      xPos= (screen.width - histWindSize.width)/2; 
      yPos= (screen.height - histWindSize.height)/2;
    }
    Point pos= new Point(xPos,yPos);
    this.setLocation(pos);
  } /* positionHistWindow */
  
  
  /**
   * setStatusMsg() - set the status msg
   * @param msg is the status window msg
   */
  public void setStatusMsg(String msg)
  { 
    statusLabel.setForeground(Color.black);
    statusLabel.setText(msg); 
  } 
  
  
  /**
   * setStatusMsg() - set the status msg
   * @param msg is the status window msg
   * @param color to draw it 
   */
  public void setStatusMsg(String msg, Color color)
  { 
    statusLabel.setForeground(color);
    statusLabel.setText(msg); 
  } 
  
  
  /**
   * setTitleHist() - set the title
   * @param title is window title
   */
  public void setTitleHist(String title)
  { 
    this.title= title;
    this.setTitle(title);
  } 
  
  
  /**
   * setHorizCaption() - set the horizontal caption
   * @param horizCaption is horizontal caption
   */
  public void setHorizCaption(String horizCaption)
  { this.horizCaption= horizCaption; } 
  
  
  /**
   * setVertCaption() - set the vertical caption
   * @param vertCaption is vertical caption (generally frequency)
   */
  public void setVertCaption(String vertCaption)
  { this.vertCaption= vertCaption; }  
  
  
  /**
   * setVisible() - set the histogram as visible or not
   * @boolean isVisibleFlag to enable/disable visibility
   * @param redoROIhistFlag will reompute thie iData.hist[]
   *        from the ROI window in the image.
   */
  public void setVisible(boolean isVisibleFlag, boolean redoROIhistFlag)
  { /* setVisible */
    this.isVisibleFlag= isVisibleFlag;
  
    /* Realize it */
    this.setVisible(isVisibleFlag);    
       
    /* set state & cause repaint to draw it*/
    updateHistogramPlot(redoROIhistFlag);
  } /* setVisible */

  
  /**
   * drawGifFile() - draw plot into Gif image file if in stand-alone mode.
   * This sets it up and lets paint() to the heavy lifting...
   * @param oGifFileName is the full path GIF output file
   * @return true if successful
   */
  boolean drawGifFile(String oGifFileName)
  { /* drawGifFile */
    if(oGifFileName==null)
      return(false);
    
    drawIntoImageFlag= true;
    this.oGifFileName= oGifFileName;
    repaint();          /* will start the process */
    
    return(true);
  } /* drawGifFile */
  
  
  /**
   * setExtraInfo() - set the extra info fields (r1,r2,f3)
   */
  public void setExtraInfo(String r1, String r2, String r3)
  { /* setExtraInfo*/
    this.r1= r1;
    this.r2= r2;
    this.r3= r3;
  } /* setExtraInfo */
  
   
  /**
   * getMinimumSize() - get the minimum preferred size
   * @return window size
   */
  public Dimension getMinimumSize()
  { /* getMinimumSize */    
    if(calibHistFlag)
      return(new Dimension(CAL_POPUP_WIDTH, CAL_POPUP_HEIGHT));
    else      
      return(new Dimension(ROI_POPUP_WIDTH, ROI_POPUP_HEIGHT));
  } /* getMinimumSize */
  
 
  /**
   * updatePeakCalTable() - update the peak calibration table
   * with new data from the calib od and gray value arrays.
   *<PRE>
   * Array of TextFields used in constructing the editable
   * Peak Calibration Table.
   * It is of size [0:PEAK_CAL_TBL_ROWS-1][0:PEAK_CAL_TBL_COLS-1].
   * holding the N_PCT_PROLOGUE rows plus the MAX_ND_STEPS.
   *  -------------------------------------------------------------
   *  col #0             col #1      col #2     col #3
   *  -------------------------------------------------------------
   * 0  Calibration units ["optical density" / "Grayvalue" / "CPM", etc.]
   * 1  Wedge window      [ndcwx1 : ndcwx2, ndcwy1 : ndcwy2] - set by ROI
   * 2  Wedge identifier  ["manufacturer and part #"]
   * 3  Step #1 OD        [od value] Gray peak [gray value]
   *    Step #2 OD        [od value] Gray peak [gray value]
   *           . . .
   * n  Step #n OD        [od value] Gray peak [gray value]
   *  -------------------------------------------------------------  
   *</PRE>     
   */
  private void updatePeakCalTable()
  { /* updatePeakCalTable */
    if(!calibHistFlag)
      return;                   /* no table... */
    
    int
      nRows= PEAK_CAL_TBL_ROWS, /* or N_PCT_PROLOGUE+MAX_ND_STEPS, */
      nCols= PEAK_CAL_TBL_COLS;
    String
      roiStr,
      sStep,
      sGpeak,
      odStr,
      grayPeakStr;
        
    /* change the text fields with the new data */
    peakCalTblTF[0][0].setText("Calibr. units:");
    peakCalTblTF[0][1].setText(calib.units);
    peakCalTblTF[0][2].setText("Calibr. abbrev.:");
    peakCalTblTF[0][3].setText(calib.unitsAbbrev);
    
    if(!getPeaksByMeasFlag)
    {
      peakCalTblTF[1][0].setText("Wedge ROI:");
      roiStr= "["+calib.ndcwx1+":"+calib.ndcwx2+", "+
               calib.ndcwy1+":"+calib.ndcwy2+"]";
      peakCalTblTF[1][1].setText(roiStr);
      peakCalTblTF[1][1].setEditable(false);
    }
    else
    {
      peakCalTblTF[1][0].setText("Mean spot values");
    }
    
    peakCalTblTF[2][0].setText("Wedge ID#:");
    peakCalTblTF[2][1].setText("<opt. part #>");
    
    for(int s= 0;s<MAX_ND_STEPS;s++)
    {
      int
        r= s+ N_PCT_PROLOGUE,
        gpVal= calib.ndWedgeGrayValues[s];
      float od= calib.ndWedgeODvalues[s];
      sStep= "Step #"+(s+1)+" "+calib.unitsAbbrev+":";
      sGpeak= "Gray peak #"+(s+1)+":";
      odStr= util.cvf2s(od,2);
      grayPeakStr= (""+gpVal);
      peakCalTblTF[r][0].setText(sStep);
      peakCalTblTF[r][1].setText(odStr);
      peakCalTblTF[r][2].setText(sGpeak);
      peakCalTblTF[r][3].setText(grayPeakStr);
    }
  } /* updatePeakCalTable */
  
  
  /**
   * refreshPeakTable() - refresh the peak table from the TextFields...
   * @param updateMapFlag to apply the new table to updating mapGrayToOD[]. 
   * @return true if succeed
   */
  public boolean refreshPeakTable(boolean updateMapFlag)
  { /* refreshPeakTable */  
    boolean grayPeaksTableChanged= false;
    int
      nRows= PEAK_CAL_TBL_ROWS, /* or N_PCT_PROLOGUE+calib.maxNDsteps, */
      nCols= PEAK_CAL_TBL_COLS;
    
    /* the the new data from the text fields */ 
    /* data for "Calibr. units" */
    calib.setUnits(peakCalTblTF[0][1].getText());
    
    /* data for "Calibr. abbrev." */    
    calib.setUnitsAbbrev(peakCalTblTF[0][3].getText());      
    
    /* data for "Wedge ID#" */
    calib.setUnitsManufacturerPartNbr(peakCalTblTF[2][1].getText());
    
    /* Get all of the (od, gray peak) data */
    int
      maxExistingSize= calib.ndWedgeGrayValues.length,
      gp,
      r;
    float od= 0.0F;
    String 
      odStr,
      gpStr;
    for(int s= 0;s<MAX_ND_STEPS;s++)
    { /* get data for each step s */
      gp= 0;
      r= s+ N_PCT_PROLOGUE;
      /* may have added to list so need to change calib.maxNDsteps */
      /* data for "Optical density value" */
      odStr= peakCalTblTF[r][1].getText();
      od= util.cvs2f(odStr,0.0F);
      if(s<maxExistingSize)
      {
        if(calib.ndWedgeODvalues[s]!=od)
          grayPeaksTableChanged= true;
        calib.ndWedgeODvalues[s]= od;
      }
      
      /* data for "Gray peak value" */
      gpStr= peakCalTblTF[r][3].getText();
      gp= util.cvs2i(gpStr,0);
      if(s<maxExistingSize)
      {
        if(calib.ndWedgeGrayValues[s]!=gp)
          grayPeaksTableChanged= true;
        calib.ndWedgeGrayValues[s]= gp;
      }
    } /* get data for each step s */
    
    /* Find the current maxPeaks and maxNDvalues
     * from non-zero values in the peaks table. 
     */
    calib.findPeakTableSizes();
    
    if(grayPeaksTableChanged || updateMapFlag)
    { /* re-extrapolate the mapGrayToOD[] */  
      if(flk.NEVER)
        System.out.println("DH-RFT the gray peaks table changed...");
      /* Regenerate the mapGrayToOD[] from the peak table */
      boolean ok= reExtrapolateCalibration(); 
    } /* re-extrapolate the mapGrayToOD[] */ 
    
    if(/* !ok && */ grayPeaksTableChanged)
      calibChangedFlag= true;
    
    return(grayPeaksTableChanged);
  } /* refreshPeakTable */
  
  
  /**
   * reExtrapolateCalibration() - regenerate the mapGrayToOD[]
   * from the peak table
   * @return true if the extrapolate succeeds
   */
  private boolean reExtrapolateCalibration()
  { /* reExtrapolateCalibration */
    calib.maxGrayValue= calib.MAX_GRAY;     /* [TEST] MUST BE 255 for extrapolation */
    String sOK= calib.extrapolateNDwedgeMap();
    if(sOK!=null)
    {
      String msg= sOK+", re-edit the table and try again.";
      setStatusMsg(msg, Color.red);
      util.popupAlertMsg(msg, flk.alertColor);
      return(false);
    }
    else
    { /* update the ImageData instance */
      iData.mapGrayToOD= calib.getMapGrayToOD();
      iData.hasODmapFlag= calib.getHasODmapFlag();
    }
    
    return(true);
  } /* reExtrapolateCalibration */
  
  
  /**
   * genPeaksFromMeasSpotList() - get the peak list from the
   * circular mask measurement spot list using the mean circle 
   * measurement grayscale values. These values are not corrected 
   * for background (since we do not know what background is).
   */ 
   public boolean genPeaksFromMeasSpotList()
   { /* genPeaksFromMeasSpotList */     
     Spot spotList[]= iData.idSL.spotList;
     ImageDataMeas idM= iData.idM;
     int
       nSpots= iData.idSL.nSpots,
       x,
       y,
       g,
       nbr= 0;
     float mnDens;
     
     if(nSpots>=MAX_ND_STEPS)
     {       
       String msg= "You can't calibrate grayscale with a spot list containing\n"+
                    "more than "+MAX_ND_STEPS+" spots.\n"+
                    "Delete some of the spots and try again.";
       setStatusMsg(msg, Color.red);
       util.popupAlertMsg(msg, flk.alertColor);
       return(false);
     }
     
     /* Init data structures */  
     calib.ndWedgeGrayValues= new int[MAX_ND_STEPS];
     iData.hist= new int[256];
     calib.hist= iData.hist;
     hist= iData.hist;
     calib.maxPeaks= 0;
     
     /* Copy sorted spot list mean gray values into peak table and 
      * histogram 
      */
     float mnGrayValues[]= new float[nSpots];
     int areaGV[]= new int[nSpots];
     for(int i=0;i<nSpots;i++)
     { /* save each spot in the peak table */
       Spot s= spotList[i];
       nbr= s.nbr;
       mnGrayValues[i]= s.mnDens;
       areaGV[i]= idM.maskArea[s.circleRadius]; 
       calib.maxPeaks++;
     }
     int sortIndex[]= util.bubbleSortIndex(mnGrayValues, nSpots,
                                           true /*ascending*/);
     for(int i=0;i<nSpots;i++)
     { /* Save as sorted array */   
       g= (int)mnGrayValues[sortIndex[i]];    
       calib.ndWedgeGrayValues[i]= g; 
       /* mark it in the the histogram*/
       iData.hist[g]= areaGV[sortIndex[i]]; 
     }
     
     return(true);
   } /* genPeaksFromMeasSpotList */
   

  /**
   * updateHistogramPlot() - set hist state and do repaint to draw it.
   * @param redoROIhistFlag will reompute thie iData.hist[]
   *        from the ROI window in the image.
   */
  public void updateHistogramPlot(boolean redoROIhistFlag)
  { /* updateHistogramPlot */    
    if(flk.activeImage.equals("left"))
    {
      iData= flk.iData1;
      imageFile= flk.imageFile1;
    }
    else if(flk.activeImage.equals("right"))
    {
      iData= flk.iData2;
      imageFile= flk.imageFile2;
    }
    else
    { /* No-OP */
      return;  
    }
    
    hist= iData.hist;
    calib= iData.calib;

    /* Reallocate or compute whatever needs to be done ... */
    if(redoROIhistFlag)
    { /* recompute the histogram from the ROI */ 
      ImageDataROI idROI= iData.idROI;
      /* Compute the histogram under the ROI in case want to use it */
      boolean ok= idROI.calcHistogramROI();
      if(!ok)
        return;
      
      iData.dwHist.setTitleHist("Histogram of ROI ["+
                                idROI.cwx1+":"+idROI.cwx2+","+
                                idROI.cwy1+":"+idROI.cwy2+"] "+
                                util.getFileNameFromPath(iData.imageFile));
    }
    
    if(getPeaksByMeasFlag)
    {
      boolean ok= reExtrapolateCalibration();
    }
    
    repaint();
   } /* updateHistogramPlot */
  
     
  /**
   * drawPlus() - draw plus sign at the specified color.
   * @param g is graphics context
   * @param x is center of object
   * @param y is center of object
   * @param color is color to draw
   */
  final private static void drawPlus(Graphics g, int x, int y, Color color)
  { /* drawPlus */
    int w= 2;
    g.setColor( color );
    g.drawLine( x, y-w, x, y+w );
    g.drawLine( x-w, y, x+w, y );
  } /* drawPlus */
  
     
  /**
   * drawFilledBox() - draw plus sign at the specified color.
   * @param g is graphics context
   * @param x is center of object
   * @param y is center of object
   * @param width is width of object
   * @param color is color to draw
   */
  final private static void drawFilledBox(Graphics g, int xC, int yC, 
                                     int width, Color color)
  { /* drawFilledBox */
    int 
      w2= Math.max((width/2),1),
      x1= xC-w2,
      y1= yC-w2,
      x2= xC+w2,
      y2= yC+w2;
    g.setColor( color );
    for(int y=y1;y<=y2;y++) 
      g.drawLine( x1, y, x2, y );
  } /* drawFilledBox */
  
    
  /**
   * drawHistInCanvas() - draw histogram of hist[].
   * @param g is graphics context
   */
  private boolean drawHistInCanvas(Graphics g)
  { /* drawHistInCanvas */
    int
      sumHist= 0,	       /* if sum is zero then no histogram to draw */
      hVal,
      minGrayVal= 1000000000,  /* min gray value value found in hist[] */
      maxGrayVal= -1,          /* max gray value value found in hist[] */
      minFreqVal= 1000000000,  /* min frequency value value found in hist[] */
      maxFreqVal= -1,          /* max frequency value value found in hist[] */
      i;
        
    /* [1] Make sure histogram exists */
    if(hist==null)
      return(false);                /* no histogram */
      
    /* Compute histogram extrema */
    for (i=0;i<=255;i++)
    { /* compute histogram extrema */
      hVal= hist[i];
      sHist[i]= hVal;               /* make a copy */
      if(hVal>0)
      { /* only count non-zero entries in histogram */
        sumHist += hVal;           /* get total of all values */
        minGrayVal= Math.min(minGrayVal,i);
        maxGrayVal= Math.max(maxGrayVal,i);
        minFreqVal= Math.min(minFreqVal,hVal);
        maxFreqVal= Math.max(maxFreqVal,hVal);
      }
    } /* compute histogram extrema */
    
    /* [1.1] If sum is zero then no histogram to draw */ 
    if(sumHist==0)
      return(false);               /* no histogram data */
    
    /* [1.2] Scale maximum histogram value to 250 so room at top */
    float scaleFactor= (250.0F/maxFreqVal);
    for(i=0;i<=255;i++)
      sHist[i]= (int)(sHist[i] * scaleFactor);
    
    /* [2] If draw plot into GIF image file, setup new Graphics g. */
    Image gifImage= null;
    if(drawIntoImageFlag)
    { /* draw into GIF file Image instead of canvas */
      gifImage= createImage(frameSize.width,frameSize.height);
      g= gifImage.getGraphics();
    }
    
    /* [3] Set up blank image with background color. */ 
    this.setBackground(optBkgrdColor);   /* clear screen */
    g.clearRect(0,0,cWidth,cHeight);       
    
    /* [4] Draw histogram at (100,100) */
    int 
      x, y,
      y1, y2,
      xSelected= 0,
      ySelected= 0,
      xCoord[]= new int[calib.maxPeaks],
      yCoord[]= new int[calib.maxPeaks],
      currentSelectedPeak= -1;
    
    /* draw the histogram and related data and and peak coords */
    for (i=0;i<=255;i++)
      if (sHist[i]>0)
      { /* draw histogram pixels */
        x= 100+i;
        y2= (355 - sHist[i]);
        g.setColor( histColor );         /* forground color */
        g.drawLine(x,355,x,y2);        
        g.setColor( histTopColor );      /* hist top color */
        g.drawLine(x,y2, x,y2);
        xPainted[i]= x;                  /* save X coords. so can find bin */
        
        /* Save coords for peaks so we can draw the peak tick marks and 
         * number labels later */
        for (int j=0;j<calib.maxPeaks;j++)
        { /* Save coords for peaks */
           if(i==activeBin)
           { /* found selected peak (active bin) */
              currentSelectedPeak= i; 
              xSelected= x;
              ySelected= Math.max((355-sHist[i]),0);                  
           } /* found selected peak (active bin) */
          if(i==calib.ndWedgeGrayValues[j])
          { /* found peak */              
            xCoord[j]= x;
            yCoord[j]= Math.max((355-sHist[i]),0);              
          } /* found peak */  
        } /* Save coords for peaks */
      } /* draw histogram pixels */
          
    /* [4.1] Draw Peak markers if histogram exists. */
    if(calibHistFlag)
    { /* Draw the peak marks */
    
      Font fTmp= new Font("Serif",Font.PLAIN,peakNbrFontSize);                                            
      boolean drawActiveBinOnceFlag= true;
      g.setColor(labelColor);
      
      for(int j=0;j<calib.maxPeaks;j++) 
      { /* Draw peak tick mark */
       
        boolean activeBinFlag= false;
        Font defaultFont= g.getFont();
        
        /* see if active bin is valid */
        if(currentSelectedPeak==-1)
          activeBinFlag= false;
        else
        { /* selected peak */
          if(currentSelectedPeak == calib.ndWedgeGrayValues[j])
            activeBinFlag= true; /* found peak in list that is selected */
          else
          { /* found selected peak that is not be in the list */            
            activeBinFlag= false;   
            if(drawActiveBinOnceFlag)
            { /* draw only once */
              y2= ySelected;
              x= xSelected;   
              y1= Math.max((y2-5),0);    /* find y1 of peak */        
              g.setColor(labelColor);    /* note this is activeBin in histogram */
              g.drawLine(x,y1, x,(y2-1)); /* draw actual peak mark from y1 to y2 */
              g.setColor(calibPeakColor);
              drawActiveBinOnceFlag= false;
            } /* draw only once */
          } /* found selected peak that is not be in the list */
        } /* selected peak */
           
        if(activeBinFlag)
          g.setColor(labelColor); /* note this is activeBin in histogram */
        else
          g.setColor(calibPeakColor);
             
        x= xCoord[j];
        y2= yCoord[j];
        y1= Math.max((y2-5),0);     /* find y1 of peak */        
        g.drawLine(x,y1, x,(y2-1)); /* draw actual peak mark from y1 to y2 */        
        g.setFont(fTmp);        
        
        if(activeBinFlag)
          g.setColor(labelColor);   /* note this is activeBin in histogram */
        else 
          g.setColor(peakNbrColor);             
        
        int peakNbr= j+1; /* zero one counting */
        g.drawString(""+peakNbr, x-2, y1-2);  /* Draw peak number label 
                                               * above tick mark */       
        
        /* reset to default font and color */
        g.setColor(calibPeakColor);       
        g.setFont(defaultFont);
                
      } /* Draw peak tick mark */    
    } /* Draw the peak marks */
    
    else if(!calibHistFlag && activeBin!=-1)
    { /* draw Peak marker in comp. window ROI if activeBin and hist exists */
      g.setColor(calibPeakColor);
      x= 100+activeBin;
      y2= Math.max((355-sHist[activeBin]),0);
      /* Draw active peak mark */
      y1= Math.max((y2-5),0);
      g.drawLine(x,y1, x,(y2-1));
    } /* draw Peak marker in comp. window ROI if activeBin and hist exists */

    /* [4.2] Draw the OD fiducial marks on the LEFT */
    g.setColor(labelColor);
    g.drawLine(100,355, 100,100);     /* Left side vertical OD */
    g.drawLine(356,355, 356, 100);    /* Right side vertical FREQ */
    g.drawLine(100,355, 355,355);     /* Horizontal axis*/
    
    /* [4.3] Draw histogram frequency marks on the RIGHT */
    int 
      factor= maxFreqVal/5,
      scaleRt[]= new int[factor],
      scaleY= 0,
      rtY= 0,
      j= -1,
      tickSize,
      freqNbr,
      n;    
     
     for (i=0; i<6; i++)
     {      
       if(i==0)
         scaleY= 0;
       else
         scaleY= factor + scaleY;  
       scaleRt[i]= scaleY;       
     }
    
    for (i=0; i<=255; i+=10)
    { /* Draw Fiducials */
      g.setColor(labelColor);
      tickSize= 3;
      if ((++j)==0)
      { /* init */
        j= -5;
        tickSize= 6;
      } /* init */
      
      /* draw it */
      x= 100;
      y= 355-i;
      g.drawLine(x,y, x-tickSize,y);  /* Left Side  OD ticks*/
      
      x= i+100;
      y= 355;
      g.drawLine(x,y, x,y+tickSize);  /* Horizontal grayscale ticks*/
            
      x= 356;
      y= 355-i;
      g.drawLine(x,y, x+tickSize,y);  /* Right Side frequency ticks */
      if(tickSize==6)
      {
        g.setColor(histColor);
        freqNbr= scaleRt[rtY++];  
        x= 356+tickSize+2;
        g.drawString((""+freqNbr), x, y);        
      }
    } /* Draw Fiducials */
    
    /* [4.4] Draw marks for each step of the ND wedge in LEFT OD scale
     * if the calibrations standard exists 
     */
    if(calibHistFlag)
    { /* the wedge exists */
      tickSize= 5;                 /* Make the tick 3 pixels wide */
      j= 0;                        /* Start off w/ ndWedgeODvalues[0] */
      g.setFont(smallFont);
      for (i=100;i<=355;i++)
      { /* Draw NDwedge */
        if((i-101)==(int)(calib.ndWedgeODvalues[j]*100) &&
        (calib.ndWedgeGrayValues[j]>0))
        { /* We have a match here */
          g.setColor(calibPeakColor);
          String ndWedgeStr= util.cvf2s(calib.ndWedgeODvalues[j],2);
          g.drawString(ndWedgeStr, 70, (355+100-i-4));
          g.setColor(calibPeakColor);
          /* draw it */
          y= 355+100-i;             /* Scaled 0.0 to 2.55 OD */
          x= 98;
          g.drawLine(x,y, x+tickSize,(y+1)); /* tick mark */
          j++;                      /* Now look for the next one */
        } /* We have a match here */
      } /* Draw NDwedge */
    }  /* the wedge exists */
    
    /* [4.5] Draw Gray to OD map P.W.L. function at (100,100).
     * If there is no calibration, draw 1:1 line.
     */
    if(calibHistFlag)
    { /* the wedge exists */      
      g.setColor(odColor);
      float maxGtoOD= 0.0F;
      for (i=0;i<=255;i++)
      { /* find max OD value in the map */
        maxGtoOD= Math.max(maxGtoOD, iData.mapGrayToOD[i]);
      }
      float scale= 256.0F/maxGtoOD;
      for (i=0;i<=255;i++)
      { /* draw ND wedge pixel */
        /* Just draw the whole thing */
        float val= scale*iData.mapGrayToOD[i];
        y= (355 - (int)val);
        x= 100+i;
        g.drawLine(x,y, x,y);    /* draw the point */
      } /* draw ND wedge pixel */
    } /* the wedge exists */
    
    /* [4.6] Label graph axes with medium lettering */
    g.setFont(mediumFont);
    
    /* [4.6.1] Draw horizontal Grayscale axis values */
    g.setColor(labelColor);
    g.drawString("0", 100,375);
    g.drawString("50", 150,375); 
    g.drawString("100", 200,375);  
    g.drawString("150", 250,375);  
    g.drawString("200", 300,375);  
    g.drawString("250", 350,375);  
    
    /* [4.6.2] Draw vertical OD axis values */
    if(calibHistFlag)
    { /* the wedge exists */      
      g.setColor(labelColor);
      float od=2.5F;
      for(y=100; y<=300; y+=50,od-=0.5F)
      { /* draw OD labels on vertical axes */
        String sOD= util.cvf2s(od,1)+" "+calib.unitsAbbrev;
        g.drawString(sOD, 40-6,y);
      } /* draw OD labels on vertical axes */
    } /* the wedge exists */
    
    /* [4.6.3] Draw text under the X axis */
    g.setFont(largeFont);    
    g.setColor(labelColor);
    g.drawString("GrayScale", 210,390);  
    
    /* [4.6.4] Draw additional grayscale statistics under X axis */
    g.drawString("Grayscale range["+minGrayVal+":"+maxGrayVal+"]",
                 130,410);
    g.drawString("Frequency range["+minFreqVal+":"+maxFreqVal+"]",
                 130,424);
    if(calibHistFlag)
    { /* the wedge exists */ 
      if(!getPeaksByMeasFlag)
        g.drawString("Region Of Interest ["+
                     calib.ndcwx1+":"+calib.ndcwx2+", "+
                     calib.ndcwy1+":"+calib.ndcwy2+"]",
                     130,438);       
     }
     else
    { /* the computing window ROI */ 
      ImageDataROI idROI= iData.idROI;  
      g.drawString("Region Of Interest["+
                   idROI.cwx1+":"+idROI.cwx2+", "+
                   idROI.cwy1+":"+idROI.cwy2+"]",
                   130,438);
     }
    
    /* [4.6.5] Show the active histogram bin if any */
    if(activeBin!=-1)
      g.drawString("Active histogram bin ["+activeBin+
                   "], Frequency ["+hist[activeBin]+"]",
                   130,460); 
    else      
      g.drawString("Active histogram bin ['not selected']",
                   130,460); 
         
    /* [FUTURE] [4.6.6] Draw additional info */
    g.setFont(mediumFont);    
    g.setColor(labelColor);
    if(r1!=null)
      g.drawString(r1, 20,460);
    if(r2!=null)
      g.drawString(r2, 20,470);
    if(r3!=null)
      g.drawString(r3, 20,480);
        
    /* [4.7] Draw special "icon" for OD legends */  
    if(calibHistFlag)
    { /* the wedge exists */      
      g.setFont(largeFont);
      g.setColor(labelColor);
      g.drawString("Gel OD Calibration", 190,70);

      g.setColor(labelColor);
      g.drawString("OD map", 6,170);   
      g.setColor(odColor);
      drawFilledBox(g, 20,183, 8, odColor);
      
      g.setColor(calibPeakColor);
      g.drawString("Calibration", 6,270); 
      g.drawString("peaks", 10,283); 
      drawFilledBox(g, 20,296, 6, calibPeakColor);  /* draw box */
    } /* the wedge exists */
    else
    { /* Generic ROI histogram */      
      g.setFont(largeFont);
      g.setColor(labelColor);
      g.drawString("Region of Interest Histogram [ "+
                   util.getFileNameFromPath(imageFile)+" ]", 
                   100,70);
    }
        
    /* [4.8] Draw special "icon" for WEDGE FREQUENCY legends */
    g.setFont(largeFont);
    g.setColor(histColor);    
    if(calibHistFlag)
    { /* the ND wedge exists - draw on the RIGHT */      
      g.drawString("Wedge", 10,387);
      g.drawString("Frequency", 10,400);
      g.drawString("Histogram", 10,413);
      drawFilledBox(g, 20,426, 8, histColor);  /* draw box */
    }
    else      
    { /* computing window ROI - draw on the LEFT */
      g.drawString("Wedge", 10,215);
      g.drawString("Frequency", 10,228);
      g.drawString("Histogram", 10,241);
      drawFilledBox(g, 30,254, 8, histColor);  /* draw box */
    }
        
    /* [4.9] Draw in the ND wedge table if the calibration exists*/
    if(calibHistFlag)
      updatePeakCalTable();
    
    /* [4.10] Redraw horizonal axis */
    g.setColor(labelColor);
    g.drawLine(100,355, 355,355);     /* Horizontal */
    
    /* [5] If drawing to a GIF file, then cvt Image to Gif stream
     * and write it out.
     */
    if(drawIntoImageFlag && gifImage!=null)
    { /* write it out */
      drawIntoImageFlag= false;
      WriteGifEncoder wge= new WriteGifEncoder(gifImage);
      gifImage= null;
      if(wge!=null)
        wge.writeFile(oGifFileName);
      
      repaint();                     /* refresh the actual canvas */
    } /* write it out */
    
    return(true);
  } /* drawHistInCanvas */
  
  
  /**
   * paint() - draw the histogram
   * @param g is graphics context
   * @see #drawHistInCanvas
   */
  public void paint(Graphics g)
  { /* paint */ 
    drawHistInCanvas(g); 
  } /* paint */
    
  
  /**
   * actionPerformed() - Handle Control panel button clicks
   * @param e is ActionEvent for buttons in control panel
   */
  public void actionPerformed(ActionEvent e)
  { /* actionPerformed */
    String cmd= e.getActionCommand();
    Button item= (Button)e.getSource();
   
    setStatusMsg("");                /* clear it */
    
    if (cmd.equals("Close"))
    {
      close();
    }
    
    else if (cmd.equals("Recompute ROI histogram"))
    { /* set state & cause repaint to draw it */
      ImageDataROI idROI= iData.idROI;  
      if(!idROI.isValidCW())   
      { 
        idROI.setROI(0,cWidth-1,0,cHeight-1);
        idROI.copyROI2CW();
      }
      updateHistogramPlot(true); 
      /* [TODO] do we want to restore the illegal ROI? */
    }
    
    else if(cmd.equals("Analyze wedge ROI"))
    { /* Analyze wedge ROI from the CW */ 
      ImageDataROI idROI= iData.idROI;  
      if(!idROI.isValidCW())
      {
         String sMsg= "First define the ROI (C-U) ULHC and (C-L) LRHC";
         System.out.println(sMsg);
         setStatusMsg(sMsg);
         return;
      }
      else
        calib.setWedgeROI(-1,-1,-1,-1);  /* Clear it so can overide with CW */
      
      /* Set up histogram, peaks and extrpolate map for the current ROI. */
      boolean ok= iData.calib.calcHistFindPeaksAndExtrapolate(iData);
      if(ok)
      {
        updateHistogramPlot(false);
        calibChangedFlag= true;   
        setStatusMsg("Updated peak table from histogram and calibration",
                      Color.black);
      }
    } /* Analyze wedge ROI from the CW*/
    
    else if (cmd.equals("Add peak"))
    { /* Add peak, rebuild table and map, then refresh */
      if(activeBin==-1)
      {        
        String msg= "You must select the peak before you can add a peak."; 
        setStatusMsg(msg, Color.red);
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      /* if activeBin!=-1, then add the peak to the peak table */
      int
        gI,
        idxJ= 0,
        nSteps= calib.maxPeaks;
      if(nSteps==MAX_ND_STEPS)
      {        
        String msg= "No more room in the peak table for additional peaks.";
        setStatusMsg(msg, Color.red);
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      boolean insertFlag= false;      
      int tmpGV[]= new int[MAX_ND_STEPS];
      for(int i=0; i<nSteps; i++)
      { /* look for the bin to insert the new peak */
        gI= calib.ndWedgeGrayValues[i];
        if(activeBin==gI)
        {
          String msg= "Ignoring this peak since already in the peak list."; 
          setStatusMsg(msg, Color.red);
          util.popupAlertMsg(msg, flk.alertColor);
          return;
        }
        
        if(activeBin<gI && i==0)
        { /* insert activeBin in the FONT of the list */
          tmpGV[idxJ++]= activeBin;
          insertFlag= true;
        }
        else if(!insertFlag && gI>activeBin)
        { /* insert activeBin in the MIDDLE of the list */
          tmpGV[idxJ++]= activeBin;
          insertFlag= true;
        }
        /* Always copy the one at the end */
        tmpGV[idxJ++]= gI;       
      } /* look for the bin to insert the new peak */
      
      /* If the new point is > all points already in the list
       * then add it at the END of the list.
       */
      if(!insertFlag)
      {
        tmpGV[idxJ++]= activeBin;
        insertFlag= true;
      }
      /* Update the active peak list */
      ++(calib.maxPeaks);
      calib.ndWedgeGrayValues= tmpGV;
      
      updatePeakCalTable();      
      /* Regenerate the mapGrayToOD[] from the peak table */
      if(reExtrapolateCalibration())  
      { /* repaint IFF succeed in creating a new map */
        calibChangedFlag= true;
        setStatusMsg("Added peak from peak table and calibration", Color.black);
        repaint();       
      } 
      else 
        calibChangedFlag= false;   /* Don't save bad extrapolations */ 
    }  /* Add peak, rebuild table and map, then refresh */
    
    else if (cmd.equals("Delete peak"))
    { /* Delete peak, rebuild table and map, then refresh */
      if(activeBin==-1)
      {        
        String msg= "You must select the peak before you can add a peak."; 
        setStatusMsg(msg, Color.red);
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      /* if activeBin!=-1, then remove the peak from the peak table */
      /* Shrink the list */  
      int
        gI,
        idxJ= 0,
        nSteps= calib.maxPeaks;
      boolean deleteFlag= false;      
      int tmpGV[]= new int[MAX_ND_STEPS];
      for(int i=0; i<nSteps; i++)
      { /* look for the bin to insert the new peak */
        gI= calib.ndWedgeGrayValues[i];
        if(activeBin==gI)
        { /* don't copy the deleted peak to the new list */
          deleteFlag= true;
          continue; 
        }     
        /* Just copy non-deleted peaks into the list. */
         tmpGV[idxJ++]= gI;
      } /* look for the bin to insert the new peak */
      if(deleteFlag)
      {
        --(calib.maxPeaks);
        calib.ndWedgeGrayValues= tmpGV;
      }
      else
      {
        String msg= "You did not select one of the red peaks in the histogram";
        setStatusMsg(msg, Color.red);
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      updatePeakCalTable();     
      /* Regenerate the mapGrayToOD[] from the peak table */
      if(reExtrapolateCalibration())  
      { /* repaint IFF succeed in creating a new map */
        calibChangedFlag= true;
        setStatusMsg("Deleted peak from peak table and calibration", Color.black);
        repaint();       
      }  
      else 
        calibChangedFlag= false;   /* Don't save bad extrapolations */ 
      activeBin= -1;
      
    } /* Delete peak, rebuild table and map, then refresh */
    
    else if (cmd.equals("Refresh peak table"))
    { /* Copy the table text data to the calib.ndWedgeXXXXvalues[] data*/
      /* Copy spot list to the histogram and calibration peak table. */
      if(getPeaksByMeasFlag)
      { /* backup spotList[0:nSpots-1] */
        if(iData.idSL.nSpots==0)
          calib.maxPeaks= 0;
        else if(!genPeaksFromMeasSpotList())
        { /* problem with too many spots */
          return;
        }        
        /* Update the Gray-peaks table text fields from calib data */
        updatePeakCalTable();
      } /* backup spotList[0:nSpots-1] */
        
      if(refreshPeakTable(true)); 
      { /* repaint IFF succeed in creating a new map */
        calibChangedFlag= true;
        setStatusMsg("Refreshed peak table and calibration", Color.black);
        repaint();       
      }
    } /* Copy the table text data to the calib.ndWedgeXXXXvalues[] data*/
    
    else if (cmd.equals("Save calibration state"))
    { /* make sure we write out the .cal file */
      boolean flag= util.writeCalibrationFile(iData);
      if(flag)
        setStatusMsg("Saved calibration file", Color.black);
      else
      {
        String msg= "Problem saving calibration file - not saved";
        setStatusMsg(msg, Color.red);
        util.popupAlertMsg(msg, flk.alertColor);
      }
       calibChangedFlag= false;
       /* Force the backup to be the same as the new calibration */
       calibBKUP.restoreFromClone(calib);   
    } /* make sure we write out the .cal file */
        
    else if(cmd.equals("SaveAs GIF"))
    { /* Save plot as GIF image */
      Popup popup= flk.evMu.popup;
      String
        oGifFileName= flk.userDir+"tmp"+flk.fileSeparator+
                      "SaveAsHistogram.gif",
        flkStateFile= popup.popupFileDialog(oGifFileName,
                                            "Enter GIF file name",
                                            false);
      if(oGifFileName!=null)
      {
        drawGifFile(oGifFileName);
        flk.util.showMsg("Saved plot as ["+oGifFileName+"]", Color.black);
      }
    } /* Save plot as GIF image */    
   
  } /* actionPerformed */

  
  /**
   * mouseHandler() - search for the active bin if any
   * @param x mouse position
   * @param y mouse position
   * @return true if found it in the xPainted[] list and set activeBin 
   *   to the index i in [0:255] else set activeBin to -1.
   */
  public boolean mouseHandler(int x, int y)
  { /* mouseHandler */    
    activeBin= -1;
    for(int i=0;i<=255;i++)
      if(x==xPainted[i])
      { /* find closest point */
        activeBin= i;
        return(true);
      }
    return(false);
  } /* mouseHandler */
  
  
  /**
   * close() - close this popup and reset flags if needed
   * [TODO] If they edited the calibration and peak calibration table,
   * and they did not save the calibration (i.e. calibChangedFlag is true),
   * then ask them here and save it if they say yes. If not,
   * then restore the previous calibration.
   */
  public void close()
  { /* close */
    boolean grayValueListChangedFlag= (calib.maxPeaks>0 &&
                                       calibBKUP.initialMaxPeaks==0);
    
    if(calibChangedFlag || grayValueListChangedFlag)
    { /* ask if they want to save the calibration */
      String
        msg2= "Calibration was saved",
        msg=
           "The calibration changed. Do you want to save it before you exit?";
      PopupYesNoDialogBox
                   pyn= new PopupYesNoDialogBox(flk,msg,"Yes","No","Cancel");
     if(pyn.cancelFlag)
       return;
    
      boolean flag= false;
      if(pyn.okFlag)
      { /* save the new calibration */
        flag= util.writeCalibrationFile(iData);
        if(flag)
        {
          util.showMsg("Saved calibration file", Color.black);
          calibChangedFlag= false;
        }
        else 
          msg2= "Problem saving calibration file - calibration not saved";
      } /* save it */
      else
         msg2= "Calibration not saved"; /* "No" means do't try to save */
        
      if(!flag || !pyn.okFlag)
      { /* restore backup calibration */
        calib.restoreFromClone(calibBKUP); 
        //if(calibHistFlag && iData.hist!=null)
        // for(int i=0;i<=255;i++)
        //    iData.hist[i]= calib.hist[i];  /* only copy histogram for ND wedge calib
        //                                    * if the CW ROI previously existed */
        iData.mapGrayToOD= calib.getMapGrayToOD();  /* restore map */
        iData.hasODmapFlag= calib.getHasODmapFlag();
        util.showMsg(msg2, Color.red);
        util.showMsg2(msg2, Color.red);
        
        /* [TODO] if did not have a calibration initially and it is a
         * ND wedge calib, then clear the ndwCW ROI.
         * [CHECK] if needed since calibBKUP may have -1 data...
         */
        if(calibHistFlag && !calib.hasPrevCALflag)
        {
          calib.setWedgeROI(-1,-1,-1,-1);  /* Clear it so can overide with CW */
        }
      } /* restore backup calibration */
    } /* ask if they want to save the calibration */
    
    this.setVisible(false);          /* close window */
    this.isVisibleFlag= false;
    
    /* [CHECK] if we really want to get rid of this or just make invisible*/
    if(iData.dwHist==this)
    { /* finish up the computing window ROI */
      iData.dwHist= null;
    }
    else if(iData.dwCalHist==this)
    { /* finish up the calibration */
      if(getPeaksByMeasFlag)
        iData.idSL.restoreSpotList();  /* This restores the current spot list */
      iData.dwCalHist= null;
    }
    
    this.dispose();               /* kill this instance for good! */
  } /* close */
  
  
  /**
   * mouseDragged() - process mouse event
   * @param e is mouse pressed event
   */
  public void mouseDragged(MouseEvent e)
  { /* mouseDragged */
    int 
      x= e.getX(),
      y= e.getY();
    boolean flag= mouseHandler(x, y);
    
    refreshPeakTable(true);
    if(activeBin!=-1)
    {
      String msg= "Frequency["+activeBin+"]="+hist[activeBin]; 
      setStatusMsg(msg, Color.red);
      repaint();        
    }
    else
      setStatusMsg("");
  } /* mouseDragged */
  
  
  /**
   * mouseReleased() - process mouse event
   * @param e is mouse pressed event
   */
  public void mouseReleased(MouseEvent e)
  { /* mouseReleased */  
    int 
      x= e.getX(),
      y= e.getY();
    boolean flag= mouseHandler(x, y);
    
    refreshPeakTable(true);
    if(activeBin!=-1 && (y>=100 && y<=355))
    { /* Only get it if inside of the histogram plot */
      String msg= "Frequency["+activeBin+"]="+hist[activeBin]; 
      setStatusMsg(msg);
      repaint();        
    }
    else
      setStatusMsg("");
  } /* mouseReleased */  
  
  
  /* others not used at this time */
  public void mousePressed(MouseEvent e)  { }  
  public void mouseClicked(MouseEvent e)  { }
  public void mouseEntered(MouseEvent e)  { }
  public void mouseExited(MouseEvent e)  { }
  public void mouseMoved(MouseEvent e)  { }
  
  
  /**
   * windowClosing() - close down the window.
   * @param e is window closing event
   * @see #close
   */
  public void windowClosing(WindowEvent e)
  { close(); }
  
  
  /* Others not used at this time */
  public void windowOpened(WindowEvent e)  { }
  public void windowActivated(WindowEvent e)  { }
  public void windowClosed(WindowEvent e)  { }
  public void windowDeactivated(WindowEvent e)  { }
  public void windowDeiconified(WindowEvent e)  { }
  public void windowIconified(WindowEvent e)  { }
  
  
} /* end of class DrawHistogram */



