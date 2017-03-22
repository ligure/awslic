package com.actionsoft.awf.workflow.calendar.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.actionsoft.awf.organization.cache.CompanyCache;
import com.actionsoft.awf.organization.cache.DepartmentCache;
import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.model.CompanyModel;
import com.actionsoft.awf.organization.model.DepartmentModel;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.workflow.calendar.cache.WorkTimeCache;
import com.actionsoft.awf.workflow.calendar.model.DayModel;
import com.actionsoft.awf.workflow.calendar.model.HolidayModel;

public class CalWorkTimeImp implements WorkCalendar {
    private HashMap holidayList;
    private HashMap worktimeMap;
    private int weekAdjust;
    private Calendar today;
    private Calendar temp;
    private int total;
    private DayModel dayModel;
    private boolean isCalSecond = false;
    private static HashMap[] worktimeConfig = null;

    static {
	reload();
    }

    public HashMap getHolidayList() {
	return this.holidayList;
    }

    public void setHolidayList(HashMap holidayList) {
	this.holidayList = holidayList;
    }

    public HashMap getWorktimeMap() {
	return this.worktimeMap;
    }

    public void setWorktimeMap(HashMap worktimeMap) {
	this.worktimeMap = worktimeMap;
    }

    public static HashMap[] getWorktimeConfig() {
	return worktimeConfig;
    }

    public static void reload() {
	worktimeConfig = WorkTimeCache.loadWorkTimeConfig();
    }

    public static CalWorkTimeImp getInstance() {
	CalWorkTimeImp instance = new CalWorkTimeImp();
	HashMap holidayListTmp = (HashMap) worktimeConfig[1].get("default");
	HashMap worktimeMapTmp = (HashMap) worktimeConfig[0].get("default");
	instance.setHolidayList(holidayListTmp);
	instance.setWorktimeMap(worktimeMapTmp);
	instance.setToday(Calendar.getInstance());
	instance.setTemp(Calendar.getInstance());
	instance.setWeekAdjust(-1);
	instance.setTotal(0);
	return instance;
    }

    public static CalWorkTimeImp getInstanceByUid(String uid) {
	String local = getLocal(uid);
	return getInstanceByLocal(local);
    }

    private static String getLocal(String uid) {
	String local = "default";
	UserModel userModel = (UserModel) UserCache.getModel(uid);
	if (userModel != null) {
	    if (userModel.getWorkcanlendar() != null
		    && userModel.getWorkcanlendar().trim().length() > 0) {
		return userModel.getWorkcanlendar();
	    }
	    DepartmentModel deptModel = (DepartmentModel) DepartmentCache
		    .getModel(userModel.getDepartmentId());
	    if (deptModel != null) {
		CompanyModel companyModel = (CompanyModel) CompanyCache
			.getModel(deptModel.getCompanyId());
		if (companyModel != null
			&& companyModel.getWorkcanlendar() != null
			&& companyModel.getWorkcanlendar().trim().length() > 0) {
		    return companyModel.getWorkcanlendar();
		}
	    }
	}
	return local;
    }

    public static CalWorkTimeImp getInstanceByLocal(String local) {
	CalWorkTimeImp instance = new CalWorkTimeImp();
	HashMap holidayListTmp = (HashMap) worktimeConfig[1].get(local);
	HashMap worktimeMapTmp = (HashMap) worktimeConfig[0].get(local);
	if (holidayListTmp == null && worktimeMapTmp == null) {
	    worktimeMapTmp = (HashMap) worktimeConfig[0].get("default");
	    holidayListTmp = (HashMap) worktimeConfig[1].get("default");
	}
	instance.setHolidayList(holidayListTmp);
	instance.setWorktimeMap(worktimeMapTmp);
	instance.setToday(Calendar.getInstance());
	instance.setTemp(Calendar.getInstance());
	instance.setWeekAdjust(-1);
	instance.setTotal(0);
	return instance;
    }

    public Calendar getTemp() {
	return this.temp;
    }

    public void setTemp(Calendar temp) {
	this.temp = temp;
    }

    public Calendar getToday() {
	return this.today;
    }

    public void setToday(Calendar today) {
	this.today = today;
    }

    public int getTotal() {
	return this.total;
    }

    public void setTotal(int total) {
	this.total = total;
    }

    public int calcWorkingTime(Calendar aday) {
	return calculateWorkTime(aday);
    }

