package project.votebackend.type;

import lombok.Getter;

@Getter
public enum Grade {
    HUNDRED("백장"),
    THOUSAND("천장"),
    TEN_THOUSAND("만장"),
    HUNDRED_THOUSAND("십만장"),
    MILLION("백만장"),
    TEN_MILLION("천만장");

    private final String label;

    Grade(String label) {
        this.label = label;
    }

    public static Grade fromAverage(long avg) {
        if (avg < 100) return HUNDRED;
        else if (avg < 1_000) return THOUSAND;
        else if (avg < 10_000) return TEN_THOUSAND;
        else if (avg < 100_000) return HUNDRED_THOUSAND;
        else if (avg < 1_000_000) return MILLION;
        else return TEN_MILLION;
    }
}
