import java.awt.*;

public class rigidSphere{
	final double radius;
	final double mass;
	final Color color;
	
	Vector position;
	Vector force;
	Vector velocity;
	Vector acceleration;
	
	public rigidSphere(double size, double mass, Color color, Vector position){
		this.radius = size;
		this.mass = mass;
		this.color = color;
		this.position = position;
	}
	
	public void setVelocity(Vector v){
		this.velocity = v;
	}
	
	public void setForce(Vector force){
		this.force = force;
	}
	
	public void setAcceleration(Vector acceleration){
		this.acceleration = acceleration;
	}
	
	public void setP(long x, long y){
		this.position.x = x;
		this.position.y = y;
	}
	
	
	
	public void upgrade(double tick){
		this.acceleration.x = this.force.x/mass;
		this.acceleration.y = this.force.y/mass;
		
	}
}
