#!/bin/sh
agentTmpDir="agentjar/tmp"
agentDir="agentjar"
agentjar="framework-trace-agent*.jar"

dir=$(cd `dirname $0`; cd ../ ;pwd)

cd $dir

echo "查找lib/下面$agentjar"

num=`find lib -name "$agentjar"|wc -l`

if [ $num == 0 ]; then
   echo "未发现$agentjar，可能是fatjar"
   path=`find lib -name '*.jar'`
   echo $path;
   mkdir -p $agentDir;
   `unzip  -o "$path" -d "$agentTmpDir" >/dev/null `
   traceAgent=`find $agentTmpDir -name "$agentjar"|head -n1`

   if [ ! $traceAgent ];then
      echo "未发现$agentjar"
   else
      echo "发现traceAgentPath:$traceAgent 移动到 $agentDir "
      `mv -f $traceAgent $agentDir/.`
   fi;
else
   traceAgent=`find lib -name "$agentjar" |head -n1`
   echo "发现traceAgentPath:$traceAgent"
fi;

if [ $traceAgent ];then
   traceAgent="-javaagent:$traceAgent"
fi;

`rm -rf $agentTmpDir`