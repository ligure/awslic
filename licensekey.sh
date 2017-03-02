#!/bin/sh

# get into run script dir (resolve to absolute path)
SCRIPT_DIR=$(cd $(dirname $0) && pwd)    # This dir is where this script live.
echo "SCRIPT_DIR:$SCRIPT_DIR"
cd $SCRIPT_DIR

#export LANG=zh_CN.GB18030

#设置AWS使用的JDK
#---------------------
export JAVA_HOME='../jdk1.6/'

#JVM选项
export JAVA_OPTS=''

${JAVA_HOME}/bin/java $JAVA_OPTS -jar ./bootstrap.jar -r -lib "./patch;./lib;./jdbc;./plugs" com.actionsoft.application.server.ServerInfoTools

