package uk.ac.ebi.pride.proteomes.web.client.datamodel.factory;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 25/10/13
 *         Time: 15:59
 */
public interface Group {
    public String getId();
    public void setId(String id);

    public int getTaxonId();
    public void setTaxonId(int taxonID);

    public String getDescription();
    public void setDescription(String description);

    public List<String> getTissues();
    public void setTissues(List<String> tissues);

    public List<String> getMemberProteins();
    public void setMemberProteins(List<String> memberProteins);

    public Alignment getUniquePeptides();
    public void setUniquePeptides(List<String> uniquePeptides);
}
