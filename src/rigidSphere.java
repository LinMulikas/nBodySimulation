import javafx.scene.shape.Circle;

import java.awt.*;

public class rigidSphere extends Circle {
    final double radius;
    final double mass;
    final Color color;
    private boolean trail = false;

    Vector position;
    Vector prePosition;
    Vector force;
    Vector velocity;
    Vector acceleration;



    public rigidSphere(double size, double mass, Color color, Vector position) {
        this.radius = size;
        this.mass = mass;
        this.color = color;
        this.position = position;
    }

    public void setVelocity(Vector v) {
        this.velocity = v;
    }

    public void setForce(Vector force) {
        this.force = force;
    }

    public void setAcceleration(Vector acceleration) {
        this.acceleration = acceleration;
    }

    public void setP(long x, long y) {
        this.position.x = x;
        this.position.y = y;
    }

    public void calPosition(double tick){
        this.prePosition.x = this.position.x;
        this.prePosition.y = this.position.y;
        this.position.x = this.position.x + this.velocity.x*tick + 0.5*this.acceleration.x*tick*tick;
        this.position.y = this.position.y + this.velocity.y*tick + 0.5*this.acceleration.y*tick*tick;
    }

    public boolean isTrail() {
        return trail;
    }

    public void setTrail(boolean trail) {
        this.trail = trail;
    }
}
