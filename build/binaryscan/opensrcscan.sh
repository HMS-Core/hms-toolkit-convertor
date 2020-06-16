#!/bin/bash
# ***********************************************************************
# Copyright: (c) Huawei Technologies Co., Ltd. 2020. All rights reserved.
# script for opensrcscan
# version: 1.0.0
# change log:
# ***********************************************************************
source /etc/profile
export SHELL=/bin/bash


echo "begin opensrcscan, current path="`pwd`
node_bin=node

# Usage:
#  arg-1 Module Name
function appendDependencies() {
    local module=$1
    #做三个事情 1、打印过程中所有堆栈异常信息 2、强制更新依赖关系 3、打印所有依赖
    cd src
    bash gradlew --stacktrace :$module:dependencies > ../opensrcscan/$module.txt
    cd ../
    $node_bin build/binaryscan/opensrcscan-1.js opensrcscan/$module.txt opensrcscan/$module-dependencies.txt
    echo >> opensrcscan/dependencies-all.txt
    cat opensrcscan/$module-dependencies.txt >> opensrcscan/dependencies-all.txt
}

# do opensrcscan
rm -fr opensrcscan
mkdir opensrcscan

modules=(`awk -F '=' '$0~/Module=/ {split($2, arr, " ");for(i in arr) print arr[i]}' build/binaryscan/opensrcscan.ini`)
if [ -X$modules = -X"" ];then
    echo "modules empty, please check build/binaryscan/opensrcscan.ini!!!"
    exit 1
fi

for var in ${modules[@]};
do
    echo module is $var
    appendDependencies $var
done

for build in `find ./src -name build -type d`; do
    rm -fr $build
done

FilterTopSwitch=`awk -F '=' '$0~/FilterTopSwitch=/ {split($2, arr, " ");print arr[1]}' build/binaryscan/opensrcscan.ini`
FilterFileSuffix=
echo FilterTopSwitch=$FilterTopSwitch
#filter direct dependency
if [ $FilterTopSwitch -eq 1 ];then
    python build/binaryscan/filterTopDependency.py opensrcscan/dependencies-all.txt
    FilterFileSuffix=.filter.result
	echo "++++++++++++++++++++++++++++++++++++++++++++++++"
	cat opensrcscan/dependencies-all.txt.filter.result
	echo "++++++++++++++++++++++++++++++++++++++++++++++++"
fi

#~ Proccess 'dependencies-all.txt or filter result'
$node_bin build/binaryscan/opensrcscan-2.js opensrcscan/dependencies-all.txt$FilterFileSuffix opensrcscan/dependencies-all.sh

#~ Download all dependencies
bash opensrcscan/dependencies-all.sh opensrcscan
echo "end opensrcscan"
