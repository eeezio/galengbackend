package com.galeng.backend.controller;

import com.galeng.backend.cache.LRUCache;
import com.galeng.backend.entity.Cg;
import com.galeng.backend.entity.Lihui;
import com.galeng.backend.entity.RuntimeSave;
import com.galeng.backend.entity.SnapshootSave;
import com.galeng.backend.instruct.Decoder;
import com.galeng.backend.protocol.RuntimeProtocol;
import com.galeng.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


@RestController//不进行视图解析
public class testController {
    @Autowired
    private UserRepository UserRepository;

    @Autowired
    private Decoder decoder;

    private LRUCache<String, String> audioCache = new LRUCache<>(50);

    private LRUCache<String, BufferedImage> imageLRUCache = new LRUCache<>(50);

    private LRUCache<String, RuntimeSave> runtimeSaveLRUCache = new LRUCache<>(10000);

    @GetMapping("/selectPassword/{account}")//RESTFUL风格
    //PathVariable的作用是实现"account"与后面真正参数的绑定
    public String selectPassword(@PathVariable("account") String account) {
        return UserRepository.selectPassword(account);
    }

    @GetMapping("/selectCg/{account}/{gameId}")
    public List<Cg> selectCg(@PathVariable("account") String account, @PathVariable("gameId") String gameId) {
        return UserRepository.selectCg(account, gameId);
    }

    @PostMapping("/saveSnapshootSave")
    //该注解时表示接收客户端的json数据
    public void saveSnapshootSave(@RequestBody SnapshootSave snapShootSave) {
        UserRepository.saveSnapshootSave(snapShootSave);
    }

    @GetMapping("/selectSnapshootSave/{account}/{gameId}")
    public List<SnapshootSave> selectSnapshootSave(@PathVariable("account") String account, @PathVariable("gameId") String gameId) {
        return UserRepository.selectSnapshootSave(account, gameId);
    }

    @PostMapping("/updateSnapshootSave")
    public void updateSnapshootSave(@RequestBody SnapshootSave snapShootSave) {
        UserRepository.updateSnapshootSave(snapShootSave);
    }

    @GetMapping("/selectRuntimeSave/{account}/{gameId}/{savePage}/{saveId}")
    public RuntimeSave selectRuntimeSave(@PathVariable("account") String account, @PathVariable("gameId") String gameId, @PathVariable("savePage") int savePage, @PathVariable("saveId") int saveId) {
        if (decoder.getRuntimeSaveLRUCache() == null) {
            decoder.setRuntimeSaveLRUCache(runtimeSaveLRUCache);
        }
        return UserRepository.selectRuntimeData(account, gameId, savePage, saveId);
    }

    @PostMapping("/saveRuntimeSave")
    public void saveRuntimeSave(@RequestBody RuntimeSave runtimeSave) {
        UserRepository.saveRuntimeSave(runtimeSave);
    }

    @PostMapping("/updateRuntimeSave")
    public void updateRuntimeSave(@RequestBody RuntimeSave runtimeSave) {
        UserRepository.updateRuntimeSave(runtimeSave);
    }

    @PostMapping("/saveLihuiSave")
    public void saveLihuiSave(@RequestBody Lihui lihui) {
        UserRepository.saveLihuiSave(lihui);
    }

    @PostMapping("/updateLihuiSave")
    public void updateLihuiSave(@RequestBody Lihui lihui) {
        UserRepository.updateLihuiSave(lihui);
    }

    @GetMapping("/selectLihuiSave/{account}/{gameId}/{savePage}/{saveId}")
    public List<Lihui> selectLihuiSave(@PathVariable("account") String account, @PathVariable("gameId") String gameId, @PathVariable("savePage") int savePage, @PathVariable("saveId") int saveId) {
        return UserRepository.selectLihuiSave(account, gameId, savePage, saveId);
    }

    @RequestMapping(value = "/getImage", params = {"imageName"})
    public void getImage(@RequestParam("imageName") String imageName, HttpServletResponse response) throws IOException {
        BufferedImage image;
        if (imageLRUCache.map.containsKey(imageName)) {
            image = imageLRUCache.get(imageName);
        } else {
            InputStream is = new FileInputStream(new File(imageName));
            image = ImageIO.read(is);
            is.close();
            imageLRUCache.put(imageName, image);
        }
        response.setContentType("image/png");
        String ext = "png";
        OutputStream os = response.getOutputStream();
        ImageIO.write(image, ext, os);
        os.flush();
        os.close();
    }

    @GetMapping("/getNextInstruct/{pc}")
    public RuntimeProtocol getNextInstruct(@PathVariable("pc") int pc) throws NoSuchMethodException {
        decoder.setScript("<text> backendtext:你好。\n欢迎λ;重修 大学计算机;voiceurl:zhewang/home/test.mp3");
        try {
            return decoder.decode(pc, null);
        } catch (NoSuchMethodException noSuchMethodException) {
            System.out.printf("NoSuchMethodException");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    //它和getmapping注解的区别就在于；它可以指定方法时put还是get
//    @RequestMapping(value = "/getImage", params = {"imageName"})
//    @RequestParam 支持下面四种参数
//
//    defaultValue 如果本次请求没有携带这个参数，或者参数为空，那么就会启用默认值
//    name 绑定本次参数的名称，要跟URL上面的一样
//    required 这个参数是不是必须的
//    value 跟name一样的作用，是name属性的一个别名
//    public String getImage(@RequestParam("imageName") String imageName) throws IOException {
//        byte[] data = null;
//        Base64.Encoder encoder = Base64.getEncoder();
//        Resource resource = new PathResource(imageName);
//        File file = resource.getFile();
//        InputStream is = new FileInputStream(file.getPath());
//        data = new byte[is.available()];
//        is.read(data);
//        is.close();
//        return "data:image/png;base64," + encoder.encodeToString(data);
//    }

    @RequestMapping("/clickSelect/{varName}/{num}")
    public void clickSelect(@PathVariable("varName") String varName, @PathVariable("num") int num, HttpServletRequest request) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        RuntimeSave tmp = runtimeSaveLRUCache.get(request.getSession().getId());
        Method method = tmp.getClass().getMethod("set" + varName, int.class);
        method.invoke(tmp, num);
    }

    @RequestMapping(value = "/getAudio", params = {"audioName"})
    public void getAudio(@RequestParam("audioName") String audioName, HttpServletResponse response) throws IOException {
        OutputStream out = response.getOutputStream();
        if (audioCache.map.containsKey(audioName)) {
            byte[] b = audioCache.get(audioName).getBytes();
            out.write(b, 0, b.length);
            out.flush();
            out.close();
        } else {
            File file = new File(audioName);
            FileInputStream in = new FileInputStream(file);
            byte[] b = null;
            while (in.available() > 0) {
                if (in.available() > 10240) {
                    b = new byte[10240];
                } else {
                    b = new byte[in.available()];
                }
                in.read(b, 0, b.length);
                out.write(b, 0, b.length);
            }
            audioCache.put(audioName, b.toString());
            in.close();
            //清空缓冲区
            out.flush();
            out.close();
        }
    }

}
