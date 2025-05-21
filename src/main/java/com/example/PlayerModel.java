package com.example;

import com.llamalad7.mixinextras.sugar.Local;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * This class is saved as playerName.object.
 * It contains the player's name, last active time, and the time they were last online.
 * If the activity time is greater than the timeout time, the player is kicked.
 * This class contains also the timeout data. The player can set itself a timeout.
 * N.B: Once the time-out is reached, the player is kicked, and he can't change the timeout.
 */

/**
 * This class can track all time of game (in the day) and the time of the last activity.
 * I think I can save session interval time (Player joined and left).
 */
public class PlayerModel implements Serializable {
    private String playerName;
    private LocalTime playerJoinTime;
    private LocalTime playerLeftTime;
    private LocalTime lastPoolingTime;
    private LocalTime totalLastTime;
    private Date lastActiveTime;
    private LocalTime timeOut;

    public PlayerModel(String playerName, LocalTime playerJoinTime) {
        this.playerName = playerName;
        this.playerJoinTime = playerJoinTime;
        this.lastActiveTime = Date.from(new Date().toInstant());
    }

    public PlayerModel(String playerName, LocalTime playerJoinTime, Date lastActiveTime) {
        this.playerName = playerName;
        this.playerJoinTime = playerJoinTime;
        this.lastActiveTime = lastActiveTime;
    }

    public boolean isBeforeToday() {
        if (lastActiveTime == null) {
            return false;
        }
        LocalDate dateToCheck = lastActiveTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();
        return dateToCheck.isBefore(today);
    }

    public LocalTime getTimeOut() {
        return timeOut;
    }

    public boolean isTimeOut() {
        if (timeOut == null) {
            return false;
        }
        return getTotalTime().isAfter(timeOut);
    }

    public void setTimeOut(LocalTime timeOut) {
        this.timeOut = timeOut;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public LocalTime getPlayerJoinTime() {
        return playerJoinTime;
    }

    public void setPlayerJoinTime(LocalTime playerJoinTime) {
        this.playerJoinTime = playerJoinTime;
    }

    public LocalTime getPlayerLeftTime() {
        return playerLeftTime;
    }

    public void setPlayerLeftTime(LocalTime playerLeftTime) {
        this.playerLeftTime = playerLeftTime;
    }

    public LocalTime getTotalTime() {

        if (lastPoolingTime == null)
            lastPoolingTime = getPlayerJoinTime();

        LocalTime currentTime = LocalTime.now();

        long secondsElapsed = java.time.Duration.between(this.lastPoolingTime, currentTime).getSeconds();
        long totalSeconds = (this.totalLastTime != null ? this.totalLastTime.toSecondOfDay() : 0) + secondsElapsed;
        totalLastTime = LocalTime.ofSecondOfDay(totalSeconds);
        lastPoolingTime = currentTime;
        return totalLastTime;
    }

    public void setLastPoolingTime(LocalTime lastPoolingTime) {
        this.lastPoolingTime = lastPoolingTime;
    }

    public void setTotalLastTime(LocalTime totalLastTime) {
        this.totalLastTime = totalLastTime;
    }

    public Date getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(Date lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }
}
