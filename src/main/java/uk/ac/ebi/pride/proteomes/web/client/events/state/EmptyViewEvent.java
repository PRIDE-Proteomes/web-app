package uk.ac.ebi.pride.proteomes.web.client.events.state;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 10:48
 */
public class EmptyViewEvent extends GwtEvent<EmptyViewEvent.EmptyViewHandler> {
    public interface EmptyViewHandler extends EventHandler {
        public void onEmptyViewEvent(EmptyViewEvent event);
    }

    private static final Type<EmptyViewHandler> TYPE = new Type<EmptyViewHandler>();
    private final String message;

    public EmptyViewEvent(HasHandlers source, String message) {
        super();
        setSource(source);
        this.message = message;
    }

    public static void fire(HasHandlers source, String message) {
        EmptyViewEvent eventInstance = new EmptyViewEvent(source, message);
        source.fireEvent(eventInstance);
    }

    public static Type<EmptyViewHandler> getType() {
        return TYPE;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public Type<EmptyViewHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(EmptyViewHandler handler) {
        handler.onEmptyViewEvent(this);
    }
}
