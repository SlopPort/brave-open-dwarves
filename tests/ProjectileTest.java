import org.braveopendwarves.data.*;
public final class ProjectileTest {
    static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    public static void main(String[] args)throws Exception{
        CombatWorld w=new CombatWorld(CombatTest.map());CombatWorld.Bolt friendly=new CombatWorld.Bolt(160,210,0,false,1),enemy=new CombatWorld.Bolt(170,210,0,true,25);w.bolts.add(friendly);w.bolts.add(enemy);w.update(.01f,0,0);
        check(w.bolts.size()==1&&friendly.damage==5,"strong projectile survives with reduced damage");
        CombatWorld.Foe foe=new CombatWorld.Foe(4,144,185);w.foes.add(foe);w.update(.01f,0,0);check(foe.hp==45&&w.bolts.isEmpty(),"remaining projectile damage applied to enemy");
        w=new CombatWorld(CombatTest.map());friendly=new CombatWorld.Bolt(160,210,0,false,1);enemy=new CombatWorld.Bolt(170,210,0,true,3);friendly.damage=4;w.bolts.add(friendly);w.bolts.add(enemy);w.update(.01f,0,0);check(w.bolts.size()==2&&friendly.damage==0&&enemy.damage==0,"equal projectiles retain zero damage like original");
        w.update(.01f,144,185);check(w.health==20,"zero damage projectile does not hurt hero");
        w=new CombatWorld(CombatTest.map());w.bolts.add(new CombatWorld.Bolt(160,210,0,true,3));w.bolts.add(new CombatWorld.Bolt(170,210,0,true,25));w.update(.01f,0,0);check(w.bolts.size()==2&&w.bolts.get(0).damage==4,"same side projectiles do not intercept");
        System.out.println("Projectile interception tests passed");
    }
}
