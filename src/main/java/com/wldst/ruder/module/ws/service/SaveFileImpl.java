package com.wldst.ruder.module.ws.service;

import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.domain.FileDomain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author wldst
 *
 */
@Service
public class SaveFileImpl extends FileDomain implements SaveFile {
    @Autowired
    private CrudUserNeo4jService neo4jService;
    
    @Override
    public Map<String, Object> docPath(String fileName) {
        HashMap<String, Object> map = new HashMap<>();
        //根据时间生成文件夹路径
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String docUrl = simpleDateFormat.format(date);
	 
        String filePath = neo4jService.getPathBy(FileDomain.FILE_STORE_PATH);        
        if(!filePath.endsWith("/")) {
            filePath=filePath +"/";
        }
        //文件保存地址
        String path = filePath + docUrl;
        //创建文件
        String realFile = path+"/" + fileName;
	File dest = new File(realFile);
        //如果文件已经存在就先删除掉
        if (dest.getParentFile().exists()) {
            dest.delete();
        }
        
        Node saveByBody = saveFileInfo(fileName, realFile);
        long fileId = saveByBody.getId();	
	map.put("dest", dest);
        map.put("path", realFile);
        map.put("nginxPath",FILE_SHOW_URL+fileId);
        return map;
    }

    private Node saveFileInfo(String fileName, String realFile) {
	Map<String, Object> fileMap = new HashMap<>();
	fileMap.put(NAME, fileName);
	fileMap.put(FILE_SIZE, "");
	fileMap.put(FILE_TYPE, fileName.split("\\.")[1]);
	fileMap.put(FILE_STORE_NAME, realFile);
        Node saveByBody = neo4jService.saveByBody(fileMap, FileDomain.FILE);
	return saveByBody;
    }

    @Override
    public boolean saveFileFromBytes(byte[] b, Map<String, Object> map) {
        //创建文件流对象
        FileOutputStream fstream = null;
        //从map中获取file对象
        File file = (File) map.get("dest");
        //判断路径是否存在，不存在就创建
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            fstream = new FileOutputStream(file, true);
            fstream.write(b);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fstream != null) {
                try {
                    fstream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return true;
    }
}
