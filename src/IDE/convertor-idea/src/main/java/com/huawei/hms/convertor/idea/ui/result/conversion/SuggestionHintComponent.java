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

package com.huawei.hms.convertor.idea.ui.result.conversion;

import static com.huawei.hms.convertor.idea.util.UiUtil.wrapComment;

import com.huawei.hms.convertor.core.result.conversion.ConversionPointDesc;
import com.huawei.hms.convertor.idea.ui.result.HmsConvertorToolWindow;
import com.huawei.hms.convertor.idea.util.HmsConvertorUtil;
import com.huawei.hms.convertor.idea.util.IconUtil;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.codeInsight.hint.PriorityQuestionAction;
import com.intellij.codeInsight.hint.QuestionAction;
import com.intellij.codeInsight.hint.ScrollAwareHint;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.actions.EditorActionUtil;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.ListPopupStep;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.HintHint;
import com.intellij.ui.LightweightHint;
import com.intellij.ui.PopupMenuListenerAdapter;
import com.intellij.ui.RowIcon;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Alarm;
import com.intellij.util.ui.UIUtil;

import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * Intention hint component
 *
 * @since 2019-07-01
 */
@Slf4j
public class SuggestionHintComponent implements Disposable, ScrollAwareHint {
    private static final int PRIORITY_VALUE = -10;

    private static final int PREFERRED_BORDER_SIZE = 6;

    private static final int MINIMUM_BORDER_SIZE = 4;

    private static final int TIME_DELAY = 500;

    private static final int REALPOINT_INDEX = 4;

    private static final int YSHIFT_INDEX = 3;

    private static final int WRAP_LENGTH = 120;

    private static final Border INACTIVE_BORDER_PREFERRED = BorderFactory.createEmptyBorder(PREFERRED_BORDER_SIZE,
        PREFERRED_BORDER_SIZE, PREFERRED_BORDER_SIZE, PREFERRED_BORDER_SIZE);

    private static final Border INACTIVE_BORDER_MINIMUM = BorderFactory.createEmptyBorder(MINIMUM_BORDER_SIZE,
        MINIMUM_BORDER_SIZE, MINIMUM_BORDER_SIZE, MINIMUM_BORDER_SIZE);

    private static final String AUTO_CONVERT_NAME = "Apply HMS Convertor Auto-Convert";

    private static final String AUTO_REVERT_NAME = "Apply HMS Convertor Auto-Revert";

    private static final Alarm TIME_ALARM = new Alarm();

    private final Editor contentEditor;

    private final RowIcon showDownIcon;

    private final JLabel inactiveLabel;

    private final SuggestionComponentHint suggestionComponentHint;

    private final JPanel rootPanel = new JPanel();

    private volatile boolean hintPopupShown;

    private volatile ListPopup suggestionPopupList;

    private DefectItem showDefectItem;

    private PopupMenuListener popupMenuListener;

    private SuggestionHintComponent(@NotNull Project project, @NotNull final Editor editor,
        @NotNull DefectItem defectItem) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        contentEditor = editor;
        showDefectItem = defectItem;
        rootPanel.setLayout(new BorderLayout());
        rootPanel.setOpaque(false);

        Icon smartTagIcon = IconUtil.QUICK_FIX_BULB;

        showDownIcon = new RowIcon(smartTagIcon, IconUtil.DOWN);

        inactiveLabel = new JLabel(showDownIcon);
        inactiveLabel.setOpaque(false);

        rootPanel.add(inactiveLabel, BorderLayout.CENTER);

        rootPanel.setBorder(editor.isOneLineMode() ? INACTIVE_BORDER_MINIMUM : INACTIVE_BORDER_PREFERRED);

        inactiveLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(@NotNull MouseEvent e) {
                onMouseExit(editor.isOneLineMode());
            }

