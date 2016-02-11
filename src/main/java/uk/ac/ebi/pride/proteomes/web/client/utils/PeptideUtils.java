package uk.ac.ebi.pride.proteomes.web.client.utils;

import com.google.common.collect.Lists;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.ModificationWithPosition;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.PeptideWithPeptiforms;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Region;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModifiedLocation;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.IllegalModificationPositionException;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.IllegalRegionValueException;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 20/11/13
 *         Time: 15:23
 */
public class PeptideUtils {

    private static Logger logger = Logger.getLogger(PeptideUtils.class.getName());

    /**
     * Filters the peptide matches outside a region
     * @param peptideMatches the peptide matches to filter
     * @param start the start point of the region
     * @param end the end point of the region
     * @return a new list containing only the peptide matches inside the region
     */
    static public List<PeptideMatch> filterPeptideMatchesNotIn(List<? extends PeptideMatch> peptideMatches, int start, int end) {
        List<PeptideMatch> filteredList = new ArrayList<>();

        if(start == end && start == 0) {
            return (List<PeptideMatch>) peptideMatches;
        }
        for(PeptideMatch peptide : peptideMatches) {
            if(inRange(peptide, start, end)) {
                filteredList.add(peptide);
            }
        }

        return filteredList;
    }
    /**
     * Filters the peptide matches outside a region
     * @param peptideMatches the peptide matches to filter
     * @param start the start point of the region
     * @param end the end point of the region
     * @return a new list containing only the peptide matches inside the region
     */
    static public List<PeptideWithPeptiforms> filterPeptideWithPeptiformsNotIn(List<PeptideWithPeptiforms> peptideMatches, int start, int end) {
        List<PeptideWithPeptiforms> filteredList = new ArrayList<>();

        if(start == end && start == 0) {
            return peptideMatches;
        }
        for(PeptideWithPeptiforms peptide : peptideMatches) {
            if(inRange(peptide, start, end)) {
                filteredList.add(peptide);
            }
        }

        return filteredList;
    }

    public static List<PeptideMatch> filterPeptidesNotInTissues(List<? extends PeptideMatch> peptideMatches, List<String> tissues) {
        List<PeptideMatch> filteredList;
        List<String> properTissues = new ArrayList<>();

        for(String tissue : tissues) {
            if(!tissue.equals("")) {
                properTissues.add(tissue);
            }
        }

        if(properTissues.isEmpty()) {
            return (List<PeptideMatch>) peptideMatches;
        }

        filteredList = new ArrayList<>();

        for (PeptideMatch peptide : peptideMatches) {
            if (peptide.getTissues().containsAll(properTissues)) {
                filteredList.add(peptide);
            }
        }

        return filteredList;
    }

