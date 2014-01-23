package uk.ac.ebi.pride.proteomes.web.client.events.requests;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 01/11/13
 *         Time: 11:33
 */
public class ProteinRequestEvent extends GwtEvent<ProteinRequestEvent.Handler> {
    public interface Handler extends EventHandler {
        public void onProteinRequestEvent(ProteinRequestEvent event);
    }

    private static final Type<Handler> TYPE = new Type<>();

    private ProteinRequestEvent(HasHandlers source) {
        super();
        setSource(source);
    }

    public static void fire(HasHandlers source) {
        ProteinRequestEvent eventInstance = new ProteinRequestEvent(source);
        source.fireEvent(eventInstance);
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
        handler.onProteinRequestEvent(this);
    }
}