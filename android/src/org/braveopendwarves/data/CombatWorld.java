package org.braveopendwarves.data;
import java.util.*;

/** First playable combat model; not a reconstruction of original EXE behavior. */
public final class CombatWorld {
    private static final Random random=new Random();
    /** Makes headless runs repeatable. */
    public static void seed(long value){random.setSeed(value);}
    private static float fireDelay(){return OriginalClock.seconds(100+random.nextInt(400));}
    public static final class Foe {
        public float x,y,vy,turnCooldown,attackCooldown,attackTime,teleportCooldown;
        public boolean teleportPending;
        public int direction=1,hp=2;
        public final int type;
        public int height(){return type==12?105:type==11?80:type==8?122:type==10?118:type==9?40:type==2?20:type==3?32:type==5?60:type==6?24:50;}
        public int width(){return type==12?92:type==11?80:type==8?186:type==10?90:type==9?160:32;}
        public float center(){return x+width()/2f;}
        public int maxHealth(){return VitalStats.health(type);}
        public Foe(int type,float x,float y){this.type=type;this.x=x;this.y=y;hp=maxHealth();attackCooldown=(type==11||type==12)?fireDelay():.8f;if(type==11||type==12)teleportCooldown=OriginalClock.seconds(100+random.nextInt(200));}
    }
    public static final class Bolt {
        public float x,y,life=1.5f;
        public float verticalSpeed,age;
        public int damage,variant;
        public int width(){return kind==6?((((int)(age*OriginalClock.HZ)/4)&1)==0?63:64):kind==5?(speed>0&&((int)(age*OriginalClock.HZ)/4)%3==2?57:58):kind==8?62:kind==14?15:kind==9?(variant==0?12:variant==1?25:30):kind==26?120:kind==3?30:kind==27?38:65;}
        public int height(){return kind==6?(speed<0&&(((int)(age*OriginalClock.HZ)/4)&1)==1?16:17):kind==5?56:kind==8?30:kind==14?36:kind==9?(variant==0?30:variant==1?23:12):kind==26?106:kind==3?12:kind==27?((((int)(age*OriginalClock.HZ)/16)&1)==0?31:33):34;}
        public final float speed;
        public final boolean hostile;
        public final int kind;
        Bolt(float x,float y,float speed){this(x,y,speed,false);}
        Bolt(float x,float y,float speed,boolean hostile){this(x,y,speed,hostile,0);}
        public Bolt(float x,float y,float speed,boolean hostile,int kind){this.x=x;this.y=y;this.speed=speed;this.hostile=hostile;this.kind=kind;damage=!hostile?(kind==6?7:kind==5?13:kind==8?12:10):kind==26?1:kind==25?5:kind==27?3:kind==9?2:kind==14?15:4;if(!hostile)life=(kind==5||kind==8?384f:256f)/OriginalClock.speed(8);if(!hostile&&kind==6)life=448f/OriginalClock.speed(10);if(kind==3||kind==25)life=512f/OriginalClock.speed(10);if(kind==14)life=640f/OriginalClock.speed(7);if(kind==26)life=1280f/OriginalClock.speed(10);}
    }
    public final ArrayList<Foe> foes=new ArrayList<Foe>();
    public final ArrayList<Bolt> bolts=new ArrayList<Bolt>();
    public int health=20,kills;
    public final int[] energy={25,0,0,0};
    public final int[] abilityLevels={1,0,0,0};
    public int selectedSpell;
    public void collectPowerOrb(int type){int channel=ItemRules.powerOrbChannel(type);if(channel>=0)abilityLevels[channel]=(abilityLevels[channel]+1)&255;}
    /** Spell id is channel+tier*4; costs from 0x49e400. */
    public static final int SPELL_COUNT=12;
    public static boolean spellImplemented(int id){return id>=0&&id<5||id==8;}
    private static final int[] SPELL_COST={1,2,2,1,5,3,5,5,7,3,7,7};
    /** Unlock rule from 0x417768: channel level must reach tier+1. */
    public boolean spellUnlocked(int id){return spellImplemented(id)&&abilityLevels[id%4]>=id/4+1;}
    public void cycleSpell(){for(int step=1;step<=SPELL_COUNT;step++){int next=(selectedSpell+step)%SPELL_COUNT;if(spellUnlocked(next)){selectedSpell=next;return;}}}

