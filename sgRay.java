/*

Copyright (c) 2016 Eron Gjoni

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
associated documentation files (the "Software"), to deal in the Software without restriction, including 
without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 

 */


package sceneGraph;

import data.JSONObject;
import sceneGraph.math.SGVec_3d;
import sceneGraph.math.SGVec_3f;
import sceneGraph.math.Vec3d;

/**
 * @author Eron Gjoni
 *
 */
public class sgRay {
	public static final int X=0, Y=1, Z=2;
	public SGVec_3d  p1, p2; 

	public sgRay() {
		//this.p1 = new SGVec_3d();
	}
	
	public sgRay(SGVec_3d origin) {
		this.workingVector =  origin.copy();
		this.p1 =  origin.copy();
	}

	public sgRay(SGVec_3d p1, SGVec_3d p2) {
		this.workingVector =  p1.copy();
		this.p1 =  p1.copy();
		if(p2 != null)
			this.p2 =  p2.copy();
	}	


	public double distTo(SGVec_3d point) { 

		SGVec_3d inPoint =  point.copy();
		inPoint.sub(this.p1); 
		SGVec_3d heading =  this.heading();
		double scale = (inPoint.dot(heading)/(heading.mag()*inPoint.mag()))*(inPoint.mag()/heading.mag());

		return point.dist(this.getRayScaledBy(scale).p2); 
	}

	/**
	 * returns the distance between the input point and the point on this ray (treated as a lineSegment) to which the input is closest.
	 * @param point
	 * @return
	 */
	public double distToStrict(SGVec_3d point) { 

		SGVec_3d inPoint =  point.copy();
		inPoint.sub(this.p1); 
		SGVec_3d heading = this.heading();
		double scale = (inPoint.dot(heading)/(heading.mag()*inPoint.mag()))*(inPoint.mag()/heading.mag());
		if(scale < 0) {
			return point.dist(this.p1);   
		} else if (scale > 1) {
			return point.dist(this.p2);   
		} else {
			return point.dist(this.getRayScaledBy(scale).p2); 
		}    

	}


	/**
	 * returns the distance between this ray treated as a line and the input ray treated as a line. 
	 * @param r
	 * @return
	 */
	public double distTo(sgRay r) {
		SGVec_3d closestOnThis = this.closestPointToRay3D(r);
		return r.distTo(closestOnThis);
	}

	/**
	 * returns the distance between this ray as a line segment, and the input ray treated as a line segment
	 */	
	public double distToStrict(sgRay r) {
		SGVec_3d closestOnThis = this.closestPointToSegment3D(r);
		return closestOnThis.dist(r.closestPointToStrict(closestOnThis));
	}

	/**
	 * returns the point on this sgRay which is closest to the input point
	 * @param point
	 * @return
	 */
	public SGVec_3d closestPointTo(SGVec_3d point) { 

		workingVector.set(point);
		workingVector.sub(this.p1); 
		SGVec_3d heading = this.heading();
		heading.mag();
		workingVector.mag();
		//workingVector.normalize();
		heading.normalize();
		double scale = workingVector.dot(heading);


		return this.getScaledTo(scale);
	}

	public SGVec_3d closestPointToStrict(SGVec_3d point) {
		SGVec_3d inPoint =  point.copy();
		inPoint.sub(this.p1); 
		SGVec_3d heading = this.heading();
		double scale = (inPoint.dot(heading)/(heading.mag()*inPoint.mag()))*(inPoint.mag()/heading.mag());

		if(scale <= 0) 
			return this.p1;
		else if (scale >= 1) 
			return this.p2;
		else 
			return this.getMultipledBy(scale); 
	}

	public SGVec_3d heading(){
		if(this.p2 == null) {
			p2 =  p1.copy();
			p2.set(0d,0d,0d);
			return p2;
		}
		else {
			workingVector.set(p2);
			return  workingVector.subCopy(p1);
		}
	}

	/**
	 * manually sets the raw variables of this
	 * ray to be equivalent to the raw variables of the 
	 * target ray. Such that the two rays align without 
	 * creating a new variable. 
	 * @param target
	 */
	public void alignTo(sgRay target) {
		p1.set(target.p1);
		p2.set(target.p2);
	}
	
