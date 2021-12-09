//改写BHTree
public class BHT{
    private Particle particle;
    private Quad quad;     // square region that the tree represents
    private BHT NW;     // tree representing northwest quadrant
    private BHT NE;     // tree representing northeast quadrant
    private BHT SW;     // tree representing southwest quadrant
    private BHT SE;     // tree representing southeast quadrant

    //Create and initialize a new bhtree. Initially, all nodes are null and will be filled by recursion
    //Each BHTree represents a quadrant and a body that represents all bodies inside the quadrant
    public BHT(Quad q){
        this.quad = q;
        this.particle = null;
        this.NW = null;
        this.NE = null;
        this.SW = null;
        this.SE = null;
    }

    //If all nodes of the BHTree are null, then the quadrant represents a single body and it is "external"
    public Boolean isExternal(){
        if(NW == null && NE == null && SW == null && SE == null) return true;
        else return false;
    }

    //We have to populate the tree with bodies. We start at the current tree and recursively travel through the branches
    public void insert(Particle b){
        //If there's not a body there already, put the body there.
        if(this.particle == null){
            this.particle = b;
        }
        //If there's already a body there, but it's not an external node
        //combine the two bodies and figure out which quadrant of the
        //tree it should be located in. Then recursively update the nodes below it.
        else if(!this.isExternal()){
            this.particle = b.centroid(this.particle, b);

            Quad northwest = this.quad.NW();
            if(b.in(northwest)){
                if(this.NW == null){
                    this.NW = new BHT(northwest);
                }
                NW.insert(b);
            } else{
                Quad northeast = this.quad.NE();
                if(b.in(northeast)){
                    if(this.NE == null){
                        this.NE = new BHT(northeast);
                    }
                    NE.insert(b);
                } else{
                    Quad southeast = this.quad.SE();
                    if(b.in(southeast)){
                        if(this.SE == null){
                            this.SE = new BHT(southeast);
                        }
                        SE.insert(b);
                    } else{
                        Quad southwest = this.quad.SW();
                        if(this.SW == null){
                            this.SW = new BHT(southwest);
                        }
                        SW.insert(b);
                    }
                }
            }
        }
        //If the node is external and contains another body, create BHTrees
        //where the bodies should go, update the node, and end
        //(do not do anything recursively)
        else if(this.isExternal()){
            Particle c = this.particle;
            Quad northwest = this.quad.NW();
            if(c.in(northwest)){
                if(this.NW == null){
                    this.NW = new BHT(northwest);
                }
                NW.insert(c);
            } else{
                Quad northeast = this.quad.NE();
                if(c.in(northeast)){
                    if(this.NE == null){
                        this.NE = new BHT(northeast);
                    }
                    NE.insert(c);
                } else{
                    Quad southeast = this.quad.SE();
                    if(c.in(southeast)){
                        if(this.SE == null){
                            this.SE = new BHT(southeast);
                        }
                        SE.insert(c);
                    } else{
                        Quad southwest = this.quad.SW();
                        //测试用 避免飞出边界时RE
                        if(!c.in(southwest)) return;

                        if(this.SW == null){
                            this.SW = new BHT(southwest);
                        }
                        SW.insert(c);
                    }
                }
            }
            this.insert(b);
        }
    }

    //用四叉树的方法近似计算施加在b上的所有力
    //Start at the main node of the tree. Then, recursively go each branch
    //Until either we reach an external node or we reach a node that is sufficiently
    //far away that the external nodes would not matter much.
    public void updateForce(Particle b, double g){
        if(this.isExternal()){
            if(this.particle != b) b.addForceTo(this.particle, g);
        } else if(this.quad.length() / (this.particle.distanceTo(b)) < 2){
            b.addForceTo(this.particle, g);
        } else{
            if(this.NW != null) this.NW.updateForce(b, g);
            if(this.SW != null) this.SW.updateForce(b, g);
            if(this.SE != null) this.SE.updateForce(b, g);
            if(this.NE != null) this.NE.updateForce(b, g);
        }
    }


}