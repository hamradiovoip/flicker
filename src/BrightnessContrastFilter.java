/* File: BrightnessContrastFilter.java */


import java.awt.image.*;


/** 
 * Class BrightnessContrastFilter implements RGBFilter for image adjustment
 * to implement a contrast/brightness filter. It maps the unscaled
 * (brightness,contrast) values with maximum (maxBrightness,maxContrast) to
 * brightness and contrast values (bScaled,cScaled) in [0.0:1.0].
 * The Filter maps RGB pixel values to a new RGB pixel value as a function 
 * of (bScaled,cScaled) and the (maxGray,minG,maxG) of the image.
 *<PRE>
 * The brightness, contrast model assumes that the half-way range of 
 * each is the original image value. The transform  for the new gray value
 * g' is:
 *       g' = slopeC*g + bIntercept.
 *
 * These are derived as follows:
 *       maxSlopeC= maxGray / (maxG - minG),
 *       bIntercept = (bScaled - 0.5) * (2 * maxGray).
 * We then  compute the contrast intercept to cover the complete range
 * so that
 *   1. if cScaled=1.0, then slopeC= maxSlopeC,
 *   2. if cScaled=0.5, then slopeC= 1.0.
 * Then,
 *       ssM= maxSlopeC-1.0) / 0.5,
 *       ssB= (0.5 * maxSlopeC + 2),
 * and then,
 *       slopeC= ssM*cScaled + ssB.    
 *</PRE>
 * The filtering part is derived from the code in the Chan & Lee 
 * "The Java Class Library", (JDK1.0.2) pg 1143.
 *<P>
 * This is also similar to the example in the more recent 
 *  JavaClassLibJDK1.1.2/JavaClassVol2/java/awt.image/
 *    RGBImageFilter/canFilterIndexColorModel
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

public class BrightnessContrastFilter extends RGBImageFilter 
{
  /* dbugCtr for debugging */
  private static int   
    dbugCtr= 0; 
  
  /** flicker instance */
  static Flicker
    flk; 
  
  /** The Image Scroller for the image data to compute filter on */
  ImageScroller
    is;
  
  /** method to use when filter RBG: 
   * (NORM_COLOR, PSEUDO_COLOR, RGB_TO_GRAY_COLOR)
   */
  int 
    colorMode= Flicker.NORM_COLOR;
    
  /** cvt RGB to grayscale. Get from global flag. 
   * Only should map RGB to grayscale if not doing sequential transforms
   * since then would transform garbage.
   */ 
  boolean 
    rgbToGrayFlag= false;	
  /** true if pseudo color else grayscale. Get from global flag
   * Only should map grayscale to RGB if not doing sequential transforms
   * since then would transform garbage.
   */
  boolean 
    pseudoColorFlag= false;	
     
  /** array hold return values (R,G,B) for hsv2rgb(H,S,V) */
  int
    rgb3[]= new int[3]; 
  /** current hue pixel value when  HSV filtering */ 
  float
    hue;
  /** current saturation pixel value when HSV filtering */ 
  float
    saturation;
  /** current value pixel value when HSV filtering */ 
  float
    value;
  /** current RED pixel when filter is running */
  int
    r;
  /** current GREEN pixel when filter is running */
  int
    g;
  /** current BLUE pixel when filter is running */
  int
    b;
  /** current new RGB pixel when filter is running */
  int
    newRGBpixel;
  /* Maximum gray value. E.g., for 8-bits it is 255, etc. */
  int
    maxGray;
  /** maximum Gray value seen in the original inputpixels[]. -1 means
   * it is not defined.
   */
  public int
    maxG= -1;  
  /** minimum Gray value seen in the original inputpixels[]. -1 means
   * it is not defined.
   */
  public int
    minG= -1;
  /** maximum contrast needed to cover the maximum dynamic range
   * compute as: maxSlopeC= maxGray/(maxG-minG));
   */
  float
    maxSlopeC;
  /** brightness intercept is (bS -0.5)*(2*maxGray) */
  float
    bIntercept;
  /** contrast slope is (cS -0.5)*maxSlopeC */
  float
    slopeC;
   /** scaled current brightness value to range of [0:1.0] */
  float
    bScaled;
   /** scaled current contrast value to range of [0:1.0] */
  float
    cScaled;
    
    
  /**
   * BrightnessContrastFilter() - constructor.
   */
  public BrightnessContrastFilter(Flicker flkS)
  { /* BrightnessContrastFilter */
    flk= flkS;
     
    canFilterIndexColorModel= true;
  } /* BrightnessContrastFilter */
    
  
  /**
   * setBrCt() - set parameters for the filter when it is invoked by 
   * pressing or dragging the mouse with the Shift key pressed.
   * @param is is the image data to compute filter on
   * @param brightness new value of brightness [0:maxBrightness-1], unscaled
   * @param contrast new value of contrast [0:maxContrast-1], unscaled
   * @param maxBrightness max value of brightness, unscaled
   * @param maxContrast max value of contrast, unscaled
   * @param calcBrightnessFlag use slider values or calc via mousedDragged
   *        [DEPRICATED]
   */
  final void setBrCt(ImageScroller is, int brightness, int contrast, 
                     int maxBrightness, int maxContrast,
                     boolean calcBrightnessFlag)
  { /* setBrCt */
    dbugCtr++;                      /* debugging dbugCtr */
    
    this.is= is;
  
    maxGray= is.iData.pixelMask;
    this.minG= is.iData.minG;
    this.maxG= is.iData.maxG; 
    
    if(Flicker.NEVER)
    {
      int scrBrightness= flk.bGui.brightnessBar.getValue();
      System.out.println("BCF-SBC.1 dbugCtr="+dbugCtr+
                         " brt="+brightness+" ctrst="+contrast+
                         " flk.bGui.brightnessLabel="+
                         flk.bGui.brightnessLabel.getText()+
                         " scrBrightness="+scrBrightness+
                         " minG=" + minG + " maxG=" + maxG);       
    }
    /* Local value to use for For brightness / contrast calc.
     * Convert to floats for computations
     */
    float
      bF= (float)Math.min(brightness, Math.max(brightness,maxBrightness)),
      cF= (float)Math.min(contrast, Math.max(contrast,maxContrast)),
      maxBF= (float)maxBrightness,     /* max brightness value */
      maxCF= (float)maxContrast;       /* max contrast value */
   
      
    /* Copy global flags since local testing is faster */
    rgbToGrayFlag= flk.viewRGB2GrayFlag;
    pseudoColorFlag= flk.viewPseudoColorFlag;
    /*
    if(rgbToGrayFlag)
      colorMode= PSEUDO_COLOR;
    else if(pseudoColorFlag)
      colorMode= RGB_TO_GRAY_COLOR;
    */
    this.colorMode= (flk.composeXformFlag)
                       ? Flicker.NORM_COLOR : flk.colorMode;
  
    /* current values scaled to [0.0:1.0] */
    bScaled= (bF/maxBF);	          
    cScaled= (cF/maxCF);
    
    /* Compute the maximum slope */
    // this.maxGray= maxGray; // Error ??
   
    /* NOTE: 120 was empirically determined as a reasonable interactive
     * range.
     */
    maxSlopeC= (float)maxGray/((float)(maxG-minG)*120);
              
    /* Compute the brightness intercept bIntercept to cover the 
     * complete range of [0:maxGray] as follows:
     * 1. if g= 0, then if bIntercept is maxGray, then  
     *   (slopeC*g +bIntercept) is maxGray.
     * 2. if (slopeC*g) is maxGray, then if bIntercept is -maxGray, then
     *   (slopeC*g +bIntercept) is 0.
     */
    /* NOTE: in computing the actual bIntercept, although (2*maxGray) is
     * correct, the empirically determined value (1*maxGray) worked better.
     */
    //bIntercept = (bScaled -0.5F)*(2*maxGray);
    bIntercept = (bScaled - 0.5F)*(maxGray);
    
    /* Compute the contrast slopeC to cover the complete range
     * so that 
     * 1. if cS=1.0, then slopeC= maxSlopeC
     * 2. if cS=0.5, then slopeC= 1.0
     */
    float
      ssM= ((maxSlopeC - 1.0F)/0.5F),
      ssB= (0.5F * maxSlopeC) + 2;
    slopeC= (ssM*cScaled) + ssB;
        
    if(Flicker.NEVER)
      System.out.println("BCF-SBC.2 dbugCtr="+dbugCtr+
                         " brt="+brightness+" ctrst="+contrast+
                         " bF="+bF+
                         " maxBF="+maxBF+
                         " bScaled="+bScaled+" cScaled="+cScaled+
                         " slopeC="+slopeC+
                         " bIntercept="+bIntercept);
    
    if(Flicker.NEVER)
      Util.printCurrentMemoryUsage("BCF: setBrCt() end");   
  } /* setBrCt */
    
       
  /**
   * hsv2rgb() - convert (hue,saturation,value) color model to
   * (red,green,blue) color model. Note: all domains and ranges are 0
   * to 1.0.
   * This is taken from: R. Sproull, W. Sutherland, and M. Ullner,
   * Device Independent Graphics, McGraw Hill, 1985, page 488.
   * @param hue in the range of [0.0:1.0]
   * @param saturation in the range of [0.0:1.0] default to 1.0
   * @param value of dynamic range
   * @param rgb is the red[0], green[1], blue[3] colormap
   */
  final void hsv2rgb(float hue, float saturation, float value, int rgb[])
  { /* hsv2rgb */
    final float wrapHexcone= 5.9F;  /* NOTE: so does NOT wrap around */
    float
      /* hh= 6.0F * hue,  */   /* 6.0F is FULL cycle=Wraps all the way */
      /* hh= 5.0F * hue,  */   /* Wraps almost all the way around */
      hh= wrapHexcone * hue,
      f= hh - ((int)hh);
    int
      baseColor= (int)hh,
      m= (int)(value *(1.0-saturation)),
      n= (int)(value *(1.0-saturation*f)),
      k= (int)(value *(1.0-saturation*(1.0-f))),
      red,
      green,
      blue;
    
    switch (baseColor)
    { /* pick a color triple */
      case 1: /* RED */
        red= n;
        green= (int)value;
        blue= m;
        break;
      case 2: /* YELLOW */
        red= m;
        green= (int)value;
        blue= k;
        break;
      case 3: /* GREEN */
        red= m;
        green= n;
        blue= (int)value;
        break;
      case 4: /* CYAN */
        red= k;
        green= m;
        blue= (int)value;
        break;
      case 5: /* BLUE */
        red= (int)value;
        green= m;
        blue= n;
        break;
      default: /* MAGENTA */
        red= (int)value;
        green= k;
        blue= m;
        break;
    } /* pick a color triple */
    
    rgb[0]= red;
    rgb[1]= green;
    rgb[2]= blue;
  } /* hsv2rgb */
  
  
  /**
   * cvByteToInt() - convert signed byte [-128:+127] to unsigned int
   * in range [0:255]
   * @param b is byte to convert
   * @return 
   */
  final int cvByteToInt(byte b)
  { /* cvByteToInt */ 
    int i= (int) 128+b; 
    return(i);
  } /* cvByteToInt */
  
  
  /**
   * cvByteToChar() - convert signed byte [-128:+127] to unsigned char
   * in range [0:255]
   * @param b is byte to convert
   * @return unsigned char
   */
  final char cvByteToChar(byte b)
  { /* cvByteToChar */
    char c= (char)((b>=0) ? (int)b : (int)(128-b-1));
    return(c);
  } /* cvByteToChar */
  
  
  /**
   * cvIntToByte() - convert int in range [0:255] to signed byte
   * in range of [-128:+127].
   * @param i is integer to convert
   * @return usigned byte
   */
  final byte cvIntToByte(int i)
  { /* cvIntToByte */
    byte b= (byte) (i-128);
    return(b);
  } /* cvIntToByte */
    
  
  /**
   * filterRGB() - 1:1 pixel filtering required for RGBFilter class.
   ** compute (r,g,b)' = f(r,g,b) where f() is a function of the
   * colorMode (NORM_COLOR, 
   * @param x coordinate of pixel (not used)
   * @param y coordinate of pixel  (not used)
   * @param rgb is pixel (r,g,b) value
   * @return new filtered pixel (r,g,b) value
   */
  public int filterRGB(int x, int y, int rgbPixel)
  { /* filterRGB */     
    /* [1] Get the individual colors */
    r= (rgbPixel >> 16) & 0xff;
    g= (rgbPixel >> 8) & 0xff;
    b= (rgbPixel >> 0) & 0xff;    
    
    /* [2] Calculate the new gray values for each channel */
    switch(colorMode)
    { /* process (r,g,b) */
      /* Do linear scaling */
      case Flicker.NORM_COLOR:
        r = (int) (slopeC*r + bIntercept);
        g = (int) (slopeC*g + bIntercept);
        b = (int) (slopeC*b + bIntercept);
        break;
      
        /* Map 8-bit gray (use red channel) to RGB pseudocolor */
      case Flicker.PSEUDO_COLOR:
        hue= ((float)r)/maxGray;
        saturation= 1.0F;
        value= 1.0F;
        
        hsv2rgb(hue, saturation, 255.0F /*value*/, rgb3);
        r= rgb3[0];
        g= rgb3[1];
        b= rgb3[2];
        break;
        
        /* Convert RGB to NTSC grayscale */
      case Flicker.RGB_TO_GRAY_COLOR:
        r= (int)(0.33F*r);
        g= (int)(0.50F*r);
        b= (int)(0.17F*r);
        break;
        
      default:
        return(rgbPixel);       /* no change */
    } /* process (r,g,b) */
    
    /* [3] Clip each color to between [0:255] */
    r= (r<0) ? 0 : ((r>255) ? 255 : r);
    g= (g<0) ? 0 : ((g>255) ? 255 : g);
    b= (b<0) ? 0 : ((b>255) ? 255 : b);
    
    /* [4] Return the result */
    newRGBpixel= ((rgbPixel & 0xff000000) | (r << 16) | (g << 8) | (b << 0));
    
    return(newRGBpixel);
  } /* filterRGB */
  
    
} /* end class: BrightnessContrastFilter  */

