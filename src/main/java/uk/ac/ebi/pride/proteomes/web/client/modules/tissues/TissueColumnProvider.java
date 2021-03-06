package uk.ac.ebi.pride.proteomes.web.client.modules.tissues;

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
 *         Date: 22/11/13
 *         Time: 15:33
 */
class TissueColumnProvider {
    public static List<Column<String, ?>> getSortingColumns(ListSorter<String> sorter) {
        List<Column<String, ?>> columns = new ArrayList<>();

        TextColumn<String> nameColumn = new TextColumn<String>() {
            @Override
            public String getValue(String object) {
                return object;
            }
        };

        nameColumn.setSortable(true);
        sorter.setComparator(nameColumn, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        columns.add(nameColumn);

        return columns;
    }

    public static List<String> getColumnTitles() {
        List<String> titles = new ArrayList<>();
        Collections.addAll(titles, "Name");
        return titles;
    }

    public static List<String> getColumnWidths() {
        List<String> widths = new ArrayList<>();
        Collections.addAll(widths, "100%");
        return widths;
    }

    public static HasKeyboardSelectionPolicy.KeyboardSelectionPolicy getKeyboardSelectionPolicy() {
        return HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED;
    }
}
