package project.votebackend.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    MALE("male"),
    FEMALE("female");

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    @JsonValue // JSON 변환 시 해당 문자열 사용
    public String getValue() {
        return value;
    }

    @JsonCreator // JSON 요청 시 "male" → MALE, "female" → FEMALE 자동 변환
    public static Gender fromValue(String value) {
        for (Gender gender : Gender.values()) {
            if (gender.value.equalsIgnoreCase(value)) {
                return gender;
            }
        }
        return null;
    }
}
