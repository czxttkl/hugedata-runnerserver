@echo off

REM Copyright 2012 AT&T
 
REM Licensed under the Apache License, Version 2.0 (the "License");
REM you may not use this file except in compliance with the License.
REM You may obtain a copy of the License at

REM http://www.apache.org/licenses/LICENSE-2.0

REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.

@setlocal

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVA%" == "" set _JAVA=%JAVA_HOME%\bin\java.exe
goto run

:noJavaHome
if "%_JAVA%" == "" set _JAVA=java.exe

:run
"%_JAVA%" -classpath "lib\runnerserver.jar;%ANDROID_HOME%\tools\lib\chimpchat.jar;" -Xms100m -Xmx1024m com.czxttkl.hugedata.server.RunnerServer

