package com.recon.dao.impl;

import static com.recon.util.GeneralUtil.GET_COLS;
import static com.recon.util.GeneralUtil.GET_FILE_ID;
import static com.recon.util.GeneralUtil.GET_KNOCKOFF_CRITERIA;
import static com.recon.util.GeneralUtil.GET_KNOCKOFF_PARAMS;
import static com.recon.util.GeneralUtil.GET_MATCH_PARAMS;
import static com.recon.util.GeneralUtil.GET_MATCH_RELAX_PARAMS;
import static com.recon.util.GeneralUtil.GET_REVERSAL_ID;
import static com.recon.util.GeneralUtil.GET_TTUM_HEADERS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.text.StrTokenizer;
import org.springframework.jdbc.core.ResultSetExtractor;
//import org.apache.poi.hssf.record.formula.functions.Replace;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;

import com.recon.dao.GenerateRupayTTUMDao;
import com.recon.model.CompareBean;
import com.recon.model.GenerateTTUMBean;
import com.recon.model.KnockOffBean;
import com.recon.util.LotusConn;
import com.recon.util.OracleConn;
import com.recon.util.demo;


@Component
public class GenerateRupayTTUMDaoImpl extends JdbcDaoSupport implements GenerateRupayTTUMDao{

@Override
public void getTTUMSwitchRecords(GenerateTTUMBean generateTTUMBeanObj)throws Exception
{
	logger.info("***** GenerateRupayTTUMDaoImpl.getTTUMSwitchRecords Start ****");
	
		try
		{
			
			if(generateTTUMBeanObj.getStDate() != null && !generateTTUMBeanObj.getStDate().equals(""))
			{
				//MAKE CHANGES HERE FOR SWITCH TTUM
				String TTUM_RECORDS = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()+
						" SET DCRS_REMARKS = '"+generateTTUMBeanObj.getStMerger_Category()+
						"-UNRECON-GENERATE-TTUM-1' WHERE DCRS_REMARKS = '"
						+generateTTUMBeanObj.getStMerger_Category()
						+"-UNRECON-1' AND TO_CHAR(TO_DATE(LOCAL_DATE,'MM/DD/YYYY'),'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"'" +
						" AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStFile_Date()+"'"; 

				logger.info("TTUM_RECORDS "+TTUM_RECORDS);

				getJdbcTemplate().execute(TTUM_RECORDS);
			}
			else
			{	

				//MAKE CHANGES HERE FOR SWITCH TTUM
				String TTUM_RECORDS = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()+
						" SET DCRS_REMARKS = '"+generateTTUMBeanObj.getStMerger_Category()+
						"-UNRECON-GENERATE-TTUM-1' WHERE DCRS_REMARKS = '"
						+generateTTUMBeanObj.getStMerger_Category()
						//+"-UNRECON-1' AND TO_CHAR(TO_DATE(LOCAL_DATE,'MM/DD/YYYY'),'DD/MM/YYYY') = TO_CHAR(SYSDATE-4,'DD/MM/YYYY')"; 
						+"-UNRECON-1' AND TO_CHAR(TO_DATE(LOCAL_DATE,'MM/DD/YYYY'),'DD/MM/YYYY') = TO_CHAR("+generateTTUMBeanObj.getStFile_Date()+"-4,'DD/MM/YYYY')" +
						" AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStFile_Date()+"' ";

				logger.info("TTUM_RECORDS "+TTUM_RECORDS);

				getJdbcTemplate().execute(TTUM_RECORDS);

			}
			logger.info("***** GenerateRupayTTUMDaoImpl.getTTUMSwitchRecords End ****");
			
		}
		catch(Exception e)
		{
			demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.getTTUMSwitchRecords");
			logger.error(" error in GenerateRupayTTUMDaoImpl.getTTUMSwitchRecords", new Exception("GenerateRupayTTUMDaoImpl.getTTUMSwitchRecords",e));
			 throw e;
		}
	}


@Override
public List<List<GenerateTTUMBean>> generateSwitchTTUM(GenerateTTUMBean generateTTUMBean,int inRec_Set_Id)throws Exception
{
	logger.info("***** GenerateRupayTTUMDaoImpl.generateSwitchTTUM Start ****");
	
	List<GenerateTTUMBean> ttum_data = new ArrayList<>();
	List<GenerateTTUMBean> Excel_headers = new ArrayList<>();
	List<String> ExcelHeaders = new ArrayList<>();
	List<String> ExcelHeaders1 = new ArrayList<>();
	List<List<GenerateTTUMBean>> Total_Data = new ArrayList<>();
	String table_cols = "DCRS_REMARKS VARCHAR (100 BYTE), CREATEDDATE DATE , CREATEDBY VARCHAR (100 BYTE),RECORDS_DATE DATE";
	String insert_cols = "DCRS_REMARKS, CREATEDDATE, CREATEDBY,RECORDS_DATE";
	String stAction = "";
	
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rset = null;
	try
	{
		//logger.info("INSIDE generateSwitchTTUM");
		
		java.util.Date varDate=null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		try {
		     varDate=dateFormat.parse(generateTTUMBean.getStDate());
		    dateFormat=new SimpleDateFormat("ddMMyy");
		    logger.info("Date :"+dateFormat.format(varDate));
		}catch (Exception e) {
			demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.generateSwitchTTUM");
			logger.error(" error in GenerateRupayTTUMDaoImpl.generateSwitchTTUM", new Exception("GenerateRupayTTUMDaoImpl.generateSwitchTTUM",e));
			 throw e;
		}
		
		if(generateTTUMBean.getStSubCategory().equalsIgnoreCase("DOMESTIC"))
		{
			logger.info("*** In DOMESTIC ***");
			generateTTUMBean.setStGLAccount("99937200010660");//as per document
			//get CUST ACCOUNT NO FROM SETTLEMENT TABLE FOR DEBIT
			String GET_TTUM_RECORDS = "SELECT ACCTNUM,AMOUNT,PAN,TO_CHAR(TO_DATE(LOCAL_DATE,'MM/DD/YYYY'),'DDMMYY') AS LOCAL_DATE,SUBSTR(TRACE,-6,6) AS TRACE ," +
								" ACCEPTORNAME " +" FROM SETTLEMENT_"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStFile_Name() 
								+ " WHERE DCRS_REMARKS LIKE '%GENERATE-TTUM-2%'";
			
			logger.info("GET_TTUM_RECORDS=="+GET_TTUM_RECORDS);
						
			conn = getConnection();
			pstmt = conn.prepareStatement(GET_TTUM_RECORDS);
			rset = pstmt.executeQuery();			
			
			
			//ExcelHeaders.add("FILE_HEADER");
			ExcelHeaders.add("ACCOUNT NUMBER");
			ExcelHeaders.add("CURRENCY CODE");
			ExcelHeaders.add("SERVICE OUTLET");
			ExcelHeaders.add("PART TRAN TYPE");
			ExcelHeaders.add("TRANSACTION AMOUNT");
			ExcelHeaders.add("TRANSACTION PARTICULARS");
			ExcelHeaders.add("REFERENCE CURRENCY CODE");
			ExcelHeaders.add("REFERENCE AMOUNT");
			ExcelHeaders.add("REMARKS");
			ExcelHeaders.add("REFERENCE NUMBER");
			ExcelHeaders.add("ACCOUNT REPORT CODE");
			
			//modified on 25/04/2018
			ExcelHeaders1.add("ACCOUNT_NUMBER");
			ExcelHeaders1.add("CURRENCY_CODE");
			ExcelHeaders1.add("SERVICE_OUTLET");
			ExcelHeaders1.add("PART_TRAN_TYPE");
			ExcelHeaders1.add("TRANSACTION_AMOUNT");
			ExcelHeaders1.add("TRANSACTION_PARTICULARS");
			ExcelHeaders1.add("REFERENCE_CURRENCY_CODE");
			ExcelHeaders1.add("REFERENCE_AMOUNT");
			ExcelHeaders1.add("REMARKS");
			ExcelHeaders1.add("REFERENCE_NUMBER");
			ExcelHeaders1.add("ACCOUNT_REPORT_CODE");
			
			
			generateTTUMBean.setStExcelHeader(ExcelHeaders);
			
			Excel_headers.add(generateTTUMBean);
			int count = 0;
			//int count1 = 0;
			while(rset.next())
			{
				count++;
				//logger.info("count is "+count);
				GenerateTTUMBean generateTTUMBeanObj = new GenerateTTUMBean();
				if(inRec_Set_Id == 1 || inRec_Set_Id == 3)
				{
					generateTTUMBeanObj.setStDebitAcc(rset.getString("ACCTNUM").replaceAll(" ", " ").replaceAll("^0*",""));
					//String[] accnums = rset.getString("ACCTNUM").split(" ");
					//generateTTUMBeanObj.setStDebitAcc(rset.getString("ACCTNUM").split(" ")[1]);
					generateTTUMBeanObj.setStCreditAcc("99937200010660");
					generateTTUMBeanObj.setStAmount(rset.getString("AMOUNT"));
					//String stTran_particulars = "REV/"+generateTTUMBean.getStCategory()+"/"+rset.getString("LOCAL_DATE")+"/"+rset.getString("TRACE");
					
					String stTran_particulars = "DR-RPAY-"+rset.getString("LOCAL_DATE")+"-"+rset.getString("TRACE")+"-"+rset.getString("ACCEPTORNAME").replaceAll("'", "");
					generateTTUMBeanObj.setStDate(rset.getString("LOCAL_DATE"));
					
				//	generateTTUMBean.setStDate(rset.getString("LOCAL_DATE"));
					generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
					//generateTTUMBeanObj.setStRRNo(rset.getString("ISSUER"));
					generateTTUMBeanObj.setStCard_Number(rset.getString("PAN"));
					/*String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);*/
					String remark = getJdbcTemplate().queryForObject("select 'RPYD'||'"+dateFormat.format(varDate)+"'||ttum_seq.nextval from dual", new Object[] {},String.class);
					generateTTUMBeanObj.setStRemark(remark);
				}
				else if(inRec_Set_Id == 2)
				{
					//CUST CR
					generateTTUMBeanObj.setStCreditAcc(rset.getString("ACCTNUM").replaceAll(" ", " ").replaceAll("^0*",""));
					//generateTTUMBeanObj.setStCreditAcc(rset.getString("ACCTNUM").split(" ")[1]);
					//GL DR
					generateTTUMBeanObj.setStDebitAcc("99937200010660");
					generateTTUMBeanObj.setStAmount(rset.getString("AMOUNT"));
					//String stTran_particulars = "REV/"+generateTTUMBean.getStCategory()+"/"+rset.getString("LOCAL_DATE")+"/"+rset.getString("TRACE")+
					String stTran_particulars = "REV-T10-RPAY-"+rset.getString("LOCAL_DATE")+"-"+rset.getString("TRACE")+"-"+rset.getString("ACCEPTORNAME").replaceAll("'", "");
					generateTTUMBeanObj.setStDate(rset.getString("LOCAL_DATE"));							
			//		generateTTUMBean.setStDate(rset.getString("LOCAL_DATE"));
					generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
					//generateTTUMBeanObj.setStRRNo(rset.getString("ISSUER"));
					generateTTUMBeanObj.setStCard_Number(rset.getString("PAN"));
					/*String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);*/
					String remark = getJdbcTemplate().queryForObject("select 'RPYD'||'"+dateFormat.format(varDate)+"'||ttum_seq.nextval from dual", new Object[] {},String.class);
					generateTTUMBeanObj.setStRemark(remark);
				}
				
				
				ttum_data.add(generateTTUMBeanObj);
				
			}
			
					
			Total_Data.add(Excel_headers);
			Total_Data.add(ttum_data);
					
			
		}
		else if(generateTTUMBean.getStSubCategory().equalsIgnoreCase("INTERNATIONAL"))
		{
			generateTTUMBean.setStGLAccount("99937200010663");
			//get CUST ACCOUNT NO FROM SETTLEMENT TABLE FOR DEBIT
			String GET_TTUM_RECORDS = "SELECT PAN,ACCTNUM,AMOUNT,ISSUER,TO_CHAR(TO_DATE(LOCAL_DATE,'MM/DD/YYYY'),'DDMMYY') AS LOCAL_DATE,SUBSTR(TRACE,-6,6) AS TRACE," +
								"ACCEPTORNAME FROM SETTLEMENT_"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStFile_Name() + " WHERE DCRS_REMARKS LIKE '%GENERATE-TTUM%'";
						
			conn = getConnection();
			pstmt = conn.prepareStatement(GET_TTUM_RECORDS);
			rset = pstmt.executeQuery();
			
			//ExcelHeaders.add("FILE_HEADER");
			ExcelHeaders.add("ACCOUNT NUMBER");
			ExcelHeaders.add("CURRENCY CODE");
			ExcelHeaders.add("SERVICE OUTLET");
			ExcelHeaders.add("PART TRAN TYPE");
			ExcelHeaders.add("TRANSACTION AMOUNT");
			ExcelHeaders.add("TRANSACTION PARTICULARS");
			ExcelHeaders.add("REFERENCE CURRENCY CODE");
			ExcelHeaders.add("REFERENCE AMOUNT");
			ExcelHeaders.add("REMARKS");
			ExcelHeaders.add("REFERENCE NUMBER");
			ExcelHeaders.add("ACCOUNT REPORT CODE");
			
			
			
			
			
			generateTTUMBean.setStExcelHeader(ExcelHeaders);
			
			Excel_headers.add(generateTTUMBean);
			
			while(rset.next())
			{
				GenerateTTUMBean generateTTUMBeanObj = new GenerateTTUMBean();
				if(inRec_Set_Id == 1 || inRec_Set_Id == 3)
				{
					generateTTUMBeanObj.setStDebitAcc(rset.getString("ACCTNUM").replaceAll(" ", " ").replaceAll("^0*",""));
					//generateTTUMBeanObj.setStDebitAcc(rset.getString("ACCTNUM").split(" ")[1]);
					generateTTUMBeanObj.setStCreditAcc("99937200010663");
					generateTTUMBeanObj.setStAmount(rset.getString("AMOUNT"));
					//String stTran_particulars = "REV/"+generateTTUMBean.getStCategory()+"/"+rset.getString("LOCAL_DATE")+"/"+rset.getString("TRACE");
					
					//String stTran_particulars = "REV/"+generateTTUMBean.getStCategory()+"/"+rset.getString("LOCAL_DATE")+"/"+rset.getString("TRACE");
					
					String stTran_particulars = "REV-RPAY-"+rset.getString("LOCAL_DATE")+"/"+rset.getString("TRACE")+"-"+rset.getString("ACCEPTORNAME");
					String stRecords_Date = rset.getString("LOCAL_DATE");
				//	generateTTUMBean.setStDate(rset.getString("LOCAL_DATE"));
					generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
					//generateTTUMBeanObj.setStRRNo(rset.getString("ISSUER"));
					generateTTUMBeanObj.setStCard_Number(rset.getString("PAN"));
					/*String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);*/
					String remark = getJdbcTemplate().queryForObject("select 'RPYI'||'"+dateFormat.format(varDate)+"'||ttum_seq.nextval from dual", new Object[] {},String.class);
					generateTTUMBeanObj.setStRemark(remark);
					generateTTUMBeanObj.setAccount_repo("");
				}
				else if(inRec_Set_Id == 2)
				{
					generateTTUMBeanObj.setStCreditAcc(rset.getString("ACCTNUM").replaceAll(" ", " ").replaceAll("^0*",""));
					//generateTTUMBeanObj.setStDebitAcc(rset.getString("ACCTNUM").split(" ")[1]);
					generateTTUMBeanObj.setStDebitAcc("99937200010660");
					generateTTUMBeanObj.setStAmount(rset.getString("AMOUNT"));
				//	String stTran_particulars = "REV/"+generateTTUMBean.getStCategory()+"/"+rset.getString("LOCAL_DATE")+"/"+rset.getString("TRACE");
					String stTran_particulars = "REV-T10-RPAY-"+rset.getString("LOCAL_DATE")+"/"+rset.getString("TRACE")+"-"+rset.getString("ACCEPTORNAME");
					
					String stRecords_Date = rset.getString("LOCAL_DATE");
			//		generateTTUMBean.setStDate(rset.getString("LOCAL_DATE"));
					generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
				//	generateTTUMBeanObj.setStRRNo(rset.getString("ISSUER"));
					generateTTUMBeanObj.setStCard_Number(rset.getString("PAN"));
					/*String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);*/
					String remark = getJdbcTemplate().queryForObject("select 'RPYI'||'"+dateFormat.format(varDate)+"'||ttum_seq.nextval from dual", new Object[] {},String.class);
					generateTTUMBeanObj.setStRemark(remark);
					generateTTUMBeanObj.setAccount_repo("");
				}
				
				
				ttum_data.add(generateTTUMBeanObj);
				
			}
			
			Total_Data.add(Excel_headers);
			Total_Data.add(ttum_data);
			
			
		
		}
		
		
		// CREATE NEW TABLE FOR INSERTING TTUM ENTRIES
		for(int i = 0 ; i<ExcelHeaders1.size();i++)
		{
			table_cols =table_cols+","+ ExcelHeaders1.get(i)+" VARCHAR (100 BYTE)";
			insert_cols = insert_cols+","+ExcelHeaders1.get(i);
		}
		
		String CHECK_TABLE = "SELECT count (*) FROM tab WHERE tname  = 'TTUM_"+generateTTUMBean.getStMerger_Category()
				+"_"+generateTTUMBean.getStFile_Name()+"'";
		logger.info("CHECK_TABLE=="+CHECK_TABLE);
		int tableExist = getJdbcTemplate().queryForObject(CHECK_TABLE, new Object[] { },Integer.class);
		logger.info("tableExist=="+tableExist);
		if(tableExist == 0)
		{
			String CREATE_QUERY = "CREATE TABLE TTUM_"+generateTTUMBean.getStMerger_Category()
					+"_"+generateTTUMBean.getStFile_Name()+" ("+table_cols+")";
			
			logger.info("CREATE_QUERY=="+CREATE_QUERY);
			getJdbcTemplate().execute(CREATE_QUERY);
		}
		
		//INSERT TTUM ENTRIES IN ABOVE CREATED TABLE
		int incount = 0;
		for(int i = 0;i<ttum_data.size();i++)
		{
			incount++;
		
		//	logger.info("inserted "+incount+" records");
			GenerateTTUMBean beanObj = new GenerateTTUMBean();
			beanObj = ttum_data.get(i);
			String INSERT_QUERY = "INSERT INTO TTUM_"+generateTTUMBean.getStMerger_Category()
						+"_"+generateTTUMBean.getStFile_Name() 
						+"("+insert_cols+") VALUES ('"+generateTTUMBean.getStMerger_Category()+
						"-UNRECON-TTUM-"+inRec_Set_Id
						+"',SYSDATE,'"+generateTTUMBean.getStEntry_by()+"',TO_DATE('"+beanObj.getStDate()+"','DDMMYY'),'"+beanObj.getStDebitAcc()+"','INR','999','D','"+
						beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','"+beanObj.getStCard_Number()+"','INR','"+beanObj.getStAmount()+"','"+
						beanObj.getStRemark()+"','"+beanObj.getAccount_repo()+"')";
			
			logger.info("INSERT_QUERY=="+INSERT_QUERY);
			getJdbcTemplate().execute(INSERT_QUERY);
			
			 INSERT_QUERY = "INSERT INTO TTUM_"+generateTTUMBean.getStMerger_Category()
					 +"_"+generateTTUMBean.getStFile_Name() 
						+"("+insert_cols+") VALUES ('"+generateTTUMBean.getStMerger_Category()
						+"-UNRECON-TTUM-"+inRec_Set_Id
						+"',SYSDATE,'"+generateTTUMBean.getStEntry_by()+"',TO_DATE('"+beanObj.getStDate()+"','DDMMYY'),'"+beanObj.getStCreditAcc()+"','INR','999','C','"+
						beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','"+beanObj.getStCard_Number()+"','INR','"+beanObj.getStAmount()+"','"+
						beanObj.getStRemark()+"','"+beanObj.getAccount_repo()+"')";
			 
			 logger.info("INSERT_QUERY=="+INSERT_QUERY);
			 getJdbcTemplate().execute(INSERT_QUERY);		
		}
	
		//UPDATE TTUM GENERATED RECORDS
		/*String value = "";
		for(int loop=1;loop<=2;loop++){
			if(loop == 1){
				value="CBS";
			}else{
				value="SWITCH";
			}*/
				String UPDATE_RECORDS = "UPDATE SETTLEMENT_"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStFile_Name()+
						" SET DCRS_REMARKS = '"+generateTTUMBean.getStMerger_Category()+"-UNRECON-GENERATED-TTUM-"+inRec_Set_Id+"'"
						+" WHERE DCRS_REMARKS = '"+generateTTUMBean.getStMerger_Category()+"-UNRECON-GENERATE-TTUM-"+inRec_Set_Id+"'";

				
				logger.info("UPDATE_RECORDS=="+UPDATE_RECORDS);
				getJdbcTemplate().execute(UPDATE_RECORDS);
				
		//}
				
				logger.info("***** GenerateRupayTTUMDaoImpl.generateSwitchTTUM End ****");
			 
		return Total_Data;
	}
	catch(Exception e)
	{
		demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.generateSwitchTTUM");
		logger.error(" error in GenerateRupayTTUMDaoImpl.generateSwitchTTUM", new Exception("GenerateRupayTTUMDaoImpl.generateSwitchTTUM",e));
		 throw e;
	}
	finally{
		if(rset!=null){
			rset.close();
		}
		if(pstmt!=null){
			pstmt.close();
		}
		if(conn!=null){
			conn.close();
		}
	}
	
}

public void TTUMRecords(GenerateTTUMBean generateTTUMBeanObj)throws Exception
{
	String JOIN1_QUERY = "", JOIN2_QUERY = "";
	logger.info("***** GenerateRupayTTUMDaoImpl.TTUMRecords Start ****");
	Connection con = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	try
	{
		/*String stTable1_Name = Table_list.get(0);//cbs file
		String stTable2_Name = Table_list.get(1);//switch file
*/		//String[] stTable1 = stTable1_Name.split("_");
	//	String[] stTable2 = stTable2_Name.split("_");
	//	String stCategory = stTable1[0] ;
		//String stFile1_Name = Table_list.get(0);//cbs
		String stTTUM_File = generateTTUMBeanObj.getStFile_Name();
		String stFile2_Name = "SWITCH" ;
		//String stFile2_Name = Table_list.get(1);//switch
		String table1_condition = "";
		String table2_condition= "";
		String condition = "";
		
		
		
		logger.info("TTUM STARTS HERE *************************************************");
		
		int table1_file_id = getJdbcTemplate().queryForObject(GET_FILE_ID, new Object[] { stTTUM_File , generateTTUMBeanObj.getStCategory(),generateTTUMBeanObj.getStSubCategory() },Integer.class);
		int table2_file_id = getJdbcTemplate().queryForObject(GET_FILE_ID, new Object[] { stFile2_Name, generateTTUMBeanObj.getStCategory(),generateTTUMBeanObj.getStSubCategory() },Integer.class);
		
	//	List<CompareBean> match_Headers = getJdbcTemplate().query(GET_MATCH_PARAMS , new Object[]{table1_file_id,table2_file_id,table2_file_id,table1_file_id,a[0]},new MatchParameterMaster()); 
		List<CompareBean> match_Headers1 = getJdbcTemplate().query(GET_MATCH_PARAMS , 
				new Object[]{table1_file_id,generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory(),1},new MatchParameterMaster1());
		List<CompareBean> match_Headers2 = getJdbcTemplate().query(GET_MATCH_PARAMS , 
				new Object[]{table2_file_id,generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory(),1},new MatchParameterMaster2());
		
	
		//prepare compare condition
		for(int i = 0; i<match_Headers1.size() ; i++)
		{
			//CHECKING PADDING FOR TABLE 1
			if(match_Headers1.get(i).getStMatchTable1_Padding().equals("Y"))
			{
				if(match_Headers1.get(i).getStMatchTable1_Datatype() != null)
				{
					if(match_Headers1.get(i).getStMatchTable1_Datatype().equals("NUMBER"))
					{
						table1_condition = "SUBSTR( TO_NUMBER( t1."+match_Headers1.get(i).getStMatchTable1_header().trim()+",'999999999.99')"+","+match_Headers1.get(i).getStMatchTable1_startcharpos()+","+
										match_Headers1.get(i).getStMatchTable1_charSize()+")";
					}
					else if(match_Headers1.get(i).getStMatchTable1_Datatype().equals("DATE"))
					{
						table1_condition = "TO_DATE(SUBSTR( t1."+match_Headers1.get(i).getStMatchTable1_header().trim()+","+match_Headers1.get(i).getStMatchTable1_startcharpos()+","+
								match_Headers1.get(i).getStMatchTable1_charSize()+")"+", ' "+match_Headers1.get(i).getStMatchTable1_DatePattern()+" ')";
					}
					else if(match_Headers1.get(i).getStMatchTable1_Datatype().equals("TIME"))
					{
						//check whether the column consists of :
						String CHECK_FORMAT = "SELECT DISTINCT SUBSTR( "+match_Headers1.get(i).getStMatchTable1_header().trim()+","+match_Headers1.get(i).getStMatchTable1_startcharpos()+","+
								match_Headers1.get(i).getStMatchTable1_charSize()+" ) FROM SETTLEMENT_"+stTTUM_File+
								" WHERE SUBSTR( "+match_Headers1.get(i).getStMatchTable1_header().trim()+","+match_Headers1.get(i).getStMatchTable1_startcharpos()+","+
								match_Headers1.get(i).getStMatchTable1_charSize()+" ) IS NOT NULL AND SEG_TRAN_ID IS NOT NULL";
						logger.info("CHECK_ FORMAT IS "+CHECK_FORMAT);
						boolean is_colon = false;
						con = getConnection();
						ps = con.prepareStatement(CHECK_FORMAT);
						rs = ps.executeQuery();
						if(rs.next())
						{
							if(rs.getString(1).contains(":"))
							{
								is_colon = true;
							}
								
						}
						
						if(is_colon)
						{
							table1_condition = "REPLACE( SUBSTR( t1."+match_Headers1.get(i).getStMatchTable1_header().trim()+","+match_Headers1.get(i).getStMatchTable1_startcharpos()+","+
									match_Headers1.get(i).getStMatchTable1_charSize()+")"+" , ':')";
							
						}
						else
						{
							table1_condition = " LPAD( SUBSTR( t1."+match_Headers1.get(i).getStMatchTable1_header().trim()+","+match_Headers1.get(i).getStMatchTable1_startcharpos()+","+
									match_Headers1.get(i).getStMatchTable1_charSize()+")"+","+6+",'0')";
							
						}
						
						
							if(rs!=null){
								rs.close();
							}
							if(ps!=null){
								ps.close();
							}
							if(con!=null){
								con.close();
							}
						
					}
				}
				else
				{
					table1_condition = "SUBSTR( t1."+match_Headers1.get(i).getStMatchTable1_header().trim()+","+match_Headers1.get(i).getStMatchTable1_startcharpos()+","+
							match_Headers1.get(i).getStMatchTable1_charSize()+")";

				}	
			}
			else
			{	
				if(match_Headers1.get(i).getStMatchTable1_Datatype()!=null)
				{
					if(match_Headers1.get(i).getStMatchTable1_Datatype().equals("NUMBER"))
					{
						table1_condition = " TO_NUMBER( t1."+match_Headers1.get(i).getStMatchTable1_header().trim()+",'9999999999.99')";
					}
					else if(match_Headers1.get(i).getStMatchTable1_Datatype().equals("DATE"))
					{
						table1_condition = " TO_DATE( t1."+match_Headers1.get(i).getStMatchTable1_header().trim()+",'"+match_Headers1.get(i).getStMatchTable1_DatePattern()+"')";						
					}
					else if(match_Headers1.get(i).getStMatchTable1_Datatype().equals("TIME"))
					{
						//check whether the column consists of :
						String CHECK_FORMAT = "SELECT DISTINCT "+match_Headers1.get(i).getStMatchTable1_header().trim()+" FROM SETTLEMENT_"+stTTUM_File
								+" WHERE "+match_Headers1.get(i).getStMatchTable1_header().trim()+" IS NOT NULL AND SEG_TRAN_ID IS NOT NULL";
						logger.info("CHECK_ FORMAT IS "+CHECK_FORMAT);
						boolean is_colon = false;
						con = getConnection();
						ps = con.prepareStatement(CHECK_FORMAT);
						rs = ps.executeQuery();
						if(rs.next())
						{
							if(rs.getString(1).contains(":"))
							{
								is_colon = true;
							}
								
						}
						
						if(is_colon)
						{
							table1_condition = "REPLACE( t1."+match_Headers1.get(i).getStMatchTable1_header().trim()+" , ':')";
							
						}
						else
						{
							table1_condition = " LPAD( t1."+match_Headers1.get(i).getStMatchTable1_header().trim()+","+6+",'0')";
							
						}
						
						if(rs!=null){
							rs.close();
						}
						if(ps!=null){
							ps.close();
						}
						if(con!=null){
							con.close();
						}
						
					}
					
				}
				else
				{
					table1_condition = " t1."+match_Headers1.get(i).getStMatchTable1_header().trim();

				}	
			}
			
			logger.info("table1_condition=="+table1_condition);
			//CHECKING PADDING FOR TABLE 2
			/*logger.info("i value is "+i);
			logger.info("match headers length is "+match_Headers2.size());
			logger.info("padding in match headers 2 is "+match_Headers2.get(i).getStMatchTable2_Padding());*/
			if(match_Headers2.get(i).getStMatchTable2_Padding().equals("Y"))
			{
				if(match_Headers2.get(i).getStMatchTable2_Datatype()!=null)
				{
					if(match_Headers2.get(i).getStMatchTable2_Datatype().equals("NUMBER"))
					{
						table2_condition = " SUBSTR( TO_NUMBER( t2."+match_Headers2.get(i).getStMatchTable2_header().trim()+",'9999999999.99')"+","+match_Headers2.get(i).getStMatchTable2_startcharpos()+","+
								 match_Headers2.get(i).getStMatchTable2_charSize()+")";
					}
					else if(match_Headers2.get(i).getStMatchTable2_Datatype().equals("DATE"))
					{
						table2_condition = " TO_DATE( SUBSTR(  t2."+match_Headers2.get(i).getStMatchTable2_header().trim()+","+match_Headers2.get(i).getStMatchTable2_startcharpos()+","+
								 match_Headers2.get(i).getStMatchTable2_charSize()+")"+",'"+match_Headers2.get(i).getStMatchTable2_DatePattern()+"')";							
					}
					else if(match_Headers2.get(i).getStMatchTable2_Datatype().equals("TIME"))
					{
						//check whether the column consists of :
						String CHECK_FORMAT = "SELECT DISTINCT SUBSTR( "+match_Headers2.get(i).getStMatchTable2_header().trim()+","+
						match_Headers2.get(i).getStMatchTable2_startcharpos()+" , "+match_Headers2.get(i).getStMatchTable2_charSize()+" ) FROM SETTLEMENT_"+stFile2_Name
						+" WHERE SUBSTR( "+match_Headers2.get(i).getStMatchTable2_header().trim()+","+
						match_Headers2.get(i).getStMatchTable2_startcharpos()+" , "+match_Headers2.get(i).getStMatchTable2_charSize()+" ) IS NOT NULL" +
								" AND SEG_TRAN_ID IS NOT NULL";
						logger.info("CHECK_ FORMAT IS "+CHECK_FORMAT);
						boolean is_colon = false;
						con = getConnection();
						ps = con.prepareStatement(CHECK_FORMAT);
						rs = ps.executeQuery();
						if(rs.next())
						{
							if(rs.getString(1).contains(":"))
							{
								is_colon = true;
							}
								
						}
						
						if(is_colon)
						{
							//ABC
							table2_condition = "REPLACE( SUBSTR( t2."+match_Headers2.get(i).getStMatchTable2_header().trim()+","+match_Headers2.get(i).getStMatchTable2_startcharpos()+","+
									 match_Headers2.get(i).getStMatchTable2_charSize()+")"+" , ':')";
							
						}
						else
						{
							table2_condition = " LPAD( SUBSTR( t2."+match_Headers2.get(i).getStMatchTable2_header().trim()+","+match_Headers2.get(i).getStMatchTable2_startcharpos()+","+
									 match_Headers2.get(i).getStMatchTable2_charSize()+")"+","+6+", '0')";
							
						}
						
					}
					
				}
				else
				{
					table2_condition = " SUBSTR( t2."+match_Headers2.get(i).getStMatchTable2_header().trim()+","+match_Headers2.get(i).getStMatchTable2_startcharpos()+","+
							 match_Headers2.get(i).getStMatchTable2_charSize()+")";

				}			
				
			}
			else
			{
				logger.info("datatype is "+match_Headers2.get(i).getStMatchTable2_Datatype());
				if(match_Headers2.get(i).getStMatchTable2_Datatype()!=null)
				{
					if(match_Headers2.get(i).getStMatchTable2_Datatype().equals("NUMBER"))
					{
						table2_condition = " TO_NUMBER( t2."+match_Headers2.get(i).getStMatchTable2_header().trim()+",'9999999999.99')";
					}
					else if(match_Headers2.get(i).getStMatchTable2_Datatype().equals("DATE"))
					{
						table2_condition = " TO_DATE( t2."+match_Headers2.get(i).getStMatchTable2_header().trim()+",'"+match_Headers2.get(i).getStMatchTable2_DatePattern()+"')";							
					}
					else if(match_Headers2.get(i).getStMatchTable2_Datatype().equals("TIME"))
					{
						//check whether the column consists of :
						String CHECK_FORMAT = "SELECT DISTINCT  "+match_Headers2.get(i).getStMatchTable2_header().trim()+" FROM SETTLEMENT_"+stFile2_Name
								+" WHERE "+match_Headers2.get(i).getStMatchTable2_header().trim()+" IS NOT NULL AND SEG_TRAN_ID IS NOT NULL";
						logger.info("CHECK_ FORMAT IS "+CHECK_FORMAT);
						boolean is_colon = false;
						con = getConnection();
						ps = con.prepareStatement(CHECK_FORMAT);
						rs = ps.executeQuery();
						if(rs.next())
						{
							if(rs.getString(1).contains(":"))
							{
								is_colon = true;
							}
								
						}
						
						if(is_colon)
						{
							table2_condition = "REPLACE( t2."+match_Headers2.get(i).getStMatchTable2_header().trim()+" , ':')";
							
						}
						else
						{
							table2_condition = "LPAD( t2."+match_Headers2.get(i).getStMatchTable2_header()+","+6+",'0')";
							
						}
						
					}
				}
				else
				{
					table2_condition = " t2."+match_Headers2.get(i).getStMatchTable2_header();

				}		
			
			}
			logger.info("table2_condition=="+table2_condition);
			
			// PREPARING ACTUAL CONDITION OF BOTH TABLES
			if(i==(match_Headers1.size()-1))
			{
				
				//condition = condition + "t1."+match_Headers.get(i).getStMatchTable1_header() + " = t2."+match_Headers.get(i).getStMatchTable2_header();
				condition = condition + table1_condition + " = "+table2_condition;
				
			}
			else
			{
				//condition = condition + "t1."+match_Headers.get(i).getStMatchTable1_header() + " = t2."+match_Headers.get(i).getStMatchTable2_header()+" AND ";
				condition = condition +" ("+ table1_condition +" = "+table2_condition +") AND ";
			
			}
			
			
		}
		
		logger.info("FINALLY CONDITION IS "+condition);
		
		if(generateTTUMBeanObj.getStDate() != null && !generateTTUMBeanObj.getStDate().equals(""))
		{		
			//FOR RC 08
			JOIN1_QUERY = "SELECT * FROM SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+stTTUM_File 
					+ " t1 INNER JOIN SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+stFile2_Name
					+ " t2 ON( "+condition + " ) WHERE"/* TO_CHAR(T1.CREATEDDATE,'DD/MM/YYYY') = "
					+"TO_CHAR(SYSDATE,'DD/MM/YYYY') AND */
					+" TO_CHAR(TO_DATE(T1.VALUE_DATE,'DD/MM/YYYY'),'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"'" 
					+" AND TO_CHAR(T1.FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStFile_Date()
					+"' AND TO_CHAR(TO_DATE(T2.LOCAL_DATE,'MM/DD/YYYY'),'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"' "
					+" AND T1.DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)+"-UNRECON-1'" +
					 " AND (T2.RESPCODE = '8' OR T2.RESPCODE = '08') AND T2.MSGTYPE = '110' AND T2.DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+
					generateTTUMBeanObj.getStSubCategory().substring(0,3)+"'";
		}
		else
		{
			//FOR RC 08
			JOIN1_QUERY = "SELECT * FROM SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+stTTUM_File 
					+ " t1 INNER JOIN SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+stFile2_Name+ " t2 ON( "+condition + " ) WHERE"/* TO_CHAR(T1.CREATEDDATE,'DD/MM/YYYY') = "
					+"TO_CHAR(SYSDATE,'DD/MM/YYYY') AND */
					//+" TO_CHAR(TO_DATE(T1.VALUE_DATE,'DD/MM/YYYY'),'DD/MM/YYYY') = TO_CHAR(SYSDATE-2,'DD-MM-YYYY')"
					+" TO_CHAR(TO_DATE(T1.VALUE_DATE,'DD/MM/YYYY'),'DD/MM/YYYY') = TO_CHAR(TO_DATE('"+generateTTUMBeanObj.getStFile_Date()+"','DD/MM/YYYY')-2,'DD-MM-YYYY')"
					+" AND T1.DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)+"-UNRECON-1'" +
					 " AND (T2.RESPCODE = '8' OR T2.RESPCODE = '08') AND T2.MSGTYPE = '110' AND T2.DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"
					 +generateTTUMBeanObj.getStSubCategory().substring(0, 3)+"'";
		}
		
		
		//FOR RC OTHER THAN 08
		if(generateTTUMBeanObj.getStDate() != null && !generateTTUMBeanObj.getStDate().equals(""))
		{		
			//FOR RC 08
			JOIN2_QUERY = "SELECT * FROM SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+stTTUM_File 
					+ " t1 INNER JOIN SETTLEMENT_"+generateTTUMBeanObj.getStCategory()
					+"_"+stFile2_Name+ " t2 ON( "+condition + " ) WHERE"/* TO_CHAR(T1.CREATEDDATE,'DD/MM/YYYY') = "
					+"TO_CHAR(SYSDATE,'DD/MM/YYYY') AND */
					+" TO_CHAR(TO_DATE(T1.VALUE_DATE,'DD/MM/YYYY'),'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"'"
					+" AND TO_CHAR(T1.FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStFile_Date()
					+"' AND TO_CHAR(TO_DATE(T2.LOCAL_DATE,'MM/DD/YYYY'),'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"' "
					+" AND T1.DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)+"-UNRECON-1'" +
					 " AND T2.RESPCODE NOT IN ('0','08','8') AND T2.MSGTYPE = '110' AND T2.DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"
					+generateTTUMBeanObj.getStSubCategory().substring(0, 3)+"'";
		}
		else
		{
			//FOR RC 08
			JOIN2_QUERY = "SELECT * FROM SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+stTTUM_File 
					+ " t1 INNER JOIN SETTLEMENT_"+generateTTUMBeanObj.getStCategory()
					+"_"+stFile2_Name+ " t2 ON( "+condition + " ) WHERE"/* TO_CHAR(T1.CREATEDDATE,'DD/MM/YYYY') = "
					+"TO_CHAR(SYSDATE,'DD/MM/YYYY') AND */
					//+" TO_CHAR(TO_DATE(T1.VALUE_DATE,'DD/MM/YYYY'),'DD/MM/YYYY') = TO_CHAR(SYSDATE-2,'DD-MM-YYYY')"
					+" TO_CHAR(TO_DATE(T1.VALUE_DATE,'DD/MM/YYYY'),'DD/MM/YYYY') = TO_CHAR(TO_DATE('"+generateTTUMBeanObj.getStFile_Date()+"','DD/MM/YYYY')-8,'DD-MM-YYYY')"
					+" AND T1.DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)+"-UNRECON-1'" +
					 " AND T2.RESPCODE NOT IN ('0','08','8') AND T2.MSGTYPE = '110' AND T2.DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+
					 	generateTTUMBeanObj.getStSubCategory().substring(0, 3)+"'";
		}
		//GET TTUM CONDITION
		/*String cond1 = getTTUMCondition(table1_file_id);
		String cond2 = getTTUMCondition(table2_file_id);*/
	
		logger.info("JOIN1 QUERY IS "+JOIN1_QUERY);
		logger.info("JOINT2 QUERY IS "+JOIN2_QUERY);
		logger.info("----------------------------------------------------------------------------------- DONE ---------------------------------------------");

		//QUERY = "SELECT * FROM TEMP_"+table1_name + " t1 INNER JOIN TEMP_"+table2_name + " t2 ON( "+condition + " ) WHERE T2.MATCHING_FLAG = 'N'";
		logger.info("COMPARE QUERY IS****************************************");
		
		
		/*if(!cond1.equals(""))
		{
			JOIN1_QUERY = JOIN1_QUERY + " AND "+cond1;
			//JOIN2_QUERY = JOIN2_QUERY + " AND "+cond1;
		}
		if(!cond2.equals(""))
		{
			JOIN1_QUERY = JOIN1_QUERY + " AND "+cond2;
			//JOIN2_QUERY = JOIN2_QUERY + " AND "+cond2;
		}
		logger.info("JOIN1 QUERY IS "+JOIN1_QUERY);
		logger.info("JOIN2_QUERY IS "+JOIN2_QUERY);*/
		
		//get failed records for table 1
		
		getFailedRecords(JOIN1_QUERY,table1_file_id, generateTTUMBeanObj);
		getFailedRecords(JOIN2_QUERY,table1_file_id, generateTTUMBeanObj);
		//getFailedRecords(JOIN2_QUERY, table1_file_id , comparebeanObj.getStMergeCategory(), stFile1_Name,stFile2_Name,table2_file_id);//for join query 2 pass file id of table 2
		
		//NOW TRUNCATE ALL TABLES----------------------------
		logger.info("--------------------------------- TRUNCATING ALL TABLES------------------------------------------------------");
		/*String TRUNCATE_QUERY = "TRUNCATE TABLE "+stTable1_Name;
		getJdbcTemplate().execute(TRUNCATE_QUERY);
		TRUNCATE_QUERY = "TRUNCATE TABLE "+stTable1_Name+"_KNOCKOFF";
		getJdbcTemplate().execute(TRUNCATE_QUERY);
		TRUNCATE_QUERY = "TRUNCATE TABLE "+stTable1_Name+"_MATCHED";
		getJdbcTemplate().execute(TRUNCATE_QUERY);
		TRUNCATE_QUERY = "TRUNCATE TABLE TEMP_"+stTable1_Name;
		getJdbcTemplate().execute(TRUNCATE_QUERY);
		TRUNCATE_QUERY = "TRUNCATE TABLE RECON_"+stTable1_Name;
		getJdbcTemplate().execute(TRUNCATE_QUERY);
		// table 2 truncate query
		TRUNCATE_QUERY = "TRUNCATE TABLE "+stTable2_Name;
		getJdbcTemplate().execute(TRUNCATE_QUERY);
		TRUNCATE_QUERY = "TRUNCATE TABLE "+stTable2_Name+"_KNOCKOFF";
		getJdbcTemplate().execute(TRUNCATE_QUERY);
		TRUNCATE_QUERY = "TRUNCATE TABLE "+stTable2_Name+"_MATCHED";
		getJdbcTemplate().execute(TRUNCATE_QUERY);
		TRUNCATE_QUERY = "TRUNCATE TABLE TEMP_"+stTable2_Name;
		getJdbcTemplate().execute(TRUNCATE_QUERY);
		TRUNCATE_QUERY = "TRUNCATE TABLE RECON_"+stTable2_Name;
		getJdbcTemplate().execute(TRUNCATE_QUERY);*/
		
		// ALREADY UPDATED OLD TTUM RECORDS IN MOVETORECON METHOD
		/*String UPDATE_OLD_TTUM_RECORDS = "UPDATE SETTLEMENT_CBS SET CREATEDDATE = TO_CHAR(SYSDATE,'DD/MON/YYYY') WHERE REMARKS LIKE '%ONUS-GENERATE-TTUM%'";
		
		getJdbcTemplate().execute(UPDATE_OLD_TTUM_RECORDS);
		
		logger.info("updated old ttum records");*/
		
		logger.info("***** GenerateRupayTTUMDaoImpl.TTUMRecords End ****");
		
	}
	catch(Exception e)
	{
		demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.TTUMRecords");
		logger.error(" error in GenerateRupayTTUMDaoImpl.TTUMRecords", new Exception("GenerateRupayTTUMDaoImpl.TTUMRecords",e));
		 throw e;
	}
	finally{
		if(rs!=null){
			rs.close();
		}
		if(ps!=null){
			ps.close();
		}
		if(con!=null){
			con.close();
		}
	}
	
}

private static class MatchParameterMaster1 implements RowMapper<CompareBean> {

