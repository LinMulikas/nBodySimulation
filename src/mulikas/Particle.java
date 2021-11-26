package mulikas;


import javafx.scene.paint.Color;

import java.util.Random;

public class Particle{
	final double mass;
	final double radius;
	final Color color;
	
	Vector<Double> velocity;
	Vector<Double> position;
	Vector<Double> acceleration;
	
	Particle(){
		Random rand = new Random();
		this.mass = 1000 * rand.nextDouble();
		this.radius = 1000 * rand.nextDouble();
		this.color = Color.color(1, 1 , 1);
	}
	
	Particle(double mass, double radius, Color color, Vector<Double> position, Vector<Double> velocity){
		this.mass = mass;
		this.radius = radius;
		this.color = color;
		this.position = position;
		this.velocity = velocity;
	}
	
	Particle(double mass, double radius, Color color, Vector<Double> position, Vector<Double> velocity, Vector<Double> acceleration){
		this.mass = mass;
		this.radius = radius;
		this.color = color;
		this.position = position;
		this.velocity = velocity;
		this.acceleration = acceleration;
	}
}
