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

package com.huawei.hms.convertor.idea.ui.result.xms;

import com.huawei.hms.convertor.core.result.diff.XmsDiff;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.util.HmsConvertorUtil;
import com.huawei.hms.convertor.openapi.XmsDiffCacheService;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.ide.actions.ShowFilePathAction;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.UIUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

@Slf4j
public class XmsDiffToolWindow extends SimpleToolWindowPanel implements Disposable, ActionListener {
    private static final String BACKUP_FILE_PREFIX = "Backup file: ";

    private static final String CURRENT_FILE_PREFIX = "Current file: ";

    private static final String[] ALL_STATUS_VALIDATE = {XmsDiffItem.Status.MODIFIED.getStatusStr(),
        XmsDiffItem.Status.DELETED.getStatusStr(), XmsDiffItem.Status.UPDATED.getStatusStr(),
        XmsDiffItem.Status.ADD.getStatusStr(), XmsDiffItem.Status.NA.getStatusStr()};

    private JPanel rootPanel;

    private JComboBox statusComboBox;

    private JPanel tablePanel;

    private JLabel statusTextLable;

    private JPanel rightPanel;

    private JLabel noDifferent;

    private JTextField filterField;

    private JLabel searchIcon;

    private JButton openBackupFileButton;

    private JButton openNewFileButton;

    private List<XmsDiffItem> xmsDiffItems = new ArrayList<>();

    private List<XmsDiffItem> showWmsDiffItems = new ArrayList<>();

    private TableView<XmsDiffItem> xmsDiffTable;

    private XmsDiffTableModel xmsDiffTableModel;

    private Project project;

    private XmsDiff xmsDiff;

    private String repoID;

    private XmsDiffActionAdapter oldXmsDiffAction;

    private XmsDiffActionAdapter newXmsDiffAction;

    public XmsDiffToolWindow(@NotNull Project project, XmsDiff diff) {
        super(true, true);
        this.project = project;
        xmsDiff = diff;
        initView();
        refreshData(xmsDiff);
    }

    private static void mergeChangedFileList(List<XmsDiffItem> diffItems, XmsDiff diff) {
        TreeMap<String, String> updateMap = diff.getUpdatedMap();
        TreeMap<String, String> modMap = diff.getModMap();
        List<String> addFiles = diff.getAddList();
        List<String> deleteFiles = diff.getDelList();
        Iterator iter = modMap.entrySet().iterator();
        String key;
        while (iter.hasNext()) {
            Object item = iter.next();
            if (item instanceof Map.Entry) {
                Map.Entry ent = (Map.Entry) item;
                key = ent.getKey().toString();
                diffItems.add(new XmsDiffItem(key, ent.getValue().toString(), XmsDiffItem.Status.MODIFIED,
                    diff.getNewXMSLocation(), diff.getOldXMSLocation()));
            }
        }
        for (String deleteFile : deleteFiles) {
            diffItems.add(new XmsDiffItem(Constant.NA, deleteFile, XmsDiffItem.Status.DELETED, diff.getNewXMSLocation(),
                diff.getOldXMSLocation()));
        }
        iter = updateMap.entrySet().iterator();
        while (iter.hasNext()) {
            Object item = iter.next();
            if (item instanceof Map.Entry) {
                Map.Entry ent = (Map.Entry) item;
                key = ent.getKey().toString();
                diffItems.add(new XmsDiffItem(key, ent.getValue().toString(), XmsDiffItem.Status.UPDATED,
                    diff.getNewXMSLocation(), diff.getOldXMSLocation()));
            }
        }
        for (String addFile : addFiles) {
            diffItems.add(new XmsDiffItem(addFile, Constant.NA, XmsDiffItem.Status.ADD, diff.getNewXMSLocation(),
                diff.getOldXMSLocation()));
        }
    }

