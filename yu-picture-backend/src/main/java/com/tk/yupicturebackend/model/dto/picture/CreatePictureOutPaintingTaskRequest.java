package com.tk.yupicturebackend.model.dto.picture;

import com.tk.yupicturebackend.api.aliyunai.model.CreateOutPaintingTaskRequest;

import java.io.Serializable;

public class CreatePictureOutPaintingTaskRequest implements Serializable {
    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 扩图参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;

    private static final long serialVersionUID = 1L;


    public Long getPictureId() {
        return pictureId;
    }

    public void setPictureId(Long pictureId) {
        this.pictureId = pictureId;
    }

    public CreateOutPaintingTaskRequest.Parameters getParameters() {
        return parameters;
    }

    public void setParameters(CreateOutPaintingTaskRequest.Parameters parameters) {
        this.parameters = parameters;
    }
}
