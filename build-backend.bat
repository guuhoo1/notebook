@echo off
set JAVA_HOME=D:\jdk17
cd /d d:\code\notebook\server
call mvn clean package -DskipTests
