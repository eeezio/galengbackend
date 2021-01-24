package com.galeng.backend.controller;

import com.galeng.backend.entity.Cg;
import com.galeng.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController//不进行视图解析
public class testController {
    @Autowired
    private UserRepository UserRepository;

    @GetMapping("/selectPasswordByAccount/{account}")//RESTFUL风格
    //PathVariable的作用是实现"account"与后面真正参数的绑定
    public String selectPasswordByAccount(@PathVariable("account") String account) {
        return UserRepository.selectPasswordByAccount(account);
    }

    @GetMapping("/selectCgByAccount/{account}/{gameid}")
    public List<Cg> selectCgByAccount(@PathVariable("account") String account,@PathVariable("gameid")String gameid) {
        return UserRepository.selectCgByAccount(account,gameid);
    }
}