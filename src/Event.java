public class Event implements Comparable<Event>{
    public final double time;         // time that event is scheduled to occur
    public final Particle a, b;       // particles involved in event, possibly null
    public final int countA, countB;  // collision counts at event creation
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
    public boolean isValid(){
        if(a != null && a.count() != countA) return false;
        if(b != null && b.count() != countB) return false;
        return true;
    }

}

