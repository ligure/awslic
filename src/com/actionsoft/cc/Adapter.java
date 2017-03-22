package com.actionsoft.cc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Binding;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.jaxws.DispatchImpl;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.impls.automation.IJIDispatch;

import com.actionsoft.application.logging.IntegratedLogger;
import com.actionsoft.awf.form.execute.RuntimeFormManager;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.ClassInfo;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.cc.app.jde.JdeHandler;
import com.actionsoft.cc.app.sap.SapHandler;
import com.actionsoft.cc.cache.CPCache;
import com.actionsoft.cc.connector.Util;
import com.actionsoft.cc.connector.com.COMProfile;
import com.actionsoft.cc.connector.db.DBProfile;
import com.actionsoft.cc.connector.ftp.FTPProfile;
import com.actionsoft.cc.connector.http.HTTPProfile;
import com.actionsoft.cc.connector.jms.JMSProfile;
import com.actionsoft.cc.connector.nativecall.NativeCallProfile;
import com.actionsoft.cc.connector.ws.ServiceConfig;
import com.actionsoft.cc.connector.ws.SoapMessageBuilder;
import com.actionsoft.cc.connector.ws.WSProfile;
import com.actionsoft.cc.connector.ws.WsdlUtils;
import com.actionsoft.cc.connector.ws.support.SoapVersion;
import com.actionsoft.cc.design.jms.DestinationModel;
import com.actionsoft.cc.design.jms.PlugIn;
import com.actionsoft.cc.design.jms.PlugInFactory;
import com.actionsoft.cc.design.jms.PlugInModel;
import com.actionsoft.cc.design.jms.ProviderFactory;
import com.actionsoft.cc.model.CPModel;

public abstract class Adapter {
    protected abstract Connector getConnector();

    protected void close() {
	getConnector().close();
    }

    protected Iterator keys() {
	return getConnector().keys();
    }

    protected String getString(String key) {
	return getConnector().getValue(key);
    }

    protected boolean loggable() {
	return getConnector() != null && getConnector()._model != null
		&& getConnector()._model._logEnable;
    }

    public Hashtable getUserData() {
	return CCUtil.getDiyData(getConnector()._model);
    }

    public static class COM extends Adapter {
	private Connector.COM com;
	private IJIComObject obj;

	private COM(String uuid) throws CCException {
	    this.com = Connector.COM.binding(uuid);
	}

	public static COM binding(String uuid) throws CCException {
	    return new COM(uuid);
	}

	private void log(String msg) {
	    if (loggable())
		IntegratedLogger.logCC(getConnector()._uuid, COMProfile
			.getInstance().getType(), msg);
	}

	protected Connector getConnector() {
	    return this.com;
	}

	public IJIDispatch queryDispatch() throws JIException {
	    IJIComObject obj = getComObject();
	    log("[操作=queryDispatch]");
	    return (IJIDispatch) JIObjectFactory.narrowObject(obj
		    .queryInterface("00020400-0000-0000-c000-000000000046"));
	}

	public IJIComObject getComObject() {
	    if (this.obj == null) {
		this.obj = this.com.getComObject();
		log("[操作=getComObject]");
	    }
	    return this.obj;
	}
    }

    public static class DB extends Adapter {
	public static final String SUPPLY_SQLSERVER = "sqlserver";
	public static final String SUPPLY_DB2 = "db2";
	public static final String SUPPLY_MYSQL = "mysql";
	public static final String SUPPLY_ORACLE = "oracle";
	public static final String SUPPLY_OSCAR = "oscar";
	private Connector.DB _jdbc;
	private static Hashtable _resourceTrack = new Hashtable();

	private String supply = null;

	private DB(String uuid) throws CCException {
	    this._jdbc = Connector.DB.binding(uuid);
	}

	public static DB binding(String uuid) throws CCException {
	    return new DB(uuid);
	}

	protected Connector getConnector() {
	    return this._jdbc;
	}

	public String convertDateField(String fieldName) {
	    String dateField = fieldName;
	    String dbSupply = getSupply();
	    if (dbSupply.toLowerCase().equals("oracle"))
		dateField = "trunc(" + fieldName + ")";
	    else if (dbSupply.toLowerCase().equals("sqlserver"))
		dateField = "CONVERT(CHAR(10), " + fieldName + ", 120)";
	    else if (dbSupply.toLowerCase().equals("mysql")) {
		dateField = "DATE(" + fieldName + ")";
	    }
	    return dateField;
	}

	public String getSupply() {
	    if (this.supply == null) {
		java.sql.Connection conn = null;
		try {
		    conn = open();
		    this.supply = conn.getMetaData().getDatabaseProductName()
			    .toLowerCase();
		    if ("microsoft sql server".equals(this.supply))
			this.supply = "sqlserver";
		    else if (this.supply.indexOf("db2") != -1)
			this.supply = "db2";
		    else if (this.supply.indexOf("oracle") != -1)
			this.supply = "oracle";
		    else if (this.supply.indexOf("oscar") != -1)
			this.supply = "oscar";
		    else if (this.supply.indexOf("mysql") != -1)
			this.supply = "mysql";
		} catch (SQLException localSQLException) {
		} finally {
		    close(conn);
		}
	    }

	    return this.supply;
	}

