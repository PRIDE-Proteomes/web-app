package uk.ac.ebi.pride.proteomes.web.client.events.updates;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 15:10
 */
public class TissueUpdateEvent extends GwtEvent<TissueUpdateEvent.Handler> {
    public interface Handler extends EventHandler {
        void onTissueUpdateEvent(TissueUpdateEvent event);
    }

    private static final Type<Handler> TYPE = new Type<>();

    private final List<String> tissues;

    private TissueUpdateEvent(List<String> tissues, HasHandlers source) {
        super();
        this.tissues = tissues;
        setSource(source);
    }

    public static void fire(HasHandlers source, List<String> tissues) {
        TissueUpdateEvent eventInstance = new TissueUpdateEvent(tissues, source);
        source.fireEvent(eventInstance);
    }

    public List<String> getTissues() {
        return tissues;
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
        handler.onTissueUpdateEvent(this);
    }
}
