package uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 25/10/13
 *         Time: 15:10
 */
public class ProteinRetriever extends DataRetriever {
    public ProteinRetriever(String webServiceRoot) {
        super(webServiceRoot);
    }

    @Override
    public void retrieveData(String id) {
        String url = root + "/protein/" + id;
        new DataRequester(id, url, Protein.class, handlers);
    }
}