	public static String buildPagingSQL(String supply, String statment,
		int start, int lineNumber, int lineCount) {
	    if (start != 0) {
		--start;
	    }

	    if (supply != null && supply.indexOf("access") != -1) {
		String orderBy = "";
		String newOrderBy = "";
		if (statment.toUpperCase().indexOf("ORDER BY") > 0) {
		    orderBy = statment.substring(statment.toUpperCase()
			    .indexOf("ORDER BY"));
		    if (orderBy.toUpperCase().indexOf("ASC") == -1
			    && orderBy.toUpperCase().indexOf("DESC") == -1) {
			orderBy = orderBy + " asc";
		    }

		    Pattern p = Pattern.compile("asc|desc", 2);
		    Matcher m = p.matcher(orderBy);
		    StringBuffer sb = new StringBuffer();
		    while (m.find()) {
			String find = m.group();
			if (find.equalsIgnoreCase("asc"))
			    m.appendReplacement(sb, "desc");
			else {
			    m.appendReplacement(sb, "asc");
			}
		    }

		    m.appendTail(sb);
		    newOrderBy = sb.toString();
		} else {
		    throw new IllegalArgumentException(
			    "查询sql必须指定排序字段和排序类型：ASC,DESC");
		}

		String ftop = "select top " + (start + lineNumber)
			+ " * from (" + statment + ") t1 ";
		ftop = "select * from (" + ftop + ") t2 " + newOrderBy;
		String rsql = "select top " + lineNumber + " * from (" + ftop
			+ ") t3";
		rsql = "select * from (" + rsql + ") t4 " + orderBy;
		return rsql;
	    }
	    throw new UnsupportedOperationException(
		    "Not Implement Database type " + supply + "!");
	}

	public String buildPagingSQL(String statment, int start,
		int lineNumber, int lineCount) {
	    return buildPagingSQL(getSupply(), statment, start, lineNumber,
		    lineCount);
	}

	public int executeUpdate(String sql) throws SQLException {
	    java.sql.Connection conn = null;
	    try {
		conn = open();
		return executeUpdate(conn, sql);
	    } finally {
		close(conn, null, null);
	    }
	}

	public int executeUpdate(java.sql.Connection conn, String sql)
		throws SQLException {
	    Statement stmt = null;
	    sql = decodeSQL(sql);
	    if (loggable()) {
		IntegratedLogger.logCC(this._jdbc._model._uuid, DBProfile
			.getInstance().getType(), "[Title=Update][sql=" + sql
			+ "]");
	    }
	    try {
		stmt = conn.createStatement();
		return stmt.executeUpdate(sql);
	    } finally {
		close(stmt, null);
	    }
	}

	public ResultSet executeQuery(java.sql.Connection conn, Statement stmt,
		String sql) throws SQLException {
	    sql = decodeSQL(sql);
	    if (loggable()) {
		IntegratedLogger.logCC(this._jdbc._model._uuid, DBProfile
			.getInstance().getType(), "[Title=Query][sql=" + sql
			+ "]");
	    }

	    return stmt.executeQuery(sql);
	}

	public String getDateDefaultValue() {
	    if (isOracle())
		return " sysdate ";
	    if (isMysql())
		return " now() ";
	    if (isSqlserver())
		return " getdate() ";
	    if (isOscar())
		return " getdate() ";
	    if (isDB2()) {
		return " current date ";
	    }
	    return " getdate() ";
	}

	public String convertLongDate(String date) {
	    if (date == null || date.trim().length() == 0) {
		return "null";
	    }

	    if (isOracle())
		return "to_date('" + date + "','YYYY-mm-dd hh24:mi:ss')";
	    if (isMysql())
		return "'" + date + "'";
	    if (isOscar()) {
		return "to_timestamp('" + date + "','YYYY-mm-dd hh24:mi:ss')";
	    }
	    return "'" + date + "'";
	}

	public String convertShortDate(String date) {
	    if (date == null || date.trim().length() == 0)
		return "null";
	    if (isOracle())
		return "to_date('" + date + "','YYYY-mm-dd')";
	    if (isMysql()) {
		if (date.indexOf(" ") > 0)
		    date = date.substring(0, date.indexOf(" "));
		return "'" + date + "'";
	    }
	    if (isOscar()) {
		return "to_date('" + date + "','YYYY-mm-dd')";
	    }
	    return "'" + date + "'";
	}

	public double getDouble(java.sql.Connection conn, String sql,
		String filedName) {
	    Statement stmt = null;
	    ResultSet rset = null;
	    try {
		stmt = conn.createStatement();
		rset = executeQuery(conn, stmt, sql);
		if (rset.next())
		    return rset.getDouble(filedName);
	    } catch (SQLException sqle) {
		sqle.printStackTrace();
	    } finally {
		close(null, stmt, rset);
	    }

	    return 0.0D;
	}

	public double getDouble(String sql, String filedName) {
	    java.sql.Connection conn = null;
	    double r = 0.0D;
	    try {
		conn = open();
		r = getDouble(conn, sql, filedName);
	    } catch (Exception e) {
		e.printStackTrace();
	    } finally {
		close(conn, null, null);
	    }
	    return r;
	}

	public int getInt(java.sql.Connection conn, String sql, String filedName) {
	    Statement stmt = null;
	    ResultSet rset = null;
	    try {
		stmt = conn.createStatement();
		rset = executeQuery(conn, stmt, sql);
		if (rset.next())
		    return rset.getInt(filedName);
	    } catch (SQLException sqle) {
		sqle.printStackTrace();
	    } finally {
		close(null, stmt, rset);
	    }

	    return 0;
	}

	public int getInt(String sql, String filedName) {
	    java.sql.Connection conn = null;
	    int r = 0;
	    try {
		conn = open();
		r = getInt(conn, sql, filedName);
	    } catch (Exception e) {
		e.printStackTrace();
	    } finally {
		close(conn, null, null);
	    }
	    return r;
	}

	public String getString(java.sql.Connection conn, String sql,
		String filedName) {
	    Statement stmt = null;
	    ResultSet rset = null;
	    try {
		stmt = conn.createStatement();
		rset = executeQuery(conn, stmt, sql);
		if (rset.next())
		    return rset.getString(filedName);
	    } catch (SQLException sqle) {
		sqle.printStackTrace();
	    } finally {
		close(null, stmt, rset);
	    }

	    return "";
	}

