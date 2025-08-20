package project.votebackend.dto.article;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class IngestPayload {

    private final Map<String, IngestClusterNode> clusters = new HashMap<>();

    @JsonAnySetter
    public void put(String key, IngestClusterNode value) {
        clusters.put(key, value);
    }
}
