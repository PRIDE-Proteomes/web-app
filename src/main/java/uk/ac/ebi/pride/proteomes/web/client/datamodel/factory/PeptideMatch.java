package uk.ac.ebi.pride.proteomes.web.client.datamodel.factory;

import java.util.Set;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 12/11/13
 *         Time: 16:52
 */
public interface PeptideMatch extends Peptide {
    public Integer getPosition();
    public Integer getUniqueness();
    public Set<String> getSharedProteins();
    public Set<String> getSharedUpEntries();
    public Set<String> getSharedGenes();

}