	@Override
	public CompareBean mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		CompareBean compareBeanObj = new CompareBean();
		compareBeanObj.setStMatchTable1_header(rs.getString("MATCH_HEADER"));
		compareBeanObj.setStMatchTable1_Padding(rs.getString("PADDING"));
		compareBeanObj.setStMatchTable1_startcharpos(rs.getString("START_CHARPOS"));
		compareBeanObj.setStMatchTable1_charSize(rs.getString("CHAR_SIZE"));
		compareBeanObj.setStMatchTable1_DatePattern(rs.getString("DATA_PATTERN"));
		compareBeanObj.setStMatchTable1_Datatype(rs.getString("DATATYPE"));
	
		
		return compareBeanObj;
		
		
	}
}

private static class MatchParameterMaster2 implements RowMapper<CompareBean> {

	@Override
	public CompareBean mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		CompareBean compareBeanObj = new CompareBean();
		compareBeanObj.setStMatchTable2_header(rs.getString("MATCH_HEADER"));
		compareBeanObj.setStMatchTable2_Padding(rs.getString("PADDING"));
		compareBeanObj.setStMatchTable2_startcharpos(rs.getString("START_CHARPOS"));
		compareBeanObj.setStMatchTable2_charSize(rs.getString("CHAR_SIZE"));
		compareBeanObj.setStMatchTable2_DatePattern(rs.getString("DATA_PATTERN"));
		compareBeanObj.setStMatchTable2_Datatype(rs.getString("DATATYPE"));
	
		
		return compareBeanObj;
		
		
	}
}

public void getFailedRecords(String QUERY,int inUpdate_File_Id,GenerateTTUMBean generateTTUMBeanObj)throws Exception
{
	logger.info("***** GenerateRupayTTUMDaoImpl.getFailedRecords Start ****");
	PreparedStatement pstmt = null;
	Connection conn = null;
	ResultSet rset = null;
	String update_condition = "",UPDATE_QUERY = "";
	//PreparedStatement pstmt1 = null;

	
	try
	{
		conn = getConnection();
		pstmt = conn.prepareStatement(QUERY);
		rset = pstmt.executeQuery();
	
		/*int reversal_id = getJdbcTemplate().queryForObject(GET_REVERSAL_ID, new Object[] { (file_id), stCategory},Integer.class);
		logger.info("reversal id is "+reversal_id);
		
		List<KnockOffBean> knockoff_Criteria = getJdbcTemplate().query(GET_KNOCKOFF_PARAMS, new Object[] { reversal_id , file_id}, new KnockOffCriteriaMaster());
		logger.info("knockoff criteria "+knockoff_Criteria.size());
*/
		List<String> query = new ArrayList<String>();
		int count = 1;
		//CREATE CONDITION USING KNOCKOFF CRITERIA 
		while(rset.next())
		{
		//	logger.info("WHILE STARTS");
			update_condition = "";
			String StRC = rset.getString("RESPCODE");
			if(StRC.equals("8") || StRC.equals("08"))
			{
				if(generateTTUMBeanObj.getStDate() != null && !generateTTUMBeanObj.getStDate().equals(""))
				{
					UPDATE_QUERY = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()
							+" SET DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
							+"-UNRECON-"+generateTTUMBeanObj.getInRec_Set_Id()
							+"-GENERATE-TTUM (8)' WHERE TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStFile_Date()+"' " +
							"AND DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)+"-UNRECON-1' " +
							" AND TO_CHAR(TO_DATE(VALUE_DATE,'DD/MM/YYYY'),'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"'";

				}
				else
				{
					
						UPDATE_QUERY = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()
								+" SET DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
								+"-UNRECON-"+generateTTUMBeanObj.getInRec_Set_Id()
								+"-GENERATE-TTUM (8)' WHERE TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStFile_Date()+"'" +
								"AND DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)+"-UNRECON-1' "
								+" AND TO_CHAR(TO_DATE(VALUE_DATE,'DD/MM/YYYY'),'DD/MM/YYYY') = TO_CHAR(TO_DATE('"+generateTTUMBeanObj.getStFile_Date()+"','DD/MM/YYYY')-2,'DD/MM/YYYY')";
					

				}
			}
			else if(!StRC.equals("8") && !StRC.equals("08") && !StRC.equals("0"))
			{

				if(generateTTUMBeanObj.getStDate() != null && !generateTTUMBeanObj.getStDate().equals(""))
				{
					UPDATE_QUERY = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()
							+" SET DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
							+"-UNRECON-"+generateTTUMBeanObj.getInRec_Set_Id()
							+"-GENERATE-TTUM ("+StRC+")' WHERE TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStFile_Date()+"' " +
							"AND DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)+"-UNRECON-1' " +
							" AND TO_CHAR(TO_DATE(VALUE_DATE,'DD/MM/YYYY'),'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"'";
				}
				else
				{
					
						UPDATE_QUERY = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()
								+" SET DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
								+"-UNRECON-"+generateTTUMBeanObj.getInRec_Set_Id()
								+"-GENERATE-TTUM ("+StRC+")' WHERE TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStFile_Date()+"' " +
								"AND DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)+"-UNRECON-1' "
								+" AND TO_CHAR(TO_DATE(VALUE_DATE,'DD/MM/YYYY'),'DD/MM/YYYY') = TO_CHAR(TO_DATE('"+generateTTUMBeanObj.getStFile_Date()+"','DD/MM/YYYY')-8,'DD/MM/YYYY')";
					
				}
			
				
			}
			
			logger.info("UPDATE_QUERY=="+UPDATE_QUERY);
			
				int rev_id = getJdbcTemplate().queryForObject(GET_REVERSAL_ID, new Object[] { (inUpdate_File_Id), generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory()},Integer.class);
				logger.info("reversal id is "+rev_id);
				
				List<KnockOffBean> knockoff_Criteria1 = getJdbcTemplate().query(GET_KNOCKOFF_PARAMS, new Object[] { rev_id , inUpdate_File_Id}, new KnockOffCriteriaMaster());
				
				for(int i = 0; i<knockoff_Criteria1.size() ; i++)
				{
					if(i == (knockoff_Criteria1.size()-1))
					{
						if(knockoff_Criteria1.get(i).getStReversal_padding().equals("Y"))
						{
								update_condition = update_condition + " SUBSTR( "+knockoff_Criteria1.get(i).getStReversal_header()+","+knockoff_Criteria1.get(i).getStReversal_charpos()
										+","+knockoff_Criteria1.get(i).getStReversal_charsize()+")"
										+" "+knockoff_Criteria1.get(i).getStReversal_condition() 
										+" SUBSTR( '"+rset.getString(knockoff_Criteria1.get(i).getStReversal_header())+"',"+knockoff_Criteria1.get(i).getStReversal_charpos()
										+","+knockoff_Criteria1.get(i).getStReversal_charsize()+")";
								
						}
						else
						{
								update_condition = update_condition + knockoff_Criteria1.get(i).getStReversal_header()
										+" "+knockoff_Criteria1.get(i).getStReversal_condition() +" '"+rset.getString(knockoff_Criteria1.get(i).getStReversal_header())
										+"'";
								
						}
					}
					else
					{
						if(knockoff_Criteria1.get(i).getStReversal_padding().equals("Y"))
						{
							update_condition = update_condition + " SUBSTR( "+knockoff_Criteria1.get(i).getStReversal_header()+","+knockoff_Criteria1.get(i).getStReversal_charpos()
										+","+knockoff_Criteria1.get(i).getStReversal_charsize()+")"
										+" "+knockoff_Criteria1.get(i).getStReversal_condition() 
										+" SUBSTR( '"+rset.getString(knockoff_Criteria1.get(i).getStReversal_header())+"',"+knockoff_Criteria1.get(i).getStReversal_charpos()
										+","+knockoff_Criteria1.get(i).getStReversal_charsize()+") AND ";
							
						}
						else
						{
								update_condition = update_condition + knockoff_Criteria1.get(i).getStReversal_header()+" "+knockoff_Criteria1.get(i).getStReversal_condition()
										+" '"+rset.getString(knockoff_Criteria1.get(i).getStReversal_header())+"' AND ";
								
						}
					
					}
					
				}
				
				logger.info("update condition is "+update_condition);
				if(!update_condition.equals(""))
				{
					UPDATE_QUERY = UPDATE_QUERY + " AND " + update_condition;
				}
				logger.info("UPDATE QUERY IS "+UPDATE_QUERY);
				
				//PreparedStatement pstmt1 = conn.prepareStatement(UPDATE_QUERY);
				
				query.add(UPDATE_QUERY);
				
				
				
				if(count == 200)
				{
					String[] update = new String[query.size()];
					update = query.toArray(update);
					getJdbcTemplate().batchUpdate(update);
					query.clear();
					count = 1;
				}
				//getJdbcTemplate().update(UPDATE_QUERY);
				
							count++;
		}
		
		//UPDATE REMAINING RECORDS IF ANY
		if(query.size() > 0)
		{
			String[] update = new String[query.size()];
			update = query.toArray(update);
			getJdbcTemplate().batchUpdate(update);
			query.clear();
		}
		logger.info("while completed");
		logger.info("***** GenerateRupayTTUMDaoImpl.getFailedRecords End ****");
		
	}
	catch(Exception e)
	{
		demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.getFailedRecords");
		logger.error(" error in GenerateRupayTTUMDaoImpl.getFailedRecords", new Exception("GenerateRupayTTUMDaoImpl.getFailedRecords",e));
		 throw e;
	}
	finally{
		if(rset!=null){
			rset.close();
		}
		if(pstmt!=null){
			pstmt.close();
		}
		if(conn!=null){
			conn.close();
		}
	}
	
	
}

private static class KnockOffCriteriaMaster implements RowMapper<KnockOffBean> {

	@Override
	public KnockOffBean mapRow(ResultSet rs, int rowNum) throws SQLException {
		KnockOffBean knockOffBean = new KnockOffBean();

		knockOffBean.setStReversal_header(rs.getString("HEADER"));
		knockOffBean.setStReversal_padding(rs.getString("PADDING"));
		knockOffBean.setStReversal_charpos(rs.getString("START_CHARPOSITION"));
		knockOffBean.setStReversal_charsize(rs.getString("CHAR_SIZE"));
		knockOffBean.setStReversal_value(rs.getString("HEADER_VALUE"));
		knockOffBean.setStReversal_condition(rs.getString("CONDITION"));
		return knockOffBean;


	}
}

@Override
public List<List<GenerateTTUMBean>> generateCBSTTUM(GenerateTTUMBean generateTTUMBeanObj,List<GenerateTTUMBean> Diff_Data)throws Exception
{
	logger.info("***** GenerateRupayTTUMDaoImpl.generateCBSTTUM Start ****");
	
	Connection conn = null;
	PreparedStatement pstmt=null;
	ResultSet rset = null;
	String stAction = "";
	List<List<GenerateTTUMBean>> Data = new ArrayList<>();
	List<GenerateTTUMBean> ttum_data = new ArrayList<>();
	List<GenerateTTUMBean> Excel_headers = new ArrayList<>();
	List<String> Headers = new ArrayList<>();
	String table_cols = "DCRS_REMARKS VARCHAR (100 BYTE), CREATEDDATE DATE , CREATEDBY VARCHAR (100 BYTE),RECORDS_DATE DATE";
	String insert_cols = "DCRS_REMARKS,CREATEDDATE, CREATEDBY,RECORDS_DATE";
	try
	{
		
		//Excel headers
		//Headers.add("FILE_HEADER");
	/*	Headers.add("ACCOUNT_NUMBER");
		Headers.add("CURRENCY_CODE");
		Headers.add("SERVICE_OUTLET");
		Headers.add("PART_TRAN_TYPE");
		Headers.add("TRANSACTION_AMOUNT");
		Headers.add("TRANSACTION_PARTICULARS");
		Headers.add("REFERENCE_NUMBER");
		Headers.add("REFERENCE_CURRENCY_CODE");
		Headers.add("REFERENCE_TRANSACTION_AMOUNT");
		Headers.add("REMARKS");*/
		
		Headers.add("ACCOUNT NUMBER");
		Headers.add("CURRENCY CODE");
		Headers.add("SERVICE OUTLET");
		Headers.add("PART TRAN TYPE");
		Headers.add("TRANSACTION AMOUNT");
		Headers.add("TRANSACTION PARTICULARS");
		Headers.add("REFERENCE CURRENCY CODE");
		Headers.add("REFERENCE AMOUNT");
		Headers.add("REMARKS");
		Headers.add("REFERENCE NUMBER");
		Headers.add("ACCOUNT REPORT CODE");
		
		generateTTUMBeanObj.setStExcelHeader(Headers);
		
		Excel_headers.add(generateTTUMBeanObj);
		
		
		
		
		
		String GET_TTUM_DATA = "SELECT E,CONTRA_ACCOUNT,FORACID,TO_NUMBER(AMOUNT,999999999.99) AS AMOUNT, TO_CHAR(TO_DATE(VALUE_DATE,'DD/MM/YYYY'),'DDMMYY') AS VALUE_DATE,"
				+"SUBSTR(REF_NO,2,6) AS REF_NO, REMARKS,PARTICULARALS FROM SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()
				+" WHERE DCRS_REMARKS LIKE '%GENERATE-TTUM-"+generateTTUMBeanObj.getInRec_Set_Id()+"%' AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStFile_Date()+"'";
		
		logger.info("GET_TTUM_dATA "+GET_TTUM_DATA);
		
		conn = getConnection();
		pstmt = conn.prepareStatement(GET_TTUM_DATA);
		rset = pstmt.executeQuery();
		
		while (rset.next())
		{
			/*if(generateTTUMBeanObj.getStSubCategory().equals("DOMESTIC"))
			{*/
				stAction = rset.getString("E");
				GenerateTTUMBean generateTTUMBean = new GenerateTTUMBean();
				if(stAction.equals("C"))
				{
					//GL DEBIT
					generateTTUMBean.setStDebitAcc(rset.getString("FORACID"));
					//CUSTOMER ACC CREDIT
					generateTTUMBean.setStCreditAcc(rset.getString("CONTRA_ACCOUNT"));
					generateTTUMBean.setStAmount(rset.getString("AMOUNT"));
					//generateTTUMBean.setStDate(rset.getString("VALUE_DATE"));
					//String stTran_particular = "REV/"+generateTTUMBeanObj.getStCategory()+"/"+rset.getString("VALUE_DATE")+"/"+rset.getString("REF_NO");
					
					//Tran Particular � REV-T2 -RPAY-DDMMYY (Transaction date)-Trace number-Merchant name
					String stTran_particular = "REV-T2-RPAY-"+rset.getString("VALUE_DATE")+"-"+rset.getString("REF_NO")+"-"+rset.getString("PARTICULARALS");
					generateTTUMBean.setStDate(rset.getString("VALUE_DATE"));
					generateTTUMBean.setStTran_particulars(stTran_particular);
					generateTTUMBean.setStCard_Number(rset.getString("REMARKS"));
					String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
					generateTTUMBean.setStRemark(remark);
					ttum_data.add(generateTTUMBean);
					Diff_Data.add(generateTTUMBean);


				}
				else if(stAction.equals("D"))
				{
					//CUSTOMER ACC DEBIT
					generateTTUMBean.setStCreditAcc(rset.getString("FORACID"));
					//GL CREDIT
					generateTTUMBean.setStDebitAcc(rset.getString("CONTRA_ACCOUNT"));
					generateTTUMBean.setStAmount(rset.getString("AMOUNT"));
					//generateTTUMBean.setStDate(rset.getString("VALUE_DATE"));
					//String stTran_particular = "REV/"+generateTTUMBeanObj.getStCategory()+"/"+rset.getString("VALUE_DATE")+"/"+rset.getString("REF_NO");
					
					//Tran Particular � ADR-RPAY-DDMMYY (Transaction date)-Trace number-Merchant name
					String stTran_particular = "ADR-RPAY-"+rset.getString("VALUE_DATE")+"-"+rset.getString("REF_NO")+"-"+rset.getString("PARTICULARALS");
					generateTTUMBean.setStDate(rset.getString("VALUE_DATE"));
					generateTTUMBean.setStTran_particulars(stTran_particular);
					generateTTUMBean.setStCard_Number(rset.getString("REMARKS"));
					String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
					generateTTUMBean.setStRemark(remark);
					ttum_data.add(generateTTUMBean);
					Diff_Data.add(generateTTUMBean);

				}
			//}
			/*else if(generateTTUMBeanObj.getStSubCategory().equals("SURCHARGE"))
			{

				//GL DEBIT
				generateTTUMBean.setStDebitAcc(rset.getString("FORACID"));
				//CUSTOMER ACC CREDIT
				generateTTUMBean.setStCreditAcc(rset.getString("CONTRA_ACCOUNT"));
				generateTTUMBean.setStAmount(rset.getString("AMOUNT"));
				//generateTTUMBean.setStDate(rset.getString("VALUE_DATE"));
				String stTran_particular = "REV/"+generateTTUMBeanObj.getStCategory()+"/"+rset.getString("VALUE_DATE")+"/"+rset.getString("REF_NO");
				generateTTUMBean.setStTran_particulars(stTran_particular);
				generateTTUMBean.setStRRNo(rset.getString("REMARKS"));
				String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
				generateTTUMBean.setStRemark(remark);
				ttum_data.add(generateTTUMBean);
				Diff_Data.add(generateTTUMBean);


			
			}*/
		}	
			Data.add(Excel_headers);
			Data.add(Diff_Data);
			
			
			
			
			
			
			// CREATE NEW TABLE FOR INSERTING TTUM ENTRIES
			for(int i = 0 ; i<Headers.size();i++)
			{
				table_cols =table_cols+","+ Headers.get(i)+" VARCHAR (100 BYTE)";
				insert_cols = insert_cols+","+Headers.get(i);
			}
			
			String CHECK_TABLE = "SELECT count (*) FROM tab WHERE tname  = 'TTUM_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
					+"_"+generateTTUMBeanObj.getStFile_Name()+"'";
			
			logger.info("CHECK_TABLE=="+CHECK_TABLE);
			int tableExist = getJdbcTemplate().queryForObject(CHECK_TABLE, new Object[] { },Integer.class);
			
			logger.info("tableExist=="+tableExist);
			if(tableExist == 0)
			{
				String CREATE_QUERY = "CREATE TABLE TTUM_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
						+"_"+generateTTUMBeanObj.getStFile_Name()+" ("+table_cols+")";
				
				logger.info("CREATE_QUERY=="+CREATE_QUERY);
				getJdbcTemplate().execute(CREATE_QUERY);
			}
			
			//INSERT TTUM ENTRIES IN ABOVE CREATED TABLE
			for(int i = 0;i<ttum_data.size();i++)
			{
				GenerateTTUMBean beanObj = new GenerateTTUMBean();
				beanObj = ttum_data.get(i);
				String INSERT_QUERY = "INSERT INTO TTUM_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
						+"_"+generateTTUMBeanObj.getStFile_Name() 
						+"("+insert_cols+") VALUES ('"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
						+"-UNRECON-TTUM-"+generateTTUMBeanObj.getInRec_Set_Id()
						+"',SYSDATE,'"+generateTTUMBeanObj.getStEntry_by()+"',TO_DATE('"+beanObj.getStDate()+"','DDMMYY'),'"+beanObj.getStDebitAcc()+"','INR','999','D','"+
							beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','"+beanObj.getStCard_Number()+"','INR','"+beanObj.getStAmount()+"','"+
							beanObj.getStRemark()+"')";
				
				logger.info("INSERT_QUERY=="+INSERT_QUERY);
				getJdbcTemplate().execute(INSERT_QUERY);
				
				 INSERT_QUERY = "INSERT INTO TTUM_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
						 +"_"+generateTTUMBeanObj.getStFile_Name() 
						 +"("+insert_cols+") VALUES ('"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
						 +"-UNRECON-TTUM-"+generateTTUMBeanObj.getInRec_Set_Id()
						 +"',SYSDATE,'"+generateTTUMBeanObj.getStEntry_by()+"',TO_DATE('"+beanObj.getStDate()+"','DDMMYY'),'"+beanObj.getStCreditAcc()+"','INR','999','C','"+
							beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','"+beanObj.getStCard_Number()+"','INR','"+beanObj.getStAmount()+"','"+
							beanObj.getStRemark()+"')";
				 
				 logger.info("INSERT_QUERY=="+INSERT_QUERY);
				 getJdbcTemplate().execute(INSERT_QUERY);		
			}
			
			//UPDATE TTUM GENERATED RECORDS
			/*String UPDATE_RECORDS = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()+
					" SET DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
					+"-UNRECON-"+generateTTUMBeanObj.getInRec_Set_Id()
					+"-GENERATED-TTUM'"
					+" WHERE DCRS_REMARKS LIKE '%GENERATE-TTUM%'";*/
			
			String UPDATE_RECORDS = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()+
					" SET DCRS_REMARKS = REPLACE(DCRS_REMARKS,'GENERATE','GENERATED')"
					+" WHERE DCRS_REMARKS LIKE '%GENERATE-TTUM%'";
			
			logger.info("UPDATE_RECORDS=="+UPDATE_RECORDS);
			getJdbcTemplate().execute(UPDATE_RECORDS);
			
		
			logger.info("***** GenerateRupayTTUMDaoImpl.generateCBSTTUM End ****");
	}
	catch(Exception e)
	{
		demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.generateCBSTTUM");
		logger.error(" error in GenerateRupayTTUMDaoImpl.generateCBSTTUM", new Exception("GenerateRupayTTUMDaoImpl.generateCBSTTUM",e));
		 throw e;
	}
	finally{
		if(rset!=null){
			rset.close();
		}
		if(pstmt!=null){
			pstmt.close();
		}
		if(conn!=null){
			conn.close();
		}
	}
	return Data;
}


@Override
public List<List<GenerateTTUMBean>> generateCBSSurchargeTTUM(GenerateTTUMBean generateTTUMBeanObj)throws Exception
{
	logger.info("***** GenerateRupayTTUMDaoImpl.generateCBSSurchargeTTUM Start ****");
	
	Connection conn = null;
	PreparedStatement pstmt=null;
	ResultSet rset=null;
	String stAction = "";
	List<List<GenerateTTUMBean>> Data = new ArrayList<>();
	List<GenerateTTUMBean> ttum_data = new ArrayList<>();
	List<GenerateTTUMBean> Excel_headers = new ArrayList<>();
	List<String> Headers = new ArrayList<>();
	String table_cols = "DCRS_REMARKS VARCHAR (100 BYTE), CREATEDDATE DATE , CREATEDBY VARCHAR (100 BYTE)";
	String insert_cols = "DCRS_REMARKS,CREATEDDATE, CREATEDBY";
	try
	{
		
		//Excel headers
		//Headers.add("FILE_HEADER");
		Headers.add("ACCOUNT_NUMBER");
		Headers.add("CURRENCY_CODE");
		Headers.add("SERVICE_OUTLET");
		Headers.add("PART_TRAN_TYPE");
		Headers.add("TRANSACTION_AMOUNT");
		Headers.add("TRANSACTION_PARTICULARS");
		Headers.add("REFERENCE_CURRENCY_CODE");
		Headers.add("REFERENCE_AMOUNT");
		Headers.add("REMARKS");
		Headers.add("REFERENCE_NUMBER");
		Headers.add("ACCOUNT_REPORT_CODE");
		
		
		generateTTUMBeanObj.setStExcelHeader(Headers);
		
		Excel_headers.add(generateTTUMBeanObj);
		
		String dateDiff =" Select to_date(MAX(FILEDATE),'DD/MM/RRRR')  - to_date('"+generateTTUMBeanObj.getStDate()+"', 'DD/MM/RRRR' )  from settlement_rupay_cbs where dcrs_remarks like '%"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)+"%' " ;
		conn = getConnection();
		pstmt = conn.prepareStatement(dateDiff);
		rset = pstmt.executeQuery();
		
		int datediff =0;
		if(rset.next()){
			datediff = rset.getInt(1);
		}
		
		
		conn = null;
		pstmt = null;
		rset = null ;
		
		String GET_TTUM_DATA ="";
		if(datediff > 3){
			 GET_TTUM_DATA = "SELECT FORACID,TO_NUMBER(AMOUNT,999999999.99) AS AMOUNT,CONTRA_ACCOUNT, TO_CHAR(TO_DATE(VALUE_DATE,'DD/MM/YYYY'),'DDMMYY') AS VALUE_DATE,"
					+"SUBSTR(REF_NO,2,6) AS REF_NO, REMARKS,PARTICULARALS FROM SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()+"_BK"
					+" WHERE DCRS_REMARKS LIKE '%GENERATE-TTUM%' AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"'";
			logger.info("GET_TTUM_dATA "+GET_TTUM_DATA);
		}else{
			 GET_TTUM_DATA = "SELECT FORACID,TO_NUMBER(AMOUNT,999999999.99) AS AMOUNT,CONTRA_ACCOUNT, TO_CHAR(TO_DATE(VALUE_DATE,'DD/MM/YYYY'),'DDMMYY') AS VALUE_DATE,"
					+"SUBSTR(REF_NO,2,6) AS REF_NO, REMARKS,PARTICULARALS FROM SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()
					+" WHERE DCRS_REMARKS LIKE '%GENERATE-TTUM%' AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"'";
			logger.info("GET_TTUM_dATA "+GET_TTUM_DATA);
		}

		
		conn = getConnection();
		pstmt = conn.prepareStatement(GET_TTUM_DATA);
		rset = pstmt.executeQuery();
		
		while (rset.next())
		{
				
				GenerateTTUMBean generateTTUMBean = new GenerateTTUMBean();
				
					//GL DEBIT
					generateTTUMBean.setStDebitAcc(rset.getString("FORACID"));
					//CUSTOMER ACC CREDIT
					generateTTUMBean.setStCreditAcc(rset.getString("CONTRA_ACCOUNT"));
					generateTTUMBean.setStAmount(rset.getString("AMOUNT"));
					//generateTTUMBean.setStDate(rset.getString("VALUE_DATE"));
					//String stTran_particular = "REV/"+generateTTUMBeanObj.getStCategory()+"/"+rset.getString("VALUE_DATE")+"/"+rset.getString("REF_NO");
					//Debit part tran
					//Tran Particular �RPAY-SUR-DDMMYY (Transaction date)-Trace number-Merchant name
					String stDRTran_particular = "RPAY-SUR-"+rset.getString("VALUE_DATE")+"-"+rset.getString("REF_NO")+"-"+rset.getString("PARTICULARALS");
					generateTTUMBean.setStTran_particulars(stDRTran_particular);
					generateTTUMBean.setStCard_Number(rset.getString("REMARKS"));
					String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
					generateTTUMBean.setStRemark(remark);
					ttum_data.add(generateTTUMBean);
					//Diff_Data.add(generateTTUMBean);
		
		}	
			Data.add(Excel_headers);
			Data.add(ttum_data);
			
			//UPDATE TTUM GENERATED RECORDS
			String UPDATE_RECORDS = "";
			if(datediff > 3){
				
				 UPDATE_RECORDS = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()+"_BK "+
						" SET DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
						+"-UNRECON-GENERATED-TTUM-3'"//generateTTUMBeanObj.getInRec_Set_Id()+"'"
						+" WHERE DCRS_REMARKS LIKE '%GENERATE-TTUM%' AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"'";
			}else{
				
				 UPDATE_RECORDS = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()+
						" SET DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
						+"-UNRECON-GENERATED-TTUM-3'"//generateTTUMBeanObj.getInRec_Set_Id()+"'"
						+" WHERE DCRS_REMARKS LIKE '%GENERATE-TTUM%' AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"'";
			}
	
			
			logger.info("UPDATE_RECORDS=="+UPDATE_RECORDS);
			getJdbcTemplate().execute(UPDATE_RECORDS);
			
			
			
			
			// CREATE NEW TABLE FOR INSERTING TTUM ENTRIES
			for(int i = 0 ; i<Headers.size();i++)
			{
				table_cols =table_cols+","+ Headers.get(i)+" VARCHAR (100 BYTE)";
				
				if((i+1)== Headers.size() ){
					insert_cols += Headers.get(i) ;
				}else{
					insert_cols += Headers.get(i)+ ",";
				}
				
				
				
			}
			
			String CHECK_TABLE = "SELECT count (*) FROM tab WHERE tname  = 'TTUM_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
					+"_"+generateTTUMBeanObj.getStFile_Name()+"'";
			int tableExist = getJdbcTemplate().queryForObject(CHECK_TABLE, new Object[] { },Integer.class);
			
			logger.info("CHECK_TABLE=="+CHECK_TABLE);
			logger.info("tableExist=="+tableExist);
			if(tableExist == 0)
			{
				String CREATE_QUERY = "CREATE TABLE TTUM_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
						+"_"+generateTTUMBeanObj.getStFile_Name()+" ("+table_cols+")";
				
				logger.info("CREATE_QUERY=="+CREATE_QUERY);
				getJdbcTemplate().execute(CREATE_QUERY);
			}
			
			//INSERT TTUM ENTRIES IN ABOVE CREATED TABLE
			for(int i = 0;i<ttum_data.size();i++)
			{
				GenerateTTUMBean beanObj = new GenerateTTUMBean();
				beanObj = ttum_data.get(i);
				String INSERT_QUERY = " INSERT INTO ttum_rupay_sur_cbs " +
						"             (dcrs_remarks, createddate, createdby, " +
						"              account_number, currency_code, service_outlet, part_tran_type, " +
						"              transaction_amount, transaction_particulars, " +
						"              reference_currency_code, reference_amount, remarks " +
						"             ) " +
						" VALUES ('"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
						+"-UNRECON-TTUM-3"
						+"',SYSDATE,'"+generateTTUMBeanObj.getStEntry_by()+"','"+beanObj.getStDebitAcc()+"','INR','999','D','"+
							beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','INR','"+beanObj.getStAmount()+"','"+
							beanObj.getStRemark()+"' ) "; //+beanObj.getStCard_Number()+
				
				logger.info("insert_cols=="+ insert_cols);
				logger.info("INSERT_QUERY=="+INSERT_QUERY);
				getJdbcTemplate().execute(INSERT_QUERY);
				
				//GET TRAN PARTICULAR FOR CR PART 
				//String stCRTran_Particular = beanObj.getStTran_particulars()
				String[] tran_part = beanObj.getStTran_particulars().split("-");
				//REV-RPAY-SURCHARGE-DDMMYY (Transaction date) � TRACE number-
				String stCRTran_Particular = "REV-RPAY-SURCHARGE-"+tran_part[2]+"-"+tran_part[3];;
							
				 INSERT_QUERY = " INSERT INTO ttum_rupay_sur_cbs " +
						 "             (dcrs_remarks, createddate, createdby, " +
						 "              account_number, currency_code, service_outlet, part_tran_type, " +
						 "              transaction_amount, transaction_particulars, " +
						 "              reference_currency_code, reference_amount, remarks " +
						 "             ) " +
						 "VALUES ('"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
						 +"-UNRECON-TTUM-3"
						 +"',SYSDATE,'"+generateTTUMBeanObj.getStEntry_by()+"','"+beanObj.getStCreditAcc()+"','INR','999','C','"+
							beanObj.getStAmount()+"','"+stCRTran_Particular+"','INR','"+beanObj.getStAmount()+"','"+
							beanObj.getStRemark()+"' ) "; //beanObj.getStCard_Number()
				 
				 logger.info("INSERT_QUERY=="+INSERT_QUERY);
				 getJdbcTemplate().execute(INSERT_QUERY);		
			}
			
		
			logger.info("***** GenerateRupayTTUMDaoImpl.generateCBSSurchargeTTUM End ****");
	}
	catch(Exception e)
	{
		demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.generateCBSSurchargeTTUM");
		logger.error(" error in GenerateRupayTTUMDaoImpl.generateCBSSurchargeTTUM", new Exception("GenerateRupayTTUMDaoImpl.generateCBSSurchargeTTUM",e));
		 throw e;
	}
	finally{
		if(rset!=null){
			rset.close();
		}
		if(pstmt!=null){
			pstmt.close();
		}
		if(conn!=null){
			conn.close();
		}
	}
	return Data;
}

