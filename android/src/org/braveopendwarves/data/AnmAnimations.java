package org.braveopendwarves.data;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.*;

/** Static ANM frame/sequence reader. Events are retained as unsupported, never executed. */
public final class AnmAnimations {
    public static final class Frame {
        public final String name;public final int x,y,width,height;
        Frame(String name,int x,int y,int w,int h){this.name=name;this.x=x;this.y=y;width=w;height=h;}
    }
    private static final class Step {final String frame;final int ticks;Step(String f,int t){frame=f;ticks=t;}}
    private static final class Sequence {final ArrayList<Step> steps=new ArrayList<Step>();boolean loop;int length;}
    private final Map<String,Frame> frames=new LinkedHashMap<String,Frame>();
    private final Map<String,Sequence> sequences=new LinkedHashMap<String,Sequence>();
    private final ArrayList<String> warnings=new ArrayList<String>();
    public AnmAnimations(byte[] bytes,int imageWidth,int imageHeight)throws IOException{
        if(bytes.length>65536)throw new IOException("ANM jest za duzy");
        Pattern definition=Pattern.compile("\\*FRAME\\s+(\\S+)\\s+(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*",Pattern.CASE_INSENSITIVE);
        Pattern step=Pattern.compile("\\*FRAME\\s+([^,\\s]+)(?:\\s*,\\s*(\\d+))?\\s*",Pattern.CASE_INSENSITIVE);
        boolean defining=false,events=false;Sequence current=null;
        for(String raw:new String(bytes,Charset.forName("windows-1252")).split("\\r?\\n")){
            int comment=raw.indexOf("//");String line=(comment<0?raw:raw.substring(0,comment)).trim();
            if(line.length()==0||line.equals("{"))continue;
            if(line.equals("}")){defining=events=false;current=null;continue;}
            if(line.equalsIgnoreCase("*ALL_FRAMES")){defining=true;current=null;events=false;continue;}
            if(line.equalsIgnoreCase("*EVENTS")){events=true;defining=false;current=null;continue;}
            if(line.toUpperCase(Locale.ROOT).startsWith("*ANIMATION ")){
                String name=key(line.substring(11).trim());if(name.length()==0||sequences.containsKey(name)||sequences.size()>=256)throw new IOException("Nieprawidlowa nazwa animacji");
                current=new Sequence();sequences.put(name,current);defining=events=false;continue;
            }
            if(events){warnings.add("Unsupported event: "+line);continue;}
            Matcher match=definition.matcher(line);
            if(defining&&match.matches()){
                String name=match.group(1);int x=number(match.group(2)),y=number(match.group(3)),w=number(match.group(4)),h=number(match.group(5));
                if(x<0||y<0||w<=0||h<=0||(long)x+w>imageWidth||(long)y+h>imageHeight||frames.size()>=512||frames.containsKey(key(name)))throw new IOException("Nieprawidlowa klatka ANM: "+name);
                frames.put(key(name),new Frame(name,x,y,w,h));continue;
            }
            if(current!=null){
                if(line.equalsIgnoreCase("*LOOP")){current.loop=true;continue;}
                match=step.matcher(line);
                if(match.matches()){
                    int duration=match.group(2)==null?1:number(match.group(2));if(duration<1||duration>10000||current.steps.size()>=512)throw new IOException("Nieprawidlowy czas klatki ANM");
                    current.steps.add(new Step(key(match.group(1)),duration));current.length+=duration;continue;
                }
            }
            throw new IOException("Nieobslugiwana instrukcja ANM: "+line);
        }
        if(frames.isEmpty()||sequences.isEmpty())throw new IOException("Brak klatek lub sekwencji ANM");
        for(Map.Entry<String,Sequence> e:sequences.entrySet()){
            if(e.getValue().steps.isEmpty())throw new IOException("Pusta animacja ANM");
            for(Step s:e.getValue().steps)if(!frames.containsKey(s.frame))warnings.add(e.getKey()+": missing frame "+s.frame);
        }
    }
    private static int number(String s)throws IOException{try{return Integer.parseInt(s);}catch(NumberFormatException e){throw new IOException("Nieprawidlowa liczba ANM",e);}}
    private static String key(String s){return s.toLowerCase(Locale.ROOT);}
    public List<String> warnings(){return Collections.unmodifiableList(warnings);}
    public int frameCount(){return frames.size();}
    public int sequenceCount(){return sequences.size();}
    public List<String> sequenceNames(){return Collections.unmodifiableList(new ArrayList<String>(sequences.keySet()));}
    public Frame sample(String name,float ticks){
        Sequence sequence=sequences.get(key(name));if(sequence==null||Float.isNaN(ticks)||Float.isInfinite(ticks))return null;
        float t=Math.max(0,ticks);if(sequence.loop)t%=sequence.length;else t=Math.min(t,sequence.length-1);
        for(Step step:sequence.steps){if(t<step.ticks)return frames.get(step.frame);t-=step.ticks;}return null;
    }
}
