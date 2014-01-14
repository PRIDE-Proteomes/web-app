package uk.ac.ebi.pride.proteomes.web.client.modules.data;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Group;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideList;
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
        public void onPeptideVarianceListsRetrieved(Collection<PeptideList> peptides);

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
    public boolean isPeptideCached(String sequence);

    public void requestGroups(String[] ids);
    public void requestProteins(String[] accessions);
    public void requestPeptideVariances(String[] sequences);

    public List<Group> getGroups(String[] ids);
    public List<Protein> getProteins(String[] accessions);
    public List<PeptideList> getPeptideVarianceLists(String[] sequences);

    public Group getGroup(String ids);
    public Protein getProtein(String accessions);
    public PeptideList getPeptideVarianceList(String sequences);
}
