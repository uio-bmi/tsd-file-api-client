package no.uio.ifi.tc.model.pojo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class ResumableUploads {

    private List<ResumableUpload> resumables;


}
