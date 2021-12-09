package algs4;

import java.awt.*;

public class test{
    public static void main(String[] args){
        Particle p1 = new Particle(0.000045, 0.00005 , 0, 0, 0.000005, 1, new Color(1, 1, 1));
        Particle p2 = new Particle(0.000055, 0.00005 , 0, 0, 0.000005, 1, new Color(1, 1, 1));
        System.out.printf("%.16f" , Math.abs(p1.distanceTo(p2) - (p1.getRadius() + p2.getRadius())));
        System.out.println();
        Particle p3 = new Particle(45, 50 , 0, 0, 5, 1, new Color(1, 1, 1));
        Particle p4 = new Particle(55, 50 , 0, 0, 5, 1, new Color(1, 1, 1));
        System.out.printf("%.16f" , Math.abs(p3.distanceTo(p4) - (p3.getRadius() + p4.getRadius())));

        System.out.println();
        Particle p5 = new Particle(0.3000004596065353, 0.5, 0.05000062540, 0.0, 0.05, 1, new Color(1, 1, 1));
        Particle p6 = new Particle(0.3999999978559621, 0.5, 2.75120e-7, 0.0, 0.05, 1, new Color(1, 1, 1));
        System.out.println(p5.timeToHit(p6));
    }
}
