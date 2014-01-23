package uk.ac.ebi.pride.proteomes.web.client.modules.modifications;

import com.google.common.collect.Multiset;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListSorter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 26/11/13
 *         Time: 10:53
 */
public class ModificationColumnProvider {
    public static List<Column<Multiset.Entry<String>, ?>> getSortingColumns
            (ListSorter<Multiset.Entry<String>> sorter) {
        List<Column<Multiset.Entry<String>, ?>> columns = new ArrayList<>();

        TextColumn<Multiset.Entry<String>> nameColumn = new TextColumn<Multiset.Entry<String>>() {
            @Override
            public String getValue(Multiset.Entry<String> object) {
                return object.getElement();
            }
        };

        nameColumn.setSortable(true);
        sorter.setComparator(nameColumn, new Comparator<Multiset.Entry<String>>() {
            @Override
            public int compare(Multiset.Entry<String> o1, Multiset.Entry<String> o2) {
                return o1.getElement().compareTo(o2.getElement());
            }
        });

        TextColumn<Multiset.Entry<String>> countColumn = new TextColumn<Multiset.Entry<String>>() {
            @Override
            public String getValue(Multiset.Entry<String> object) {
                return String.valueOf(object.getCount());
            }
        };

        countColumn.setSortable(true);
        sorter.setComparator(countColumn, new Comparator<Multiset.Entry<String>>() {
            @Override
            public int compare(Multiset.Entry<String> o1, Multiset.Entry<String> o2) {
                return new Integer(o1.getCount()).compareTo(o2.getCount());
            }
        });

        columns.add(nameColumn);

        return columns;
    }

    public static List<String> getColumnTitles() {
        List<String> titles = new ArrayList<>();
        Collections.addAll(titles, "Name", "Count");
        return titles;
    }

    public static List<String> getColumnWidths() {
        List<String> widths = new ArrayList<>();
        Collections.addAll(widths, "70%", "30%");
        return widths;
    }

    public static HasKeyboardSelectionPolicy.KeyboardSelectionPolicy getKeyboardSelectionPolicy() {
        return HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED;
    }
}
