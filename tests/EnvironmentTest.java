import org.braveopendwarves.data.*;
public final class EnvironmentTest {
    static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    public static void main(String[] args)throws Exception{
        OriginalLevel map=CombatTest.map();CombatWorld combat=new CombatWorld(map);EnvironmentWorld env=new EnvironmentWorld(map);
        map.cell(1,2)[5]=0x400;combat.immunity=100;env.update(.01f,64,128,combat);check(combat.health==0,"lethal flag bypasses immunity");
        combat=new CombatWorld(map);map.cell(1,2)[5]=0x80;env=new EnvironmentWorld(map);
        for(int i=0;i<128;i++)env.update(1f/75,64,128,combat);
        check(env.water&&env.interval==120&&env.remainder==0&&combat.health==19,"first underwater interval");
        for(int i=0;i<120+112+104;i++)env.update(1f/75,64,128,combat);
        check(combat.health==16&&env.remainder==0,"four underwater intervals remove four health points");
        float[] state=env.snapshot();EnvironmentWorld restored=new EnvironmentWorld(map);restored.restore(state);check(restored.interval==96,"breath state restored");
        env.update(.01f,128,128,combat);check(!env.water&&env.interval==128&&env.counter==0,"air resets breath interval");
        for(int i=0;i<3000;i++)restored.update(1f/75,64,128,combat);check(combat.health==0,"continued immersion drowns");
        state[0]=Float.NaN;boolean rejected=false;try{env.restore(state);}catch(java.io.IOException e){rejected=true;}check(rejected,"reject nonfinite breath");
        System.out.println("Environment tests passed");
    }
}
