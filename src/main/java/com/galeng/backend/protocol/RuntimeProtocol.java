package com.galeng.backend.protocol;

import com.galeng.backend.entity.Lihui;
import com.galeng.backend.entity.SelectItem;
import lombok.Data;

import java.util.ArrayList;

@Data
public class RuntimeProtocol implements Protocol {
    int pc = -1;

    boolean setLihui = false;
    ArrayList<Lihui> LihuiList = null;

    //    public void updateLihuiList(int lihuiId, String lihuiSrc, String animationName, String animationDuration, String animationDelay, String jumpUp, String rightMove, String leftMove) {
    public void updateLihuiList(StringBuffer... args) {
        Lihui temp = LihuiList.get(LihuiList.size() - 1);
        temp.setLihuiId(Integer.parseInt(args[0].toString()));
        temp.setLihuiSrc(args[1].toString());
        temp.setAnimationName(args[2].toString());
        temp.setAnimationDelay(args[3].toString());
        temp.setAnimationDuration(args[4].toString());
        temp.setJumpUp(args[5].toString());
        temp.setLeftMove(args[6].toString());
        temp.setRightMove(args[7].toString());
    }

    /**
     * voice与text一组
     */
    boolean setText = false;
    String storyVoiceUrl = null;
    String storyBackendText = null;

    boolean setBgm = false;
    String storyBgmUrl = null;

    boolean setSe = false;
    String storySeUrl = null;

    boolean setBg = false;
    String StoryBgUrl = null;

    boolean setBgAnimation = false;
    String StoryBgAnimationName = null;
    String StoryBgAnimationDelay = null;
    String StoryBgAnimationDuration = null;
    String StoryBgShakeUp = null;

    boolean setMask = false;
    String StoryMaskUrl = null;

    boolean setMaskAnimation = false;
    String StoryMaskAnimationName = null;
    String StoryMaskAnimationDelay = null;
    String StoryMaskAnimationDuration = null;

    boolean setDialog = false;
    String storyDialogUrl = null;

    boolean setSelect = false;
    ArrayList<SelectItem> selectList;

    public void updateSelectList(StringBuffer... args) {
        SelectItem temp = selectList.get(selectList.size() - 1);
        temp.setSelectId(Integer.parseInt(args[0].toString()));
        temp.setLikabilityId(args[1].toString());
        temp.setImproveNum(Integer.parseInt(args[2].toString()));
    }
}
