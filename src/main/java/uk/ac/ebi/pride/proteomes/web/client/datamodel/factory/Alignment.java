package uk.ac.ebi.pride.proteomes.web.client.datamodel.factory;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 28/10/13
 *         Time: 09:48
 */
public interface Alignment {
    public List<AlignedProtein> getMemberProteins();
    public void setMemberProteins(List<AlignedProtein> memberProteins);

    public List<Mismatch> getSequenceMismatches();
    public void setSequenceMismatches(List<Mismatch> sequenceMisMatches);
}
