package uk.ac.ebi.pride.proteomes.web.client.datamodel.factory;

import java.util.List;
import java.util.Map;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 25/10/13
 *         Time: 15:59
 */
public interface Group {
    public String getId();
    public int getTaxonId();
    public String getDescription();
    public List<String> getTissues();
    public List<String> getMemberProteins();
    public Map<String, List<String>> getUniquePeptides();
}
