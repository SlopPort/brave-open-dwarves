package org.braveopendwarves;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.view.*;
import java.util.*;

/** Original standalone demonstration. No original executable or game data is loaded. */
public final class GameView extends View {
    static final int W=960, H=540, TILE=40, COLS=72, ROWS=12;
    final Paint p=new Paint(3);
    final SharedPreferences saves;
    final ArrayList<Gem> gems=new ArrayList<Gem>();
    final ArrayList<Enemy> enemies=new ArrayList<Enemy>();
    final ArrayList<Shot> shots=new ArrayList<Shot>();
    final int[][] map=new int[ROWS][COLS];
    final int[] colors={0xff72df98,0xffff775e,0xffcceeff,0xff73bbff};
    float x,y,vx,vy,camera,invincible,cooldown,clock,scale=1,ox,oy;
    boolean grounded,left,right,jump,fire,paused,background,menu=true,dead,complete,mage;
    int level=1,keys,score,hp=5,facing=1,element;
    long last;
    static class Gem { float x,y; int kind; boolean key,taken; Gem(float a,float b,int c,boolean k){x=a;y=b;kind=c;key=k;} }
    static class Enemy { float x,y,start,end; int dir=1; boolean alive=true; Enemy(float a,float b,float c){x=a;start=a-65;end=c;y=b;} }
    static class Shot { float x,y,v,life=1.3f; Shot(float a,float b,float c){x=a;y=b;v=c;} }
    GameView(Context c){super(c);saves=c.getSharedPreferences("progress",0);setFocusable(true);}
    void suspend(){background=true;last=0;left=right=jump=fire=false;}
    void resume(){background=false;last=0;invalidate();}
    void togglePause(){if(!menu&&!dead&&!complete)paused=!paused;left=right=jump=fire=false;invalidate();}
    void start(int n){
        level=n;keys=0;hp=5;x=80;y=360;vx=vy=0;camera=0;invincible=0;cooldown=0;
        dead=complete=paused=false;menu=false;left=right=jump=fire=false;
        gems.clear();enemies.clear();shots.clear();
        for(int r=0;r<ROWS;r++)Arrays.fill(map[r],0);
        for(int c=0;c<COLS;c++){map[11][c]=1;if(c==0||c==COLS-1)for(int r=0;r<ROWS;r++)map[r][c]=1;}
        // All ledges are reachable from the floor or a neighbouring ledge.
        for(int b=0;b<6;b++){
            int col=5+b*11; int row=8;
            for(int c=col;c<col+5;c++)map[row][c]=2;
            for(int c=col+5;c<col+9;c++)map[5][c]=2;
            gems.add(new Gem((col+2)*40, row*40-24,b%4,b<3));
            gems.add(new Gem((col+7)*40, 5*40-24,(b+1)%4,false));
            for(int c=col;c<col+8;c+=2)gems.add(new Gem(c*40+20,414,(c+b)%4,false));
            enemies.add(new Enemy((col+3)*40,410,(col+9)*40));
        }
        saves.edit().putInt("level",level).apply();last=0;invalidate();
    }
    boolean solid(int c,int r){return c<0||c>=COLS||r>=ROWS||(r>=0&&map[r][c]==1);}
    void step(float dt){
        clock+=dt;if(menu||paused||dead||complete)return;
        invincible=Math.max(0,invincible-dt);cooldown-=dt;
        vx=(left?-190:0)+(right?190:0);if(vx!=0)facing=vx>0?1:-1;
        if(jump&&grounded){vy=-560;grounded=false;}jump=false;
        float nx=x+vx*dt;
        int top=(int)(y/40),bottom=(int)((y+35)/40);
        if(vx>0){int c=(int)((nx+26)/40);for(int r=top;r<=bottom;r++)if(solid(c,r)){nx=c*40-27;break;}}
        if(vx<0){int c=(int)(nx/40);for(int r=top;r<=bottom;r++)if(solid(c,r)){nx=(c+1)*40;break;}}
        x=nx;vy+=1050*dt;float ny=y+vy*dt;grounded=false;
        int lc=(int)((x+2)/40),rc=(int)((x+25)/40);
        if(vy>=0){int r=(int)((ny+36)/40);for(int c=lc;c<=rc;c++)if(solid(c,r)||(r>=0&&r<ROWS&&c>=0&&c<COLS&&map[r][c]==2&&y+36<=r*40+.1f)){ny=r*40-36;vy=0;grounded=true;break;}}
        else {int r=(int)(ny/40);for(int c=lc;c<=rc;c++)if(solid(c,r)){ny=(r+1)*40;vy=0;break;}}
        y=ny;
        if(fire&&cooldown<=0){cooldown=mage?.28f:.38f;shots.add(new Shot(x+13+facing*18,y+15,facing*(mage?430:340)));}
        for(Gem g:gems)if(!g.taken&&Math.abs(x+13-g.x)<27&&Math.abs(y+18-g.y)<32){g.taken=true;score+=g.key?100:10;if(g.key)keys|=1<<g.kind;}
        for(Enemy e:enemies)if(e.alive){
            e.x+=e.dir*(50+level*5)*dt;if(e.x<e.start||e.x>e.end)e.dir=-e.dir;
            if(Math.abs(x+13-e.x)<28&&Math.abs(y+20-e.y)<30&&invincible<=0){hp--;invincible=1.5f;vy=-230;if(hp<=0)dead=true;}
        }
        for(int i=shots.size()-1;i>=0;i--){Shot s=shots.get(i);s.x+=s.v*dt;s.life-=dt;
            for(Enemy e:enemies)if(e.alive&&Math.abs(e.x-s.x)<24&&Math.abs(e.y-s.y)<27){e.alive=false;s.life=0;score+=50;}
            if(solid((int)(s.x/40),(int)(s.y/40)))s.life=0;
            if(s.life<=0)shots.remove(i);
        }
        camera=Math.max(0,Math.min(COLS*40-W,x-W*.4f));
        if(x>COLS*40-135&&keys==7){complete=true;saves.edit().putInt("level",level+1).putInt("best",Math.max(score,saves.getInt("best",0))).apply();}
    }
    void rect(Canvas c,float a,float b,float w,float h,int color){p.setColor(color);c.drawRect(a,b,a+w,b+h,p);}
    void text(Canvas c,String s,float a,float b,float size,int color){p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif-medium",0));c.drawText(s,a,b,p);}
    void center(Canvas c,String s,float y,float size,int color){p.setTextSize(size);text(c,s,(W-p.measureText(s))/2,y,size,color);}
    void gem(Canvas c,float x,float y,int col,float size){p.setColor(col);Path path=new Path();path.moveTo(x,y-size);path.lineTo(x+size*.7f,y);path.lineTo(x,y+size);path.lineTo(x-size*.7f,y);path.close();c.drawPath(path,p);p.setColor(0xaaffffff);c.drawLine(x,y-size,x-size*.2f,y,p);}
    void dwarf(Canvas c,float a,float b,boolean wizard){
        rect(c,a+1,b+27,11,10,0xff342c37);rect(c,a+17,b+27,11,10,0xff342c37);
        rect(c,a,b+10,28,19,wizard?0xff547fd4:0xffba6651);rect(c,a+5,b+1,20,17,0xffefbc88);
        p.setColor(0xffe4dcc4);c.drawOval(a+3,b+12,a+26,b+30,p);
        rect(c,a+7,b+6,3,3,0xff192332);rect(c,a+20,b+6,3,3,0xff192332);
        if(wizard){p.setColor(0xff669ce4);Path hat=new Path();hat.moveTo(a-3,b+2);hat.lineTo(a+12,b-23);hat.lineTo(a+30,b+2);hat.close();c.drawPath(hat,p);}
        else {rect(c,a+2,b-5,25,8,0xff8da4b7);rect(c,a+11,b-10,7,7,0xffc2d6e3);}
        rect(c,a+(facing>0?31:-8),b+10,4,26,0xff98704e);
        if(!wizard)rect(c,a+(facing>0?28:-14),b+8,17,10,0xffc5dce1);
        else gem(c,a+(facing>0?33:-6),b+7,colors[element],7);
    }
    void button(Canvas c,String s,float a,float b,float w,boolean down){p.setColor(down?0xb8498e99:0x9936475c);c.drawRoundRect(a,b,a+w,b+66,15,15,p);text(c,s,a+15,b+42,22,0xffeef6ed);}
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);if(background)return;
        long now=System.nanoTime();float dt=last==0?0:Math.min(.035f,(now-last)/1e9f);last=now;step(dt);
        scale=Math.min(getWidth()/(float)W,getHeight()/(float)H);ox=(getWidth()-W*scale)/2;oy=(getHeight()-H*scale)/2;
        c.drawColor(0xff080f1b);c.save();c.translate(ox,oy);c.scale(scale,scale);c.clipRect(0,0,W,H);
        rect(c,0,0,W,H,0xff111d2e);
        for(int i=0;i<28;i++){float a=(i*139-camera*.18f)%1050;if(a<0)a+=1050;rect(c,a,95+(i*41)%160,45,310,0xff192b3b);}
        for(int i=0;i<55;i++){float a=(i*83-camera*.4f)%990;if(a<0)a+=990;gem(c,a,90+(i*97)%330,0xff294653,2);}
        if(menu){
            text(c,"B O D  /  ANDROID PROTOTYPE",80,88,17,0xff7bd5b6);
            text(c,"BRAVE OPEN",80,170,58,0xfff5edd8);text(c,"DWARVES",80,232,58,0xfff5edd8);
            text(c,"Trzy klucze. Zapomniane jaskinie. Twoja wyprawa.",83,282,19,0xffa5b8c5);
            button(c,mage?"POSTAC: MAG":"POSTAC: WOJOWNIK",80,316,280,false);
            button(c,"NOWA GRA",80,400,210,false);button(c,"KONTYNUUJ",310,400,210,false);
            c.save();c.translate(700,270);c.scale(4,4);dwarf(c,0,0,mage);c.restore();
            text(c,"Wlasny silnik i grafika / demonstracja 0.1",80,512,16,0xff7992a7);
        }else{
            c.save();c.translate(-camera,0);
            for(int r=0;r<ROWS;r++)for(int col=Math.max(0,(int)camera/40);col<Math.min(COLS,(int)(camera+W)/40+1);col++)if(map[r][col]!=0){
                rect(c,col*40,r*40,40,40,0xff334451);rect(c,col*40+2,r*40+5,36,32,0xff3d505b);rect(c,col*40,r*40,40,4,0xff6a937f);
                rect(c,col*40+6,r*40+15,15,2,0xff455e67);
            }
            float door=COLS*40-120;rect(c,door,360,55,80,keys==7?0xff568c71:0xff51494c);rect(c,door+8,370,39,70,0xff172734);
            for(int k=0;k<3;k++)gem(c,door+12+k*15,384,(keys&(1<<k))!=0?colors[k]:0xff526071,6);
            for(Gem g:gems)if(!g.taken)gem(c,g.x,g.y+(float)Math.sin(clock*3+g.x)*3,colors[g.kind],g.key?17:9);
            for(Enemy e:enemies)if(e.alive){p.setColor(0xffa97dcc);c.drawOval(e.x-17,e.y-13,e.x+17,e.y+15,p);rect(c,e.x-9,e.y-4,5,5,0xffffdd8b);rect(c,e.x+5,e.y-4,5,5,0xffffdd8b);}
            for(Shot s:shots)gem(c,s.x,s.y,colors[element],mage?9:6);
            if(invincible<=0||((int)(invincible*12))%2==0)dwarf(c,x,y,mage);
            c.restore();
            rect(c,0,0,W,64,0xe5101929);text(c,"BOD",22,39,24,0xff7bd5b6);text(c,"JASKINIA "+level,102,39,18,0xffe2eced);
            for(int i=0;i<5;i++)gem(c,288+i*22,31,i<hp?0xffff8872:0xff344657,9);
            for(int i=0;i<3;i++)gem(c,440+i*30,31,(keys&(1<<i))!=0?colors[i]:0xff344657,12);
            text(c,"SKARB "+score,560,39,18,0xffe5d6a3);button(c,"PAUZA",804,4,138,paused);
            rect(c,0,472,W,68,0xdd101929);
            button(c,"<",18,470,78,left);button(c,">",108,470,78,right);
            button(c,"ZMIANA",334,470,140,false);button(c,"ATAK",678,470,120,fire);button(c,"SKOK",814,470,126,jump);
            if(clock<8)text(c,"Zbierz trzy duze klucze i dotrzyj do bramy po prawej.",200,94,18,0xffd4e5de);
            if(paused||dead||complete){rect(c,0,70,W,400,0xe6111d2e);center(c,complete?"JASKINIA OCZYSZCZONA":dead?"KONIEC WYPRAWY":"ODPOCZYNEK",218,36,0xfff5edd8);
                center(c,complete?"Wszystkie trzy klucze odnalezione.":dead?"Sprobuj jeszcze raz.":"Gra jest wstrzymana.",264,20,0xffa8c3cc);
                button(c,complete?"DALEJ":dead?"PONOW":"WZNOW",300,308,170,false);button(c,"MENU",490,308,170,false);
            }
        }
        c.restore();postInvalidateOnAnimation();
    }
    @Override public boolean onTouchEvent(MotionEvent e){
        float a=(e.getX()-ox)/scale,b=(e.getY()-oy)/scale;
        int action=e.getActionMasked();
        if(action==MotionEvent.ACTION_DOWN){
            if(menu){if(b>=316&&b<382&&a>=80&&a<360){mage=!mage;}else if(b>=400&&b<466&&a>=80&&a<520){score=0;start(a<300?1:saves.getInt("level",1));}return true;}
            if(paused||dead||complete){if(b>=308&&b<374){if(a>=300&&a<470){if(complete)start(level+1);else if(dead){score=0;start(level);}else paused=false;}else if(a>=490&&a<660){menu=true;left=right=jump=fire=false;}}return true;}
            if(b<70&&a>804){togglePause();return true;}
            if(b>470&&a>334&&a<474){mage=!mage;element=(element+1)%4;return true;}
        }
        left=right=fire=false;
        if(action!=MotionEvent.ACTION_CANCEL)for(int i=0;i<e.getPointerCount();i++){
            if((action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_POINTER_UP)&&i==e.getActionIndex())continue;
            a=(e.getX(i)-ox)/scale;b=(e.getY(i)-oy)/scale;
            if(b>=450){if(a<100)left=true;else if(a<200)right=true;else if(a>=678&&a<800)fire=true;else if(a>=814&&(action==MotionEvent.ACTION_DOWN||action==MotionEvent.ACTION_POINTER_DOWN)&&i==e.getActionIndex())jump=true;}
        }
        return true;
    }
    @Override public boolean onKeyDown(int key,KeyEvent e){if(key==KeyEvent.KEYCODE_DPAD_LEFT)left=true;else if(key==KeyEvent.KEYCODE_DPAD_RIGHT)right=true;else if(key==KeyEvent.KEYCODE_SPACE||key==KeyEvent.KEYCODE_BUTTON_A)jump=true;else if(key==KeyEvent.KEYCODE_ENTER||key==KeyEvent.KEYCODE_BUTTON_X)fire=true;else return super.onKeyDown(key,e);return true;}
    @Override public boolean onKeyUp(int key,KeyEvent e){if(key==KeyEvent.KEYCODE_DPAD_LEFT)left=false;else if(key==KeyEvent.KEYCODE_DPAD_RIGHT)right=false;else if(key==KeyEvent.KEYCODE_ENTER||key==KeyEvent.KEYCODE_BUTTON_X)fire=false;else return super.onKeyUp(key,e);return true;}
}
