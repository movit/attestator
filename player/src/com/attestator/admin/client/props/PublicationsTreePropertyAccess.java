package com.attestator.admin.client.props;

import com.attestator.common.shared.helper.DateHelper;
import com.attestator.common.shared.helper.NullHelper;
import com.attestator.common.shared.vo.AdditionalQuestionVO;
import com.attestator.common.shared.vo.BaseVO;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.PublicationsTreeItem;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;

public class PublicationsTreePropertyAccess {
    
    public ValueProvider<PublicationsTreeItem, MetaTestVO> metatest = new GetterValueProvider<PublicationsTreeItem, MetaTestVO>(
            "metatest") {
        @Override
        public MetaTestVO getValue(PublicationsTreeItem object) {
            if (object instanceof PublicationVO) {
                return ((PublicationVO) object).getMetatest();
            }
            return null;
        }
    };

    public ValueProvider<PublicationsTreeItem, String> name = new GetterValueProvider<PublicationsTreeItem, String>(
            "name") {
        @Override
        public String getValue(PublicationsTreeItem object) {
            if (object instanceof MetaTestVO) {
                return ((MetaTestVO) object).getName();
            }
            return null;
        }
    };
    
    public ValueProvider<PublicationsTreeItem, String> metatestId = new GetterValueProvider<PublicationsTreeItem, String>(
            "metatestId") {
        @Override
        public String getValue(PublicationsTreeItem object) {
            if (object instanceof PublicationVO) {
                return ((PublicationVO) object).getMetatestId();
            }
            return null;
        }
    };

    public ValueProvider<PublicationsTreeItem, String> metatestName = new GetterValueProvider<PublicationsTreeItem, String>(
            "metatest.name") {
        @Override
        public String getValue(PublicationsTreeItem object) {
            if (object instanceof PublicationVO) {
                return ((PublicationVO) object).getMetatest().getName();
            }
            return null;
        }
    };

    public ValueProvider<PublicationsTreeItem, Long> reportsCount = new GetterValueProvider<PublicationsTreeItem, Long>(
            "reportsCount") {
        @Override
        public Long getValue(PublicationsTreeItem object) {
            if (object instanceof PublicationVO) {
                return ((PublicationVO) object).getReportsCount();
            }
            return null;
        }
    };

    public ValueProvider<PublicationsTreeItem, String> fillBeforeTest = new GetterValueProvider<PublicationsTreeItem, String>(
            "fillBeforeTest") {
        @Override
        public String getValue(PublicationsTreeItem object) {
            if (object instanceof PublicationVO) {
                StringBuilder sb = new StringBuilder();
                
                PublicationVO publication = (PublicationVO) object;
                boolean someQuestions = false;                
                
                if (publication.isThisAskLastName()) {
                    sb.append("Фамилию");
                    if (publication.isThisAskLastNameRequired()) {
                        sb.append("*");
                    }
                    someQuestions = true;
                }
                
                if (publication.isThisAskFirstName()) {
                    if (someQuestions) {
                        sb.append(", ");
                    }
                    sb.append("Имя");
                    if (publication.isThisAskFirstNameRequired()) {
                        sb.append("*");
                    }
                    someQuestions = true;
                }
                
                if (publication.isThisAskMiddleName()) {
                    if (someQuestions) {
                        sb.append(", ");
                    }
                    sb.append("Отчество");
                    if (publication.isThisAskMiddleNameRequired()) {
                        sb.append("*");
                    }                
                    someQuestions = true;
                }
                
                if (publication.isThisAskEmail()) {
                    if (someQuestions) {
                        sb.append(", ");
                    }
                    sb.append("Email");
                    if (publication.isThisAskEmailRequired()) {
                        sb.append("*");
                    }                
                    someQuestions = true;
                }
                
                for (AdditionalQuestionVO aq : publication.getAdditionalQuestions()) {
                    if (someQuestions) {
                        sb.append(", ");
                    }
                    sb.append(aq.getText());
                    if (aq.isThisRequired()) {
                        sb.append("*");
                    }                
                    someQuestions = true;
                }
                
                if (!someQuestions) {
                    sb.append("ничего не нужно");
                }
                
                return sb.toString();
            }
                        
            return null;
        }
    };
    
