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

import com.huawei.hms.convertor.core.engine.fixbot.model.MethodItem;
import com.huawei.hms.convertor.core.kits.KitsConstants;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.ui.result.summary.KitItem;
import com.huawei.hms.convertor.idea.ui.result.summary.KitTableModel;
import com.huawei.hms.convertor.idea.ui.result.summary.MethodTableModel;
import com.huawei.hms.convertor.util.Constant;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ImageLoader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
public class SummaryDialog extends DialogWrapper {
    private static final int ROW_HEIGHT = 25;

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

    private TableView<MethodItem> methodTable;

    private MethodTableModel methodTableModel;

    private List<KitItem> kitItems = new ArrayList<>();

    private TreeMap<String, List<MethodItem>> kit2MethodItemsMap = new TreeMap<>();

    public SummaryDialog(@NotNull Project project) {
        super(project);
        this.project = project;

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
        kitTablePanel.setPreferredSize(new Dimension(300, -1));
        methodTablePanel.add(createMethodTable());
        splitePane.setDividerLocation(splitePane.getPreferredSize().width / 2);
    }

    public JPanel createKitTable() {
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        kitTableModel = new KitTableModel();
        kitTable = new TableView<>(kitTableModel);
        kitTable.setRowHeight(ROW_HEIGHT);
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
                    final int selectedRow = kitTable.getSelectedRow();
                    if (selectedRow < 0) {
                        return;
                    }

                    final int kitTableModelSelectedIndex = kitTable.convertRowIndexToModel(selectedRow);
                    if (kitTableModelSelectedIndex < 0) {
                        return;
                    }
                    final String kitName = kitItems.get(kitTableModelSelectedIndex).getKitName();
                    methodTableModel.setItems(kit2MethodItemsMap.get(kitName));
                }
            }
        });

        final ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(kitTable);
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
        methodTable.setRowHeight(ROW_HEIGHT);
        methodTable.setDragEnabled(false);
        methodTable.getTableHeader().setVisible(true);
        methodTable.getTableHeader().setReorderingAllowed(false);
        methodTable.setDefaultRenderer(Object.class, new MethodTableCellRenderer());
        methodTable.setEnabled(true);
        methodTable.setAutoscrolls(true);
        methodTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        methodTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        final ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(methodTable);
        toolbarDecorator.disableAddAction();
        toolbarDecorator.disableRemoveAction();
        toolbarDecorator.disableUpAction();
        toolbarDecorator.disableAddAction();
        toolbarDecorator.disableDownAction();

        tablePanel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER);
        return tablePanel;
    }

    public void refreshData(TreeMap<String, List<MethodItem>> kit2Methods, List<String> allKits) {
        kit2MethodItemsMap = kit2Methods;
        int kitIndex = 0;
        int methodCount = 0;
        int supportCount = 0;
        for (Iterator ite = kit2MethodItemsMap.keySet().iterator(); ite.hasNext();) {
            String kit = ite.next().toString();
            List<MethodItem> methodItems = kit2MethodItemsMap.get(kit);
            KitItem kitItem = new KitItem(++kitIndex, kit, methodItems.size());
            kitItems.add(kitItem);
            methodCount += methodItems.size();
            for (MethodItem methodItem : methodItems) {
                if (methodItem.isSupport()) {
                    supportCount++;
                }
            }
        }
        int kitCount = allKits.size();
        if (allKits.contains(KitsConstants.COMMON)) {
            kitCount = kitCount - 1;
        }
        if (allKits.contains(KitsConstants.OTHER)) {
            kitCount = kitCount - 1;
        }
        totalTextField.setText(String.valueOf(kitCount));
        methodTextField.setText(String.valueOf(methodCount));
        supportTextField.setText(String.valueOf(supportCount));
        if (kit2MethodItemsMap.isEmpty()) {
            BalloonNotifications.showSuccessNotification(HmsConvertorBundle.message("no_gms_found"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }
        kitTableModel.setItems(kitItems);
        methodTableModel.setItems(kit2MethodItemsMap.get(kitItems.get(Constant.FIRST_INDEX).getKitName()));
        splitePane.setDividerLocation(splitePane.getPreferredSize().width / 2);
    }

    @NotNull
    @Override
    public Action[] createActions() {
        return new Action[] {};
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    private static class KitTableCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 8151103041655612461L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
            // Restore Default Status
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
            // Restore Default Status
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
