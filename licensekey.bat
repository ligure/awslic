@echo off
REM ���뵱ǰ�������ļ����ڵ�Ŀ¼
cd /d %~dp0

rem ����AWSʹ�õ�JDK
rem ---------------------
set JAVA_HOME=.\jdk1.6\

%JAVA_HOME%bin\java %JAVA_OPTS% -jar ./bootstrap.jar -r -lib "./lib;" com.actionsoft.application.server.ServerInfoTools