package com.attestator.admin.client.props;

import java.util.Date;

import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface PublicationVOPropertyAccess extends PropertyAccess<PublicationVO> {
    ValueProvider<PublicationVO, MetaTestVO> metatest();

    ValueProvider<PublicationVO, String> metatestId();
    
    @Path("metatest.name")
    ValueProvider<PublicationVO, String> metatestName();

    ValueProvider<PublicationVO, Long> reportsCount();
    
    ValueProvider<PublicationVO, Date> start();
    ValueProvider<PublicationVO, Date> end();
    
    ValueProvider<PublicationVO, String> introduction();
    
    ValueProvider<PublicationVO, Integer> maxAttempts();
    
    ValueProvider<PublicationVO, Double> minScore();

    ValueProvider<PublicationVO, Boolean> interruptOnFalure();
    
    ValueProvider<PublicationVO, Long> maxTakeTestTime();
    ValueProvider<PublicationVO, Long> maxQuestionAnswerTime();
    
    ValueProvider<PublicationVO, Boolean> allowSkipQuestions();
    ValueProvider<PublicationVO, Boolean> allowInterruptTest();
    ValueProvider<PublicationVO, Boolean> randomQuestionsOrder();
    
    ValueProvider<PublicationVO, Boolean> askFirstName();
    ValueProvider<PublicationVO, Boolean> askFirstNameRequired();

    ValueProvider<PublicationVO, Boolean> askLastName();
    ValueProvider<PublicationVO, Boolean> askLastNameRequired();
    
    ValueProvider<PublicationVO, Boolean> askMiddleName();
    ValueProvider<PublicationVO, Boolean> askMiddleNameRequired();
    
    ValueProvider<PublicationVO, Boolean> askEmail();
    ValueProvider<PublicationVO, Boolean> askEmailRequired();
    
    ModelKeyProvider<PublicationVO> id();
}
