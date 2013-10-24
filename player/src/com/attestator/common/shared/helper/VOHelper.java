package com.attestator.common.shared.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.attestator.common.shared.vo.BaseVO;
import com.attestator.common.shared.vo.GroupVO;
import com.attestator.common.shared.vo.MTEGroupVO;
import com.attestator.common.shared.vo.MTEQuestionVO;
import com.attestator.common.shared.vo.MetaTestEntryVO;

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
}
