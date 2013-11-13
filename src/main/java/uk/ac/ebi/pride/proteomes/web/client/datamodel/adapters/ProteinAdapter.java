package uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModifiedLocation;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;
import uk.ac.ebi.pride.widgets.client.common.handler.PeptideHandler;
import uk.ac.ebi.pride.widgets.client.common.handler.ProteinHandler;
import uk.ac.ebi.pride.widgets.client.common.handler.ProteinModificationHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 11/11/13
 *         Time: 13:43
 */
public class ProteinAdapter implements ProteinHandler {
    private final Protein protein;
    private final List<ProteinModificationHandler> modificationHandlers;
    private List<PeptideHandler> peptideHandlers;

    public ProteinAdapter(Protein protein) {
        this.protein = protein;

        peptideHandlers = new ArrayList<PeptideHandler>();

        for(PeptideMatch p : protein.getPeptides()) {
            peptideHandlers.add(new PeptideAdapter(p));
        }

        modificationHandlers = new ArrayList<ProteinModificationHandler>();

        for(ModifiedLocation mod : protein.getModifiedLocations()) {
            modificationHandlers.add(new ProteinModificationAdapter(mod,
                                                                    protein));
        }
    }

    @Override
    public Integer getLength() {
        return protein.getSequence().length();
    }

    @Override
    public String getSequence() {
        return protein.getSequence();
    }

    @Override
    public List<ProteinModificationHandler> getModifications() {
        return modificationHandlers;
    }

    @Override
    public List<PeptideHandler> getPeptides() {
        return peptideHandlers;
    }
}
