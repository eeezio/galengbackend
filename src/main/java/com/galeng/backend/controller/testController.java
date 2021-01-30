package com.galeng.backend.controller;

import com.galeng.backend.entity.Cg;
import com.galeng.backend.entity.Lihui;
import com.galeng.backend.entity.RuntimeSave;
import com.galeng.backend.entity.SnapshootSave;
import com.galeng.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;


@RestController//不进行视图解析
public class testController {
    @Autowired
    private UserRepository UserRepository;

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
        System.out.println("savesnapshoot");
        UserRepository.saveSnapshootSave(snapShootSave);
    }

    @GetMapping("/selectSnapshootSave/{account}/{gameId}")
    public List<SnapshootSave> selectSnapshootSave(@PathVariable("account") String account, @PathVariable("gameId") String gameId) {
        return UserRepository.selectSnapshootSave(account, gameId);
    }

    @PostMapping("/updateSnapshootSave")
    public void updateSnapshootSave(@RequestBody SnapshootSave snapShootSave) {
        System.out.println("updatesnapshoot");
        UserRepository.updateSnapshootSave(snapShootSave);
    }

    @GetMapping("/selectRuntimeSave/{account}/{gameId}/{savePage}/{saveId}")
    public RuntimeSave selectRuntimeSave(@PathVariable("account") String account, @PathVariable("gameId") String gameId, @PathVariable("savePage") int savePage, @PathVariable("saveId") int saveId) {
        return UserRepository.selectRuntimeData(account, gameId, savePage, saveId);
    }

    @PostMapping("/saveRuntimeSave")
    public void saveRuntimeSave(@RequestBody RuntimeSave runtimeSave) {
        System.out.println("saveruntime");
        UserRepository.saveRuntimeSave(runtimeSave);
    }

    @PostMapping("/updateRuntimeSave")
    public void updateRuntimeSave(@RequestBody RuntimeSave runtimeSave) {
        System.out.println("updateruntime");
        UserRepository.updateRuntimeSave(runtimeSave);
    }

    @PostMapping("/saveLihuiSave")
    public void saveLihuiSave(@RequestBody Lihui lihui) {
        System.out.println("savelihui");
        UserRepository.saveLihuiSave(lihui);
    }

    @PostMapping("/updateLihuiSave")
    public void updateLihuiSave(@RequestBody Lihui lihui) {
        System.out.println("updatelihui");
        UserRepository.updateLihuiSave(lihui);
    }

    @GetMapping("/selectLihuiSave/{account}/{gameId}/{savePage}/{saveId}")
    public List<Lihui> selectLihuiSave(@PathVariable("account") String account, @PathVariable("gameId") String gameId, @PathVariable("savePage") int savePage, @PathVariable("saveId") int saveId) {
        return UserRepository.selectLihuiSave(account, gameId, savePage, saveId);
    }

    @RequestMapping(value = "/getImage", params = {"imageName"})
//    public void getImage(@RequestParam("imageName") String imageName, HttpServletResponse response) throws IOException {
//        InputStream is = new FileInputStream(new File(imageName));
//        BufferedImage image = ImageIO.read(is);
//        response.setContentType("image/png");
//        String ext = "png";
//        OutputStream os = response.getOutputStream();
//        ImageIO.write(image, ext, os);
//        is.close();
//        os.flush();
//        os.close();
//    }
    public String getImage(@RequestParam("imageName") String imageName) throws IOException {
        byte[] data = null;
        Base64.Encoder encoder = Base64.getEncoder();
        Resource resource = new PathResource(imageName);
        File file = resource.getFile();
        InputStream is = new FileInputStream(file.getPath());
        data = new byte[is.available()];
        is.read(data);
        is.close();
        return "data:image/png;base64,"+encoder.encodeToString(data);
    }
}
