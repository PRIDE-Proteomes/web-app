package uk.ac.ebi.pride.proteomes.web.client.modules.data;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.PeptideWithVariances;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Group;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;

import java.util.Collection;
import java.util.List;

/**
 * In this file are contained the interfaces between a class that
 * serves data and a class that needs the data.
 * The server uses callbacks from the bound client (using the bind method)
 * to deliver the data asynchronously.
 *
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 25/10/13
 *         Time: 15:05
 */
public interface DataServer {
    public interface DataClient {
        public void onGroupsRetrieved(Collection<Group> groups);
        public void onProteinsRetrieved(Collection<Protein> proteins);
        public void onPeptideVarianceListsRetrieved(Collection<PeptideWithVariances> peptides);

        public void onRetrievalError(ErroneousResult invalidResponse);
    }
    public interface ErroneousResult {
        public Class getRequestedType();
        public String getRequestedIdentifier();
        public String getErrorDescription();
    }

    public void bind(DataClient client);

    public boolean isGroupCached(String id);
    public boolean isProteinCached(String accession);
    public boolean isPeptideCached(String sequence, String proteinId, int position);
    public boolean isAnyPeptideCached(String sequence);
    public boolean isPeptideVarianceCached(String varianceId);

    public void requestGroups(List<String> ids);
    public void requestProteins(List<String> accessions);
    public void requestPeptideVariances(List<String> sequences, List<String> proteinIds, List<Integer> positions);
    public void requestPeptideVariances(List<String> sequences, List<String> proteinIds);

    public List<Group> getCachedGroups(List<String> groupIds);
    public List<Protein> getCachedProteins(List<String> proteinIds);
    public List<PeptideWithVariances> getCachedPeptideVarianceLists(List<String> sequences, List<String> proteinIds, List<Integer> positions);
    public List<PeptideWithVariances> getCachedPeptideVarianceLists(List<String> sequences, List<String> proteinIds);
    public List<Peptide> getCachedPeptideVariances(List<String> varianceId);

    public Group getCachedGroup(String ids);
    public Protein getCachedProtein(String accessions);
    public PeptideWithVariances getCachedPeptideVarianceList(String sequence, String proteinId, int position);
    public PeptideWithVariances getCachedPeptideVarianceList(String sequence, String proteinIds);
    public Peptide getCachedPeptideVariance(String varianceId);
}
