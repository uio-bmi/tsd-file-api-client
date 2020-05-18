package no.uio.ifi.tc.model.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class TSDFiles extends TSDFileAPIResponse {

    @SerializedName("files")
    private Collection<TSDFile> files;

    @SerializedName("page")
    private String page;

}
