package com.attestator.admin.server;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;

import com.attestator.admin.server.csv.CSVFormat;
import com.attestator.admin.server.csv.CSVParser;
import com.attestator.admin.server.csv.CSVRecord;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.ChoiceVO;
import com.attestator.common.shared.vo.GroupVO;
import com.attestator.common.shared.vo.SingleChoiceQuestionVO;
import com.attestator.player.server.Singletons;

public class UploadQuestionsServlet extends BaseFileUploadServlet {
    private static final long serialVersionUID = 4322264137509699010L;
    
    public static class QuestionsParseException extends UploadException{
        private static final long serialVersionUID = -128676724393925838L;

        public QuestionsParseException() {
            super();
        }

        public QuestionsParseException(String message, Throwable cause) {
            super(message, cause);
        }

        public QuestionsParseException(String message) {
            super(message);
        }

        public QuestionsParseException(Throwable cause) {
            super(cause);
        }
        
    }
    
    private static final Logger logger = Logger
            .getLogger(UploadQuestionsServlet.class);

    @Override
    public void process(InputStream upload) throws QuestionsParseException {
        try {
            InputStreamReader reader = new InputStreamReader(upload);
            CSVFormat format = CSVFormat.EXCEL.toBuilder().withHeader().withDelimiter(';').build();
            CSVParser parser = new CSVParser(reader, format);

            Map<String, Integer> header = parser.getHeaderMap();
            if (header.size() < 5) {
                throw new QuestionsParseException("Incorrect CSV format. Should be minimum 5 collumns. (groupId, questionId, questionText, varian2, rightAnswerId)");
            }
            
            GroupVO defaultGroup = Singletons.al().getDeafultGroup();
            
            int groupIdPos = 0;
            int questionIdPos = 1;
            int questionTextPos = 2;
            int answerPos = header.size() - 1;            
            
            logger.info("Parsing questions file ...");
            
            for (CSVRecord record: parser.getRecords()) {
                // Parse and validate line
                String groupId = record.get(groupIdPos).trim();
                String groupName = groupId;
                if (!StringHelper.isEmptyOrNull(groupId)) {
                    // Guarantee group id be unique 
                    groupId = LoginManager.getThreadLocalTenatId() + groupId;
                    
                    GroupVO group = Singletons.al().getById(GroupVO.class, groupId);
                    if (group == null) {
                        group = new GroupVO();
                        group.setId(groupId);
                        group.setName(groupName);
                        Singletons.al().saveGroup(group);
                    }
                }
                else {
                    logger.warn("Empty GROUP_ID in column " + groupIdPos + " set GROUP_ID to default");
                    groupId = defaultGroup.getId();                    
                }                
                
                String questionId = record.get(questionIdPos).trim();
                if (StringHelper.isEmptyOrNull(questionId)) {
                    logger.warn("Empty QUESTION_ID in column " + questionIdPos + " skip this line");
                    continue;
                }
                else {
                    questionId = LoginManager.getThreadLocalTenatId() + questionId;
                }
                
                String questionText = record.get(questionTextPos).trim();                
                if (StringHelper.isEmptyOrNull(questionText)) {
                    logger.warn("Empty QUESTION_TEXT in column " + questionTextPos + ". Skip this line");
                    continue;
                }

                ArrayList<String> answers = new ArrayList<String>();
                for (int i = 3; i < answerPos; i++) {
                    String answerText = record.get(i).trim();
                    if (StringHelper.isNotEmptyOrNull(answerText)) {
                        answers.add(answerText);
                    }
                }         
                
                if (answers.size() < 1) {
                    logger.warn("No answers found. Skip this line");
                    continue;
                }
                
                String rightAnswerId = record.get(answerPos).trim();
                if (StringHelper.isEmptyOrNull(rightAnswerId)) {
                    logger.warn("Empty RIGHT_ANSWER_ID in column " + answerPos + ". Skip this line");
                    continue;
                }
                
                String rightAnswerMarker = rightAnswerId + ")";
                int rightAnswerIndex = -1;
                for (int i = 0; i < answers.size(); i++) {
                    String answer = answers.get(i); 
                    if (answer.startsWith(rightAnswerMarker)) {
                        rightAnswerIndex = i;
                    }
                    // Remove marker
                    answer = answer.replaceFirst("[^\\)]+\\)", "").trim();
                    answers.set(i, answer);
                }
                
                if (rightAnswerIndex < 0) {
                    logger.warn("Can't find answer marked by " + rightAnswerMarker + ". Skip this line");
                    continue;
                }
                
                // Build question for save
                
                SingleChoiceQuestionVO question = Singletons.al().getById(SingleChoiceQuestionVO.class, questionId);
                if (question == null) {
                    question = new SingleChoiceQuestionVO();
                    question.setId(questionId);
                }
                
                question.setText(questionText);
                question.setGroupId(groupId);
                question.setChoices(new ArrayList<ChoiceVO>());
                for (int i = 0; i < answers.size(); i++) {
                    String answer = answers.get(i); 
                    ChoiceVO choice = new ChoiceVO();
                    choice.setText(answer);
                    if (i == rightAnswerIndex) { 
                        choice.setRight(true);
                    }
                    question.getChoices().add(choice);
                }
                
                // Save question
                Singletons.al().saveQuestion(question);
            }
            logger.info("Questions file parsing finished.");
            
        } catch (Throwable e) {
            throw new QuestionsParseException(e);
        }
    }
}
