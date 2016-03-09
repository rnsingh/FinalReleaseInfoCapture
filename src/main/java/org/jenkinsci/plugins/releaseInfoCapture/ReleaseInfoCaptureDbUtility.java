package org.jenkinsci.plugins.releaseInfoCapture;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;




public class ReleaseInfoCaptureDbUtility {
	
	
	  static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	  static final String DB_URL = "jdbc:mysql://localhost:3306/release_notes";

	   //  Database credentials
	   static final String USER = "root";
	   static final String PASS = "mysql";
	   
	public Connection connectDb()
	
	{
		Connection conn = null;
		Statement stmt = null;
		   
		   try{
			   
		
				
		      //STEP 2: Register JDBC driver
				Class.forName(JDBC_DRIVER);

		      //STEP 3: Open a connection
		      System.out.println("Connecting to database...");
		      conn = (Connection) DriverManager.getConnection(DB_URL,USER,PASS);
		      System.out.println("Connected to database...");
		   
	}
		   catch(SQLException se){
			      //Handle errors for JDBC
			      se.printStackTrace();
			   }catch(Exception e){
			      //Handle errors for Class.forName
			      e.printStackTrace();
			   }
		return conn;
		
	}
	
	
	public void updateDb(String jobNameInDb, String CHNG, String name,String buildURL,String buildTag,String buildRevision,String buildNumber,String buildTimer,String wiki) //throws Exception
	{
		ReleaseInfoCaptureDbUtility dbUtil = new ReleaseInfoCaptureDbUtility();
		Connection conn = null;
		Statement stmt = null;
		
		try
		{
			 conn = dbUtil.connectDb();
			 stmt = (Statement) conn.createStatement();
		     String sql = null;
		     
		     sql = "INSERT INTO `rel_notes`" +
		    		  "VALUES ( '"+jobNameInDb+"','"+CHNG+"','"+name+"','"+buildURL+"','"+buildTag+"','"+buildRevision+"','"+buildNumber+"','"+buildTimer+"','"+wiki+"' )";
		      stmt.executeUpdate(sql);
		}
		catch(SQLException se){
		      //Handle errors for JDBC
		      se.printStackTrace();
		   }catch(Exception e){
		      //Handle errors for Class.forName
		      e.printStackTrace();
		   }finally{
		      //finally block used to close resources
		      try{
		         if(stmt!=null)
		            stmt.close();
		      }catch(SQLException se2){
		      }// nothing we can do
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }//end finally try
		   }//end try
		
	}
	   
}

