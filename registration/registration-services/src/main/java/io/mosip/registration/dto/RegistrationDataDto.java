package io.mosip.registration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import io.mosip.registration.dto.packetmanager.DocumentDto;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistrationDataDto {
	
	private String name;
	private String email;
	private String phone;
	private String langCode;

	private String docs;

	public void setAdditionalDocumentInfo(Map<String, DocumentDto> documents){
		Map<String,String> docs = new HashMap<>();
		documents.forEach((field_id, doc)->{
			String docString = new String(doc.getDocument());
			docs.put(field_id, docString);
		});
		try {
			this.setDocs(JsonUtils.javaObjectToJsonString(docs));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