@Override
public void TTUM_forDPart(GenerateTTUMBean generatettumBeanObj)throws Exception
{
	logger.info("***** GenerateRupayTTUMDaoImpl.TTUM_forDPart Start ****");
	
	Connection conn = null;
	PreparedStatement pstmt=null;
	ResultSet rset=null;
	
	String update_condition = "";
	try
	{
		
		int file_id = getJdbcTemplate().queryForObject(GET_FILE_ID, new Object[] { generatettumBeanObj.getStFile_Name() , generatettumBeanObj.getStCategory(),generatettumBeanObj.getStSubCategory() },Integer.class);
		int reversal_id = getJdbcTemplate().queryForObject(GET_REVERSAL_ID, new Object[] { (file_id), (generatettumBeanObj.getStCategory()+"_"+generatettumBeanObj.getStSubCategory())},Integer.class);
		
		List<KnockOffBean> knockoff_Criteria = getJdbcTemplate().query(GET_KNOCKOFF_CRITERIA, new Object[] { reversal_id , file_id}, new KnockOffCriteriaMaster());
		
		String condition = getCondition(knockoff_Criteria);
		
		
		
		String KNOCKOFF_QUERY = "SELECT * FROM SETTLEMENT_"+generatettumBeanObj.getStCategory()+"_"+generatettumBeanObj.getStFile_Name()+
				" OS1 WHERE OS1.DCRS_REMARKS = '"+generatettumBeanObj.getStCategory()+"_"+generatettumBeanObj.getStSubCategory().substring(0, 3)+
				"-UNRECON-1' AND OS1.E = 'D' "+ //AND TO_CHAR(OS1.CREATEDDATE,'DD/MM/YYYY') = TO_CHAR(SYSDATE,'DD/MM/YYYY') " +
				" AND TO_CHAR(TO_DATE(VALUE_DATE,'DD-MM-YYYY'),'DD/MM/YYYY') = '"+generatettumBeanObj.getStDate()+"' AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generatettumBeanObj.getStFile_Date()+"'"+
				"AND EXISTS  " +
				" (SELECT * FROM SETTLEMENT_"+generatettumBeanObj.getStCategory()+"_"+generatettumBeanObj.getStFile_Name()
				+" OS2 WHERE (OS2.E = 'C' AND OS2.DCRS_REMARKS = '"+generatettumBeanObj.getStMerger_Category()+"-MATCHED-"+generatettumBeanObj.getInRec_Set_Id()+"') "
				+" AND ("+condition+")) ";
		
		logger.info("KNOCKOFF QUERY FOR PART TYPE D IS "+KNOCKOFF_QUERY);
		
		//UPDATE THESE RECORDS
		conn = getConnection();
		pstmt = conn.prepareStatement(KNOCKOFF_QUERY);				
		rset = pstmt.executeQuery();
		
		while(rset.next())
		{
			/*String UPDATE_QUERY = "UPDATE SETTLEMENT_"+generatettumBeanObj.getStCategory()+"_"+generatettumBeanObj.getStFile_Name()+
					" SET DCRS_REMARKS = '"+generatettumBeanObj.getStCategory()+"_"+generatettumBeanObj.getStSubCategory()+"-GENERATE-TTUM'"+
					" WHERE E = 'D'";// AND TO_CHAR(CREATEDDATE,'DD/MM/YYYY') = TO_CHAR(SYSDATE,'DD/MM/YYYY') "; 
*/
			String UPDATE_QUERY = "UPDATE SETTLEMENT_"+generatettumBeanObj.getStCategory()+"_"+generatettumBeanObj.getStFile_Name()+
					" SET DCRS_REMARKS = REPLACE(DCRS_REMARKS,'UNRECON','UNRECON-GENERATE-TTUM') "+
					" WHERE E = 'D'";// AND TO_CHAR(CREATEDDATE,'DD/MM/YYYY') = TO_CHAR(SYSDATE,'DD/MM/YYYY') "; 
			// DCRS_REMARKS = REPLACE(DCRS_REMARKS,'UNRECON','UNRECON-GENERATE-TTUM) 
			for(int i = 0; i<knockoff_Criteria.size() ; i++)
			{
				if(i == (knockoff_Criteria.size()-1))
				{
					if(knockoff_Criteria.get(i).getStReversal_padding().equals("Y"))
					{
						update_condition = update_condition + " SUBSTR( "+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
								+","+knockoff_Criteria.get(i).getStReversal_charsize()+")"
								+" "+knockoff_Criteria.get(i).getStReversal_condition() 
								+" SUBSTR( '"+rset.getString(knockoff_Criteria.get(i).getStReversal_header())+"',"+knockoff_Criteria.get(i).getStReversal_charpos()
								+","+knockoff_Criteria.get(i).getStReversal_charsize()+")";

					}
					else
					{
						update_condition = update_condition + knockoff_Criteria.get(i).getStReversal_header()
								+" "+knockoff_Criteria.get(i).getStReversal_condition() +" '"+rset.getString(knockoff_Criteria.get(i).getStReversal_header())
								+"'";

					}
				}
				else
				{
					if(knockoff_Criteria.get(i).getStReversal_padding().equals("Y"))
					{
						update_condition = update_condition + " SUBSTR( "+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
								+","+knockoff_Criteria.get(i).getStReversal_charsize()+")"
								+" "+knockoff_Criteria.get(i).getStReversal_condition() 
								+" SUBSTR( '"+rset.getString(knockoff_Criteria.get(i).getStReversal_header())+"',"+knockoff_Criteria.get(i).getStReversal_charpos()
								+","+knockoff_Criteria.get(i).getStReversal_charsize()+") AND ";

					}
					else
					{
						update_condition = update_condition + knockoff_Criteria.get(i).getStReversal_header()+" "+knockoff_Criteria.get(i).getStReversal_condition()
								+" '"+rset.getString(knockoff_Criteria.get(i).getStReversal_header())+"' AND ";

					}

				}

			}
			
			if(!update_condition.equals(""))
			{
				UPDATE_QUERY = UPDATE_QUERY +" AND "+update_condition;
			}
			logger.info("UPDATE QUERY IS "+UPDATE_QUERY);
			
			getJdbcTemplate().execute(UPDATE_QUERY);

		}
		logger.info("***** GenerateRupayTTUMDaoImpl.TTUM_forDPart End ****");
		
	}
	catch(Exception e)
	{
		demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.TTUM_forDPart");
		logger.error(" error in GenerateRupayTTUMDaoImpl.TTUM_forDPart", new Exception("GenerateRupayTTUMDaoImpl.TTUM_forDPart",e));
		 throw e;
	}
	finally{
		if(rset!=null){
			rset.close();
		}
		if(pstmt!=null){
			pstmt.close();
		}
		if(conn!=null){
			conn.close();
		}
	}
}


public String getCondition(List<KnockOffBean> knockoff_Criteria) throws Exception
{
	logger.info("***** GenerateRupayTTUMDaoImpl.getCondition Start ****");
	
	String select_parameters = "", condition = "", update_condition="" ;
	List<KnockOffBean> Update_Headers = new ArrayList<>();
	try{
	for(int i = 0 ;i<knockoff_Criteria.size();i++)
	{
		if(i == (knockoff_Criteria.size()-1))
		{
			if(knockoff_Criteria.get(i).getStReversal_value()!=null)
			{
				if(knockoff_Criteria.get(i).getStReversal_padding().equals("Y"))
				{
					condition = condition + " SUBSTR( OS1."+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
							+","+knockoff_Criteria.get(i).getStReversal_charsize()+")"
							+" "+knockoff_Criteria.get(i).getStReversal_condition() + " "+knockoff_Criteria.get(i).getStReversal_value();
					/*update_condition = update_condition +knockoff_Criteria.get(i).getStReversal_header()+" "+knockoff_Criteria.get(i).getStReversal_condition() 
							+ " "+knockoff_Criteria.get(i).getStReversal_value();*/
					/*update_condition = update_condition +"SUBSTR("+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
							+","+knockoff_Criteria.get(i).getStReversal_charsize()+") "+ knockoff_Criteria.get(i).getStReversal_condition()+
							knockoff_Criteria.get(i).getStReversal_value();*/
					select_parameters = select_parameters+ " SUBSTR( OS1."+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
							+","+knockoff_Criteria.get(i).getStReversal_charsize()+") AS "+knockoff_Criteria.get(i).getStReversal_header();
				}
				else
				{
					condition = condition +"OS1."+ knockoff_Criteria.get(i).getStReversal_header()+" "+knockoff_Criteria.get(i).getStReversal_condition()
								+" "+knockoff_Criteria.get(i).getStReversal_value();
					/*update_condition = update_condition +knockoff_Criteria.get(i).getStReversal_header()+" "+knockoff_Criteria.get(i).getStReversal_condition() 
							+ " "+knockoff_Criteria.get(i).getStReversal_value();*/
					/*update_condition = update_condition +knockoff_Criteria.get(i).getStReversal_header()+" = ?";*/
					select_parameters = select_parameters + "OS1."+ knockoff_Criteria.get(i).getStReversal_header();
				}
			}
			else
			{
				Update_Headers.add(knockoff_Criteria.get(i));
				if(knockoff_Criteria.get(i).getStReversal_padding().equals("Y"))
				{
					condition = condition + " SUBSTR( OS1."+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
							+","+knockoff_Criteria.get(i).getStReversal_charsize()+")"
							+" "+knockoff_Criteria.get(i).getStReversal_condition() 
							+" SUBSTR( OS2."+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
							+","+knockoff_Criteria.get(i).getStReversal_charsize()+")";
					update_condition = update_condition +" SUBSTR("+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
							+","+knockoff_Criteria.get(i).getStReversal_charsize()+") = ?";
					select_parameters = select_parameters + " SUBSTR( OS1."+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
							+","+knockoff_Criteria.get(i).getStReversal_charsize()+") AS "+knockoff_Criteria.get(i).getStReversal_header();
				}
				else
				{
					condition = condition +"OS1."+ knockoff_Criteria.get(i).getStReversal_header()+" "+knockoff_Criteria.get(i).getStReversal_condition() +" OS2."+knockoff_Criteria.get(i).getStReversal_header();
					update_condition = update_condition +knockoff_Criteria.get(i).getStReversal_header()+" = ?";
					select_parameters = select_parameters + "OS1."+ knockoff_Criteria.get(i).getStReversal_header();
				}
			
				
			}
		}
		else
		{
			if(knockoff_Criteria.get(i).getStReversal_value()!=null)
			{
				if(knockoff_Criteria.get(i).getStReversal_padding().equals("Y"))
				{
					condition = condition + " SUBSTR( OS1."+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
							+","+knockoff_Criteria.get(i).getStReversal_charsize()+")"
							+" "+knockoff_Criteria.get(i).getStReversal_condition() + " "+knockoff_Criteria.get(i).getStReversal_value()+" AND ";
					/*update_condition = update_condition +" SUBSTR("+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
							+","+knockoff_Criteria.get(i).getStReversal_charsize()
							+") = ?"+" AND ";*/
					select_parameters = select_parameters + " SUBSTR( OS1."+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
					+","+knockoff_Criteria.get(i).getStReversal_charsize()+") AS "+knockoff_Criteria.get(i).getStReversal_header()+" , ";
				}
				else
				{
					condition = condition +"OS1."+ knockoff_Criteria.get(i).getStReversal_header()+" "+knockoff_Criteria.get(i).getStReversal_condition()
								+" "+knockoff_Criteria.get(i).getStReversal_value()+" AND ";
					/*update_condition = update_condition +knockoff_Criteria.get(i).getStReversal_header()+" = ?"+" AND ";*/
					select_parameters = select_parameters + "OS1."+ knockoff_Criteria.get(i).getStReversal_header()+" , ";
				}
			}
			else
			{
				Update_Headers.add(knockoff_Criteria.get(i));
				if(knockoff_Criteria.get(i).getStReversal_padding().equals("Y"))
				{
					condition = condition + " SUBSTR( OS1."+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
							+","+knockoff_Criteria.get(i).getStReversal_charsize()+")"
							+" "+knockoff_Criteria.get(i).getStReversal_condition() 
							+" SUBSTR( OS2."+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
							+","+knockoff_Criteria.get(i).getStReversal_charsize()+") AND ";
					update_condition = update_condition +" SUBSTR("+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
							+","+knockoff_Criteria.get(i).getStReversal_charsize()
							+") = ? AND ";
					select_parameters = select_parameters + " SUBSTR("+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
							+","+knockoff_Criteria.get(i).getStReversal_charsize()
							+") AS "+knockoff_Criteria.get(i).getStReversal_header()+" , ";
				}
				else
				{
					condition = condition +"OS1."+ knockoff_Criteria.get(i).getStReversal_header()+" "+knockoff_Criteria.get(i).getStReversal_condition()
							+" OS2."+knockoff_Criteria.get(i).getStReversal_header()+" AND ";
					update_condition = update_condition +knockoff_Criteria.get(i).getStReversal_header()+" = ? AND ";
					select_parameters = select_parameters + "OS1."+ knockoff_Criteria.get(i).getStReversal_header()+" , ";
				}
			
				
			}
			
		}
		
		logger.info("condition=="+condition);
		logger.info("update_condition=="+update_condition);
		logger.info("select_parameters=="+select_parameters);
		
		logger.info("***** GenerateRupayTTUMDaoImpl.getCondition End ****");
		
	}
	
	}catch(Exception e)
	{
		demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.getCondition");
		logger.error(" error in GenerateRupayTTUMDaoImpl.getCondition", new Exception("GenerateRupayTTUMDaoImpl.getCondition",e));
		 throw e;
	}
	

	
	return condition;
}


