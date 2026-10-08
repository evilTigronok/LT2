package game.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.prefs.Preferences;

public final class MusicManager {

    private static final Preferences PREFERENCES =
            Preferences.userNodeForPackage(MusicManager.class);

    private static final String VOLUME_KEY = "music_volume";

    /**
     * Громкость музыки в диапазоне 0.0 - 1.0.
     */
    private static double volume =
            clamp(PREFERENCES.getDouble(VOLUME_KEY, 1.0));

    private static MediaPlayer currentPlayer;

    private MusicManager() {
    }

    public static void playLoop(String resourcePath) {

        stop();

        URL url =
                MusicManager.class.getResource(resourcePath);

        if (url == null) {

            System.err.println(
                    "Music resource not found: " +
                            resourcePath
            );

            return;
        }

        try {

            Media media =
                    new Media(
                            url.toExternalForm()
                    );

            MediaPlayer player =
                    new MediaPlayer(media);

            player.setCycleCount(
                    MediaPlayer.INDEFINITE
            );

            player.setVolume(volume);

            player.setOnError(() ->
                    System.err.println(
                            "Music playback error: " +
                                    player.getError()
                    )
            );

            currentPlayer = player;

            player.play();

        } catch (Exception e) {

            System.err.println(
                    "Could not start music: " +
                            resourcePath
            );

            e.printStackTrace();
        }
    }

    /**
     * Устанавливает громкость музыки.
     *
     * @param value значение от 0.0 до 1.0
     */
    public static void setVolume(double value) {

        volume = clamp(value);

        PREFERENCES.putDouble(
                VOLUME_KEY,
                volume
        );

        if (currentPlayer != null) {

            currentPlayer.setVolume(volume);
        }
    }

    /**
     * Возвращает текущую громкость музыки.
     *
     * @return значение от 0.0 до 1.0
     */
    public static double getVolume() {

        return volume;
    }

    public static void stop() {

        if (currentPlayer != null) {

            try {

                currentPlayer.stop();
                currentPlayer.dispose();

            } catch (Exception ignored) {
            }

            currentPlayer = null;
        }
    }

    private static double clamp(double value) {

        return Math.max(
                0.0,
                Math.min(1.0, value)
        );
    }
}