package uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideList;

/**
 * This class retrieves the variances of a specified peptide sequence
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 29/10/13
 *         Time: 16:17
 */

public class PeptideVarianceRetriever extends DataRetriever {
    public PeptideVarianceRetriever(String webServiceRoot) {
        super(webServiceRoot);
    }

    @Override
    public void retrieveData(String id) {
        String url = root + "/peptide/" + id;
        new DataRequester(id, url, PeptideList.class, handlers);
    }
}
