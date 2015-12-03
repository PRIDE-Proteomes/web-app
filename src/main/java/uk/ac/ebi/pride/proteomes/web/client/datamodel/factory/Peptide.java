package uk.ac.ebi.pride.proteomes.web.client.datamodel.factory;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 28/10/13
 *         Time: 09:52
 */
public interface Peptide {
    String getId();
    boolean getSymbolic();
    String getSequence();
    int getTaxonID();
    List<ModifiedLocation> getModifiedLocations();
    List<String> getTissues();
    List<String> getAssays();
    List<String> getClusters();
}
