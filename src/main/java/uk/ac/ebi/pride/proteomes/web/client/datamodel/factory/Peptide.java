package uk.ac.ebi.pride.proteomes.web.client.datamodel.factory;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 28/10/13
 *         Time: 09:52
 */
public interface Peptide {
    public String getId();
    public boolean getSymbolic();
    public String getSequence();
    public int getTaxonID();
    public List<ModifiedLocation> getModifiedLocations();
    public List<String> getTissues();
    public List<String> getAssays();
    public List<String> getClusters();
}
