package uk.ac.ebi.pride.proteomes.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.DataProvider;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.DataServer;
import uk.ac.ebi.pride.proteomes.web.client.modules.history.AppController;
import uk.ac.ebi.pride.proteomes.web.client.modules.main.MainPresenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.main.MainView;
import uk.ac.ebi.pride.proteomes.web.client.modules.whistleblower.WhistleBlower;

/**
 * The appController is in charge of creating the graphical structure of the
 * application, by binding presenters and panels they can attach to and
 * then control.
 * It also instantiates all the objects that listen to the event bus.
 * ?? Also manages history and presenter view coordination ??
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 15/10/13
 *         Time: 16:54
 */
public class AppStarter implements RunAsyncCallback {

    private static final String webRoot = "http://ves-ebi-4d.ebi.ac" +
                                          ".uk:8110/pride/ws/proteomes";
    private final EventBus eventBus;
    private HasWidgets container;

    public AppStarter(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void bindToContainer(HasWidgets container) {
        this.container = container;

        // lazy initialization
        GWT.runAsync(this);
    }

    @Override
    public void onFailure(Throwable reason) { }

    @Override
    public void onSuccess() {

        // Stick together the whole app first by creating the listeners to
        // the event bus, then creating the structure of the graphical elements

        //TextPresenter.View textView = new TextView();
        //Presenter textPresenter = new TextPresenter(eventBus, textView);

        //ButtonsPresenter.View buttonsView = new ButtonsView();
        //Presenter buttonsPresenter = new ButtonsPresenter(eventBus,
        //        buttonsView);

        MainPresenter.View mainView = new MainView();
        //Presenter mainPresenter = new MainPresenter(eventBus,
        //                                            mainView,
        //                                            textPresenter,
        //                                            buttonsPresenter);

        SimplePanel mainPanel = new SimplePanel();
        container.add(mainPanel);

        //mainPresenter.bindToContainer(mainPanel);

        WhistleBlower whistle = new WhistleBlower(eventBus);

        AppController appController = new AppController(eventBus);
        DataServer provider = new DataProvider(appController, webRoot);

        appController.bindServer(provider);


        // fire first event to reach initial state

        History.fireCurrentHistoryState();
    }
}
