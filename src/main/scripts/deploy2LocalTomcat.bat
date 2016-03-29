@echo off
set PROJECT_DIR="F:\workspace\java\web\idea\ws03\transform-app-server"
set TOMCAT_HOME="E:\development\apache-tomcat-7.0.57"
set WAR_NAME=AppServer.war
cd /d %PROJECT_DIR%
call mvn clean package
cd /d %TOMCAT_HOME%\webapps
del /q %WAR_NAME%
copy %PROJECT_DIR%\target\%WAR_NAME% .
