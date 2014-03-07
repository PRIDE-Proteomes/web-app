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
    public void retrieveData(String sequence, Integer taxonId) {
        String url = root + "/peptide/" + sequence;
        if (taxonId != null) {
            url += "?species=" + taxonId;
        }
        new DataRequester(sequence, url, PeptideList.class, handlers);
    }

}
