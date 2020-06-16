/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 */
'use strict';


var fs = require('fs');

var file = process.argv[2];
var fileOut = process.argv[3];
//~ var file = 'dependencies-all.txt';
//~ var fileOut = 'dependencies-all.sh';

var filelistIn = fs.readFileSync(file).toString();
var inLines = filelistIn.split(/\r\n|\r|\n/);
var outLines = [];
var lines = [];

//~
for (var i = 0; i < inLines.length; i++) {
    var line = inLines[i].replace(/^[+\\ \|]*--- /, '')
    if (line == '') {
        continue;
    }

    // 工程依赖
    if (line.indexOf ('project :') == 0) {
        //~ console.log ('--', line);
        continue;
    }

    // 重复输入依赖
    if (lines.indexOf (line) != -1) {
        //~ console.log ('--', line);
        continue;
    }

    // 记录处理过的输入
    //~ console.log (line);
    lines.push (line);

    // 分解单行
    var gav = line.split(':');
    //~ console.log (gav[0].replace(/\./g, '/'), gav[1], gav[2]);
	
	if(gav.length == 2 && gav[1].indexOf(' -> ')) {
		var secgav = gav[1].split(' -> ');
		gav[1] = secgav[0];
		gav.push(secgav[1]);
	}
	//console.log (gav[0], gav[1], gav[2])
    // 本地依赖、被覆盖的依赖
	
    if (gav.length < 3 || gav[0] == '' || gav[2].indexOf(' (*)') != -1) {
        //~ console.log ('--', line);
        continue;
    }

    // 被升级的依赖，修正版本号
    if (gav[2].indexOf(' -> ') != -1) {
        gav[2] = gav[2].replace(/.* -> /, '');
    }

    //~ console.log (gav[0].replace(/\./g, '/'), gav[1], gav[2]);
    //~ continue;

    var outLine =
    '$mvnget ' + gav[0] + ' ' + gav[1] + ' ' + gav[2] + ' $destDir';

    // 重复输出
    if (outLines.indexOf (outLine) != -1) {
        //~ console.log ('--', outLine);
        continue;
    }

    console.log (outLine);
    outLines.push (outLine);
}

fs.writeFileSync(fileOut, [
    '#!/bin/bash',
    '',
    'destDir=$1',
    '',
    'classdir=`dirname $0`/../build/binaryscan',
    '',
    'mvnget="java -classpath $classdir gradle_download"',
    '',
    '',
    ].join('\n'));
fs.appendFileSync(fileOut, outLines.join('\n'));
fs.appendFileSync(fileOut, '\n');
