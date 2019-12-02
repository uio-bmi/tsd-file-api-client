package no.uio.ifi.tc.model.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class Token extends TSDFileAPIResponse {

    private String token;

}
