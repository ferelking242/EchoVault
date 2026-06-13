#!/bin/sh
  # Gradle start up script for UN*X

  APP_NAME="Gradle"
  APP_BASE_NAME=`basename "$0"`

  APP_HOME=`pwd -P`

  MAX_FD="maximum"

  warn() {
      echo "$*"
  }

  die() {
      echo
      echo "$*"
      echo
      exit 1
  }

  if [ "$APP_HOME" = "" ] ; then
      APP_HOME="$(cd "$(dirname "$0")" && pwd)"
  fi

  CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

  case `uname` in
      Darwin* )
          darwin=true
          ;;
      CYGWIN* )
          cygwin=true
          ;;
      MSYS* | MINGW* )
          msys=true
          ;;
      NONSTOP* )
          nonstop=true
          ;;
  esac

  GRADLE_OPTS="$GRADLE_OPTS \"-Xdock:name=$APP_NAME\" \"-Xdock:icon=$APP_HOME/media/gradle.icns\""

  exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
  