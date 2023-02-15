package com.recon.auto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;

import com.recon.util.OracleConn;

public class ReadUCONewCBSData {
	public static String stLine;

	public static void main(String[] args) {


		
		int lineNumber = 0,sr_no = 1, batchNumber = 0, batchSize = 0, startLine = 1;
		boolean batchExecuted = false;
		
		String InsertQuery = "INSERT INTO CBS_UCO_RAWDATA(SOLID, TRAN_AMOUNT, PART_TRAN_TYPE, CARD_NUMBER, RRN, ACC_NUMBER, SYSDT, SYS_TIME, CMD, TRAN_TYPE, DEVICE_ID, TRAN_ID, TRAN_DATE, FEE, SRV_TAX, SRV_CHRG, VISAIND, CREATEDBY, FILEDATE) "+
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TO_DATE(?,'DD/MM/YYYY'))";
		
		try
		{
			File file = new File("D:\\bhagyashree\\SHAREFOLDER\\ATMH_FIN10_SIM2_08122021.txt");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			OracleConn oracObj = new OracleConn();
			Connection conn = oracObj.getconn();
			PreparedStatement ps = conn.prepareStatement(InsertQuery);
			
			while((stLine = br.readLine()) != null)
			{
				if(startLine >= 12)
				{
					if(!stLine.trim().contains("-----------------------------") && !stLine.trim().equals(""))
					{
						stLine = stLine.substring(10);

						lineNumber++;
						batchExecuted = false;
						sr_no = 1 ;

						String[] splitData = stLine.split("\\|");

						for(int i = 1; i <= splitData.length ; i++)
						{
							if(i != 13)
							{
								//System.out.println("i "+i+" data "+splitData[i-1]);
								ps.setString(sr_no++, splitData[i-1]);
							}
						}

						//System.out.println(sr_no);
						ps.setString(sr_no++, "INT");
						ps.setString(sr_no++, "28/12/2021");

						ps.addBatch();
						batchSize++;

						if(batchSize == 10000)
						{
							batchNumber++;
							System.out.println("Batch Executed is "+batchNumber);
							ps.executeBatch();
							batchSize = 0;
							batchExecuted = true;
						}
					}
				}
				else
				{
					startLine++;
				}
			}
			
			if(!batchExecuted)
			{
				batchNumber++;
				System.out.println("Batch Executed is "+batchNumber);
				ps.executeBatch();
			}
			
			System.out.println("Total count is "+lineNumber);
			
			br.close();
			ps.close();
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in ReadUCOSwitchData "+e);
			System.out.println("Exception at line "+stLine);
		}
	
	}

}
