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

package com.huawei.hms.convertor.idea.ui.analysis;

import com.huawei.hms.convertor.core.bi.enumration.AnalyseExportEnum;
import com.huawei.hms.convertor.core.engine.fixbot.model.api.FixbotApiInfo;
import com.huawei.hms.convertor.core.engine.fixbot.model.kit.KitStatisticsResult;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.huawei.hms.convertor.idea.ui.result.summary.KitItem;
import com.huawei.hms.convertor.idea.ui.result.summary.KitTableModel;
import com.huawei.hms.convertor.idea.ui.result.summary.MethodTableModel;
import com.huawei.hms.convertor.idea.util.AnalyseResultExportUtil;
import com.huawei.hms.convertor.idea.util.SummaryResultUtil;
import com.huawei.hms.convertor.openapi.BIReportService;
import com.huawei.hms.convertor.util.Constant;

import com.intellij.ide.actions.ShowFilePathAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ImageLoader;

import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Summary dialog
 *
 * @since 2019/11/28
 */
@Slf4j
public class SummaryDialog extends DialogWrapper {
    private static final int MOUSE_SINGLE_CLICK = 1;

    private JPanel rootPanel;

    private JTextField totalTextField;

    private JTextField methodTextField;

    private JPanel kitTablePanel;

    private JPanel methodTablePanel;

    private JSplitPane splitePane;

    private JTextField supportTextField;

    private JLabel gmsApisLabel;

    private JLabel totalMetLabel;

    private JLabel supportLabel;

    private Project project;

    private TableView<KitItem> kitTable;

    private KitTableModel kitTableModel;

    private TableView<FixbotApiInfo> methodTable;

    private MethodTableModel methodTableModel;

    private List<KitItem> kitItems = new ArrayList<>();

    private TreeMap<String, List<FixbotApiInfo>> kit2FixbotMethodsMap;

    public SummaryDialog(@NotNull Project project) {
        super(project);
        this.project = project;
        kit2FixbotMethodsMap = new TreeMap<>();

        init();
    }

    @Override
    public void init() {
        super.init();
        setTitle(Constant.PLUGIN_NAME);
        getWindow().setIconImage(ImageLoader.loadFromResource("/icons/convertor.png"));

        totalTextField.setText("");
        methodTextField.setText("");
        supportTextField.setText("");
        gmsApisLabel.setText(HmsConvertorBundle.message("gms_kits"));
        totalMetLabel.setText(HmsConvertorBundle.message("total_methods"));
        supportLabel.setText(HmsConvertorBundle.message("total_support"));
        kitTablePanel.add(createKitTable());
        kitTablePanel.setPreferredSize(new Dimension(UIConstants.Dialog.SUMMARY_DIALOG_TABLE_WIDTH, UIConstants.Dialog.SUMMARY_DIALOG_TABLE_HEIGHT));
        methodTablePanel.add(createMethodTable());
        splitePane.setDividerLocation(splitePane.getPreferredSize().width / 2);
    }

