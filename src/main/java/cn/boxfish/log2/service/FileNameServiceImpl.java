package cn.boxfish.log2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Created by lvtu on 2016/11/17.
 */
@Component
public class FileNameServiceImpl {

    private final static String fileToIdentity = "file_collection_map";

    @Autowired
    private MongoOperations mongoOperations;

    private final static Logger logger = LoggerFactory.getLogger(FileNameServiceImpl.class);

    public String fileId(String fullpath) {
        List<Map> fullpathList = mongoOperations.find(Query.query(where("fullpath").is(fullpath)), Map.class, fileToIdentity);
        if (!CollectionUtils.isEmpty(fullpathList)) {
            return (String) fullpathList.get(0).get("file_id");
        }
        String fileId = String.valueOf(System.currentTimeMillis()) + "_" + UUID.randomUUID();
        Map<String, String> map = new HashMap<>();
        map.put("fullpath", fullpath);
        map.put("file_id", fileId);
        map.put("state", "0");
        mongoOperations.insert(map,fileToIdentity);
        return fileId;
    }

    public void mark(String fullpath){

    }
}
