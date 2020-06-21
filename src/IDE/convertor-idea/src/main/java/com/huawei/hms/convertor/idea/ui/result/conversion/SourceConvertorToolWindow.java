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

import com.huawei.hms.convertor.core.bi.bean.ConversionOperationBean;
import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.engine.fixbot.model.RoutePolicy;
import com.huawei.hms.convertor.core.engine.fixbot.util.FixbotConstants;
import com.huawei.hms.convertor.core.event.context.EventType;
import com.huawei.hms.convertor.core.event.context.project.ProjectEvent;
import com.huawei.hms.convertor.core.kits.KitsConstants;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.core.result.conversion.ConversionItem;
import com.huawei.hms.convertor.core.result.conversion.ConvertType;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.listener.DocumentChangeListener;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.ui.result.difftool.HmsConvertorDiffUserDataKeys;
import com.huawei.hms.convertor.idea.ui.result.searchcombobox.ComboBoxFilterDecorator;
import com.huawei.hms.convertor.idea.ui.result.searchcombobox.CustomComboBoxRenderer;
import com.huawei.hms.convertor.idea.util.IconUtil;
import com.huawei.hms.convertor.idea.util.TimeUtil;
import com.huawei.hms.convertor.idea.util.UiUtil;
import com.huawei.hms.convertor.openapi.BIReportService;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.ConversionCacheService;
import com.huawei.hms.convertor.openapi.EventService;
import com.huawei.hms.convertor.openapi.result.Result;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.ScrollingModel;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.UIUtil;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.NoSuchFileException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Source convertor tool window
 *
 * @since 2019-06-11
 */
public class SourceConvertorToolWindow extends SimpleToolWindowPanel implements Disposable {
    private static final Logger LOG = LoggerFactory.getLogger(SourceConvertorToolWindow.class);

    private static final int RIGHT_MARGIN = 10;

    private static final long serialVersionUID = 8086729502521504370L;

    private static final int ROW_HEIGHT = 25;

    private static final int MOUSE_SINGLE_CLICK = 1;

    private static final int MOUSE_DOUBLE_CLICK = 2;

    private static final int CLICK_DELAY = 300; // ms

    private JPanel rootPanel;

    private JLabel totalNumberLabel;

    private JComboBox fileComboBox;

    private JComboBox kitNameComboBox;

    private JCheckBox showConvertedCheckBox;

    private JLabel convertedNumberLabel;

    private JButton convertButton;

    private JButton revertButton;

    private TableView<DefectItem> resultTable;

    private ResultTableModel resultTableModel;

    private List<DefectItem> defectItemList = new ArrayList<>();

    private List<DefectItem> allDefectItems = new ArrayList<>();

    private TreeMap<String, List<DefectItem>> fileToDefectsMap = new TreeMap<>();

    private int totalCount = 0;

    private int confirmedTotalCount = 0;

    private int confirmedFilteredCount = 0;

    private int autoConvertFilteredCount = 0;

    private int convertedTotalCount = 0;

    private boolean isConfirmedAllFiltered = false;

    private Project project;

    private String inspectPath;

    private String repoID;

    private Timer mouseTimer;

    private HashSet<String> kitNames = new HashSet<>();

    private DocumentChangeListener changeListener = null;

    private ActionListener filterAction = (e) -> filterAndRefresh();

    private ComboBoxKeyAdapter comboBoxKeyAdapter = new ComboBoxKeyAdapter();

    private ConfigCacheService configCacheService;

    private boolean enableConvert = true;

    public SourceConvertorToolWindow(@NotNull Project project) {
        super(true, true);
        this.project = project;

        init();
    }

    public SourceConvertorToolWindow() {
        super(true);
    }

    @Override
    public void dispose() {
        if (mouseTimer != null) {
            mouseTimer.stop();
        }
    }

    public void init() {
        configCacheService = ConfigCacheService.getInstance();
        rootPanel.setLayout(new BorderLayout());

        JBScrollPane topScrollPane = new JBScrollPane(createTopPanel());
        topScrollPane.setLayout(new ScrollPaneLayout());
        topScrollPane.setBorder(BorderFactory.createEmptyBorder());
        rootPanel.add(topScrollPane, BorderLayout.NORTH);

        rootPanel.add(createResultTable(), BorderLayout.CENTER);

        initSummaryData();
        addSummaryListener();

        setContent(rootPanel);
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        topPanel.setLayout(gridBagLayout);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        LayoutConfigeration layoutConfigeration = new LayoutConfigeration();

        JLabel fileLabel = new JLabel(HmsConvertorBundle.message("file"));
        layoutConfigeration.setParams(gridBagLayout, gridBagConstraints, 0, 0);
        layoutComponent(topPanel, fileLabel, layoutConfigeration);

        fileComboBox = new ComboBox(320);
        fileComboBox.setAutoscrolls(true);
        layoutConfigeration.setParams(gridBagLayout, gridBagConstraints, 1, 20);
        layoutComponent(topPanel, fileComboBox, layoutConfigeration);

        JLabel convertTypeLabel = new JLabel(HmsConvertorBundle.message("gms_kits_name"));
        layoutConfigeration.setParams(gridBagLayout, gridBagConstraints, 2, 0);
        layoutComponent(topPanel, convertTypeLabel, layoutConfigeration);

        kitNameComboBox = new ComboBox(130);
        kitNameComboBox.setAutoscrolls(true);
        layoutConfigeration.setParams(gridBagLayout, gridBagConstraints, 3, 0);
        layoutComponent(topPanel, kitNameComboBox, layoutConfigeration);

        showConvertedCheckBox = new JCheckBox("");
        showConvertedCheckBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        layoutConfigeration.setParams(gridBagLayout, gridBagConstraints, 4, 0);
        layoutComponent(topPanel, showConvertedCheckBox, layoutConfigeration);

        JLabel showConvertedLabel = new JLabel(HmsConvertorBundle.message("show_converted"));
        layoutConfigeration.setParams(gridBagLayout, gridBagConstraints, 5, 0);
        layoutComponent(topPanel, showConvertedLabel, layoutConfigeration);

        JPanel splitPanel = new JPanel();
        splitPanel.setPreferredSize(new Dimension(120, -1));
        layoutConfigeration.setParams(gridBagLayout, gridBagConstraints, 6, 0);
        layoutComponent(topPanel, splitPanel, layoutConfigeration);

        JLabel totalLabel = new JLabel(HmsConvertorBundle.message("total") + ": ");
        layoutConfigeration.setParams(gridBagLayout, gridBagConstraints, 7, 0);
        layoutComponent(topPanel, totalLabel, layoutConfigeration);

        addCopmponentForTopPanel(layoutConfigeration, gridBagLayout, gridBagConstraints, topPanel);

        return topPanel;
    }

