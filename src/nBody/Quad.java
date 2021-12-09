package nBody;//A quadrant object that can self-subdivide. Important for creating a Barnes-Hut tree, since it holds quadrants.

import static algs4.Quad.testContain;

public class Quad {
  
  private double xmid, ymid, length;
  //Create a square quadrant with a given length and midpoints (xmid,ymid)
  public Quad(double xmid, double ymid, double length) {
    this.xmid=xmid;
    this.ymid=ymid; 
    this.length=length;
  }
  
  //How long is this quadrant?
  public double length() {
    return length;
  }
  
  //Check if the current quadrant contains a point
  public boolean contains(double xmid, double ymid) {
      return testContain(xmid, ymid, this.xmid, this.length, this.ymid);
  }
  //Create subdivisions of the current quadrant
  
  // Subdivision: Northwest quadrant
  public Quad NW() {
    Quad newquad = new Quad(this.xmid-this.length/4.0, this.ymid+this.length/4.0,this.length/2.0);
    return newquad;
  }
  
  // Subdivision: Northeast quadrant
  public Quad NE() {
    Quad newquad = new Quad(this.xmid+this.length/4.0, this.ymid+this.length/4.0,this.length/2.0);
    return newquad;
  }
  
  // Subdivision: Southwest quadrant
  public Quad SW() {
    Quad newquad = new Quad(this.xmid-this.length/4.0, this.ymid-this.length/4.0,this.length/2.0);
    return newquad;
  }
  
  // Subdivision: Southeast quadrant
  public Quad SE() {
    Quad newquad = new Quad(this.xmid+this.length/4.0, this.ymid-this.length/4.0,this.length/2.0);
    return newquad;
  }
 
  
}
