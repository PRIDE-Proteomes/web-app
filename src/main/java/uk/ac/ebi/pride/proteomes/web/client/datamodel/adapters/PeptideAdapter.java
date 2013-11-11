package uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;
import uk.ac.ebi.pride.widgets.client.common.handler.PeptideHandler;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 11/11/13
 *         Time: 13:50
 */
public class PeptideAdapter implements PeptideHandler {
    private final Peptide peptide;
    private final Protein protein;

    public PeptideAdapter(Peptide peptide, Protein protein) {
        this.peptide = peptide;
        this.protein = protein;
    }

    @Override
    public String getSequence() {
        return peptide.getSequence();
    }

    @Override
    public Integer getSite() {
        return 0; // todo
        // we don't have a way to know the exact site the peptide
        // starts, we cannot calculate it, it-s sequence may repeat in the
        // protein :/
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
