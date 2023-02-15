package com.recon.control;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.recon.model.CompareBean;
import com.recon.model.CompareSetupBean;
import com.recon.model.GenerateTTUMBean;
import com.recon.model.LoginBean;
import com.recon.model.Mastercbs_respbean;
import com.recon.service.AccountingService;
import com.recon.service.GenerateRupayTTUMService;
import com.recon.service.GenerateTTUMService;
import com.recon.service.ISourceService;
import com.recon.util.CSRFToken;
import com.recon.util.FileDetailsJson;
import com.recon.util.demo;

@Controller
public class GenerateTTUMController {

	@Autowired
	GenerateTTUMService generateTTUMservice;
	
	@Autowired
	GenerateRupayTTUMService generateRupayTTUMService;
	
	@Autowired
	AccountingService accountingService;
	@Autowired ISourceService iSourceService;
	
	private static final Logger logger = Logger.getLogger(GenerateTTUMController.class);
	
	@RequestMapping(value="GenerateTTUM", method=RequestMethod.GET)
	public String getComparePage(ModelAndView modelAndView, GenerateTTUMBean generateTTUMBean,@RequestParam("category")String category,Model model,HttpServletRequest request) throws Exception
	{
		 logger.info("***** GenerateTTUMController.getComparePage Start ****");
		 
		List<String> subcat = new ArrayList<>();
		List<String> respcode = new ArrayList<>();
		List<CompareSetupBean> setupBeans = new ArrayList<CompareSetupBean>();
		List<String> files = new ArrayList<>();
		String display = "";
		
		
		 
		 logger.info("in GetHeaderList"+category);
		 
         subcat = iSourceService.getSubcategories(category);
        
         
         if(category.equals("ONUS") || category.equals("AMEX") ||category.equals("CARDTOCARD")||category.equals("ONUSPOS") ) {
        	 
        	 setupBeans =  iSourceService.getFileList(category,"-");
        	for(CompareSetupBean bean :setupBeans) {
        		
        		files.add(bean.getStFileName());
        		
        		display="none";
        	}
        	
        	
        	
         }
         System.out.println("-----"+subcat);

         logger.info("-----"+subcat);

        // String csrf = CSRFToken.getTokenForSession(request.getSession());
		 
  		//redirectAttributes.addFlashAttribute("CSRFToken", csrf);
  	//	modelAndView.addObject("CSRFToken", csrf);
         model.addAttribute("category", category);
         model.addAttribute("subcategory",subcat );
         model.addAttribute("respcode",respcode );

         model.addAttribute("files",files );
         model.addAttribute("display", display);

         
         logger.info("***** GenerateTTUMController.getComparePage End ****");

 		//modelAndView.setViewName("GenerateTTUM");
 		return "GenerateTTUM";

	}
	
	
	@RequestMapping(value="GenerateTTUM" ,method=RequestMethod.POST)
	public String  compareData(@ModelAttribute("generateTTUMBean") GenerateTTUMBean generateTTUMBean, RedirectAttributes redirectAttributes,HttpSession httpsession,Model model) throws Exception
	{
		 logger.info("***** GenerateTTUMController.compareData Start ****");
		try
		{
			generateTTUMBean.setStEntry_by(((LoginBean) httpsession.getAttribute("loginBean")).getUser_id());
			List<String> Table_list = new ArrayList<>();
			generateTTUMBean.setStFile_Name(generateTTUMBean.getStSelectedFile());
			//String Catego=generateTTUMBean.getStCategorynew();
			logger.info(generateTTUMBean.getStCategory());
			logger.info(generateTTUMBean.getStSelectedFile());
			String stCategory = generateTTUMBean.getStCategory();
			if(!generateTTUMBean.getStSubCategory().equalsIgnoreCase("-"))
			{
				stCategory = generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStSubCategory();
				generateTTUMBean.setStMerger_Category(generateTTUMBean.getStCategory()+"_"+generateTTUMBean.getStSubCategory().substring(0, 3));
				//compareBean.setStSub_Category_substr(compareBean.getStSubCategory().substring(0,3));
			}
			else
			{
				stCategory = generateTTUMBean.getStCategory();
				generateTTUMBean.setStMerger_Category(generateTTUMBean.getStCategory());
			}
			
			//GET LATEST DATE OF FILE PROCESSED
			String stFileDate=null;
			
			
				stFileDate = generateRupayTTUMService.getLatestFileDate(generateTTUMBean);
				
				
			logger.info("stFileDate=="+stFileDate);
			generateTTUMBean.setStFile_Date(stFileDate);
			//PUT VALIDATION FOR MANUAL FILE PROCESSED?
			/**
			 * IF YES THEN GENERATE TTUM ELSE THROW ERROR TO PROCESS MAN FILE
			 */
			
			
			if(generateTTUMBean.getStCategory().equals("ONUS"))
			{
				logger.info("*** In ONUS ***");
				String table_name = generateTTUMBean.getStSelectedFile();
				logger.info("table_name=="+table_name);
				if(table_name.equals("CBS"))
				{
					Table_list.add("CBS");
					Table_list.add("SWITCH");
					
				//	generateTTUMservice.TTUMRecords(Table_list, generateTTUMBean);
				}
				List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateTTUM(generateTTUMBean);

				model.addAttribute("generate_ttum",generatettumObj);

				return "GenerateAmexTTUMExcel";
				//return "generateTTUMExcel";
			}
			if(generateTTUMBean.getStCategory().equals("CARDTOCARD"))
			{
				logger.info("*** In CARDTOCARD ***");
				List<List<GenerateTTUMBean>> generatettumObj=null;
				String table_name = generateTTUMBean.getStSelectedFile();
				if(generateTTUMBean.getInRec_Set_Id() == 5)
				{
			generatettumObj = generateTTUMservice.generateTTUMForCARDTOCARD(generateTTUMBean);
				}else if(generateTTUMBean.getInRec_Set_Id() == 6){
					generatettumObj = generateTTUMservice.generateTTUMForCARDTOCARD(generateTTUMBean);
				}

				model.addAttribute("generate_ttum",generatettumObj);

				return "GenerateCardtocard_ttum";
				//return "generateTTUMExcel";
			}
			else if(generateTTUMBean.getStCategory().equals("AMEX"))
			{
				logger.info("*** In AMEX ***");
				List<List<GenerateTTUMBean>> generatettumObj = new ArrayList<>();
				String table_name1 = generateTTUMBean.getStSelectedFile();
				String table_name="CBS";
				logger.info(table_name1);
				if(table_name1.equals("CBS"))
				{
					Table_list.add("CBS");
					Table_list.add("SWITCH");
					
					//generateTTUMservice.TTUMRecords(Table_list, generateTTUMBean);
					generatettumObj = generateTTUMservice.generateTTUMForAMEX(generateTTUMBean);
				}	
				else if(table_name1.equals("SWITCH"))
				{
					
				}
				//List<GenerateTTUMBean> generatettumObj = generateTTUMservice.generateTTUM(generateTTUMBean);
				

				model.addAttribute("generate_ttum",generatettumObj);

				return "GenerateAmexTTUMExcel";
			
				
			}
			
			
			else if(generateTTUMBean.getStCategory().equals("RUPAY"))
			{
				logger.info("*** In RUPAY ***");
				if(generateTTUMBean.getStSubCategory().equals("DOMESTIC"))
				{
					logger.info("*** In DOMESTIC ***");
					//if(generateTTUMBean.getStFile_Name().equals("SWITCH"))
					if(generateTTUMBean.getInRec_Set_Id() == 1)
					{
						logger.info("*** In Rec Set Id - 1 ***");
						if(generateTTUMBean.getStSelectedFile().equals("SWITCH"))
						{

							//1.update remarks as generate TTUM
							generateRupayTTUMService.getTTUMSwitchRecords(generateTTUMBean);

							//2. Then generate its TTUM
							List<List<GenerateTTUMBean>> Data = generateRupayTTUMService.generateSwitchTTUM(generateTTUMBean,1);
							
							//generateRupayTTUMService.generateTTUM(Data); // ttum in text file method

							model.addAttribute("generate_ttum", Data);
						}
						else if(generateTTUMBean.getStSelectedFile().equals("CBS"))
						{
							//IF BOTH C AND D ARE PRESENT WITH DIFFERENT AMOUNT 
							List<GenerateTTUMBean> TTUM_data = generateRupayTTUMService.getCandDdifference(generateTTUMBean);

							//FOR SINGLE PART TRAN TYPE C
							//generateRupayTTUMService.TTUMRecords(generateTTUMBean);//commented because it is already done in manual file
							//we have to update those records as generatettum entries
							
							generateRupayTTUMService.getFailedCBSRecords(generateTTUMBean);

							//FOR SINGLE PART TRAN TYPE D WITH PROCESSED C PART
							generateRupayTTUMService.TTUM_forDPart(generateTTUMBean);


							//generate TTUM
							List<List<GenerateTTUMBean>> Data = generateRupayTTUMService.generateCBSTTUM(generateTTUMBean,TTUM_data);

							model.addAttribute("generate_ttum", Data);

						}
					}
					else if(generateTTUMBean.getInRec_Set_Id() == 2 )
					{
						logger.info("*** In Rec Set Id - 2 ***");
						//if(generateTTUMBean.getStSelectedFile().equals("REPORT-C"))
						if(generateTTUMBean.getStSelectedFile().equals("SWITCH"))
						{
							generateTTUMBean.setStFile_Name("SWITCH");
							// changes made by jyoti and minakshi on 04/25/2018
							//UNCOMMENTED BY INT8624 FOR CYCLE-2 SWITCH TTUM
							generateRupayTTUMService.getReportCRecords(generateTTUMBean);
							
							//2. Then generate its TTUM
							List<List<GenerateTTUMBean>> Data = generateRupayTTUMService.generateSwitchTTUM(generateTTUMBean,2);
							logger.info("got the data.....");
							model.addAttribute("generate_ttum", Data);
						}
						else if(generateTTUMBean.getStSelectedFile().equals("RUPAY"))
						{
							generateRupayTTUMService.getRupayTTUMRecords(generateTTUMBean);
							
							List<List<GenerateTTUMBean>> Data = generateRupayTTUMService.GenerateRupayTTUM(generateTTUMBean, 2);
							
							model.addAttribute("generate_ttum", Data);
						}
						
						
					}
					//REPORT E TTUM
					else if(generateTTUMBean.getInRec_Set_Id() == 3)
					{
						logger.info("*** In Rec Set Id - 3 ***");
						generateTTUMBean.setStSubCategory("SURCHARGE");
						generateTTUMBean.setStMerger_Category("RUPAY_SUR");
						//if(generateTTUMBean.getStSelectedFile().equals("REPORT-E"))
						if(generateTTUMBean.getStSelectedFile().equals("SWITCH"))
						{
							generateTTUMBean.setStFile_Name("SWITCH");	
							//GET SURCHARGE LOGIC FROM ACCOUNTINGDAOIMPL CODE
							generateRupayTTUMService.getReportERecords(generateTTUMBean);
							List<List<GenerateTTUMBean>> Data = accountingService.getReportERecords(generateTTUMBean,generateTTUMBean.getInRec_Set_Id());
							model.addAttribute("generate_ttum", Data);
							/*generateRupayTTUMService.getReportERecords(generateTTUMBean);
							List<List<GenerateTTUMBean>> Data = generateRupayTTUMService.generateSwitchTTUM(generateTTUMBean,3);
							model.addAttribute("generate_ttum", Data);*/
							
							//SURCHARGE CAPPING LOGIC
							//call getReportE method of generaterupayttumdaoimpl file
							//then go for capping
							
						}
						else if(generateTTUMBean.getStSelectedFile().equals("CBS"))
						{
							generateTTUMBean.setStSubCategory("SURCHARGE");
							generateRupayTTUMService.getSurchargeRecords(generateTTUMBean);

							List<List<GenerateTTUMBean>> Data = generateRupayTTUMService.generateCBSSurchargeTTUM(generateTTUMBean);
							model.addAttribute("generate_ttum", Data);
						}


					}
				//TTUM FOR MATCHED RECORDS OF SURCHARGE LEVEL / 3RD CYCLE
				/*else if(generateTTUMBean.getStSubCategory().equals("SURCHARGE") )
				{*/
					else if(generateTTUMBean.getInRec_Set_Id() == -1)
					{
						generateTTUMBean.setStSubCategory("SURCHARGE");
						List<List<GenerateTTUMBean>> Data = generateRupayTTUMService.getMatchedRecordsTTUM(generateTTUMBean);
						model.addAttribute("generate_ttum", Data);
					}
					
			//	}
				}
				else if(generateTTUMBean.getStSubCategory().equals("INTERNATIONAL"))
				{
					logger.info("*** In INTERNATIONAL ***");
					//if(generateTTUMBean.getStFile_Name().equals("SWITCH"))
					if(generateTTUMBean.getInRec_Set_Id() == 1)
					{
						if(generateTTUMBean.getStSelectedFile().equals("SWITCH"))
						{

							//1.update remarks as generate TTUM
							generateRupayTTUMService.getTTUMSwitchRecords(generateTTUMBean);

							//2. Then generate its TTUM
							List<List<GenerateTTUMBean>> Data = generateRupayTTUMService.generateSwitchTTUM(generateTTUMBean,1);

							model.addAttribute("generate_ttum", Data);
						}
						if(generateTTUMBean.getStSelectedFile().equals("CBS"))
						{
							//IF BOTH C AND D ARE PRESENT WITH DIFFERENT AMOUNT 
							List<GenerateTTUMBean> TTUM_data = generateRupayTTUMService.getCandDdifference(generateTTUMBean);

							//FOR SINGLE PART TRAN TYPE C
							generateRupayTTUMService.TTUMRecords(generateTTUMBean);

							//FOR SINGLE PART TRAN TYPE D WITH PROCESSED C PART
							generateRupayTTUMService.TTUM_forDPart(generateTTUMBean);


							//generate TTUM
							List<List<GenerateTTUMBean>> Data = generateRupayTTUMService.generateCBSTTUM(generateTTUMBean,TTUM_data);

							model.addAttribute("generate_ttum", Data);

						}
					}
					else if(generateTTUMBean.getInRec_Set_Id() == 2 )
					{
						logger.info("*** In RecSet Id - 2 ***");
						if(generateTTUMBean.getStSelectedFile().equals("REPORT-C"))
						{
							generateTTUMBean.setStFile_Name("SWITCH");
							generateRupayTTUMService.getReportCRecords(generateTTUMBean);
							
							//2. Then generate its TTUM
							List<List<GenerateTTUMBean>> Data = generateRupayTTUMService.generateSwitchTTUM(generateTTUMBean,2);
							
							model.addAttribute("generate_ttum", Data);
						}
						else if(generateTTUMBean.getStSelectedFile().equals("RUPAY"))
						{
							generateRupayTTUMService.getRupayTTUMRecords(generateTTUMBean);
							
							List<List<GenerateTTUMBean>> Data = generateRupayTTUMService.GenerateRupayTTUM(generateTTUMBean, 2);
							
							model.addAttribute("generate_ttum", Data);
						}
						
						
					}
					else if(generateTTUMBean.getInRec_Set_Id() == 4)//FOR INTERNATIONAL MATCHED RECORDS
					{
						logger.info("*** In RecSet Id - 4 ***");
						generateTTUMBean.setStFile_Name("RUPAY");
						
						//GAIN OR LOSS TTUM
						List<List<GenerateTTUMBean>> Data = generateRupayTTUMService.getMatchedIntTxn(generateTTUMBean, generateTTUMBean.getInRec_Set_Id());
						
						//LEVY CHARGES TTUM
						Data = generateRupayTTUMService.LevyCharges(Data, generateTTUMBean);
						
						model.addAttribute("generate_ttum", Data);						
					}
				
					
				}
					
				
				
				
				return "generateRupayTTUMExcel";
			}else if(generateTTUMBean.getStCategory().equals("CASHNET")){
				logger.info("*** In CASHNET ***");
				if(generateTTUMBean.getStSubCategory().equals("ISSUER")) {
					logger.info("*** In ISSUER ***");
					
					String table_name = generateTTUMBean.getStSelectedFile();
					if(table_name.equals("CBS"))
					{
						Table_list.add("CBS");
						Table_list.add("SWITCH");
						
						List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generatecashnetTTUM(generateTTUMBean);

						model.addAttribute("generate_ttum",generatettumObj);

						return "GenerateCashnetTTUMExcel";
						
					//	generateTTUMservice.TTUMRecords(Table_list, generateTTUMBean);
					}else if(table_name.equals("SWITCH")) { 
						
						List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generatecashnetTTUM(generateTTUMBean);

						model.addAttribute("generate_ttum",generatettumObj);

						return "GenerateCashnetTTUMExcel";
					
					} else {
						
						return "GenerateCashnetTTUMExcel";
						
						
					}
					
					
				}else if(generateTTUMBean.getStSubCategory().equals("ACQUIRER")) {
					logger.info("*** In ACQUIRER ***");
					
					String table_name = generateTTUMBean.getStSelectedFile();
					if(table_name.equals("CBS"))
					{
						Table_list.add("CBS");
						Table_list.add("SWITCH");
						
						List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generatecashnetTTUM(generateTTUMBean);

						model.addAttribute("generate_ttum",generatettumObj);

						return "GenerateCashnetTTUMExcel";
						
					//	generateTTUMservice.TTUMRecords(Table_list, generateTTUMBean);
					}else if(table_name.equals("SWITCH")) { 
						
						List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generatecashnetTTUM(generateTTUMBean);

						model.addAttribute("generate_ttum",generatettumObj);

						return "GenerateCashnetTTUMExcel";
					
					}if(table_name.equals("CASHNET"))
					{
						Table_list.add("CBS");
						Table_list.add("SWITCH");
						Table_list.add("CASHNET");
						
						List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generatecashnetTTUM(generateTTUMBean);

						model.addAttribute("generate_ttum",generatettumObj);

						return "GenerateCashnetTTUMExcel";
						
					//	generateTTUMservice.TTUMRecords(Table_list, generateTTUMBean);
					} else {
						
						return "GenerateCashnetTTUMExcel";
						
						
					}
					
					
				}
				else {
					
					return "GenerateCashnetTTUMExcel";
					
					
				}
				

			}/*else if(generateTTUMBean.getStCategory().equals("NFS")){
				logger.info("*** In NFS ***");
				if(generateTTUMBean.getStSubCategory().equals("ISSUER")) {
					
					
					return "GenerateCashnetTTUMExcel";
					
				}else if(generateTTUMBean.getStSubCategory().equals("ACQUIRER")) { 
					
					String table_name = generateTTUMBean.getStSelectedFile();
					
					if(table_name.equals("REV_REPORT")) {
						
						List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateNFSTTUM(generateTTUMBean);

						model.addAttribute("generate_ttum",generatettumObj);

						return "GenerateCashnetTTUMExcel";
						
						
					}else {
						
						return "GenerateCashnetTTUMExcel";
					}
					
					
				} else {
					
					return "GenerateCashnetTTUMExcel";
				
					
				}
				
			}*/
			else if(generateTTUMBean.getStCategory().equals("NFS")){
				logger.info("*** In NFS ***");
				if(generateTTUMBean.getStSubCategory().equals("ISSUER")) {
					
					
					return "GenerateCashnetTTUMExcel";
					
				}else if(generateTTUMBean.getStSubCategory().equals("ACQUIRER")) { 
					
					String table_name = generateTTUMBean.getStSelectedFile();
					
					if(table_name.equals("REV_REPORT")) {
						
						List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateNFSTTUM(generateTTUMBean);

						model.addAttribute("generate_ttum",generatettumObj);

						return "GenerateCashnetTTUMExcel";
						
						
					}else {
						
						return "GenerateCashnetTTUMExcel";
					}
					
					
				} else {
					
					return "GenerateCashnetTTUMExcel";
				
					
				}
				
			}

			
/*written by jiten-Start */
			
			else if(generateTTUMBean.getStCategory().equals("NFS")){
				logger.info("*** In NFS ***");
				if(generateTTUMBean.getStSubCategory().equals("ISSUER")) {
					logger.info("*** NFS ISSUER  ***");
					String tb_name = generateTTUMBean.getStSelectedFile();
				if(	tb_name.equals("CBS")){
					Table_list.add("CBS");
					Table_list.add("SWITCH");
					List<List<GenerateTTUMBean>>genList=generateTTUMservice.generateNFSTTUM(generateTTUMBean);
					model.addAttribute("generate_ttum",genList);
					
					return "GenerateCashnetTTUMExcel";
					
				}
				else if(tb_name.equals("SWITCH")) {
					List<List<GenerateTTUMBean>>genList=generateTTUMservice.generateNFSTTUM(generateTTUMBean);
					model.addAttribute("generate_ttum",genList);
					return "GenerateCashnetTTUMExcel";
					} else {
						return "GenerateCashnetTTUMExcel";
					}					
					
					
				}else if(generateTTUMBean.getStSubCategory().equals("ACQUIRER")) { 
					logger.info("*** In ACQUIRER ***");
					String table_name = generateTTUMBean.getStSelectedFile();
					if(table_name.equals("CBS"))
					{
						Table_list.add("CBS");
						Table_list.add("SWITCH");
						List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateNFSTTUM(generateTTUMBean);

						model.addAttribute("generate_ttum",generatettumObj);

						return "GenerateCashnetTTUMExcel";
						 
					}
					else if(table_name.equals("SWITCH"))
							{
						
						List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateNFSTTUM(generateTTUMBean);
						model.addAttribute("generate_ttum",generatettumObj);

						return "GenerateCashnetTTUMExcel";
					}
					
					if(table_name.equals("NFS")){
						

						Table_list.add("CBS");
						Table_list.add("SWITCH");
						Table_list.add("NFS");
						
						List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateNFSTTUM(generateTTUMBean);

						model.addAttribute("generate_ttum",generatettumObj);						
						return "GenerateCashnetTTUMExcel";
					}
					
					if(table_name.equals("REV_REPORT")) {
						
						List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateNFSTTUM(generateTTUMBean);

						model.addAttribute("generate_ttum",generatettumObj);

						return "GenerateCashnetTTUMExcel";
												
					}else {
						 
						return "GenerateCashnetTTUMExcel";
					}
				} else {
					
					return "GenerateCashnetTTUMExcel";
									
				}
				
			} 
			/*written by jiten-end */
			
			
			
			else if(generateTTUMBean.getStCategory().equals("MASTERCARD"))
			{
				logger.info("*** In MASTERCARD ***");
				if(generateTTUMBean.getStSubCategory().equals("ISSUER"))
				{
					logger.info("*** In ISSUER ***");
					if(generateTTUMBean.getInRec_Set_Id() == 1)
					{
						logger.info("*** In Rec Set Id - 1 ***");
						if(generateTTUMBean.getStSelectedFile().equalsIgnoreCase("SWITCH"))
						{
							List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateTTUMForMastercard_Switch(generateTTUMBean);

							model.addAttribute("generate_ttum",generatettumObj);
							
								return "Mastercard_Iss_Switch";
						}
						else if(generateTTUMBean.getStSelectedFile().equalsIgnoreCase("CBS"))
						{
							List<List<Mastercbs_respbean>> generatettumObj = generateTTUMservice.generateTTUMForMastercard_Iss_cbs(generateTTUMBean);

							model.addAttribute("generate_ttum",generatettumObj);
							//generateTTUMBean=null;
							
								return "Mastercard_Iss_Cbs";
						}
						
					}
					else if(generateTTUMBean.getInRec_Set_Id() == 2)
					{
						logger.info("*** In Rec Set Id - 2 ***");
						if(generateTTUMBean.getStSelectedFile().equalsIgnoreCase("SWITCH"))
						{
							
						}
						else if(generateTTUMBean.getStSelectedFile().equalsIgnoreCase("CBS"))
						{
							List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateTTUMForMastercard_C_Repo(generateTTUMBean);
							
							model.addAttribute("generate_ttum",generatettumObj);
								return "Mastercard_c_excel";
						}
						else if(generateTTUMBean.getStSelectedFile().equalsIgnoreCase("POS"))
						{
							
							List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateTTUMForMastercard_Issuer(generateTTUMBean);

							model.addAttribute("generate_ttum",generatettumObj);
							//generateTTUMBean=null;
							
								return "Mastercard_Issuer_Excel";
						}
						else if(generateTTUMBean.getStSelectedFile().equalsIgnoreCase("ATM"))
						{
							List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateTTUMForMastercard_C_Repo(generateTTUMBean);
							
							model.addAttribute("generate_ttum",generatettumObj);
								return "Mastercard_c_excel";
						}
						
				}
					
					else if(generateTTUMBean.getInRec_Set_Id() == 3)
					{
						logger.info("*** In Rec Set Id - 3 ***");
						if(generateTTUMBean.getStSelectedFile().equalsIgnoreCase("SWITCH"))
						{
							List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateTTUMForMastercard(generateTTUMBean);

							model.addAttribute("generate_ttum",generatettumObj);
							
								return "GenerateMastcard_SurchExcel";
						}
						
						
				}
					
					}
				else if(generateTTUMBean.getStSubCategory().equals("ACQUIRER"))
				{
					logger.info("*** In ACQUIRER ***");
					if(generateTTUMBean.getInRec_Set_Id() == 1)
					{
						logger.info("*** In Rec Set Id - 1 ***");
						if(generateTTUMBean.getStSelectedFile().equalsIgnoreCase("SWITCH"))
						{
							List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateTTUMForMastercard_Switch(generateTTUMBean);

							model.addAttribute("generate_ttum",generatettumObj);
							
								return "Mastercard_Iss_Switch";
						}
						else if(generateTTUMBean.getStSelectedFile().equalsIgnoreCase("CBS"))
						{
							List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateTTUMForMastercard_Acq_cbs(generateTTUMBean);

							model.addAttribute("generate_ttum",generatettumObj);
							
								return "Mastercard_Acq_Cbs";
						}
						
					}
					if(generateTTUMBean.getInRec_Set_Id() == 2)
					{
						logger.info("*** In Rec Set Id - 2 ***");
						if(generateTTUMBean.getStSelectedFile().equalsIgnoreCase("SWITCH"))
						{
							List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateTTUMForMastercard_Switch(generateTTUMBean);

							model.addAttribute("generate_ttum",generatettumObj);
							
								return "Mastercard_Iss_Switch";
						}
						else if(generateTTUMBean.getStSelectedFile().equalsIgnoreCase("DCC"))
						{
							//List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateTTUMForMastercard_ATM_DCC(generateTTUMBean);
							List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateTTUMForMastercard_Switch(generateTTUMBean);
							model.addAttribute("generate_ttum",generatettumObj);
							
								return "Mastercard_Acq_switch";
						}
						else if(generateTTUMBean.getStSelectedFile().equalsIgnoreCase("ATM"))
						{
							//List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateTTUMForMastercard_ATM_DCC(generateTTUMBean);
							List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateTTUMForMastercard_Switch(generateTTUMBean);
							model.addAttribute("generate_ttum",generatettumObj);
							
								return "Mastercard_Acq_switch";
						}
						
						
					}
				}
				
				return "generateRupayTTUMExcel";
			}
			else if(generateTTUMBean.getStCategory().equals("VISA"))
			{
				
				logger.info("*** In VISA ***");
				
				if(generateTTUMBean.getStFile_Name().equals("CBS")) {
					
					List<List<GenerateTTUMBean>> generatettumObj =null;
					generatettumObj = generateTTUMservice.generateVISATTUM(generateTTUMBean);
					 
					model.addAttribute("generate_ttum",generatettumObj);
					
				}
				
				else if(generateTTUMBean.getStFile_Name().equals("SWITCH"))
				{
					List<List<GenerateTTUMBean>> generatettumObj =null;
					
					if(generateTTUMBean.getInRec_Set_Id()==7 ) {
						
						 generatettumObj = generateTTUMservice.generateVISATTUM(generateTTUMBean);
						 
						
						
					} else {
					
					 generatettumObj = generateTTUMservice.generateVISATTUM(generateTTUMBean);
					}
					

					model.addAttribute("generate_ttum",generatettumObj);
					
					
				}else  if(generateTTUMBean.getStFile_Name().equals("VISA"))
				{
					List<List<GenerateTTUMBean>> generatettumObj = generateTTUMservice.generateVISATTUM(generateTTUMBean);;

					model.addAttribute("generate_ttum",generatettumObj);
					
					
				}
				
				
					
				 logger.info("***** GenerateTTUMController.compareData End ****");
				
				return "GenerateCashnetTTUMExcel";
			}
			else
			{
				return "generateTTUMExcel";
			}
			
			
		}
		catch(Exception e)
		{
			 demo.logSQLException(e, "GenerateTTUMController.compareData");
			 logger.error(" error in GenerateTTUMController.compareData", new Exception("GenerateTTUMController.compareData",e));
			return "GenerateTTUM";
		}
	}


	
	
