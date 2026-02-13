@ECHO OFF
SETLOCAL

SET DIRNAME=%~dp0
IF "%DIRNAME%"=="" SET DIRNAME=.
SET APP_HOME=%DIRNAME%

SET JAVA_EXE=java.exe
IF DEFINED JAVA_HOME SET JAVA_EXE=%JAVA_HOME%\bin\java.exe

"%JAVA_EXE%" -Dorg.gradle.appname=gradlew -classpath "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*

ENDLOCAL
