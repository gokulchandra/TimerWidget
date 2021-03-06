package com.held.timer.widget.api;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.held.timer.widget.dataobjects.PageItem;
import com.held.timer.widget.dataobjects.PageItemsList;


@Path("/api")
public class TimerUpdate {

	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost:3306/time_analysis";


	static final String USER = "root";
	static final String PASS = "gokul";

	/*Production db password*/
	/*static final String PASS = "time_machine";*/

	Connection conn = null;
	Statement stmt = null;

	@Path("/remove/page/all")
	@PUT
	@Produces("text/plain")
	public String removeAllUserPage(
			@FormParam ("userId") String userId){
		String sql="";
		String status = "";
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			sql = "UPDATE `time_analysis`.`page_active_time`"
					+ " SET `user_id`='Cleared',"
					+ " `is_active`='0',"
					+ " `is_deleted`='1'"
					+ " WHERE user_id = \'"+userId+"\';";

			int success = stmt.executeUpdate(sql);
			if(success>0){
				System.out.println("All pages for user"+userId+" has been deleted");
				status = "All pages for user has been deleted";
			}else{
				System.out.println("All pages for user"+userId+" deletetion unsuccessful");
				
			}


		} catch (SQLException e) {

			e.printStackTrace();
			status = "All pages for user has NOT been deleted";
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return status;

	}



	@Path("/remove/page/")
	@PUT
	@Produces("text/plain")
	public String removeUserPage(
			@FormParam ("userId") String userId,
			@FormParam ("pageId") String pageId){

		String sql="";
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			sql = "UPDATE `time_analysis`.`page_active_time`"
					+ " SET `user_id`='Cleared',"
					+ " `is_active`='0',"
					+ " `is_deleted`='1'"
					+ " WHERE `page_id`=\'"+pageId+"\' "
					+ "and user_id = \'"+userId+"\';";

			/*previous query*/
			/*			sql ="DELETE from `time_analysis`.`page_active_time`"
					+ " WHERE `page_id`='"+pageId+"'"
					+ "and user_id = \'"+userId+"\' ;";*/

			int success = stmt.executeUpdate(sql);
			if(success>0){
				System.out.println("page"+pageId+" for user"+userId+" has been deleted");
			}else{
				System.out.println("page"+pageId+" for user"+userId+" is not successful");
			}


		} catch (SQLException e) {

			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pageId;

	}


	@Path("/blacklist/page/")
	@PUT
	@Produces("text/plain")
	public String blackListUserPage(
			@FormParam ("userId") String userId,
			@FormParam ("pageId") String pageId){

		String sql="";
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();

			String baseUrl = pageId;
			if(baseUrl.startsWith("https://")){
				baseUrl =  baseUrl.replaceFirst("https://", "");
				int i = baseUrl.indexOf("/");
				baseUrl = baseUrl.substring(0,i);
			}else if(baseUrl.startsWith("http://")){
				baseUrl =  baseUrl.replaceFirst("http://", "");
				int i = baseUrl.indexOf("/");
				baseUrl = baseUrl.substring(0,i);
			}


			sql = "UPDATE `time_analysis`.`page_active_time` "
					+ "SET `user_id`='Cleared',"
					+ " `is_active`='0',"
					+ " `is_deleted`='1'"
					+ " WHERE `page_id` like '%"+baseUrl+"%'"
					+ "and user_id = \'"+userId+"\' ;";

			int success = stmt.executeUpdate(sql);
			if(success>0){
				System.out.println("pages "+baseUrl+" for user"+userId+" has been deleted");
			}else{
				System.out.println("page"+baseUrl+" for user"+userId+" is not successful");
			}


			sql  = "SELECT * FROM time_analysis.blacklisted_page "
					+ "where user_id like '"+userId+"'"
					+ "and base_url like '"+baseUrl+"';";

			ResultSet result = stmt.executeQuery(sql);
			if(!result.next()){
				sql = "INSERT INTO `time_analysis`.`blacklisted_page` (`user_id`, `base_url`)"
						+ " VALUES ('"+userId+"', '"+baseUrl+"');";
				stmt.executeUpdate(sql);
				System.out.println("Entry created for url "+baseUrl+" for user "+userId+"! ");
			}else{
				System.out.println("Entry exists for url "+baseUrl+" for user "+userId+"! ");

			}

		} catch (SQLException e) {

			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pageId;

	}