    public int calcWorkingTimeOfSecond(Calendar aday) {
	this.isCalSecond = true;
	DayModel tmpdayModel = (DayModel) this.worktimeMap.get(String
		.valueOf(weekday(aday)));
	int total = calculateWorkTime(aday);
	if (tmpdayModel != null && tmpdayModel.getWorkTimeOfDay() == 1438
		&& aday.get(11) == 0) {
	    total += 43200;
	}
	return total;
    }

    private int calculateWorkTime(Calendar createDay) {
	if (createDay.after(this.today)) {
	    return 0;
	}
	if (createDay.get(6) == this.today.get(6)
		&& createDay.get(1) == this.today.get(1)) {
	    if (isWorkday(createDay)) {
		this.total = calToday(createDay, this.today, false);
		return this.total;
	    }
	    return 0;
	}
	if (isWorkday(createDay)) {
	    this.temp = (Calendar) createDay.clone();
	    this.temp.set(11, 23);
	    this.temp.set(12, 59);
	    this.temp.set(13, 59);
	    this.total += calToday(createDay, this.temp, false);
	}
	if (isWorkday(this.today)) {
	    this.temp = (Calendar) this.today.clone();
	    this.temp.set(11, 0);
	    this.temp.set(12, 0);
	    this.temp.set(13, 0);
	    this.total += calToday(this.temp, this.today, true);
	}
	for (createDay.add(6, 1); createDay.get(1) < this.today.get(1)
		|| (createDay.get(1) == this.today.get(1) && createDay.get(6) != this.today
			.get(6));) {
	    if (isWorkday(createDay)) {
		this.dayModel = (DayModel) this.worktimeMap.get(String
			.valueOf(weekday(createDay)));
		if (this.isCalSecond)
		    this.total += this.dayModel.getWorkTimeOfDay() * 60;
		else
		    this.total += this.dayModel.getWorkTimeOfDay();
	    }
	    createDay.add(6, 1);
	}
	return this.total;
    }

    private int calculateWorkTime2(Calendar createDay) {
	if (createDay.after(this.today)) {
	    return 0;
	}
	if (createDay.get(6) == this.today.get(6)
		&& createDay.get(1) == this.today.get(1)) {
	    if (isWorkday(createDay)) {
		this.total = calToday2(createDay, this.today);
		return this.total;
	    }
	    return 0;
	}
	if (isWorkday(createDay)) {
	    this.temp = (Calendar) createDay.clone();
	    this.temp.set(11, 23);
	    this.temp.set(12, 59);
	    this.temp.set(13, 59);
	    this.total += calToday2(createDay, this.temp);
	}
	if (isWorkday(this.today)) {
	    this.temp = (Calendar) this.today.clone();
	    this.temp.set(11, 0);
	    this.temp.set(12, 0);
	    this.temp.set(13, 0);
	    this.total += calToday2(this.temp, this.today);
	}
	for (createDay.add(6, 1); createDay.get(1) < this.today.get(1)
		|| (createDay.get(1) == this.today.get(1) && createDay.get(6) != this.today
			.get(6));) {
	    if (isWorkday(createDay)) {
		this.dayModel = (DayModel) this.worktimeMap.get(String
			.valueOf(weekday(createDay)));
		if (this.isCalSecond)
		    this.total += this.dayModel.getWorkTimeOfDay() * 60;
		else
		    this.total += this.dayModel.getWorkTimeOfDay();
	    }
	    createDay.add(6, 1);
	}
	return this.total;
    }

