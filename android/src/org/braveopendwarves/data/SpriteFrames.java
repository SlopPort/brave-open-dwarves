package org.braveopendwarves.data;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/** Original text FRM rectangles: rect uses edges, rectsx uses x/y/width/height. */
public final class SpriteFrames {
    public static final class Frame {
        public final String name;public final int x,y,width,height;
        Frame(String n,int x,int y,int w,int h){name=n;this.x=x;this.y=y;width=w;height=h;}
    }
    private final Map<String,Frame> frames=new LinkedHashMap<String,Frame>();
    public SpriteFrames(byte[] bytes,int imageWidth,int imageHeight)throws IOException{
        String text=new String(bytes,Charset.forName("windows-1252")),name=null;
        for(String line:text.split("\\r?\\n")){
            line=line.trim();if(line.length()==0||line.startsWith("//")||line.startsWith(";"))continue;
            if(line.startsWith("[")){name=null;continue;}
            int equals=line.indexOf('=');if(equals<0)continue;
            String key=line.substring(0,equals).trim().toLowerCase(Locale.ROOT),value=line.substring(equals+1).trim();
            if(key.equals("name")){name=value;continue;}
            if(!key.equals("rect")&&!key.equals("rectsx"))continue;
            if(name==null||name.length()==0)throw new IOException("Brak nazwy klatki FRM");
            String[] parts=value.split(",");if(parts.length!=4)throw new IOException("Nieprawidlowy prostokat FRM");
            int[] numbers=new int[4];try{for(int i=0;i<4;i++)numbers[i]=Integer.parseInt(parts[i].trim());}catch(NumberFormatException e){throw new IOException("Nieprawidlowe liczby FRM",e);}
            int x=numbers[0],y=numbers[1],w=key.equals("rect")?numbers[2]-x:numbers[2],h=key.equals("rect")?numbers[3]-y:numbers[3];
            if(x<0||y<0||w<=0||h<=0||(long)x+w>imageWidth||(long)y+h>imageHeight)throw new IOException("Klatka poza obrazem: "+name);
            String normalized=name.toLowerCase(Locale.ROOT);if(frames.containsKey(normalized))throw new IOException("Powtorzona klatka FRM");
            frames.put(normalized,new Frame(name,x,y,w,h));
        }
        if(frames.isEmpty())throw new IOException("Pusty plik FRM");
    }
    public Frame get(String name)throws IOException{Frame f=frames.get(name.toLowerCase(Locale.ROOT));if(f==null)throw new IOException("Brak klatki "+name);return f;}
    public int size(){return frames.size();}
}