    public ValueProvider<PublicationsTreeItem, String> start = new GetterValueProvider<PublicationsTreeItem, String>(
            "start") {
        @Override
        public String getValue(PublicationsTreeItem object) {
            if (object instanceof PublicationVO) {
                if (((PublicationVO) object).getStart()!= null) {
                    return DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT).format(((PublicationVO) object).getStart());
                }
                else {
                    return "не указано";
                }
            }            
            return null;
        }
    };

    public ValueProvider<PublicationsTreeItem, String> end = new GetterValueProvider<PublicationsTreeItem, String>(
            "end") {
        @Override
        public String getValue(PublicationsTreeItem object) {
            if (object instanceof PublicationVO) {
                if (((PublicationVO) object).getEnd()!= null) {
                    return DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT).format(((PublicationVO) object).getEnd());
                }
                else {
                    return "не указано";
                }
            }            
            return null;
        }
    };

    public ValueProvider<PublicationsTreeItem, String> introduction = new 
            GetterValueProvider<PublicationsTreeItem, String>("introduction") {
        @Override
        public String getValue(PublicationsTreeItem object) {
            if (object instanceof PublicationVO) {
                return ((PublicationVO) object).getIntroduction();
            }
            return null;
        }
    };
    
     public ValueProvider<PublicationsTreeItem, String> maxAttempts = new
             GetterValueProvider<PublicationsTreeItem, String>("maxAttempts") {
         @Override
         public String getValue(PublicationsTreeItem object) {
             if (object instanceof PublicationVO) {
                 if (NullHelper.nullSafeIntegerOrZerro(((PublicationVO) object).getMaxAttempts()) > 0) {
                     return ((PublicationVO) object).getMaxAttempts().toString();
                 }
                 else {
                     return "неограничено";
                 }
             }
             return null;
         }
     };
    
     public ValueProvider<PublicationsTreeItem, String> minScore = new
             GetterValueProvider<PublicationsTreeItem, String>("minScore") {
         @Override
         public String getValue(PublicationsTreeItem object) {
             if (object instanceof PublicationVO) {
                 if (NullHelper.nullSafeLongOrZerro(((PublicationVO) object).getMinScore()) > 0) {
                     return ((PublicationVO) object).getMinScore().toString();
                 }
                 else {
                     return "неважно";
                 }                 
             }
             return null;
         }
     };
    
     public ValueProvider<PublicationsTreeItem, Boolean> interruptOnFalure = new
             GetterValueProvider<PublicationsTreeItem, Boolean>("interruptOnFalure") {
         @Override
         public Boolean getValue(PublicationsTreeItem object) {
             if (object instanceof PublicationVO) {
                 return ((PublicationVO) object).getInterruptOnFalure();
             }
             return null;
         }
     };
    
     public ValueProvider<PublicationsTreeItem, String> maxTakeTestTime = new
             GetterValueProvider<PublicationsTreeItem, String>("maxTakeTestTime") {
         @Override
         public String getValue(PublicationsTreeItem object) {
             if (object instanceof PublicationVO) {
                 if (NullHelper.nullSafeLongOrZerro(((PublicationVO) object).getMaxTakeTestTime()) > 0) {
                     return DateHelper.formatTimeValue(((PublicationVO) object).getMaxTakeTestTime() / 1000);
                 }
                 else {
                     return "неограничено";
                 }                 
             }
             return null;
         }
     };
     
     public ValueProvider<PublicationsTreeItem, Long> maxQuestionAnswerTime = new
             GetterValueProvider<PublicationsTreeItem, Long>("maxQuestionAnswerTime") {
         @Override
         public Long getValue(PublicationsTreeItem object) {
             if (object instanceof PublicationVO) {
                 return ((PublicationVO) object).getMaxQuestionAnswerTime();
             }
             return null;
         }
     };
    
     public ValueProvider<PublicationsTreeItem, Boolean> allowSkipQuestions = new
             GetterValueProvider<PublicationsTreeItem, Boolean>("allowSkipQuestions") {
         @Override
         public Boolean getValue(PublicationsTreeItem object) {
             if (object instanceof PublicationVO) {
                 return ((PublicationVO) object).getAllowSkipQuestions();
             }
             return null;
         }
     };
     
     public ValueProvider<PublicationsTreeItem, Boolean> allowInterruptTest = new
             GetterValueProvider<PublicationsTreeItem, Boolean>("allowInterruptTest") {
         @Override
         public Boolean getValue(PublicationsTreeItem object) {
             if (object instanceof PublicationVO) {
                 return ((PublicationVO) object).getAllowInterruptTest();
             }
             return null;
         }
     };
     
     public ValueProvider<PublicationsTreeItem, Boolean> randomQuestionsOrder = new
             GetterValueProvider<PublicationsTreeItem, Boolean>("randomQuestionsOrder") {
         @Override
         public Boolean getValue(PublicationsTreeItem object) {
             if (object instanceof PublicationVO) {
                 return ((PublicationVO) object).getRandomQuestionsOrder();
             }
             return null;
         }
     };
    
     public ValueProvider<PublicationsTreeItem, Boolean> askFirstName = new
             GetterValueProvider<PublicationsTreeItem, Boolean>("askFirstName") {
         @Override
         public Boolean getValue(PublicationsTreeItem object) {
             if (object instanceof PublicationVO) {
                 return ((PublicationVO) object).getAskFirstName();
             }
             return null;
         }
     };
     
     public ValueProvider<PublicationsTreeItem, Boolean> askFirstNameRequired = new
             GetterValueProvider<PublicationsTreeItem, Boolean>("askFirstNameRequired") {
         @Override
         public Boolean getValue(PublicationsTreeItem object) {
             if (object instanceof PublicationVO) {
                 return ((PublicationVO) object).getAskFirstNameRequired();
             }
             return null;
         }
     };
    
     public ValueProvider<PublicationsTreeItem, Boolean> askLastName = new
             GetterValueProvider<PublicationsTreeItem, Boolean>("askLastName") {
         @Override
         public Boolean getValue(PublicationsTreeItem object) {
             if (object instanceof PublicationVO) {
                 return ((PublicationVO) object).getAskLastName();
             }
             return null;
         }
     };
     
     public ValueProvider<PublicationsTreeItem, Boolean> askLastNameRequired = new
             GetterValueProvider<PublicationsTreeItem, Boolean>("askLastNameRequired") {
         @Override
         public Boolean getValue(PublicationsTreeItem object) {
             if (object instanceof PublicationVO) {
                 return ((PublicationVO) object).getAskLastNameRequired();
             }
             return null;
         }
     };
    
     public ValueProvider<PublicationsTreeItem, Boolean> askMiddleName = new
             GetterValueProvider<PublicationsTreeItem, Boolean>("askMiddleName") {
         @Override
         public Boolean getValue(PublicationsTreeItem object) {
             if (object instanceof PublicationVO) {
                 return ((PublicationVO) object).getAskMiddleName();
             }
             return null;
         }
     };
     
     public ValueProvider<PublicationsTreeItem, Boolean> askMiddleNameRequired = new
             GetterValueProvider<PublicationsTreeItem, Boolean>("askMiddleNameRequired") {
         @Override
         public Boolean getValue(PublicationsTreeItem object) {
             if (object instanceof PublicationVO) {
                 return ((PublicationVO) object).getAskMiddleNameRequired();
             }
             return null;
         }
     };
    
     public ValueProvider<PublicationsTreeItem, Boolean> askEmail = new
             GetterValueProvider<PublicationsTreeItem, Boolean>("askEmail") {
         @Override
         public Boolean getValue(PublicationsTreeItem object) {
             if (object instanceof PublicationVO) {
                 return ((PublicationVO) object).getAskEmail();
             }
             return null;
         }
     };
     
     public ValueProvider<PublicationsTreeItem, Boolean> askEmailRequired = new
             GetterValueProvider<PublicationsTreeItem, Boolean>("askEmailRequired") {
         @Override
         public Boolean getValue(PublicationsTreeItem object) {
             if (object instanceof PublicationVO) {
                 return ((PublicationVO) object).getAskEmailRequired();
             }
             return null;
         }
     };
    
     public ModelKeyProvider<PublicationsTreeItem> id = new ModelKeyProvider<PublicationsTreeItem>() {
        @Override
        public String getKey(PublicationsTreeItem item) {
            return ((BaseVO)item).getId();
        }
     };
}
