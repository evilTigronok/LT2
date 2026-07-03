package game.sessions;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {

    private Map<String, ClientSession> sessions =
            new HashMap<>();

    public ClientSession createSession(String clientId) {

        ClientSession session =
                new ClientSession();

        sessions.put(clientId, session);

        return session;
    }

    public ClientSession getSession(String clientId) {
        return sessions.get(clientId);
    }

    public void removeSession(String clientId) {
        sessions.remove(clientId);
    }
}