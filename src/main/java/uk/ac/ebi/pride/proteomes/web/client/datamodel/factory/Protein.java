package uk.ac.ebi.pride.proteomes.web.client.datamodel.factory;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 25/10/13
 *         Time: 15:59
 */
public interface Protein {
    public String getAccession();
    public void setAccession(String accession);

    public String getSequence();
    public void setSequence(String sequence);

    public String getDescription();
    public void getDescription(String description);

    public List<ModifiedLocation> getModifiedLocations();
    public void setModifiedLocations(List<ModifiedLocation> modifiedLocations);

    public List<String> getTissues();
    public void setTissues(List<String> tissues);

    public String getCoverage();
    public void setCoverage(String coverage);

    public List<List<Integer>> getRegions();
    public void setRegions(List<List<Integer>> regions);

    public List<PeptideMatch> getPeptides();
    public void setPeptides(List<PeptideMatch> peptides);
}