    public JPanel createKitTable() {
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        kitTableModel = new KitTableModel();
        kitTable = new TableView<>(kitTableModel);
        kitTable.setRowHeight(UIConstants.Dialog.ROW_HEIGHT);
        kitTable.setDragEnabled(false);
        kitTable.setEnabled(true);
        kitTable.setAutoscrolls(true);
        kitTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        kitTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        kitTable.getTableHeader().setVisible(true);
        kitTable.getTableHeader().setReorderingAllowed(false);
        kitTable.setDefaultRenderer(Object.class, new KitTableCellRenderer());

        kitTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == MOUSE_SINGLE_CLICK)) {
                    int selectedRow = kitTable.getSelectedRow();
                    if (selectedRow < 0) {
                        return;
                    }

                    int kitTableModelSelectedIndex = kitTable.convertRowIndexToModel(selectedRow);
                    if (kitTableModelSelectedIndex < 0) {
                        return;
                    }
                    String kitName = kitItems.get(kitTableModelSelectedIndex).getKitName();
                    methodTableModel.setItems(kit2FixbotMethodsMap.get(kitName));
                }
            }
        });

        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(kitTable);
        toolbarDecorator.disableRemoveAction();
        toolbarDecorator.disableUpAction();
        toolbarDecorator.disableAddAction();
        toolbarDecorator.disableDownAction();

        tablePanel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER);
        return tablePanel;
    }

    public JPanel createMethodTable() {
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        methodTableModel = new MethodTableModel();
        methodTable = new TableView<>(methodTableModel);
        methodTable.setRowHeight(UIConstants.Dialog.ROW_HEIGHT);
        methodTable.setDragEnabled(false);
        methodTable.getTableHeader().setVisible(true);
        methodTable.getTableHeader().setReorderingAllowed(false);
        methodTable.setDefaultRenderer(Object.class, new MethodTableCellRenderer());
        methodTable.setEnabled(true);
        methodTable.setAutoscrolls(true);
        methodTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        methodTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(methodTable);
        toolbarDecorator.disableAddAction();
        toolbarDecorator.disableRemoveAction();
        toolbarDecorator.disableUpAction();
        toolbarDecorator.disableAddAction();
        toolbarDecorator.disableDownAction();

        tablePanel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER);
        return tablePanel;
    }

    public void refreshData(TreeMap<String, List<FixbotApiInfo>> kit2Methods,
        List<KitStatisticsResult> kitStatisticsResults) {
        kit2FixbotMethodsMap = kit2Methods;
        int kitIndex = 0;
        int methodCount = 0;
        int supportCount = 0;
        List<String> kits4DependOnGmsMethod = new ArrayList<>();
        for (Iterator ite = kit2FixbotMethodsMap.keySet().iterator(); ite.hasNext();) {
            String kit = ite.next().toString();
            List<FixbotApiInfo> fixbotMethods = kit2FixbotMethodsMap.get(kit);
            KitItem kitItem = new KitItem(++kitIndex, kit, fixbotMethods.size());
            kitItems.add(kitItem);
            kits4DependOnGmsMethod.add(kit);
            methodCount += fixbotMethods.size();
            for (FixbotApiInfo fixbotMethod : fixbotMethods) {
                if (fixbotMethod.isSupport()) {
                    supportCount++;
                }
            }
        }

        List<String> kits4DependOnGmsClassOrField =
            SummaryResultUtil.computeKit4DependOnGmsClassOrField(kitStatisticsResults, kits4DependOnGmsMethod);
        for (String kit : kits4DependOnGmsClassOrField) {
            KitItem kitItem = new KitItem(++kitIndex, kit, 0);
            kitItems.add(kitItem);
        }

        totalTextField.setText(String.valueOf(kitItems.size()));
        methodTextField.setText(String.valueOf(methodCount));
        supportTextField.setText(String.valueOf(supportCount));
        if (kit2FixbotMethodsMap.isEmpty()) {
            BalloonNotifications.showSuccessNotification(HmsConvertorBundle.message("no_gms_found"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }
        kitTableModel.setItems(kitItems);
        methodTableModel.setItems(kit2FixbotMethodsMap.get(kitItems.get(Constant.FIRST_INDEX).getKitName()));
        splitePane.setDividerLocation(splitePane.getPreferredSize().width / 2);
    }

    @Override
    public Action[] createActions() {
        return new Action[] {getOKAction(), getCancelAction()};
    }

    @Override
    public Action getOKAction() {
        Action okAction = super.getOKAction();
        okAction.putValue(Action.NAME, HmsConvertorBundle.message("summary_export2file"));
        okAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
        return okAction;
    }

    @Override
    public Action getCancelAction() {
        Action cancelAction = super.getCancelAction();
        cancelAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
        cancelAction.putValue(Action.NAME, HmsConvertorBundle.message("summary_close"));
        return cancelAction;
    }

    @Override
    public void doOKAction() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            BIReportService.getInstance().traceExportClick(project.getBasePath(), AnalyseExportEnum.PRE_ANALYZE_DETAIL);

            exportAnalyseResult();
        }, ModalityState.defaultModalityState());
        super.doOKAction();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    private void exportAnalyseResult() {
        Optional<String> analyseFilePath;
        try {
            analyseFilePath = AnalyseResultExportUtil.exportPdf(project.getBasePath());
        } catch (Exception e) {
            log.error("export analyseResult fail, projectBasePath: {}.", project.getBasePath());
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("summary_export2file_error"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }
        if (!analyseFilePath.isPresent()) {
            log.error("export analyseResult fail, projectBasePath: {}.", project.getBasePath());
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("summary_export2file_error"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }

        File analyseResultFile = new File(analyseFilePath.get());
        ShowFilePathAction.openFile(analyseResultFile);
    }

    private static class KitTableCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 8151103041655612461L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
            // Restore default status
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setForeground(JBColor.black);
            table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            setHorizontalAlignment(SwingConstants.CENTER);
            return this;
        }
    }

    private static class MethodTableCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 8151103041655612462L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
            // Restore default status
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            if (column == MethodTableModel.METHOD_NAME_COLUMN_INDEX) {
                setHorizontalAlignment(SwingConstants.LEFT);
            } else {
                setHorizontalAlignment(SwingConstants.CENTER);
            }
            return this;
        }
    }

}
