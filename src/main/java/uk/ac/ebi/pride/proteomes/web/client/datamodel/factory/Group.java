package uk.ac.ebi.pride.proteomes.web.client.datamodel.factory;

import java.util.List;

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
    public Alignment getUniquePeptides();
}
