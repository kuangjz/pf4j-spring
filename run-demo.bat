REM
REM This script creates and run the pf4j-spring demo.
REM

REM create artifacts using maven
call mvn clean package -DskipTests

REM create demo-dist folder
rmdir demo-dist /s /q
mkdir demo-dist
mkdir demo-dist\plugins

REM copy artifacts to demo-dist folder
xcopy demo\app\target\pf4j-spring-demo-app-*.jar demo-dist /s /i
xcopy demo\plugins\plugin1\target\pf4j-spring-demo-plugin1-*.zip demo-dist\plugins /s
xcopy demo\plugins\plugin2\target\pf4j-spring-demo-plugin2-*.zip demo-dist\plugins /s
xcopy demo\plugins\plugin3\target\pf4j-spring-demo-plugin3-*.jar demo-dist\plugins /s
xcopy demo\plugins\enabled.txt demo-dist\plugins /s
xcopy demo\plugins\disabled.txt demo-dist\plugins /s

cd demo-dist

REM unzip app
REM deprecated:  jar xf pf4j-spring-demo-app-*.zip
REM deprecated: del pf4j-spring-demo-app-*.zip

REM run demo
rename pf4j-spring-demo-app-*-SNAPSHOT.jar pf4j-spring-demo.jar
java -jar pf4j-spring-demo.jar

cd ..
