package com.wldst.ruder.module.oa.service;

import java.util.List;
import java.util.Map;

/**
 * 会议室预定表服务接口
 * @author wendy
 * @date 2015-09-15
 *
 */
public interface IMeetingRoomService {

	String getTableHeader(int days);

	List queryMeetingInfos();

	int addManager(String userId, String username, String useraccount, String dept);

	Map<String, String> saveImportMeeting(Map<String, Object> vomap);

	List<Map<String, Object>> getImportMeetingList();

    Map<String, Object> getMeetManager(String userId);

}
