package no.uio.ifi.tc.model.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@EqualsAndHashCode
@ToString
@Data
public class TSDFile {

    @SerializedName("filename")
    private String fileName;

    @SerializedName("size")
    private Long size;

    @SerializedName("modified_date")
    private String modifiedDate;

    @SerializedName("href")
    private String href;

    @SerializedName("exportable")
    private Boolean exportable;

    @SerializedName("reason")
    private String reason;

    @SerializedName("mime-type")
    private String mimeType;

    @SerializedName("owner")
    private String owner;

}
