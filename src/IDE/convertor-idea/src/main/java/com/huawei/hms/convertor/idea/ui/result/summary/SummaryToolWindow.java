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

package com.huawei.hms.convertor.idea.ui.result.summary;

import com.huawei.hms.convertor.core.engine.fixbot.model.api.FixbotApiInfo;
import com.huawei.hms.convertor.core.engine.fixbot.model.kit.KitStatisticsResult;
import com.huawei.hms.convertor.core.result.summary.SummaryCacheManager;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.huawei.hms.convertor.idea.util.SummaryResultUtil;
import com.huawei.hms.convertor.openapi.SummaryCacheService;
import com.huawei.hms.convertor.util.Constant;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;

import org.jetbrains.annotations.NotNull;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Summary tool window
 *
 * @since 2019/11/30
 */
public class SummaryToolWindow extends SimpleToolWindowPanel implements Disposable {
    private static final long serialVersionUID = -5082041560673705712L;

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

    public SummaryToolWindow(@NotNull Project project) {
        super(true, true);
        this.project = project;
        kit2FixbotMethodsMap = new TreeMap<>();

        init();
    }

    @Override
    public void dispose() {
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public void asyncClearData() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            clearData();
        }, ModalityState.defaultModalityState());
    }

    public void refreshData(TreeMap<String, List<FixbotApiInfo>> kit2Methods,
        List<KitStatisticsResult> kitStatisticsResults) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            clearData();
            dealTextField(kit2Methods, kitStatisticsResults);
            if (kit2FixbotMethodsMap.isEmpty()) {
                BalloonNotifications.showSuccessNotification(HmsConvertorBundle.message("no_gms_found"), project,
                    Constant.PLUGIN_NAME, true);
                return;
            }
            kitTableModel.setItems(kitItems);
            methodTableModel.setItems(kit2FixbotMethodsMap.get(kitItems.get(Constant.FIRST_INDEX).getKitName()));
            splitePane.setDividerLocation(splitePane.getSize().width / UIConstants.ToolWindow.Summary.SPLITE_PANE_DIVISOR);
        }, ModalityState.defaultModalityState());
    }

    public void loadLastConversion() {
        asyncClearData();
        TreeMap<String, List<FixbotApiInfo>> kit2MethodItemListMap =
            SummaryCacheService.getInstance().loadSummary(project.getBasePath());
        List<KitStatisticsResult> kitStatisticsResults =
            SummaryCacheManager.getInstance().getKitStatisticsResults(project.getBasePath());
        refreshData(kit2MethodItemListMap, kitStatisticsResults);
    }

    private void init() {
        gmsApisLabel.setText(HmsConvertorBundle.message("gms_kits") + ":");
        totalMetLabel.setText(HmsConvertorBundle.message("total_methods") + ":");
        supportLabel.setText(HmsConvertorBundle.message("total_support") + ":");
        kitTablePanel.add(createKitTable(), BorderLayout.CENTER);
        methodTablePanel.add(createMethodTable(), BorderLayout.CENTER);
        splitePane.setDividerLocation(splitePane.getPreferredSize().width / 2);
        setTextFieldStyle(totalTextField);
        setTextFieldStyle(supportTextField);
        setTextFieldStyle(methodTextField);
        setContent(rootPanel);
    }

    private void setTextFieldStyle(JTextField textField) {
        textField.setText("0");
        textField.setHorizontalAlignment(SwingConstants.LEFT);
        textField.setPreferredSize(new Dimension(UIConstants.ToolWindow.Summary.TEXT_FIELD_WIDTH, UIConstants.ToolWindow.Summary.TEXT_FIELD_HEIGHT));
        textField.setBorder(new EmptyBorder(0, 0, 0, 0));
        textField.setBackground(rootPanel.getBackground());
    }

    private JPanel createMethodTable() {
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        methodTableModel = new MethodTableModel();
        methodTable = new TableView<>(methodTableModel);
        methodTable.setRowHeight(UIConstants.ToolWindow.ROW_HEIGHT);
        methodTable.setEnabled(true);
        methodTable.setAutoscrolls(true);
        methodTable.setDragEnabled(false);
        methodTable.getTableHeader().setVisible(true);
        methodTable.getTableHeader().setReorderingAllowed(false);
        methodTable.setDefaultRenderer(Object.class, new MethodTableCellRenderer());
        methodTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        methodTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        final ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(methodTable);
        toolbarDecorator.disableAddAction();
        toolbarDecorator.disableUpAction();
        toolbarDecorator.disableDownAction();
        toolbarDecorator.disableRemoveAction();

        tablePanel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createKitTable() {
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        kitTableModel = new KitTableModel();
        kitTable = new TableView<>(kitTableModel);
        kitTable.setRowHeight(UIConstants.ToolWindow.ROW_HEIGHT);
        kitTable.setDragEnabled(false);
        kitTable.getTableHeader().setVisible(true);
        kitTable.getTableHeader().setReorderingAllowed(false);
        kitTable.setDefaultRenderer(Object.class, new KitTableCellRenderer());
        kitTable.setEnabled(true);
        kitTable.setAutoscrolls(true);
        kitTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        kitTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        kitTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && (MOUSE_SINGLE_CLICK == e.getClickCount())) {
                    final int selectedRow = kitTable.getSelectedRow();
                    if (selectedRow < 0) {
                        return;
                    }

                    final int kitTableModelSelectedIndex = kitTable.convertRowIndexToModel(selectedRow);
                    if (kitTableModelSelectedIndex < 0) {
                        return;
                    }
                    final String kitName = kitItems.get(kitTableModelSelectedIndex).getKitName();
                    methodTableModel.setItems(kit2FixbotMethodsMap.get(kitName));
                }
            }
        });

        final ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(kitTable);
        toolbarDecorator.disableAddAction();
        toolbarDecorator.disableRemoveAction();
        toolbarDecorator.disableUpAction();
        toolbarDecorator.disableDownAction();

        tablePanel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER);
        return tablePanel;
    }

    private void clearData() {
        totalTextField.setText("");
        methodTextField.setText("");
        supportTextField.setText("");
        if (!kitItems.isEmpty()) {
            kitItems.clear();
        }
        if (!kit2FixbotMethodsMap.isEmpty()) {
            kit2FixbotMethodsMap.clear();
            // clear summary toolWindow, so need to clear summary toolWindow cache
            SummaryCacheService.getInstance().clearAnalyseResultCache4SummaryResult(project.getBasePath());
        }
        kitTableModel.setItems(Collections.emptyList());
        methodTableModel.setItems(Collections.emptyList());
    }

    private void dealTextField(TreeMap<String, List<FixbotApiInfo>> kit2MethodItemListMap,
        List<KitStatisticsResult> kitStatisticsResults) {
        kit2FixbotMethodsMap = new TreeMap<>(kit2MethodItemListMap);
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
        supportTextField.setText(String.valueOf(supportCount));
        methodTextField.setText(String.valueOf(methodCount));
    }

    private static class KitTableCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 8151103041655612463L;

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
        private static final long serialVersionUID = 8151103041655612464L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
            // Restore default status
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            if (MethodTableModel.METHOD_NAME_COLUMN_INDEX == column) {
                setHorizontalAlignment(SwingConstants.LEFT);
            } else {
                setHorizontalAlignment(SwingConstants.CENTER);
            }
            return this;
        }
    }
}
