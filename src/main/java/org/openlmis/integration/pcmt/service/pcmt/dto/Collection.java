
package org.openlmis.integration.pcmt.service.pcmt.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "locale",
    "scope",
    "data"
})
public class Collection {

    @JsonProperty("locale")
    public Object locale;
    @JsonProperty("scope")
    public Object scope;
    @JsonProperty("data")
    public List<String> data = null;

}