	public void heading(double[] newHead){
		if(p2 == null) p2 =  p1.copy();
		p2.set(newHead);
		p2.set(p1);		
	}

	public void heading(SGVec_3d newHead){
		if(p2 == null) p2 =  p1.copy();
		p2.set(p1);
		p2.add(newHead);
	}
	public void heading(SGVec_3f newHead){
		if(p2 == null) p2 =  p1.copy();
		p2.set(p1);
		p2.add(new SGVec_3d(newHead));
	}

	


	/**
	 * sets the input vector equal to this sgRay's heading.
	 * @param setTo
	 */
	public void getHeading(SGVec_3d setTo){
		setTo.set(p2);
		setTo.sub(this.p1);
	}


	/**
	 * @return a copy of this ray with its z-component set to 0;
	 */
	public sgRay get2DCopy() {
		return this.get2DCopy(sgRay.Z);
	}

	/**
	 * gets a copy of this ray, with the component specified by
	 * collapseOnAxis set to 0. 
	 * @param collapseOnAxis the axis on which to collapse the ray.
	 * @return
	 */
	public sgRay get2DCopy(int collapseOnAxis) {
		sgRay result = this.copy(); 
		if(collapseOnAxis == sgRay.X) {
			result.p1.setX_(0); 
			result.p2.setX_(0);
		}
		if(collapseOnAxis == sgRay.Y) {
			result.p1.setY_(0);
			result.p2.setY_(0);
		}
		if(collapseOnAxis == sgRay.Z) {
			result.p1.setZ_(0);
			result.p2.setZ_(0);
		}

		return result;
	}

	public SGVec_3d origin(){
		return  p1.copy();
	}

	public double mag() {
		workingVector.set(p2);
		return  (workingVector.sub(p1)).mag();   
	}

	public void mag(double newMag) {
		workingVector.set(p2);
		SGVec_3d dir =  workingVector.sub(p1);
		dir.setMag(newMag);
		this.heading(dir);   
	}


	/**
	 * Returns the scalar projection of the input vector on this 
	 * ray. In other words, if this ray goes from (5, 0) to (10, 0), 
	 * and the input vector is (7.5, 7), this function 
	 * would output 0.5. Because that is amount the ray would need 
	 * to be scaled by so that its tip is where the vector would project onto
	 * this ray. 
	 * 
	 * Due to floating point errors, the intended properties of this function might 
	 * not be entirely consistent with its output under summation. 
	 * 
	 * To help spare programmer cognitive cycles debugging in such circumstances, the intended properties 
	 * are listed for reference here (despite their being easily inferred). 
	 * 
	 * 1. calling scaledProjection(someVector) should return the same value as calling 
	 * scaledProjection(closestPointTo(someVector).
	 * 2. calling getMultipliedBy(scaledProjection(someVector)) should return the same 
	 * vector as calling closestPointTo(someVector)
	 * 
	 * 
	 * @param input a vector to project onto this ray  
	 */
	public double scaledProjection(SGVec_3d input) {
		workingVector.set(input);
		workingVector.sub(this.p1); 
		SGVec_3d heading = this.heading();
		double headingMag = heading.mag();
		double workingVectorMag = workingVector.mag();
		if(workingVectorMag == 0 || headingMag == 0) 
			return 0;
		else 
			return (workingVector.dot(heading)/(headingMag*workingVectorMag))*(workingVectorMag/headingMag);
	}


	protected SGVec_3d workingVector; 



	/**
	 * divides the ray by the amount specified by divisor, such that the 
	 * base of the ray remains where it is, and the tip
	 * is scaled accordinly.  
	 * @param divisor
	 */
	public void div(double divisor) {
		p2.sub(p1); 
		p2.div(divisor);
		p2.add(p1);
	}


	/**
	 * multiples the ray by the amount specified by scalar, such that the 
	 * base of the ray remains where it is, and the tip
	 * is scaled accordinly.  
	 * @param divisor
	 */
	public void mult(double scalar) {
		p2.sub(p1); 
		p2.mult(scalar);
		p2.add(p1);
	}


	/**
	 * Returns a SGVec_3d representing where the tip
	 * of this ray would be if mult() was called on the ray
	 * with scalar as the parameter. 
	 * @param scalar
	 * @return
	 */
	public SGVec_3d getMultipledBy(double scalar) {
		SGVec_3d result = this.heading();
		result.mult(scalar);
		result.add(p1); 
		return result;
	}


