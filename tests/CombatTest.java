import org.braveopendwarves.data.*;
public final class CombatTest {
    static void put(byte[] b,int p,int n){for(int i=0;i<4;i++)b[p+i]=(byte)(n>>(i*8));}
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    static OriginalLevel map() throws Exception {
        byte[] b=new byte[2048+8*5*24];put(b,0,8);put(b,4,5);put(b,332,1);put(b,336,2);put(b,360,1);
        for(int x=0;x<8;x++)put(b,2048+(4*8+x)*24,1);
        return new OriginalLevel(b);
    }
    public static void main(String[] args)throws Exception{
        CombatWorld w=new CombatWorld(map());CombatWorld.Foe f=new CombatWorld.Foe(4,160,206);w.foes.add(f);f.hp=8;
        check(w.attack(100,206,1,false),"melee accepted");check(f.hp<=0&&w.kills==1,"melee kill");
        check(!w.attack(100,206,1,false),"cooldown enforced");
        w=new CombatWorld(map());f=new CombatWorld.Foe(4,160,206);w.foes.add(f);
        w.attack(100,206,-1,false);check(f.hp==50,"melee direction");
        w=new CombatWorld(map());f=new CombatWorld.Foe(4,220,206);w.foes.add(f);
        for(int i=0;i<240;i++){w.attack(100,206,1,true);w.update(1f/120,100,206);}
        check(f.hp<=0&&w.kills==1,"projectiles hit and kill once");
        w=new CombatWorld(map());f=new CombatWorld.Foe(4,100,206);w.foes.add(f);
        w.update(.01f,100,206);check(w.health==15,"contact damage");check(Math.abs(w.immunity-170f/75)<.001f,"hurt phase plus original protection duration");
        for(int i=0;i<50;i++)w.update(.01f,f.x,f.y);check(w.health==15,"immunity window");
        w.health=1;w.immunity=0;w.update(.01f,f.x,f.y);check(w.health==0,"death");
        check(!w.attack(100,206,1,true),"dead hero cannot attack");
        w=new CombatWorld(map());f=new CombatWorld.Foe(4,475,206);w.foes.add(f);
        for(int i=0;i<600;i++)w.update(1f/120,0,0);check(f.x>=0&&f.x<480,"patrol stays within wall boundaries");
        w.health=19;w.heal();w.heal();check(w.health==20,"health cap");
        OriginalLevel blocked=map();blocked.cell(2,3)[0]=1;
        w=new CombatWorld(blocked);f=new CombatWorld.Foe(4,180,206);w.foes.add(f);
        w.attack(100,206,1,false);check(f.hp==50,"melee cannot cross wall");
        w=new CombatWorld(blocked);w.attack(100,206,1,true);check(w.bolts.isEmpty(),"muzzle cannot spawn inside wall");
        w=new CombatWorld(blocked);w.attack(50,206,1,true);w.update(.5f,50,206);check(w.bolts.isEmpty(),"projectile cannot tunnel through wall");
        OriginalLevel edge=map();edge.cell(3,4)[0]=0;
        w=new CombatWorld(edge);f=new CombatWorld.Foe(4,160,206);w.foes.add(f);
        for(int i=0;i<600;i++)w.update(1f/120,0,0);check(f.x+32<=192&&f.y<=206.1f,"patrol avoids missing floor");
        w=new CombatWorld(map());f=new CombatWorld.Foe(3,200,100);w.foes.add(f);
        for(int i=0;i<120;i++)w.update(1f/120,0,0);check(Math.abs(f.y-100)<.01f,"bat does not fall");
        w=new CombatWorld(map());f=new CombatWorld.Foe(2,200,200);w.foes.add(f);
        for(int i=0;i<120;i++)w.update(1f/120,0,0);check(Math.abs(f.y+20-256)<.1f,"worm rests on floor using short body");
        w=new CombatWorld(map());f=new CombatWorld.Foe(6,400,232);w.foes.add(f);
        for(int i=0;i<360;i++)w.update(1f/120,100,206);check(w.health<20,"crawler projectile damages hero");check(f.hp==15,"hostile bolt does not damage shooter");
        w=new CombatWorld(map());f=new CombatWorld.Foe(5,160,196);w.foes.add(f);w.attack(100,206,1,false);check(f.hp==40&&w.kills==0,"large skeleton survives one melee strike");
        System.out.println("PASS combat: melee, facing, cooldown, projectiles, damage, immunity, death, patrol, healing");
    }
}

