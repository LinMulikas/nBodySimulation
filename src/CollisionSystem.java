import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;


public class CollisionSystem{
    private double width;
    private Quad q;
    private double[] checkTimeList;
    private int[] checkParticlesList;
    private static final double HZ = 5;    // number of redraw events per clock tick
    public final double G = 6.67259e-11;
    private MinPQ pq;          // the priority queue
    private double t = 0.0;           // simulation clock time
    private Particle[] particles;     // the array of particles

    private ConcurrentLinkedQueue<Event> safePQ = new ConcurrentLinkedQueue<>();

    private BHT tree; //用于存储所有节点的总树

    public int numToCheck;
    public double[][] ans;
    public double[][] myAns;
    public double[][] errors;

    private boolean guiContinue = false;

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

    private void predictAction(Particle a, Particle b){
        if(a == null) return;

        double dt = a.timeToHit(b);
        if(dt >= 0 && dt <= 1.0 / HZ){
            pq.insert(new Event(t + dt, a, b));
        }

        double dtX = a.timeToHitVerticalWall(width);
        double dtY = a.timeToHitHorizontalWall(width);
        if(dtX >= 0 && dtX <= 1.0 / HZ) pq.insert(new Event(t + dtX, a, null));
        if(dtY >= 0 && dtY <= 1.0 / HZ) pq.insert(new Event(t + dtY, null, a));
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
                pq.insert(new Event(t + dt, a, particles[i]));
            }
        }


        // particle-wall collisions
        double dtX = a.timeToHitVerticalWall(width);
        double dtY = a.timeToHitHorizontalWall(width);
        if(dtX >= 0 && dtX <= 1.0 / HZ) pq.insert(new Event(t + dtX, a, null));
        if(dtY >= 0 && dtY <= 1.0 / HZ) pq.insert(new Event(t + dtY, null, a));
    }

    public void predictByTree(Particle a){
        if(a == null) return;

        //改为使用BHT进行预测
        BHT b = tree.find(a);
        tree.BHTPredict(b, pq, HZ, t);

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
        StdDraw.pause(5);
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

        //初始化的建树
        tree = new BHT(q);

        for(Particle p : particles){
            tree.insert(p);
        }

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
                            System.out.println("模拟用时：" + (end - start) / 1000.0 + " 秒");
                            System.out.println();
                            if(hasAnswerList){
                                printArray(errors);
                                System.out.println();
                            }
                            if(GUI){
                                printCount++;
                            }
                            else{
                                System.out.println("答案已输出");
                                Scanner in = new Scanner(System.in);
                                int a = in.nextInt();
                            }
                        }
                        else{
                            if(!this.guiContinue){
                                System.out.println("是否继续运行？（y/n）");
                                Scanner in = new Scanner(System.in);
                                String contin = in.next();
                                if(contin.equals("y") || contin.equals("Y")){
                                    this.guiContinue = true;
                                }
                            }
                        }
                    }
                }
            }

            for(Particle p : particles){
                p.move(e.time - t);
            }

//            Arrays.stream(particles).forEach(particle -> particle.move(e.time - t));


            //每次循环中move后重新建树
            tree = new BHT(q);
            for(Particle p : particles){
                tree.insert(p);
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
                predictByTree(particle);
            }

//            for(Particle particle : particles){
//                particle.parallelPredict(particle, tree, pq, width, HZ, t);
//            }

//            List<Particle> list = Arrays.asList(particles);
//            Collections.synchronizedList(list).parallelStream().forEach(this::predictByTree);
//
//            list.parallelStream().forEach(this::predictByTree);
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
//        tree = new BHT(q);
//
//        for(Particle p : particles){
//            tree.insert(p);
//        }

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

        // 读取文件
        if(fileRead.equals("y") || fileRead.equals("Y")){
            Particle[] particles = null;

            System.out.println("文件是否包括答案？（y/n）");
            String hasAns = in.next();
            if(hasAns.equals("y") || hasAns.equals("Y")){
                hasAnswerList = true;
            }

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

                /**
                 * 质点初始化
                 */
                particles = new Particle[number];
                for(int i = 0; i < number; i++){
                    String[] line0 = br.readLine().split("\\s");
                    ArrayList<String> line = new ArrayList<>();
                    for(String str : line0){
                        if(!str.isEmpty()){
                            line.add(str);
                        }
                    }
                    double rx = Double.parseDouble(line.get(0));
                    double ry = Double.parseDouble(line.get(1));
                    double vx = Double.parseDouble(line.get(2));
                    double vy = Double.parseDouble(line.get(3));
                    double radius = Double.parseDouble(line.get(4));
                    double mass = Double.parseDouble(line.get(5));
                    int r = Integer.parseInt(line.get(6));
                    int g = Integer.parseInt(line.get(7));
                    int b = Integer.parseInt(line.get(8));
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
                    String[] line0 = br.readLine().split("\\s");
                    ArrayList<String> line = new ArrayList<>();
                    for(String str : line0){
                        if(!str.isEmpty()){
                            line.add(str);
                        }
                    }
                    checkList[i] = Double.parseDouble(line.get(0));
                    ids[i] = Integer.parseInt(line.get(1));
                }

                system.setCheckTimeList(checkList);
                system.setCheckParticlesList(ids);

                if(hasAnswerList){
                    double[][] ans = new double[numCheck][4];
                    for(int i = 0; i < numCheck; i++){
                        String[] line = br.readLine().split("\\s");

                        for(int j = 0; j < 4; j++){
                            ans[i][j] = Double.parseDouble(line[j]);
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
                StdDraw.setCanvasSize(600, 600);
                // enable double buffering
                StdDraw.enableDoubleBuffering();
                StdDraw.setScale(0, system.width);
            }
            system.simulate(hasCheckList, hasAnswerList, GUI);
        }

    }
}
