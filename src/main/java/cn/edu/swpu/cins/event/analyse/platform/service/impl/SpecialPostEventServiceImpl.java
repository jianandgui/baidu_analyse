package cn.edu.swpu.cins.event.analyse.platform.service.impl;

import cn.edu.swpu.cins.event.analyse.platform.dao.SpecialPostDao;
import cn.edu.swpu.cins.event.analyse.platform.dao.SpecialPostEventDao;
import cn.edu.swpu.cins.event.analyse.platform.exception.NoEventException;
import cn.edu.swpu.cins.event.analyse.platform.model.persistence.SpecialPostEvent;
import cn.edu.swpu.cins.event.analyse.platform.service.SpecialPostEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecialPostEventServiceImpl implements SpecialPostEventService {

    private SpecialPostDao specialPostDao;
    private SpecialPostEventDao specialPostEventDao;

    @Autowired
    public SpecialPostEventServiceImpl(SpecialPostDao specialPostDao, SpecialPostEventDao specialPostEventDao) {
        this.specialPostDao = specialPostDao;
        this.specialPostEventDao = specialPostEventDao;
    }

    @Override
    public List<SpecialPostEvent> queryAll(List<Integer> ids) throws NoEventException {
        List<String> urls = specialPostDao.selectSpecialPostByIds(ids);
        if (urls.isEmpty()) {
            throw new NoEventException("没有对应的专帖名");
        }
        List<SpecialPostEvent> specialPostEventList = specialPostEventDao.selectAll(urls);
        if (specialPostEventList.isEmpty()) {
            throw new NoEventException();
        }
        return specialPostEventList;
    }
}
