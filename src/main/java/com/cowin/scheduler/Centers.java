package com.cowin.scheduler;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Setter
@Getter
public class Centers {
    private String name;

    @JsonProperty("center_id")
    private Integer centerId;
    private List<Session> sessions;
}
