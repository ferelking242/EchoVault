@rem Gradle startup script for Windows

  @if "%DEBUG%"=="" @echo off

  @rem Set local scope for the variables with windows NT shell
  if "%OS%"=="Windows_NT" setlocal

  set DIRNAME=%~dp0
  if "%DIRNAME%"=="" set DIRNAME=.
  @rem This is normally unused
  set APP_BASE_NAME=%~n0
  set APP_HOME=%DIRNAME%

  @rem Resolve any "." and ".." in APP_HOME to make it shorter.
  for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

  set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

  @rem Find java.exe
  if defined JAVA_HOME goto findJavaFromJavaHome

  echo. 1>&2
  echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
  exit /b 1

  :findJavaFromJavaHome
  set JAVA_HOME=%JAVA_HOME:"=%
  set JAVA_EXE=%JAVA_HOME%/bin/java.exe

  if exist "%JAVA_EXE%" goto execute

  echo. 1>&2
  echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
  exit /b 1

  :execute
  "%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

  :end
  if "%ERRORLEVEL%"=="0" goto mainEnd
  exit 1
  :mainEnd
  if "%OS%"=="Windows_NT" endlocal
  