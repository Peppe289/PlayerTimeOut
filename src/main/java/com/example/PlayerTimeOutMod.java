package com.example;

import com.example.utils.SerializePlayerModel;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerTimeOutMod implements ModInitializer {
    public static final String MOD_ID = "PlayerTimeOutMod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final ExecutorService THREADPOOL = Executors.newCachedThreadPool();
    public static Path gameDirectory;
    public static HashMap<String, PlayerModel> playerModels = new HashMap<>();

    public synchronized static HashMap<String, PlayerModel> getPlayerModels() {
        return playerModels;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Loading...");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CommandPlayerTime.registerCommand(dispatcher);
        });

        gameDirectory = FabricLoader.getInstance().getGameDir();
        File file = new File(gameDirectory + "/config/PlayerTimeOut");
        if (!file.exists()) {
            if (!file.mkdir()) {
                throw new RuntimeException("Could not create directory " + file.getAbsolutePath());
            }
        }

        SerializePlayerModel.setPath(gameDirectory + "/config/PlayerTimeOut/");

        ServerPlayConnectionEvents.JOIN.register((header, sender, server) -> {
            ServerPlayerEntity player = header.getPlayer();
            String playerName = player.getNameForScoreboard();

            PlayerModel playerModel = null;

            /*
             * Check if SerializePlayerModel file exists.
             */
            if (SerializePlayerModel.isFileExists(playerName)) {
                try {
                    playerModel = SerializePlayerModel.readObj(playerName);
                    /*
                     * If last access isn't today, reset all other values.
                     */
                    if (playerModel.isBeforeToday()) {
                        playerModel.setPlayerJoinTime(LocalTime.now());
                        playerModel.setPlayerLeftTime(null);
                        playerModel.setTotalLastTime(null);
                        playerModel.setLastActiveTime(Date.from(new Date().toInstant()));
                    }
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            if (playerModel == null) {
                playerModel = new PlayerModel(playerName, LocalTime.now());
            }

            playerModel.setLastPoolingTime(null);

            playerModel.setPlayerJoinTime(LocalTime.now());
            getPlayerModels().put(playerName, playerModel);

            final PlayerModel finalPlayerModel = playerModel;
            THREADPOOL.submit(() -> {
                //LOGGER.info("Checking timeout for player: " + playerName);
                while (!player.isDisconnected()) {
                    //LOGGER.info("Checking timeout for player: " + playerName);
                    //LOGGER.info("Current time: " + finalPlayerModel.getTotalTime().toString());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    if (finalPlayerModel.isTimeOut())
                        player.networkHandler.disconnect(Text.literal("Go to study!"));
                }
            });
        });

        ServerPlayConnectionEvents.DISCONNECT.register((header, server) -> {
            ServerPlayerEntity player = header.getPlayer();
            String playerName = player.getNameForScoreboard();

            PlayerModel playerModel = getPlayerModels().get(playerName);
            playerModel.setPlayerLeftTime(LocalTime.now());

            playerModel.getTotalTime();
            getPlayerModels().remove(playerName);

            try {
                SerializePlayerModel.writeObj(playerModel);
            } catch (IOException e) {
                LOGGER.error("Error saving player model data for {}", playerName, e);
            }
        });
    }
}