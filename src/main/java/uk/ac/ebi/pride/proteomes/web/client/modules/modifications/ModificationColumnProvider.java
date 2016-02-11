package uk.ac.ebi.pride.proteomes.web.client.modules.modifications;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
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

            TextColumn<ModificationWithPosition> nameColumn = new TextColumn<ModificationWithPosition>() {
                @Override
                public String getValue(ModificationWithPosition object) {
                    if(object != null) {
                        return object.getModification();

                    }
                    return "PEPE";
                }
            };

            nameColumn.setSortable(true);
            sorter.setComparator(nameColumn, new Comparator<ModificationWithPosition>() {
                @Override
                public int compare(ModificationWithPosition o1, ModificationWithPosition o2) {
                    return o1.getModification().compareTo(o2.getModification());
                }
            });

            TextColumn<ModificationWithPosition> positionColumn = new TextColumn<ModificationWithPosition>() {
                @Override
                public String getValue(ModificationWithPosition object) {
                    return String.valueOf(object.getPosition());
                }
            };
            positionColumn.setSortable(true);
            sorter.setComparator(positionColumn, new Comparator<ModificationWithPosition>() {
                @Override
                public int compare(ModificationWithPosition o1, ModificationWithPosition o2) {
                    return new Integer(o1.getPosition()).compareTo(o2.getPosition());
                }
            });

            columns.add(nameColumn);
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
