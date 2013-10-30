package uk.ac.ebi.pride.proteomes.web.client.modules.main;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 17/10/13
 *         Time: 10:56
 */
public class MainPresenter implements Presenter {
    public interface View extends uk.ac.ebi.pride.proteomes.web.client.modules.View {
        public void hidePopup();
        public void showPopup();
        public void showPopup(String message);
        public AcceptsOneWidget getNorthPlaceHolder();
        public AcceptsOneWidget getSouthPlaceHolder();
    }

    private final EventBus eventBus;
    private final View view;
    private final Presenter northPresenter;
    private final Presenter southPresenter;

    public MainPresenter(EventBus eventBus, View view,
                         Presenter northPresenter, Presenter southPresenter) {
        this.eventBus = eventBus;
        this.view = view;
        this.northPresenter = northPresenter;
        this.southPresenter = southPresenter;
    }

    @Override
    public void bindToContainer(AcceptsOneWidget container) {
        view.bindToContainer(container);
        northPresenter.bindToContainer(view.getNorthPlaceHolder());
        southPresenter.bindToContainer(view.getSouthPlaceHolder());
    }
}
