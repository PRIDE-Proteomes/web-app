package uk.ac.ebi.pride.proteomes.web.client.modules.peptides;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModifiedLocation;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListSorter;

import java.util.*;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 12/11/13
 *         Time: 15:44
 */
class PeptideColumnProvider {
    public static List<Column<PeptideMatch, ?>> getSortingColumns
            (ListSorter<PeptideMatch> sorter) {

        List<Column<PeptideMatch, ?>> columns = new ArrayList<>();

        TextColumn<PeptideMatch> sequenceColumn = new TextColumn<PeptideMatch>() {
            @Override
            public String getValue(PeptideMatch object) {
                return object.getSequence();
            }
        };

        sequenceColumn.setSortable(true);
        sorter.setComparator(sequenceColumn, new Comparator<PeptideMatch>() {
            @Override
            public int compare(PeptideMatch o1, PeptideMatch o2) {
                return o1.getSequence().compareTo(o2.getSequence());
            }
        });

        // site is a name for "starting position in protein"
        TextColumn<PeptideMatch> siteColumn = new TextColumn<PeptideMatch>() {
            @Override
            public String getValue(PeptideMatch object) {
                return object.getPosition() + "-" + (object.getPosition() + object.getSequence().length() - 1);
            }
        };

        siteColumn.setSortable(true);
        sorter.setComparator(siteColumn, new Comparator<PeptideMatch>() {
            @Override
            public int compare(PeptideMatch o1, PeptideMatch o2) {
                if(o1.getPosition().compareTo(o2.getPosition()) == 0) {
                    return ((Integer) o1.getSequence().length()).compareTo(o2.getSequence().length());
                }
                return o1.getPosition().compareTo(o2.getPosition());
            }
        });

        // Column that shows the tissues the peptide has been seen in.
        TextColumn<PeptideMatch> tissuesColumn = new TextColumn<PeptideMatch>() {
            @Override
            public String getValue(PeptideMatch object) {
                StringBuilder sb = new StringBuilder();
                for(String tissue : object.getTissues()) {
                    sb.append(tissue).append(", ");
                }
                return sb.length() == 0 ? "Not Available" : sb.substring(0, sb.length() - 2);
            }
        };

        tissuesColumn.setSortable(true);
        sorter.setComparator(tissuesColumn, new Comparator<PeptideMatch>() {
            @Override
            public int compare(PeptideMatch o1, PeptideMatch o2) {
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
        TextColumn<PeptideMatch> modsColumn = new TextColumn<PeptideMatch>() {
            @Override
            public String getValue(PeptideMatch object) {
                Set<String> modSet = new HashSet<>();
                for(ModifiedLocation modLoc : object.getModifiedLocations()) {
                    modSet.add(modLoc.getModification());
                }
                StringBuilder sb = new StringBuilder();
                for(String mod : modSet) {
                    sb.append(mod).append(", ");
                }
                return sb.length() == 0 ? "Not Available" : sb.substring(0, sb.length() - 2);
            }
        };

        modsColumn.setSortable(true);
        sorter.setComparator(modsColumn, new Comparator<PeptideMatch>() {
            @Override
            public int compare(PeptideMatch o1, PeptideMatch o2) {
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

        // Column that shows an star depending on a rating given by Pride.
//        Column<PeptideMatch, String> prideQRatingColumn = new Column<PeptideMatch,
//                String>(new MedalImageCell(5, 5)) {
//            @Override
//            public String getValue(PeptideMatch object) {
//                ((MedalImageCell) getCell()).setValue(object.getPrideqRating());
//                return null;
//            }
//        };
//
//        prideQRatingColumn.setSortable(true);
//        sorter.setComparator(prideQRatingColumn, new Comparator<Peptide>() {
//            @Override
//            public int compare(Peptide o1, Peptide o2) {
//                return o1.getPrideqRating().compareTo(o2.getPrideqRating());
//            }
//        });

        columns.add(sequenceColumn);
        columns.add(siteColumn);
        columns.add(modsColumn);
        columns.add(tissuesColumn);
        return columns;
    }

    public static List<String> getColumnTitles() {
        List<String> titles = new ArrayList<>();
        Collections.addAll(titles, "Sequence", "Region", "Modifications",
                                   "Tissues");
        return titles;
    }

    public static List<String> getColumnWidths() {
        List<String> widths = new ArrayList<>();
        Collections.addAll(widths, "30%", "10%", "30%", "30%");
        return widths;
    }

    public static HasKeyboardSelectionPolicy.KeyboardSelectionPolicy getKeyboardSelectionPolicy() {
        return HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED;
    }
}
