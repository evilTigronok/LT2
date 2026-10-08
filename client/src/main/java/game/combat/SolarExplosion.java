package game.combat;

public class SolarExplosion {

    private final float x;
    private final float y;

    private final float maxRadius;
    private final float duration;

    private float time;

    public SolarExplosion(float x, float y, float maxRadius, float duration) {
        this.x = x;
        this.y = y;
        this.maxRadius = maxRadius;
        this.duration = duration;
        this.time = 0f;
    }

    public void update(float deltaSeconds) {
        time += deltaSeconds;
    }

    public boolean isFinished() {
        return time >= duration;
    }

    public float getProgress() {
        if (duration <= 0f) {
            return 1f;
        }

        return Math.min(1f, time / duration);
    }

    public float getCurrentRadius() {
        return maxRadius * getProgress();
    }

    public float getAlpha() {
        float progress = getProgress();

        // Быстро появляется, затем постепенно исчезает.
        if (progress < 0.25f) {
            return progress / 0.25f;
        }

        return 1f - ((progress - 0.25f) / 0.75f);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getMaxRadius() {
        return maxRadius;
    }
}