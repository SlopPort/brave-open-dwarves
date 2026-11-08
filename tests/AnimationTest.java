import org.braveopendwarves.data.*;
import java.io.*;
public final class AnimationTest {
    public static void main(String[] args)throws Exception{
        String source="*ALL_FRAMES\n{\n*FRAME first 0,0,10,10\n*FRAME second 10,0,10,10\n}\n*ANIMATION walk\n{\n*FRAME first,2\n*FRAME second,3\n*LOOP\n}\n*ANIMATION death\n{\n*FRAME second\n}\n";
        AnmAnimations a=new AnmAnimations(source.getBytes("US-ASCII"),20,10);
        if(!a.sample("Walk",0).name.equals("first")||!a.sample("walk",2).name.equals("second")||!a.sample("walk",5).name.equals("first")||!a.sample("death",999).name.equals("second"))throw new AssertionError("Timing and loop");
        if(a.sample("unknown",0)!=null||a.sample("walk",Float.NaN)!=null)throw new AssertionError("Unknown animation/time");
        try{new AnmAnimations(source.replace("second 10,0,10,10","second 10,0,99,10").getBytes("US-ASCII"),20,10);throw new AssertionError("Bounds");}catch(IOException expected){}
        a=new AnmAnimations(source.replace("*FRAME second,3","*FRAME absent,3").getBytes("US-ASCII"),20,10);
        if(a.warnings().size()!=1||a.sample("walk",2)!=null)throw new AssertionError("Unresolved original reference remains explicit");
        System.out.println("PASS ANM: frame geometry, ticks, loops, clamping, invalid bounds, missing references");
    }
}
