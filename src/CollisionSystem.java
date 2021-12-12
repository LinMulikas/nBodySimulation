import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;


public class CollisionSystem{
    private double width;
    private Quad q;
    private double[] checkTimeList;
    private int[] checkParticlesList;
    private static double HZ = 8;    // number of redraw events per clock tick
    public final double G = 6.67259e-11;
    private PriorityBlockingQueue<Event> pq;          // the priority queue
    private double t = 0.0;           // simulation clock time
    private Particle[] particles;     // the array of particles

    private int accuracy = 8;
    private BarnesHutTree tree; //用于存储所有节点的总树

    public int numToCheck;
    public double[][] ans;
    public double[][] myAns;
    public double[][] errors;

    private boolean guiContinue = false;

    private int printCount = 0;

    public CollisionSystem(){

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

        for(int i = 0; i < particles.length; i++){
            double dt = a.timeToHit(particles[i]);
            if(dt >= 0 && dt <= 1.0 / HZ){
                pq.add(new Event(t + dt, a, particles[i]));
            }
        }


        // particle-wall collisions
        double dtX = a.timeToHitVerticalWall(width);
        double dtY = a.timeToHitHorizontalWall(width);
        if(dtX >= 0 && dtX <= 1.0 / HZ) pq.add(new Event(t + dtX, a, null));
        if(dtY >= 0 && dtY <= 1.0 / HZ) pq.add(new Event(t + dtY, null, a));
    }


    // redraw all particles
    private void redraw(){
        StdDraw.clear();
        Arrays.stream(particles).parallel().forEach(Particle::draw);
        StdDraw.show();
    }


    public void simulate(boolean hasCheckList, boolean hasAnswerList, boolean GUI){

        long start = System.currentTimeMillis();

        // initialize PQ with collision events and redraw event
        pq = new PriorityBlockingQueue<Event>();
        double checkTime = 0;
        int index = 0;

        if(hasCheckList){
            if(checkTimeList.length != 0){
                checkTime = this.checkTimeList[index];
            }
            errors = new double[numToCheck][4];
        }

        pq.add(new Event(0, null, null));        // redraw event
        double t_0 = t;

        //初始化的建树
        tree = new BarnesHutTree(q);

        for(Particle p : particles){
            tree.insert(p);
        }

        /**
         * 初始化的预测
         */
        for(Particle particle : particles){
            predict(particle);
        }

        while(true){
            /**
             * 对所有预测进行操作
             * 但是是操作完再进行新的预测，所以会出现错误时间的问题
             */
            while(!pq.isEmpty()){
                Event event = pq.remove();
                if(event.isValid()){
                    Particle a = event.a;
                    Particle b = event.b;
                    /**
                     * 全体粒子的移动
                     */
                    for(int i = 0; i < particles.length; i++){
                        particles[i].move(event.time - t);
                    }

                    t = event.time;

                    /**
                     * 检查点的检测
                     */
                    if(t > checkTime){
                        if(hasCheckList){
                            if(numToCheck > index){

                                this.recordAns(index, myAns, particles, checkParticlesList[index]);

                                index++;
                                if(index < numToCheck){
                                    checkTime = checkTimeList[index];
                                }
                            }
                            else{
                                if(printCount == 0){
                                    printArray(myAns);
                                    printCount++;
                                }
                            }
                        }
                    }

                    /**
                     * Event Handle
                     */

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
                        while(!pq.isEmpty()){
                            pq.remove();
                        }

                        pq.add(new Event(t + 1.0 / HZ, null, null));

                        /**
                         * 预测
                         */
                        tree = new BarnesHutTree(q);

                        for(Particle p : particles){
                            tree.insert(p);
                            p.predictWalls(pq, width, HZ, t);
                        }

                        Arrays.stream(particles).parallel().forEach(particle -> {
                            particle.calNeighbors(tree, accuracy);
                            particle.neighbors.forEach(x -> {
                                x.action(particle, pq, HZ, t);
                            });
                        });

                        if(GUI){
                            redraw();
                        }
                    }

                    /**
                     * 预测
                     */

                    tree = new BarnesHutTree(q);

                    for(Particle p : particles){
                        tree.insert(p);
                        p.predictWalls(pq, width, HZ, t);
                    }

                    this.calForces();

                    Arrays.stream(particles).parallel().forEach(particle -> {
                        particle.calNeighbors(tree, accuracy);
                        particle.neighbors.forEach(x -> {
                            x.action(particle, pq, HZ, t);
                        });
                    });
                }
            }
        }


    }

    public void calForces(){
        /**
         * BHT
         */
        Arrays.stream(particles).parallel().forEach(particle -> {
            particle.resetForce();
            tree.updateForce(particle, this.G);
            particle.changeVelocity(1.0 / HZ);
        });
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


    /**
     * Unit tests the {@code algs4.CollisionSystem} data type.
     * Reads in the particle collision system from a standard input
     * (or generates {@code N} random particles if a command-line integer
     * is specified); simulates the system.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args){
        boolean hasCheckList = true;
        boolean hasAnswerList = false;
        boolean GUI = true;

        Scanner in = new Scanner(System.in);

        CollisionSystem system = new CollisionSystem();

        Particle[] particles;


        String model = StdIn.readString();
        if(model.equals("terminal")){
            GUI = false;
        }

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

        int numCheck = StdIn.readInt();
        system.numToCheck = numCheck;

        double[] checkList = new double[numCheck];
        int[] ids = new int[numCheck];


        for(int i = 0; i < numCheck; i++){
            checkList[i] = StdIn.readDouble();
            ids[i] = StdIn.readInt();
        }

        system.setCheckTimeList(checkList);
        system.setCheckParticlesList(ids);


        double[][] myAns = new double[numCheck][4];
        for(int i = 0; i < numCheck; i++){
            for(int j = 0; j < 4; j++){
                myAns[i][j] = 0;
            }
        }
        system.myAns = myAns;

        system.setParticles(particles);
        if(GUI){
            StdDraw.setCanvasSize(600, 600);
            // enable double buffering
            StdDraw.enableDoubleBuffering();
            StdDraw.setScale(0, system.width);
        }

        system.simulate(hasCheckList, hasAnswerList, GUI);
    }
}
