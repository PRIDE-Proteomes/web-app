package uk.ac.ebi.pride.proteomes.web.client.utils;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.Region;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModifiedLocation;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideList;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.IllegalRegionValueException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 20/11/13
 *         Time: 15:23
 */
public class PeptideUtils {
    /**
     * Filters the peptide matches outside a region
     * @param peptideMatches the peptide matches to filter
     * @param start the start point of the region
     * @param end the end point of the region
     * @return a new list containing only the peptide matches inside the region
     */
    static public List<PeptideMatch> filterPeptideMatchesNotIn(List<PeptideMatch> peptideMatches, int start, int end) {
        List<PeptideMatch> filteredList = new ArrayList<PeptideMatch>();

        if(start == end && start == 0) {
            return peptideMatches;
        }
        for(PeptideMatch peptide : peptideMatches) {
            if(inRange(peptide, start, end)) {
                filteredList.add(peptide);
            }
        }

        return filteredList;
    }

    public static List<PeptideMatch> filterPeptidesNotInTissue(List<PeptideMatch> peptideMatches, String tissue) {
        List<PeptideMatch> filteredList;

        if(tissue.isEmpty()) {
            return peptideMatches;
        }

        filteredList = new ArrayList<PeptideMatch>();

        for(PeptideMatch peptide : peptideMatches) {
            if(peptide.getTissues().contains(tissue)) {
                filteredList.add(peptide);
            }
        }

        return filteredList;
    }

    /**
     * @param peptides
     * @param modification
     * @return A list of items that where already contained in the original
     * list
     */
    public static List<Peptide> filterPeptidesWithoutModification
    (List<Peptide> peptides, String modification) {
        List<Peptide> filteredList;

        if(modification.isEmpty()) {
            return peptides;
        }

        filteredList = new ArrayList<Peptide>();

        for(Peptide peptide : peptides) {
            for(ModifiedLocation modLoc : peptide.getModifiedLocations()) {
                if(modLoc.getModification().equals(modification)) {
                    filteredList.add(peptide);
                    break;
                }
            }
        }

        return filteredList;
    }

    /**
     * @param peptideMatches
     * @param modification
     * @return A list of items that where already contained in the original
     * list
     */
    public static List<PeptideMatch> filterPeptideMatchesWithoutModification
    (List<PeptideMatch> peptideMatches, String modification) {
        List<PeptideMatch> filteredList;

        if(modification.isEmpty()) {
            return peptideMatches;
        }

        filteredList = new ArrayList<PeptideMatch>();

        for(PeptideMatch peptide : peptideMatches) {
            for(ModifiedLocation modLoc : peptide.getModifiedLocations()) {
                if(modLoc.getModification().equals(modification)) {
                    filteredList.add(peptide);
                    break;
                }
            }
        }

        return filteredList;
    }

    static public int firstIndexWithSequence(List<? extends Peptide> peptides, String sequence) {
        int index = -1;

        for(int i = 0; i < peptides.size(); i++) {
            if(peptides.get(i).getSequence().equals(sequence)) {
                index = i;
                break;
            }
        }

        return index;
    }

    static public int firstIndexWithId(List<? extends Peptide> peptides, String id) {
        int index = -1;

        for(int i = 0; i < peptides.size(); i++) {
            if(peptides.get(i).getId().equals(id)) {
                index = i;
                break;
            }
        }

        return index;
    }

    public static boolean inRange(PeptideMatch peptide, int start, int end) {
        if(start == end && start == 0) {
            return true;
        }
        return peptide.getPosition() >= start &&
               peptide.getSequence().length() + peptide.getPosition() - 1 <= end;
    }

    public static Collection<Peptide> getFirstOfEach(List<PeptideList> peptidesLists) {
        List<Peptide> peptides = new ArrayList<Peptide>();

        for(PeptideList list : peptidesLists) {
            if(!list.getPeptideList().isEmpty()) {
                peptides.add(list.getPeptideList().get(0));
            }
        }

        return peptides;
    }

    public static List<PeptideMatch> filterPeptides(List<PeptideMatch> peptides,
                                                    int start, int end,
                                                    String tissue, String mod) {

        return filterPeptideMatchesWithoutModification(
                filterPeptidesNotInTissue(
                        filterPeptideMatchesNotIn(
                                peptides,
                                start, end),
                        tissue),
                mod);
    }

    public static boolean isPeptideMatchNotFiltered(PeptideMatch match,
                                                    String[] regions,
                                                    String[] mods,
                                                    String[] tissues) {
        boolean notFiltered;
        String[] newRegions, newMods, newTissues;

        // Check if the peptide match is inside any region,
        // any tissue and any modification type. The lack of
        // filters should yield that the peptide is contained.
        notFiltered = regions.length == 0 && mods.length == 0 && tissues.length == 0;

        if(notFiltered) {
            return true;
        }

        // Because the filtering code for peptide lists checks for empty
        // filters we initialize the filter lists accordingly,
        // otherwise we have to use a lot of nested ifs to check for all
        // possible cases where some filter lists are empty.

        if(regions.length == 0) {
            newRegions = new String[1];
            newRegions[0] = "0-0";
        }
        else {
            newRegions = regions;
        }

        if(mods.length == 0) {
            newMods = new String[1];
            newMods[0] = "";
        }
        else {
            newMods = mods;
        }

        if(tissues.length == 0) {
            newTissues = new String[1];
            newTissues[0] = "";
        }
        else {
            newTissues = tissues;
        }

        for(String regionId : newRegions) {
            for(String tissue : newTissues) {
                for(String mod : newMods) {
                    try {
                        Region region = Region.tokenize(regionId);
                        List<PeptideMatch> pList = new ArrayList<PeptideMatch>();
                        pList.add(match);

                        if(!filterPeptides(pList,
                                region.getStart(), region.getEnd(),
                                tissue, mod).isEmpty()) {
                            return true;
                        }
                    } catch (IllegalRegionValueException e) {
                    }
                }
            }
        }
        return false;
    }
}
