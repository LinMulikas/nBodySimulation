package algs4; /******************************************************************************
 *  Compilation:  javac algs4.CollisionSystem.java
 *  Execution:    java algs4.CollisionSystem n               (n random particles)
 *                java algs4.CollisionSystem < input.txt     (from a file)
 *  Dependencies: StdDraw.java algs4.Particle.java algs4.MinPQ.java
 *  Data files:   https://algs4.cs.princeton.edu/61event/diffusion.txt
 *                https://algs4.cs.princeton.edu/61event/diffusion2.txt
 *                https://algs4.cs.princeton.edu/61event/diffusion3.txt
 *                https://algs4.cs.princeton.edu/61event/brownian.txt
 *                https://algs4.cs.princeton.edu/61event/brownian2.txt
 *                https://algs4.cs.princeton.edu/61event/billiards5.txt
 *                https://algs4.cs.princeton.edu/61event/pendulum.txt
 *
 *  Creates n random particles and simulates their motion according
 *  to the laws of elastic collisions.
 *
 ******************************************************************************/

import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.StdIn;

import java.awt.Color;

/**
 * The {@code algs4.CollisionSystem} class represents a collection of particles
 * moving in the unit box, according to the laws of elastic collision.
 * This event-based simulation relies on a priority queue.
 * <p>
 * For additional documentation,
 * see <a href="https://algs4.cs.princeton.edu/61event">Section 6.1</a> of
 * <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 */
public class CollisionSystem{
	private static final double HZ = 0.5;    // number of redraw events per clock tick
	public final double G = 6.67e-11;
	private MinPQ<Event> pq;          // the priority queue
	private double t = 0.0;           // simulation clock time
	private Particle[] particles;     // the array of particles
	
	/**
	 * Initializes a system with the specified collection of particles.
	 * The individual particles will be mutated during the simulation.
	 *
	 * @param particles the array of particles
	 */
	public CollisionSystem(Particle[] particles){
		this.particles = particles.clone();   // defensive copy
	}
	
	// updates priority queue with all new events for particle a
	private void predict(Particle a, double limit){
		if(a == null) return;
		
		// particle-particle collisions
		
		for(int i = 0; i < particles.length; i++){
			double dt = a.timeToHit(particles[i]);
			if(t + dt <= limit) pq.insert(new Event(t + dt, a, particles[i]));
		}
		
		// particle-wall collisions
		double dtX = a.timeToHitVerticalWall();
		double dtY = a.timeToHitHorizontalWall();
		if(t + dtX <= limit) pq.insert(new Event(t + dtX, a, null));
		if(t + dtY <= limit) pq.insert(new Event(t + dtY, null, a));
	}
	
	private void checkPartCollision(Particle a, Particle[] particles, double tick){
		for(Particle p : particles){
			double dt = a.timeToHit(p);
			if(dt <= tick){
				pq.insert(new Event(t + dt, a, p));
			}
		}
	}
	
	private void checkWallCollision(Particle a){
		double dt_x = a.timeToHitVerticalWall();
		double dt_y = a.timeToHitHorizontalWall();
		pq.insert(new Event(t + dt_x, a, null));
		pq.insert(new Event(t + dt_y, null, a));
	}
	
	/**
	 * 检测当前质点一段时间后的状态，并把可能发生的事件加入pq里
	 *
	 * @param a
	 */
	private void predictByTick(Particle a){
		if(a == null) return;
		double tick = 1.0 / HZ;
		// particle-particle collisions
		checkPartCollision(a, particles, tick);
		// particle-wall collisions
		checkWallCollision(a);
	}
	
	// redraw all particles
	private void redraw(double limit){
		StdDraw.clear();
		for(int i = 0; i < particles.length; i++){
			particles[i].draw();
		}
		StdDraw.show();
		StdDraw.pause(20);
		
		if(t < limit){
			pq.insert(new Event(t + 1.0 / HZ, null, null));
		}
		
	}
	
	private void reDraw(){
		StdDraw.clear();
		
		for(Particle p : particles){
			p.draw();
		}
		StdDraw.show();
		StdDraw.pause(20);
		
		if(pq.peer().time > t + 2){
			pq.insert(new Event(t + 1.0 / HZ, null, null));
		}
	}
	
	public void simulateUniverse(){
		pq = new MinPQ<Event>();
		
		for(Particle p : particles){
			predictByTick(p);
		}
		
		pq.insert(new Event(0, null, null));
		
		while(!pq.isEmpty()){
			Event event = pq.delMin();
			if(event.isValid()){
				t = event.time;
				eventHandle(event);
				predictByTick(event.a);
				predictByTick(event.b);
			}
		}
	}
	
	public void moveEvent(Particle[] particles){
		for(Particle p : particles){
			pq.insert(new Event(t + 2, p, null, false));
			pq.insert(new Event(t + 4, p, null, false));
			pq.insert(new Event(t + 6, p, null, false));
		}
	}
	
