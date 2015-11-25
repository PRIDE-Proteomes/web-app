package uk.ac.ebi.pride.proteomes.web.client.datamodel;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModifiedLocation;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 17/01/14
 *         Time: 16:38
 */
public class EmptyPeptideMatch implements PeptideMatch{
    private EmptyPeptideMatch() {
    }

    @Override
    public Integer getPosition() {
        return 0;
    }

    @Override
    public Integer getUniqueness() {
        return 0;  //NON_UNIQUE
    }

    @Override
    public Set<String> getSharedProteins() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getSharedGenes() {
        return Collections.emptySet();
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public boolean getSymbolic() {
        return false;
    }

    @Override
    public String getSequence() {
        return "";
    }

    @Override
    public int getTaxonID() {
        return 0;
    }

    @Override
    public List<ModifiedLocation> getModifiedLocations() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getTissues() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getAssays() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getClusters() {
        return Collections.emptyList();
    }

    private static class LazyHolder {
        private static final EmptyPeptideMatch instance = new EmptyPeptideMatch();
    }

    public static EmptyPeptideMatch getInstance() {
        return LazyHolder.instance;
    }
}
