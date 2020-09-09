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

import com.huawei.generator.ast.custom.XClassDoc;
import com.huawei.generator.ast.custom.XFieldDoc;
import com.huawei.generator.ast.custom.XMethodDoc;
import com.huawei.hms.convertor.core.engine.fixbot.util.FixbotParams;
import com.huawei.hms.convertor.core.engine.xms.XmsConstants;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.huawei.hms.convertor.idea.ui.javadoc.ApiDetailToolWindow;
import com.huawei.hms.convertor.idea.ui.javadoc.DocContents;
import com.huawei.hms.convertor.idea.ui.javadoc.DocDisplayInfo;
import com.huawei.hms.convertor.idea.ui.javadoc.JavaDocPanelInfos;
import com.huawei.hms.convertor.idea.ui.javadoc.JavaDocToolWindow;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.FixbotAnalyzeService;
import com.huawei.hms.convertor.openapi.SummaryCacheService;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.inquiry.InquiryEntrances;
import com.huawei.inquiry.docs.Docs;
import com.huawei.inquiry.docs.EntireDoc;
import com.huawei.inquiry.docs.Struct;
import com.huawei.inquiry.docs.XDocs;
import com.huawei.inquiry.docs.ZClassDoc;
import com.huawei.inquiry.docs.ZDocs;
import com.huawei.inquiry.docs.ZFieldDoc;
import com.huawei.inquiry.docs.ZMethodDoc;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.ui.UIUtil;
import lombok.extern.slf4j.Slf4j;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SizeRequirements;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.InlineView;
import javax.swing.text.html.ParagraphView;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * java doc util
 *
 * @since 2020-08-04
 */
@Slf4j
public class JavaDocUtil {
    private static String paramsForIde = "";

    private static String exceptionForIde = "";

    private static String returnForIde = "";

    public static String getHmsAndGmsMethodDispalyInfos(ZMethodDoc zMethodDoc) {
        StringBuilder paramsStringBuilder = new StringBuilder();
        StringBuilder exceptionStringBuilder = new StringBuilder();
        StringBuilder paramsTemp = new StringBuilder();
        StringBuilder exceptionTemp = new StringBuilder();
        StringBuilder zMethodInfos = new StringBuilder();

        if (zMethodDoc.getParamsForIDE() == null) {
            paramsTemp.append("NULL");
        } else {
            for (Map.Entry<String, Struct> entry : zMethodDoc.getParamsForIDE().entrySet()) {
                paramsTemp.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue().getTypeName())
                    .append(" ")
                    .append(entry.getValue().getDescription())
                    .append("<br>");
                if (entry.getValue().canJump()) {
                    paramsForIde = paramsTemp.toString()
                        .replaceAll(entry.getValue().getTypeName(),
                            String.format(Locale.ROOT, UIConstants.JavaDoc.HIGHT_LIGHT_LINK,
                                entry.getValue().getTypeName(), entry.getValue().getTypeName()));
                    paramsTemp.setLength(0);
                    paramsTemp.append(paramsForIde);
                }
            }
        }
        paramsStringBuilder.append(paramsTemp.toString());

        if (zMethodDoc.getExceptionsForIDE() == null) {
            exceptionTemp.append("NULL");
        } else {
            for (Struct struct : zMethodDoc.getExceptionsForIDE()) {
                exceptionTemp.append(struct.getTypeName()).append(": ").append(struct.getDescription()).append("<br>");
                if (struct.canJump()) {
                    exceptionForIde = exceptionTemp.toString()
                        .replaceAll(struct.getTypeName(), String.format(Locale.ROOT,
                            UIConstants.JavaDoc.HIGHT_LIGHT_LINK, struct.getTypeName(), struct.getTypeName()));
                    exceptionTemp.setLength(0);
                    exceptionTemp.append(exceptionForIde);
                }
            }
        }
        exceptionStringBuilder.append(exceptionTemp.toString());

        if (zMethodDoc.getReturnForIDE() == null) {
            returnForIde = "NULL";
        } else {
            returnForIde =
                zMethodDoc.getReturnForIDE().getTypeName() + " " + zMethodDoc.getReturnForIDE().getDescription();
            if (zMethodDoc.getReturnForIDE().canJump()) {
                returnForIde = returnForIde.replaceAll(zMethodDoc.getReturnForIDE().getTypeName(),
                    String.format(Locale.ROOT, UIConstants.JavaDoc.HIGHT_LIGHT_LINK,
                        zMethodDoc.getReturnForIDE().getTypeName(), zMethodDoc.getReturnForIDE().getTypeName()));
            }
        }

