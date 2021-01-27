@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  pz-zdoc startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..
set INPUT_PATH=%~3

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and PZ_ZDOC_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
set JAVA_EXE=%INPUT_PATH%\jre64\bin\java.exe
if exist JAVA_EXE goto execute

if defined JAVA_HOME goto findJavaFromJavaHome

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto finish

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto validateJavaVersion

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto finish

:validateJavaVersion
@rem Validate java version
set TARGET_JAVA_VERSION=18

for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
)
set JAVA_VERSION=%JAVA_VERSION:"=%
for /f "delims=. tokens=1-3" %%v in ("%JAVA_VERSION%") do (
	@rem Only valid version is java 1.8
    if not %%v%%w == %TARGET_JAVA_VERSION% goto wrongJavaVersion
)
if "%ERRORLEVEL%" == "0" goto execute

:wrongJavaVersion
echo.
echo ERROR: JAVA_HOME points to a wrong Java version (%JAVA_VERSION%).
echo.
echo Please set your JAVA_HOME variable in your environment to match the
echo location of Java version %TARGET_JAVA_VERSION% installation.

goto finish

:execute
@rem Setup the command line
set CLASSPATH=%!classpath!%

@rem Execute pz-zdoc
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %PZ_ZDOC_OPTS% -classpath "%CLASSPATH%" %!mainClassName!% %*

:finish
exit /b 1
