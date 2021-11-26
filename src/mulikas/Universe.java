package mulikas;

import java.awt.*;

public class Universe{
	final double G = 6.67e-11;
	double tick;
	Particle[] particles;
	
	double width;
	double height;
	
	public Universe(int n){
		particles = new Particle[n];
		for(int i = 0; i < 1; i++){
		
		}
	}
	
	public Universe(Particle[] particles){
		this.particles = particles.clone();
	}
}
