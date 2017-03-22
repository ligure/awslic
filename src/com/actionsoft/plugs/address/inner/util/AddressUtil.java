package com.actionsoft.plugs.address.inner.util;

import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.application.server.conf.AWFServerConf;
import com.actionsoft.awf.organization.cache.DepartmentCache;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.plugs.address.inner.model.OtherAddressInfoModel;
import com.actionsoft.plugs.address.inner.model.OtherAddressModel;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import net.sf.json.JSONObject;
/**
 * 
 * @description 修改系统类，悬浮框增加姓名和部门//增加username和departmentfullname@wangaz20140620
 * @version 1.0
 * @author wangaz
 * @update 2014年6月20日 下午5:00:52
 */
public class AddressUtil
{
  public static OtherAddressModel getCompanyInfo(int paramInt)
  {
    Connection localConnection = null;
    Statement localStatement = null;
    ResultSet localResultSet = null;
    OtherAddressModel localOtherAddressModel = new OtherAddressModel();
    String str = "select * from BO_AWS_ADDRESS where bindid = " + paramInt;
    localConnection = DBSql.open();
    try {
      localStatement = localConnection.createStatement();
      localResultSet = localStatement.executeQuery(str);
      while (localResultSet.next()) {
        localOtherAddressModel = new OtherAddressModel();
        localOtherAddressModel.setId(localResultSet.getInt("ID"));
        localOtherAddressModel.setBindid(localResultSet.getInt("BINDID"));
        localOtherAddressModel.setUnitname(localResultSet.getString("UNITNAME"));
        if (localOtherAddressModel.getUnitname() == null) {
          localOtherAddressModel.setUnitname("");
        }
        localOtherAddressModel.setUnittype(localResultSet.getString("UNITTYPE"));
        if (localOtherAddressModel.getUnittype() == null) {
          localOtherAddressModel.setUnittype("");
        }
        localOtherAddressModel.setUnitadd(localResultSet.getString("UNITADD"));
        if (localOtherAddressModel.getUnitadd() == null) {
          localOtherAddressModel.setUnitadd("");
        }
        localOtherAddressModel.setUnitps(localResultSet.getString("UNITPS"));
        if (localOtherAddressModel.getUnitps() == null) {
          localOtherAddressModel.setUnitps("");
        }
        localOtherAddressModel.setUnittel(localResultSet.getString("UNITTEL"));
        if (localOtherAddressModel.getUnittel() == null) {
          localOtherAddressModel.setUnittel("");
        }
        localOtherAddressModel.setUnitwww(localResultSet.getString("UNITWWW"));
        if (localOtherAddressModel.getUnitwww() == null) {
          localOtherAddressModel.setUnitwww("");
        }
        localOtherAddressModel.setOrderindex(localResultSet.getInt("ORDERINDEX"));
        localOtherAddressModel.setUnitfax(localResultSet.getString("UNITFAX"));
        if (localOtherAddressModel.getUnitfax() == null) {
          localOtherAddressModel.setUnitfax("");
        }
        localOtherAddressModel.setUnitdesc(localResultSet.getString("UNITDESC"));
        if (localOtherAddressModel.getUnitdesc() == null) {
          localOtherAddressModel.setUnitdesc("");
        }
        localOtherAddressModel.setTelznoeno(localResultSet.getString("TELZONENO"));
        if (localOtherAddressModel.getTelznoeno() == null) {
          localOtherAddressModel.setTelznoeno("");
        }

        localOtherAddressModel.setUnitmail(localResultSet.getString("UNITMAIL"));
        if (localOtherAddressModel.getUnitmail() == null)
          localOtherAddressModel.setUnitmail("");
      }
    }
    catch (SQLException localSQLException)
    {
      localSQLException.printStackTrace(System.err);
    } finally {
      DBSql.close(localConnection, localStatement, localResultSet);
    }
    return localOtherAddressModel;
  }

  public static ArrayList execFastSearch(String paramString)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("select * from BO_AWS_ADDRESS_S where DEPTNAME like '%").append(paramString).append("%'").append(" or ").append("USERNAME like '%").append(paramString).append("%'").append(" or ").append("TEL like '%").append(paramString).append("%'").append(" or ").append("FAX like '%").append(paramString).append("%'").append(" or ").append("MOBILE like '%").append(paramString).append("%'").append(" or ").append("EMAIL like '%").append(paramString).append("%'").append(" or ").append("MEMO like '%").append(paramString).append("%'").append(" or ").append("POSTION like '%").append(paramString).append("%'").append(" order by DEPTNAME");