public List<GenerateTTUMBean> getCandDdifference(GenerateTTUMBean generateTTUMBeanObj)throws Exception
{
	logger.info("***** GenerateRupayTTUMDaoImpl.getCandDdifference Start ****");
	String condition = ""; 
	
	List<GenerateTTUMBean> ttum_data = new ArrayList<>();
	//List<GenerateTTUMBean> Excel_headers = new ArrayList<>();
	List<String> Headers = new ArrayList<>(); 
	String table_cols = "DCRS_REMARKS VARCHAR (100 BYTE), CREATEDDATE DATE , CREATEDBY VARCHAR (100 BYTE),RECORDS_DATE DATE";
	String insert_cols = "DCRS_REMARKS,CREATEDDATE, CREATEDBY,RECORDS_DATE";
	//String stUpdate_Condition = "";
	
	Connection conn = null;
	PreparedStatement pstmt1=null;
	PreparedStatement pstmt2=null;
	ResultSet rset1=null;
	ResultSet rset2=null;
	try
	{
		
		//Excel headers
		
		Headers.add("ACCOUNT NUMBER");
		Headers.add("CURRENCY CODE");
		Headers.add("SERVICE OUTLET");
		Headers.add("PART TRAN TYPE");
		Headers.add("TRANSACTION AMOUNT");
		Headers.add("TRANSACTION PARTICULARS");
		Headers.add("REFERENCE CURRENCY CODE");
		Headers.add("REFERENCE AMOUNT");
		Headers.add("REMARKS");
		Headers.add("REFERENCE NUMBER");
		Headers.add("ACCOUNT REPORT CODE");
		
		
	/*	generateTTUMBeanObj.setStExcelHeader(Headers);
		
		Excel_headers.add(generateTTUMBeanObj);*/
		
		


		
		
		int file_id = getJdbcTemplate().queryForObject(GET_FILE_ID, new Object[] { generateTTUMBeanObj.getStFile_Name() , generateTTUMBeanObj.getStCategory(),generateTTUMBeanObj.getStSubCategory() },Integer.class);
		int reversal_id = getJdbcTemplate().queryForObject(GET_REVERSAL_ID, new Object[] { (file_id), (generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory())},Integer.class);
		
		List<KnockOffBean> knockoff_Criteria = getJdbcTemplate().query(GET_KNOCKOFF_CRITERIA, new Object[] { reversal_id , file_id}, new KnockOffCriteriaMaster());
		
		
		for(int i = 0 ;i<knockoff_Criteria.size();i++)
		{
			if(i == (knockoff_Criteria.size()-1))
			{
				if(!knockoff_Criteria.get(i).getStReversal_header().equals("AMOUNT"))
				{
					if(knockoff_Criteria.get(i).getStReversal_value()!=null)
					{
						if(knockoff_Criteria.get(i).getStReversal_padding().equals("Y"))
						{
							condition = condition + " SUBSTR( OS1."+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
									+","+knockoff_Criteria.get(i).getStReversal_charsize()+")"
									+" "+knockoff_Criteria.get(i).getStReversal_condition() + " "+knockoff_Criteria.get(i).getStReversal_value();
							
						}
						else
						{
							condition = condition +"OS1."+ knockoff_Criteria.get(i).getStReversal_header()+" "+knockoff_Criteria.get(i).getStReversal_condition()
									+" "+knockoff_Criteria.get(i).getStReversal_value();
							
						}
					}
					else
					{
						if(knockoff_Criteria.get(i).getStReversal_padding().equals("Y"))
						{
							condition = condition + " SUBSTR( OS1."+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
									+","+knockoff_Criteria.get(i).getStReversal_charsize()+")"
									+" "+knockoff_Criteria.get(i).getStReversal_condition() 
									+" SUBSTR( OS2."+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
									+","+knockoff_Criteria.get(i).getStReversal_charsize()+")";
							
						}
						else
						{
							condition = condition +"OS1."+ knockoff_Criteria.get(i).getStReversal_header()+" "+knockoff_Criteria.get(i).getStReversal_condition() +" OS2."+knockoff_Criteria.get(i).getStReversal_header();
							
							
						}


					}
				}
			}
			else
			{
				if(!knockoff_Criteria.get(i).getStReversal_header().equals("AMOUNT"))
				{
					if(knockoff_Criteria.get(i).getStReversal_value()!=null)
					{
						if(knockoff_Criteria.get(i).getStReversal_padding().equals("Y"))
						{
							condition = condition + " SUBSTR( OS1."+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
									+","+knockoff_Criteria.get(i).getStReversal_charsize()+")"
									+" "+knockoff_Criteria.get(i).getStReversal_condition() + " "+knockoff_Criteria.get(i).getStReversal_value()+" AND ";
							
						}
						else
						{
							condition = condition +"OS1."+ knockoff_Criteria.get(i).getStReversal_header()+" "+knockoff_Criteria.get(i).getStReversal_condition()
									+" "+knockoff_Criteria.get(i).getStReversal_value()+" AND ";
						}
					}
					else
					{
						if(knockoff_Criteria.get(i).getStReversal_padding().equals("Y"))
						{
							condition = condition + " SUBSTR( OS1."+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
									+","+knockoff_Criteria.get(i).getStReversal_charsize()+")"
									+" "+knockoff_Criteria.get(i).getStReversal_condition() 
									+" SUBSTR( OS2."+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
									+","+knockoff_Criteria.get(i).getStReversal_charsize()+") AND ";
							
						}
						else
						{
							condition = condition +"OS1."+ knockoff_Criteria.get(i).getStReversal_header()+" "+knockoff_Criteria.get(i).getStReversal_condition()
									+" OS2."+knockoff_Criteria.get(i).getStReversal_header()+" AND ";
							
						}


					}
				}
				
			}
			
		}
		
		logger.info("condition=="+condition);
		
		//GET ALL COLS OF TABLE
		List<String> Columns1 = getJdbcTemplate().query(GET_COLS, new Object[]{"SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()}, new ColumnsMapper());
		String stcols = "";
		for(int i = 0 ; i<Columns1.size(); i++)
		{
			if(i == 0)
			{
				if(Columns1.get(i).equals("AMOUNT"))
				{
					stcols = "TO_NUMBER(" + Columns1.get(i) +",'9999999999.99') AS AMOUNT";
				}
				else
				{
					stcols =  Columns1.get(i);

				}
			}
			else
			{

				if(Columns1.get(i).equals("AMOUNT"))
				{
					stcols = stcols + ", TO_NUMBER(" + Columns1.get(i) +",'9999999999.99') AS AMOUNT ";
				}
				else
				{
					stcols = stcols + "," + Columns1.get(i);

				}
			
			}
		}
		
			
		// QUERY TO GET PART TRAN TYPE D RECORDS
		String GET_C_RECORDS  = "SELECT "+stcols+" FROM SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()+
				" OS1 WHERE OS1.E = 'C' AND OS1.DCRS_REMARKS = '"+generateTTUMBeanObj.getStMerger_Category()+"-UNRECON-1' " +
				" AND EXISTS ( SELECT * FROM SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()+
				" OS2 WHERE (OS2.E = 'D' AND "+condition+")AND OS2.DCRS_REMARKS = '"+generateTTUMBeanObj.getStMerger_Category()+"-UNRECON-1')" +
				" AND TO_CHAR(TO_DATE(VALUE_DATE,'DD-MM-YYYY'),'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"' AND "+
				"TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStFile_Date()+"'";
		
		// QUERY TO GET PART TRAN TYPE C RECORDS
		String GET_D_RECORDS  = "SELECT TO_NUMBER(AMOUNT,'9999999999.99') AS AMOUNT,TO_CHAR(TO_DATE(VALUE_DATE,'DD/MM/YYYY'),'DDMMYY') AS VALUE_DATE,SUBSTR(REF_NO,2,6) AS REF_NO  " +
				"FROM SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()+
				" OS1 WHERE OS1.E = 'D' AND OS1.DCRS_REMARKS = '"+generateTTUMBeanObj.getStMerger_Category()+"-UNRECON-1'" +
				" AND EXISTS ( SELECT * FROM SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()+
				" OS2 WHERE (OS2.E = 'C' AND "+condition+") AND OS2.DCRS_REMARKS = '"+generateTTUMBeanObj.getStMerger_Category()+"-UNRECON-1')"+
				" AND TO_CHAR(TO_DATE(VALUE_DATE,'DD-MM-YYYY'),'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"'"+
				" AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStFile_Date()+"'";
		
		logger.info("GET_C_RECORDS "+GET_C_RECORDS);
		conn = getConnection();
		pstmt1 = conn.prepareStatement(GET_C_RECORDS);
		rset1 = pstmt1.executeQuery();
		
		logger.info("GET_D_RECORDS "+GET_D_RECORDS);
		pstmt2 = conn.prepareStatement(GET_D_RECORDS);
		rset2 = pstmt2.executeQuery();
		
		
		while(rset1.next() && rset2.next())
		{
			Float amount_1 = Float.parseFloat(rset1.getString("AMOUNT"));
			Float amount_2 = Float.parseFloat(rset2.getString("AMOUNT"));
			
			Float amount_diff = amount_2 - amount_1;
			
			logger.info("amount difference is "+amount_diff);
			
			if(amount_diff < 0)
			{
				// IF DIFF IS -VE
				GenerateTTUMBean generateTTUMBeanObj1 = new GenerateTTUMBean();
				//CUST ACC DR
				generateTTUMBeanObj1.setStDebitAcc(rset1.getString("CONTRA_ACCOUNT"));
				//RUPAY GL CR
				generateTTUMBeanObj1.setStCreditAcc(rset1.getString("FORACID"));
				generateTTUMBeanObj1.setStAmount(amount_diff+"");
				/*String tran_particular = "REV/"+generateTTUMBeanObj.getStCategory()+"/"+rset2.getString("VALUE_DATE")+"/"+
								rset2.getString("REF_NO");*/
				
				String stTran_particulars = "ADR-RPAY-"+rset1.getString("VALUE_DATE")+"-"+rset2.getString("REF_NO")+"-"+rset1.getString("PARTICULARALS");
				generateTTUMBeanObj1.setStDate(rset1.getString("VALUE_DATE"));
				
				generateTTUMBeanObj1.setStTran_particulars(stTran_particulars);
				generateTTUMBeanObj1.setStCard_Number(rset1.getString("REMARKS"));
				String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
				generateTTUMBeanObj1.setStRemark(remark);
				
				ttum_data.add(generateTTUMBeanObj1);
											
			}
			else
			{
				// IF DIFF IS +VE
				GenerateTTUMBean generateTTUMBeanObj1 = new GenerateTTUMBean();
				//RUPAY GL DR
				generateTTUMBeanObj1.setStDebitAcc(rset1.getString("FORACID"));
				//CUSTOMER ACC CR
				generateTTUMBeanObj1.setStCreditAcc(rset1.getString("CONTRA_ACCOUNT"));
				generateTTUMBeanObj1.setStAmount(amount_diff+"");
				/*String tran_particular = "REV/"+generateTTUMBeanObj.getStCategory()+"/"+rset2.getString("VALUE_DATE")+"/"+
								rset2.getString("REF_NO");*/
				
				//REV-RPAY-DDMMYY (Transaction date)-Trace number-Merchant name
				
				String stTran_particulars = "REV-RPAY-"+rset1.getString("VALUE_DATE")+"-"+rset2.getString("REF_NO")+"-"+rset1.getString("PARTICULARALS");
				generateTTUMBeanObj1.setStDate(rset1.getString("VALUE_DATE"));
				
				generateTTUMBeanObj1.setStTran_particulars(stTran_particulars);
				generateTTUMBeanObj1.setStCard_Number(rset1.getString("REMARKS"));
				String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
				generateTTUMBeanObj1.setStRemark(remark);
				
				ttum_data.add(generateTTUMBeanObj1);
				
			
			}
			//updation of these records
			String update_condition = "";
			
			for(int i = 0; i<knockoff_Criteria.size() ; i++)
			{
				if(!knockoff_Criteria.get(i).getStReversal_header().equals("AMOUNT"))
				{
					if(i == (knockoff_Criteria.size()-1))
					{
						if(knockoff_Criteria.get(i).getStReversal_padding().equals("Y"))
						{
							update_condition = update_condition + " SUBSTR( "+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
									+","+knockoff_Criteria.get(i).getStReversal_charsize()+")"
									+" "+knockoff_Criteria.get(i).getStReversal_condition() 
									+" SUBSTR( '"+rset1.getString(knockoff_Criteria.get(i).getStReversal_header())+"',"+knockoff_Criteria.get(i).getStReversal_charpos()
									+","+knockoff_Criteria.get(i).getStReversal_charsize()+")";

						}
						else
						{
							update_condition = update_condition + knockoff_Criteria.get(i).getStReversal_header()
									+" "+knockoff_Criteria.get(i).getStReversal_condition() +" '"+rset1.getString(knockoff_Criteria.get(i).getStReversal_header())
									+"'";

						}
					}
					else
					{
						if(knockoff_Criteria.get(i).getStReversal_padding().equals("Y"))
						{
							update_condition = update_condition + " SUBSTR( "+knockoff_Criteria.get(i).getStReversal_header()+","+knockoff_Criteria.get(i).getStReversal_charpos()
									+","+knockoff_Criteria.get(i).getStReversal_charsize()+")"
									+" "+knockoff_Criteria.get(i).getStReversal_condition() 
									+" SUBSTR( '"+rset1.getString(knockoff_Criteria.get(i).getStReversal_header())+"',"+knockoff_Criteria.get(i).getStReversal_charpos()
									+","+knockoff_Criteria.get(i).getStReversal_charsize()+") AND ";

						}
						else
						{
							update_condition = update_condition + knockoff_Criteria.get(i).getStReversal_header()+" "+knockoff_Criteria.get(i).getStReversal_condition()
									+" '"+rset1.getString(knockoff_Criteria.get(i).getStReversal_header())+"' AND ";

						}

					}
				}
				
			}
		
			logger.info("UDPATE CONDITION IS "+update_condition);
			
			
			String UPDATE_RECORDS = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()
						+" SET DCRS_REMARKS = '"+generateTTUMBeanObj.getStMerger_Category()
						+"-UNRECON-GENERATED-TTUM-"+generateTTUMBeanObj.getInRec_Set_Id()+"' WHERE "+update_condition;
			
			logger.info("UPDATE CONDITION IS "+UPDATE_RECORDS);
			getJdbcTemplate().execute(UPDATE_RECORDS);
		
			
			
		}
		
		//data.add(Excel_headers);
		//data.add(ttum_data);
		

		// CREATE NEW TABLE FOR INSERTING TTUM ENTRIES
		for(int i = 0 ; i<Headers.size();i++)
		{
			table_cols =table_cols+","+ Headers.get(i)+" VARCHAR (100 BYTE)";
			insert_cols = insert_cols+","+Headers.get(i);
		}
		
		String CHECK_TABLE = "SELECT count (*) FROM tab WHERE tname  = 'TTUM_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
				+"_"+generateTTUMBeanObj.getStFile_Name()+"'";
		int tableExist = getJdbcTemplate().queryForObject(CHECK_TABLE, new Object[] { },Integer.class);
		
		logger.info("CHECK_TABLE=="+CHECK_TABLE);
		logger.info("tableExist=="+tableExist);
		if(tableExist == 0)
		{
			String CREATE_QUERY = "CREATE TABLE TTUM_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
					+"_"+generateTTUMBeanObj.getStFile_Name()+" ("+table_cols+")";
			
			logger.info("CREATE_QUERY=="+CREATE_QUERY);
			getJdbcTemplate().execute(CREATE_QUERY);
		}
		
		//INSERT TTUM ENTRIES IN ABOVE CREATED TABLE
		for(int i = 0;i<ttum_data.size();i++)
		{
			GenerateTTUMBean beanObj = new GenerateTTUMBean();
			beanObj = ttum_data.get(i);
			String INSERT_QUERY = "INSERT INTO TTUM_"+generateTTUMBeanObj.getStMerger_Category()
					+"_"+generateTTUMBeanObj.getStFile_Name() 
						+"("+insert_cols+") VALUES ('"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
						+"UNRECON-TTUM-"+generateTTUMBeanObj.getInRec_Set_Id()+
						"',SYSDATE,'"+generateTTUMBeanObj.getStEntry_by()+"',TO_DATE('"+beanObj.getStDate()+"','DDMMYY'),'"+beanObj.getStDebitAcc()+"','INR','999','D','"+
						beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','"+beanObj.getStCard_Number()+"','INR','"+beanObj.getStAmount()+"','"+
						beanObj.getStRemark()+"')";
			
			logger.info("INSERT_QUERY=="+INSERT_QUERY);
			getJdbcTemplate().execute(INSERT_QUERY);
			
			 INSERT_QUERY = "INSERT INTO TTUM_"+generateTTUMBeanObj.getStMerger_Category()
					 	+"_"+generateTTUMBeanObj.getStFile_Name() 
						+"("+insert_cols+") VALUES (''," +generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
						+"UNRECON-TTUM-"+generateTTUMBeanObj.getInRec_Set_Id()+
						"',SYSDATE,'"+generateTTUMBeanObj.getStEntry_by()+"',TO_DATE('"+beanObj.getStDate()+"','DDMMYY'),'"+beanObj.getStCreditAcc()+"','INR','999','C','"+
						beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','"+beanObj.getStCard_Number()+"','INR','"+beanObj.getStAmount()+"','"+
						beanObj.getStRemark()+"')";
			 
			 logger.info("INSERT_QUERY=="+INSERT_QUERY);
			 getJdbcTemplate().execute(INSERT_QUERY);		
		}
		
		logger.info("***** GenerateRupayTTUMDaoImpl.getCandDdifference End ****");
		
	}
	catch(Exception e)
	{
		demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.getCandDdifference");
		logger.error(" error in GenerateRupayTTUMDaoImpl.getCandDdifference", new Exception("GenerateRupayTTUMDaoImpl.getCandDdifference",e));
		 throw e;
	}
	finally{
		if(rset2!=null){
			rset2.close();
		}
		if(pstmt2!=null){
			pstmt2.close();
		}
		if(rset1!=null){
			rset1.close();
		}
		if(pstmt1!=null){
			pstmt1.close();
		}
		if(conn!=null){
			conn.close();
		}
	}
	return ttum_data;
}

private static class ColumnsMapper implements RowMapper<String> {

	@Override
	public String mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		String stColumns = rs.getString("COLUMN_NAME");
		
		return stColumns;
		
	}
}

@Override
public void getReportCRecords(GenerateTTUMBean generateTTUMBeanObj)throws Exception
{
	logger.info("***** GenerateRupayTTUMDaoImpl.getReportCRecords Start ****");
	
	String GET_TTUM_RECORDS = "";
	String table_cols = " CREATEDDATE DATE , CREATEDBY VARCHAR (100 BYTE)";
	String insert_cols = "CREATEDDATE, CREATEDBY";
	try
	{
		if(generateTTUMBeanObj.getStDate() != null && !generateTTUMBeanObj.getStDate().equals(""))
		{
			/*GET_TTUM_RECORDS = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()
					+" SET DCRS_REMARKS = '"+generateTTUMBeanObj.getStMerger_Category()
					+"-UNRECON-GENERATE-TTUM-"+generateTTUMBeanObj.getInRec_Set_Id()
					+"' WHERE DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
					+"-UNRECON-2' AND TO_CHAR(TO_DATE(LOCAL_DATE,'MM/DD/YYYY'),'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"'" 
					+" AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStFile_Date()+"'";*/
			GET_TTUM_RECORDS = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()
			+" SET DCRS_REMARKS = '"+generateTTUMBeanObj.getStMerger_Category()
			+"-UNRECON-GENERATE-TTUM-"+generateTTUMBeanObj.getInRec_Set_Id()
			+"' WHERE DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
			+"-UNRECON-2' AND TO_CHAR(TO_DATE(LOCAL_DATE,'MM/DD/YYYY'),'DD/MM/YYYY') between '"+generateTTUMBeanObj.getStStart_Date()+"' AND '"+generateTTUMBeanObj.getStEnd_Date()+"'" 
			+" AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"'";
		}
		else
		{
			GET_TTUM_RECORDS = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()
					+" SET DCRS_REMARKS = '"+generateTTUMBeanObj.getStMerger_Category()
					+"-UNRECON-GENERATE-TTUM-"+generateTTUMBeanObj.getInRec_Set_Id()
					+"' WHERE DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
					+"-UNRECON-2' AND TO_CHAR(TO_DATE(LOCAL_DATE,'MM/DD/YYYY'),'DD/MM/YYYY') = TO_CHAR(TO_DATE('"+generateTTUMBeanObj.getStFile_Date()+"'-10,'DD/MM/YYY'),'DD/MM/YYYY')"
					+" AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStFile_Date()+"'";
			
		}
		logger.info("GET_TTUM_RECORDS IN getReportCRecords "+GET_TTUM_RECORDS);
		getJdbcTemplate().execute(GET_TTUM_RECORDS);
		
		logger.info("***** GenerateRupayTTUMDaoImpl.getReportCRecords End ****");
	}
	catch(Exception e)
	{
		demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.getReportCRecords");
		logger.error(" error in GenerateRupayTTUMDaoImpl.getReportCRecords", new Exception("GenerateRupayTTUMDaoImpl.getReportCRecords",e));
		 throw e;
	}
}


public void getRupayTTUMRecords(GenerateTTUMBean generateTTUMBeanObj)throws Exception
{
	logger.info("***** GenerateRupayTTUMDaoImpl.getRupayTTUMRecords Start ****");
	String UPDATE_QUERY = "";
	try
	{
		String strDate=generateTTUMBeanObj.getStDate();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
		java.util.Date varDate=null;
		try {	
			varDate=dateFormat.parse(strDate);
		    dateFormat=new SimpleDateFormat("yymmdd");
		    logger.info("Date :"+dateFormat.format(varDate));
		}catch (Exception e) {
		    // TODO: handle exception
		    e.printStackTrace();
		}
		if(generateTTUMBeanObj.getStDate() != null && !generateTTUMBeanObj.getStDate().equals(""))
		{
			//TTUM FOR SPECIFIED DATE
			UPDATE_QUERY = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()
					+" SET DCRS_REMARKS = '"+generateTTUMBeanObj.getStMerger_Category()
					+"-UNRECON-GENERATE-TTUM-2' WHERE DCRS_REMARKS = '"+generateTTUMBeanObj.getStMerger_Category()
					+"-UNRECON-2' AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"' AND " ;
					//"(TXNFUNCTION_CODE = '200' OR TXNFUNCTION_CODE = '262') " ; 
			//Changes by minakshi 09Sept18
			if(generateTTUMBeanObj.getStMerger_Category().equals("RUPAY_DOM")){
				UPDATE_QUERY = UPDATE_QUERY + " (TXNFUNCTION_CODE = '200' OR TXNFUNCTION_CODE = '262') ";
			}else{
				UPDATE_QUERY = UPDATE_QUERY + " (TXNFUNCTION_CODE = '200' OR TXNFUNCTION_CODE = '263') ";
			}
			//Changes by minakshi 06July18
			if(generateTTUMBeanObj.getStFunctionCode().equals("200")){
				UPDATE_QUERY = UPDATE_QUERY + " and DATE_SETTLEMENT = "+dateFormat.format(varDate);
			}
					//changes done on 07th jan 2018 as per sameer's new requirement for TTUM
					//+" AND TO_CHAR(TO_DATE(SUBSTR(DATE_SETTLEMENT,1,6),'YYMMDD'),'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"'";
		}
		else
		{
			//GENERATE TTUM FOR LAST PROCESSED FILE
			UPDATE_QUERY = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()
					+" SET DCRS_REMARKS = '"+generateTTUMBeanObj.getStMerger_Category()
					+"-UNRECON-GENERATE-TTUM-2' WHERE DCRS_REMARKS = '"+generateTTUMBeanObj.getStMerger_Category()
					+"-UNRECON-2' AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStFile_Date()+"' AND " ;
				//	"(TXNFUNCTION_CODE = '200' OR TXNFUNCTION_CODE = '262') ";
					//+" AND TO_CHAR(TO_DATE(SUBSTR(DATE_SETTLEMENT,1,6),'YYMMDD'),'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStFile_Date()+"'";
			
			//Changes by minakshi 09Sept18
			if(generateTTUMBeanObj.getStMerger_Category().equals("RUPAY_DOM")){
				UPDATE_QUERY = UPDATE_QUERY + " (TXNFUNCTION_CODE = '200' OR TXNFUNCTION_CODE = '262') ";
			}else{
				UPDATE_QUERY = UPDATE_QUERY + " (TXNFUNCTION_CODE = '200' OR TXNFUNCTION_CODE = '263') ";
			}
			//Changes by minakshi 06July18
			if(generateTTUMBeanObj.getStFunctionCode().equals("200")){
				UPDATE_QUERY = UPDATE_QUERY + " and DATE_SETTLEMENT = "+varDate;
			}
		
		}
		
		logger.info("UPDATE_QUERY=="+UPDATE_QUERY);
		getJdbcTemplate().execute(UPDATE_QUERY);			
		
		logger.info("***** GenerateRupayTTUMDaoImpl.getRupayTTUMRecords End ****");
		
	}
	catch(Exception e)
	{
		demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.getRupayTTUMRecords");
		logger.error(" error in GenerateRupayTTUMDaoImpl.getRupayTTUMRecords", new Exception("GenerateRupayTTUMDaoImpl.getRupayTTUMRecords",e));
		 throw e;
	}
}

public List<List<GenerateTTUMBean>> GenerateRupayTTUM(GenerateTTUMBean generateTTUMBean,int inRec_Set_Id)throws Exception
{
	
	
	logger.info("***** GenerateRupayTTUMDaoImpl.GenerateRupayTTUM Start ****");

List<GenerateTTUMBean> Excel_Header = new ArrayList<>();
List<GenerateTTUMBean> TTUM_data = new ArrayList<>();
List<GenerateTTUMBean> TTUM_C_Data = new ArrayList<>();
List<GenerateTTUMBean> TTUM_D_Data = new ArrayList<>();
List<GenerateTTUMBean> TTUM_D_Data1 = new ArrayList<>();
List<List<GenerateTTUMBean>>Data = new ArrayList<>();
List<String> ExcelHeaders = new ArrayList<>();
List<String> ExcelHeaders1 = new ArrayList<>();
String table_cols = "DCRS_REMARKS VARCHAR (100 BYTE), CREATEDDATE DATE , CREATEDBY VARCHAR (100 BYTE)";
String insert_cols = "DCRS_REMARKS, CREATEDDATE, CREATEDBY";
String GET_ACC_NUMBS =null;

PreparedStatement pstmt_200 = null;
ResultSet rset_200 = null;
PreparedStatement pstmt_con_200=null;
ResultSet rset_con_200 = null;
PreparedStatement pstmt_262 = null;
ResultSet rset_262 = null;
PreparedStatement pstmt_con_262 = null;
ResultSet rset_con_262 = null;
Connection conn=null;

try
{
	ExcelHeaders.add("ACCOUNT NUMBER");
	ExcelHeaders.add("CURRENCY CODE");
	ExcelHeaders.add("SERVICE OUTLET");
	ExcelHeaders.add("PART TRAN TYPE");
	ExcelHeaders.add("TRANSACTION AMOUNT");
	ExcelHeaders.add("TRANSACTION PARTICULARS");
	ExcelHeaders.add("REFERENCE CURRENCY CODE");
	ExcelHeaders.add("REFERENCE AMOUNT");
	ExcelHeaders.add("REMARKS");
	ExcelHeaders.add("REFERENCE NUMBER");
	ExcelHeaders.add("ACCOUNT REPORT CODE");
	
	ExcelHeaders1.add("ACCOUNT_NUMBER");
	ExcelHeaders1.add("CURRENCY_CODE");
	ExcelHeaders1.add("SERVICE_OUTLET");
	ExcelHeaders1.add("PAR_TRAN_TYPE");
	ExcelHeaders1.add("TRANSACTION_AMOUNT");
	ExcelHeaders1.add("TRANSACTION_PARTICULARS");
	ExcelHeaders1.add("REFERENCE_CURRENCY_CODE");
	ExcelHeaders1.add("REFERENCE_AMOUNT");
	ExcelHeaders1.add("REMARKS");
	ExcelHeaders1.add("REFERENCE_NUMBER");
	
	generateTTUMBean.setStExcelHeader(ExcelHeaders);
	
	Excel_Header.add(generateTTUMBean);
	conn = getConnection();
	
	java.util.Date varDate=null;
	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	try {
	     varDate=dateFormat.parse(generateTTUMBean.getStFile_Date());
	    dateFormat=new SimpleDateFormat("ddMMyy");
	    logger.info("Date :"+dateFormat.format(varDate));
	}catch (Exception e) {
		demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.GenerateRupayTTUM");
		logger.error(" error in GenerateRupayTTUMDaoImpl.GenerateRupayTTUM", new Exception("GenerateRupayTTUMDaoImpl.GenerateRupayTTUM",e));
		 throw e;
	}
	
	//Changes by minakshi 06July18
			String strDate=generateTTUMBean.getStDate();
			SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/mm/yyyy");
			java.util.Date varDate1=null;
			try {	
				varDate1=dateFormat1.parse(strDate);
			    dateFormat1=new SimpleDateFormat("yymmdd");
			    logger.info("Date :"+dateFormat1.format(varDate1));
			}catch (Exception e) {
			    // TODO: handle exception
			    e.printStackTrace();
			}
			
	
	if(generateTTUMBean.getStSubCategory().equals("DOMESTIC"))
	{
		logger.info("*** In DOMESTIC ***");
		 String GETDATA = "SELECT TABLENAME FROM MAIN_FILESOURCE WHERE FILENAME = 'CBS' AND FILE_CATEGORY = 'RUPAY' AND FILE_SUBCATEGORY = 'DOMESTIC'";
		 String rawtablename = getJdbcTemplate().queryForObject(GETDATA, new Object[]{},String.class);
		 
		 logger.info("GETDATA=="+GETDATA);
		 logger.info("rawtablename=="+rawtablename);
		 
		String GET_TTUM_RECORDS_200 = "SELECT substr(DATEANDTIME_LOCAL_TRANSACTION,1,6) as tran_date,TO_CHAR(FILEDATE,'DDMMYY') AS RECON_DATE,TO_CHAR(FILEDATE,'DD-MM-YY') AS RECON_DATE1,UNIQUE_FILE_NAME,TO_CHAR(TO_DATE(SUBSTR(DATEANDTIME_LOCAL_TRANSACTION,1,6),'YY/MM/DD'),'DDMMYY') AS DATEANDTIME_LOCAL_TRANSACTION," +
				"AMOUNT_TRANSACTION,PRIMARY_ACCOUNT_NUMBER,APPROVAL_CODE,SUBSTR(ACQUIRER_REFERENCE_DATA,11,12) AS ACQUIRER_REFERENCE_DATA" +
				" FROM SETTLEMENT_"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStFile_Name()+
				" WHERE DCRS_REMARKS LIKE '%"+generateTTUMBean.getStMerger_Category()+"-UNRECON-GENERATE-TTUM%' AND TXNFUNCTION_CODE = '200' and DATE_SETTLEMENT = '"+dateFormat1.format(varDate1)+"'"+ //Changes by minakshi 06July18
				" AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBean.getStDate()+"'";

		logger.info("GET_TTUM_RECORDS_200=="+GET_TTUM_RECORDS_200);
	
		String GET_TTUM_RECORDS_262 = "SELECT substr(DATEANDTIME_LOCAL_TRANSACTION,1,6) as tran_date, TO_CHAR(FILEDATE,'DD-MM-YY') AS RECON_DATE,UNIQUE_FILE_NAME,TO_CHAR(TO_DATE(SUBSTR(DATEANDTIME_LOCAL_TRANSACTION,1,6),'YY/MM/DD'),'DDMMYY') AS DATEANDTIME_LOCAL_TRANSACTION," +
				"AMOUNT_TRANSACTION,PRIMARY_ACCOUNT_NUMBER,APPROVAL_CODE,SUBSTR(ACQUIRER_REFERENCE_DATA,11,12) AS ACQUIRER_REFERENCE_DATA"+//,SUM(AMOUNT_TRANSACTION) AS DR_AMT" +
				" FROM SETTLEMENT_"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStFile_Name()+
							" WHERE DCRS_REMARKS LIKE '%GENERATE-TTUM%' AND TXNFUNCTION_CODE = '262' AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBean.getStDate()+"'";
							/*" GROUP BY FILEDATE,UNIQUE_FILE_NAME,DATEANDTIME_LOCAL_TRANSACTION,AMOUNT_TRANSACTION,PRIMARY_ACCOUNT_NUMBER,APPROVAL_CODE," +
							"ACQUIRER_REFERENCE_DATA,PRIMARY_ACCOUNT_NUMBER,APPROVAL_CODE, ACQUIRER_REFERENCE_DATA";*/

		logger.info("GET_TTUM_RECORDS_262=="+GET_TTUM_RECORDS_262);
		
		//GET ACC NUMBERS
		if(generateTTUMBean.getStFunctionCode().equals("200"))
		{
		  GET_ACC_NUMBS = "SELECT DISTINCT T2.CONTRA_ACCOUNT,T1.PRIMARY_ACCOUNT_NUMBER FROM SETTLEMENT_RUPAY_RUPAY T1,CBS_RUPAY_RAWDATA T2"+ 
				 		" WHERE T1.DCRS_REMARKS LIKE '%RUPAY_DOM-UNRECON-GENERATE-TTUM%' AND  T1.TXNFUNCTION_CODE = '200' "+ 
				 		" AND TO_CHAR(T1.FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBean.getStDate()+"' AND T1.PRIMARY_ACCOUNT_NUMBER = T2.REMARKS and t2.CONTRA_ACCOUNT is not null";
		
		  logger.info("GET_ACC_NUMBS=="+GET_ACC_NUMBS);
		}
		else if(generateTTUMBean.getStFunctionCode().equals("262"))
		{
			 GET_ACC_NUMBS = "SELECT DISTINCT T2.CONTRA_ACCOUNT,T1.PRIMARY_ACCOUNT_NUMBER FROM SETTLEMENT_RUPAY_RUPAY T1,CBS_RUPAY_RAWDATA T2"+ 
			 		" WHERE T1.DCRS_REMARKS LIKE '%RUPAY_DOM-UNRECON-GENERATE-TTUM%' AND T1.TXNFUNCTION_CODE = '262' "+ 
			 		" AND TO_CHAR(T1.FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBean.getStDate()+"' AND T1.PRIMARY_ACCOUNT_NUMBER = T2.REMARKS and t2.CONTRA_ACCOUNT is not null";
		
			 logger.info("GET_ACC_NUMBS=="+GET_ACC_NUMBS);
		}
		 Map<String, String> ACCNUMS = (Map<String, String>) getJdbcTemplate().query(GET_ACC_NUMBS, new Object[] {},new ResultSetExtractor() {
			 public Object extractData(ResultSet rs) throws SQLException {
				 Map map = new HashMap();
				 while (rs.next()) {
				 String col1 = rs.getString("PRIMARY_ACCOUNT_NUMBER");
				 String col2 = rs.getString("CONTRA_ACCOUNT");
				/* if(col2.contains("78000010021"))
				 {
					 String get_man_acc="select t1.MAN_CONTRA_ACCOUNT from  SETTLEMENT_RUPAY_SWITCH t1 where t1.pan='"+col1+"' " +
					 		"and t1.CONTRA_ACCOUNT='"+col2+"' and TO_CHAR(T1.FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBean.getStDate()+"'";
				 map.put(col1, col2);
				 }else{*/
					 map.put(col1, col2);
				 }
				 //}
				 return map;
				 };
				 });			 
		 
		if(generateTTUMBean.getStFunctionCode().equals("200"))
		{
			pstmt_200 = conn.prepareStatement(GET_TTUM_RECORDS_200);
			rset_200 = pstmt_200.executeQuery();
			
			Double total_amount = 0.00;
			String sttotal_amount = "";
			String stTran_particulars= "";
			String FileName = "";
           String man_acco="";
           int loop_count1=0;
			while(rset_200.next())
			{

				GenerateTTUMBean generateTTUMBeanObj = new GenerateTTUMBean();

				//generateTTUMBeanObj.setStCreditAcc("99937200010660");

				generateTTUMBeanObj.setStAmount(rset_200.getString("AMOUNT_TRANSACTION"));
				total_amount+=Double.valueOf(rset_200.getString("AMOUNT_TRANSACTION"));
				
				
				String card_num = rset_200.getString("PRIMARY_ACCOUNT_NUMBER");
				//GETDATA = "SELECT DISTINCT CONTRA_ACCOUNT FROM "+rawtablename+" WHERE REMARKS = '"+card_num+"'";
				String stAccNum = "";
				stAccNum = ACCNUMS.get(card_num);
				
				//Changes by minakshi 06July18
				if(stAccNum == null || stAccNum.equals("")){
					generateTTUMBeanObj.setStDebitAcc("ACCOUNT NUMBER NOT AVAILABLE");
					
				//generateTTUMBeanObj.setStDebitAcc(stAccNum);				
				//stTran_particulars = "LP-RPAY"+"-"+rset_200.getString("RECON_DATE")+"-"+rset_200.getString("APPROVAL_CODE");
				stTran_particulars = "LP-RPAY"+"-"+rset_200.getString("TRAN_DATE")+"-"+rset_200.getString("APPROVAL_CODE");
				//generateTTUMBeanObj.setStDate(rset_200.getString("DATEANDTIME_LOCAL_TRANSACTION"));
				generateTTUMBeanObj.setStDate(generateTTUMBean.getStDate());
				//+"/"+rset.getString("TRACE");//WHICH FIELD TO BE TAKEN IN CASE OF RUPAY?
				generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
				//generateTTUMBeanObj.setStCard_Number(rset_200.getString("PRIMARY_ACCOUNT_NUMBER"));
				generateTTUMBeanObj.setStRemark(rset_200.getString("PRIMARY_ACCOUNT_NUMBER"));
				//generateTTUMBeanObj.setStCard_Number(rset_200.getString("ACQUIRER_REFERENCE_DATA"));
				String remark = getJdbcTemplate().queryForObject("select 'RPYD'||'"+dateFormat.format(varDate)+"'||ttum_seq.nextval from dual", new Object[] {},String.class);
			    generateTTUMBeanObj.setStCard_Number(remark);
				//TTUM_data.add(generateTTUMBeanObj);
				FileName = rset_200.getString("UNIQUE_FILE_NAME");
				TTUM_D_Data.add(generateTTUMBeanObj);
				TTUM_D_Data1.add(generateTTUMBeanObj);
				stTran_particulars = "LP RPAY "+rset_200.getString("RECON_DATE1");//+" "+rset_200.getString("APPROVAL_CODE");
				}
				else if(stAccNum.contains("78000010021"))
				{
					//logger.info("Contrac acco-->>"+stAccNum);
					//logger.info("Card no-->>"+card_num);
					String get_man_acc_200="select distinct t1.CONTRA_ACCOUNT,t2.ACCTNUM,t1.FILEDATE,t1.TRAN_DATE,t1.AMOUNT,t2.AMOUNT_EQUIV,substr(t1.REF_NO,2,6),substr(t2.TRACE,2,6) from cbs_rupay_rawdata t1 inner join "
						      + "switch_rawdata t2 on t1.REMARKS = t2.PAN and t1.FILEDATE = t2.FILEDATE and trunc(TO_NUMBER(REPLACE(T1.AMOUNT,',',''))) = trunc(TO_NUMBER(t2.AMOUNT_EQUIV)) and substr(t1.REF_NO,2,6) "
						      + "=substr(t2.TRACE,2,6)  and t2.ACCTNUM is not null "
						      + "where t1.CONTRA_ACCOUNT = '"+stAccNum+"' and t2.PAN = '"+card_num+"' order by t1.FILEDATE";
													
													pstmt_con_200 = conn.prepareStatement(get_man_acc_200);
													rset_con_262 = pstmt_con_200.executeQuery();
													
														
													while(rset_con_262.next())
													{
														if(loop_count1==0)
														{
														//stAccNum=rset_con_262.getString(rset_con_262.getString("ACCTNUM").replaceAll(" ", "").replaceAll("^0*",""));
														//String split_dt[]=stAccNum.split("\\s+");
														generateTTUMBeanObj.setStDebitAcc(rset_con_262.getString("ACCTNUM").replaceAll(" ", " ").replaceAll("^0*",""));
														loop_count1++;
														}
													}
					stTran_particulars = "LP-RPAY"+"-"+rset_200.getString("TRAN_DATE")+"-"+rset_200.getString("APPROVAL_CODE");
					//generateTTUMBeanObj.setStDate(rset_200.getString("DATEANDTIME_LOCAL_TRANSACTION"));
					generateTTUMBeanObj.setStDate(generateTTUMBean.getStDate());
					//+"/"+rset.getString("TRACE");//WHICH FIELD TO BE TAKEN IN CASE OF RUPAY?
					generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
					//generateTTUMBeanObj.setStCard_Number(rset_200.getString("PRIMARY_ACCOUNT_NUMBER"));
					generateTTUMBeanObj.setStRemark(rset_200.getString("PRIMARY_ACCOUNT_NUMBER"));
					//generateTTUMBeanObj.setStCard_Number(rset_200.getString("ACQUIRER_REFERENCE_DATA"));
					String remark = getJdbcTemplate().queryForObject("select 'RPYD'||'"+dateFormat.format(varDate)+"'||ttum_seq.nextval from dual", new Object[] {},String.class);
					generateTTUMBeanObj.setStCard_Number(remark);
					/*String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
						generateTTUMBeanObj.setStRemark(remark);*/
					//TTUM_data.add(generateTTUMBeanObj);
					FileName = rset_200.getString("UNIQUE_FILE_NAME");
					TTUM_D_Data.add(generateTTUMBeanObj);
					TTUM_D_Data1.add(generateTTUMBeanObj);
					stTran_particulars = "LP RPAY "+rset_200.getString("RECON_DATE1");//+" "+rset_200.getString("APPROVAL_CODE");
				}
				else
				{
					generateTTUMBeanObj.setStDebitAcc(stAccNum);				
				//stTran_particulars = "LP-RPAY"+"-"+rset_200.getString("RECON_DATE")+"-"+rset_200.getString("APPROVAL_CODE");
				stTran_particulars = "LP-RPAY"+"-"+rset_200.getString("TRAN_DATE")+"-"+rset_200.getString("APPROVAL_CODE");
				//generateTTUMBeanObj.setStDate(rset_200.getString("DATEANDTIME_LOCAL_TRANSACTION"));
				generateTTUMBeanObj.setStDate(generateTTUMBean.getStDate());
				//+"/"+rset.getString("TRACE");//WHICH FIELD TO BE TAKEN IN CASE OF RUPAY?
				generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
				//generateTTUMBeanObj.setStCard_Number(rset_200.getString("PRIMARY_ACCOUNT_NUMBER"));
				generateTTUMBeanObj.setStRemark(rset_200.getString("PRIMARY_ACCOUNT_NUMBER"));
				//generateTTUMBeanObj.setStCard_Number(rset_200.getString("ACQUIRER_REFERENCE_DATA"));
				String remark = getJdbcTemplate().queryForObject("select 'RPYD'||'"+dateFormat.format(varDate)+"'||ttum_seq.nextval from dual", new Object[] {},String.class);
				generateTTUMBeanObj.setStCard_Number(remark);
				/*String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
					generateTTUMBeanObj.setStRemark(remark);*/
				//TTUM_data.add(generateTTUMBeanObj);
				FileName = rset_200.getString("UNIQUE_FILE_NAME");
				TTUM_D_Data.add(generateTTUMBeanObj);
				stTran_particulars = "LP RPAY "+rset_200.getString("RECON_DATE1");//+" "+rset_200.getString("APPROVAL_CODE");

			}}
			//CHANGES MADE BY INT5779 FOR SINGLE CREDIT ENTRY
			if(TTUM_D_Data.size()>0)
			{
				GenerateTTUMBean generateTTUMBeanObj = new GenerateTTUMBean();

				generateTTUMBeanObj.setStCreditAcc("99937200010660");

				//generateTTUMBeanObj.setStAmount(rset_200.getString("AMOUNT_TRANSACTION"));
				sttotal_amount = String.valueOf(Double.parseDouble(new DecimalFormat("##.####").format(total_amount))); 
				generateTTUMBeanObj.setStAmount(sttotal_amount);
				//String stTran_particulars = "LP RPAY "+rset_200.getString("RECON_DATE");//+" "+rset_200.getString("APPROVAL_CODE");
				generateTTUMBeanObj.setStDate(generateTTUMBean.getStDate());
				//+"/"+rset.getString("TRACE");//WHICH FIELD TO BE TAKEN IN CASE OF RUPAY?
				generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
				/*generateTTUMBeanObj.setStRemark(rset_200.getString("PRIMARY_ACCOUNT_NUMBER"));*/
				generateTTUMBeanObj.setStRemark(FileName);
				//	generateTTUMBeanObj.setStCard_Number(rset_200.getString("PRIMARY_ACCOUNT_NUMBER"));
				//in reference field they need file name as per mail received on 08th feb 2018
				//generateTTUMBeanObj.setStCard_Number(rset_200.getString("UNIQUE_FILE_NAME"));
				//generateTTUMBeanObj.setStCard_Number(FileName);
				/*String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);*/
				String remark = getJdbcTemplate().queryForObject("select 'RPYD'||'"+dateFormat.format(varDate)+"'||ttum_seq.nextval from dual", new Object[] {},String.class);
				generateTTUMBeanObj.setStCard_Number(remark);
				TTUM_C_Data.add(generateTTUMBeanObj);

			}
		}
		else if(generateTTUMBean.getStFunctionCode().equals("262")){

			pstmt_262 = conn.prepareStatement(GET_TTUM_RECORDS_262);
			rset_262 = pstmt_262.executeQuery();
			String recon_date = "";
			String stfileName = "";
			Double total_amount = 0.00;
			boolean flag = false;
			String stTran_particulars ="";
			String sttotal_amount = "";
			int loop_count=0;

			logger.info("WHILE LOOP STARTED AT "+new java.sql.Timestamp(new java.util.Date().getTime()));
			String card_num = "";


			while(rset_262.next())
			{
				flag = true;

				{
					GenerateTTUMBean generateTTUMBeanObj = new GenerateTTUMBean();


					//*****************************************AS PER AJAY SAWANT ACC NUMBER IS FETCHED FROM RAW TABLE
					card_num = rset_262.getString("PRIMARY_ACCOUNT_NUMBER");

					String stAccNum = "";

					stAccNum = ACCNUMS.get(card_num);


					//Changes by minakshi 06July18
					if(stAccNum == null || stAccNum.equals("")){
						generateTTUMBeanObj.setStCreditAcc("ACCOUNT NUMBER NOT AVAILABLE");
						//generateTTUMBeanObj.setStCreditAcc(stAccNum);												
						generateTTUMBeanObj.setStAmount(rset_262.getString("AMOUNT_TRANSACTION"));
						//String stTran_particulars = "LP-RPAY-"+"-"+rset_262.getString("DATEANDTIME_LOCAL_TRANSACTION")+"-"+rset_262.getString("APPROVAL_CODE");
						//String stTran_particulars = "REFUND RPAY "+rset_262.getString("DATEANDTIME_LOCAL_TRANSACTION")+" "+rset_262.getString("APPROVAL_CODE");
						//stTran_particulars = "REFUND RPAY "+rset_262.getString("RECON_DATE")+" "+rset_262.getString("APPROVAL_CODE");
						stTran_particulars = "REFUND RPAY"+"-"+rset_262.getString("TRAN_DATE")+"-"+rset_262.getString("APPROVAL_CODE");
						//generateTTUMBeanObj.setStDate(rset_262.getString("DATEANDTIME_LOCAL_TRANSACTION"));
						generateTTUMBeanObj.setStDate(generateTTUMBean.getStDate());
						//+"/"+rset.getString("TRACE");//WHICH FIELD TO BE TAKEN IN CASE OF RUPAY?
						generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
						//generateTTUMBeanObj.setStCard_Number(rset_262.getString("PRIMARY_ACCOUNT_NUMBER"));
						generateTTUMBeanObj.setStCard_Number(rset_262.getString("ACQUIRER_REFERENCE_DATA"));
						/*String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
					generateTTUMBeanObj.setStRemark(remark);*/
						//generateTTUMBeanObj.setStRemark(rset_262.getString("ACQUIRER_REFERENCE_DATA"));
						generateTTUMBeanObj.setStRemark(rset_262.getString("PRIMARY_ACCOUNT_NUMBER"));
						TTUM_C_Data.add(generateTTUMBeanObj);
						total_amount+=Double.valueOf(rset_262.getString("AMOUNT_TRANSACTION"));
						/*sttotal_amount = String.valueOf(total_amount);*/
						recon_date = rset_262.getString("RECON_DATE");
						stfileName = rset_262.getString("UNIQUE_FILE_NAME");
					}						
					else if(stAccNum.contains("78000010021"))
					{
						//logger.info("Contrac acco-->>"+stAccNum);
						//logger.info("Card no-->>"+card_num);

						/*String get_man_acc_262="select t1.MAN_CONTRA_ACCOUNT from  SETTLEMENT_RUPAY_SWITCH t1 where t1.pan='"+card_num+"' " +
						 		"and t1.CONTRA_ACCOUNT='"+stAccNum+"' and TO_CHAR(T1.FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBean.getStDate()+"'";
*/							
						String get_man_acc_262="select distinct t1.CONTRA_ACCOUNT,t2.ACCTNUM,t1.FILEDATE,t1.TRAN_DATE,t1.AMOUNT,t2.AMOUNT_EQUIV,substr(t1.REF_NO,2,6),substr(t2.TRACE,2,6) from cbs_rupay_rawdata t1 inner join "
  + "switch_rawdata t2 on t1.REMARKS = t2.PAN and t1.FILEDATE = t2.FILEDATE and trunc(TO_NUMBER(REPLACE(T1.AMOUNT,',',''))) = trunc(TO_NUMBER(t2.AMOUNT_EQUIV)) and substr(t1.REF_NO,2,6) "
  + "=substr(t2.TRACE,2,6)  and t2.ACCTNUM is not null "
  + "where t1.CONTRA_ACCOUNT = '"+stAccNum+"' and t2.PAN = '"+card_num+"' order by t1.FILEDATE";
						
						pstmt_con_262 = conn.prepareStatement(get_man_acc_262);
						rset_con_262 = pstmt_con_262.executeQuery();
						
							
						while(rset_con_262.next())
						{
							if(loop_count==0)
							{
							//stAccNum=rset_con_262.getString(rset_con_262.getString("ACCTNUM").replaceAll(" ", "").replaceAll("^0*",""));
							//String split_dt[]=stAccNum.split("\\s+");
							generateTTUMBeanObj.setStCreditAcc(rset_con_262.getString("ACCTNUM").replaceAll(" ", " ").replaceAll("^0*",""));
							loop_count++;
							}
						}
						generateTTUMBeanObj.setStAmount(rset_262.getString("AMOUNT_TRANSACTION"));
						//String stTran_particulars = "LP-RPAY-"+"-"+rset_262.getString("DATEANDTIME_LOCAL_TRANSACTION")+"-"+rset_262.getString("APPROVAL_CODE");
						//String stTran_particulars = "REFUND RPAY "+rset_262.getString("DATEANDTIME_LOCAL_TRANSACTION")+" "+rset_262.getString("APPROVAL_CODE");
						//stTran_particulars = "REFUND RPAY "+rset_262.getString("RECON_DATE")+" "+rset_262.getString("APPROVAL_CODE");
						stTran_particulars = "REFUND RPAY"+"-"+rset_262.getString("TRAN_DATE")+"-"+rset_262.getString("APPROVAL_CODE");
						//generateTTUMBeanObj.setStDate(rset_262.getString("DATEANDTIME_LOCAL_TRANSACTION"));
						generateTTUMBeanObj.setStDate(generateTTUMBean.getStDate());
						//+"/"+rset.getString("TRACE");//WHICH FIELD TO BE TAKEN IN CASE OF RUPAY?
						generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
						//generateTTUMBeanObj.setStCard_Number(rset_262.getString("PRIMARY_ACCOUNT_NUMBER"));
						generateTTUMBeanObj.setStCard_Number(rset_262.getString("ACQUIRER_REFERENCE_DATA"));
						/*String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
					generateTTUMBeanObj.setStRemark(remark);*/
						//generateTTUMBeanObj.setStRemark(rset_262.getString("ACQUIRER_REFERENCE_DATA"));
						generateTTUMBeanObj.setStRemark(rset_262.getString("PRIMARY_ACCOUNT_NUMBER"));
						TTUM_C_Data.add(generateTTUMBeanObj);
						total_amount+=Double.valueOf(rset_262.getString("AMOUNT_TRANSACTION"));
						/*sttotal_amount = String.valueOf(total_amount);*/
						recon_date = rset_262.getString("RECON_DATE");
						stfileName = rset_262.getString("UNIQUE_FILE_NAME");
						loop_count=0;
					}
					
					
					else{
						generateTTUMBeanObj.setStCreditAcc(stAccNum);
					generateTTUMBeanObj.setStAmount(rset_262.getString("AMOUNT_TRANSACTION"));
					//String stTran_particulars = "LP-RPAY-"+"-"+rset_262.getString("DATEANDTIME_LOCAL_TRANSACTION")+"-"+rset_262.getString("APPROVAL_CODE");
					//String stTran_particulars = "REFUND RPAY "+rset_262.getString("DATEANDTIME_LOCAL_TRANSACTION")+" "+rset_262.getString("APPROVAL_CODE");
					//stTran_particulars = "REFUND RPAY "+rset_262.getString("RECON_DATE")+" "+rset_262.getString("APPROVAL_CODE");
					stTran_particulars = "REFUND RPAY"+"-"+rset_262.getString("TRAN_DATE")+"-"+rset_262.getString("APPROVAL_CODE");
					//generateTTUMBeanObj.setStDate(rset_262.getString("DATEANDTIME_LOCAL_TRANSACTION"));
					generateTTUMBeanObj.setStDate(generateTTUMBean.getStDate());
					//+"/"+rset.getString("TRACE");//WHICH FIELD TO BE TAKEN IN CASE OF RUPAY?
					generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
					//generateTTUMBeanObj.setStCard_Number(rset_262.getString("PRIMARY_ACCOUNT_NUMBER"));
					generateTTUMBeanObj.setStCard_Number(rset_262.getString("ACQUIRER_REFERENCE_DATA"));
					/*String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
				generateTTUMBeanObj.setStRemark(remark);*/
					//generateTTUMBeanObj.setStRemark(rset_262.getString("ACQUIRER_REFERENCE_DATA"));
					generateTTUMBeanObj.setStRemark(rset_262.getString("PRIMARY_ACCOUNT_NUMBER"));
					TTUM_C_Data.add(generateTTUMBeanObj);
					total_amount+=Double.valueOf(rset_262.getString("AMOUNT_TRANSACTION"));
					/*sttotal_amount = String.valueOf(total_amount);*/
					recon_date = rset_262.getString("RECON_DATE");
					stfileName = rset_262.getString("UNIQUE_FILE_NAME");
				}
				}
			}
			logger.info("WHILE LOOP ENDED AT "+new java.sql.Timestamp(new java.util.Date().getTime()));
			//adding one single entry for chargeback account
			if(flag)
			{
				/*String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);*/
				String remark = getJdbcTemplate().queryForObject("select 'RPYD'||'"+dateFormat.format(varDate)+"'|| ttum_seq.nextval from dual", new Object[] {},String.class);

				GenerateTTUMBean generateTTUMBeanObj = new GenerateTTUMBean();
				generateTTUMBeanObj.setStDebitAcc("99934450010017");
				sttotal_amount = String.valueOf(Double.parseDouble(new DecimalFormat("##.####").format(total_amount))); 
				generateTTUMBeanObj.setStAmount(sttotal_amount);
				generateTTUMBeanObj.setStCard_Number(stfileName);
				stTran_particulars = "REFUND RPAY"+recon_date;
				generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
				generateTTUMBeanObj.setStRemark(remark);
				//generateTTUMBeanObj.setStRemark(stlastremark);
				TTUM_D_Data.add(generateTTUMBeanObj);
				TTUM_D_Data1.add(generateTTUMBeanObj);

			}
		}
	}
	else if(generateTTUMBean.getStSubCategory().equalsIgnoreCase("INTERNATIONAL"))
	{
		logger.info("*** In INTERNATIONAL ***");
		
		String GET_TTUM_RECORDS_200 = "SELECT substr(DATEANDTIME_LOCAL_TRANSACTION,1,6) as tran_date,TO_CHAR(FILEDATE,'DDMMYY') AS RECON_DATE,TO_CHAR(FILEDATE,'DD-MM-YY') AS RECON_DATE1,UNIQUE_FILE_NAME,TO_CHAR(TO_DATE(SUBSTR(DATEANDTIME_LOCAL_TRANSACTION,1,6),'YY/MM/DD'),'DDMMYY') AS DATEANDTIME_LOCAL_TRANSACTION," +
				"AMOUNT_TRANSACTION,PRIMARY_ACCOUNT_NUMBER,APPROVAL_CODE,SUBSTR(ACQUIRER_REFERENCE_DATA,11,12) AS ACQUIRER_REFERENCE_DATA" +
				" FROM SETTLEMENT_"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStFile_Name()+
				" WHERE DCRS_REMARKS LIKE '%"+generateTTUMBean.getStMerger_Category()+"-UNRECON-GENERATE-TTUM%' AND TXNFUNCTION_CODE = '200' and DATE_SETTLEMENT = '"+dateFormat1.format(varDate1)+"'"+ 
				" AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBean.getStDate()+"'";
		
		String GET_TTUM_RECORDS_262 = "SELECT substr(DATEANDTIME_LOCAL_TRANSACTION,1,6) as tran_date,TO_CHAR(FILEDATE,'DDMMYY') AS RECON_DATE,TO_CHAR(FILEDATE,'DD-MM-YY') AS RECON_DATE1,UNIQUE_FILE_NAME,TO_CHAR(TO_DATE(SUBSTR(DATEANDTIME_LOCAL_TRANSACTION,1,6),'YY/MM/DD'),'DDMMYY') AS DATEANDTIME_LOCAL_TRANSACTION," +
				"AMOUNT_TRANSACTION,PRIMARY_ACCOUNT_NUMBER,APPROVAL_CODE,SUBSTR(ACQUIRER_REFERENCE_DATA,11,12) AS ACQUIRER_REFERENCE_DATA"+//,SUM(AMOUNT_TRANSACTION) AS DR_AMT" +
				" FROM SETTLEMENT_"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStFile_Name()+
				" WHERE DCRS_REMARKS LIKE '%"+generateTTUMBean.getStMerger_Category()+"-UNRECON-GENERATE-TTUM%' AND TXNFUNCTION_CODE = '263' AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBean.getStDate()+"'" ;
				//" GROUP BY FILEDATE,UNIQUE_FILE_NAME,DATEANDTIME_LOCAL_TRANSACTION,AMOUNT_TRANSACTION,PRIMARY_ACCOUNT_NUMBER,APPROVAL_CODE," +
				//"ACQUIRER_REFERENCE_DATA,PRIMARY_ACCOUNT_NUMBER,APPROVAL_CODE, ACQUIRER_REFERENCE_DATA";


		logger.info("GET_TTUM_RECORDS_200=="+GET_TTUM_RECORDS_200);
		
		logger.info("GET_TTUM_RECORDS_262=="+GET_TTUM_RECORDS_262);
		
		
		//GET ACC NUMBERS
				if(generateTTUMBean.getStFunctionCode().equals("200"))
				{
				  GET_ACC_NUMBS = "SELECT DISTINCT T2.CONTRA_ACCOUNT,T1.PRIMARY_ACCOUNT_NUMBER FROM SETTLEMENT_RUPAY_RUPAY T1,CBS_RUPAY_RAWDATA T2"+ 
						 		" WHERE T1.DCRS_REMARKS LIKE '%RUPAY_INT-UNRECON-GENERATE-TTUM%' AND  T1.TXNFUNCTION_CODE = '200' "+ 
						 		" AND TO_CHAR(T1.FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBean.getStDate()+"' AND T1.PRIMARY_ACCOUNT_NUMBER = T2.REMARKS and t2.CONTRA_ACCOUNT is not null";
				
				  logger.info("GET_ACC_NUMBS=="+GET_ACC_NUMBS);
				}
				else if(generateTTUMBean.getStFunctionCode().equals("263"))
				{
					 GET_ACC_NUMBS = "SELECT DISTINCT T2.CONTRA_ACCOUNT,T1.PRIMARY_ACCOUNT_NUMBER FROM SETTLEMENT_RUPAY_RUPAY T1,CBS_RUPAY_RAWDATA T2"+ 
					 		" WHERE T1.DCRS_REMARKS LIKE '%RUPAY_INT-UNRECON-GENERATE-TTUM%' AND T1.TXNFUNCTION_CODE = '263' "+ 
					 		" AND TO_CHAR(T1.FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBean.getStDate()+"' AND T1.PRIMARY_ACCOUNT_NUMBER = T2.REMARKS and t2.CONTRA_ACCOUNT is not null";
				
					 logger.info("GET_ACC_NUMBS=="+GET_ACC_NUMBS);
				}
				 Map<String, String> ACCNUMS = (Map<String, String>) getJdbcTemplate().query(GET_ACC_NUMBS, new Object[] {},new ResultSetExtractor() {
					 public Object extractData(ResultSet rs) throws SQLException {
						 Map map = new HashMap();
						 while (rs.next()) {
						 String col1 = rs.getString("PRIMARY_ACCOUNT_NUMBER");
						 String col2 = rs.getString("CONTRA_ACCOUNT");
						/* if(col2.contains("78000010021"))
						 {
							 String get_man_acc="select t1.MAN_CONTRA_ACCOUNT from  SETTLEMENT_RUPAY_SWITCH t1 where t1.pan='"+col1+"' " +
							 		"and t1.CONTRA_ACCOUNT='"+col2+"' and TO_CHAR(T1.FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBean.getStDate()+"'";
						 map.put(col1, col2);
						 }else{*/
							 map.put(col1, col2);
						 }
						 //}
						 return map;
						 };
						 });			 
				 
				 if(generateTTUMBean.getStFunctionCode().equals("200"))
					{
						pstmt_200 = conn.prepareStatement(GET_TTUM_RECORDS_200);
						rset_200 = pstmt_200.executeQuery();
						
						Double total_amount = 0.00;
						String sttotal_amount = "";
						String stTran_particulars= "";
						String FileName = "";
			           String man_acco="";
			           int loop_count1=0;
			           int cnt = 0;
						while(rset_200.next())
						{
							cnt++;
							GenerateTTUMBean generateTTUMBeanObj = new GenerateTTUMBean();
							/*if(cnt%2==0){
								generateTTUMBeanObj.setPart_tran_type("C");
								cnt++;
							}else{
								generateTTUMBeanObj.setPart_tran_type("D");
								cnt++;
							}*/
							generateTTUMBeanObj.setStSubCategory(generateTTUMBean.getStSubCategory());
							generateTTUMBeanObj.setStFunctionCode(generateTTUMBean.getStFunctionCode());
							generateTTUMBeanObj.setStCreditAcc("99937200010663");	
							generateTTUMBeanObj.setStAmount(rset_200.getString("AMOUNT_TRANSACTION"));
							total_amount+=Double.valueOf(rset_200.getString("AMOUNT_TRANSACTION"));
							
							
							String card_num = rset_200.getString("PRIMARY_ACCOUNT_NUMBER");
							//GETDATA = "SELECT DISTINCT CONTRA_ACCOUNT FROM "+rawtablename+" WHERE REMARKS = '"+card_num+"'";
							String stAccNum = "";
							stAccNum = ACCNUMS.get(card_num);
							
							//Changes by minakshi 06July18
							if(stAccNum == null || stAccNum.equals("")){
								generateTTUMBeanObj.setStDebitAcc("ACCOUNT NUMBER NOT AVAILABLE");
								generateTTUMBeanObj.setPart_tran_type("D");
								//generateTTUMBeanObj.setStCreditAcc("99937200010663");
							//generateTTUMBeanObj.setStDebitAcc(stAccNum);				
							//stTran_particulars = "LP-RPAY"+"-"+rset_200.getString("RECON_DATE")+"-"+rset_200.getString("APPROVAL_CODE");
							stTran_particulars = "LP/RUP"+"/"+rset_200.getString("TRAN_DATE")+"/"+rset_200.getString("APPROVAL_CODE");
							//generateTTUMBeanObj.setStDate(rset_200.getString("DATEANDTIME_LOCAL_TRANSACTION"));
							generateTTUMBeanObj.setStDate(generateTTUMBean.getStDate());
							//+"/"+rset.getString("TRACE");//WHICH FIELD TO BE TAKEN IN CASE OF RUPAY?
							generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
							//generateTTUMBeanObj.setStCard_Number(rset_200.getString("PRIMARY_ACCOUNT_NUMBER"));
							generateTTUMBeanObj.setStRemark(rset_200.getString("PRIMARY_ACCOUNT_NUMBER"));
							//generateTTUMBeanObj.setStCard_Number(rset_200.getString("ACQUIRER_REFERENCE_DATA"));
							String remark = getJdbcTemplate().queryForObject("select 'RPYI'||'"+dateFormat.format(varDate)+"'||ttum_seq.nextval from dual", new Object[] {},String.class);
						    generateTTUMBeanObj.setStCard_Number(remark);
							//TTUM_data.add(generateTTUMBeanObj);
							FileName = rset_200.getString("UNIQUE_FILE_NAME");
							TTUM_D_Data.add(generateTTUMBeanObj);
							TTUM_D_Data1.add(generateTTUMBeanObj);
							stTran_particulars = "LP/RUP/ "+rset_200.getString("RECON_DATE1");//+" "+rset_200.getString("APPROVAL_CODE");
							}
							else if(stAccNum.contains("78000010021"))
							{
								//logger.info("Contrac acco-->>"+stAccNum);
								//logger.info("Card no-->>"+card_num);
								String get_man_acc_200="select distinct t1.CONTRA_ACCOUNT,t2.ACCTNUM,t1.FILEDATE,t1.TRAN_DATE,t1.AMOUNT,t2.AMOUNT_EQUIV,substr(t1.REF_NO,2,6),substr(t2.TRACE,2,6) from cbs_rupay_rawdata t1 inner join "
									      + "switch_rawdata t2 on t1.REMARKS = t2.PAN and t1.FILEDATE = t2.FILEDATE and trunc(TO_NUMBER(REPLACE(T1.AMOUNT,',',''))) = trunc(TO_NUMBER(t2.AMOUNT_EQUIV)) and substr(t1.REF_NO,2,6) "
									      + "=substr(t2.TRACE,2,6)  and t2.ACCTNUM is not null "
									      + "where t1.CONTRA_ACCOUNT = '"+stAccNum+"' and t2.PAN = '"+card_num+"' order by t1.FILEDATE";
																
																pstmt_con_200 = conn.prepareStatement(get_man_acc_200);
																rset_con_262 = pstmt_con_200.executeQuery();
																
																	
																while(rset_con_262.next())
																{
																	if(loop_count1==0)
																	{
																	//stAccNum=rset_con_262.getString(rset_con_262.getString("ACCTNUM").replaceAll(" ", "").replaceAll("^0*",""));
																	//String split_dt[]=stAccNum.split("\\s+");
																	generateTTUMBeanObj.setStDebitAcc(rset_con_262.getString("ACCTNUM").replaceAll(" ", " ").replaceAll("^0*",""));
																	loop_count1++;
																	}
																}
							   // generateTTUMBeanObj.setStCreditAcc("99937200010663");		
								generateTTUMBeanObj.setPart_tran_type("D");
								stTran_particulars = "LP/RUP"+"/"+rset_200.getString("TRAN_DATE")+"/"+rset_200.getString("APPROVAL_CODE");
								//generateTTUMBeanObj.setStDate(rset_200.getString("DATEANDTIME_LOCAL_TRANSACTION"));
								generateTTUMBeanObj.setStDate(generateTTUMBean.getStDate());
								//+"/"+rset.getString("TRACE");//WHICH FIELD TO BE TAKEN IN CASE OF RUPAY?
								generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
								//generateTTUMBeanObj.setStCard_Number(rset_200.getString("PRIMARY_ACCOUNT_NUMBER"));
								generateTTUMBeanObj.setStRemark(rset_200.getString("PRIMARY_ACCOUNT_NUMBER"));
								//generateTTUMBeanObj.setStCard_Number(rset_200.getString("ACQUIRER_REFERENCE_DATA"));
								String remark = getJdbcTemplate().queryForObject("select 'RPYI'||'"+dateFormat.format(varDate)+"'||ttum_seq.nextval from dual", new Object[] {},String.class);
								generateTTUMBeanObj.setStCard_Number(remark);
								/*String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
									generateTTUMBeanObj.setStRemark(remark);*/
								//TTUM_data.add(generateTTUMBeanObj);
								FileName = rset_200.getString("UNIQUE_FILE_NAME");
								TTUM_D_Data.add(generateTTUMBeanObj);
								TTUM_D_Data1.add(generateTTUMBeanObj);
								stTran_particulars = "LP/RUP/"+rset_200.getString("RECON_DATE1");//+" "+rset_200.getString("APPROVAL_CODE");
							}
							else
							{
								generateTTUMBeanObj.setStDebitAcc(stAccNum);	
								generateTTUMBeanObj.setPart_tran_type("D");
								//generateTTUMBeanObj.setStCreditAcc("99937200010663");
							//stTran_particulars = "LP-RPAY"+"-"+rset_200.getString("RECON_DATE")+"-"+rset_200.getString("APPROVAL_CODE");
							stTran_particulars = "LP/RUP"+"/"+rset_200.getString("TRAN_DATE")+"/"+rset_200.getString("APPROVAL_CODE");
							//generateTTUMBeanObj.setStDate(rset_200.getString("DATEANDTIME_LOCAL_TRANSACTION"));
							generateTTUMBeanObj.setStDate(generateTTUMBean.getStDate());
							//+"/"+rset.getString("TRACE");//WHICH FIELD TO BE TAKEN IN CASE OF RUPAY?
							generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
							//generateTTUMBeanObj.setStCard_Number(rset_200.getString("PRIMARY_ACCOUNT_NUMBER"));
							generateTTUMBeanObj.setStRemark(rset_200.getString("PRIMARY_ACCOUNT_NUMBER"));
							//generateTTUMBeanObj.setStCard_Number(rset_200.getString("ACQUIRER_REFERENCE_DATA"));
							String remark = getJdbcTemplate().queryForObject("select 'RPYI'||'"+dateFormat.format(varDate)+"'||ttum_seq.nextval from dual", new Object[] {},String.class);
							generateTTUMBeanObj.setStCard_Number(remark);
							/*String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
								generateTTUMBeanObj.setStRemark(remark);*/
							//TTUM_data.add(generateTTUMBeanObj);
							FileName = rset_200.getString("UNIQUE_FILE_NAME");
							TTUM_D_Data.add(generateTTUMBeanObj);
							TTUM_D_Data1.add(generateTTUMBeanObj);
							stTran_particulars = "LP/RUP/"+rset_200.getString("RECON_DATE1");//+" "+rset_200.getString("APPROVAL_CODE");

						}
						//CHANGES MADE BY int5688 FOR multiple CREDIT ENTRY
						if(TTUM_D_Data.size()>0)
						{
							//GenerateTTUMBean generateTTUMBeanObj1 = new GenerateTTUMBean();

							generateTTUMBeanObj.setStCreditAcc("99937200010663");
							//if(cnt%2==0){
								generateTTUMBeanObj.setPart_tran_type("C");
								//cnt++;
							//}
							//generateTTUMBeanObj.setPart_tran_type("C");
							//sttotal_amount = String.valueOf(Double.parseDouble(new DecimalFormat("##.####").format(total_amount))); 
							//generateTTUMBeanObj.setStAmount(sttotal_amount);							
							//generateTTUMBeanObj.setStDate(generateTTUMBean.getStDate());							
							//generateTTUMBeanObj.setStTran_particulars(stTran_particulars);							
							//generateTTUMBeanObj.setStRemark(FileName);
							//String remark = getJdbcTemplate().queryForObject("select 'RPYI'||'"+dateFormat.format(varDate)+"'||ttum_seq.nextval from dual", new Object[] {},String.class);
							//generateTTUMBeanObj.setStCard_Number(remark);
							TTUM_D_Data.add(generateTTUMBeanObj);
						}
						}
					}
				 
				 else if(generateTTUMBean.getStFunctionCode().equals("263")){

						pstmt_262 = conn.prepareStatement(GET_TTUM_RECORDS_262);
						rset_262 = pstmt_262.executeQuery();
						String recon_date = "";
						String stfileName = "";
						Double total_amount = 0.00;
						boolean flag = false;
						String stTran_particulars ="";
						String sttotal_amount = "";
						int loop_count=0;

						logger.info("WHILE LOOP STARTED AT "+new java.sql.Timestamp(new java.util.Date().getTime()));
						String card_num = "";


						while(rset_262.next())
						{
							flag = true;

							{
								GenerateTTUMBean generateTTUMBeanObj = new GenerateTTUMBean();

								generateTTUMBeanObj.setStSubCategory(generateTTUMBean.getStSubCategory());
								generateTTUMBeanObj.setStFunctionCode(generateTTUMBean.getStFunctionCode());
								//generateTTUMBeanObj.setStDebitAcc("99934450010117");	
								
								//*****************************************AS PER AJAY SAWANT ACC NUMBER IS FETCHED FROM RAW TABLE
								card_num = rset_262.getString("PRIMARY_ACCOUNT_NUMBER");

								String stAccNum = "";

								stAccNum = ACCNUMS.get(card_num);


								//Changes by minakshi 06July18
								if(stAccNum == null || stAccNum.equals("")){
									generateTTUMBeanObj.setStCreditAcc("ACCOUNT NUMBER NOT AVAILABLE");																				
									generateTTUMBeanObj.setStAmount(rset_262.getString("AMOUNT_TRANSACTION"));									
									stTran_particulars = "REFUND/RPAY"+"/"+rset_262.getString("TRAN_DATE")+"/"+rset_262.getString("APPROVAL_CODE");									
									generateTTUMBeanObj.setStDate(generateTTUMBean.getStDate());									
									generateTTUMBeanObj.setStTran_particulars(stTran_particulars);	
									String remark = getJdbcTemplate().queryForObject("select 'RPYI'||'"+dateFormat.format(varDate)+"'||ttum_seq.nextval from dual", new Object[] {},String.class);
								    generateTTUMBeanObj.setStCard_Number(remark);									
									generateTTUMBeanObj.setStRemark(rset_262.getString("PRIMARY_ACCOUNT_NUMBER"));
									//generateTTUMBeanObj.setPart_tran_type("D");
									TTUM_C_Data.add(generateTTUMBeanObj);
									TTUM_D_Data1.add(generateTTUMBeanObj);
									total_amount+=Double.valueOf(rset_262.getString("AMOUNT_TRANSACTION"));									
									recon_date = rset_262.getString("RECON_DATE");
									stfileName = rset_262.getString("UNIQUE_FILE_NAME");
								}						
								else if(stAccNum.contains("78000010021"))
								{
										
									String get_man_acc_262="select distinct t1.CONTRA_ACCOUNT,t2.ACCTNUM,t1.FILEDATE,t1.TRAN_DATE,t1.AMOUNT,t2.AMOUNT_EQUIV,substr(t1.REF_NO,2,6),substr(t2.TRACE,2,6) from cbs_rupay_rawdata t1 inner join "
			  + "switch_rawdata t2 on t1.REMARKS = t2.PAN and t1.FILEDATE = t2.FILEDATE and trunc(TO_NUMBER(REPLACE(T1.AMOUNT,',',''))) = trunc(TO_NUMBER(t2.AMOUNT_EQUIV)) and substr(t1.REF_NO,2,6) "
			  + "=substr(t2.TRACE,2,6)  and t2.ACCTNUM is not null "
			  + "where t1.CONTRA_ACCOUNT = '"+stAccNum+"' and t2.PAN = '"+card_num+"' order by t1.FILEDATE";
									
									pstmt_con_262 = conn.prepareStatement(get_man_acc_262);
									rset_con_262 = pstmt_con_262.executeQuery();
									
										
									while(rset_con_262.next())
									{
										if(loop_count==0)
										{
										
										generateTTUMBeanObj.setStCreditAcc(rset_con_262.getString("ACCTNUM").replaceAll(" ", " ").replaceAll("^0*",""));
										loop_count++;
										}
									}
									generateTTUMBeanObj.setStAmount(rset_262.getString("AMOUNT_TRANSACTION"));									
									stTran_particulars = "REFUND/RPAY"+"/"+rset_262.getString("TRAN_DATE")+"/"+rset_262.getString("APPROVAL_CODE");									
									generateTTUMBeanObj.setStDate(generateTTUMBean.getStDate());									
									generateTTUMBeanObj.setStTran_particulars(stTran_particulars);	
									String remark = getJdbcTemplate().queryForObject("select 'RPYI'||'"+dateFormat.format(varDate)+"'||ttum_seq.nextval from dual", new Object[] {},String.class);
									generateTTUMBeanObj.setStCard_Number(remark);								
									generateTTUMBeanObj.setStRemark(rset_262.getString("PRIMARY_ACCOUNT_NUMBER"));
									//generateTTUMBeanObj.setPart_tran_type("D");
									TTUM_C_Data.add(generateTTUMBeanObj);
									TTUM_D_Data1.add(generateTTUMBeanObj);
									total_amount+=Double.valueOf(rset_262.getString("AMOUNT_TRANSACTION"));									
									recon_date = rset_262.getString("RECON_DATE");
									stfileName = rset_262.getString("UNIQUE_FILE_NAME");
									loop_count=0;
								} else{
									generateTTUMBeanObj.setStCreditAcc(stAccNum);
								generateTTUMBeanObj.setStAmount(rset_262.getString("AMOUNT_TRANSACTION"));
								stTran_particulars = "REFUND/RPAY"+"/"+rset_262.getString("TRAN_DATE")+"/"+rset_262.getString("APPROVAL_CODE");
								generateTTUMBeanObj.setStDate(generateTTUMBean.getStDate());
								generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
								String remark = getJdbcTemplate().queryForObject("select 'RPYI'||'"+dateFormat.format(varDate)+"'||ttum_seq.nextval from dual", new Object[] {},String.class);
								generateTTUMBeanObj.setStCard_Number(remark);
								generateTTUMBeanObj.setStRemark(rset_262.getString("PRIMARY_ACCOUNT_NUMBER"));
								//generateTTUMBeanObj.setPart_tran_type("D");
								TTUM_C_Data.add(generateTTUMBeanObj);
								TTUM_D_Data1.add(generateTTUMBeanObj);
								total_amount+=Double.valueOf(rset_262.getString("AMOUNT_TRANSACTION"));
								recon_date = rset_262.getString("RECON_DATE");
								stfileName = rset_262.getString("UNIQUE_FILE_NAME");
							}
							}
						}
						
						logger.info("WHILE LOOP ENDED AT "+new java.sql.Timestamp(new java.util.Date().getTime()));
						//adding one single entry for chargeback account
						if(flag)
						{
							/*String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);*/
							String remark = getJdbcTemplate().queryForObject("select 'RPYI'||'"+dateFormat.format(varDate)+"'|| ttum_seq.nextval from dual", new Object[] {},String.class);

							GenerateTTUMBean generateTTUMBeanObj = new GenerateTTUMBean();
							//generateTTUMBeanObj.setPart_tran_type("D");
							generateTTUMBeanObj.setStDebitAcc("99934450010117");
							sttotal_amount = String.valueOf(Double.parseDouble(new DecimalFormat("##.####").format(total_amount))); 
							generateTTUMBeanObj.setStAmount(sttotal_amount);
							generateTTUMBeanObj.setStCard_Number(stfileName);
							stTran_particulars = "REFUND/RPAY"+recon_date;
							generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
							generateTTUMBeanObj.setStRemark(remark);
							//generateTTUMBeanObj.setStRemark(stlastremark);
							TTUM_D_Data.add(generateTTUMBeanObj);
							TTUM_D_Data1.add(generateTTUMBeanObj);
						

						}
				 //}
					}
	

		
	}	
		
	
	logger.info("DONE STEP 1...........");
	
	for(int i = 0 ; i<ExcelHeaders1.size();i++)
	{
		table_cols =table_cols+","+ ExcelHeaders1.get(i)+" VARCHAR (100 BYTE)";
		insert_cols = insert_cols+","+ExcelHeaders1.get(i);
	}
	
	String CHECK_TABLE = "SELECT count (*) FROM tab WHERE tname  = 'TTUM_"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStSubCategory().substring(0, 3)
			+"_"+generateTTUMBean.getStFile_Name()+"'";
	int tableExist = getJdbcTemplate().queryForObject(CHECK_TABLE, new Object[] { },Integer.class);
	
	logger.info("CHECK_TABLE=="+CHECK_TABLE);
	logger.info("tableExist=="+tableExist);
	if(tableExist == 0)
	{
		String CREATE_QUERY = "CREATE TABLE TTUM_"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStSubCategory().substring(0, 3)
				+"_"+generateTTUMBean.getStFile_Name()+" ("+table_cols+")";
		
		logger.info("CREATE_QUERY=="+CREATE_QUERY);
		getJdbcTemplate().execute(CREATE_QUERY);
		logger.info("TABLE CREATED");
	}
	
	//INSERT TTUM ENTRIES IN ABOVE CREATED TABLE
	
	//for(int i = 0;i<TTUM_C_Data.size();i++)
	List<String> QUERIES = new ArrayList<>();
	int count =0;
	String code = "";
	if(generateTTUMBean.getStSubCategory().equalsIgnoreCase("INTERNATIONAL") && generateTTUMBean.getStFunctionCode().equals("200")){
	for(GenerateTTUMBean beanObj : TTUM_D_Data1)
	{
		
		String INSERT_QUERY1 = "INSERT INTO TTUM_"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStSubCategory().substring(0, 3)
				+"_"+generateTTUMBean.getStFile_Name() 
				+"("+insert_cols+") VALUES ('"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStSubCategory().substring(0, 3)+ //added by INT 6261 to insert data into ttum table
				"-UNRECON-TTUM-"+inRec_Set_Id+"-"+generateTTUMBean.getStFunctionCode()
				+"',SYSDATE,'"+generateTTUMBean.getStEntry_by()+"','"+beanObj.getStDebitAcc()+"','INR','999','D','"+
				beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','INR','"+beanObj.getStAmount()+"','"+
				beanObj.getStRemark()+"','"+beanObj.getStCard_Number()+"')";
	//getJdbcTemplate().execute(INSERT_QUERY);
		logger.info("INSERT_QUERY=="+INSERT_QUERY1);
		QUERIES.add(INSERT_QUERY1);
		count++;//added by INT 6261 to insert data into ttum table
		
		
		String INSERT_QUERY = "INSERT INTO TTUM_"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStSubCategory().substring(0, 3)
				 +"_"+generateTTUMBean.getStFile_Name() 
					+"("+insert_cols+")  VALUES ('"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStSubCategory().substring(0, 3) //added by INT 6261 to insert data into ttum table
					+"-UNRECON-TTUM-"+inRec_Set_Id+"-"+generateTTUMBean.getStFunctionCode()
					+"',SYSDATE,'"+generateTTUMBean.getStEntry_by()+"','"+beanObj.getStCreditAcc()+"','INR','999','C','"+
					beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','INR','"+beanObj.getStAmount()+"','"+
					beanObj.getStRemark()+"','"+beanObj.getStCard_Number()+"')";
		 //getJdbcTemplate().execute(INSERT_QUERY);	
		logger.info("INSERT_QUERY=="+INSERT_QUERY);
		QUERIES.add(INSERT_QUERY);
		count++;//added by INT 6261 to insert data into ttum table
		
		 if(count == 200)
		 {
			 String[] insert = new String[count];
			 insert = QUERIES.toArray(insert);
			 getJdbcTemplate().batchUpdate(insert);
			 QUERIES.clear();
			 count = 0;
		 }
		 
			}
	
	 if(count > 0 )
		{

			 String[] insert = new String[count];
			 insert = QUERIES.toArray(insert);
			 getJdbcTemplate().batchUpdate(insert);
			 QUERIES.clear();
			 count = 0;
		 
		}
	 
	}else{/*	
	for(GenerateTTUMBean beanObj : TTUM_D_Data)
	{
		
		String INSERT_QUERY1 = "INSERT INTO TTUM_"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStSubCategory().substring(0, 3)
				+"_"+generateTTUMBean.getStFile_Name() 
				+"("+insert_cols+") VALUES ('"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStSubCategory().substring(0, 3)+ //added by INT 6261 to insert data into ttum table
				"-UNRECON-TTUM-"+inRec_Set_Id+"-"+generateTTUMBean.getStFunctionCode()
				+"',SYSDATE,'"+generateTTUMBean.getStEntry_by()+"','"+beanObj.getStDebitAcc()+"','INR','999','D','"+
				beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','INR','"+beanObj.getStAmount()+"','"+
				beanObj.getStRemark()+"','"+beanObj.getStCard_Number()+"')";
	//getJdbcTemplate().execute(INSERT_QUERY);
		logger.info("INSERT_QUERY=="+INSERT_QUERY1);
		QUERIES.add(INSERT_QUERY1);
		count++;//added by INT 6261 to insert data into ttum table
		
		
		String INSERT_QUERY = "INSERT INTO TTUM_"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStSubCategory().substring(0, 3)
				 +"_"+generateTTUMBean.getStFile_Name() 
					+"("+insert_cols+")  VALUES ('"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStSubCategory().substring(0, 3) //added by INT 6261 to insert data into ttum table
					+"-UNRECON-TTUM-"+inRec_Set_Id+"-"+generateTTUMBean.getStFunctionCode()
					+"',SYSDATE,'"+generateTTUMBean.getStEntry_by()+"','"+beanObj.getStCreditAcc()+"','INR','999','C','"+
					beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','INR','"+beanObj.getStAmount()+"','"+
					beanObj.getStRemark()+"','"+beanObj.getStCard_Number()+"')";
		 //getJdbcTemplate().execute(INSERT_QUERY);	
		logger.info("INSERT_QUERY=="+INSERT_QUERY);
		QUERIES.add(INSERT_QUERY);
		count++;//added by INT 6261 to insert data into ttum table
		
		 if(count == 200)
		 {
			 String[] insert = new String[count];
			 insert = QUERIES.toArray(insert);
			 getJdbcTemplate().batchUpdate(insert);
			 QUERIES.clear();
			 count = 0;
		 }
		 
	}
	*/
		
		for(GenerateTTUMBean beanObj : TTUM_C_Data)
		{
			
			String INSERT_QUERY = "INSERT INTO TTUM_"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStSubCategory().substring(0, 3)
					 +"_"+generateTTUMBean.getStFile_Name() 
						+"("+insert_cols+")  VALUES ('"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStSubCategory().substring(0, 3) //added by INT 6261 to insert data into ttum table
						+"-UNRECON-TTUM-"+inRec_Set_Id+"-"+generateTTUMBean.getStFunctionCode()
						+"',SYSDATE,'"+generateTTUMBean.getStEntry_by()+"','"+beanObj.getStCreditAcc()+"','INR','999','C','"+
						beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','INR','"+beanObj.getStAmount()+"','"+
						beanObj.getStRemark()+"','"+beanObj.getStCard_Number()+"')";
			 //getJdbcTemplate().execute(INSERT_QUERY);	
			logger.info("INSERT_QUERY=="+INSERT_QUERY);
			QUERIES.add(INSERT_QUERY);
			count++;//added by INT 6261 to insert data into ttum table
			
			 if(count == 200)
			 {
				 String[] insert = new String[count];
				 insert = QUERIES.toArray(insert);
				 getJdbcTemplate().batchUpdate(insert);
				 QUERIES.clear();
				 count = 0;
			 }
			 
		}
		
		if(count > 0 )
		{

			 String[] insert = new String[count];
			 insert = QUERIES.toArray(insert);
			 getJdbcTemplate().batchUpdate(insert);
			 QUERIES.clear();
			 count = 0;
		 
		}
		
		count =0;
		for(GenerateTTUMBean beanObj : TTUM_D_Data)
		{
			String INSERT_QUERY = "INSERT INTO TTUM_"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStSubCategory().substring(0, 3)
					+"_"+generateTTUMBean.getStFile_Name() 
					+"("+insert_cols+") VALUES ('"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStSubCategory().substring(0, 3)+ //added by INT 6261 to insert data into ttum table
					"-UNRECON-TTUM-"+inRec_Set_Id+"-"+generateTTUMBean.getStFunctionCode()
					+"',SYSDATE,'"+generateTTUMBean.getStEntry_by()+"','"+beanObj.getStDebitAcc()+"','INR','999','D','"+
					beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','INR','"+beanObj.getStAmount()+"','"+
					beanObj.getStRemark()+"','"+beanObj.getStCard_Number()+"')";
		//getJdbcTemplate().execute(INSERT_QUERY);
			logger.info("INSERT_QUERY=="+INSERT_QUERY);
			QUERIES.add(INSERT_QUERY);
			count++;//added by INT 6261 to insert data into ttum table
			
			 if(count == 200)
			 {
				 String[] insert = new String[count];
				 insert = QUERIES.toArray(insert);
				 getJdbcTemplate().batchUpdate(insert);
				 QUERIES.clear();
				 count = 0;
			 }
		}
		if(count > 0 )
		{

			 String[] insert = new String[count];
			 insert = QUERIES.toArray(insert);
			 getJdbcTemplate().batchUpdate(insert);
			 QUERIES.clear();
			 count = 0;
		 
		}
		
	}
	 
	if(generateTTUMBean.getStSubCategory().equalsIgnoreCase("INTERNATIONAL")){
	if(generateTTUMBean.getStFunctionCode().equals("200")){
	TTUM_D_Data = new ArrayList<>();
	PreparedStatement ps;
	ResultSet rs;
	String GET_TTUM_200 = "SELECT ACCOUNT_NUMBER,PAR_TRAN_TYPE,TRANSACTION_AMOUNT,TRANSACTION_PARTICULARS,REMARKS,REFERENCE_NUMBER" +
			" FROM TTUM_RUPAY_INT_RUPAY WHERE DCRS_REMARKS LIKE '%UNRECON-TTUM-2-200%'  order by REFERENCE_NUMBER,TRANSACTION_AMOUNT,ACCOUNT_NUMBER";
	
	logger.info("GET_TTUM_200 "+GET_TTUM_200);
	
conn = getConnection();
ps = conn.prepareStatement(GET_TTUM_200);
rs = ps.executeQuery();
while(rs.next())
{
	count++;
	//logger.info("count is "+count);
	GenerateTTUMBean generateTTUMBeanObj = new GenerateTTUMBean();		
	
		generateTTUMBeanObj.setStSubCategory("INTERNATIONAL");
		generateTTUMBeanObj.setStDebitAcc(rs.getString("ACCOUNT_NUMBER").replaceAll(" ", " ").replaceAll("^0*",""));		
		generateTTUMBeanObj.setStCreditAcc(rs.getString("ACCOUNT_NUMBER").replaceAll(" ", " ").replaceAll("^0*",""));
		generateTTUMBeanObj.setStAmount(rs.getString("TRANSACTION_AMOUNT"));		
		generateTTUMBeanObj.setStTran_particulars(rs.getString("TRANSACTION_PARTICULARS"));		
		generateTTUMBeanObj.setStCard_Number(rs.getString("REFERENCE_NUMBER"));		
		generateTTUMBeanObj.setStRemark(rs.getString("REMARKS"));	
		generateTTUMBeanObj.setPart_tran_type(rs.getString("PAR_TRAN_TYPE"));
					
		TTUM_D_Data.add(generateTTUMBeanObj);
	}

}
}
	logger.info("INSERTED IN TTUM TABLE");
	
	Data.add(Excel_Header);
	Data.add(TTUM_data);
	Data.add(TTUM_C_Data);
	Data.add(TTUM_D_Data);
	
	String UPDATE_QUERY = "UPDATE SETTLEMENT_"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStFile_Name()+" SET DCRS_REMARKS = "+
	" REPLACE(DCRS_REMARKS ,'GENERATE','GENERATED') WHERE TXNFUNCTION_CODE = '"+generateTTUMBean.getStFunctionCode()
	+"' AND DCRS_REMARKS = '"+generateTTUMBean.getStMerger_Category()+"-UNRECON-GENERATE-TTUM-2' "
	+" AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBean.getStDate()+"'";
	
	logger.info("update query is "+UPDATE_QUERY);
	
	getJdbcTemplate().execute(UPDATE_QUERY);
	
	
	String CHECK_TABLE1 = "SELECT count (*) FROM tab WHERE tname  = UPPER('TTUM_"+generateTTUMBean.getStMerger_Category()
			+"_"+generateTTUMBean.getStFile_Name()+"_BKUP')";
	int tableExist1 = getJdbcTemplate().queryForObject(CHECK_TABLE1, new Object[] { },Integer.class);
	
	if(tableExist1 == 0)
	{
		String CREATE_QUERY1 = "CREATE TABLE TTUM_"+generateTTUMBean.getStMerger_Category()
				+"_"+generateTTUMBean.getStFile_Name()+"_BKUP ("+table_cols+",FILEDATE DATE,MOVED_ON DATE)";
		
		getJdbcTemplate().execute(CREATE_QUERY1);
	}
	
	String query = "insert into TTUM_"+generateTTUMBean.getStMerger_Category()+"_"+generateTTUMBean.getStFile_Name()+"_BKUP ("+insert_cols+",FILEDATE,MOVED_ON) "
			+ "select "+insert_cols+",TO_DATE('"+generateTTUMBean.getStFile_Date()+"','DD/MM/YYYY'),sysdate from TTUM_"+generateTTUMBean.getStMerger_Category()
				+"_"+generateTTUMBean.getStFile_Name();
	logger.info(query);
	
	
	
	getJdbcTemplate().execute(query);
	
	String query2 = "truncate table TTUM_"+generateTTUMBean.getStMerger_Category()+"_"+generateTTUMBean.getStFile_Name();
	logger.info(query2);
	getJdbcTemplate().execute(query2);
	
	
	logger.info("***** GenerateRupayTTUMDaoImpl.GenerateRupayTTUM End ****");
		
}
catch(Exception e)
{
	demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.GenerateRupayTTUM");
	logger.error(" error in GenerateRupayTTUMDaoImpl.GenerateRupayTTUM", new Exception("GenerateRupayTTUMDaoImpl.GenerateRupayTTUM",e));
	 throw e;
}
finally{
	if(rset_200!=null){
		rset_200.close();
	}
	if(pstmt_200!=null){
		pstmt_200.close();
	}
	if(rset_262!=null){
		rset_262.close();
	}
	if(pstmt_262!=null){
		pstmt_262.close();
	}
	
	if(rset_con_200!=null){
		rset_con_200.close();
	}		
	if(pstmt_con_200!=null){
		pstmt_con_200.close();
	}
	if(rset_con_262!=null){
		rset_con_262.close();
	}
	if(pstmt_con_262!=null){
		pstmt_con_262.close();
	}
	
	if(conn!=null){
		conn.close();
	}
}

return Data;



}




@Override
public boolean IssurttumAlreadyGenrated(GenerateTTUMBean generatettumBean) throws Exception{
	
	String datediff =" Select to_date(MAX(FILEDATE),'DD/MM/RRRR')  - to_date('"+generatettumBean.getStDate()+"', 'DD/MM/RRRR' )  from settlement_rupay_SWITCH where dcrs_remarks like '%"+generatettumBean.getStSubCategory().substring(0, 3)+"%' " ;
	System.out.println(datediff);
	int dateDiff = 	getJdbcTemplate().queryForObject(datediff, Integer.class);
	

	
	String CHECK_QUERY = "";
	
	if(dateDiff > 3){
		CHECK_QUERY = "SELECT COUNT(1) from SETTLEMENT_RUPAY_SWITCH_BK   WHERE TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generatettumBean.getStDate()+"' AND "
				+ " DCRS_REMARKS = '" +generatettumBean.getStMerger_Category()+"-UNRECON-GENERATED-TTUM-3' " ;
				 
	
	}else{
		CHECK_QUERY = "SELECT COUNT(1) from SETTLEMENT_RUPAY_SWITCH  WHERE TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generatettumBean.getStDate()+"' AND "
				+ " DCRS_REMARKS = '" +generatettumBean.getStMerger_Category()+"-UNRECON-GENERATED-TTUM-3' " ;
			 
	}
	
	logger.info("***** CHECK_QUERY  **** "+CHECK_QUERY );
	
	int count = 	getJdbcTemplate().queryForObject(CHECK_QUERY, Integer.class);
	
	logger.info("***** CHECK_QUERY COUNT  **** "+count );
	
	if(count > 0) return true;
	
	
	return false;
}

@Override
public boolean cleanAlreadyProcessedSURTTUMRecords(GenerateTTUMBean generatettumBean)throws Exception{
	
	String datediff =" Select to_date(MAX(FILEDATE),'DD/MM/RRRR')  - to_date('"+generatettumBean.getStDate()+"', 'DD/MM/RRRR' )  from SETTLEMENT_RUPAY_SWITCH where dcrs_remarks like '%"+generatettumBean.getStSubCategory().substring(0, 3)+"%' " ;
	int dateDiff = 	getJdbcTemplate().queryForObject(datediff, Integer.class);
	
	String UPDATE_QUERY = "" ;
	System.out.println("dateDiff ::> "+dateDiff);
	if(dateDiff > 3){
		 UPDATE_QUERY = "UPDATE SETTLEMENT_RUPAY_SWITCH_BK SET DCRS_REMARKS = '"+generatettumBean.getStCategory()+"_"+generatettumBean.getStSubCategory().substring(0, 3)+
			"-UNRECON-GENERATE-TTUM-"+3+"' WHERE  TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generatettumBean.getStDate()+"' AND "
					+ " DCRS_REMARKS = '" +generatettumBean.getStMerger_Category()+"-UNRECON-GENERATED-TTUM-"+3 + "' ";
	}else{
		 UPDATE_QUERY = "UPDATE SETTLEMENT_RUPAY_SWITCH SET DCRS_REMARKS = '"+generatettumBean.getStCategory()+"_"+generatettumBean.getStSubCategory().substring(0, 3)+
			"-UNRECON-GENERATE-TTUM-"+3+"' WHERE  TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generatettumBean.getStDate()+"' AND "
					+ " DCRS_REMARKS = '" +generatettumBean.getStMerger_Category()+"-UNRECON-GENERATED-TTUM-"+3 + "' ";
	}

	logger.info("***** UPDATE_QUERY  **** "+UPDATE_QUERY );
	
	
int updatedCount =	getJdbcTemplate().update(UPDATE_QUERY);
	
logger.info("***** UPDATE_COUNT  **** "+updatedCount );




String DELETE_TTUM_QUERY = "DELETE FROM TTUM_"+generatettumBean.getStMerger_Category()
		+"_"+generatettumBean.getStFile_Name() 
		+" where RECORDS_DATE = TO_DATE('"+generatettumBean.getStDate()+"','DD/MM/YYYY')" ;

logger.info("*****OLD DELETE_TTUM_QUERY  **** "+DELETE_TTUM_QUERY  );

getJdbcTemplate().update(DELETE_TTUM_QUERY);

String DELETE_TRACK_QUERY = "DELETE FROM MAIN_TRACK_TXN where FILEDATE = " +
				"TO_DATE('"+generatettumBean.getStDate()+"','DD/MM/YYYY')";

logger.info("*****OLD DELETE_TTUM_QUERY  **** "+DELETE_TRACK_QUERY  );

getJdbcTemplate().update(DELETE_TRACK_QUERY);


	
	
	return true;
}




//GENERATE TTUM FOR REPORT E
public void getReportERecords(GenerateTTUMBean generatettumBeanObj)throws Exception
{
	logger.info("***** GenerateRupayTTUMDaoImpl.getReportERecords Start ****");
	String UPDATE_QUERY = "";
	try
	{
		
		
		String datediff =" Select to_date(MAX(FILEDATE),'DD/MM/RRRR')  - to_date('"+generatettumBeanObj.getStDate()+"', 'DD/MM/RRRR' )  from SETTLEMENT_"+generatettumBeanObj.getStCategory()+"_"+generatettumBeanObj.getStFile_Name()+" where dcrs_remarks like '%"+generatettumBeanObj.getStSubCategory().substring(0, 3)+"%' " ;
	int dateDiff = 	getJdbcTemplate().queryForInt(datediff);
	
	System.out.println("dateDiff ::> "+dateDiff );
	if(dateDiff > 3){
		UPDATE_QUERY = "UPDATE SETTLEMENT_"+generatettumBeanObj.getStCategory()+"_"+generatettumBeanObj.getStFile_Name()+"_BK "
				+" SET DCRS_REMARKS = '"+generatettumBeanObj.getStCategory()+"_"+generatettumBeanObj.getStSubCategory().substring(0, 3)
				+"-UNRECON-GENERATE-TTUM-3' WHERE DCRS_REMARKS = '"+generatettumBeanObj.getStCategory()+"_"+generatettumBeanObj.getStSubCategory().substring(0, 3)+
				"-UNRECON-3' AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generatettumBeanObj.getStDate()+"'";
	}else{
		UPDATE_QUERY = "UPDATE SETTLEMENT_"+generatettumBeanObj.getStCategory()+"_"+generatettumBeanObj.getStFile_Name()
				+" SET DCRS_REMARKS = '"+generatettumBeanObj.getStCategory()+"_"+generatettumBeanObj.getStSubCategory().substring(0, 3)
				+"-UNRECON-GENERATE-TTUM-3' WHERE DCRS_REMARKS = '"+generatettumBeanObj.getStCategory()+"_"+generatettumBeanObj.getStSubCategory().substring(0, 3)+
				"-UNRECON-3' AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generatettumBeanObj.getStDate()+"'";
	}
	
	

					
		logger.info("UPDATE_QUERY=="+UPDATE_QUERY);
		getJdbcTemplate().execute(UPDATE_QUERY);
		
		logger.info("***** GenerateRupayTTUMDaoImpl.getReportERecords End ****");
		
	}
	catch(Exception e)
	{
		demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.getReportERecords");
		logger.error(" error in GenerateRupayTTUMDaoImpl.getReportERecords", new Exception("GenerateRupayTTUMDaoImpl.getReportERecords",e));
		 throw e;
	}
}

//GET SURCHARGE RECORDS FOR TTUM
@Override
public void getSurchargeRecords(GenerateTTUMBean generateTTUMBeanObj)throws Exception
{
	logger.info("***** GenerateRupayTTUMDaoImpl.getSurchargeRecords Start ****");
	String UPDATE_QUERY = "";
	try
	{
		if(generateTTUMBeanObj.getStDate() != null && !generateTTUMBeanObj.getStDate().equals(""))
		{
			
			
			String datediff =" Select to_date(MAX(FILEDATE),'DD/MM/RRRR')  - to_date('"+generateTTUMBeanObj.getStDate()+"', 'DD/MM/RRRR' )  from settlement_rupay_cbs where dcrs_remarks like '%"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)+"%' " ;
		int dateDiff = 	getJdbcTemplate().queryForInt(datediff);
			
		if(dateDiff > 3){
			
			UPDATE_QUERY = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()+"_BK "+
					" SET DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)+
					"-UNRECON-GENERATE-TTUM-3' WHERE DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
					+"-UNRECON-3'"
					+" AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"'";
					//+" AND VALUE_DATE = TO_CHAR('"+generateTTUMBeanObj.getStDate()+"','DD-MM-YYYY')";
		}else{
			
			UPDATE_QUERY = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()+ 
					" SET DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)+
					"-UNRECON-GENERATE-TTUM-3' WHERE DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
					+"-UNRECON-3'"
					+" AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"'";
					//+" AND VALUE_DATE = TO_CHAR('"+generateTTUMBeanObj.getStDate()+"','DD-MM-YYYY')";
		}
			
		}
		else
		{

			UPDATE_QUERY = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()+"_BK "+
					" SET DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)+
					"-UNRECON-GENERATE-TTUM-3' WHERE DCRS_REMARKS ='"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
					+"-UNRECON-3' AND "					
					+"VALUE_DATE = TO_CHAR(SYSDATE-10,'DD/MM/YYYY')";
		
		}
			
		logger.info("UPDATE_QUERY=="+UPDATE_QUERY);
		getJdbcTemplate().execute(UPDATE_QUERY);
		
		logger.info("***** GenerateRupayTTUMDaoImpl.getSurchargeRecords End ****");
	}
	catch(Exception e)
	{
		demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.getSurchargeRecords");
		logger.error(" error in GenerateRupayTTUMDaoImpl.getSurchargeRecords", new Exception("GenerateRupayTTUMDaoImpl.getSurchargeRecords",e));
		 throw e;
	}
}

