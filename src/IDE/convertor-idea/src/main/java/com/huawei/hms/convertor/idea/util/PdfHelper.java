/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.convertor.idea.util;

import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.huawei.hms.convertor.idea.ui.result.export.AnalyseResultHyperlinkTable;
import com.huawei.hms.convertor.idea.ui.result.export.AnalyseResultLine;
import com.huawei.hms.convertor.idea.ui.result.export.AnalyseResultTable;

import lombok.extern.slf4j.Slf4j;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class PdfHelper {
    private final PDDocument document;

    private final float mediaBoxWidth;

    private final float mediaBoxHeight;

    private final float mediaBoxMargin;

    private final PDFont font;

    private final float fontSize;

    private final float contentMargin;

    private final float rowHeight;

    private PDPage page;

    private int pageIndex;

    private PDPageContentStream contentStream;

    private float currentX;

    private float currentY;

    public PdfHelper(float mediaBoxWidth, float mediaBoxHeight, float mediaBoxMargin, PDFont font, float fontSize,
        float contentMargin, float rowHeight) {
        document = new PDDocument();
        // used to record current pageIndex, and initialize to -1 because first pageIndex is 0
        pageIndex = -1;
        this.mediaBoxWidth = mediaBoxWidth;
        this.mediaBoxHeight = mediaBoxHeight;
        this.mediaBoxMargin = mediaBoxMargin;
        this.font = font;
        this.fontSize = fontSize;
        this.contentMargin = contentMargin;
        this.rowHeight = rowHeight;
    }

    /**
     * save and close document
     *
     * @param outputFilePath ouput file path
     * @throws IOException IO exception
     */
    public void saveAndClose(String outputFilePath) throws IOException {
        PdfUtil.closeContentStream(contentStream);
        PdfUtil.saveAndCloseDocument(document, outputFilePath);
        log.info("save and close success, outputFilePath: {}.", outputFilePath);
    }

    public PDDocument getDocument() {
        return document;
    }

    /**
     * create page
     *
     * @throws IOException IO exception
     */
    public void createPage() throws IOException {
        PdfUtil.closeContentStream(contentStream);

        page = PdfUtil.createPage(document, mediaBoxWidth, mediaBoxHeight);
        pageIndex++;
        contentStream = PdfUtil.createContentStream(document, page, font, fontSize);

        currentX = page.getMediaBox().getLowerLeftX() + mediaBoxMargin;
        currentY = page.getMediaBox().getUpperRightY() - mediaBoxMargin;
        log.info("create page success, pageIndex: {}.", pageIndex);
    }

    public int getPageIndex() {
        return pageIndex;
    }

    /**
     * draw line
     *
     * @param line line
     * @throws IOException IO exception
     */
    public void drawLine(AnalyseResultLine line) throws IOException {
        if (isReachBottom()) {
            createPage();
        }

        AnalyseResultHyperlinkTable hyperlinkTable = line.getHyperlinkTable();
        if (hyperlinkTable != null) {
            float[] hyperlinkTableStartXy = new float[2];
            hyperlinkTableStartXy[0] =
                hyperlinkTable.getTableStartX() == null ? currentX : hyperlinkTable.getTableStartX();
            hyperlinkTableStartXy[1] = currentY + UIConstants.CommonAnalyseResult.CONTENT_MARGIN;
            drawHyperlinkTable(hyperlinkTableStartXy, hyperlinkTable);
        }

        float lineStartX = currentX;
        float lineStartY = currentY;
        PdfUtil.drawText(contentStream, lineStartX, lineStartY, line.getLine());
        lineStartY -= contentMargin;
        currentY = lineStartY;
    }

    /**
     * <pre>
     * draw tables horizon
     * each table must have same row count
     * </pre>
     *
     * @param tables tables
     * @throws IOException IO exception
     */
    public void drawTablesHorizon(List<AnalyseResultTable> tables) throws IOException {
        int rowCount = tables.get(0).getTables().length;
        float tableStartX = currentX;
        float tableStartY = currentY;

        float rowStartY = tableStartY;
        int subTableFromIdx = 0;
        int subTableToIdx = 0;
        float[] subTableStartXy = new float[2];
        subTableStartXy[0] = tableStartX;
        subTableStartXy[1] = tableStartY;
        for (int i = 0; i < rowCount; i++) {
            if (isReachBottom(rowStartY)) {
                subTableToIdx = i;
                // draw a page
                if (subTableFromIdx < subTableToIdx) {
                    drawTablesHorizonInPage(subTableStartXy, tables, subTableFromIdx, subTableToIdx);
                }

                createPage();
                subTableStartXy[0] = currentX;
                subTableStartXy[1] = currentY;
                rowStartY = subTableStartXy[1];
                // user double pointers for pagination
                subTableFromIdx = subTableToIdx;
            }

            rowStartY -= rowHeight;
        }

        // draw last page
        if (subTableToIdx < rowCount) {
            subTableFromIdx = subTableToIdx;
            subTableToIdx = rowCount;

            currentY = drawTablesHorizonInPage(subTableStartXy, tables, subTableFromIdx, subTableToIdx);
        }
    }

    /**
     * remove margin row after header table
     */
    public void removeMarginRowAfterHeaderTable() {
        currentY += UIConstants.CommonAnalyseResult.ROW_HEIGHT;
    }

    public float[] getCurrentXy() {
        return new float[] {currentX, currentY};
    }

    public float getCurrentY() {
        return currentY;
    }

    private float drawTablesHorizonInPage(float[] subTableStartXy, List<AnalyseResultTable> tables, int subTableFromIdx,
        int subTableToIdx) throws IOException {
        String[][] subTable;
        float[] subTableCurrentXy = new float[2];
        subTableCurrentXy[0] = subTableStartXy[0];
        subTableCurrentXy[1] = subTableStartXy[1];
        for (AnalyseResultTable table : tables) {
            subTable = Arrays.copyOfRange(table.getTables(), subTableFromIdx, subTableToIdx);

            AnalyseResultHyperlinkTable hyperlinkTable = table.getHyperlinkTable();
            if (hyperlinkTable != null) {
                drawHyperlinkTable(subTableStartXy, hyperlinkTable, subTableFromIdx, subTableToIdx);
            }

            subTableCurrentXy = PdfUtil.drawTable(contentStream, contentMargin, rowHeight, table.getColWidth(),
                subTableStartXy, subTable);
            // sub table startY not change
            subTableStartXy[0] = subTableCurrentXy[0];
        }
        return subTableCurrentXy[1];
    }

    private void drawHyperlinkTable(float[] hyperlinkTableStartXy, AnalyseResultHyperlinkTable hyperlinkTable)
        throws IOException {
        PdfUtil.drawHyperlink(page, contentMargin, rowHeight, hyperlinkTable.getColWidth(),
            hyperlinkTable.getCellWidth(), hyperlinkTable.getCellHeight(), hyperlinkTableStartXy,
            hyperlinkTable.getTables());
    }

    private void drawHyperlinkTable(float[] subTableStartXy, AnalyseResultHyperlinkTable hyperlinkTable,
        int subTableFromIdx, int subTableToIdx) throws IOException {
        String[][] subHyperlinkTable = Arrays.copyOfRange(hyperlinkTable.getTables(), subTableFromIdx, subTableToIdx);
        float[] subHyperlinkTableCurrentXy = new float[2];
        subHyperlinkTableCurrentXy[0] = subTableStartXy[0];
        subHyperlinkTableCurrentXy[1] = subTableStartXy[1];

        PdfUtil.drawHyperlink(page, contentMargin, rowHeight, hyperlinkTable.getColWidth(),
            hyperlinkTable.getCellWidth(), hyperlinkTable.getCellHeight(), subHyperlinkTableCurrentXy,
            subHyperlinkTable);
    }

    private boolean isReachBottom() {
        return isReachBottom(currentY);
    }

    private boolean isReachBottom(float rowStartY) {
        return rowStartY <= mediaBoxMargin;
    }
}
