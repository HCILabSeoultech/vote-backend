package project.votebackend.util;

import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.Map;

public class PageResponseUtil {
    // Page<T> 객체를 프론트엔드에 전달하기 좋은 형태(Map)로 변환하는 유틸 메서드
    public static <T> Map<String, Object> toResponse(Page<T> page) {
        Map<String, Object> response = new HashMap<>();

        // 현재 페이지의 데이터 리스트
        response.put("content", page.getContent());

        // 현재 페이지 번호 (0부터 시작)
        response.put("page", page.getNumber());

        // 페이지 당 데이터 개수
        response.put("size", page.getSize());

        // 전체 데이터 개수
        response.put("totalElements", page.getTotalElements());

        // 전체 페이지 수
        response.put("totalPages", page.getTotalPages());

        // 현재 페이지가 마지막 페이지인지 여부
        response.put("last", page.isLast());

        return response;
    }
}
