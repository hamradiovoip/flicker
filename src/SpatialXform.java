/* File: SpatialXform.java */

import java.util.*;
import java.lang.*;
import java.io.*;

/**
 * Class SpatialXform is used for doing non-linear order warping
 * These methods are derived from:
 *<PRE>
 * Digital Image Warping, George Wolberg,
 * IEEE Computer Press Monograph, Los Alamitos, CA, 1990.
 * ISBN-0-8186-8944-7
 *
 * See Section 3.6 Polygonal Transformations pp 61-75. A bivariate
 * polygonal transformation for performing spatial interpolation
 *
 * This generates (aV, bV) from 6 landmarks.
 *
 * u=  SUM     SUM    aU_ij * (x**i) * (y**j)
 *    i=0:n  i=0:n-1
 *
 * v=  SUM     SUM    bV_ij * (x**i) * (y**j)
 *    i=0:n  i=0:n-1
 *
 * We use (n=2) (for n=1, this is a 3x3 Affine xform).
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

public class SpatialXform
{ /* SpatialXform */
  /** Global instance */
  private static Flicker
    flk;
  /** landmarks to use */
  private static Landmark
    lms;	
  private static Util
    util;
	
  /** Maximum number of terms in spatial transform */
  final public int 
    MXTERMS= 10;

  /* ---Points for computing u(x,y) X-axis --- */
  
  /** # of CTRL points in x1[], y2[], zU[] */
  int
    nU;			 
  /** x1 is list of x coordinates for image 1 */
  double
    x1[];		 
  /** x2 is list of x coordinates for image 2 */
  double
    x2[];			/** ARG: */
  /** OUTPUT: z[]= f(x[],y[]) */
  double
    z12U[];		
  /** [MXTERMS][MXTERMS]coefficients */
  double
    aU[][];		
		
  /* --- Points for computing v(x,y) Y-axis --- */
  /** # of CTRL points in y1[], y2[], zV[] */
  int
    nV;	
  /** y1 is list of x coordinates for image 1 */
  double 
    y1[];	
  /** y2 is list of x coordinates for image 2 */
  double
    y2[];	
  /** OUTPUT: z[]= f(x[],y[]) */
  double 
    z12V[];	
  /** [MXTERMS][MXTERMS] coefficients */
  double
    bV[][];		
  /** (dynamic) weights */
  double
    wM[];			
  
  
  /**
   * SpatialXform() - constructor.
   */
  public SpatialXform(Flicker flk)
  { /* SpatialXform */
    this.flk= flk;
    
    this.lms= flk.lms;
    this.util= flk.util;
    
    cleanup();       /* destroy data structures */
  } /* SpatialXform */
  
  
  /**
   * cleanup() - destroy data structures
   */
  public void cleanup()
  {
    x1= null;
    x2= null;
    z12U= null;
    aU= null;
    y1= null;
    y2= null;
    z12V= null;
    bV= null;
    wM= null;
  }
  
  
  /**
   * solvePolyXformCoef() - solve the aU, bV coefficients from LMS data.
   * @return true if succeed
   */
  public boolean solvePolyXformCoef()
  { /* solvePolyXformCoef */
    double delta= 0.0;		   /* smoothing factor, 0 is not smooth  */
    int
      nLM= lms.nLM,
      termsA,
      termsB,
      nM= 3;
    
    nU= nLM;
    nV= nLM;
    x1= new double[nLM];
    x2= new double[nLM];
    y1= new double[nLM];
    y2= new double[nLM];
    
    for(int i= 0; i<nLM; i++)
    { /* make copy of the values */
      x1[i]= (double)lms.x1[i];
      x2[i]= (double)lms.x2[i];
      y1[i]= (double)lms.y1[i];
      y2[i]= (double)lms.y2[i];
    }
    z12U= new double[nLM];
    z12V= new double[nLM];
    aU= aU;
    bV= bV;
    aU= new double[MXTERMS][MXTERMS];
    bV= new double[MXTERMS][MXTERMS];
    termsA= fitWarpLSQ(nM, x1, x2, z12U, aU, delta);
    termsB= fitWarpLSQ(nM, y1, y2, z12V, bV, delta);
    
    return((termsA>0) && (termsB>0));
  } /* solvePolyXformCoef */
  
    
  /**
   * get_aU() - get aU[MXTERMS][MXTERMS] array
   * this is solution to:
   *<PRE>
   * u=  SUM     SUM    aU_ij * (x**i) * (y**j)
   *    i=0:n  i=0:n-1
   *</PRE>
   * @return aU matrix
   */
  final public double[][] get_aU()
  { /* get_aU */
    return(aU);
  } /* get_aU */
    
  
  /**
   * get_bV() - get bV[MXTERMS][MXTERMS] array
   * this is solution to:
   *<PRE>
   * v=  SUM     SUM    bV_ij * (x**i) * (y**j)
   *    i=0:n  i=0:n-1
   *</PRE>
   * @return bV matrix
   */
  final public double[][] get_bV()
  { /* get_bV */
    return(bV);
  } /* get_bV */
 
  
  /**
   * evalPoly() - eval f_n(x,y)
   *<PRE>
   * f=  SUM     SUM    c_ij * (x**i) * (y**j)
   *    i=0:n  i=0:n-1
   *</PRE>
   * @param n ishighest degree of polynomial
   * @param xInt is point
   * @param yInt is point
   * @param aM is [MXTERMS][MXTERMS] coeffs. 
   * @return value of f_n(x,y)
   */
  final public int evalPoly(int n, int xInt, int yInt, double aM[][] )
  { /* evalPoly */
    int
      i,
      j;
    double
      xI,
      yJ,
      x= (double)xInt,
      y= (double)yInt,
      val= 0.0;
    
    for(i=0;i<=2; i++)
      for(j=0;j<2; j++)
      {
        switch(i)
        {
          case 0: xI= 1.0; break;
          case 1: xI= x; break;
          case 2: xI= x*x; break;
          case 3: xI= x*x*x; break;
          default: xI= 1.0;
        }
        
        switch(j)
        {
          case 0: yJ= 1.0; break;
          case 1: yJ= y; break;
          case 2: yJ= y*y; break;
          case 3: yJ= y*y*y; break;
          default: yJ= 1.0;
        }
        
        val += aM[i][j] * xI * yJ;
      }
    
    return((int)val);
  } /* evalPoly */
  
  
  /**
   * basis() - return (x,y) value of f'th orthogonal basis function.
   * pg 75
   * @param f is basis function desired 
   * @param x point
   * @param y point
   * @return (x,y) value of f'th orthogonal basis function
   */
  final static double basis(int f, double x, double y)
  { /* basis */
    double h;
    
    switch(f)
    {
      case 0: h= 1.0;   break;
      case 1: h= x;     break;
      case 2: h= y;     break;
      case 3: h= x*x;   break;
      case 4: h= y*y;   break;
      case 5: h= y*y;   break;
      case 6: h= x*x*x; break;
      case 7: h= x*x*y; break;
      case 8: h= x*y*y; break;
      case 9: h= y*y*y; break;
      default: h= 0;
    }
    
    return(h);
  } /* basis */
  
  
  /**
   * poly() - compute k'th polynomial function at point (x,y).
   * pg 74
   * @param k is k'th fct
   * @param x point
   * @param y point
   * @param aM coefficients
   * @return k'th polynomial function at point (x,y)
   */
  final static double poly(int k, double x, double y, double aM[][])
  { /* poly */
    int i;
    double p;
    
    for(i=0, p= 0.0; i<k; i++)
      p += (aM[k][i] * poly(i,x,y, aM));
    p += (aM[k][k] * basis(k,x,y));
    
    return(p);
  } /* poly */
  
  
  /**
   * coef() - Find kth mapping function coefficient (Eq. 3.6.24)
   * pg 74
   * @param k is kth mapping function 
   * @param nM is #CTL pts xM[],yM[],zM[]
   * @param xM list of points
   * @param yM list of points
   * @param wM weights
   * @param zM z[]= f(x[],y[]
   * @param aM coefficients
   * @return kth mapping function coefficient
   */
  final static double coef(int k, int nM, double xM[], double yM[], 
                           double wM[], double zM[], double aM[][])
  { /* coef */
    int i;
    double
      p,
      num,
      denum;

    num= 0.0;
    denum= 0.0;
    for(i= 0; i<nM; i++)
    {
      p= poly(k, xM[i], yM[i], aM);
      num   += (wM[i] * zM[i] * p);
      denum += (wM[i] * p * p);
    }
    
    return(num / denum);
  } /* coef */
    
  
  /**
   * init_alpha() - compute parameter alpha(j,k) (EQ. 3.6.23)
   * pg 73
   * @param j index
   * @param k index
   * @param nM is #CTL pts xM[],yM[],zM[]
   * @param xM list of points
   * @param yM list of points
   * @param wM weights
   * @param aM coefficients
   * @return alpha(j,k)
   */
  final static double init_alpha(int j, int k, int nM, double xM[],
                                 double yM[], double wM[], double aM[][])
  { /* init_alpha */
    int i;
    double
      a,
      h,
      p,
      num,
      denum;
    
    if(k==0)
      a= 1.0;			/* case 0: a_j0 */
    
    else if(j==k)
    { /* case 1: a_jj */
      num= 0;
      denum= 0;
      for(i= 0; i<nM; i++)
      {
        h= basis(j, xM[i], yM[i]);
        num   += wM[i];
        denum += (wM[i] * h);
      }
      
      a= -num / denum;
    } /* case 1: a_jj */
    
    else
    { /* case 2: a_jk */
      num= 0;
      denum= 0;
      for(i= 0; i<nM; i++)
      {
        h= basis(j, xM[i], yM[i]);
        p= poly(k, xM[i], yM[i], aM);
        num   += (wM[i] * p * h);
        denum += (wM[i] * p * p);
      }
      
      a= (-aM[j][j] * num) / denum;
    } /* case 2: a_jk */
    
    return(a);
  } /* init_alpha */
  
  
  /**
   * fitWarpLSQ() - weighted LSQ spatial transform w/ orthogonal polynomials
   * Based on algorithm described by Ardeshir Goshtasby in "Image
   * resgistration by local approximation methods", Image and Vision
   * computing, Vold 6, No. 4, 1988. pg 72 
   * @param nM is #CTL pts xM[],yM[],zM[]
   * @param xM list of points
   * @param yM list of points
   * @param zM return z[]= f(x[],y[]
   * @param aM coefficients
   * @param delta is smoothing factor, 0 is not smooth
   * @return the number of terms.
   */
  public int fitWarpLSQ(int nM, double xM[], double yM[], double zM[],
                        double aM[][], double delta )
  { /* fitWarpLSQ */
    int
      i,			/*  */
      j,			/*  */
      k,			/*  */
      x,			/*  */
      y,			/*  */
      t,			/*  */
      terms= 0;		/*  */
    double
      a,			/*  */
      f,			/*  */
      p,			/*  */
      dx2,	  /*  */
      dy2;		/*  */
    
    /* [1] nM is # of control points */
    wM= new double[nM];	 /* memory for weights */
    
    
    /* [2] Determine # of terms necessary for error < 0.5 (optional) */
    double
      dx,
      dy;
    for(terms=3; terms<MXTERMS; terms++)
      for(i=0;i<nM; i++)
      { /* initialize */
        for(j=0; j<nM;j++)
        { /* init wM[]: the weights of the nM control points on x,y */
          dx= (xM[i] - xM[j]);
          dy= (yM[i] - yM[j]);
          
          dx2= dx*dx;
          dy2= dy*dy;
          wM[i]= 1.0/Math.sqrt(dx2+dy2+delta);
        }
        
        for(j=0; j<terms;j++)
        { /* Init aM[][]: alpha_jk coeffs on the ortho polynomials */
          aM[j][j]= init_alpha(j,j, nM, xM, yM, wM, aM);
          for(k=0; k<j; k++)
            aM[j][k]= init_alpha(j,k, nM, xM, yM, wM, aM);
        }
        
        for(t=0, f= 0; t<terms; t++)
        { /* compute error at each control point over all terms */
          a= coef(t, nM, xM, yM, wM, zM, aM);
          p= poly(t, xM[i], yM[i], aM);
          f += (a * p);
          if(Math.abs(zM[i] - f) > 0.5)
            break;
        }
        
        if(i==nM)
          break;		/* found terms such that error < 0.5 */
      } /* initialize */
    
    return(terms);
  } /* fitWarpLSQ */
  
    
  /**
   * computeSurfaceWarp() - compute surface warp from data.
   * @param nM is # CTRL points xM, yM, zM
   * @param terms is # of terms
   * @param xM points
   * @param yM points
   * @param zM return z[]= f(x[],y[])
   * @param aM coefficients
   * @param delta is smoothing factor, 0 is not smooth
   * @param xSize is size of xM
   * @param ySize is size of yM
   * @param sOut return fitted surface values of points (xSize by ySize).
   * @return true if succeed
   */
  public boolean computeSurfaceWarp(int nM, int terms, 
                                    double xM[], double yM[],
                                    double zM[], double aM[][],
                                    double delta, int xSize, int ySize,
                                    double sOut[])
  { /* computeSurfaceWarp */
    int
      i,				/*  */
      j,				/*  */
      k,				/*  */
      m,				/*  */
      x,				/*  */
      y,				/*  */
      t;				/*  */
    double
      a,		  	/*  */
      f,			  /*  */
      p,			  /*  */
      dx2,			/*  */
      dy2;			/*  */    
    
    /* Perform surface approximation */
    m= 0;
    double
      dx,
      dy;
    for(y= 0; y<ySize; y++)
    { /* process y */
      for(x= 0; x<xSize; x++)
      { /* process x */
        for(i= 0; i<nM; i++)
        {
          dx= ((double)x - xM[i]);
          dy= ((double)y - yM[i]);
          
          dx2= dx*dx;
          dy2= dy*dy;
          wM[i]= 1.0/Math.sqrt(dx2+dy2+delta);
        }
        
        for(j=0; j<terms;j++)
        { /* Init aM[][]: alpha_jk coeffs on the ortho polynomials */
          aM[j][j]= init_alpha(j,j, nM, xM, yM, wM, aM);
          for(k=0; k<j; k++)
            aM[j][k]= init_alpha(j,k, nM, xM, yM, wM, aM);
        }
        
        for(i=0, f= 0; i<terms; i++)
        { /* evaluate surface (x,y) over all terms */
          a= coef(i, nM, xM, yM, wM, zM, aM);
          p= poly(i, (double)x, (double)y, aM);
          f += (a * p);
        }
        
        sOut[m++]= (double) f;	/* save fitted surface values */
      } /* process x */
    } /* process y */
    
    return(true);
  } /* computeSurfaceWarp */
    
  
  /**
   * warpLSQ() - weighted LSQ spatial transform with orthogonal polynomials
   * Based on algorithm described by Ardeshir Goshtasby in "Image
   * resgistration by local approximation methods", Image and Vision
   * computing, Vold 6, No. 4, 1988. pg 72
   * @param nM is # CTRL points xM, yM, zM
   * @param xM points
   * @param yM points
   * @param zM return z[]= f(x[],y[])
   * @param aM coefficients
   * @param delta is smoothing factor, 0 is not smooth
   * @param xSize is size of xM
   * @param ySize is size of yM
   * @param sOut return fitted surface values of points (xSize by ySize).
   * @return true if succeed
   */
  public boolean warpLSQ(int nM, double xM[], double yM[], double zM[], 
                         double aM[][], double delta, int xSize, int ySize, 
                         double sOut[])
  { /* warpLSQ */
    /* [1] Determine # of terms necessary for error < 0.5 (optional) */
    int terms= fitWarpLSQ(nM, xM, yM, zM, aM, delta);    
    
    /* [2] Perform surface approximation */
    computeSurfaceWarp(nM, terms, xM, yM, zM, aM,
    delta, xSize, ySize, sOut);
    
    return(true);
  } /* warpLSQ */
  
} /* end of class SpatialXform */
