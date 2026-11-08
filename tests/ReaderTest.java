import org.braveopendwarves.data.*;
import java.io.*;
import java.security.*;
import java.util.*;

public final class ReaderTest {
    static int rejected;
    interface Probe {void run() throws Exception;}
    static void reject(Probe probe) throws Exception {try{probe.run();}catch(IOException expected){rejected++;return;}throw new AssertionError("Invalid input accepted");}
    static String hash(byte[] b) throws Exception {StringBuilder s=new StringBuilder();for(byte x:MessageDigest.getInstance("SHA-256").digest(b))s.append(String.format("%02x",x&255));return s.toString();}
    public static void main(String[] args) throws Exception {
        int pcx=0,levels=0,total=0;long raw=0;
        try(GogArchive archive=new GogArchive(new File(args[0]))){
            for(GogArchive.Entry e:archive.entries()){
                byte[] b=archive.read(e.name);total++;raw+=b.length;
                if(e.name.toLowerCase(Locale.ROOT).endsWith(".pcx")){
                    PcxImage im=PcxImage.decode(b,false);byte[] rgba=new byte[im.argb.length*4];int i=0;
                    for(int p:im.argb){rgba[i++]=(byte)(p>>16);rgba[i++]=(byte)(p>>8);rgba[i++]=(byte)p;rgba[i++]=(byte)(p>>>24);}
                    System.out.println("PCX\t"+e.name+"\t"+im.width+"\t"+im.height+"\t"+hash(rgba));pcx++;
                }
                if(e.name.toLowerCase(Locale.ROOT).contains("/levels/")){
                    OriginalLevel level=new OriginalLevel(b);
                    if(level.trailer.length!=0)throw new AssertionError("Unparsed map data: "+e.name);
                    for(OriginalLevel.Enemy enemy:level.enemies)if(enemy.x>=level.width*64||enemy.y>=level.height*64)throw new AssertionError("Enemy outside map: "+e.name);
                    levels++;
                }
                if(e.name.toLowerCase(Locale.ROOT).endsWith(".anm")){
                    String sheetName=e.name.substring(0,e.name.length()-4)+".pcx";PcxImage sheet=PcxImage.decode(archive.read(sheetName),false);
                    AnmAnimations animations=new AnmAnimations(b,sheet.width,sheet.height);
                    if(e.name.toLowerCase(Locale.ROOT).contains("boss")&&animations.sample("WalkRight",0)==null)throw new AssertionError("Missing boss walk");
                    System.out.println("ANM\t"+e.name+"\tframes="+animations.frameCount()+"\tsequences="+animations.sequenceCount()+"\twarnings="+animations.warnings());
                }
            }
            if(!Arrays.equals(archive.read("DATA/ITEMS.PCX"),archive.read("Data\\Items.pcx")))throw new AssertionError("Case normalization");
            reject(()->archive.read("missing.pcx"));
            byte[] image=archive.read("Data/Items.pcx");byte[] broken=image.clone();broken[2]=0;reject(()->PcxImage.decode(broken,true));
            if(PcxImage.decode(image,true).argb[0]!=0)throw new AssertionError("Item color key");
            if(PcxImage.decode(archive.read("Data/Creatures/Gnome1.pcx"),true).argb[0]!=0)throw new AssertionError("Hero color key");
            byte[] truncated=Arrays.copyOf(image,200);reject(()->PcxImage.decode(truncated,true));
            byte[] overflow=image.clone();overflow[128]=(byte)255;overflow[129]=0;overflow[66]=1;overflow[67]=0;reject(()->PcxImage.decode(overflow,true));
            reject(()->new OriginalLevel(new byte[2048]));
            byte[] map=archive.read("Data/Levels/Level0001.dat");
            PcxImage sheet=PcxImage.decode(archive.read("Data/Shots.pcx"),false);
            SpriteFrames frames=new SpriteFrames(archive.read("Data/SHOTS.FRM"),sheet.width,sheet.height);
            if(frames.get("BT_RIGHT").width!=65||frames.get("bt_left").height!=34)throw new AssertionError("Shot frames");
            reject(()->new SpriteFrames("[Sprite 1]\nname=x\nrect=0,0,999,8".getBytes("US-ASCII"),64,64));
            reject(()->new OriginalLevel(Arrays.copyOf(map,2049)));
            byte[] invalidSpawn=map.clone();Arrays.fill(invalidSpawn,332,336,(byte)127);reject(()->new OriginalLevel(invalidSpawn));
            byte[] invalidCount=map.clone();Arrays.fill(invalidCount,340,344,(byte)127);reject(()->new OriginalLevel(invalidCount));
        }
        byte[] original=java.nio.file.Files.readAllBytes(new File(args[0]).toPath());
        for(int test=0;test<9;test++){
            byte[] b=original.clone();
            if(test==0)b[0]=0;
            if(test==1)b[32]=9;
            if(test==2)Arrays.fill(b,38,42,(byte)255);
            if(test==3)b[256]=0;
            if(test==4)Arrays.fill(b,268,272,(byte)127);
            if(test==5)b=Arrays.copyOf(b,b.length-1);
            if(test==6){b[278]='.';b[279]='.';b[280]='/';}
            if(test==7){int second=GogArchive.le32(b,264);b[second+22+12]='1';}
            if(test==8)b[GogArchive.le32(b,264)-1]=1;
            File tmp=File.createTempFile("gog-invalid-",".dat",new File("build"));
            try{java.nio.file.Files.write(tmp.toPath(),b);reject(()->{try(GogArchive ignored=new GogArchive(tmp)){} });}finally{tmp.delete();}
        }
        File corrupt=File.createTempFile("gog-zlib-",".dat",new File("build"));
        try{original[296]=0;java.nio.file.Files.write(corrupt.toPath(),original);reject(()->{try(GogArchive a=new GogArchive(corrupt)){a.read(a.entries().get(0).name);}});}finally{corrupt.delete();}
        System.out.println("PASS entries="+total+" pcx="+pcx+" levels="+levels+" bytes="+raw+" rejected="+rejected);
    }
}
