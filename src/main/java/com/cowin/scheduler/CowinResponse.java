package com.cowin.scheduler;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Setter
@Getter
public class CowinResponse {
    private List<Centers> centers;
}
