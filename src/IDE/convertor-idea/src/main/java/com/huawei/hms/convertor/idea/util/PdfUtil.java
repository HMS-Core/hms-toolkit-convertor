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

import com.huawei.hms.convertor.idea.ui.result.export.AnalyseResultInternalLinkDest;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;

import java.io.IOException;

public class PdfUtil {
    private PdfUtil() {
    }

    /**
     * save and close document
     *
     * @param document document
     * @param outputFilePath ouput file path
     * @throws IOException IO exception
     */
    public static void saveAndCloseDocument(PDDocument document, String outputFilePath) throws IOException {
        document.save(outputFilePath);
        document.close();
    }

    /**
     * create page
     *
     * @param document document
     * @param mediaBoxWidth media box width
     * @param mediaBoxHeight media box height
     * @return page
     */
    public static PDPage createPage(PDDocument document, float mediaBoxWidth, float mediaBoxHeight) {
        PDPage page = new PDPage();
        page.setMediaBox(new PDRectangle(mediaBoxWidth, mediaBoxHeight));
        document.addPage(page);
        return page;
    }

    /**
     * create content stream
     *
     * @param document document
     * @param page page
     * @param font font
     * @param fontSize font size
     * @return content stream
     * @throws IOException IO exception
     */
    public static PDPageContentStream createContentStream(PDDocument document, PDPage page, PDFont font, float fontSize)
        throws IOException {
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.setFont(font, fontSize);
        return contentStream;
    }

    /**
     * close content stream
     *
     * @param contentStream content stream
     * @throws IOException IO exception
     */
    public static void closeContentStream(PDPageContentStream contentStream) throws IOException {
        if (contentStream != null) {
            contentStream.close();
        }
    }

