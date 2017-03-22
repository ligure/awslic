package com.actionsoft.awf.workflow.calendar.cache;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.actionsoft.awf.workflow.calendar.model.DayModel;
import com.actionsoft.awf.workflow.calendar.model.HolidayModel;

public class WorkTimeCache {

    public static HashMap[] loadWorkTimeConfig() {
	HashMap localWorkTimeList = new HashMap();
	HashMap localHolidayList = new HashMap();
	HashMap timezoneList = new HashMap();
	SAXReader saxreader = new SAXReader();
	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	Document doc = DocumentFactory.getInstance().createDocument();
	try {
	    doc = saxreader.read("aws-worktime.xml");
	    Iterator localeConfigIT = doc.getRootElement().elementIterator();
	    localeConfigIT = doc.getRootElement()
		    .selectNodes("//worktime-config/worktime-local").iterator();
	    while (localeConfigIT.hasNext()) {
		Element localElement = (Element) localeConfigIT.next();
		String key = localElement.attributeValue("id");
		Iterator localIterator = localElement.elementIterator();
		while (localIterator.hasNext()) {
		    Element local = (Element) localIterator.next();
		    if (local.getName().equals("workday")) {
			HashMap workTime = new HashMap();
			Iterator dayOfWeekIterator = local.elementIterator();
			while (dayOfWeekIterator.hasNext()) {
			    Element workdayElement = (Element) dayOfWeekIterator
				    .next();
			    DayModel dayModel = new DayModel();
			    dayModel.setDayOfWeek(Integer
				    .parseInt(workdayElement
					    .attributeValue("day")));
			    Iterator workdayIterator = workdayElement
				    .elementIterator();
			    while (workdayIterator.hasNext()) {
				Element workday = (Element) workdayIterator
					.next();
				if (workday.getName().equals("work_time_am")) {
				    dayModel.setHourOfWorkAM(Integer
					    .parseInt(workday.getTextTrim()));
				    dayModel.setMinuteOfWorkAM(Integer
					    .parseInt(workday
						    .attributeValue("minute")));
				} else if (workday.getName().equals(
					"rest_time_am")) {
				    dayModel.setHourOfRestAM(Integer
					    .parseInt(workday.getTextTrim()));
				    dayModel.setMinuteOfRestAM(Integer
					    .parseInt(workday
						    .attributeValue("minute")));
				} else if (workday.getName().equals(
					"work_time_pm")) {
				    dayModel.setHourOfWorkPM(Integer
					    .parseInt(workday.getTextTrim()));
				    dayModel.setMinuteOfWorkPM(Integer
					    .parseInt(workday
						    .attributeValue("minute")));
				} else if (workday.getName().equals(
					"rest_time_pm")) {
				    dayModel.setHourOfRestPM(Integer
					    .parseInt(workday.getTextTrim()));
				    dayModel.setMinuteOfRestPM(Integer
					    .parseInt(workday
						    .attributeValue("minute")));
				}
			    }
			    int worktime = calTime(dayModel.getHourOfWorkAM(),
				    dayModel.getMinuteOfWorkAM(),
				    dayModel.getHourOfRestAM(),
				    dayModel.getMinuteOfRestAM())
				    + calTime(dayModel.getHourOfWorkPM(),
					    dayModel.getMinuteOfWorkPM(),
					    dayModel.getHourOfRestPM(),
					    dayModel.getMinuteOfRestPM());
			    dayModel.setWorkTimeOfDay(worktime);
			    workTime.put(
				    String.valueOf(dayModel.getDayOfWeek()),
				    dayModel);
			}
			localWorkTimeList.put(key, workTime);
		    } else if (local.getName().equals("holiday")) {
			HashMap holidayList = new HashMap();
			Iterator holidayIterator = local.elementIterator();
			while (holidayIterator.hasNext()) {
			    Element holiday = (Element) holidayIterator.next();
			    if (holiday.getName().equals("day_of_month")) {
				HolidayModel model = new HolidayModel();
				String name = holiday.attributeValue("name");
				String dateFrom = holiday
					.attributeValue("datefrom");
				String dateEnd = holiday
					.attributeValue("dateend");
				model.setName(name);
				model.setDateFrom(dateFormat.parse(dateFrom));
				model.setDateEnd(dateFormat.parse(dateEnd));
				holidayList.put(
					String.valueOf(holidayList.size()),
					model);
			    }
			}
			localHolidayList.put(key, holidayList);
		    } else if (local.getName().equals("timezone")) {
			String timezone = local.getText();
			timezoneList.put(key, timezone);
		    }
		}
	    }
	} catch (Exception ex) {
	    System.out.println(ex);
	    System.out.println("请检查“worktime.xml”文件的正确性，您需要确定可能有以下几项：");
	    System.out
		    .println("1.日期、月份、星期、小时、分钟等各种属性的值是否为数值！为非数值的话将会引起时间计算异常。");
	    System.out.println("2.是否有重复的值，这会造成输出结果不正确。");
	    System.out.println("3.设定小时时请使用24小时格式，如下午3点请设为15点。");
	    System.out.println("4.月份全部从1开始，如设定1为1月份，2为2月份，依此类推。");
	    System.out.println("5.日期全部从1开始，如设定1为1号，2为2号，依此类推。");
	    System.out.println("6.月份与日期之间用“/”隔开。");
	    System.out.println("7.星期全部从1开始，如设定1为星期一，2为星期二，依此类推。");
	    System.out.println("8.请按照范例填写各个属性值。");
	    System.out
		    .println("9.各个值的范例是否在规定范围内，月份不能大于12,星期不能大于7,小时不能大于24,分钟不能大于等于60等等！");
	    System.out.println("");
	    System.out
		    .println("如果以上方法不能解决您的问题，那您可以初步确定这是一个Bug，请您联系您的系统管理员或将错误信息发至”Actionsoft“公司，以便我们在软件的下一个版本时修正此Bug。谢谢您的合作！");
	    System.out.println("");
	}
	HashMap[] tmp = new HashMap[3];
	tmp[0] = localWorkTimeList;
	tmp[1] = localHolidayList;
	tmp[2] = timezoneList;
	return tmp;
    }

    private static int calTime(int firstHour, int firstMinute, int secondHour,
	    int secondMinute) {
	if (firstHour > secondHour
		|| (firstHour == secondHour && firstMinute >= secondMinute)) {
	    return 0;
	}
	return (secondHour - firstHour) * 60 + secondMinute - firstMinute;
    }

    public static void main(String[] args) {
	loadWorkTimeConfig();
    }

}