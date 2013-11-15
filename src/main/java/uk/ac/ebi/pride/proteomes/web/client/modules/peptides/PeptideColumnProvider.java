package uk.ac.ebi.pride.proteomes.web.client.modules.peptides;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 12/11/13
 *         Time: 15:44
 */
public class PeptideColumnProvider {
    public static List<Column<PeptideMatch, ?>> getSortingColumns
            (ColumnSortEvent.ListHandler<PeptideMatch> sorter) {

        List<Column<PeptideMatch, ?>> columns = new
                ArrayList<Column<PeptideMatch, ?>>();

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
                return object.getPosition().toString();
            }
        };

        siteColumn.setSortable(true);
        sorter.setComparator(siteColumn, new Comparator<PeptideMatch>() {
            @Override
            public int compare(PeptideMatch o1, PeptideMatch o2) {
                return o1.getPosition().compareTo(o2.getPosition());
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
        return columns;
    }

}