    /**
     * draw text
     *
     * @param contentStream content stream
     * @param textStartX text startX
     * @param textStartY text startY
     * @param text text
     * @throws IOException IO exception
     */
    public static void drawText(PDPageContentStream contentStream, float textStartX, float textStartY, String text)
        throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(textStartX, textStartY);
        contentStream.showText(text);
        contentStream.endText();
    }

    /**
     * draw table
     *
     * @param contentStream content stream
     * @param contentMargin content margin
     * @param rowHeight row height
     * @param colWidth col width
     * @param tableStartXy table startXy
     * @param table table
     * @return currentXy, 0th is X, 1st is Y
     * @throws IOException IO exception
     */
    public static float[] drawTable(PDPageContentStream contentStream, float contentMargin, float rowHeight,
        float colWidth, float[] tableStartXy, String[][] table) throws IOException {
        int rowCount = table.length;
        int colCount = table[0].length;
        float tableWidth = colWidth * colCount;
        float tableHeight = rowHeight * rowCount;
        float tableStartX = tableStartXy[0];
        float tableStartY = tableStartXy[1];

        float rowStartY = tableStartY;
        for (int i = 0; i <= rowCount; i++) {
            contentStream.moveTo(tableStartX, rowStartY);
            contentStream.lineTo(tableStartX + tableWidth, rowStartY);
            contentStream.stroke();
            rowStartY -= rowHeight;
        }

        float colStartX = tableStartX;
        for (int i = 0; i <= colCount; i++) {
            contentStream.moveTo(colStartX, tableStartY);
            contentStream.lineTo(colStartX, tableStartY - tableHeight);
            contentStream.stroke();
            colStartX += colWidth;
        }

        float cellStartX = tableStartX + contentMargin;
        float cellStartY = tableStartY - contentMargin;
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                String cell = table[i][j];
                drawText(contentStream, cellStartX, cellStartY, cell);
                cellStartX += colWidth;
            }
            cellStartX = tableStartX + contentMargin;
            cellStartY -= rowHeight;
        }

        return new float[] {colStartX - colWidth, rowStartY};
    }

    /**
     * draw hyperlink
     *
     * @param page page
     * @param contentMargin content margin
     * @param rowHeight row height
     * @param colWidth col width
     * @param cellWidth cell width
     * @param cellHeight cell height
     * @param linkTableStartXy hyperlink table startXy
     * @param linkUriTable hyperlink uri table
     * @throws IOException IO exception
     */
    public static void drawHyperlink(PDPage page, float contentMargin, float rowHeight, float colWidth, float cellWidth,
        float cellHeight, float[] linkTableStartXy, String[][] linkUriTable) throws IOException {
        int rowCount = linkUriTable.length;
        int colCount = linkUriTable[0].length;
        float tableStartX = linkTableStartXy[0];
        float tableStartY = linkTableStartXy[1];
        float cellStartX = tableStartX + contentMargin;
        float cellStartY = tableStartY - contentMargin;
        for (int i = 0; i < rowCount; i++) {
            if (linkUriTable[i] != null) {
                for (int j = 0; j < colCount; j++) {
                    String uri = linkUriTable[i][j];
                    if (uri != null) {
                        drawHyperlink(page, cellStartX, cellStartY + cellHeight, cellStartX + cellWidth, cellStartY,
                            uri);
                    }
                    cellStartX += colWidth;
                }
            }
            cellStartX = tableStartX + contentMargin;
            cellStartY -= rowHeight;
        }
    }

    /**
     * draw internal link
     *
     * @param document document
     * @param pageIndex page index
     * @param contentMargin content margin
     * @param rowHeight row height
     * @param colWidth col width
     * @param cellWidth cell width
     * @param cellHeight cell height
     * @param linkTableStartXy internal link table startXy
     * @param linkDestTable internal link dest table
     * @throws IOException IO exception
     */
    public static void drawInternalLink(PDDocument document, int pageIndex, float contentMargin, float rowHeight,
        float colWidth, float cellWidth, float cellHeight, float[] linkTableStartXy,
        AnalyseResultInternalLinkDest[][] linkDestTable) throws IOException {
        PDPage page = document.getPage(pageIndex);
        drawInternalLink(document, page, contentMargin, rowHeight, colWidth, cellWidth, cellHeight, linkTableStartXy,
            linkDestTable);
    }

    /**
     * draw internal link
     *
     * @param document document
     * @param page page
     * @param contentMargin content margin
     * @param rowHeight row height
     * @param colWidth col width
     * @param cellWidth cell width
     * @param cellHeight cell height
     * @param linkTableStartXy internal link table startXy
     * @param linkDestTable internal link dest table
     * @throws IOException IO exception
     */
    public static void drawInternalLink(PDDocument document, PDPage page, float contentMargin, float rowHeight,
        float colWidth, float cellWidth, float cellHeight, float[] linkTableStartXy,
        AnalyseResultInternalLinkDest[][] linkDestTable) throws IOException {
        int rowCount = linkDestTable.length;
        int colCount = linkDestTable[0].length;
        float tableStartX = linkTableStartXy[0];
        float tableStartY = linkTableStartXy[1];
        float cellStartX = tableStartX + contentMargin;
        float cellStartY = tableStartY - contentMargin;
        for (int i = 0; i < rowCount; i++) {
            if (linkDestTable[i] != null) {
                for (int j = 0; j < colCount; j++) {
                    AnalyseResultInternalLinkDest linkDest = linkDestTable[i][j];
                    if (linkDest != null) {
                        drawInternalLink(document, page, cellStartX, cellStartY + cellHeight, cellStartX + cellWidth,
                            cellStartY, linkDest);
                    }
                    cellStartX += colWidth;
                }
            }
            cellStartX = tableStartX + contentMargin;
            cellStartY -= rowHeight;
        }
    }

    private static void drawHyperlink(PDPage page, float lowerLeftX, float lowerLeftY, float upperRightX,
        float upperRightY, String uri) throws IOException {
        PDAnnotationLink link = new PDAnnotationLink();
        PDActionURI actionUri = new PDActionURI();
        actionUri.setURI(uri);
        link.setAction(actionUri);

        PDRectangle rectangle = new PDRectangle();
        rectangle.setLowerLeftX(lowerLeftX);
        rectangle.setLowerLeftY(lowerLeftY);
        rectangle.setUpperRightX(upperRightX);
        rectangle.setUpperRightY(upperRightY);
        link.setRectangle(rectangle);

        PDBorderStyleDictionary borderStyle = new PDBorderStyleDictionary();
        borderStyle.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
        link.setBorderStyle(borderStyle);

        page.getAnnotations().add(link);
    }

    private static void drawInternalLink(PDDocument document, PDPage page, float rectangleLowerLeftX,
        float rectangleLowerLeftY, float rectangleUpperRightX, float rectangleUpperRightY,
        AnalyseResultInternalLinkDest linkDest) throws IOException {
        PDPageXYZDestination destination = new PDPageXYZDestination();
        destination.setPage(document.getPage(linkDest.getDestPageIndex()));
        destination.setTop(linkDest.getDestY());
        // reserve current zoom
        destination.setZoom(0);

        PDActionGoTo action = new PDActionGoTo();
        action.setDestination(destination);
        PDAnnotationLink link = new PDAnnotationLink();
        link.setAction(action);
        link.setPage(page);

        PDRectangle rectangle = new PDRectangle();
        rectangle.setLowerLeftX(rectangleLowerLeftX);
        rectangle.setLowerLeftY(rectangleLowerLeftY);
        rectangle.setUpperRightX(rectangleUpperRightX);
        rectangle.setUpperRightY(rectangleUpperRightY);
        link.setRectangle(rectangle);

        PDBorderStyleDictionary borderStyle = new PDBorderStyleDictionary();
        borderStyle.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
        link.setBorderStyle(borderStyle);

        page.getAnnotations().add(link);
    }
}
