package uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModifiedLocation;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;
import uk.ac.ebi.pride.widgets.client.common.handler.PrideModificationHandler;
import uk.ac.ebi.pride.widgets.client.common.handler.ProteinModificationHandler;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 11/11/13
 *         Time: 13:48
 */
public class ProteinModificationAdapter implements ProteinModificationHandler {
    private final ModifiedLocation modifiedLocation;
    private final ModificationAdapter modification;
    private int count;

    public ProteinModificationAdapter(ModifiedLocation mod, Protein protein) {
        modifiedLocation = mod;
        modification = new ModificationAdapter(mod.getModification());
        count = 0;

        for(ModifiedLocation modif : protein.getModifiedLocations()) {
            if(modif.getModification().equals(mod.getModification())) {
                count++;
            }
        }
    }

    @Override
    public Integer getSite() {
        return modifiedLocation.getLocation();
    }

    @Override
    public PrideModificationHandler getPrideModification() {
        return modification;
    }

    @Override
    public Integer getCount() {
        return count;
    }

    @Override
    public Integer getUniqueness() {
        return null; // what is exactly this "uniqueness"?
    }

    @Override
    public Double getPrideScore() {
        return .0;
    }

    @Override
    public Double getMascotScore() {
        return .0;
    }
}
