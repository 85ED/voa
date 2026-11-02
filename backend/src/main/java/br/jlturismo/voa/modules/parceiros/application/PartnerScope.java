package br.jlturismo.voa.modules.parceiros.application;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.auth.domain.PapelUsuario;

import java.util.UUID;

public final class PartnerScope {

    private PartnerScope() {}

    public static UUID effectiveParceiroFilter(VoaUserPrincipal principal, UUID requestedParceiroId) {
        if (principal.hasPapel(PapelUsuario.PARTNER)) {
            UUID own = principal.getParceiroId();
            if (own == null) {
                throw new IllegalStateException("Usuário parceiro sem vínculo parceiro_id");
            }
            if (requestedParceiroId != null && !requestedParceiroId.equals(own)) {
                throw new IllegalArgumentException("Acesso negado a dados de outro parceiro");
            }
            return own;
        }
        return requestedParceiroId;
    }

    public static void assertCanAccessParceiro(VoaUserPrincipal principal, UUID parceiroId) {
        if (parceiroId == null) {
            return;
        }
        if (principal.hasPapel(PapelUsuario.PARTNER)) {
            UUID own = principal.getParceiroId();
            if (own == null || !own.equals(parceiroId)) {
                throw new IllegalArgumentException("Acesso negado");
            }
        }
    }
}
