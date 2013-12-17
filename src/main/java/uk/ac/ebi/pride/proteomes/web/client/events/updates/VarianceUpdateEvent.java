package uk.ac.ebi.pride.proteomes.web.client.events.updates;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 15:13
 */
public class VarianceUpdateEvent extends GwtEvent<VarianceUpdateEvent.Handler> {
    public interface Handler extends EventHandler {
        public void onVarianceUpdateEvent(VarianceUpdateEvent event);
    }

    private static final Type<Handler> TYPE = new Type<Handler>();

    private String[] varianceIDs;

    public VarianceUpdateEvent(String[] varianceIDs, HasHandlers source) {
        super();
        this.varianceIDs = varianceIDs;
        setSource(source);
    }

    public static void fire(HasHandlers source, String[] variances) {
        VarianceUpdateEvent eventInstance = new VarianceUpdateEvent(variances, source);
        source.fireEvent(eventInstance);
    }

    public String[] getVarianceIDs() {
        return varianceIDs;
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
        handler.onVarianceUpdateEvent(this);
    }
}