            @Override
            public void mouseEntered(@NotNull MouseEvent e) {
                onMouseEnter(editor.isOneLineMode());
            }
        });

        suggestionComponentHint = new SuggestionComponentHint(rootPanel);
        ListPopupStep<ConversionPointDesc> step = getConversionListPopupStep(project, showDefectItem);
        recreateMyPopup(step);
        EditorUtil.disposeWithEditor(contentEditor, this);
    }

    public static SuggestionHintComponent showSuggestionHint(@NotNull Project project, @NotNull Editor editor,
        boolean showExpanded, DefectItem defectItem) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        final SuggestionHintComponent component = new SuggestionHintComponent(project, editor, defectItem);

        component.showIntentionHintImpl(!showExpanded);
        if (!showExpanded) {
            return component;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            if (!editor.isDisposed() && editor.getComponent().isShowing()) {
                log.info("show popup menu");
                component.showPopup(false);
            }
        }, project.getDisposed());
        return component;
    }

    @Override
    public void dispose() {
        ApplicationManager.getApplication().assertIsDispatchThread();
        suggestionComponentHint.hide();
        rootPanel.setVisible(false);

        showDefectItem = null;
        if (popupMenuListener != null) {
            final Container ancestor =
                SwingUtilities.getAncestorOfClass(JComboBox.class, contentEditor.getContentComponent());
            if (ancestor != null && ancestor instanceof JComboBox) {
                ((JComboBox) ancestor).removePopupMenuListener(popupMenuListener);
            }

            popupMenuListener = null;
        }
    }

    @Override
    public void editorScrolled() {
        closePopup();
    }

    private static Border createActiveBorder() {
        return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(getBorderColor(), 1),
            BorderFactory.createEmptyBorder(PREFERRED_BORDER_SIZE - 1, PREFERRED_BORDER_SIZE - 1,
                PREFERRED_BORDER_SIZE - 1, PREFERRED_BORDER_SIZE - 1));
    }

    private static Border createActiveBorderSmall() {
        return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(getBorderColor(), 1),
            BorderFactory.createEmptyBorder(MINIMUM_BORDER_SIZE - 1, MINIMUM_BORDER_SIZE - 1, MINIMUM_BORDER_SIZE - 1,
                MINIMUM_BORDER_SIZE - 1));
    }

    private static Color getBorderColor() {
        return EditorColorsManager.getInstance().getGlobalScheme().getColor(EditorColors.SELECTED_TEARLINE_COLOR);
    }

    private static boolean canPlaceBulbOnTheSameLine(Editor editor) {
        if (ApplicationManager.getApplication().isUnitTestMode() || editor.isOneLineMode()) {
            return false;
        }
        if (Registry.is("always.show.intention.above.current.line", false)) {
            return false;
        }
        final int offset = editor.getCaretModel().getOffset();
        final VisualPosition pos = editor.offsetToVisualPosition(offset);
        int line = pos.line;

        final int firstNonSpaceColumnOnTheLine = EditorActionUtil.findFirstNonSpaceColumnOnTheLine(editor, line);
        if (firstNonSpaceColumnOnTheLine == -1) {
            return false;
        }
        final Point point = editor.visualPositionToXY(new VisualPosition(line, firstNonSpaceColumnOnTheLine));
        return point.x > IconUtil.REAL_INTENTION_BULB.getIconWidth()
            + (editor.isOneLineMode() ? MINIMUM_BORDER_SIZE : PREFERRED_BORDER_SIZE) * 2;
    }

    private void recreate() {
        ApplicationManager.getApplication().assertIsDispatchThread();
        ListPopupStep step = suggestionPopupList.getListStep();
        recreateMyPopup(step);
    }

    private void showIntentionHintImpl(final boolean delay) {
        final int offset = contentEditor.getCaretModel().getOffset();
        suggestionComponentHint.setDelayFlag(delay);
        HintManagerImpl hintManager = HintManagerImpl.getInstanceImpl();
        QuestionAction action = new IntentionHintQuestionAction();

        if (hintManager.canShowQuestionAction(action)) {
            showHint(hintManager, contentEditor, offset, action);
        }
    }

    private void showHint(HintManagerImpl hintManager, Editor editor, int caretOffset, QuestionAction action) {
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            hintManager.showQuestionHint(contentEditor, new Point(), caretOffset, caretOffset, suggestionComponentHint,
                action, HintManager.ABOVE);
            return;
        }
        final int offset = editor.getCaretModel().getOffset();
        final VisualPosition pos = editor.offsetToVisualPosition(offset);
        int line = pos.line;

        final Point position = editor.visualPositionToXY(new VisualPosition(line, 0));
        if (!editor.getComponent().isDisplayable()) {
            log.warn("Editor is not displayable!");
        }

        final boolean oneLineEditor = editor.isOneLineMode();
        JComponent convertComponent = editor.getContentComponent();
        Point realPoint;
        if (oneLineEditor) {
            final JComponent contentComponent = editor.getContentComponent();
            Container ancestorOfClass = SwingUtilities.getAncestorOfClass(JComboBox.class, contentComponent);
            if (ancestorOfClass != null && ancestorOfClass instanceof JComponent) {
                convertComponent = (JComponent) ancestorOfClass;
            } else {
                ancestorOfClass = SwingUtilities.getAncestorOfClass(JTextField.class, contentComponent);
                if (ancestorOfClass != null && ancestorOfClass instanceof JComponent) {
                    convertComponent = (JComponent) ancestorOfClass;
                }
            }

            realPoint = new Point(-(IconUtil.REAL_INTENTION_BULB.getIconWidth() / 2) - REALPOINT_INDEX,
                -(IconUtil.REAL_INTENTION_BULB.getIconHeight() / 2));
        } else {
            Rectangle visibleArea = editor.getScrollingModel().getVisibleArea();
            if (position.y < visibleArea.y || position.y >= visibleArea.y + visibleArea.height) {
                return;
            }

            // try to place bulb on the same line
            int yShift = -(PREFERRED_BORDER_SIZE + IconUtil.REAL_INTENTION_BULB.getIconHeight());
            if (canPlaceBulbOnTheSameLine(editor)) {
                yShift = -(PREFERRED_BORDER_SIZE
                    + (IconUtil.REAL_INTENTION_BULB.getIconHeight() - editor.getLineHeight()) / 2 + YSHIFT_INDEX);
            } else if (position.y < visibleArea.y + editor.getLineHeight()) {
                yShift = editor.getLineHeight() - PREFERRED_BORDER_SIZE;
            } else {
                log.debug("do whatever");
            }

            final int xShift = IconUtil.REAL_INTENTION_BULB.getIconWidth();

            realPoint = new Point(Math.max(0, visibleArea.x - xShift), position.y + yShift);
        }

        Point location = SwingUtilities.convertPoint(convertComponent, realPoint,
            editor.getComponent().getRootPane().getLayeredPane());
        hintManager.showQuestionHint(contentEditor, new Point(location.x, location.y), caretOffset, caretOffset,
            suggestionComponentHint, action, HintManager.ABOVE);
    }

    private ListPopupStep<ConversionPointDesc> getConversionListPopupStep(@NotNull Project project,
        @NotNull DefectItem defectItem) {
        List<ConversionPointDesc> descriptions = defectItem.getDescriptions();
        List<ConversionPointDesc> conversionDescriptions = new ArrayList<>();

        final ConversionPointDesc defectDescription = new ConversionPointDesc();
        if (defectItem.isConverted()) {
            defectDescription.setText(AUTO_REVERT_NAME);
        } else {
            defectDescription.setText(AUTO_CONVERT_NAME);
        }

        conversionDescriptions.add(defectDescription);

        if (!defectItem.isConverted() && descriptions.size() > 0) {
            conversionDescriptions.addAll(descriptions);
        }
        return new IntentionHintListPopupStep<>(project, defectItem, conversionDescriptions);
    }

    private void hide() {
        log.info("hide popup menu");
        Disposer.dispose(this);
    }

    private void onMouseExit(final boolean small) {
        final JComponent content = suggestionPopupList.getContent();
        Window ancestor = null;
        if (content != null) {
            ancestor = SwingUtilities.getWindowAncestor(content);
        }

        if (ancestor == null) {
            inactiveLabel.setIcon(showDownIcon);
            rootPanel.setBorder(small ? INACTIVE_BORDER_MINIMUM : INACTIVE_BORDER_PREFERRED);
        }
    }

    private void onMouseEnter(final boolean small) {
        inactiveLabel.setIcon(showDownIcon);
        rootPanel.setBorder(small ? createActiveBorderSmall() : createActiveBorder());

        String acceleratorsText = KeymapUtil.getFirstKeyboardShortcutText(
            ActionManager.getInstance().getAction(IdeActions.ACTION_SHOW_INTENTION_ACTIONS));
        if (!acceleratorsText.isEmpty()) {
            inactiveLabel.setToolTipText(CodeInsightBundle.message("lightbulb.tooltip", acceleratorsText));
        }
    }

    private void closePopup() {
        ApplicationManager.getApplication().assertIsDispatchThread();
        suggestionPopupList.cancel();
        hintPopupShown = false;
    }

    private void showPopup(boolean mouseClick) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        if (suggestionPopupList == null || suggestionPopupList.isDisposed() || hintPopupShown) {
            recreate();
        }

        if (mouseClick && rootPanel.isShowing() && null != suggestionPopupList) {
            final RelativePoint swCorner = RelativePoint.getSouthWestOf(rootPanel);
            final int yOffset = canPlaceBulbOnTheSameLine(contentEditor) ? 0 : contentEditor.getLineHeight()
                - (contentEditor.isOneLineMode() ? MINIMUM_BORDER_SIZE : PREFERRED_BORDER_SIZE);
            suggestionPopupList.show(new RelativePoint(swCorner.getComponent(),
                new Point(swCorner.getPoint().x, swCorner.getPoint().y + yOffset)));
        } else if (null != suggestionPopupList) {
            suggestionPopupList.showInBestPositionFor(contentEditor);
        } else {
            return;
        }

        hintPopupShown = true;
    }

    private void getPopupMenuListenerAdapter() {
        popupMenuListener = new PopupMenuListenerAdapter() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                hide();
            }
        };
    }

    private void recreateMyPopup(@NotNull ListPopupStep step) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        if (suggestionPopupList != null) {
            Disposer.dispose(suggestionPopupList);
        }
        suggestionPopupList = JBPopupFactory.getInstance().createListPopup(step);

        suggestionPopupList.addListener(new JBPopupListener() {
            @Override
            public void onClosed(@NotNull LightweightWindowEvent event) {
                hintPopupShown = false;
            }

            @Override
            public void beforeShown(LightweightWindowEvent event) {
            }
        });

        if (contentEditor.isOneLineMode()) {
            // hide popup on combobox popup show
            final Container ancestor =
                SwingUtilities.getAncestorOfClass(JComboBox.class, contentEditor.getContentComponent());
            if (ancestor != null && ancestor instanceof JComboBox) {
                final JComboBox comboBox = (JComboBox) ancestor;
                getPopupMenuListenerAdapter();

                comboBox.addPopupMenuListener(popupMenuListener);
            }
        }

        Disposer.register(this, suggestionPopupList);
        Disposer.register(suggestionPopupList, ApplicationManager.getApplication()::assertIsDispatchThread);
    }

    private static class SuggestionComponentHint extends LightweightHint {
        private boolean visibleFlag;

        private boolean delayFlag;

        private SuggestionComponentHint(JComponent component) {
            super(component);
        }

        @Override
        public void show(@NotNull final JComponent parentComponent, final int x, final int y,
            final JComponent focusBackComponent, @NotNull HintHint hintHint) {
            visibleFlag = true;
            if (delayFlag) {
                TIME_ALARM.cancelAllRequests();
                TIME_ALARM.addRequest(() -> showImpl(parentComponent, x, y, focusBackComponent), TIME_DELAY);
            } else {
                showImpl(parentComponent, x, y, focusBackComponent);
            }
        }

        private void showImpl(JComponent parentComponent, int x, int y, JComponent focusBackComponent) {
            if (!parentComponent.isShowing()) {
                return;
            }
            super.show(parentComponent, x, y, focusBackComponent, new HintHint(parentComponent, new Point(x, y)));
        }

        @Override
        public void hide() {
            super.hide();
            visibleFlag = false;
            TIME_ALARM.cancelAllRequests();
        }

        @Override
        public boolean isVisible() {
            return visibleFlag || super.isVisible();
        }

        private void setDelayFlag(boolean delayFlag) {
            this.delayFlag = delayFlag;
        }
    }

    private static class IntentionHintListPopupStep<T extends ConversionPointDesc> extends BaseListPopupStep<T> {
        private Project project;

        private DefectItem defectItem;

        public IntentionHintListPopupStep(@NotNull Project project, @NotNull DefectItem defectItem,
            @NotNull List<T> defectDescriptions) {
            super(null, defectDescriptions);
            this.project = project;
            this.defectItem = defectItem;
        }

        @Nullable
        @Override
        public PopupStep onChosen(T selectedValue, boolean finalChoice) {
            final String descText = selectedValue.getText();
            final Optional<HmsConvertorToolWindow> toolWindow = HmsConvertorUtil.getHmsConvertorToolWindow(project);
            if (AUTO_CONVERT_NAME.equals(descText) && toolWindow.isPresent()) {
                toolWindow.get().getSourceConvertorToolWindow().asyncConvertDefectItem(defectItem);
            } else if (AUTO_REVERT_NAME.equals(descText) && toolWindow.isPresent()) {
                toolWindow.get().getSourceConvertorToolWindow().asyncRevertDefectItem(defectItem);
            } else {
                final String descUrl = selectedValue.getUrl();
                if (!StringUtil.isEmptyOrSpaces(descUrl)) {
                    UIUtil.invokeLaterIfNeeded(() -> {
                        BrowserUtil.browse(descUrl);
                    });
                }
            }

            return super.onChosen(selectedValue, finalChoice);
        }

        @Override
        public String getTextFor(ConversionPointDesc value) {
            final String descText = wrapComment(value.getText(), WRAP_LENGTH);
            return StringUtil.isEmptyOrSpaces(descText) ? "" : descText.trim();
        }

        @Override
        public Icon getIconFor(ConversionPointDesc value) {
            final String descText = value.getText();
            if (AUTO_CONVERT_NAME.equals(descText) || AUTO_REVERT_NAME.equals(descText)) {
                return IconUtil.REAL_INTENTION_BULB;
            }

            final String descUrl = value.getUrl();
            return StringUtil.isEmptyOrSpaces(descUrl) ? IconUtil.EMPTY : IconUtil.EXPLORER;
        }
    }

    private static class IntentionHintQuestionAction implements PriorityQuestionAction {
        @Override
        public int getPriority() {
            return PRIORITY_VALUE;
        }

        @Override
        public boolean execute() {
            return true;
        }
    }
}
