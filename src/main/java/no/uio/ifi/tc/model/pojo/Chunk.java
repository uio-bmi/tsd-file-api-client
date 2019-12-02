package no.uio.ifi.tc.model.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class Chunk extends TSDFileAPIResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("filename")
    private String fileName;

    @SerializedName("max_chunk")
    private String maxChunk;

}
