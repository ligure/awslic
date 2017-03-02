@echo off
REM 进入当前批处理文件所在的目录
cd /d %~dp0

rem 设置AWS使用的JDK
rem ---------------------
set JAVA_HOME=.\jdk1.6\

%JAVA_HOME%bin\java %JAVA_OPTS% -jar ./bootstrap.jar -r -lib "./lib;" com.actionsoft.application.server.ServerInfoTools