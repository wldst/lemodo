package com.wldst.ruder.module.workflow.biz;

import bsh.EvalError;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.workflow.beans.BpmInstance;
import com.wldst.ruder.module.workflow.exceptions.BizException;
import com.wldst.ruder.module.workflow.exceptions.CrudBaseException;
import com.wldst.ruder.module.workflow.persist.DaoWfCombineOperate;
import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author wldst
 */
@Component
public class BpmInstanceManagerService {

    public static Logger logger = LoggerFactory.getLogger(BpmInstanceManagerService.class);
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private DaoWfCombineOperate daoWfCombineOperate;
    @Autowired
    private BpmInstance bpmi;
    @Autowired
    private BpmInstanceStateTransfer workflowStatsTransfer;

    public Map<String, Object> getWorkflowFromCache(long bizDataID, String bizLabel) {
        Map<String, Object> retFlow = null;
        try {
            long workflowID = daoWfCombineOperate.getWorkflowInstanceID(bizDataID, bizLabel);
            if (workflowID > 0) {
                retFlow = daoWfCombineOperate.getBpmiById(workflowID);
            }
        } catch (Exception ex) {
            LoggerTool.error(logger,"获取流程实例信息失败:", ex);
            throw new CrudBaseException("获取流程实例信息失败:", ex);
        }
        return retFlow;
    }

    public Map<String, Object> getBpmi(long bizDataID, String bizTabName) {
        return this.getWorkflowFromCache(bizDataID, bizTabName);
    }

    public Map<String, Object> getBpmi(long wfInsID) {
        Map<String, Object> retFlow = null;
        try {
            if (wfInsID > 0) {
                retFlow = daoWfCombineOperate.getBpmiById(wfInsID);
            }
        } catch (Exception ex) {
            LoggerTool.error(logger,"获取流程实例信息失败:", ex);
            throw new CrudBaseException("获取流程实例信息失败:", ex);
        }
        return retFlow;
    }

    public boolean runFlow(Map<String, Object> workflow, Long currentUserId) throws BizException {
        boolean retFlag = false;
        try {
            retFlag = workflowStatsTransfer.run(workflow, currentUserId);
        } catch (EvalError e) {
            throw new BizException("启动流程失败", e);
        }
        return retFlag;
    }

    public boolean finishFlow(Map<String, Object> workflow) {
        boolean retFlag = false;

        retFlag = workflowStatsTransfer.finish(workflow);
        return retFlag;
    }

    public boolean terminateFlow(Map<String, Object> workflow) {
        boolean retFlag = false;
        retFlag = workflowStatsTransfer.terminate(workflow);
        return retFlag;
    }

    public boolean pauseFlow(Map<String, Object> workflow) {
        boolean retFlag = false;

        retFlag = workflowStatsTransfer.pause(workflow);
        return retFlag;
    }

    public boolean suspendFlow(Map<String, Object> workflow) {
        boolean retFlag = false;

        retFlag = workflowStatsTransfer.suspend(workflow);
        return retFlag;
    }

}