        if (zMethodDoc.getMethodDes() != null || !Objects.equals(zMethodDoc.getMethodDes(), "")) {
            zMethodInfos.append(zMethodDoc.getMethodDes() + "<br>");
        }
        String methodInfo = methodInfoDisplay(zMethodInfos, paramsStringBuilder, returnForIde, exceptionStringBuilder);
        return methodInfo;
    }

    public static String getXmsMethodDispalyInfos(XMethodDoc methodDoc) {
        StringBuilder paramsStringBuilder = new StringBuilder();
        StringBuilder exceptionStringBuilder = new StringBuilder();
        StringBuilder paramsTemp = new StringBuilder();
        StringBuilder exceptionTemp = new StringBuilder();
        StringBuilder xmsMethodInfos = new StringBuilder();
        if (methodDoc.getParamsForIDE() == null) {
            paramsStringBuilder.append("NULL");
        } else {
            for (Map.Entry<String, Struct> entry : methodDoc.getParamsForIDE().entrySet()) {
                paramsTemp.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue().getTypeName())
                    .append(" ")
                    .append(entry.getValue().getDescription())
                    .append("<br>");
                if (entry.getValue().canJump()) {
                    paramsForIde = paramsTemp.toString()
                        .replaceAll(entry.getValue().getTypeName(),
                            String.format(Locale.ROOT, UIConstants.JavaDoc.HIGHT_LIGHT_LINK,
                                entry.getValue().getTypeName(), entry.getValue().getTypeName()));
                    paramsTemp.setLength(0);
                    paramsTemp.append(paramsForIde);
                }
            }
        }
        paramsStringBuilder.append(paramsTemp.toString());

        if (methodDoc.getExceptionsForIDE() == null) {
            exceptionStringBuilder.append("NULL");
        } else {
            for (Struct struct : methodDoc.getExceptionsForIDE()) {
                exceptionStringBuilder.append(struct.getTypeName())
                    .append(": ")
                    .append(struct.getDescription())
                    .append("<br>");
                if (struct.canJump()) {
                    exceptionForIde = exceptionTemp.toString()
                        .replaceAll(struct.getTypeName(), String.format(Locale.ROOT,
                            UIConstants.JavaDoc.HIGHT_LIGHT_LINK, struct.getTypeName(), struct.getTypeName()));
                    exceptionTemp.setLength(0);
                    exceptionTemp.append(exceptionForIde);
                }
            }
        }
        exceptionStringBuilder.append(exceptionTemp.toString());

        if (methodDoc.getReturnForIDE() == null) {
            returnForIde = "NULL";
        } else {
            returnForIde =
                methodDoc.getReturnForIDE().getTypeName() + " " + methodDoc.getReturnForIDE().getDescription();
            if (methodDoc.getReturnForIDE().canJump()) {
                returnForIde = returnForIde.replaceAll(methodDoc.getReturnForIDE().getTypeName(),
                    String.format(Locale.ROOT, UIConstants.JavaDoc.HIGHT_LIGHT_LINK,
                        methodDoc.getReturnForIDE().getTypeName(), methodDoc.getReturnForIDE().getTypeName()));
            }
        }
        if (methodDoc.getDescriptions() != null || !Objects.equals(methodDoc.getDescriptions(), "")) {
            xmsMethodInfos.append(methodDoc.getDescriptions() + "<br>");
        }
        String methodInfo =
            methodInfoDisplay(xmsMethodInfos, paramsStringBuilder, returnForIde, exceptionStringBuilder);
        return methodInfo;
    }

    public static String methodInfoDisplay(StringBuilder methodInfos, StringBuilder paramsStringBuilder,
        String returnForIde, StringBuilder exceptionStringBuilder) {
        if (!Objects.equals(paramsStringBuilder.toString(), "")) {
            methodInfos.append("<b>Params: </b><br> " + paramsStringBuilder.toString());
        }
        if (!Objects.equals(returnForIde, " ")) {
            methodInfos.append("<b>Return: </b> " + returnForIde);
        }

        if (!Objects.equals(exceptionStringBuilder.toString(), "")) {
            methodInfos.append("<b>Exceptions: </b> " + exceptionStringBuilder.toString());
        }
        return methodInfos.toString();
    }

    public static void setLabelLink(JLabel jLabel, String text, Docs docs) {
        String url;
        if (docs == null) {
            jLabel.setText("");
            return;
        }
        if (docs instanceof ZMethodDoc) {
            url = ((ZMethodDoc) docs).getMethodUrl();
        } else if (docs instanceof ZFieldDoc) {
            url = ((ZFieldDoc) docs).getUrl();
        } else if (docs instanceof ZClassDoc) {
            url = ((ZClassDoc) docs).getClassUrl();
        } else {
            url = GrsServiceProvider.getGrsAllianceDomain() + HmsConvertorBundle.message("xms_java_doc_url");
        }
        if (Objects.equals(url, "") || url == null) {
            return;
        }
        setLabelLinkStyle(jLabel, text);
        jLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                jLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new java.net.URI(url));
                } catch (URISyntaxException | IOException ex) {
                    log.error("url error: {}", ex.getMessage());
                }
            }
        });
    }

    public static void customRemoveMouseListener(JLabel jLabel) {
        MouseListener[] mouseListeners = jLabel.getMouseListeners();
        for (MouseListener mouseListener : mouseListeners) {
            jLabel.removeMouseListener(mouseListener);
        }
    }

    private static void setLabelLinkStyle(JLabel jLabel, String text) {
        jLabel.setText(UIConstants.Html.HTML_HEAD + text + UIConstants.Html.HTML_END);
        Font font =
            new Font(UIConstants.JavaDoc.MICROSOFT_YAHEI, Font.PLAIN, UIConstants.Util.JavaDoc.LABEL_LINK_STYLE_FONT);
        jLabel.setFont(font);
        jLabel.setForeground(UIConstants.JavaDoc.LINK_COLOR);
    }

    public static void setLabelStyle(JLabel jLabel) {
        Font font = new Font(UIConstants.JavaDoc.HUAWEI_SANS, Font.PLAIN, UIConstants.Util.JavaDoc.LABEL_STYLE_FONT);
        jLabel.setFont(font);
        jLabel.setForeground(UIConstants.JavaDoc.LABEL_COLOR);
    }

    public static void setContentStyle(JTextPane jTextPane) {
        Font font = new Font(UIConstants.JavaDoc.HUAWEI_SANS, Font.PLAIN, UIConstants.Util.JavaDoc.CONTENT_STYLE_FONT);
        jTextPane.setBorder(BorderFactory.createEmptyBorder());
        jTextPane.setFont(font);
        jTextPane.setForeground(UIConstants.JavaDoc.FOREGROUND_COLOR);
        jTextPane.setBackground(UIConstants.JavaDoc.CONTENT_BACKGROUND_COLOR);
    }

    public static void setApiContentStyle(JTextPane jTextField) {
        jTextField.setPreferredSize(new Dimension(UIConstants.JavaDoc.API_CONTENT_WIDTH, UIConstants.JavaDoc.AUTO_SIZE));
        jTextField.setBorder(BorderFactory.createEmptyBorder());
        Font font =
            new Font(UIConstants.JavaDoc.HUAWEI_SANS_BOLD, Font.PLAIN, UIConstants.Util.JavaDoc.API_CONTENT_STYLE_FONT);
        jTextField.setFont(font);
        jTextField.setForeground(UIConstants.JavaDoc.FOREGROUND_COLOR);
    }

    public static void setContentScrollPaneStyle(JScrollPane jScrollPane) {
        jScrollPane.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane.setPreferredSize(new Dimension(UIConstants.Util.JavaDoc.CONTENT_SCROLL_PANE_STYLE_WIDTH,
            UIConstants.Util.JavaDoc.CONTENT_SCROLL_PANE_STYLE_HEIGHT));
        jScrollPane.setForeground(UIConstants.JavaDoc.FOREGROUND_COLOR);
        jScrollPane.setBackground(UIConstants.JavaDoc.JPANEL_BACKGROUND_COLOR);
    }

    public static void setJavaDocScrollPaneStyle(JScrollPane javaDocScroll) {
        javaDocScroll.setForeground(UIConstants.JavaDoc.FOREGROUND_COLOR);
        javaDocScroll.setBackground(UIConstants.JavaDoc.JPANEL_BACKGROUND_COLOR);
    }

    public static void setJpanelStyle(JPanel jpanel) {
        jpanel.setBackground(UIConstants.JavaDoc.JPANEL_BACKGROUND_COLOR);
    }

    public static void showJavaDocWindow(Project project, JavaDocPanelInfos javaDocPanelInfos) {
        ApiDetailToolWindow  apiDetailToolWindow = ToolWindowUtil.getApiDetailToolWindow(project).get();
        removeMethodDocListener(apiDetailToolWindow.getJavaDocToolWindow().getBackToApi());
        apiDetailToolWindow.getJavaDocToolWindow().refreshData(javaDocPanelInfos);
        ToolWindow toolWindow =
            ToolWindowManager.getInstance(project).getToolWindow(UIConstants.JavaDocToolWindow.JAVADOC_TOOL_WINDOW_ID);
        if (toolWindow == null) {
            log.warn("Can not get API Details tool window!");
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("no_tool_window"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }
        ToolWindowUtil.showWindow(toolWindow);
        setJScrollPaneStyle(apiDetailToolWindow);
        CardLayout cardLayout = apiDetailToolWindow.getCardLayout();
        cardLayout.show(apiDetailToolWindow.getContentPanel(), UIConstants.JavaDoc.JAVADOC_TOOLWINDOW);
    }

    public static void addMethodDocListener(JavaDocToolWindow javaDocToolWindow, JTextPane jTextPane,
        JavaDocPanelInfos javaDocPanelInfos,Project project) {
        jTextPane.addHyperlinkListener(e -> {
            EntireDoc entireDoc;
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String previousApi = javaDocPanelInfos.getFullApi();
                String currentApi = e.getDescription();
                addApiListener(javaDocToolWindow, project,previousApi);
                entireDoc = InquiryEntrances
                    .getInstance(System.getProperty(XmsConstants.KEY_XMS_JAR),
                        getDependencyVersionMap(javaDocToolWindow.getProject()))
                    .getDocs(currentApi, EntireDoc.SCOPETYPE.CLASS);
                javaDocToolWindow
                    .refreshData(new JavaDocPanelInfos(currentApi, entireDoc, EntireDoc.SCOPETYPE.CLASS, false));
                javaDocToolWindow.getApi().setText(setApiTextStyle(currentApi));
                javaDocToolWindow.getBackToApi().setTextApi(previousApi,true);
                javaDocToolWindow.getBackToApi().setText(setTextDisplay(previousApi));
                ApiDetailToolWindow  apiDetailToolWindow = ToolWindowUtil.getApiDetailToolWindow(project).get();
                setJScrollPaneStyle(apiDetailToolWindow);
            }
        });
        jTextPane.setEditable(false);
    }

    public static void addApiListener(JavaDocToolWindow javaDocToolWindow,Project project, String previousApi) {
        javaDocToolWindow.getBackToApi().addHyperlinkListener(e -> {
            EntireDoc entireDoc;
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                entireDoc = InquiryEntrances
                    .getInstance(System.getProperty(XmsConstants.KEY_XMS_JAR),
                        getDependencyVersionMap(javaDocToolWindow.getProject()))
                    .getDocs(e.getDescription(), EntireDoc.SCOPETYPE.METHOD);
                javaDocToolWindow.refreshData(
                    new JavaDocPanelInfos(e.getDescription(), entireDoc, EntireDoc.SCOPETYPE.METHOD, false));
                JavaDocUtil.removeMethodDocListener(javaDocToolWindow.getBackToApi());
                javaDocToolWindow.getBackToApi().setTextApi(null,false);
                ApiDetailToolWindow  apiDetailToolWindow = ToolWindowUtil.getApiDetailToolWindow(project).get();
                setJScrollPaneStyle(apiDetailToolWindow);
            }
        });
        javaDocToolWindow.getBackToApi().setEditable(false);
    }

    public static void removeMethodDocListener(JTextPane jTextPane) {
        HyperlinkListener[] hyperlinkListeners = jTextPane.getHyperlinkListeners();
        for (HyperlinkListener hyperlinkListener : hyperlinkListeners) {
            jTextPane.removeHyperlinkListener(hyperlinkListener);
        }
    }

    public static HTMLEditorKit getHTMLEditorKit() {
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit() {
            @Override
            public ViewFactory getViewFactory() {
                return new HTMLFactory() {
                    @Override
                    public View create(Element elem) {
                        View view = super.create(elem);
                        if (view instanceof InlineView) {
                            return JavaDocUtil.getInlineView(elem);
                        } else if (view instanceof ParagraphView) {
                            return JavaDocUtil.getParagraphView(elem);
                        }
                        return view;
                    }
                };
            }
        };
        return htmlEditorKit;
    }

    public static Map<String, String> getDependencyVersionMap(Project project) {
        Map<String, String> dependencyVersionMap =
            SummaryCacheService.getInstance().getDependencyVersion(project.getBasePath());
        if (dependencyVersionMap != null) {
            return dependencyVersionMap;
        }
        String inspectFolder =
            project.getBasePath().substring(project.getBasePath().lastIndexOf(Constant.UNIX_FILE_SEPARATOR_IN_CHAR) + 1);
        String repoID = inspectFolder + "." + project.getBasePath().hashCode();
        FixbotParams fixbotPreAnalysisParams = FixbotAnalyzeService.getInstance()
            .buildFixbotPreAnalysisParams(repoID, project.getBasePath(), ConfigCacheService.getInstance(),
                project.getBasePath());
        Optional<Map<String, String>> dependencyVersionMapInfo =
            FixbotAnalyzeService.getInstance().preAnalysis4DependencyVersion(fixbotPreAnalysisParams);
        dependencyVersionMap = dependencyVersionMapInfo.get();
        return dependencyVersionMap;
    }

    public static void setXmsContent(JavaDocPanelInfos javaDocPanelInfos, JavaDocToolWindow javaDocToolWindow,
        JLabel jLabel, JTextPane jTextPane, JLabel learnMore,Project project) {
        String xmsFieldContent = "";
        Set<XDocs> xDocsSet = javaDocPanelInfos.getEntireDoc().getXDocs();
        EntireDoc.EXCEPTIONTYPE exceptionType = javaDocPanelInfos.getEntireDoc().getExceptiontype();

        if (xDocsSet == null || xDocsSet.size() == 0) {
            xmsFieldContent = UIConstants.JavaDoc.NOT_FIND_LABEL;
            if (exceptionType == EntireDoc.EXCEPTIONTYPE.KITNOTSUPPORT) {
                xmsFieldContent = UIConstants.JavaDoc.KIT_NOT_SUPPORT;
            }
            if (exceptionType == EntireDoc.EXCEPTIONTYPE.HMSNOTMATCHGMS) {
                xmsFieldContent = UIConstants.JavaDoc.XMS_NOT_SUPPORT;
            }
            jLabel.setText(UIConstants.JavaDoc.XMS_FILED);
            jTextPane.setText(JavaDocUtil.setTextDisplay(xmsFieldContent));
            return;
        }
        XDocs xDocs = (XDocs) xDocsSet.toArray()[0];
        if (xDocs.getTypeClass() == XFieldDoc.class) {
            jLabel.setText(UIConstants.JavaDoc.XMS_FILED);
            xmsFieldContent = getXFieldDoc(xDocsSet);
            jTextPane.setText(JavaDocUtil.setTextDisplay(xmsFieldContent));
            setLabelLink(learnMore, UIConstants.JavaDoc.LEARN_MORE, xDocs);
        }
        if (xDocs.getTypeClass() == XMethodDoc.class) {
            jLabel.setText(UIConstants.JavaDoc.XMS_METHOD);
            String xmsMethodContent = "";
            xmsMethodContent = getXMethodInfo(xDocsSet);
            jTextPane.setText(JavaDocUtil.setTextDisplay(xmsMethodContent));
            JavaDocUtil.addMethodDocListener(javaDocToolWindow, jTextPane, javaDocPanelInfos,project);
            setLabelLink(learnMore, UIConstants.JavaDoc.LEARN_MORE, xDocs);
        }
    }

    public static DocContents updataDocContent(JavaDocPanelInfos javaDocPanelInfos, String xmsContent,
        String hmsContent, String gmsContent) {
        String xmsContentTemp = xmsContent;
        String hmsContentTemp = hmsContent;
        String gmsContentTemp = gmsContent;
        EntireDoc.EXCEPTIONTYPE exceptionType = javaDocPanelInfos.getEntireDoc().getExceptiontype();
        if (exceptionType != null) {
            switch (exceptionType) {
                case HMSNOTSUPPORT:
                    hmsContentTemp = UIConstants.JavaDoc.HMS_NOT_SUPPORT;
                    break;
                case GMSNOTSUPPORT:
                    hmsContentTemp = UIConstants.JavaDoc.HMS_NOT_SUPPORT;
                    gmsContentTemp = UIConstants.JavaDoc.GMS_NOT_SUPPORT;
                    break;
                case KITNOTSUPPORT:
                    xmsContentTemp = UIConstants.JavaDoc.KIT_NOT_SUPPORT;
                    hmsContentTemp = UIConstants.JavaDoc.KIT_NOT_SUPPORT;
                    gmsContentTemp = UIConstants.JavaDoc.KIT_NOT_SUPPORT;
                    break;
                case HMSNOTMATCHGMS:
                    xmsContentTemp = UIConstants.JavaDoc.XMS_NOT_SUPPORT;
                    gmsContentTemp = UIConstants.JavaDoc.GMS_NOT_SUPPORT;
                    break;
                default:
                    break;
            }
        }
        DocContents docContents = new DocContents(xmsContentTemp, hmsContentTemp, gmsContentTemp);
        return docContents;
    }

    public static String setTextDisplay(String text) {
        if (Objects.equals(text, "")) {
            text = UIConstants.JavaDoc.DESCRIPTIONS_IS_EMPTY;
        }
        text = UIUtil.isUnderDarcula()
            ? "<html><font face=\"HuaweiSans\" size=\"14px\" color=\"#b1b3b4\">" + text + "</font></html>"
            : "<html><font face=\"HuaweiSans\" size=\"14px\" color=\"#212121\">" + text + "</font></html>";
        return text;
    }


    public static String setApiTextStyle(String text) {
        text = UIUtil.isUnderDarcula()
            ? "<html><font face=\"HuaweiSans\" size=\"14px\" color=\"#FFFFFF\"><b>" + text + "</b></font></html>"
            : "<html><font face=\"HuaweiSans\" size=\"14px\" color=\"#212121\"><b>" + text + "</b></font></html>";
        return text;
    }

    public static String setBackApiFormat() {
        String apiFormat;
        apiFormat = UIUtil.isUnderDarcula()
            ? "<html><font face= \"Microsoft YaHei\" size=\"14px\"><a href= '%s'style=color:#51A2FF;text-decoration:none> &lt; Back to</a><span style=color:#D3D3D3;font-family:Microsoft YaHei;font-size:14px;> ...%s</span></html>"
            : "<html><font face= \"Microsoft YaHei\" size=\"14px\"><a href= '%s'style=color:#51A2FF;text-decoration:none> &lt; Back to</a><span style=color:#212121;font-family:Microsoft YaHei;font-size:14px;> ...%s</span></html>";
        return apiFormat;
    }

    public static String setAllBackApiFormat() {
        String apiFormat;
        apiFormat = UIUtil.isUnderDarcula()
            ? "<html><font face= \"Microsoft YaHei\" size=\"14px\"><a href= '%s'style=color:#51A2FF;text-decoration:none> &lt; Back to</a><span style=color:#D3D3D3;font-family:Microsoft YaHei;font-size:14px;> %s</span></html>"
            : "<html><font face= \"Microsoft YaHei\" size=\"14px\"><a href= '%s'style=color:#51A2FF;text-decoration:none> &lt; Back to</a><span style=color:#212121;font-family:Microsoft YaHei;font-size:14px;> %s</span></html>";
        return apiFormat;
    }

    private static InlineView getInlineView(Element elem) {
        InlineView inlineview = new InlineView(elem) {
            public int getBreakWeight(int axis, float pos, float len) {
                return View.GoodBreakWeight;
            }

            public View breakView(int axis, int positionStart, float pos, float len) {
                if (axis != View.X_AXIS) {
                    return this;
                }
                checkPainter();
                int positionEnd = getGlyphPainter().getBoundedPosition(this, positionStart, pos, len);
                if (positionStart == getStartOffset() && positionEnd == getEndOffset()) {
                    return this;
                }
                return createFragment(positionStart, positionEnd);
            }
        };
        return inlineview;
    }

    private static ParagraphView getParagraphView(Element elem) {
        ParagraphView paragraphview = new ParagraphView(elem) {
            protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements requirements) {
                if (requirements == null) {
                    requirements = new SizeRequirements();
                }
                float pref = layoutPool.getPreferredSpan(axis);
                float min = layoutPool.getMinimumSpan(axis);
                requirements.minimum = (int) min;
                requirements.preferred = Math.max(requirements.minimum, (int) pref);
                requirements.maximum = Integer.MAX_VALUE;
                requirements.alignment = UIConstants.JavaDoc.API_CONTENT_ALIGNMENT_SIZE;
                return requirements;
            }

        };
        return paragraphview;
    }

    public static DocDisplayInfo getMethodInfo(JavaDocPanelInfos javaDocPanelInfos) {
        String xMethodinfo = "";
        XMethodDoc xmsMethodDocLink = null;
        Set<XDocs> xMethodSet = javaDocPanelInfos.getEntireDoc().getXDocs();
        if (xMethodSet != null) {
            xMethodinfo = getXMethodInfo(xMethodSet);
            if (!xMethodSet.isEmpty()) {
                xmsMethodDocLink = (XMethodDoc) xMethodSet.toArray()[0];
            }
        }
        String hMethodinfo = "";
        ZMethodDoc hmsMethodDocLink = null;
        Set<ZDocs> hmsMethodSet = javaDocPanelInfos.getEntireDoc().getHDocs();
        if (hmsMethodSet != null) {
            hMethodinfo = getZMethodInfo(hmsMethodSet);
            if (!hmsMethodSet.isEmpty()) {
                hmsMethodDocLink = (ZMethodDoc) hmsMethodSet.toArray()[0];
            }
        }
        ZMethodDoc gMethodDocLink = null;
        Set<ZDocs> gmsMethodSet = javaDocPanelInfos.getEntireDoc().getGDocs();
        if (gmsMethodSet != null) {
            if (!gmsMethodSet.isEmpty()){
                gMethodDocLink = (ZMethodDoc) gmsMethodSet.toArray()[0];
            }
        }
        String xmsMethodContent = xMethodSet == null ? UIConstants.JavaDoc.NOT_FIND_LABEL : xMethodinfo;
        String hmsMethodContent = hmsMethodSet == null ? UIConstants.JavaDoc.NOT_FIND_LABEL : hMethodinfo;
        String gmsMethodContent = gmsMethodSet == null ? UIConstants.JavaDoc.NOT_FIND_LABEL : HmsConvertorBundle.message("gms_api_details");

        DocContents methodContents =
            updataDocContent(javaDocPanelInfos, xmsMethodContent, hmsMethodContent, gmsMethodContent);
        DocDisplayInfo docDisplayInfo = new DocDisplayInfo(methodContents, xmsMethodDocLink, hmsMethodDocLink, gMethodDocLink);
        return docDisplayInfo;
    }

    public static DocDisplayInfo getClassInfo(JavaDocPanelInfos javaDocPanelInfos) {
        String xClassinfo = "";
        XClassDoc xmsClassDocLink = null;
        Set<XDocs> xDocsSet = javaDocPanelInfos.getEntireDoc().getXDocs();
        if (xDocsSet != null) {
            xClassinfo = xDocClassInfo(xDocsSet);
            if (!xDocsSet.isEmpty()){
                xmsClassDocLink = (XClassDoc) xDocsSet.toArray()[0];
            }
        }

        ZClassDoc hClassDocLink = null;
        String hClassinfo = "";
        Set<ZDocs> hDocsSet = javaDocPanelInfos.getEntireDoc().getHDocs();
        if (hDocsSet != null) {
            hClassinfo = zDocClassInfo(hDocsSet);
            if(!hDocsSet.isEmpty()) {
                hClassDocLink = (ZClassDoc) hDocsSet.toArray()[0];
            }
        }

        ZClassDoc gClassDocLink = null;
        Set<ZDocs> gDocsSet = javaDocPanelInfos.getEntireDoc().getGDocs();
        if (gDocsSet != null) {
            if (!gDocsSet.isEmpty()){
                gClassDocLink = (ZClassDoc) gDocsSet.toArray()[0];
            }
        }

        String xmsClassContent = xDocsSet == null ? UIConstants.JavaDoc.NOT_FIND_LABEL : xClassinfo;
        String hmsClassContent = hDocsSet == null ? UIConstants.JavaDoc.NOT_FIND_LABEL : hClassinfo;
        String gmsClassContent = gDocsSet == null ? UIConstants.JavaDoc.NOT_FIND_LABEL : HmsConvertorBundle.message("gms_api_details");
        DocContents classContents =
            updataDocContent(javaDocPanelInfos, xmsClassContent, hmsClassContent, gmsClassContent);
        DocDisplayInfo docDisplayInfo = new DocDisplayInfo(classContents, xmsClassDocLink, hClassDocLink, gClassDocLink);
        return docDisplayInfo;
    }

    public static DocDisplayInfo getFieldInfo(JavaDocPanelInfos javaDocPanelInfos) {
        String hmsFieldinfo = "";
        StringBuilder xFieldBuilder = new StringBuilder();
        ZFieldDoc hFieldDocLink = null;
        Set<ZDocs> hmsFieldSet = javaDocPanelInfos.getEntireDoc().getHDocs();
        if (hmsFieldSet != null) {
            for (ZDocs hmsField : hmsFieldSet) {
                ZFieldDoc hmsFieldDoc = (ZFieldDoc) hmsField;
                xFieldBuilder.append(hmsFieldDoc.getDes())
                    .append("<Br>");
            }
            hmsFieldinfo = xFieldBuilder.toString();
            if (!hmsFieldSet.isEmpty()){
                hFieldDocLink = (ZFieldDoc) hmsFieldSet.toArray()[0];
            }
        }
        ZFieldDoc gFieldDocLink = null;
        Set<ZDocs> gmsFieldSet = javaDocPanelInfos.getEntireDoc().getGDocs();
        if (gmsFieldSet != null) {
            if (!gmsFieldSet.isEmpty()){
                gFieldDocLink = (ZFieldDoc) gmsFieldSet.toArray()[0];
            }
        }
        String hmsFieldContent = hmsFieldSet == null ? UIConstants.JavaDoc.NOT_FIND_LABEL : hmsFieldinfo;
        String gmsFieldContent = gmsFieldSet == null ? UIConstants.JavaDoc.NOT_FIND_LABEL : HmsConvertorBundle.message("gms_api_details");

        DocContents fieldContents = updataDocContent(javaDocPanelInfos, "", hmsFieldContent, gmsFieldContent);
        DocDisplayInfo docDisplayInfo = new DocDisplayInfo(fieldContents, null, hFieldDocLink, gFieldDocLink);
        return docDisplayInfo;
    }

    public static String getXFieldDoc(Set<XDocs> xFieldSet){
        StringBuilder xFieldBuilder = new StringBuilder();
        String xFieldInfo ="";
        if (xFieldSet.size() > 1) {
            for (int i = 0; i < xFieldSet.size(); i++) {
                XFieldDoc xmsFieldDoc = (XFieldDoc) xFieldSet.toArray()[i];
                xFieldBuilder.append("<b>Field")
                    .append(i)
                    .append(": </b>")
                    .append(xmsFieldDoc.getDescriptions())
                    .append("<br>");
            }
            xFieldInfo = xFieldBuilder.toString();
        }else if (xFieldSet.size() == 1){
            XFieldDoc xmsFieldDoc = (XFieldDoc) xFieldSet.toArray()[0];
            xFieldInfo  = xmsFieldDoc.getDescriptions();
        }

        return xFieldInfo;
    }

    public static String getZFieldDoc(Set<ZDocs> zFieldSet){
        StringBuilder xFieldBuilder = new StringBuilder();
        String zFieldInfo ="";
        if (zFieldSet.size() > 1) {
            for (int i = 0; i < zFieldSet.size(); i++) {
                ZFieldDoc zFieldDoc = (ZFieldDoc) zFieldSet.toArray()[i];
                xFieldBuilder.append("<b>Field")
                    .append(i)
                    .append(": </b>")
                    .append(zFieldDoc.getSignature())
                    .append("<br>")
                    .append(zFieldDoc.getDes())
                    .append("<br>");
            }
            zFieldInfo = xFieldBuilder.toString();
        }else if (zFieldSet.size() == 1){
            ZFieldDoc zmsFieldDoc = (ZFieldDoc) zFieldSet.toArray()[0];
            zFieldInfo  = zmsFieldDoc.getSignature() + UIConstants.Html.BR + zmsFieldDoc.getDes();
        }

        return zFieldInfo;
    }

    public static String getXMethodInfo(Set<XDocs> xMethodSet) {
        StringBuilder xMethodBuilder = new StringBuilder();
        String xMethodInfo = "";
        if (xMethodSet.size() > 1) {
            for (int i = 0; i < xMethodSet.size(); i++) {
                XMethodDoc xMethodDoc = (XMethodDoc) xMethodSet.toArray()[i];
                xMethodBuilder.append("<b>Method")
                    .append(i)
                    .append(": </b>")
                    .append(xMethodDoc.getSignature())
                    .append("<Br>")
                    .append(getXmsMethodDispalyInfos(xMethodDoc))
                    .append("<Br>");
            }
            xMethodInfo = xMethodBuilder.toString();
        } else if (xMethodSet.size() == 1) {
            XMethodDoc xMethodDoc = (XMethodDoc) xMethodSet.toArray()[0];
            xMethodInfo = getXmsMethodDispalyInfos(xMethodDoc);
        }
        return xMethodInfo;
    }

    public static String getZMethodInfo(Set<ZDocs> zMethodSet) {
        StringBuilder hMethodBuilder = new StringBuilder();
        String hMethodInfo = "";
        if (zMethodSet.size() > 1) {
            for (int i = 0; i < zMethodSet.size(); i++) {
                ZMethodDoc hMethodDoc = (ZMethodDoc) zMethodSet.toArray()[i];
                hMethodBuilder.append("<b>Method")
                    .append(i)
                    .append(": </b>")
                    .append(hMethodDoc.getSignature())
                    .append("<Br>")
                    .append(getHmsAndGmsMethodDispalyInfos(hMethodDoc))
                    .append("<Br>");
            }
            hMethodInfo = hMethodBuilder.toString();
        } else if (zMethodSet.size() == 1) {
            ZMethodDoc hMethodDoc = (ZMethodDoc) zMethodSet.toArray()[0];
            hMethodInfo = getHmsAndGmsMethodDispalyInfos(hMethodDoc);
        }
        return hMethodInfo;
    }

    private static String xDocClassInfo(Set<XDocs> xDocsSet) {
        StringBuilder xClassBuilder = new StringBuilder();
        String xClassinfo = "";
        if (xDocsSet.size() > 1) {
            for (int i = 0; i < xDocsSet.size(); i++) {
                XClassDoc xmsClassDoc = (XClassDoc) xDocsSet.toArray()[i];
                xClassBuilder.append("<b>Class")
                    .append(i)
                    .append(": </b>")
                    .append(xmsClassDoc)
                    .append("<Br>")
                    .append(xmsClassDoc.getXClassInfo())
                    .append("<Br>");
            }
            xClassinfo = xClassBuilder.toString();
        } else if (xDocsSet.size() == 1) {
            XClassDoc xmsClassDoc = (XClassDoc) xDocsSet.toArray()[0];
            xClassinfo = xmsClassDoc.getXClassInfo();
        }
        return xClassinfo;
    }

    private static String zDocClassInfo(Set<ZDocs> hDocsSet) {
        StringBuilder hClassBuilder = new StringBuilder();
        String hClassinfo = "";
        if (hDocsSet.size() > 1) {
            for (int i = 0; i < hDocsSet.size(); i++) {
                ZClassDoc hmsClassDoc = (ZClassDoc) hDocsSet.toArray()[i];
                hClassBuilder.append("<b>Class")
                    .append(i)
                    .append(": </b>")
                    .append(hmsClassDoc)
                    .append("<Br>")
                    .append(hmsClassDoc.getClassDes())
                    .append("<Br>");
            }
            hClassinfo = hClassBuilder.toString();
        } else if (hDocsSet.size() == 1) {
            ZClassDoc hmsClassDoc = (ZClassDoc) hDocsSet.toArray()[0];
            hClassinfo = hmsClassDoc.getClassDes();
        }
        return hClassinfo;
    }

    public static void setJScrollPaneStyle(ApiDetailToolWindow apiDetailToolWindow){
        JScrollPane rootScrollPane = apiDetailToolWindow.getRootScrollPane();
        rootScrollPane.doLayout();
        JScrollBar horizontalScrollBar = rootScrollPane.getHorizontalScrollBar();
        horizontalScrollBar.setValue(horizontalScrollBar.getMinimum());
    }
}