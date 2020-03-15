package com.imdroid.utils;

import javazoom.jl.player.Player;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class VoiceUtil {
    private static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    public static void play(String filename) {
        singleThreadExecutor.execute(() -> {
            try {
                BufferedInputStream buffer = new BufferedInputStream(new FileInputStream(filename));
                Player player = new Player(buffer);
                player.play();
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        });
    }
}
