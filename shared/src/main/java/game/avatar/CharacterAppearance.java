package game.avatar;

import java.util.ArrayList;
import java.util.List;

public class CharacterAppearance {

    private CharacterBody body;
    private CharacterClothing clothing;
    private final List<CharacterAccessory> accessories;

    public CharacterAppearance() {
        this.body = new CharacterBody();
        this.clothing = new CharacterClothing();
        this.accessories = new ArrayList<>();
    }

    public CharacterBody getBody() {
        return body;
    }

    public void setBody(CharacterBody body) {
        this.body = body;
    }

    public CharacterClothing getClothing() {
        return clothing;
    }

    public void setClothing(CharacterClothing clothing) {
        this.clothing = clothing;
    }

    public List<CharacterAccessory> getAccessories() {
        return accessories;
    }

    public void addAccessory(CharacterAccessory accessory) {
        accessories.add(accessory);
    }
}