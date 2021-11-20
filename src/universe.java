import java.util.ArrayList;
import javafx.*;

public class universe {
	double G = 6.67e-11;
	double width;
	double height;
	double tick = 0.1;
	boolean ShowGUI = false;

	universe(double x, double y){
		this.width = x;
		this.height = y;
	}

	universe(double x, double y, double g){
		this.width = x;
		this.height = y;
		this.G = g;
	}

	public boolean checkBallCollision(rigidSphere ball){
		return false;
	}

	public boolean checkBoundaryCollision(rigidSphere ball){
		ball.calPosition(this.tick);
		return false;
	}

	ArrayList<rigidSphere> spheres = new ArrayList<>();

	public Vector gravityTo(rigidSphere ball1, rigidSphere ball2){
		double x = ball2.position.x - ball1.position.x;
		double y = ball2.position.y - ball1.position.y;
		double d = Vector.distance(ball1.position, ball2.position);

		double scalaForce = gravity(ball1.mass, ball2.mass, d);

		return new Vector(scalaForce*(x/d), scalaForce*(y/d));
	}

	public void addSphere(rigidSphere ball){
		this.spheres.add(ball);
	}

	public double gravity(double m1, double m2, double distance){
		return (this.G*m1*m2)/(distance*distance);
	}

	public double getG(){
		return G;
	}

	public void setG(double g){
		G = g;
	}

	/**
	 * 计算 v 的变化量
	 *
	 * @param a
	 * @param tick
	 *
	 * @return
	 */
	public Vector dv(Vector a, double tick){
		return new Vector(a.x*tick, a.y*tick);
	}

	public Vector dx(Vector velocity, Vector a, double tick){
		return new Vector(velocity.x*tick + 0.5*a.x*tick*tick, velocity.y*tick + 0.5*a.y*tick*tick);
	}

}
