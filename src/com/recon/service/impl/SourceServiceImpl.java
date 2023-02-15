package com.recon.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.recon.dao.ISourceDao;
import com.recon.model.CompareSetupBean;
import com.recon.model.ConfigurationBean;
import com.recon.model.SettlementBean;
import com.recon.model.SettlementBean;
import com.recon.model.Settlement_FinalBean;
import com.recon.service.ISourceService;
import com.recon.util.GenerateSettleTTUMBean;

@Component
public class SourceServiceImpl implements ISourceService {

	@Autowired ISourceDao iSourceDao;
	
	@Override
	public List<ConfigurationBean> getFileDetails() throws Exception {
		return iSourceDao.getFileDetails();
	}

	@Override
	public boolean updateFileDetails(ConfigurationBean ftpBean) throws Exception {
		return iSourceDao.updateFileDetails(ftpBean);
	}

	@Override
	public boolean addFileSource(ConfigurationBean configBean) throws Exception {
	
		return iSourceDao.addFileSource(configBean);
	}

	@Override
	public int getFileId(ConfigurationBean configBean) {
		// TODO Auto-generated method stub
		return iSourceDao.getFileId(configBean);
	}

	@Override
	public boolean chkTblExistOrNot(ConfigurationBean configBean) throws Exception {
		return iSourceDao.chkTblExistOrNot(configBean);
	}

	@Override
	public boolean addConfigParams(ConfigurationBean configBean) throws Exception {
		return iSourceDao.addConfigParams(configBean);
		
	}

	@Override
	public String getHeaderList(int fileId) {
	
		return iSourceDao.getHeaderList(fileId);
	}

	@Override
	public ArrayList<ConfigurationBean> getCompareDetails(int fileId,
			String category,String subcat) throws Exception {
		
		return iSourceDao.getCompareDetails(fileId, category,subcat);
	}

	@Override
	public List<ConfigurationBean> getknockoffDetails(int fileId,
			String category,String  subcat) throws Exception {
		
		return iSourceDao.getknockoffDetails(fileId, category,subcat);
	}

	@Override
	public List<ConfigurationBean> getknockoffcrt(int fileId, String category,String  subcat) throws Exception {
	
		return iSourceDao.getknockoffcrt(fileId, category,subcat);
	}

	@Override
	public List<CompareSetupBean> getFileList(String category, String subcat) throws Exception {
		
		return iSourceDao.getFileList( category,  subcat);
	}

	@Override
	public List<String> gettable_list() {
		
		return iSourceDao.gettable_list();
	}

	@Override
	public List<CompareSetupBean> getFileTypeList(String category,String subcat, String table) throws Exception {
		// TODO Auto-generated method stub
		return iSourceDao.getFileTypeList( category,subcat,  table);
	}

	@Override
	public List<String> getSubcategories(String category) throws Exception
	{
		return iSourceDao.getSubcategories(category);
	}


	@Override
	public boolean generateCTF(SettlementBean settlementBean) throws IOException {
		// TODO Auto-generated method stub
		return iSourceDao.generateCTF(settlementBean);
	}

	@Override
	public Settlement_FinalBean getReportDetails(SettlementBean settlementBean) {
		// TODO Auto-generated method stub
		return iSourceDao.getReportDetails(settlementBean) ;
	}

	@Override
	public List<GenerateSettleTTUMBean> generateSettlTTUM(
			SettlementBean settlementBean) {
		// TODO Auto-generated method stub
		return iSourceDao.generateSettlTTUM(settlementBean);
	}

		

}