    public int calToday(Calendar day, Calendar temp, boolean isToday) {
	if (isToday)
	    this.dayModel = (DayModel) this.worktimeMap.get(String
		    .valueOf(weekday(temp)));
	else {
	    this.dayModel = (DayModel) this.worktimeMap.get(String
		    .valueOf(weekday(day)));
	}
	int createTime = timeOfSpace(day);
	int currentlyTime = timeOfSpace(temp);
	if (currentlyTime == 5) {
	    switch (createTime) {
	    case 1:
		if (this.isCalSecond) {
		    return calTime(this.dayModel.getHourOfWorkAM(),
			    this.dayModel.getMinuteOfWorkAM(), 0,
			    this.dayModel.getHourOfRestAM(),
			    this.dayModel.getMinuteOfRestAM(), 0)
			    + calTime(this.dayModel.getHourOfWorkPM(),
				    this.dayModel.getMinuteOfWorkPM(), 0,
				    this.dayModel.getHourOfRestPM(),
				    this.dayModel.getMinuteOfRestPM(), 0);
		}
		return calTime(this.dayModel.getHourOfWorkAM(),
			this.dayModel.getMinuteOfWorkAM(),
			this.dayModel.getHourOfRestAM(),
			this.dayModel.getMinuteOfRestAM())
			+ calTime(this.dayModel.getHourOfWorkPM(),
				this.dayModel.getMinuteOfWorkPM(),
				this.dayModel.getHourOfRestPM(),
				this.dayModel.getMinuteOfRestPM());
	    case 2:
		if (this.isCalSecond) {
		    return calTime(day.get(11), day.get(12), day.get(13),
			    this.dayModel.getHourOfRestAM(),
			    this.dayModel.getMinuteOfRestAM(), 0)
			    + calTime(this.dayModel.getHourOfWorkPM(),
				    this.dayModel.getMinuteOfWorkPM(), 0,
				    this.dayModel.getHourOfRestPM(),
				    this.dayModel.getMinuteOfRestPM(), 0);
		}
		return calTime(day.get(11), day.get(12),
			this.dayModel.getHourOfRestAM(),
			this.dayModel.getMinuteOfRestAM())
			+ calTime(this.dayModel.getHourOfWorkPM(),
				this.dayModel.getMinuteOfWorkPM(),
				this.dayModel.getHourOfRestPM(),
				this.dayModel.getMinuteOfRestPM());
	    case 3:
		if (this.isCalSecond) {
		    return calTime(this.dayModel.getHourOfWorkPM(),
			    this.dayModel.getMinuteOfWorkPM(), 0,
			    this.dayModel.getHourOfRestPM(),
			    this.dayModel.getMinuteOfRestPM(), 0);
		}
		return calTime(this.dayModel.getHourOfWorkPM(),
			this.dayModel.getMinuteOfWorkPM(),
			this.dayModel.getHourOfRestPM(),
			this.dayModel.getMinuteOfRestPM());
	    case 4:
		if (this.isCalSecond) {
		    return calTime(day.get(11), day.get(12), day.get(13),
			    this.dayModel.getHourOfRestPM(),
			    this.dayModel.getMinuteOfRestPM(), 0);
		}
		return calTime(day.get(11), day.get(12),
			this.dayModel.getHourOfRestPM(),
			this.dayModel.getMinuteOfRestPM());
	    case 5:
		return 0;
	    }
	} else if (currentlyTime == 4) {
	    switch (createTime) {
	    case 1:
		if (this.isCalSecond) {
		    return calTime(this.dayModel.getHourOfWorkAM(),
			    this.dayModel.getMinuteOfWorkAM(), 0,
			    this.dayModel.getHourOfRestAM(),
			    this.dayModel.getMinuteOfRestAM(), 0)
			    + calTime(this.dayModel.getHourOfWorkPM(),
				    this.dayModel.getMinuteOfWorkPM(), 0,
				    temp.get(11), temp.get(12), temp.get(13));
		}
		return calTime(this.dayModel.getHourOfWorkAM(),
			this.dayModel.getMinuteOfWorkAM(),
			this.dayModel.getHourOfRestAM(),
			this.dayModel.getMinuteOfRestAM())
			+ calTime(this.dayModel.getHourOfWorkPM(),
				this.dayModel.getMinuteOfWorkPM(),
				temp.get(11), temp.get(12));
	    case 2:
		if (this.isCalSecond) {
		    return calTime(day.get(11), day.get(12), day.get(13),
			    this.dayModel.getHourOfRestAM(),
			    this.dayModel.getMinuteOfRestAM(), 0)
			    + calTime(this.dayModel.getHourOfWorkPM(),
				    this.dayModel.getMinuteOfWorkPM(), 0,
				    temp.get(11), temp.get(12), temp.get(13));
		}
		return calTime(day.get(11), day.get(12),
			this.dayModel.getHourOfRestAM(),
			this.dayModel.getMinuteOfRestAM())
			+ calTime(this.dayModel.getHourOfWorkPM(),
				this.dayModel.getMinuteOfWorkPM(),
				temp.get(11), temp.get(12));
	    case 3:
		if (this.isCalSecond) {
		    return calTime(this.dayModel.getHourOfWorkPM(),
			    this.dayModel.getMinuteOfWorkPM(), 0, temp.get(11),
			    temp.get(12), temp.get(13));
		}
		return calTime(this.dayModel.getHourOfWorkPM(),
			this.dayModel.getMinuteOfWorkPM(), temp.get(11),
			temp.get(12));
	    case 4:
		if (this.isCalSecond) {
		    return calTime(day.get(11), day.get(12), day.get(13),
			    temp.get(11), temp.get(12), temp.get(13));
		}
		return calTime(day.get(11), day.get(12), temp.get(11),
			temp.get(12));
	    case 5:
		return 0;
	    }
	} else if (currentlyTime == 3) {
	    switch (createTime) {
	    case 1:
		if (this.isCalSecond) {
		    return calTime(this.dayModel.getHourOfWorkAM(),
			    this.dayModel.getMinuteOfWorkAM(), 0,
			    this.dayModel.getHourOfRestAM(),
			    this.dayModel.getMinuteOfRestAM(), 0);
		}
		return calTime(this.dayModel.getHourOfWorkAM(),
			this.dayModel.getMinuteOfWorkAM(),
			this.dayModel.getHourOfRestAM(),
			this.dayModel.getMinuteOfRestAM());
	    case 2:
		if (this.isCalSecond) {
		    return calTime(day.get(11), day.get(12), day.get(13),
			    this.dayModel.getHourOfRestAM(),
			    this.dayModel.getMinuteOfRestAM(), 0);
		}
		return calTime(day.get(11), day.get(12),
			this.dayModel.getHourOfRestAM(),
			this.dayModel.getMinuteOfRestAM());
	    case 3:
		return 0;
	    case 4:
		return 0;
	    case 5:
		return 0;
	    }
	} else if (currentlyTime == 2) {
	    switch (createTime) {
	    case 1:
		if (this.isCalSecond) {
		    return calTime(this.dayModel.getHourOfWorkAM(),
			    this.dayModel.getMinuteOfWorkAM(), 0, temp.get(11),
			    temp.get(12), temp.get(13));
		}
		return calTime(this.dayModel.getHourOfWorkAM(),
			this.dayModel.getMinuteOfWorkAM(), temp.get(11),
			temp.get(12));
	    case 2:
		if (this.isCalSecond) {
		    return calTime(day.get(11), day.get(12), day.get(13),
			    temp.get(11), temp.get(12), temp.get(13));
		}
		return calTime(day.get(11), day.get(12), temp.get(11),
			temp.get(12));
	    case 3:
		return 0;
	    case 4:
		return 0;
	    case 5:
		return 0;
	    }
	}
	return 0;
    }

