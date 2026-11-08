package org.braveopendwarves.data;
import java.io.IOException;

/** Observed level layout. Unknown cell fields and trailing sections are preserved. */
public final class OriginalLevel {
    public final int width,height,spawnX,spawnY,exitX,exitY,theme;
    public final int[][] cells; public final Item[] items; public final Marker[] markers; public final Enemy[] enemies; public final byte[] trailer;
    public static final class Item {public final int type,x,y,unknown; public boolean taken; Item(byte[] b,int p){type=GogArchive.le16(b,p);x=GogArchive.le16(b,p+2);y=GogArchive.le16(b,p+4);unknown=GogArchive.le32(b,p+6);}}
    public static final class Enemy {public final int type,flags,x,y;Enemy(byte[] b,int p){type=GogArchive.le16(b,p);flags=GogArchive.le16(b,p+2);x=GogArchive.le16(b,p+4);y=GogArchive.le16(b,p+6);}}
    public static final class Marker {public final int type,x,y;Marker(byte[] b,int p){type=GogArchive.le16(b,p);x=GogArchive.le16(b,p+2);y=GogArchive.le16(b,p+4);}}
    public OriginalLevel(byte[] b) throws IOException {
        if(b.length<2048)throw new IOException("Urwany naglowek poziomu");
        width=GogArchive.le32(b,0);height=GogArchive.le32(b,4);
        int count=GogArchive.le32(b,340),markerCount=GogArchive.le32(b,352),monsters=GogArchive.le32(b,356);
        if(width<1||height<1||width>512||height>512||count<0||count>20000||markerCount<0||markerCount>20000||monsters<0||monsters>20000)throw new IOException("Nieprawidlowe wymiary poziomu");
        long end=2048L+(long)width*height*24+(long)count*10+(long)markerCount*6+(long)monsters*8;
        if(end>b.length)throw new IOException("Urwane dane poziomu");
        spawnX=GogArchive.le32(b,332);spawnY=GogArchive.le32(b,336);exitX=GogArchive.le32(b,344);exitY=GogArchive.le32(b,348);theme=GogArchive.le32(b,360);
        if(spawnX<0||spawnX>=width||spawnY<0||spawnY>=height||theme<1||theme>5)throw new IOException("Nieprawidlowy punkt startu lub motyw");
        cells=new int[width*height][6];int p=2048;
        for(int[] cell:cells)for(int f=0;f<6;f++,p+=4)cell[f]=GogArchive.le32(b,p);
        items=new Item[count];for(int i=0;i<count;i++,p+=10)items[i]=new Item(b,p);
        markers=new Marker[markerCount];for(int i=0;i<markerCount;i++,p+=6)markers[i]=new Marker(b,p);
        enemies=new Enemy[monsters];for(int i=0;i<monsters;i++,p+=8)enemies[i]=new Enemy(b,p);
        trailer=new byte[b.length-p];System.arraycopy(b,p,trailer,0,trailer.length);
    }
    public int[] cell(int x,int y){return x<0||y<0||x>=width||y>=height?null:cells[y*width+x];}
    public boolean solid(int x,int y){int[] c=cell(x,y);return c==null||c[0]==1;}
    public boolean ladder(int x,int y){int[] c=cell(x,y);return c!=null&&(c[5]&16)!=0;}
}
