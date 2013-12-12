package uk.ac.ebi.pride.proteomes.web.client.modules.googleanalytics;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.events.SnoopingEventBus;
import uk.ac.ebi.pride.proteomes.web.client.events.state.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.utils.Console;
import uk.ac.ebi.pride.proteomes.web.googleanalytics.client.GATracker;

/**
 * The reporter sends information about the user interacting with the
 * application so statistics can be aggregated in Google analytics.
 *
 * To do this, the trackPageview method is used, as well as direct reporting
 * of the events happening in the event bus, because trackPageView doesn't
 * track the internal state application (the end of URL, after the #)
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 02/12/13
 *         Time: 16:06
 */
public class Reporter implements
        StateChangingActionEvent.StateChangingActionHandler
{
    private final EventBus eventBus;
    private boolean trackingIsEnabled = false;

    public Reporter(EventBus eventBus) {
        this.eventBus =  eventBus;

        if(GWT.isScript()) {
            String hostName = Window.Location.getHostName();
            if(hostName.equals("www.ebi.ac.uk") ||
               hostName.equals("wwwdev.ebi.ac.uk")) {
                GATracker.setAccount("UA-46176625-1");
                GATracker.setDomainName("ebi.ac.uk");
                GATracker.trackPageview();
                trackingIsEnabled = true;
            }
        }

        if(!trackingIsEnabled && Console.VERBOSE) {
            Console.info("(GAnalytics): Tracking is disabled, " +
                         "will output to console instead.");
        }

        eventBus.addHandler(StateChangingActionEvent.getType(), this);
    }

    @Override
    public void onStateChangingActionEvent(StateChangingActionEvent event) {
        reportEvent(event.getAction().getType(),
                    event.getAction().getName(),
                    simpleName(event.getSource().getClass().toString()));
    }

    private void reportEvent(String category, String action, String module) {
        if(trackingIsEnabled) {
            GATracker.trackEvent(category, action, module);
        }
        if(Console.VERBOSE) {
            Console.info("(GAnalytics): " + getIndentation() + action + "(\"" +
                    category + "\") <- " + module);
        }
    }

    private String simpleName(String className) {
        return className.substring(className.lastIndexOf(".") + 1);
    }

    private String getIndentation() {
        if(eventBus instanceof SnoopingEventBus) {
            String indent = ((SnoopingEventBus) eventBus).getIndentation();
            return indent.substring(0, indent.length() - 2);
        }
        else {
            return "";
        }
    }
}
