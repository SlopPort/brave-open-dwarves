package org.braveopendwarves.data;
import java.util.*;

/** Lift geometry and rail tests recovered from Gnomes.exe 414990/414b40.
 * Initial wait is fixed at two seconds; the original randomizes it. */
public final class LiftWorld {
    public final ArrayList<Lift> lifts=new ArrayList<Lift>();
    private final OriginalLevel level;
    public static final class Lift {
        public final int type,column,row;
        public float x,y,previousX,previousY,wait=2,clock;
        public int direction=1,phase;
        Lift(OriginalLevel.Marker m){type=m.type;column=m.x;row=m.y;x=column*64+32;y=type==1?(row+1)*64-1:(row+3)*64;previousX=x;previousY=y;}
        public float floor(){return y+(type==1?32:0);}
        public int halfWidth(){return type==1?48:32;}
        public boolean supports(float left,float feet){return left+29>x-halfWidth()&&left+2<x+halfWidth()&&Math.abs(feet-floor())<4;}
    }
    public LiftWorld(OriginalLevel map){level=map;for(OriginalLevel.Marker m:map.markers)if(m.type==1||m.type==2)lifts.add(new Lift(m));}
    public void update(float dt){
        if(Float.isNaN(dt)||Float.isInfinite(dt)||dt<0||dt>1)throw new IllegalArgumentException("Invalid lift interval");
        for(Lift l:lifts){
            l.previousX=l.x;l.previousY=l.y;l.clock+=dt;
            while(l.clock>=.01f){l.clock-=.01f;if(l.wait>0){l.wait=Math.max(0,l.wait-.01f);continue;}
                int step=l.direction==0?-1:1;boolean allowed;
                if(l.type==1){
                    int c=(int)((l.x+step+(l.direction==0?46:35)*step)/64),r=(int)(l.y/64);
                    int[] cell=level.cell(c,r);allowed=cell!=null&&(cell[5]&0x4000)!=0;
                    if(allowed)l.x+=step;
                }else{
                    int[] cell=level.cell((int)(l.x/64),(int)((l.y+step+(l.direction==0?-128:1))/64));
                    allowed=cell!=null&&cell[0]==0&&(cell[5]&0x20)==0;
                    if(allowed){l.y+=step;l.phase=(l.phase+(step<0?1:3))&3;}
                }
                if(!allowed){l.direction^=1;l.wait=2;}
            }
        }
    }
}