	/**
	 * Returns a SGVec_3d representing where the tip
	 * of this ray would be if div() was called on the ray
	 * with scalar as the parameter. 
	 * @param scalar
	 * @return
	 */
	public SGVec_3d getDivideddBy(double divisor) {
		SGVec_3d result =  this.heading().copy();
		result.mult(divisor);
		result.add(p1); 
		return result;
	}


	/**
	 * Returns a SGVec_3d representing where the tip
	 * of this ray would be if mag(scale) was called on the ray
	 * with scalar as the parameter. 
	 * @param scalar
	 * @return
	 */
	public SGVec_3d getScaledTo(double scale) {
		SGVec_3d result =  this.heading().copy();
		result.normalize(); 
		result.mult(scale);
		result.add(p1); 
		return result;
	}


/**
 * scale the ray outward in both directions by a large amount. (900000)
 */
	public void elongate() {
		sgRay reverseRay = new sgRay(this.p2.copy(), this.p1.copy()); 
		sgRay result = this.getRayScaledTo(900000); 
		reverseRay = reverseRay.getRayScaledTo(900000);
		result.p1 = reverseRay.p2.copy();
		this.p1.set(result.p1); 
		this.p2.set(result.p2);
	}

	public sgRay copy() {
		return new sgRay(this.p1, this.p2);  
	}

	public void reverse() {
		SGVec_3d temp = this.p1; 
		this.p1 = this.p2;
		this.p2 = temp; 
	}

	public sgRay getReversed() {
		return new sgRay(this.p2, this.p1);
	}

	public sgRay getRayScaledTo(double scalar) {
		return new sgRay(p1, this.getScaledTo(scalar));
	}

	/*
	 * reverses this ray's direction so that it 
	 * has a positive dot product with the heading of r
	 * if dot product is already positive, does nothing.
	 */
	public void pointWith(sgRay r) {
		if(this.heading().dot(r.heading()) < 0) this.reverse(); 
	}

	public void pointWith(SGVec_3d heading) {
		if(this.heading().dot(heading) < 0) this.reverse(); 
	}
	public sgRay getRayScaledBy(double scalar) {

		return new sgRay(p1, this.getMultipledBy(scalar));
	}

	public void contractTo(double percent) {
		//contracts both ends of a ray toward its center such that the total length of the ray
		//is the percent % of its current length; 
		double halfPercent = 1-((1-percent)/2f);

		p1 =  p1.lerp(p2, halfPercent);//)new SGVec_3d(p1Tempx, p1Tempy, p1Tempz);
		p2 =  p2.lerp(p1, halfPercent);//new SGVec_3d(p2Tempx, p2Tempy, p2Tempz);
	}

	public void translateTo(SGVec_3d newLocation) {

		workingVector.set(p2);
		workingVector.sub(p1);
		workingVector.add(newLocation);
		p2.set(workingVector);
		p1.set(newLocation);
	}

	public void translateTipTo(SGVec_3d newLocation) {
		workingVector.set(newLocation);
		SGVec_3d transBy =  workingVector.sub(p2); 
		this.translateBy(transBy); 
	}

	public void translateBy(SGVec_3d toAdd) {
		p1.add(toAdd); 
		p2.add(toAdd);
	}


	public void normalize() {
		this.mag(1);  
	}

	public SGVec_3d intercepts2D (sgRay r) {
		SGVec_3d result =  p1.copy();
		
		double p0_x = this.p1.x;
		double p0_y = this.p1.y; 
		double p1_x = this.p2.x;
		double p1_y = this.p2.y;

		double p2_x = r.p1.x;
		double p2_y = r.p1.y;
		double p3_x = r.p2.x; 
		double p3_y = r.p2.y;

		double s1_x, s1_y, s2_x, s2_y;
		s1_x = p1_x - p0_x;     s1_y = p1_y - p0_y;
		s2_x = p3_x - p2_x;     s2_y = p3_y - p2_y;

		double t;
		t = ( s2_x * (p0_y - p2_y) - s2_y * (p0_x - p2_x)) / (-s2_x * s1_y + s1_x * s2_y);

		//if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
		// Collision detected
		return  result.set(p0_x + (t * s1_x), p0_y + (t * s1_y), 0d);
		// }

		//return null; // No collision
	}

