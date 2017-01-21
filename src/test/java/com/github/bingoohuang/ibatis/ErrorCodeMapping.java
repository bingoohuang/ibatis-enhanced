package com.github.bingoohuang.ibatis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class ErrorCodeMapping {
    private String sspCode;
    private String uniCode;
    private String desc;
}
