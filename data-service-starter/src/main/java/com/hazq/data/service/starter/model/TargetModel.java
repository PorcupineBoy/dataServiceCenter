package com.hazq.data.service.starter.model;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TargetModel  implements Serializable {
    private String id;
    private String attrCode;
    private String attrName;
    private String targetOperationRule;
}
