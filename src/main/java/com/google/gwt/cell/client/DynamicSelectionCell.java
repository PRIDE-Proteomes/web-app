package com.google.gwt.cell.client;

/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import java.util.List;
import java.util.TreeMap;

public class DynamicSelectionCell extends AbstractInputCell<String, String> {

    interface Template extends SafeHtmlTemplates {
        @Template("<option value=\"{0}\">{0}</option>")
        SafeHtml deselected(String option);

        @Template("<option value=\"{0}\" selected=\"selected\">{0}</option>")
        SafeHtml selected(String option);
    }

    private static Template template;

    /**
     *  key: rowIndex
     *  value: List of options to show for this row
     */
    public TreeMap<Integer, List<String>> optionsMap = new TreeMap<Integer, List<String>>();

    /**
     * Construct a new {@link SelectionCell} with the specified options.
     *
     */
    public DynamicSelectionCell() {
        super("change");
        if (template == null) {
            template = GWT.create(Template.class);
        }
    }

    public void addOptions(List<String> newOps, int rowIndex) {
        optionsMap.put(rowIndex, newOps);
    }

    public void removeOptions(int rowIndex) {
        optionsMap.remove(rowIndex);
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, String value,
                               NativeEvent event, ValueUpdater<String> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        String type = event.getType();
        if ("change".equals(type)) {
            Object key = context.getKey();
            SelectElement select = parent.getFirstChild().cast();
            String newValue = optionsMap.get(context.getIndex()).get(select.getSelectedIndex());
            setViewData(key, newValue);
            finishEditing(parent, newValue, key, valueUpdater);
            if (valueUpdater != null) {
                valueUpdater.update(newValue);
            }
        }
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb) {
        // Get the view data.
        Object key = context.getKey();
        String viewData = getViewData(key);
        if (viewData != null && viewData.equals(value)) {
            clearViewData(key);
            viewData = null;
        }

        int selectedIndex = getSelectedIndex(viewData == null ? value : viewData, context.getIndex());
        sb.appendHtmlConstant("<select tabindex=\"-1\">");
        int index = 0;
        try {
            for (String option : optionsMap.get(context.getIndex())) {
                if (index++ == selectedIndex) {
                    sb.append(template.selected(option));
                } else {
                    sb.append(template.deselected(option));
                }
            }
        } catch (Exception e) {
            System.out.println("error");
        }
        sb.appendHtmlConstant("</select>");
    }

    private int getSelectedIndex(String value, int rowIndex) {
        if (optionsMap.get(rowIndex) == null) {
            return -1;
        }
        return optionsMap.get(rowIndex).indexOf(value);
    }
}