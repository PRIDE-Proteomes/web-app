package uk.ac.ebi.pride.proteomes.web.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.events.SnoopingEventBus;

/**
 * This class is the entry point of the application, i.e. the Code that starts
 * up the whole application.
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 15/10/13
 *         Time: 15:51
 */
public class WebApp implements EntryPoint {
    // The id of the html element in the main page where we will inject the
    // application
    public static final String PLACEHOLDER_ID = "webapp";

    @Override
    public void onModuleLoad() {
        final EventBus eventBus = new SnoopingEventBus();
        final AppStarter appStarter = new AppStarter(eventBus);
        appStarter.bindToContainer(RootPanel.get(PLACEHOLDER_ID));
    }
}
