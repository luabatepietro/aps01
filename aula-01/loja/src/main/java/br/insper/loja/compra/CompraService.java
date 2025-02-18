package br.insper.loja.compra;

import br.insper.loja.evento.EventoService;
import br.insper.loja.produto.Produto;
import br.insper.loja.produto.ProdutoService;
import br.insper.loja.produto.ProdutoController;
import br.insper.loja.usuario.Usuario;
import br.insper.loja.usuario.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.logging.Logger;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompraService {
    private static final Logger logger = Logger.getLogger(CompraService.class.getName());

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EventoService eventoService;

    @Autowired
    private ProdutoService produtoService;

    public List<Compra> getCompras() {
        return compraRepository.findAll();
    }

    public Compra salvarCompra(Compra compra) {

        Usuario usuario = usuarioService.getUsuario(compra.getUsuario());
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }

        compra.setNome(usuario.getNome());
        compra.setDataCompra(LocalDateTime.now());

        List<Produto> produtos = compra.getProdutos().stream()
                .map(produtoId -> {
                    Produto produto = produtoService.buscarProdutoPorId(produtoId);
                    if (produto == null) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado: " + produtoId);
                    }
                    return produto;
                })
                .toList();

        // Calcula o total e verifica o estoque
        double total = 0;
        for (Produto produto : produtos) {
            if (produto.getQuantidade() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto sem estoque: " + produto.getNome());
            }
            produtoService.decrementarQuantidade(produto.getId(), 1);
            total += produto.getPreco();
        }

        compra.setTotal(total);

        eventoService.salvarEvento(usuario.getEmail(), "Compra realizada. Total: R$ " + total);

        return compraRepository.save(compra);
    }

}
