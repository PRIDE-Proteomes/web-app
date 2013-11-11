package uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters;

import uk.ac.ebi.pride.widgets.client.common.handler.PrideModificationHandler;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 11/11/13
 *         Time: 11:18
 */
public class ModificationAdapter implements PrideModificationHandler {
    private String type;

    public ModificationAdapter(String modificationType) {
        type = modificationType;
    }

    @Override
    public int getId() {
        return type.hashCode();
    }

    @Override
    public String getName() {
        return type;
    }

    @Override
    public Double getDiffMono() {
        return 0.0;
    }

    @Override
    public boolean isBioSignificance() {
        return false;
    }
}
