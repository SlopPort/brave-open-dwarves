package org.braveopendwarves;
import android.content.Context;
import android.graphics.*;
import android.view.*;
import org.braveopendwarves.data.*;
import java.io.*;
import java.util.*;

/** Original maps with experimental combat; original EXE behavior is not yet reconstructed. */
public final class OriginalGameView extends View {
    final GogArchive archive;
    final MainActivity activity;
    final Paint paint=new Paint(3);
    final ArrayList<String> levels=new ArrayList<String>();
    final HashMap<String,Bitmap> images=new HashMap<String,Bitmap>();
    OriginalLevel level;
    Bitmap blocks,animate,items,mage,warrior,skeleton,worm,bat,minoskel,crawler,shots;
    SpriteFrames.Frame boltRight,boltLeft;
    SpriteFrames.Frame enemyBoltRight,enemyBoltLeft;
    SpriteFrames.Frame bossBoltRight,bossBoltLeft;
    int selected,keys,treasure,facing=1;boolean wizard=true,ground,paused,background,overview;
    boolean left,right,up,down,jump,fire,climbing;
    CombatWorld combat;
    LiftWorld lifts;
    ExitDoor door;
    EnvironmentWorld environment;
    int bankedScore;
    boolean completed;
    Bitmap bossWorm;
    AnmAnimations bossWormAnimations;
    Bitmap bossMino;
    AnmAnimations bossMinoAnimations;
    SpriteFrames.Frame[] skullRight,skullLeft,fireMissiles,fireballRight,fireballLeft,lightning,earthRight,earthLeft;
    Bitmap lich;AnmAnimations lichAnimations;SpriteFrames.Frame icicle;
    Bitmap fireElemental;AnmAnimations fireElementalAnimations;
    SpriteFrames.Frame webFrame;Bitmap bossSpider;AnmAnimations bossSpiderAnimations;
    int totalScore(){return bankedScore+ItemRules.score(level);}
    void advance(){
        int number=Integer.parseInt(levels.get(selected).replaceAll("[^0-9]",""));
        if(number==50||number>=1000){completed=true;paused=true;resetInput();save();return;}
        String nextName=String.format(Locale.ROOT,"Data/Levels/Level%04d.dat",number+1);int next=levels.indexOf(nextName);
        if(next<0){error="Brak nastepnego poziomu";paused=true;return;}
        int score=totalScore(),health=combat.health;int[] energy=combat.energy.clone(),abilities=combat.abilityLevels.clone();int spell=combat.selectedSpell;boolean character=wizard;load(next);
        if(error==null){bankedScore=score;combat.health=health;System.arraycopy(energy,0,combat.energy,0,4);System.arraycopy(abilities,0,combat.abilityLevels,0,4);combat.selectedSpell=spell;wizard=character;combat.resetChapterEnergy(number+1,wizard);save();}
    }
    float x,y,vy,camX,camY,time,scale=1,ox,oy;long last;
    String error;
    String levelDigest;
    OriginalGameView(MainActivity owner,GogArchive data) throws IOException {
        super(owner);activity=owner;archive=data;
        setFocusableInTouchMode(true);requestFocus();paint.setFilterBitmap(false);
        for(GogArchive.Entry entry:data.entries())if(entry.name.toLowerCase(Locale.ROOT).matches("data/levels/level[0-9]{4}\\.dat"))levels.add(entry.name);
        Collections.sort(levels);if(levels.isEmpty())throw new IOException("Archiwum nie zawiera poziomow");
        items=bitmap("Data/Items.pcx");mage=bitmap("Data/Creatures/Gnome1.pcx");warrior=bitmap("Data/Creatures/Gnome2_axe.pcx");skeleton=bitmap("Data/Creatures/SKEL.PCX");
        shots=bitmap("Data/Shots.pcx");SpriteFrames frames=new SpriteFrames(archive.read("Data/SHOTS.FRM"),shots.getWidth(),shots.getHeight());
        worm=bitmap("Data/Creatures/WORM.PCX");bat=bitmap("Data/Creatures/Bat.pcx");
        minoskel=bitmap("Data/Creatures/Minoskel.pcx");crawler=bitmap("Data/Creatures/Crawler.pcx");
        bossWorm=bitmap("Data/Creatures/boss_worm.pcx");bossWormAnimations=new AnmAnimations(archive.read("Data/Creatures/boss_worm.anm"),bossWorm.getWidth(),bossWorm.getHeight());
        lich=bitmap("Data/Creatures/lich.pcx");lichAnimations=new AnmAnimations(archive.read("Data/Creatures/lich.anm"),lich.getWidth(),lich.getHeight());icicle=frames.get("icicle");
        fireElemental=bitmap("Data/Creatures/fire_elemental.pcx");fireElementalAnimations=new AnmAnimations(archive.read("Data/Creatures/fire_elemental.anm"),fireElemental.getWidth(),fireElemental.getHeight());
        bossSpider=bitmap("Data/Creatures/boss_spider.pcx");bossSpiderAnimations=new AnmAnimations(archive.read("Data/Creatures/boss_spider.anm"),bossSpider.getWidth(),bossSpider.getHeight());webFrame=frames.get("Web");
        bossMino=bitmap("Data/Creatures/boss_mino.pcx");bossMinoAnimations=new AnmAnimations(archive.read("Data/Creatures/boss_mino.anm"),bossMino.getWidth(),bossMino.getHeight());
        skullRight=new SpriteFrames.Frame[]{frames.get("skull_r1"),frames.get("skull_r2")};skullLeft=new SpriteFrames.Frame[]{frames.get("skull_l1"),frames.get("skull_l2")};
        earthRight=new SpriteFrames.Frame[]{frames.get("bte_right"),frames.get("bte_r2")};earthLeft=new SpriteFrames.Frame[]{frames.get("bte_left"),frames.get("bte_l2")};
        fireballRight=new SpriteFrames.Frame[]{frames.get("fbr_1"),frames.get("fbr_2"),frames.get("fbr_3")};fireballLeft=new SpriteFrames.Frame[]{frames.get("fbl_1"),frames.get("fbl_2"),frames.get("fbl_3")};lightning=new SpriteFrames.Frame[]{frames.get("lightning6_1"),frames.get("lightning6_2"),frames.get("lightning6_3")};
        fireMissiles=new SpriteFrames.Frame[]{frames.get("mis_8"),frames.get("mis_9"),frames.get("mis_6")};
        enemyBoltRight=frames.get("crawler_right");enemyBoltLeft=frames.get("crawler_left");
        bossBoltRight=frames.get("zelman_r");bossBoltLeft=frames.get("zelman_l");
        boltRight=frames.get("bt_right");boltLeft=frames.get("bt_left");load(0);if(level==null)throw new IOException(error);
    }
    Bitmap bitmap(String name) throws IOException {
        Bitmap cached=images.get(name);if(cached!=null)return cached;
        PcxImage image=PcxImage.decode(archive.read(name),true);
        Bitmap result=Bitmap.createBitmap(image.argb,image.width,image.height,Bitmap.Config.ARGB_8888);images.put(name,result);return result;
    }
    void load(int index){
        try{
            byte[] bytes=archive.read(levels.get(index));OriginalLevel next=new OriginalLevel(bytes);
            Bitmap nextBlocks=bitmap("Data/Blocks"+next.theme+".pcx"),nextAnimate=bitmap("Data/Animate"+next.theme+".pcx");
            level=next;levelDigest=RunState.digest(bytes);combat=new CombatWorld(next);lifts=new LiftWorld(next);blocks=nextBlocks;animate=nextAnimate;selected=index;
            bankedScore=0;completed=false;environment=new EnvironmentWorld(next);door=new ExitDoor(next);climbing=false;x=level.spawnX*64+16;y=level.spawnY*64;vy=0;keys=treasure=0;paused=false;error=null;resetInput();last=0;
        }catch(IOException e){error=e.getMessage();paused=true;}
        invalidate();
    }
    void resetInput(){left=right=up=down=jump=fire=false;}
    void suspend(){save();background=true;paused=true;last=0;resetInput();}
    void resume(){background=false;last=0;invalidate();}
    void togglePause(){paused=!paused;resetInput();invalidate();}
    void save(){
        if(level==null||combat.health<=0)return;
        RunState state=new RunState();state.levelName=levels.get(selected);state.digest=levelDigest;state.x=x;state.y=y;state.health=combat.health;state.keys=keys;state.treasure=treasure;state.kills=combat.kills;state.facing=facing;state.wizard=wizard;
        state.taken=new boolean[level.items.length];for(int i=0;i<state.taken.length;i++)state.taken[i]=level.items[i].taken;
        state.abilityLevels=combat.abilityLevels.clone();state.selectedSpell=combat.selectedSpell;
        state.energyClock=combat.energyClock;
        state.energy=combat.energy.clone();
        state.captureFoes(combat);
        state.captureLifts(lifts);
        state.doorProgress=door.progress;
        state.environment=environment.snapshot();state.immunity=combat.immunity;state.webbed=combat.webbed;
        state.bankedScore=bankedScore;state.completed=completed;
        try{state.write(new File(activity.getFilesDir(),"run.bod"));}catch(IOException e){error="Nie udalo sie zapisac: "+e.getMessage();}
    }
    void restore()throws IOException{
        RunState state=RunState.read(new File(activity.getFilesDir(),"run.bod"));int index=levels.indexOf(state.levelName);if(index<0)throw new IOException("Brak zapisanej mapy");
        load(index);if(error!=null)throw new IOException(error);state.validate(levels.get(selected),levelDigest,level,combat);state.restoreLifts(lifts);
        x=state.x;y=state.y;vy=0;facing=state.facing;wizard=state.wizard;keys=state.keys;treasure=state.treasure;combat.health=state.health;combat.kills=state.kills;combat.immunity=state.immunity;combat.webbed=state.webbed;
        door.progress=state.doorProgress;
        environment.restore(state.environment);
        bankedScore=state.bankedScore;completed=state.completed;
        climbing=level.ladder((int)(x+16)/64,(int)(y+25)/64);
        for(int i=0;i<state.taken.length;i++)level.items[i].taken=state.taken[i];
        state.restoreFoes(combat);
        System.arraycopy(state.energy,0,combat.energy,0,4);combat.energyClock=state.energyClock;System.arraycopy(state.abilityLevels,0,combat.abilityLevels,0,4);combat.selectedSpell=state.selectedSpell;
        paused=true;last=0;
    }
    boolean solid(int c,int r,boolean climb){return level.solid(c,r)&&!(climb&&level.ladder(c,r));}
    void update(float dt){
        if(paused||overview||completed||level==null)return;time+=dt;
        if(combat.health<=0){resetInput();return;}
        LiftWorld.Lift passenger=null;
        if(vy>=0)for(LiftWorld.Lift lift:lifts.lifts)if(lift.supports(x,y+50)){passenger=lift;break;}
        lifts.update(dt);
        if(passenger!=null){
            float carriedX=x+passenger.x-passenger.previousX,carriedY=y+passenger.y-passenger.previousY;
            // Stop carrying at a ceiling or wall; full original crush handling is still unknown.
            if(!level.solid((int)(carriedX+2)/64,(int)(carriedY+2)/64)&&!level.solid((int)(carriedX+29)/64,(int)(carriedY+2)/64)){x=carriedX;y=carriedY;ground=true;}
        }
        if(combat.webbed<=0){
        boolean onLadder=level.ladder((int)(x+16)/64,(int)(y+25)/64)||level.ladder((int)(x+16)/64,(int)(y+51)/64);
        boolean climb=onLadder&&(up||down||climbing);climbing=climb;
        float vx=(left?-180:0)+(right?180:0);if(vx!=0)facing=vx>0?1:-1;
        if(jump&&(ground||climb)){vy=-450;ground=false;climb=climbing=false;}jump=false;
        float nx=x+vx*dt;
        if(vx>0){int c=(int)((nx+31)/64);for(int r=(int)(y+2)/64;r<=(int)(y+49)/64;r++)if(solid(c,r,climb)){nx=c*64-32;break;}}
        if(vx<0){int c=(int)Math.floor(nx/64);for(int r=(int)(y+2)/64;r<=(int)(y+49)/64;r++)if(solid(c,r,climb)){nx=(c+1)*64;break;}}
        x=Math.max(0,Math.min(level.width*64-32,nx));
        if(climb){vy=(down?125:0)-(up?125:0);}else vy+=1000*dt;
        float ny=y+vy*dt;ground=false;
        if(vy>=0){int r=(int)((ny+50)/64);for(int c=(int)(x+2)/64;c<=(int)(x+29)/64;c++)if(solid(c,r,climb)){ny=r*64-50;vy=0;ground=true;break;}}
        else{int r=(int)Math.floor(ny/64);for(int c=(int)(x+2)/64;c<=(int)(x+29)/64;c++)if(solid(c,r,climb)){ny=(r+1)*64;vy=0;break;}}
        if(vy>=0)for(LiftWorld.Lift lift:lifts.lifts){
            float floor=lift.floor();
            if(x+29>lift.x-lift.halfWidth()&&x+2<lift.x+lift.halfWidth()&&y+50<=floor+3&&ny+50>=floor&&floor-50<=ny){ny=floor-50;vy=0;ground=true;}
        }
        y=Math.max(0,Math.min(level.height*64-50,ny));
        }else{vy=0;jump=false;}
        combat.regenerateEnergy(dt,wizard);
        if(fire)combat.attack(x,y,facing,wizard);
        combat.viewportX=Math.max(0,Math.min(level.width*64-960,x-450));combat.viewportY=Math.max(0,Math.min(level.height*64-420,y-180));combat.viewportWidth=960;
        combat.update(dt,x,y);
        environment.update(dt,x,y,combat);
        if(combat.health<=0){resetInput();return;}
        for(OriginalLevel.Item item:level.items)if(!item.taken&&Math.abs(item.x-x-16)<25&&Math.abs(item.y-y-25)<35){
            if(ItemRules.supported(item.type)){
                item.taken=true;treasure++;if(item.type>=41&&item.type<=43)keys|=1<<(item.type-41);
                combat.collectPowerOrb(item.type);
                combat.replenishEnergy(item.type);
                combat.health=Math.min(20,combat.health+ItemRules.healing(item.type));
                if(ItemRules.healing(item.type)>0)environment.remainder=0;
                if(ItemRules.protectionTicks(item.type)>0)combat.immunity=Math.max(combat.immunity,OriginalClock.seconds(ItemRules.protectionTicks(item.type)));
                activity.playItem(item.type);
            }
        }
        boolean bossPresent=false,bossAlive=false;for(OriginalLevel.Enemy enemy:level.enemies)if(enemy.type==8||enemy.type==9||enemy.type==10||enemy.type==11||enemy.type==12)bossPresent=true;for(CombatWorld.Foe foe:combat.foes)if(foe.hp>0)bossAlive=true;
        if(bossPresent&&!bossAlive&&door.progress<0){door.unlock();activity.playDoor();}
        if(door.update(dt,keys,x+16,y+49,ground)){advance();}
        else if(door.started)activity.playDoor();
    }
    void txt(Canvas c,String s,float x,float y,int size,int color){paint.setColor(color);paint.setTextSize(size);c.drawText(s,x,y,paint);}
    void fill(Canvas c,float x,float y,float w,float h,int color){paint.setColor(color);c.drawRect(x,y,x+w,y+h,paint);}
    void button(Canvas c,String s,float x,float y,float w,boolean active){paint.setColor(active?0xff427d80:0xff273d50);c.drawRoundRect(x,y,x+w,y+60,10,10,paint);txt(c,s,x+12,y+38,19,0xfff3eddb);}
    void tile(Canvas c,Bitmap image,int index,int size,float x,float y){
        if(index<0||index>=(image.getWidth()/size)*(image.getHeight()/size))return;
        int col=image.getWidth()/size;paint.setColor(Color.WHITE);
        c.drawBitmap(image,new Rect(index%col*size,index/col*size,index%col*size+size,index/col*size+size),new RectF(x,y,x+size,y+size),paint);
    }
    void world(Canvas c){
        int firstX=overview?0:Math.max(0,(int)camX/64),lastX=overview?level.width:Math.min(level.width,(int)(camX+960)/64+2);
        int firstY=overview?0:Math.max(0,(int)camY/64),lastY=overview?level.height:Math.min(level.height,(int)(camY+420)/64+2);
        for(int r=firstY;r<lastY;r++)for(int col=firstX;col<lastX;col++){
            int[] cell=level.cell(col,r);
            if(cell[1]!=0)tile(c,blocks,cell[1],64,col*64,r*64);
            if(cell[2]!=0)tile(c,blocks,cell[2],64,col*64,r*64);
            if(cell[3]!=0)tile(c,animate,cell[3],64,col*64,r*64);
            if((cell[5]&0x800)!=0)tile(c,animate,door.tile(cell[5],time),64,col*64,r*64);
        }
        drawLifts(c,false);
        for(OriginalLevel.Item item:level.items)if(!item.taken&&item.type>=1&&item.type<=100)tile(c,items,item.type-1,32,item.x-16,item.y-16);
        for(CombatWorld.Foe enemy:combat.foes)if(enemy.hp>0||enemy.type==8||enemy.type==9||enemy.type==10||enemy.type==11||enemy.type==12){
            if(enemy.type==8||enemy.type==9||enemy.type==10||enemy.type==11||enemy.type==12){
                String sequence=(enemy.hp<=0?"Dead":enemy.attackTime>0?(enemy.type==10?"Kick":"Attack"):(enemy.type==10&&enemy.vy>1?"Fall":"Walk"))+(enemy.direction>0?"Right":"Left");
                if(enemy.type==10&&enemy.hp<=0)sequence="Dead";
                if(enemy.type==8)sequence=enemy.hp<=0?"DeadSpider":(enemy.attackTime>0?"Shot":"Walk")+(enemy.direction>0?"Right":"Left");
                if(enemy.type==11)sequence=enemy.hp<=0?"Dead":(enemy.attackTime>0?"Cast":enemy.vy>1?"Fall":"Walk")+(enemy.direction>0?"Right":"Left");
                if(enemy.type==12)sequence=enemy.hp<=0?"Dead":(enemy.attackTime>0?"Cast":"Walk")+(enemy.direction>0?"Right":"Left");
                AnmAnimations animations=enemy.type==12?lichAnimations:enemy.type==11?fireElementalAnimations:enemy.type==8?bossSpiderAnimations:enemy.type==10?bossMinoAnimations:bossWormAnimations;Bitmap sheet=enemy.type==12?lich:enemy.type==11?fireElemental:enemy.type==8?bossSpider:enemy.type==10?bossMino:bossWorm;
                AnmAnimations.Frame f=animations.sample(sequence,enemy.attackTime>0?(OriginalClock.seconds(enemy.type==8?29:79)-enemy.attackTime)*OriginalClock.HZ:time*OriginalClock.HZ);
                if(f!=null){paint.setColor(Color.WHITE);c.drawBitmap(sheet,new Rect(f.x,f.y,f.x+f.width,f.y+f.height),new RectF(enemy.center()-f.width/2f,enemy.y+enemy.height()-f.height,enemy.center()+f.width/2f,enemy.y+enemy.height()),paint);}
                if(enemy.hp>0){fill(c,enemy.x,enemy.y-12,enemy.width(),5,0xff421f28);fill(c,enemy.x,enemy.y-12,enemy.width()*enemy.hp/enemy.maxHealth(),5,0xffd65c51);}continue;
            }
            if(enemy.type==5){
                int column=2+(int)(time*7)%4,row=0;if(enemy.direction<0){column=(int)(time*7)%4;row=1;}
                paint.setColor(Color.WHITE);c.drawBitmap(minoskel,new Rect(column*96,row*90,column*96+96,row*90+90),new RectF(enemy.x-32,enemy.y-30,enemy.x+64,enemy.y+60),paint);continue;
            }
            if(enemy.type==6){
                int column=(int)(time*8)%4+(enemy.direction<0?6:0);paint.setColor(Color.WHITE);
                c.drawBitmap(crawler,new Rect(column*64,0,column*64+64,32),new RectF(enemy.x-16,enemy.y-8,enemy.x+48,enemy.y+24),paint);continue;
            }
            if(enemy.type==2){
                int column=(int)(time*8)%4+(enemy.direction<0?6:0);paint.setColor(Color.WHITE);
                c.drawBitmap(worm,new Rect(column*64,0,column*64+64,25),new RectF(enemy.x-16,enemy.y-5,enemy.x+48,enemy.y+20),paint);continue;
            }
            if(enemy.type==3){
                int column=(int)(time*9)%6;paint.setColor(Color.WHITE);
                c.save();if(enemy.direction<0){c.translate((enemy.x+16)*2,0);c.scale(-1,1);}
                c.drawBitmap(bat,new Rect(column*44,0,column*44+44,54),new RectF(enemy.x-6,enemy.y-11,enemy.x+38,enemy.y+43),paint);c.restore();continue;
            }
            // This sheet has 90x105 cells, unlike the hero's 80x80 sheet.
            int column=(int)(time*7)%6,row=enemy.direction>0?0:1;
            paint.setColor(Color.WHITE);
            c.drawBitmap(skeleton,new Rect(column*90,row*105,column*90+90,row*105+105),new RectF(enemy.x-29,enemy.y-55,enemy.x+61,enemy.y+50),paint);
        }
        for(CombatWorld.Bolt bolt:combat.bolts){
            SpriteFrames.Frame f=bolt.hostile?(bolt.speed>0?enemyBoltRight:enemyBoltLeft):(bolt.speed>0?boltRight:boltLeft);paint.setColor(Color.WHITE);
            if(bolt.kind==6)f=(bolt.speed>0?earthRight:earthLeft)[((int)(bolt.age*OriginalClock.HZ)/4)&1];
            if(bolt.kind==5)f=(bolt.speed>0?fireballRight:fireballLeft)[((int)(bolt.age*OriginalClock.HZ)/4)%3];
            if(bolt.kind==8)f=lightning[((int)(bolt.age*OriginalClock.HZ)/4)%3];
            if(bolt.kind==14)f=icicle;
            if(bolt.kind==9)f=fireMissiles[bolt.variant];
            if(bolt.kind==26)f=webFrame;
            if(bolt.kind==25)f=bolt.speed>0?bossBoltRight:bossBoltLeft;
            if(bolt.kind==27)f=(bolt.speed>0?skullRight:skullLeft)[((int)(bolt.age*OriginalClock.HZ)/16)&1];
            c.drawBitmap(shots,new Rect(f.x,f.y,f.x+f.width,f.y+f.height),new RectF(bolt.x-f.width/2f,bolt.y-f.height/2f,bolt.x+f.width/2f,bolt.y+f.height/2f),paint);
        }
        if(combat.swing>0){paint.setColor(0xffeddfad);paint.setStrokeWidth(5);c.drawLine(x+16+facing*28,y+10,x+16+facing*76,y+40,paint);}
        int frame=HeroFrames.frame(facing,left||right,ground,climbing,up||down,vy,time);
        if(combat.immunity<=0||((int)(combat.immunity*12))%2==0)tile(c,wizard?mage:warrior,frame,80,x-24,y-28);
        if(combat.webbed>0){paint.setColor(Color.WHITE);c.drawBitmap(shots,new Rect(webFrame.x,webFrame.y,webFrame.x+webFrame.width,webFrame.y+webFrame.height),new RectF(x-44,y-40,x+76,y+66),paint);}
        drawLifts(c,true);
    }
    void drawLifts(Canvas c,boolean front){
        paint.setColor(Color.WHITE);
        for(LiftWorld.Lift lift:lifts.lifts){
            int sx=lift.type==1?(front?384:480):(front?256:352),sy=lift.type==1?128:256,w=lift.type==1?96:64,h=lift.type==1?128:112;
            if(sx+w>animate.getWidth()||sy+h>animate.getHeight())continue;
            if(!front&&lift.type==2){
                float bottom=lift.y-102;
                for(float top=(lift.row+1)*64;top<bottom;top+=64){int height=(int)Math.min(64,bottom-top);if(height>0&&animate.getWidth()>=640&&animate.getHeight()>=196)c.drawBitmap(animate,new Rect(576,128+lift.phase,640,128+lift.phase+height),new RectF(lift.x-32,top,lift.x+32,top+height),paint);}
            }
            float top=lift.y-(lift.type==1?90:112);
            c.drawBitmap(animate,new Rect(sx,sy,sx+w,sy+h),new RectF(lift.x-w/2f,top,lift.x+w/2f,top+h),paint);
        }
    }
    @Override protected void onDraw(Canvas c){
        if(background)return;long now=System.nanoTime();float dt=last==0?0:Math.min(.05f,(now-last)/1e9f);last=now;
        // Substeps avoid skipping thin collision boundaries when the device is busy.
        while(dt>0){float part=Math.min(dt,1f/120);update(part);dt-=part;}
        scale=Math.min(getWidth()/960f,getHeight()/540f);ox=(getWidth()-960*scale)/2;oy=(getHeight()-540*scale)/2;
        c.drawColor(Color.BLACK);c.save();c.translate(ox,oy);c.scale(scale,scale);fill(c,0,0,960,540,0xff100e17);
        if(level!=null){c.save();c.clipRect(0,50,960,470);
            if(overview){float fit=Math.min(960f/(level.width*64),420f/(level.height*64));c.translate((960-level.width*64*fit)/2,50+(420-level.height*64*fit)/2);c.scale(fit,fit);}
            else{camX=Math.max(0,Math.min(level.width*64-960,x-450));camY=Math.max(0,Math.min(level.height*64-420,y-180));c.translate(-camX,50-camY);}
            world(c);c.restore();
        }
        fill(c,0,0,960,50,0xff101c2c);txt(c,"ORYGINAL: "+levels.get(selected).replace("Data/Levels/", ""),14,32,19,0xffd8e4da);
        txt(c,"HP "+combat.health+"  KLUCZE "+Integer.bitCount(keys)+"/3",360,32,17,0xffe9cd86);
        if(environment.water)txt(c,"POD WODA",365,66,15,0xff89d9ff);
        button(c,"<",560,0,48,false);button(c,">",614,0,48,false);button(c,"MAPA",670,0,92,overview);button(c,"PAUZA",770,0,102,paused);button(c,"MENU",880,0,80,false);
        fill(c,0,470,960,70,0xff101c2c);button(c,"<",12,476,65,left);button(c,">",85,476,65,right);button(c,"GORA",170,476,92,up);button(c,"DOL",270,476,82,down);
        button(c,wizard?"MAG":"WOJOWNIK",374,476,138,false);button(c,"SKOK",530,476,100,jump);
        button(c,"ATAK",645,476,110,fire);txt(c,"Pok: "+combat.kills+"  Pkt: "+totalScore(),770,488,12,0xffe9cd86);txt(c,"CZAR "+(combat.selectedSpell+1)+"  >",770,507,14,0xffb4dbef);txt(c,"MANA "+combat.energy[0]+" | "+combat.energy[1]+" | "+combat.energy[2]+" | "+combat.energy[3],770,527,12,0xffb4dbef);
        if(paused){fill(c,200,188,560,115,0xee101c2c);txt(c,error==null?"PAUZA — dotknij PAUZA, aby wrocic":error,220,250,22,0xfff0e5ce);}
        if(completed){fill(c,200,188,560,115,0xff101c2c);txt(c,"KONIEC WYPRAWY — PUNKTY: "+totalScore(),220,250,22,0xfff0e5ce);}
        if(combat.health<=0){fill(c,210,155,540,190,0xee101c2c);txt(c,"KONIEC WYPRAWY",300,215,30,0xfff0e5ce);button(c,"PONOW",300,260,160,false);button(c,"MENU",500,260,160,false);}
        c.restore();postInvalidateOnAnimation();
    }
    @Override public boolean onTouchEvent(MotionEvent event){
        int action=event.getActionMasked();float a=(event.getX()-ox)/scale,b=(event.getY()-oy)/scale;
        if(action==MotionEvent.ACTION_DOWN){
            if(combat.health<=0){if(b>=260&&b<320){if(a>=300&&a<460)load(selected);else if(a>=500&&a<660)activity.showHome();}return true;}
            if(b<60){resetInput();if(a>=880){activity.showHome();return true;}if(a>=770)paused=!paused;else if(a>=670)overview=!overview;else if(a>=614&&selected+1<levels.size())load(selected+1);else if(a>=560&&selected>0)load(selected-1);return true;}
            if(b>=494&&b<516&&a>=770){if(wizard)combat.cycleSpell();return true;}
            if(b>=470&&a>=374&&a<512){wizard=!wizard;return true;}
        }
        left=right=up=down=fire=false;
        if(action!=MotionEvent.ACTION_CANCEL)for(int i=0;i<event.getPointerCount();i++){
            if((action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_POINTER_UP)&&i==event.getActionIndex())continue;
            a=(event.getX(i)-ox)/scale;b=(event.getY(i)-oy)/scale;
            if(b>=470){if(a<80)left=true;else if(a<160)right=true;else if(a<265)up=true;else if(a<360)down=true;else if(a>=645&&a<755)fire=true;else if(a>=530&&a<630&&(action==MotionEvent.ACTION_DOWN||action==MotionEvent.ACTION_POINTER_DOWN)&&i==event.getActionIndex())jump=true;}
        }return true;
    }
}

