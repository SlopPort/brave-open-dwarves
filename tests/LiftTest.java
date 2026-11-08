import org.braveopendwarves.data.*;
public final class LiftTest {
    static void put(byte[] b,int p,int n){for(int i=0;i<4;i++)b[p+i]=(byte)(n>>(8*i));}
    static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    static OriginalLevel map(int type)throws Exception{
        byte[] b=new byte[2048+8*8*24+6];put(b,0,8);put(b,4,8);put(b,360,2);put(b,352,1);
        int p=b.length-6;b[p]=(byte)type;b[p+2]=2;b[p+4]=2;return new OriginalLevel(b);
    }
    public static void main(String[] args)throws Exception{
        OriginalLevel map=map(1);for(int c=1;c<6;c++)map.cell(c,2)[5]=0x4000;
        LiftWorld w=new LiftWorld(map);LiftWorld.Lift l=w.lifts.get(0);
        check(l.x==160&&l.y==191&&l.floor()==223,"horizontal initial geometry");
        check(l.supports(144,223)&&!l.supports(10,223),"support bounds");
        l.wait=0;w.update(.025f);check(l.x==162&&l.previousX==160,"ten millisecond steps");
        l.x=350;l.clock=0;w.update(.02f);check(l.x==350&&l.direction==0&&l.wait>1.9f,"rail end reverses and waits");
        map=map(2);w=new LiftWorld(map);l=w.lifts.get(0);
        check(l.y==320&&l.floor()==320&&l.halfWidth()==32,"vertical initial geometry");
        l.wait=0;w.update(.025f);check(l.y==322&&l.phase==2,"vertical steps and animation phase");
        map.cell(2,5)[5]=0x20;w.update(.01f);check(l.y==322&&l.direction==0,"vertical stop flag");
        boolean rejected=false;try{w.update(Float.NaN);}catch(IllegalArgumentException e){rejected=true;}check(rejected,"invalid interval");
        RunState state=new RunState();state.captureLifts(w);float saved=l.y;l.y=500;state.restoreLifts(w);check(l.y==saved&&l.previousY==saved,"restore lift without spurious carry");
        state.liftState[0][2]=Float.NaN;rejected=false;try{state.restoreLifts(w);}catch(java.io.IOException e){rejected=true;}check(rejected,"reject corrupt lift timer");
        try(GogArchive archive=new GogArchive(new java.io.File("Gnomes.dat"))){
            int count=0;for(GogArchive.Entry entry:archive.entries())if(entry.name.toLowerCase(java.util.Locale.ROOT).matches("data/levels/level[0-9]{4}\\.dat")){
                OriginalLevel original=new OriginalLevel(archive.read(entry.name));LiftWorld actual=new LiftWorld(original);count+=actual.lifts.size();
                for(int tick=0;tick<3000;tick++)actual.update(.01f);
                for(LiftWorld.Lift lift:actual.lifts)check(lift.x>=0&&lift.x<original.width*64&&lift.y>=0&&lift.y<=original.height*64,"campaign lift stays in map");
            }check(count==179,"all campaign lift markers");
        }
        System.out.println("Lift tests passed");
    }
}