    public int calToday2(Calendar day, Calendar temp) {
	this.dayModel = (DayModel) this.worktimeMap.get(String
		.valueOf(weekday(day)));
	int createTime = timeOfSpace(day);
	int currentlyTime = timeOfSpace(temp);
	if (currentlyTime == 5) {
	    switch (createTime) {
	    case 1:
		if (this.isCalSecond) {
		    return calTime(this.dayModel.getHourOfWorkAM(),
			    this.dayModel.getMinuteOfWorkAM(), 0,
			    this.dayModel.getHourOfRestAM(),
			    this.dayModel.getMinuteOfRestAM(), 0)
			    + calTime(this.dayModel.getHourOfWorkPM(),
				    this.dayModel.getMinuteOfWorkPM(), 0,
				    this.dayModel.getHourOfRestPM(),
				    this.dayModel.getMinuteOfRestPM(), 0);
		}
		return calTime(this.dayModel.getHourOfWorkAM(),
			this.dayModel.getMinuteOfWorkAM(),
			this.dayModel.getHourOfRestAM(),
			this.dayModel.getMinuteOfRestAM())
			+ calTime(this.dayModel.getHourOfWorkPM(),
				this.dayModel.getMinuteOfWorkPM(),
				this.dayModel.getHourOfRestPM(),
				this.dayModel.getMinuteOfRestPM());
	    case 2:
		if (this.isCalSecond) {
		    return calTime(day.get(11), day.get(12), day.get(13),
			    this.dayModel.getHourOfRestAM(),
			    this.dayModel.getMinuteOfRestAM(), 0)
			    + calTime(this.dayModel.getHourOfWorkPM(),
				    this.dayModel.getMinuteOfWorkPM(), 0,
				    this.dayModel.getHourOfRestPM(),
				    this.dayModel.getMinuteOfRestPM(), 0);
		}
		return calTime(day.get(11), day.get(12),
			this.dayModel.getHourOfRestAM(),
			this.dayModel.getMinuteOfRestAM())
			+ calTime(this.dayModel.getHourOfWorkPM(),
				this.dayModel.getMinuteOfWorkPM(),
				this.dayModel.getHourOfRestPM(),
				this.dayModel.getMinuteOfRestPM());
	    case 3:
		if (this.isCalSecond) {
		    return calTime(this.dayModel.getHourOfWorkPM(),
			    this.dayModel.getMinuteOfWorkPM(), 0,
			    this.dayModel.getHourOfRestPM(),
			    this.dayModel.getMinuteOfRestPM(), 0);
		}
		return calTime(this.dayModel.getHourOfWorkPM(),
			this.dayModel.getMinuteOfWorkPM(),
			this.dayModel.getHourOfRestPM(),
			this.dayModel.getMinuteOfRestPM());
	    case 4:
		if (this.isCalSecond) {
		    return calTime(day.get(11), day.get(12), day.get(13),
			    this.dayModel.getHourOfRestPM(),
			    this.dayModel.getMinuteOfRestPM(), 0);
		}
		return calTime(day.get(11), day.get(12),
			this.dayModel.getHourOfRestPM(),
			this.dayModel.getMinuteOfRestPM());
	    case 5:
		return 0;
	    }
	} else if (currentlyTime == 4) {
	    switch (createTime) {
	    case 1:
		if (this.isCalSecond) {
		    return calTime(this.dayModel.getHourOfWorkAM(),
			    this.dayModel.getMinuteOfWorkAM(), 0,
			    this.dayModel.getHourOfRestAM(),
			    this.dayModel.getMinuteOfRestAM(), 0)
			    + calTime(this.dayModel.getHourOfWorkPM(),
				    this.dayModel.getMinuteOfWorkPM(), 0,
				    temp.get(11), temp.get(12), temp.get(13));
		}
		return calTime(this.dayModel.getHourOfWorkAM(),
			this.dayModel.getMinuteOfWorkAM(),
			this.dayModel.getHourOfRestAM(),
			this.dayModel.getMinuteOfRestAM())
			+ calTime(this.dayModel.getHourOfWorkPM(),
				this.dayModel.getMinuteOfWorkPM(),
				temp.get(11), temp.get(12));
	    case 2:
		if (this.isCalSecond) {
		    return calTime(day.get(11), day.get(12), day.get(13),
			    this.dayModel.getHourOfRestAM(),
			    this.dayModel.getMinuteOfRestAM(), 0)
			    + calTime(this.dayModel.getHourOfWorkPM(),
				    this.dayModel.getMinuteOfWorkPM(), 0,
				    temp.get(11), temp.get(12), temp.get(13));
		}
		return calTime(day.get(11), day.get(12),
			this.dayModel.getHourOfRestAM(),
			this.dayModel.getMinuteOfRestAM())
			+ calTime(this.dayModel.getHourOfWorkPM(),
				this.dayModel.getMinuteOfWorkPM(),
				temp.get(11), temp.get(12));
	    case 3:
		if (this.isCalSecond) {
		    return calTime(this.dayModel.getHourOfWorkPM(),
			    this.dayModel.getMinuteOfWorkPM(), 0, temp.get(11),
			    temp.get(12), temp.get(13));
		}
		return calTime(this.dayModel.getHourOfWorkPM(),
			this.dayModel.getMinuteOfWorkPM(), temp.get(11),
			temp.get(12));
	    case 4:
		if (this.isCalSecond) {
		    return calTime(day.get(11), day.get(12), day.get(13),
			    temp.get(11), temp.get(12), temp.get(13));
		}
		return calTime(day.get(11), day.get(12), temp.get(11),
			temp.get(12));
	    case 5:
		return 0;
	    }
	} else if (currentlyTime == 3) {
	    switch (createTime) {
	    case 1:
		if (this.isCalSecond) {
		    return calTime(this.dayModel.getHourOfWorkAM(),
			    this.dayModel.getMinuteOfWorkAM(), 0,
			    this.dayModel.getHourOfRestAM(),
			    this.dayModel.getMinuteOfRestAM(), 0);
		}
		return calTime(this.dayModel.getHourOfWorkAM(),
			this.dayModel.getMinuteOfWorkAM(),
			this.dayModel.getHourOfRestAM(),
			this.dayModel.getMinuteOfRestAM());
	    case 2:
		if (this.isCalSecond) {
		    return calTime(day.get(11), day.get(12), day.get(13),
			    this.dayModel.getHourOfRestAM(),
			    this.dayModel.getMinuteOfRestAM(), 0);
		}
		return calTime(day.get(11), day.get(12),
			this.dayModel.getHourOfRestAM(),
			this.dayModel.getMinuteOfRestAM());
	    case 3:
		return 0;
	    case 4:
		return 0;
	    case 5:
		return 0;
	    }
	} else if (currentlyTime == 2) {
	    switch (createTime) {
	    case 1:
		if (this.isCalSecond) {
		    return calTime(this.dayModel.getHourOfWorkAM(),
			    this.dayModel.getMinuteOfWorkAM(), 0, temp.get(11),
			    temp.get(12), temp.get(13));
		}
		return calTime(this.dayModel.getHourOfWorkAM(),
			this.dayModel.getMinuteOfWorkAM(), temp.get(11),
			temp.get(12));
	    case 2:
		if (this.isCalSecond) {
		    return calTime(day.get(11), day.get(12), day.get(13),
			    temp.get(11), temp.get(12), temp.get(13));
		}
		return calTime(day.get(11), day.get(12), temp.get(11),
			temp.get(12));
	    case 3:
		return 0;
	    case 4:
		return 0;
	    case 5:
		return 0;
	    }
	}
	return 0;
    }

