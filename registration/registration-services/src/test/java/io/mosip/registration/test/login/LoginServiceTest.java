package io.mosip.registration.test.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.mosip.registration.audit.AuditManagerSerivceImpl;
import io.mosip.registration.constants.AuditEvent;
import io.mosip.registration.constants.Components;
import io.mosip.registration.dao.AppAuthenticationDAO;
import io.mosip.registration.dao.AppAuthenticationDetails;
import io.mosip.registration.dao.RegistrationCenterDAO;
import io.mosip.registration.dao.ScreenAuthorizationDAO;
import io.mosip.registration.dao.ScreenAuthorizationDetails;
import io.mosip.registration.dao.UserDetailDAO;
import io.mosip.registration.dto.AuthorizationDTO;
import io.mosip.registration.dto.RegistrationCenterDetailDTO;
import io.mosip.registration.dto.UserDTO;
import io.mosip.registration.entity.RegistrationCenter;
import io.mosip.registration.entity.UserDetail;
import io.mosip.registration.repositories.AppAuthenticationRepository;
import io.mosip.registration.repositories.RegistrationCenterRepository;
import io.mosip.registration.repositories.ScreenAuthorizationRepository;
import io.mosip.registration.repositories.UserDetailRepository;
import io.mosip.registration.service.login.impl.LoginServiceImpl;

public class LoginServiceTest {

	@Mock
	private AuditManagerSerivceImpl auditFactory;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@InjectMocks
	private LoginServiceImpl loginServiceImpl;

	@Mock
	private AppAuthenticationRepository appAuthenticationRepository;

	@Mock
	private AppAuthenticationDAO appAuthenticationDAO;

	@Mock
	private UserDetailRepository userDetailRepository;

	@Mock
	private UserDetailDAO userDetailDAO;

	@Mock
	private RegistrationCenterRepository registrationCenterRepository;

	@Mock
	private RegistrationCenterDAO registrationCenterDAO;
	
	@Mock
	private ScreenAuthorizationRepository screenAuthorizationRepository;

	@Mock
	private ScreenAuthorizationDAO screenAuthorizationDAO;
	
	@Before
	public void initialize() throws IOException, URISyntaxException {
		doNothing().when(auditFactory).audit(Mockito.any(AuditEvent.class), Mockito.any(Components.class),
				Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	public void getModesOfLoginTest() {

		List<AppAuthenticationDetails> loginList = new ArrayList<AppAuthenticationDetails>();
		Set<String> roleSet = new HashSet<>();
		roleSet.add("OFFICER");
		Mockito.when(appAuthenticationRepository.findByIsActiveTrueAndAppAuthenticationMethodIdProcessIdAndAppAuthenticationMethodIdRoleCodeInOrderByMethodSequence("LOGIN", roleSet)).thenReturn(loginList);

		List<String> modes = new ArrayList<>();
		loginList.stream().map(loginMethod -> loginMethod.getAppAuthenticationMethodId().getAuthMethodCode()).collect(Collectors.toList());

		Mockito.when(appAuthenticationRepository.findByIsActiveTrueAndAppAuthenticationMethodIdProcessIdAndAppAuthenticationMethodIdRoleCodeInOrderByMethodSequence("LOGIN",roleSet)).thenReturn(loginList);
		
		Mockito.when(appAuthenticationDAO.getModesOfLogin("LOGIN",roleSet)).thenReturn(modes);
		assertEquals(modes,loginServiceImpl.getModesOfLogin("LOGIN",roleSet));
	}

	@Test
	public void getUserDetailTest() {

		UserDetail userDetail = new UserDetail();
		userDetail.setId("mosip");
		List<UserDetail> userDetailList = new ArrayList<UserDetail>();
		userDetailList.add(userDetail);
		Mockito.when(userDetailRepository.findByIdIgnoreCaseAndIsActiveTrue(Mockito.anyString()))
				.thenReturn(userDetailList);
		
		Mockito.when(userDetailDAO.getUserDetail(Mockito.anyString())).thenReturn(userDetail);
		
		UserDTO userDTO = new UserDTO();
		userDTO.setId(userDetail.getId());
		
		assertEquals(userDTO,loginServiceImpl.getUserDetail("mosip"));		
	}

	@Test
	public void getRegistrationCenterDetailsTest() {

		RegistrationCenter registrationCenter = new RegistrationCenter();

		RegistrationCenterDetailDTO centerDetailDTO = new RegistrationCenterDetailDTO();
		Optional<RegistrationCenter> registrationCenterList = Optional.of(registrationCenter);
		Mockito.when(registrationCenterRepository.findByIsActiveTrueAndRegistartionCenterIdIdAndRegistartionCenterIdLangCode(Mockito.anyString(),Mockito.anyString()))
				.thenReturn(registrationCenterList);
		
		Mockito.when(registrationCenterDAO.getRegistrationCenterDetails(Mockito.anyString(),Mockito.anyString())).thenReturn(centerDetailDTO);
		
		assertEquals(centerDetailDTO,loginServiceImpl.getRegistrationCenterDetails("mosip","eng"));
	}


	@Test
	public void getScreenAuthorizationDetailsTest() {

		Set<ScreenAuthorizationDetails> authorizationList = new HashSet<>();
		List<String> roleList = new ArrayList<>();
		Mockito.when(screenAuthorizationRepository
				.findByScreenAuthorizationIdRoleCodeInAndIsPermittedTrueAndIsActiveTrue(roleList))
				.thenReturn(authorizationList);
		AuthorizationDTO authorizationDTO = new AuthorizationDTO();
		Mockito.when(screenAuthorizationDAO.getScreenAuthorizationDetails(roleList)).thenReturn(authorizationDTO);
		assertNotNull(loginServiceImpl.getScreenAuthorizationDetails(roleList));

	}

	@Test
	public void updateLoginParamsTest() {
		doNothing().when(auditFactory).audit(Mockito.any(AuditEvent.class), Mockito.any(Components.class),
				Mockito.anyString(), Mockito.anyString());
		doNothing().when(userDetailDAO).updateLoginParams(Mockito.any(UserDetail.class));
		
		UserDTO userDTO = new UserDTO();
		userDTO.setId("mosip");
		userDTO.setUnsuccessfulLoginCount(0);
		userDTO.setLastLoginDtimes(new Timestamp(System.currentTimeMillis()));
		userDTO.setLastLoginMethod("PWD");
		userDTO.setUserlockTillDtimes(new Timestamp(System.currentTimeMillis()));
		
		UserDetail userDetail = new UserDetail();
		userDetail.setId(userDTO.getId());
		userDetail.setUnsuccessfulLoginCount(userDTO.getUnsuccessfulLoginCount());
		userDetail.setLastLoginDtimes(new Timestamp(System.currentTimeMillis()));
		userDetail.setLastLoginMethod(userDTO.getLastLoginMethod());
		userDetail.setUserlockTillDtimes(userDTO.getUserlockTillDtimes());
		
		Mockito.when(userDetailDAO.getUserDetail(userDTO.getId())).thenReturn(userDetail);
		
		
		loginServiceImpl.updateLoginParams(userDTO);
	}
}
