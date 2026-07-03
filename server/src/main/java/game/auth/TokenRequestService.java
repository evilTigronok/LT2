package game.auth;

import java.util.HashMap;
import java.util.Map;

public class TokenRequestService {

    private Map<String, Long> lastRequest =
            new HashMap<>();

    private final long COOLDOWN =
            60 * 60 * 1000; // 1 hour

    public boolean canRequest(String user) {

        long now = System.currentTimeMillis();

        if (!lastRequest.containsKey(user)) {
            lastRequest.put(user, now);
            return true;
        }

        long last = lastRequest.get(user);

        if (now - last >= COOLDOWN) {
            lastRequest.put(user, now);
            return true;
        }

        return false;
    }
}