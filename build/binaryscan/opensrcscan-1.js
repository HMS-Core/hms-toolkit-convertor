/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 */

'use strict';


var fs = require('fs');

var file = process.argv[2];
var fileOut = process.argv[3];

var filelistIn = fs.readFileSync(file).toString();
var inLines = filelistIn.split(/\r\n|\r|\n/);
var outLines = [];

var cfgfileLines = fs.readFileSync('build/binaryscan/opensrcscan.ini').toString().split(/\r\n|\r|\n/);
var targetLines = [];
for (var i = 0; i < cfgfileLines.length; i++) {
    if (cfgfileLines[i].indexOf("Channel=") == 0) {
        console.log(cfgfileLines[i]);
        var sub = cfgfileLines[i].replace('Channel=', '');
        var tmp = sub.split(' ');
        var s;
        for (var y = 0; y < tmp.length; y++) {
            s = tmp[y].trim();
            if (s != '') {
                targetLines.push(s + 'ReleaseRuntimeClasspath -');
            }
        }
        break;
    }
}
targetLines.push('runtime -');
targetLines.push('_releaseCompile -');
targetLines.push('releaseRuntimeClasspath -');

for (var i = 0; i < targetLines.length; i++) {
    console.log(targetLines[i]);
}

function hitTarget(inputline) {
    for (var i = 0; i < targetLines.length; i++) {
        if (inputline.indexOf(targetLines[i]) == 0) {
            return true;
        }
    }
    return false;
}

//~ start : The next line of that line start with 'releaseCompileClasspath'
//~ end   : The Previous line of that first empty line after the start of the line
var found = false;
for (var i = 0; i < inLines.length; i++) {
    if (hitTarget(inLines[i])) {
        console.log(inLines[i]);
        found = true;
        continue;
    }
    if (found && inLines[i] == '') {
        found = false;
        continue;
    }
    if (found) {
        console.log(inLines[i]);
        if (inLines[i].trim() != '') {
            outLines.push(inLines[i]);
        }
    }
}

fs.writeFileSync(fileOut, outLines.join('\r\n'));
