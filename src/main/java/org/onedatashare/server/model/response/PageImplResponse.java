package org.onedatashare.server.model.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

public class PageImplResponse<T> extends PageImpl<T> {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PageImplResponse(@JsonProperty("content") List<T> content,
                        @JsonProperty("number") int number,
                        @JsonProperty("size") int size,
                        @JsonProperty("totalElements") Long totalElements,
                        @JsonProperty("pageable") JsonNode pageable,
                        @JsonProperty("last") boolean last,
                        @JsonProperty("totalPages") int totalPages,
                        @JsonProperty("sort") JsonNode sort,
                        @JsonProperty("first") boolean first,
                        @JsonProperty("numberOfElements") int numberOfElements) {

        super(content, PageRequest.of(number, size), totalElements);
    }

    public PageImplResponse(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public PageImplResponse(List<T> content) {
        super(content);
    }

    public PageImplResponse() {
        super(new ArrayList<>());
    }
}