package GameObjects;

//main feature - 25% of critical damage (2x damage)
public class Knife extends GameObject {
    private int damage;
    private int durability;
    private int maxDurability;
    private static final int CRIT_CHANCE_PERCENT = 25;
 
    public Knife(String name) {
        super(name, true); // portable = true
        this.damage = 15;
        this.durability = 20;
        this.maxDurability = 20;
    }
 
    //using knife - return damage
    //durability decreases
    public int use() {
        if (isBroken()) {
            System.out.println(name + " is broken and can not be used!");
            return 0;
        }
        durability--;
        if (isBroken()) {
            System.out.println(name + " is broken now after this!");
        }
 
        boolean isCrit = Math.random() * 100 < CRIT_CHANCE_PERCENT;
        if (isCrit) {
            System.out.println("Critical damage!");
            return damage * 2;
        }
        return damage;
    }
 
    public boolean isBroken()   { return durability <= 0; }
    public int getDamage()      { return damage; }
    public int getDurability()  { return durability; }
 
    @Override
    public String toString() {
        return name + " [Damage: " + damage + ", Durability: " + durability + "/" + maxDurability + "]";
    }
}