	/*public SGVec_3d intercepts2D(sgRay r) {
		SGVec_3d result = new SGVec_3d();

		double a1 = p2.y - p1.y;
		double b1 = p1.x - p2.x;
		double c1 = a1*p1.x + b1*p1.y;

		double a2 = r.p2.y - r.p1.y;
		double b2 = r.p1.x - r.p2.y;
		double c2 = a2* + b2* r.p1.y;

		double det = a1*b2 - a2*b1;
		if(det == 0){
			// Lines are parallel
			return null;
		}
		else {
			result.x = (b2*c1 - b1*c2)/det;
			result.y = (a1*c2 - a2*c1)/det;
		}   
		return result;
	}*/

	/**
	 * If the closest point to this sgRay on the input sgRay lies
	 * beyond the bounds of that input sgRay, this returns closest point
	 * to the input Rays bound; 	
	 * @param r
	 * @return
	 */
	public SGVec_3d closestPointToSegment3D(sgRay r) {
		SGVec_3d closestToThis =  r.closestPointToRay3DStrict(this); 
		return this.closestPointTo(closestToThis);
	}

	/*public SGVec_3d closestPointToSegment3DStrict(sgRay r) {

	}*/

	/**
	 * returns the point on this ray which is closest to the input ray
	 * @param r
	 * @return
	 */

	public SGVec_3d closestPointToRay3D(sgRay r) {
		SGVec_3d result = null;
		
		workingVector.set(p2);
		SGVec_3d   u =  workingVector.sub(this.p1);
		workingVector.set(r.p2);
		SGVec_3d   v =  workingVector.sub(r.p1);
		workingVector.set(this.p1);
		SGVec_3d   w =  workingVector.sub(r.p1);
		double    a = u.dot(u);         // always >= 0
		double    b = u.dot(v);
		double    c = v.dot(v);         // always >= 0
		double    d = u.dot(w);
		double    e = v.dot(w);
		double    D = a*c - b*b;        // always >= 0
		double    sc; //tc

		// compute the line parameters of the two closest points
		if (D < Double.MIN_VALUE) {          // the lines are almost parallel
			sc = 0.0;
			//tc = (b>c ? d/b : e/c);    // use the largest denominator
		}
		else {
			sc = (b*e - c*d) / D;
			//tc = (a*e - b*d) / D;
		}

		result =  this.getRayScaledBy(sc).p2;
		return result;
	}

	public SGVec_3d closestPointToRay3DStrict(sgRay r) {
		SGVec_3d result = null;

		workingVector.set(p2);
		SGVec_3d   u =  workingVector.sub(this.p1);
		workingVector.set(r.p2);
		SGVec_3d   v =  workingVector.sub(r.p1);
		workingVector.set(this.p1);
		SGVec_3d   w =  workingVector.sub(r.p1);
		double    a = u.dot(u);         // always >= 0
		double    b = u.dot(v);
		double    c = v.dot(v);         // always >= 0
		double    d = u.dot(w);
		double    e = v.dot(w);
		double    D = a*c - b*b;        // always >= 0
		double    sc; //tc

		// compute the line parameters of the two closest points
		if (D < Double.MIN_VALUE) {          // the lines are almost parallel
			sc = 0.0;
			//tc = (b>c ? d/b : e/c);    // use the largest denominator
		}
		else {
			sc = (b*e - c*d) / D;
			//tc = (a*e - b*d) / D;
		}

		if(sc < 0 ) result = this.p1;
		else if (sc > 1) result = this.p2;
		else result =  this.getRayScaledBy(sc).p2;	

		return result;
	}

