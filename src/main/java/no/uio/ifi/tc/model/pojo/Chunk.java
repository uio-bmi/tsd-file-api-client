package no.uio.ifi.tc.model.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Chunk {

    @SerializedName("id")
    private String id;

    @SerializedName("filename")
    private String fileName;

    @SerializedName("max_chunk")
    private String maxChunk;

}
