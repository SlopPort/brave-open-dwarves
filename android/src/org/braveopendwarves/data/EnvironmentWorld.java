package org.braveopendwarves.data;

/** Flags and shrinking breath interval recovered from EXE 40b6f0.
 * Uses nominal original clock and one original health point per interval. */
public final class EnvironmentWorld {
    private final OriginalLevel level;
    public float clock;
    public int counter,interval=128,remainder;
    public boolean water;
    public EnvironmentWorld(OriginalLevel map){level=map;}
    public void update(float dt,float x,float y,CombatWorld combat){
        if(combat.health<=0)return;
        int[] cell=level.cell((int)Math.floor((x+16)/64),(int)Math.floor((y+49)/64));
        water=cell!=null&&(cell[5]&0x80)!=0;
        if(cell!=null&&(cell[5]&0x400)!=0){combat.health=0;return;}
        if(!water){clock=0;counter=0;interval=128;return;}
        clock+=dt*OriginalClock.HZ;
        while(clock>=1&&combat.health>0){clock-=1;counter++;
            if(counter>=interval){counter=0;interval=Math.max(0,interval-8);combat.health--;remainder=0;}
        }
    }
    public float[] snapshot(){return new float[]{clock,counter,interval,remainder};}
    public void restore(float[] state)throws java.io.IOException{
        if(state.length!=4)throw new java.io.IOException("Bledny zapis oddechu");
        for(float v:state)if(Float.isNaN(v)||Float.isInfinite(v))throw new java.io.IOException("Bledny zapis oddechu");
        if(state[0]<0||state[0]>=1||state[1]<0||state[1]>128||state[1]!=(int)state[1]||state[2]<0||state[2]>128||state[2]%8!=0||state[3]<0||state[3]>3||state[3]!=(int)state[3])throw new java.io.IOException("Bledny zapis oddechu");
        clock=state[0];counter=(int)state[1];interval=(int)state[2];remainder=(int)state[3];
    }
}
