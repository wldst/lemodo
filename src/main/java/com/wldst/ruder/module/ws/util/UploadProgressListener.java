package com.wldst.ruder.module.ws.util;
 
import org.apache.commons.fileupload.ProgressListener;
 
import jakarta.servlet.http.HttpSession;
 

public class UploadProgressListener implements ProgressListener {
    private HttpSession session;
 
    public void setSession(HttpSession session) {
        this.session = session;
        ProgressEntity status = new ProgressEntity();// 保存上传状态
        session.setAttribute("status", status);
    }
 
    @Override
    public void update(long bytesRead, long contentLength, int items) {
        ProgressEntity status = (ProgressEntity) session.getAttribute("status");
        status.setBytesRead(bytesRead);// 已读取数据长度
        status.setContentLength(contentLength);// 文件总长度
        status.setItems(items);// 正在保存第几个文件
 
    }
}