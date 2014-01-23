package uk.ac.ebi.pride.proteomes.web.client.events.state;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 11:24
 */
public class InvalidStateEvent extends GwtEvent<InvalidStateEvent.Handler> {
    public interface Handler extends EventHandler {
        public void onInvalidStateEvent(InvalidStateEvent event);
    }

    private static final Type<Handler> TYPE = new Type<>();
    private final String message;
    private final String state;

    public InvalidStateEvent(HasHandlers source, String message,
                             String state) {
        super();
        setSource(source);
        this.message = message;
        this.state = state;
    }

    public static void fire(HasHandlers source, String message, String state) {
        InvalidStateEvent eventInstance = new InvalidStateEvent(source, message, state);
        source.fireEvent(eventInstance);
    }

    public static  Type<Handler> getType() {
        return TYPE;
    }

    public String getMessage() {
        return message;
    }

    public String getState() {
        return state;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onInvalidStateEvent(this);
    }
}