	 @RequestMapping(value = "/getRespCode", method = RequestMethod.POST)
	 @ResponseBody
	 public  FileDetailsJson getSubCategory (@RequestParam("category")String category,@RequestParam("subcat")String subcategory,@RequestParam("filename") String filename,@RequestParam("filedate") String filedate,FileDetailsJson dataJson) throws Exception{
		 logger.info("***** GenerateTTUMController.getSubCategory Start ****");
		 try{
		 JSONObject objJSON = new JSONObject();
//			List<CompareSetupBean> setupBeans = new ArrayList<CompareSetupBean>();
		 List<Integer> respCode = new ArrayList<>();
		 List<Integer> respCode2 = new ArrayList<>();
			 
			 logger.info("in GetHeaderList"+category);
			 HashMap<String, Object> JSONROOT = new HashMap<String, Object>();
	         Gson gson = new GsonBuilder().setPrettyPrinting().create();
			 
	        
	         if(category.equalsIgnoreCase("MASTERCARD"))
	         {
	        	 respCode=generateTTUMservice.getRespcode(category, subcategory, filename, filedate);
	         }
	         else{
	        	 respCode = generateTTUMservice.getRespCode(category, subcategory, filename, null);
	         }
			// setupBeans =  iSourceService.getFileList(category,subcat);
	         
			/* JSONROOT.put("Records", setupBeans);
			 String jsonArray = gson.toJson(JSONROOT);
			 dataJson.setParams("OK", setupBeans,0);
			 
			 objJSON.put("setupBeans", setupBeans);*/
	         JSONROOT.put("respCode", respCode);
			 String jsonArray = gson.toJson(JSONROOT);
			// dataJson.setParams("OK", subcat,0);
			 dataJson.setParams("OK", 0 , respCode,null);
			 
			 objJSON.put("respCode", respCode);
			 
			 
			 logger.info("***** GenerateTTUMController.getSubCategory End ****");
			 
		 }catch(Exception e){
			 demo.logSQLException(e, "GenerateTTUMController.getSubCategory");
			 logger.error(" error in GenerateTTUMController.getSubCategory", new Exception("GenerateTTUMController.getSubCategory",e));
			 //logger.error(e.getMessage());
			 dataJson.setParams("ERROR", e.getMessage());
			 
		 }
			 
			 
			return dataJson;
			
		 
	 }
	
	
	
	
	

}