    private static void showTwoFiles(Project project, String newFilePath, String oldFilePath) {
        if (Constant.NA.equals(newFilePath) && !Constant.NA.equals(oldFilePath)) {
            showFile(project, oldFilePath, false);
        } else if (!Constant.NA.equals(newFilePath) && Constant.NA.equals(oldFilePath)) {
            showFile(project, newFilePath, true);
        } else if (!Constant.NA.equals(newFilePath) && !Constant.NA.equals(oldFilePath)) {
            showDiff(project, newFilePath, oldFilePath);
        } else {
            log.error("Two invalid file name! {} {}", newFilePath, oldFilePath);
        }
    }

    private static void showDiff(Project project, String newFilePath, String oldFilePath) {
        DiffContent newFileContent;
        if ("N/A".equals(newFilePath)) {
            newFileContent = DiffContentFactory.getInstance().create("");
        } else {
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(newFilePath));
            VirtualFile newFile = LocalFileSystem.getInstance().findFileByPath(newFilePath);
            if (newFile == null) {
                Messages.showErrorDialog(project, "New file does not exist!", "Error");
                log.warn("showDiff: newFile[VirtualFile] is null!");
                return;
            }
            newFileContent = DiffContentFactory.getInstance().create(project, newFile);
        }

        DiffContent oldFileContent;
        if (Constant.NA.equals(oldFilePath)) {
            oldFileContent = DiffContentFactory.getInstance().create("");
        } else {
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(oldFilePath));
            VirtualFile oldFile = LocalFileSystem.getInstance().findFileByPath(oldFilePath);
            if (oldFile == null) {
                Messages.showErrorDialog(project, "Old file does not exist!", "Error");
                log.warn("showDiff: oldFile[VirtualFile] is null!");
                return;
            }
            oldFileContent = DiffContentFactory.getInstance().create(project, oldFile);
        }

        String newFileName;
        if (newFilePath.contains(Constant.SEPARATOR)) {
            newFileName = CURRENT_FILE_PREFIX + newFilePath.substring(newFilePath.lastIndexOf(Constant.SEPARATOR) + 1);
        } else {
            newFileName = CURRENT_FILE_PREFIX + newFilePath;
        }
        String oldFileName;
        if (oldFilePath.contains(Constant.SEPARATOR)) {
            oldFileName = BACKUP_FILE_PREFIX + oldFilePath.substring(oldFilePath.lastIndexOf(Constant.SEPARATOR) + 1);
        } else {
            oldFileName = BACKUP_FILE_PREFIX + oldFilePath;
        }

        DiffRequest diffRequest =
            new SimpleDiffRequest(Constant.PLUGIN_NAME, newFileContent, oldFileContent, newFileName, oldFileName);

        UIUtil.invokeLaterIfNeeded(() -> {
            DiffManager.getInstance().showDiff(project, diffRequest);
        });
    }

    private static void showFile(Project project, String filePath, boolean newFileFlag) {
        try {
            DiffContent emptyFileContent = DiffContentFactory.getInstance().create("");
            final DiffContent fileContent =
                DiffContentFactory.getInstance().create(FileUtil.readToFormatString(filePath, Constant.UTF8));
            String fileName;
            if (filePath.contains(Constant.SEPARATOR)) {
                fileName = filePath.substring(filePath.lastIndexOf(Constant.SEPARATOR) + 1);
            } else {
                fileName = filePath;
            }
            DiffRequest diffRequest;
            if (newFileFlag) {
                diffRequest = new SimpleDiffRequest(Constant.PLUGIN_NAME, fileContent, emptyFileContent,
                    CURRENT_FILE_PREFIX + fileName, Constant.NA);
            } else {
                diffRequest = new SimpleDiffRequest(Constant.PLUGIN_NAME, emptyFileContent, fileContent, Constant.NA,
                    BACKUP_FILE_PREFIX + fileName);
            }

            UIUtil.invokeLaterIfNeeded(() -> {
                DiffManager.getInstance().showDiff(project, diffRequest);
            });
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            BalloonNotifications.showErrorNotification(e.getMessage(), project, Constant.PLUGIN_NAME, true);
        }
    }

    private static void setTable(JPanel panel, TableView<XmsDiffItem> tableView) {
        panel.setLayout(new BorderLayout());
        tableView.getTableHeader().setVisible(true);
        tableView.getTableHeader().setReorderingAllowed(false);
        tableView.setDefaultRenderer(Object.class, new XmsDiffTableCellRenderer());
        tableView.setRowHeight(25);
        tableView.setDragEnabled(true);
        tableView.setEnabled(true);
        tableView.setAutoscrolls(true);
        tableView.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tableView.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        final ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(tableView);
        toolbarDecorator.disableRemoveAction();
        toolbarDecorator.disableUpAction();
        toolbarDecorator.disableAddAction();
        toolbarDecorator.disableDownAction();
        panel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER);
    }

    private void initView() {
        setContent(rootPanel);
        addFilterListener();
        openBackupFileButton.setToolTipText(HmsConvertorBundle.message("open_backup_folder"));
        oldXmsDiffAction = new XmsDiffActionAdapter(XmsDiffActionAdapter.OLD_LOCATION);
        openBackupFileButton.addMouseListener(oldXmsDiffAction);
        openNewFileButton.setToolTipText(HmsConvertorBundle.message("open_new_folder"));
        newXmsDiffAction = new XmsDiffActionAdapter(XmsDiffActionAdapter.NEW_LOCATION);
        openNewFileButton.addMouseListener(newXmsDiffAction);
    }

    private void addFilterListener() {
        filterField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    doSelectActions();
                }
            }
        });
        searchIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && (1 == e.getClickCount())) {
                    doSelectActions();
                }
            }
        });
    }

    public void refreshData(XmsDiff diff) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            xmsDiff = diff;
            XmsDiffCacheService.getInstance().setXmsDiff(project.getBasePath(), xmsDiff);
            xmsDiffItems = new ArrayList<>();
            showWmsDiffItems = new ArrayList<>();

            if (xmsDiff == null) {
                noDifferent.setVisible(true);
                rightPanel.setVisible(false);
                HmsConvertorUtil.getHmsConvertorToolWindow(project).ifPresent(hmsConvertorToolWindow -> {
                    hmsConvertorToolWindow.setXmsDiffVisible(false);
                });
                return;
            }

            HmsConvertorUtil.getHmsConvertorToolWindow(project).ifPresent(hmsConvertorToolWindow -> {
                hmsConvertorToolWindow.setXmsDiffVisible(true);
            });
            noDifferent.setVisible(false);
            rightPanel.setVisible(true);
            xmsDiff.setOldXMSLocation(xmsDiff.getOldXMSLocation().replace("\\", "/"));
            xmsDiff.setNewXMSLocation(xmsDiff.getNewXMSLocation().replace("\\", "/"));

            setShowDataWithDiff(xmsDiff);
            statusComboBox.removeActionListener(this);
            statusComboBox.removeAllItems();
            setStatusComboBox(statusComboBox, xmsDiff);
            statusComboBox.addActionListener(this);
        }, ModalityState.defaultModalityState());
    }

    private void doSelectActions() {
        showWmsDiffItems.clear();
        String status = statusComboBox.getSelectedItem().toString();
        String[] statusValidates = getValidates(status);
        String filterText =
            StringUtil.isEmpty(filterField.getText()) ? "" : StringUtils.lowerCase(filterField.getText());
        for (XmsDiffItem item : xmsDiffItems) {
            String newFileName =
                StringUtil.isEmpty(item.getNewFileName()) ? "" : StringUtils.lowerCase(item.getNewFileName());
            String oldFileName =
                StringUtil.isEmpty(item.getOldFileName()) ? "" : StringUtils.lowerCase(item.getOldFileName());
            if (isInValidates(statusValidates, item.getStatus().getStatusStr())
                && (newFileName.contains(filterText) || oldFileName.contains(filterText))) {
                showWmsDiffItems.add(item);
            }
        }
        xmsDiffTableModel.setItems(showWmsDiffItems);
    }

    private String[] getValidates(String choice) {
        for (String validate : XmsDiffToolWindow.ALL_STATUS_VALIDATE) {
            if (validate.equals(choice)) {
                return new String[] {validate};
            }
        }
        return XmsDiffToolWindow.ALL_STATUS_VALIDATE;
    }

    private boolean isInValidates(String[] validates, String choice) {
        for (String statusValidate : validates) {
            if (choice.equals(statusValidate)) {
                return true;
            }
        }
        return false;
    }

    private void setShowDataWithDiff(XmsDiff diff) {
        newXmsDiffAction.setDiff(diff);
        oldXmsDiffAction.setDiff(diff);
        mergeChangedFileList(xmsDiffItems, diff);
        createXmsDiffTable();
    }

    private void setStatusComboBox(JComboBox comboBox, XmsDiff diff) {
        comboBox.addItem(Constant.ALL);
        if (!diff.getModMap().isEmpty()) {
            comboBox.addItem(XmsDiffItem.Status.MODIFIED.getStatusStr());
        }
        if (!diff.getDelList().isEmpty()) {
            comboBox.addItem(XmsDiffItem.Status.DELETED.getStatusStr());
        }
        if (!diff.getUpdatedMap().isEmpty()) {
            comboBox.addItem(XmsDiffItem.Status.UPDATED.getStatusStr());
        }
        if (!diff.getAddList().isEmpty()) {
            comboBox.addItem(XmsDiffItem.Status.ADD.getStatusStr());
        }
        comboBox.setSelectedItem(Constant.ALL);
    }

    private void createXmsDiffTable() {
        JPanel xmsDiffPanel = new JPanel();
        xmsDiffPanel.setLayout(new BorderLayout());
        xmsDiffTableModel = new XmsDiffTableModel();
        xmsDiffTable = new TableView<>(xmsDiffTableModel);
        xmsDiffPanel.setVisible(true);
        setTable(xmsDiffPanel, xmsDiffTable);
        showWmsDiffItems.addAll(xmsDiffItems);
        xmsDiffTableModel.setItems(showWmsDiffItems);
        addListenerForTable(xmsDiffTable);
        tablePanel.removeAll();
        tablePanel.add(xmsDiffPanel);
    }

    private void addListenerForTable(TableView<XmsDiffItem> xmsItemsTable) {
        xmsItemsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && (2 == e.getClickCount())) {
                    final int selectedRow = xmsItemsTable.getSelectedRow();
                    if (selectedRow < 0) {
                        return;
                    }
                    XmsDiffItem selectedObject = xmsItemsTable.getSelectedObject();
                    final String newFileName = selectedObject.getNewFileName();
                    final String oldFileName = selectedObject.getOldFileName();
                    log.info("chose file {} {}", newFileName, oldFileName);
                    showTwoFiles(project, newFileName, oldFileName);
                }
            }
        });
    }

    @Override
    public void dispose() {
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        doSelectActions();
    }

    public void loadXmsDiff() {
        XmsDiff newXmsDiff = XmsDiffCacheService.getInstance().loadXmsDiff(project.getBasePath());
        if (newXmsDiff != null) {
            refreshData(newXmsDiff);
        } else {
            refreshData(null);
        }
    }

    private static class XmsDiffTableCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 22222222222222L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setForeground(JBColor.black);
            table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            if (column == XmsDiffTableModel.STATUS_COLUMN_INDEX) {
                setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
                setAlignmentX(10);
            }

            return this;
        }
    }

    private static class XmsDiffActionAdapter extends MouseAdapter {
        private static final int OLD_LOCATION = 0;

        private static final int NEW_LOCATION = 1;

        private XmsDiff diff;

        private int locationType;

        XmsDiffActionAdapter(int locationType) {
            this.locationType = locationType;
        }

        private void setDiff(XmsDiff diff) {
            this.diff = diff;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (diff != null && SwingUtilities.isLeftMouseButton(e) && (1 == e.getClickCount())) {
                File file;
                switch (locationType) {
                    case OLD_LOCATION:
                        file = new File(diff.getOldXMSLocation());
                        ShowFilePathAction.openFile(file);
                        break;
                    case NEW_LOCATION:
                        file = new File(diff.getNewXMSLocation());
                        ShowFilePathAction.openFile(file);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
