package algs4;

import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.StdRandom;

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
public class Particle{
    private static final double INFINITY = Double.POSITIVE_INFINITY;

    private double rx, ry;        // position
    private double vx, vy;        // velocity
    private double fx, fy;
    private double ax, ay;
    private int count;            // number of collisions so far
    private final double radius;  // radius
    private final double mass;    // mass
    private final Color color;    // color

    public String toString(double width){
        return width * this.rx + " " + width * this.ry + " " + width * this.vx + " " + width * this.vy + "\n";
    }

    public String toString(){
        return this.rx + " " + this.ry + " " + this.vx + " " + this.vy + "\n";
    }

    public Particle centroid(Particle a, Particle b){
        double m = a.mass + b.mass;
        return new Particle((a.rx * a.mass + b.rx * b.mass) / m, (a.ry * a.mass + b.ry * b.mass) / m, 0, 0, a.radius, m, a.color);
    }

    public boolean in(Quad q){
        return q.contains(this.rx, this.ry);
    }

    public double v(){
        return Math.sqrt(this.vx * this.vx + this.vy * this.vy);
    }

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
    public Particle(double rx, double ry, double vx, double vy, double radius, double mass, Color color){
        this.vx = vx;
        this.vy = vy;
        this.rx = rx;
        this.ry = ry;
        this.radius = radius;
        this.mass = mass;
        this.color = color;
    }

    public double distanceTo(Particle particle){
        return Math.sqrt((this.rx - particle.rx) * (this.rx - particle.rx) + (this.ry - particle.ry) * (this.ry - particle.ry));
    }

    public double squareDistanceTo(Particle particle){
        return (this.rx - particle.rx) * (this.rx - particle.rx) + (this.ry - particle.ry) * (this.ry - particle.ry);
    }

    public void addForceTo(Particle particle, double G){
        double r = distanceTo(particle);
        if(this.distanceTo(particle) >= (this.radius + particle.radius)){
            double netForce = G * this.mass * particle.mass / (r * r);
            this.fx += netForce * (particle.rx - this.rx) / (r);
            this.fy += netForce * (particle.ry - this.ry) / (r);
        }
    }


    public void resetForce(){
        this.fx = 0;
        this.fy = 0;
    }

    /**
     * Initializes a particle with a random position and velocity.
     * The position is uniform in the unit box; the velocity in
     * either direciton is chosen uniformly at random.
     */
    public Particle(double width){
        rx = StdRandom.uniform(0.0, width);
        ry = StdRandom.uniform(0.0, width);
        vx = StdRandom.uniform(-0.002, 0.02);
        vy = StdRandom.uniform(-0.002, 0.02);
        radius = 0.02;
        mass = 1000000;
        color = Color.BLACK;
    }

    /**
     * Moves this particle in a straight line (based on its velocity)
     * for the specified amount of time.
     *
     * @param dt the amount of time
     */
    public void move(double dt){
        this.vx += this.ax * dt;
        this.vy += this.ay * dt;

        rx += vx * dt;
        ry += vy * dt;

    }

    public double getRx(){
        return this.rx;
    }

    public double getRy(){
        return this.ry;
    }

    public double getVx(){
        return this.vx;
    }

    public double getVy(){
        return this.vy;
    }



    public void calAcceleration(){
        this.ax = this.fx / mass;
        this.ay = this.fy / mass;
    }

    /**
     * Draws this particle to standard draw.
     */
    public void draw(){
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
    public int count(){
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
    public double timeToHit(Particle that){

        double d_0 = Math.abs(this.distanceTo(that) - (this.radius + that.radius));
        if(d_0 <= 1e-6){
            return 0;
        }

        if(this == that) return INFINITY;
        double dx = that.rx - this.rx;
        double dy = that.ry - this.ry;
        double dv_x = that.vx - this.vx;
        double dv_y = that.vy - this.vy;
        double dvdr = dx * dv_x + dy * dv_y;
        if(dvdr > 0) return INFINITY;
        double dvdv = dv_x * dv_x + dv_y * dv_y;
        if(dvdv == 0) return INFINITY;
        double drdr = dx * dx + dy * dy;
        double sigma = this.radius + that.radius;
        double d = (dvdr * dvdr) - dvdv * (drdr - sigma * sigma);
        // if (drdr < sigma*sigma) StdOut.println("overlapping particles");
        if(d < 0) return INFINITY;
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
    public double timeToHitVerticalWall(double width){
        if(vx > 0) return (width - rx - radius) / vx;
        else if(vx < 0) return (radius - rx) / vx;
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
    public double timeToHitHorizontalWall(double width){
        if(vy > 0) return (width - ry - radius) / vy;
        else if(vy < 0) return (radius - ry) / vy;
        else return INFINITY;
    }

    /**
     * Updates the velocities of this particle and the specified particle according
     * to the laws of elastic collision. Assumes that the particles are colliding
     * at this instant.
     *
     * @param that the other particle
     */
    public void bounceOff(Particle that){
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
    public void bounceOffVerticalWall(){
        vx = -vx;
        count++;
    }

    /**
     * Updates the velocity of this particle upon collision with a horizontal
     * wall (by reflecting the velocity in the <em>y</em>-direction).
     * Assumes that the particle is colliding with a horizontal wall at this instant.
     */
    public void bounceOffHorizontalWall(){
        vy = -vy;
        count++;
    }

    public double kineticEnergy(){
        return 0.5 * mass * (vx * vx + vy * vy);
    }

    public double getRadius(){
        return this.radius;
    }
}