    /**
     * @param peptideMatches List of peptide matches that need to be filtered
     *                       according to the modifications
     * @param modifications List of modification that need to filter the
     *                      peptides
     * @param proteinLength Protein sequence legth. Is used to translate the position of the modifications
     * @return A list of items that where already contained in the original
     * list
     */
    public static List<PeptideMatch> filterPeptideMatchesWithoutAnyModifications
    (List<? extends PeptideMatch> peptideMatches, List<ModificationWithPosition> modifications, int proteinLength) {
        List<PeptideMatch> filteredList = new ArrayList<>();
       List<ModificationWithPosition> properModifications = new ArrayList<>();

        for(ModificationWithPosition mod : modifications) {
//            if(!mod.equals("")) {
                properModifications.add(mod);
//            }
        }

        if(properModifications.isEmpty()) {
            return (List<PeptideMatch>) peptideMatches;
        }


        for(PeptideMatch peptide : peptideMatches) {
            Collection<ModificationWithPosition> mods = extractModifications(peptide, proteinLength);
            if(mods.containsAll(modifications)){
                filteredList.add(peptide);
            }
        }

        return filteredList;
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

    /**
     * This function determines whether the peptide match is "inside" a region,
     * which in practise means whether it's region intersects between the region
     * defined by start and end
     * @param peptide The peptide match that needs to be checked
     * @param start the start point of the region
     * @param end the end point of the region
     * @return whether the region defined by peptide and start + end intersect
     */
    public static boolean inRange(PeptideMatch peptide, int start, int end) {
        if(start == end && start == 0) {
            return true;
        }
        if(end < start) {
            return false;
        }

        int pepStart = peptide.getPosition();
        int pepEnd = peptide.getSequence().length() + peptide.getPosition() - 1;

        return end >= pepStart && start <= pepEnd;
    }

    public static List<PeptideMatch> filterPeptideMatches(List<? extends PeptideMatch> peptides,
                                                          int start, int end,
                                                          List<String> tissues,
                                                          List<ModificationWithPosition> mods,
                                                          int proteinLength) {

        return filterPeptideMatchesWithoutAnyModifications(
                filterPeptidesNotInTissues(
                        filterPeptideMatchesNotIn(
                                peptides,
                                start, end),
                        tissues),
                mods, proteinLength);
    }

    public static List<PeptideMatch> filterPeptideMatches(List<PeptideMatch> peptides,
                                                          List<String> tissues,
                                                          List<ModificationWithPosition> mods,
                                                          int proteinLength) {

        return filterPeptideMatchesWithoutAnyModifications(
                filterPeptidesNotInTissues(
                        peptides,
                        tissues),
                mods, proteinLength);
    }

    public static boolean isPeptideMatchNotFiltered(PeptideMatch match,
                                                    List<String> regions,
                                                    List<String> mods,
                                                    List<String> tissues,
                                                    int proteinLength) {
        boolean notFiltered;
        List<String> newRegions, newMods, newTissues;

        // Check if the peptide match is inside any region,
        // any tissue and any modification type. The lack of
        // filters should yield that the peptide is contained.
        notFiltered = regions.isEmpty() && mods.isEmpty() && tissues.isEmpty();

        if(notFiltered) {
            return true;
        }

        // Because the filtering code for peptide lists checks for empty
        // filters we initialize the filter lists accordingly,
        // otherwise we have to use a lot of nested ifs to check for all
        // possible cases where some filter lists are empty.

        if(regions.isEmpty()) {
            newRegions = new ArrayList<>();
            newRegions.add("");
        }
        else {
            newRegions = regions;
        }

        if(mods.isEmpty()) {
            newMods = new ArrayList<>();
            newMods.add("");
        }
        else {
            newMods = mods;
        }

        if(tissues.isEmpty()) {
            newTissues = new ArrayList<>();
            newTissues.add("");
        }
        else {
            newTissues = tissues;
        }

        for(String regionId : newRegions) {
            for(String tissue : newTissues) {
                for(String modId : newMods) {
                    try {
                        //We want to check permutations,
                        // so we  pack the single tissues and modifications
                        // into lists and use the filterPeptideMatches method
                        Region region;
                        if(regionId.equals("")) {
                            region = Region.emptyRegion();
                        }
                        else {
                            region = Region.tokenize(regionId);
                        }
                        List<PeptideMatch> pList = new ArrayList<>();
                        pList.add(match);
                        List<String> tList = new ArrayList<>();
                        tList.add(tissue);
                        List<ModificationWithPosition> mList = new ArrayList<>();

                        ModificationWithPosition mod;
                        if(modId.equals("")) {
                            mod = ModificationWithPosition.emptyModificationWithPosition();
                        }
                        else {
                            mod = ModificationWithPosition.tokenize(regionId);
                        }

                        mList.add(mod);

                        if(!filterPeptideMatches(pList,
                                region.getStart(), region.getEnd(),
                                tList, mList, proteinLength).isEmpty()) {
                            return true;
                        }
                    } catch (IllegalRegionValueException | IllegalModificationPositionException ignored) {
                    }
                }
            }
        }
        return false;
    }


    public static List<ModificationWithPosition> extractModifications(PeptideMatch peptide, int proteinLength) {
        Set<ModificationWithPosition> mods = new HashSet<>();

        for (final ModifiedLocation modLoc : peptide.getModifiedLocations()) {
            final int position = translateModificationToProteinPosition(modLoc, peptide, proteinLength);
            try {
                mods.add(new ModificationWithPosition(modLoc.getModification(), position))   ;
            } catch (IllegalModificationPositionException e) {
                logger.info("Error while converting modifications");
            }

        }
        return Lists.newArrayList(mods);
    }

    public static int translateModificationToProteinPosition(ModifiedLocation mod, PeptideMatch peptide, int proteinLength){

        int position = -1;

        //n-terminal mod, we propagate the mod only to the n terminal position of the protein
        if (mod.getPosition() == 0) {
            if (peptide.getPosition() == 1) {
                position = 0;
            }
        }
        //c-terminal mod, we propagate the mod only to the c terminal position of the protein
        else if (mod.getPosition() == peptide.getSequence().length() + 1) {
            if (peptide.getPosition() == proteinLength - peptide.getSequence().length() + 1) {
                position = proteinLength + 1;
            }
        } else {
            position = peptide.getPosition() + mod.getPosition() - 1;
        }

        return position;
    }
}
