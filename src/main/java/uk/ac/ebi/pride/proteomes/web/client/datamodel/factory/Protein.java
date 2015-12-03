package uk.ac.ebi.pride.proteomes.web.client.datamodel.factory;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 25/10/13
 *         Time: 15:59
 */
public interface Protein {
    String getAccession();
    List<String> getGenes();
    int getTaxonID();
    String getSequence();

    @Deprecated
    String getDescription();

    //Protein Description
    String getName();
    String getAlternativeName();
    String getSpecies();
    String getGeneSymbol();
    String getProteinEvidence();

    List<ModifiedLocation> getModifiedLocations();
    List<String> getTissues();
    String getCoverage();
    List<List<Integer>> getRegions();
    List<PeptideMatch> getPeptides();
    List<Feature> getFeatures();
    int getUniquePeptideToProteinCount();
    int getUniquePeptideToGeneCount();
    int getNonUniquePeptidesCount();


}
