package com.galeng.backend.entity;

import lombok.Data;


@Data
public class Cg {
    String gameId;
    String account;
    int cgId;
    int cgNum;
    int cgPage;
    boolean haveData;
    String littleCg;
    String cgList;
}
