package com.example.utils;

import com.example.PlayerModel;

import java.io.*;
import java.nio.file.Path;

public class SerializePlayerModel {

    public static String path;

    public static void setPath(String path) {
        SerializePlayerModel.path = path;
    }

    public static boolean isFileExists(String playerName) {
        File file = new File(path + playerName);
        return file.exists();
    }

    public static void writeObj(PlayerModel model) throws IOException {
        System.out.println("Writeing object to file: " + path + model.getPlayerName());
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path + model.getPlayerName()));
        oos.writeObject(model);
        oos.flush();
        oos.close();
    }

    public static PlayerModel readObj(String playerName) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path + playerName));
        PlayerModel model = (PlayerModel) ois.readObject();
        ois.close();
        return model;
    }
}
