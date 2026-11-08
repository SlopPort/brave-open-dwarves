import org.braveopendwarves.data.*;
public final class BossTest {
    static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    public static void main(String[] args)throws Exception{
        CombatWorld w=new CombatWorld(CombatTest.map());CombatWorld.Foe boss=new CombatWorld.Foe(9,240,216);boss.direction=-1;boss.attackCooldown=0;w.foes.add(boss);w.immunity=100;
        w.update(.01f,50,206);check(boss.attackTime>1,"boss starts windup in range");float position=boss.x;
        for(int i=0;i<40;i++)w.update(.01f,50,206);check(boss.x==position,"boss holds position while attacking");
        check(!w.bolts.isEmpty()&&w.bolts.get(0).hostile,"boss fires during attack");
        check(w.bolts.get(0).kind==25&&Math.abs(w.bolts.get(0).speed)==750&&w.bolts.get(0).life<512f/750,"boss projectile type speed and range budget");
        boss.hp=1;w.cooldown=0;check(w.attack(boss.x-50,boss.y,1,false)&&boss.hp<=0&&w.kills==1,"boss can be defeated by melee");
        ExitDoor door=new ExitDoor(CombatTest.map());door.unlock();check(door.progress==0,"boss unlock begins door animation");
        try(GogArchive archive=new GogArchive(new java.io.File("Gnomes.dat"))){OriginalLevel level=new OriginalLevel(archive.read("Data/Levels/Level0010.dat"));w=new CombatWorld(level);check(w.foes.size()==1&&w.foes.get(0).type==9,"original map 10 includes boss");}
        w=new CombatWorld(CombatTest.map());boss=new CombatWorld.Foe(10,240,138);boss.attackCooldown=0;w.foes.add(boss);w.immunity=100;
        w.update(.001f,450,206);check(boss.attackTime>1&&boss.direction==1,"mino starts independent radial attack");
        for(int i=0;i<26;i++)w.update(.01f,450,206);
        check(w.bolts.size()==8,"mino fires eight rays");boolean above=false,below=false;
        for(CombatWorld.Bolt shot:w.bolts){check(shot.kind==27&&Math.abs(Math.hypot(shot.speed,shot.verticalSpeed)-300)<.01,"radial velocity magnitude");above|=shot.verticalSpeed<0;below|=shot.verticalSpeed>0;}
        check(above&&below,"rays spread vertically");
        w=new CombatWorld(CombatTest.map());boss=new CombatWorld.Foe(8,200,134);boss.attackCooldown=0;w.foes.add(boss);w.immunity=100;
        w.update(.001f,450,206);check(boss.attackTime>0&&boss.attackTime<.4f,"spider short windup");for(int i=0;i<29;i++)w.update(1f/75,450,206);check(!w.bolts.isEmpty()&&w.bolts.get(0).kind==26,"spider fires web");
        w=new CombatWorld(CombatTest.map());CombatWorld.Bolt web=new CombatWorld.Bolt(116,231,0,true,26);w.bolts.add(web);w.update(.01f,100,206);check(w.health==20&&w.webbed>1.3f,"web binds without health damage");check(!w.attack(100,206,1,true),"bound hero cannot attack");w.update(1.4f,100,206);check(w.webbed==0,"web bind expires");
        try(GogArchive archive=new GogArchive(new java.io.File("Gnomes.dat"))){OriginalLevel level=new OriginalLevel(archive.read("Data/Levels/Level0020.dat"));w=new CombatWorld(level);boolean found=false;for(CombatWorld.Foe foe:w.foes)found|=foe.type==10;check(found,"original map 20 includes mino");}
        try(GogArchive archive=new GogArchive(new java.io.File("Gnomes.dat"))){OriginalLevel level=new OriginalLevel(archive.read("Data/Levels/Level0040.dat"));w=new CombatWorld(level);check(w.foes.size()==1&&w.foes.get(0).type==11,"original map 40 includes fire elemental");boss=w.foes.get(0);check(boss.hp==750&&boss.width()==80&&boss.height()==80,"fire elemental health and frame bounds");AnmAnimations anm=new AnmAnimations(archive.read("Data/Creatures/fire_elemental.anm"),640,240);check(anm.sample("WalkRight",5)!=null&&anm.sample("FallLeft",5)!=null&&anm.sample("Dead",0)!=null,"fire elemental sequences resolve");}
        w=new CombatWorld(CombatTest.map());boss=new CombatWorld.Foe(11,240,176);boss.attackCooldown=0;w.foes.add(boss);w.immunity=100;
        w.update(.001f,30,206);check(boss.attackTime>1,"fire elemental starts cast");position=boss.x;
        for(int i=0;i<18;i++)w.update(1f/75,30,206);check(w.bolts.isEmpty(),"fire elemental waits nineteen ticks before firing");
        for(int i=0;i<2;i++)w.update(1f/75,30,206);check(w.bolts.size()==3&&boss.x==position&&boss.direction==-1,"fire elemental casts three shots while facing hero and holding position");
        for(int i=0;i<3;i++){CombatWorld.Bolt shot=w.bolts.get(i);check(shot.kind==9&&shot.variant==i&&shot.damage==2,"fire elemental original shot type variant and half damage");check(Math.abs(shot.speed-new float[]{0,300,420}[i])<.01&&Math.abs(shot.verticalSpeed-new float[]{-420,-300,0}[i])<.01,"fire elemental fixed world direction vectors");}
        w.bolts.clear();boss.attackTime=.001f;w.update(.002f,30,206);check(boss.attackTime==0&&boss.attackCooldown>=100f/75&&boss.attackCooldown<500f/75,"fire elemental randomized cast delay");
        OriginalLevel destination=CombatTest.map();for(int col=0;col<destination.width;col++)destination.cell(col,4)[0]=0;destination.cell(5,3)[0]=1;
        w=new CombatWorld(destination);boss=new CombatWorld.Foe(11,50,100);boss.teleportPending=true;w.foes.add(boss);w.immunity=100;
        w.update(.001f,20,20);check(boss.center()==5*64+32&&boss.y+boss.height()==2*64+63,"teleport uses original center and feet coordinates");check(boss.teleportCooldown>=100f/75&&boss.teleportCooldown<1300f/75&&Math.abs(boss.attackCooldown-4f/75)<.0001f,"teleport resets original delays");
        w=new CombatWorld(CombatTest.map());boss=new CombatWorld.Foe(11,240,176);boss.teleportCooldown=.001f;boss.attackTime=.5f;w.foes.add(boss);w.immunity=100;w.update(.002f,20,20);check(!boss.teleportPending&&boss.teleportCooldown==.001f,"teleport countdown waits during cast");
        boss.attackTime=0;w.update(.002f,20,20);check(boss.teleportPending,"expired patrol countdown enters pending teleport");position=boss.x;w.update(.001f,20,20);check(!boss.teleportPending&&boss.x==position,"map without eligible interior floor terminates teleport safely");
        try(GogArchive archive=new GogArchive(new java.io.File("Gnomes.dat"))){OriginalLevel level=new OriginalLevel(archive.read("Data/Levels/Level0050.dat"));w=new CombatWorld(level);check(w.foes.size()==9&&w.foes.get(0).type==12,"map 50 includes lich and eight bats");boss=w.foes.get(0);check(boss.hp==800&&boss.width()==92&&boss.height()==105,"lich health and geometry");AnmAnimations anm=new AnmAnimations(archive.read("Data/Creatures/lich.anm"),552,210);check(anm.sample("CastRight",0)!=null&&anm.sample("CastLeft",0)!=null,"lich cast animations resolve");}
        java.lang.reflect.Field rng=CombatWorld.class.getDeclaredField("random");rng.setAccessible(true);java.util.Random random=(java.util.Random)rng.get(null);
        int[] seeds={0,2,3},counts={8,3,4},kinds={27,9,14},damage={3,2,15};
        for(int spell=0;spell<3;spell++){
            w=new CombatWorld(CombatTest.map());w.viewportX=100;w.viewportY=20;w.viewportWidth=200;w.immunity=100;boss=new CombatWorld.Foe(12,240,151);boss.attackTime=60f/75+.001f;w.foes.add(boss);random.setSeed(seeds[spell]);
            w.update(.002f,20,206);check(w.bolts.size()==counts[spell]&&boss.direction==-1,"lich spell count and facing");
            for(CombatWorld.Bolt shot:w.bolts){check(shot.kind==kinds[spell]&&shot.damage==damage[spell],"lich original spell type and damage");if(spell==2)check(shot.speed==0&&shot.verticalSpeed>=525&&shot.verticalSpeed<=750&&shot.x>=110&&shot.x<290&&Math.abs(shot.y-20-shot.verticalSpeed*.002f)<.001f,"icicle viewport placement and fall speed");}
        }
        w=new CombatWorld(destination);boss=new CombatWorld.Foe(12,50,100);boss.teleportPending=true;w.foes.add(boss);w.immunity=100;w.update(.001f,20,20);check(boss.center()==352&&boss.y+105==191,"lich shares original teleport floor placement");
        for(int kind:new int[]{9,22,5,20,24,1}){
            w=new CombatWorld(CombatTest.map());boss=new CombatWorld.Foe(11,240,176);boss.teleportCooldown=10;w.foes.add(boss);w.immunity=100;CombatWorld.Bolt shot=new CombatWorld.Bolt(280,216,0,false,kind);w.bolts.add(shot);w.update(0,0,0);
            int expected=kind==24?730:kind==1?740:750;check(boss.hp==expected&&boss.teleportCooldown==10,"fire elemental projectile immunity and vulnerability without hit teleport");
        }
        w=new CombatWorld(CombatTest.map());boss=new CombatWorld.Foe(12,240,151);boss.teleportCooldown=10;w.foes.add(boss);w.immunity=100;w.bolts.add(new CombatWorld.Bolt(286,200,0,false,1));w.update(0,0,0);check(boss.hp==790&&Math.abs(boss.teleportCooldown-5f/75)<.0001f,"lich damage primes five-tick teleport");
        boss.teleportCooldown=2f/75;w.bolts.add(new CombatWorld.Bolt(286,200,0,false,1));w.update(0,0,0);check(Math.abs(boss.teleportCooldown-2f/75)<.0001f,"further hits do not postpone an earlier teleport");
        System.out.println("Boss tests passed");
    }
}
