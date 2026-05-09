package GameObjects;

public class FirstAidKit extends GameObject {
    private int healAmount;

    public FirstAidKit(String name, int healAmount) {
        super(name, true); 
        this.healAmount = healAmount;
    }

    public int getHealAmount() {
        return healAmount;
    }
}