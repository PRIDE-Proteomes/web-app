package uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.widgets.client.common.handler.PeptideHandler;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 11/11/13
 *         Time: 13:50
 */
public class PeptideAdapter implements PeptideHandler {
    private final PeptideMatch peptide;

    public PeptideAdapter(PeptideMatch peptide) {
        this.peptide = peptide;
    }

    @Override
    public String getSequence() {
        return peptide.getSequence();
    }

    @Override
    public Integer getSite() {
        return peptide.getSite();
    }

    @Override
    public Integer getEnd() {
        return getSite() + peptide.getSequence().length();
    }

    @Override
    public Integer getUniqueness() {
        return 0; // todo
        //we don't know if a peptide is unique with the
        // current model!
    }
}
