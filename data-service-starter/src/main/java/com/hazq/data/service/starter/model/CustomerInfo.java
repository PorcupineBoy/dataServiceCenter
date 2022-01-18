package com.hazq.data.service.starter.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;

@Setter
@Getter
public class CustomerInfo  implements Serializable {
    private String clientId;

    private HashMap<String, String> tags;
}
