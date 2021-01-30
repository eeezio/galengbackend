package com.galeng.backend.repository;

import com.galeng.backend.entity.Cg;
import com.galeng.backend.entity.Lihui;
import com.galeng.backend.entity.RuntimeSave;
import com.galeng.backend.entity.SnapshootSave;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository {
    public String selectPassword(String account);
    public List<Cg> selectCg(String account,String gameId);
    public List<SnapshootSave> selectSnapshootSave(String account, String gameId);
    public void saveSnapshootSave(SnapshootSave snapShootSave);
    public void updateSnapshootSave(SnapshootSave snapShootSave);
    public RuntimeSave selectRuntimeData(String account,String gameId,int savePage,int saveId);
    public void saveRuntimeSave(RuntimeSave runtimeSave);
    public void updateRuntimeSave(RuntimeSave runtimeSave);
    public List<Lihui> selectLihuiSave(String account,String gameId,int savePage,int saveId);
    public void saveLihuiSave(Lihui lihui);
    public void updateLihuiSave(Lihui lihui);
}
