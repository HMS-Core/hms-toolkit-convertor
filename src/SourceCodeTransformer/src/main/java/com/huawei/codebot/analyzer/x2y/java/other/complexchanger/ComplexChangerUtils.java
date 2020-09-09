package com.huawei.codebot.analyzer.x2y.java.other.complexchanger;

import com.huawei.codebot.framework.FixStatus;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.model.DefectInstance;

public class ComplexChangerUtils {
    /**
     *
     * @param filePath         path of file
     * @param buggyLineNumber  the number of line which need to be change
     * @param buggyLineContent the content of line which need to be change
     * @param fixedLineContent the content of line which will be changed
     * @param fixerInfo        the message of this change
     * @return DefectInstance the result of conversion
     */
    public static DefectInstance createDefectInstance(String filePath, int buggyLineNumber, String buggyLineContent,
            String fixedLineContent, FixerInfo fixerInfo) {
        DefectInstance defectInstance = new DefectInstance();
        defectInstance.buggyLines.put(filePath, buggyLineNumber, buggyLineContent);
        defectInstance.defectType = fixerInfo.type.toString();
        defectInstance.message = fixerInfo.description;
        defectInstance.mainBuggyLineNumber = Math.abs(buggyLineNumber);
        defectInstance.mainBuggyFilePath = filePath;
        defectInstance.mainFixedFilePath = filePath;
        defectInstance.mainFixedLineNumber = Math.abs(buggyLineNumber);
        defectInstance.fixedLines.put(filePath, buggyLineNumber, fixedLineContent);
        defectInstance.isFixed = true;
        defectInstance.status = FixStatus.AUTOFIX.toString();
        defectInstance.context.add("Complex", "complex");
        return defectInstance;
    }
}
