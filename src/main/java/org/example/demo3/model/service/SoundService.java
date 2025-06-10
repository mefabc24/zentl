package org.example.demo3.model.service;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.example.demo3.model.cards.Card;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoundService {
    private static SoundService instance;
    private final Map<String, Media> soundCache = new HashMap<>();
    private final String soundDirectory = "/org/example/demo3/assets/audio/";
    private final String defaultSoundName = "default.mp3";
    private final String menuMusicName = "MenuTheme.mp3";

    private MediaPlayer menuMusicPlayer;
    private Timeline fadeOutTimeline;
    private final List<MediaPlayer> activeSoundEffects = new ArrayList<>();

    private SoundService() {
        preloadSound(defaultSoundName);
        preloadSound(menuMusicName);
        initializeMenuMusic();
    }

    public static synchronized SoundService getInstance() {
        if (instance == null) {
            instance = new SoundService();
        }
        return instance;
    }

    private void initializeMenuMusic() {
        Media menuMusic = soundCache.get(menuMusicName);
        if (menuMusic != null) {
            menuMusicPlayer = new MediaPlayer(menuMusic);
            menuMusicPlayer.setOnEndOfMedia(() -> menuMusicPlayer.seek(Duration.ZERO));
            menuMusicPlayer.setVolume(0.5);
            System.out.println("Menu music initialized.");
        } else {
            System.err.println("Failed to initialize menu music because it was not preloaded.");
        }
    }

    public void startMenuMusic() {
        if (menuMusicPlayer == null) return;
        if (fadeOutTimeline != null && fadeOutTimeline.getStatus() == Timeline.Status.RUNNING) {
            fadeOutTimeline.stop();
        }
        if (menuMusicPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            menuMusicPlayer.setVolume(0.5);
            System.out.println("Starting menu music...");
            menuMusicPlayer.play();
        }
    }

    public void stopMenuMusic() {
        if (menuMusicPlayer == null || menuMusicPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            return;
        }
        System.out.println("Fading out menu music...");
        fadeOutTimeline = new Timeline(
                new KeyFrame(Duration.seconds(2), new KeyValue(menuMusicPlayer.volumeProperty(), 0))
        );
        fadeOutTimeline.setOnFinished(event -> {
            menuMusicPlayer.stop();
            System.out.println("Menu music stopped after fade-out.");
        });
        fadeOutTimeline.play();
    }
    
    public void playSoundForCard(Card card) {
        // Step 1: Determine the name of the specific sound to try.
        if (card == null || card.getImagePath() == null || card.getImagePath().isEmpty()) {
            playSound(defaultSoundName); // Play default if card is invalid
            return;
        }
        String imageName = new File(card.getImagePath()).getName();
        String specificSoundName = imageName.replaceAll("\\.\\w+$", ".mp3");

        // Step 2: Try to play the specific sound. The playSound method now returns a boolean.
        boolean success = playSound(specificSoundName);

        // Step 3: If playing the specific sound failed, play the default sound.
        if (!success) {
            playSound(defaultSoundName);
        }
    }

    /**
     * Tries to play a sound by its name.
     * @param soundName The name of the sound file (e.g., "Geralt.mp3").
     * @return true if the sound could be played, false otherwise.
     */
    private boolean playSound(String soundName) {
        // First, try to get the sound from the cache.
        Media sound = soundCache.get(soundName);

        // If not in cache, try to load it now.
        if (sound == null) {
            sound = loadSound(soundName); // Use a dedicated loading method
        }

        // If after trying to load it's still null, the file doesn't exist. Abort.
        if (sound == null) {
            return false;
        }

        // If we have a valid Media object, play it.
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        activeSoundEffects.add(mediaPlayer);

        mediaPlayer.setOnEndOfMedia(() -> {
            activeSoundEffects.remove(mediaPlayer);
            mediaPlayer.dispose();
        });

        mediaPlayer.setOnError(() -> {
            System.err.println("MediaPlayer Error for sound: " + soundName);
            if (mediaPlayer.getError() != null) {
                mediaPlayer.getError().printStackTrace();
            }
            activeSoundEffects.remove(mediaPlayer);
        });

        mediaPlayer.play();
        return true; // Successfully started playing
    }

    /**
     * Loads a sound and puts it into the cache.
     * This method is now separate from preloading for clarity.
     * @param soundName The name of the sound file.
     * @return The Media object if successful, otherwise null.
     */
    private Media loadSound(String soundName) {
        if (soundName == null || soundName.isEmpty()) return null;
        // If it's already in the cache, just return it.
        if (soundCache.containsKey(soundName)) {
            return soundCache.get(soundName);
        }
        try {
            URL resourceUrl = getClass().getResource(soundDirectory + soundName);
            if (resourceUrl == null) {
                // This is not an error, it just means the specific file doesn't exist.
                return null;
            }
            Media sound = new Media(resourceUrl.toExternalForm());
            soundCache.put(soundName, sound);
            // System.out.println("Dynamically loaded sound: " + soundName);
            return sound;
        } catch (Exception e) {
            System.err.println("Failed to load sound: '" + soundName + "'. Reason: " + e.getMessage());
            return null;
        }
    }

    // Preload is now just a specific use case of loadSound
    private void preloadSound(String soundName) {
        System.out.println("Preloading sound: " + soundName);
        if (loadSound(soundName) == null) {
            System.err.println("----> CRITICAL: Failed to preload essential sound: " + soundName);
        }
    }
}