package uk.ac.ebi.pride.proteomes.web.client.datamodel.factory;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 12/11/13
 *         Time: 16:52
 */
public interface PeptideMatch extends Peptide {
    public Integer getSite();
    public void setSite(Integer site);
}