	@Path("/view/userId/{userId}/search/{search}/page/{page}")
	@GET
	@Produces("text/plain")
	public String searchLinks(
			@PathParam("userId") String userId,
			@PathParam("search") String searchText,
			@PathParam("page") Integer page
			) throws SQLException, JsonGenerationException, JsonMappingException{
		PageItemsList lPageItemsList = new PageItemsList();
		ArrayList<PageItem> lPageItems = new ArrayList<PageItem> ();
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonString = "";
		String sql="";
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();

			sql = "SELECT * FROM time_analysis.page_active_time"
					+ " where page_title like '%"+searchText+"%'"
					+ " and user_id like '"+userId+"' "
					+ "order by cumulative_time desc;";

			ResultSet result = stmt.executeQuery(sql);
			PageItem item = null;
			result.absolute(page*15);
			int rowCount = 0;
			while(result.next()){
				item = new PageItem();

				item.setPageId(result.getString("page_id"));
				item.setPageTitle(result.getString("page_title"));
				item.setUserId(result.getString("user_id"));
				item.setDuration(result.getInt("cumulative_time"));
				item.setIconUrl(result.getString("icon_url"));
				lPageItems.add(item);
				if(++rowCount==15){
					break;
				}

			}
			lPageItemsList.setlPageItems(lPageItems);
			jsonString = objectMapper.writeValueAsString(lPageItemsList);

		}catch(Exception e){
			System.out.println(e);
		}finally{
			conn.close();

		}

