package com.galeng.backend.entity;

import lombok.Data;

@Data
public class SnapshootSave {
    String gameId;
    String account;
    int saveId;
    String image;
    String saveText;
    int savePage;
    boolean haveData;
}
