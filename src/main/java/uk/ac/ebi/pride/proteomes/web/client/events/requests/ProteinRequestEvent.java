package uk.ac.ebi.pride.proteomes.web.client.events.requests;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 01/11/13
 *         Time: 11:33
 */
public class ProteinRequestEvent extends
        GwtEvent<ProteinRequestEvent.ProteinRequestHandler> {

    public interface ProteinRequestHandler extends EventHandler {
        public void onProteinRequestEvent(ProteinRequestEvent event);
    }

    private static final Type<ProteinRequestHandler> TYPE = new Type<ProteinRequestHandler>();

    public ProteinRequestEvent(HasHandlers source) {
        super();
        setSource(source);
    }

    public static void fire(HasHandlers source) {
        ProteinRequestEvent eventInstance = new ProteinRequestEvent(source);
        source.fireEvent(eventInstance);
    }

    public static Type<ProteinRequestHandler> getType() {
        return TYPE;
    }

    @Override
    public Type<ProteinRequestHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ProteinRequestHandler handler) {
        handler.onProteinRequestEvent(this);
    }
}