#!/usr/bin/env sh

# Резервная копия оригинального файла gradlew с исправленными концами строк (LF)
DIRNAME=$(dirname "$0")
APP_BASE_NAME=$(basename "$0")
APP_HOME=$(cd "$DIRNAME" && pwd)

# Поиск Java
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

if [ ! -x "$JAVACMD" ] ; then
    die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
fi

# Сбор параметров
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Запуск Gradle
exec "$JAVACMD" "-Dorg.gradle.appname=$APP_BASE_NAME" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
