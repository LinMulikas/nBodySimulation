public class Vector{
	double x;
	double y;
	
	Vector(){
		this.x = 0;
		this.y = 0;
	}
	
	Vector(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public static double distance(Vector v1, Vector v2){
		return Math.sqrt((v1.x - v2.x)*(v1.x - v2.x) + (v1.y - v2.y)*(v1.y - v2.y));
	}
	
	public static double dot(Vector v1, Vector v2){
		return v1.x*v2.x + v1.y*v2.y;
	}
	
	public static Vector change(Vector v1, Vector v2){
		return new Vector(v1.x + v2.x, v1.y + v2.y);
	}
	
	public static Vector fromTo(Vector v1, Vector v2){
		return new Vector(v2.x - v1.x, v2.y - v1.y);
	}
	
	public double magnitude(){
		x = this.x;
		y = this.y;
		return Math.sqrt(x*x + y*y);
	}
	
	public double dot(Vector v){
		return dot(this, v);
	}
	
	public void minus(Vector v){
		this.x -= v.x;
		this.y -= v.y;
	}
	
	public void add(Vector v){
		this.x += v.x;
		this.y += v.y;
	}
	
	

}