	/**
	 * Simulates the system of particles for the specified amount of time.
	 *
	 * @param limit the amount of time
	 */
	public void simulate(double limit){
		
		// initialize PQ with collision events and redraw event
		pq = new MinPQ<Event>();
		
		for(int i = 0; i < particles.length; i++){
			predict(particles[i], limit);
		}
		
		pq.insert(new Event(0, null, null));        // redraw event
		
		// the main event-driven simulation loop
		while(!pq.isEmpty()){
			
			// get impending event, discard if invalidated
			Event e = pq.delMin();
			if(!e.isValid()) continue;
			Particle a = e.a;
			Particle b = e.b;
			
			// physical collision, so update positions, and then simulation clock
			for(int i = 0; i < particles.length; i++){
				particles[i].move(e.time - t, particles, G);
			}
			
			t = e.time;
			
			// process event
			if(a != null && b != null) a.bounceOff(b);              // particle-particle collision
			else if(a != null && b == null) a.bounceOffVerticalWall();   // particle-wall collision
			else if(a == null && b != null) b.bounceOffHorizontalWall(); // particle-wall collision
			else if(a == null && b == null) redraw(limit);               // redraw event
			
			// update the priority queue with new collisions involving a or b
			predict(a, limit);
			predict(b, limit);
		}
	}
	
	public void eventHandle(Event e){
		Particle a = e.a;
		Particle b = e.b;
		
		if(a != null && b != null) a.bounceOff(b);              // particle-particle collision
		else if(a != null) a.bounceOffVerticalWall();   // particle-wall collision
		else if(b != null) b.bounceOffHorizontalWall(); // particle-wall collision
		else reDraw();               // redraw event
		
		moveAll(particles);
		
		predictByTick(a);
		predictByTick(b);
	}
	
	private void moveAll(Particle[] particles){
		for(Particle p : particles){
			p.move(2, particles, G);
		}
	}
	
	/***************************************************************************
	 *  An event during a particle collision simulation. Each event contains
	 *  the time at which it will occur (assuming no supervening actions)
	 *  and the particles a and b involved.
	 *
	 *    -  a and b both null:      redraw event
	 *    -  a null, b not null:     collision with vertical wall
	 *    -  a not null, b null:     collision with horizontal wall
	 *    -  a and b both not null:  binary collision between a and b
	 ***************************************************************************/
	private static class Event implements Comparable<Event>{
		private final double time;         // time that event is scheduled to occur
		private final Particle a, b;       // particles involved in event, possibly null
		private final int countA, countB;  // collision counts at event creation
		public boolean collission = true;
		
		// create a new event to occur at time t involving a and b
		public Event(double t, Particle a, Particle b){
			this.time = t;
			this.a = a;
			this.b = b;
			if(a != null) countA = a.count();
			else countA = -1;
			if(b != null) countB = b.count();
			else countB = -1;
		}
		
		public Event(double t, Particle a, Particle b, boolean collision){
			this.time = t;
			this.a = a;
			this.b = b;
			if(a != null) countA = a.count();
			else countA = -1;
			if(b != null) countB = b.count();
			else countB = -1;
			this.collission = collision;
		}
		
		
		// compare times when two events will occur
		public int compareTo(Event that){
			return Double.compare(this.time, that.time);
		}
		
		// has any collision occurred between when event was created and now?
		public boolean isValid(){
			if(a != null && a.count() != countA) return false;
			if(b != null && b.count() != countB) return false;
			return true;
		}
		
	}
	
	
	/**
	 * Unit tests the {@code algs4.CollisionSystem} data type.
	 * Reads in the particle collision system from a standard input
	 * (or generates {@code N} random particles if a command-line integer
	 * is specified); simulates the system.
	 *
	 * @param args the command-line arguments
	 */
	public static void main(String[] args){
		
		StdDraw.setCanvasSize(600, 600);
		
		// enable double buffering
		StdDraw.enableDoubleBuffering();
		
		// the array of particles
		Particle[] particles;
		
		// create n random particles
		if(args.length == 1){
			int n = Integer.parseInt(args[0]);
			particles = new Particle[n];
			for(int i = 0; i < n; i++)
				particles[i] = new Particle();
		}
		// or read from standard input
		else{
			int n = StdIn.readInt();
			particles = new Particle[n];
			for(int i = 0; i < n; i++){
				double rx = StdIn.readDouble();
				double ry = StdIn.readDouble();
				double vx = StdIn.readDouble();
				double vy = StdIn.readDouble();
				double radius = StdIn.readDouble();
				double mass = StdIn.readDouble();
				int r = StdIn.readInt();
				int g = StdIn.readInt();
				int b = StdIn.readInt();
				Color color = new Color(r, g, b);
				particles[i] = new Particle(rx, ry, vx, vy, radius, mass, color);
			}
		}
		
		// create collision system and simulate
		CollisionSystem system = new CollisionSystem(particles);
		system.simulate(1000);
	}
	
	
}