    private int timeOfSpace(Calendar day) {
	if (day.get(11) < this.dayModel.getHourOfWorkAM()
		|| (day.get(11) == this.dayModel.getHourOfWorkAM() && day
			.get(12) <= this.dayModel.getMinuteOfWorkAM())) {
	    return 1;
	}
	if (day.get(11) < this.dayModel.getHourOfRestAM()
		|| (day.get(11) == this.dayModel.getHourOfRestAM() && day
			.get(12) <= this.dayModel.getMinuteOfRestAM())) {
	    return 2;
	}
	if (day.get(11) < this.dayModel.getHourOfWorkPM()
		|| (day.get(11) == this.dayModel.getHourOfWorkPM() && day
			.get(12) <= this.dayModel.getMinuteOfWorkPM())) {
	    return 3;
	}
	if (day.get(11) < this.dayModel.getHourOfRestPM()
		|| (day.get(11) == this.dayModel.getHourOfRestPM() && day
			.get(12) <= this.dayModel.getMinuteOfRestPM())) {
	    return 4;
	}
	return 5;
    }

    private int calTime(int firstHour, int firstMinute, int secondHour,
	    int secondMinute) {
	if (firstHour > secondHour
		|| (firstHour == secondHour && firstMinute >= secondMinute)) {
	    return 0;
	}
	return (secondHour - firstHour) * 60 + secondMinute - firstMinute;
    }

