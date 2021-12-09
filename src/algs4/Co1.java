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
public class Co1{
    private double width;
    private Quad q;
    private double[] checkTimeList;
    private int[] checkParticlesList;
    private static final double HZ = 0.5;    // number of redraw events per clock tick
    public final double G = 6.67e-11;
    private MinPQ<Event> pq;          // the priority queue
    private double t = 0.0;           // simulation clock time
    private Particle[] particles;     // the array of particles

    public int numToCheck;
    public double[][] ans;
    public double[][] myAns;
    public double[][] errors;

    public Co1(){

    }

    public Co1(Particle[] particles){
        this.particles = particles.clone();   // defensive copy
    }

    public void setParticles(Particle[] particles){
        this.particles = particles;
    }

    public void setWidth(double width){
        this.width = width;
        this.q = new Quad(width);
    }


    /**
     * 设计一个小范围的误差浮动，尝试解决牛顿摆
     *
     * @param a
     */
    private void predict(Particle a){
        if(a == null) return;

        // particle-particle collisions
        for(int i = 0; i < particles.length; i++){
            double dt = a.timeToHit(particles[i]);
            if(dt > 0 && dt <= 1.0 / HZ){
                pq.insert(new Event(t + dt, a, particles[i]));
            }
        }
        // particle-wall collisions
        double dtX = a.timeToHitVerticalWall(width);
        double dtY = a.timeToHitHorizontalWall(width);
        if(dtX >= 0 && dtX <= 1.0 / HZ) pq.insert(new Event(t + dtX, a, null));
        if(dtY >= 0 && dtY <= 1.0 / HZ) pq.insert(new Event(t + dtY, null, a));
    }


    // redraw all particles
    private void redraw(){
        StdDraw.clear();
        for(int i = 0; i < particles.length; i++){
            particles[i].draw();
        }
        StdDraw.show();
        StdDraw.pause(10);

    }


    public void simulate(boolean checkAns){

        // initialize PQ with collision events and redraw event
        pq = new MinPQ<Event>();
        double checkTime = 0;
        int index = 0;

        if(checkAns){
            checkTime = this.checkTimeList[index];
            errors = new double[numToCheck][4];
        }


        pq.insert(new Event(0, null, null));        // redraw event


        /**
         * 初始化的预测
         */
        for(Particle particle : particles){
            predict(particle);
        }


        // the main event-driven simulation loop
        while(!pq.isEmpty()){


            Event e = pq.delMin();

            Particle a = e.a;
            Particle b = e.b;

            // physical collision, so update positions, and then simulation clock
            if(checkAns){
                if(e.time > checkTime && numToCheck > index){

                    for(int i = 0; i < particles.length; i++){
                        particles[i].move(checkTime - t);
                    }

                    this.recordAns(index, myAns, particles, checkParticlesList[index]);
                    this.calErrors(index);

                    index++;
                    if(index < numToCheck){
                        checkTime = checkTimeList[index];
                    }
                }
                else{
                    if(index >= numToCheck){
                        printArray(myAns);
                        System.out.println();
                        printArray(errors);
                        System.out.println();
                    }

                    for(int i = 0; i < particles.length; i++){
                        particles[i].move(e.time - t);
                    }
                }
            }
            else{
                for(int i = 0; i < particles.length; i++){
                    particles[i].move(e.time - t);
                }
            }
            t = e.time;

            // process event
            if(a != null && b != null){
                a.bounceOff(b);
            }             // particle-particle collision
            else if(a != null){
                a.bounceOffVerticalWall();
            }  // particle-wall collision
            else if(b != null){
                b.bounceOffHorizontalWall();
            }// particle-wall collision
            else{
                redraw();
            }               // redraw event

            while(!pq.isEmpty()){
                pq.delMin();
            }


            pq.insert(new Event(t + 1.0 / HZ, null, null));

            // 被注释掉的部分是保持1tick为最小时间，但会造成视觉上碰撞时候的"帧数"上升
//			pq.insert(new Event(t_0 + 1.0 / HZ, null, null));
//			t_0 += 1.0 / HZ;

            /**
             * 更新引力状态和速度状态
             */
            // update the priority queue with new collisions involving a or b
            this.calForces();
            /**
             * 新的预测
             */
            for(Particle particle : particles){
                predict(particle);
            }
        }
    }

    public void calForces(){
        /**
         * BHT
         */
        BHT tree = new BHT(q);

        for(Particle p : particles){
            tree.insert(p);
        }

        for(Particle p : particles){
            p.resetForce();
            tree.updateForce(p, this.G);
            p.calAcceleration();
        }
    }

    public void printParticles(double width){
        for(Particle p : particles){
            System.out.print(p.toString(width));
        }
    }

    public void calErrors(int index){
        for(int i = 0; i < 4; i++){
            errors[index][i] = 100 * Math.abs((myAns[index][i] - ans[index][i]) / ans[index][i]);
        }
    }

    public void printArray(double[][] array){
        for(int i = 0; i < numToCheck; i++){
            for(int j = 0; j < 4; j++){
                System.out.printf("%f ", array[i][j]);
            }
            System.out.println();
        }
    }

    public void recordAns(int index, double[][] myAns, Particle[] particles, int id){
        myAns[index][0] = particles[id].getRx();
        myAns[index][1] = particles[id].getRy();
        myAns[index][2] = particles[id].getVx();
        myAns[index][3] = particles[id].getVy();
    }

    public double[] getCheckTimeList(){
        return checkTimeList;
    }

    public void setCheckTimeList(double[] checkTimeList){
        this.checkTimeList = checkTimeList;
    }

    public int[] getCheckParticlesList(){
        return checkParticlesList;
    }

    public void setCheckParticlesList(int[] checkParticlesList){
        this.checkParticlesList = checkParticlesList;
    }


    private static class Event implements Comparable<Event>{
        private final double time;         // time that event is scheduled to occur
        private final Particle a, b;       // particles involved in event, possibly null
        private final int countA, countB;  // collision counts at event creation
        public boolean collision = true;

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


        // compare times when two events will occur
        public int compareTo(Event that){
            return Double.compare(this.time, that.time);
        }

        // has any collision occurred between when event was created and now?
        public boolean isValid(double t){
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
        boolean checkAns = false;

        Co1 system = new Co1();


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
                particles[i] = new Particle(1);
        }
        // or read from standard input
        else{
            int width = StdIn.readInt();
            system.setWidth(width);
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
        if(checkAns){
            int numCheck = StdIn.readInt();
            system.numToCheck = numCheck;

            double[] checkList = new double[numCheck];
            int[] ids = new int[numCheck];
            double[][] ans = new double[numCheck][4];
            double[][] myAns = new double[numCheck][4];

            for(int i = 0; i < numCheck; i++){
                checkList[i] = StdIn.readDouble();
                ids[i] = StdIn.readInt();
            }

            for(int i = 0; i < numCheck; i++){
                for(int j = 0; j < 4; j++){
                    ans[i][j] = StdIn.readDouble();
                    myAns[i][j] = 0;
                }
            }

            system.setCheckTimeList(checkList);
            system.setCheckParticlesList(ids);

            system.ans = ans;
            system.myAns = myAns;
        }

        // create collision system and simulate

        system.setParticles(particles);
        StdDraw.setScale(0, system.width);
        system.simulate(checkAns);
    }
}
