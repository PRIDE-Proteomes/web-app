package uk.ac.ebi.pride.proteomes.web.client.datamodel;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModifiedLocation;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideList;

import java.util.List;
import java.util.Set;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 17/01/14
 *         Time: 11:51
 */
public class PeptideWithVariances implements PeptideMatch, PeptideList {
    private static class EmptyPeptideWithVariances {
        private static final PeptideWithVariances instance = new PeptideWithVariances();
    }

    private final PeptideMatch match;
    private final PeptideList list;

    public PeptideWithVariances(PeptideMatch match, PeptideList list) {
        this.match = match;
        this.list = list;
    }

    private PeptideWithVariances() {
        this.match = EmptyPeptideMatch.getInstance();
        this.list = new EmptyPeptideList();
    }

    public static PeptideWithVariances emptyPeptideWithVariances() {
        return EmptyPeptideWithVariances.instance;
    }

    @Override
    public Integer getPosition() {
        return match.getPosition();
    }

    @Override
    public Integer getUniqueness() {
        return match.getUniqueness();
    }

    @Override
    public Set<String> getSharedProteins() {
        return match.getSharedProteins();
    }

    @Override
    public Set<String> getSharedUpEntries() {
        return match.getSharedUpEntries();
    }

    @Override
    public Set<String> getSharedGenes() {
        return match.getSharedGenes();
    }

    @Override
    public String getId() {
        return match.getId();
    }

    @Override
    public boolean getSymbolic() {
        return match.getSymbolic();
    }

    @Override
    public String getSequence() {
        return match.getSequence();
    }

    @Override
    public int getTaxonID() {
        return match.getTaxonID();
    }

    @Override
    public List<ModifiedLocation> getModifiedLocations() {
        return match.getModifiedLocations();
    }

    @Override
    public List<String> getTissues() {
        return match.getTissues();
    }

    @Override
    public List<String> getAssays() {
        return match.getAssays();
    }

    @Override
    public List<String> getClusters() {
        return match.getClusters();
    }

    @Override
    public List<Peptide> getPeptideList() {
        return list.getPeptideList();
    }
}
