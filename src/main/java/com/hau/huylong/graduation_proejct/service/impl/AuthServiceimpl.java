package com.hau.huylong.graduation_proejct.service.impl;

import com.hau.huylong.graduation_proejct.common.enums.TypeUser;
import com.hau.huylong.graduation_proejct.common.exception.APIException;
import com.hau.huylong.graduation_proejct.common.util.AuthorityUtil;
import com.hau.huylong.graduation_proejct.common.util.JwtTokenUtil;
import com.hau.huylong.graduation_proejct.config.auth.Commons;
import com.hau.huylong.graduation_proejct.entity.auth.*;
import com.hau.huylong.graduation_proejct.entity.hau.Company;
import com.hau.huylong.graduation_proejct.entity.hau.UserInfo;
import com.hau.huylong.graduation_proejct.model.dto.auth.UserDTO;
import com.hau.huylong.graduation_proejct.model.request.SignupRequest;
import com.hau.huylong.graduation_proejct.model.request.TokenRefreshRequest;
import com.hau.huylong.graduation_proejct.model.response.TokenRefreshResponse;
import com.hau.huylong.graduation_proejct.model.response.UserResponse;
import com.hau.huylong.graduation_proejct.repository.auth.*;
import com.hau.huylong.graduation_proejct.repository.hau.CompanyReps;
import com.hau.huylong.graduation_proejct.service.*;
import com.hau.huylong.graduation_proejct.service.mapper.RefreshTokenMapper;
import com.hau.huylong.graduation_proejct.service.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.*;

import static com.hau.huylong.graduation_proejct.common.enums.RoleEnums.Role.USER;

@Service
@Slf4j
public class AuthServiceimpl implements AuthService {
    private final UserReps userReps;
    private final RefreshTokenService refreshToken;
    private final UserMapper userMapper;
    private final JwtTokenUtil tokenUtil;
    private final RefreshTokenReps refreshTokenReps;
    private final RefreshTokenMapper refreshTokenMapper;
    private final UserVerificationService userVerificationService;
    private final EmailService emailService;
    private final UserVerificationReps userVerificationReps;
    private final RoleReps roleReps;
    private final UserService userService;
    private final UserInfoReps userInfoReps;
    private final ResetPasswordTokenService resetPasswordTokenService;
    private final CompanyReps companyReps;
    private final String portFe;

    public AuthServiceimpl(UserReps userReps, RefreshTokenService refreshToken, UserMapper userMapper,
                           JwtTokenUtil tokenUtil, RefreshTokenReps refreshTokenReps, RefreshTokenMapper refreshTokenMapper,
                           UserVerificationService userVerificationService, EmailService emailService,
                           UserVerificationReps userVerificationReps, RoleReps roleReps,
                           UserService userService, UserInfoReps userInfoReps,
                           ResetPasswordTokenService resetPasswordTokenService, CompanyReps companyReps, @Value("${port-fe}") String portFe) {
        this.userReps = userReps;
        this.refreshToken = refreshToken;
        this.userMapper = userMapper;
        this.tokenUtil = tokenUtil;
        this.refreshTokenReps = refreshTokenReps;
        this.refreshTokenMapper = refreshTokenMapper;
        this.userVerificationService = userVerificationService;
        this.emailService = emailService;
        this.userVerificationReps = userVerificationReps;
        this.roleReps = roleReps;
        this.userService = userService;
        this.userInfoReps = userInfoReps;
        this.resetPasswordTokenService = resetPasswordTokenService;
        this.companyReps = companyReps;
        this.portFe = portFe;
    }

    /**
     * Lấy lại access token khi token hết hạn
     * @RequestBody: request
     * @Return: TokenRefreshResponse
     * */
    @Override
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        RefreshToken refresh = refreshTokenReps.findByRefreshToken(requestRefreshToken)
                .orElseThrow(() -> APIException.from(HttpStatus.NOT_FOUND).withMessage("Refresh token not found."));
        User user = userReps.findById(refresh.getUserId())
                .orElseThrow(() -> APIException.from(HttpStatus.NOT_FOUND).withMessage("Username not found."));
        UserInfo userInfo = userInfoReps.findByUserId(user.getId())
                .orElseThrow(() -> APIException.from(HttpStatus.NOT_FOUND).withMessage("Username not found."));

