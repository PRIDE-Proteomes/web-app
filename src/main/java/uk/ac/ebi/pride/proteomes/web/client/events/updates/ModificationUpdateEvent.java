package uk.ac.ebi.pride.proteomes.web.client.events.updates;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 15:15
 */
public class ModificationUpdateEvent extends GwtEvent<ModificationUpdateEvent.Handler> {
    public interface Handler extends EventHandler {
        public void onModificationUpdateEvent(ModificationUpdateEvent event);
    }

    private static final Type<Handler> TYPE = new Type<>();

    private List<String> modifications;

    private ModificationUpdateEvent(List<String> modifications, HasHandlers source) {
        super();
        this.modifications = modifications;
        setSource(source);
    }

    public static void fire(HasHandlers source, List<String> modifications) {
        ModificationUpdateEvent eventInstance = new ModificationUpdateEvent(modifications, source);
        source.fireEvent(eventInstance);
    }

    public List<String> getModifications() {
        return modifications;
    }

    public static Type<Handler> getType() {
        return TYPE;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onModificationUpdateEvent(this);
    }
}
