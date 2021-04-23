#!/bin/sh

env_path=$(dirname $0)/../../env.sh
if [ -f $env_path ]; then
    source $env_path
fi

case "`uname`" in
    Linux)
        bin_abs_path=$(readlink -f $(dirname $0))
        ;;
    *)
        bin_abs_path=`cd $(dirname $0); pwd`
        ;;
esac
base=${bin_abs_path}/..

#这里的实例id会在env.sh中注入
appName=${instance_id}

get_pid() {
        STR=$1
        PID=$2
        if [ ! -z "$PID" ]; then
                JAVA_PID=`ps -C java -f --width 1000|grep "$STR"|grep "$PID"|grep -v grep|awk '{print $2}'`
            else
                JAVA_PID=`ps -C java -f --width 1000|grep "$STR"|grep -v grep|awk '{print $2}'`
        fi
    echo $JAVA_PID;
}

pid=`get_pid "appName=${appName}"`
if [ ! "$pid" = "" ]; then
    echo "${appName} is already running. exit"
    exit -1;
fi

if [ "$1" = "debug" ]; then
    JAVA_DEBUG_OPT="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=5005,server=y,suspend=n"
fi

cd $base
if [ ! -d "logs" ]; then
  mkdir logs
fi
if [ ! -d "tmp" ]; then
    mkdir tmp
fi


## set java path
if [ -z "$JAVA" ] ; then
  JAVA=$(which java)
fi
str=`file -L $JAVA | grep 64-bit`
if [ -n "$str" ]; then
	JAVA_OPTS="-server -Xms256M -Xmx1024M -XX:NewSize=64M -XX:MaxNewSize=256M -XX:MetaspaceSize=64M -XX:MaxMetaspaceSize=512M -XX:PermSize=56M -XX:MaxPermSize=56M -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSInitiatingOccupancyOnly -XX:+PrintTenuringDistribution -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:logs/gc.log"
else
	JAVA_OPTS="-server -Xms1024m -Xmx1024m -XX:NewSize=256m -XX:MaxNewSize=256m -XX:MaxPermSize=128m "
fi

JAVA_OPTS=" -Djava.io.tmpdir=$base/tmp -DappName=${appName} -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 $JAVA_OPTS"
if [ -n "$HEY_JVM_OPTIONS" ]; then
    java $REMOTE_JAVA_DEBUG_OPTS -Djava.io.tmpdir=$base/tmp -DappName=${appName} -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 $HEY_JVM_OPTIONS $JAVA_DEBUG_OPT -classpath 'conf:lib/*:.'  com.oppo.test.coverage.backend.CoverageBackendApplication 1>>logs/app.log 2>&1 &
    else java  $JAVA_OPTS $JAVA_DEBUG_OPT -classpath 'conf:lib/*:.'  com.oppo.test.coverage.backend.CoverageBackendApplication 1>>logs/app.log 2>&1 &
fi

echo $! > $base/server.pid

echo OK!`cat $base/server.pid`

