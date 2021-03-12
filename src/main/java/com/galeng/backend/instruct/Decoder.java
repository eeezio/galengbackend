package com.galeng.backend.instruct;


import com.galeng.backend.cache.LRUCache;
import com.galeng.backend.entity.Lihui;
import com.galeng.backend.entity.RuntimeSave;
import com.galeng.backend.entity.SelectItem;
import com.galeng.backend.protocol.RuntimeProtocol;
import org.springframework.stereotype.Repository;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

@Repository
public class Decoder {

    private ArrayList<String> instructQue;

    LRUCache<String, RuntimeSave> runtimeSaveLRUCache;

    public void setInstructQue(ArrayList instructQue) {
        this.instructQue = instructQue;
    }

    public ArrayList getInstructQue() {
        return instructQue;
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

    private int addInstructField(String instruct, int tag, StringBuffer instructField) {
        while (tag < instruct.length() && !endOfSubStatement(instruct, tag)) {
            if (instruct.charAt(tag) == 'λ') tag++;
            instructField.append(instruct.charAt(tag++));
        }
        return tag;
    }

    private void setProtocol(int tag, boolean setObject, String instruct, RuntimeProtocol protocol, List<String> funcName, StringBuffer... instructFields) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        int count = 0;
        while (tag < instruct.length()) {
            tag = removePrefix(instruct, tag);
            tag = addInstructField(instruct, tag, instructFields[count]);
            if (!setObject) {
                Method method = protocol.getClass().getMethod(funcName.get(count), instructFields[count].toString().getClass());
                method.invoke(protocol, instructFields[count].toString());
            }
            count++;
            tag++;
        }
        if (setObject) {
            Method method = protocol.getClass().getMethod(funcName.get(0), instructFields.getClass());
            //反射调用方法，传入数组时，要将数组强转为Object
            method.invoke(protocol, (Object) instructFields);
        }
    }

