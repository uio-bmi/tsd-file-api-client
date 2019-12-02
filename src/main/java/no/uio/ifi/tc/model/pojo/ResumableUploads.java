package no.uio.ifi.tc.model.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class ResumableUploads extends TSDFileAPIResponse {

    private List<ResumableUpload> resumables;


}
