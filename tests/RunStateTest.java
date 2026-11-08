import org.braveopendwarves.data.*;
import java.io.*;
import java.nio.file.*;
public final class RunStateTest {
    interface Probe{void run()throws Exception;}
    static void reject(Probe p)throws Exception{try{p.run();}catch(IOException expected){return;}throw new AssertionError("Invalid state accepted");}
    public static void main(String[] args)throws Exception{
        OriginalLevel level=CombatTest.map();CombatWorld combat=new CombatWorld(level);combat.foes.add(new CombatWorld.Foe(4,200,206));
        RunState s=new RunState();s.levelName="Data/Levels/Level0001.dat";s.digest=RunState.digest(new byte[]{1,2,3});s.x=100;s.y=206;s.health=3;s.keys=2;s.facing=-1;s.wizard=true;
        s.taken=new boolean[0];s.foeX=new float[]{200};s.foeY=new float[]{206};s.foeHp=new int[]{1};s.foeDirection=new int[]{-1};
        s.validate(s.levelName,s.digest,level,combat);
        File file=File.createTempFile("bod-checkpoint-",".tmp",new File("build"));
        try{
            s.write(file);RunState read=RunState.read(file);read.validate(s.levelName,s.digest,level,combat);
            if(read.x!=100||read.health!=3||read.facing!=-1||read.foeHp[0]!=1||!read.wizard)throw new AssertionError("Round trip");
            s.health=4;s.immunity=3.25f;s.doorProgress=70;s.bankedScore=1234;s.completed=true;s.write(file);if(RunState.read(file).health!=4||RunState.read(file).doorProgress!=70||RunState.read(file).bankedScore!=1234||!RunState.read(file).completed||RunState.read(file).immunity!=3.25f)throw new AssertionError("Replace existing save and campaign state");
            File backup=new File(file.getPath()+".bak");if(!file.renameTo(backup))throw new AssertionError("Backup fixture");
            if(!RunState.exists(file)||RunState.read(file).health!=4)throw new AssertionError("Interrupted save recovery");
            if(!backup.renameTo(file))throw new AssertionError("Restore fixture");
            reject(()->read.validate(s.levelName,"different",level,combat));
            read.x=Float.NaN;reject(()->read.validate(s.levelName,s.digest,level,combat));read.x=100;
            read.foeX[0]=99999;reject(()->read.validate(s.levelName,s.digest,level,combat));
            byte[] modern=Files.readAllBytes(file.toPath());byte[] latest=java.util.Arrays.copyOf(modern,modern.length-8);latest[7]=11;Files.write(file.toPath(),latest);if(RunState.read(file).selectedSpell!=0)throw new AssertionError("Version11 ability defaults");byte[] newest=java.util.Arrays.copyOf(latest,latest.length-4);newest[7]=10;Files.write(file.toPath(),newest);if(RunState.read(file).energyClock!=0)throw new AssertionError("Version10 regeneration clock default");byte[] current=java.util.Arrays.copyOf(newest,newest.length-16);current[7]=9;Files.write(file.toPath(),current);RunState v9state=RunState.read(file);if(v9state.energy[0]!=25)throw new AssertionError("Version9 energy default");byte[] complete=java.util.Arrays.copyOf(current,current.length-24);complete[7]=8;Files.write(file.toPath(),complete);RunState v8state=RunState.read(file);if(v8state.foeMotion!=null)throw new AssertionError("Version 8 keeps legacy enemy defaults");byte[] bytes=java.util.Arrays.copyOf(complete,complete.length-8);
            byte[] v7=java.util.Arrays.copyOf(complete,complete.length-4);v7[7]=7;Files.write(file.toPath(),v7);if(RunState.read(file).webbed!=0)throw new AssertionError("Version 7 web default");
            byte[] v6=bytes.clone();v6[7]=6;Files.write(file.toPath(),v6);if(RunState.read(file).immunity!=0)throw new AssertionError("Version 6 protection default");
            byte[] v5=bytes.clone();v5[7]=5;java.nio.ByteBuffer.wrap(v5).putFloat(v5.length-9,2);Files.write(file.toPath(),v5);RunState upgraded=RunState.read(file);upgraded.validate(s.levelName,s.digest,level,combat);if(upgraded.health!=14||upgraded.foeHp[0]!=25||upgraded.environment[3]!=0)throw new AssertionError("Version 5 health scale migration");
            upgraded.validate(s.levelName,s.digest,level,combat);if(upgraded.health!=14)throw new AssertionError("Migration only once");
            byte[] v4=java.util.Arrays.copyOf(bytes,bytes.length-5);v4[7]=4;Files.write(file.toPath(),v4);RunState v4state=RunState.read(file);if(v4state.bankedScore!=0||v4state.completed)throw new AssertionError("Version 4 campaign defaults");
            byte[] v3=java.util.Arrays.copyOf(bytes,bytes.length-21);v3[7]=3;Files.write(file.toPath(),v3);RunState v3state=RunState.read(file);if(v3state.environment[2]!=128||v3state.doorProgress!=70)throw new AssertionError("Version 3 environment default");
            byte[] legacy=java.util.Arrays.copyOf(bytes,bytes.length-25);legacy[7]=2;Files.write(file.toPath(),legacy);RunState older=RunState.read(file);older.validate(s.levelName,s.digest,level,combat);if(older.doorProgress!=-1)throw new AssertionError("Version 2 door default");
            legacy=java.util.Arrays.copyOf(bytes,bytes.length-29);legacy[7]=1;Files.write(file.toPath(),legacy);older=RunState.read(file);older.validate(s.levelName,s.digest,level,combat);older.restoreLifts(new LiftWorld(level));
            Files.write(file.toPath(),java.util.Arrays.copyOf(bytes,bytes.length-1));reject(()->RunState.read(file));
            Files.write(file.toPath(),new byte[16]);reject(()->RunState.read(file));
            s.foeMotion=new float[][]{{125,.1f,1.5f,.8f,0,0}};s.write(file);RunState motion=RunState.read(file);motion.validate(s.levelName,s.digest,level,combat);motion.restoreFoes(combat);CombatWorld.Foe foe=combat.foes.get(0);if(foe.vy!=125||foe.turnCooldown!=.1f||foe.attackCooldown!=1.5f||foe.attackTime!=.8f)throw new AssertionError("Enemy motion and attack round trip");
            motion.foeMotion[0][3]=Float.NaN;reject(()->motion.validate(s.levelName,s.digest,level,combat));motion.foeMotion[0][3]=2;reject(()->motion.validate(s.levelName,s.digest,level,combat));motion.foeMotion[0][3]=.8f;motion.foeMotion[0][5]=1;reject(()->motion.validate(s.levelName,s.digest,level,combat));
            s.energyClock=62.5f;s.energy=new int[]{12,34,56,78};s.write(file);RunState energized=RunState.read(file);if(energized.energyClock!=62.5f||!java.util.Arrays.equals(energized.energy,s.energy))throw new AssertionError("Energy round trip");energized.energy[2]=101;reject(()->energized.validate(s.levelName,s.digest,level,combat));energized.energy[2]=-1;reject(()->energized.validate(s.levelName,s.digest,level,combat));
            energized.energy[2]=56;energized.energyClock=128;reject(()->energized.validate(s.levelName,s.digest,level,combat));energized.energyClock=Float.NaN;reject(()->energized.validate(s.levelName,s.digest,level,combat));
            s.abilityLevels=new int[]{2,1,1,0};s.selectedSpell=2;s.write(file);RunState skilled=RunState.read(file);skilled.validate(s.levelName,s.digest,level,combat);if(skilled.selectedSpell!=2||!java.util.Arrays.equals(skilled.abilityLevels,s.abilityLevels))throw new AssertionError("Spell and ability round trip");skilled.selectedSpell=4;reject(()->skilled.validate(s.levelName,s.digest,level,combat));skilled.selectedSpell=2;skilled.abilityLevels[2]=0;reject(()->skilled.validate(s.levelName,s.digest,level,combat));
            s.abilityLevels[3]=1;s.selectedSpell=3;s.write(file);RunState fourth=RunState.read(file);fourth.validate(s.levelName,s.digest,level,combat);if(fourth.selectedSpell!=3)throw new AssertionError("Fourth spell checkpoint");
            s.health=0;reject(()->s.validate(s.levelName,s.digest,level,combat));
            s.health=3;s.doorProgress=Float.NaN;reject(()->s.validate(s.levelName,s.digest,level,combat));
        }finally{file.delete();new File(file.getPath()+".tmp").delete();}
        System.out.println("PASS checkpoint: round trip, data identity, nonfinite/out-of-bounds, truncation, version, death");
    }
}