    private boolean testLogicBool(String logicFormula, HttpServletRequest request) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Stack<Integer> stackOperand = new Stack<>();
        Stack<Boolean> stackAns = new Stack<>();
        int i = 0;
        while (i < logicFormula.length()) {
            switch (logicFormula.charAt(i)) {
                case '(':
                    i += 1;
                    StringBuffer varName = new StringBuffer();
                    while (logicFormula.charAt(i) != ')') {
                        varName.append(logicFormula.charAt(i));
                        i += 1;
                    }
                    RuntimeSave tmpSave = runtimeSaveLRUCache.get(request.getSession().getId());
                    Method method = tmpSave.getClass().getMethod("get" + varName.toString());
                    stackOperand.push((int) method.invoke(tmpSave));
                    i += 1;
                    break;
                case '{':
                    i += 1;
                    StringBuffer value = new StringBuffer();
                    while (logicFormula.charAt(i) != '}') {
                        value.append(logicFormula.charAt(i));
                        i += 1;
                    }
                    stackOperand.push(Integer.valueOf(value.toString()));
                    i += 1;
                    break;
                case '>':
                    Integer opG1 = stackOperand.pop();
                    Integer opG2 = stackOperand.pop();
                    if (opG1 > opG2) {
                        stackAns.push(true);
                    } else {
                        stackAns.push(false);
                    }
                    i += 1;
                    break;
                case '<':
                    Integer opL1 = stackOperand.pop();
                    Integer opL2 = stackOperand.pop();
                    if (opL1 < opL2) {
                        stackAns.push(true);
                    } else {
                        stackAns.push(false);
                    }
                    i += 1;
                    break;
                case '=':
                    Integer opE1 = stackOperand.pop();
                    Integer opE2 = stackOperand.pop();
                    if (opE1 == opE2) {
                        stackAns.push(true);
                    } else {
                        stackAns.push(false);
                    }
                    i += 1;
                    break;
                //未来做语法检查；只支持&&
                case '&':
                    Boolean opA1 = stackAns.pop();
                    Boolean opA2 = stackAns.pop();
                    if (opA1 && opA2) {
                        stackAns.push(true);
                    } else {
                        stackAns.push(false);
                    }
                    i += 1;
                    break;
                //未来做语法检查；只支持||
                case '|':
                    Boolean opO1 = stackAns.pop();
                    Boolean opO2 = stackAns.pop();
                    if (opO1 || opO2) {
                        stackAns.push(true);
                    } else {
                        stackAns.push(false);
                    }
                    i += 1;
                    break;
            }
        }
        return stackAns.peek();
    }


    public RuntimeProtocol decode(int pc, RuntimeProtocol runtimeProtocol, HttpServletRequest request) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (runtimeProtocol == null) {
            runtimeProtocol = new RuntimeProtocol();
        }
        String instruct = instructQue.get(pc);
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
                return decode(pc + 1, runtimeProtocol, request);
            case "<se>":
                StringBuffer storySeUrl = new StringBuffer();
                runtimeProtocol.setSetSe(true);
                runtimeProtocol.setPc(pc + 1);
                funcName.add("setStorySeUrl");
                setProtocol(tag, false, instruct, runtimeProtocol, funcName, storySeUrl);
                return decode(pc + 1, runtimeProtocol, request);
            case "<bg>":
                StringBuffer storyBgUrl = new StringBuffer();
                runtimeProtocol.setSetBg(true);
                runtimeProtocol.setPc(pc + 1);
                funcName.add("setStoryBgUrl");
                setProtocol(tag, false, instruct, runtimeProtocol, funcName, storyBgUrl);
                return decode(pc + 1, runtimeProtocol, request);
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
                return decode(pc + 1, runtimeProtocol, request);
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
                return decode(pc + 1, runtimeProtocol, request);
            case "<mask>":
                StringBuffer storyMaskUrl = new StringBuffer();
                funcName.add("setStoryMaskUrl");
                runtimeProtocol.setSetMask(true);
                runtimeProtocol.setPc(pc + 1);
                setProtocol(tag, false, instruct, runtimeProtocol, funcName, storyMaskUrl);
                return decode(pc + 1, runtimeProtocol, request);
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
                return decode(pc + 1, runtimeProtocol, request);
            case "<dialog>":
                StringBuffer storyDialogUrl = new StringBuffer();
                runtimeProtocol.setSetDialog(true);
                runtimeProtocol.setPc(pc + 1);
                funcName.add("setStoryDialogUrl");
                setProtocol(tag, false, instruct, runtimeProtocol, funcName, storyDialogUrl);
                return decode(pc + 1, runtimeProtocol, request);
            case "<select>":
                StringBuffer text = new StringBuffer();
                StringBuffer varName = new StringBuffer();
                StringBuffer improveNum = new StringBuffer();
                runtimeProtocol.setSetSelect(true);
                runtimeProtocol.setPc(pc + 1);
                if (runtimeProtocol.getSelectList() == null) {
                    runtimeProtocol.setSelectList(new ArrayList<SelectItem>());
                }
                runtimeProtocol.getSelectList().add(new SelectItem());
                funcName.add("updateSelectList");
                setProtocol(tag, true, instruct, runtimeProtocol, funcName, text, varName, improveNum);
                return decode(pc + 1, runtimeProtocol, request);
            case "<immflush>":
                runtimeProtocol.setPc(pc + 1);
                return runtimeProtocol;
            case "<jump>":
                StringBuffer logicFormula = new StringBuffer();
                StringBuffer jumpLabel = new StringBuffer();
                tag = removePrefix(instruct, tag);
                //这里将来要做语义检查。
                tag = addInstructField(instruct, tag, logicFormula);
                tag = removePrefix(instruct, tag);
                tag = addInstructField(instruct, tag, jumpLabel);
                if (testLogicBool(logicFormula.toString(), request)) {
                    return decode(Integer.valueOf(jumpLabel.toString()), runtimeProtocol, request);
                } else {
                    runtimeProtocol.setPc(pc + 1);
                    return decode(pc + 1, runtimeProtocol, request);
                }
        }
        return null;
    }
}
