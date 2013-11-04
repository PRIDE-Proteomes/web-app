package uk.ac.ebi.pride.proteomes.web.client.events.updates;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 15:13
 */
public class VarianceUpdateEvent extends
        GwtEvent<VarianceUpdateEvent.VarianceUpdateHandler> {

    public interface VarianceUpdateHandler extends EventHandler {
        public void onVarianceUpdateEvent(VarianceUpdateEvent event);
    }

    private static final Type<VarianceUpdateHandler> TYPE = new Type<VarianceUpdateHandler>();

    private String[] variances;

    public VarianceUpdateEvent(String[] variances, HasHandlers source) {
        super();
        this.variances = variances;
        setSource(source);
    }

    public static void fire(HasHandlers source, String[] variances) {
        VarianceUpdateEvent eventInstance = new VarianceUpdateEvent(variances, source);
        source.fireEvent(eventInstance);
    }

    public String[] getVariances() {
        return variances;
    }

    public static Type<VarianceUpdateHandler> getType() {
        return TYPE;
    }

    @Override
    public Type<VarianceUpdateHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(VarianceUpdateHandler handler) {
        handler.onVarianceUpdateEvent(this);
    }
}