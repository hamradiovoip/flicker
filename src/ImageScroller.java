/* File: ImageScroller.java */

import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.TextEvent;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.image.*;
import java.util.*;
import java.util.EventListener;
import java.lang.*;
import java.io.*; 
import javax.swing.JComponent.*;
import javax.swing.*;


/**
 * ImageScroller class supports a scrollable image for the Left
 * and right images. It is also used for the top flicker window with
 * scrolling disabled. It acquires an image and has a text-title, 
 * image canvas, horizontal and vertical scroll bars, and delay 
 * scrollbar (for flickering). 
 * The ImageScroller is generally created as part of the GUI before
 * the image has been loaded. Therefore, we need to do several 
 * setXXXX() fct calls after the image has been loaded to tell
 * the ImageScroller "stuff" it will need.
 *
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

public class ImageScroller extends Panel 
       implements AdjustmentListener, TextListener
{
  private Flicker 
    flk;  
  private Util 
    util;       
  
  /** last image scroller - used in new operation
   * "flicker", "left", "right"
   */
  public static String
    lastISName;	    
  
  /* --- <STATE> part of the image scroller instance --- */
  /** Image data for mapGraytoOD[] that will be set later... */
  public ImageData
    iData;
  /** width of image from iData.iWidth or zoomed data */
  public int
    isWidth;
  /** height of image from iData.iHeight or zoomed data */
  public int
    isHeight;
  /** SliderState threshold parameters associated with the image */
  public SliderState 
    state;
  
  /* --- GUI part of the image scroller instance --- */  
  /** pointer to the associated image canvas */
  public ScrollableImageCanvas 
    siCanvas;  
  /** opt. holds vs & Delay scroll box SOUTH */
  public Panel
    southPanel;
  /** opt. holds Delay scroll label & bar*/
  public Panel
    dPanel;			       
  /** opt. label for image delay */  
  public JLabel
    delayLabel; 
  /** opt. scroll bar for image delay */
  public Scrollbar 
    delayBar= null;
       
  /** image title placed above canvas */
  public TextField
    txtField;			     
     
  /** preferred width of canvas */
  public int
    preferredWidth= 0;  
  /** preferred height of canvas */
  public int
    preferredHeight= 0; 
  /** prewired scroller steps */
  public int
    steps= 100;		     
  /** thickness of scroller */
  public int
    pagestep= 1;	     
  
  /** set if xObj,yObj is set */
  public boolean
    img_selectedFlag; 
  /** create and use scrollers flag */
  boolean
    useScrollBarsFlag;
  /** */
  boolean
    repackFlag;
  /** Name of GIF file used in "SaveAs" of oImg to local GIF file */
  public String
    oGifFileName;  
  /** obj name "left", "right" or "both". */
  public String
    name= "none";
  /** current title for image scroller associated with iData */
  public String	  	 
    title;
  /** scroller for the canvas, siCanvas */
  ScrollPane
    sp;  
  /** deals with scroller events */
  Adjustable
   aH;
  /** deals with scroller events */
  Adjustable 
    aV;
  /** keep track of ScrollPostions */
  Point
    scrollPos;
  /** calc ScrollPostions */
  int
    oldIWidth;  
  /** calc ScrollPostions */
  int
    oldIHeight;  
  /** Instance of Brightness/Contrast Filter. It is reused across
   * images for this image scroller.
   */
  BrightnessContrastFilter
    bcImgF;
  /** X cursor position mapped to image */   
  int
    xObj= -1;
  /** Y cursor position mapped to image */
  int
    yObj= -1;
  /** guard region flag */
  public boolean 
    guardRegionFlag= false;   
  /** size of right & left guard region */
  public int 
    guardWidth= 0;
  /** size of top & bottom guard region */
  public int 
    guardHeight= 0;
  /** size of img with guard region */
  public int 
    guardImgWidth= 0;
  /** size of img with guard region */
  public int 
    guardImgHeight= 0;  
  
  /**
   * ImageScroller() - constructor. Resize to prefered size.
   * @param flk is instance of flicker
   * @param name
   * @param title
   * @param iData is current ImageData iData - this may change for 
   * flicker Image Scroller 
   * @param preferredWidth is size of canvas
   * @param preferredHeight
   * @param def_delay is Flicker delay scroll bar values
   * @param min_delay
   * @param max_delay
   * @param useScrollBarsFlag
   */
  public ImageScroller(Flicker flk, String name, String title, 
                       ImageData iData, int preferredWidth,
                       int preferredHeight, int def_delay, int min_delay,
                       int max_delay, boolean useScrollBarsFlag)
  { /* ImageScroller */
    this.flk= flk;
    this.name= name;
    
    this.title= (title==null) ? "" : title;
    this.iData= iData;
    this.img_selectedFlag= false;    
    this.useScrollBarsFlag= useScrollBarsFlag;
    this.isWidth= iData.iWidth;       /* set to original image size */
    this.isHeight= iData.iHeight; 
    this.preferredHeight= preferredHeight;
    this.preferredWidth= preferredWidth;
    this.bcImgF= new BrightnessContrastFilter(flk);    
    
    iData.zoomedWidth= 0;
    iData.zoomedHeight= 0;
    
    if(name.equals("left") || name.equals("right"))
      iData.title= title;
    
    if(iData.state!=null)
      iData.state.delay= def_delay;
    
    oGifFileName= null;       /* Name of GIF file used in "SaveAs" of oImg */
    repackFlag= false;
    this.util= flk.util;
    this.util.gcAndMemoryStats("ImageScroller():before new scrollpane & siCanvas");
    setLayout(new BorderLayout(0,0)); /* make scroll bars flush to image*/
    
    if(useScrollBarsFlag)
    { /* adding scrollbar and listener */
      southPanel= new Panel();
      southPanel.setLayout(new GridLayout(2,1, /*R,C*/ 1,1 /*gap*/));
      add("South", southPanel);

      if(max_delay>min_delay && min_delay>0 &&
         def_delay>=min_delay && def_delay<=max_delay)
      { /* create display time delay scroll bar */
        dPanel= new Panel();
        dPanel.setLayout(new GridLayout(1,2,2,2));
        /* dPanel.setLayout(new FlowLayout(FlowLayout.LEFT),2,2); */
        southPanel.add(dPanel);
        
        delayLabel= new JLabel("Delay:" + def_delay/1000.0 + "Sec");
        delayLabel.setToolTipText("Time to display this image when flickering");
        delayBar= new Scrollbar(Scrollbar.HORIZONTAL,
                                def_delay/10,  /* init. value */
                                flk.bGui.THICKNESS,
                                min_delay/10, /* minimum value */
                                max_delay/10  /* max value */
                               );
        delayBar.addAdjustmentListener(this);
        dPanel.add(delayLabel);
        dPanel.add(delayBar);
      }
    } /* adding scrollbars and listener */
    
    txtField= new TextField(this.title,25);
    add("North",txtField);
    /* Note: set flkIS (w/ no scroll-bars) to read-only */
    txtField.addTextListener(this);  /* DEPRICATED? */
    txtField.setEditable(false);     /* or could use fct(useScrollBarsFlag) */

    /* Add new scrollPane */
    if(!useScrollBarsFlag)
      this.sp= new ScrollPane(ScrollPane.SCROLLBARS_NEVER);
    else
      this.sp= new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);    
    
    /* Special listeners for ScrollPane */
    aH= sp.getHAdjustable();
    aV= sp.getVAdjustable();
    aH.addAdjustmentListener(this);
    aV.addAdjustmentListener(this);
    
    siCanvas= new ScrollableImageCanvas(iData.iImg, this, flk, 
                                        useScrollBarsFlag);
    siCanvas.setBackground(Color.white);
    siCanvas.bcImgF= this.bcImgF;
    add("Center", sp);
    sp.add(siCanvas);    
    
    setCanvasSize(preferredWidth, preferredHeight);
    scrollPos= new Point(0,0);  
    
    guardRegionFlag= false;
    siCanvas.guardRegionFlag= false;   
    
    repaint();
  } /* ImageScroller */  
    
  
  /**
   * setGuardRegion() - force siCanvas to create guard region
   * @param newImgWidth new image width
   * @param newImgHeight new image height
   * @param zoomMag magification factor
   */
   public void setGuardRegion(int newImgWidth, int newImgHeight, double zoomMag) 
   { /* setGuardRegion */
         
      /* Resize img that already has guard region */     
      Dimension guardSize= getGuardRegionDimension(zoomMag);
      
      siCanvas.guardWidth= (int) guardSize.width;
      siCanvas.guardHeight= (int) guardSize.height;          
      
      guardWidth= siCanvas.guardWidth;
      guardHeight= siCanvas.guardHeight;      
     
      siCanvas.guardImgWidth= (siCanvas.guardWidth) + newImgWidth;           
      siCanvas.guardImgHeight= (siCanvas.guardHeight) + newImgHeight;   
      
      guardImgWidth= siCanvas.guardImgWidth;
      guardImgHeight= siCanvas.guardImgHeight;         
      
      if(flk.NEVER)
        System.out.println("addGuardRegion()side="+siCanvas.isName+" guardWidth="+siCanvas.guardWidth+
                           "  guardHeight="+siCanvas.guardHeight+
                           "  guardImgWidth="+siCanvas.guardImgWidth+
                           "  guardImgHeight="+siCanvas.guardImgHeight+
                           "  preferredWidth="+preferredWidth+
                           "  preferredHeight="+preferredHeight+
                           "  newImgWidth="+newImgWidth+
                           "  newImgHeight="+newImgHeight);   
      
      resizeImageCanvas(iData.iImg, siCanvas.guardImgWidth, 
                        siCanvas.guardImgHeight, iData.mag);
      
      /* NOTE: must copy back since resizeImageCanvas() does a new siCanvas */
      siCanvas.guardWidth= guardWidth;
      siCanvas.guardHeight= guardHeight;
      siCanvas.guardImgWidth=  guardImgWidth;
      siCanvas.guardImgHeight= guardImgHeight;  
      siCanvas.guardRegionFlag= guardRegionFlag;     
   } /* setGuardRegion */
  
   
  /**
   * addGuardRegion() - set up siCanvas for guard region
   * @param guardRegionFlag boolean to turn on/off guard region
   */
  public void addGuardRegion(boolean guardRegionFlag) 
  { /* addGuardRegion */    
  
    if(guardRegionFlag)
    { /* true */
      if(this.guardRegionFlag == true)
        return;/* already on */
      else
        this.guardRegionFlag= guardRegionFlag;
      
      /* resize img with guard region */
      double mag= iData.mag;
      Dimension guardSize= getGuardRegionDimension(1.0);
      
      siCanvas.guardWidth= (int) guardSize.width;
      siCanvas.guardHeight= (int) guardSize.height;          
      
      guardWidth= siCanvas.guardWidth;
      guardHeight= siCanvas.guardHeight;
      int
        w= 0,
        h= 0;    
   
      if(iData!=null)
      {        
        if(iData.iImg!=null)
        {         
          w= (int) (iData.iImg.getWidth(this)*mag);
          h= (int) (iData.iImg.getHeight(this)*mag);
        }
        else
          System.out.println("addGuardRegion() iImg is null!");
      }
      else
        System.out.println("addGuardRegion() iData is null!");                
        
      siCanvas.guardImgWidth= (siCanvas.guardWidth)+w;           
      siCanvas.guardImgHeight= (siCanvas.guardHeight)+h;   
      
      guardImgWidth= siCanvas.guardImgWidth;
      guardImgHeight= siCanvas.guardImgHeight;         
      
      if(flk.NEVER)
        System.out.println("addGuardRegion()side="+siCanvas.isName+" guardWidth="+siCanvas.guardWidth+
                           "  guardHeight="+siCanvas.guardHeight+
                           "  guardImgWidth="+siCanvas.guardImgWidth+
                           "  guardImgHeight="+siCanvas.guardImgHeight+
                           "  preferredWidth="+preferredWidth+
                           "  preferredHeight="+preferredHeight+
                           "  w="+w+
                           "  h="+h);   
      
      resizeImageCanvas(iData.iImg, siCanvas.guardImgWidth, 
                        siCanvas.guardImgHeight, iData.mag);
      
      /* must copy back since resizeImageCanvas does a new siCanvas */
      siCanvas.guardWidth= guardWidth;
      siCanvas.guardHeight= guardHeight;
      siCanvas.guardImgWidth=  guardImgWidth;
      siCanvas.guardImgHeight= guardImgHeight;  
      siCanvas.guardRegionFlag= guardRegionFlag;
       
    } /* true */
    else
    { /* false */
       if(this.guardRegionFlag == false)
         return;/* already off */
       else
       { /* revert back to img without guard region */  
         
         this.guardRegionFlag= guardRegionFlag;         
         
         /* if zoomed have to change the size accordingly */              
         if(iData.mag!=0.0)
         {/* zoomed */
            int
              w= (int)(iData.iImg.getWidth(this)* iData.mag),
              h= (int)(iData.iImg.getHeight(this)* iData.mag);
              
            resizeImageCanvas(iData.iImg,w,h,iData.mag);
           
         }/* zoomed */
         else
         {
           int
             w= iData.iImg.getWidth(this),
             h= iData.iImg.getHeight(this);
            
           resizeImageCanvas(iData.iImg,w,h,iData.mag);           
         }          
       } /* revert back to img without guard region */  
       repaint();        
    } /* false */
   
    siCanvas.guardRegionFlag= guardRegionFlag; 
  
  } /* addGuardRegion */    
  
   
  /**
   * getGuardRegionDimension() - return guard size
   * @return guard region point
   */
  public Dimension getGuardRegionDimension(double mag)
  { /* guardRegionCorrection */
    
    int 
      w= 0,
      h= 0;   
   
    if(iData!=null)
    {
      if(iData.iImg!=null)
      {
        w= (int) (iData.iImg.getWidth(this)*mag);
        h= (int) (iData.iImg.getHeight(this)*mag);
      }
    }  
    
    w= (int) preferredWidth;
    h= (int) preferredHeight;    
    Dimension d= new Dimension(w,h);
    
    return(d);
    
  } /* guardRegionCorrection */  
  
  
  /**
   * resizeImageCanvas() - Reset Image, used in zoom and dezoom.
   * @param img is the image to set to be drawn
   * @param newWidth tof the canvas
   * @param newHeight tof the canvas
   * @param mage of the canvas
   */
  public synchronized void resizeImageCanvas(Image img, 
                                             int newWidth, int newHeight,
                                             double mag)
  { /* resizeImageCanvas */    
    if(img!=null)
    { /* img not null */    
    
      util.gcAndMemoryStats("resizeImageCanvas():start");
      if(flk.NEVER)
        System.out.println("resizeImageCanvas.1 : start");
      oldIWidth=  this.isWidth; /* save old value */
      oldIHeight= this.isHeight;
      double 
        diffX,
        diffY;
      Point
        curScrPt= sp.getScrollPosition(); /* get the current sp position */
      Point
        curPos= new Point(siCanvas.curPos);  
      Point 
        flkCurPos= new Point(siCanvas.flkCurPos);
      Adjustable
        ha= sp.getHAdjustable(),
        va= sp.getVAdjustable();
      int
        hMax= ha.getMaximum() - ha.getVisibleAmount(),
        vMax= va.getMaximum() - va.getVisibleAmount();
           
      if(oldIWidth < newWidth) 
      {
        diffX= (double)((double) oldIWidth / (double) newWidth);
        diffY=  (double) ((double) oldIHeight / (double) newHeight);
      }
      else 
      {
        diffX= (double)((double) newWidth / (double) oldIWidth);
        diffY=  (double) ((double) newHeight / (double) oldIHeight);
      }
      
      int newHMax= (int) (hMax / diffX);
      int newVMax= (int) (vMax / diffY);
      
      double xRatio= (double)curScrPt.x/(double)hMax;
      double yRatio= (double)curScrPt.y/(double)vMax;
      
      int x= (int) Math.round(newHMax * (double) xRatio);
      int y= (int) Math.round(newVMax * (double) yRatio);
      
      scrollPos= new Point(x,y);
      
      if(flk.NEVER)
        System.out.println("IS: resizeImageCanvas.1: hMax="+hMax + " diffX="+
                           diffX+" newHMax="+newHMax+" xRatio="+xRatio+
                           " x="+x+" y="+y+" xObj= "+xObj+" yObj="+yObj); 
      this.isWidth= newWidth;
      this.isHeight= newHeight;
      int
        tmpX= 0,
        tmpY= 0;
      
      /* trial obj */
      if(mag!=1.0) 
      {
        tmpX= (int) (siCanvas.curPos.x * mag);
        tmpY= (int) (siCanvas.curPos.y * mag);
      }
      else 
      {
        tmpX= siCanvas.curPos.x;
        tmpY= siCanvas.curPos.y;
      }

      /* Save the cursor position before remove the old canvas */
      this.xObj= siCanvas.xObj;
      this.yObj= siCanvas.yObj;
      
      /* Remove the old canvas */
      iData.threadIsPaintFlag= false;
      sp.remove(siCanvas); /* take siCanvas out of sp */
      remove(sp);          /* take sp out of this */
      this.sp= null;
      
      /* create new one with propwer size img etc */
      ScrollableImageCanvas
        siCanvasNew= new ScrollableImageCanvas(img, this, flk,  true);
      
      /* must copy all variables so we do not miss anything */
      siCanvas.copy(siCanvasNew);
      
      /* del old one */
      siCanvas.cleanup();
      siCanvas= null;
      
      /* copy new one to old name */
      siCanvas= siCanvasNew;
      aH.removeAdjustmentListener(this);
      aV.removeAdjustmentListener(this);
      aH= null;
      aV= null;
      util.gcAndMemoryStats("resizeImageCanvas():before new scrollpane & siCanvas");
      
      /* copy associated elements for new image */     
      siCanvas.sicWidth= newWidth;
      siCanvas.sicHeight= newHeight;
      iData.zoomedWidth= newWidth;
      iData.zoomedHeight= newHeight;
      
      iData.mag= mag;
      siCanvas.xObj= this.xObj;
      siCanvas.yObj= this.yObj;
      siCanvas.curPos= curPos;           
      siCanvas.flkCurPos= flkCurPos;
      
      if(!useScrollBarsFlag)
        this.sp= new ScrollPane(ScrollPane.SCROLLBARS_NEVER);
      else
        this.sp= new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);     

      add("Center", sp);
      sp.add(siCanvas);
      flk.pack();   /* have to do this to get the new sp size */
     
      /* Repaint and reset the scroller cursor */
      iData.threadIsPaintFlag= true;          
      
      aH= sp.getHAdjustable();            
      aV= sp.getVAdjustable();
      
      aH.addAdjustmentListener(this);
      aV.addAdjustmentListener(this);
      
      /* recalc based on new size */
      int
        hMaxNew= aH.getMaximum() - aH.getVisibleAmount(),
        vMaxNew= aV.getMaximum() - aV.getVisibleAmount();    
           
      x= (int) Math.round(hMaxNew * (double) xRatio);
      y= (int) Math.round(vMaxNew * (double) yRatio);          
      
      scrollPos= new Point(x,y);   
      
      if(flk.NEVER)
        System.out.println("IS: resizeImageCanvas.2: hMaxNew="+hMaxNew + " diffX="+
                           diffX+" vMaxNew="+vMaxNew+" xRatio="+xRatio+
                           " x="+x+" y="+y);
       
      /* reposition scrollers/xObj in new zoomed img */
      siCanvas.setTrialLMS(tmpX,tmpY);     
      repaint();
      flk.repaint();
      util.gcAndMemoryStats("resizeImageCanvas():end");
      if(flk.NEVER)
        System.out.println("resizeImageCanvas.2 : end");
    } /* img not null */          
        
  } /* resizeImageCanvas */ 
      
           
  /**
   * getPreferredSize() - getPreferredSize, needed for sizing canvas and
   * ScrollPane correctly. NOTE: Used offsets since scroll bars take up 
   * space from the canvas.
   * @return canvas size
   */
  public Dimension getPreferredSize() 
  { /* getPreferredSize */      
    Dimension
      preferredCanvasSize;
    /* add extra space for scrollable canvas since scrollers take up space */
    if(useScrollBarsFlag)
      preferredCanvasSize= new Dimension(flk.canvasSize+20,flk.canvasSize+70);
    else
      preferredCanvasSize= new Dimension(flk.canvasSize,flk.canvasSize);
    repaint();
    return(preferredCanvasSize);  
  } /* getPreferredSize */
    
    
  /**
   * setCanvasSize() - change the size of this Canvas. 
   * We assume that the legality of the new size is checked
   * prior to this method being called.
   * @param size is new size
   */
   public void setCanvasSize(int size)
   { /* setCanvasSize */
     if(size>0) 
     { /* force canvas to preferred size */
       setCanvasSize(size,size);
     } /* force canvas to preferred size */
     else
     {
       Dimension d= getPreferredSize();
       setCanvasSize(d.width, d.height);           
     }       
   } /* setCanvasSize */
   
          
  /**
   * setCanvasSize() - change the size of this Canvas.
   * We assume that the legality of the new size is checked
   * prior to this method being called.
   * @param preferredWidth is new width
   * @param preferredHeight is new height
   */
   public void setCanvasSize(int preferredWidth, int preferredHeight)
   { /* setCanvasSize */
     if(preferredWidth>0 && preferredHeight>0) 
     { 
       this.preferredWidth= preferredWidth;
       this.preferredHeight= preferredHeight;
       if(isWidth>0 && isHeight>0)
         siCanvas.setCanvasSize(isWidth, isHeight); 
       else
         siCanvas.setCanvasSize(preferredWidth, preferredHeight);
       
       this.setSize(preferredWidth, preferredHeight);           
     } 
   } /* setCanvasSize */
   
   
  /**
   * setImageData() - change the ImageData for the panel.
   * Note: this method is needed since iData may not exist
   * when the ImageScroller was created.
   * [DEPRICATED?]
   * @param iData is Image data
   */
  public synchronized void setImageData(ImageData iData)
  { /* setImageData */
    this.iData= iData;
    
    this.isWidth= iData.iWidth;
    this.isHeight= iData.iHeight;
    iData.bnd.setMapGrayToOD(iData.mapGrayToOD);
    
  } /* setImageData */
  
    
  /**
   * setImageData() - change the ImageData for the panel.
   * Note: this method is needed since iData may not exist
   * when the ImageScroller was created.
   * [DEPRICATED?]
   * @param iData is Image data
   * @param xyImg is the position of the image in the scrollable canvas
   */
  public synchronized void setImageData(ImageData iData, Point xyImg)
  { /* setImageData */
    setImageData(iData);
    siCanvas.setImgPosition(xyImg.x, xyImg.y);
  } /* setImageData */
  
  
  /**
   * setTitle() - change the title for the panel.
   * You can do a temporary change or a permanent change.
   * @param title to use
   * @param saveTitleFlag if keep title change
   */
  public void setTitle(String title, boolean saveTitleFlag)
  {
    if(saveTitleFlag)
      this.title= title;
    if(name.equals("left") || name.equals("right"))
      iData.setTitle(title);
    txtField.setText(title);
  }
  
  
  /**
   * getDelay() - get the Image delay time
   */
  public int getDelay()
  { return(iData.state.delay); }
  
  
  /**
   * setImgPosition() - set the Image position. Don't repaint here...
   * @param xyImg is current image position
   */
  public void setImgPosition(Point xyImg)
  { siCanvas.setImgPosition(xyImg.x, xyImg.y); }
  
  
  /**
   * setImgPosition() - set the Image position. Don't repaint here...
   * @param xImg is current image position
   * @param yImg is current image position
   */
  public void setImgPosition(int xImg, int yImg)
  { siCanvas.setImgPosition(xImg, yImg); }
  
  
  /**
   * getImgPosition() - get the Image position.
   * @return image position
   */
  public Point getImgPosition()
  { return(siCanvas.getImgPosition()); }
  
  
  /**
   * setObjPosition() - set the object position. Don't repaint here...
   * @param xyObj is object position
   */
  public void setObjPosition(Point xyObj)
  { siCanvas.setObjPosition(xyObj.x, xyObj.y); }
  
  
  /**
   * setObjPosition() - set the object position. Don't repaint here...
   * @param xObj is object position
   * @param yObj is object position
   */
  public void setObjPosition(int xObj, int yObj)
  { siCanvas.setObjPosition(xObj, yObj); }
  
  
  /**
   * setcurrentIS() - set the currentIS, used when flickering
   * @param currentIS is current ImageScroller (right or left)
   */
  public void setcurrentIS(ImageScroller currentIS)
  { siCanvas.currentIS= currentIS; }
  
  
  /**
   * getObjPosition() - get the object position.
   * @return current object position
   */
  public Point getObjPosition()
  {
    Point xyObj= siCanvas.getObjPosition();    
    return(xyObj);
  }
  
  
  /**
   * getXObjPosition() - get the X object position.
   * @return current X object position
   */
  public int getXObjPosition()
  {
    int xObj= siCanvas.xObj;    
    return(xObj);
  }
  
  
  /**
   * getYObjPosition() - get the X object position.
   * @return current X object position
   */
  public int getYObjPosition()
  {
    int yObj= siCanvas.yObj;    
    return(yObj);
  }
  
    
   /**
    * getScrollPosition() - return scroll positions
    * @return Point of scroll position
    */
   public Point getScrollPosition()
   {     
      int
        hsX= getScrollbarHeight(),
        vsY= getScrollbarWidth();
      Point pt= new Point(hsX, vsY);
      return(pt);
   }
    
   /**
    * getScrollBarWidth() - return scroll width position
    * @return int scroll position
    */
   public int getScrollbarWidth()
   { /* getScrollbarWidth */  
     if(siCanvas==null)
       return(0);
     
     int nChild= sp.getComponentCount();
     if(nChild<=0)
       return(0);
     
     Point pt= sp.getScrollPosition();
     int w= pt.x;
     return(w);
   } /* getScrollbarWidth */
    
   
   /**
    * getScrollBarHeight() - return scroll height position
    * @return int scroll position
    */
   public int getScrollbarHeight()
   { /* getScrollbarHeight */
      Point pt= sp.getScrollPosition();
      int h= pt.y;
      return(h);
   } /* getScrollbarHeight */
    
   
   /**
    * getMaxScrollBarWidth() - return scroll max width
    * @return int scroll max position
    */
   public int getMaxScrollBarWidth()
   { /* getMaxScrollBarWidth */
      int w= sp.getVScrollbarWidth();       
      return(w);
   } /* getMaxScrollBarWidth */
    
   
   /**
    * getMaxScrollBarHeight() - return scroll max height
    * @return int scroll max position
    */
   public int getMaxScrollBarHeight()
   {
      int h= sp.getHScrollbarHeight();
      return(h);
   }
   
  
  /**
   * setBndState() - set the boundary state in the imageCanvas
   * @param bndOpenFlag open/close boundary state flag
   */
  public void setBndState(boolean bndOpenFlag)
  { iData.bnd.setBndState(bndOpenFlag); }
  
  
  /**
   * getValidFeaturesFlag() - get the valid features set after measurement
   * @return valid features flag
   */
  public boolean getValidFeaturesFlag()
  { return(iData.bnd.getValidFeaturesFlag()); }
  
  
  /**
   * startMeasurement() - start measurement of object.
   * @param measType type specified as "Quant" or "Bkgrd"
   */
  public void startMeasurement(String measType)
  { /* startMeasurement */
    if(iData.iPix==null)
    { /* make sure the input pixels exist */
      iData.checkAndMakeIpix(true);
    }
    
    iData.bnd.startMeasurement(measType, isWidth, isHeight);
  } /* startMeasurement */
  
  
  /**
   * finishMeasurement() - finish measurement and compute features
   */
  public void finishMeasurement()
  {  /* finishMeasurement */
    iData.bnd.setMapGrayToOD(iData.mapGrayToOD); /* set up anyways. */
    iData.bnd.finishMeasurement(isWidth, isHeight,iData.iPix,
                                iData.pixelMask);
  } /* finishMeasurement */
  
  
  /**
   * cvFeatures2str() - cvt object features to printable string
   * @param imgFile to display
   * @return printable features string
   */
  public String cvFeatures2str(String imgFile)
  { return(iData.bnd.cvFeatures2str(imgFile)); }
  
  
  /**
   * setLandmarksTextListToDraw() - set text list including color 
   * and position. To disable the list, just set nTextItems to 0.
   * @param nTextItems is the number of text items
   * @param color_text is list of text label colors
   * @param text is list of text labels
   * @param font_text is list of fonts of labels
   * @param x_text is list of x coordinates of labels
   * @param y_text is list of y coordinates of labels
   */
  public void setLandmarksTextListToDraw(int nTextItems, Color color_text[],
                                         String text[], Font font_text[],
                                         int x_text[], int y_text[])
  { /* setLandmarksTextListToDraw */
    if(siCanvas!=null)
      siCanvas.setLandmarksTextListToDraw(nTextItems, color_text, text,
                                        font_text, x_text, y_text);
  } /* setLandmarksTextListToDraw */
    
    
  /**
   * clearLandmarkTextListToDraw() - disable text list of things to draw in canvas.
   */
  public void clearLandmarkTextListToDraw()
  { 
    if(siCanvas!=null)
      siCanvas.clearLandmarkTextListToDraw(); // Could be a problem if siCanvas is null TODO
  } 
    
  
  /**
   * getNbrTextItems() - return actual number of text items to display
   * @return nbr text items to display
   */
  public int getNbrTextItems()
  {return(siCanvas.nTextItems);}  
  
  
  /**
   * getColorText() -  text list items colors
   * @return text list items colors
   */
  public Color[] getColorText()
  {return(siCanvas.color_text);}
  
  
  /**
   * getText() - text list items 
   * @return text list items  
   */
  public String[] getText()
  {return(siCanvas.text);}   
   
    
  /**
   * getFontText() - get text list fonts 
   * @return text list fonts 
   */
  public Font[] getFontText()
  {return(siCanvas.font_text);}
    
      
  /**
   * getXText() - x text list items positions  
   * @return x text list items positions 
   */
  public int[] getXText()
  {return(siCanvas.x_text);}
    
  
  /**
   * getYText() - y text list items positions 
   * @return y text list items positions 
   */
  public int[] getYText()
  {return(siCanvas.y_text);}
  
  
  /**
   * processBCimage() - process colormap image by running the filter.
   * NOTE: need to do repaint() just after this call.
   * NOTE: we must sync this so that it completes a full image computation
   * before we do it again.
   * If not allowing image transforms (i.e. !flk.allowXformFlag), then
   * use the id.iImg. Otherwise, if we are composing output images then
   * use id.oImg else id.iImg.
   * It creates the id.bcImg in the process if successful
   * @param id is the ImageData to get the image to process
   * @return true if succeed
   */
  public synchronized boolean processBCimage(ImageData id)
  { /* processBCimage */
    boolean flag= siCanvas.processBCimage(id);
    return(flag);    
  } /* processBCimage */
   
  
  /**
   * drawImageTitle() - draw title in image.
   * This uses the mouse position in the canvas to get
   * the [x,y,g(x,y)/od(x,y)] values and set it to:
   *       <title> + (x,y,g) + "[use]"
   */
  public void drawImageTitle()
  {siCanvas.drawImageTitle();}
  
    
  /**
   * getImgCursor() - get the (x,y) scrollable cursor for this image
   * If there is no cursor, then return (0,0).
   * @return image cursor point
   */
  public Point getImgCursor()
  { /* getImgCursor */
    Point cursor;    
    if(siCanvas!=null)
      cursor= sp.getScrollPosition();
    else
      cursor= new Point(0,0);                       /* NO-OP */
    return(cursor);
  }  /* getImgCursor */
  
  
  /**
   * getImgCursorMax() - get the (x,y) scrollable cursor Maximum for image
   * If there is no cursor, then return (0,0).
   * @return image cursor point maximum
   */
  public Point getImgCursorMax()
  { /* getImgCursorMax */
    Point curMax; 
    if(siCanvas!=null)
    {
      Dimension d= siCanvas.getMaximumSize();
      curMax= new Point(d.width,d.height);
    }
    else
      curMax= new Point(0,0);
    
    return(curMax);
  } /* getImgCursorMax */
  
  
  /**
   * updateClickableCanvas() - perform canvas update operations 
   * invoked by various controls
   */
  void updateClickableCanvas()
  {
    if(siCanvas!=null)
      siCanvas.updateClickableCanvas(); 
  }
  
  
  /**
   * adjustmentValueChanged() - handle ImageScroller scroll events.
   * Handle ImageScroller scroll events & cause repaint
   * of canvas. If we are using the scroll bar for this canvas, then set
   * the obj position for use by other canvas for the same image.
   * Handle the delay scrollbar as well if it exists.
   * @param e is adjustment event for scroll bars
   */
  public void adjustmentValueChanged(AdjustmentEvent e)
  { /* adjustmentValueChanged */
    String arg= e.toString();	/* arg for the event */
    Object source= e.getSource();
    
     if(source==aV || source==aH)
     {       
       scrollPos= sp.getScrollPosition(); /* get the current sp position */ 
       repaint();
       return;
     }    
           
    /* Test if fooling with the image canvas scroll bars */
    if(delayBar!=null && source==delayBar)
    { /* Image delays scrollbar */
      int
        val= ((Scrollbar)source).getValue(),
        mSec= val*10;
      String msg;
      
      val= Math.max(1,val);
      iData.state.delay= mSec;
      msg= "Delay:" + mSec/1000.0 + "Sec";
      delayLabel.setText(msg);
      return;
    }
    /* If NOT using the scroll bar, then set the obj position
     * using the scroll bars cursors in the rough position.
     */
    else if(source==sp && siCanvas!=null) 
    {
      Dimension dim= siCanvas.getMaximumSize();
      Point pt= new Point(sp.getScrollPosition());
      int
        vMax= dim.height,
        hMax= dim.width,
        hVal= pt.x,
        vVal= pt.y;
      
      int xScrCursor= - (iData.iImg.getWidth(this) - getSize().width) /
                          hMax * hVal;
      
      int yScrCursor= - (iData.iImg.getHeight(this) - getSize().height) /
                          vMax * vVal;
      siCanvas.setImgPosition(xScrCursor, yScrCursor);
      siCanvas.updateImageScrollableTitles(true);
    }
    paintSiCanvas();  
 
  } /* adjustmentValueChanged */
  
        
  /**
   * textValueChanged() - process text change events in image title 
   * text area
   * NOTE: this is DEPRICATED!
   * @param e is text event
   */
  public void textValueChanged(TextEvent e)
  { /* textValueChanged */
    String arg= e.toString();	/* arg for the event */
    Object source= e.getSource();
  } /* textValueChanged */
     
    
  /**
   * saveAsOverlayFile() - save repainted image as GIF file
   * @param saveAsImgFile is full path name of the GIF file to write
   */
  public void saveAsOverlayFile(String saveAsImgFile)
  { /* saveAsOverlayFile */
    siCanvas.saveAsImgFile= saveAsImgFile; 
    paintSiCanvas();  
  } /* saveAsOverlayFile */
  
    
  /**
   * saveOImgAsGifFile() - save the oImg into a Gif image file in the 
   * < userDir >/tmp/.
   * This sets it up and lets paint() do the heavy lifting...
   * @param defGifFile is the full path GIF output file
   * @param promptForFileFlag if want to have user overide file name
   * @return true if successful, false if unable to generate image file.
   * @see #repaint
   */
  boolean saveOImgAsGifFile(String defGifFile,
                            boolean promptForFileFlag)
  { /* saveOImgAsGifFile */
    if(iData.oImg==null || !flk.allowXformFlag)
    {      
       oGifFileName= null;
       return(false);
    }
     
    /* [1] Get the file name to save it as */
    String
      name= flk.util.getFileNameFromPath(iData.imageFile);
    int idx= name.lastIndexOf("."); /* remove file extension if any */
    if(idx!=-1)
      name= name.substring(0,idx);
    String
      initialGifPath= ((defGifFile!=null)
                         ? defGifFile
                         : flk.userTmpDir +"oImg-"+ name+".gif");
    if(promptForFileFlag)
    {                            
      Popup popup= new Popup(flk);      /* Open the directory browser */
      oGifFileName= popup.popupFileDialog(initialGifPath,
                                          "Enter GIF file name",
                                          false /* SAVE file */);    
    }
    else 
      oGifFileName= initialGifPath;
                               
    if(oGifFileName==null)
      return(false);
        
    /* [2] If drawing to a GIF file, then cvt Image to Gif stream
     * and write it out.
     */
    WriteGifEncoder wge= new WriteGifEncoder(iData.oImg);
    if(wge==null)
    {      
      oGifFileName= null;
      return(false);
    }
    else
    {
      wge.writeFile(oGifFileName);
      util.showMsg("Saved transform image ["+oGifFileName+"]",
                   Color.black); 
    }
        
    return(true);
  } /* saveOImgAsGifFile */
  
    
  /**
    * paintSiCanvas() - paint the scrollable canvas using thread safe method in
    * one place.
    */
  public synchronized void paintSiCanvas() 
  { /* paintSiCanvas */       
    if(iData.threadIsPaintFlag)
      if(siCanvas!=null)
       siCanvas.repaint();         
  } /* paintSiCanvas */
    
  
  /**
   * update() - update without background the scrollable canvas
   * @param g is graphics context
   */
  public void update(Graphics g)
  { paint(g); }
  
  
  /**
   * paintComponent() - repaint scrollable canvas in region defined by scroll bars
   * @param g is graphics context
   */
  public void paint(Graphics g)
  { /* paintComponent */
    // super.paintComponent(g);
    paintSiCanvas();   
  } /* paintComponent */
  
} /* end class: ImageScroller */



