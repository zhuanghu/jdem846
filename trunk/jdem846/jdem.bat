
set CLASSPATH=lib\dom4j-1.6.1.jar
set CLASSPATH=%CLASSPATH%;lib\javassist.jar
set CLASSPATH=%CLASSPATH%;lib\jaxen-1.1-beta-6.jar
set CLASSPATH=%CLASSPATH%;lib\scannotation-1.0.2.jar
set CLASSPATH=%CLASSPATH%;lib\reflections-0.9.5-RC2.one-jar.jar
set CLASSPATH=%CLASSPATH%;jdem846.jar


echo %CLASSPATH%
java -classpath "%CLASSPATH%" us.wthr.jdem846.JDemMain -no-debug