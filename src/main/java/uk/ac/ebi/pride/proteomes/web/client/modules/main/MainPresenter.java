package uk.ac.ebi.pride.proteomes.web.client.modules.main;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.events.state.EmptyViewEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.InvalidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.ValidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.ErrorOnUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.View;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 17/10/13
 *         Time: 10:56
 */
public class MainPresenter implements Presenter, EmptyViewEvent.Handler,
                                      ValidStateEvent.Handler,
                                      InvalidStateEvent.Handler,
                                      ErrorOnUpdateEvent.Handler {

    public interface ThisView extends View {
        public void hideMessage();
        public void showLoadingMessage();
        public void showInfoMessage(String message);
        public AcceptsOneWidget getPlaceHolder(int i);
    }

    private final EventBus eventBus;
    private final ThisView view;
    private final List<Presenter> presenterList;

    public MainPresenter(EventBus eventBus, ThisView view,
                         List<Presenter> presenters) {
        this.eventBus = eventBus;
        this.view = view;
        this.presenterList = presenters;

        eventBus.addHandler(EmptyViewEvent.getType(), this);
        eventBus.addHandler(ErrorOnUpdateEvent.getType(), this);
        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(InvalidStateEvent.getType(), this);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEventFromSource(event, this);
    }

    @Override
    public void bindToContainer(AcceptsOneWidget container) {
        view.bindToContainer(container);
        for(int i = 0; i < presenterList.size(); i++) {
            presenterList.get(i).bindToContainer(view.getPlaceHolder(i));
        }
    }

    @Override
    public void onEmptyViewEvent(EmptyViewEvent event) {
        view.showInfoMessage(event.getMessage());
    }

    @Override
    public void onValidStateEvent(ValidStateEvent event) {
        view.hideMessage();
    }

    @Override
    public void onInvalidStateEvent(InvalidStateEvent event) {
        view.showInfoMessage(event.getMessage());
    }

    @Override
    public void onUpdateErrorEvent(ErrorOnUpdateEvent event) {
        view.showInfoMessage(event.getMessage());
    }
}
