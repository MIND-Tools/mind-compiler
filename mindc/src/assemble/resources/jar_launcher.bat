@REM Copyright (C) 2009 Sogeti High-Tech
@REM Copyright (C) 2014 Schneider Electric
@REM
@REM  This file is part of "Mind Compiler" is free software: you can redistribute 
@REM  it and/or modify it under the terms of the GNU Lesser General Public License 
@REM  as published by the Free Software Foundation, either version 3 of the 
@REM  License, or (at your option) any later version.
@REM 
@REM  This program is distributed in the hope that it will be useful, but WITHOUT 
@REM  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
@REM  FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
@REM  details.
@REM 
@REM  You should have received a copy of the GNU Lesser General Public License
@REM  along with this program.  If not, see <http://www.gnu.org/licenses/>.
@REM 
@REM  Contact: mind@ow2.org
@REM 
@REM  Authors: Edine Coly
@REM  Contributors: Schneider Electric Mind4SE
@REM -----------------------------------------------------------------------------

@REM Generic launcher for Mind tools
@REM
@REM Required parameters:
@REM --------------------
@REM   1st arg: Java class
@REM   Other args: Parameters for command line
@REM
@REM Optional ENV vars:
@REM ------------------
@REM   JAVA_HOME - location of a JRE home dir
@REM

@echo off
setlocal
@REM ==== CHECK JAVA_HOME ===
if "%JAVA_HOME%" == "" goto NoJHome

@REM ==== CHECK JAVA_HOME_EXE ===
"%JAVA_HOME%\bin\java.exe" -? > nul 2>&1
set JAVACMD="%JAVA_HOME%\bin\java.exe"
if ERRORLEVEL 0 goto OkJava
echo.
echo ERROR: JAVA_HOME is set to an invalid directory.
echo JAVA_HOME = "%JAVA_HOME%"
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto error

:NoJHome
@REM ==== CHECK JAVA IS ACCESSIBLE FROM PATH ====
java.exe -? > nul 2>&1
set JAVACMD="java.exe"
if ERRORLEVEL 0 goto OkJava
 
echo.
echo ERROR: Java not found in your system.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto error

:OkJava
@REM use the batch path to determine MIND_HOME
pushd %~dp0..\
set MIND_HOME=%cd%
popd

set MIND_RUNTIME=%MIND_HOME%\runtime
set MIND_LIB=%MIND_HOME%\lib
set MIND_EXT=%MIND_HOME%\ext
if not "%MIND_CLASSPATH%" == "" set MIND_CLASSPATH=%MIND_CLASSPATH%;

for /r "%MIND_LIB%\" %%i in (*.jar) do (
  set VarTmp=%%~fnxi;& call :concat
  )
for /r "%MIND_EXT%\" %%i in (*.jar) do (
  set VarTmp=%%~fnxi;& call :concat
  )

goto :run

:concat
set MIND_CLASSPATH=%VarTmp%%MIND_CLASSPATH%
goto :eof

:run
@REM Split: 1st arg->CMD
set CMD=%1

@REM Split: other args->PARAMS (remove in loop 1st character until space is found)
set PARAMS=%*
:loop
if "%PARAMS:~0,1%" neq " " (
  set PARAMS=%PARAMS:~1%
  goto :loop )

@rem echo CMD=%CMD%
@rem echo PARAMS=%PARAMS%
@rem echo MIND_HOME=%MIND_HOME%
@rem echo MIND_LIB=%MIND_LIB%
@rem echo MIND_EXT=%MIND_EXT%
@rem echo MIND_RUNTIME=%MIND_RUNTIME%
@rem echo MIND_CLASSPATH=%MIND_CLASSPATH%

echo %CMD% | findstr ".*mind.Launcher" > nul 2>&1
if ERRORLEVEL 1 (
  echo %CMD% | findstr ".*doc.Launcher" > nul 2>&1
)
if ERRORLEVEL 1 (
  echo %CMD% | findstr ".*unit.Launcher" > nul 2>&1
)
if NOT ERRORLEVEL 1 (
  @rem echo %JAVACMD% -classpath %MIND_CLASSPATH% %PARAMS% --src-path=%MIND_RUNTIME%
  %JAVACMD% -classpath %MIND_CLASSPATH% %PARAMS% --src-path=%MIND_RUNTIME%
) else (
  @rem echo %JAVACMD% -classpath %MIND_CLASSPATH% %PARAMS%
  %JAVACMD% -classpath %MIND_CLASSPATH% %PARAMS%
)

goto :eof


:error
@REM If not set, force ERRORLEVEL to 1
if NOT ERRORLEVEL 1 exit /b 1