    private int calTime(int firstHour, int firstMinute, int firstSecond,
	    int secondHour, int secondMinute, int secondSecond) {
	if (firstHour > secondHour
		|| (firstHour == secondHour && firstMinute >= secondMinute && firstSecond >= secondSecond)) {
	    return 0;
	}
	return (secondHour - firstHour) * 60 * 60
		+ (secondMinute - firstMinute) * 60 + secondSecond
		- firstSecond;
    }

    public boolean isHoliday(Calendar day) {
	HashMap holidayList = getHolidayList();
	String dayStr = DateFormat.getDateInstance().format(day.getTime());
	try {
	    Date cur = DateFormat.getDateInstance().parse(dayStr);
	    for (int i = 0; i < holidayList.size(); ++i) {
		HolidayModel model = (HolidayModel) holidayList.get(String
			.valueOf(i));
		Date datefrom = model.getDateFrom();
		Date dateend = model.getDateEnd();
		if ((cur.before(dateend) && cur.after(datefrom))
			|| cur.equals(datefrom) || cur.equals(dateend)) {
		    return true;
		}
	    }
	} catch (Exception localException) {
	}
	return false;
    }

    public boolean isWorkday(Calendar day) {
	return this.worktimeMap.containsKey(String.valueOf(weekday(day)))
		&& !(isHoliday(day));
    }

