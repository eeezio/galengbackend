package com.galeng.backend.instruct;


import com.galeng.backend.cache.LRUCache;
import com.galeng.backend.entity.Lihui;
import com.galeng.backend.entity.RuntimeSave;
import com.galeng.backend.entity.SelectItem;
import com.galeng.backend.protocol.RuntimeProtocol;
import com.galeng.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

@Repository
public class Decoder {

    private ArrayList<String> script = new ArrayList<>();

    LRUCache<String, RuntimeSave> runtimeSaveLRUCache;

    public void setScript(String instruct) {
        script.add(instruct);
    }

    public void setRuntimeSaveLRUCache(LRUCache<String, RuntimeSave> runtimeSaveLRUCache) {
        this.runtimeSaveLRUCache = runtimeSaveLRUCache;
    }

    public LRUCache getRuntimeSaveLRUCache() {
        return runtimeSaveLRUCache;
    }

    private boolean endOfSubStatement(String instruct, int tag) {
        return instruct.charAt(tag) == ';' && instruct.charAt(tag - 1) != 'λ';
    }

    private int removePrefix(String instruct, int tag) {
        while (':' != instruct.charAt(tag - 1) && tag < instruct.length()) {
            tag++;
        }
        return tag;
    }

    private void addInstructField(String instruct, int tag, StringBuffer instructField) {
        while (tag < instruct.length() && !endOfSubStatement(instruct, tag)) {
            if (instruct.charAt(tag) == 'λ') tag++;
            instructField.append(instruct.charAt(tag++));
        }
    }

