@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  pz-zdoc startup script for Windows
@rem
@rem ##########################################################################

echo Preparing to launch ZomboidDoc...

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and PZ_ZDOC_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Set game directory path to APP_HOME if env var not set
if "%PZ_DIR_PATH%"=="" goto setDirPathToHome

@rem Ensure path is Windows-style path
set PZ_DIR_PATH=%PZ_DIR_PATH:/=\%

@rem Save current directory and change to target directory
pushd %PZ_DIR_PATH% 2>nul
if %ERRORLEVEL% == 1 goto dirNotFoundError

@rem Save value of current directory
set PZ_DIR_ABS_PATH=%CD%

@rem Restore original directory
popd

@rem Turn directory path into absolute path
set PZ_DIR_PATH=%PZ_DIR_ABS_PATH%

if exist %PZ_DIR_PATH% goto checkValidDir

:dirNotFoundError
echo.
echo ERROR: directory %PZ_DIR_PATH% does not exist or is not accessible
echo.
goto finish

:checkValidDir
if exist "%PZ_DIR_PATH%\ProjectZomboid32.exe" goto findJava
echo.
echo ERROR: PZ_DIR_PATH points to an invalid or corrupt game directory: %PZ_DIR_PATH%
echo.
goto finish

:setDirPathToHome
set PZ_DIR_PATH="%APP_HOME%"

@rem Find java.exe
:findJava

echo Project Zomboid directory path:
echo.%PZ_DIR_PATH%

@rem Search for Java executable in game directory first
set JAVA_EXE=%PZ_DIR_PATH%\jre\bin\java.exe
if exist %JAVA_EXE% goto validateJavaVersion

if defined JAVA_HOME goto findJavaFromJavaHome

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto finish

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%\bin\java.exe

if exist "%JAVA_EXE%" goto validateJavaVersion

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto finish

:validateJavaVersion
@rem Validate java version
set JAVA_TARGET_VERSION=18

for /f "tokens=3" %%g in ('%JAVA_EXE% -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION_INFO=%%g
)
set JAVA_VERSION=%JAVA_VERSION_INFO:"=%
for /f "delims=. tokens=1-3" %%v in ("%JAVA_VERSION%") do (
	@rem Only valid version is java 1.8
    if not %%v%%w == %JAVA_TARGET_VERSION% goto wrongJavaVersion
)
if "%ERRORLEVEL%" == "0" goto execute

:wrongJavaVersion
echo.
echo ERROR: JAVA_HOME points to a wrong Java version (%JAVA_VERSION%).
echo.
echo Please set your JAVA_HOME variable in your environment to match the
echo location of Java version %JAVA_TARGET_VERSION% installation.

goto finish

:execute
echo Executing with Java version: %JAVA_VERSION_INFO:"=%
echo %JAVA_EXE%

@rem Setup the command line
set CLASSPATH=%!classpath!%

echo Launching ZomboidDoc...
@rem Execute pz-zdoc
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %PZ_ZDOC_OPTS% -classpath "%CLASSPATH%" %!mainClassName!% %*

:finish
exit /b 0
