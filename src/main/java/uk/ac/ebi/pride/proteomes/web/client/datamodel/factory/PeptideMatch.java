package uk.ac.ebi.pride.proteomes.web.client.datamodel.factory;

import java.util.Set;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 12/11/13
 *         Time: 16:52
 */
public interface PeptideMatch extends Peptide {

    Integer getPosition();

    Integer getUniqueness();

    Set<String> getSharedProteins();

    Set<String> getSharedGenes();

}
