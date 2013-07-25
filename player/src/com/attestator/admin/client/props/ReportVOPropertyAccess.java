package com.attestator.admin.client.props;

import java.util.Date;

import com.attestator.common.shared.vo.ReportVO;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface ReportVOPropertyAccess extends PropertyAccess<ReportVO> {
    ValueProvider<ReportVO, String> firstName();
    ValueProvider<ReportVO, String> lastName();
    ValueProvider<ReportVO, String> middleName();
    ValueProvider<ReportVO, String> email();

    ValueProvider<ReportVO, String> metatestName();
    ValueProvider<ReportVO, Date> start();
    ValueProvider<ReportVO, Date> end();
    ValueProvider<ReportVO, Boolean> finished();
    ValueProvider<ReportVO, String> clientId();
    ValueProvider<ReportVO, String> host();

    ValueProvider<ReportVO, Double> score();
    ValueProvider<ReportVO, Integer> numErrors();
    ValueProvider<ReportVO, Integer> numAnswers();
    ValueProvider<ReportVO, Integer> numUnanswered();
    
    ModelKeyProvider<ReportVO> id();
}
