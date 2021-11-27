package algs4; /******************************************************************************
 *  Compilation:  javac algs4.Particle.java
 *  Execution:    none
 *  Dependencies: StdDraw.java
 *
 *  A particle moving in the unit box with a given position, velocity,
 *  radius, and mass.
 *
 ******************************************************************************/

import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.StdRandom;
import nBody.Body;

import java.awt.Color;

/**
 * The {@code algs4.Particle} class represents a particle moving in the unit box,
 * with a given position, velocity, radius, and mass. Methods are provided
 * for moving the particle and for predicting and resolvling elastic
 * collisions with vertical walls, horizontal walls, and other particles.
 * This data type is mutable because the position and velocity change.
 * <p>
 * For additional documentation,
 * see <a href="https://algs4.cs.princeton.edu/61event">Section 6.1</a> of
 * <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 */
public class Particle {
    private static final double INFINITY = Double.POSITIVE_INFINITY;

    private double nextEventTime;
	public final double G = 6.67e-11;
    private double rx, ry;        // position
    //public double rx_, ry_;
    private double vx, vy;        // velocity
    //public double vx_, vy_;
    private double fx, fy;
    private double ax, ay;
    private int count;            // number of collisions so far
    private final double radius;  // radius
    private final double mass;    // mass
    private final Color color;    // color


    /**
     * Initializes a particle with the specified position, velocity, radius, mass, and color.
     *
     * @param rx     <em>x</em>-coordinate of position
     * @param ry     <em>y</em>-coordinate of position
     * @param vx     <em>x</em>-coordinate of velocity
     * @param vy     <em>y</em>-coordinate of velocity
     * @param radius the radius
     * @param mass   the mass
     * @param color  the color
     */
    public Particle(double rx, double ry, double vx, double vy, double radius, double mass, Color color) {
        this.vx = vx;
        this.vy = vy;
        this.rx = rx;
        this.ry = ry;
        this.radius = radius;
        this.mass = mass;
        this.color = color;
    }

    /**
     * Initializes a particle with a random position and velocity.
     * The position is uniform in the unit box; the velocity in
     * either direciton is chosen uniformly at random.
     */
    public Particle() {
		radius = 0.015;
        rx = StdRandom.uniform(0.0+radius, 1.0-radius);
        ry = StdRandom.uniform(0.0+radius, 1.0-radius);
        vx = StdRandom.uniform(-0.005, 0.005);
        vy = StdRandom.uniform(-0.005, 0.005);
        mass = 10000000;
        color = Color.BLACK;
    }

//    public double squareDistanceTo(Particle that) {
//        double dx = this.rx - that.rx;
//        double dy = this.ry - that.ry;
//        return dx * dx + dy * dy;
//    }

