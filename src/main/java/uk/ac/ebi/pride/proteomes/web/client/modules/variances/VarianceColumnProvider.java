package uk.ac.ebi.pride.proteomes.web.client.modules.variances;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ProvidesKey;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModifiedLocation;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;

import java.util.*;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 29/11/13
 *         Time: 14:46
 */
public class VarianceColumnProvider {
    /**
     * The key provider that allows us to identify Contacts even if a field
     * changes. We identify contacts by their unique ID.
     */
    public static final ProvidesKey<Peptide> KEY_PROVIDER =
            new ProvidesKey<Peptide>() {
                @Override
                public Object getKey(Peptide item) {
                    return item.getSequence();
                }
            };

    public static List<Column<Peptide, ?>> getSortingColumns(ColumnSortEvent.ListHandler<Peptide> sorter) {

        List<Column<Peptide, ?>> columns = new ArrayList<Column<Peptide, ?>>();

        TextColumn<Peptide> sequenceColumn = new TextColumn<Peptide>() {
            @Override
            public String getValue(Peptide object) {
                return object.getSequence();
            }
        };

        sequenceColumn.setSortable(true);
        sorter.setComparator(sequenceColumn, new Comparator<Peptide>() {
            @Override
            public int compare(Peptide o1, Peptide o2) {
                return o1.getSequence().compareTo(o2.getSequence());
            }
        });

        // Column that shows the tissues the peptide has been seen in.
        TextColumn<Peptide> tissuesColumn = new TextColumn<Peptide>() {
            @Override
            public String getValue(Peptide object) {
                StringBuilder sb = new StringBuilder();
                for(String tissue : object.getTissues()) {
                    sb.append(tissue).append(", ");
                }
                return sb.length() == 0 ? "None" : sb.substring(0, sb.length() - 2);
            }
        };

        tissuesColumn.setSortable(true);
        sorter.setComparator(tissuesColumn, new Comparator<Peptide>() {
            @Override
            public int compare(Peptide o1, Peptide o2) {
                StringBuilder sb1 = new StringBuilder();
                StringBuilder sb2 = new StringBuilder();
                for(String tissue : o1.getTissues()) {
                    sb1.append(tissue);
                }
                for(String tissue : o2.getTissues()) {
                    sb2.append(tissue);
                }
                return sb1.toString().compareTo(sb2.toString());
            }
        });

        // Column that shows the modifications the peptide has.
        TextColumn<Peptide> modsColumn = new TextColumn<Peptide>() {
            @Override
            public String getValue(Peptide object) {
                Set<String> modSet = new HashSet<String>();
                for(ModifiedLocation modLoc : object.getModifiedLocations()) {
                    modSet.add(modLoc.getModification());
                }
                StringBuilder sb = new StringBuilder();
                for(String mod : modSet) {
                    sb.append(mod).append(", ");
                }
                return sb.length() == 0 ? "None" : sb.substring(0, sb.length() - 2);
            }
        };

        tissuesColumn.setSortable(true);
        sorter.setComparator(tissuesColumn, new Comparator<Peptide>() {
            @Override
            public int compare(Peptide o1, Peptide o2) {
                if(o1.getModifiedLocations().size() !=
                        o2.getModifiedLocations().size()) {
                    return new Integer(o1.getModifiedLocations().size())
                            .compareTo(o2.getModifiedLocations().size());
                }
                else {
                    StringBuilder sb1 = new StringBuilder();
                    StringBuilder sb2 = new StringBuilder();
                    for(String tissue : o1.getTissues()) {
                        sb1.append(tissue);
                    }
                    for(String tissue : o2.getTissues()) {
                        sb2.append(tissue);
                    }
                    return sb1.toString().compareTo(sb2.toString());
                }
            }
        });

        columns.add(sequenceColumn);
        columns.add(modsColumn);
        columns.add(tissuesColumn);
        return columns;
    }

    public static List<String> getColumnTitles() {
        List<String> titles = new ArrayList<String>();
        Collections.addAll(titles, "Sequence", "Modifications", "Tissues");
        return titles;
    }

    public static List<String> getColumnWidths() {
        List<String> widths = new ArrayList<String>();
        Collections.addAll(widths, "20%", "40%", "40%");
        return widths;
    }
}
