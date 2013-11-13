package uk.ac.ebi.pride.proteomes.web.client.events.updates;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 15:15
 */
public class ModificationUpdateEvent extends
        GwtEvent<ModificationUpdateEvent.ModificationUpdateHandler> {

    public interface ModificationUpdateHandler extends EventHandler {
        public void onModificationUpdateEvent(ModificationUpdateEvent event);
    }

    private static final Type<ModificationUpdateHandler> TYPE = new Type<ModificationUpdateHandler>();

    private String[] modifications;

    public ModificationUpdateEvent(String[] modifications, HasHandlers source) {
        super();
        this.modifications = modifications;
        setSource(source);
    }

    public static void fire(HasHandlers source, String[] modifications) {
        ModificationUpdateEvent eventInstance = new ModificationUpdateEvent(modifications, source);
        source.fireEvent(eventInstance);
    }

    public String[] getModifications() {
        return modifications;
    }

    public static Type<ModificationUpdateHandler> getType() {
        return TYPE;
    }

    @Override
    public Type<ModificationUpdateHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ModificationUpdateHandler handler) {
        handler.onModificationUpdateEvent(this);
    }
}