//ttum for matched records
public List<List<GenerateTTUMBean>> getMatchedRecordsTTUM(GenerateTTUMBean generateTTUMbean)throws Exception
{
	logger.info("***** GenerateRupayTTUMDaoImpl.getMatchedRecordsTTUM Start ****");
	
	String EQUAL_AMT_DATA = "";
	String VARIATION_POSITIVE = "";
	String VARIATION_NEGATIVE = "";
	List<GenerateTTUMBean> TTUM_Data = new ArrayList<>();
	List<GenerateTTUMBean> TTUM_D_Data = new ArrayList<>();
	List<GenerateTTUMBean> TTUM_C_Data = new ArrayList<>();
	List<List<GenerateTTUMBean>> Data = new ArrayList<>();
	List<String> ExcelHeaders = new ArrayList<>();
	List<GenerateTTUMBean> Excel_Header = new ArrayList<>();
	String table_cols = "DCRS_REMARKS VARCHAR (100 BYTE), CREATEDDATE DATE , CREATEDBY VARCHAR (100 BYTE),RECORDS_DATE DATE";
	String insert_cols = "DCRS_REMARKS,CREATEDDATE, CREATEDBY,RECORDS_DATE";
	
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rset = null;
	
	try
	{
		
		
		ExcelHeaders.add("ACCOUNT NUMBER");
		ExcelHeaders.add("CURRENCY CODE");
		ExcelHeaders.add("SERVICE OUTLET");
		ExcelHeaders.add("PART TRAN TYPE");
		ExcelHeaders.add("TRANSACTION AMOUNT");
		ExcelHeaders.add("TRANSACTION PARTICULARS");
		ExcelHeaders.add("REFERENCE CURRENCY CODE");
		ExcelHeaders.add("REFERENCE AMOUNT");
		ExcelHeaders.add("REMARKS");
		ExcelHeaders.add("REFERENCE NUMBER");
		ExcelHeaders.add("ACCOUNT REPORT CODE");
		
		
		generateTTUMbean.setStExcelHeader(ExcelHeaders);
		
		Excel_Header.add(generateTTUMbean);
		
		Data.add(Excel_Header);
		
		
		// CREATE NEW TABLE FOR INSERTING TTUM ENTRIES
				for(int i = 0 ; i<ExcelHeaders.size();i++)
				{
					table_cols =table_cols+","+ ExcelHeaders.get(i)+" VARCHAR (100 BYTE)";
					insert_cols = insert_cols+","+ExcelHeaders.get(i);
				}
				
				String CHECK_TABLE = "SELECT count (*) FROM tab WHERE tname  = 'TTUM_"+generateTTUMbean.getStCategory()+"_"+generateTTUMbean.getStSubCategory().substring(0, 3)
						+"_"+generateTTUMbean.getStFile_Name()+"'";
				int tableExist = getJdbcTemplate().queryForObject(CHECK_TABLE, new Object[] { },Integer.class);
				
				logger.info("CHECK_TABLE=="+CHECK_TABLE);
				logger.info("tableExist=="+tableExist);
				
				if(tableExist == 0)
				{
					String CREATE_QUERY = "CREATE TABLE TTUM_"+generateTTUMbean.getStCategory()+"_"+generateTTUMbean.getStSubCategory().substring(0, 3)
							+"_"+generateTTUMbean.getStFile_Name()+" ("+table_cols+")";
					
					logger.info("CREATE_QUERY=="+CREATE_QUERY);
					getJdbcTemplate().execute(CREATE_QUERY);
				}
		
		
		
		
		//VARIATION IS 0
		EQUAL_AMT_DATA = "SELECT T2.FORACID,T2.AMOUNT,TO_CHAR(TO_DATE(T1.LOCAL_DATE,'MM/DD/YYYY'),'DDMMYY') AS LOCAL_DATE," +
				" SUBSTR(T2.REF_NO,2,6) AS REF_NO,T1.PAN," +
				"T1.ACCEPTORNAME,TO_CHAR(T2.CREATEDDATE,'DDMMYY') AS FILE_PROCESS_DT FROM SETTLEMENT_RUPAY_SWITCH T1, SETTLEMENT_RUPAY_CBS T2, SETTLEMENT_RUPAY_RUPAY T3" +
				" WHERE T1.DCRS_REMARKS = 'RUPAY_SUR-MATCHED-3' AND T2.DCRS_REMARKS = 'RUPAY_SUR-MATCHED-3'"
				+" AND T3.DCRS_REMARKS = 'RUPAY_DOM-MATCHED-2' AND T1.PAN = T3.PRIMARY_ACCOUNT_NUMBER AND T1.AUTHNUM = T3.APPROVAL_CODE " +
				"AND T1.ISSUER = SUBSTR(T3.ACQUIRER_REFERENCE_DATA,11,12)"
				+" AND T1.PAN = T2.REMARKS AND SUBSTR(T1.TRACE,2,6) = SUBSTR(T2.REF_NO,2,6) AND T1.MERCHANT_TYPE = T2.PARTICULARALS2"
				+" AND (TO_NUMBER(T1.AMOUNT,'9999999999.99')+TO_NUMBER(T2.AMOUNT,'9999999999.99')) = T3.AMOUNT_TRANSACTION " +
				" AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMbean.getStDate()+"'";
		
		logger.info("EQUAL_AMT_DATA=="+EQUAL_AMT_DATA);
		
		conn = getConnection();
		pstmt = conn.prepareStatement(EQUAL_AMT_DATA);
		rset = pstmt.executeQuery();
		float credit_amount = 0.0f;
		String stCredit_tranparticulars = "";
		while(rset.next())
		{
			//SINGLE CR AND MULITPLE DEBIT 
			GenerateTTUMBean generatettumBeanObj = new GenerateTTUMBean();
			
			//SURCHARGE ACC DEBIT
			generatettumBeanObj.setStDebitAcc(rset.getString("FORACID"));
			//RUPAY GL ACC CREDIT
			/*generatettumBeanObj.setStCreditAcc("99937200010660");*/
			credit_amount = credit_amount + Float.parseFloat(rset.getString("AMOUNT"));
			generatettumBeanObj.setStAmount(rset.getString("AMOUNT"));
			//String stTran_particulars = "REV/"+generateTTUMbean.getStCategory()+"/"+rset.getString("LOCAL_DATE")+"/"+rset.getString("REF_NO");
			
			String stTran_particulars = "RPAY-SUR-"+rset.getString("LOCAL_DATE")+"/"+rset.getString("REF_NO")+rset.getString("ACCEPTORNAME");
			generatettumBeanObj.setStDate(rset.getString("LOCAL_DATE"));
			
			stCredit_tranparticulars = "RPAY-SURCHARGE-"+rset.getString("FILE_PROCESS_DT");
			
			generatettumBeanObj.setStTran_particulars(stTran_particulars);
			generatettumBeanObj.setStCard_Number(rset.getString("PAN"));
			String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
			generatettumBeanObj.setStRemark(remark);
			TTUM_Data.add(generatettumBeanObj);
			
		}
		logger.info("for VARIATION 0 CREDIT AMOUNT IS "+credit_amount);
		//INSERT TTUM ENTRIES IN ABOVE CREATED TABLE
		for(int i = 0;i<TTUM_Data.size();i++)
		{
			//Multiple Debit entries in SURCHARGE GL
			GenerateTTUMBean beanObj = new GenerateTTUMBean();
			beanObj = TTUM_Data.get(i);
			String INSERT_QUERY = "INSERT INTO TTUM_"+generateTTUMbean.getStCategory()+"_"+generateTTUMbean.getStSubCategory().substring(0, 3)
					+"_"+generateTTUMbean.getStFile_Name() 
					+"("+insert_cols+") VALUES ('"+generateTTUMbean.getStCategory()+"_"+generateTTUMbean.getStSubCategory().substring(0, 3)
					+"-MATCHED-TTUM-3"+
					"',SYSDATE,'"+generateTTUMbean.getStEntry_by()+"',TO_DATE('"+beanObj.getStDate()+"','DDMMYY'),'"+beanObj.getStDebitAcc()+"','INR','999','D','"+
					beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','"+beanObj.getStCard_Number()+"','INR','"+beanObj.getStAmount()+"','"+
					beanObj.getStRemark()+"')";
			
			logger.info("INSERT_QUERY=="+INSERT_QUERY);
			getJdbcTemplate().execute(INSERT_QUERY);

			//SINGLE CREDIT ENTRY IN RUPAY GL ACC
			if(i==(TTUM_Data.size()-1))
			{
				
				INSERT_QUERY = "INSERT INTO TTUM_"+generateTTUMbean.getStCategory()+"_"+generateTTUMbean.getStSubCategory().substring(0, 3)
						+"_"+generateTTUMbean.getStFile_Name() 
						+"("+insert_cols+") VALUES ('"+generateTTUMbean.getStCategory()+"_"+generateTTUMbean.getStSubCategory().substring(0, 3)
						+"-MATCHED-TTUM-3"
						+"',SYSDATE,'"+generateTTUMbean.getStEntry_by()+"',TO_DATE('"+beanObj.getStDate()+"','DDMMYY'),'"+"'99937200010660'"+"','INR','999','C','"+
						credit_amount+"','"+stCredit_tranparticulars+"','"+"SURCHARGE CHARGED AND CLAIM RECEIVED"+"','INR','"+credit_amount+"','"+
						beanObj.getStRemark()+"')";
				
				logger.info("INSERT_QUERY=="+INSERT_QUERY);
				getJdbcTemplate().execute(INSERT_QUERY);
			}
		}


		
		Data.add(TTUM_Data);
		
		pstmt = null;
		rset = null;
		
		
		//SUM OF AMOUNT IN SWITCH AND SURCHARGE IS > RUPAY AMOUNT (i.e +ve)
		VARIATION_POSITIVE = "SELECT T2.FORACID,TO_CHAR(TO_DATE(T1.LOCAL_DATE,'MM/DD/YYYY'),'DDMMYY') AS LOCAL_DATE, SUBSTR(T2.REF_NO,2,6) AS REF_NO,T1.ISSUER,"
				+"T1.CONTRA_ACCOUNT,TO_NUMBER(T3.AMOUNT_TRANSACTION,'9999999999.99') AS AMOUNT3 , TO_NUMBER(T1.AMOUNT,'9999999999.99') AS AMOUNT1," +
				" TO_NUMBER(T2.AMOUNT,'9999999999.99') AS AMOUNT2,T1.ACCEPTORNAME,TO_CHAR(T2.CREATEDDATE,'DDMMYY') AS FILE_PROCESS_DT "
				+" FROM SETTLEMENT_RUPAY_SWITCH T1, SETTLEMENT_RUPAY_CBS T2, SETTLEMENT_RUPAY_RUPAY T3 WHERE T1.DCRS_REMARKS = 'RUPAY_SUR-MATCHED-3' AND T2.DCRS_REMARKS = 'RUPAY_SUR-MATCHED-3'"
				+" AND T3.DCRS_REMARKS = 'RUPAY_DOM-MATCHED-2' AND T1.PAN = T3.PRIMARY_ACCOUNT_NUMBER AND T1.AUTHNUM = T3.APPROVAL_CODE AND T1.ISSUER = SUBSTR(T3.ACQUIRER_REFERENCE_DATA,11,12)"
				+" AND T1.PAN = T2.REMARKS AND SUBSTR(T1.TRACE,2,6) = SUBSTR(T2.REF_NO,2,6) AND T1.MERCHANT_TYPE = T2.PARTICULARALS2"
				+" AND (TO_NUMBER(T1.AMOUNT,'9999999999.99')+TO_NUMBER(T2.AMOUNT,'9999999999.99')) > T3.AMOUNT_TRANSACTION " +
				" AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMbean.getStDate()+"'";
		
		logger.info("VARIATION_POSITIVE=="+VARIATION_POSITIVE);
		
		pstmt = conn.prepareStatement(VARIATION_POSITIVE);
		rset = pstmt.executeQuery();
		credit_amount = 0.0f;
		String credit_remark = "";
		String stDate ="";
		while(rset.next())
		{
			//Debit RUPAY SURCHARGE ACC
			GenerateTTUMBean generateTTUMBeanObj = new GenerateTTUMBean();
			generateTTUMBeanObj.setStDebitAcc(rset.getString("FORACID"));
			generateTTUMBeanObj.setStAmount(rset.getString("AMOUNT2"));
			//String stTran_particulars = "REV/"+generateTTUMbean.getStCategory()+"/"+rset.getString("LOCAL_DATE")+"/"+rset.getString("REF_NO");
			String stTran_particulars = "RPAY-SUR-"+rset.getString("LOCAL_DATE")+"/"+rset.getString("REF_NO")+"-"+rset.getString("ACCEPTORNAME");
			generateTTUMBeanObj.setStDate(rset.getString("LOCAL_DATE"));
			generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
			generateTTUMBeanObj.setStCard_Number(rset.getString("PAN"));
			String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
			generateTTUMBeanObj.setStRemark(remark);
			TTUM_D_Data.add(generateTTUMBeanObj);
			
			
			//credit RUPAY GL ACC part 
				// RUPAY GL ACCOUNT CR
			//SINGLE CREDIT ENTRY
			
			/*GenerateTTUMBean generateTTUMBeanObj2 = new GenerateTTUMBean();
			generateTTUMBeanObj2.setStCreditAcc("99937200010660");*/
			credit_amount = credit_amount+( Float.parseFloat(rset.getString("AMOUNT3")) - Float.parseFloat(rset.getString("AMOUNT1")));
			stCredit_tranparticulars = "RPAY-SURCHARGE-"+rset.getString("FILE_PROCESS_DT");
			credit_remark = remark;
			stDate = rset.getString("LOCAL_DATE");
			/*generateTTUMBeanObj2.setStAmount(credit_amount+"");
			generateTTUMBeanObj2.setStTran_particulars(stTran_particulars);
			generateTTUMBeanObj2.setStCard_Number(rset.getString("PAN"));
			generateTTUMBeanObj2.setStRemark(remark);
			TTUM_C_Data.add(generateTTUMBeanObj2);*/
			
				//MULTIPLE CR IN CUST ACC 
			GenerateTTUMBean generateTTUMBeanObj3 = new GenerateTTUMBean();
			generateTTUMBeanObj3.setStCreditAcc(rset.getString("CONTRA_ACCOUNT"));
			Float increditamt_2 = (Float.parseFloat(rset.getString("AMOUNT1")) + Float.parseFloat(rset.getString("AMOUNT2")))-Float.parseFloat(rset.getString("AMOUNT3"));
			generateTTUMBeanObj3.setStAmount(increditamt_2+"");
			String stTran_particulars3 = "REV-RPAY-SURCHARGE-"+rset.getString("LOCAL_DATE");//+"/"+rset.getString("REF_NO")+"-"+rset.getString("ACCEPTORNAME");
			generateTTUMBeanObj3.setStTran_particulars(stTran_particulars3);
			generateTTUMBeanObj3.setStDate(rset.getString("LOCAL_DATE"));
			generateTTUMBeanObj3.setStCard_Number(rset.getString("PAN"));
			generateTTUMBeanObj3.setStRemark(remark);
			TTUM_C_Data.add(generateTTUMBeanObj3);
			
		}

		//INSERT TTUM ENTRIES IN ABOVE CREATED TABLE
		for(int i = 0;i<TTUM_D_Data.size();i++)
		{
			GenerateTTUMBean beanObj = new GenerateTTUMBean();
			beanObj = TTUM_D_Data.get(i);
			String INSERT_QUERY = "INSERT INTO TTUM_"+generateTTUMbean.getStCategory()+"_"+generateTTUMbean.getStSubCategory().substring(0, 3)
					+"_"+generateTTUMbean.getStFile_Name() 
					+"("+insert_cols+") VALUES ('"+generateTTUMbean.getStCategory()+"_"+generateTTUMbean.getStSubCategory().substring(0, 3)
					+"-MATCHED-TTUM-3"					
					+"',SYSDATE,'"+generateTTUMbean.getStEntry_by()+"',TO_DATE('"+beanObj.getStDate()+"','DDMMYY'),'"+beanObj.getStDebitAcc()+"','INR','999','D','"+
					beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','"+beanObj.getStCard_Number()+"','INR','"+beanObj.getStAmount()+"','"+
					beanObj.getStRemark()+"')";
			
			logger.info("INSERT_QUERY=="+INSERT_QUERY);
			getJdbcTemplate().execute(INSERT_QUERY);	
		}
		//SINGLE RUPAY GL ACC
		/*for(int i = 0;i<TTUM_C_Data.size();i++)
		{*/
		//	GenerateTTUMBean beanObj = new GenerateTTUMBean();
//			beanObj = TTUM_C_Data.get(i);

			
			 String CR_INSERT_QUERY = "INSERT INTO TTUM_"+generateTTUMbean.getStCategory()+"_"+generateTTUMbean.getStSubCategory().substring(0, 3)
					 +"_"+generateTTUMbean.getStFile_Name() 
						+"("+insert_cols+") VALUES ('"+generateTTUMbean.getStCategory()+"_"+generateTTUMbean.getStSubCategory().substring(0, 3)
						+"-MATCHED-TTUM-3"
						+"',SYSDATE,'"+generateTTUMbean.getStEntry_by()+"',TO_DATE('"+stDate+"','DDMMYY'),'99937200010660','INR','999','C','"+
						credit_amount+"','"+stCredit_tranparticulars+"','SURCHARGE CHARGED AND CLAIM RECEIVED','INR','"+credit_amount+"','"+
						credit_remark+"')";
			 
			 logger.info("CR_INSERT_QUERY=="+CR_INSERT_QUERY);
			 getJdbcTemplate().execute(CR_INSERT_QUERY);		
		//}

		
		
		pstmt = null;
		rset = null;
		
		//SUM OF AMOUNT IN SWITCH AND SURCHARGE IS < RUPAY AMOUNT
		VARIATION_NEGATIVE = "SELECT T2.FORACID,TO_CHAR(TO_DATE(T1.LOCAL_DATE,'MM/DD/YYYY'),'DDMMYY') AS LOCAL_DATE, SUBSTR(T2.REF_NO,2,6) AS REF_NO,T1.ISSUER,"
				+"T1.CONTRA_ACCOUNT,TO_NUMBER(T3.AMOUNT_TRANSACTION,'9999999999.99') AS AMOUNT3 , TO_NUMBER(T1.AMOUNT,'9999999999.99') AS AMOUNT1," +
				" TO_NUMBER(T2.AMOUNT,'9999999999.99') AS AMOUNT2,T1.ACCEPTORNAME,TO_CHAR(T2.CREATEDDATE,'DDMMYY') AS FILE_PROCESS_DT "
				+" FROM SETTLEMENT_RUPAY_SWITCH T1, SETTLEMENT_RUPAY_CBS T2, SETTLEMENT_RUPAY_RUPAY T3 WHERE T1.DCRS_REMARKS = 'RUPAY_SUR-MATCHED-3' AND T2.DCRS_REMARKS = 'RUPAY_SUR-MATCHED-3'"
				+" AND T3.DCRS_REMARKS = 'RUPAY_DOM-MATCHED-2' AND T1.PAN = T3.PRIMARY_ACCOUNT_NUMBER AND T1.AUTHNUM = T3.APPROVAL_CODE AND T1.ISSUER = SUBSTR(T3.ACQUIRER_REFERENCE_DATA,11,12)"
				+" AND T1.PAN = T2.REMARKS AND SUBSTR(T1.TRACE,2,6) = SUBSTR(T2.REF_NO,2,6) AND T1.MERCHANT_TYPE = T2.PARTICULARALS2"
				+" AND (TO_NUMBER(T1.AMOUNT,'9999999999.99')+TO_NUMBER(T2.AMOUNT,'9999999999.99')) < T3.AMOUNT_TRANSACTION " +
				" AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMbean.getStDate()+"'";
		
		logger.info("VARIATION_NEGATIVE=="+VARIATION_NEGATIVE);
		
		pstmt = conn.prepareStatement(VARIATION_NEGATIVE);
		rset = pstmt.executeQuery();
		credit_amount = 0.0f;
		credit_remark = "";
		String stTran_particulars3 = "";
		while(rset.next())
		{
			//MULTIPLE ENTRIES FOR RUPAY SURCHARGE ACC DR
			GenerateTTUMBean generateTTUMBeanObj = new GenerateTTUMBean();
			generateTTUMBeanObj.setStDebitAcc(rset.getString("FORACID"));
			generateTTUMBeanObj.setStAmount(rset.getString("AMOUNT2"));
			//String stTran_particulars = "REV/"+generateTTUMbean.getStCategory()+"/"+rset.getString("LOCAL_DATE")+"/"+rset.getString("REF_NO");
			//Tran Particular �RPAY-SUR-DDMMYY (Transaction date)-Trace number-Merchant name
			String stTran_particulars1 = "RPAY-SUR-"+rset.getString("LOCAL_DATE")+"-"+rset.getString("REF_NO")+"-"+rset.getString("ACCEPTORNAME");
			generateTTUMBeanObj.setStTran_particulars(stTran_particulars1);
			generateTTUMBeanObj.setStDate(rset.getString("LOCAL_DATE"));
			generateTTUMBeanObj.setStCard_Number(rset.getString("PAN"));
			String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
			generateTTUMBeanObj.setStRemark(remark);
			TTUM_D_Data.add(generateTTUMBeanObj);
			
			
			//MULTIPLE ENTRIES FOR CUST ACC DR
			GenerateTTUMBean generateTTUMBeanObj2 = new GenerateTTUMBean();
			generateTTUMBeanObj2.setStDebitAcc(rset.getString("CONTRA_ACCOUNT"));
			//Tran Particular �DR-RPAY-SURCHARGE-DDMMYY (Transaction date)
			String stTran_particulars2 = "DR-RPAY-SURCHARGE-"+rset.getString("LOCAL_DATE");
			
			generateTTUMBeanObj2.setStTran_particulars(stTran_particulars2);
			generateTTUMBeanObj2.setStDate(rset.getString("LOCAL_DATE"));
			Float custAmt = Float.parseFloat(rset.getString("AMOUNT3")) - (Float.parseFloat(rset.getString("AMOUNT1"))+Float.parseFloat(rset.getString("AMOUNT2")));
			generateTTUMBeanObj2.setStAmount(custAmt+"");
			generateTTUMBeanObj2.setStCard_Number(rset.getString("PAN"));
			generateTTUMBeanObj2.setStRemark(remark);
			TTUM_D_Data.add(generateTTUMBeanObj2);
			
			
			//SINGLE ENTRY FOR RUPAY GL CREDIT
			/*GenerateTTUMBean generateTTUMBeanObj3 = new GenerateTTUMBean();
			generateTTUMBeanObj3.setStCreditAcc("99937200010660");*/
			//Tran Particular �RPAY-SURCHARGE-DDMMYY (File process date)
			stTran_particulars3 = "DR-RPAY-SURCHARGE-"+rset.getString("FILE_PROCESS_DT");
			credit_remark = remark;
			//generateTTUMBeanObj3.setStTran_particulars(stTran_particulars3);
			Float RupayGlAmt =credit_amount+(Float.parseFloat(rset.getString("AMOUNT3"))-Float.parseFloat(rset.getString("AMOUNT1")));
			stDate = rset.getString("LOCAL_DATE");
			/*generateTTUMBeanObj3.setStAmount(RupayGlAmt+"");
			generateTTUMBeanObj3.setStCard_Number(rset.getString("PAN"));
			generateTTUMBeanObj3.setStRemark(remark);
			TTUM_C_Data.add(generateTTUMBeanObj3);*/
			
			
			
		}
		
		for(int i = 0;i<TTUM_D_Data.size();i++)
		{
			GenerateTTUMBean beanObj = new GenerateTTUMBean();
			beanObj = TTUM_D_Data.get(i);
			String INSERT_QUERY = "INSERT INTO TTUM_"+generateTTUMbean.getStCategory()+"_"+generateTTUMbean.getStSubCategory().substring(0, 3)
						+"_"+generateTTUMbean.getStFile_Name() 
						+"("+insert_cols+") VALUES ('"+generateTTUMbean.getStCategory()+"_"+generateTTUMbean.getStSubCategory().substring(0, 3)
						+"-MATCHED-TTUM-3"
						+"',SYSDATE,'"+generateTTUMbean.getStEntry_by()+"',TO_DATE('"+beanObj.getStDate()+"','DDMMYY'),'"+beanObj.getStDebitAcc()+"','INR','999','D','"+
						beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','"+beanObj.getStCard_Number()+"','INR','"+beanObj.getStAmount()+"','"+
						beanObj.getStRemark()+"')";
			
			logger.info("INSERT_QUERY=="+INSERT_QUERY);
			getJdbcTemplate().execute(INSERT_QUERY);	
		}
		
		/*for(int i = 0;i<TTUM_C_Data.size();i++)
		{
			GenerateTTUMBean beanObj = new GenerateTTUMBean();
			beanObj = TTUM_C_Data.get(i);*/
				
			CR_INSERT_QUERY = "INSERT INTO TTUM_"+generateTTUMbean.getStCategory()+"_"+generateTTUMbean.getStSubCategory().substring(0, 3)
					 +"_"+generateTTUMbean.getStFile_Name() 
						+"("+insert_cols+") VALUES ('"+generateTTUMbean.getStCategory()+"_"+generateTTUMbean.getStSubCategory().substring(0, 3)
						+"-MATCHED-TTUM-3"+
						"',SYSDATE,'"+generateTTUMbean.getStEntry_by()+"',TO_DATE('"+stDate+"','DDMMYY'),'99937200010660','INR','999','C','"+
						credit_amount+"','"+stTran_particulars3+"','SURCHARGE CHARGED AND CLAIM RECEIVED','INR','"+credit_amount+"','"+
						credit_remark+"')";
			
			logger.info("CR_INSERT_QUERY=="+CR_INSERT_QUERY);
			 getJdbcTemplate().execute(CR_INSERT_QUERY);		
		//}
		
		
		
		//update THESE RECORDS
		String UPDATE_QUERY = "UPDATE SETTLEMENT_"+generateTTUMbean.getStCategory()+"_"+generateTTUMbean.getStFile_Name()
				+" SET DCRS_REMARKS = '"+generateTTUMbean.getStCategory()+"_"+generateTTUMbean.getStSubCategory().substring(0, 3)
				+"-MATCHED-GENERATED-TTUM-3'"+//+generateTTUMbean.getInRec_Set_Id()+"'"+
				" WHERE DCRS_REMARKS = '"+generateTTUMbean.getStCategory()+"_"+generateTTUMbean.getStSubCategory().substring(0, 3)
				+"-MATCHED-3'";
				//"+generateTTUMbean.getInRec_Set_Id()+"'";
		
		logger.info("UPDATE_QUERY=="+UPDATE_QUERY);
		getJdbcTemplate().execute(UPDATE_QUERY);

		
		Data.add(TTUM_C_Data);
		Data.add(TTUM_D_Data);
		
		
		logger.info("***** GenerateRupayTTUMDaoImpl.getMatchedRecordsTTUM End ****");
		
		
	}
	catch(Exception e)
	{
		demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.getMatchedRecordsTTUM");
		logger.error(" error in GenerateRupayTTUMDaoImpl.getMatchedRecordsTTUM", new Exception("GenerateRupayTTUMDaoImpl.getMatchedRecordsTTUM",e));
		 throw e;
	}
	finally{
		if(rset!=null){
			rset.close();
		}
		if(pstmt!=null){
			pstmt.close();
		}
		if(conn!=null){
			conn.close();
		}
	}
	return Data;
}


