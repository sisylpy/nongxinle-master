package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxOcrTaskDao;
import com.nongxinle.entity.NxOcrTaskEntity;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.service.NxOcrTaskService;



@Service("nxOcrTaskService")
public class NxOcrTaskServiceImpl implements NxOcrTaskService {
	@Autowired
	private NxOcrTaskDao nxOcrTaskDao;
	
	@Override
	public NxOcrTaskEntity queryObject(Integer nxOcrTaskId){
		return nxOcrTaskDao.queryObject(nxOcrTaskId);
	}
	
	@Override
	public List<NxOcrTaskEntity> queryList(Map<String, Object> map){
		return nxOcrTaskDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxOcrTaskDao.queryTotal(map);
	}
	
	@Override
	public void save(NxOcrTaskEntity nxOcrTask){
		nxOcrTaskDao.save(nxOcrTask);
	}
	
	@Override
	public void update(NxOcrTaskEntity nxOcrTask){
		nxOcrTaskDao.update(nxOcrTask);
	}
	
	@Override
	public void delete(Integer nxOcrTaskId){
		nxOcrTaskDao.delete(nxOcrTaskId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxOcrTaskIds){
		nxOcrTaskDao.deleteBatch(nxOcrTaskIds);
	}

	@Override
	public void updateTaskStatistics(Integer taskId) {
		nxOcrTaskDao.updateTaskStatistics(taskId);
		// 更新任务的更新时间
		NxOcrTaskEntity task = nxOcrTaskDao.queryObject(taskId);
		if (task != null) {
			task.setNxOcrTaskUpdateDate(com.nongxinle.utils.DateUtils.formatWhatYearDayTime(0));
			nxOcrTaskDao.update(task);
		}
	}
	
	@Override
	public List<NxOcrTaskEntity> queryTasksByDepartmentAndStatus(Map<String, Object> map) {
		return nxOcrTaskDao.queryTasksByDepartmentAndStatus(map);
	}
	
	@Override
	public int queryTotalByDepartmentAndStatus(Map<String, Object> map) {
		return nxOcrTaskDao.queryTotalByDepartmentAndStatus(map);
	}
	
	@Override
	public List<NxDepartmentEntity> queryTasksDepartmentByDisId(Map<String, Object> map) {
		return nxOcrTaskDao.queryTasksDepartmentByDisId(map);
	}

}
