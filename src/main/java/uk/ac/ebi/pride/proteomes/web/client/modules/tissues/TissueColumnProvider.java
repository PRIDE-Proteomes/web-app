package uk.ac.ebi.pride.proteomes.web.client.modules.tissues;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ProvidesKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 22/11/13
 *         Time: 15:33
 */
public class TissueColumnProvider {
    public static List<Column<String, ?>> getSortingColumns
            (ColumnSortEvent.ListHandler<String> sorter) {
        List<Column<String, ?>> columns = new ArrayList<Column<String, ?>>();

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
        List<String> titles = new ArrayList<String>();
        Collections.addAll(titles, "Name");
        return titles;
    }

    public static List<String> getColumnWidths() {
        List<String> widths = new ArrayList<String>();
        Collections.addAll(widths, "100%");
        return widths;
    }
}
