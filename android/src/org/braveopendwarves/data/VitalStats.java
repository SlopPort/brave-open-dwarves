package org.braveopendwarves.data;
/** Original actor health table at EXE49ebd8. */
public final class VitalStats {
    private static final int[] HEALTH={0,20,5,10,50,75,15,25,1000,500,1000,750,800};
    private VitalStats(){}
    public static int health(int type){return type>0&&type<HEALTH.length?HEALTH[type]:0;}
    public static int legacyHealth(int type){return type==10?40:type==9?20:type==5?4:2;}
}
