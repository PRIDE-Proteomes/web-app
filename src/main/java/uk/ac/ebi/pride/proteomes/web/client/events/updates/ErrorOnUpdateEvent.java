package uk.ac.ebi.pride.proteomes.web.client.events.updates;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 22/10/13
 *         Time: 11:17
 */
public class ErrorOnUpdateEvent extends GwtEvent<ErrorOnUpdateEvent.Handler> {
    public interface Handler extends EventHandler {
        public void onUpdateErrorEvent(ErrorOnUpdateEvent event);
    }

    private static final Type<Handler> TYPE = new Type<>();

    private final String text;

    private ErrorOnUpdateEvent(String text, HasHandlers source) {
        super();
        this.text = text;
        setSource(source);
    }

    public static void fire(HasHandlers source, String text) {
        ErrorOnUpdateEvent eventInstance = new ErrorOnUpdateEvent(text, source);
        source.fireEvent(eventInstance);
    }

    public String getMessage() {
        return text;
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
        handler.onUpdateErrorEvent(this);
    }
}
