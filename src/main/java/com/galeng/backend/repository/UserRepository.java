package com.galeng.backend.repository;

import com.galeng.backend.entity.Cg;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository {
    public String selectPasswordByAccount(String Account);
    public List<Cg> selectCgByAccount(String Account,String gameid);
}