public List<List<GenerateTTUMBean>> getMatchedIntTxn(GenerateTTUMBean generateTTUMBeanObj,int inRec_Set_Id)throws Exception
{
	logger.info("***** GenerateRupayTTUMDaoImpl.getMatchedIntTxn Start ****");
	
	List<GenerateTTUMBean> TTUM_Data = new ArrayList<>();
	List<List<GenerateTTUMBean>> Data = new ArrayList<>();
	List<GenerateTTUMBean> TTUM_D_Data = new ArrayList<>();
	List<GenerateTTUMBean> TTUM_C_Data = new ArrayList<>();
	
	Connection conn =null;
	PreparedStatement pstmt = null;
	ResultSet rset =null;
	try
	{
		String GET_DATA = "SELECT PAN,TO_CHAR(TO_DATE(LOCAL_DATE,'MM/DD/YYYY'),'DDMMYY') AS LOCAL_DATE,SUBSTR(TRACE,-6,6) AS TRACE" +
				",DIFF_AMOUNT,ACCEPTORNAME FROM SETTLEMENT_RUPAY_SWITCH WHERE RELAX_PARAM = 'Y' AND DCRS_REMARKS = '"+generateTTUMBeanObj.getStMerger_Category()+"_"
				+"MATCHED-"+inRec_Set_Id+"'";
		
		logger.info("GET_DATA=="+GET_DATA);
		conn = getConnection();
		pstmt = conn.prepareStatement(GET_DATA);
		rset = pstmt.executeQuery();
		float inCredit_Amt = 0.00f;
		float inDebit_Amt = 0.00f;
		String stTran_parti1 = "";
		String stInt_Remarks = "";
		String stTran_Parti2 = "";
		String stInt_Remarks2 = "";
		while(rset.next())
		{
			int inAmt = Integer.parseInt(rset.getString("DIFF_AMOUNT"));
			
			if(inAmt > 0 )
			{
				//GAIN
				GenerateTTUMBean generatettumBean = new GenerateTTUMBean();
				generatettumBean.setStDebitAcc("99987750010154");
				generatettumBean.setStAmount(rset.getString("DIFF_AMOUNT"));
				inCredit_Amt = inCredit_Amt + Float.parseFloat(rset.getString("DIFF_AMOUNT"));
				//Tran Particular �RPAY-EXCH -DDMMYY (Transaction date)-Trace number-Merchant name
				String stTran_Particular = "RPAY-EXCH-"+rset.getString("LOCAL_DATE")+"-"+rset.getString("TRACE")+"-"+rset.getString("ACCEPTORNAME");
				stTran_parti1 = stTran_Particular;
				generatettumBean.setStTran_particulars(stTran_Particular);
				generatettumBean.setStCard_Number(rset.getString("PAN"));
				String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
				stInt_Remarks = remark;
				generatettumBean.setStRemark(remark);
				TTUM_D_Data.add(generatettumBean);
			}
			else if(inAmt < 0)
			{
				//LOSS
				GenerateTTUMBean generatettumBean = new GenerateTTUMBean();
				generatettumBean.setStCreditAcc("99987750010154");
				generatettumBean.setStAmount(rset.getString("DIFF_AMOUNT"));
				inDebit_Amt = inDebit_Amt + Float.parseFloat(rset.getString("DIFF_AMOUNT"));
				//Tran Particular �RPAY-EXCH -DDMMYY (Transaction date)-Trace number-Merchant name
				String stTran_Particular = "RPAY-EXCH-"+rset.getString("LOCAL_DATE")+"-"+rset.getString("TRACE")+"-"+rset.getString("ACCEPTORNAME");
				stTran_Parti2 = stTran_Particular;
				generatettumBean.setStTran_particulars(stTran_Particular);
				generatettumBean.setStCard_Number(rset.getString("PAN"));
				String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
				stInt_Remarks2 = remark;
				generatettumBean.setStRemark(remark);
				TTUM_C_Data.add(generatettumBean);
			}
			
		}
		
		//SET INT RUPAY POOL ACC
		
		if(inCredit_Amt > 0)
		{
			//GAIN 
			GenerateTTUMBean generateTTUMBean = new GenerateTTUMBean();
			generateTTUMBean.setStCreditAcc("99937200010663");
			generateTTUMBean.setStTran_particulars(stTran_parti1);
			generateTTUMBean.setStRemark(stInt_Remarks);
			generateTTUMBean.setStCard_Number("");
			TTUM_C_Data.add(generateTTUMBean);
		}
		
		if(inDebit_Amt > 0)
		{
			//LOSS
			GenerateTTUMBean generateTTUMBean = new GenerateTTUMBean();
			generateTTUMBean.setStDebitAcc("99937200010663");
			generateTTUMBean.setStTran_particulars(stTran_Parti2);
			generateTTUMBean.setStRemark(stInt_Remarks2);
			generateTTUMBean.setStCard_Number("");
			TTUM_D_Data.add(generateTTUMBean);
			
		}
		
		Data.add(TTUM_C_Data);
		Data.add(TTUM_D_Data);
		
		logger.info("***** GenerateRupayTTUMDaoImpl.getMatchedIntTxn End ****");
		
	}
	catch(Exception e)
	{
		demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.getMatchedIntTxn");
		logger.error(" error in GenerateRupayTTUMDaoImpl.getMatchedIntTxn", new Exception("GenerateRupayTTUMDaoImpl.getMatchedIntTxn",e));
		 throw e;
	}
	finally{
		if(rset!=null){
			rset.close();
		}
		if(pstmt!=null){
			pstmt.close();
		}
		if(conn!=null){
			conn.close();
		}
	}
	
	return Data;
}


