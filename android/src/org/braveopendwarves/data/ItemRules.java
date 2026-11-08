package org.braveopendwarves.data;

/** Scores from EXE 49f258; effects from dispatch 4162b0.
 * Healing uses original health units. */
public final class ItemRules {
    private ItemRules(){}
    private static final int[] SCORE={0,70,75,80,85,90,95,50,60,300,300,300,500,600,700,800,900,900,1000,15,20,0,0,0,0,0,0,30,1000,1100,1200,1300,1400,1500,200,200,5,10,15,1,10,150,150,150,10,10,10,150,25,10,10,50,400,450,500,50,50,50,50,700,70,0,0,0,0,0,0,500,600};
    public static int score(int type){return type>0&&type<SCORE.length?SCORE[type]:0;}
    public static boolean supported(int type){
        return type>=1&&type<=8||type>=12&&type<=19||type>=21&&type<=33||type>=35&&type<=50||type>=55&&type<=68;
    }
    public static int powerOrbChannel(int type){return type>=55&&type<=58?type-55:-1;}
    public static int energyGain(int type,int channel){
        if(channel<0||channel>3)return 0;
        if(type==6&&channel==0||type==3&&channel==1||type==1&&channel==2||type==5&&channel==3)return 5;
        if(type==4&&(channel==0||channel==2)||type==2&&(channel==1||channel==3))return 3;
        return 0;
    }
    public static int healing(int type){return type==40?20:type==44?5:type==45?10:type==46?2:0;}
    public static int protectionTicks(int type){return type==40?500:0;}
    public static boolean chest(int type){return type==7||type==8||type==59||type==60||type==67||type==68;}
    public static int score(OriginalLevel level){int total=0;for(OriginalLevel.Item item:level.items)if(item.taken)total+=score(item.type);return total;}
}
