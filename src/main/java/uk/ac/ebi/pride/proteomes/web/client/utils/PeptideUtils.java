package uk.ac.ebi.pride.proteomes.web.client.utils;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 20/11/13
 *         Time: 15:23
 */
public class PeptideUtils {
    /**
     * Filters the peptide matches outside a region
     * @param peptides the peptide matches to filter
     * @param start the start point of the region
     * @param end the end point of the region
     * @return a new list containing only the peptide matches inside the region
     */
    static public List<PeptideMatch> filterPeptidesNotIn(List<PeptideMatch> peptides, int start, int end) {
        List<PeptideMatch> filteredList = new ArrayList<PeptideMatch>();

        for(PeptideMatch peptide : peptides) {
            if(inRange(peptide, start, end)) {
                filteredList.add(peptide);
            }
        }

        return filteredList;
    }

    static public int firstIndexOf(List<PeptideMatch> peptides, String sequence) {
        int index = -1;

        for(int i = 0; i < peptides.size(); i++) {
            if(peptides.get(i).getSequence().equals(sequence)) {
                index = i;
                break;
            }
        }

        return index;
    }

    public static boolean inRange(PeptideMatch peptide, int start, int end) {
        return !(peptide.getPosition() < start ||
                peptide.getSequence().length() + peptide.getPosition() - 1 > end);
    }
}
