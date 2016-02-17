package uk.ac.ebi.pride.proteomes.web.client.modules.modifications;

import com.google.gwt.cell.client.DynamicSelectionCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.ModificationWithPosition;
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
class ModificationColumnProvider {

    public static List<Column<ModificationWithPosition, ?>> getSortingColumns(ListSorter<ModificationWithPosition> sorter) {
        List<Column<ModificationWithPosition, ?>> columns = new ArrayList<>();

        Column<ModificationWithPosition, String> nameColumn = new  Column<ModificationWithPosition, String>(new TextCell()) {
            @Override
            public String getValue(ModificationWithPosition object) {
                return object.getModification();
            }
        };

        nameColumn.setSortable(false); //the indexes of the DynamicSelectionCell are not updated accordingly
        sorter.setComparator(nameColumn, new Comparator<ModificationWithPosition>() {
            @Override
            public int compare(ModificationWithPosition o1, ModificationWithPosition o2) {
                return o1.getModification().compareTo(o2.getModification());
            }
        });

        columns.add(nameColumn);

        Column<ModificationWithPosition, String> positionColumn = new Column<ModificationWithPosition, String>(new DynamicSelectionCell()) {
            @Override
            public String getValue(ModificationWithPosition object) {
                return String.valueOf("All");
            }
        };
        positionColumn.setFieldUpdater(new FieldUpdater<ModificationWithPosition, String>() {
            @Override
            public void update(int index, ModificationWithPosition object, String value) {
                if (value!= null && !value.isEmpty() && !value.equals("All")) {
                    object.setPosition(Integer.parseInt(value));
                } else {
                    object.setPosition(null);
                }
            }
        });

        positionColumn.setSortable(false);

        columns.add(positionColumn);

        return columns;
    }


    public static List<String> getColumnTitles() {
        List<String> titles = new ArrayList<>();
        Collections.addAll(titles, "Name", "Position");
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