    private void addCopmponentForTopPanel(LayoutConfigeration layoutConfigeration, GridBagLayout gridBagLayout,
        GridBagConstraints gridBagConstraints, JPanel topPanel) {
        totalNumberLabel = new JLabel("");
        totalNumberLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, RIGHT_MARGIN));
        layoutConfigeration.setParams(gridBagLayout, gridBagConstraints, 8, 0);
        layoutComponent(topPanel, totalNumberLabel, layoutConfigeration);

        JLabel convertedCountLabel = new JLabel(HmsConvertorBundle.message("converted") + ": ");
        layoutConfigeration.setParams(gridBagLayout, gridBagConstraints, 9, 0);
        layoutComponent(topPanel, convertedCountLabel, layoutConfigeration);

        convertedNumberLabel = new JLabel("");
        convertedNumberLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, RIGHT_MARGIN));
        layoutConfigeration.setParams(gridBagLayout, gridBagConstraints, 10, 1);
        layoutComponent(topPanel, convertedNumberLabel, layoutConfigeration);

        convertButton = new JButton(HmsConvertorBundle.message("convert"));
        convertButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        layoutConfigeration.setParams(gridBagLayout, gridBagConstraints, 11, 0);
        layoutComponent(topPanel, convertButton, layoutConfigeration);

        revertButton = new JButton(HmsConvertorBundle.message("revert"));
        revertButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        layoutConfigeration.setParams(gridBagLayout, gridBagConstraints, 12, 0);
        layoutComponent(topPanel, revertButton, layoutConfigeration);
    }

    private void layoutComponent(JPanel panel, JComponent component, LayoutConfigeration layoutConfigeration) {
        GridBagConstraints constraints = layoutConfigeration.getGridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.ipadx = 5;
        constraints.gridx = layoutConfigeration.getColunm();
        constraints.gridy = 0;
        constraints.weightx = layoutConfigeration.getWeigthx();
        constraints.weighty = 0;
        layoutConfigeration.getGridBagLayout().setConstraints(component, constraints);
        panel.add(component);
    }

    public JPanel createResultTable() {
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());

        resultTableModel = new ResultTableModel();
        resultTable = new TableView<>(resultTableModel);
        resultTable.getTableHeader().setVisible(true);
        resultTable.getTableHeader().setReorderingAllowed(false);
        resultTable.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        resultTable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = resultTable.columnAtPoint(e.getPoint());
                if (ResultTableModel.CONFIRM_COLUMN_INDEX != column || !enableConvert) {
                    return;
                }

                confirmAllDefects();
            }
        });

        resultTable.setRowHeight(ROW_HEIGHT);
        resultTable.setDragEnabled(true);
        resultTable.setEnabled(true);
        resultTable.setAutoscrolls(true);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        resultTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        setMouseTimer();

        setResultTable();

        createPopupMenu();

        final ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(resultTable);
        toolbarDecorator.disableAddAction();
        toolbarDecorator.disableRemoveAction();
        toolbarDecorator.disableUpAction();
        toolbarDecorator.disableDownAction();
        tablePanel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER);

        return tablePanel;
    }

    private void setResultTable() {
        resultTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.isControlDown() || e.isShiftDown() || e.isAltDown() || !enableConvert) {
                    return;
                }

                if (SwingUtilities.isLeftMouseButton(e) && (MOUSE_SINGLE_CLICK == e.getClickCount())
                    && !mouseTimer.isRunning()) {
                    mouseTimer.start();
                } else if (SwingUtilities.isLeftMouseButton(e) && MOUSE_DOUBLE_CLICK == e.getClickCount()
                    && mouseTimer.isRunning()) {
                    mouseTimer.stop();
                    showDiff();
                    e.consume();
                } else {
                    e.consume();
                }
            }
        });
    }

    private void setMouseTimer() {
        mouseTimer = new Timer(CLICK_DELAY, e1 -> {
            int selectedColumn = resultTable.getSelectedColumn();
            DefectItem defectItem = resultTable.getSelectedObject();
            if (null == defectItem) {
                LOG.warn("MOUSE_SINGLE_CLICK: defectItem is null!");
                mouseTimer.stop();
                return;
            }

            if (ResultTableModel.CONFIRM_COLUMN_INDEX == selectedColumn) {
                confirmOrSetConverted(defectItem);
                mouseTimer.stop();
                return;
            }

            ApplicationManager.getApplication().invokeLater(() -> {
                ReadAction.run(() -> {
                    openFile(defectItem);
                    mouseTimer.stop();
                });
            });
        });
    }

    private void initSummaryData() {
        totalNumberLabel.setText("0");

        fileComboBox.removeAllItems();
        fileComboBox.addItem(Constant.ALL);
        fileComboBox.setSelectedItem(Constant.ALL);

        kitNameComboBox.removeAllItems();
        kitNameComboBox.addItem(Constant.ALL);
        kitNameComboBox.setSelectedItem(Constant.ALL);

        showConvertedCheckBox.setSelected(true);
        totalCount = 0;
        convertedTotalCount = 0;
        convertedNumberLabel.setText("0");
    }

    public void asyncClearData() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            clearData();
        }, ModalityState.defaultModalityState());
    }

    public void clearData() {
        removeListenerForCombox();

        if (!defectItemList.isEmpty()) {
            defectItemList.clear();
            ConversionCacheService.getInstance().clearConversions(project.getBasePath());
        }
        if (!fileToDefectsMap.isEmpty()) {
            fileToDefectsMap.clear();
        }

        initSummaryData();
        isConfirmedAllFiltered = false;
        resultTable.getTableHeader().repaint();

        resultTableModel.setItems(defectItemList);

        if (changeListener != null) {
            EditorFactory.getInstance().getEventMulticaster().removeDocumentListener(changeListener);
            changeListener = null;
        }
    }

    private void coversionItem2defectItem(List<ConversionItem> conversionItems) {
        defectItemList.clear();
        for (ConversionItem item : conversionItems) {
            defectItemList.add(new DefectItem(item));
        }
    }

    public void refreshData(List<ConversionItem> conversionItems) {
        enableConversionFunc(true);
        ApplicationManager.getApplication().invokeAndWait(() -> {
            clearData();
            coversionItem2defectItem(conversionItems);
            constructResultTable();
            ComboBoxFilterDecorator decorator = ComboBoxFilterDecorator.decorate(fileComboBox);
            fileComboBox.setRenderer(new CustomComboBoxRenderer(decorator.getFilterLabel()));

            parseKitName();

            resultTableModel.setItems(defectItemList);
            if (defectItemList.isEmpty()) {
                BalloonNotifications.showSuccessNotification(HmsConvertorBundle.message("no_gms_found"), project,
                    Constant.PLUGIN_NAME, true);
            } else {
                addListenerForCombox();
                changeListener = new DocumentChangeListener(inspectPath, project);
                EditorFactory.getInstance().getEventMulticaster().addDocumentListener(changeListener);
                enableConvert = true;
            }
        }, ModalityState.defaultModalityState());
    }

    private void addListenerForCombox() {
        fileComboBox.addActionListener(filterAction);
        kitNameComboBox.addActionListener(filterAction);
        showConvertedCheckBox.addActionListener(filterAction);
        kitNameComboBox.addKeyListener(comboBoxKeyAdapter);
    }

    private void removeListenerForCombox() {
        if (fileComboBox.getActionListeners().length > 0) {
            fileComboBox.removeActionListener(filterAction);
        }
        if (kitNameComboBox.getActionListeners().length > 0) {
            kitNameComboBox.removeActionListener(filterAction);
        }
        if (showConvertedCheckBox.getActionListeners().length > 0) {
            showConvertedCheckBox.removeActionListener(filterAction);
        }
        if (kitNameComboBox.getKeyListeners().length > 0) {
            kitNameComboBox.removeKeyListener(comboBoxKeyAdapter);
        }
    }

    private void refreshConversionListAndTable(List<ConversionItem> conversionItems) {
        enableConversionFunc(true);
        LocalFileSystem.getInstance().refresh(true);

        refreshResultTable(conversionItems);
        LOG.info("HMS convert/revert finished! Elapsed time: {}", TimeUtil.getInstance().getElapsedTime());
    }

    public void refreshResultTable(List<ConversionItem> updateItemsData) {
        LOG.info("refreshResultTable start! updateItemsData size is : {}", updateItemsData.size());
        if (updateItemsData.isEmpty()) {
            return;
        }
        List<DefectItem> defectItemsData = defectToConvertItem(updateItemsData);
        defectItemsData.forEach(updateItem -> {
            defectItemList.forEach(defectItem -> {
                if (updateItem.getConversionId().equals(defectItem.getConversionId())) {
                    defectItem.setConverted(updateItem.isConverted());
                    defectItem.setDefectStartLine(updateItem.getDefectStartLine());
                    defectItem.setDefectEndLine(updateItem.getDefectEndLine());
                }
            });
        });
        defectItemsData.forEach(updateItem -> {
            allDefectItems.forEach(defectItem -> {
                if (updateItem.getConversionId().equals(defectItem.getConversionId())) {
                    defectItem.setConverted(updateItem.isConverted());
                    defectItem.setDefectStartLine(updateItem.getDefectStartLine());
                    defectItem.setDefectEndLine(updateItem.getDefectEndLine());
                }
            });
        });

        convertedTotalCount = (int) allDefectItems.stream().filter(DefectItem::isConverted).count();
        LOG.info("refreshResultTable end! converted is : {}, all is {}", convertedTotalCount, allDefectItems.size());
        setConvertedTotalCount(convertedTotalCount);

        resultTable.repaint();
    }

    private void constructResultTable() {
        inspectPath = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_PATH,
            String.class, "");
        repoID =
            configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.REPO_ID, String.class, "");
        totalCount = defectItemList.size();

        totalNumberLabel.setText(String.valueOf(totalCount));

        int autoConvertCount =
            (int) defectItemList.stream().filter(item -> item.getConvertType().equals(ConvertType.AUTO)).count();
        if (autoConvertCount > 0) {
            isConfirmedAllFiltered = true;
        } else {
            isConfirmedAllFiltered = false;
        }

        confirmedTotalCount = (int) defectItemList.stream().filter(DefectItem::isConfirmed).count();
        confirmedFilteredCount = confirmedTotalCount;

        convertedTotalCount = (int) defectItemList.stream().filter(DefectItem::isConverted).count();
        setConvertedTotalCount(convertedTotalCount);

        isConfirmedAllFiltered = false;
        resultTable.getTableHeader().repaint();
        allDefectItems = new ArrayList<>(defectItemList);
        fileToDefectsMap = new TreeMap<>(defectItemList.stream().collect(Collectors.groupingBy(DefectItem::getFile)));
        for (String file : fileToDefectsMap.keySet()) {
            fileComboBox.addItem(file);
        }
    }

    private void addSummaryListener() {
        convertButtonAddListener();
        revertButtonAddListener();
    }

    private void enableConversionFunc(boolean enable) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            enableConvert = enable;
            convertButton.setEnabled(enable);
            revertButton.setEnabled(enable);
            fileComboBox.setEnabled(enable);
            kitNameComboBox.setEnabled(enable);
            showConvertedCheckBox.setEnabled(enable);
            for (DefectItem item : defectItemList) {
                item.setEnable(enable);
            }
            resultTable.repaint();
            resultTable.getTableHeader().repaint();
        }, ModalityState.defaultModalityState());
    }

    private void converButtonAction() {
        TimeUtil.getInstance().getStartTime();
        if (0 == confirmedTotalCount) {
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("no_selected_defect"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }
        ApplicationManager.getApplication().invokeAndWait(() -> {
            FileDocumentManager.getInstance().saveAllDocuments();
        }, ModalityState.defaultModalityState());
        enableConversionFunc(false);

        LocalFileSystem.getInstance().refresh(false);
        List<DefectItem> defectItems = getDefects(true);
        List<DefectItem> mergedDefects = mergeDefects(defectItems);
        List<String> itemIds = new ArrayList<>();
        for (DefectItem defectItem : mergedDefects) {
            itemIds.add(defectItem.getConversionId());
        }

        Result result = EventService.getInstance()
            .submitProjectEvent(ProjectEvent.<List<String>, List<ConversionItem>> of(project.getBasePath(),
                EventType.CONVERT_EVENT, itemIds, (message) -> refreshConversionListAndTable(message)));
        if (result.getCode() != 0) {
            LOG.info("HMS convert defectItem failed. {}", result.getMessage());
        }
    }

    private void revertButtonAction() {
        if (0 == confirmedTotalCount) {
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("no_selected_defect"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }
        ApplicationManager.getApplication().invokeAndWait(() -> {
            FileDocumentManager.getInstance().saveAllDocuments();
        }, ModalityState.defaultModalityState());
        enableConversionFunc(false);
        TimeUtil.getInstance().getStartTime();

        LocalFileSystem.getInstance().refresh(false);
        List<DefectItem> defectItems = getDefects(false);
        List<DefectItem> mergeDefects = mergeDefects(defectItems);
        List<String> itemIds = new ArrayList<>();
        for (DefectItem defectItem : mergeDefects) {
            itemIds.add(defectItem.getConversionId());
        }
        Result result = EventService.getInstance()
            .submitProjectEvent(ProjectEvent.<List<String>, List<ConversionItem>> of(project.getBasePath(),
                EventType.REVERT_EVENT, itemIds, (message) -> refreshConversionListAndTable(message)));
        if (result.getCode() != 0) {
            LOG.info("Revert defectItem failed. {}", result.getMessage());
        }
    }

    private void convertButtonAddListener() {
        convertButton.addActionListener((e) -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    converButtonAction();
                }
            }).start();
        });
    }

    private void revertButtonAddListener() {
        revertButton.addActionListener((e) -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    revertButtonAction();
                }
            }).start();
        });
    }

    private void parseKitName() {
        defectItemList.forEach(item -> {
            String kitNameStr = item.getKitName();
            if ("[]" != kitNameStr) {
                String[] ayy = kitNameStr.substring(1, kitNameStr.length() - 1).split(",");
                List<String> kits = Arrays.asList(ayy);
                kits.forEach(kit -> {
                    kitNames.add(kit.trim());
                });
            } else {
                item.setKitName(KitsConstants.OTHER);
                kitNames.add(KitsConstants.OTHER);
            }

        });
        kitNames.forEach(name -> {
            kitNameComboBox.addItem(name);
        });
    }

    public void loadLastConversion() {
        synchronized (SourceConvertorToolWindow.class) {
            asyncClearData();
            List<ConversionItem> conversionItems =
                ConversionCacheService.getInstance().loadConversions(project.getBasePath());
            refreshData(conversionItems);
        }
    }

    // isToConvert: true, get defects to convert; false, get defects to revert
    private List<DefectItem> getDefects(boolean isToConvert) {
        List<DefectItem> defectItems = new ArrayList<>();
        for (DefectItem defectItem : defectItemList) {
            if (((!ConvertType.AUTO.equals(defectItem.getConvertType())
                && !ConvertType.DUMMY.equals(defectItem.getConvertType())) || (!defectItem.isConfirmed()))
                || (defectItem.isConverted() == isToConvert)) {
                continue;
            }

            defectItems.add(defectItem);
        }

        return defectItems;
    }

    // Merge defects which on the same defect start line
    private List<DefectItem> mergeDefects(List<DefectItem> defectItems) {
        Map<String, List<DefectItem>> file2DefectsMap =
            defectItems.stream().collect(Collectors.groupingBy(DefectItem::getFile));

        Comparator comparator = new DefectStartLineComparator();
        List<DefectItem> mergedDefects = new ArrayList<>();
        for (Iterator ite = file2DefectsMap.keySet().iterator(); ite.hasNext();) {
            String file = ite.next().toString();
            List<DefectItem> fileDefectItems = file2DefectsMap.get(file);
            fileDefectItems.sort(comparator);

            DefectItem lastItem = fileDefectItems.get(Constant.FIRST_INDEX);
            int size = fileDefectItems.size();
            for (int i = 1; i < size; i++) {
                DefectItem item = fileDefectItems.get(i);
                if ((item.getDefectStartLine() == lastItem.getDefectStartLine())
                    && (item.getDefectEndLine() == lastItem.getDefectEndLine())) {
                    LOG.debug("i = {}, lastItem = {}", i, lastItem.toString());
                    LOG.debug("i = {}, item = {}", i, item.toString());

                    lastItem.setFixEndLine(item.getFixEndLine());
                    lastItem.setDefectContent(
                        (lastItem.getDefectContent() + Constant.LINE_SEPARATOR + item.getDefectContent()).trim());
                    lastItem.setFixContent(
                        (lastItem.getFixContent() + Constant.LINE_SEPARATOR + item.getFixContent()).trim());
                    lastItem.setFileTailConvert(lastItem.isFileTailConvert() || item.isFileTailConvert());
                    lastItem.setMergedDescription(lastItem.getMergedDescription().replace("</html>", ";&nbsp;&nbsp;")
                        + item.getMergedDescription().replace("<html>", ""));
                    HashSet<String> kitNamesSet = new HashSet<>();
                    mergeKit(kitNamesSet, lastItem);
                    mergeKit(kitNamesSet, item);

                    lastItem.setKitName(kitNamesSet.toString());
                    if (item.getDescriptions().size() > 0) {
                        lastItem.getDescriptions().addAll(item.getDescriptions());
                    }
                    lastItem.setDescriptions(lastItem.getDescriptions());

                    LOG.debug("i = {}, merged lastItem = {}", i, lastItem.toString());
                } else {
                    mergedDefects.add(lastItem);
                    lastItem = item;
                }
            }

            mergedDefects.add(lastItem);
        }

        return mergedDefects;
    }

    private void mergeKit(HashSet<String> kitNamesSet, DefectItem item) {
        String kitName = item.getKitName();
        if (!StringUtil.isEmpty(kitName)) {
            String[] kitStr = kitName.substring(1, kitName.length() - 1).split(",");
            List<String> kits = Arrays.asList(kitStr);
            kits.forEach(kit -> {
                kitNamesSet.add(kit.trim());
            });
        }
    }

    private void confirmAllDefects() {
        isConfirmedAllFiltered = !isConfirmedAllFiltered;
        List<DefectItem> filteredDefectItems = getFilteredDefectItems();
        if (isConfirmedAllFiltered) {
            for (DefectItem defectItem : filteredDefectItems) {
                if (ConvertType.AUTO.equals(defectItem.getConvertType()) && !defectItem.isConfirmed()
                    || ConvertType.DUMMY.equals(defectItem.getConvertType()) && defectItem.isConverted()) {
                    defectItem.setConfirmed(true);
                    confirmedTotalCount++;
                    confirmedFilteredCount++;
                }
            }
        } else {
            for (DefectItem defectItem : filteredDefectItems) {
                if (ConvertType.AUTO.equals(defectItem.getConvertType()) && defectItem.isConfirmed()
                    || ConvertType.DUMMY.equals(defectItem.getConvertType()) && defectItem.isConverted()) {
                    defectItem.setConfirmed(false);
                    confirmedTotalCount--;
                    confirmedFilteredCount--;
                }
            }
        }

        resultTable.getTableHeader().repaint();
        resultTable.repaint();
    }

    private void confirmOrSetConverted(DefectItem defectItem) {
        if (ConvertType.AUTO.equals(defectItem.getConvertType())
            || ConvertType.DUMMY.equals(defectItem.getConvertType())) {
            boolean isConfirmed = !defectItem.isConfirmed();
            if (isConfirmed) {
                confirmedTotalCount++;
                confirmedFilteredCount++;
            } else {
                confirmedTotalCount--;
                confirmedFilteredCount--;
            }
            defectItem.setConfirmed(isConfirmed);

            if ((confirmedFilteredCount == autoConvertFilteredCount) && (autoConvertFilteredCount > 0)) {
                isConfirmedAllFiltered = true;
            } else {
                isConfirmedAllFiltered = false;
            }
            resultTable.repaint();
        } else {
            boolean isConverted = !defectItem.isConverted();
            defectItem.setConverted(isConverted);
            List<String> selectItems = new ArrayList<>();
            selectItems.add(defectItem.getConversionId());
            setManualItem(isConverted, selectItems);
        }
    }

    private void openFile(DefectItem defectItem) {
        try {
            String filePath = defectItem.getFile();
            if (filePath.equals(Constant.NA)) {
                String allianceDomain = configCacheService.getProjectConfig(project.getBasePath(),
                    ConfigKeyConstants.ALLIANCE_DOMAIN, String.class, "");
                BrowserUtil.browse(allianceDomain + HmsConvertorBundle.message("initHelp_url"));
                return;
            }
            String defectFile = inspectPath + File.separatorChar + filePath;

            File file = new File(defectFile);
            if (StringUtil.isEmpty(defectFile) || (!file.exists()) || (!file.isFile())) {
                LOG.warn(defectFile + " is not a valid file!");
                throw new NoSuchFileException(filePath + "is not a valid file!");
            }

            VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            if (null == virtualFile) {
                LOG.warn("openFile: virtualFile is null!");
                return;
            }

            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile);
            Editor editor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
            if (null == editor) {
                LOG.warn("openFile: editor is null!");
                return;
            }

            final int lineCount = editor.getDocument().getLineCount();
            int startLine = Math.abs(defectItem.getDefectStartLine());
            int endLine = Math.abs(defectItem.getDefectEndLine());
            int startLineIndex = (startLine > lineCount) ? (lineCount - 1) : (startLine - 1);
            int endLineIndex = (endLine > lineCount) ? (lineCount - 1) : (endLine - 1);
            int startOffset = editor.getDocument().getLineStartOffset(startLineIndex);
            int endOffset = editor.getDocument().getLineEndOffset(endLineIndex);

            CaretModel caretModel = editor.getCaretModel();
            caretModel.removeCaret(caretModel.getCurrentCaret());
            caretModel.moveToOffset(startOffset);

            SelectionModel selectionModel = editor.getSelectionModel();
            selectionModel.removeSelection();
            selectionModel.setSelection(startOffset, endOffset);

            ScrollingModel scrollingModel = editor.getScrollingModel();
            scrollingModel.scrollToCaret(ScrollType.CENTER_UP);
            RoutePolicy routePolicy = configCacheService.getProjectConfig(project.getBasePath(),
                ConfigKeyConstants.ROUTE_POLICY, RoutePolicy.class, RoutePolicy.UNKNOWN);
            editor.getScrollingModel().runActionOnScrollingFinished(() -> {
                if (ConvertType.AUTO.equals(defectItem.getConvertType()) || (RoutePolicy.G_AND_H.equals(routePolicy)
                    && ConvertType.DUMMY.equals(defectItem.getConvertType()))) {
                    showSuggestionHint(project, editor, defectItem);
                } else {
                    UiUtil.showDefectTips(defectItem, editor);
                }
            });
        } catch (NoSuchFileException e) {
            LOG.warn(e.getMessage(), e);
            BalloonNotifications.showErrorNotification(e.getMessage(), project, Constant.PLUGIN_NAME, true);
        }
    }

    private void showSuggestionHint(Project project, Editor editor, DefectItem defectItem) {
        SuggestionHintComponent.showSuggestionHint(project, editor, true, defectItem);
    }

    private void convertDefectItem(DefectItem defectItem) {
        if ((!ConvertType.AUTO.equals(defectItem.getConvertType())
            && !ConvertType.DUMMY.equals(defectItem.getConvertType())) || defectItem.isConverted()) {
            return;
        }

        FileDocumentManager.getInstance().saveAllDocuments();
        Result result = EventService.getInstance()
            .submitProjectEvent(
                ProjectEvent.<String, List<ConversionItem>> of(project.getBasePath(), EventType.CONVERT_EVENT,
                    defectItem.getConversionId(), (message) -> refreshConversionListAndTable(message)));
        if (result.getCode() != 0) {
            LOG.info("HMS convert defectItem failed. {}", result.getMessage());
        }
        LOG.info("HMS convert defectItem finished.");
    }

    public void asyncConvertDefectItem(final DefectItem defectItem) {
        Runnable asyncConvert = (() -> {
            UiUtil.setStatusBarInfo(project, "");
            convertDefectItem(defectItem);
            UiUtil.setStatusBarInfo(project, "Convert OK!");
        });

        ApplicationManager.getApplication().invokeAndWait(asyncConvert, ModalityState.NON_MODAL);
    }

    private void revertDefectItem(DefectItem defectItem) {
        if ((!ConvertType.AUTO.equals(defectItem.getConvertType())
            && !ConvertType.DUMMY.equals(defectItem.getConvertType())) || !defectItem.isConverted()) {
            return;
        }

        FileDocumentManager.getInstance().saveAllDocuments();
        Result result = EventService.getInstance()
            .submitProjectEvent(
                ProjectEvent.<String, List<ConversionItem>> of(project.getBasePath(), EventType.REVERT_EVENT,
                    defectItem.getConversionId(), (message) -> refreshConversionListAndTable(message)));
        if (result.getCode() != 0) {
            LOG.info("HMS convert defectItem failed. {}", result.getMessage());
        }
        LOG.info("HMS revert defectItem finished.");
    }

    public void asyncRevertDefectItem(final DefectItem defectItem) {
        Runnable asyncRevert = (() -> {
            UiUtil.setStatusBarInfo(project, "");
            revertDefectItem(defectItem);
            UiUtil.setStatusBarInfo(project, "Revert OK!");
        });

        ApplicationManager.getApplication().invokeAndWait(asyncRevert, ModalityState.NON_MODAL);
    }

    private void showDiff() {
        try {
            DefectItem defectItem = resultTable.getSelectedObject();
            if (null == defectItem) {
                LOG.warn("showDiff: defectItem is null!");
                return;
            }

            if (ConvertType.MANUAL.equals(defectItem.getConvertType())) {
                return;
            }

            String file = defectItem.getFile();
            String fileName;
            if (file.contains(Constant.SEPARATOR)) {
                fileName = file.substring(file.lastIndexOf(Constant.SEPARATOR) + 1);
            } else {
                fileName = file;
            }

            final String defectFilePath = inspectPath + File.separatorChar + file;
            final DiffContent defectFileContent =
                DiffContentFactory.getInstance().create(FileUtil.readToFormatString(defectFilePath, Constant.UTF8));

            String fixFilePath = Constant.PLUGIN_CACHE_PATH + repoID + File.separatorChar + FixbotConstants.FIXBOT_DIR
                + File.separatorChar + file;
            final DiffContent fixFileContent =
                DiffContentFactory.getInstance().create(FileUtil.readToFormatString(fixFilePath, Constant.UTF8));

            DiffRequest diffRequest = new SimpleDiffRequest(Constant.PLUGIN_NAME, defectFileContent, fixFileContent,
                "Original File - " + fileName, "Fix File - " + fileName);

            UIUtil.invokeLaterIfNeeded(() -> {
                diffRequest.putUserData(HmsConvertorDiffUserDataKeys.DEFECT, defectItem);
                DiffManager.getInstance().showDiff(project, diffRequest);
            });
        } catch (IOException e) {
            LOG.warn(e.getMessage(), e);
            BalloonNotifications.showErrorNotification(e.getMessage(), project, Constant.PLUGIN_NAME, true);
        }
    }

    public void postProcessingAfterConvert(Document document) {
        FileDocumentManager.getInstance().saveDocument(document);
        convertedTotalCount++;
    }

    public void postProcessingAfterRevert(Document document) {
        FileDocumentManager.getInstance().saveDocument(document);
        convertedTotalCount--;
    }

    private void filterAndRefresh() {
        ApplicationManager.getApplication().invokeLater(() -> {
            List<DefectItem> filteredDefectItems = getFilteredDefectItems();
            resultTableModel.setItems(filteredDefectItems);

            autoConvertFilteredCount = (int) filteredDefectItems.stream()
                .filter(defectItem -> ConvertType.AUTO.equals(defectItem.getConvertType()))
                .count();
            confirmedFilteredCount =
                (int) filteredDefectItems.stream().filter(defectItem -> defectItem.isConfirmed()).count();
            if ((confirmedFilteredCount == autoConvertFilteredCount) && (autoConvertFilteredCount > 0)) {
                isConfirmedAllFiltered = true;
            } else {
                isConfirmedAllFiltered = false;
            }
            resultTable.getTableHeader().repaint();

            convertedTotalCount = Math.max(convertedTotalCount, 0);
            setConvertedTotalCount(convertedTotalCount);
        });
    }

    private List<DefectItem> defectToConvertItem(List<ConversionItem> items) {
        List<DefectItem> defectItems = new ArrayList<>();
        items.forEach(item -> defectItems.add(new DefectItem(item)));
        return defectItems;
    }

    private List<DefectItem> getFilteredDefectItems() {
        String fileName = getFilter(fileComboBox);
        String kitName = getFilter(kitNameComboBox);
        boolean converted = showConvertedCheckBox.isSelected();
        List<ConversionItem> conversionItems =
            ConversionCacheService.getInstance().queryConversions(project.getBasePath(), fileName, kitName, converted);
        coversionItem2defectItem(conversionItems);
        return defectItemList;
    }

    private String getFilter(JComboBox comboBox) {
        return String.valueOf(comboBox.getSelectedItem());
    }

    private void createPopupMenu() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.addSeparator();
        actionGroup.add(new ConfirmAction());
        actionGroup.add(new UnconfirmAction());
        actionGroup.addSeparator();
        actionGroup.add(new SetToConvertedAction());
        actionGroup.add(new SetToUnConvertedAction());
        actionGroup.addSeparator();

        PopupHandler.installPopupHandler(resultTable, actionGroup, ActionPlaces.UNKNOWN, ActionManager.getInstance());
    }

    private void confirmDefects(boolean isToConfirm) {
        List<DefectItem> defectItems = resultTable.getSelectedObjects();
        for (DefectItem defectItem : defectItems) {
            if (ConvertType.AUTO.equals(defectItem.getConvertType()) && (defectItem.isConfirmed() != isToConfirm)) {
                defectItem.setConfirmed(isToConfirm);
                confirmedTotalCount = isToConfirm ? (confirmedTotalCount + 1) : (confirmedTotalCount - 1);
                confirmedFilteredCount = isToConfirm ? (confirmedFilteredCount + 1) : (confirmedFilteredCount - 1);
            }
        }

        if ((confirmedFilteredCount == autoConvertFilteredCount) && (autoConvertFilteredCount > 0)) {
            isConfirmedAllFiltered = true;
        } else {
            isConfirmedAllFiltered = false;
        }
        resultTable.getTableHeader().repaint();
    }

    private boolean isConfirmEnabled(boolean isToConfirm) {
        boolean isEnabled = false;

        List<DefectItem> defectItems = resultTable.getSelectedObjects();
        for (DefectItem defectItem : defectItems) {
            if (ConvertType.MANUAL.equals(defectItem.getConvertType())) {
                isEnabled = false;
                break;
            }

            if (defectItem.isConfirmed() != isToConfirm) {
                isEnabled = true;
            }
        }

        return isEnabled;
    }

    private void traceConvertOperation(String projectPath, int convertedTotalCount) {
        long totalNum = totalCount;
        long currentProcessNum = convertedTotalCount - Long.parseLong(convertedNumberLabel.getText());
        if (currentProcessNum == 0) {
            return;
        }
        boolean isPatchProcess = currentProcessNum > 1;
        Long timeCost = System.currentTimeMillis() - Long.parseLong(ConfigCacheService.getInstance()
            .getProjectConfig(project.getBasePath(), ConfigKeyConstants.NEW_CONVERSION_BEGIN_TIME, String.class, null));
        double processRate = convertedTotalCount * 1.0 / totalCount;
        ConversionOperationBean data = ConversionOperationBean.builder()
            .totalNum(String.valueOf(totalNum))
            .processedNum(String.valueOf(convertedTotalCount))
            .currentProcessNum(String.valueOf(currentProcessNum))
            .isBatchProcess(isPatchProcess)
            .timeCost(String.valueOf(timeCost))
            .processRate(new DecimalFormat(ProjectConstants.Common.PERCENT).format(processRate))
            .build();
        BIReportService.getInstance().traceConversionOperation(projectPath, data);
    }

    private void setToConverted(boolean isSetToConverted) {
        List<DefectItem> defectItems = resultTable.getSelectedObjects();
        List<String> selectItems = new ArrayList<>();
        for (DefectItem defectItem : defectItems) {
            if (defectItem.isConverted() != isSetToConverted) {
                defectItem.setConverted(isSetToConverted);
                selectItems.add(defectItem.getConversionId());
            }
        }
        setManualItem(isSetToConverted, selectItems);
    }

    private void setManualItem(boolean isSetToConverted, List<String> selectItems) {
        EventType type;
        if (isSetToConverted) {
            type = EventType.CONVERT_EVENT;
        } else {
            type = EventType.REVERT_EVENT;
        }

        Result result = EventService.getInstance()
            .submitProjectEvent(ProjectEvent.<List<String>, List<ConversionItem>> of(project.getBasePath(), type,
                selectItems, (message) -> refreshConversionListAndTable(message)));
        if (result.getCode() != 0) {
            LOG.info("HMS convert defectItem failed. {}", selectItems);
        }
    }

    private boolean isSetToConvertedEnabled(boolean isSetToConverted) {
        boolean isEnabled = false;

        List<DefectItem> defectItems = resultTable.getSelectedObjects();
        for (DefectItem defectItem : defectItems) {
            if (ConvertType.AUTO.equals(defectItem.getConvertType())) {
                isEnabled = false;
                break;
            }

            if (defectItem.isConverted() != isSetToConverted) {
                isEnabled = true;
            }
        }

        return isEnabled;
    }

    private void setConvertedTotalCount(int convertedTotalCount) {
        if (convertedTotalCount < totalCount) {
            convertedNumberLabel.setForeground(JBColor.RED);
        } else {
            convertedNumberLabel.setForeground(JBColor.BLACK);
        }
        // bi report action: trace convert operation.
        traceConvertOperation(project.getBasePath(), convertedTotalCount);
        convertedNumberLabel.setText(String.valueOf(convertedTotalCount));
    }

    private static class LayoutConfigeration {
        private GridBagLayout gridBagLayout;

        private GridBagConstraints gridBagConstraints;

        private int column;

        private int weightx;

        public LayoutConfigeration() {
        }

        public void setParams(GridBagLayout gridBagLayout, GridBagConstraints gridBagConstraints, int column,
            int weightx) {
            this.gridBagLayout = gridBagLayout;
            this.gridBagConstraints = gridBagConstraints;
            this.column = column;
            this.weightx = weightx;
        }

        public GridBagLayout getGridBagLayout() {
            return gridBagLayout;
        }

        public GridBagConstraints getGridBagConstraints() {
            return gridBagConstraints;
        }

        public int getColunm() {
            return column;
        }

        public int getWeigthx() {
            return weightx;
        }
    }

    private static class DefectStartLineComparator implements Comparator<DefectItem>, Serializable {
        private static final long serialVersionUID = -6953084591915057226L;

        @Override
        public int compare(DefectItem o1, DefectItem o2) {
            if (o1.getDefectStartLine() != o2.getDefectStartLine()) {
                return Math.abs(o1.getDefectStartLine()) - Math.abs(o2.getDefectStartLine());
            } else {
                return Math.abs(o1.getFixStartLine()) - Math.abs(o2.getFixStartLine());
            }
        }
    }

    private static class ComboBoxKeyAdapter extends KeyAdapter {
        public ComboBoxKeyAdapter() {
        }

        /**
         * Invoked when a key has been pressed.
         */
        @Override
        public void keyPressed(KeyEvent e) {
            char keyChar = e.getKeyChar();
            if (Character.isDefined(keyChar)) {
                return;
            }

            Component component = e.getComponent();
            if ((component != null) && (component instanceof JComboBox)) {
                JComboBox comboBox = (JComboBox) component;

                if (comboBox.isPopupVisible()) {
                    comboBox.hidePopup();
                }
                e.consume();
            }
        }
    }

    private class HeaderRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 8151103041655612451L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
            // Restore Default Status
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if ((ResultTableModel.DEFECT_CONTENT_COLUMN_INDEX == column)
                || (ResultTableModel.DESCRIPTION_COLUMN_INDEX == column)) {
                table.getTableHeader().setCursor(Cursor.getDefaultCursor());
            } else {
                table.getTableHeader().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            if (ResultTableModel.CONFIRM_COLUMN_INDEX == column) {
                setIcon(IconUtil.getConfirmIcon(isConfirmedAllFiltered, enableConvert));
            } else {
                setIcon(null);
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createLineBorder(JBColor.GRAY));

            return this;
        }
    }

    private class ConfirmAction extends AnAction {
        public ConfirmAction() {
            super(HmsConvertorBundle.message("select"));
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            confirmDefects(true);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            boolean isEnabled = isConfirmEnabled(true);
            if (null != e) {
                e.getPresentation().setEnabled(isEnabled);
            }
        }
    }

    private class UnconfirmAction extends AnAction {
        public UnconfirmAction() {
            super(HmsConvertorBundle.message("unselect"));
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            confirmDefects(false);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            boolean isEnabled = isConfirmEnabled(false);
            if (null != e) {
                e.getPresentation().setEnabled(isEnabled);
            }
        }
    }

    private class SetToConvertedAction extends AnAction {
        public SetToConvertedAction() {
            super("Set to converted");
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            setToConverted(true);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            boolean isEnabled = isSetToConvertedEnabled(true);
            if (null != e) {
                e.getPresentation().setEnabled(isEnabled);
            }
        }
    }

    private class SetToUnConvertedAction extends AnAction {
        public SetToUnConvertedAction() {
            super("Set to unconverted");
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            setToConverted(false);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            boolean isEnabled = isSetToConvertedEnabled(false);
            if (null != e) {
                e.getPresentation().setEnabled(isEnabled);
            }
        }
    }
}
