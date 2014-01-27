package uk.ac.ebi.pride.proteomes.web.client.modules.variances;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModifiedLocation;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListSorter;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Comparator;
import java.util.Collections;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 29/11/13
 *         Time: 14:46
 */
class VarianceColumnProvider {
    public static List<Column<Peptide, ?>> getSortingColumns(ListSorter<Peptide> sorter) {
        List<Column<Peptide, ?>> columns = new ArrayList<>();

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
                return sb.length() == 0 ? "N/A" : sb.substring(0, sb.length() - 2);
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
                Set<String> modSet = new HashSet<>();
                for(ModifiedLocation modLoc : object.getModifiedLocations()) {
                    modSet.add(modLoc.getPosition() + ":" + modLoc.getModification());
                }
                StringBuilder sb = new StringBuilder();
                for(String mod : modSet) {
                    sb.append(mod).append(", ");
                }
                return sb.length() == 0 ? "N/A" : sb.substring(0, sb.length() - 2);
            }
        };

        modsColumn.setSortable(true);
        sorter.setComparator(modsColumn, new Comparator<Peptide>() {
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

        // Column that shows the assays the peptide has been seen in.
        Column<Peptide, SafeHtml> multiAssayLinkColumn = new Column<Peptide, SafeHtml>(new SafeHtmlCell()) {
           @Override
           public SafeHtml getValue(Peptide obj)
           {
               // create a direct link(s) to PRIDE Archive for the assay(s)
               FlowPanel panel = new FlowPanel();
               for (String assayId : obj.getAssays()) {
                   panel.add(new Anchor(assayId, "http://wwwdev.ebi.ac.uk/pride/archive/assays/" + assayId, "_blank"));
                   if(!assayId.equals(obj.getAssays().get(obj.getAssays().size() - 1))) {
                       panel.add(new InlineLabel(" "));
                   }
               }
               return SafeHtmlUtils.fromSafeConstant(panel.toString());
           }
        };

        multiAssayLinkColumn.setSortable(true);
        sorter.setComparator(multiAssayLinkColumn, new Comparator<Peptide>() {
            @Override
            public int compare(Peptide o1, Peptide o2) {
                StringBuilder sb1 = new StringBuilder();
                StringBuilder sb2 = new StringBuilder();
                for(String assay : o1.getAssays()) {
                    sb1.append(assay);
                }
                for(String assay : o2.getAssays()) {
                    sb2.append(assay);
                }
                return sb1.toString().compareTo(sb2.toString());
            }
        });

        columns.add(sequenceColumn);
        columns.add(modsColumn);
        columns.add(tissuesColumn);
        columns.add(multiAssayLinkColumn);
        return columns;
    }

    public static List<String> getColumnTitles() {
        List<String> titles = new ArrayList<>();
        Collections.addAll(titles, "Sequence", "Modifications", "Tissues", "Assays");
        return titles;
    }

    public static List<String> getColumnWidths() {
        List<String> widths = new ArrayList<>();
        Collections.addAll(widths, "25%", "25%", "25%", "25%");
        return widths;
    }

    public static HasKeyboardSelectionPolicy.KeyboardSelectionPolicy getKeyboardSelectionPolicy() {
        return HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED;
    }
}
