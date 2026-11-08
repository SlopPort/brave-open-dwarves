package org.braveopendwarves.data;

/** Gnome1/Gnome2_axe sheet layout: 80px cells, four columns per direction.
 * State selection is adapted to the port's provisional movement model. */
public final class HeroFrames {
    private static final int[] WALK={0,1,2,3},CLIMB={0,1,2,1};
    private HeroFrames(){}
    public static int frame(int facing,boolean moving,boolean grounded,boolean climbing,boolean climbingMoving,float verticalSpeed,float time){
        int phase=((int)(Math.max(0,time)*OriginalClock.HZ/8))&3;
        if(climbing)return 16+CLIMB[climbingMoving?phase:0];
        int side=facing<0?4:0;
        if(!grounded)return side+(verticalSpeed<0?(facing<0?2:1):(facing<0?1:2));
        if(moving)return side+(facing<0?3-WALK[phase]:WALK[phase]);
        return facing<0?7:0;
    }
}
