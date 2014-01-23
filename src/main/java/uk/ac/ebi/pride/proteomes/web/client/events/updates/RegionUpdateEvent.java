package uk.ac.ebi.pride.proteomes.web.client.events.updates;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Region;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 16:34
 */
public class RegionUpdateEvent extends GwtEvent<RegionUpdateEvent.Handler> {
    public interface Handler extends EventHandler {
        public void onRegionUpdateEvent(RegionUpdateEvent event);
    }

    private static final Type<Handler> TYPE = new Type<>();

    private List<Region> regionList;

    public RegionUpdateEvent(List<Region> regions, HasHandlers source) {
        super();
        this.regionList = regions;
        setSource(source);
    }

    public static void fire(HasHandlers source, List<Region> regions) {
        RegionUpdateEvent eventInstance = new RegionUpdateEvent(regions, source);
        source.fireEvent(eventInstance);
    }

    public List<Region> getRegions() {
        return regionList;
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
        handler.onRegionUpdateEvent(this);
    }
}
