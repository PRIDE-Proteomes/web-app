package uk.ac.ebi.pride.proteomes.web.client.datamodel;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 28/10/13
 *         Time: 09:52
 */
public interface Peptide {
    public String getSequence();
    public void setSequence(String sequence);

    public int getTaxonId();
    public void setTaxonId(int taxonID);

    public List<ModifiedLocation> getModifiedLocations();
    public void setModifiedLocations(List<ModifiedLocation> modifiedLocations);

    public List<String> getTissues();
    public void setTissues(List<String> tissues);
}
