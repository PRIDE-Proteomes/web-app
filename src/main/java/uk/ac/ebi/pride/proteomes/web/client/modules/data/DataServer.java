package uk.ac.ebi.pride.proteomes.web.client.modules.data;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.PeptideWithPeptiforms;
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
    interface DataClient {
        void onGroupsRetrieved(Collection<Group> groups);
        void onProteinsRetrieved(Collection<Protein> proteins);
        void onPeptiformListsRetrieved(Collection<PeptideWithPeptiforms> peptides);

        void onRetrievalError(ErroneousResult invalidResponse);
    }
    interface ErroneousResult {
        Class getRequestedType();
        String getRequestedIdentifier();
        String getErrorDescription();
    }

    void bind(DataClient client);

    boolean isGroupCached(String id);
    boolean isProteinCached(String accession);
    boolean isPeptideCached(String sequence, String proteinId, int position);
    boolean isAnyPeptideCached(String sequence);
    boolean isPeptiformCached(String varianceId);

    void requestGroups(List<String> ids);
    void requestProteins(List<String> accessions);
    void requestPeptiforms(List<String> sequences, List<String> proteinIds, List<Integer> positions);
    void requestPeptiforms(List<String> sequences, List<String> proteinIds);

    List<Group> getCachedGroups(List<String> groupIds);
    List<Protein> getCachedProteins(List<String> proteinIds);
    List<PeptideWithPeptiforms> getCachedPeptiformLists(List<String> sequences, List<String> proteinIds, List<Integer> positions);
    List<PeptideWithPeptiforms> getCachedPeptiformLists(List<String> sequences, List<String> proteinIds);
    List<Peptide> getCachedPeptiforms(List<String> varianceId);

    Group getCachedGroup(String ids);
    Protein getCachedProtein(String accessions);
    PeptideWithPeptiforms getCachedPeptiformList(String sequence, String proteinId, int position);
    PeptideWithPeptiforms getCachedPeptiformList(String sequence, String proteinIds);
    Peptide getCachedPeptiform(String varianceId);
}