        Set<GrantedAuthority> authorities = new HashSet<>();
        Set<String> roles = new HashSet<>();
        user.getRoles().forEach(r -> roles.add(r.getCode()));
        roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));

        CustomUser customUser = new CustomUser(user.getUsername(), user.getPassword(), true,
                true, true, true, authorities);
        customUser.setId(user.getId());
        customUser.setFullName(userInfo.getFullName());
        customUser.setAvatar(userInfo.getAvatar());

        String accessToken = tokenUtil.generateToken(customUser.getUsername(), roles,
                customUser.getId(), customUser.getFullName(), customUser.getAvatar(), customUser.getType());

        return refreshToken.findByRefreshToken(requestRefreshToken)
                .map(refreshToken::verifyExpiration).map(u -> {
                    if (!tokenUtil.validateJwtToken(u.getAccessToken())) {
                        u.setAccessToken(accessToken);
                        u.setStatus(true);
                        refreshTokenMapper.copy(u, refresh);
                        refreshTokenReps.save(refresh);
                        return new TokenRefreshResponse(accessToken, u.getRefreshToken());
                    }
                    return new TokenRefreshResponse(u.getAccessToken(), u.getRefreshToken());
                }).orElseThrow(() -> APIException.from(HttpStatus.FORBIDDEN).withMessage("Refresh token is not in database!"));
    }

    /**
     * Lấy thông tin tài khoản
     * @Return: UserResponse
     * */
    @Override
    public UserResponse getInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser customUser = (CustomUser) authentication.getPrincipal();
        Optional<User> userOpt = userService.findByUsernameAndStatus(customUser.getUsername(), User.Status.ACTIVE);


        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserInfo userInfo = userInfoReps.findByUserId(user.getId())
                    .orElseThrow(() -> APIException.from(HttpStatus.NOT_FOUND).withMessage("Username not found."));

            Set<String> roles = AuthorityUtil.authorityListToSet(customUser.getAuthorities());

            return UserResponse.builder()
                    .id(userInfo.getUserId())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .status(user.getMapStatus().get(user.getStatus()))
                    .type(user.getType().name())
                    .address(userInfo.getAddress())
                    .avatar(userInfo.getAvatar())
                    .town(userInfo.getTown())
                    .authorities(roles)
                    .fullName(userInfo.getFullName())
                    .dateOfBirth(userInfo.getDateOfBirth())
                    .gender(user.getMapGender().get(userInfo.getGender()))
                    .phoneNumber(userInfo.getPhoneNumber())
                    .marriageStatus(userInfo.getMarriageStatus())
                    .build();
        }
        return null;
    }

    /**
     * Đăng xuất
     *
     * @Param: request
     * @Param: response
     * @Return: void
     * */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String accessToken = parseJwt(request);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextLogoutHandler contextLogoutHandler = new SecurityContextLogoutHandler();
        if (authentication != null) {
            if (authentication.isAuthenticated()) {
                CustomUser user = (CustomUser) authentication.getPrincipal();
                contextLogoutHandler.logout(request, response, authentication);
                if (contextLogoutHandler.isInvalidateHttpSession()) {
                    authentication.setAuthenticated(false);
                    Optional<RefreshToken> refreshTokens =
                            refreshTokenReps.findByUserIdAndAccessToken(user.getId(), accessToken);
                    if (refreshTokens.isPresent()) {
                        refreshTokenReps.delete(refreshTokens.get());
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                }
            }
        }
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader(Commons.AUTH_HEADER);
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7, headerAuth.length());
        }
        return null;
    }

    /**
     * Đăng kí
     *
     * @RequestBody: signupRequest
     * @Param: request
     * @Return: UserDTO
     * */
    @Override
    public UserDTO signup(SignupRequest signupRequest, HttpServletRequest request) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        validRegister(signupRequest);

        //disable new user before activation
        User user = new User();
        user.setStatus(User.Status.WAITING);
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());

        if (signupRequest.getPassword() != null && signupRequest.getConfirmPassword() != null
                && Objects.equals(signupRequest.getPassword(), signupRequest.getConfirmPassword())) {
            user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        }

        if (signupRequest.getType() != null) {
            if (signupRequest.getType().equalsIgnoreCase("employer")) {
                user.setType(TypeUser.EMPLOYER);
            } else {
                user.setType(TypeUser.CANDIDATE);
            }
        } else {
            user.setType(TypeUser.CANDIDATE);
        }

        //set role user for all account when register
        Set<Role> roles = new HashSet<>();
        Optional<Role> role = roleReps.findByCode(USER.name());
        role.ifPresent(roles::add);
        user.setRoles(roles);

        Optional<UserDTO> userDTO = Optional.of(userMapper.to(userReps.save(user)));
        //create and save verification code if the user is saved
        userDTO.ifPresent(u -> {
            try {
                String code = UUID.randomUUID().toString();
                userVerificationService.save(userDTO.get().getId(), code);

                if (u.getType() != null && u.getType().equalsIgnoreCase("employer")) {
                    // thông tin cty
                    Company company = new Company();
                    company.setUserId(u.getId());
                    companyReps.save(company);

                    // thông tin người liên hệ
                    UserInfo userInfo = new UserInfo();
                    userInfo.setCompanyId(company.getId());
                    userInfo.setUserId(company.getUserId());
                    userInfo.setType(TypeUser.EMPLOYER);
                    userInfoReps.save(userInfo);
                } else {
                    UserInfo userInfo = new UserInfo();
                    userInfo.setUserId(u.getId());
                    userInfo.setType(TypeUser.CANDIDATE);
                    userInfoReps.save(userInfo);
                }

                //send verify to email
                emailService.sendMail(u, request);
            } catch (Exception e) {
                log.error(e.getMessage());
                throw APIException.from(HttpStatus.BAD_REQUEST).withMessage(e.getMessage());
            }
        });

        return userDTO.get();
    }


    /**
     * Valid register account
     * @RequestBody: SignupRequest
     * */
    private void validRegister(SignupRequest signupRequest) {
        if (userReps.existsByUsername(signupRequest.getUsername())) {
            throw APIException.from(HttpStatus.BAD_REQUEST).withMessage("Username is existed.");
        }

        if (userReps.existsByEmail(signupRequest.getEmail())) {
            throw APIException.from(HttpStatus.BAD_REQUEST).withMessage("Email is existed.");
        }

        if (Objects.nonNull(signupRequest.getPassword())
                && Objects.nonNull(signupRequest.getConfirmPassword())) {
            if (!Objects.equals(signupRequest.getPassword(), signupRequest.getConfirmPassword())) {
                throw APIException.from(HttpStatus.BAD_REQUEST).withMessage("Password must match.");
            }
        }
    }

    /**
     * xác nhận tài khoản
     *
     * @Param: code
     * @Return: boolean
     * */
    @Override
    public boolean verifyAccount(String code) {
        Optional<UserVerification> userVerification = userVerificationService.findByCode(code);
        if (userVerification.isPresent()) {
            UserVerification verification = userVerification.get();
            if (!verification.getStatus().equals(UserVerification.Status.ACTIVE)) {
                verification.setStatus(UserVerification.Status.ACTIVE);
                verification.setType(UserVerification.Type.email);
                Optional<User> user = userReps.findById(verification.getUserId());
                if (user.isPresent()) {
                    //if the user account is not actived
                    if (!user.get().getStatus().equals(User.Status.ACTIVE)) {
                        //get time current
                        Date date = Date.from(Instant.now());
                        //check if the code is expired
                        if (userVerification.get().getExpireDate().before(date)) {
                            throw APIException.from(HttpStatus.BAD_REQUEST)
                                    .withMessage("Your verification code has expired");
                        } else {
                            //the code is valid
                            //active the user account
                            user.get().setStatus(User.Status.ACTIVE);
                            userReps.save(user.get());
                            return true;
                        }
                    } else {
                        return false;
                    }
                }
                userVerificationReps.save(verification);
            }
        } else {
            throw APIException.from(HttpStatus.BAD_REQUEST)
                    .withMessage("Your verification code is invalid.");
        }
        return false;
    }

    @Override
    public void forgotPassword(String email, HttpServletRequest request) throws MessagingException {
        UserDTO userDto = userReps.findByEmail(email).map(userMapper::to).orElseThrow(() ->
                APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy người dùng với email: " + email));
        String token = UUID.randomUUID().toString();
        if (resetPasswordTokenService.createTokenResetPassword(userDto.getId(), token)) {
            String url = ServletUriComponentsBuilder.fromRequestUri(request)
                    .replacePath(request.getPathInfo())
                    .build()
                    .toUriString();

            Map<String, Object> variables = new HashMap<>();
            String sub = "Email Reset Password !";

            String start = org.apache.commons.lang3.StringUtils.join("<a href=", splitUrl(url), portFe , "/#/pages/update-password?token=", token, ">");
            String end = "</a>";
            String activeUrl = org.apache.commons.lang3.StringUtils.join("Click ", start, " here ", end, " to reset your password !");
            variables.put("url", activeUrl);
            emailService.sendEmail(sub, variables, null, null, userDto);
        }
    }
    private String splitUrl(String url) {
        return url.replaceAll("[0-9]", "");
    }

    @Override
    public boolean updatePassword(String newPassword, String token, String oldPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (token != null) {
            String rs = resetPasswordTokenService.validatePasswordResetToken(token);

            if (rs != null) {
                throw APIException.from(HttpStatus.BAD_REQUEST).withMessage(rs);
            }

            Optional<ResetPasswordToken> resetPasswordToken = resetPasswordTokenService.findByToken(token);
            if (resetPasswordToken.isPresent()) {
                Optional<User> userOptional = userReps.findById(resetPasswordToken.get().getUserId());
                if (userOptional.isPresent()) {
                    if (newPassword.equals(oldPassword)) {
                        userOptional.get().setPassword(passwordEncoder.encode(newPassword));
                    } else {
                        throw APIException.from(HttpStatus.BAD_REQUEST).withMessage("Mật khẩu không trùng khớp !");
                    }

                    User user = userReps.save(userOptional.get());
                    return user.getId() > 0;
                }
            }
        }
        return false;
    }
}