	public String getString(String sql, String filedName) {
	    java.sql.Connection conn = null;
	    String r = null;
	    try {
		conn = open();
		r = getString(conn, sql, filedName);
	    } catch (Exception e) {
		e.printStackTrace();
	    } finally {
		close(conn, null, null);
	    }
	    return r;
	}

	public Timestamp getTimestamp(java.sql.Connection conn, String sql,
		String filedName) {
	    Statement stmt = null;
	    ResultSet rset = null;
	    try {
		stmt = conn.createStatement();
		rset = executeQuery(conn, stmt, sql);
		if (!rset.next())
		    return rset.getTimestamp(filedName);
	    } catch (SQLException e) {
		e.printStackTrace();
	    } finally {
		close(null, stmt, rset);
	    }

	    return null;
	}

	public Timestamp getTimestamp(String sql, String filedName) {
	    java.sql.Connection conn = null;
	    Timestamp r = null;
	    try {
		conn = open();
		r = getTimestamp(conn, sql, filedName);
	    } catch (Exception e) {
		e.printStackTrace();
	    } finally {
		close(conn, null, null);
	    }

	    return r;
	}

	public java.sql.Connection open() throws SQLException {
	    java.sql.Connection conn = this._jdbc.getDataSource()
		    .getConnection();
	    if (conn != null) {
		_resourceTrack.put(Thread.currentThread().getName()
			+ ",hashcode=" + conn.hashCode(),
			ClassInfo.getThreadStackTraces());
	    }
	    return conn;
	}

	public Hashtable getResourceTrack() {
	    return _resourceTrack;
	}

	public static void close(ResultSet rset) {
	    close(null, null, rset);
	}

	public static void close(java.sql.Connection conn) {
	    close(conn, null, null);
	}

	public static void close(Statement stmt, ResultSet rset) {
	    close(null, stmt, rset);
	}