/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
/*                 CLASS   ScrollableImageCanvas                  */
/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */

/**
 * ScrollableImageCanvas class supports a scrollable canvas
 * Define a scrollable image canvas. Note the scroll bars may
 * or may not exist and so we need to test for this.
 **/
class ScrollableImageCanvas extends Canvas
      implements MouseListener, MouseMotionListener, KeyListener
{
  
  /** new boundary state */          
  Util
    util;
  Flicker
    flk;
  Info
    info;
  
  /** target Size */
  final int
    tSize= 7;
  /** max # of text items */
  final int   
    MAX_TEXT= 52;	
  /** offset to draw trial obj */
  final int
    X_TRIAL_OBJ_OFFSET= -3; 
   /** offset to draw label */
  final int
    Y_TRIAL_OBJ_OFFSET= 5;  
  /** Parent class */
  public ImageScroller
    is;	
  
  /** name of ImageScroller parent */
  String
    isName;
  /** from time before...  */
  String
    lastlastISName;		        
  /** Name of GIF file to save the next repaint. This is
   * initially null, and is reset to null after it writes the file.
   */
  String
    saveAsImgFile;
  /** Instance of Brightness/Contrast Filter. It is reused across
   * images for this image scroller.
   */
  BrightnessContrastFilter
    bcImgF;
  
  /** actual # text items to display */
  int
    nTextItems= 0;	
  /** + base address of ULHC of clipped image in image canvas used 
   * in last paint */
  int
    xBase= 0;	
  int
    yBase= 0;
  /** + image address (x,y)+(xBase,yBase) */
  int
    xImg= 0;			      
  /** + externally set (x,y) object position*/
  int
    yImg= 0;
  int
    xObj= -1;			      
  int
    yObj= -1;
  
  /** The true position of ROI with respect to the image not canvas */
  int
    imgRoiX1;
  /** The true position of ROI with respect to the image not canvas */
  int
    imgRoiX2;
  /** The true position of ROI with respect to the image not canvas */
  int
    imgRoiY1;
  /** The true position of ROI with respect to the image not canvas */
  int
    imgRoiY2;
  /** img X coords for circle */
  int
    circleX;
  /** img Y coords for circle */
  int
    circleY;
  /** bk grd X */
  int
    bkGrdX;  
  /** bk grd Y */
  int
    bkGrdY;
  /** tic mark X */
  int
    ticX= -1;
  /** tic mark Y */
  int
    ticY= -1;  
  
  /** gray value of (x,y) pixel object */
  int
    gValue= 0;		
  
  /** CTRL/Mouse */
  private boolean
    ctrlMod;		
  /** SHIFT/Mouse */
  private boolean	     
    shiftMod;			      
  /** CTRL-SHIFT/Mouse */
  private boolean
    ctrlShiftMod;		    
  /** ALT/Mouse*/
   private boolean
     altMod;
  /** selected image title color */
  static Color	
    selectedImageColor= Color.blue;
  /** selected image title color */
  static Color	
    unselectedImageColor= Color.black;
  /** text list items colors */
  Color
    color_text[]= new Color[MAX_TEXT]; 
  /** text list items  */
  String
    text[]= new String[MAX_TEXT]; 
  /** text list fonts  */
  Font
    font_text[]= new Font[MAX_TEXT]; 
  /** x text list items positions  */
  int
    x_text[]= new int[MAX_TEXT];  
  /** y text list items positions  */
  int
    y_text[]= new int[MAX_TEXT];
  /** Maximum canvas size */
  Dimension 
    maxCanvasDim;
  /** center point */
  Point
    centerPt;
  /** current scroll position */
  Point
    scrollPos;
  /** dimensions of the view port */
  Dimension
    viewPortSize;
  /** canvas width */
  int
    cWidth;
  /** canvas height */
  int
    cHeight;
  /** image width */
  int
    sicWidth;
  /** image height */
  int
    sicHeight;  
  /** size of right & left guard region */
  public int 
    guardWidth= 0;
  /** size of top & bottom guard region */
  public int 
    guardHeight= 0;
  /** size of img with guard region */
  public int 
    guardImgWidth= 0;
  /** size of img with guard region */
  public int 
    guardImgHeight= 0;  
  
  /** center coords of image, for zoom/dezoom */
  int
    xCtr= 0;
  /** center coords of image, for zoom/dezoom */
  int
    yCtr= 0;    
   /** preferred size of canvas */
  public int
    preferredWidth= 0;  
  /** preferred size of canvas */
  public int
    preferredHeight= 0; 
  /** create and use scrollers flag */
  boolean
    useScrollBarsFlag;
  /** right or left IS that is being flickered */
  public ImageScroller
    currentIS;
  /** keep track of current "+" where last clicked in 1.0 (mag) state */
  public Point
    curPos;
  /* Keep position specifcally for flicker window, need location of last clicked */
  public Point
    flkCurPos;      
  /** guard region flag */
  public boolean 
    guardRegionFlag= false;
  
  
  /**
   * ScrollableImageCanvas() - constructor.
   * @param img is the image to create
   * @param is is the image scroller associated with the image
   * @param flk is the link to the main class.
   */
  ScrollableImageCanvas(Image img, ImageScroller is, Flicker flk,
                        boolean useScrollBarsFlag)
  { /* ScrollableImageCanvas */    
    this.flk= flk;
    this.util= flk.util;
    this.info= flk.info;
    this.is= is;
    this.isName= is.name;
    this.useScrollBarsFlag= useScrollBarsFlag;
    
    /* pass down sizes */
    this.preferredWidth= this.is.preferredWidth;
    this.preferredHeight= this.is.preferredHeight;
    /* Setup boundary measurement state */

    /* Name of GIF file to save the next repaint. This is
     * initially null, and is reset to null after it writes the file.
     */
    saveAsImgFile= null;

    /* [TODO] Setup boundary measurement state */
    
    /* Key and Mouse Listeners */
    this.addKeyListener(this);
    this.addMouseListener(this);
    this.addMouseMotionListener(this); 

    /* Map (x,y) to brightness/contrast */
    cWidth= getSize().width;          /* canvas size */
    cHeight= getSize().height;         /* canvas size */
   
    if(img!=null)
    {
      sicWidth= img.getWidth(this);  /* update raw image size */
      sicHeight= img.getHeight(this);        
      /** center coords of  image, for zoom/dezoom */
      xCtr= (sicWidth/2);
      yCtr= (sicHeight/2);    
      is.iData.magVal= is.iData.state.zoomMagVal;
   
      /* [1] limit the zoom */
      is.iData.mag= Math.max(is.iData.magVal, SliderState.MIN_ZOOM_MAG_VAL);
      is.iData.mag= Math.min(is.iData.mag, SliderState.MAX_ZOOM_MAG_VAL);         
    }
    this.bcImgF= is.bcImgF;
    curPos= new Point(0,0);
    flkCurPos= new Point(0,0);
       
    setCanvasSize(preferredWidth, preferredHeight);  
    Util.sleepMsec(20);
    repaint();  
  } /* ScrollableImageCanvas */
  
  
  /**
   * copy() - destroy data structures
   */
  public void copy(ScrollableImageCanvas newSIC)
  { /* copy */    
    newSIC.isName= this.isName;
    newSIC.lastlastISName= this.lastlastISName;	
    newSIC.saveAsImgFile= this.saveAsImgFile;
    newSIC.bcImgF= this.bcImgF;
    newSIC.nTextItems= this.nTextItems;	
    newSIC.xBase= this.xBase;	
    newSIC.yBase= this.yBase;
    newSIC.xImg= this.xImg;			
    newSIC.yImg= this.yImg;
    newSIC.xObj= this.xObj;			 
    newSIC.yObj= this.yObj;
    newSIC.imgRoiX1= this.imgRoiX1;
    newSIC.imgRoiX2= this.imgRoiX2;
    newSIC.imgRoiY1= this.imgRoiY1;
    newSIC.imgRoiY2= this.imgRoiY2;
    newSIC.circleX= this.circleX;
    newSIC.circleY= this.circleY;
    newSIC.bkGrdX= this.bkGrdX;  
    newSIC.bkGrdY= this.bkGrdY;
    newSIC.ticX= this.ticX;
    newSIC.ticY= this.ticY;  
    newSIC.gValue= this.gValue;		
  
    newSIC.ctrlMod= this.ctrlMod;		    
    newSIC.shiftMod= this.shiftMod;			
    newSIC.ctrlShiftMod= this.ctrlShiftMod;		 
    newSIC.altMod= this.altMod;
    newSIC.selectedImageColor= this.selectedImageColor;
    newSIC.unselectedImageColor= this.unselectedImageColor;
    newSIC.color_text= this.color_text;
    newSIC.font_text= this.font_text;
    newSIC.x_text= this.x_text;
    newSIC.y_text= this.y_text;
    newSIC.text=this.text;  
    
    newSIC.maxCanvasDim= this.maxCanvasDim;
    newSIC.centerPt= this.centerPt;
    newSIC.scrollPos= this.scrollPos;
    newSIC.viewPortSize= this.viewPortSize;
    newSIC.cWidth= this.cWidth;
    newSIC.cHeight= this.cHeight;
    newSIC.sicWidth= this.sicWidth;
    newSIC.sicHeight= this.sicHeight;  
    newSIC.guardWidth= this.guardWidth;
    newSIC.guardHeight= this.guardHeight;
    newSIC.guardImgWidth= this.guardImgWidth;
    newSIC.guardImgHeight= this.guardImgHeight;  
    newSIC.xCtr= this.xCtr;
    newSIC.yCtr= this.yCtr;  
    newSIC.preferredWidth= this.preferredWidth;  
    newSIC.preferredHeight= this.preferredHeight; 
    newSIC.useScrollBarsFlag= this.useScrollBarsFlag;
    newSIC.currentIS= this.currentIS;
    newSIC.curPos= this.curPos;
    newSIC.flkCurPos= this.flkCurPos;  
    //newSIC.guardRegionCorrectionPos= this.guardRegionCorrectionPos;
    newSIC.guardRegionFlag= this.guardRegionFlag;   
      
  } /* copy */
    
  
  /**
   * cleanup() - destroy data structures
   */
  public void cleanup()
  { /* cleanup */
    color_text= null;
    font_text= null;
    x_text= null;
    y_text= null;
    text=null; 
  
    this.removeKeyListener(this);
    this.removeMouseListener(this);
    this.removeMouseMotionListener(this);  
    
    //flk.util.gcAndMemoryStats("Cleanup before setImageData change image"); 
    
  } /* cleanup */
  
    
  /**
   * getPreferredSize() - getPreferredSize, must be implemented for
   * ScrollPane to be displayed correctly.
   */
  public Dimension getPreferredSize() 
  { 
    Dimension dim= new Dimension(is.isWidth,is.isHeight);
    return(dim);  
  }
  
  
  /**
   * setCanvasSize() - change the size of this Canvas.
   * We assume that the legality of the new size is checked
   * prior to this method being called.
   * @param size is new size
   */
   public void setCanvasSize(int size)
   { /* setCanvasSize */
     if(size>0) 
     { /* force canvas to preferred size */
       setCanvasSize(size, size);   
     } /* force canvas to preferred size */
     else
     {
       Dimension d= getPreferredSize();
       setCanvasSize(d.width, d.height);           
     }     
   } /* setCanvasSize */
   
          
  /**
   * setCanvasSize() - change the size of this Canvases.
   * We assume that the legality of the new size is checked
   * prior to this method being called.
   * @param preferredWidth is new width
   * @param preferredHeight is new height
   */
   public void setCanvasSize(int preferredWidth, int preferredHeight)
   { /* setCanvasSize */
     if(preferredWidth>0 && preferredHeight>0) 
     { /* force canvas to preferred size */
       this.preferredWidth= preferredWidth;
       this.preferredHeight= preferredHeight;
      
       cWidth= preferredWidth;
       cHeight= preferredHeight;     
             
       if(is!=null && is.isWidth>0 && is.isHeight>0)
         setSize(is.isWidth,is.isHeight);
       else
         setSize(preferredWidth, preferredHeight);             
     } /* force canvas to preferred size */
   } /* setCanvasSize */
    
    
  /**
   * setImgPosition() - set the Image position. 
   * @param xImg is the image position.
   * @param yImg is the image position
   */
  public void setImgPosition(int xImg, int yImg)
  { /* setImgPosition */
    this.xImg= xImg;
    this.yImg= yImg;
    
    /* Map (xImg,yImg) to the scrollbar cursors positions.
     * Then force it to recenter the image cursor at the  (xImg, yImg)
     * by repainting it.
     */      
    int offsetAdj= 10;
    
    //   if(isName.equals("flicker"))
    //      offsetAdj= 0;
    int
      xAdj= (int) flk.canvasSize/2,
      yAdj= (int) (flk.canvasSize/2) - offsetAdj,/* Adjust for offset */
      x= xImg-xAdj,
      y= yImg-yAdj;     
    
    /* Move scrollbars to the mapped (xImg,yImg)
     * and thus the image viewed in the canvas.
     */
      is.sp.setScrollPosition(x,y-7);
      this.repaint();		/* move image */
  } /* setImgPosition */
  
  
  /**
   * getImgPosition() - get the Image position.
   * @return image position point
   */
  public Point getImgPosition()
  {
    Point xyImg= new Point(xImg, yImg);    
    return(xyImg);
  }
    
  
  /**
   * setObjPosition() - set the object position. Don't repaint here...
   * @param xObj is object position
   * @param yObj is object position
   */
  public void setObjPosition(int xObj, int yObj)
  {
    this.xObj= xObj;
    this.yObj= yObj;
  }
  
  
  /**
   * getObjPosition() - get the object position.
   * @return object position point
   */
  public Point getObjPosition()
  {
    Point xyObj= new Point(xObj, yObj);
    return(xyObj);
  }
       
   
  /**
   * setLandmarksTextListToDraw() - set text list including color and 
   * position. To disable the list, just set nTextItems to 0.
   * @param nTextItems to set
   * @param color_text list to set
   * @param text list to set
   * @param font_text list to set
   * @param x_text list to set
   * @param y_text list to set
   */
  public void setLandmarksTextListToDraw(int nTextItems, Color color_text[],
                                String text[], Font font_text[],
                                int x_text[], int y_text[])
  { /* setLandmarksTextListToDraw */
    this.nTextItems= nTextItems;
    this.color_text= color_text;
    this.text= text;
    this.font_text= font_text;
    this.x_text= x_text;
    this.y_text= y_text;   
  } /* setLandmarksTextListToDraw */
    
    
  /**
   * clearLandmarkTextListToDraw() - disable text list of landmarks to 
   * draw in canvas.
   */
  public void clearLandmarkTextListToDraw()
  { /* clearLandmarkTextListToDraw */
    this.nTextItems= 0;	    /* only need to set count to zero */
  } /* clearLandmarkTextListToDraw */
  
  
  /**
   * drawLandmarksTextInImage() - redraw landmark text[] in the image at 
   * (x,y)[] locs with the specified color and fonts.
   * Note: landmarks are indicated by "+<letter>"
   * @param g is graphics context
   * @param xFlkOrigin new x (0,0) to draw img to in flicker window only
   * @param yFlkOrigin new y (0,0) to draw img to in flicker window only
   */
  final private void drawLandmarksTextInImage(Graphics g,
                                              int xFlkOrigin, 
                                              int yFlkOrigin)
  { /* drawLandmarksTextInImage */
    int
      x, y,
      deltaX= -3,  /* Offset so draw label at that point */
      deltaY= +4;    
      
    for(int i=0; i<nTextItems; i++)
    { /* Draw landmarks */  
      
      if(!text[i].equals(""))
      { /* landmark label not null */
        //g.setColor(color_text[i]);
        g.setColor(Color.red);
        g.setFont(font_text[i]);      
        
        Point pt= is.iData.mapStateToZoom(new Point(x_text[i], y_text[i]));
        
        if(isName.equals("flicker"))
        { /* flicker window only */               
          if(xFlkOrigin>=0 && yFlkOrigin>=0)
          {
            pt.x= pt.x - xFlkOrigin;
            pt.y= pt.y - yFlkOrigin;           
          }
          else
            continue;   /* out of bounds */
          
          if(pt.x<0 || pt.y<0)
            continue;   /* out of bounds */       
        }  /* flicker window only */
        
        x= pt.x+deltaX;
        y= pt.y+deltaY;        
        
        g.drawString(text[i], x, y);
      } /* landmark label not null */
    } /* Draw landmarks */  
  } /* drawLandmarksTextInImage */
    
    
  /**
   * getNewImageCenterPoint() - Calc new center point in image based 
   * on scrollers
   * @return Point new center point image based on scrollers
   */
  public synchronized Point getNewImageCenterPoint()
  { /* getNewImageCenterPoint */
    while(is.sp==null)
      util.sleepMsec(10);
    Dimension viewportSize= is.sp.getViewportSize();
    Dimension canvasSize= this.getSize();
    Point pt= new Point(0,0);
    pt= is.sp.getScrollPosition();  
    
    int half= (int)(viewportSize.width/2);
    Point imgPt= new Point(pt.x+half,pt.y+half);   
    return(imgPt);     
  } /* getNewImageCenterPoint */
  
  
  /**
   * drawTrialObjInImage() - redraw xyObj in window if visible.
   * @param g is graphics context
   */
  final private void drawTrialObjInImage(Graphics g)
  { /* drawTrialObjInImage */    
    
    /* [1] Draw "+" at (deltaX,deltaY) from (x,y) */
    if(xObj>=1 || yObj>=1)
    {
      Point pt;
      g.setColor(flk.trialObjColor);     
      int
        xOffset= X_TRIAL_OBJ_OFFSET,
        yOffset= Y_TRIAL_OBJ_OFFSET;         
      int
        x= xObj + xOffset,
        y= yObj + yOffset;
      
      if(guardRegionFlag)
      { /* guard region */
        int
        gw= ((int)guardWidth/2),
        gh= ((int)guardHeight/2);
        
        if(x>gw || y>gh )
        {
          
          x= x-gw;
          y= y-gh;
          
          if(flk.NEVER)
            System.out.println("IS:drawTrialObjInImage: guard region on: x="+x+
                               " y="+y+"mapped to guardWidth="+gw+
                               " guardHeight="+gh+" x="+x+" y="+y);
                   
          g.drawString("+",x, y);
        }
        
      } /* guard region */
      else
      {
        if(flk.NEVER)
          g.setColor(Color.red);        
        g.drawString("+",x, y);       
      }
    }   
  } /* drawTrialObjInImage */
  
  
  /**
   * mapRelRoiCoordstoImageCoords() - remap ROI(x,y) in current canvas to 
   * img ROI(x,y).
   * NOTE: it is possible for either (x1,y1) to be -1 or (x2,y2) to be -1
   * and thus be undefined
   * @param x1 is ROI coords
   * @param x2 is ROI coords 
   * @param y1 is ROI coords 
   * @param y2 is ROI coords 
   * @return false if neither(x1,y1) or (x2,y2) defined, or img null.
   */
  final boolean mapRelRoiCoordstoImageCoords(int x1, int x2,
                                             int y1, int y2,
                                             Image pImg)
  { /* mapRelRoiCoordstoImageCoords */    
   /* calc offset */
    int
      xS, 
      yS;                          /* Subwindow to draw. Must be < 0
                                    * since it draws the lower right
                                    * rectangle from there.
                                    */
    /* [1] Make sure the image is loaded ok */
    if(pImg==null)
      return(false);
    
    /* [2] Make coords valid */
    if(x1<=-1 && y1<=-1 && x2<=-1 && y2<=-1)
      return(false);
    
    maxCanvasDim= getMaximumSize();
    
    if(is.sp!=null)
    { /* get position of canvas from scroll bars */
      int
        dWidth= - (sicWidth - cWidth),  /* - offsets */
        dHeight= - (sicHeight - cHeight),
        hVal= scrollPos.x,       /* actual scroll positions */
        vVal= scrollPos.y,
        hMax= maxCanvasDim.width,     /* max range of scroll bars */
        vMax= maxCanvasDim.height;      
      xS= (hVal * dWidth)/hMax;       /* compute new ULHC base subwindow */
      yS= (vVal * dHeight)/vMax;
      
      /* Create new X and Y values using the offsets calc above,
       * make sure if does not go out of bounds
       */      
      /* X1 */
      imgRoiX1= xS + x1;
      ticX= imgRoiX1;
      if(imgRoiX1 < 0) 
      {
        ticX= xS;    /* neg number */
        imgRoiX1= 0;
      }
      if(imgRoiX1>sicWidth) 
      {
        imgRoiX1= sicWidth;
        ticX= sicWidth;
      }
      
      /* X2 */
      imgRoiX2= xS + x2;
      if(imgRoiX2 < 0)
        imgRoiX2= 0;
      if(imgRoiX2>sicWidth)
        imgRoiX2= sicWidth;
      
      /* Y1 */
      imgRoiY1= yS + y1;
      ticY= imgRoiY1;
      if(imgRoiY1 < 0) 
      {
        ticY= yS;        /* neg number */
        imgRoiY1= 0;
      }
      if(imgRoiY1>sicHeight)
      {
        imgRoiY1= sicHeight;
        ticY= imgRoiY1;
      }
      
      /* Y2 */
      imgRoiY2= yS + y2;
      if(imgRoiY2 < 0)
        imgRoiY2= 0;
      if(imgRoiY2>sicHeight)
        imgRoiY2= sicHeight;
    } /* get position of canvas from scroll bars */
    
    return(true);    
  } /* mapRelRoiCoordstoImageCoords */
  
  
  /**
   * drawRoiInImage() - draw region of interest in window if visible.
   * @param g is graphics context
   */
  final private void drawRoiInImage(Graphics g, Image iImg)
  { /* drawRoiInImage */    
     ImageData iData= is.iData;  
     if(iData==null)
       return;    
     ImageDataROI idROI= iData.idROI; 
     if(idROI.roiX1<0 && idROI.roiY1<0 && idROI.roiX2<0 && idROI.roiY2<0)
       return;
  
     int ticSize= 10;
     int modifiedTicSizeX= ticSize;  
     int modifiedTicSizeY= ticSize; 
     
     /* [1] Remap ROI canvas coords to img coords in
      * (imgRoiX1, mgRoiY1, imgRoiX2, imgRoiY2)
      */
     boolean
       ok= mapRelRoiCoordstoImageCoords(idROI.roiX1,idROI.roiX2,
                                        idROI.roiY1, idROI.roiY2, iImg);
     
     if(!ok)
       return;                   /* nothing to draw */
     if(flk.NEVER)
       {
         System.out.println("IS:drawRoiInImage().1: idROI.roiX1= "+idROI.roiX1+
                            " idROI.roiY1= "+idROI.roiY1+
                            " idROI.roiX2= "+idROI.roiX2+
                            " idROI.roiY2= "+idROI.roiY2);         
       } 
     /* special case for ULHC since upper (x,y) will be neg */
     if(ticX < 0)
       modifiedTicSizeX= ticX + ticSize; /* need to adjust for ULHC */
     if(ticY < 0)
       modifiedTicSizeY= ticY + ticSize; /* need to adjust for ULHC */   
       
     /* [2] tic mark for Upper Left Hand Corner */
     if(modifiedTicSizeX > 0 && modifiedTicSizeY > 0)
     { /* ULHC */
       g.setColor(flk.roiColor);
       Point pt1= is.iData.mapStateToZoom(new Point(imgRoiX1,imgRoiY1));             
       
       if(flk.NEVER)
       {
         System.out.println("IS:drawRoiInImage().2: imgRoiX1= "+imgRoiX1+
                            " imgRoiY1= "+imgRoiY1+
                            " imgRoiX2= "+imgRoiX2+
                            " imgRoiY2= "+imgRoiY2+" pt="+pt1);         
       }
       if(imgRoiY1 > 0 && (modifiedTicSizeX) != 0)
         g.drawLine(pt1.x,pt1.y,
                    pt1.x+modifiedTicSizeX,pt1.y);  /* top line */          
       if(imgRoiX1 > 0 && (modifiedTicSizeY) != 0)
         g.drawLine(pt1.x,pt1.y, 
                    pt1.x, modifiedTicSizeY+pt1.y); /* vert tic */         
     } /* ULHC */
     
     /* [3] tic mark for Lower Rt Hand Corner */
     if(idROI.roiX2 > 0 && idROI.roiY2 > 0)
     { /* LRHC */
       g.setColor(flk.roiColor);       
       Point pt2= is.iData.mapStateToZoom(new Point(imgRoiX2,imgRoiY2));  
      
       if(flk.NEVER)
       {
         System.out.println("IS:drawRoiInImage().3: imgRoiX2= "+imgRoiX2+
                            " imgRoiY2= "+imgRoiY2+" pt="+pt2);         
       }
       if(imgRoiY2 > 0 && (imgRoiX2-ticSize) != 0)
         g.drawLine(pt2.x,pt2.y, pt2.x-ticSize,
                    pt2.y); /* horiz tic */
       
       if(imgRoiX2 > 0 && (imgRoiY2-ticSize) != 0)
        g.drawLine(pt2.x,pt2.y, pt2.x,
                   pt2.y-ticSize); /* vert tic */
     } /* LRHC */
     
     /* [4] Draw rectangle if both ULHC and LRHC exist.
      * Then truncate lines that fall out of bounds.
      */
     if(idROI.roiX1!=-1 && idROI.roiY1!=-1 && 
        idROI.roiX2!=-1 && idROI.roiY2!=-1)
     { /* Draw rectangle */  
       if(imgRoiY1 > 0 && imgRoiX2 != 0)
       {
         /* recalc coords for zoomed images */
         Point
           pt1= is.iData.mapStateToZoom(new Point(imgRoiX2,imgRoiY1)),
           pt2= is.iData.mapStateToZoom(new Point(imgRoiX1,imgRoiY1));       
      
         g.drawLine(pt1.x,pt1.y,pt2.x,pt2.y);  /* top line */ 
       }      
       if(imgRoiY2 > 0 && imgRoiX2 != 0)
       {
         /* recalc coords for zoomed images */
         Point
           pt1= is.iData.mapStateToZoom(new Point(imgRoiX1,imgRoiY2)),
           pt2= is.iData.mapStateToZoom(new Point(imgRoiX2,imgRoiY2));       
      
         g.drawLine(pt1.x,pt1.y,pt2.x,pt2.y);  /* top line */
       }
       if(imgRoiX1 > 0 && imgRoiY2 !=0)
       { 
         /* recalc coords for zoomed images */
         Point
           pt1= is.iData.mapStateToZoom(new Point(imgRoiX1,imgRoiY1)),
           pt2= is.iData.mapStateToZoom(new Point(imgRoiX1,imgRoiY2));       
      
         g.drawLine(pt1.x,pt1.y,pt2.x,pt2.y);  /* top line */
       }
       if(imgRoiX2 > 0 && imgRoiY2 !=0)
       {
         /* recalc coords for zoomed images */
         Point
           pt1= is.iData.mapStateToZoom(new Point(imgRoiX2,imgRoiY1)),
           pt2= is.iData.mapStateToZoom(new Point(imgRoiX2,imgRoiY2));       
      
         g.drawLine(pt1.x,pt1.y,pt2.x,pt2.y);  /* top line */
       }
     } /* Draw rectangle */     
  } /* drawRoiInImage */  
  
  
  /**
   * mapRelCircleCoordstoImageCoords() - remap circle center and background
   * (x,y) coords in current canvas to img coords (x,y).
   * @param cX is circle x coords 
   * @param cY is circle y coords 
   * @param bX is background coords 
   * @param bX is background y coords 
   * @return true if ok, false if no img
   */
  final boolean mapRelCircleCoordstoImageCoords(int cX, int cY,
                                                int bX, int bY,
                                                Image pImg)
  { /* mapRelCircleCoordstoImageCoords */
   /* calc offset */
    int
      xS, 
      yS;		                       /* Subwindow to draw. Must be < 0
                                    * since it draws the lower right
                                    * rectangle from there.
                                    */
    /* [1] Make sure the image is loaded ok */
    if(pImg==null)
      return(false);
        
    /* get position of canvas from scroll bars */
   if(is.sp!=null && is.useScrollBarsFlag)
    { /* get position of canvas from scroll bars */
      Dimension dim= getMaximumSize();
      Point pt= new Point(is.sp.getScrollPosition());
      
      int
        dWidth= - (sicWidth - cWidth),   /* - offsets */
        dHeight= - (sicHeight - cHeight),
        hMax= maxCanvasDim.width,      /* max range of scroll bars */
        vMax= maxCanvasDim.height;
      
      xS= (scrollPos.x * dWidth)/hMax; /* calc/ new ULHC base subwindow */
      yS= (scrollPos.y * dHeight)/vMax;
    
      /* Create new X and Y values using the offsets calc above,
       * make sure if does not go out of bounds 
       */                  
      /* X Circle */
      circleX=cX;  //= xS + cX;
      if(circleX < 0)
        circleX= 0;
      if(circleX > sicWidth)
        circleX= sicWidth;
      
      /* Y Circle */
      circleY= cY;   //yS + cY;
      if(circleY < 0)
        circleY= 0;
      if(circleY > sicHeight)
        circleY= sicHeight;
    
      /* X Back ground */
      bkGrdX= xS + bX;
      if(bkGrdX < 0)
        bkGrdX= 0;
      if(bkGrdX > sicWidth)
        bkGrdX= sicWidth;
      
      /* Y Back ground */
      bkGrdY= yS + bY;
      if(bkGrdY < 0)
        bkGrdY= 0;
      if(bkGrdY > sicHeight)
        bkGrdY= sicHeight;
      
    } /* get position of canvas from scroll bars */   
    return(true);    
  } /* mapRelCircleCoordstoImageCoords */
    
  
  /**
   * drawBackgroundCircleInImage() - draw background circle in window 
   * if visible.
   * @param g is graphics context
   */
  final private void drawBackgroundCircleInImage(Graphics g, Image pImg)
  { /* drawBackgroundCircleInImage */    
     ImageData iData= is.iData;
     int 
       xLbl= 0,
       yLbl= 0;
     
     /* remap coords */
     ImageDataMeas idM= iData.idM;
     mapRelCircleCoordstoImageCoords(idM.measObjX, idM.measObjY,
                                     idM.bkgrdObjX, idM.bkgrdObjY,
                                     pImg);          
     /* Background Circle */
     if(idM.bkgrdGrayValue>=0 && bkGrdX > 0 && bkGrdY > 0)
     {     
       g.setColor(flk.bkgrdCircleColor); 
       
       Point pt= new Point(bkGrdX, bkGrdY);
       Point bgPt= is.iData.mapStateToZoom(pt);    /* map bkgrd */
       int 
         zoomRadius= (int) (flk.bkgrdCircleRadius * is.iData.mag);   
       
       drawCircleAroundSpot(g, bgPt.x, bgPt.y,
                            flk.bkgrdCircleColor,
                            flk.bkgrdCircleRadius,
                            0);
       xLbl= bgPt.x + zoomRadius + (int) (2*is.iData.mag);
       yLbl= bgPt.y + (int) (4*is.iData.mag);
       g.drawString("B", xLbl, yLbl);
     }
  } /* drawBackgroundCircleInImage */
    
  
  /**
   * drawMeasCircleInImage() - draw measurement circles in window if 
   * visible.
   * @param g is graphics context
   */
  final private void drawMeasCircleInImage(Graphics g, Image pImg)
  { /* drawMeasCircleInImage */    
     ImageData iData= is.iData;
     int 
       xLbl= 0,
       yLbl= 0;
     
     /* remap coords */
     ImageDataMeas idM= iData.idM;     
     mapRelCircleCoordstoImageCoords(idM.measObjX,idM.measObjY,
                                     idM.bkgrdObjX, idM.bkgrdObjY,
                                     pImg);                
     /* Circle */
     if(idM.measGrayValue>=0 && circleX > 0 && circleY > 0)
     {     
       g.setColor(flk.measCircleColor); 
       Point pt= new Point(circleX, circleY);
       Point bgPt= is.iData.mapStateToZoom(pt);    /* bkgrd */
       int 
         zoomRadius= (int) (flk.bkgrdCircleRadius * is.iData.mag);   
       
       drawCircleAroundSpot(g, circleX, circleY,
                            flk.measCircleColor, flk.measCircleRadius,
                            0);
       xLbl= circleX + zoomRadius + (int)(2*is.iData.mag);
       yLbl= circleY + (int) (4*is.iData.mag);
       g.drawString("M",xLbl,yLbl); 
     }
  } /* drawMeasCircleInImage */
  
  
  /**
   * drawSpotMeasurementsInImage() - Draw spot measurements in image 
   * if valid and is visible. It will draw various combinations of things
   * depending on the switch options. 
   *<PRE> 
   *   Location switches               Action
   *   -----------------------------   ----------------------------  
   *   flk.viewDrawSpotLocCircleFlag   Draw circle 
   * or
   *   flk.viewDrawSpotLocPlusFlag     Draw '+'
   * or 
   *    neither                        Don't indicate location
   *
   *   Annotation switches             Action draw right of location
   *   -----------------------------   -----------------------------  
   *   flk.viewDrawSpotAnnNbrFlag      Draw s.nbr 
   * or
   *   flk.viewDrawSpotAnnIdFlag       Draw s.id or "<none>"
   * or 
   *    neither                        Don't indicate annotation
   *
   *</PRE>
   * @param g is graphics context
   */
  final private void drawSpotMeasurementsInImage(Graphics g, Image pImg)
  { /* drawSpotMeasurementsInImage */    
     ImageData iData= is.iData;
     ImageDataSpotList idSL= iData.idSL;
     Spot spotList[]= idSL.spotList;
     int
       nSpots= idSL.nSpots,
       nbr= 0,
       xLbl= 0,
       yLbl= 0,
       nCirMask,
       xLblOffset;
     if(spotList==null)
       return;
     
     /* see if mouse is on a spot. This will be the current spot */
     Spot curSpot= idSL.lookupSpotInSpotListByXY(xObj, yObj);
     
     for(int i=0;i<nSpots;i++)
     { /* draw each spot as <loc><annotation> */ 
       Spot s= spotList[i];
       /* remap coords (s.xC, s.yC, s.xB, s.yB) to
        * (circleX, circleY, bkGrdX, bkGrdY).
        */              
       Point pt= new Point(s.xB, s.yB);
       Point bgPt= is.iData.mapStateToZoom(pt);   /* bkgrd */
       
       pt= new Point(s.xC, s.yC);
       Point mPt= is.iData.mapStateToZoom(pt);    /* spot*/
             
       mapRelCircleCoordstoImageCoords(mPt.x,mPt.y, bgPt.x,bgPt.y,pImg);
       
       /* indicate the current spot with a different color... */
       Color fgColor= (curSpot==s)
                        ? flk.trialObjColor
                        : flk.measCircleColor;         
       g.setColor(fgColor);
       
       /* make sure choosen coordinate is in the window */
       if(circleX > 0 && circleY > 0)
       { /* draw the circle spot since it is in the scrollable window */
         nbr= s.nbr;                    /* Spot # */
         String  sId= ((s.id)==null || s.id.length()==0) 
                         ? "<none>" : s.id;
         nCirMask= s.nCirMask;          /* circular mask diameter */ 
         xLblOffset= xLbl+(nCirMask/2)+6;
         if(flk.viewDrawSpotLocCircleFlag)
         { /* draw a circle of size s.nCirMask with {ann} */ 
           /* compute offsets so text is to the right of circle */
           xLbl= circleX + nCirMask + (int) (2 * is.iData.mag);
           yLbl= circleY+ (int) (3 * is.iData.mag);
           
           drawCircleAroundSpot(g, circleX, circleY, 
                                fgColor, nCirMask, 0);
           if(flk.viewDrawSpotAnnNbrFlag && flk.viewDrawSpotAnnIdFlag)
             g.drawString((s.nbr+" "+sId), xLbl, yLbl);
           else
           { /* one or the other */
             if(flk.viewDrawSpotAnnNbrFlag)
               g.drawString((""+s.nbr), xLbl, yLbl);
             else if(flk.viewDrawSpotAnnIdFlag)
               g.drawString(sId, xLbl, yLbl);
           } /* one or the other */
         } /* draw a circle of size s.nCirMask with {ann} */ 
         
         else if(flk.viewDrawSpotLocPlusFlag)
         { /* draw "+"{ann} note offsets because of string drawing offset */  
           xLbl= circleX - (int) (3*is.iData.mag);
           yLbl= circleY + (int) (3*is.iData.mag);
           if(flk.viewDrawSpotAnnNbrFlag && flk.viewDrawSpotAnnIdFlag)
             g.drawString(("+"+s.nbr+" "+sId), xLbl, yLbl);
           else
           { /* One or the other or neither */
             if(flk.viewDrawSpotAnnNbrFlag)
               g.drawString(("+"+s.nbr), xLbl, yLbl);
             if(flk.viewDrawSpotAnnIdFlag)
               g.drawString(("+"+sId), xLbl, yLbl);
             if(!flk.viewDrawSpotAnnNbrFlag && !flk.viewDrawSpotAnnIdFlag)
               g.drawString("+", xLbl, yLbl);
           } /* One or the other or neither */
         } /* draw "+"{ann} note offsets because of string drawing offset */
         else
         { /* just draw annotation */              
           xLbl= circleX;
           yLbl= circleY;
           if(flk.viewDrawSpotAnnNbrFlag && flk.viewDrawSpotAnnIdFlag)
             g.drawString((s.nbr+" "+sId), xLbl, yLbl);
           else
           { /* one or the other */
             if(flk.viewDrawSpotAnnNbrFlag)
               g.drawString((""+s.nbr), xLbl, yLbl);
             else if(flk.viewDrawSpotAnnIdFlag)
               g.drawString(sId, xLbl, yLbl);
           } /* one or the other */
         } /* just draw annotation */
       } /* draw the spot since it is in the scrollable window */
     } /*  /* draw each spot as <loc><annotation> */ 
  } /* drawSpotMeasurementsInImage */
  
  
  /**
   * drawCircleAroundSpot() - draw a circle around spot if visible.
   * NOTE: It draws (2*radius+thickess) diameter circle.
   * @param g is graphics context
   * @param xC is center of circle
   * @param yC is center of the circle
   * @param color to use
   * @param radius to draw circle
   * @param thickness (0,1,2) of the line
   */
  private void drawCircleAroundSpot(Graphics g, int xC, int yC, 
                                    Color color, int radius,
                                    int thickness)
  { /* drawCircleAroundSpot */
    if(radius==0)
      radius= 1;
    if(thickness==0)
      thickness= 1;
    
    int zoomRadius= (int)(radius * is.iData.mag);   
    
    g.setColor(color);
    for(int t=0;t<thickness;t++)
      g.drawArc( xC-zoomRadius-1, yC-zoomRadius-1, 2*zoomRadius+t,
                2*zoomRadius+t, 0,360);
  } /* drawCircleAroundSpot */

  
  /**
   * drawTargetOverlay() - Draw Target Overlay in the ImageScroller window.
  */
  private void drawTargetOverlay(Graphics g, 
                                 ImageScroller is, 
                                 ImageScroller currentIS)
   { /* drawTargetOverlay() */
     if(is!=null && flk.viewTrialObjFlag && 
        is.sp!=null && is.useScrollBarsFlag) 
     { /* right or left canvas */
       int
         halfWayWidth= (int)(viewPortSize.width/2),
         halfWayHeight= (int)(viewPortSize.height/2),
         upperX= scrollPos.x + halfWayWidth,
         lowerX= scrollPos.x + halfWayWidth,
         rightX= scrollPos.x + viewPortSize.width,
         leftX= scrollPos.x,
         upperY= scrollPos.y,
         lowerY= scrollPos.y + viewPortSize.height,
         rightY= scrollPos.y + halfWayHeight,
         leftY= scrollPos.y + halfWayHeight,
         xH= scrollPos.x + halfWayWidth,
         yH= scrollPos.y + halfWayHeight;
       
       Point center= new Point(scrollPos.x + halfWayWidth,
       scrollPos.y + halfWayHeight);
       
       g.setColor(flk.targetColor);
       g.drawOval(center.x-tSize, center.y-tSize, 2*tSize, 2*tSize);
       if(flk.NEVER && flk.dbugFlag) 
       { /* print args so can DEBUG and check */
         System.out.println("IS-drawTargetOverlay() "+isName+
                            " scrollPos.x= "+scrollPos.x+
                            " scrollPos.y= "+scrollPos.y+
                            " halfWayWidth="+halfWayWidth+
                            " halfWayHeight="+halfWayHeight+
                            " v-PortSize.width="+viewPortSize.width+
                            " v-PortSize.height="+viewPortSize.height+"\n"+
                            " (upperX,upperY)="+"("+upperX+","+upperY+")"+
                            " (lowerX,lowerY)="+"("+lowerX+","+lowerY+")"+
                            " (rightX,rightY)="+"("+rightX+","+rightY+")"+
                            " (leftX,leftY)="+"("+leftX+","+leftY+")"+ "\n"+
                            " center="+center);
       } /* print args so can DEBUG and check */
       /* Upper line */
       g.drawLine(upperX, upperY, center.x, center.y-tSize);
       /* Lower Line */
       g.drawLine(lowerX, lowerY, center.x, center.y+tSize);
       /* Right line */
       g.drawLine(rightX, rightY, center.x+tSize, center.y);
       /* Left line */
       g.drawLine(leftX, leftY, center.x-tSize, center.y);
     } /* right or left canvas */
     
     else if(!is.useScrollBarsFlag && currentIS!=null)
     { /* For Flicker canvas */     
       scrollPos= currentIS.sp.getScrollPosition();
       viewPortSize= currentIS.sp.getViewportSize();       
       
       /* Get proportionate correction value for flicker window. It 
        * does not have scrollers and when you click on the right or
        * left images the it does not align in the flicker window. */       
        int
         fudgeFactorAlignmenWidth= (int)((flk.canvasSize/flk.MAX_CANVAS_SIZE)*10),
         fudgeFactorAlignmenHeight= (int)((flk.canvasSize/flk.MAX_CANVAS_SIZE)*10);
                
       if(flk.canvasSize >flk.MIN_CANVAS_SIZE && flk.canvasSize <=250)
       {
         fudgeFactorAlignmenWidth= 1;
         fudgeFactorAlignmenHeight= 8;
       }
       else if(flk.canvasSize >250 && flk.canvasSize <=350)
       {
         fudgeFactorAlignmenWidth= 0;
         fudgeFactorAlignmenHeight= 8;
       }
       else if(flk.canvasSize >350 && flk.canvasSize <=flk.MAX_CANVAS_SIZE)
       {
         fudgeFactorAlignmenWidth= -1;     
         fudgeFactorAlignmenHeight= 9;
       }    
             
       int        
         halfWayWidth= (int)(viewPortSize.width/2)+fudgeFactorAlignmenWidth,
         halfWayHeight= (int)(viewPortSize.height/2)-fudgeFactorAlignmenHeight,
         upperX= halfWayWidth,
         lowerX= halfWayWidth,
         rightX= viewPortSize.width+fudgeFactorAlignmenWidth,
         leftX= 0,
         upperY= 0,
         lowerY= scrollPos.y + viewPortSize.height-fudgeFactorAlignmenHeight,
         rightY= halfWayHeight,
         leftY= halfWayHeight,
         xH= scrollPos.x + halfWayWidth,
         yH= scrollPos.y + halfWayHeight;
      
       Point center= new Point(halfWayWidth, halfWayHeight);
       
       g.setColor(flk.targetColor);
       g.drawOval(center.x-tSize, center.y-tSize, 2*tSize, 2*tSize);
       if(flk.NEVER && flk.dbugFlag) 
       { /* print args so can DEBUG and check */
         System.out.println("IS-drawTargetOverlay() "+isName+
                            " flk.canvasSize=" + flk.canvasSize +
                            " flk.MAX_CANVAS_SIZE"+flk.MAX_CANVAS_SIZE+
                            " fudgeFactorAlignmenWidth= "+fudgeFactorAlignmenWidth+
                            " pre calc:"+(float)(flk.canvasSize / flk.MAX_CANVAS_SIZE)+
                            " scrollPos.x= "+scrollPos.x+
                            " scrollPos.y= "+scrollPos.y+
                            " halfWayWidth="+halfWayWidth+
                            " halfWayHeight="+halfWayHeight+
                            " viewPortSize.width="+viewPortSize.width+
                            " viewPortSize.height="+viewPortSize.height+"\n"+
                            " is.isWidth=" + is.isWidth+
                            " is.isHeight=" + is.isHeight+
                            " preferredWidth="+ preferredWidth+
                            " preferredHeight="+ preferredHeight+"\n"+
                            " (upperX,upperY)="+"("+upperX+","+upperY+")"+
                            " (lowerX,lowerY)="+"("+lowerX+","+lowerY+")"+
                            " (rightX,rightY)="+"("+rightX+","+rightY+")"+
                            " (leftX,leftY)="+"("+leftX+","+leftY+")"+ "\n"+
                            " center="+center);
       } /* print args so can DEBUG and check */

       /* Upper line */
       g.drawLine(upperX, upperY, center.x, center.y-tSize);
       /* Lower Line */
       g.drawLine(lowerX, lowerY, center.x, center.y+tSize);
       /* Right line */
       g.drawLine(rightX, rightY, center.x+tSize, center.y);
       /* Left line */
       g.drawLine(leftX, leftY, center.x-tSize, center.y);             
     } /* For Flicker canvas */
  } /* drawTargetOverlay() */
  
    
  /**
   * showImageMousePositions() - show the image mouse positions
   * Only report if debugging...
   * @param e is mouse event
   * @param msgs is message to display
   * @param x is image object position 
   * @param y is image object position 
   */
  final void showImageMousePositions(MouseEvent e, String msgs, 
                                     int x, int y)
  { /* showImageMousePositions */
    if(flk.dbugFlag)
    { /* report it */
      if(msgs==null)
        msgs= "";		/* protect it. */
      util.showMsg("[" + msgs + "] " +
                   " x=" + x + ", y=" + y +
                   (ctrlMod
                      ? " CTRL"
                      : (shiftMod ? " SHIFT" : "")) +
                   " (x,y)Img= (" + xImg + "," + yImg + ")" +
                   " (x,y)Base= (" + xBase + "," + yBase + ")" +
                   " (x,y)Obj= (" + xObj + "," + yObj + ")",
                   Color.blue);      
    } /* report it */
  } /* showImageMousePositions */
   
     
  /**
   * mapRelXYtoImage() - map (x,y) Mouse in current canvas to (xImg,yImg)
   * as well as updating the parent.(xImg,yImg) values
   * which are RELATIVE to the current image.
   * Set the lastISName for possible use elsewhere.
   * @param e is mouse event
   * @param x is image cursor
   * @param y is image cursor
   */
  final void mapRelXYtoImage(MouseEvent e, int x, int y)
  { /* mapRelXYtoImage */
    int 
      modifiers= 0,    
      xCorrect= 0,                  /* correction because of arrow cursor*/
      yCorrect= 0;
    
    if(e!=null)
    {
      modifiers= e.getModifiers();
      altMod= ((modifiers & InputEvent.ALT_MASK) != 0);
      ctrlMod= ((modifiers & InputEvent.CTRL_MASK) != 0);
      shiftMod= ((modifiers & InputEvent.SHIFT_MASK) != 0);
      ctrlShiftMod= (ctrlMod && shiftMod);
    }    
    
    xImg= x; // + xBase + xCorrect;     /* absolute position in the image */
    yImg= y; // yImg= y + yBase + yCorrect;

    lastlastISName= is.lastISName;  /* save previous canvas*/
    
    /* Set the current image canvas so we can see if was here last...*/
    is.lastISName= is.name;	        /* left, right, flicker */
    
    /* Set to: left, right, both */
    if("flicker".equals(isName))
      flk.activeImage= "both";
    else 
      flk.activeImage= is.name;
  } /* mapRelXYtoImage */
  
  
  /**
   * updateImageScrollableTitles() - update 1 or 2 scrollable images
   * @param forceUpdateFlag state
   */
  final public void updateImageScrollableTitles(boolean forceUpdateFlag)
  { /* updateImageScrollableTitles */
    String lcName= is.lastISName;
    
    if("left".equals(isName) || forceUpdateFlag)
      flk.i1IS.drawImageTitle();
    
    if("right".equals(isName) || forceUpdateFlag)
      flk.i2IS.drawImageTitle();
  } /* updateImageScrollableTitles */
  
    
  /**
   * drawImageTitle() - draw title in image. Also if displaying grayscale 
   * and using circular mask, then draw the circle around (xObj,yObj)
   */
  final public void drawImageTitle()
  { /* drawImageTitle */
    String 
      oldTitle= is.title;
    int
      x= xObj,
      y= yObj;
    
    if(flk.useGuardRegionImageFlag)
    {
       x= xObj - ((int) guardWidth/2);
       y= yObj - ((int) guardHeight/2);
       
       if(x<0)
         x= 0;
       if(y<0)
         y= 0;
    }
    String
      sT= is.iData.idM.getPixelValueStr(x,y);
    is.setTitle(sT, true);
    is.title= oldTitle;
  } /* drawImageTitle */
   
  
  /**
   * setTrialLMS() - set trial landmark in & center image, save object(x,y).
   * This moves the scroll image and object to (x,y).
   * @param x is trial LMS cursor
   * @param y is trial LMS cursor
   */
  final void setTrialLMS(int x, int y)
  { /* setTrialLMS */
    if(is==flk.i1IS || is==flk.i2IS)
    {
      is.img_selectedFlag= true;       /* mark it as visited... */
      is.setObjPosition(x,y);
      
      /* Move the scrollbars and image to track object */
      setImgPosition(x,y);
    }
  } /* setTrialLMS */
      
  
  /**
   * keyPressed() - handle key pressed events
   * @param e is KeyEvent
   */
  public void keyPressed(KeyEvent e)
  { flk.ekb.keyPressed(e); }
  
  
  /**
   * keyTyped() - handle key down events
   * @param e is KeyEvent
   */
  public void keyTyped(KeyEvent e)
  { flk.ekb.keyTyped(e); }
  
  
  /**
   * keyReleased() - handle key down events
   * @param e is KeyEvent
   */
  public void keyReleased(KeyEvent e)
  { flk.ekb.keyReleased(e); }
  
  
  /**
   * processBCimage() - process colormap image by running the filter.
   * NOTE: need to do repaint() just after this call.
   * NOTE: we must sync this so that it completes a full image computation
   * before we do it again.
   * If not allowing image transforms (i.e. !flk.allowXformFlag), then
   * use the id.iImg. Otherwise, if we are composing output images then
   * use id.oImg else id.iImg.
   * It creates the id.bcImg in the process if successful
   * @param id is the ImageData to get the image to process
   * @return true if succeed
   */
  public synchronized boolean processBCimage(ImageData id)
  { /* processBCimage */

    /* Get the image to use: zImg, oImg, and iImg in that order. */
    Image img= id.getImageForBCInput(); 
                        
    /* [TODO] Check if may need to add code to make it refilter if change
     * window coordinates or zoom. 
     */
    try
    {
      ImageProducer ip= img.getSource();
      ip= new FilteredImageSource(ip, bcImgF);
      if(id.bcImg!=null)
      {
        id.bcImg.flush();
        id.bcImg= null;
        flk.util.gcAndMemoryStats("Clean up old bcImg processBCimage call");
      }
      id.bcImg= getToolkit().createImage(ip);
      
    
      flk.util.gcAndMemoryStats("Clean up after processBCimage call");
      if(flk.NEVER)
      {
        Image gifImage= null;       
        Graphics g= getGraphics();
        //  if(g != null)
        //   paint(g); //  paintComponent(g); //paint(g);
        // g.dispose();
        if(id.bcImg!=null)//&&
        { /* write it out for testing */
            g.drawImage(id.bcImg, 0, 0, this);
            WriteGifEncoder wge= new WriteGifEncoder(id.bcImg);
            gifImage= null;
            if(wge!=null)
                wge.writeFile("C:\\BC_TEST.gif");
        } /* write it out for testing */
        
        ip= null;
        g= null;
      }
      return(true);
    }
    catch(Exception e)
    {
      System.out.println("IS-PBCI e="+e);
      e.printStackTrace();        
      return(false);
    }
  } /* processBCimage */
  
     
  /**
   * updateClickableCanvas() - perform clickable canvas update
   * operations invoked by various controls. 
   * It uses the current (xImg,yImg) values for this canvas.
   */
  void updateClickableCanvas()
  { /* updateClickableCanvas */  
    if(is!=flk.i1IS && is!=flk.i2IS)
      return;
    
    if(!is.iData.bnd.bndOpenFlag && !ctrlMod && !shiftMod)
    { /* MOUSE: Set the new Object position from the mouse */
      is.img_selectedFlag= true;  /* NOW force it to be define. */
      xObj= xImg;
      yObj= yImg;
      updateImageScrollableTitles(false);
                  	/* draw the object ast '+' */
      
      /* Process clickable image events */ 
      if(flk.userClickableImageDBflag || flk.doMeasureProtIDlookupAndPopupFlag)
      { /* treat the image as a clickable database image */  
        if(flk.doMeasureProtIDlookupAndPopupFlag)
        { /* Measure the spot and add it to the spot list */
          float measVal= is.iData.idM.captureMeasValue();
          if(measVal>=0)
          { /* print the measurement value and update  measured spot */
            is.iData.idM.showMeasValue("circleMask");
          }
        } /* Measure the spot and add it to the spot list */
        Spot curSpot= is.iData.idM.curSpot;
        String clickableCGIbaseURL= null;     
        
        /* Use cascade of tests to compute the URL to use */
        if(is==flk.i1IS && flk.clickableCGIbaseURL1!=null)
          clickableCGIbaseURL= flk.clickableCGIbaseURL1;
        else if(is==flk.i1IS && flk.clickableCGIbaseURL1pix!=null)
          clickableCGIbaseURL= flk.clickableCGIbaseURL1pix;
        else if(is==flk.i2IS && flk.clickableCGIbaseURL2!=null)
          clickableCGIbaseURL= flk.clickableCGIbaseURL2;
        else if(is==flk.i2IS && flk.clickableCGIbaseURL2pix!=null)
          clickableCGIbaseURL= flk.clickableCGIbaseURL2pix;  
          
        if(clickableCGIbaseURL!=null && curSpot!=null &&
           (flk.useSwiss2DpageServerFlag || flk.usePIRUniprotServerFlag ||
            flk.usePIRiProClassServerFlag || flk.usePIRiProLinkFlag))
        { /* get Swiss-Prot ID, NAME and save in spot*/
          util.showMsg(
                    "Searching for spot identification on active map server",
                       Color.red);
          util.showMsg2("", Color.black);
          String pData[]= util.getProteinIDdataByXYurl(clickableCGIbaseURL,
                                                       curSpot.xC,
                                                       curSpot.yC,null);
          if(pData!=null)
          { /* found it */
            curSpot.id= pData[0];
            curSpot.name= pData[1];
            util.showMsg("Found spot identification on active map server",
                         Color.black);
            util.showMsg2("Protein Name["+curSpot.id+
                          "] Id ["+curSpot.name+"]",
                          Color.black);           
          }
          else
          {
            util.showMsg("Spot not found on active map server",Color.black);
            util.showMsg2("",Color.black);
          }
        } /*get Swiss-Prot ID, NAME and save in spot*/
                
        boolean addPixnamFlag= ((is==flk.i1IS &&
                                 flk.clickableCGIbaseURL1pix!=null) ||
                                (is==flk.i2IS &&
                                 flk.clickableCGIbaseURL2pix!=null));
        if(clickableCGIbaseURL!=null)
        { /* Also, service it from a CGI database */
          String
            fullPixFile= is.title,
            pixFile= fullPixFile,
            dataURL;
          
          /* [TODO] potential trouble if image is CGI-BIN generated */
          int ext= fullPixFile.lastIndexOf(".gif");          
          if(ext<=-1)
            ext= fullPixFile.lastIndexOf(".jpg");          
          if(ext<=-1)
            ext= fullPixFile.lastIndexOf(".tif");         
          if(ext<=-1)
            ext= fullPixFile.lastIndexOf(".tiff");        
          if(ext<=-1)
            ext= fullPixFile.lastIndexOf(".ppx");
          if(ext>0)
            pixFile= fullPixFile.substring(0,ext);          
          
          /* Guard region calculation */
          int
            x= xObj,
            y= yObj;
           if(flk.useGuardRegionImageFlag)
           {
             x= x - ((int) guardWidth/2);
             y= y - ((int) guardHeight/2);
             if(x<0)
               x= 0;
             if(y<0)
               y= 0;
           }          
          
          /* Better way to do it */  
          pixFile= flk.util.getFileNameFromPath(fullPixFile);          
          dataURL= clickableCGIbaseURL + x + "," + y;
          
          /* If PIR database, then make sure have Swiss-Prot ID to pass
           * to it.
           */
          if(flk.usePIRUniprotServerFlag || 
             flk.usePIRiProClassServerFlag ||
             flk.usePIRiProLinkFlag)
            dataURL= is.iData.idSL.mapSPIDtoPIRURL(dataURL,
                                                   clickableCGIbaseURL,
                                                   xObj, yObj);
          
          /* Now popup the Web browser with this URL */
          Popup popup= new Popup(flk);
          popup.popupViewer(dataURL, flk.popupWindowName);
          util.showMsg("Selecting spot in ["+pixFile+
                       "] at ("+xObj+","+yObj+")",
                       Color.black);
        } /* Also, service it from a CGI database */        
      } /* treat the image as a clickable database image */
      repaint();	 
      flk.repaint();
    } /* MOUSE: Set the new Object position from the mouse */    
  } /* updateClickableCanvas */
  
     
  /**
   * updateScrollCanvas() - scroll the image in the scrollable canvas.
   * It uses the current (xImg,yImg) values for this canvas.
   */
  void updateScrollCanvas()
  { /* updateScrollCanvas */ 
    setTrialLMS(xImg, yImg);
    updateImageScrollableTitles(true);
    
    if(flk.viewGangScrollFlag)
    { /* GANG move both images together */
      setTrialLMS(xImg, yImg);            /* move current image */
      updateImageScrollableTitles(true);
      
      /* Now GANG move the other image to same ABSOLUTE position */
      ImageScroller icOther= ((is==flk.i1IS) ? flk.i2IS : flk.i1IS);
      ScrollableImageCanvas siOther= icOther.siCanvas;
      int
        dXobj= (xObj - siOther.xObj),
        dYobj= (yObj - siOther.yObj);
      
      /* Now GANG move the other image to same ABSOLUTE position */
      siOther.xImg= (xImg - dXobj);
      siOther.yImg= (yImg - dYobj);
      siOther.setTrialLMS(xImg, yImg);   /* move current image */
      siOther.updateImageScrollableTitles(false);
    } /* GANG move both images together */
  } /* updateScrollCanvas */ 
  
  
  /**
   * highlightActiveIS() - highlight ScrollableImageCanvas.
   */
  private void highlightActiveIS()
  { /* highlightActiveIS */ 
     /* Disable selected color for all images */
    flk.flkIS.txtField.setForeground(unselectedImageColor);   
    flk.i1IS.txtField.setForeground(unselectedImageColor);   
    flk.i2IS.txtField.setForeground(unselectedImageColor);   
    
    /* set active color */
    this.is.txtField.setForeground(selectedImageColor);    
  } /* highlighthighlightActiveIS */
    
  
  /**
   * mouseDragged() - update (xImg,yImg) with relative position in canvas
   * by adding (x,y) to paint ULHC base address (xBase,yBase).
   * If not CONTROL and not SHIFT keys, set the new image scroll positions.
   * If SHIFT key, change brightness/contrast for this canvas.
   * @param e is MouseEvent
   */
  public void mouseDragged(MouseEvent e)
  { /* mouseDragged */
    
   int
      x= e.getX(),
      y= e.getY(),
      tmpX=0,
      tmpY=0; 
    /* remap to orig coords 1.0 mag*/
    if(is.iData.mag == 1.0) 
      curPos= new Point(x,y); 
    else if(is.iData.mag > 1.0)
    { 
      double xDb= x/is.iData.mag;
      double yDb= y/is.iData.mag;           
      tmpX= (int) Math.round(xDb);
      tmpY= (int) Math.round(yDb);      
      curPos= new Point(tmpX,tmpY);
    }        
    else
    {
      double xDb= x*is.iData.mag;
      double yDb= y*is.iData.mag;
      tmpX= (int) Math.round(xDb);
      tmpY= (int) Math.round(yDb);      
      curPos= new Point(tmpX,tmpY);     
    }
    flkCurPos= new Point(x,y); 
    //guardRegionCorrectionPos= guardRegionCorrection(flkCurPos);
    
    mapRelXYtoImage(e,x,y);	        /* set (xImg,yImg) */
    
    if(is.iData.bnd.bndOpenFlag)
      is.iData.bnd.processBoundaryMode(xImg, yImg, shiftMod);
    
    else if(!ctrlMod && !shiftMod)
    { /* MOUSE: Set the new Object position from the mouse */
      is.img_selectedFlag= true; /* NOW force it to be define. */
      xObj= xImg;
      yObj= yImg;
      
    /* [TODO] redraw the images with the new scroll position.
     * Note that we can't just do a simple
     *          is.setImgPosition(x,y)
     * to move the scrollbars and image to track object since
     * it does not latch where we first put the mouse down.
     */
      updateImageScrollableTitles(false);
      if(flk.NEVER && flk.dbugFlag)
        showImageMousePositions(e, "is.mouseDragged", x, y);
      repaint();		            /* draw the object as a '+' */
    }    
    else if(shiftMod)
    { /* SHIFT key, change brightness/contrast for this canvas */
      /* Do not process if out of range, stay in canvas area */
      if(x<0 || y<0 ||  x>cWidth || y>cHeight)
        return;

      try
      { /* run the Brightness Contrast Filter then repaint */        
        int 
          brightness= x,
          contrast= (cHeight-y);
        
         if(flk.viewGangBCFlag)
         { /* gang BC */              
           /* Left image */
           flk.i1IS.bcImgF.setBrCt(flk.i1IS, brightness,contrast, 
                                   cWidth,cHeight, true); 
           if(processBCimage(flk.iData1)) 
           { /* repaint */
             /* Note: MUST call paint directly here since repaint is
              * getting stuck in a queue somewhere and causing a long
              * delay before calling paint.
              */
            Graphics g= getGraphics();
             if(g != null)
               repaint(); 
           } /* repaint */
                      
           /* Right image */
           flk.i2IS.bcImgF.setBrCt(flk.i2IS, brightness,contrast,
                                   cWidth,cHeight, true);  
           if(processBCimage(flk.iData2)) 
           { /* repaint */
             /* Note: MUST call paint directly here since repaint is
              * getting stuck in a queue somewhere and causing a long
              * delay before calling paint.
              */
             Graphics g= getGraphics();
             if(g != null)
               repaint(); 
           } /* repaint */
           
          /* Force the values into the visible slider bars with values
           * in the range of [0:100]% (?)
           */
           
           /* Do left image */
           flk.iData1.state.brightness= (100*x)/cHeight; /* scale 0:100 brightness */
           flk.iData1.state.contrast= (100*y)/cWidth;   /* scale 0:100 contrast */
                      
           /* Do right image */
           flk.iData2.state.brightness= (100*x)/cHeight; /* scale 0:100 brightness */
           flk.iData2.state.contrast= (100*y)/cWidth;   /* scale 0:100 contrast */
                     
           /* Brightness in scroller */
           flk.bGui.brightnessLabel.setText("Brightness: " +
                                            (flk.iData1.state.brightness- 
                                             flk.curState.DEF_BRIGHTNESS)+"%");
           flk.bGui.brightnessBar.setValue(flk.iData1.state.brightness);  
           
           /* Contrast in scroller */
           flk.bGui.contrastLabel.setText("Contrast: " +
                                          (flk.iData1.state.contrast-
                                           flk.curState.DEF_CONTRAST) + "%");
           flk.bGui.contrastBar.setValue((int) flk.iData1.state.contrast);                      
         } /* gang BC */
        
         else
         { /* non-gang BC */           
           bcImgF.setBrCt(is, brightness,contrast, cWidth,cHeight, true);        
           
           if(processBCimage(is.iData)) 
           { /* repaint */
             /* Note: MUST call paint directly here since repaint is
              * getting stuck in a queue somewhere and causing a long
              * delay before calling paint.
              */
            Graphics g= getGraphics();
            if(g != null)
              repaint(); //paint(g); //paint(g);  paintComponent
           } /* repaint */

          /* Force the values into the visible slider bars with values
           * in the range of [0:100]% (?)
           */
           is.iData.state.brightness= (100*x)/cHeight; /* scale 0:100 brightness */
           is.iData.state.contrast= (100*y)/cWidth;   /* scale 0:100 contrast */
           
           /* brightness */
           flk.bGui.brightnessLabel.setText("Brightness: " +
                                           (this.is.iData.state.brightness-
           flk.curState.DEF_BRIGHTNESS)+"%");
           flk.bGui.brightnessBar.setValue(this.is.iData.state.brightness);
           
           /* contrast */
           flk.bGui.contrastLabel.setText("Contrast: " +
                                          (this.is.iData.state.contrast-
           flk.curState.DEF_CONTRAST) + "%");
           flk.bGui.contrastBar.setValue((int)this.is.iData.state.contrast);        
         } /* non-gang BC */        
      } /* run the Brightness Contrast Filter then repaint */
      catch(Exception e2)
      {
        if(flk.CONSOLE_FLAG)
          System.out.println("IS-MD e2="+e2);
        e2.printStackTrace();        
      }                  
    } /* SHIFT key, change brightness/contrast for this canvas */   
    else
      if(altMod)
      { /* future: for implementing dragging canvas with hand cursor */       
      }
    repaint();
    
  } /* mouseDragged */
  
  
  /**
   * mousePressed() - update (xImg,yImg) with relative position in canvas
   * by adding (x,y) to paint ULHC base address (xBase,yBase).
   *
   * If !CTRL & !SHIFT mode, then set trial landmark for corresponding image
   * at the (xObj,yObj) defined by the current mouse position.
   * Draw the position in the image title. Set the img_selectedFlag
   * for the image.
   * If clickableImageDBflag, then dont set the trial landmark, but access
   * the database if the clickableGUIbaseURL exists.
   * @param e is MouseEvent
   */
  public void mousePressed(MouseEvent e)
  { /* mousePressed */    
    
    if(this.is!=null && this.is.name!=null)
    { /* select or deselect stuff based on which image click on */
      String name= this.is.name;
      flk.lastISName= name;
      
      /* [1] set the states for the image that was just clicked on (R,L,or Flk)*/
      if(flk.i1IS!=null && name.equals("left"))
        flk.evs.setEventScrollers(flk.iData1.state);
      else if(flk.i2IS!=null && name.equals("right"))
        flk.evs.setEventScrollers(flk.iData2.state);
      else if(name.equals("flicker"))
        flk.evs.setEventScrollers(this.is.iData.state);
      
      flk.activeImage= is.name; /* change to new active image */
      
      boolean enableFlag= (name.equals("left") || name.equals("right") ||
                           flk.viewGangBCFlag || flk.viewGangZoomFlag);
      /* enable/disable GUI menu items that must have an image selected to be enabled*/
      flk.bGui.setMustSelectImageMenuItemsEnable(enableFlag);      
    } /* select or deselect stuff based on which image click on */
    
    flk.curState= this.is.iData.state;
    /* Enable clickable DB checkbox if image is selected
     * and it is clickable capable.
     */
    flk.chkIfClickableDB(false);
   
    /* [2] get X and Y */
    int
      x= e.getX(),
      y= e.getY(),
      tmpX=0,
      tmpY=0;     
    
    /* remap to orig coords 1.0 mag */
    if(is.iData.mag == 1.0) 
      curPos= new Point(x,y); 
    
    else if(is.iData.mag > 1.0)
    {      
      double xDb= x/is.iData.mag;
      double yDb= y/is.iData.mag;           
      tmpX= (int) Math.round(xDb);
      tmpY= (int) Math.round(yDb);      
      curPos= new Point(tmpX,tmpY);
    }        
    else
    {
      double xDb= x*is.iData.mag;
      double yDb= y*is.iData.mag;
      tmpX= (int) Math.round(xDb);
      tmpY= (int) Math.round(yDb);      
      curPos= new Point(tmpX,tmpY);     
    }
       
    flkCurPos= new Point(x,y);  
        
    if(flk.NEVER)
      System.out.println("IS:mousePressed():"+isName+" x="+x+" y="+y+                       
                         "mapped to 1.0 state="+curPos);        
 
    /* [4] Set the active image title color */   
    highlightActiveIS(); 
    mapRelXYtoImage(e,x,y);	/* map mouse to this.(xImg,yImg) */
    if(flk.NEVER && flk.dbugFlag)
      showImageMousePositions(e, "is.mousePressed", x, y);
    
    /* [5] Perform canvas update operations invoked 
     * by various controls. It uses the current (xImg,yImg)
     * values for this canvas.
     */
    if(is.iData.bnd.bndOpenFlag)
      is.iData.bnd.processBoundaryMode(xImg, yImg, shiftMod);
    else
      updateClickableCanvas();        /* e.g. popup database for point */
        
    String name= is.name;
    if(altMod && (name.equals("left") || name.equals("right")))
    { /* ALT-click is equivalent to C-M to measure a spot */
      float measVal= is.iData.idM.captureMeasValue();
      if(measVal>=0)
        is.iData.idM.showMeasValue("circleMask");
    }
     
    /* Display gray value in report if viewing gray values */
    if(flk.viewDispGrayValuesFlag&& !isName.equals("flicker"))
    {
      String sT= is.iData.idM.getPixelValueStr(xObj,yObj);
      if(sT!=null)
        flk.util.appendReportMsg(sT+"\n");
    }
    return;
  } /* mousePressed */
      
   
  /**
   * mouseReleased() - update (xImg,yImg) with relative position in canvas
   * by adding (x,y) to paint ULHC base address (xBase,yBase).
   * If CONTROL key, rescroll canvas.
   * @param e is MouseEvent
   */
  public void mouseReleased(MouseEvent e)
  { /* mouseReleased */
    int
      x= e.getX(),
      y= e.getY();
  
    flk.curState= this.is.iData.state;
    
    mapRelXYtoImage(e,x,y);
    if(flk.NEVER && flk.dbugFlag)
      showImageMousePositions(e, "is.mouseReleased", x, y);
    
    if(is.iData.bnd.bndOpenFlag)
      is.iData.bnd.processBoundaryMode(xImg, yImg, shiftMod);
    
    /* [3] CTRL/MOUSE set the trial landmark for the corresponding image
     * and move the scroll image and object to (x,y)
     */
    else if(ctrlMod && !shiftMod)
    { /* scroll image */
      updateScrollCanvas();
    } /* scroll image */
  } /* mouseReleased */
  
  
  public void mouseClicked(MouseEvent e) { }
  public void mouseMoved(MouseEvent e) 
  {   
    if(!isName.equals("flicker"))
      this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
  }
  public void mouseEntered(MouseEvent e) { }
  public void mouseExited(MouseEvent e) { }    
       
      
  /**
   * guardRegionCorrection() - return guard region location
   * @return  guard region point
   */
  public Point guardRegionCorrection(Point pt)
  { /* guardRegionCorrection */
    Point newPt= new Point(0,0);
    int 
      x= 0,
      y= 0;   
    if(pt.x<0 || pt.y<0)
       return(newPt);   
    
    newPt= new Point(x,y);
    
    return(newPt);    
  } /* guardRegionCorrection */
  
        
  /**
   * update() - update without background the scrollable canvas
   * @param g is graphics context
   */
  public void update(Graphics g)
  { paint(g); }
  
       
  /**
   * paint() - repaint scrollable canvas in region defined by scroll bars
   * @param g is graphics context
   */
  public void paint(Graphics g)
  { /* paintComponent */       
    if(flk.repaintLockFlag==true || is.sp==null) 
      return;      
      
    /* [1] Lookup the image to draw with the prioity function. This uses
     * (bcImg, zImg, oImg and iImg) in that order depending on the status
     * of these images and global transform modes.
     */        
    Image pImg= is.iData.getImageForDisplayInput();    
    if(pImg==null)
      return;  
       
    /* [1.1] Make sure the image is loaded ok */
    if(!prepareImage(pImg, this))
      return;    
     
    if(is!=null && is.scrollPos!=null)
      is.sp.setScrollPosition(is.scrollPos); /* for some reason must have this here
                                              * to keep the scroll bars at the same 
                                              * position after a zoom */
    
    /* [2] Update some sizes only one time since they are used in some of 
     * the draw methods above.
     */
    maxCanvasDim= this.getMaximumSize();   
    centerPt= getNewImageCenterPoint();
    
    /* fixes a unwanted resize bug with guard region when flickering is
     * turned on. Only happens when the image is moved to the lower right
     * before and during flickering.*/
    if(!flk.useGuardRegionImageFlag)
    {
      sicWidth= pImg.getWidth(this);  /* update raw image size */
      sicHeight= pImg.getHeight(this);   
     
    }
    else
    {           
      sicWidth= pImg.getWidth(this);  /* update guard region image size */
      sicHeight= pImg.getHeight(this); 
      
      sicWidth= sicWidth+guardWidth;
      sicHeight= sicHeight+guardHeight;  
      guardImgWidth= sicWidth;
      guardImgHeight= sicHeight;      
    }         
         
    xCtr= (sicWidth/2);   /* center coords of image, for zoom/dezoom */
    yCtr= (sicHeight/2);    
    int 
      cWidth2= getSize().width,       /* update canvas size */                        
      cHeight2= getSize().height;        
        
    cWidth= cWidth2;
    cHeight= cHeight2;
    
    is.isWidth= sicWidth;           /* update parent */
    is.isHeight= sicHeight; 
    
    is.iData.magVal= is.iData.state.zoomMagVal;   
    
    /* [3] part of guard region code, create an offscreen image first, 
     * paint the various objects, ROIs, LMs, guard regions etc to it */     
    Image imgG= null;
    Image imgG2= null;
    Graphics offScreenG;  /* for off screen drawing of guard region */
    Graphics offScreenG2;    
    
    if(sicWidth == 0 ||sicHeight == 0)// can not create a 0,0 img
      return;
    imgG= createImage(sicWidth,sicHeight);         
    offScreenG= imgG.getGraphics();          
    
    /* [4] limit the zoom */
    is.iData.mag= Math.max(is.iData.magVal, SliderState.MIN_ZOOM_MAG_VAL);
    is.iData.mag= Math.min(is.iData.mag, SliderState.MAX_ZOOM_MAG_VAL);    
    if(flk.NEVER)
    {
      String str= "ImageScroller.paint() iW="+ sicWidth+ " pImg="+pImg+" ";    
      System.out.println(str);
      is.iData.printProperties(str,pImg,null);      
    }
   
    /* Subwindow to draw. Must be < 0 since it draws the lower right
     * rectangle from there.
     */  
    int
      xS,
      yS;
    
    /* [5] If writing out the image as a GIF file. Save the current
     * graphics g. Then get a new graphics g for a tmp image. Then
     * save it to a GIF file, then restore the graphics g,
     * clear saveAsImgFile and repaint. NOTE saveAsImgFile is
     * initially null.
     */
    Image gifImage= null;    
    if(saveAsImgFile!=null)
    { /* draw into GIF file Image instead of canvas */
      gifImage= createImage(sicWidth,sicHeight);
      g= gifImage.getGraphics();
    } /* draw into GIF file Image instead of canvas */      
         
    /* [6] Get the subwindow to draw in flicker window */
    if(is.sp!=null && is.useScrollBarsFlag)
    { /* get position of left or right canvas from scroll bars */
      scrollPos= is.sp.getScrollPosition();
      viewPortSize= is.sp.getViewportSize();   
      
      if(flk.NEVER)     
       System.out.println("ImageScroller() scrollPos= "+ scrollPos+
                          "  viewPortSize="+viewPortSize+ "  is.iData.mag="+is.iData.mag);
      if(scrollPos!=null)
      { 
        int
          dWidth= - (sicWidth - cWidth),  /* - offsets */
          dHeight= - (sicHeight - cHeight);         
        
        /* Compute new ULHC base subwindow */
        xS= (scrollPos.x * dWidth)/maxCanvasDim.width;  
        yS= (scrollPos.y * dHeight)/maxCanvasDim.height;        
      }
      else
      {
        xS= 0;
        yS= 0;
      }      
    } /* get position of left or right canvas from scroll bars */
    else
    { /* get position of flicker window canvas from elsewhere */            
      if(currentIS!=null)
      {
        xS= currentIS.siCanvas.xObj;
        yS= currentIS.siCanvas.yObj-10;  /* alignment problem to be fixed */
      }
      else
      {
        xS= 0;
        yS= 0;
      }
    } /* get position of flicker window canvas from elsewhere */
    
    /* [6.1] Clip it for safety. Must be < 0 */
    xS= (xS>0) ? 0 : xS;
    yS= (yS>0) ? 0 : yS;    
    
    /* [6.2] Save the ULHC window base coordinates.
     * Note: [xyBase= -xyS].
     */
    xBase= -xS;
    yBase= -yS;
    int
      xToDraw= 0,
      yToDraw= 0,
      dx1= 0,       /* flicker window */
      dy1= 0,
      dx2= flk.flkCanvasSize,
      dy2= flk.flkCanvasSize,
      sx1= 0,       /* source img */
      sy1= 0,
      sx2= flk.flkCanvasSize,
      sy2= flk.flkCanvasSize;
    
    /* [6.3] draw the images if using scroll bars */
    if(is.useScrollBarsFlag)
    {     
      offScreenG.drawImage(pImg, 0, 0, this);     
    }
    else 
    { /* flicker window */          
      /* map to ctr of canvas */
      int
        xCenter= (flk.flkCanvasSize/2),
        yCenter= (flk.flkCanvasSize/2);
      if(yCenter>10)
        yCenter= yCenter-10;      /* quick fix for alignment trouble 
                                   * in flk window */      
     
      if(currentIS==null)/* sometimes null at init image load */
      { /* currentIS null */
         sx1= 0;         
         sx2= flk.flkCanvasSize;    
            
         sy1= 0;
         sy2= flk.flkCanvasSize;       
      } /* currentIS null */
      else
      { /* currentIS not null*/
        /* remap x for flicker window */                     
        if(currentIS.siCanvas.flkCurPos.x < xCenter)
        { /* left of center */
          sx1= 0;         
          sx2= flk.flkCanvasSize;
        } /* left of center */
        else
        { /* right of center */
          sx1= currentIS.siCanvas.flkCurPos.x - xCenter;
          if((currentIS.siCanvas.flkCurPos.x - xCenter) < 0)
          { /* out of bounds */
            sx1= 0;
            sx2= flk.flkCanvasSize;             
          } /* out of bounds */
          else
          {
            sx2= sx1 + flk.flkCanvasSize;
            
            if(sx2 > sicWidth)
            { /* out of bounds */           
              sx2= sicWidth;            
              sx1= sicWidth - flk.flkCanvasSize;                    
            } /* out of bounds */
          }         
        } /* right of center */
        
        /* [6.4] remap y for flicker window */
        if(currentIS.siCanvas.flkCurPos.y < yCenter)
        { /* up of center */
          sy1= 0;
          sy2= flk.flkCanvasSize;
        } /* up of center */
        else
        { /* down of center */
          sy1= currentIS.siCanvas.flkCurPos.y - yCenter;
          if((currentIS.siCanvas.flkCurPos.y - yCenter) < 0)
          { /* out of bounds */
            sy1= 0;
            sy2= flk.flkCanvasSize;
             
          } /* out of bounds */
          else
          {
            sy2= sy1 + flk.flkCanvasSize;
            if(sy2 > sicHeight)
            { /* out of bounds */            
              sy2= sicHeight;              
              sy1= sicHeight - flk.flkCanvasSize;              
            } /* out of bounds */
          }
        } /* down of center */              
      } /* currentIS not null*/     
       
      /* [6.5] Draw source window image.
       * See Chan & Lee Vol II, page 700.
       */ 
     if(flk.useGuardRegionImageFlag)
     { /* flicker window with guard region */          
       
       /* Create offscreen img to draw guard region and img onto */
       imgG2= createImage(guardImgWidth,guardImgHeight);
       offScreenG2= imgG2.getGraphics();
       
       /* Make background guard color for offscreen img */
       offScreenG.setColor(flk.guardRegionColor);
       offScreenG.fillRect(0,0,guardImgWidth,guardImgHeight);
       
       /* Draw pImg to the 1rst offscreen graphics */
       offScreenG.drawImage(pImg, (int) guardWidth/2,(int) guardHeight/2, this);
              
       /* Draw 1rst offscreen img to 2nd offscreen img based on correct 
        * position of right or left img */    
       offScreenG2.drawImage(imgG, 0,0,dx2,dy2, sx1,sy1,sx2,sy2,this);
       
       /* Draw offscreen img to g so guard region & img
        * will be displayed correctly */
       g.drawImage(imgG2, 0,0, this);
       
       /* Draw target overlay if enabled. */
       drawTargetOverlay(g, is, currentIS);
       
      if(flk.NEVER)
         System.out.println("IS:paint()guardRegionFlag flicker:"+isName+" guardWidth="+guardWidth+
                            "  guardHeight="+guardHeight+
                            "  guardImgWidth="+guardImgWidth+
                            "  guardImgHeight="+guardImgHeight+
                            "  sicWidth="+sicWidth+
                            "  sicHeight="+sicHeight+
                            "  cWidth="+cWidth+
                            "  cHeight="+cHeight+" xCtr="+xCtr+
                            "  yCtr="+xCtr+" flkCurPos="+flkCurPos+
                            "  flk.flkCanvasSize="+flk.flkCanvasSize
                            );    
     } /* flicker window with guard region */  
     else
     {              
        /* Draw target overlay if enabled. */       
        g.drawImage(pImg, dx1,dy1,dx2,dy2, 
                    sx1,sy1,sx2,sy2, this);
        drawTargetOverlay(g, is, currentIS);
     }           
      
     if(flk.NEVER)
      if(currentIS!=null)
        System.out.println("ImageScroller().2.5 flicker window "+
                           currentIS.siCanvas.isName+
                           " x="+
                           currentIS.siCanvas.flkCurPos.x+
                           " y="+currentIS.siCanvas.flkCurPos.x+
                           " flk.flkCanvasSize="+flk.flkCanvasSize+
                           " cWidth="+cWidth+" cHeight="+cHeight+
                           " sx1="+sx1+" sy1="+sy1+" sx2="+sx2+" sy2="+sy2+
                           " dx1="+dx1+" dy1="+dy1+" dx2="+dx2+" dy2="+dy2);
    } /* flicker window */   
       
    /* [7] Redraw landmark text[] in the image at (x,y)[] locs.
     * These are the landmarks.
     */     
    if(flk.viewLMSflag && nTextItems > 0)
      drawLandmarksTextInImage(offScreenG, sx1, sy1);
    
    /* [8] Draw trial object if valid  only for Left and Right */
    if(flk.viewTrialObjFlag && (is==flk.i1IS || is==flk.i2IS))
      drawTrialObjInImage(offScreenG);
        
    /* [9] Draw boundary if valid */
    if(flk.viewBoundaryFlag)
      is.iData.bnd.drawBoundaryInImage(offScreenG);
    
    /* [10] Draw Region Of Interest (ROI) if valid */
    if(flk.viewRoiFlag && (is==flk.i1IS || is==flk.i2IS))
      drawRoiInImage(offScreenG, pImg);
    
    /* [11] Draw BackgroundCircle "circle-B" if valid */    
    if(flk.viewMeasCircleFlag && (is==flk.i1IS || is==flk.i2IS))
      drawBackgroundCircleInImage(offScreenG, pImg);
        
    /* [12] Draw measurement "circle-M" if valid and NOT drawing
     * all of the spots in the spot list.
     */
    if(flk.viewMeasCircleFlag && !flk.spotsListModeFlag &&
       (is==flk.i1IS || is==flk.i2IS))
      drawMeasCircleInImage(offScreenG, pImg); 
    
    /* [13] Draw list of ALL spot measurements if valid */
    if(flk.spotsListModeFlag && (is==flk.i1IS || is==flk.i2IS))
      drawSpotMeasurementsInImage(offScreenG, pImg);        
    
    /* [14] Draw guard region for right or left img */
    if(guardRegionFlag && is.useScrollBarsFlag)
    { /* guard region for right or left img */
           
      if(flk.NEVER)
        System.out.println("IS:paint():"+isName+" guardWidth="+guardWidth+
                        "  guardHeight="+guardHeight+
                        "  guardImgWidth="+guardImgWidth+
                        "  guardImgHeight="+guardImgHeight+
                        "  sicWidth="+sicWidth+
                        "  sicHeight="+sicHeight+
                        "  cWidth="+cWidth+
                        "  cHeight="+cHeight);         
     
      /* [14.1] Create offscreen img to draw guard region and img onto */
      imgG2= createImage(guardImgWidth,guardImgHeight);
      offScreenG2= imgG2.getGraphics();
      
      /* [14.2] Make background guard color for offscreen img */
      offScreenG2.setColor(flk.guardRegionColor);
      offScreenG2.fillRect(0,0,guardImgWidth,guardImgHeight);
      
      
     /* [14.3] Draw imgG (which should have all other objects, LMs, etc) to the
      * 2nd offscreen graphics */
      offScreenG2.drawImage(imgG, (int) guardWidth/2, (int) guardHeight/2, this);
      
      int 
        w= pImg.getWidth(this)+(int) guardWidth/2,  
        h= pImg.getHeight(this)+(int) guardHeight/2;    
      offScreenG2.setColor(flk.guardRegionColor);      
      offScreenG2.fillRect(0,h,guardImgWidth,guardImgHeight);
      offScreenG2.fillRect(w,0,guardImgWidth,guardImgHeight);
      
      /* [14.4] Draw target overlay if enabled. */
      drawTargetOverlay(offScreenG2, is, currentIS);
      
     /* [14.5] Draw  offscreen img to g so guard region & img
      * will be displayed correctly */
      g.drawImage(imgG2,0, 0, this);      
     

    } /* guard region for right or left img */
    else
    { /* No guard region for right or left img */
      
      if(is.useScrollBarsFlag)
      { /* non-flicker windows */
        g.drawImage(imgG,0, 0, this);    
        drawTargetOverlay(g, is, currentIS);
      }/* non-flicker windows */
    } /* No guard region for right or left img */   
    
    /* [15] If drawing to a GIF file, then cvt Image to Gif stream
     * and write it out.
     */
    if(gifImage!=null)
    { /* write it out */
      WriteGifEncoder wge= new WriteGifEncoder(gifImage);
      gifImage= null;
      if(wge!=null)
        wge.writeFile(saveAsImgFile);      
      saveAsImgFile= null;    
    } /* write it out */

    if(is.repackFlag)
    {
      flk.pack();
      is.repackFlag= false;
    }      
  } /* paintComponent */
  
} /* End class: ScrollableImageCanvas */


