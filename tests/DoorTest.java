import org.braveopendwarves.data.*;
public final class DoorTest {
    static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    public static void main(String[] args)throws Exception{
        ExitDoor d=new ExitDoor(CombatTest.map());
        check(!d.update(.01f,3,32,60,true)&&d.progress<0,"three keys required");
        check(!d.update(.01f,7,128,60,true)&&d.progress<0,"right edge excluded");
        check(!d.update(.01f,7,32,60,true)&&d.started&&d.progress==0,"starts in door bounds");
        check(d.tile(0x800,0)==40&&d.tile(0x3800,0)==51,"opening tiles and quadrants");
        for(int i=0;i<100;i++)d.update(.01f,7,32,60,false);check(Math.abs(d.progress-75)<.001f,"nominal original clock advances 75 ticks per second");
        for(int i=0;i<250;i++)d.update(.01f,7,32,60,false);
        check(d.progress==149,"opening finishes");
        check(!d.update(.01f,7,32,60,false),"airborne hero cannot exit");
        check(d.update(.01f,7,32,60,true),"standing hero exits open door");
        check(d.tile(0x800,0)==24&&d.tile(0x3800,.1f)==53,"open shimmer");
        System.out.println("Door tests passed");
    }
}
