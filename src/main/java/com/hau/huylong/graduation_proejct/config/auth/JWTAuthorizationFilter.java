package com.hau.huylong.graduation_proejct.config.auth;

import com.hau.huylong.graduation_proejct.common.exception.APIException;
import com.hau.huylong.graduation_proejct.common.util.JsonUtil;
import com.hau.huylong.graduation_proejct.common.util.StringUtil;
import com.hau.huylong.graduation_proejct.entity.auth.CustomUser;
import com.hau.huylong.graduation_proejct.entity.auth.User;
import com.hau.huylong.graduation_proejct.service.UserService;
import io.jsonwebtoken.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.CollectionUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static com.hau.huylong.graduation_proejct.config.auth.Commons.AUTH_HEADER;


@Log4j2
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
    private final UserService userService;

    public JWTAuthorizationFilter(AuthenticationManager authenticationManager, UserService userService) {
        super(authenticationManager);
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String token = request.getHeader(AUTH_HEADER);
        if (StringUtil.isEmpty(token) || !token.startsWith(Commons.TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

//        String method = request.getMethod();
//        String path = request.getServletPath();
        UsernamePasswordAuthenticationToken authentication = getAuthentication(token);
        if (authentication == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Thông tin xác thực không hợp lệ");
            return;
        }

//        String username = authentication.getName();
//        if (!checkPermission(username, path)) {
//            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Path không hợp lệ");
//            return;
//        }

        log.info("\n\n ===>>> When before update SecurityContextHolder. Authentication: {} \n\n",
                JsonUtil.toJson(SecurityContextHolder.getContext().getAuthentication()));
        log.info("\n\n ===>>> Updating SecurityContextHolder to contain Authentication ... \n\n");

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("\n\n ===>>> When after update SecurityContextHolder. Authentication: {} \n\n",
                JsonUtil.toJson(SecurityContextHolder.getContext().getAuthentication()));
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .requireIssuer(Commons.TOKEN_ISSUER)
                    .setSigningKey(Commons.TOKEN_SECRET.getBytes())
                    .parseClaimsJws(token.replace(Commons.TOKEN_PREFIX, ""));

            String username = claims.getBody().getSubject();

            List<GrantedAuthority> grantedAuthorities = getGrantedAuthorities(username);

            Integer userId = claims.getBody().get("uid", Integer.class);
            String fullname = claims.getBody().get("fullname", String.class);
            String avatar = claims.getBody().get("avatar", String.class);
            String type = claims.getBody().get("type", String.class);

            UserDetails userDetails = new CustomUser(username,
                    "no-password",
                    true,
                    true,
                    true,
                    true,
                    grantedAuthorities,
                    userId,
                    fullname,
                    avatar, type);
            return new UsernamePasswordAuthenticationToken(userDetails, null, grantedAuthorities);

        } catch (SignatureException e) {
            log.error("===>>> Verify signature failed. Don't trust the JWT!!!");
            return null;
        } catch (ExpiredJwtException e) {
            log.error("===>>> JWT is expired!!!");
            return null;
        } catch (MissingClaimException e) {
            log.error("===>>> The required claim is not present!!!");
            return null;
        } catch (IncorrectClaimException e) {
            log.error("===>>> The required claim has the wrong value!!!");
            return null;
        } catch (APIException e) {
            log.error(e.getMessage());
            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private List<GrantedAuthority> getGrantedAuthorities(String username) throws APIException {
        Set<String> roles = getRoles(username);
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        if (!CollectionUtils.isEmpty(roles)) {
            roles.forEach(auth -> {
                GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + auth);
                grantedAuthorities.add(authority);
            });
        }

        return grantedAuthorities;
    }

    /*private boolean checkPermission(String username, String path) throws APIException {
        if (StringUtil.isEmpty(username)) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("===>>> Tài khoản không hợp lệ!");
        }
        if (StringUtil.isEmpty(path)) {
            return false;
        }

        User user = userService.findByUsernameAndStatus(username, User.Status.ACTIVE)
                .orElseThrow(() -> APIException.from(HttpStatus.NOT_FOUND).withMessage("===>>> Tài khoản không tồn tại!"));

        if (CollectionUtils.isEmpty(user.getGroups())) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("===>>> Tài khoản không hợp lệ!");
        }

        List<Role> roles = new ArrayList<>();
        user.getRoles().forEach(r -> {
            if (r.getPath().equals(path)) {
                roles.add(r);
            }
        });

        boolean valid = false;
        if (!roles.isEmpty()) {
            valid = user.getGroups()
                    .stream()
                    .anyMatch(ug -> ug.getGroup().getRoles().containsAll(roles));
        }
        return valid;
    }*/

    /*private Set<String> getRoles(String username, String method, String path) throws APIException {
        if (StringUtil.isEmpty(username)) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("===>>> Tài khoản không hợp lệ!");
        }
        User user = userService.findByUsernameAndStatus(username, User.Status.ACTIVE)
                .orElseThrow(() -> APIException.from(HttpStatus.NOT_FOUND)
                        .withMessage("===>>> Tài khoản không tồn tại!"));
        Set<String> roles = new HashSet<String>();
        roles.add(Commons.DEFAULT_ROLE);

        if (!CollectionUtils.isEmpty(user.getGroups())) {
            List<Group> groups = user.getGroups()
                    .stream()
                    .map(UserGroup::getGroup)
                    .collect(Collectors.toList());

            groups.forEach(g -> roles.addAll(getRoles(g, method, path)));
        }
        return roles;
    }*/

    /*private Set<String> getRoles(Group group, String method, String path) {
        Set<String> roles = new HashSet<>();
        if (!CollectionUtils.isEmpty(group.getRoles())) {
            group.getRoles().forEach(r -> {
                if (Objects.nonNull(path) && Objects.nonNull(method)) {
                    if (Objects.equals(r.getPath(), path)) {
                        roles.add(r.getCode());
                    }
                }
            });
        }
        return roles;
    }*/

    private Set<String> getRoles(String username) {
        Set<String> roles = new HashSet<>();
        Optional<User> user = userService.findByUsernameAndStatus(username, User.Status.ACTIVE);
        if (user.isPresent()) {
            if (!CollectionUtils.isEmpty(user.get().getRoles())) {
                user.get().getRoles().forEach(r -> roles.add(r.getCode()));
            }
        }
        return roles;
    }
}
