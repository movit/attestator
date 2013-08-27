package com.attestator.common.shared.helper;

import java.util.ArrayList;
import java.util.List;

import com.attestator.common.shared.vo.BaseVO;
import com.attestator.common.shared.vo.GroupVO;

public class VOHelper {
    public static <T extends BaseVO> List<String> getIds(List<T> vos) {
        ArrayList<String> result = new ArrayList<String>();
        if (NullHelper.isNotEmptyOrNull(vos)) {
            for (BaseVO vo: vos) {
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
    
}
