package uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModifiedLocation;
import uk.ac.ebi.pride.widgets.client.common.handler.PeptideHandler;

import java.util.List;
import java.util.Set;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 11/11/13
 *         Time: 13:50
 */
public class PeptideAdapter implements PeptideHandler, PeptideMatch {
    private final PeptideMatch peptide;

    public PeptideAdapter(PeptideMatch peptide) {
        this.peptide = peptide;
    }

    @Override
    public String getId() {
        return peptide.getId();
    }

    @Override
    public boolean getSymbolic() {
        return peptide.getSymbolic();
    }

    @Override
    public String getSequence() {
        return peptide.getSequence();
    }

    @Override
    public int getTaxonID() {
        return peptide.getTaxonID();
    }

    @Override
    public List<ModifiedLocation> getModifiedLocations() {
        return peptide.getModifiedLocations();
    }

    @Override
    public List<String> getTissues() {
        return peptide.getTissues();
    }

    @Override
    public List<String> getAssays() {
        return peptide.getAssays();
    }

    @Override
    public List<String> getClusters() {
        return peptide.getClusters();
    }

    @Override
    public Integer getSite() {
        return getPosition();
    }

    @Override
    public Integer getEnd() {
        return getPosition() + peptide.getSequence().length() - 1;
    }

    @Override
    public Integer getPosition() {
        return peptide.getPosition();
    }

    @Override
    public Integer getUniqueness() {
        return peptide.getUniqueness();
    }

    @Override
    public Set<String> getSharedProteins() {
        return peptide.getSharedProteins();
    }

    @Override
    public Set<String> getSharedUpEntries() {
        return peptide.getSharedUpEntries();
    }

    @Override
    public Set<String> getSharedGenes() {
        return peptide.getSharedGenes();
    }
}
