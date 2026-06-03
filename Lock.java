package GameObjects;

public class Lock {
    private boolean isLocked;

    public Lock(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public boolean isLocked() { return isLocked; }

    public void setLocked(boolean locked) {
        this.isLocked = locked;
    }
}