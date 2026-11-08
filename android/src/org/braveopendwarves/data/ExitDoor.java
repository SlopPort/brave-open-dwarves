package org.braveopendwarves.data;

/** Door state and tile table recovered at 416fc0/41f411; tick rate provisional. */
public final class ExitDoor {
    private static final int[] OPENING={40,2,40,2,40,2,40,2,40,2,40,2,40,2,4,6,8,20,22,24};
    private final OriginalLevel level;
    public float progress=-1; // -1 closed, 0..149 opening, 149 open
    public boolean started;
    public void unlock(){if(progress<0){progress=0;started=true;}}
    public ExitDoor(OriginalLevel map){level=map;}
    public boolean inside(float centerX,float feet){return centerX>=level.exitX*64&&centerX<(level.exitX+2)*64&&feet>=level.exitY*64&&feet<(level.exitY+2)*64;}
    public boolean update(float dt,int keys,float centerX,float feet,boolean standing){
        started=false;
        if(progress<0){if(keys==7&&inside(centerX,feet)){progress=0;started=true;}return false;}
        if(progress<149){progress=Math.min(149,progress+dt*OriginalClock.HZ);return false;}
        return standing&&inside(centerX,feet);
    }
    public int tile(int flags,float time){
        int base=progress<0?0:progress>=149?24+18*((int)(time*OriginalClock.HZ/4)&1):OPENING[Math.min(19,(int)(progress*20/150))];
        int quadrant=(flags>>12)&3;return base+(quadrant&1)+(quadrant>>1)*10;
    }
}
