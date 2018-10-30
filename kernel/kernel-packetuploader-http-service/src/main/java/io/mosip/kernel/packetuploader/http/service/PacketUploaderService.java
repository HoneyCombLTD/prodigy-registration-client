package io.mosip.kernel.packetuploader.http.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.core.exception.IOException;
import io.mosip.kernel.packetuploader.http.dto.PacketUploaderResponceDTO;

/**
 * Service interface for packet uploader
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 */
@Service
public interface PacketUploaderService {

	/**
	 * This stores packet into configured location
	 * 
	 * @param packet
	 *            packet to store
	 * @return {@link PacketUploaderResponceDTO} with file information
	 * @throws IOException 
	 */
	PacketUploaderResponceDTO storePacket(MultipartFile packet) throws IOException;

}
