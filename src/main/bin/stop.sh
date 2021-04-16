#!/bin/sh
env_path=$(dirname $0)/../../env.sh
if [ -f $env_path ]; then
    source $env_path
fi

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

base=`dirname $0`/..

appName=${instance_id}

pid=""

if [ "$pid" == "" ] ; then
    pid=`get_pid "appName=${appName}"`
fi

if [ "$pid" == "" ]; then
    echo "${appName} is not running."
    exit -1;
fi

echo -e "`hostname`: stopping ${appName} $pid ... "
kill -15 $pid

LOOPS=0
while (true);
do
    gpid=`get_pid "appName=${appName}" "$pid"`
    if [ "$gpid" == "" ] ; then
        echo "Oook! cost:$LOOPS"
        if [ -f "$pidfile" ]; then
                `rm $pidfile`
        fi
        break;
    fi
    let LOOPS=LOOPS+1
    sleep 1
done