    public double distanceTo(Particle that) {
        double dx = this.rx - that.rx;
        double dy = this.ry - that.ry;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public void addForce(Particle particle) {
        double r = distanceTo(particle);
        double netForce = (G * this.mass * particle.mass) / (r * r);
        this.fx += netForce * (particle.rx - this.rx) / (this.distanceTo(particle));
        this.fy += netForce * (particle.ry - this.ry) / (this.distanceTo(particle));
        //公式错啦 原本的 distanceTo方法返回的是距离的平方.
        //修改了distanceTo方法避免产生干扰 把另一个方法名改成SquareDistanceTo

    }

    public void calNetForce(Particle[] particles) {
        this.fx = this.calNetForce_x(particles);
        this.fy = this.calNetForce_y(particles);
    }

    private double calNetForce_x(Particle[] particles) {
        double netF_x = 0;
        for (Particle p : particles) {
            if (p != this) {
                if (this.distanceTo(p) > (this.radius + p.radius)) {
                    netF_x += this.getNetForceTo(p) * (p.rx - this.rx) / (this.distanceTo(p));
                }
            }
        }
        return netF_x;
    }

    private double calNetForce_y(Particle[] particles) {
        double netF_y = 0;
        for (Particle p : particles) {
            if (p != this) {
                if (this.distanceTo(p) > (this.radius + p.radius)) {
                    netF_y += this.getNetForceTo(p) * (p.ry - this.ry) / (this.distanceTo(p));
                }
            }
        }
        return netF_y;
    }

    public double getNetForceTo(Particle particle) {
        double r = distanceTo(particle);
        return G * this.mass * particle.mass / (r * r);
    }


    /**
     * Moves this particle in a straight line (based on its velocity)
     * for the specified amount of time.
     *
     * @param dt the amount of time
     */
    public void move(double dt) {
    	//应该先用加速度位移公式再改变速度的量
//      this.vx += this.ax * dt;
//      this.vy += this.ay * dt;
        rx += vx * dt + 0.5 * this.ax * dt * dt;
        ry += vy * dt + 0.5 * this.ay * dt * dt;

		this.vx += this.ax * dt;
		this.vy += this.ay * dt;

    }

    public void move(double dt, Particle[] particles) {
//        this.vx += this.ax * dt;
//        this.vy += this.ay * dt;
//
//        rx += vx * dt;
//        ry += vy * dt;
		rx += vx * dt + 0.5 * this.ax * dt * dt;
		ry += vy * dt + 0.5 * this.ay * dt * dt;

		this.vx += this.ax * dt;
		this.vy += this.ay * dt;

        this.calNetForce(particles);
        this.calAcceleration();
    }

    private class nextAction {
        double totalTime;
        double dt;
        boolean collision = false;
    }

//    public void tryMoveDirectly(double dt, double time) {
//
//    	//同样的问题：应该先算x后算v
////        this.vx_ = this.vx + this.ax * dt;
////        this.vy_ = this.vy + this.ay * dt;
//        rx_ += vx * dt + 0.5 * this.ax * dt * dt;
//        ry_ += vy * dt + 0.5 * this.ay * dt * dt;
//
//		this.vx_ = this.vx + this.ax * dt;
//		this.vy_ = this.vy + this.ay * dt;
//
//        this.nextEventTime = time;
//    }

    public void moveDirectly(double dt) {
        if (dt < 0) {
            return;
        }
//        this.vx += this.ax * dt;
//        this.vy += this.ay * dt;
        rx += vx * dt + 0.5 * this.ax * dt * dt;
        ry += vy * dt + 0.5 * this.ay * dt * dt;

		this.vx += this.ax * dt;
		this.vy += this.ay * dt;
    }

    public void calAcceleration() {
        this.ax = this.fx / mass;
        this.ay = this.fy / mass;
    }

    /**
     * Draws this particle to standard draw.
     */
    public void draw() {
        StdDraw.setPenColor(color);
        StdDraw.filledCircle(rx, ry, radius);
    }

    /**
     * Returns the number of collisions involving this particle with
     * vertical walls, horizontal walls, or other particles.
     * This is equal to the number of calls to {@link #bounceOff},
     * {@link #bounceOffVerticalWall}, and
     * {@link #bounceOffHorizontalWall}.
     *
     * @return the number of collisions involving this particle with
     * vertical walls, horizontal walls, or other particles
     */
    public int count() {
        return count;
    }

    /**
     * Returns the amount of time for this particle to collide with the specified
     * particle, assuming no interening collisions.
     *
     * @param that the other particle
     * @return the amount of time for this particle to collide with the specified
     * particle, assuming no interening collisions;
     * {@code Double.POSITIVE_INFINITY} if the particles will not collide
     */
    public double timeToHit(Particle that) {
        if (this == that) return INFINITY;
        double dx = that.rx - this.rx;
        double dy = that.ry - this.ry;
        double dv_x = that.vx - this.vx;
        double dv_y = that.vy - this.vy;
        double dvdr = dx * dv_x + dy * dv_y;
        if (dvdr > 0) return INFINITY;
        double dvdv = dv_x * dv_x + dv_y * dv_y;
        if (dvdv == 0) return INFINITY;
        double drdr = dx * dx + dy * dy;
        double sigma = this.radius + that.radius;
        double d = (dvdr * dvdr) - dvdv * (drdr - sigma * sigma);
        // if (drdr < sigma*sigma) StdOut.println("overlapping particles");
        if (d < 0) return INFINITY;
        return -(dvdr + Math.sqrt(d)) / dvdv;
    }

    /**
     * Returns the amount of time for this particle to collide with a vertical
     * wall, assuming no interening collisions.
     *
     * @return the amount of time for this particle to collide with a vertical wall,
     * assuming no interening collisions;
     * {@code Double.POSITIVE_INFINITY} if the particle will not collide
     * with a vertical wall
     */
    public double timeToHitVerticalWall() {
        if (vx > 0) return (1.0 - rx - radius) / vx;
        else if (vx < 0) return (radius - rx) / vx;
        else return INFINITY;
    }

    /**
     * Returns the amount of time for this particle to collide with a horizontal
     * wall, assuming no interening collisions.
     *
     * @return the amount of time for this particle to collide with a horizontal wall,
     * assuming no interening collisions;
     * {@code Double.POSITIVE_INFINITY} if the particle will not collide
     * with a horizontal wall
     */
    public double timeToHitHorizontalWall() {
        if (vy > 0) return (1.0 - ry - radius) / vy;
        else if (vy < 0) return (radius - ry) / vy;
        else return INFINITY;
    }

    /**
     * Updates the velocities of this particle and the specified particle according
     * to the laws of elastic collision. Assumes that the particles are colliding
     * at this instant.
     *
     * @param that the other particle
     */
    public void bounceOff(Particle that) {
        double dx = that.rx - this.rx;
        double dy = that.ry - this.ry;
        double dvx = that.vx - this.vx;
        double dvy = that.vy - this.vy;
        double dvdr = dx * dvx + dy * dvy;             // dv dot dr
        double dist = this.radius + that.radius;   // distance between particle centers at collison

        // magnitude of normal force
        double magnitude = 2 * this.mass * that.mass * dvdr / ((this.mass + that.mass) * dist);

        // normal force, and in x and y directions
        double fx = magnitude * dx / dist;
        double fy = magnitude * dy / dist;

        // update velocities according to normal force
        this.vx += fx / this.mass;
        this.vy += fy / this.mass;
        that.vx -= fx / that.mass;
        that.vy -= fy / that.mass;

        // update collision counts
        this.count++;
        that.count++;
    }

    /**
     * Updates the velocity of this particle upon collision with a vertical
     * wall (by reflecting the velocity in the <em>x</em>-direction).
     * Assumes that the particle is colliding with a vertical wall at this instant.
     */
    public void bounceOffVerticalWall() {
        vx = -vx;
        count++;
    }

    /**
     * Updates the velocity of this particle upon collision with a horizontal
     * wall (by reflecting the velocity in the <em>y</em>-direction).
     * Assumes that the particle is colliding with a horizontal wall at this instant.
     */
    public void bounceOffHorizontalWall() {
        vy = -vy;
        count++;
    }

    /**
     * Returns the kinetic energy of this particle.
     * The kinetic energy is given by the formula 1/2 <em>m</em> <em>v</em><sup>2</sup>,
     * where <em>m</em> is the mass of this particle and <em>v</em> is its velocity.
     *
     * @return the kinetic energy of this particle
     */
    public double kineticEnergy() {
        return 0.5 * mass * (vx * vx + vy * vy);
    }

	public String toString(){
		return "" + rx + ", " + ry + ", " + vx + ", " + vy + ", " + mass;
	}

	//计算两个天体的质心
	public Particle centroid(Particle a, Particle b){
        double m = a.mass + b.mass;
		return new Particle ((a.rx*a.mass+b.rx*b.mass)/m,a.ry*a.mass+b.ry*b.mass/m,a.vx+b.vx,a.ay+b.ay,a.radius,m,a.color);
	}

	//该质点是否在本象限
    public boolean in(Quad q){
        return q.contains(this.rx, this.ry);
    }

    //重置速度
    public void resetForce(){
        fx = 0.0;
        fy = 0.0;
    }


}
