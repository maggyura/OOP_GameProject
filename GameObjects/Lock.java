package GameObjects;

public class Lock {
    private boolean isLocked;

    public Lock(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public boolean isLocked() { return isLocked; }

    // it allows to open the lock with a key
    public boolean unlock(GameObject item) {
        if (item instanceof Key) {
            this.isLocked = false;
            return true;
        }
        return false;
    }

    public void setLocked(boolean locked) {
        this.isLocked = locked;
    }
}
