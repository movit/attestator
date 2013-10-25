package com.attestator.common.shared.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.attestator.common.shared.vo.AdditionalQuestionVO;
import com.attestator.common.shared.vo.BaseVO;
import com.attestator.common.shared.vo.GroupVO;
import com.attestator.common.shared.vo.MTEGroupVO;
import com.attestator.common.shared.vo.MTEQuestionVO;
import com.attestator.common.shared.vo.MetaTestEntryVO;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.PublicationVO;

public class VOHelper {
    public static <T extends BaseVO> T getById(Collection<T> vos, String id) {
        if (StringHelper.isEmptyOrNull(id)) {
            return null;
        }
        if (NullHelper.isEmptyOrNull(vos)) {
            return null;
        }        
        for (T vo: vos) {
            if (id.equals(vo.getId())) {
                return vo;
            }
        }
        return null;
    }
    
    public static <T extends BaseVO> List<String> getIds(Collection<T> vos) {
        ArrayList<String> result = new ArrayList<String>();
        if (NullHelper.isNotEmptyOrNull(vos)) {
            for (T vo: vos) {
                result.add(vo.getId());
            }
        }
        return result;
    }
    
    public static List<String> getNames(List<GroupVO> vos) {
        ArrayList<String> result = new ArrayList<String>();
        if (NullHelper.isNotEmptyOrNull(vos)) {
            for (GroupVO vo: vos) {
                result.add(vo.getName());
            }
        }
        return result;
    }
    
    public static Set<String> getQuestionsIds(List<MetaTestEntryVO> entries) {
        HashSet<String> result = new HashSet<String>();
        if (entries != null) {
            for (MetaTestEntryVO entry: entries) {
                if (entry instanceof MTEQuestionVO && !StringHelper.isEmptyOrNull(((MTEQuestionVO) entry).getQuestionId())) {
                    result.add(((MTEQuestionVO) entry).getQuestionId());
                }
            }
        }
        return result;
    }
    
    public static Set<String> getGroupsIds(List<MetaTestEntryVO> entries) {
        HashSet<String> result = new HashSet<String>();
        if (entries != null) {
            for (MetaTestEntryVO entry: entries) {
                if (entry instanceof MTEGroupVO && !StringHelper.isEmptyOrNull(((MTEGroupVO) entry).getGroupId())) {
                    result.add(((MTEGroupVO) entry).getGroupId());
                }
            }
        }
        return result;
    }
    
    public static void copyMetatestForPublicationEditor(MetaTestVO dst, MetaTestVO src) {
        dst.setId(src.getId());
        dst.setCreated(src.getCreated());
        dst.setModified(src.getModified());
        dst.setTenantId(src.getTenantId());
        dst.setName(src.getName());
        dst.setNumberOfQuestions(dst.getNumberOfQuestions());
        dst.setEntries(null);
    }
    
    public static void copyAdditionalQuestionForEditor(AdditionalQuestionVO dst, AdditionalQuestionVO src) {
        dst.setId(src.getId());
        dst.setText(src.getText());
        dst.setCheckValue(src.getCheckValue());
        dst.setRequired(src.getRequired());
        dst.setOrder(src.getOrder());
        dst.setAnswerType(src.getAnswerType());        
    }
    
    public static void copyPublicationForEditor(PublicationVO dst, PublicationVO src) {
        dst.setId(src.getId());
        dst.setCreated(src.getCreated());
        dst.setModified(src.getModified());
        dst.setTenantId(src.getTenantId());
        dst.setMetatestId(src.getMetatestId());
        dst.setMetatest(src.getMetatest());
        dst.setReportsCount(src.getReportsCount());
        dst.setStart(src.getStart());
        dst.setEnd(src.getEnd());
        dst.setIntroduction(src.getIntroduction());
        dst.setMaxAttempts(src.getMaxAttempts());
        dst.setMinScore(src.getMinScore());    
        dst.setInterruptOnFalure(src.getInterruptOnFalure());
        dst.setMaxTakeTestTime(src.getMaxTakeTestTime());
        dst.setMaxQuestionAnswerTime(src.getMaxQuestionAnswerTime());
        dst.setAllowSkipQuestions(src.getAllowSkipQuestions()); 
        dst.setAllowInterruptTest(src.getAllowInterruptTest());
        dst.setRandomQuestionsOrder(src.getRandomQuestionsOrder());
        dst.setAskFirstName(src.getAskFirstName());
        dst.setAskFirstNameRequired(src.getAskFirstNameRequired());
        dst.setAskLastName(src.getAskLastName());
        dst.setAskLastNameRequired(src.getAskLastNameRequired());
        dst.setAskMiddleName(src.getAskMiddleName());
        dst.setAskMiddleNameRequired(src.getAskMiddleNameRequired());
        dst.setAskEmail(src.getAskEmail());
        dst.setAskEmailRequired(src.getAskEmailRequired());        
        if (src.getAdditionalQuestions() != null) {
            dst.setAdditionalQuestions(new ArrayList<AdditionalQuestionVO>());
            for (AdditionalQuestionVO srcAq: src.getAdditionalQuestions()) {
                AdditionalQuestionVO dstAq = new AdditionalQuestionVO();
                copyAdditionalQuestionForEditor(dstAq, srcAq);
                dst.getAdditionalQuestions().add(dstAq);
            }        
        }
        else {
            dst.setAdditionalQuestions(null);
        }
    }
    
    public static PublicationVO clonePublicationForEditor(PublicationVO publication) {
        PublicationVO result = new PublicationVO();
        copyPublicationForEditor(result, publication);
        return result;
    }
    public static MetaTestVO cloneMeatestForPublicationEditor(MetaTestVO metatest) {
        MetaTestVO result = new MetaTestVO();
        copyMetatestForPublicationEditor(result, metatest);
        return result;
    }
    
}
