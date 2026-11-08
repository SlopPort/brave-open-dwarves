import org.braveopendwarves.data.*;
public final class ItemTest {
    static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    public static void main(String[] args)throws Exception{
        check(ItemRules.score(1)==70&&ItemRules.score(31)==1300&&ItemRules.score(68)==600,"original score table");
        check(ItemRules.score(-1)==0&&ItemRules.score(1000)==0,"unknown scores bounded");
        check(ItemRules.healing(31)==0&&ItemRules.healing(40)==20&&ItemRules.healing(44)==5&&ItemRules.healing(45)==10&&ItemRules.healing(46)==2,"healing IDs and scaled amounts");
        check(!ItemRules.supported(9)&&!ItemRules.supported(34)&&!ItemRules.supported(52)&&!ItemRules.supported(182),"unimplemented equipment and effects preserved");
        check(ItemRules.supported(28)&&ItemRules.supported(46)&&ItemRules.supported(68),"valuables and healing supported");
        try(GogArchive archive=new GogArchive(new java.io.File("Gnomes.dat"))){
            OriginalLevel map=new OriginalLevel(archive.read("Data/Levels/Level0001.dat"));int expected=0,count=0;
            for(OriginalLevel.Item item:map.items)if(ItemRules.supported(item.type)){item.taken=true;expected+=ItemRules.score(item.type);count++;}
            check(count>0&&ItemRules.score(map)==expected,"score derives from saved pickup flags");
        }
        check(ItemRules.protectionTicks(40)==500&&ItemRules.protectionTicks(45)==0,"protection pickup duration");
        int[][] gains={{0,0,5,0},{0,3,0,3},{0,5,0,0},{3,0,3,0},{0,0,0,5},{5,0,0,0}};
        for(int type=1;type<=6;type++)for(int channel=0;channel<4;channel++)check(ItemRules.energyGain(type,channel)==gains[type-1][channel],"original energy pickup mapping");
        CombatWorld w=new CombatWorld(CombatTest.map());check(w.energy[0]==25&&w.energy[1]==0,"original wizard starting energy");w.energy[0]=99;w.replenishEnergy(6);check(w.energy[0]==100,"energy capped at hundred");
        w.energy[0]=1;check(w.attack(100,206,1,true)&&w.energy[0]==0,"base wizard shot consumes one energy");w.cooldown=0;check(!w.attack(100,206,1,true)&&w.cooldown==0,"empty energy blocks cast without cooldown");check(w.attack(100,206,1,false),"melee remains available with no energy");
        for(int map:new int[]{1,11,21,31,41,1001}){java.util.Arrays.fill(w.energy,99);w.resetChapterEnergy(map,true);check(java.util.Arrays.equals(w.energy,new int[]{25,0,0,0}),"wizard chapter energy reset");java.util.Arrays.fill(w.energy,99);w.resetChapterEnergy(map,false);check(java.util.Arrays.equals(w.energy,new int[]{0,25,0,0}),"warrior chapter energy reset");}
        for(int map:new int[]{2,10,20,50,1002}){w.energy[0]=12;w.energy[1]=34;w.energy[2]=56;w.energy[3]=78;w.resetChapterEnergy(map,true);check(java.util.Arrays.equals(w.energy,new int[]{12,34,56,78}),"ordinary map preserves energy");}
        w.energyClock=0;java.util.Arrays.fill(w.energy,0);w.regenerateEnergy(127f/75,true);check(w.energy[0]==0,"regeneration waits full128 ticks");w.regenerateEnergy(1f/75,true);check(w.energy[0]==1&&w.energy[1]==0,"wizard regeneration uses first channel");w.regenerateEnergy(256f/75,false);check(w.energy[1]==2&&w.energy[0]==1,"warrior regeneration uses second channel and handles multiple pulses");w.energy[0]=24;w.regenerateEnergy(256f/75,true);check(w.energy[0]==25,"regeneration stops at25");w.energy[0]=80;w.regenerateEnergy(128f/75,true);check(w.energy[0]==80,"regeneration leaves collected excess intact");w.health=0;float clock=w.energyClock;w.regenerateEnergy(1,true);check(w.energyClock==clock,"dead hero does not regenerate");
        w=new CombatWorld(CombatTest.map());w.cycleSpell();check(w.selectedSpell==0,"locked spells skipped");w.collectPowerOrb(56);w.cycleSpell();w.energy[1]=2;check(w.selectedSpell==1&&w.attack(100,206,1,true)&&w.energy[1]==0&&w.bolts.get(0).kind==5&&w.bolts.get(0).damage==13,"orb unlocks original fireball and energy cost");
        w.collectPowerOrb(57);w.cycleSpell();w.energy[2]=2;w.cooldown=0;check(w.selectedSpell==2&&w.attack(100,206,-1,true)&&w.energy[2]==0&&w.bolts.get(1).kind==8&&w.bolts.get(1).damage==12&&w.bolts.get(1).speed==-600,"orb unlocks original lightning and direction");w.collectPowerOrb(55);check(w.abilityLevels[0]==2&&ItemRules.supported(55)&&ItemRules.powerOrbChannel(58)==3,"power orbs increase correct ability levels");w.resetChapterEnergy(11,true);check(w.selectedSpell==0&&java.util.Arrays.equals(w.abilityLevels,new int[]{1,0,0,0}),"chapter resets ability progression and selection");
        w=new CombatWorld(CombatTest.map());w.collectPowerOrb(58);w.cycleSpell();w.energy[3]=1;check(w.selectedSpell==3&&w.attack(100,206,-1,true)&&w.energy[3]==0,"fourth power orb unlocks earth cast at original cost");CombatWorld.Bolt earth=w.bolts.get(0);check(earth.kind==6&&earth.damage==7&&earth.speed==-750&&Math.abs(earth.life-448f/750)<.0001f&&earth.y>=217&&earth.y<=236,"earth projectile original parameters and randomized height");check(Math.abs(w.cooldown-6f/75)<.0001f,"earth cast original cadence");w.update(6f/75,0,0);check(!w.attack(100,206,-1,true),"fourth cast cannot fire when energy empty");
        earth.age=0;check(earth.width()==63&&earth.height()==17,"earth first original frame geometry");earth.age=4f/75;check(earth.width()==64&&earth.height()==16,"earth second left frame geometry");
        System.out.println("Item tests passed");
    }
}