	public static void close(java.sql.Connection conn, Statement stmt,
		ResultSet rset) {
	    try {
		if (rset != null) {
		    rset.close();
		    rset = null;
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    try {
		if (stmt != null) {
		    stmt.close();
		    stmt = null;
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	    if (conn == null)
		return;
	    try {
		if (!conn.getAutoCommit())
		    conn.setAutoCommit(true);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    try {
		_resourceTrack.remove(Thread.currentThread().getName()
			+ ",hashcode=" + conn.hashCode());
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    try {
		conn.close();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	private String decodeSQL(String sql) {
	    if (isMysql()) {
		String os = System.getProperties().getProperty("os.name")
			.toLowerCase();
		if (os != null && os.indexOf("windows") != -1) {
		    if (sql.toUpperCase().indexOf("INSERT INTO") > -1) {
			String tmp = sql.substring(0, sql.indexOf("("));
			sql = new UtilString(sql).replace(tmp,
				tmp.toUpperCase());
		    } else if (sql.toUpperCase().indexOf("UPDATE") > -1) {
			String tmp;
			if (sql.toUpperCase().indexOf("SET") > -1)
			    tmp = sql.substring(0,
				    sql.toUpperCase().indexOf("SET"));
			else {
			    tmp = sql;
			}
			sql = new UtilString(sql).replace(tmp,
				tmp.toUpperCase());
		    } else if (sql.toUpperCase().indexOf("WHERE") > 0) {
			String tmp = sql.substring(0, sql.toUpperCase()
				.indexOf("WHERE"));
			if (sql.toUpperCase().indexOf("FROM") > 0) {
			    tmp = sql.substring(
				    sql.toUpperCase().indexOf("FROM"), sql
					    .toUpperCase().indexOf("WHERE"));
			}
			sql = new UtilString(sql).replace(tmp,
				tmp.toUpperCase());
		    } else {
			sql = sql.toUpperCase();
		    }
		}
	    }

	    return sql;
	}

	private boolean isOscar() {
	    return getSupply() != null && getSupply().indexOf("oscar") != -1;
	}

	private boolean isOracle() {
	    return getSupply() != null && getSupply().indexOf("oracle") != -1;
	}

	private boolean isMysql() {
	    return getSupply() != null && getSupply().indexOf("mysql") != -1;
	}

	private boolean isDB2() {
	    return getSupply() != null && getSupply().indexOf("db2") != -1;
	}

	private boolean isSqlserver() {
	    return getSupply() != null
		    && getSupply().indexOf("sqlserver") != -1;
	}
    }

    public static class FTP extends Adapter {
	public static final int ASCII_FILE_TYPE = 0;
	public static final int BINARY_FILE_TYPE = 2;
	public static final int LOCAL_FILE_TYPE = 3;
	public static final int STREAM_TRANSFER_MODE = 10;
	public static final int BLOCK_TRANSFER_MODE = 11;
	public static final int COMPRESSED_TRANSFER_MODE = 12;
	private Connector.FTP _ftp;
	private FTPClient _client;

	private FTP(String uuid) throws CCException {
	    this._ftp = Connector.FTP.binding(uuid);
	    this._client = this._ftp.getFTPClient();
	}

	public static FTP binding(String uuid) throws CCException {
	    return new FTP(uuid);
	}

	protected Connector getConnector() {
	    return this._ftp;
	}

	public void close() {
	    if (getConnector() != null)
		getConnector().close();
	}

	private void log(String msg) {
	    if (loggable())
		IntegratedLogger.logCC(getConnector()._uuid, FTPProfile
			.getInstance().getType(), msg);
	}

	public String[] listNames() throws IOException {
	    return this._client.listNames();
	}

	public boolean login() throws SocketException, IOException {
	    boolean login = this._ftp.login();
	    if (!login) {
		close();
	    }

	    return login;
	}

	public String[] listNames(String pathname) throws IOException {
	    log("[操作=listNames][路径=" + pathname + "]");
	    return this._client.listNames(pathname);
	}

	public FTPFile[] listFiles() throws IOException {
	    log("[操作=listFiles][路径=" + printWorkingDirectory() + "]");
	    org.apache.commons.net.ftp.FTPFile[] fs = this._client.listFiles();
	    return newAwsFtpFile(fs);
	}

	private FTPFile[] newAwsFtpFile(org.apache.commons.net.ftp.FTPFile[] fs) {
	    if (fs == null) {
		return null;
	    }

	    List ay = new ArrayList();
	    for (int i = 0; i < fs.length; ++i) {
		if (!".".equals(fs[i].getName())
			&& !"..".equals(fs[i].getName())) {
		    ay.add(new FTPFile(fs[i], null));
		}
	    }

	    FTPFile[] ftp = new FTPFile[ay.size()];
	    for (int i = 0; i < ftp.length; ++i) {
		ftp[i] = ((FTPFile) ay.get(i));
	    }
	    return ftp;
	}

	public FTPFile[] listFiles(String pathname) throws IOException {
	    log("[操作=listFiles][路径=" + pathname + "]");
	    org.apache.commons.net.ftp.FTPFile[] fs = this._client
		    .listFiles(pathname);
	    return newAwsFtpFile(fs);
	}

	public boolean storeFile(String filepath, String upName)
		throws IOException {
	    FileInputStream fis = new FileInputStream(filepath);
	    boolean store = this._client.storeFile(upName, fis);
	    fis.close();
	    log("[操作=storeFile][路径=" + filepath + "][FTP文件=" + upName + "][状态="
		    + (store ? "成功" : "失败") + "]");
	    return store;
	}

	public boolean storeFile(String filepath) throws IOException {
	    File file = new File(filepath);
	    boolean store = file.exists() ? storeFile(file.getAbsolutePath(),
		    file.getName()) : false;
	    log("[操作=storeFile][路径=" + filepath + "][FTP文件=" + file.getName()
		    + "][状态=" + (store ? "成功" : "失败") + "]");
	    return store;
	}

	public boolean retrieveFile(String remote, OutputStream os)
		throws UnsupportedEncodingException, IOException {
	    boolean ret = this._client.retrieveFile(remote, os);
	    os.close();
	    log("[操作=retrieveFile][FTP文件=" + remote + "][状态="
		    + (ret ? "成功" : "失败") + "]");
	    return ret;
	}

	public boolean setFileType(int fileType) throws IOException {
	    boolean ok = this._client.setFileType(fileType);
	    log("[操作=setFileType][FileType=" + fileType + "][状态="
		    + (ok ? "成功" : "失败") + "]");
	    return ok;
	}

	public boolean setFileType(int fileType, int formatOrByteSize)
		throws IOException {
	    boolean ok = this._client.setFileType(fileType, formatOrByteSize);
	    log("[操作=setFileType][FileType=" + fileType + "][formatOrByteSize="
		    + formatOrByteSize + "][状态=" + (ok ? "成功" : "失败") + "]");
	    return ok;
	}

	public boolean setFileTransferMode(int mode) throws IOException {
	    boolean ok = this._client.setFileTransferMode(mode);
	    log("[操作=setFileTransferMode][mode=" + mode + "][状态="
		    + (ok ? "成功" : "失败") + "]");
	    return ok;
	}

	public InputStream retrieveFile(String remote) throws SocketException,
		IOException {
	    log("[操作=retrieveFile][FTPFile=" + remote + "]");
	    return this._client.retrieveFileStream(remote);
	}

	public boolean appendFile(String remote, InputStream local)
		throws IOException {
	    boolean ok = this._client.appendFile(remote, local);
	    log("[操作=appendFile][FTPFile=" + remote + "][状态="
		    + (ok ? "成功" : "失败") + "]");
	    return ok;
	}

	public OutputStream appendFileStream(String remote) throws IOException {
	    log("[操作=appendFileStream][FTPFile=" + remote + "]");
	    return this._client.appendFileStream(remote);
	}

	public boolean rename(String from, String to) throws IOException {
	    boolean ok = this._client.rename(from, to);
	    log("[操作=rename][OLD=" + from + "][NEW=" + to + "][状态="
		    + (ok ? "成功" : "失败") + "]");
	    return ok;
	}

	public boolean changeDirectory(String path) throws IOException {
	    return this._client.changeWorkingDirectory(path);
	}

	public boolean changeToParentDirectory() throws IOException {
	    return this._client.changeToParentDirectory();
	}

	public boolean makeDirectory(String pathName) throws IOException {
	    boolean ok = this._client.makeDirectory(pathName);
	    log("[操作=makeDirectory][pathName=" + pathName + "][状态="
		    + (ok ? "成功" : "失败") + "]");
	    return ok;
	}

	public boolean removeDirectory(String path) throws IOException {
	    boolean ok = this._client.removeDirectory(path);
	    log("[操作=removeDirectory][pathName=" + path + "][状态="
		    + (ok ? "成功" : "失败") + "]");
	    return ok;
	}

	public boolean deleteFile(String pathname) throws IOException {
	    boolean ok = this._client.deleteFile(pathname);
	    log("[操作=deleteFile][pathName=" + pathname + "][状态="
		    + (ok ? "成功" : "失败") + "]");
	    return ok;
	}

	public String printWorkingDirectory() throws IOException {
	    return this._client.printWorkingDirectory();
	}

	public static class FTPFile {
	    public static final int FILE_TYPE = 0;
	    public static final int DIRECTORY_TYPE = 1;
	    public static final int SYMBOLIC_LINK_TYPE = 2;
	    public static final int UNKNOWN_TYPE = 3;
	    public static final int USER_ACCESS = 0;
	    public static final int GROUP_ACCESS = 1;
	    public static final int WORLD_ACCESS = 2;
	    public static final int READ_PERMISSION = 0;
	    public static final int WRITE_PERMISSION = 1;
	    public static final int EXECUTE_PERMISSION = 2;
	    private org.apache.commons.net.ftp.FTPFile _af;

	    private FTPFile(org.apache.commons.net.ftp.FTPFile af) {
		this._af = af;
	    }

	    private FTPFile(org.apache.commons.net.ftp.FTPFile ftpfile,
		    FTPFile ftpfile1) {
		this(ftpfile);
	    }

	    public boolean hasPermission(int access, int permission) {
		return this._af.hasPermission(access, permission);
	    }

	    public String toString() {
		return this._af.toString();
	    }

	    public Calendar getTimestamp() {
		return this._af.getTimestamp();
	    }

	    public String getLink() {
		return this._af.getLink();
	    }

	    public String getUser() {
		return this._af.getUser();
	    }

	    public String getGroup() {
		return this._af.getGroup();
	    }

	    public int getHardLinkCount() {
		return this._af.getHardLinkCount();
	    }

	    public long getSize() {
		return this._af.getSize();
	    }

	    public String getName() {
		return this._af.getName();
	    }

	    public int getType() {
		return this._af.getType();
	    }

	    public boolean isUnknown() {
		return this._af.isUnknown();
	    }

	    public boolean isSymbolicLink() {
		return this._af.isSymbolicLink();
	    }

	    public boolean isFile() {
		return this._af.isFile();
	    }

	    public boolean isDirectory() {
		return this._af.isDirectory();
	    }

	    public String getRawListing() {
		return this._af.getRawListing();
	    }
	}
    }

    public static class HTTP extends Adapter {
	private Connector.HTTP http;
	private HttpClient hc;
	private JSONObject jo = null;
	private Properties uriParam;

	private HTTP(String uuid) throws CCException {
	    this.http = Connector.HTTP.binding(uuid);
	    this.hc = this.http.getClient();
	    this.jo = JSONObject.fromObject(this.http._model._profile);
	    boolean auth = (this.jo.containsKey("idcheck")) ? this.jo
		    .getBoolean("idcheck") : false;
	    String httpUrl = this.jo.getString("httpUrl");
	    String encoding = this.jo.getString("encoding");
	    if (auth) {
		URL url = null;
		try {
		    url = new URL(httpUrl);
		} catch (Exception e) {
		    throw new CCException(e);
		}

		int port = url.getPort();
		if (port == -1) {
		    port = (url.getProtocol().equals("http")) ? 80 : 443;
		}

		String user = this.jo.getString("user");
		String password = this.jo.getString("password");
		this.hc.getState().setCredentials(
			new AuthScope(url.getHost(), port),
			new UsernamePasswordCredentials(user, password));
	    }

	    this.hc.getParams().setContentCharset(encoding);
	}

	public void setParam(Properties param) {
	    this.uriParam = param;
	}

	public static HTTP binding(String uuid) throws CCException {
	    return new HTTP(uuid);
	}

	public HttpClient getHttpClient() {
	    return this.hc;
	}

	private String prepareUri() {
	    String httpUrl = this.jo.getString("httpUrl");
	    if (httpUrl.indexOf('}') == -1) {
		return httpUrl;
	    }

	    StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < httpUrl.length(); ++i) {
		char c = httpUrl.charAt(i);
		if (c == '{') {
		    int next = httpUrl.indexOf('}', i);
		    if (next == -1) {
			throw new IllegalStateException(
				"Http URI模板参数定义错误，请使用格式：{参数名称}");
		    }

		    String p = httpUrl.substring(i + 1, next);
		    if (this.uriParam == null || !this.uriParam.containsKey(p)) {
			throw new IllegalArgumentException("URI参数错误，请设置参数：" + p
				+ "的值");
		    }
		    try {
			sb.append(URLEncoder.encode(
				this.uriParam.getProperty(p), "UTF-8"));
		    } catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		    }

		    i = next;
		} else {
		    sb.append(c);
		}
	    }

	    return sb.toString();
	}

	public int executeMethod(HttpMethod method) throws HttpException,
		IOException {
	    log("[操作=execute][method=" + method.getName() + "][URL="
		    + method.getPath() + "]");
	    return getHttpClient().executeMethod(method);
	}

	private NameValuePair[] getNameValuePair(JSONArray params) {
	    NameValuePair[] nvp = new NameValuePair[params.size()];
	    RuntimeFormManager rfm = null;
	    if (this.uriParam != null
		    && this.uriParam.containsKey(UserContext.class))
		rfm = new RuntimeFormManager(
			(UserContext) this.uriParam.get(UserContext.class));
	    else {
		rfm = new RuntimeFormManager();
	    }

	    for (int i = 0; i < params.size(); ++i) {
		JSONArray ja = params.getJSONArray(i);
		String key = ja.getString(0);
		String v = this.uriParam.getProperty(key) != null ? this.uriParam
			.getProperty(key) : ja.getString(1);
		nvp[i] = new NameValuePair(key, rfm.convertMacrosValue(v));
	    }

	    return nvp;
	}

	public HttpMethod getMethod() {
	    String httpUrl = prepareUri();
	    String method = this.jo.getString("method");
	    JSONArray params = this.jo.getJSONArray("params");
	    NameValuePair[] nvp = getNameValuePair(params);
	    if (method.equals("GET")) {
		GetMethod gm = new GetMethod(httpUrl);
		gm.setQueryString(nvp);
		return gm;
	    }
	    if (method.equals("PUT")) {
		PutMethod pm = new PutMethod(httpUrl);
		pm.setQueryString(nvp);
		return pm;
	    }
	    if (method.equals("DELETE")) {
		DeleteMethod dm = new DeleteMethod(httpUrl);
		dm.setQueryString(nvp);
		return dm;
	    }
	    PostMethod pm = new PostMethod(httpUrl);
	    pm.addParameters(nvp);
	    return pm;
	}

	private void log(String msg) {
	    if (loggable())
		IntegratedLogger.logCC(getConnector()._uuid, HTTPProfile
			.getInstance().getType(), msg);
	}

	protected Connector getConnector() {
	    return this.http;
	}

	protected void close() {
	    getConnector().close();
	}
    }

    public static class JDE extends Adapter {
	private JdeHandler _jdehandler = null;
	private CPModel _model;

	private JDE(String uuid) throws CCException {
	    try {
		this._model = CCUtil.profileExistCheck(uuid);

		this._jdehandler = JdeHandler.getInstance();
	    } catch (Exception e) {
		throw new CCException(e);
	    }
	}

	public static JDE binding(String uuid) throws CCException {
	    return new JDE(uuid);
	}

	public String execute(String xmlRequest) throws Exception {
	    OutputStreamWriter writer = null;
	    BufferedReader reader = null;
	    try {
		URLConnection conn = this._jdehandler
			.getConnection(this._model._profile);

		conn.connect();

		writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
		writer.write(xmlRequest);
		writer.flush();
		writer.close();
		writer = null;

		reader = new BufferedReader(new InputStreamReader(
			conn.getInputStream(), "UTF-8"));

		StringBuilder sbResponse = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
		    sbResponse.append(line);
		}

		log("[操作=execute][URL=" + conn.getURL().toString() + "]"
			+ "[PARA=" + xmlRequest + "]");
		return sbResponse.toString();
	    } finally {
		if (writer != null)
		    try {
			writer.close();
		    } catch (IOException localIOException2) {
		    }
		if (reader != null)
		    try {
			reader.close();
		    } catch (IOException localIOException3) {
		    }
	    }
	}

	private void log(String msg) {
	    if (loggable())
		IntegratedLogger.logCC(this._model._uuid, HTTPProfile
			.getInstance().getType(), msg);
	}

	protected Connector getConnector() {
	    return null;
	}

	public void close() {
	}
    }

    public static class JMS extends Adapter {
	private Connector.JMS jms;
	private ConnectionFactory cf;

	private JMS(String uuid) throws CCException {
	    this.jms = Connector.JMS.binding(uuid);
	    this.cf = this.jms.getConnectionFactory();
	}

	public static JMS binding(String uuid) throws CCException {
	    return new JMS(uuid);
	}

	public javax.jms.Connection open() throws JMSException {
	    log("[操作=open Connection]");
	    return this.cf.createConnection();
	}

	private void log(String msg) {
	    if (loggable())
		IntegratedLogger.logCC(getConnector()._uuid, JMSProfile
			.getInstance().getType(), msg);
	}

	public Session createSession(javax.jms.Connection conn)
		throws JMSException {
	    JSONObject jo = JSONObject.fromObject(this.jms._model._profile);
	    int ack = Util.isBlank(jo, "acknowledgeMode") ? 1 : Integer
		    .parseInt(jo.getString("acknowledgeMode"));
	    boolean tran = Util.isBlank(jo, "transacted") ? false : Boolean
		    .parseBoolean(jo.getString("transacted"));
	    log("[操作=createSession][ACKNOWLEDGE=" + ack + "][TRANSACTED="
		    + tran + "]");
	    return conn.createSession(tran, ack);
	}

	public Collection discoverDestination() throws Exception {
	    JSONObject jo = JSONObject.fromObject(this.jms._model._profile);
	    String vendor = jo.getString("vendor");
	    PlugInModel pi = ProviderFactory.findPlugIn(vendor);
	    log("[操作=discoverDestination][vendor=" + vendor + "]");
	    if (pi != null && pi.getPluginFactory() != null
		    && !"".equals(pi.getPluginFactory())) {
		PlugInFactory factory = (PlugInFactory) Class.forName(
			pi.getPluginFactory()).newInstance();
		Util.populateBeanFromJSONArrayProperties(factory,
			jo.getString("jmx"));
		PlugIn plugIn = factory.createPlugIn(this.jms._model);
		return plugIn.discoverDestinations();
	    }

	    throw new CCException("CC JMS[" + vendor + "]类型插件未实现");
	}

	public Destination resolveDestination(Session s, DestinationModel d)
		throws Exception {
	    JSONObject jo = JSONObject.fromObject(this.jms._model._profile);
	    String vendor = jo.getString("vendor");
	    log("[操作=resolveDestination][vendor=" + vendor + "]");
	    PlugInModel pi = ProviderFactory.findPlugIn(vendor);
	    if (pi != null && pi.getPluginFactory() != null
		    && !"".equals(pi.getPluginFactory())) {
		PlugInFactory factory = (PlugInFactory) Class.forName(
			pi.getPluginFactory()).newInstance();
		Util.populateBeanFromJSONArrayProperties(factory,
			jo.getString("jmx"));
		PlugIn plugIn = factory.createPlugIn(this.jms._model);
		return plugIn.resolveDestination(s, d);
	    }

	    throw new CCException("CC JMS[" + vendor + "]类型插件未实现");
	}

	protected Connector getConnector() {
	    return this.jms;
	}
    }

    public static class NativeCall extends Adapter {
	private String _uuid;
	private Map _env;
	CommandLine _commandLine;
	private Executor executor;
	private Connector.NativeCall nc;
	private OutputStream out;
	private OutputStream err = System.err;
	private InputStream input;

	private void log(String msg) {
	    if (loggable())
		IntegratedLogger.logCC(getConnector()._uuid, NativeCallProfile
			.getInstance().getType(), msg);
	}

	private NativeCall(String uuid) throws CCException {
	    this._uuid = uuid;
	    this.nc = Connector.NativeCall.binding(uuid);
	    this.executor = this.nc.getExecutor();

	    this.executor
		    .setProcessDestroyer(new ShutdownHookProcessDestroyer());

	    initConfig();
	}

	public static NativeCall binding(String uuid) throws CCException {
	    return new NativeCall(uuid);
	}

	protected Connector getConnector() {
	    return this.nc;
	}

	private void initConfig() {
	    JSONObject jo = JSONObject
		    .fromObject(CPCache.getModel(this._uuid)._profile);
	    this._commandLine = CommandLine.parse(CCUtil.getString(jo,
		    "command"));
	    String args = CCUtil.getString(jo, "args");
	    if (!"".equals(args)) {
		String sep = CCUtil.getString(jo, "separator");
		String regex = (sep.equals("comma")) ? ",+" : " +";
		this._commandLine.addArguments(args.split(regex));
	    }

	    String workDirectory = CCUtil.getString(jo, "workingdDirectory");
	    if (!"".equals(workDirectory))
		this.executor.setWorkingDirectory(new File(workDirectory));
	}

	public int execute() throws CCException {
	    try {
		this.executor.setStreamHandler(new PumpStreamHandler(this.out,
			this.err, this.input));
		int r = this.executor.execute(this._commandLine, this._env);
		log("[操作=execute][结果=" + r + "]");
		return r;
	    } catch (Exception e) {
		throw new CCException(e);
	    }
	}

	public void setProcessTimeout(int timeout) {
	    if (timeout > 0) {
		log("[操作=setProcessTimeout][timeout=" + timeout + "]");
		this.executor.setWatchdog(new ExecuteWatchdog(timeout));
	    }
	}

	public void setOutputStream(OutputStream out) {
	    this.out = out;
	}

	public void setErrorStream(OutputStream err) {
	    this.err = err;
	}

	public void setInputStream(InputStream input) {
	    this.input = input;
	}

	public void setEnvMap(Hashtable env) {
	    this._env = env;
	}

	public void setExitValue(int value) {
	    this.executor.setExitValue(value);
	}

	public void setExitValues(int[] values) {
	    this.executor.setExitValues(values);
	}

	public boolean isFailure(int exitValue) {
	    return this.executor.isFailure(exitValue);
	}
    }

    public static class SAP extends Adapter {
	private SapHandler _saphandler = null;
	private CPModel _model;

	private SAP(String uuid) throws CCException {
	    try {
		this._model = CCUtil.profileExistCheck(uuid);

		this._saphandler = SapHandler.getInstance();
	    } catch (Exception e) {
		throw new CCException(e);
	    }
	}

	public static SAP binding(String uuid) throws CCException {
	    return new SAP(uuid);
	}

	public String execute(String xmlRequest) throws Exception {
	    OutputStreamWriter writer = null;
	    BufferedReader reader = null;
	    try {
		URLConnection conn = this._saphandler
			.getConnection(this._model._profile);

		conn.connect();

		writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
		writer.write(xmlRequest);
		writer.flush();
		writer.close();
		writer = null;

		reader = new BufferedReader(new InputStreamReader(
			conn.getInputStream(), "UTF-8"));

		StringBuilder sbResponse = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
		    sbResponse.append(line);
		}

		log("[操作=execute][URL=" + conn.getURL().toString() + "]"
			+ "[PARA=" + xmlRequest + "]");
		return sbResponse.toString();
	    } finally {
		if (writer != null)
		    try {
			writer.close();
		    } catch (IOException localIOException2) {
		    }
		if (reader != null)
		    try {
			reader.close();
		    } catch (IOException localIOException3) {
		    }
	    }
	}

	private void log(String msg) {
	    if (loggable())
		IntegratedLogger.logCC(this._model._uuid, HTTPProfile
			.getInstance().getType(), msg);
	}

	protected Connector getConnector() {
	    return null;
	}

	public void close() {
	}
    }

    public static class WS extends Adapter {
	private Connector.WS ws;
	private ServiceConfig cfgUtil;

	private WS(String uuid) throws CCException {
	    this.ws = Connector.WS.binding(uuid);
	    this.cfgUtil = this.ws.getConfig();
	}

	public ServiceConfig getMetaDataObj() {
	    return this.cfgUtil;
	}

	public static WS binding(String uuid) throws CCException {
	    return new WS(uuid);
	}

	private Dispatch createDispatch() {
	    Service sv = Service.create(this.cfgUtil.getServiceName());
	    QName p = this.cfgUtil.getPort();
	    sv.addPort(p, this.cfgUtil.getBindingId(),
		    this.cfgUtil.getAddress());
	    Dispatch dip = sv.createDispatch(p, SOAPMessage.class,
		    Service.Mode.MESSAGE);
	    prepare(dip);
	    return dip;
	}

	public Set getDynamicParameters() {
	    String input = this.cfgUtil.getInput();
	    Set set = new HashSet();
	    Pattern p = Pattern.compile("\\$\\{(.+?)}");
	    Matcher m = p.matcher(input);
	    while (m.find()) {
		set.add(m.group(1));
	    }

	    return set;
	}

	private void prepare(Dispatch dis) {
	    if (this.cfgUtil.useSoapAction()) {
		dis.getRequestContext().put(
			"javax.xml.ws.soap.http.soapaction.uri",
			this.cfgUtil.getSoapAction());
	    }

	    Binding binding = ((DispatchImpl) dis).getBinding();
	    if (binding instanceof SOAPBinding) {
		((SOAPBinding) binding).setMTOMEnabled(this.cfgUtil.isMotm());
	    }

	    ((DispatchImpl) dis).getClient().getOutInterceptors()
		    .addAll(this.cfgUtil.getOutWSSec());
	    ((DispatchImpl) dis).getClient().getInInterceptors()
		    .addAll(this.cfgUtil.getInWSSec());

	    if (this.cfgUtil.useHttpBasic()) {
		dis.getRequestContext().put(
			"javax.xml.ws.security.auth.username",
			this.cfgUtil.getHttpUser());
		dis.getRequestContext().put(
			"javax.xml.ws.security.auth.password",
			this.cfgUtil.getHttpPassword());
	    }
	}

	public SOAPMessage invoke() throws CCException {
	    return invoke(null);
	}

	public SOAPMessage invoke(Hashtable dynamicValues) throws CCException {
	    Dispatch dp = createDispatch();
	    log("[操作=invoke]");

	    if (dynamicValues == null) {
		dynamicValues = new Hashtable();
	    }

	    String input = replace(this.cfgUtil.getInput(), dynamicValues,
		    "${", "}");

	    StringReader sr = new StringReader(
		    new RuntimeFormManager().convertMacrosValue(input));
	    SOAPMessage msg = null;
	    try {
		MessageFactory mf = MessageFactory.newInstance();
		if ("http://www.w3.org/2003/05/soap/bindings/HTTP/"
			.equals(this.cfgUtil.getBindingId())) {
		    mf = MessageFactory.newInstance();
		}

		msg = mf.createMessage();
		msg.getSOAPPart().setContent(new StreamSource(sr));
	    } catch (Exception e) {
		throw new CCException(e);
	    }

	    if (this.cfgUtil.isOneWay()) {
		dp.invokeOneWay(msg);
		return null;
	    }

	    return (SOAPMessage) dp.invoke(msg);
	}

	private static String replace(String content, Hashtable tagKeyValue,
		String taghead, String tagtail) {
	    int begin = 0;
	    int end = 0;
	    StringBuffer repString = new StringBuffer();

	    boolean firstsign = true;

	    while (begin != -1) {
		begin = end;

		if (firstsign)
		    begin = content.indexOf(taghead, begin);
		else {
		    begin = content.indexOf(taghead, begin + 1);
		}

		if (begin == -1 && firstsign) {
		    repString.append(content.substring(end));
		} else if (begin == -1 && !firstsign) {
		    repString.append(content.substring(end + 1));
		} else {
		    if (!firstsign)
			repString.append(content.substring(end + 1, begin));
		    else {
			repString.append(content.substring(end, begin));
		    }

		    end = content.indexOf(tagtail, begin);
		    String tag = content.substring(begin + taghead.length(),
			    end);

		    if (tagKeyValue.get(tag) != null) {
			String reptag = tagKeyValue.get(tag).toString();
			repString.append(reptag);
		    } else {
			String reptag = taghead + tag + tagtail;
			repString.append(reptag);
		    }
		}

		firstsign = false;
	    }

	    return repString.toString();
	}

	protected Connector getConnector() {
	    return this.ws;
	}

	private void log(String msg) {
	    if (loggable())
		IntegratedLogger.logCC(getConnector()._uuid, WSProfile
			.getInstance().getType(), msg);
	}

	public BindingOperation getBindingOperation() throws WSDLException {
	    return this.cfgUtil.getOperation();
	}

	public Definition getDefinition() throws WSDLException {
	    String wsdlUrl = this.ws.getValue("wsdlUrl");
	    log("[操作=getDefinition]");
	    return ((WSDLManager) getBus().getExtension(WSDLManager.class))
		    .getDefinition(wsdlUrl);
	}

	public String buildStringMessageFromInput(boolean buildOptional)
		throws Exception {
	    return buildSoapMessageFromInput(getBindingOperation(),
		    buildOptional);
	}

	private String buildSoapMessageFromInput(
		BindingOperation bindingOperation, boolean buildOptional)
		throws Exception {
	    return buildSoapMessageFromInput(bindingOperation, buildOptional,
		    true);
	}

	private String buildSoapMessageFromInput(
		BindingOperation bindingOperation, boolean buildOptional,
		boolean alwaysBuildHeaders) throws Exception {
	    String url = getString("wsdlUrl");
	    Definition d = getDefinition();
	    SchemaTypeSystem ss = WsdlUtils.loadSchemaTypes(url);
	    SoapVersion sv = null;

	    BindingInput bindingInput = bindingOperation.getBindingInput();
	    if (bindingInput != null) {
		if (WsdlUtils
			.getExtensiblityElement(
				bindingInput.getExtensibilityElements(),
				SOAPBody.class) != null)
		    sv = SoapVersion.Soap11;
		else if (WsdlUtils.getExtensiblityElement(
			bindingInput.getExtensibilityElements(),
			SOAP12Body.class) != null) {
		    sv = SoapVersion.Soap12;
		}
	    }

	    if (sv == null) {
		throw new Exception(bindingOperation.getName().toString()
			+ ":不是soap绑定");
	    }

	    SoapMessageBuilder sb = new SoapMessageBuilder(d, ss, sv);
	    return sb.buildSoapMessageFromInput(bindingOperation,
		    buildOptional, alwaysBuildHeaders);
	}

	private static Bus getBus() {
	    return BusFactory.getThreadDefaultBus();
	}
    }
}