	/**
	 * returns the point on this ray which is closest to 
	 * the input sgRay. If that point lies outside of the bounds
	 * of this ray, returns null. 
	 * @param r
	 * @return
	 */
	public SGVec_3d closestPointToRay3DBounded(sgRay r) {
		SGVec_3d result = null;

		workingVector.set(p2);
		SGVec_3d   u =  workingVector.sub(this.p1);
		workingVector.set(r.p2);
		SGVec_3d   v =  workingVector.sub(r.p1);
		workingVector.set(this.p1);
		SGVec_3d   w =  workingVector.sub(r.p1);
		double    a = u.dot(u);         // always >= 0
		double    b = u.dot(v);
		double    c = v.dot(v);         // always >= 0
		double    d = u.dot(w);
		double    e = v.dot(w);
		double    D = a*c - b*b;        // always >= 0
		double    sc; //tc

		// compute the line parameters of the two closest points
		if (D < Double.MIN_VALUE) {          // the lines are almost parallel
			sc = 0.0;
			//tc = (b>c ? d/b : e/c);    // use the largest denominator
		}
		else {
			sc = (b*e - c*d) / D;
			//tc = (a*e - b*d) / D;
		}

		if(sc < 0 ) result = null;
		else if (sc > 1) result = null;
		else result =  this.getRayScaledBy(sc).p2;	

		return result;
	}

	//returns a ray perpendicular to this ray on the XY plane;
	public sgRay getPerpendicular2D() {
		SGVec_3d heading = this.heading(); 
		workingVector.set(heading.x-1d, heading.x, 0d);
		SGVec_3d perpHeading = workingVector;
		return new sgRay(this.p1,  workingVector.add(this.p1));
	}

	public SGVec_3d intercepts2DStrict(sgRay r) { 
		//will also return null if the intersection does not occur on the 
		//line segment specified by the ray.
		SGVec_3d result =  p1.copy();

		//boolean over = false;
		double a1 = p2.y - p1.y;
		double b1 = p1.x - p2.x;
		double c1 = a1*p1.x + b1*p1.y;

		double a2 = r.p2.y - r.p1.y;
		double b2 = r.p1.x - r.p2.y;
		double c2 = a2* + b2* r.p1.y;

		double det = a1*b2 - a2*b1;
		if(det == 0){
			// Lines are parallel
			return null;
		}
		else {
			result.setX_((b2*c1 - b1*c2)/det);
			result.setY_((a1*c2 - a2*c1)/det);

		}   

		double position = result.dot(this.heading()); 
		if (position > 1 || position < 0) return null;

		return result;
	}

	/**
	 * Given two planes specified by a1,a2,a3 and b1,b2,b3 returns a
	 * ray representing the line along which the two planes intersect
	 * 
	 * @param a1 the first vertex of a triangle on the first plane
	 * @param a2 the second vertex of a triangle on the first plane
	 * @param a3 the third vertex od a triangle on the first plane
	 * @param b1 the first vertex of a triangle on the second plane
	 * @param b2 the second vertex of a triangle on the second plane
	 * @param b3 the third vertex od a triangle on the second plane
	 * @return a sgRay along the line of intersection of these two planes, or null if inputs are coplanar
	 */
	public static sgRay planePlaneIntersect(SGVec_3d a1, SGVec_3d  a2, SGVec_3d  a3, SGVec_3d  b1, SGVec_3d  b2, SGVec_3d  b3) {
		sgRay a1a2 = new sgRay(a1,a2);
		sgRay a1a3 = new sgRay(a1,a3); 
		sgRay a2a3 = new sgRay(a2,a3);

		SGVec_3d interceptsa1a2 =  a1a2.intersectsPlane(b1, b2, b3);
		SGVec_3d interceptsa1a3 =  a1a3.intersectsPlane(b1, b2, b3);
		SGVec_3d interceptsa2a3 =  a2a3.intersectsPlane(b1, b2, b3);

		SGVec_3d[] notNullCandidates = {interceptsa1a2, interceptsa1a3, interceptsa2a3};
		SGVec_3d notNull1 = null;  
		SGVec_3d notNull2 = null; 

		for(int i=0; i<notNullCandidates.length; i++) {
			if(notNullCandidates[i] != null) {
				if(notNull1 == null) 
					notNull1 =  notNullCandidates[i]; 
				else {
					notNull2 =  notNullCandidates[i];
					break;
				}
			}
		}		
		if(notNull1 != null && notNull2 != null) 
			return new sgRay(notNull1, notNull2);
		else 
			return null;
	}