    public float energyClock;
    public void regenerateEnergy(float dt,boolean wizard){
        if(health<=0)return;
        energyClock+=dt*OriginalClock.HZ;
        while(energyClock+.0001f>=128){energyClock=Math.max(0,energyClock-128);int channel=wizard?0:1;if(energy[channel]<25)energy[channel]++;}
    }
    public void resetChapterEnergy(int levelNumber,boolean wizard){
        if(levelNumber>0&&levelNumber%10==1){Arrays.fill(energy,0);Arrays.fill(abilityLevels,0);selectedSpell=wizard?0:1;energy[selectedSpell]=25;abilityLevels[selectedSpell]=1;}
    }
    public void replenishEnergy(int itemType){for(int i=0;i<4;i++)energy[i]=Math.min(100,energy[i]+ItemRules.energyGain(itemType,i));}
    public float immunity,cooldown,swing,webbed;
    public float viewportX,viewportY,viewportWidth=960;
    private final OriginalLevel level;
    private boolean clear(float x,float y,float endX,float endY){
        int steps=Math.max(1,(int)(Math.max(Math.abs(endX-x),Math.abs(endY-y))/8)+1);
        for(int i=0;i<=steps;i++){
            float t=i/(float)steps;
            if(level.solid((int)Math.floor((x+(endX-x)*t)/64),(int)Math.floor((y+(endY-y)*t)/64)))return false;
        }
        return true;
    }
    public CombatWorld(OriginalLevel level){
        this.level=level;
        for(OriginalLevel.Enemy e:level.enemies)if(e.type>=2&&e.type<=6||e.type==8||e.type==9||e.type==10||e.type==11||e.type==12){Foe f=new Foe(e.type,e.x,e.y);f.x-=f.width()/2;f.y-=f.height();f.direction=e.flags==6?-1:1;if(e.type==10)f.attackCooldown=3;foes.add(f);}
    }
    private void teleport(Foe f){
        // Rejection sampling in 0x407c80 is uniform over these eligible cells.
        // Reservoir selection also terminates on custom maps with no valid floor.
        int count=0,targetX=0,targetY=0;
        for(int row=2;row<level.height-1;row++)for(int col=1;col<level.width-1;col++)
            if(level.cell(col,row)[0]==1&&level.cell(col,row-1)[0]==0&&random.nextInt(++count)==0){targetX=col;targetY=row;}
        if(count>0){f.x=targetX*64+32-f.width()/2f;f.y=(targetY-1)*64+63-f.height();f.vy=0;}
        f.teleportPending=false;f.teleportCooldown=OriginalClock.seconds(100+random.nextInt(1200));
        // State12 sets five, then calls the cast helper once before returning to patrol.
        f.attackCooldown=OriginalClock.seconds(4);
    }
    private void fireMissiles(Foe f){
        // Original indices 0,1,2 remain world directions when facing left.
        float[] horizontal={0,1,1.4f},vertical={-1.4f,-1,0};
        for(int ray=0;ray<3;ray++){
            Bolt shot=new Bolt(f.center(),f.y+f.height()-25,horizontal[ray]*OriginalClock.speed(4),true,9);
            shot.variant=ray;shot.verticalSpeed=vertical[ray]*OriginalClock.speed(4);shot.life=512f/OriginalClock.speed(4);bolts.add(shot);
        }
    }
    private void radialSkulls(Foe f){
        for(int ray=0;ray<8;ray++){
            double angle=Math.toRadians(12+45*ray);Bolt shot=new Bolt(f.center()+f.direction*f.width()/2f,f.y+f.height()-15,(float)Math.cos(angle)*OriginalClock.speed(4),true,27);
            shot.verticalSpeed=(float)Math.sin(angle)*OriginalClock.speed(4);shot.life=512f/OriginalClock.speed(4);bolts.add(shot);
        }
    }
    private void hitFoe(Foe f,int damage,int kind){
        if(f.hp<=0||damage<0)return;
        // Fire elemental hit handler 0x407a80 rejects four projectile types.
        if(f.type==11&&(kind==9||kind==22||kind==5||kind==20))return;
        if(f.type==11&&kind==24)damage*=2;
        f.hp-=damage;
        // Only the lich shortens its teleport delay when hit (0x40846f).
        if(f.type==12)f.teleportCooldown=Math.min(f.teleportCooldown,OriginalClock.seconds(5));
        if(f.hp<=0)kills++;
    }
    public boolean attack(float x,float y,int direction,boolean wizard){
        int cost=selectedSpell>=0&&selectedSpell<SPELL_COST.length?SPELL_COST[selectedSpell]:0;
        if(cooldown>0||health<=0||webbed>0||(wizard&&(!spellUnlocked(selectedSpell)||energy[selectedSpell%4]<cost)))return false;
        if(wizard)energy[selectedSpell%4]-=cost;
        cooldown=wizard?OriginalClock.seconds(selectedSpell==3?6:selectedSpell==2?19:25):OriginalClock.seconds(10);
        if(wizard&&selectedSpell==8){for(int i=0;i<7;i++){Bolt b=new Bolt(viewportX+10+random.nextInt(Math.max(1,(int)viewportWidth-20)),viewportY,0,false,14);b.damage=15;b.verticalSpeed=OriginalClock.speed(7+random.nextInt(4));bolts.add(b);}return true;}
        if(wizard&&selectedSpell==4){float origin=x+16+direction*35,shotY=y+24;if(clear(x+16,y+24,origin,shotY)){Bolt b=new Bolt(origin,shotY,direction*OriginalClock.speed(6),false,10);b.damage=1;b.life=1920f/OriginalClock.speed(6);bolts.add(b);}return true;}
        if(wizard){float origin=x+16+direction*24,shotY=selectedSpell==3?y+30-random.nextInt(20):y+22;if(clear(x+16,y+22,origin,shotY))bolts.add(new Bolt(origin,shotY,direction*OriginalClock.speed(selectedSpell==3?10:8),false,selectedSpell==0?1:selectedSpell==1?5:selectedSpell==2?8:6));}
        else{
            swing=.16f;
            for(Foe f:foes)if(f.hp>0&&Math.abs(f.y+f.height()/2-y-25)<45&&(f.center()-x-16)*direction>=-f.width()/2&&(f.center()-x-16)*direction-f.width()/2<86&&clear(x+16,y+22,f.center(),f.y+f.height()/2)){hitFoe(f,35,0);}
        }
        return true;
    }
    public void update(float dt,float heroX,float heroY){
        if(health<=0)return;
        webbed=Math.max(0,webbed-dt);immunity=Math.max(0,immunity-dt);cooldown=Math.max(0,cooldown-dt);swing=Math.max(0,swing-dt);
        for(Foe f:foes)if(f.hp>0){
            if((f.type==11||f.type==12)&&f.teleportPending){teleport(f);continue;}
            f.turnCooldown=Math.max(0,f.turnCooldown-dt);
            int height=f.height();int front=(int)Math.floor((f.x+(f.direction>0?f.width()+2:-2))/64),feet=(int)Math.floor((f.y+height+1)/64);
            boolean grounded=level.solid((int)(f.center()/64),feet);
            if((f.type==11||f.type==12)&&f.attackTime<=0&&grounded){
                f.teleportCooldown=Math.max(0,f.teleportCooldown-dt);
                if(f.teleportCooldown==0){f.teleportPending=true;continue;}
            }
            boolean wall=level.solid(front,(int)((f.y+height/2)/64));
            boolean ledge=f.type!=3&&grounded&&!level.solid(front,feet);
            if((wall||ledge)&&f.turnCooldown<=0&&f.attackTime<=0){f.direction=-f.direction;f.turnCooldown=.15f;}
            else if(!wall&&!ledge&&f.attackTime<=0)f.x+=f.direction*(f.type==8?OriginalClock.speed(4):f.type==9||f.type==10||f.type==11||f.type==12?OriginalClock.speed(3):55)*dt;
            if(f.type!=3){
                f.vy+=1000*dt;float ny=f.y+f.vy*dt;
                int row=(int)((ny+height)/64);
                if(f.vy>=0&&(level.solid((int)((f.x+3)/64),row)||level.solid((int)((f.x+f.width()-3)/64),row))){ny=row*64-height;f.vy=0;}
                f.y=ny;
            }
            if(immunity<=0&&Math.abs(f.center()-heroX-16)<f.width()/2+13&&Math.abs(f.y+height/2-heroY-25)<height/2+24&&clear(heroX+16,heroY+22,f.center(),f.y+height/2)){health=Math.max(0,health-5);immunity=OriginalClock.seconds(170);}
            f.attackCooldown-=dt;
            if(f.type==8){
                if(f.attackTime>0){float before=f.attackTime;f.attackTime=Math.max(0,f.attackTime-dt);if(before>OriginalClock.seconds(1)&&f.attackTime<=OriginalClock.seconds(1))bolts.add(new Bolt(f.center()+f.direction*93,f.y+height-15,f.direction*OriginalClock.speed(10),true,26));if(f.attackTime==0)f.attackCooldown=OriginalClock.seconds(10);}
                else{float distance=(heroX+16-f.center())*f.direction;if(webbed<=0&&f.attackCooldown<=0&&distance>=32&&distance<384&&Math.abs(heroY+25-f.y-height/2)<90&&clear(f.center(),f.y+height/2,heroX+16,heroY+25))f.attackTime=OriginalClock.seconds(29);}
            }
            if(f.type==9){
                if(f.attackTime>0){float before=f.attackTime;f.attackTime=Math.max(0,f.attackTime-dt);if(before>OriginalClock.seconds(60)&&f.attackTime<=OriginalClock.seconds(60))bolts.add(new Bolt(f.center()+f.direction*85,f.y+height-15,f.direction*OriginalClock.speed(10),true,25));if(f.attackTime==0)f.attackCooldown=1;}
                else{float distance=(heroX+16-f.center())*f.direction;if(f.attackCooldown<=0&&distance>=128&&distance<384&&Math.abs(heroY+25-f.y-height/2)<70&&clear(f.center(),f.y+20,heroX+16,heroY+25))f.attackTime=OriginalClock.seconds(79);}
            }
            if(f.type==10){
                if(f.attackTime>0){float before=f.attackTime;f.attackTime=Math.max(0,f.attackTime-dt);
                    if(before>OriginalClock.seconds(60)&&f.attackTime<=OriginalClock.seconds(60))for(int ray=0;ray<8;ray++){
                        double angle=Math.toRadians(12+45*ray);Bolt shot=new Bolt(f.center()+f.direction*45,f.y+height-15,(float)Math.cos(angle)*OriginalClock.speed(4),true,27);shot.verticalSpeed=(float)Math.sin(angle)*OriginalClock.speed(4);shot.life=512f/OriginalClock.speed(4);bolts.add(shot);
                    }
                    if(f.attackTime==0)f.attackCooldown=3;
                }else if(f.attackCooldown<=0){f.direction=heroX+16>=f.center()?1:-1;f.attackTime=OriginalClock.seconds(79);}
            }
            if(f.type==11){
                if(f.attackTime>0){
                    f.direction=heroX+16>=f.center()?1:-1;
                    float before=f.attackTime;f.attackTime=Math.max(0,f.attackTime-dt);
                    if(before>OriginalClock.seconds(60)&&f.attackTime<=OriginalClock.seconds(60))fireMissiles(f);
                    if(f.attackTime==0)f.attackCooldown=fireDelay();
                }else if(f.attackCooldown<=0)f.attackTime=OriginalClock.seconds(79);
            }
            if(f.type==12){
                if(f.attackTime>0){
                    f.direction=heroX+16>=f.center()?1:-1;
                    float before=f.attackTime;f.attackTime=Math.max(0,f.attackTime-dt);
                    if(before>OriginalClock.seconds(60)&&f.attackTime<=OriginalClock.seconds(60)){
                        int spell=random.nextInt(3);
                        if(spell==0)radialSkulls(f);
                        else if(spell==1)fireMissiles(f);
                        else for(int ray=0;ray<4;ray++){
                            Bolt shot=new Bolt(viewportX+10+random.nextInt(Math.max(1,(int)viewportWidth-20)),viewportY,0,true,14);
                            shot.verticalSpeed=OriginalClock.speed(7+random.nextInt(4));bolts.add(shot);
                        }
                    }
                    if(f.attackTime==0)f.attackCooldown=fireDelay();
                }else if(f.attackCooldown<=0)f.attackTime=OriginalClock.seconds(79);
            }
            if(f.type==6&&f.attackCooldown<=0&&Math.abs(f.x-heroX)<450&&Math.abs(f.y+height/2-heroY-25)<45&&clear(f.x+16,f.y+height/2,heroX+16,heroY+25)){
                int dir=heroX>=f.x?1:-1;bolts.add(new Bolt(f.x+16+dir*18,f.y+height/2,dir*OriginalClock.speed(10),true,3));f.attackCooldown=1.6f;
            }
        }
        for(Bolt b:bolts){
            b.life-=dt;b.age+=dt;if(b.kind==26&&b.damage!=0)b.verticalSpeed+=.5f*OriginalClock.HZ*OriginalClock.HZ*dt;float previous=b.x,previousY=b.y;b.x+=b.speed*dt;b.y+=b.verticalSpeed*dt;
            if(!clear(previous,previousY,b.x,b.y))b.life=0;
        }
        for(int i=0;i<bolts.size();i++)for(int j=i+1;j<bolts.size();j++){
            Bolt a=bolts.get(i),b=bolts.get(j);
            if(a.life<=0||b.life<=0||a.hostile==b.hostile)continue;
            if(Math.abs(a.x-b.x)<(a.width()+b.width())/2f&&Math.abs(a.y-b.y)<(a.height()+b.height())/2f){
                int first=a.damage,second=b.damage;a.damage-=second;b.damage-=first;
                if(a.damage<0)a.life=0;if(b.damage<0)b.life=0;
            }
        }
        for(int i=bolts.size()-1;i>=0;i--){
            Bolt b=bolts.get(i);
            if(b.hostile&&b.life>0&&Math.abs(b.x-heroX-16)<22&&Math.abs(b.y-heroY-25)<28){
                if(immunity<=0&&health>0&&b.kind==26){webbed=OriginalClock.seconds(100);b.damage=0;}
                if(immunity<=0&&health>0&&b.damage>0){health=Math.max(0,health-b.damage);immunity=OriginalClock.seconds(170);}b.life=0;
            }
            if(!b.hostile&&b.life>0)for(Foe f:foes)if(f.hp>0&&Math.abs(f.center()-b.x)<f.width()/2+7&&Math.abs(f.y+f.height()/2-b.y)<f.height()/2+8){
                hitFoe(f,b.damage,b.kind);b.life=0;break;
            }
            if(b.life<=0)bolts.remove(i);
        }
    }
    public void heal(){health=Math.min(20,health+1);}
}