public List<List<GenerateTTUMBean>> LevyCharges(List<List<GenerateTTUMBean>> Data,GenerateTTUMBean generatettumBeanObj)throws Exception
{
	logger.info("***** GenerateRupayTTUMDaoImpl.LevyCharges Start ****");
	List<List<GenerateTTUMBean>> TTUM_Data = new ArrayList<>();
	List<GenerateTTUMBean> TTUM_D_Data = new ArrayList<>();
	List<GenerateTTUMBean> TTUM_C_Data = new ArrayList<>();
	
	Connection conn =null;
	PreparedStatement pstmt = null;
	ResultSet rset =null;
	try
	{
		TTUM_C_Data = Data.get(0);
		TTUM_D_Data = Data.get(1);
		
		String GET_DATA = "SELECT T2.PAN,T2.ACCTNUM,TO_CHAR(TO_DATE(T2.LOCAL_DATE,'MM/DD/YYYY'),'DDMMYY') AS LOCAL_DATE,SUBSTR(T2.TRACE,-6,6) AS TRACE" +
				",T2.ACCEPTORNAME,T1.AMOUNT_SETTLEMENT,T1.AMOUNT_TRANSACTION FROM SETTLEMENT_"+generatettumBeanObj.getStCategory()+"_"
				+generatettumBeanObj.getStFile_Name()+" T1 , SETTLEMENT_"+generatettumBeanObj.getStCategory()+"_SWITCH T2 "
				+" WHERE T1.DCRS_REMARKS = '"+generatettumBeanObj.getStMerger_Category()+"-MATCHED-"+generatettumBeanObj.getInRec_Set_Id()
				+"' AND T2.DCRS_REMARKS = '"+generatettumBeanObj.getStMerger_Category()+"-MATCHED-"+generatettumBeanObj.getInRec_Set_Id()+"'" 
				+" AND T2.PAN = T1.PRIMARY_ACCOUNT_NUMBER AND T2.AUTHNUM = T1.APPROVAL_CODE AND T2.ISSUER = T1.ACQUIRER_REFERENCE_DATA";
		
		logger.info("GET_DATA=="+GET_DATA);
		conn = getConnection();
		pstmt = conn.prepareStatement(GET_DATA);
		rset = pstmt.executeQuery();
		
		String GET_RATE = "SELECT RATE FROM MAIN_ACCOUNTING_TABLE WHERE CATEGORY = '"+generatettumBeanObj.getStCategory()+"_"+generatettumBeanObj.getStSubCategory()+"'";
		logger.info("GET_RATE=="+GET_RATE);
		String Rate = getJdbcTemplate().queryForObject(GET_RATE, new Object[] {},String.class);		
		logger.info("Rate=="+Rate);
		while(rset.next())
		{
			GenerateTTUMBean generatettumBean = new GenerateTTUMBean();
			float debit_amt = (Float.parseFloat(rset.getString("AMOUNT_SETTLEMENT"))+(Float.parseFloat(rset.getString("AMOUNT_SETTLEMENT"))*
						Float.parseFloat(Rate))) - Float.parseFloat(rset.getString("AMOUNT_TRANSACTION")); 
			
			logger.info("debit amount is "+debit_amt);
			
			//EXCHANGE GAIN/LOSS ACC CR
			generatettumBean.setStCreditAcc("99987750010154");
			//CUST ACC DR
			generatettumBean.setStDebitAcc(rset.getString("ACCTNUM").replaceAll(" ", " ").replaceAll("^0*",""));
			generatettumBean.setStCard_Number(rset.getString("PAN"));
			generatettumBean.setStAmount(debit_amt+"");
			String stTran_Particular = "DR-RPAY-MARKUP"+rset.getString("LOCAL_DATE")+"-"+rset.getString("TRACE")+"-"+rset.getString("ACCEPTORNAME");
			generatettumBean.setStTran_particulars(stTran_Particular);
			String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
			generatettumBean.setStRemark(remark);
			TTUM_C_Data.add(generatettumBean);

		}
		
		TTUM_Data.add(TTUM_C_Data);
		TTUM_Data.add(TTUM_D_Data);
		
		logger.info("***** GenerateRupayTTUMDaoImpl.LevyCharges End ****");
	}
	catch(Exception e)
	{
		demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.LevyCharges");
		logger.error(" error in GenerateRupayTTUMDaoImpl.LevyCharges", new Exception("GenerateRupayTTUMDaoImpl.LevyCharges",e));
		 throw e;
	}
	finally{
		if(rset!=null){
			rset.close();
		}
		if(pstmt!=null){
			pstmt.close();
		}
		if(conn!=null){
			conn.close();
		}
	}
	return Data;
}

public String getLatestFileDate(GenerateTTUMBean generateTTUMBean)
{
	logger.info("***** GenerateRupayTTUMDaoImpl.getLatestFileDate Start ****");
	String stFileDate = "";
	String GET_FILEDATE="";
	
	if(generateTTUMBean.getStCategory().equals("CASHNET")) {
	
		if(generateTTUMBean.getStFile_Name().equals("CBS")) {
			
		/*	GET_FILEDATE ="SELECT MAX(CBSFILEDATE) FROM glbl_CBS_unmatch";
			stFileDate = getJdbcTemplate().queryForObject(GET_FILEDATE, new Object[]{}, String.class);
			
		} else{*/
			GET_FILEDATE ="SELECT TO_CHAR(MAX(FILEDATE),'DD/MM/YYYY') FROM SETTLEMENT_"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStSubCategory().substring(0, 3)+ "_"+generateTTUMBean.getStSelectedFile();
			stFileDate = getJdbcTemplate().queryForObject(GET_FILEDATE, new Object[]{}, String.class);
		}
	} else {
		
		GET_FILEDATE ="SELECT TO_CHAR(MAX(FILEDATE),'DD/MM/YYYY') FROM SETTLEMENT_"+generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStSelectedFile();
		stFileDate = getJdbcTemplate().queryForObject(GET_FILEDATE, new Object[]{}, String.class);
		
	}
	logger.info("GET_FILEDATE=="+GET_FILEDATE);
	logger.info("stFileDate=="+stFileDate);
	
	logger.info("***** GenerateRupayTTUMDaoImpl.getLatestFileDate End ****");
	
	return stFileDate;
}

//added by int5779 on 07 jan 2018 as per new requirement doc
@Override
public void getFailedCBSRecords(GenerateTTUMBean generateTTUMBeanObj) throws Exception
{
	logger.info("***** GenerateRupayTTUMDaoImpl.getFailedCBSRecords Start ****");
	/*UPDATE_QUERY = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStFile_Name()
			+" SET DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)
			+"-UNRECON-"+generateTTUMBeanObj.getInRec_Set_Id()
			+"-GENERATE-TTUM (8)' WHERE TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStFile_Date()+"' " +
			"AND DCRS_REMARKS = '"+generateTTUMBeanObj.getStCategory()+"_"+generateTTUMBeanObj.getStSubCategory().substring(0, 3)+"-UNRECON-1' " +
			" AND TO_CHAR(TO_DATE(VALUE_DATE,'DD/MM/YYYY'),'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"'";*/

	//	commented by sushant 07/oct/2019
	/*String UPDATE_QUERY = "UPDATE SETTLEMENT_"+generateTTUMBeanObj.getStCategory()+"_CBS SET DCRS_REMARKS = REPLACE(DCRS_REMARKS,'UNRECON','UNRECON-GENERATE-TTUM') "+
			"WHERE DCRS_REMARKS LIKE '%(%' "+
			" AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStFile_Date()+"' AND "+
			"TO_CHAR(TO_DATE(VALUE_DATE,'DD-MM-YYYY'),'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"' ";*/
	
	String UPDATE_QUERY = "UPDATE GLBL_CBS_UNMATCH SET DCRS_REMARKS = REPLACE(DCRS_REMARKS,'UNRECON','UNRECON-GENERATE-TTUM') "+
			"WHERE DCRS_REMARKS LIKE '%(%' "+
			" AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStFile_Date()+"' AND "+
			"TO_CHAR(TO_DATE(VALUE_DATE,'DD-MM-YYYY'),'DD/MM/YYYY') = '"+generateTTUMBeanObj.getStDate()+"' ";
			
	logger.info("UPDATE_QUERY=="+UPDATE_QUERY);
	try
	{
		getJdbcTemplate().execute(UPDATE_QUERY);
		logger.info("***** GenerateRupayTTUMDaoImpl.getFailedCBSRecords End ****");
	}
	
	
	catch(Exception e)
	{
		demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.getFailedCBSRecords");
		logger.error(" error in GenerateRupayTTUMDaoImpl.getFailedCBSRecords", new Exception("GenerateRupayTTUMDaoImpl.getFailedCBSRecords",e));
		 throw e;
	}
			
	
		
			
}


@Override
public List<List<GenerateTTUMBean>> generateDisputeTTUM(GenerateTTUMBean ttumBean) throws Exception {
	
	logger.info("***** GenerateRupayTTUMDaoImpl.generateDisputeTTUM Start ****");
	
	List<GenerateTTUMBean> ttum_data = new ArrayList<>();
	List<GenerateTTUMBean> ttum_data_cbs = new ArrayList<>();
	List<GenerateTTUMBean> ttum_switch = new ArrayList<>();
	List<GenerateTTUMBean> ttum_cbs = new ArrayList<>();
	List<GenerateTTUMBean> ttum_others = new ArrayList<>();
	List<GenerateTTUMBean> Excel_headers = new ArrayList<>();
	List<String> ExcelHeaders = new ArrayList<>();
	List<String> ExcelHeaders1 = new ArrayList<>();
	List<List<GenerateTTUMBean>> Total_Data = new ArrayList<>();
	String table_cols = "DCRS_REMARKS VARCHAR (100 BYTE), CREATEDDATE DATE , CREATEDBY VARCHAR (100 BYTE),RECORDS_DATE DATE";
	String insert_cols = "DCRS_REMARKS, CREATEDDATE, CREATEDBY,RECORDS_DATE";
	String stAction = "";
	
	Connection conn=null;
	PreparedStatement pstmt =null;
	ResultSet rset=null;
	PreparedStatement pstmt1 =null;
	ResultSet rset1=null;
	PreparedStatement ps =null;
	ResultSet rs=null;
	PreparedStatement ps1 =null;
	ResultSet rs1=null;
	PreparedStatement ps2 =null;
	ResultSet rs2=null;
	
	try
	{
		
		java.util.Date varDate=null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		try {
		     varDate=dateFormat.parse(ttumBean.getStDate());
		    dateFormat=new SimpleDateFormat("ddMMyy");
		    logger.info("Date :"+dateFormat.format(varDate));
		}catch (Exception e) {
			demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.generateDisputeTTUM");
			logger.error(" error in GenerateRupayTTUMDaoImpl.generateDisputeTTUM", new Exception("GenerateRupayTTUMDaoImpl.generateDisputeTTUM",e));
			 throw e;
		}
		
		
		logger.info("INSIDE generateSwitchTTUM");
		if(ttumBean.getStSubCategory().equalsIgnoreCase("DOMESTIC"))
		{
			ttumBean.setStGLAccount("99937200010660");//as per document
			//get CUST ACCOUNT NO FROM SETTLEMENT TABLE FOR DEBIT
			String GET_TTUM_RECORDS = "SELECT ACCTNUM,AMOUNT,PAN,TO_CHAR(TO_DATE(LOCAL_DATE,'MM/DD/YYYY'),'DDMMYY') AS LOCAL_DATE,SUBSTR(TRACE,-6,6) AS TRACE ," +
								" ACCEPTORNAME,filedate " +" FROM SETTLEMENT_"+ttumBean.getStCategory()+"_Switch" 
								+ " WHERE DCRS_REMARKS LIKE '%GENERATE-TTUM-2%'";
						
			conn = getConnection();
			pstmt = conn.prepareStatement(GET_TTUM_RECORDS);
			rset = pstmt.executeQuery();
			
			String GET_TTUM_RECORDS_CBS = "SELECT E,CONTRA_ACCOUNT,FORACID,TO_NUMBER(AMOUNT,999999999.99) AS AMOUNT, TO_CHAR(TO_DATE(VALUE_DATE,'DD/MM/YYYY'),'DDMMYY') AS VALUE_DATE,"
					+"SUBSTR(REF_NO,2,6) AS REF_NO, REMARKS,PARTICULARALS,filedate FROM GLBL_CBS_UNMATCH "    //SETTLEMENT_"+ttumBean.getStCategory()+"_CBS"
					+" WHERE DCRS_REMARKS LIKE '%GENERATE-TTUM-1%'";
			
			logger.info("GET_TTUM_RECORDS_CBS "+GET_TTUM_RECORDS_CBS);
			
			//Connection conn = getConnection();
			pstmt1 = conn.prepareStatement(GET_TTUM_RECORDS_CBS);
			rset1 = pstmt1.executeQuery();
			
			//ExcelHeaders.add("FILE_HEADER");
			ExcelHeaders.add("ACCOUNT NUMBER");
			ExcelHeaders.add("CURRENCY CODE");
			ExcelHeaders.add("SERVICE OUTLET");
			ExcelHeaders.add("PART TRAN TYPE");
			ExcelHeaders.add("TRANSACTION AMOUNT");
			ExcelHeaders.add("TRANSACTION PARTICULARS");
			ExcelHeaders.add("REFERENCE NUMBER");
			ExcelHeaders.add("REFERENCE CURRENCY CODE");
			ExcelHeaders.add("REFERENCE AMOUNT");
			ExcelHeaders.add("REMARKS");
			ExcelHeaders.add("REPORT CODE");
			
			//modified on 25/04/2018
			ExcelHeaders1.add("ACCOUNT_NUMBER");
			ExcelHeaders1.add("CURRENCY_CODE");
			ExcelHeaders1.add("SERVICE_OUTLET");
			ExcelHeaders1.add("PART_TRAN_TYPE");
			ExcelHeaders1.add("TRANSACTION_AMOUNT");
			ExcelHeaders1.add("TRANSACTION_PARTICULARS");
			ExcelHeaders1.add("REFERENCE_NUMBER");
			ExcelHeaders1.add("REFERENCE_CURRENCY_CODE");
			ExcelHeaders1.add("REFERENCE_AMOUNT");
			ExcelHeaders1.add("REMARKS");
			ExcelHeaders1.add("ACCOUNT_REPORT_CODE");
			ExcelHeaders1.add("FILEDATE");
			
			ttumBean.setStExcelHeader(ExcelHeaders);
			
			Excel_headers.add(ttumBean);
			int count = 0;
			int count1 = 0;
			while(rset.next())
			{
				count++;
					GenerateTTUMBean generateTTUMBeanObj = new GenerateTTUMBean();		
					//CUST CR
					generateTTUMBeanObj.setStCreditAcc(rset.getString("ACCTNUM").replaceAll(" ", " ").replaceAll("^0*",""));
					//GL DR
					generateTTUMBeanObj.setStDebitAcc("99937200010660");
					generateTTUMBeanObj.setStAmount(rset.getString("AMOUNT"));
					String stTran_particulars = "REV-RPAY-"+rset.getString("LOCAL_DATE")+"-"+rset.getString("ACCEPTORNAME").replaceAll("'", "");
					generateTTUMBeanObj.setStDate(rset.getString("LOCAL_DATE"));							
					generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
					String remark = getJdbcTemplate().queryForObject("select 'RPYD'||'"+dateFormat.format(varDate)+"'||ttum_seq.nextval from dual", new Object[] {},String.class);
					generateTTUMBeanObj.setStCard_Number(remark);
					generateTTUMBeanObj.setStRemark(rset.getString("PAN")+"/"+rset.getString("TRACE"));	
					generateTTUMBeanObj.setFiledate(rset.getString("filedate"));
					ttum_data.add(generateTTUMBeanObj);
					 
				
			}
			
			while(rset1.next())
			{
				 
					stAction = rset1.getString("E");
					GenerateTTUMBean generateTTUMBean1 = new GenerateTTUMBean();
					
					String card_num = rset1.getString("REMARKS");
					
					if(stAction.equals("C"))
					{
						
						String stAccNum = rset1.getString("CONTRA_ACCOUNT");
						if(stAccNum.contains("78000010021")){
							String get_man_acc="select distinct t1.CONTRA_ACCOUNT,t2.ACCTNUM,t1.FILEDATE,t1.TRAN_DATE,t1.AMOUNT,t2.AMOUNT_EQUIV,substr(t1.REF_NO,2,6),substr(t2.TRACE,2,6) from cbs_rupay_rawdata t1 inner join "
								      + "switch_rawdata t2 on t1.REMARKS = t2.PAN and t1.FILEDATE = t2.FILEDATE and trunc(TO_NUMBER(REPLACE(T1.AMOUNT,',',''))) = trunc(TO_NUMBER(t2.AMOUNT_EQUIV)) and substr(t1.REF_NO,2,6) "
								      + "=substr(t2.TRACE,2,6)  and t2.ACCTNUM is not null "
								      + "where t1.CONTRA_ACCOUNT = '"+stAccNum+"' and t2.PAN = '"+card_num+"' order by t1.FILEDATE";
															
															PreparedStatement pstmt_con = conn.prepareStatement(get_man_acc);
															ResultSet rset_con = pstmt_con.executeQuery();
															
																
															while(rset_con.next())
															{
																generateTTUMBean1.setStCreditAcc(rset_con.getString("ACCTNUM").replaceAll(" ", " ").replaceAll("^0*",""));
																
																}
															}else{
																generateTTUMBean1.setStCreditAcc(rset1.getString("CONTRA_ACCOUNT"));
															}
						
						
						//GL DEBIT
						generateTTUMBean1.setStDebitAcc(rset1.getString("FORACID"));
						//CUSTOMER ACC CREDIT
		 
						generateTTUMBean1.setStAmount(rset1.getString("AMOUNT"));
						String stTran_particular = "REV-RPAY-"+rset1.getString("VALUE_DATE")+"-"+rset1.getString("PARTICULARALS").replace("'", "");
						generateTTUMBean1.setStDate(rset1.getString("VALUE_DATE"));
						generateTTUMBean1.setStTran_particulars(stTran_particular);
						
						String remark = getJdbcTemplate().queryForObject("select 'RPYD'||'"+dateFormat.format(varDate)+"'||ttum_seq.nextval from dual", new Object[] {},String.class);
						generateTTUMBean1.setStCard_Number(remark);
						generateTTUMBean1.setStRemark(rset1.getString("REMARKS")+"/"+rset1.getString("REF_NO"));
						generateTTUMBean1.setFiledate(rset1.getString("filedate"));
						ttum_data_cbs.add(generateTTUMBean1);


					}
					else if(stAction.equals("D"))
					{
						String stAccNum = rset1.getString("CONTRA_ACCOUNT");
						if(stAccNum.contains("78000010021")){
							String get_man_acc="select distinct t1.CONTRA_ACCOUNT,t2.ACCTNUM,t1.FILEDATE,t1.TRAN_DATE,t1.AMOUNT,t2.AMOUNT_EQUIV,substr(t1.REF_NO,2,6),substr(t2.TRACE,2,6) from cbs_rupay_rawdata t1 inner join "
								      + "switch_rawdata t2 on t1.REMARKS = t2.PAN and t1.FILEDATE = t2.FILEDATE and trunc(TO_NUMBER(REPLACE(T1.AMOUNT,',',''))) = trunc(TO_NUMBER(t2.AMOUNT_EQUIV)) and substr(t1.REF_NO,2,6) "
								      + "=substr(t2.TRACE,2,6)  and t2.ACCTNUM is not null "
								      + "where t1.CONTRA_ACCOUNT = '"+stAccNum+"' and t2.PAN = '"+card_num+"' order by t1.FILEDATE";
															
															PreparedStatement pstmt_con = conn.prepareStatement(get_man_acc);
															ResultSet rset_con = pstmt_con.executeQuery();
															
																
															while(rset_con.next())
															{
																generateTTUMBean1.setStDebitAcc(rset_con.getString("ACCTNUM").replaceAll(" ", " ").replaceAll("^0*",""));
																}
															}else{
																generateTTUMBean1.setStDebitAcc(rset1.getString("CONTRA_ACCOUNT"));
															}
						
						//CUSTOMER ACC DEBIT
						generateTTUMBean1.setStCreditAcc(rset1.getString("FORACID"));
						//GL CREDIT
						generateTTUMBean1.setStAmount(rset1.getString("AMOUNT"));
						String stTran_particular = "ADR-RPAY-"+rset1.getString("VALUE_DATE")+"-"+rset1.getString("PARTICULARALS");
						generateTTUMBean1.setStDate(rset1.getString("VALUE_DATE"));
						generateTTUMBean1.setStTran_particulars(stTran_particular);
						String remark = getJdbcTemplate().queryForObject("select 'RPYD'||'"+dateFormat.format(varDate)+"'||ttum_seq.nextval from dual", new Object[] {},String.class);
						generateTTUMBean1.setStCard_Number(remark);
						generateTTUMBean1.setStRemark(rset1.getString("REMARKS")+"/"+rset1.getString("REF_NO"));
						generateTTUMBean1.setFiledate(rset1.getString("filedate"));
						ttum_data_cbs.add(generateTTUMBean1);
					}
				
			}
			
		}
		else if(ttumBean.getStSubCategory().equalsIgnoreCase("INTERNATIONAL"))
		{
			ttumBean.setStGLAccount("99937200010663");
			//get CUST ACCOUNT NO FROM SETTLEMENT TABLE FOR DEBIT
			String GET_TTUM_RECORDS = "SELECT PAN,ACCTNUM,AMOUNT,ISSUER,TO_CHAR(TO_DATE(LOCAL_DATE,'MM/DD/YYYY'),'DDMMYY') AS LOCAL_DATE,SUBSTR(TRACE,-6,6) AS TRACE," +
								"ACCEPTORNAME FROM SETTLEMENT_"+ttumBean.getStCategory()+"_"+ttumBean.getStFile_Name() + " WHERE DCRS_REMARKS LIKE '%GENERATE-TTUM%'";
						
			conn = getConnection();
			pstmt = conn.prepareStatement(GET_TTUM_RECORDS);
			rset = pstmt.executeQuery();
			
			//ExcelHeaders.add("FILE_HEADER");
			ExcelHeaders.add("ACCOUNT NUMBER");
			ExcelHeaders.add("CURRENCY CODE");
			ExcelHeaders.add("SERVICE OUTLET");
			ExcelHeaders.add("PART TRAN TYPE");
			ExcelHeaders.add("TRANSACTION AMOUNT");
			ExcelHeaders.add("TRANSACTION PARTICULARS");
			ExcelHeaders.add("REFERENCE NUMBER");
			ExcelHeaders.add("REFERENCE CURRENCY CODE");
			ExcelHeaders.add("REFERENCE AMOUNT");
			ExcelHeaders.add("REMARKS");
			ExcelHeaders.add("REPORT CODE");
			
			
			
			
			
			ttumBean.setStExcelHeader(ExcelHeaders);
			
			Excel_headers.add(ttumBean);
			
			while(rset.next())
			{
				GenerateTTUMBean generateTTUMBeanObj = new GenerateTTUMBean();
				
					generateTTUMBeanObj.setStCreditAcc(rset.getString("ACCTNUM").replaceAll(" ", " ").replaceAll("^0*",""));
					//generateTTUMBeanObj.setStDebitAcc(rset.getString("ACCTNUM").split(" ")[1]);
					generateTTUMBeanObj.setStDebitAcc("99937200010660");
					generateTTUMBeanObj.setStAmount(rset.getString("AMOUNT"));
				//	String stTran_particulars = "REV/"+generateTTUMBean.getStCategory()+"/"+rset.getString("LOCAL_DATE")+"/"+rset.getString("TRACE");
					String stTran_particulars = "REV-RPAY-"+rset.getString("LOCAL_DATE")+"/"+rset.getString("TRACE")+"-"+rset.getString("ACCEPTORNAME");
					
					String stRecords_Date = rset.getString("LOCAL_DATE");
			//		generateTTUMBean.setStDate(rset.getString("LOCAL_DATE"));
					generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
				//	generateTTUMBeanObj.setStRRNo(rset.getString("ISSUER"));
					generateTTUMBeanObj.setStCard_Number(rset.getString("PAN"));
					/*String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);*/
					String remark = getJdbcTemplate().queryForObject("select 'RPYI'||'"+dateFormat.format(varDate)+"'||ttum_seq.nextval from dual", new Object[] {},String.class);
					generateTTUMBeanObj.setStRemark(remark);
					generateTTUMBeanObj.setAccount_repo("");
				
				ttum_data.add(generateTTUMBeanObj);
				
			}
			
			Total_Data.add(Excel_headers);
			Total_Data.add(ttum_data);
			
			
		
		}
		
		
		
		
		
		// CREATE NEW TABLE FOR INSERTING TTUM ENTRIES
		for(int i = 0 ; i<ExcelHeaders1.size();i++)
		{
			table_cols =table_cols+","+ ExcelHeaders1.get(i)+" VARCHAR (100 BYTE)";
			insert_cols = insert_cols+","+ExcelHeaders1.get(i);
		}
		
		String CHECK_TABLE = "SELECT count (*) FROM tab WHERE tname  = UPPER('TTUM_"+ttumBean.getStMerger_Category()
				+"_"+ttumBean.getStFile_Name()+"')";
		int tableExist = getJdbcTemplate().queryForObject(CHECK_TABLE, new Object[] { },Integer.class);
		
		logger.info("CHECK_TABLE=="+CHECK_TABLE);
		logger.info("tableExist=="+tableExist);
		
		if(tableExist == 0)
		{
			String CREATE_QUERY = "CREATE TABLE TTUM_"+ttumBean.getStMerger_Category()
					+"_"+ttumBean.getStFile_Name()+" ("+table_cols+")";
			
			logger.info("CREATE_QUERY=="+CREATE_QUERY);
			getJdbcTemplate().execute(CREATE_QUERY);
		}
		
		String query1 = "truncate table TTUM_"+ttumBean.getStMerger_Category()+"_"+ttumBean.getStFile_Name();
		logger.info(query1);
		getJdbcTemplate().execute(query1);
		
		//INSERT TTUM ENTRIES IN ABOVE CREATED TABLE
		int incount = 0;
		int count=0;
		for(int i = 0;i<ttum_data.size();i++)
		{
			incount++;
			GenerateTTUMBean beanObj = new GenerateTTUMBean();
			beanObj = ttum_data.get(i);
			String INSERT_QUERY = "INSERT INTO TTUM_"+ttumBean.getStMerger_Category()
						+"_"+ttumBean.getStFile_Name() 
						+"("+insert_cols+") VALUES ('"+ttumBean.getStMerger_Category()+
						"-UNRECON-TTUM-2"
						+"',SYSDATE,'"+ttumBean.getStEntry_by()+"',TO_DATE('"+beanObj.getStDate()+"','DDMMYY'),'"+beanObj.getStDebitAcc()+"','INR','999','D','"+
						beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','"+beanObj.getStCard_Number()+"','INR','"+beanObj.getStAmount()+"','"+
						beanObj.getStRemark()+"','"+beanObj.getAccount_repo()+"','"+beanObj.getFiledate()+"')";
			
			logger.info("INSERT_QUERY=="+INSERT_QUERY);
			getJdbcTemplate().execute(INSERT_QUERY);
			
			 INSERT_QUERY = "INSERT INTO TTUM_"+ttumBean.getStMerger_Category()
					 +"_"+ttumBean.getStFile_Name() 
						+"("+insert_cols+") VALUES ('"+ttumBean.getStMerger_Category()
						+"-UNRECON-TTUM-2"
						+"',SYSDATE,'"+ttumBean.getStEntry_by()+"',TO_DATE('"+beanObj.getStDate()+"','DDMMYY'),'"+beanObj.getStCreditAcc().trim()+"','INR','999','C','"+
						beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','"+beanObj.getStCard_Number()+"','INR','"+beanObj.getStAmount()+"','"+
						beanObj.getStRemark()+"','"+beanObj.getAccount_repo()+"','"+beanObj.getFiledate()+"')";
			
			 logger.info("INSERT_QUERY=="+INSERT_QUERY);
			 getJdbcTemplate().execute(INSERT_QUERY);		
	
		}
		
		int incount1 = 0;
		for(int i = 0;i<ttum_data_cbs.size();i++)
		{
			incount1++;
			
			GenerateTTUMBean beanObj = new GenerateTTUMBean();
			beanObj = ttum_data_cbs.get(i);
			String INSERT_QUERY = "INSERT INTO TTUM_"+ttumBean.getStMerger_Category()
						+"_"+ttumBean.getStFile_Name() 
						+"("+insert_cols+") VALUES ('"+ttumBean.getStMerger_Category()+
						"-UNRECON-TTUM-1"
						+"',SYSDATE,'"+ttumBean.getStEntry_by()+"',TO_DATE('"+beanObj.getStDate()+"','DDMMYY'),'"+beanObj.getStDebitAcc()+"','INR','999','D','"+
						beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','"+beanObj.getStCard_Number()+"','INR','"+beanObj.getStAmount()+"','"+
						beanObj.getStRemark()+"','"+beanObj.getAccount_repo()+"','"+beanObj.getFiledate()+"')";
			
			logger.info("INSERT_QUERY=="+INSERT_QUERY);
			getJdbcTemplate().execute(INSERT_QUERY);
			
			 INSERT_QUERY = "INSERT INTO TTUM_"+ttumBean.getStMerger_Category()
					 +"_"+ttumBean.getStFile_Name() 
						+"("+insert_cols+") VALUES ('"+ttumBean.getStMerger_Category()
						+"-UNRECON-TTUM-1"
						+"',SYSDATE,'"+ttumBean.getStEntry_by()+"',TO_DATE('"+beanObj.getStDate()+"','DDMMYY'),'"+beanObj.getStCreditAcc().trim()+"','INR','999','C','"+
						beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','"+beanObj.getStCard_Number()+"','INR','"+beanObj.getStAmount()+"','"+
						beanObj.getStRemark()+"','"+beanObj.getAccount_repo()+"','"+beanObj.getFiledate()+"')";
			
			 logger.info("INSERT_QUERY=="+INSERT_QUERY);
			 getJdbcTemplate().execute(INSERT_QUERY);	
			 
			
		}
		
		String GET_TTUM_SWITCH = "SELECT ACCOUNT_NUMBER,PART_TRAN_TYPE,TRANSACTION_AMOUNT,TRANSACTION_PARTICULARS,REMARKS,REFERENCE_NUMBER" +
				" FROM TTUM_RUPAY_DOM_DISPUTE WHERE DCRS_REMARKS LIKE '%UNRECON-TTUM-2%' and to_char(to_date(substr(filedate,1,10),'yyyy-mm-dd'),'dd/mm/yyyy') = '"+ttumBean.getStDate()+"' order by REFERENCE_NUMBER,TRANSACTION_AMOUNT,ACCOUNT_NUMBER";
		
		logger.info("GET_TTUM_SWITCH "+GET_TTUM_SWITCH);
		
	conn = getConnection();
	ps = conn.prepareStatement(GET_TTUM_SWITCH);
	rs = ps.executeQuery();
	while(rs.next())
	{
		count++;
		//logger.info("count is "+count);
		GenerateTTUMBean generateTTUMBeanObj = new GenerateTTUMBean();		
		//for(int loop=0;loop<2;loop++){
			//if(loop==0){
			//CUST CR
			generateTTUMBeanObj.setStCreditAcc(rs.getString("ACCOUNT_NUMBER").replaceAll(" ", " ").replaceAll("^0*",""));
			//generateTTUMBeanObj.setStCreditAcc(rset.getString("ACCTNUM").split(" ")[1]);
			//GL DR
			generateTTUMBeanObj.setStDebitAcc("99937200010660");
			generateTTUMBeanObj.setStAmount(rs.getString("TRANSACTION_AMOUNT"));
			//String stTran_particulars = "REV/"+generateTTUMBean.getStCategory()+"/"+rset.getString("LOCAL_DATE")+"/"+rset.getString("TRACE")+
			//String stTran_particulars = "REV-T10-RPAY-"+rs.getString("LOCAL_DATE")+"-"+rs.getString("TRACE")+"-"+rs.getString("ACCEPTORNAME").replaceAll("'", "");
			//generateTTUMBeanObj.setStDate(rs.getString("LOCAL_DATE"));							
	//		generateTTUMBean.setStDate(rset.getString("LOCAL_DATE"));
			generateTTUMBeanObj.setStTran_particulars(rs.getString("TRANSACTION_PARTICULARS"));
			//generateTTUMBeanObj.setStRRNo(rset.getString("ISSUER"));
			generateTTUMBeanObj.setStCard_Number(rs.getString("REFERENCE_NUMBER"));
			//String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
			generateTTUMBeanObj.setStRemark(rs.getString("REMARKS"));	
			generateTTUMBeanObj.setPart_tran_type(rs.getString("PART_TRAN_TYPE"));
						
			ttum_switch.add(generateTTUMBeanObj);
		}
		
		 String GET_TTUM_CBS = "SELECT ACCOUNT_NUMBER,PART_TRAN_TYPE,TRANSACTION_AMOUNT,TRANSACTION_PARTICULARS,REMARKS,REFERENCE_NUMBER" +
					" FROM TTUM_RUPAY_DOM_DISPUTE WHERE DCRS_REMARKS LIKE '%UNRECON-TTUM-1%' and to_char(to_date(substr(filedate,1,10),'yyyy-mm-dd'),'dd/mm/yyyy') = '"+ttumBean.getStDate()+"' order by REFERENCE_NUMBER,TRANSACTION_AMOUNT,ACCOUNT_NUMBER";
			
			logger.info("GET_TTUM_RECORDS_CBS "+GET_TTUM_CBS);
			
			//Connection conn = getConnection();
			ps1 = conn.prepareStatement(GET_TTUM_CBS);
			rs1 = ps1.executeQuery();
			
			while(rs1.next())
			{
				/*if(generateTTUMBeanObj.getStSubCategory().equals("DOMESTIC"))
				{*/
					//stAction = rs1.getString("E");
					GenerateTTUMBean generateTTUMBean1 = new GenerateTTUMBean();
					
						//GL DEBIT
						generateTTUMBean1.setStDebitAcc("99937200010660");
						//CUSTOMER ACC CREDIT
						generateTTUMBean1.setStCreditAcc(rs1.getString("ACCOUNT_NUMBER"));
						generateTTUMBean1.setStAmount(rs1.getString("TRANSACTION_AMOUNT"));
						//generateTTUMBean.setStDate(rset.getString("VALUE_DATE"));
						//String stTran_particular = "REV/"+generateTTUMBeanObj.getStCategory()+"/"+rset.getString("VALUE_DATE")+"/"+rset.getString("REF_NO");
						
						//Tran Particular � REV-T2 -RPAY-DDMMYY (Transaction date)-Trace number-Merchant name
						//String stTran_particular = "REV-T2-RPAY-"+rs1.getString("VALUE_DATE")+"-"+rset1.getString("REF_NO")+"-"+rset1.getString("PARTICULARALS");
						//generateTTUMBean1.setStDate(rs1.getString("VALUE_DATE"));
						String particular = rs1.getString("TRANSACTION_PARTICULARS");
						if(particular.contains("RPAY-POS")){
							particular = particular.replace(particular.substring(15, 24), "");
						}
						generateTTUMBean1.setStTran_particulars(particular);
						generateTTUMBean1.setStCard_Number(rs1.getString("REFERENCE_NUMBER"));
						//String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
						generateTTUMBean1.setStRemark(rs1.getString("REMARKS"));
						generateTTUMBean1.setPart_tran_type(rs1.getString("PART_TRAN_TYPE"));
						//generateTTUMBean1.setPart_tran_type(stAction);
						ttum_cbs.add(generateTTUMBean1);
						//Diff_Data.add(generateTTUMBean1);


					}
			
			//String GET_TTUM_OTHER = "SELECT PAN,TRACE,LOCAL_DATE,AMOUNT_EQUIV FROM TEMP_RUPAY_DISPUTE where filedate = to_date('"+ttumBean.getStDate()+"','dd/mm/yyyy')";
			String GET_TTUM_OTHER = "SELECT PAN,TRACE,LOCAL_DATE,AMOUNT_EQUIV,TXNFUNCTION_CODE,DATE_SETTLEMENT,AMOUNT_SETTLEMENT,SWITCH_REMARKS,CBS_REMARKS FROM TEMP_RUPAY_DISPUTE1 where filedate = to_date('"+ttumBean.getStDate()+"','dd/mm/yyyy')";
			
			
			logger.info("GET_TTUM_RECORDS_OTHER "+GET_TTUM_OTHER);
			
			//Connection conn = getConnection();
			ps2 = conn.prepareStatement(GET_TTUM_OTHER);
			rs2 = ps2.executeQuery();
			
			while(rs2.next())
			{
			
					GenerateTTUMBean generateTTUMBean1 = new GenerateTTUMBean();
					
						
						generateTTUMBean1.setPan(rs2.getString("pan"));
						generateTTUMBean1.setTrace(rs2.getString("trace"));
						generateTTUMBean1.setLocal_date(rs2.getString("local_date"));
						generateTTUMBean1.setAmount_equiv(rs2.getString("amount_equiv"));
						generateTTUMBean1.setFuncation_code(rs2.getString("TXNFUNCTION_CODE"));
						generateTTUMBean1.setSettlement_date(rs2.getString("DATE_SETTLEMENT"));
						generateTTUMBean1.setSettlement_amount(rs2.getString("AMOUNT_SETTLEMENT"));
						generateTTUMBean1.setSwitch_remarks(rs2.getString("SWITCH_REMARKS"));
						generateTTUMBean1.setCbs_remarks(rs2.getString("CBS_REMARKS"));
						
						ttum_others.add(generateTTUMBean1);
						//Diff_Data.add(generateTTUMBean1);


					}
		
			Total_Data.add(Excel_headers);
			Total_Data.add(null);
			Total_Data.add(2,ttum_switch);
			Total_Data.add(3,ttum_cbs);
			Total_Data.add(4,ttum_others);
		
		//UPDATE TTUM GENERATED RECORDS
		String value = "";
		for(int loop=1;loop<=2;loop++){
			if(loop == 1){
				value="CBS";
			}else{
				value="SWITCH";
			}
				String UPDATE_RECORDS = "UPDATE SETTLEMENT_"+ttumBean.getStCategory()+"_"+value+
						" SET DCRS_REMARKS = '"+ttumBean.getStMerger_Category()+"-UNRECON-GENERATED-TTUM-"+loop+"'"
						+" WHERE DCRS_REMARKS = '"+ttumBean.getStMerger_Category()+"-UNRECON-GENERATE-TTUM-"+loop+"'";

				logger.info("UPDATE_RECORDS=="+UPDATE_RECORDS);
				getJdbcTemplate().execute(UPDATE_RECORDS);
				
		}
		
		String CHECK_TABLE1 = "SELECT count (*) FROM tab WHERE tname  = UPPER('TTUM_"+ttumBean.getStMerger_Category()
				+"_"+ttumBean.getStFile_Name()+"_BKUP')";
		int tableExist1 = getJdbcTemplate().queryForObject(CHECK_TABLE1, new Object[] { },Integer.class);
		
		if(tableExist1 == 0)
		{
			String CREATE_QUERY1 = "CREATE TABLE TTUM_"+ttumBean.getStMerger_Category()
					+"_"+ttumBean.getStFile_Name()+"_BKUP ("+table_cols+",MOVED_ON DATE)";
			
			getJdbcTemplate().execute(CREATE_QUERY1);
		}
		
		String query = "insert into TTUM_"+ttumBean.getStMerger_Category()+"_"+ttumBean.getStFile_Name()+"_BKUP ("+insert_cols+",MOVED_ON) select "+insert_cols+",sysdate from TTUM_"+ttumBean.getStMerger_Category()
					+"_"+ttumBean.getStFile_Name();
		logger.info(query);
		
		
		
		getJdbcTemplate().execute(query);
		
		String query2 = "truncate table TTUM_"+ttumBean.getStMerger_Category()+"_"+ttumBean.getStFile_Name();
		logger.info(query2);
		getJdbcTemplate().execute(query2);
			 
		logger.info("***** GenerateRupayTTUMDaoImpl.generateDisputeTTUM End ****");
		
		return Total_Data;
	}
	catch(Exception e)
	{
		demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.generateDisputeTTUM");
		logger.error(" error in GenerateRupayTTUMDaoImpl.generateDisputeTTUM", new Exception("GenerateRupayTTUMDaoImpl.generateDisputeTTUM",e));
		return Total_Data;
	}
	
	finally{
		if(rset!=null){
			rset.close();
		}
		if(pstmt!=null){
			pstmt.close();
		}
		if(rset1!=null){
			rset1.close();
		}
		if(pstmt1!=null){
			pstmt1.close();
		}
		if(rs!=null){
			rs.close();
		}
		if(ps!=null){
			ps.close();
		}
		if(rs1!=null){
			rs1.close();
		}
		if(ps1!=null){
			ps1.close();
		}
		if(rs2!=null){
			rs2.close();
		}
		if(ps2!=null){
			ps2.close();
		}
		if(conn!=null){
			conn.close();
		}
	}

}


