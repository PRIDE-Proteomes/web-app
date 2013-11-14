package uk.ac.ebi.pride.proteomes.web.client.events.state;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 14/11/13
 *         Time: 15:30
 */
public class ValidStateEvent extends GwtEvent<ValidStateEvent.ValidStateHandler> {
    public interface ValidStateHandler extends EventHandler {
        public void onValidStateEvent(ValidStateEvent event);
    }

    private static final GwtEvent.Type<ValidStateHandler> TYPE = new GwtEvent.Type<ValidStateHandler>();

    public ValidStateEvent(HasHandlers source) {
        super();
        setSource(source);
    }

    public static void fire(HasHandlers source) {
        ValidStateEvent eventInstance = new ValidStateEvent(source);
        source.fireEvent(eventInstance);
    }

    public static GwtEvent.Type<ValidStateHandler> getType() {
        return TYPE;
    }

    @Override
    public Type<ValidStateHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ValidStateHandler handler) {
        handler.onValidStateEvent(this);
    }
}
