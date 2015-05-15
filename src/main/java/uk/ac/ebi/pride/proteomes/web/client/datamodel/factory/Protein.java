package uk.ac.ebi.pride.proteomes.web.client.datamodel.factory;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 25/10/13
 *         Time: 15:59
 */
public interface Protein {
    public String getAccession();
    public String getGene();
    public int getTaxonID();
    public String getSequence();
    public String getDescription();
    public List<ModifiedLocation> getModifiedLocations();
    public List<String> getTissues();
    public String getCoverage();
    public List<List<Integer>> getRegions();
    public List<PeptideMatch> getPeptides();
    public int getUniquePeptideCount();
}
