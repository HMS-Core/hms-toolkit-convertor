#!/bin/bash
# ***********************************************************************
# Copyright: (c) Huawei Technologies Co., Ltd. 2019. All rights reserved.
# script for build
# version: 1.0.0
# change log:
# ***********************************************************************
set -ex
set -o pipefail

if [[ ${JDK_PATH} != "" ]]; then
	export JAVA_HOME=${JDK_PATH}
	export PATH=${JDK_PATH}/bin:$PATH
fi

#!/bin/bash
source /etc/profile
export SHELL=/bin/bash

basepath=$(cd `dirname $0`; pwd)
cd $basepath/../src
chmod a+x gradlew
./gradlew clean
./gradlew build -x test
./gradlew -p IDE/intellij-plugin buildPlugin