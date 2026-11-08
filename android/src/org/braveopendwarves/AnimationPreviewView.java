package org.braveopendwarves;
import android.graphics.*;
import android.view.*;
import org.braveopendwarves.data.*;
import java.io.*;
import java.util.*;

/** Inspect original frame geometry and animation order without guessing enemy AI. */
final class AnimationPreviewView extends View {
    final MainActivity owner;final Bitmap sheet;final AnmAnimations animations;final List<String> names;final String path;
    final Paint paint=new Paint(3);int index,speed=1;float ticks,scale,ox,oy;long last;boolean paused,background;
    AnimationPreviewView(MainActivity owner,GogArchive archive,String path)throws IOException{
        super(owner);this.owner=owner;this.path=path;
        PcxImage image=PcxImage.decode(archive.read(path.substring(0,path.length()-4)+".pcx"),true);
        sheet=Bitmap.createBitmap(image.argb,image.width,image.height,Bitmap.Config.ARGB_8888);
        animations=new AnmAnimations(archive.read(path),image.width,image.height);names=animations.sequenceNames();paint.setFilterBitmap(false);
    }
    void suspend(){background=true;last=0;}
    void resume(){background=false;last=0;invalidate();}
    void text(Canvas c,String value,float x,float y,int size,int color){paint.setColor(color);paint.setTextSize(size);c.drawText(value,x,y,paint);}
    void button(Canvas c,String label,int x,int width){paint.setColor(0xff2b455b);c.drawRoundRect(x,462,x+width,526,12,12,paint);text(c,label,x+12,502,21,0xffebeddf);}
    @Override protected void onDraw(Canvas c){
        if(background)return;long now=System.nanoTime();if(last!=0&&!paused)ticks+=Math.min(.05f,(now-last)/1e9f)*(speed==0?10:speed==1?20:40);last=now;
        scale=Math.min(getWidth()/960f,getHeight()/540f);ox=(getWidth()-960*scale)/2;oy=(getHeight()-540*scale)/2;
        c.drawColor(0xff101c2a);c.save();c.translate(ox,oy);c.scale(scale,scale);
        text(c,path.replace("Data/Creatures/",""),22,38,25,0xffe6e9dc);text(c,names.get(index),22,76,22,0xff80d7c1);
        for(int y=100;y<430;y+=24)for(int x=200;x<760;x+=24){paint.setColor(((x/24+y/24)&1)==0?0xff293340:0xff202b39);c.drawRect(x,y,x+24,y+24,paint);}
        AnmAnimations.Frame frame=animations.sample(names.get(index),ticks);
        if(frame!=null){float size=Math.min(2,Math.min(500f/frame.width,290f/frame.height));float w=frame.width*size,h=frame.height*size;paint.setColor(Color.WHITE);c.drawBitmap(sheet,new Rect(frame.x,frame.y,frame.x+frame.width,frame.y+frame.height),new RectF(480-w/2,400-h,480+w/2,400),paint);text(c,frame.name,22,438,18,0xffa5bdc6);}
        else text(c,"Ta sekwencja wskazuje brakujaca klatke w danych gry.",230,270,19,0xffe6bf8a);
        button(c,"< POPRZEDNIA",20,200);button(c,"> NASTEPNA",234,200);button(c,paused?"WZNOW":"PAUZA",448,140);button(c,"TEMPO x"+(speed==0?"0.5":speed==1?"1":"2"),602,160);button(c,"MENU",776,164);
        c.restore();postInvalidateOnAnimation();
    }
    @Override public boolean onTouchEvent(MotionEvent e){if(e.getActionMasked()!=MotionEvent.ACTION_DOWN)return true;float x=(e.getX()-ox)/scale,y=(e.getY()-oy)/scale;if(y<462)return true;
        if(x>=776){owner.showHome();return true;}if(x>=602)speed=(speed+1)%3;else if(x>=448)paused=!paused;else{index=(index+(x<234?names.size()-1:1))%names.size();ticks=0;}invalidate();return true;
    }
}
