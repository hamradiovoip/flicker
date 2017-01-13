/* File: Affine.java */



/**
 * The Affine class does affine transform setup and pixel operations.
 *<PRE>
 * Given 3 landmarks (lm1,lm2,lm3) for the two images, solve 
 * the linear transforms for (a,b,c,d,e,f)
 *     x2= a*x1 + b*y1 +c 
 *     y2= d*x1 + e*y1 +f
 * Then also solve the inverse transform (a0,b0,c0,e0,f0).
 * This lets us map pixels at location (x,y) in one image to the
 * corresponding location (x',y') in the other image.
 * NOTE: it will fail if the landmarks are co-linear.
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

public class Affine
{ /* Affine */
  
  /** Global instance */
  private Flicker
    flk;
      
  /** affine xform calculations */
  public static String
    affine_calcsString;	
  
  /* +++++++ State instance +++++++++ */
  
  /** Image width  */
  private int
    width;
  /** Image height  */
  private int
    height;
  
  /* instance of affine transformations for triple */
  
  /** coeff for x2= a*x + b*y1 + c */
  public float
    a;	      
  /** coeff for x2= a*x1 + b*y1 + c */   
  public float
    b;
  /** coeff for x2= a*x1 + b*y1 + c */  
  public float
    c;
  /** coeff for y2= d*x1 + e*y1 + f */
  public float
    d;	       
  /** coeff for y2= d*x1 + e*y1 + f */
  public float   
    e;  
  /**coeff for y2= d*x1 + e*y1 + f */    
  public float  
    f;
  
  /** inverse coefficient for x2= aO*x1 + bO*y1 + cO */
  public float			
    a0;		
  /** inverse coefficient for x2= aO*x1 + bO*y1 + cO */
  public float		
    b0;
  /** inverse coefficient for x2= aO*x1 + bO*y1 + cO */
  public float		
    c0;
  /** inverse coefficient for y2= dO*x1 + eO*y1 + fO*/
  public float		
    d0;	       
  /** inverse coefficient for y2= dO*x1 + eO*y1 + fO*/
  public float		
    e0;
  /** inverse coefficient for y2= dO*x1 + eO*y1 + fO*/
  public float		
    f0;
  
  /** threshold for colinearity tests  */
  public float
    thrColinearity;
  /** LSQ colinearity err for I1 lms wrt I1 */
  public float 
    minLSQcolinearity1;
  /** LSQ colinearity error for I2 lms wrt I2 */
  public float 
    minLSQcolinearity2;
  
  /** accumulated error messages */
  public String
    errMsg= "";	
  
  /** indices of three landmarks */
  public int
    lm1= -1;	
  /** indices of three landmarks */
  public int
    lm2= -1;	
  /** indices of three landmarks */
  public int
    lm3= -1;
  	  
  /** flip left and right landmarks after copy to local [FUTURE] */
  private boolean
    isFlipLMSflag;  
  /** Landmark DB has 3 valid landmarks. This does NOT check
   * if they are co-linear.
   */
  private boolean
    isValidLMSflag;
  
  /* Local Landmarks are not valunlid unless isValidLMSflag is true. */
  private float
    x11, x12, x13,
    y11, y12, y13,      
    x21, x22, x23,      
    y21, y22, y23;

  
  /**
   * Affine() - constructor which sets up the global links
   * @param flk is main class
   */
  public Affine(Flicker flk)
  { /* Affine */
    this.flk= flk;    
  } /* Affine */
  

  /**
   * initAffine() - init the affine instance for another transform.
   * @param thrColinearity threshold
   * @param width of mapping image
   * @param height of mapping image
   */
  public void initAffine(float thrColinearity, int width, int height)
  { /* initAffine */
    this.thrColinearity= thrColinearity;
    
    this.width= width;
    this.height= height;
    
    initDefaultState();
  } /* initAffine */
  
  
  /**
   * initDefaultState() - Initialize affine state to no transform present.
   */
  public void initDefaultState()
  { /* initDefaultState */
    errMsg= "";	  /* No error */
     
    lm1= -1;		/* default is none */
    lm2= -1;
    lm3= -1;
    
    minLSQcolinearity1= 0.0F;
    minLSQcolinearity2= 0.0F;
    
    isFlipLMSflag= false;
    isValidLMSflag= false;
    
    /* coeff for 1:1 forward transform */
    a= 1.0F;	  
    b= 0.0F;
    c= 0.0F;
    d= 0.0F;	   
    e= 1.0F;  
    f= 0.0F;
  
    /* inverse coefficient for 1:1 inverse transform */
    a0= 1.0F;		
    b0= 0.0F;	
    c0= 0.0F;		
    d0= 0.0F;		
    e0= 1.0F;	
    f0= 0.0F;
  } /* initDefaultState */
  
  
  /**
   * getAffineCoef() - return array with [a,b,c,d,e,f]
   * @return list of coefficients
   */
  final public float[] getAffineCoef()
  {
    float data[]= {a,b,c,d,e,f};
    return(data);
  }
  
  
  /**
   * getINVAffineCoef() - return array with [aO,bO,cO,dO,eO,fO]
   * @return list of inverse coefficients
   */
  final public float[] getINVAffineCoef()
  {
    float data[]= {a0,b0,c0,d0,e0,f0};
    return(data);
  }
  
  
  /**
   * getAffineMLSQcolinearity() - get landmark colinearity.
   * @param idx is the landmark set to use (1 or 2)
   * @return minLSQcolinearity1 or minLSQcolinearity2 if index is 1 or 2.
   */
  final public float getAffineMLSQcolinearity(int idx)
  { return((idx==1) ? minLSQcolinearity1 : minLSQcolinearity2); }
  
  
  /**
   * flipLMS() - flip the local copy of the 3 landmarks
   */
  private void flipLMS()
  { /* flipLMS */ 
    float tmp;    
    tmp= x21; x21= x11; x11= tmp;  /* swap */
    tmp= x22; x22= x12; x12= tmp;  /* swap */
    tmp= x23; x23= x13; x13= tmp;  /* swap */
    tmp= y21; y21= y11; y11= tmp;  /* swap */
    tmp= y22; y22= y12; y12= tmp;  /* swap */
    tmp= y23; y23= y13; y13= tmp;  /* swap */    
  } /* flipLMS */
 
 
  /**
   * setLMSindexes() - set the 3 landmark indexes (lm1,lm2,lm3)
   * into the landmark stack where nLM should be >=3.
   * Check for legality of each landmark index.
   * @param lm1 is 1st landmark to use, LM# are >0
   * @param lm2 is 2nd landmark to use LM# are >0
   * @param lm3 is 3rd landmark to use LM# are >0
   * @return true if all 3 landmarks exist.
   */
  final public String setLMSindexes(int lm1, int lm2, int lm3,
                                     boolean isFlipLMSflag)
  { /* setLMSindexes */
    String sErr= "";            /* No error */
    this.isFlipLMSflag= isFlipLMSflag;
    
    
    /* Make sure landmarks are adequate */
    isValidLMSflag= ((Landmark.nLM>=3) &&
                     (lm1>-1 && lm2>-1 && lm3>-1) &&
                     (lm1<=Landmark.nLM && lm2<=Landmark.nLM &&
                      lm3<=Landmark.nLM) &&
                     (lm1!=lm2 && lm1!=lm3 && lm2!=lm3));  
    if(!isValidLMSflag)
    {
      sErr= "Need 3 unique landmarks for affine transform";
      return(sErr);
    }
    
    this.lm1= lm1;  /* save the landmark #s */
    this.lm2= lm2;
    this.lm3= lm3;
    
    /* Get the landmarks from the landmark database and
     * Copy landmarks to Affine state variables.
     */
    x11= (float)Landmark.x1[lm1];
    x12= (float)Landmark.x1[lm2];
    x13= (float)Landmark.x1[lm3];
    
    y11= (float)Landmark.y1[lm1];
    y12= (float)Landmark.y1[lm2];
    y13= (float)Landmark.y1[lm3];
    
    x21= (float)Landmark.x2[lm1];
    x22= (float)Landmark.x2[lm2];
    x23= (float)Landmark.x2[lm3];
    
    y21= (float)Landmark.y2[lm1];
    y22= (float)Landmark.y2[lm2];
    y23= (float)Landmark.y2[lm3];
    
    /* NOTE: this swaps the left and right landmarks */
    if(this.isFlipLMSflag)
      flipLMS();  
    
    return(sErr);
  } /* setLMSindexes */
  
  
  /**
   * calcInverseAffine() - compute inverse affine coefficients.
   * Must be called after solve forward coefficients.
   * @return TRUE if succeed.
   */
  final public boolean calcInverseAffine()
  { /*calcInverseAffine*/
    float
      ae=      (this.a * this.e),
      bd=      (this.b * this.d),
      bf=      (this.b * this.f),
      aae=     (this.a * ae),
      abf=     (this.a * bf),
      ace=     (ae * this.c),
      aeMbd=   (ae - bd),
      abfMace= (abf - ace),
      aDaeMbd=  (float)0.0;
    
    if(aeMbd==0.0 || aae==0.0 || this.e==0.0)
      return(false);		 /* problem since would divide by 0.0 */
    else
      aDaeMbd= ae/aeMbd;
    
    a0= (a * aDaeMbd);   /* Compute inverse coefficients */
    b0= -(b * aDaeMbd);
    c0= (aDaeMbd * abfMace)/aae;
    d0= -(a0 * d)/e;
    e0= (float)(1.0 - (b0 * d))/e;
    f0= -((c0 * d) + f)/e;
    
    return(true);
  } /*calcInverseAffine*/
  
    
  /**
   * colinearityLSQerr() - compute colinearity of 3 landmarks as LSQ error.
   * @param useImgNbr to use (1 or 2)
   * @return sqrt(min(lsqerr(x), lsqerr(y))/3)
   */
  public float colinearityLSQerr(int useImgNbr)
  { /* colinearityLSQerr */
    float
      x1,y1,
      x2,y2,
      x3,y3,
      mnX,
      mnY,
      xVar,
      yVar,
      minVar,
      minConlinearity;
    
    if(useImgNbr==1)
    {
      x1= x11;
      y1= y11;
      
      x2= x12;
      y2= y12;
      
      x3= x13;
      y3= y13;
    }
    else
    {
      x1= x21;
      y1= y21;
      
      x2= x22;
      y2= y22;
      
      x3= x23;
      y3= y23;
    }
    
    mnX= (x1+x2+x3)/3;
    mnY= (y1+y2+y3)/3;
    
    xVar= (x1-mnX)*(x1-mnX) + (x2-mnX)*(x2-mnX) + (x3-mnX)*(x3-mnX);
    yVar= (y1-mnY)*(y1-mnY) + (y2-mnY)*(y2-mnY) + (y3-mnY)*(y3-mnY);
    
    minVar= Math.min(xVar, yVar);
    
    minConlinearity= (float)Math.sqrt((double)minVar/3.0);
    
    return(minConlinearity);
  } /* colinearityLSQerr */
  
  
  /*
   * getAffineCalcsString() - calc affine calculations string
   * @return affy summaryization string
   */
  public String getAffineCalcsString()
  { /* getAffineCalcsString */    
    String 
      msg= "\n[Solution of Affine transform]\n" +
           getLMstr()+
           "For the solution of:\n" +
           "     x2= a*x1 + b*y1 + c\n" +
           "     y2= d*x1 + e*y1 + f\n" +
           "Where the coefficients are:\n" +
           "     a=" + this.a + ", b=" + this.b + ", c=" + this.c + "\n" +
           "     d=" + this.d + ", e=" + this.e + ", f=" + this.f + "\n"+
           "Colinearity tests LSQerr(I1)=" + this.minLSQcolinearity1 +
           ", LSQerr(I2)=" +  this.minLSQcolinearity2 + "\n";
    
    return(msg);
  } /* getAffineCalcsString */
  
  
  /**
   * getLMstr() - return the 3 landmark values for I1 and I2
   * @return string showing landmarks
   */
  private String getLMstr()
  { /* getLMstr */    
    String 
      msg= "  LM#  x1,y1   x2,y2\n" +
           "  ===  =====   =====\n" +
           "  LM1 (" + (int)x11 + "," + (int)y11 + " : " + 
                       (int)x21 + "," + (int)y21 + ")\n"+
           "  LM2 (" + (int)x12 + "," + (int)y12 + " : " + 
                       (int)x22 + "," + (int)y22 + ")\n"+
           "  LM3 (" + (int)x13 + "," + (int)y13 + " : " + 
                       (int)x23 + "," + (int)y23 + ")\n";
    return(msg);      
  } /* getLMstr */
  
 
  
  /**
   * showAffineLM() - show affine Landmarks
   * @return string 
   */
  public String showAffineLM(String msg)
  { /* showAffineLM */ 
    String outMsg= msg + "\n[Affine LM]\n" + getLMstr();
    if(Flicker.CONSOLE_FLAG || flk.dbugFlag)
      System.out.println(outMsg);    
    return(outMsg);
  } /* showAffineLM */
    
  
  /**
   * solveAffineXform() - compute the affine transformation for 3 LM
   * of the 3 landmarks and return a dynamically allocated affine object.
   *<PRE>
   * This solves the 2 simultaneous equations for (a,b,c,d,e,f):
   *        x2= a*x1 + b*y1 + c
   *        y2= d*x1 + e*y1 + f
   * given the three landmarks.   *
   * [TODO] - check calculations since coeffients look wierd...
   *</PRE>
   * @return "" if succeed, else message that particular image is co-linear.
   */
  final public String solveAffineXform()
  { /* solveAffineXform */    
    float
      x12x11= (x12 - x11),   /* local vars if use more than once */
      x13x11= (x13 - x11),
      y12y11= (y12 - y11),
      y13y11= (y13 - y11),
      x22x21= (x22 - x21),
      y22y21= (y22 - y21),
      x12x11y13y11= x12x11 * y13y11,
      x13x11y12y11= x13x11 * y12y11,
      x12x11y13y11_x13x11y12y11= x12x11y13y11 - x13x11y12y11;
    
    /* Look for trouble before it happens... i.e. dividing by 0.0 */
    if(x12x11y13y11_x13x11y12y11==0.0 || y12y11==0.0)
    {
      this.errMsg= "Co-linear LMS["+(lm1-1+"A")+","+(lm2-1+"A")+","+
                                    (lm3-1+"A");
      return(this.errMsg);
    }
    
    /* Solve for 1st (X) coefficients */
    this.b= ((x12x11 * (x23-x21) - x13x11 * x22x21) /
             x12x11y13y11_x13x11y12y11);
    this.e= ((x12x11 * (y23-y21) - x13x11 * y22y21) /
             x12x11y13y11_x13x11y12y11);
    
    /* Solve for 2nd (Y) coefficients */
    this.a= (x22x21 - this.b*y12y11) / x12x11;
    this.d= (y22y21 - this.e*y12y11) / x12x11;
    
    /* Solve the third coefficients which are the constants */
    this.c= x21 - this.a*x11 - this.b*y11;
    this.f= y21 - this.d*x11 - this.e*y11;
    
    /* Compute the least square errors to check co-linearity. */
    this.minLSQcolinearity1= colinearityLSQerr(1);
    this.minLSQcolinearity2= colinearityLSQerr(2);
    
    if(this.minLSQcolinearity1 < this.thrColinearity)
    {
      this.errMsg= "mLSQ=" + this.minLSQcolinearity1 +
                   " Co-linear I1 LMS["+(lm1-1+"A")+","+
                                      (lm2-1+"A")+","+(lm3-1+"A")+
                   "] - redefine";
      return(this.errMsg);
    }
    
    if(this.minLSQcolinearity2 < this.thrColinearity)
    {
      this.errMsg= "mLSQ=" + this.minLSQcolinearity2 +
                   " Co-linear I2 LMS["+(lm1-1+"A")+","+(lm2-1+"A")+
                                     ","+(lm3-1+"A")+ "] - redefine";
      return(this.errMsg);
    }
        
    /* compute inverse affine coefficients. */
    calcInverseAffine();  
    
    /* [] Generate summary report string */
    affine_calcsString= showAffineCalcs();
    
    flk.validAffineFlag= true;
    
    return("");
  } /* solveAffineXform */
    
  
  /**
   * mapXYtoAffineIdx() - Compute Affine transform (x',y') idx from f(x,y)
   * where idx is the index of the pixel in the input image to be used for
   * the output image. Note: pixelIdx= (y*width + x).
   *<PRE>
   * This assumes that the coefficients exist.
   *        x'= a*x + b*y + c,
   *        y'= d*x + e*y + f,
   *       idxA= (y'*width + x')
   * Then,
   *  compute gO= inputPixels[idxA]
   *</PRE>
   * @param x to map
   * @param y to map
   * @return idxI
   */
  final public int mapXYtoAffineIdx(int x, int y)
  { /* mapXYtoAffineIdx */
    int
      idxA,			  /* 1-D index of input pixel address */
      xP, yP;			/* xy Prime */
    
    /* Eval the 3 point affine transform */
    xP= (int)(this.a*x + this.b*y + this.c);
    yP= (int)(this.d*x + this.e*y + this.f);
    
    /* Test and clip if needed */
    if(xP<0)
      xP= 0;
    else if(xP>=width)
      xP= width-1;
    
    if(yP<0)
      yP= 0;
    else if(yP>=height)
      yP= height-1;
    
    /* Compute input image pixel address */
    idxA= (yP*width + xP);
    
    return(idxA);
  } /* mapXYtoAffineIdx */
  
  
  /**
   * mapXYtoInvAffineX() - Compute Inverse Affine transform x'= f(x,y)
   * to be used for the output image.
   *<PRE>
   * This assumes that the coefficients exist.
   *        x'= aO*x + bO*y + cO,
   *</PRE>
   * @param x to map
   * @param y to map
   * @return( x')
   */
  final public int mapXYtoInvAffineX(int x, int y)
  { /* mapXYtoInvAffineX */
    int xP;		          	/* xy Prime */
    
    /* Eval the 3 point affine transform */
    xP= (int)(a0*x + b0*y + c0);
    
    /* Test and clip if needed */
    if(xP<0)
      xP= 0;
    else if(xP>=width)
      xP= width-1;
    
    return(xP);
  } /* mapXYtoInvAffineX */
    
  
  /**
   * mapXYtoInvAffineY() - Compute Inverse Affine transform y' from f(x,y)
   * to be used for the output image.
   *<PRE>
   * This assumes that the coefficients exist.
   *        y'= dO*x + eO*y + fO,
   *</PRE>
   * @param x to map
   * @param y to map
   * @return (y')
   */
  final public int mapXYtoInvAffineY(int x, int y)
  { /* mapXYtoInvAffineY */
    int  yP;			/* xy Prime */
    
    /* Eval the 3 point affine transform */
    yP= (int)(d0*x + e0*y + f0);
    
    /* Test and clip if needed */
    if(yP<0)
      yP= 0;
    else if(yP>=height)
      yP= height-1;
    
    return(yP);
  } /* mapXYtoInvAffineY */
  
  
  /**
   * mapXYtoAffineX() - Compute Affine transform x' from f(x,y)
   * to be used for the output image.
   *<PRE>
   * This assumes that the coefficients exist.
   *        x'= a*x + b*y + c,
   *</PRE>
   * @param x to map
   * @param y to map
   * @return (x')
   */
  public final int mapXYtoAffineX(int x, int y)
  { /* mapXYtoAffineX */
    int xP;			/* xy Prime */
    
    /* Eval the 3 point affine transform */
    xP= (int)(a*x + b*y + c);
    
    /* Test and clip if needed */
    if(xP<0)
      xP= 0;
    else if(xP>=width)
      xP= width-1;
    
    return(xP);
  } /* mapXYtoAffineX */
    
  
  /**
   * mapXYtoAffineY() - Compute Affine transform y' from f(x,y)
   * to be used for the output image.
   *<PRE>
   * This assumes that the coefficients exist.
   *        y'= d*x + e*y + f,
   *</PRE>
   * @param x to map
   * @param y to map
   * @return (y')
   */
  public final int mapXYtoAffineY(int x, int y)
  { /* mapXYtoAffineY */
    int  yP;		                       	/* xy Prime */
    
    /* Eval the 3 point affine transform */
    yP= (int)(d*x + e*y + f);
    
    /* Test and clip if needed */
    if(yP<0)
      yP= 0;
    else if(yP>=height)
      yP= height-1;
    
    return(yP);
  } /* mapXYtoAffineY */
  
  
  /**
   * showAffineCalcs() - show affine calculations string report.
   * @return string showing Affine calculations
   */
  public String showAffineCalcs()
  { /* showAffineCalcs */
    String
      msg= "\n[Solution of Affine transform]\n" +
           "  LM#  x1,y1   x2,y2\n" +
           "  ===  =====   =====\n" +
           "  LM1 (" + (int)x11 + "," + (int)y11 + " : " + 
                       (int)x21 + "," + (int)y21 + ")\n"+
           "  LM2 (" + (int)x12 + "," + (int)y12 + " : " +
                       (int)x22 + "," + (int)y22 + ")\n"+
           "  LM3 (" + (int)x13 + "," + (int)y13 + " : " +
                       (int)x23 + "," + (int)y23 + ")\n"+
           "For the solution of:\n" +
           "     x2= a*x1 + b*y1 + c\n" +
           "     y2= d*x1 + e*y1 + f\n" +
           "Where the coefficients are:\n" +
           "     a=" + Util.cvf2s(a,4) + ", b=" + Util.cvf2s(b,4) +
                   ", c=" + Util.cvf2s(c,4) + "\n" +
           "     d=" + Util.cvf2s(d,4) + ", e=" + Util.cvf2s(e,4) + 
                   ", f=" + Util.cvf2s(f,4) + "\n" +
           "Colinearity tests LSQerr(I1)=" + minLSQcolinearity1 +
           ", LSQerr(I2)=" +  minLSQcolinearity2 + "\n";
    
    if(Flicker.NEVER)
    { /* add inverse transform */
      float 
        x11t= mapXYtoAffineX((int)x21,(int)y21),
        y11t= mapXYtoAffineY((int)x21,(int)y21),
        x21t= mapXYtoAffineX((int)x11,(int)y11),
        y21t= mapXYtoAffineY((int)x11,(int)y11),
         
        x12t= mapXYtoAffineX((int)x22,(int)y22),
        y12t= mapXYtoAffineY((int)x22,(int)y22),
        x22t= mapXYtoAffineX((int)x12,(int)y12),
        y22t= mapXYtoAffineY((int)x12,(int)y12),
         
        x13t= mapXYtoAffineX((int)x23,(int)y23),
        y13t= mapXYtoAffineY((int)x23,(int)y21),
        x23t= mapXYtoAffineX((int)x13,(int)y13),
        y23t= mapXYtoAffineY((int)x13,(int)y13);
      
      String      
        msgT= "\n[Remapped landmark coords by Affine transform]\n" +
           "  LM#at   x1,y1    x2,y2\n" +
           "  =====  ======   ======\n" +
           "  LM1at (" + (int)x11t + "," + (int)y11t + " : " +
                         (int)x21t + "," + (int)y21t + ")\n"+
           "  LM2at (" + (int)x12t + "," + (int)y12t + " : " + 
                         (int)x22t + "," + (int)y22t + ")\n"+
           "  LM3at (" + (int)x13t + "," + (int)y13t + " : " + 
                         (int)x23t + "," + (int)y23t + ")\n";
      msg += msgT;
    }
    
    return(msg);
  } /* showAffineCalcs */
    
  
}  /* end of class Affine */
