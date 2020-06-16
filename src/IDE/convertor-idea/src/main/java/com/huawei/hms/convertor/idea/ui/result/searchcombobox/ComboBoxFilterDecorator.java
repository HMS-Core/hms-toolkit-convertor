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

package com.huawei.hms.convertor.idea.ui.result.searchcombobox;

import lombok.extern.slf4j.Slf4j;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;

/**
 * ComboBox filter decorator
 *
 * @since 2019-08-06
 */
@Slf4j
public final class ComboBoxFilterDecorator<T> {
    private Popup filterPopup;

    private JLabel filterLabel;

    private JComboBox<T> comboBox;

    private List<T> originalItems;

    private Object selectedItem;

    private TextHandler textHandler = new TextHandler();

    private ComboBoxFilterDecorator(JComboBox<T> comboBox) {
        this.comboBox = comboBox;
    }

    /**
     * Decorate comboBox filter
     *
     * @param comboBox the comboBox to decorate
     * @param <T> Generic parameter
     * @return ComboBoxFilterDecorator
     */
    public static <T> ComboBoxFilterDecorator<T> decorate(JComboBox<T> comboBox) {
        ComboBoxFilterDecorator decorator = new ComboBoxFilterDecorator(comboBox);
        decorator.init();
        return decorator;
    }

    private void init() {
        prepareComboFiltering();
        initFilterLabel();
        initComboPopupListener();
        initComboKeyListener();
    }

    private void prepareComboFiltering() {
        DefaultComboBoxModel<T> model = (DefaultComboBoxModel<T>) comboBox.getModel();
        this.originalItems = new ArrayList<>();
        for (int i = 0; i < model.getSize(); i++) {
            this.originalItems.add(model.getElementAt(i));
        }
    }

    private void initComboKeyListener() {
        comboBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showFilterPopup();
            }
        });

        comboBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                resetFilterPopup();
            }
        });

        addComboBoxLisener();
    }

    private void addComboBoxLisener() {
        comboBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char keyChar = e.getKeyChar();
                int keyCode = e.getKeyCode();
                if (keyCode != KeyEvent.VK_CAPS_LOCK && keyCode != KeyEvent.VK_SHIFT && !Character.isDefined(keyChar)) {
                    resetFilterPopup();
                    if (comboBox.isPopupVisible()) {
                        comboBox.hidePopup();
                    }
                    e.consume();
                    return;
                }

                inputFilter(keyCode, keyChar);

                if (!comboBox.isPopupVisible()) {
                    comboBox.showPopup();
                }
                if (!textHandler.text.isEmpty()) {
                    showFilterPopup();
                    performFilter();
                } else {
                    resetFilterPopup();
                }
                e.consume();
            }
        });
    }

    private void inputFilter(int keyCode, char keyChar) {
        switch (keyCode) {
            case KeyEvent.VK_DELETE:
                return;
            case KeyEvent.VK_ENTER:
                resetFilterPopup();
                return;
            case KeyEvent.VK_ESCAPE:
                if (null != selectedItem) {
                    comboBox.setSelectedItem(selectedItem);
                }
                resetFilterPopup();
                return;
            case KeyEvent.VK_BACK_SPACE:
                textHandler.removeCharAtEnd();
                break;
            case KeyEvent.VK_SPACE:
                break;
            case KeyEvent.VK_CAPS_LOCK:
                break;
            case KeyEvent.VK_SHIFT:
                break;
            default:
                textHandler.add(keyChar);
        }
    }

    private void initFilterLabel() {
        filterLabel = new CustomLabel();
        filterLabel.setOpaque(true);
        filterLabel.setFont(filterLabel.getFont().deriveFont(Font.PLAIN));
    }

    public JLabel getFilterLabel() {
        return filterLabel;
    }

    private void initComboPopupListener() {
        comboBox.addPopupMenuListener(new FilterBoundsPopupMenuListener());
    }

    private void showFilterPopup() {
        if (null == filterPopup) {
            Point point = new Point(0, 0);
            SwingUtilities.convertPointToScreen(point, comboBox);
            filterLabel.setPreferredSize(new Dimension(comboBox.getWidth(), comboBox.getHeight()));
            filterPopup = PopupFactory.getSharedInstance()
                .getPopup(comboBox, filterLabel, point.x, point.y - filterLabel.getPreferredSize().height);

            selectedItem = comboBox.getSelectedItem();
        }

        filterPopup.show();
    }

    private void resetFilterPopup() {
        if (null != filterPopup) {
            filterPopup.hide();
            filterPopup = null;
            filterLabel.setText("");
            textHandler.reset();

            // add items in the original order
            Object lastSelectedItem = comboBox.getSelectedItem();
            DefaultComboBoxModel<T> model = (DefaultComboBoxModel<T>) comboBox.getModel();
            model.removeAllElements();
            for (T item : originalItems) {
                model.addElement(item);
            }
            // preserve the selection
            model.setSelectedItem(lastSelectedItem);
            this.selectedItem = lastSelectedItem;
        }
    }

    private void hideFilterPopup() {
        if (null != filterPopup) {
            filterPopup.hide();
            filterPopup = null;
            filterLabel.setText("");
            textHandler.reset();
        }
    }

    private void performFilter() {
        filterLabel.setText(textHandler.getText());
        if (comboBox.getModel() instanceof DefaultComboBoxModel) {
            DefaultComboBoxModel<T> model = (DefaultComboBoxModel<T>) comboBox.getModel();
            model.removeAllElements();

            // add matched items
            for (T item : originalItems) {
                if (item instanceof String) {
                    String itemFile = (String) item;
                    if (itemFile.contains(textHandler.getText())) {
                        model.addElement(item);
                    }
                }
            }
        }
    }

    private static class TextHandler {
        private String text = "";

        /**
         * Add a key char
         *
         * @param keyChar press key char
         */
        public void add(char keyChar) {
            text += keyChar;
        }

        /**
         * Remove char at the end
         */
        public void removeCharAtEnd() {
            if (text.length() > 0) {
                text = text.substring(0, text.length() - 1);
            }
        }

        /**
         * Reset the input text
         */
        public void reset() {
            text = "";
        }

        /**
         * Get the input text
         *
         * @return input text
         */
        public String getText() {
            return text;
        }
    }

    private class FilterBoundsPopupMenuListener extends CustomPopupMenuListener {
        FilterBoundsPopupMenuListener() {
            super();
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            super.popupMenuWillBecomeInvisible(e);
            hideFilterPopup();
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            hideFilterPopup();
        }
    }
}
