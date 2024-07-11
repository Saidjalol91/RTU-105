package com.moren.rems.schedule.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moren.rems.schedule.dao.RemsScheduleDao;

/**
 * 통신상태 스케쥴용 Service
 * @author shlee
 */
@Service
public class RemsScheduleService {
	
	@Autowired
	RemsScheduleDao remsScheduleDao;
	
	/**
	 * 통신상태 Update
	 * 1분마다 Update
	 */
	public void machineComStatusUpdate() {
		remsScheduleDao.update("RemsRtuDao.updateScheduleRtuEnrgyComStatus",null);
		remsScheduleDao.update("RemsRtuDao.updateScheduleRtuComStatus",null);
	}
}
