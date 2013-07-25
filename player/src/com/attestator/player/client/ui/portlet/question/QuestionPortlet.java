package com.attestator.player.client.ui.portlet.question;

import com.attestator.common.shared.vo.AnswerVO;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

public interface QuestionPortlet extends IsWidget, HasValue<AnswerVO>, HasEnabled {

}
