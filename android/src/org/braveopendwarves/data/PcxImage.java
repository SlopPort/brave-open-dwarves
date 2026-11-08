package org.braveopendwarves.data;
import java.io.IOException;

/** PCX 8-bit indexed or 24-bit planar RGB, RLE. */
public final class PcxImage {
    public final int width,height; public final int[] argb;
    private PcxImage(int w,int h,int[] p){width=w;height=h;argb=p;}
    public static PcxImage decode(byte[] b,boolean transparent) throws IOException {
        if(b.length<128||(b[0]&255)!=10||b[2]!=1||b[3]!=8||(b[65]!=1&&b[65]!=3))throw new IOException("Nieobslugiwany PCX");
        int planes=b[65],limit=b.length;
        if(planes==1){if(b.length<897||b[b.length-769]!=12)throw new IOException("Brak palety PCX");limit=b.length-769;}
        int w=GogArchive.le16(b,8)-GogArchive.le16(b,4)+1,h=GogArchive.le16(b,10)-GogArchive.le16(b,6)+1;
        int stride=GogArchive.le16(b,66),palette=b.length-768;
        if(w<1||h<1||w>4096||h>4096||(long)w*h>4000000||stride<w||stride>8192)throw new IOException("Nieprawidlowe wymiary PCX");
        int[] pixels=new int[w*h],colors=new int[256];
        if(planes==1)for(int i=0;i<256;i++){
            int color=0xff000000|((b[palette+i*3]&255)<<16)|((b[palette+i*3+1]&255)<<8)|(b[palette+i*3+2]&255);
            colors[i]=color;
        }
        int pos=128;byte[] scanline=new byte[stride*planes];
        for(int y=0;y<h;y++){
            int x=0;
            while(x<scanline.length){
                if(pos>=limit)throw new IOException("Urwany obraz PCX");
                int value=b[pos++]&255,count=1;
                if((value&192)==192){count=value&63;if(count==0||pos>=limit)throw new IOException("Nieprawidlowe RLE PCX");value=b[pos++]&255;}
                if(x+count>scanline.length)throw new IOException("RLE przekracza wiersz PCX");
                for(int i=0;i<count;i++,x++)scanline[x]=(byte)value;
            }
            for(x=0;x<w;x++){
                int color=planes==1?colors[scanline[x]&255]:0xff000000|((scanline[x]&255)<<16)|((scanline[stride+x]&255)<<8)|(scanline[stride*2+x]&255);
                pixels[y*w+x]=color;
            }
        }
        if(pos!=limit)throw new IOException("Nadmiarowe dane obrazu PCX");
        if(transparent){
            // Some original palettes use (252,4,252), others (255,0,255).
            // Select the most frequent magenta candidate actually used by this sheet.
            java.util.HashMap<Integer,Integer> counts=new java.util.HashMap<Integer,Integer>();
            int key=0,best=0;
            for(int color:pixels)if(((color>>16)&255)>=240&&((color>>8)&255)<=16&&(color&255)>=240){
                Integer previous=counts.get(color);int count=previous==null?1:previous+1;counts.put(color,count);
                if(count>best){best=count;key=color;}
            }
            if(best>0)for(int i=0;i<pixels.length;i++)if(pixels[i]==key)pixels[i]=0;
        }
        return new PcxImage(w,h,pixels);
    }
}
