package com.attestator.player.client.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.attestator.common.client.ui.resolurces.Resources;
import com.attestator.common.shared.helper.DateHelper;
import com.attestator.common.shared.helper.NullHelper;
import com.attestator.common.shared.helper.ReportHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.AnswerVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.common.shared.vo.SingleChoiceQuestionVO;
import com.attestator.player.client.MainScreen;
import com.attestator.player.client.Player;
import com.attestator.player.client.helper.HistoryHelper.HistoryToken;
import com.attestator.player.client.helper.WindowHelper;
import com.attestator.player.client.rpc.PlayerAsyncCallback;
import com.attestator.player.client.rpc.PlayerAsyncEmptyCallback;
import com.attestator.player.client.ui.portlet.PublicationPortlet;
import com.attestator.player.client.ui.portlet.question.QuestionPortlet;
import com.attestator.player.client.ui.portlet.question.SCQuestionPortlet;
import com.attestator.player.shared.dto.TestDTO;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.ButtonBar;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutData;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

public class TestScreen extends MainScreen {
    private static final String NEXT = "Пропустить";
    private static final String CONTINUE = "Продолжить";
    private static final String START_TEST = "Начать тест";
    private static final String ANSWER = "Ответить";
    private static final String STOP = "Прервать тест";

    public static final String HISTORY_TOKEN = "test";
    private static TestScreen instance;

    private final VerticalLayoutContainer mainLayout = new VerticalLayoutContainer();
    private final ContentPanel mainPanel = new ContentPanel();
    private final ContentPanel topPanel = new ContentPanel();
    private final ContentPanel centerPanel = new ContentPanel();
    private final ContentPanel buttonsPanel = new ContentPanel();
    private final ContentPanel navigationPanel = new ContentPanel();

    private final PublicationPortlet publicationPoprtlet = new PublicationPortlet();
    private final SCQuestionPortlet scqPortlet = new SCQuestionPortlet();

    private final TextButton introductionButton = new TextButton(START_TEST);
    private final TextButton publicationButton = new TextButton(CONTINUE);
    private final TextButton skipQuestionButton = new TextButton(NEXT);
    private final TextButton answerQuestionButton = new TextButton(ANSWER);
    private final TextButton interruptTestButton = new TextButton(STOP);

    private final List<TextButton> navigationButtons = new ArrayList<TextButton>();

    private final Label questionNoLabel = new Label();
    private final Label testTimeLabel = new Label();
    private final Label questionTimeLabel = new Label();

    private PublicationVO publication;
    private int questionNo = -1;
    private ReportVO report;

    private Timer questionTimer;
    private Timer testTimer;

    private enum State {
        clean, publication, question, report, loading
    };

    private State state = State.clean;

    @Override
    public Widget asWidget() {
        return mainPanel;
    }

    public static TestScreen instance() {
        if (instance == null) {
            instance = new TestScreen();
        }
        return instance;
    }

