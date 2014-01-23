package uk.ac.ebi.pride.proteomes.web.client.events.state;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 14/11/13
 *         Time: 15:30
 */
public class ValidStateEvent extends GwtEvent<ValidStateEvent.Handler> {
    public interface Handler extends EventHandler {
        public void onValidStateEvent(ValidStateEvent event);
    }

    public static enum ViewType {
        Group, Protein
    }

    private static final GwtEvent.Type<Handler> TYPE = new GwtEvent.Type<>();
    private final ViewType viewType;

    private ValidStateEvent(HasHandlers source, ViewType viewType) {
        super();
        setSource(source);
        this.viewType = viewType;
    }

    public static void fire(HasHandlers source, ViewType viewType) {
        ValidStateEvent eventInstance = new ValidStateEvent(source, viewType);
        source.fireEvent(eventInstance);
    }

    public static GwtEvent.Type<Handler> getType() {
        return TYPE;
    }

    public ViewType getViewType() {
        return viewType;
    }
    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onValidStateEvent(this);
    }
}
