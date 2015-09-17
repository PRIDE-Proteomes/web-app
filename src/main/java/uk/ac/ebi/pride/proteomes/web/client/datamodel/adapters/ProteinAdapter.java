package uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Feature;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModifiedLocation;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;
import uk.ac.ebi.pride.widgets.client.common.handler.FeatureHandler;
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
    private final List<ProteinModificationHandler> modifications;
    private final List<PeptideHandler> peptideHandlers;
    private final List<FeatureHandler> featureHandlers;

    public ProteinAdapter(Protein protein) {
        this.protein = protein;

        peptideHandlers = new ArrayList<>();
        for(PeptideMatch p : protein.getPeptides()) {
            peptideHandlers.add(new PeptideAdapter(p));
        }

        modifications = new ArrayList<>();
        for(ModifiedLocation mod : protein.getModifiedLocations()) {
            modifications.add(new ProteinModificationAdapter(mod, protein));
        }

        featureHandlers = new ArrayList<>();
        for(Feature feature : protein.getFeatures()) {
            featureHandlers.add(new FeatureAdapter(feature));
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
        return modifications;
    }

    @Override
    public List<PeptideHandler> getPeptides() {
        return peptideHandlers;
    }

    @Override
    public List<FeatureHandler> getFeatures() {
        return featureHandlers;
    }
}
