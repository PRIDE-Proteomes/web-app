package uk.ac.ebi.pride.proteomes.web.client.events.state;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 27/01/14
 *         Time: 16:32
 */
public class LoadingDataEvent extends GwtEvent<LoadingDataEvent.Handler> {
    public interface Handler extends EventHandler {
        void onLoadingDataEvent(LoadingDataEvent event);
    }

    private static final Type<Handler> TYPE = new Type<>();
    private final boolean switching;

    private LoadingDataEvent(HasHandlers source, boolean switching) {
        super();
        setSource(source);
        this.switching = switching;
    }

    public static void fire(HasHandlers source, boolean switching) {
        LoadingDataEvent eventInstance = new LoadingDataEvent(source, switching);
        source.fireEvent(eventInstance);
    }

    public static GwtEvent.Type<Handler> getType() {
        return TYPE;
    }

    public boolean isSwitching() {
        return switching;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onLoadingDataEvent(this);
    }
}
