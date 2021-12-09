import java.awt.Color;
import java.io.*;
import java.util.Scanner;
import java.util.Timer;


public class CollisionSystem{
    private double width;
    private Quad q;
    private double[] checkTimeList;
    private int[] checkParticlesList;
    private static final double HZ = 10;    // number of redraw events per clock tick
    public final double G = 6.67e-11;
    private MinPQ pq;          // the priority queue
    private double t = 0.0;           // simulation clock time
    private Particle[] particles;     // the array of particles

    public int numToCheck;
    public double[][] ans;
    public double[][] myAns;
    public double[][] errors;

    private int printCount = 0;

    public CollisionSystem(){

    }

    public CollisionSystem(Particle[] particles){
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
            if(dt >= 0 && dt <= 1.0 / HZ){
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


    public void simulate(boolean hasCheckList, boolean hasAnswerList, boolean GUI){

        long start = System.currentTimeMillis();

        // initialize PQ with collision events and redraw event
        pq = new MinPQ();
        double checkTime = 0;
        int index = 0;

        if(hasCheckList){
            if(checkTimeList.length != 0){
                checkTime = this.checkTimeList[index];
            }
            errors = new double[numToCheck][4];
        }

        pq.insert(new Event(0, null, null));        // redraw event


        /**
         * 初始化的预测
         */
        for(Particle particle : particles){
            predict(particle);
        }

        while(!pq.isEmpty()){
            Event e = pq.delMin();
            if(!e.isValid()) continue;
            // 牛顿摆怎么解决捏。

            // physical collision, so update positions, and then simulation clock
            if(hasCheckList){
                if(e.time > checkTime && numToCheck > index){

                    for(int i = 0; i < particles.length; i++){
                        particles[i].move(checkTime - t);
                    }
                    t = checkTime;

                    this.recordAns(index, myAns, particles, checkParticlesList[index]);

                    if(hasAnswerList){
                        this.calErrors(index);
                    }

                    index++;
                    if(index < numToCheck){
                        checkTime = checkTimeList[index];
                    }
                }
                else{
                    if(index >= numToCheck){
                        if(printCount == 0){
                            printArray(myAns);
                            System.out.println();
                            long end = System.currentTimeMillis();
                            System.out.println("用时：" + (end - start)/1000.0 + "秒");
                            System.out.println();
                            if(hasAnswerList){
                                printArray(errors);
                                System.out.println();
                            }
                            if(GUI){
                                printCount++;
                            }
                            else{
                                Scanner in = new Scanner(System.in);
                                int a = in.nextInt();
                            }
                        }
                    }
                }
            }

            for(int i = 0; i < particles.length; i++){
                particles[i].move(e.time - t);
            }

            t = e.time;

            // process event
            Particle a = e.a;
            Particle b = e.b;


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
                if(GUI){
                    redraw();
                }
            }


            while(!pq.isEmpty()){
                pq.delMin();
            }

            pq.insert(new Event(t + 1.0 / HZ, null, null));

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

    public void handle(Event e){
        // process event
        Particle a = e.a;
        Particle b = e.b;
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
            p.changeVelocity(1.0 / HZ);
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

        System.out.println("是否要读取文件？（y/n）");
        String fileRead = in.next();

        if(fileRead.equals("y") || fileRead.equals("Y")){
            Particle[] particles = null;

            System.out.println("请输入文件路径(含名字)：");
            String filePath = in.next();
            InputStreamReader fileReader = null;

            try{
                try{
                    fileReader = new InputStreamReader(
                            new FileInputStream(filePath), "GBK");
                }
                catch(UnsupportedEncodingException e){
                    e.printStackTrace();
                }
            }
            catch(FileNotFoundException e){
                e.printStackTrace();
            }

            BufferedReader br = new BufferedReader(fileReader);

            try{
                String model = br.readLine();
                if(model.equals("terminal")){
                    GUI = false;
                }

                if(GUI){
                    StdDraw.setCanvasSize(600, 600);

                    // enable double buffering
                    StdDraw.enableDoubleBuffering();
                }

                String strWidth = br.readLine();
                String strNumber = br.readLine();
                int width = Integer.parseInt(strWidth);
                int number = Integer.parseInt(strNumber);

                system.setWidth(width);

                particles = new Particle[number];
                for(int i = 0; i < number; i++){
                    String[] line = br.readLine().split("\t");
                    double rx = Double.parseDouble(line[0]);
                    double ry = Double.parseDouble(line[1]);
                    double vx = Double.parseDouble(line[2]);
                    double vy = Double.parseDouble(line[3]);
                    double radius = Double.parseDouble(line[4]);
                    double mass = Double.parseDouble(line[5]);
                    int r = Integer.parseInt(line[6]);
                    int g = Integer.parseInt(line[6]);
                    int b = Integer.parseInt(line[6]);
                    Color color = new Color(r, g, b);
                    particles[i] = new Particle(rx, ry, vx, vy, radius, mass, color);
                }
                system.setParticles(particles);

                String strNumCheck = br.readLine();
                int numCheck = Integer.parseInt(strNumCheck);
                system.numToCheck = numCheck;

                double[] checkList = new double[numCheck];
                int[] ids = new int[numCheck];


                for(int i = 0; i < numCheck; i++){
                    String[] line = br.readLine().split("\t");
                    checkList[i] = Double.parseDouble(line[0]);
                    ids[i] = Integer.parseInt(line[1]);
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
            }
            catch(IOException e){
                e.printStackTrace();
            }

            // create collision system and simulate

            system.setParticles(particles);
            if(GUI){
                StdDraw.setScale(0, system.width);
            }



            system.simulate(hasCheckList, hasAnswerList, GUI);

        }
        else{
            System.out.println("请输入标准格式的输入：");

            // the array of particles
            Particle[] particles;


            String model = StdIn.readString();
            if(model.equals("terminal")){
                GUI = false;
            }

            if(GUI){
                StdDraw.setCanvasSize(600, 600);
                // enable double buffering
                StdDraw.enableDoubleBuffering();
            }

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
            if(hasCheckList){
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

                if(hasAnswerList){
                    double[][] ans = new double[numCheck][4];
                    for(int i = 0; i < numCheck; i++){
                        for(int j = 0; j < 4; j++){
                            ans[i][j] = StdIn.readDouble();
                        }
                    }
                    system.ans = ans;
                }


                double[][] myAns = new double[numCheck][4];
                for(int i = 0; i < numCheck; i++){
                    for(int j = 0; j < 4; j++){
                        myAns[i][j] = 0;
                    }
                }
                system.myAns = myAns;
            }


            // create collision system and simulate

            system.setParticles(particles);
            if(GUI){
                StdDraw.setScale(0, system.width);
            }
            system.simulate(hasCheckList, hasAnswerList, GUI);
        }

    }
}