		return jsonString;

	}




	@Path("/view/userId/{userId}/notify")
	@GET
	@Produces("text/plain")
	public String getLongestViewedPage(
			@PathParam("userId") String userId ) throws SQLException, JsonGenerationException, JsonMappingException{
		PageItemsList lPageItemsList = new PageItemsList();
		ArrayList<PageItem> lPageItems = new ArrayList<PageItem> ();
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonString = "";
		String sql="";
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();

			sql = "SELECT * FROM time_analysis.page_active_time"
					+ " where (UNIX_TIMESTAMP(now()) - UNIX_TIMESTAMP(last_updated_timestamp)) <= 86400"
					+ " and user_id like '"+userId+"' "
					+ "order by cumulative_time desc limit 1;";

			ResultSet result = stmt.executeQuery(sql);
			PageItem item = null;


			while(result.next()){
				item = new PageItem();

				item.setPageId(result.getString("page_id"));
				item.setPageTitle(result.getString("page_title"));
				item.setUserId(result.getString("user_id"));
				item.setDuration(result.getInt("cumulative_time"));
				item.setIconUrl(result.getString("icon_url"));
				lPageItems.add(item);
			}
			lPageItemsList.setlPageItems(lPageItems);
			jsonString = objectMapper.writeValueAsString(lPageItemsList);

		}catch(Exception e){
			System.out.println(e);
		}finally{
			conn.close();

		}

		return jsonString;

	}






	@Path("/view/userId/{userId}/trending/{page}")
	@GET
	@Produces("text/plain")
	public String getTrendingPages(
			@PathParam("userId") String userId,
			@PathParam("page") Integer page) throws SQLException, JsonGenerationException, JsonMappingException{
		PageItemsList lPageItemsList = new PageItemsList();
		ArrayList<PageItem> lPageItems = new ArrayList<PageItem> ();
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonString = "";
		String sql="";
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			/*			sql = "select * from `time_analysis`.`page_active_time` "
					+ "where `user_id` like \'"+userId+"\' and"
							+ "`is_deleted` ='0' and"
							+ "`is_active` = '1'" 
							+ " order by `cumulative_time` desc limit 10;";*/

			/*			sql = "select page_id,page_title,user_id,cumulative_time,cumulative_time / ( UNIX_TIMESTAMP(now()) - UNIX_TIMESTAMP(last_updated_timestamp ))  "
					+ "from page_active_time "
					+ "where `user_id` like \'"+userId+"\' and"
					+ "`is_deleted` ='0' and"
					+ "`is_active` = '1'"
					+ " order by cumulative_time / ( UNIX_TIMESTAMP(now()) - UNIX_TIMESTAMP(last_updated_timestamp )) desc "
					+ "limit 10 ;";
			 */

			sql = "SELECT * FROM time_analysis.page_active_time"
					+ " where (UNIX_TIMESTAMP(now()) - UNIX_TIMESTAMP(last_updated_timestamp)) <= 86400"
					+ " and user_id like '"+userId+"' "
					+ "order by cumulative_time desc ;";

			ResultSet result = stmt.executeQuery(sql);
			PageItem item = null;

			result.absolute(page*15);
			int rowCount = 0;
			HashMap<String, Boolean> mBaseUrls = new HashMap<String, Boolean>();   
			while(result.next()){
				item = new PageItem();

/*				if(null!=mBaseUrls.get(result.getString("base_url")) && mBaseUrls.get(result.getString("base_url"))){
					continue;
				}else{
					mBaseUrls.put(result.getString("base_url"), true);*/
					item.setPageId(result.getString("page_id"));
					item.setPageTitle(result.getString("page_title"));
					item.setUserId(result.getString("user_id"));
					item.setDuration(result.getInt("cumulative_time"));
					item.setIconUrl(result.getString("icon_url"));
					item.setBaseUrl(result.getString("base_url"));
					lPageItems.add(item);
					if(++rowCount==15){
						break;
					}
				/*}*/
			}

			lPageItemsList.setlPageItems(lPageItems);
			jsonString = objectMapper.writeValueAsString(lPageItemsList);

		}catch(Exception e){
			System.out.println(e);
		}finally{
			conn.close();

		}

		return jsonString;

	}

	@Path("/view/userId/{userId}/baseurl/{baseurl}")
	@GET
	@Produces("text/plain")
	public String getCollatedPages(
			@PathParam("userId") String userId,
			@PathParam("baseurl") String baseUrl
			) throws SQLException{
		PageItemsList lPageItemsList = new PageItemsList();
		ArrayList<PageItem> lPageItems = new ArrayList<PageItem> ();
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonString = "";
		String sql="";
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();

			sql = "SELECT * FROM time_analysis.page_active_time"
					+ " where (UNIX_TIMESTAMP(now()) - UNIX_TIMESTAMP(last_updated_timestamp)) <= 86400"
					+ " and user_id like '"+userId+"' "
					+ "and base_url like '"+baseUrl+"'"
					+ "order by cumulative_time desc ;";
			ResultSet result = stmt.executeQuery(sql);
			PageItem item = null;

			int rowCount = 0;

			while(result.next()){
				item = new PageItem();

				item.setPageId(result.getString("page_id"));
				item.setPageTitle(result.getString("page_title"));
				item.setUserId(result.getString("user_id"));
				item.setDuration(result.getInt("cumulative_time"));
				item.setIconUrl(result.getString("icon_url"));
				item.setBaseUrl(result.getString("base_url"));
				lPageItems.add(item);
			}


			lPageItemsList.setlPageItems(lPageItems);
			jsonString = objectMapper.writeValueAsString(lPageItemsList);

		}catch(Exception e){
			System.out.println(e);
		}finally{
			conn.close();
		}


		return jsonString;
	}

	@Path("/update/timerId/duration")
	@POST
	@Produces("text/plain")
	public String postActiveDuration(
			@FormParam("url") String timerId,
			@FormParam("time") String viewTime,
			@FormParam("userId") String userId,
			@FormParam("title") String pageTitle,
			@FormParam("iconUrl") String iconUrl)throws ClassNotFoundException, SQLException{
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss ");
		String timeStamp = dateFormat.format(date);
		String sql="";
		String updateStatus = "";
		int prevTime=0;
		if(viewTime.equalsIgnoreCase("") || userId.equalsIgnoreCase("") || pageTitle.equalsIgnoreCase("new tab") || pageTitle.equalsIgnoreCase("") || timerId.equalsIgnoreCase("")){
			System.out.println("Invalid Page ");
			updateStatus =  "Invalid Details";
			return updateStatus;
		}
		if(timerId.startsWith("chrome://")){
			System.out.println("Invalid Page received ");
			System.out.println(timerId);
			updateStatus =  "No user Id";
			return updateStatus;
		}
		if(null == iconUrl || iconUrl.isEmpty() || iconUrl.equalsIgnoreCase("")){
			iconUrl  = "http://52.26.203.91:80/icon.png";
		}else{
			iconUrl = "http://www.google.com/s2/favicons?domain_url="+iconUrl;
		}
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);

			stmt = conn.createStatement();

			String baseUrl = timerId;
			if(baseUrl.startsWith("https://")){
				baseUrl =  baseUrl.replaceFirst("https://", "");
				int i = baseUrl.indexOf("/");
				baseUrl = baseUrl.substring(0,i);
			}else if(baseUrl.startsWith("http://")){
				baseUrl =  baseUrl.replaceFirst("http://", "");
				int i = baseUrl.indexOf("/");
				baseUrl = baseUrl.substring(0,i);
			}

			sql  = "SELECT * FROM time_analysis.blacklisted_page "
					+ "where user_id like '"+userId+"'"
					+ "and base_url like '"+baseUrl+"';";

			ResultSet result = stmt.executeQuery(sql);
			if(!result.next()){
				System.out.println("Page "+timerId+" is not on Blacklist "+baseUrl+" for user "+userId+"! ");
			}else{
				System.out.println("Entry exists in blacklist for url "+baseUrl+" for user "+userId+"! ");
				updateStatus = "Domain in Blacklist -- Timer Not Updated";
				return updateStatus;
			}


			sql = "select * from `time_analysis`.`page_active_time`"
					+ " where page_id =\'"+timerId+"\' "
					+ "and user_id = \'"+userId+"\' ;";

			ResultSet result1 = stmt.executeQuery(sql);
			if(!result1.next()){
				sql = "INSERT INTO"
						+ " `time_analysis`.`page_active_time`"
						+ " (`page_id`, `cumulative_time`,`user_id`,`page_title`,`icon_url`,`base_url`) "
						+ "VALUES (\'"+timerId+"\','0',\'"+userId+"\',\'"+pageTitle+"\',\'"+iconUrl+"\',\'"+baseUrl+"\');";
				stmt.execute(sql);
				System.out.println("Created new entry for user"+userId+" page title" + pageTitle);
			}else{

				prevTime = result1.getInt("cumulative_time");
			}
			result1.close();
			int addTime = Integer.valueOf(viewTime);
			int newTime = prevTime+ addTime;

			sql = "UPDATE `time_analysis`.`page_active_time`"
					+ " SET "
					+ "`cumulative_time`='"+newTime+"',"
					+ "`last_updated_timestamp`='"+timeStamp+"',"
					+ "`icon_url`=\'"+iconUrl+"\',"
					+ "`base_url` = \'"+baseUrl+"\'"
					+ " WHERE `page_id`='"+timerId+"'"
					+ "and user_id = \'"+userId+"\' ;";
			stmt.executeUpdate(sql);

			System.out.println("Updated entry for user"+userId+" page title" + pageTitle);
			updateStatus = "Time Updated";

		} catch (SQLException e) {
			System.out.println("SQL connection error");
			e.printStackTrace();
			updateStatus = "update Failed SQL Error";
		}finally{

			conn.close();
		}

		return updateStatus;

	}

	@Path("/update/timerId/{pageId}/duration/{viewTime}")
	@POST
	@Produces("text/plain")
	public String postViewdDuration(@PathParam("pageId") String timerId, @PathParam("viewTime") String viewTime ) throws ClassNotFoundException, SQLException{

		String sql="";
		int prevTime=0;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);

			stmt = conn.createStatement();
			sql = "select * from timer_page_map where timer_id = "+timerId+";";
			ResultSet result = stmt.executeQuery(sql);
			if(!result.next()){
				return "";
			}
			result.close();

			sql = "select * from page_active_time where timer_id = "+timerId+";";
			ResultSet result1 = stmt.executeQuery(sql);
			if(!result1.next()){
				sql = "INSERT INTO"
						+ " `held_tracker`.`page_active_time`"
						+ " (`timer_id`, `cumulative_time`) "
						+ "VALUES ('"+timerId+"','0');";
				stmt.execute(sql);

			}else{

				prevTime = result1.getInt(2);
			}
			result1.close();
			int addTime = Integer.valueOf(viewTime);
			int newTime = prevTime+ addTime;

			sql = "UPDATE `held_tracker`.`page_active_time`"
					+ " SET "
					+ "`cumulative_time`='"+newTime+"'"
					+ " WHERE `timer_id`='"+timerId+"';";
			stmt.executeUpdate(sql);

		} catch (SQLException e) {
			System.out.println("SQL connection error");
			e.printStackTrace();
		}finally{

			conn.close();
		}

		return "Time Updated";

	}
}