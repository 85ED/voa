package br.jlturismo.voa.modules.core.config;

import br.jlturismo.voa.modules.auth.infrastructure.UsuarioRepository;
import br.jlturismo.voa.modules.bilhetes.infrastructure.BilheteRepository;
import br.jlturismo.voa.modules.pagamentos.infrastructure.PagamentoRepository;
import br.jlturismo.voa.modules.promocoes.infrastructure.PromocaoEntity;
import br.jlturismo.voa.modules.promocoes.infrastructure.PromocaoRepository;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.ClienteEntity;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.ClienteRepository;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.SolicitacaoRepository;
import br.jlturismo.voa.modules.vendas.infrastructure.VendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoDataSeedRunnerTest {

    @Mock
    ClienteRepository clienteRepository;

    @Mock
    SolicitacaoRepository solicitacaoRepository;

    @Mock
    PromocaoRepository promocaoRepository;

    @Mock
    VendaRepository vendaRepository;

    @Mock
    PagamentoRepository pagamentoRepository;

    @Mock
    BilheteRepository bilheteRepository;

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    VoaProperties properties;

    DemoDataSeedRunner runner;

    @BeforeEach
    void setUp() {
        runner = new DemoDataSeedRunner(
                clienteRepository,
                solicitacaoRepository,
                promocaoRepository,
                vendaRepository,
                pagamentoRepository,
                bilheteRepository,
                usuarioRepository,
                properties);
    }

    @Test
    void inserePromocoesPorSlugQuandoAusentes() throws Exception {
        when(promocaoRepository.findByOrganizacaoIdAndSlug(eq(OrgConstants.JL_ORG_ID), any()))
                .thenReturn(Optional.empty());
        when(clienteRepository.search(eq(OrgConstants.JL_ORG_ID), any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(properties.seed()).thenReturn(new VoaProperties.Seed("admin@test.local", "x"));
        when(usuarioRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());

        runner.run(new DefaultApplicationArguments());

        ArgumentCaptor<PromocaoEntity> captor = ArgumentCaptor.forClass(PromocaoEntity.class);
        verify(promocaoRepository, times(3)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(PromocaoEntity::getSlug)
                .containsExactlyInAnyOrder("demo-rio-de-janeiro", "demo-buenos-aires", "demo-lisboa");
    }

    @Test
    void naoDuplicaPromocoesExistentes() throws Exception {
        when(promocaoRepository.findByOrganizacaoIdAndSlug(eq(OrgConstants.JL_ORG_ID), eq("demo-rio-de-janeiro")))
                .thenReturn(Optional.of(new PromocaoEntity()));
        when(promocaoRepository.findByOrganizacaoIdAndSlug(eq(OrgConstants.JL_ORG_ID), eq("demo-buenos-aires")))
                .thenReturn(Optional.of(new PromocaoEntity()));
        when(promocaoRepository.findByOrganizacaoIdAndSlug(eq(OrgConstants.JL_ORG_ID), eq("demo-lisboa")))
                .thenReturn(Optional.of(new PromocaoEntity()));
        when(clienteRepository.search(eq(OrgConstants.JL_ORG_ID), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new ClienteEntity())));

        runner.run(new DefaultApplicationArguments());

        verify(promocaoRepository, never()).save(any());
    }
}
