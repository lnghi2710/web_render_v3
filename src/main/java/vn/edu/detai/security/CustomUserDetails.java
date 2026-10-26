package vn.edu.detai.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.edu.detai.entity.User;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    @Getter private final Long id;
    @Getter private final String fullName;
    @Getter private final String roleLabel;
    @Getter private final String initials;
    @Getter private final vn.edu.detai.enums.Role role;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(User u) {
        this.id = u.getId();
        this.fullName = u.getFullName();
        this.role = u.getRole();
        this.roleLabel = u.getRole().getNhan();
        this.initials = vn.edu.detai.util.Names.initials(u.getFullName());
        this.username = u.getUsername();
        this.password = u.getPassword();
        this.enabled = u.isEnabled();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()));
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    /** Tài khoản bị khóa không đăng nhập được (FR01). */
    @Override public boolean isEnabled() { return enabled; }
}
