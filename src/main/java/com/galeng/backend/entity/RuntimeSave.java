package com.galeng.backend.entity;

import lombok.Data;

@Data
public class RuntimeSave {
    int pc;
    int likAbility1;
    int likAbility2;
    int likAbility3;
    String storyBgmUrl;
    String storyDialogUrl;
    String storySeUrl;
    String storyText;
    boolean storyMode;
    boolean selectMode;
    int savePage;
    int saveId;
    String account;
    String gameId;
    String storyVoiceUrl;
    String storyBgUrl;
    String storyBgAnimationName;
    String storyBgAnimationDuration;
    String storyBgAnimationDelay;
    String storyBgShakeUp;
    String storyMaskUrl;
    String storyMaskAnimationName;
    String storyMaskAnimationDuration;
    String storyMaskAnimationDelay;
}
