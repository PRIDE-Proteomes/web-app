package uk.ac.ebi.pride.proteomes.web.client.datamodel;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 25/10/13
 *         Time: 16:05
 */
public interface ModifiedLocation {
    public int getLocation();
    public void setLocation(int location);

    public String getModification();
    public void setModification(String modification);
}
