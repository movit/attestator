package com.attestator.common.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.attestator.admin.server.LoginManager;
import com.attestator.common.shared.helper.CheckHelper;
import com.attestator.common.shared.vo.BaseVO;
import com.attestator.common.shared.vo.CacheType;
import com.attestator.common.shared.vo.ChangeMarkerVO;
import com.attestator.common.shared.vo.MTEGroupVO;
import com.attestator.common.shared.vo.MTEQuestionVO;
import com.attestator.common.shared.vo.MetaTestEntryVO;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.SingleChoiceQuestionVO;
import com.attestator.player.server.Singletons;
import com.google.code.morphia.query.Query;

public class CommonLogic {
    private static final Logger logger = Logger.getLogger(CommonLogic.class);
    
    public <T extends BaseVO> T getById(Class<T> clazz, String id) {
        CheckHelper.throwIfNull(clazz, "clazz");
        CheckHelper.throwIfNullOrEmpty(id, "id");
        
        Query<T> q = Singletons.ds().createQuery(clazz);
        q.field("_id").equal(id);
        T result = q.get();
        return result;
    }
    
    public List<String> getQuestionIdsByGroupId(String groupId) {
        CheckHelper.throwIfNullOrEmpty(groupId, "groupId");        
        
        List<String>      result = new ArrayList<String>();
        Query<QuestionVO> q      = Singletons.ds().createQuery(QuestionVO.class);
        
        if (groupId != null) {
            q.field("groupId").equal(groupId);
        }
        else {
            q.field("groupId").doesNotExist();
        }
        
        List<QuestionVO> questions = q.asList();
        for (QuestionVO question: questions) {
            result.add(question.getId());
        }
        
        return result;
    }
    
    
    public List<QuestionVO> generateQuestionsList(MetaTestVO metatest, boolean randomQuestionsOrder) {
        CheckHelper.throwIfNull(metatest, "metatest");
        
        List<QuestionVO> result = new ArrayList<QuestionVO>();
        
        // Prepare question by group mapping
        Map<String, List<String>> questionsIdsByGroup = new HashMap<String, List<String>>();
        for (MetaTestEntryVO mte: metatest.getEntries()) {
            if (mte instanceof MTEGroupVO) {
                String mteGroupId = ((MTEGroupVO) mte).getGroupId();
                if (questionsIdsByGroup.get(mteGroupId) == null) {
                    List<String> mteGroupQuestions = getQuestionIdsByGroupId(mteGroupId);
                    Collections.shuffle(mteGroupQuestions);
                    questionsIdsByGroup.put(mteGroupId, mteGroupQuestions);
                }
            }
        }
        
        // Fill questions in the test
        for (MetaTestEntryVO mte: metatest.getEntries()) {
            if (mte instanceof MTEQuestionVO) {
                String mteQuestionId = ((MTEQuestionVO) mte).getQuestionId();
                if (mteQuestionId == null) {
                    continue;
                }
                //TODO do we allow question duplication if user add one question to test several times
//                if (testQuestionIds.contains(mteQuestionId)) {
//                    continue;
//                }
                
                QuestionVO question = getById(QuestionVO.class, mteQuestionId); 
                if (question == null) {
                    continue;
                }                
                
                // Remove question from available group source
                if (question.getGroupId() != null) {
                    List<String> availableGroupQuestions = questionsIdsByGroup.get(question.getGroupId());
                    if (availableGroupQuestions != null) {
                        availableGroupQuestions.remove(question.getId());
                    }
                }
                
                result.add(question);
            } 
            else if (mte instanceof MTEGroupVO) {
                MTEGroupVO      mteGroup   = ((MTEGroupVO) mte);
                List<String>    availableGroupQuestions = questionsIdsByGroup.get(mteGroup.getGroupId());
                
                int numOfQuestionsToAdd = 0;
                if (mteGroup.getNumberOfQuestions() != null) {
                    numOfQuestionsToAdd = Math.min(mteGroup.getNumberOfQuestions(), availableGroupQuestions.size()); 
                }
                else {
                    numOfQuestionsToAdd = availableGroupQuestions.size();
                }
                        
                for (int i = numOfQuestionsToAdd - 1; i >= 0; i--) {
                    String     questionId  = availableGroupQuestions.remove(i);                    
                    QuestionVO question    = getById(QuestionVO.class, questionId);
                    
                    result.add(question);
                }   
            }
            else {
                logger.warn("Unsupported metatest entry " + mte.getClass().getName() + " skip for now.");
            }
        }
        
        // Shuffle questions, choices etc
        prepare(result, randomQuestionsOrder);
        
        return result;
    }
    
    private void prepare(List<QuestionVO> questions, boolean randomQuestionsOrder) {
        for (QuestionVO question: questions) {
            prepare(question);
        }
        
        if (randomQuestionsOrder) {
            Collections.shuffle(questions);
        }
    }

    private void prepare(QuestionVO question) {
        if (question instanceof SingleChoiceQuestionVO) {
            if (((SingleChoiceQuestionVO) question).isThisRandomChoiceOrder()) {
                Collections.shuffle(((SingleChoiceQuestionVO) question).getChoices());
            }
        }
    }

    
    protected void putGlobalChangesMarker() {
        putChangesMarker(null, null);
    }

    protected void putChangesMarker(String clientId, CacheType type, String ... entries) {
        ChangeMarkerVO marker = new ChangeMarkerVO(clientId, LoginManager.getThreadLocalTenatId(), type, entries);
        Singletons.ds().save(marker);
    }
}
