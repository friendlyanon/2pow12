package com.github.friendlyanon.twopowtwelve.game;

import com.github.friendlyanon.twopowtwelve.util.FastList;
import lombok.val;

import javax.sound.sampled.*;
import java.util.HashMap;
import java.util.Optional;

public class Audio {
    private static Audio audio;
    private static boolean errorState;
    private static boolean loaded;
    private HashMap<Resources, Clip> sounds = new HashMap<>();
    private FastList<Clip> ingameClips;
    private LineListener listener = this::listenHandler;

    private Audio() {}

    public static Audio getInstance() {
        return audio == null ? (audio = new Audio()) : audio;
    }

    private void listenHandler(LineEvent lineEvent) {
        if (lineEvent.getType() != LineEvent.Type.STOP) {
            return;
        }
        ((Clip) lineEvent.getSource()).setFramePosition(0);
        val clip = ingameClips.get((int) (Math.random() * ingameClips.size()));
        clip.loop(0);
    }

    private void loadClip(Resources res, AudioInputStream input) {
        val format = input.getFormat();
        if (format == null) {
            return;
        }
        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
            try {
                val clip = AudioSystem.getClip();
                clip.open(input);
                sounds.put(res, clip);
                return;
            }
            catch (Exception ignored) {
            }
        }

        val decoded = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            format.getSampleRate(),
            16,
            format.getChannels(),
            format.getChannels() << 1,
            format.getSampleRate(),
            false
        );

        val decodedInput = AudioSystem.getAudioInputStream(decoded, input);

        try {
            val clip = AudioSystem.getClip();
            clip.open(decodedInput);
            sounds.put(res, clip);
        }
        catch (Exception ignored) {
            errorState = true;
        }
    }

    private boolean loadAudio(Resources res) {
        if (errorState) {
            return false;
        }
        if (sounds.containsKey(res)) {
            return true;
        }
        Optional
            .ofNullable(getClass().getClassLoader().getResource(res.filename))
            .flatMap(x -> {
                try {
                    return Optional.ofNullable(AudioSystem.getAudioInputStream(x));
                }
                catch (Exception ignored) {
                    return Optional.empty();
                }
            })
            .ifPresentOrElse(x -> loadClip(res, x), () -> errorState = true);
        return !errorState;
    }

    public void loadAudioSamples() {
        if (loaded || errorState) {
            return;
        }
        for (val value : Resources.values()) {
            if (!loadAudio(value)) {
                return;
            }
        }
        ingameClips = new FastList<>(sounds.size() - 1);
        for (val pair : sounds.entrySet()) {
            if (pair.getKey() == Resources.TITLE) {
                continue;
            }
            ingameClips.add(pair.getValue());
        }
        loaded = true;
    }

    private void resetClips() {
        muteIngame();
        val clip = sounds.get(Resources.TITLE);
        clip.setFramePosition(0);
        clip.stop();
    }

    public void muteIngame() {
        for (val clip : ingameClips) {
            clip.removeLineListener(listener);
            clip.setFramePosition(0);
            clip.stop();
        }
    }

    public void playTitle() {
        if (!loaded || errorState) {
            return;
        }
        resetClips();
        val clip = sounds.get(Resources.TITLE);
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void playIngame() {
        if (!loaded || errorState) {
            return;
        }
        resetClips();
        for (val clip : ingameClips) {
            clip.addLineListener(listener);
        }
        ingameClips.get((int) (Math.random() * ingameClips.size())).loop(0);
    }

    private enum Resources {
        TITLE("562520_Chill---Game-Loop-A"),
        INGAME1("581605_Evil-Fantasy-Trance-Loop"),
        INGAME2("522943_Antiskill--PredatorLoop"),
        INGAME3("555786_Play---Game-Loop-A"),
        INGAME4("572013_quotStrawberryquot---Game-");

        private String filename;

        Resources(String filename) {
            this.filename = "audio/" + filename + ".mp3";
        }
    }
}