    Connection localConnection = null;
    Statement localStatement = null;
    ResultSet localResultSet = null;
    ArrayList localArrayList = new ArrayList();
    localConnection = DBSql.open();
    OtherAddressInfoModel localOtherAddressInfoModel = null;
    try {
      localStatement = localConnection.createStatement();
      localResultSet = localStatement.executeQuery(localStringBuffer.toString());
      while (localResultSet.next()) {
        localOtherAddressInfoModel = new OtherAddressInfoModel();
        localOtherAddressInfoModel.setBindid(localResultSet.getInt("bindid"));
        localOtherAddressInfoModel.setDeptname(localResultSet.getString("DEPTNAME"));
        localOtherAddressInfoModel.setUsername(localResultSet.getString("USERNAME"));
        localOtherAddressInfoModel.setPostion(localResultSet.getString("POSTION"));
        localOtherAddressInfoModel.setTel(localResultSet.getString("TEL"));
        localOtherAddressInfoModel.setFax(localResultSet.getString("FAX"));

        localOtherAddressInfoModel.setMobile(localResultSet.getString("MOBILE"));
        localOtherAddressInfoModel.setEmail(localResultSet.getString("EMAIL"));
        localOtherAddressInfoModel.setMemo(localResultSet.getString("MEMO"));
        localArrayList.add(localOtherAddressInfoModel);
      }
    }
    catch (SQLException localSQLException) {
      localSQLException.printStackTrace(System.err);
    } finally {
      DBSql.close(localConnection, localStatement, localResultSet);
    }
    return localArrayList;
  }

  public static ArrayList getUnderlingEnterpriseTypeList()
  {
    Connection localConnection = null;
    Statement localStatement = null;
    ResultSet localResultSet = null;
    ArrayList localArrayList = new ArrayList();
    String str = "select DISTINCT UNITTYPE ,ORDERINDEX from BO_AWS_ADDRESS ORDER BY ORDERINDEX";
    localConnection = DBSql.open();
    try {
      localStatement = localConnection.createStatement();
      localResultSet = localStatement.executeQuery(str);
      while (localResultSet.next())
        localArrayList.add(localResultSet.getString("UNITTYPE"));
    }
    catch (Exception localException)
    {
      localException.printStackTrace(System.err);
    } finally {
      DBSql.close(localConnection, localStatement, localResultSet);
    }
    return localArrayList;
  }

  public static ArrayList getOtherAdressInfoList(OtherAddressModel paramOtherAddressModel) {
    Connection localConnection = null;
    Statement localStatement = null;
    ResultSet localResultSet = null;
    ArrayList localArrayList = new ArrayList();
    String str = "select * from BO_AWS_ADDRESS_S where bindid = '" + paramOtherAddressModel.getBindid() + "'";
    localConnection = DBSql.open();
    OtherAddressInfoModel localOtherAddressInfoModel = null;
    try {
      localStatement = localConnection.createStatement();
      localResultSet = localStatement.executeQuery(str);
      while (localResultSet.next()) {
        localOtherAddressInfoModel = new OtherAddressInfoModel();
        localOtherAddressInfoModel.setDeptname(localResultSet.getString("DEPTNAME"));
        localOtherAddressInfoModel.setUsername(localResultSet.getString("USERNAME"));
        localOtherAddressInfoModel.setPostion(localResultSet.getString("POSTION"));
        localOtherAddressInfoModel.setTel(localResultSet.getString("TEL"));
        localOtherAddressInfoModel.setFax(localResultSet.getString("FAX"));
        localOtherAddressInfoModel.setMobile(localResultSet.getString("MOBILE"));
        localOtherAddressInfoModel.setEmail(localResultSet.getString("EMAIL"));
        localOtherAddressInfoModel.setMemo(localResultSet.getString("MEMO"));
        localArrayList.add(localOtherAddressInfoModel);
      }
    } catch (SQLException localSQLException) {
      localSQLException.printStackTrace(System.err);
    } finally {
      DBSql.close(localConnection, localStatement, localResultSet);
    }
    return localArrayList;
  }

  public static ArrayList getOtherAdressList(String paramString)
  {
    Connection localConnection = null;
    Statement localStatement = null;
    ResultSet localResultSet = null;
    ArrayList localArrayList = new ArrayList();
    String str = "select * from  BO_AWS_ADDRESS where UNITTYPE = '" + paramString + "'";
    localConnection = DBSql.open();
    try {
      localStatement = localConnection.createStatement();
      localResultSet = localStatement.executeQuery(str);
      OtherAddressModel localOtherAddressModel = null;
      while (localResultSet.next()) {
        localOtherAddressModel = new OtherAddressModel();
        localOtherAddressModel.setId(localResultSet.getInt("ID"));
        localOtherAddressModel.setBindid(localResultSet.getInt("BINDID"));

        localOtherAddressModel.setUnitname(localResultSet.getString("UNITNAME"));
        if (localOtherAddressModel.getUnitname() == null) {
          localOtherAddressModel.setUnitname("");
        }
        localOtherAddressModel.setUnittype(localResultSet.getString("UNITTYPE"));
        if (localOtherAddressModel.getUnittype() == null) {
          localOtherAddressModel.setUnittype("");
        }
        localOtherAddressModel.setUnitadd(localResultSet.getString("UNITADD"));
        if (localOtherAddressModel.getUnitadd() == null) {
          localOtherAddressModel.setUnitadd("");
        }
        localOtherAddressModel.setUnitps(localResultSet.getString("UNITPS"));
        if (localOtherAddressModel.getUnitps() == null) {
          localOtherAddressModel.setUnitps("");
        }
        localOtherAddressModel.setUnittel(localResultSet.getString("UNITTEL"));
        if (localOtherAddressModel.getUnittel() == null) {
          localOtherAddressModel.setUnittel("");
        }
        localOtherAddressModel.setUnitwww(localResultSet.getString("UNITWWW"));
        if (localOtherAddressModel.getUnitwww() == null) {
          localOtherAddressModel.setUnitwww("");
        }
        localOtherAddressModel.setOrderindex(localResultSet.getInt("ORDERINDEX"));
        localOtherAddressModel.setUnitfax(localResultSet.getString("UNITFAX"));
        if (localOtherAddressModel.getUnitfax() == null) {
          localOtherAddressModel.setUnitfax("");
        }
        localOtherAddressModel.setUnitdesc(localResultSet.getString("UNITDESC"));
        if (localOtherAddressModel.getUnitdesc() == null) {
          localOtherAddressModel.setUnitdesc("");
        }
        localOtherAddressModel.setTelznoeno(localResultSet.getString("TELZONENO"));
        if (localOtherAddressModel.getTelznoeno() == null) {
          localOtherAddressModel.setTelznoeno("");
        }

        localOtherAddressModel.setUnitmail(localResultSet.getString("UNITMAIL"));
        if (localOtherAddressModel.getUnitmail() == null) {
          localOtherAddressModel.setUnitmail("");
        }
        localArrayList.add(localOtherAddressModel);
      }
    } catch (SQLException localSQLException) {
      localSQLException.printStackTrace(System.err);
    } finally {
      DBSql.close(localConnection, localStatement, localResultSet);
    }
    return localArrayList;
  }

  public static String getTooltipSpanId(UserModel paramUserModel, boolean paramBoolean, int paramInt) {
    String str = "tooltip_" + paramUserModel.getId();
    if (paramBoolean) {
      str = "tooltip_" + paramInt;
    }
    return str;
  }

  public static String getTooltipScript(UserModel paramUserModel, String paramString) {
    return getTooltipScript(paramUserModel, paramString, false, 0);
  }

  public static String getTooltipScript(UserModel paramUserModel, String paramString, boolean paramBoolean, int paramInt) {
    StringBuffer localStringBuffer = new StringBuffer();
    String str1 = "uTooltip_" + paramUserModel.getId();
    localStringBuffer.append("<script>");
    localStringBuffer.append("var ").append(str1);
    JSONObject localJSONObject = new JSONObject();
    String str2 = AWFConfig._awfServerConf.getDocumentPath() + "Photo/group" + paramUserModel.getUID() + "/file0/" + paramUserModel.getUID() + ".jpg";
    File localFile = new File(str2);
    if (localFile.exists())
      localJSONObject.put("img", "./downfile.wf?flag1=" + paramUserModel.getUID() + "&flag2=0&sid=" + paramString + "&rootDir=Photo&filename=" + paramUserModel.getUID() + ".jpg");
    else {
      localJSONObject.put("img", "../aws_img/userPhoto.jpg");
    }
    localJSONObject.put("officetel", paramUserModel.getOfficeTel());
    localJSONObject.put("mobile", paramUserModel.getMobile());
    localJSONObject.put("officefax", paramUserModel.getOfficeFax());
    localJSONObject.put("mail", paramUserModel.getEmail());
    localJSONObject.put("hometel", paramUserModel.getHomeTel());
    localJSONObject.put("workstatus", paramUserModel.getWorkStatus());
    //增加username和departmentfullname@wangaz20140620
    localJSONObject.put("username", paramUserModel.getUserName());
    String str = DepartmentCache.getFullName(paramUserModel.getDepartmentId());
    String departfuname = str.substring(0, str.length()-1);
    localJSONObject.put("departfullname", departfuname);
    
    localStringBuffer.append("=").append(localJSONObject.toString()).append(";");
    localStringBuffer.append("if(isShowUserTooltip){showUserTooltip(" + str1 + ",'" + getTooltipSpanId(paramUserModel, paramBoolean, paramInt) + "')}");
    localStringBuffer.append("</script>");
    return localStringBuffer.toString();
  }
}