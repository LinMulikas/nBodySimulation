import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;


public class Co1{
    private double width;
    private Quad q;
    private double[] checkTimeList;
    private int[] checkParticlesList;
    private static double HZ = 5;    // number of redraw events per clock tick
    public final double G = 6.67259e-11;
    private PriorityBlockingQueue<Event> pq;          // the priority queue
    private double t = 0.0;           // simulation clock time
    private Particle[] particles;     // the array of particles

    private int accuracy = 6;
    private BarnesHutTree tree; //用于存储所有节点的总树

    public int numToCheck;
    public double[][] ans;
    public double[][] myAns;
    public double[][] errors;

    private boolean guiContinue = false;

    private int printCount = 0;

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

    private void predictAction(Particle a, Particle b){
        if(a == null) return;

        double dt = a.timeToHit(b);
        if(dt >= 0 && dt <= 1.0 / HZ){
            pq.add(new Event(t + dt, a, b));
        }

        double dtX = a.timeToHitVerticalWall(width);
        double dtY = a.timeToHitHorizontalWall(width);
        if(dtX >= 0 && dtX <= 1.0 / HZ) pq.add(new Event(t + dtX, a, null));
        if(dtY >= 0 && dtY <= 1.0 / HZ) pq.add(new Event(t + dtY, null, a));
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

    public void predictByTree(Particle a){
        if(a == null) return;

        //改为使用BHT进行预测
        BarnesHutTree b = tree.find(a);
        tree.BHTPredict(b, accuracy, pq, HZ, t);

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

                                if(hasAnswerList){
                                    this.calErrors(index);
                                }

                                index++;
                                if(index < numToCheck){
                                    checkTime = checkTimeList[index];
                                }
                            }
                            else{
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
                                        int mulikas = in.nextInt();
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
                                        else{
                                            System.out.println("程序已停止");
                                            while(true) ;
                                        }
                                    }
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


//        while(!pq.isEmpty()){
//            Event e = pq.remove();
//            if(!e.isValid()) continue;
//            // 牛顿摆怎么解决捏。
//
//            // physical collision, so update positions, and then simulation clock
//            /**
//             * 检查点的检测
//             */
//            if(e.time > checkTime){
//                if(hasCheckList){
//                    if(numToCheck > index){
//
//                        for(int i = 0; i < particles.length; i++){
//                            particles[i].move(checkTime - t);
//                        }
//
//                        t = checkTime;
//
//                        this.recordAns(index, myAns, particles, checkParticlesList[index]);
//
//                        if(hasAnswerList){
//                            this.calErrors(index);
//                        }
//
//                        index++;
//                        if(index < numToCheck){
//                            checkTime = checkTimeList[index];
//                        }
//                    }
//                    else{
//                        if(printCount == 0){
//                            printArray(myAns);
//                            System.out.println();
//                            long end = System.currentTimeMillis();
//                            System.out.println("模拟用时：" + (end - start) / 1000.0 + " 秒");
//                            System.out.println();
//                            if(hasAnswerList){
//                                printArray(errors);
//                                System.out.println();
//                            }
//                            if(GUI){
//                                printCount++;
//                            }
//                            else{
//                                System.out.println("答案已输出");
//                                Scanner in = new Scanner(System.in);
//                                int a = in.nextInt();
//                            }
//                        }
//                        else{
//                            if(!this.guiContinue){
//                                System.out.println("是否继续运行？（y/n）");
//                                Scanner in = new Scanner(System.in);
//                                String contin = in.next();
//                                if(contin.equals("y") || contin.equals("Y")){
//                                    this.guiContinue = true;
//                                }
//                            }
//                        }
//                    }
//                }
//
//            }
//
//            /**
//             * 事件的处理
//             */
//            for(Particle p : particles){
//                p.move(e.time - t);
//            }
//
//            t = e.time;
//
//            // process event
//            Particle a = e.a;
//            Particle b = e.b;
//
//
//            if(a != null && b != null){
//                a.bounceOff(b);
//            }             // particle-particle collision
//            else if(a != null){
//                a.bounceOffVerticalWall();
//            }  // particle-wall collision
//            else if(b != null){
//                b.bounceOffHorizontalWall();
//            }// particle-wall collision
//            else{
//
//                while(!pq.isEmpty()){
//                    pq.remove();
//                }
//
//                pq.add(new Event(t + 1.0 / HZ, null, null));
//
//                /**
//                 * 新的预测
//                 */
//
//                //每次循环中move后重新建树
//                tree = new BHT(q);
//
//                for(Particle p : particles){
//                    tree.insert(p);
//                    p.predictWalls(pq, width, HZ, t);
//                }
//
//                Arrays.stream(particles).parallel().forEach(particle -> {
//                    particle.calNeighbors(tree);
//                    particle.neighbors.forEach(x -> {
//                        x.action(particle, pq, HZ, t);
//                    });
//                });
//
//                if(GUI){
//                    redraw();
//                }
//            }
//
//
//
//            /**
//             * 更新引力状态和速度状态
//             */
//            // update the priority queue with new collisions involving a or b
//            this.calForces();
//
//
////            Arrays.stream(particles).parallel().forEach(particle -> {
////                tree.insert(particle);
////                particle.predictWalls(pq, width, HZ, t);
////            });
//
////            for(Particle particle : particles){
////                predictByTree(particle);
////            }
//
//
////            System.out.println();
//        }
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

        Co1 system = new Co1();

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
        }
        else{
            System.out.println("请输入标准格式的输入：");

            // the array of particles
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
        }

        System.out.println("请设置系统碰撞检测精度：" +
                "\n（正整数，默认为6，一般建议8-16，如果输入格式不正确将采取默认值）");
        if(in.hasNextInt()){
            int a = in.nextInt();
            if(a > 0){
                system.accuracy = a;
            }
        }
        if(GUI){
            System.out.println("请设置画面渲染频率：" +
                    "\n正浮点数，将决定视觉上的流畅度，如果频率过高会降低模拟速度；如果频率过低可能无法正常绘制图形。\n" +
                    "建议为5-20之间，不建议超过100");
            if(in.hasNextDouble()){
                double hz = in.nextDouble();
                if(hz > 0){
                    HZ = hz;
                }
            }
        }

        if(GUI){
            // enable double buffering
            StdDraw.enableDoubleBuffering();
            StdDraw.setScale(0, system.width);
            StdDraw.setCanvasSize(800, 800);
            // enable double buffering
            StdDraw.enableDoubleBuffering();
            StdDraw.setScale(0, system.width);
        }

        system.simulate(hasCheckList, hasAnswerList, GUI);
    }
}