    private TestScreen() {
        mainPanel.setPixelSize(600, -1);
        mainPanel.getElement().getStyle().setProperty("margin", "auto");
        mainPanel.getElement().getStyle().setMarginTop(40, Unit.PX);

        mainPanel.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (!event.isAttached()) {
                    cancelQuestionTimer();
                    cancelTestTimer();
                }
            }
        });

        publicationButton.addSelectHandler(new SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                if (publicationPoprtlet.isValid()) {
                    publicationPoprtlet.fillReport(report);

                    switchToNextQuestionOrReport();
                } else {
                    (new AlertMessageBox("Предупреждение",
                            "Не все поля заполнены правильно")).show();
                    publicationPoprtlet.focusInvalid();
                }
            }
        });

        introductionButton.addSelectHandler(new SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                switchToNextQuestionOrReport();
            }
        });

        interruptTestButton.setIcon(Resources.ICONS.delete16x16());
        interruptTestButton.setIconAlign(IconAlign.RIGHT);
        interruptTestButton.addSelectHandler(new SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                switchTo(State.report);
            }
        });

        skipQuestionButton.setIcon(Resources.ICONS.next16x16());
        skipQuestionButton.setIconAlign(IconAlign.RIGHT);
        skipQuestionButton.addSelectHandler(new SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                switchToNextQuestionOrReport();
            }
        });

        answerQuestionButton.setWidth(130);
        answerQuestionButton.setIcon(Resources.ICONS.checkMark16x16());
        answerQuestionButton.setIconAlign(IconAlign.RIGHT);
        answerQuestionButton.addSelectHandler(new SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                QuestionVO question = report.getQuestions()
                        .get(questionNo);

                if (report.isQuestionAnswered(question.getId())) {
                    return;
                }

                AnswerVO answer = scqPortlet.getValue();
                report.getAnswers().add(answer);
                Player.rpc.addAnswer(getTenantId(), report.getId(),
                        answer, new PlayerAsyncCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                if (publication.isAllowSkipQuestions()) {
                                    TextButton navButton = navigationButtons
                                            .get(questionNo);
                                    navButton.setIcon(Resources.ICONS
                                            .checkBoxChecked16x16());
                                }
                                switchToNextQuestionOrReport();
                            }
                        });

            }
        });

        questionNoLabel.getElement().addClassName(
                Resources.STYLES.testScreenCss().questionNoLabel());

        testTimeLabel.getElement().addClassName(
                Resources.STYLES.testScreenCss().timerLabel());
        testTimeLabel.getElement().getStyle().setTextAlign(TextAlign.RIGHT);

        questionTimeLabel.getElement().addClassName(
                Resources.STYLES.testScreenCss().timerLabel());
        questionTimeLabel.getElement().getStyle().setTextAlign(TextAlign.RIGHT);

        topPanel.setHeaderVisible(false);
        topPanel.setBodyBorder(false);
        topPanel.setBorders(false);
        mainLayout.add(topPanel, new VerticalLayoutData(-1, -1, new Margins(5,
                5, 0, 5)));

        centerPanel.setHeaderVisible(false);
        centerPanel.setBodyBorder(false);
        centerPanel.setBorders(false);
        mainLayout.add(centerPanel, new VerticalLayoutData(-1, -1, new Margins(
                5, 5, 0, 5)));

        buttonsPanel.setHeaderVisible(false);
        buttonsPanel.setBodyBorder(false);
        buttonsPanel.setBorders(false);
        mainLayout.add(buttonsPanel, new VerticalLayoutData(-1, -1,
                new Margins(5, 5, 0, 5)));

        navigationPanel.setHeadingText("Вопросы");
        navigationPanel.setHeaderVisible(true);
        navigationPanel.setCollapsible(true);
        navigationPanel.collapse();
        navigationPanel.setBodyBorder(false);
        navigationPanel.setBorders(false);
        mainLayout.add(navigationPanel, new VerticalLayoutData(-1, -1,
                new Margins(5, 5, 5, 5)));

        mainPanel.setWidget(mainLayout);
        mainPanel.getElement().disableTextSelection(true);

        scqPortlet.addValueChangeHandler(new ValueChangeHandler<AnswerVO>() {
            @Override
            public void onValueChange(ValueChangeEvent<AnswerVO> event) {
                answerQuestionButton.setEnabled(event.getValue() != null);
            }
        });
    }

    private int nextUnansweredQuestion(int fromQuestion) {
        for (int i = 1; i < report.getQuestions().size(); i++) {
            int no = (fromQuestion + i) % report.getQuestions().size();
            QuestionVO question = report.getQuestions().get(no);
            if (!report.isQuestionAnswered(question.getId())) {
                return no;
            }
        }
        return -1;
    }

    private void clear() {
        navigationButtons.clear();
        questionNo = -1;
        cancelQuestionTimer();
        cancelTestTimer();
        topPanel.clear();
        centerPanel.clear();
        buttonsPanel.clear();
        navigationPanel.hide();
        navigationPanel.clear();
        navigationPanel.collapse();
        mainPanel.forceLayout();
    }

    private void loadActiveTest(String publicationId) {
        mainLayout.mask("Загрузка...");
        
        Player.rpc.getActiveTest(getTenantId(), publicationId,
            new PlayerAsyncCallback<TestDTO>() {
                @Override
                public void onSuccess(TestDTO result) {
                    mainLayout.unmask();
                    if (result == null || NullHelper.isEmptyOrNull(result.getQuestions())) {
                        History.newItem(newToken("publications"));
                        return;
                    } else {
                        initFromTest(result);
                    }
                    WindowHelper.setBrowserWindowTitle(result.getPublication().getMetatest().getName());
                }

                @Override
                public void onFailure(Throwable caught) {
                    mainLayout.unmask();
                    super.onFailure(caught);
                    WindowHelper.setBrowserWindowTitle("Тест не найден");
                }
            });
    }

    private void loadUnfinishedReportOrStartNewTest(final String publicationId) {
        mainLayout.mask("Загрузка...");
        Player.rpc.getLatestUnfinishedReport(getTenantId(), publicationId, new PlayerAsyncCallback<ReportVO>() {
            @Override
            public void onSuccess(ReportVO result) {
                mainLayout.unmask();
                if (result != null) {
                    WindowHelper.setBrowserWindowTitle(result.getPublication().getMetatest().getName());
                    initFromReport(result);
                }
                else {
                    loadActiveTest(publicationId);
                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
                mainLayout.unmask();
                loadActiveTest(publicationId);
            }
        });
    }
    
    @Override
    public void initContent(HistoryToken token) {
        String publicationId = token.getProperties().get("publicationId");

        if (publicationId == null) {
            History.newItem(newToken("publications"));
            return;
        }

        switchTo(State.clean);
        
        loadUnfinishedReportOrStartNewTest(publicationId);
    }

    private void initFromReport(ReportVO report) {
        switchTo(State.clean);

        this.publication = report.getPublication();
        this.report = report;

        mainPanel.setHeadingText(report.getPublication().getMetatest().getName());
        
        switchToNextQuestionOrReport();
    }
    
    private void initFromTest(TestDTO test) {
        switchTo(State.clean);

        this.publication = test.getPublication();
        this.report = new ReportVO();
        report.setPublication(test.getPublication());
        report.setQuestions(test.getQuestions());

        mainPanel.setHeadingText(test.getPublication().getMetatest().getName());

        if (isPublicationStateNeeded()) {
            switchTo(State.publication);
        } else {
            switchTo(State.question, 0);
        }
    }

    private QuestionPortlet initQuestionPortlet(QuestionVO question) {
        if (question instanceof SingleChoiceQuestionVO) {
            scqPortlet.init((SingleChoiceQuestionVO) question);
            return scqPortlet;
        }
        return null;
    }

    private void showButtons(TextButton... buttons) {
        buttonsPanel.clear();

        ButtonBar bb = new ButtonBar();

        ArrayList<TextButton> buttonsList = new ArrayList<TextButton>(
                Arrays.asList(buttons));

        // Put interruptTestButton to left in any case
        if (buttonsList.contains(interruptTestButton)) {
            bb.add(interruptTestButton, new BoxLayoutData(new Margins(0)));
            buttonsList.remove(interruptTestButton);
        }

        // Add spacer which allign buttons to right
        BoxLayoutData flex = new BoxLayoutData(new Margins(0, 5, 0, 0));
        flex.setFlex(1);
        bb.add(new Label(), flex);

        // Add buttons in order them
        for (int i = 0; i < buttonsList.size(); i++) {
            BoxLayoutData blData = null;
            if (i < (buttonsList.size() - 1)) {
                blData = new BoxLayoutData(new Margins(0, 5, 0, 0));
            } else {
                blData = new BoxLayoutData(new Margins(0));
            }
            bb.add(buttonsList.get(i), blData);
        }

        buttonsPanel.setWidget(bb);
    }

    private void switchTo(State newState, int... questionNo) {
        int newQuestionNo = -1;
        if (questionNo.length > 0) {
            newQuestionNo = questionNo[0];
        }

        // Do nothing if state unchanged
        if (state == newState) {
            if (state != State.question) {
                return;
            } else {
                if (NullHelper.nullSafeEquals(this.questionNo, newQuestionNo)) {
                    return;
                }
            }
        }

        switch (newState) {
        case clean:
            clear();
            break;

        case publication:
            clear();
            publicationPoprtlet.init(publication);
            centerPanel.setWidget(publicationPoprtlet);
            publicationButton.setText(START_TEST);
            showButtons(publicationButton);
            break;

        case question:
            if (state == State.question && this.questionNo == newQuestionNo) {
                return;
            }

            // Init main widgets only once per question state
            if (state != State.question) {
                clear();

                initTopPanel();

                if (publication.isAllowSkipQuestions()) {
                    addNavigationButtons();
                }

                if (report.getStart() == null) {
                    Player.rpc.startReport(getTenantId(), report, new PlayerAsyncEmptyCallback<Void>());
                }
                else {
                    if (publication.getMaxTakeTestTime() != null) {
                        Long maxTakeTestTime = publication.getMaxTakeTestTime();
                        maxTakeTestTime = maxTakeTestTime - (System.currentTimeMillis() - report.getStart().getTime()); 
                        startTestTimer(maxTakeTestTime);
                    }
                }
            }

            cancelQuestionTimer();

            QuestionVO question = report.getQuestions().get(newQuestionNo);
            AnswerVO answer = report.getAnswerByQuestionId(question.getId());

            questionNoLabel.setText("Вопрос " + (newQuestionNo + 1));

            QuestionPortlet questionPortlet = initQuestionPortlet(question);
            questionPortlet.setValue(answer);
            questionPortlet.setEnabled(!report.isQuestionAnswered(question
                    .getId()));
            centerPanel.setWidget(questionPortlet);

            ArrayList<TextButton> buttons = new ArrayList<TextButton>();
            if (publication.isAllowInterruptTest()) {
                buttons.add(interruptTestButton);
            }
            if (!report.isQuestionAnswered(question.getId())) {
                answerQuestionButton.setEnabled(false);
                buttons.add(answerQuestionButton);
            }
            if (publication.isAllowSkipQuestions()) {
                if (this.questionNo >= 0) {
                    TextButton curNavButton = navigationButtons
                            .get(this.questionNo);
                    curNavButton.setHTML(navButtonHTML(this.questionNo, false));
                }

                if (newQuestionNo >= 0) {
                    TextButton newNavButton = navigationButtons
                            .get(newQuestionNo);
                    newNavButton.setHTML(navButtonHTML(newQuestionNo, true));
                }

                buttons.add(skipQuestionButton);
            }
            showButtons(buttons.toArray(new TextButton[0]));

            long questionTime = questionTimerTime(newQuestionNo);
            if (questionTime > 0) {
                startQuestionTimer(questionTime);
            }

            this.questionNo = newQuestionNo;
            break;

        case report:
            report.setFinished(true);
            Player.rpc.finishReport(getTenantId(), report.getId(), report.isThisInterrupted(),
                    new PlayerAsyncCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            History.newItem(newToken("report", "id",
                                    report.getId()));
                        }
                    });
            break;
        }

        mainPanel.forceLayout();
        state = newState;
    }

    private String testTimerText(long sec) {
        return "До конца теста - " + DateHelper.formatTimeValue(sec);
    }

    private String questionTimerText(long sec) {
        return "На этот вопрос - " + DateHelper.formatTimeValue(sec);
    }

    private void switchToNextQuestionOrReport() {
        int no = nextUnansweredQuestion(questionNo);

        boolean interrupt = false;
        if (report.getPublication().getThisMinScore() > 0
                && report.getPublication().isThisInterruptOnFalure()) {

            interrupt = ReportHelper.getPossibleScore(report, no) < 
                    report.getPublication().getThisMinScore();
        }

        if (interrupt) {
            report.setInterrupted(true);
            switchTo(State.report);
        }

        if (!publication.isAllowSkipQuestions()) {
            if (no < questionNo || no < 0) {
                switchTo(State.report);
            } else {
                switchTo(State.question, no);
            }
        } else {
            if (no >= 0) {
                switchTo(State.question, no);
            } else {
                switchTo(State.report);
            }
        }
    }

    private void startQuestionTimer(final long ms) {
        cancelQuestionTimer();

        final long s = ms / 1000;

        String text = questionTimerText(s);
        questionTimeLabel.setText(text);

        questionTimer = new Timer() {
            private long sec = s;

            @Override
            public void run() {
                String text = questionTimerText(sec);
                questionTimeLabel.setText(text);

                sec--;

                if (sec <= 0) {
                    cancel();
                    switchToNextQuestionOrReport();
                }
            }
        };

        questionTimer.scheduleRepeating(1000);
    }

    private void startTestTimer(final long ms) {
        cancelTestTimer();

        final long s = ms / 1000;

        String text = testTimerText(s);
        testTimeLabel.setText(text);

        testTimer = new Timer() {
            private long sec = s;

            @Override
            public void run() {
                String text = testTimerText(sec);
                testTimeLabel.setText(text);

                sec--;

                if (sec <= 0) {
                    cancel();
                    switchTo(State.report);
                }
            }
        };

        testTimer.scheduleRepeating(1000);
    }

    private void cancelTestTimer() {
        if (testTimer != null) {
            testTimer.cancel();
            testTimer = null;
        }
        testTimeLabel.setText("");
    }

    private void cancelQuestionTimer() {
        if (questionTimer != null) {
            questionTimer.cancel();
            questionTimer = null;
        }
        questionTimeLabel.setText("");
    }

    private long questionTimerTime(int no) {
        if (publication.isAllowSkipQuestions()) {
            return 0;
        }
        if (report.getQuestions().get(no).getMaxQuestionAnswerTime() != null) {
            return report.getQuestions().get(no)
                    .getMaxQuestionAnswerTime();
        } else if (publication.getMaxQuestionAnswerTime() != null) {
            return publication.getMaxQuestionAnswerTime();
        }
        return 0;
    }

    private boolean isPublicationStateNeeded() {

        boolean result = !StringHelper.isEmptyOrNull(publication
                .getIntroduction());
        result = result || publication.isAskFirstName();
        result = result || publication.isAskLastName();
        result = result || publication.isAskMiddleName();
        result = result || publication.isAskEmail();
        result = result || !publication.getAdditionalQuestions().isEmpty();

        return result;
    }

    private void initTopPanel() {
        HBoxLayoutContainer hbl = new HBoxLayoutContainer();

        BoxLayoutData bl = null;

        bl = new BoxLayoutData(new Margins(0, 5, 0, 0));
        bl.setFlex(1);
        hbl.add(questionNoLabel, bl);

        bl = new BoxLayoutData(new Margins(0, 5, 0, 0));
        bl.setFlex(1);
        hbl.add(questionTimeLabel, bl);

        bl = new BoxLayoutData(new Margins(0, 5, 0, 0));
        bl.setFlex(1);
        hbl.add(testTimeLabel, bl);

        topPanel.add(hbl);
    }

    private final SelectHandler navigationButtonHandler = new SelectHandler() {
        @Override
        public void onSelect(SelectEvent event) {
            TextButton btn = (TextButton) event.getSource();
            btn.hideToolTip();
            Integer no = btn.getData("no");
            switchTo(State.question, no);
        }
    };

    private void addNavigationButtons() {
        FlowLayoutContainer layout = new FlowLayoutContainer();
        MarginData layoutData = new MarginData(new Margins(5, 0, 0, 5));
        for (int i = 0; i < report.getQuestions().size(); i++) {
            QuestionVO question = report.getQuestions().get(i);
            TextButton btn = new TextButton();
            btn.setHeight(22);
            btn.setMinWidth(48);
            btn.setData("no", new Integer(i));
            btn.setHTML(navButtonHTML(i, false));
            btn.setToolTip(question.getText());
            if (report.isQuestionAnswered(question.getId())) {
                btn.setIcon(Resources.ICONS.checkBoxChecked16x16());
            }
            else {
                btn.setIcon(Resources.ICONS.checkBoxUnchecked16x16());
            }
            btn.setIconAlign(IconAlign.RIGHT);
            btn.addSelectHandler(navigationButtonHandler);
            layout.add(btn, layoutData);
            navigationButtons.add(btn);
        }
        navigationPanel.setWidget(layout);
        navigationPanel.show();
    }

    private String navButtonHTML(int questionNo, boolean current) {
        if (current) {
            return "<div class='"
                    + Resources.STYLES.testScreenCss().navButtonCurrent()
                    + "'>" + (questionNo + 1) + "</div>";
        } else {
            return "<div>" + (questionNo + 1) + "</div>";
        }
    }

    public ReportVO getReport() {
        return report;
    }
}
