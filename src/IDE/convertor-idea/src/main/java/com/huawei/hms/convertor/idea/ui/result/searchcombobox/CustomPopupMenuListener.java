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

import com.intellij.ui.components.JBScrollPane;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboPopup;

/**
 * Custom the style of the JComboBox popup menu
 *
 * @since 2019-08-06
 */
public class CustomPopupMenuListener implements PopupMenuListener {
    private static final int MARGIN_WIDTH = 5;

    private boolean scrollBarRequired;

    private boolean popupWider;

    private int maximumWidth;

    private JScrollPane scrollPane;

    /**
     * Constructor to change popup style
     */
    public CustomPopupMenuListener() {
        this.scrollBarRequired = true;
        this.popupWider = true;
        this.maximumWidth = -1;
        this.scrollPane = new JBScrollPane();
    }

    /**
     * Before visible, change the bounds of the popup
     *
     * @param e PopupMenuEvent
     */
    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        Object source = e.getSource();
        if (source instanceof JComboBox) {
            JComboBox comboBox = (JComboBox) source;
            if (comboBox.getItemCount() == 0) {
                return;
            }

            final Object child = comboBox.getAccessibleContext().getAccessibleChild(0);
            if (child instanceof BasicComboPopup) {
                SwingUtilities.invokeLater(() -> {
                    changePopupStyle((BasicComboPopup) child);
                });
            }
        }
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        if (scrollPane != null) {
            scrollPane.setHorizontalScrollBar(null);
        }
    }

    /**
     * Change the style of the popup
     *
     * @param popup Popup of the comboBox
     */
    protected void changePopupStyle(BasicComboPopup popup) {
        scrollPane = getScrollPane(popup);
        if (popupWider) {
            popupWider(popup);
        }
        setHorizontalScrollBar(popup);

        Component comboBox = popup.getInvoker();
        int height = comboBox.getPreferredSize().height;
        Point location = comboBox.getLocationOnScreen();
        popup.setLocation(location.x, location.y + height - 1);
        popup.setLocation(location.x, location.y + height);
    }

    private void popupWider(BasicComboPopup popup) {
        JList list = popup.getList();
        int popupWidth = list.getPreferredSize().width + MARGIN_WIDTH + getScrollBarWidth(popup, scrollPane);
        Dimension scrollPaneSize = scrollPane.getPreferredSize();
        if (maximumWidth != -1) {
            popupWidth = Math.min(popupWidth, maximumWidth);
        }
        popupWidth = Math.max(popupWidth, scrollPaneSize.width);
        scrollPaneSize.width = popupWidth;
        scrollPane.setMaximumSize(scrollPaneSize);
        scrollPane.setPreferredSize(scrollPaneSize);
    }

    private void setHorizontalScrollBar(BasicComboPopup popup) {
        JViewport viewport = scrollPane.getViewport();
        Point point = viewport.getViewPosition();
        point.x = 0;
        viewport.setViewPosition(point);
        if (!scrollBarRequired) {
            scrollPane.setHorizontalScrollBar(null);
            return;
        }

        JScrollBar horizontal = scrollPane.getHorizontalScrollBar();
        if (horizontal == null) {
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            horizontal = new JScrollBar(JScrollBar.HORIZONTAL);
            scrollPane.setHorizontalScrollBar(horizontal);
        }
        if (horizontalScrollBarWillBeVisible(popup, scrollPane)) {
            Dimension scrollPaneSize = scrollPane.getPreferredSize();
            scrollPaneSize.height += horizontal.getPreferredSize().height;
            scrollPane.setMaximumSize(scrollPaneSize);
            scrollPane.setPreferredSize(scrollPaneSize);
            scrollPane.revalidate();
        }
    }

    private JScrollPane getScrollPane(BasicComboPopup popup) {
        JList list = popup.getList();
        Container container = SwingUtilities.getAncestorOfClass(JScrollPane.class, list);

        if (container instanceof JScrollPane) {
            return (JScrollPane) container;
        }
        return new JBScrollPane();
    }

    private int getScrollBarWidth(BasicComboPopup popup, JScrollPane scrollPane) {
        int scrollBarWidth = 0;
        if (popup.getInvoker() instanceof JComboBox) {
            JComboBox comboBox = (JComboBox) popup.getInvoker();

            if (comboBox.getItemCount() > comboBox.getMaximumRowCount()) {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                scrollBarWidth = vertical.getPreferredSize().width;
            }
        }
        return scrollBarWidth;
    }

    private boolean horizontalScrollBarWillBeVisible(BasicComboPopup popup, JScrollPane scrollPane) {
        JList list = popup.getList();
        int scrollBarWidth = getScrollBarWidth(popup, scrollPane);
        int popupWidth = list.getPreferredSize().width + scrollBarWidth;

        return popupWidth > scrollPane.getPreferredSize().width;
    }
}