    private void setProtocol(int tag, boolean setObject, String instruct, RuntimeProtocol protocol, List<String> funcName, StringBuffer... instructFields) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        int count = 0;
        while (tag < instruct.length()) {
            tag = removePrefix(instruct, tag);
            addInstructField(instruct, tag, instructFields[count]);
            if (!setObject) {
                Method method = protocol.getClass().getMethod(funcName.get(count), instructFields[count].toString().getClass());
                method.invoke(protocol, instructFields[count].toString());
            }
            count++;
            tag++;
        }
        if (setObject) {
            Method method = protocol.getClass().getMethod(funcName.get(count), instructFields.getClass());
            method.invoke(protocol, instructFields);
        }
    }

    private boolean testLogicBool(String logicFormula) {
        Stack<Byte> stack = new Stack<>();
        for (Byte i :
                logicFormula.getBytes()) {
            switch (i) {
                case '(':
                    stack.push(i);
                case ')':
            }
        }
        return false;
    }


    public RuntimeProtocol decode(int pc, RuntimeProtocol runtimeProtocol) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (runtimeProtocol == null) {
            runtimeProtocol = new RuntimeProtocol();
        }
        String instruct = script.get(pc);
        StringBuffer instructHead = new StringBuffer();
        instructHead.append(instruct.charAt(0));
        int tag = 1;
        while (instruct.charAt(tag - 1) != '>') {
            instructHead.append(instruct.charAt(tag));
            tag += 1;
        }
        List<String> funcName = new ArrayList<>();
        String head = instructHead.toString();
        switch (head) {
            case "<text>":
                StringBuffer storyVoiceUrl = new StringBuffer();
                StringBuffer storyBackendText = new StringBuffer();
                funcName.add("setStoryBackendText");
                funcName.add("setStoryVoiceUrl");
                runtimeProtocol.setSetText(true);
                runtimeProtocol.setPc(pc + 1);
                setProtocol(tag, false, instruct, runtimeProtocol, funcName, storyBackendText, storyVoiceUrl);
                return runtimeProtocol;
            case "<bgm>":
                StringBuffer storyBgmUrl = new StringBuffer();
                runtimeProtocol.setSetBgm(true);
                runtimeProtocol.setPc(pc + 1);
                funcName.add("setStoryBgmUrl");
                setProtocol(tag, false, instruct, runtimeProtocol, funcName, storyBgmUrl);
                decode(pc + 1, runtimeProtocol);
            case "<se>":
                StringBuffer storySeUrl = new StringBuffer();
                runtimeProtocol.setSetSe(true);
                runtimeProtocol.setPc(pc + 1);
                funcName.add("setStorySeUrl");
                setProtocol(tag, false, instruct, runtimeProtocol, funcName, storySeUrl);
                decode(pc + 1, runtimeProtocol);
            case "<bg>":
                StringBuffer storyBgUrl = new StringBuffer();
                runtimeProtocol.setSetBg(true);
                runtimeProtocol.setPc(pc + 1);
                funcName.add("setStoryBgUrl");
                setProtocol(tag, false, instruct, runtimeProtocol, funcName, storyBgUrl);
                decode(pc + 1, runtimeProtocol);
            case "<bg_animation>":
                StringBuffer storyBgAnimationName = new StringBuffer();
                StringBuffer storyBgAnimationDelay = new StringBuffer();
                StringBuffer storyBgAnimationDuration = new StringBuffer();
                StringBuffer storyBgShakeUp = new StringBuffer();
                runtimeProtocol.setSetBgAnimation(true);
                runtimeProtocol.setPc(pc + 1);
                funcName.add("setStoryBgAnimationName");
                funcName.add("setStoryBgAnimationDelay");
                funcName.add("setStoryBgAnimationDuration");
                funcName.add("setStoryBgShakeUp");
                setProtocol(tag, false, instruct, runtimeProtocol, funcName, storyBgAnimationName, storyBgAnimationDelay, storyBgAnimationDuration, storyBgShakeUp);
                decode(pc + 1, runtimeProtocol);
            case "<mask_animation>":
                StringBuffer storyMaskAnimationName = new StringBuffer();
                StringBuffer storyMaskAnimationDelay = new StringBuffer();
                StringBuffer storyMaskAnimationDuration = new StringBuffer();
                runtimeProtocol.setSetMaskAnimation(true);
                runtimeProtocol.setPc(pc + 1);
                funcName.add("setStoryMaskAnimationName");
                funcName.add("setStoryMaskAnimationDelay");
                funcName.add("setStoryMaskAnimationDuration");
                setProtocol(tag, false, instruct, runtimeProtocol, funcName, storyMaskAnimationName, storyMaskAnimationDelay, storyMaskAnimationDuration);
                decode(pc + 1, runtimeProtocol);
            case "<mask>":
                StringBuffer storyMaskUrl = new StringBuffer();
                funcName.add("setStoryMaskUrl");
                runtimeProtocol.setSetMask(true);
                runtimeProtocol.setPc(pc + 1);
                setProtocol(tag, false, instruct, runtimeProtocol, funcName, storyMaskUrl);
                decode(pc + 1, runtimeProtocol);
            case "<lihui>":
                StringBuffer LihuiId = new StringBuffer();
                StringBuffer LihuiSrc = new StringBuffer();
                StringBuffer animationName = new StringBuffer();
                StringBuffer animationDuration = new StringBuffer();
                StringBuffer animationDelay = new StringBuffer();
                StringBuffer JumpUp = new StringBuffer();
                StringBuffer rightMove = new StringBuffer();
                StringBuffer leftMove = new StringBuffer();
                runtimeProtocol.setSetLihui(true);
                runtimeProtocol.setPc(pc + 1);
                if (runtimeProtocol.getLihuiList() == null) {
                    runtimeProtocol.setLihuiList(new ArrayList<Lihui>());
                }
                runtimeProtocol.getLihuiList().add(new Lihui());
                funcName.add("updateLihuiList");
                setProtocol(tag, true, instruct, runtimeProtocol, funcName, LihuiId, LihuiSrc, animationName, animationDelay, animationDuration, JumpUp, leftMove, rightMove);
                decode(pc + 1, runtimeProtocol);
            case "<dialog>":
                StringBuffer storyDialogUrl = new StringBuffer();
                runtimeProtocol.setSetDialog(true);
                runtimeProtocol.setPc(pc + 1);
                funcName.add("setDialogUrl");
                setProtocol(tag, false, instruct, runtimeProtocol, funcName, storyDialogUrl);
                decode(pc + 1, runtimeProtocol);
            case "<select>":
                StringBuffer selectId = new StringBuffer();
                StringBuffer likabilityId = new StringBuffer();
                StringBuffer improveNum = new StringBuffer();
                runtimeProtocol.setSetSelect(true);
                runtimeProtocol.setPc(pc + 1);
                if (runtimeProtocol.getSelectList() == null) {
                    runtimeProtocol.setSelectList(new ArrayList<SelectItem>());
                }
                runtimeProtocol.getSelectList().add(new SelectItem());
                funcName.add("updateSelectList");
                setProtocol(tag, true, instruct, runtimeProtocol, funcName, selectId, likabilityId, improveNum);
                decode(pc + 1, runtimeProtocol);
            case "<selectclear>":
                runtimeProtocol.getSelectList().clear();
            case "<immflush>":
                runtimeProtocol.setPc(pc + 1);
                return runtimeProtocol;
            case "<jump>":
                StringBuffer logicFormula = new StringBuffer();
                StringBuffer jumpLabel = new StringBuffer();
                tag = removePrefix(instruct, tag);
                //这里将来要做语义检查。
                addInstructField(instruct, tag, logicFormula);
                tag = removePrefix(instruct, tag);
                addInstructField(instruct, tag, jumpLabel);
                if (testLogicBool(logicFormula.toString())) {
                    decode(Integer.valueOf(jumpLabel.toString()), runtimeProtocol);
                } else {
                    runtimeProtocol.setPc(pc + 1);
                    decode(pc + 1, runtimeProtocol);
                }
        }
        return null;
    }
}
