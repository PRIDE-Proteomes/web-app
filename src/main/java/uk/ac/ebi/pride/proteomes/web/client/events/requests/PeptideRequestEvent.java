package uk.ac.ebi.pride.proteomes.web.client.events.requests;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 01/11/13
 *         Time: 11:36
 */
public class PeptideRequestEvent extends
        GwtEvent<PeptideRequestEvent.PeptideRequestHandler> {

    public interface PeptideRequestHandler extends EventHandler {
        public void onPeptideRequestEvent(PeptideRequestEvent event);
    }

    private static final Type<PeptideRequestHandler> TYPE = new Type<PeptideRequestHandler>();

    public PeptideRequestEvent(HasHandlers source) {
        super();
        setSource(source);
    }

    public static void fire(HasHandlers source) {
        PeptideRequestEvent eventInstance = new PeptideRequestEvent(source);
        source.fireEvent(eventInstance);
    }

    public static Type<PeptideRequestHandler> getType() {
        return TYPE;
    }

    @Override
    public Type<PeptideRequestHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(PeptideRequestHandler handler) {
        handler.onPeptideRequestEvent(this);
    }
}