    private int weekday(Calendar day) {
	return day.get(7) + this.weekAdjust == 0 ? 7 : day.get(7)
		+ this.weekAdjust;
    }

    public String getNHourAfterDate(Calendar currentDay, int m) {
	String date = getNHourseDate(currentDay, m * 60);
	return date;
    }

    private String getNHourseDate(Calendar currentDay, int m) {
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Calendar n = (Calendar) currentDay.clone();
	currentDay.add(12, m);
	Calendar afterCalendar = currentDay;
	CalWorkTimeImp instan = getInstance();
	addDate(afterCalendar);
	addDate(n);
	this.today.setTime(afterCalendar.getTime());
	instan.setToday(this.today);

	int realityM = instan.calculateWorkTime2(n);

	DayModel tmpdayModel = (DayModel) this.worktimeMap.get(String
		.valueOf(weekday(n)));
	if (tmpdayModel != null && tmpdayModel.getWorkTimeOfDay() == 1440) {
	    realityM += 720;
	}

	this.dayModel = (DayModel) this.worktimeMap.get(String
		.valueOf(weekday(afterCalendar)));
	int afterTime = timeOfSpace(afterCalendar);
	switch (afterTime) {
	case 1:
	    rsetDate(afterCalendar, this.dayModel.getHourOfWorkAM(),
		    this.dayModel.getMinuteOfWorkAM());
	    m -= realityM;
	    if (m > 0) {
		getNHourseDate(afterCalendar, m);
	    }

	    break;
	case 2:
	    m -= realityM;
	    if (m > 0) {
		getNHourseDate(afterCalendar, m);
	    }
	    break;
	case 3:
	    rsetDate(afterCalendar, this.dayModel.getHourOfWorkPM(),
		    this.dayModel.getMinuteOfWorkPM());
	    m -= realityM;
	    if (m > 0) {
		getNHourseDate(afterCalendar, m);
	    }
	    break;
	case 4:
	    m -= realityM;
	    if (m > 0) {
		getNHourseDate(afterCalendar, m);
	    }
	    break;
	case 5:
	    rsetDate(afterCalendar, this.dayModel.getHourOfWorkAM(),
		    this.dayModel.getMinuteOfWorkAM());
	    afterCalendar.add(5, 1);

	    m -= realityM;
	    if (m > 0) {
		getNHourseDate(afterCalendar, m);
	    }

	}

	Date afterDate = afterCalendar.getTime();

	return format.format(afterDate);
    }

    public void addDate(Calendar day) {
	this.dayModel = (DayModel) this.worktimeMap.get(String
		.valueOf(weekday(day)));
	if (this.dayModel.getWorkTimeOfDay() == 0) {
	    day.add(5, 1);
	    addDate(day);
	}
    }

    private void rsetDate(Calendar afterCalendar, int myhour, int myminute) {
	int hour = afterCalendar.get(11);
	int minute = afterCalendar.get(12);
	afterCalendar.add(11, -hour);
	afterCalendar.add(12, -minute);
	afterCalendar.add(11, myhour);

	afterCalendar.add(12, myminute);
    }

    public static void main(String[] args) throws ParseException {
	test2();
    }

    private static void test2() throws ParseException {
	CalWorkTimeImp instan = getInstance();
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Date start = df.parse("2013-05-15 9:30:00");
	Calendar startC = Calendar.getInstance();
	Calendar l = (Calendar) startC.clone();
	startC.setTime(start);
	for (int i = 1; i <= 10; ++i) {
	    l = (Calendar) startC.clone();
	    String str = instan.getNHourAfterDate(l, 60 * i);
	}
    }

    public static void test() throws ParseException {
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Date start = df.parse("2013-05-17 17:30:00");
	Date end = df.parse("2013-05-16 13:30:00");
	CalWorkTimeImp instan = getInstance();
	Calendar today = Calendar.getInstance();
	today.setTime(end);
	instan.setToday(today);
	Calendar startC = Calendar.getInstance();
	startC.setTime(start);
	int m = instan.calculateWorkTime2(startC);
    }

    public void setWeekAdjust(int weekAdjust) {
	this.weekAdjust = weekAdjust;
    }

    public int getWeekAdjust() {
	return this.weekAdjust;
    }
}