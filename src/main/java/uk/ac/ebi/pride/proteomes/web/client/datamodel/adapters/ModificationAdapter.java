package uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.ModificationWithPosition;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.IllegalModificationPositionException;
import uk.ac.ebi.pride.widgets.client.common.handler.PrideModificationHandler;

import java.util.logging.Logger;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 11/11/13
 *         Time: 11:18
 */
public class ModificationAdapter implements PrideModificationHandler {

    private static Logger logger = Logger.getLogger(ModificationAdapter.class.getName());

    private ModificationWithPosition modificationWithPosition = null;

    public ModificationAdapter(String modificationType, int position) {
        try {
            modificationWithPosition = new ModificationWithPosition(modificationType, position);
        } catch (IllegalModificationPositionException e) {
            logger.info("Error while converting modifications");
        }
    }

    @Override
    public int getId() {
        return modificationWithPosition.hashCode();
    }

    @Override
    public String getName() {
        return modificationWithPosition.getModification();
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
