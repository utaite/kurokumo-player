package com.yuyu.kurokumoplayer.main;

public enum Action {

    PREV_ACTION("kurokumoplayer.action.prev"),
    PLAY_ACTION("kurokumoplayer.action.play"),
    NEXT_ACTION("kurokumoplayer.action.next"),
    STARTFOREGROUND_ACTION("kurokumoplayer.action.startforeground"),
    STOPFOREGROUND_ACTION("kurokumoplayer.action.stopforeground"),
    RIGHT_NO(1), RIGHT_SHUFFLE(2), LEFT_NO(3), LEFT_SHUFFLE(4),
    REPEAT_OFF(11), REPEAT_ONE(12), REPEAT_ALL(13),
    SHUFFLE_OFF(21), SHUFFLE_ON(22),
    NOTIFI_MOVE(31), NOTIFI_STOP(32), NOTIFI_PLAY_ON(33), NOTIFI_PLAY_OFF(34),
    RESUME_NONE(41), RESUME_TRUE(42), RESUME_FALSE(43);

    private String action;
    private int number;

    Action(String action) {
        this.action = action;
    }

    Action(int number) {
        this.number = number;
    }

    public int getAction() {
        return number;
    }

}
