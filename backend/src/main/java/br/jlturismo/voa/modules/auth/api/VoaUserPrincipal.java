package br.jlturismo.voa.modules.auth.api;

import br.jlturismo.voa.modules.auth.domain.PapelUsuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class VoaUserPrincipal implements UserDetails {

    private final UUID id;
    private final UUID organizacaoId;
    private final String email;
    private final Set<PapelUsuario> papeis;

    public VoaUserPrincipal(UUID id, UUID organizacaoId, String email, Set<PapelUsuario> papeis) {
        this.id = id;
        this.organizacaoId = organizacaoId;
        this.email = email;
        this.papeis = papeis;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return papeis.stream()
                .map(p -> new SimpleGrantedAuthority("ROLE_" + p.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
