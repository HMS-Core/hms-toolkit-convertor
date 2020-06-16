# ***********************************************************************
# Copyright: (c) Huawei Technologies Co., Ltd. 2020. All rights reserved.
# ***********************************************************************

#需要是unix格式
import re
import string
import sys

projectrestr = re.compile(r'[+\\\\]--- (project :\S+)')
def isProject(info):
    ret = projectrestr.search(info)
    if ret != None:
        return True, ret.group(1)
    return False, info[5:]

def addPackage(dependences, projectName, pkg):
    pkgLst = dependences.get(projectName)
    if None == pkgLst:
        dependences[projectName] = [pkg]
    else:
        pkgLst.append(pkg)

def doParse(fileinfo):
    with open(fileinfo, 'r') as f:
        contents = f.readlines()
        lineno = 0
        topDependences = {} #{project:[]}
        projectName = 'ROOT'
        projectLevel = 0
        projectStack = [(projectName, projectLevel)] #[(projectName, projectLevel)]
        for line in contents:
            lineno += 1
            info = string.strip(line)
            if len(info) == 0:
                continue
            pos = string.find(info, '+---')
            if pos == -1:
                pos = string.find(info, '\\---')
                if pos == -1:
                    print 'warn: mismatch line, %s[lineno:%d]' % (info, lineno)
                    continue
            level = pos / 5  #5 is unit
            isProjectFlag, value = isProject(info[pos:])
            if isProjectFlag:
                newProject = value
                projectName = newProject
                projectLevel = level + 1
                projectStack.append((projectName, projectLevel))
                print 'Info: new project:%s level:%d' % (projectName, projectLevel)
            else:
                package = value
                if package[0] == ':': #local binary, skip it
                    print 'Info: local binary:%s skip it.' % (package)
                    continue
                
                if projectLevel == level: # top dependency
                    print 'Info: find current project:%s package:%s' % (projectName, package)
                    addPackage(topDependences, projectName, package)
                elif level < projectLevel: #upper level
                    while len(projectStack) > 0:
                        pname, plevel = projectStack[-1]
                        if level < plevel:
                            projectStack.pop()
                            continue
                        else:
                            #trace back to parent-parent project
                            projectName = pname
                            projectLevel = plevel
                            break
                    if len(projectStack) > 0:
                        print 'Info: find parent-parent project:%s package:%s' % (pname, package)
                        addPackage(topDependences, pname, package)
                    else:
                        print 'Warn: unexpect path. info:%s lineno:%d, can not find parent project.' % (info, lineno)
                        continue
                else: #level > projectLevel  other sub package, skip it
                    continue
        return topDependences


def filtPackages(fileinfo):
    ret = doParse(fileinfo)
    result = []
    for project, lst in ret.items():
        print 'Project:%s\n  %s' % (project, '\n  '.join(lst))
        for pkg in lst:
            #do not trip the package info
            #pos = string.find(pkg, ' ')
            #if pos != -1:
            #    pkg = pkg[0:pos]
            pkg = '%s\n' % pkg
            if pkg not in result:
                result.append(pkg)
    try:
        output = '%s.filter.result' % fileinfo
        fobj = open(output, 'w')
        fobj.writelines(result)
        fobj.close()
        print 'Info: all filt packages info output into %s succeed!' % output
    except:
        print 'Err: open file:%s failed!' % output
    #print 'all pkg need do openscan:\n  %s' % '\n  '.join(result)
    return result

if __name__ == '__main__':
    filtPackages(sys.argv[1])
