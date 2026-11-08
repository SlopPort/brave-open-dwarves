package org.braveopendwarves.data;
import java.io.*;
import java.security.*;
import java.util.*;

/** Private engine checkpoint, bound to the exact original level bytes. */
public final class RunState {
    public String levelName,digest;
    public float x,y;
    public float doorProgress=-1;
    public float immunity,webbed,energyClock;
    public float[] environment={0,0,128,0};
    public int[] energy={25,0,0,0};
    public int[] abilityLevels={1,0,0,0};
    public int selectedSpell;
    public int bankedScore;
    public boolean completed;
    private boolean legacyHealth;
    public int health,keys,treasure,kills,facing;
    public boolean wizard;
    public boolean[] taken;
    public float[] foeX,foeY;
    public int[] foeHp,foeDirection;
    public float[][] foeMotion;
    public float[][] liftState=new float[0][];
    public void captureFoes(CombatWorld world){
        int n=world.foes.size();foeX=new float[n];foeY=new float[n];foeHp=new int[n];foeDirection=new int[n];foeMotion=new float[n][6];
        for(int i=0;i<n;i++){
            CombatWorld.Foe f=world.foes.get(i);foeX[i]=f.x;foeY[i]=f.y;foeHp[i]=Math.max(0,f.hp);foeDirection[i]=f.direction;
            foeMotion[i]=new float[]{f.vy,f.turnCooldown,Math.max(0,f.attackCooldown),f.attackTime,f.teleportCooldown,f.teleportPending?1:0};
        }
    }
    public void restoreFoes(CombatWorld world){
        for(int i=0;i<foeX.length;i++){
            CombatWorld.Foe f=world.foes.get(i);f.x=foeX[i];f.y=foeY[i];f.hp=foeHp[i];f.direction=foeDirection[i];
            if(foeMotion!=null){float[] m=foeMotion[i];f.vy=m[0];f.turnCooldown=m[1];f.attackCooldown=m[2];f.attackTime=m[3];f.teleportCooldown=m[4];f.teleportPending=m[5]==1;}
        }
    }
    public void captureLifts(LiftWorld world){
        liftState=new float[world.lifts.size()][7];
        for(int i=0;i<liftState.length;i++){LiftWorld.Lift l=world.lifts.get(i);liftState[i]=new float[]{l.x,l.y,l.wait,l.clock,l.direction,l.phase,l.type};}
    }
    public void restoreLifts(LiftWorld world)throws IOException{
        if(liftState.length!=world.lifts.size())throw new IOException("Zapis nie zawiera stanu wind; rozpocznij mape ponownie");
        for(int i=0;i<liftState.length;i++){
            float[] s=liftState[i];LiftWorld.Lift l=world.lifts.get(i);
            for(float v:s)if(Float.isNaN(v)||Float.isInfinite(v))throw new IOException("Nieprawidlowy stan windy");
            if(s[0]<0||s[1]<0||s[0]>512*64||s[1]>512*64||s[2]<0||s[2]>2.01f||s[3]<0||s[3]>=.011f||(s[4]!=0&&s[4]!=1)||s[5]<0||s[5]>3||s[5]!=(int)s[5]||s[6]!=l.type)throw new IOException("Nieprawidlowy stan windy");
        }
        for(int i=0;i<liftState.length;i++){float[] s=liftState[i];LiftWorld.Lift l=world.lifts.get(i);l.x=l.previousX=s[0];l.y=l.previousY=s[1];l.wait=s[2];l.clock=s[3];l.direction=(int)s[4];l.phase=(int)s[5];}
    }
    public static String digest(byte[] bytes){
        try{StringBuilder b=new StringBuilder();for(byte v:MessageDigest.getInstance("SHA-256").digest(bytes))b.append(String.format(Locale.ROOT,"%02x",v&255));return b.toString();}
        catch(NoSuchAlgorithmException e){throw new AssertionError(e);}
    }
    public void write(File target)throws IOException{
        File temp=new File(target.getParentFile(),target.getName()+".tmp");
        try(DataOutputStream out=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(temp)))){
            out.writeInt(0x424f4431);out.writeInt(12);out.writeUTF(levelName);out.writeUTF(digest);
            out.writeFloat(x);out.writeFloat(y);out.writeInt(health);out.writeInt(keys);out.writeInt(treasure);out.writeInt(kills);out.writeInt(facing);out.writeBoolean(wizard);
            out.writeInt(taken.length);for(boolean t:taken)out.writeBoolean(t);
            out.writeInt(foeX.length);for(int i=0;i<foeX.length;i++){out.writeFloat(foeX[i]);out.writeFloat(foeY[i]);out.writeInt(foeHp[i]);out.writeInt(foeDirection[i]);}
            out.writeInt(liftState.length);for(float[] state:liftState)for(float v:state)out.writeFloat(v);
            out.writeFloat(doorProgress);
            for(float v:environment)out.writeFloat(v);
            out.writeInt(bankedScore);out.writeBoolean(completed);out.writeFloat(immunity);out.writeFloat(webbed);
            for(int i=0;i<foeX.length;i++)for(float v:foeMotion==null?new float[6]:foeMotion[i])out.writeFloat(v);
            for(int value:energy)out.writeInt(value);out.writeFloat(energyClock);for(int level:abilityLevels)out.writeByte(level);out.writeInt(selectedSpell);
        }
        File backup=new File(target.getPath()+".bak");
        if(target.exists()){
            if(backup.exists()&&!backup.delete())throw new IOException("Nie mozna odswiezyc kopii zapisu");
            if(!target.renameTo(backup))throw new IOException("Nie mozna zachowac poprzedniego zapisu");
        }
        if(!temp.renameTo(target)){
            if(backup.exists()&&!backup.renameTo(target))throw new IOException("Poprzedni zapis pozostaje w kopii .bak");
            throw new IOException("Nie mozna zapisac punktu kontrolnego");
        }
        if(backup.exists())backup.delete();
    }
    public static boolean exists(File source){return source.isFile()||new File(source.getPath()+".bak").isFile();}
    public static RunState read(File source)throws IOException{
        if(!source.isFile())source=new File(source.getPath()+".bak");
        if(source.length()>1000000)throw new IOException("Punkt kontrolny jest za duzy");
        try(DataInputStream in=new DataInputStream(new BufferedInputStream(new FileInputStream(source)))){
            if(in.readInt()!=0x424f4431)throw new IOException("Nieobslugiwany zapis");
            int version=in.readInt();if(version<1||version>12)throw new IOException("Nieobslugiwany zapis");
            RunState s=new RunState();s.legacyHealth=version<6;s.levelName=in.readUTF();s.digest=in.readUTF();s.x=in.readFloat();s.y=in.readFloat();s.health=in.readInt();s.keys=in.readInt();s.treasure=in.readInt();s.kills=in.readInt();s.facing=in.readInt();s.wizard=in.readBoolean();
            int n=in.readInt();if(n<0||n>20000)throw new IOException("Bledny licznik przedmiotow");s.taken=new boolean[n];for(int i=0;i<n;i++)s.taken[i]=in.readBoolean();
            n=in.readInt();if(n<0||n>20000)throw new IOException("Bledny licznik przeciwnikow");s.foeX=new float[n];s.foeY=new float[n];s.foeHp=new int[n];s.foeDirection=new int[n];
            for(int i=0;i<n;i++){s.foeX[i]=in.readFloat();s.foeY[i]=in.readFloat();s.foeHp[i]=in.readInt();s.foeDirection[i]=in.readInt();}
            if(version>=2){n=in.readInt();if(n<0||n>20000)throw new IOException("Bledny licznik wind");s.liftState=new float[n][7];for(float[] state:s.liftState)for(int i=0;i<7;i++)state[i]=in.readFloat();}
            if(version>=3)s.doorProgress=in.readFloat();
            if(version>=4)for(int i=0;i<4;i++)s.environment[i]=in.readFloat();
            if(version>=5){s.bankedScore=in.readInt();s.completed=in.readBoolean();}
            if(version>=7)s.immunity=in.readFloat();if(version>=8)s.webbed=in.readFloat();
            if(version>=9){s.foeMotion=new float[s.foeX.length][6];for(float[] motion:s.foeMotion)for(int i=0;i<6;i++)motion[i]=in.readFloat();}
            if(version>=10)for(int i=0;i<4;i++)s.energy[i]=in.readInt();
            if(version>=11)s.energyClock=in.readFloat();
            if(version>=12){for(int i=0;i<4;i++)s.abilityLevels[i]=in.readUnsignedByte();s.selectedSpell=in.readInt();}
            else if(!s.wizard){s.abilityLevels=new int[]{0,1,0,0};s.selectedSpell=1;}
            if(in.read()!=-1)throw new IOException("Nadmiarowe dane zapisu");return s;
        }
    }
    private static boolean coordinate(float x,float y,OriginalLevel level){return !Float.isNaN(x)&&!Float.isInfinite(x)&&!Float.isNaN(y)&&!Float.isInfinite(y)&&x>=0&&x<=level.width*64-32&&y>=-105&&y<=level.height*64;}
    public void validate(String expectedName,String expectedDigest,OriginalLevel level,CombatWorld combat)throws IOException{
        if(legacyHealth){
            if(health<1||health>5||Float.isNaN(environment[3])||Float.isInfinite(environment[3])||environment[3]<0||environment[3]>3||environment[3]!=(int)environment[3])throw new IOException("Nieprawidlowe stare zdrowie");
            health=health*4-(int)environment[3];environment[3]=0;
            if(foeHp.length==combat.foes.size())for(int i=0;i<foeHp.length;i++){int type=combat.foes.get(i).type;if(foeHp[i]<0||foeHp[i]>VitalStats.legacyHealth(type))throw new IOException("Nieprawidlowe stare zdrowie przeciwnika");foeHp[i]=(int)Math.ceil(foeHp[i]*(double)VitalStats.health(type)/VitalStats.legacyHealth(type));}
            legacyHealth=false;
        }
        if(Float.isNaN(energyClock)||Float.isInfinite(energyClock)||energyClock<0||energyClock>=128)throw new IOException("Nieprawidlowy licznik energii");
        if(abilityLevels==null||abilityLevels.length!=4||!CombatWorld.spellImplemented(selectedSpell))throw new IOException("Nieprawidlowy wybor czaru");
        for(int levelValue:abilityLevels)if(levelValue<0||levelValue>255)throw new IOException("Nieprawidlowy poziom umiejetnosci");
        if(abilityLevels[selectedSpell%4]<selectedSpell/4+1)throw new IOException("Zapis wybral zablokowany czar");
        if(energy==null||energy.length!=4)throw new IOException("Nieprawidlowa energia");
        for(int value:energy)if(value<0||value>100)throw new IOException("Nieprawidlowa energia");
        if(Float.isNaN(webbed)||Float.isInfinite(webbed)||webbed<0||webbed>OriginalClock.seconds(100)+.001f)throw new IOException("Nieprawidlowy czas pajeczyny");
        if(Float.isNaN(immunity)||Float.isInfinite(immunity)||immunity<0||immunity>OriginalClock.seconds(500)+.001f)throw new IOException("Nieprawidlowy czas ochrony");
        if(bankedScore<0||bankedScore>100000000)throw new IOException("Nieprawidlowy wynik kampanii");
        if(Float.isNaN(doorProgress)||Float.isInfinite(doorProgress)||doorProgress < -1||doorProgress>149)throw new IOException("Nieprawidlowy stan drzwi");
        if(!expectedName.equals(levelName)||!expectedDigest.equals(digest))throw new IOException("Zapis pochodzi z innej mapy lub wersji danych");
        if(!coordinate(x,y,level)||y<0||health<1||health>20||keys<0||keys>7||treasure<0||treasure>level.items.length||kills<0||kills>combat.foes.size()||(facing!=1&&facing!=-1)||taken.length!=level.items.length||foeX.length!=combat.foes.size())throw new IOException("Nieprawidlowy stan zapisu");
        if(foeMotion!=null){
            if(foeMotion.length!=foeX.length)throw new IOException("Nieprawidlowy licznik stanow przeciwnikow");
            for(int i=0;i<foeMotion.length;i++){
                float[] m=foeMotion[i];if(m==null||m.length!=6)throw new IOException("Nieprawidlowy stan ruchu przeciwnika");
                for(float v:m)if(Float.isNaN(v)||Float.isInfinite(v))throw new IOException("Nieprawidlowy licznik przeciwnika");
                if(Math.abs(m[0])>100000||m[1]<0||m[1]>.151f||m[2]<0||m[2]>OriginalClock.seconds(500)+.001f||m[3]<0||m[3]>OriginalClock.seconds(79)+.001f||m[4]<0||m[4]>OriginalClock.seconds(1300)+.001f||(m[5]!=0&&m[5]!=1)||(combat.foes.get(i).type!=11&&combat.foes.get(i).type!=12&&(m[4]!=0||m[5]!=0)))throw new IOException("Nieprawidlowy stan ruchu przeciwnika");
            }
        }
        for(int i=0;i<foeX.length;i++)if(!coordinate(foeX[i],foeY[i],level)||foeHp[i]<0||foeHp[i]>combat.foes.get(i).maxHealth()||(foeDirection[i]!=1&&foeDirection[i]!=-1))throw new IOException("Nieprawidlowy stan przeciwnika");
    }
}