	/**
	 * @param ta the first vertex of a triangle on the plane
	 * @param tb the second vertex of a triangle on the plane 
	 * @param tc the third vertex of a triangle on the plane
	 * @return the point where this ray intersects the plane specified by the triangle ta,tb,tc. 
	 */
	public SGVec_3d intersectsPlane(SGVec_3d  ta, SGVec_3d tb, SGVec_3d tc) {
		double[] uvw = new double[3]; 
		return intersectsPlane(ta, tb, tc, uvw);
	}

	
	SGVec_3d tta, ttb, ttc;
	public SGVec_3d intersectsPlane(SGVec_3d ta, SGVec_3d tb, SGVec_3d tc, double[] uvw) {
		if(tta == null) {
			tta =  ta.copy(); ttb =  tb.copy(); ttc =  tc.copy(); 
		} else {
			tta.set(ta); ttb.set(tb); ttc.set(tc); 
		}
		tta.sub(p1); 
		ttb.sub(p1); 
		ttc.sub(p1);
		
		SGVec_3d result =  planeIntersectTest(tta, ttb, ttc, uvw).copy();
		return  result.add(this.p1);
	}
	
	/**
	 * @param ta the first vertex of a triangle on the plane
	 * @param tb the second vertex of a triangle on the plane 
	 * @param tc the third vertex of a triangle on the plane
	 * @param result the variable in which to hold the result
	 */
	public void intersectsPlane(SGVec_3d ta, SGVec_3d tb, SGVec_3d tc, SGVec_3d result) {
		double[] uvw = new double[3]; 
		result.set(intersectsPlane(ta, tb, tc, uvw));
	}
	
	
	/**
	 * Similar to intersectsPlane, but returns false if intersection does not occur on the triangle strictly defined by ta, tb, and tc
	 * @param ta the first vertex of a triangle on the plane
	 * @param tb the second vertex of a triangle on the plane 
	 * @param tc the third vertex of a triangle on the plane
	 * @param result the variable in which to hold the result
	 */
	public boolean intersectsTriangle(SGVec_3d ta, SGVec_3d tb, SGVec_3d tc, SGVec_3d result) {
		double[] uvw = new double[3]; 
		result.set(intersectsPlane(ta, tb, tc, uvw));
		if(Double.isNaN(uvw[0]) || Double.isNaN(uvw[1]) || Double.isNaN(uvw[2]) || uvw[0] < 0 || uvw[1] < 0 || uvw[2] < 0) 
			return false; 
		else 
			return true;
	}

	SGVec_3d I,u,v,n,dir,w0; 
	boolean inUse = false;
	
	private SGVec_3d planeIntersectTest(SGVec_3d ta, SGVec_3d tb, SGVec_3d tc, double[] uvw) {
		
		if(u== null) {
			u =  tb.copy();
			v =  tc.copy();
			dir = this.heading();
			w0 =  p1.copy(); w0.set(0,0,0);
			I = p1.copy();
		} else {
			u.set(tb); 
			v.set(tc); 
			n.set(0,0,0);
			dir.set(this.heading()); 
			w0.set(0,0,0);
		}
		//SGVec_3d w = new SGVec_3d();
		double  r, a, b;
		u.sub(ta);
		v.sub(ta);
		
		n =  u.crossCopy(v);

		w0.sub(ta);
		a = -(n.dot(w0));
		b = n.dot(dir);
		r = a / b;
		I.set(0,0,0);
		I.set(dir); 
		I.mult(r);
		//double[] barycentric = new double[3]; 
		barycentric(ta, tb, tc, I, uvw);

		
		return I.copy();		
	}

	
	/* Find where this ray intersects a sphere
	 * @param SGVec_3d the center of the sphere to test against.
	 * @param radius radius of the sphere
	 * @param S1 reference to variable in which the first intersection will be placed
	 * @param S2 reference to variable in which the second intersection will be placed
	 * @return number of intersections found;
	 */
	public int intersectsSphere(SGVec_3d sphereCenter, double radius, SGVec_3d S1, SGVec_3d S2) {
		SGVec_3d tp1 =  p1.subCopy(sphereCenter);
		SGVec_3d tp2 =  p2.subCopy(sphereCenter);
		int result = intersectsSphere(tp1, tp2, radius, S1, S2);
		S1.add(sphereCenter); S2.add(sphereCenter);
		return result;
	}
	/* Find where this ray intersects a sphere
	 * @param radius radius of the sphere
	 * @param S1 reference to variable in which the first intersection will be placed
	 * @param S2 reference to variable in which the second intersection will be placed
	 * @return number of intersections found;
	 */
	public int intersectsSphere(SGVec_3d rp1, SGVec_3d rp2, double radius, SGVec_3d S1, SGVec_3d S2) {
		SGVec_3d direction =  rp2.subCopy(rp1);
		SGVec_3d e =  direction.copy();   // e=ray.dir
		e.normalize();                            // e=g/|g|
		SGVec_3d h =  p1.copy();
		h.set(0d,0d,0d);
		h =  h.sub(rp1);  // h=r.o-c.M
		double lf = e.dot(h);                      // lf=e.h
		double s = Math.pow(radius, 2)-h.dot(h)+Math.pow(lf, 2);   // s=r^2-h^2+lf^2
		if (s < 0.0) return 0;                    // no intersection points ?
		s = Math.sqrt(s);                              // s=sqrt(r^2-h^2+lf^2)

		int result = 0;
		if (lf < s) {                               // S1 behind A ?
			if (lf+s >= 0) {                         // S2 before A ?}
				s = -s;                               // swap S1 <-> S2}
				result = 1;                           // one intersection point
			} 
		}else result = 2;                          // 2 intersection points

		S1.set(e.multCopy(lf-s));  
		S1.add(rp1); // S1=A+e*(lf-s)
		S2.set(e.multCopy(lf+s));  
		S2.add(rp1); // S2=A+e*(lf+s)

		// only for testing

		return result;
	}

