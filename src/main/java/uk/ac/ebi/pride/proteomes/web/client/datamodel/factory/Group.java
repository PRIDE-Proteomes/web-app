package uk.ac.ebi.pride.proteomes.web.client.datamodel.factory;

import java.util.List;
import java.util.Map;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 25/10/13
 *         Time: 15:59
 */
public interface Group {
    String getId();
    int getTaxonId();
    String getDescription();
    List<String> getTissues();
    List<String> getMemberProteins();
    Map<String, List<String>> getUniquePeptides();
}
