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
class ProteinModificationAdapter implements ProteinModificationHandler {
    private final ModifiedLocation modifiedLocation;
    private final ModificationAdapter modification;
    private int count;

    /**
     *
     * @param mod the modified location that needs to be wrapped.
     * @param protein we need the protein because we need to count how many
     *                times the modification is found in the protein.
     */
    public ProteinModificationAdapter(ModifiedLocation mod, Protein protein) {
        modifiedLocation = mod;
        modification = new ModificationAdapter(mod.getModification());
        count = 0;

        for(ModifiedLocation modLoc : protein.getModifiedLocations()) {
            if(modLoc.getModification().equals(mod.getModification())) {
                count++;
            }
        }
    }

    @Override
    public Integer getSite() {
        return modifiedLocation.getPosition();
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
        return 0; //Uniqueness.NON_UNIQUE
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
