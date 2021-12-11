public class Quad{

    private double xmid, ymid, length;

    public double getXmid() {
        return xmid;
    }

    public double getYmid() {
        return ymid;
    }

    public double getLength() {
        return length;
    }

    //Create a square quadrant with a given length and midpoints (xmid,ymid)
    public Quad(double xmid, double ymid, double length){
        this.xmid = xmid;
        this.ymid = ymid;
        this.length = length;
    }

    public Quad(double width){
        this.xmid = 0.5 * width;
        this.ymid = 0.5 * width;
        this.length = width;
    }

    //How long is this quadrant?
    public double length(){
        return length;
    }

    //Check if the current quadrant contains a point
    public boolean contains(double xmid, double ymid){
        return testContain(xmid, ymid, this.xmid, this.length, this.ymid);
    }

    public static boolean testContain(double xmid, double ymid, double xmid2, double length, double ymid2){
        if(xmid <= xmid2 + length / 2.0 && xmid >= xmid2 - length / 2.0 && ymid <= ymid2 + length / 2.0 && ymid >= ymid2 - length / 2.0){
            return true;
        } else{
            return false;
        }
    }
    //Create subdivisions of the current quadrant

    // Subdivision: Northwest quadrant
    public Quad NW(){
        Quad newquad = new Quad(this.xmid - this.length / 4.0, this.ymid + this.length / 4.0, this.length / 2.0);
        return newquad;
    }

    // Subdivision: Northeast quadrant
    public Quad NE(){
        Quad newquad = new Quad(this.xmid + this.length / 4.0, this.ymid + this.length / 4.0, this.length / 2.0);
        return newquad;
    }

    // Subdivision: Southwest quadrant
    public Quad SW(){
        Quad newquad = new Quad(this.xmid - this.length / 4.0, this.ymid - this.length / 4.0, this.length / 2.0);
        return newquad;
    }

    // Subdivision: Southeast quadrant
    public Quad SE(){
        Quad newquad = new Quad(this.xmid + this.length / 4.0, this.ymid - this.length / 4.0, this.length / 2.0);
        return newquad;
    }


}
