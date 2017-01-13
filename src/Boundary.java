/* File: Boundary.java */

import java.awt.*;

/**
 * Class Boundary performs boundary operations and its instance
 * exists in the ImageData object.
 *<P>
 * This work was produced by Peter Lemkin of the National Cancer
 * Institute, an agency of the United States Government.  As a work of
 * the United States Government there is no associated copyright.  It is
 * offered as open source software under the Mozilla Public License
 * (version 1.1) subject to the limitations noted in the accompanying
 * LEGAL file. This notice must be included with the code. The Flicker 
 * Mozilla and Legal files are available on Web site
 * http://open2dprot.sourceforge.net/Flicker
 *<P>
 * @author P. Lemkin (LECB/NCI), G. Thornwall (SAIC), Frederick, MD
 * @version $Date$   $Revision$
 * @see <A HREF="http://open2dprot.sourceforge.net/Flicker">Flicker Home</A>
 */

public class Boundary 
{
  
  private Flicker
    flk;
  
  /* --- Boundary state instance --- */
  
  /** Max boundary size  */
  final public int
    MAX_BND= 2000;	
  
  /* Stuff for boundary measurement */
  /** "Quant" or "Bkgrd"  */
  public String
    measType;	
  /** boundary is active  */
  public boolean 
    bndOpenFlag= false;		
  /** gray scale to OD map */
  float
    mapGrayToOD[];	
  /** Boundary of object */
  public Point 
    bnd[]= null;
  /** width of image to measure */
  int 
    iWidth;	
  /** height of image to measure */
  int 
    iHeight;	
  /** measurement # */
  int 
    measurementNbr= 0;		
  /** Max size of boundary array */
  int 
    maxBnd= 0;	
  /** # points drawn so far */
  int 
    nPoints= 0;	
  /** color to draw boundary */
  Color
    bndColor= Color.red;	

  /** Set object features as function of input pixels [] and boundary */
  boolean
    validFeaturesFlag= false;
  double
    mnbackground,
    density,
    sx,
    sy,
    sxy,
    maxd,
    mind,
    xAbs,
    yAbs,
    densRaw,
    sdDensity;
  int
    area,
    perim,
    merx1,
    merx2,
    mery1,
    mery2;
  /** coordinates for drawing using polyline */
  int 
    xPoints[]= null,		
    yPoints[]= null;
  
  
  /**
   * Boundary() - constructor.
   */
  public Boundary(Flicker flk)
  { /* Boundary */
    this.flk= flk; 
    
    /* Setup boundary measurement state */
    clean();
  } /* Boundary */


  /**
   * setMapGrayToOD() - set the MapGrayToOD[] table
   * @param mapGrayToOD map of grayscale to OD
   */
  public void setMapGrayToOD(float mapGrayToOD[])
  { this.mapGrayToOD= mapGrayToOD; }
  

