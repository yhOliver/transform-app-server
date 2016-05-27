@echo off
set PROJECT_DIR="D:\workspace\transform-app-server"
set TOMCAT_HOME="D:\Software\apache-tomcat-7.0.69"
set WAR_NAME=AppServer.war
cd /d %PROJECT_DIR%
call mvn clean package
cd /d %TOMCAT_HOME%\webapps
del /q %WAR_NAME%
copy %PROJECT_DIR%\target\%WAR_NAME% .
