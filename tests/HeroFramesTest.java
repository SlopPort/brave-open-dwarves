import org.braveopendwarves.data.*;
public final class HeroFramesTest {
    static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    public static void main(String[] args){
        check(HeroFrames.frame(1,false,true,false,false,0,0)==0,"idle right");
        check(HeroFrames.frame(-1,false,true,false,false,0,0)==7,"idle left");
        for(int i=0;i<40;i++){int a=HeroFrames.frame(1,true,true,false,false,0,i*.1f),b=HeroFrames.frame(-1,true,true,false,false,0,i*.1f);check(a>=0&&a<=3&&b==7-a,"direction stays in sheet row");}
        check(HeroFrames.frame(1,false,false,false,false,-100,0)==1&&HeroFrames.frame(1,false,false,false,false,100,0)==2,"rising and falling");
        check(HeroFrames.frame(-1,false,false,false,false,-100,0)==6&&HeroFrames.frame(-1,false,false,false,false,100,0)==5,"left airborne poses");
        check(HeroFrames.frame(1,false,false,true,false,0,10)==16,"ladder rest frame");
        for(int i=0;i<40;i++)check(HeroFrames.frame(1,false,false,true,true,0,i*.1f)>=16&&HeroFrames.frame(1,false,false,true,true,0,i*.1f)<=18,"ladder animation bounds");
        System.out.println("Hero frame tests passed");
    }
}