	SGVec_3d m, at, bt, ct, pt;;
	SGVec_3d bc, ca, ac; 
	public void barycentric(SGVec_3d a, SGVec_3d b, SGVec_3d c, SGVec_3d p, double[] uvw) {
		if(m == null) {
			//m=a.copy();
			//m.set(0d,0d,0d);
			bc =  b.copy();
			ca =  c.copy();
			at = new SGVec_3d(a);
			bt = new SGVec_3d(b);
			ct = new SGVec_3d(c);
			pt = new SGVec_3d(p);
		} else {
			bc.set(b);
			ca.set(a);
			at.set(a);
			bt.set(b);
			ct.set(c);
			pt.set(p);
		}
		
		m = new SGVec_3d(((SGVec_3d)bc.sub(ct)).crossCopy((SGVec_3d)ca.subCopy(at)));

		double nu;
		double nv;
		double ood;

		double x = Math.abs(m.x);
		double y = Math.abs(m.y);
		double z = Math.abs(m.z);

		if (x >= y && x >= z) {
			nu = triArea2D(pt.y, pt.z, bt.y, bt.z, ct.y, ct.z);
			nv = triArea2D(pt.y, pt.z, ct.y, ct.z, at.y, at.z);
			ood = 1.0f / m.x;
		}
		else if (y >= x && y >= z) {
			nu = triArea2D(pt.x, pt.z, bt.x, bt.z, ct.x, ct.z);
			nv = triArea2D(pt.x, pt.z, ct.x, ct.z, at.x, at.z);
			ood = 1.0f / -m.y;
		}
		else {
			nu = triArea2D(pt.x, pt.y, bt.x, bt.y, ct.x, ct.y);
			nv = triArea2D(pt.x, pt.y, ct.x, ct.y, at.x, at.y);
			ood = 1.0f / m.z;
		}
		uvw[0] = nu * ood;
		uvw[1] = nv * ood;
		uvw[2] = 1.0f - uvw[0] - uvw[1];
	}

	@Override
	public String toString() {
		String result = "sgRay " + System.identityHashCode(this) + "\n"+this.p1+"\n             v \n" + this.p2+"\n ------------";
		return result;		
	}

	public static double triArea2D(double x1, double y1, double x2, double y2, double x3, double y3) {
		return (x1 - x2) * (y2 - y3) - (x2 - x3) * (y1 - y2);   
	}


	public void p1(SGVec_3d in) {
		this.p1 =  in.copy();
	}

	public void p2(SGVec_3d in) {
		this.p2 =  in.copy();
	}
	


	public double lerp(double a, double b, double t) {
		return (1-t)*a + t*b;
	}

	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		result.setJSONArray("p1", p1.toJSONArray());
		result.setJSONArray("p2", p2.toJSONArray());
		return result;
	}


}