@Override
public List<List<GenerateTTUMBean>> generateVisaDisputeTTUM(
		GenerateTTUMBean ttumBean) throws Exception {
	
	logger.info("***** GenerateRupayTTUMDaoImpl.generateVisaDisputeTTUM Start ****");
	
	List<GenerateTTUMBean> ttum_data = new ArrayList<>();
	List<GenerateTTUMBean> ttum_data_cbs = new ArrayList<>();
	List<GenerateTTUMBean> ttum_switch = new ArrayList<>();
	List<GenerateTTUMBean> ttum_cbs = new ArrayList<>();
	List<GenerateTTUMBean> ttum_others = new ArrayList<>();
	List<GenerateTTUMBean> Excel_headers = new ArrayList<>();
	List<String> ExcelHeaders = new ArrayList<>();
	List<String> ExcelHeaders1 = new ArrayList<>();
	List<List<GenerateTTUMBean>> Total_Data = new ArrayList<>();
	String table_cols = "DCRS_REMARKS VARCHAR (100 BYTE), CREATEDDATE DATE , CREATEDBY VARCHAR (100 BYTE),RECORDS_DATE DATE";
	String insert_cols = "DCRS_REMARKS, CREATEDDATE, CREATEDBY,RECORDS_DATE";
	String stAction = "";
	
	Connection conn=null;
	PreparedStatement pstmt =null;
	ResultSet rset=null;
	PreparedStatement pstmt1 =null;
	ResultSet rset1=null;
	PreparedStatement ps =null;
	ResultSet rs=null;
	PreparedStatement ps1 =null;
	ResultSet rs1=null;
	PreparedStatement ps2 =null;
	ResultSet rs2=null;
	
	try
	{
		logger.info("INSIDE generateSwitchTTUM");
		if(ttumBean.getStSubCategory().equalsIgnoreCase("ISSUER"))
		{
			logger.info("*** In ISSUER ***");
			ttumBean.setStGLAccount("99937200010089");//as per document
			//get CUST ACCOUNT NO FROM SETTLEMENT TABLE FOR DEBIT
			String GET_TTUM_RECORDS = "SELECT trim(ltrim(ACCTNUM,'0')) ACCTNUM,AMOUNT,PAN,TO_CHAR(TO_DATE(LOCAL_DATE,'MM/DD/YYYY'),'DDMMYY') AS LOCAL_DATE,SUBSTR(TRACE,-6,6) AS TRACE ," +
								" ACCEPTORNAME,filedate " +" FROM SETTLEMENT_"+ttumBean.getStCategory()+"_Switch" 
								+ " WHERE DCRS_REMARKS LIKE '%GENERATE-TTUM-2%'";
				
			logger.info("GET_TTUM_RECORDS=="+GET_TTUM_RECORDS);
			 conn = getConnection();
			 pstmt = conn.prepareStatement(GET_TTUM_RECORDS);
			 rset = pstmt.executeQuery();
			
			String GET_TTUM_RECORDS_CBS = "SELECT E,CONTRA_ACCOUNT,FORACID,TO_NUMBER(AMOUNT,999999999.99) AS AMOUNT, TO_CHAR(TO_DATE(VALUE_DATE,'DD/MM/YYYY'),'DDMMYY') AS VALUE_DATE,"
					+"SUBSTR(REF_NO,2,6) AS TRACE, REMARKS,PARTICULARALS,filedate,SUBSTR(REF_NO,2,6) FROM SETTLEMENT_"+ttumBean.getStCategory()+"_CBS"
					+" WHERE DCRS_REMARKS LIKE '%GENERATE-TTUM-1%'";
			
			logger.info("GET_TTUM_RECORDS_CBS "+GET_TTUM_RECORDS_CBS);
			
			//Connection conn = getConnection();
			 pstmt1 = conn.prepareStatement(GET_TTUM_RECORDS_CBS);
			 rset1 = pstmt1.executeQuery();
			
			//ExcelHeaders.add("FILE_HEADER");
			ExcelHeaders.add("ACCOUNT NUMBER");
			ExcelHeaders.add("CURRENCY CODE");
			ExcelHeaders.add("SERVICE OUTLET");
			ExcelHeaders.add("PART TRAN TYPE");
			ExcelHeaders.add("TRANSACTION AMOUNT");
			ExcelHeaders.add("TRANSACTION PARTICULARS");
			ExcelHeaders.add("REFERENCE NUMBER");
			ExcelHeaders.add("REFERENCE CURRENCY CODE");
			ExcelHeaders.add("REFERENCE AMOUNT");
			ExcelHeaders.add("REMARKS");
			ExcelHeaders.add("REPORT CODE");
			
			//modified on 25/04/2018
			ExcelHeaders1.add("ACCOUNT_NUMBER");
			ExcelHeaders1.add("CURRENCY_CODE");
			ExcelHeaders1.add("SERVICE_OUTLET");
			ExcelHeaders1.add("PART_TRAN_TYPE");
			ExcelHeaders1.add("TRANSACTION_AMOUNT");
			ExcelHeaders1.add("TRANSACTION_PARTICULARS");
			ExcelHeaders1.add("REFERENCE_NUMBER");
			ExcelHeaders1.add("REFERENCE_CURRENCY_CODE");
			ExcelHeaders1.add("REFERENCE_AMOUNT");
			ExcelHeaders1.add("REMARKS");
			ExcelHeaders1.add("ACCOUNT_REPORT_CODE");
			ExcelHeaders1.add("FILEDATE");
			
			ttumBean.setStExcelHeader(ExcelHeaders);
			
			Excel_headers.add(ttumBean);
			int count = 0;
			int count1 = 0;
			while(rset.next())
			{
				count++;
				//logger.info("count is "+count);
				GenerateTTUMBean generateTTUMBeanObj = new GenerateTTUMBean();		
				//for(int loop=0;loop<2;loop++){
					//if(loop==0){
					//CUST CR
					generateTTUMBeanObj.setStCreditAcc(rset.getString("ACCTNUM").replaceAll(" ", ""));
					//generateTTUMBeanObj.setStCreditAcc(rset.getString("ACCTNUM").split(" ")[1]);
					//GL DR
					generateTTUMBeanObj.setStDebitAcc("99937200010089");
					generateTTUMBeanObj.setStAmount(rset.getString("AMOUNT"));
					//String stTran_particulars = "REV/"+generateTTUMBean.getStCategory()+"/"+rset.getString("LOCAL_DATE")+"/"+rset.getString("TRACE")+
					String stTran_particulars = "REV/VISA/"+rset.getString("LOCAL_DATE")+"/"+rset.getString("ACCEPTORNAME").replaceAll("'", "");
					generateTTUMBeanObj.setStDate(rset.getString("LOCAL_DATE"));							
			//		generateTTUMBean.setStDate(rset.getString("LOCAL_DATE"));
					generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
					//generateTTUMBeanObj.setStRRNo(rset.getString("ISSUER"));
					
					generateTTUMBeanObj.setTrace(rset.getString("TRACE"));
					
					String remark = getJdbcTemplate().queryForObject("select 'VISI'||to_char(sysdate,'ddmmyy')||ttum_seq.nextval from dual", new Object[] {},String.class);
					generateTTUMBeanObj.setStCard_Number(remark);
					generateTTUMBeanObj.setStRemark(rset.getString("PAN"));	
					//generateTTUMBeanObj.setPart_tran_type("C");
								
					generateTTUMBeanObj.setFiledate(rset.getString("filedate"));
					ttum_data.add(generateTTUMBeanObj);
					/*}else{
						
						generateTTUMBeanObj.setStCreditAcc(rset.getString("ACCTNUM").replaceAll(" ", "").replaceAll("^0*",""));
						//generateTTUMBeanObj.setStCreditAcc(rset.getString("ACCTNUM").split(" ")[1]);
						//GL DR
						generateTTUMBeanObj.setStDebitAcc("99937200010660");
						generateTTUMBeanObj.setStAmount(rset.getString("AMOUNT"));
						//String stTran_particulars = "REV/"+generateTTUMBean.getStCategory()+"/"+rset.getString("LOCAL_DATE")+"/"+rset.getString("TRACE")+
						String stTran_particulars = "REV-T10-RPAY-"+rset.getString("LOCAL_DATE")+"-"+rset.getString("TRACE")+"-"+rset.getString("ACCEPTORNAME").replaceAll("'", "");
						generateTTUMBeanObj.setStDate(rset.getString("LOCAL_DATE"));							
				//		generateTTUMBean.setStDate(rset.getString("LOCAL_DATE"));
						generateTTUMBeanObj.setStTran_particulars(stTran_particulars);
						//generateTTUMBeanObj.setStRRNo(rset.getString("ISSUER"));
						generateTTUMBeanObj.setStCard_Number(rset.getString("PAN"));
						String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
						generateTTUMBeanObj.setStRemark(remark);
						generateTTUMBeanObj.setPart_tran_type("D");
									
						ttum_data.add(generateTTUMBeanObj);
						
					}*/
				//}
				
			}
			
			while(rset1.next())
			{
				/*if(generateTTUMBeanObj.getStSubCategory().equals("DOMESTIC"))
				{*/
					stAction = rset1.getString("E");
					GenerateTTUMBean generateTTUMBean1 = new GenerateTTUMBean();
					if(stAction.equals("C"))
					{
						//GL DEBIT
						generateTTUMBean1.setStDebitAcc(rset1.getString("FORACID"));
						//CUSTOMER ACC CREDIT
						generateTTUMBean1.setStCreditAcc(rset1.getString("CONTRA_ACCOUNT"));
						generateTTUMBean1.setStAmount(rset1.getString("AMOUNT"));
						//generateTTUMBean.setStDate(rset.getString("VALUE_DATE"));
						//String stTran_particular = "REV/"+generateTTUMBeanObj.getStCategory()+"/"+rset.getString("VALUE_DATE")+"/"+rset.getString("REF_NO");
						
						//Tran Particular � REV-T2 -RPAY-DDMMYY (Transaction date)-Trace number-Merchant name
						String stTran_particular = "REV/VISA/"+rset1.getString("VALUE_DATE")+"/"+rset1.getString("PARTICULARALS");//"/"+rset1.getString("REF_NO")+
						generateTTUMBean1.setStDate(rset1.getString("VALUE_DATE"));
						generateTTUMBean1.setStTran_particulars(stTran_particular);
						generateTTUMBean1.setTrace(rset1.getString("TRACE"));
						String remark = getJdbcTemplate().queryForObject("select 'VISI'||to_char(sysdate,'ddmmyy')||ttum_seq.nextval from dual", new Object[] {},String.class);
						generateTTUMBean1.setStCard_Number(remark);
						generateTTUMBean1.setStRemark(rset1.getString("REMARKS"));
						//generateTTUMBean1.setPart_tran_type(stAction);
						generateTTUMBean1.setFiledate(rset1.getString("filedate"));
						ttum_data_cbs.add(generateTTUMBean1);
						//Diff_Data.add(generateTTUMBean1);


					}
				
			}
			
			/*Total_Data.add(Excel_headers);
			Total_Data.add(null);*/
			//Total_Data.add(2,ttum_data);
			//Total_Data.add(3,ttum_data_cbs);
			
			
		}		
		
		
		
		// CREATE NEW TABLE FOR INSERTING TTUM ENTRIES
		for(int i = 0 ; i<ExcelHeaders1.size();i++)
		{
			table_cols =table_cols+","+ ExcelHeaders1.get(i)+" VARCHAR (100 BYTE)";
			insert_cols = insert_cols+","+ExcelHeaders1.get(i);
		}
		
		String CHECK_TABLE = "SELECT count (*) FROM tab WHERE tname  = UPPER('temp_TTUM_"+ttumBean.getStMerger_Category()+"_"+ttumBean.getStFile_Name()+"')";
		int tableExist = getJdbcTemplate().queryForObject(CHECK_TABLE, new Object[] { },Integer.class);
		
		logger.info("CHECK_TABLE=="+CHECK_TABLE);
		logger.info("tableExist=="+tableExist);
		
		if(tableExist == 0)
		{
			String CREATE_QUERY = "CREATE TABLE temp_TTUM_"+ttumBean.getStMerger_Category()
					+"_"+ttumBean.getStFile_Name()+" ("+table_cols+")";
			
			logger.info("CREATE_QUERY=="+CREATE_QUERY);
			
			getJdbcTemplate().execute(CREATE_QUERY);
		}
		
		//INSERT TTUM ENTRIES IN ABOVE CREATED TABLE
		int incount = 0;
		int count=0;
		for(int i = 0;i<ttum_data.size();i++)
		{
			incount++;
		
		//	logger.info("inserted "+incount+" records");
			GenerateTTUMBean beanObj = new GenerateTTUMBean();
			beanObj = ttum_data.get(i);
			String INSERT_QUERY = "INSERT INTO temp_TTUM_"+ttumBean.getStMerger_Category()
						+"_SWITCH"
						+"("+insert_cols+") VALUES ('"+ttumBean.getStMerger_Category()+
						"-UNRECON-TTUM-2"
						+"',SYSDATE,'"+ttumBean.getStEntry_by()+"',TO_DATE('"+beanObj.getStDate()+"','DDMMYY'),'"+beanObj.getStDebitAcc()+"','INR','999','D','"+
						beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','"+beanObj.getStCard_Number()+"','INR','"+beanObj.getStAmount()+"','"+
						beanObj.getStRemark()+"/"+beanObj.getTrace()+"','"+beanObj.getAccount_repo()+"',to_char(TO_DATE('"+ttumBean.getStDate()+"','dd/mm/yyyy'),'dd/mm/yyyy'))";
			
			logger.info("INSERT_QUERY=="+INSERT_QUERY);
			getJdbcTemplate().execute(INSERT_QUERY);
			
			 logger.info("INSERT_QUERY"+INSERT_QUERY);
			
			 INSERT_QUERY = "INSERT INTO temp_TTUM_"+ttumBean.getStMerger_Category()
					 +"_SWITCH" 
						+"("+insert_cols+") VALUES ('"+ttumBean.getStMerger_Category()
						+"-UNRECON-TTUM-2"
						+"',SYSDATE,'"+ttumBean.getStEntry_by()+"',TO_DATE('"+beanObj.getStDate()+"','DDMMYY'),'"+beanObj.getStCreditAcc()+"','INR','999','C','"+
						beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','"+beanObj.getStCard_Number()+"','INR','"+beanObj.getStAmount()+"','"+
						beanObj.getStRemark()+"/"+beanObj.getTrace()+"','"+beanObj.getAccount_repo()+"',to_char(TO_DATE('"+ttumBean.getStDate()+"','dd/mm/yyyy'),'dd/mm/yyyy'))";
			 getJdbcTemplate().execute(INSERT_QUERY);	
			 
			 logger.info("INSERT_QUERY"+INSERT_QUERY);
			 
			 
	
		}
		
		int incount1 = 0;
		for(int i = 0;i<ttum_data_cbs.size();i++)
		{
			incount1++;
			
		//	logger.info("inserted "+incount+" records");
			GenerateTTUMBean beanObj = new GenerateTTUMBean();
			beanObj = ttum_data_cbs.get(i);
			String INSERT_QUERY = "INSERT INTO temp_TTUM_"+ttumBean.getStMerger_Category()
						+"_CBS" 
						+"("+insert_cols+") VALUES ('"+ttumBean.getStMerger_Category()+
						"-UNRECON-TTUM-1"
						+"',SYSDATE,'"+ttumBean.getStEntry_by()+"',TO_DATE('"+beanObj.getStDate()+"','DDMMYY'),'"+beanObj.getStDebitAcc()+"','INR','999','D','"+
						beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','"+beanObj.getStCard_Number()+"','INR','"+beanObj.getStAmount()+"','"+
						beanObj.getStRemark()+"/"+beanObj.getTrace()+"','"+beanObj.getAccount_repo()+"',to_char(TO_DATE('"+ttumBean.getStDate()+"','dd/mm/yyyy'),'dd/mm/yyyy'))";
			
			logger.info("INSERT_QUERY=="+INSERT_QUERY);
			getJdbcTemplate().execute(INSERT_QUERY);
			
			 INSERT_QUERY = "INSERT INTO temp_TTUM_"+ttumBean.getStMerger_Category()
					 +"_CBS"
						+"("+insert_cols+") VALUES ('"+ttumBean.getStMerger_Category()
						+"-UNRECON-TTUM-1"
						+"',SYSDATE,'"+ttumBean.getStEntry_by()+"',TO_DATE('"+beanObj.getStDate()+"','DDMMYY'),'"+beanObj.getStCreditAcc()+"','INR','999','C','"+
						beanObj.getStAmount()+"','"+beanObj.getStTran_particulars()+"','"+beanObj.getStCard_Number()+"','INR','"+beanObj.getStAmount()+"','"+
						beanObj.getStRemark()+"/"+beanObj.getTrace()+"','"+beanObj.getAccount_repo()+"',to_char(TO_DATE('"+ttumBean.getStDate()+"','dd/mm/yyyy'),'dd/mm/yyyy'))";
			 
			 logger.info("INSERT_QUERY=="+INSERT_QUERY);
			 getJdbcTemplate().execute(INSERT_QUERY);	
			 
			
		}
		
		String GET_TTUM_SWITCH = "SELECT ACCOUNT_NUMBER,PART_TRAN_TYPE,TRANSACTION_AMOUNT,TRANSACTION_PARTICULARS,REMARKS,REFERENCE_NUMBER" +
				" FROM temp_TTUM_VISA_ISS_SWITCH WHERE DCRS_REMARKS LIKE '%UNRECON-TTUM-2%' and filedate = '"+ttumBean.getStDate()+"' order by REFERENCE_NUMBER,TRANSACTION_AMOUNT,ACCOUNT_NUMBER";
		
		logger.info("GET_TTUM_SWITCH=="+GET_TTUM_SWITCH);
		
	 conn = getConnection();
	 ps = conn.prepareStatement(GET_TTUM_SWITCH);
	 rs = ps.executeQuery();
	while(rs.next())
	{
		count++;
		//logger.info("count is "+count);
		GenerateTTUMBean generateTTUMBeanObj = new GenerateTTUMBean();		
		//for(int loop=0;loop<2;loop++){
			//if(loop==0){
			//CUST CR
			generateTTUMBeanObj.setStCreditAcc(rs.getString("ACCOUNT_NUMBER").replaceAll(" ", " ").replaceAll("^0*",""));
			generateTTUMBeanObj.setStDebitAcc("99937200010089");
			generateTTUMBeanObj.setStAmount(rs.getString("TRANSACTION_AMOUNT"));
			generateTTUMBeanObj.setStTran_particulars(rs.getString("TRANSACTION_PARTICULARS"));
			generateTTUMBeanObj.setStCard_Number(rs.getString("REFERENCE_NUMBER"));
			generateTTUMBeanObj.setStRemark(rs.getString("REMARKS"));	
			generateTTUMBeanObj.setPart_tran_type(rs.getString("PART_TRAN_TYPE"));
						
			ttum_switch.add(generateTTUMBeanObj);
		}
	logger.info("ttum_switch.size()"+ttum_switch.size());
		 String GET_TTUM_CBS = "SELECT ACCOUNT_NUMBER,PART_TRAN_TYPE,TRANSACTION_AMOUNT,TRANSACTION_PARTICULARS,REMARKS,REFERENCE_NUMBER" +
					" FROM temp_TTUM_VISA_ISS_CBS WHERE DCRS_REMARKS LIKE '%UNRECON-TTUM-1%' and filedate = '"+ttumBean.getStDate()+"' order by REFERENCE_NUMBER,TRANSACTION_AMOUNT,ACCOUNT_NUMBER";
			
			logger.info("GET_TTUM_RECORDS_CBS "+GET_TTUM_CBS);
			
			//Connection conn = getConnection();
			ps1 = conn.prepareStatement(GET_TTUM_CBS);
			rs1 = ps1.executeQuery();
			
			while(rs1.next())
			{
				/*if(generateTTUMBeanObj.getStSubCategory().equals("DOMESTIC"))
				{*/
					//stAction = rs1.getString("E");
					GenerateTTUMBean generateTTUMBean1 = new GenerateTTUMBean();
					
						//GL DEBIT
						generateTTUMBean1.setStDebitAcc("99937200010089");
						//CUSTOMER ACC CREDIT
						generateTTUMBean1.setStCreditAcc(rs1.getString("ACCOUNT_NUMBER"));
						generateTTUMBean1.setStAmount(rs1.getString("TRANSACTION_AMOUNT"));
						//generateTTUMBean.setStDate(rset.getString("VALUE_DATE"));
						//String stTran_particular = "REV/"+generateTTUMBeanObj.getStCategory()+"/"+rset.getString("VALUE_DATE")+"/"+rset.getString("REF_NO");
						
						//Tran Particular � REV-T2 -RPAY-DDMMYY (Transaction date)-Trace number-Merchant name
						//String stTran_particular = "REV-T2-RPAY-"+rs1.getString("VALUE_DATE")+"-"+rset1.getString("REF_NO")+"-"+rset1.getString("PARTICULARALS");
						//generateTTUMBean1.setStDate(rs1.getString("VALUE_DATE"));
						String particular = rs1.getString("TRANSACTION_PARTICULARS");
						if(particular.contains("RPAY-POS")){
							particular = particular.replace(particular.substring(15, 24), "");
						}
						generateTTUMBean1.setStTran_particulars(particular);
						//generateTTUMBean1.setStTran_particulars(rs1.getString("TRANSACTION_PARTICULARS"));
						generateTTUMBean1.setStCard_Number(rs1.getString("REFERENCE_NUMBER"));
						//String remark = getJdbcTemplate().queryForObject("select 'DCRM'||ttum_seq.nextval from dual", new Object[] {},String.class);
						generateTTUMBean1.setStRemark(rs1.getString("REMARKS"));
						generateTTUMBean1.setPart_tran_type(rs1.getString("PART_TRAN_TYPE"));
						//generateTTUMBean1.setPart_tran_type(stAction);
						ttum_cbs.add(generateTTUMBean1);
						//Diff_Data.add(generateTTUMBean1);


					}
			
			logger.info("ttum_cbs.size()"+ttum_cbs.size());
			String GET_TTUM_OTHER = "SELECT PAN,TRACE,LOCAL_DATE,AMOUNT_EQUIV FROM TEMP_VISA_DISPUTE where filedate = to_char(to_date('"+ttumBean.getStDate()+"','dd/mm/yyyy'),'dd-mon-yyyy')";
			
			logger.info("GET_TTUM_RECORDS_OTHER "+GET_TTUM_OTHER);
			
			//Connection conn = getConnection();
			ps2 = conn.prepareStatement(GET_TTUM_OTHER);
			rs2 = ps2.executeQuery();
			
			while(rs2.next())
			{
			
					GenerateTTUMBean generateTTUMBean1 = new GenerateTTUMBean();
					
						
						generateTTUMBean1.setPan(rs2.getString("pan"));
						generateTTUMBean1.setTrace(rs2.getString("trace"));
						generateTTUMBean1.setLocal_date(rs2.getString("local_date"));
						generateTTUMBean1.setAmount_equiv(rs2.getString("amount_equiv"));
						
						ttum_others.add(generateTTUMBean1);
						//Diff_Data.add(generateTTUMBean1);


					}
		
			Total_Data.add(Excel_headers);
			Total_Data.add(null);
			Total_Data.add(2,ttum_switch);
			Total_Data.add(3,ttum_cbs);
			Total_Data.add(4,ttum_others);
		
		//UPDATE TTUM GENERATED RECORDS
		String value = "";
		for(int loop=1;loop<=2;loop++){
			if(loop == 1){
				value="CBS";
			}else{
				value="SWITCH";
			}
				String UPDATE_RECORDS = "UPDATE SETTLEMENT_"+ttumBean.getStCategory()+"_"+value+
						" SET DCRS_REMARKS = '"+ttumBean.getStMerger_Category()+"-UNRECON-GENERATED-TTUM-"+loop+"'"
						+" WHERE DCRS_REMARKS = '"+ttumBean.getStMerger_Category()+"-UNRECON-GENERATE-TTUM-"+loop+"'";
				
				String insert_Main_ttum = "insert into TTUM_"+ttumBean.getStMerger_Category()+"_"+value+""
						+ "(select * from temp_TTUM_"+ttumBean.getStMerger_Category()+"_"+value+")";
				
				
				String truncate_temp_ttum = "truncate table temp_TTUM_"+ttumBean.getStMerger_Category()+"_"+value+"";
				
				logger.info("UPDATE_RECORDS "+UPDATE_RECORDS);
				logger.info("insert_Main_ttum "+insert_Main_ttum);
				logger.info("truncate_temp_ttum "+truncate_temp_ttum);

				getJdbcTemplate().execute(UPDATE_RECORDS);
				
				getJdbcTemplate().update(insert_Main_ttum);
				
				getJdbcTemplate().update(truncate_temp_ttum );
				
				logger.info("***** GenerateRupayTTUMDaoImpl.generateVisaDisputeTTUM End ****");
				
		}
			 
		return Total_Data;
	}
	catch(Exception e)
	{
		demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.generateVisaDisputeTTUM");
		logger.error(" error in GenerateRupayTTUMDaoImpl.generateVisaDisputeTTUM", new Exception("GenerateRupayTTUMDaoImpl.generateVisaDisputeTTUM",e));
		return Total_Data;
	}
	
	finally{
		if(rset!=null){
			rset.close();
		}
		if(pstmt!=null){
			pstmt.close();
		}
		if(rset1!=null){
			rset1.close();
		}
		if(pstmt1!=null){
			pstmt1.close();
		}
		if(rs!=null){
			rs.close();
		}
		if(ps!=null){
			ps.close();
		}
		if(rs1!=null){
			rs1.close();
		}
		if(ps1!=null){
			ps1.close();
		}
		if(rs2!=null){
			rs2.close();
		}
		if(ps2!=null){
			ps2.close();
		}
		if(conn!=null){
			conn.close();
		}
	}
	


}



}
