package uk.ac.ebi.pride.proteomes.web.client.datamodel;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideList;

import java.util.Collections;
import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 12/12/13
 *         Time: 10:14
 */
public class EmptyPeptideList implements PeptideList {
    @Override
    public List<Peptide> getPeptideList() {
        return Collections.emptyList();
    }
}
