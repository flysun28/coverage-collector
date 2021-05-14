#!/bin/bash

# 加载平台下发的环境变量
source $(dirname $0)/../../env.sh
source $(dirname $0)/agent.sh
SKYWALKING_AGENT=-javaagent:/home/service/tools/apache-skywalking-apm-bin/agent/skywalking-agent.jar
TRACEAGENT1=-javaagent:/home/service/tools/traceagent.jar
case "`uname`" in
    Linux)
                bin_abs_path=$(readlink -f $(dirname $0))
                ;;
        *)
                bin_abs_path=`cd $(dirname $0); pwd`
                ;;
esac

# 服务的工作目录
base=${bin_abs_path}/..
# appName 是实例id，由平台注入 主要是用于判断实例是否已经在运行中
appName=${instance_id}

get_pid() {
	STR=$1
	JAVA_PID=`ps -C java -f --width 1000|grep "$STR"|grep -v grep|awk '{print $2}'`
    echo $JAVA_PID;
}
# 判断应用是否已经启动，是的话退出脚本不再执行启动
pid=`get_pid "appName=${appName}"`

if [ ! "$pid" = "" ]; then
        echo "${appName} is already running. exit"
        exit -1;
fi

# java.net.preferIPv4Stack  如果系统中开启了IPV6协议，网络编程经常会获取到IPv6的地址，有了这个配置优先拿IPv4的地址
# -DappName=${appName} 不要漏了  主要是用于判断实例是否已经在运行中
# JAVA_OPTS="${JAVA_OPTS} -server -Djava.io.tmpdir=$base/tmp -DappName=${appName} -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8"

# jvm 相关参数
MEM_OPTS="-Xms${Xms:-8g} -Xmx${Xmx:-8g} -Xss512k -XX:NewRatio=2"

# 发生内存溢出的时候把内存快照dump下来
OOM_OPTS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$base/../${appName}.hprof "

# -Xloggc 建议加上，把gc日志落盘可以分析  具体使用哪些gc收集器暂时不做推荐，业务方可按自身需求来
GC_OPTS="-XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75 -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:./logs/gc.log"

# 启动程序 把标准输出和标准错误都输出到 logs/server.log 文件
# -classpath 'conf:lib/*:.'  这里将conf和lib及. 都加入了classpath  如果使用了配置中心，一般是将配置拉到 $base下的conf文件夹
# TODO  -classpath 参数啥时候需要的说明    和java 打包相关

cd $base

if [ ! -d "tmp" ]; then
  mkdir tmp
fi

if [ ! -d "logs" ]; then
  mkdir logs
fi

java $TRACEAGENT1 $JACOCO_OPTS $JAVA_OPTS $MEM_OPTS $OOM_OPTS $GC_OPTS -classpath 'conf:lib/*:.' -jar lib/jacococoverage-0.0.1-SNAPSHOT.jar 1>>logs/server.log 2>&1 &

# 把进程号写入 server.pid文件里面  此文件主要是云平台在使用
echo $! > $base/bin/server.pid

# 回显启动的进程号
echo OK!`cat $base/bin/server.pid`