  /**
   * drawBoundary() - draw boundary backwards
   * @param g is graphics context where we draw the boundary
   */
  public void drawBoundary(Graphics g)
  { /* drawBoundary */
    if(nPoints>0)
    { /* draw it */
      if(xPoints==null || xPoints.length!=(nPoints+1))
      { /* regrow arrays */
        xPoints= new int[nPoints+1];
        yPoints= new int[nPoints+1];
        int i;
        
        for(i=0;i<nPoints;i++)
        { /* cvt Points to ints */
          xPoints[i]= bnd[i].x;
          yPoints[i]= bnd[i].y;
        }
      }
      
      /* Draw boundary using draw polygon API. */
      g.drawPolyline(xPoints, yPoints, nPoints);
    }
  } /* drawBoundary */
  
  
  /**
   * clean() - clean up before init or when kill instance before GC
   */
  public void clean()
  { /* clean */
    measType= null;
    bnd= null;
    nPoints= 0;
    mapGrayToOD= null;
    xPoints= null;		
    yPoints= null;
    bndOpenFlag= false;
    maxBnd= 0;      
  } /* cleanup */
  
  
  /**
   * setBndState() - set the boundary state in the imageCanvas
   * @param bndOpenFlag to toggle boundary drawing overlay
   */
  final public void setBndState(boolean bndOpenFlag)
  { this.bndOpenFlag= bndOpenFlag; }
  
  
  /**
   * setBndColor() - set the boundary drawing color
   * @param c is boundary color
   */
  final public void setBndColor(Color c)
  { this.bndColor= c; }
  
  
  /**
   * getValidFeaturesFlag() - get the valid features set after measurement
   * @return true if features are valid (i.e. boundary defined and features
   *         computed)
   */
  final public boolean getValidFeaturesFlag()
  { return(validFeaturesFlag); }
  
  
  /**
   * startMeasurement() - start measurement of object.
   * @param measType is "Quant" or "Bkgrd"
   * @param iWidth is width of image
   * @param iHeight is height of image
   * @see #finishMeasurement
   */
  final public void startMeasurement(String measType, 
                                     int iWidth, int iHeight)
  { /* startMeasurement */
    this.measType= measType;
    this.iWidth= iWidth;
    this.iHeight= iHeight;
    
    this.maxBnd= MAX_BND;
    bnd= new Point[MAX_BND];
    if( "Bkgrd".equals(measType))
      this.mnbackground= 0.0;
    nPoints= 0;
    bndOpenFlag= true;
    validFeaturesFlag= false;
    
    /* [TODO] switch cursor to pencil */
    
    /* clear object features */
    this.density= 0.0;
    this.sx=  0.0;
    this.sy=  0.0;
    this.maxd= 0.0;
    this.mind= 0.0;
    this.xAbs= 0.0;
    this.yAbs= 0.0;
    this.densRaw= 0.0;
    this.sdDensity= 0.0;
    this.area= 0;
    this.perim= 0;
    this.merx1= 0;
    this.merx2= 0;
    this.mery1= 0;
    this.mery2= 0;
    
    /* Switch cursor */
    /* is.setCursor(Frame.CROSSHAIR_CURSOR); */
  } /* startMeasurement */
    
  
  /**
   * measureRegion() - compute image features for object inside boundary.
   * Measure features (area, perimeter, centroid, and
   * integrated density, if the PPX grayToNDmap[] is calibrated.
   *<PRE>
   * ALGORITHM:
   * [1] Given contiguous the convex hull list of points enclosing region.
   * [2] Define the region by a run length map with one run/line.
   * [3] When finished measuring, compute features as function of pixel
   *     data inside of the RLM. (Only count pixels INSIDE of boundary.)
   * [4] Then conditionally print these out with the area, density, etc.
   *
   * This algorithm is the same used in the FLICKER program described
   * in:
   *   Lemkin,P., Merril,C., Lipkin,L., etal. "Software Aids for
   *   the Analysis of 2D Gel Electrophoresis Images", Comp. Biomed.
   *   Res. 12, 517 (1979).
   *</PRE>
   * @param inputPixels is array of pixels to use.
   * @param pixelMask is pixel mask to use on inputPixels
   */
  public void measureRegion(int inputPixels[], int pixelMask)
  { /* measureRegion */
    int
      x, y,
      minX, maxX,		 /* extent of drawing in X */
      yTop, yBot,	   /* extent of drawing in Y */
      i,
      g,
      area,			      /* area within maximum convex hull */
      perimeter;		  /* in pixels (i.e. boundar lth) */
    double
      minD, maxD,		  /* min and max density */
      d,			      /* pixel in OD space */
      dTotPrime= 0.0,	  /* D'= D - Area * mnBkgDens */
      x0, y0,			  /* Spot centroid (density weighted means)*/
      sdX, sdY,	     	  /* spot std deviations (density weighted) */
      dX, dY,			  /* deviations from the mean */
      sdXY,			      /* cross deviation */
      sdxxSq, sdyySq,     /* centroids */
      sdxySq,	
      dTot,			      /* total density */
      dMean,			  /* mean density */
      sdD, sdDSq;		  /* density, mean density, stdDev density */
    int
      xEntrance[]= new int[iWidth], /* dynamic. alloc to # rows in image */
      xExit[]= new int[iWidth];
        
    /* [1] Clear out RLM and boundary list */
    for (y= iWidth - 1; y >= 0; y--)
    {
      xEntrance[y]= iWidth + 1;
      xExit[y]= -1;
    }
    
    yTop= iHeight + 1;		/* set to "infinity" */
    yBot= -1;
    
    minX= iWidth+1;
    maxX= -1;    
    
    /* [2] Define the region by a run length map with one run/line.
     * Build the run length map from existing boundary.
     */
    for (i= 0; i<nPoints; i++)
    {
      x= bnd[i].x;
      y= bnd[i].y;
      
      xEntrance[y]= Math.min(xEntrance[y], x);
      xExit[y]= Math.max(xExit[y], x);
      minX= Math.min(minX, x);
      maxX= Math.max(maxX, x);
      
      yBot= Math.max(yBot, y);
      yTop= Math.min(yTop, y);
    }    
    
    /* [3] When finished measuring, compute features as function
     * of pixel data inside of the RLM. (Only count pixels
     * INSIDE of boundary.
     */
    perimeter= nPoints;          /* i.e. the drawn boundary rather
                                  * than the MAX CONVEX HULL */
    area= 0;
    maxD= -10000000.0;
    minD=  10000000.0;
    dTot= 0.0;
    x0= 0;
    y0= 0;
    for (y= yTop; y <= yBot; y++)
    {
      int yw= y*iWidth;
      for (x= xEntrance[y]; x <= xExit[y]; x++)
      { /* compute features under the maximum convex hull */
        area++;
        g= (inputPixels[yw + x] & pixelMask);
        d= ((mapGrayToOD==null)
              ? (float)g
              : mapGrayToOD[g]);	/* need OD map if it exists */
        
        minD= Math.min(d, minD);
        maxD= Math.max(d, maxD);
        dTot += d;
        x0 += d*x;
        y0 += d*y;
      } /* compute features under the maximum convex hull */
    }
    
    dMean= dTot / area;		/* compute weighted means */
    x0= x0 / dTot;
    y0= y0 / dTot;
    
    /* [4] Compute the rest of the features. */
    sdDSq= sdxxSq= sdyySq= sdxySq= 0.0;
    for (y= yTop; y <= yBot; y++)
    {
      for (x= xEntrance[y]; x <= xExit[y]; x++)
      { /* compute 2nd order features */
        g= (inputPixels[y*iWidth + x] & pixelMask);
        d= ((mapGrayToOD==null)
              ? g
              : mapGrayToOD[g]);	/* need OD map */
        
        dX= (x0 - x);
        dY= (y0 - y);
        if (dX < 0)
          dX= -dX;
        if (dY < 0)
          dY= -dY;
        sdDSq += (dMean - d)*(dMean - d);
        sdxxSq += d*(dX*dX);
        sdyySq += d*(dY*dY);
        sdxySq += d*(dX*dY);
      } /* compute 2nd order features */
    }
    
    /* [5] Compute std deviations */
    sdD= (dTot==0.0) ? 0.0 : Math.sqrt((double)(sdDSq/dTot));
    sdX= (dTot==0.0) ? 0.0 : Math.sqrt((double)(sdxxSq/dTot));
    sdY= (dTot==0.0) ? 0.0 : Math.sqrt((double)(sdyySq/dTot));
    sdXY= (dTot==0.0) ? 0.0 : Math.sqrt((double)(sdxySq/dTot));
    dTotPrime= (dTot - (this.mnbackground * area)); /* could get < 0 */    
    
    /* [6] Set object features */
    this.density=   (double) dTotPrime;
    this.sx=        (double) sdX;
    this.sy=        (double) sdY;
    this.sxy=       (double) sdXY;
    this.maxd=      (double) maxD;
    this.mind=      (double) minD;
    this.area=      area;
    this.perim=     perimeter;
    this.xAbs=      (double) x0;
    this.yAbs=      (double) y0;
    this.densRaw=   (double) dTot;
    this.sdDensity= (double) sdD;
    this.merx1=     minX;
    this.merx2=     maxX;
    this.mery1=     yTop;
    this.mery2=     yBot;
    
    if(this.measType.equals("Bkgrd"))
      this.mnbackground= this.density;
    
    xEntrance= null;		      /*  G.C. */
    xExit= null;	          	  /*  G.C. */
    validFeaturesFlag= true;
  } /* measureRegion */
  
  
  /**
   * regrowBoundary() - increase boundary size
   * @param newSize of boundary
   */
  final public void regrowBoundary(int newSize)
  { /* regrowBoundary */
    if(newSize<=0 || this.nPoints>newSize)
      newSize= 2*this.maxBnd+1;
    
    Point newBnd[]= new Point[newSize+1];
    for(int i=0;i<this.nPoints;i++)
      newBnd[i]= this.bnd[i];
    this.bnd= newBnd;
    this.maxBnd= newSize;
  } /* regrowBoundary */
    
  
  /**
   * interpolatePoints() - interpolate (xOld,yOld) to (xNew,yNew) into list
   * of x,y coordinates of maximum size maxN which are pushed onto the
   * bnd[] point list.
   * Algorithm taken from Lemkin etal, Comput. in Biomed. Res., 12,
   * 517-544 (1979).
   * Compute the interpolated line from (xOld,yOld) to (xNew,yNew)
   * storing it in the arrays and returning the number of points
   * generated.
   * @param xOld is starting coordinate
   * @param yOld is starting coordinate
   * @param xNew is final coordinate
   * @param yNew is final coordinate
   * @return the # of points generated this trip.
   */
  final public int interpolatePoints(int xOld, int yOld, 
                                     int xNew, int yNew)  
  { /*interpolatePoints*/
    int
      n= 0,			/* # generate this trip */
      x, y,
      sign;
    float
      dX, dY,
      dXabs, dYabs,
      m, b;
    
    /* [1] Determine differences */
    if(nPoints>=(maxBnd-1))
      regrowBoundary(2*maxBnd+1);     /* make it fit */
    dX= (float)(xNew - xOld);
    dY= (float)(yNew - yOld);
    dXabs= Math.abs(dX);
    dYabs= Math.abs(dY);
    if((dXabs + dYabs)==0.0)
    { /* single point - null vector */
      n++;
      bnd[nPoints].x= xOld;
      bnd[nPoints++].y= yOld;
    } /* single point - null vector */    
    
    /* [2] Test if horizontal line */
    else if(dY == 0.0 && dX != 0.0)
    { /* Draw horizontal line */
      sign= (dX > 0.0) ? 1 : -1;
      x= xOld;
      y= yOld;
      while ((nPoints<maxBnd) &&
             ((sign>0 && x<=xNew) || (sign<0 && x >=xNew)))
      { /* loop generating points */
        n++;
        bnd[nPoints].x= x;
        bnd[nPoints++].y= y;
        x += sign;
      } /* loop generating points */
    } /* Draw horizontal line */    
    
    /* [3] Test if vertical line */
    else if(dX == 0.0 && dY != 0.0)
    { /* Draw vertical line */
      sign= (dY > 0.0) ? 1 : -1;
      x= xOld;
      y= yOld;
      while ((nPoints<maxBnd) &&
             ((sign>0 && y<=yNew) || (sign<0 && y>=yNew)))
      { /* loop generating points */
        n++;
        bnd[nPoints].x= x;
        bnd[nPoints++].y= y;
        y += sign;
      } /* loop generating points */
    } /* Draw vertical line */    
    
    /* [4] Test if draw x=m*y+b line */
    else if(dXabs <= dYabs)
    { /* Draw x=m*y+b line */
      m= dX/dY;
      b= xOld - m*yOld;
      x= xOld;
      y= yOld;
      sign= (dY > 0.0) ? 1 : -1;
      while ((nPoints<maxBnd) &&
             ((sign>0 && y<=yNew) || (sign<0 && y>=yNew)))
      { /* loop generating points */
        n++;
        x= (int)(m*y + b);
        bnd[nPoints].x= x;
        bnd[nPoints++].y= y;
        y += sign;
      } /* loop generating points */
    } /* Draw x=m*y+b line */
    
    /* [5] Test if draw y=m*x+b line */
    else if(dXabs > dYabs)
    { /* Draw y=m*x+b line */
      m= dY/dX;
      b= yOld - m*xOld;
      x= xOld;
      y= yOld;
      sign= (dX > 0.0) ? 1 : -1;
      while ((nPoints<maxBnd) &&
             ((sign>0 && x<=xNew) || (sign<0 && x>=xNew)))
      { /* loop generating points */
        n++;
        x= (int)(m*y + b);
        bnd[nPoints].x= x;
        bnd[nPoints++].y= y;
        x += sign;
      } /* loop generating points */
    } /* Draw y=m*x+b line */
    
    return(n);
  } /*interpolatePoints*/
  
  
  /**
   * getBoundaryLength() - return nPoints for boundary
   * @return length of boundary
   */
  final public int getBoundaryLength()
  { return(this.nPoints); }
  
  
  /**
   * getBoundary() - return boundary
   * @return boundary points
   */
  final public Point[] getBoundary()
  { return(this.bnd); }
    
  
  /**
   * pushBoundary() - push unique boundary points from last point to current
   * @param x is new boundary coordinate
   * @param y is new boundary coordinate
   * @return length of boundary or 0 if there is a problem
   */
  final public int pushBoundary(int x, int y)
  { /* pushBoundary */
    if(nPoints<(maxBnd-1) && (bnd[nPoints-1].x!=x || bnd[nPoints-1].y!=y))
    {
      int n= interpolatePoints(bnd[nPoints-1].x, bnd[nPoints-1].y, x,y);
      return(n);
    }
    else
      return(0);
  } /* pushBoundary */
  
    
  /**
   * eraseBoundary() - erase boundary backwards one at a time.
   */
  final public void eraseBoundary()
  { /* eraseBoundary */
    if(nPoints>0)
      nPoints--;
  } /* eraseBoundary */
  
  
  /**
   * finishMeasurement() - finish measurement and compute features
   * @param iWidth is getSize().width of image
   * @param iHeight is getSize().height of image
   * @param inputPix is image buffer pixels from iData{1|2}. input pixels
   * @param pixelMask to get gray value from low order inputPix bits
   * @return true if ok.
   */
  public boolean finishMeasurement(int iWidth, int iHeight, 
                                   int inputPix[], int pixelMask)
  { /* finishMeasurement */
    /* is.setCursor(Frame.DEFAULT_CURSOR); */    
    if(nPoints <=1)
    {
      String msg= "Can't measure - not enough points";
      Util.popupAlertMsg(msg, flk.alertColor); 
      return(false);
    }
        
    /* [1] [TEST] Close boundary */
    interpolatePoints(bnd[nPoints-1].x, bnd[nPoints-1].y, bnd[0].x, bnd[0].y);
    
    /* [2] Compute features and save in state variables */
    measureRegion(inputPix, pixelMask);  /* features for object inside boundary */    
    
    /* [3] Set flags */
    bndOpenFlag= false;
    validFeaturesFlag= true;
    
    /* [4] [TODO] up global data refs so don't cause problems with G.C. */
    
    return(true);
  } /* finishMeasurement */  
  
  
  /**
   * cvFeatures2str() - cvt object features to printable string
   * @param imageFile is image file
   * @return printable string
   */
  public String cvFeatures2str(String imageFile)
  { /* cvFeatures2str */
    double
      mnDens,
      pSQa;			/* shapeFeature= perimeter**2/area */
    String
    sR= "";
    
    /* Some derived features */
    mnDens= densRaw / area;
    pSQa= ((area == 0) ? 0.0 : (float)(perim*perim)/area);
    
    /* Compute full string suitable for a viewer window */
    sR= "Measurement #" + measurementNbr + " for [" + imageFile + "]\n" +
         " area=" + area + " perim=" + perim +
         " density=" + Util.cvf2s((float)densRaw,1) +
         " +/-" + Util.cvf2s((float)sdDensity,1) +
         " density'=" + Util.cvf2s((float)density,1) +
         " +/-"+ Util.cvf2s((float)sdDensity,1) + "\n";
    
    sR += " mnBkgrdDens=" + Util.cvf2s((float)mnbackground,2) +
          " mnDens=" + Util.cvf2s((float)mnDens,2) +
          " (P*P)/A=" + Util.cvf2s((float)pSQa,2) + "\n";

    sR += " centroid=(" + Util.cvf2s((float)xAbs,1) + "," +
         Util.cvf2s((float)yAbs,1) + ") +/-(" +
         Util.cvf2s((float)sx,2) + "," +
         Util.cvf2s((float)sy,2) + ")\n";
    
    sR += " M.E.R.=(" + merx1 + ":" + merx2 +
          "," + mery1 + ":" + mery2+ ")" +
          " [minD:maxD]=[" + Util.cvf2s((float)mind,2) + "," +
          Util.cvf2s((float)maxd,2) + "\n\n";
    
    return(sR);
  } /* cvFeatures2str */
  
  
  /**
   * drawBoundaryInImage() - redraw boundary in window if visible.
   * If the boundary exists, then draw bnd[0:nPoints-1] in bndColor.
   * @param g is graphics context
   */
  final public void drawBoundaryInImage(Graphics g)
  { /* drawBoundaryInImage */
    if(nPoints>0 && bnd!=null)
    {
      int
        x1,
        y1,
        x2= bnd[0].x,
        y2= bnd[0].y;
      
      g.setColor(bndColor);
      for(int i=0; i<nPoints; i++)
      {
        x1= x2;
        y1= y2;
        x2= bnd[i].x;
        y2= bnd[i].y;
        g.drawLine(x1,y1,x2,y2);
      }
    }
  } /* drawBoundaryInImage */
  
  
  /**
   * processBoundaryMode() - process drawing mouse event if boundaryMode
   * @param x is point to push
   * @param y is point to push
   * @param shiftMod status
   */
  final public void processBoundaryMode(int x, int y, boolean shiftMod)
  { /* processBoundaryMode */
    if(bndOpenFlag)
      return;
    
    /* PUSH/ERASE boundary points in image */
    if(!shiftMod)
      pushBoundary(x,y);
    else
      eraseBoundary();    
  } /* processBoundaryMode */
  
  
} /* End class: Boundary */


