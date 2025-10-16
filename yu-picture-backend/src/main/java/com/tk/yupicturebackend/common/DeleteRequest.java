package com.tk.yupicturebackend.common;

import lombok.Data;
//删除请求类
import java.io.Serializable;

@Data
public class DeleteRequest implements Serializable {
    private Long id;
    private static final long serialVersionUID = 1L;
}
